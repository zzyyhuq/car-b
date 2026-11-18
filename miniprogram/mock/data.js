/**
 * 阶段 1 的假数据，全部集中在文件里。
 * 阶段 3 接后端时，把页面里的 require 换成 wx.request 即可，页面结构不用大改。
 */

const services = [
  {
    id: 1,
    name: '标准精洗',
    category: '洗车',
    price: 68,
    duration: 60,
    pointsRate: 1,
    emoji: '🚿',
    tag: '热门',
    brief: '高压冲洗 + 泡沫清洗 + 内饰吸尘',
    description: '适合日常通勤车辆的基础清洁，洗完后车身洁净透亮。',
    items: ['高压水枪冲洗车身', '中性泡沫清洗', '轮毂与轮胎清洁', '内饰吸尘', '玻璃擦拭']
  },
  {
    id: 2,
    name: '内饰深度清洁',
    category: '内饰',
    price: 288,
    duration: 120,
    pointsRate: 1.5,
    emoji: '✨',
    tag: '推荐',
    brief: '座椅地毯深度清洁，臭氧除味',
    description: '针对有小孩、宠物的家庭用车，清除内饰顽固污渍与异味。',
    items: ['座椅深度清洗', '地毯与脚垫清洁', '顶棚清洁', '臭氧除味', '内饰养护']
  },
  {
    id: 3,
    name: '全车打蜡',
    category: '漆面',
    price: 198,
    duration: 90,
    pointsRate: 1.2,
    emoji: '🧴',
    tag: '性价比',
    brief: '漆面清洁后手工上蜡，提升光泽',
    description: '手工上蜡形成保护层，隔绝紫外线与酸雨，适合换季养护。',
    items: ['漆面深度清洁', '去污黏土处理', '手工上蜡', '缝隙清理']
  },
  {
    id: 4,
    name: '漆面镀晶',
    category: '漆面',
    price: 1280,
    duration: 240,
    pointsRate: 2,
    emoji: '💎',
    tag: '尊享',
    brief: '三层镀晶施工，保护约 12 个月',
    description: '新车或漆面良好的车辆首选，硬度提升、拨水效果明显。',
    items: ['漆面矫正', '脱脂处理', '三层镀晶', '固化养护', '交车质检']
  },
  {
    id: 5,
    name: '发动机舱清洁',
    category: '保养',
    price: 158,
    duration: 45,
    pointsRate: 1,
    emoji: '🔧',
    tag: '进阶',
    brief: '低压清洗 + 线路保护',
    description: '清除发动机舱油污与灰尘，降低自燃风险，延长线路寿命。',
    items: ['舱内除尘', '低压水洗', '油污分解', '线路保护剂', '启动检测']
  },
  {
    id: 6,
    name: '空调管路杀菌',
    category: '保养',
    price: 128,
    duration: 40,
    pointsRate: 1,
    emoji: '🌀',
    tag: '换季',
    brief: '蒸发箱清洗 + 风道杀菌',
    description: '解决空调异味，换季必做项目，建议每年一次。',
    items: ['蒸发箱清洗', '风道杀菌', '滤芯检查', '出风检测']
  }
]

const categories = ['全部', '洗车', '内饰', '漆面', '保养']

const cars = [
  { id: 1, plateNo: '粤B·D12345', brand: '宝马', model: '3系', color: '白色' },
  { id: 2, plateNo: '粤B·8K666', brand: '特斯拉', model: 'Model Y', color: '黑色' }
]

const notices = [
  '本店营业时间 9:00 - 18:00，节假日正常营业',
  '新用户首次预约享 8 折优惠',
  '订单完成后自动发放积分，可兑换精洗券'
]

const activities = [
  {
    id: 1,
    title: '秋季漆面养护节',
    desc: '打蜡 / 镀晶项目 8 折',
    bg: 'linear-gradient(135deg, #1677ff, #41b3ff)'
  },
  {
    id: 2,
    title: '积分翻倍月',
    desc: '本月订单积分 x2',
    bg: 'linear-gradient(135deg, #7c4dff, #b388ff)'
  }
]

const goods = [
  { id: 1, name: '免费精洗券', desc: '凭券免费洗车一次', points: 300, emoji: '🧴', stock: 20 },
  { id: 2, name: '玻璃水一瓶', desc: '冬季防冻型玻璃水', points: 120, emoji: '💧', stock: 50 },
  { id: 3, name: '车内除味喷雾', desc: '植物提取，清新除味', points: 200, emoji: '🌿', stock: 30 },
  { id: 4, name: '洗车毛巾套装', desc: '超细纤维三件套', points: 150, emoji: '🧽', stock: 40 }
]

module.exports = {
  services,
  categories,
  cars,
  notices,
  activities,
  goods
}
