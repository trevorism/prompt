import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import Splash from '../src/views/Splash.vue'

const auth = vi.hoisted(() => ({ session: null, login: vi.fn() }))

vi.mock('@trevorism/ui-auth', async () => {
  const { reactive, computed } = await import('vue')
  auth.session = reactive({ authenticated: false, loading: false })
  return {
    useAuth: () => ({
      user: computed(() => (auth.session.authenticated ? { username: 'tester' } : null)),
      isAuthenticated: computed(() => auth.session.authenticated),
      isAdmin: computed(() => false),
      loading: computed(() => auth.session.loading),
      ready: Promise.resolve(),
      login: auth.login,
      logout: vi.fn()
    })
  }
})

vi.mock('@trevorism/ui-header-bar', () => ({
  MenuBar: { name: 'MenuBar', template: '<nav class="menu-bar" />' }
}))

const get = vi.hoisted(() => vi.fn())
vi.mock('axios', () => ({ default: { get, post: vi.fn() } }))

const stubs = {
  'va-button': {
    props: ['to'],
    template: '<button class="va-button" :data-to="to"><slot /></button>'
  },
  'va-tabs': { template: '<div class="va-tabs"><slot /></div>' },
  'va-tab': { template: '<div class="va-tab"><slot /></div>' },
  'all-questions': { template: '<div class="all-questions" />' },
  'pending-questions': { template: '<div class="pending-questions" />' },
  'unanswered-questions': { template: '<div class="unanswered-questions" />' },
  'my-questions': { template: '<div class="my-questions" />' }
}

const mounted = []

function mountSplash() {
  const wrapper = mount(Splash, { global: { stubs } })
  mounted.push(wrapper)
  return wrapper
}

const buttonSaying = (wrapper, text) =>
  wrapper.findAll('button').find((node) => node.text().trim() === text)

describe('Splash', () => {
  beforeEach(() => {
    get.mockReset()
    auth.login.mockClear()
    auth.session.authenticated = false
    auth.session.loading = false
  })

  afterEach(() => {
    while (mounted.length) {
      mounted.pop().unmount()
    }
  })

  it('says it is still checking rather than claiming you are signed out', () => {
    auth.session.loading = true

    const wrapper = mountSplash()

    expect(wrapper.text()).toContain('Checking your session')
    expect(wrapper.text()).not.toContain('Please log in')
    expect(buttonSaying(wrapper, 'Login')).toBeUndefined()
  })

  it('offers login to an anonymous visitor and hides the tabs', () => {
    const wrapper = mountSplash()

    expect(wrapper.text()).toContain('Please log in')
    expect(buttonSaying(wrapper, 'Login')).toBeDefined()
    expect(wrapper.find('.va-tabs').exists()).toBe(false)
  })

  it('starts the handoff rather than linking at the login app', async () => {
    const wrapper = mountSplash()

    await buttonSaying(wrapper, 'Login').trigger('click')

    expect(auth.login).toHaveBeenCalledTimes(1)
  })

  it('no longer probes an api endpoint to work out whether you are signed in', () => {
    mountSplash()

    expect(get).not.toHaveBeenCalled()
  })

  it('shows the tabs and the ask button once signed in', async () => {
    auth.session.authenticated = true
    const wrapper = mountSplash()
    await nextTick()

    expect(wrapper.find('.va-tabs').exists()).toBe(true)
    expect(wrapper.find('.all-questions').exists()).toBe(true)
    expect(buttonSaying(wrapper, 'Ask a question')).toBeDefined()
    expect(wrapper.text()).not.toContain('Please log in')
  })

  it('flips to signed in without a reload', async () => {
    const wrapper = mountSplash()
    expect(wrapper.find('.va-tabs').exists()).toBe(false)

    auth.session.authenticated = true
    await nextTick()

    expect(wrapper.find('.va-tabs').exists()).toBe(true)
    expect(buttonSaying(wrapper, 'Login')).toBeUndefined()
  })

  it('flips back when the session ends', async () => {
    auth.session.authenticated = true
    const wrapper = mountSplash()
    await nextTick()

    auth.session.authenticated = false
    await nextTick()

    expect(wrapper.find('.va-tabs').exists()).toBe(false)
    expect(wrapper.text()).toContain('Please log in')
  })
})
