import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Mirrors the k3s Ingress path routing (see infra-scripts/manifests/mtracker/30-ingress.yaml):
    // "/api" goes to the Spring Boot backend, everything else is served by this dev server.
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
})
