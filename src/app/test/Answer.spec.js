import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Answer from '../src/components/Answer.vue'

const stubs = {
  'va-chip': { template: '<span class="va-chip"><slot /></span>' }
}

function mountAnswer(props) {
  return mount(Answer, { props, global: { stubs } })
}

describe('Answer', () => {
  const base = { text: 'the answer', user: 'alice', date: 1751683200000 }

  it('renders the answer text and user', () => {
    const wrapper = mountAnswer(base)
    expect(wrapper.text()).toContain('the answer')
    expect(wrapper.text()).toContain('alice')
  })

  it('shows an Approved chip when approved is true', () => {
    const wrapper = mountAnswer({ ...base, approved: true })
    const chips = wrapper.findAll('.va-chip').map((c) => c.text())
    expect(chips).toContain('Approved')
    expect(chips).not.toContain('Rejected')
  })

  it('shows a Rejected chip when approved is false', () => {
    const wrapper = mountAnswer({ ...base, approved: false })
    const chips = wrapper.findAll('.va-chip').map((c) => c.text())
    expect(chips).toContain('Rejected')
    expect(chips).not.toContain('Approved')
  })

  it('shows no decision chip for a plain answer (approved null)', () => {
    const wrapper = mountAnswer(base)
    expect(wrapper.findAll('.va-chip')).toHaveLength(0)
  })
})
