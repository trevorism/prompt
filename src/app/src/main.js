import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

import VueClickAway from 'vue3-click-away'
import { createVuestic } from 'vuestic-ui'
import config from '../vuestic.config.js'
import './style.css'

// Calm blue + slate light theme. Semantic success/danger are reserved for approve/reject.
const lightPreset = {
  primary: '#2563eb',        // blue-600
  secondary: '#64748b',      // slate-500
  success: '#16a34a',        // green-600
  info: '#0284c7',           // sky-600
  danger: '#dc2626',         // red-600
  warning: '#d97706',        // amber-600
  backgroundPrimary: '#ffffff',
  backgroundSecondary: '#f8fafc', // slate-50
  backgroundElement: '#ffffff',
  backgroundBorder: '#e2e8f0',    // slate-200
  textPrimary: '#1e293b',         // slate-800
  textInverted: '#ffffff'
}

const vuesticConfig = {
  ...config,
  colors: {
    ...config.colors,
    currentPresetName: 'light',
    presets: {
      ...(config.colors?.presets || {}),
      light: lightPreset
    }
  }
}

const app = createApp(App)
app.use(router)
app.use(VueClickAway)
app.use(createVuestic({ config: vuesticConfig }))
app.mount('#app')
