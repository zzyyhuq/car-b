import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

const TOKEN_KEY = 'beauty_admin_token'
const USER_KEY = 'beauty_admin_user'

function readUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  } catch (e) {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref(readUser())

  const role = computed(() => (user.value && user.value.role) || '')
  const isAdmin = computed(() => role.value === 'ADMIN')
  const displayName = computed(() => {
    if (!user.value) return '未登录'
    return user.value.nickname || user.value.username || '用户'
  })

  function setSession(data) {
    token.value = data.token
    user.value = data
    localStorage.setItem(TOKEN_KEY, data.token)
    localStorage.setItem(USER_KEY, JSON.stringify(data))
  }

  function clear() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return { token, user, role, isAdmin, displayName, setSession, clear }
})
