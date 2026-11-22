import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // 开发期把 /api 转到 Spring Boot，避免再处理跨域
      // 需要临时连别的端口时：VITE_API_TARGET=http://localhost:8082 npm run dev
      '/api': {
        target: process.env.VITE_API_TARGET || 'http://localhost:8081',
        changeOrigin: true
      }
    }
  }
})
