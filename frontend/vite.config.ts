import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const config = {
  plugins: [vue()],
  test: {
    environment: 'jsdom',
    globals: true,
  },
}

export default defineConfig(config)
