package com.example.beauty.module.servicetype.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.beauty.common.PageResult;
import com.example.beauty.common.cache.CacheKeys;
import com.example.beauty.common.cache.CacheService;
import com.example.beauty.common.exception.BusinessException;
import com.example.beauty.module.servicetype.dto.ServiceTypeQuery;
import com.example.beauty.module.servicetype.dto.ServiceTypeSaveDTO;
import com.example.beauty.module.servicetype.entity.ServiceType;
import com.example.beauty.module.servicetype.mapper.ServiceTypeMapper;
import com.example.beauty.module.servicetype.service.ServiceTypeService;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ServiceTypeServiceImpl implements ServiceTypeService {

    private final ServiceTypeMapper serviceTypeMapper;
    private final CacheService cacheService;

    public ServiceTypeServiceImpl(ServiceTypeMapper serviceTypeMapper, CacheService cacheService) {
        this.serviceTypeMapper = serviceTypeMapper;
        this.cacheService = cacheService;
    }

    @Override
    public PageResult<ServiceType> page(ServiceTypeQuery query) {
        LambdaQueryWrapper<ServiceType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getName()), ServiceType::getName, query.getName())
                .eq(StringUtils.hasText(query.getCategory()), ServiceType::getCategory, query.getCategory())
                .eq(query.getStatus() != null, ServiceType::getStatus, query.getStatus())
                .orderByDesc(ServiceType::getCreateTime);
        Page<ServiceType> page = serviceTypeMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()), wrapper);
        return PageResult.of(page);
    }

    @Override
    public List<ServiceType> listOnSale() {
        // 读多写少，缓存 30 分钟；增删改时由写操作主动失效
        return cacheService.get(CacheKeys.SERVICE_TYPE_LIST,
                new TypeReference<List<ServiceType>>() {
                },
                CacheKeys.SERVICE_TYPE_TTL,
                () -> serviceTypeMapper.selectList(new LambdaQueryWrapper<ServiceType>()
                        .eq(ServiceType::getStatus, 1)
                        .orderByAsc(ServiceType::getId)));
    }

    @Override
    public ServiceType getById(Long id) {
        ServiceType serviceType = serviceTypeMapper.selectById(id);
        if (serviceType == null) {
            throw new BusinessException(404, "服务项目不存在");
        }
        return serviceType;
    }

    @Override
    public void create(ServiceTypeSaveDTO dto) {
        ServiceType entity = new ServiceType();
        BeanUtils.copyProperties(dto, entity);
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (entity.getPointsRate() == null) {
            entity.setPointsRate(BigDecimal.ONE);
        }
        serviceTypeMapper.insert(entity);
        cacheService.evict(CacheKeys.SERVICE_TYPE_LIST);
    }

    @Override
    public void update(Long id, ServiceTypeSaveDTO dto) {
        getById(id);
        ServiceType entity = new ServiceType();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        // updateById 默认忽略 null，没传的字段保持原值
        serviceTypeMapper.updateById(entity);
        cacheService.evict(CacheKeys.SERVICE_TYPE_LIST);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态只能是 0（下架）或 1（上架）");
        }
        getById(id);
        ServiceType entity = new ServiceType();
        entity.setId(id);
        entity.setStatus(status);
        serviceTypeMapper.updateById(entity);
        cacheService.evict(CacheKeys.SERVICE_TYPE_LIST);
    }

    @Override
    public void delete(Long id) {
        getById(id);
        serviceTypeMapper.deleteById(id);
        cacheService.evict(CacheKeys.SERVICE_TYPE_LIST);
    }
}
