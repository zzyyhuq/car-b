package com.example.beauty.common.cache;

import com.example.beauty.common.exception.BusinessException;
import com.example.beauty.module.cache.vo.CacheKeyVO;
import com.example.beauty.module.cache.vo.CacheOverviewVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis 业务缓存 + 管理端缓存查询。
 *
 * <p>设计要点：
 * <ul>
 *   <li>Redis 不可用时自动降级：直接执行 loader 查库，接口不报错；</li>
 *   <li>失败后 30 秒内不再重试，避免每个请求都等连接超时；</li>
 *   <li>管理端只能查看/删除 {@link CacheKeys#PREFIX} 开头的 key，不碰其它数据。</li>
 * </ul>
 */
@Slf4j
@Service
public class CacheService {

    /** Redis 不可用后的退避时间：这段时间内直接用数据库 */
    private static final long DOWN_BACKOFF_MILLIS = 30_000L;
    /** SCAN 每次建议返回的 key 数量，避免一次扫太多阻塞 Redis */
    private static final int SCAN_BATCH = 200;
    private static final int DEFAULT_KEY_LIMIT = 100;
    private static final int MAX_KEY_LIMIT = 500;

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    /** 大于当前时间表示处于降级期 */
    private volatile long redisDownUntil = 0L;
    /** 降级期间只打一条告警，避免日志刷屏 */
    private volatile boolean downLogged = false;

    public CacheService(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    /**
     * 读缓存，未命中则查库并回填。Redis 异常时直接查库，不影响业务。
     */
    public <T> T get(String key, TypeReference<T> type, Duration ttl, Supplier<T> loader) {
        if (isAvailable()) {
            String json;
            try {
                json = redis.opsForValue().get(key);
                markUp();
            } catch (DataAccessException e) {
                markDown("读取缓存失败 key=" + key, e);
                json = null;
            } catch (Exception e) {
                log.warn("读取缓存异常 key={}：{}", key, e.getMessage());
                json = null;
            }
            if (json != null) {
                try {
                    return objectMapper.readValue(json, type);
                } catch (Exception e) {
                    // 缓存结构变了（比如实体新增字段），删掉旧值重新加载，不把 Redis 判成故障
                    log.warn("缓存反序列化失败，删除旧值 key={}：{}", key, e.getMessage());
                    evictQuietly(key);
                }
            }
        }
        T value = loader.get();
        if (value != null && isAvailable()) {
            try {
                redis.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
                markUp();
            } catch (DataAccessException e) {
                markDown("写入缓存失败 key=" + key, e);
            } catch (Exception e) {
                log.warn("缓存序列化失败 key={}：{}", key, e.getMessage());
            }
        }
        return value;
    }

    /** 删除指定 key（业务写操作后精准失效） */
    public void evict(String... keys) {
        if (!isAvailable() || keys.length == 0) {
            return;
        }
        try {
            redis.delete(List.of(keys));
            markUp();
        } catch (Exception e) {
            markDown("删除缓存失败", e);
        }
    }

    /** 按前缀批量失效，用于公告/活动按 limit 生成多个 key 的场景 */
    public void evictByPrefix(String prefix) {
        if (!isAvailable()) {
            return;
        }
        List<String> keys = scanKeys(prefix);
        if (keys.isEmpty()) {
            return;
        }
        try {
            redis.delete(keys);
            markUp();
        } catch (Exception e) {
            markDown("按前缀删除缓存失败 prefix=" + prefix, e);
        }
    }

    /** 管理端概览：Redis 连接状态与运行信息 */
    public CacheOverviewVO overview() {
        CacheOverviewVO vo = new CacheOverviewVO();
        if (!isAvailable()) {
            vo.setConnected(false);
            vo.setMessage("Redis 未连接，缓存已自动降级为直接查库");
            return vo;
        }
        try {
            Properties info = redis.execute(
                    (RedisCallback<Properties>) connection -> connection.serverCommands().info());
            if (info == null) {
                throw new IllegalStateException("Redis INFO 返回为空");
            }
            vo.setConnected(true);
            vo.setVersion(info.getProperty("redis_version"));
            vo.setUsedMemory(info.getProperty("used_memory_human"));
            vo.setUptimeSeconds(parseLong(info.getProperty("uptime_in_seconds")));

            long hits = parseLong(info.getProperty("keyspace_hits"));
            long misses = parseLong(info.getProperty("keyspace_misses"));
            vo.setHitRate(hits + misses == 0 ? 0D : (double) hits / (hits + misses));

            Long dbSize = redis.execute(
                    (RedisCallback<Long>) connection -> connection.serverCommands().dbSize());
            vo.setKeyCount(dbSize == null ? 0 : dbSize);
            markUp();
        } catch (Exception e) {
            markDown("读取 Redis 运行信息失败", e);
            vo = new CacheOverviewVO();
            vo.setConnected(false);
            vo.setMessage("Redis 未连接，缓存已自动降级为直接查库");
        }
        return vo;
    }

    /** 管理端：按前缀查询 key（支持简写，如 notice 会补全为 beauty:notice） */
    public List<CacheKeyVO> keys(String prefix, Integer limit) {
        ensureAvailable();
        int size = limit == null || limit <= 0
                ? DEFAULT_KEY_LIMIT
                : Math.min(limit, MAX_KEY_LIMIT);
        String normalized = normalizePrefix(prefix);
        List<CacheKeyVO> result = new ArrayList<>();
        try {
            ScanOptions options = ScanOptions.scanOptions()
                    .match(normalized + "*")
                    .count(SCAN_BATCH)
                    .build();
            try (Cursor<String> cursor = redis.scan(options)) {
                while (cursor.hasNext() && result.size() < size) {
                    result.add(toVO(cursor.next()));
                }
            }
            markUp();
        } catch (DataAccessException e) {
            throw unavailable(e, "扫描缓存失败");
        }
        result.sort(Comparator.comparing(CacheKeyVO::getKey));
        return result;
    }

    /** 管理端：删除单个业务缓存 key */
    public void deleteKey(String key) {
        if (key == null || key.isBlank()) {
            throw new BusinessException(400, "缺少缓存 key");
        }
        if (!key.startsWith(CacheKeys.PREFIX)
                || key.contains("*") || key.contains("?") || key.contains("[")) {
            throw new BusinessException(400, "只能删除 beauty: 开头的缓存，且不支持通配符");
        }
        ensureAvailable();
        try {
            redis.delete(key);
            markUp();
        } catch (DataAccessException e) {
            throw unavailable(e, "删除缓存失败 key=" + key);
        }
    }

    /** 管理端：清空所有业务缓存，返回删除数量 */
    public long clearBusiness() {
        ensureAvailable();
        long total = 0;
        try {
            List<String> batch = new ArrayList<>(SCAN_BATCH);
            ScanOptions options = ScanOptions.scanOptions()
                    .match(CacheKeys.PREFIX + "*")
                    .count(SCAN_BATCH)
                    .build();
            try (Cursor<String> cursor = redis.scan(options)) {
                while (cursor.hasNext()) {
                    batch.add(cursor.next());
                    if (batch.size() >= SCAN_BATCH) {
                        total += deleteBatch(batch);
                    }
                }
            }
            total += deleteBatch(batch);
            markUp();
        } catch (DataAccessException e) {
            throw unavailable(e, "清空业务缓存失败");
        }
        return total;
    }

    private long deleteBatch(List<String> batch) {
        if (batch.isEmpty()) {
            return 0;
        }
        Long removed = redis.delete(batch);
        batch.clear();
        return removed == null ? 0 : removed;
    }

    private CacheKeyVO toVO(String key) {
        DataType type = redis.type(key);
        Long ttl = redis.getExpire(key, TimeUnit.SECONDS);
        CacheKeyVO vo = new CacheKeyVO();
        vo.setKey(key);
        vo.setType(type == null ? "unknown" : type.code());
        vo.setTtl(ttl == null ? -2 : ttl);
        vo.setDescription(describe(key));
        return vo;
    }

    private void evictQuietly(String key) {
        try {
            redis.delete(key);
        } catch (Exception ignored) {
            // 清不掉就等 TTL 自然过期，不影响本次请求
        }
    }

    private List<String> scanKeys(String prefix) {
        List<String> keys = new ArrayList<>();
        try (Cursor<String> cursor = redis.scan(ScanOptions.scanOptions()
                .match(prefix + "*")
                .count(SCAN_BATCH)
                .build())) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
            markUp();
        } catch (Exception e) {
            markDown("扫描缓存 key 失败 prefix=" + prefix, e);
        }
        return keys;
    }

    private void ensureAvailable() {
        if (!isAvailable()) {
            throw new BusinessException(503, "Redis 未连接，请先启动 Redis 再管理缓存");
        }
    }

    private boolean isAvailable() {
        return System.currentTimeMillis() >= redisDownUntil;
    }

    private void markUp() {
        if (downLogged) {
            log.info("Redis 缓存已恢复");
        }
        redisDownUntil = 0L;
        downLogged = false;
    }

    private void markDown(String action, Exception e) {
        redisDownUntil = System.currentTimeMillis() + DOWN_BACKOFF_MILLIS;
        if (!downLogged) {
            downLogged = true;
            log.warn("{}，{} 秒内缓存降级为直接查库：{}",
                    action, DOWN_BACKOFF_MILLIS / 1000, e.getMessage());
        } else {
            log.debug("{}：{}", action, e.getMessage());
        }
    }

    private BusinessException unavailable(DataAccessException e, String action) {
        markDown(action, e);
        return new BusinessException(503, "Redis 未连接，请先启动 Redis 再管理缓存");
    }

    private String normalizePrefix(String prefix) {
        String value = prefix == null ? "" : prefix.trim();
        if (value.isEmpty() || "beauty".equals(value)) {
            return CacheKeys.PREFIX;
        }
        if (!value.startsWith(CacheKeys.PREFIX)) {
            value = CacheKeys.PREFIX + value;
        }
        while (value.endsWith("*")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String describe(String key) {
        if (key.startsWith(CacheKeys.SERVICE_TYPE_LIST)) {
            return "上架服务项目列表";
        }
        if (key.startsWith(CacheKeys.NOTICE_LIST)) {
            return "已发布公告列表";
        }
        if (key.startsWith(CacheKeys.ACTIVITY_LIST)) {
            return "进行中活动列表";
        }
        return "业务缓存";
    }

    private static long parseLong(String value) {
        try {
            return value == null ? 0L : Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
