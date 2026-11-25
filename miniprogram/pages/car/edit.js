const { get, post, put } = require('../../utils/request')

Page({
  data: {
    carId: null,
    form: {
      plateNo: '',
      brand: '',
      model: '',
      color: ''
    },
    submitting: false
  },

  onLoad(options) {
    const id = Number(options.id)
    if (!id) {
      wx.setNavigationBarTitle({ title: '添加车辆' })
      return
    }

    this.setData({ carId: id })
    wx.setNavigationBarTitle({ title: '编辑车辆' })
    get('/api/car/my', {}, { loading: true })
      .then(cars => {
        const car = (cars || []).find(item => item.id === id)
        if (!car) {
          wx.showToast({ title: '车辆不存在', icon: 'none' })
          wx.navigateBack()
          return
        }
        this.setData({
          form: {
            plateNo: car.plateNo || '',
            brand: car.brand || '',
            model: car.model || '',
            color: car.color || ''
          }
        })
      })
      .catch(() => {})
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ ['form.' + field]: e.detail.value })
  },

  submit() {
    const { carId, form, submitting } = this.data
    if (submitting) {
      return
    }
    if (!form.plateNo.trim()) {
      wx.showToast({ title: '请输入车牌号', icon: 'none' })
      return
    }

    this.setData({ submitting: true })
    const request = carId
      ? put('/api/car/' + carId, form, { loading: true })
      : post('/api/car', form, { loading: true })

    request
      .then(() => {
        wx.showToast({ title: carId ? '已保存' : '已添加', icon: 'success' })
        setTimeout(() => wx.navigateBack(), 500)
      })
      .catch(() => {})
      .then(() => this.setData({ submitting: false }))
  }
})
