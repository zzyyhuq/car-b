/**
 * 服务项目展示字段转换。
 * 后端只存名称/分类/价格/描述等基础字段，emoji、标签、服务内容条目这些
 * 展示用的东西在前端统一推导，列表页和详情页共用。
 */

const CATEGORY_EMOJI = {
  洗车: '🚿',
  内饰: '✨',
  漆面: '💎',
  保养: '🔧'
}

function formatPrice(value) {
  const price = Number(value || 0)
  return Number.isInteger(price) ? price : price.toFixed(2)
}

function tagOf(pointsRate) {
  const rate = Number(pointsRate || 0)
  if (rate >= 2) return '尊享'
  if (rate >= 1.5) return '推荐'
  if (rate >= 1.2) return '性价比'
  return ''
}

function emojiOf(category) {
  return CATEGORY_EMOJI[category] || '🚗'
}

/** 后端字段 → 页面展示对象 */
function toServiceView(item) {
  return {
    id: item.id,
    name: item.name,
    category: item.category || '其他',
    price: formatPrice(item.price),
    duration: item.duration,
    pointsRate: item.pointsRate,
    emoji: emojiOf(item.category),
    tag: tagOf(item.pointsRate),
    brief: item.description || '',
    description: item.description || '',
    image: item.image || ''
  }
}

/** 把描述按顿号/逗号拆成服务内容条目，例如“高压冲洗、泡沫清洗、轮毂清洁” */
function descriptionItems(description) {
  if (!description) {
    return []
  }
  return description
    .split(/[、，,。；;]/)
    .map(text => text.trim())
    .filter(Boolean)
}

module.exports = {
  toServiceView,
  descriptionItems,
  formatPrice,
  tagOf,
  emojiOf
}
