import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

import { TrevorismAuth } from '@trevorism/ui-auth'
import VueClickAway from 'vue3-click-away'
import { createVuestic } from 'vuestic-ui'
import config from '../vuestic.config.js'
import './style.css'

// Calm blue + slate light theme. Semantic success/danger are reserved for approve/reject.
const lightPreset = {
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
app.use(TrevorismAuth, { router })
app.use(VueClickAway)
app.use(createVuestic({ config: vuesticConfig }))
app.mount('#app')
