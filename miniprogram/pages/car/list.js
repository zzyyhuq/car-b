const { get, del } = require('../../utils/request')

Page({
  data: {
    cars: [],
    loading: true
  },

  onShow() {
    this.loadCars()
  },

  onPullDownRefresh() {
    this.loadCars().then(() => wx.stopPullDownRefresh())
  },

  loadCars() {
    return get('/api/car/my', {}, { loading: !this.data.cars.length })
      .then(cars => {
        this.setData({ cars: cars || [], loading: false })
      })
      .catch(() => {
        this.setData({ loading: false })
      })
  },

  addCar() {
    wx.navigateTo({ url: '/pages/car/edit' })
  },

  editCar(e) {
    wx.navigateTo({ url: '/pages/car/edit?id=' + e.currentTarget.dataset.id })
  },

  deleteCar(e) {
    const id = Number(e.currentTarget.dataset.id)
    const car = this.data.cars.find(item => item.id === id)
    wx.showModal({
      title: '删除车辆',
      content: '确认删除 ' + ((car && car.plateNo) || '该车辆') + '？',
      confirmColor: '#e34d59',
      success: res => {
        if (!res.confirm) {
          return
        }
        del('/api/car/' + id, {}, { loading: true })
          .then(() => {
            wx.showToast({ title: '已删除', icon: 'success' })
            this.loadCars()
          })
          .catch(() => {})
      }
    })
  }
})
