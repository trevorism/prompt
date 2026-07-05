import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import axios from 'axios'
import Question from '../src/components/Question.vue'

vi.mock('axios', () => ({ default: { post: vi.fn() } }))

const stubs = {
  'va-chip': { template: '<span class="va-chip"><slot /></span>' },
  'va-button': { template: '<button class="va-button"><slot /></button>' },
  'va-form': { template: '<form class="va-form"><slot /></form>' },
  'va-inner-loading': { template: '<span><slot /></span>' },
  'va-textarea': {
    props: ['modelValue'],
    emits: ['update:modelValue'],
    template:
      '<textarea class="va-textarea" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>'
  }
}

const now = 1751683200000
function mountQuestion(props = {}) {
  return mount(Question, {
    props: { id: 'q1', text: 'Why?', user: 'bob', date: now, ...props },
    global: { stubs }
  })
}

// Find a stubbed va-button by its (trimmed) label.
function button(wrapper, label) {
  return wrapper.findAll('.va-button').find((b) => b.text().trim() === label)
}

beforeEach(() => {
  vi.clearAllMocks()
})

describe('Question rendering', () => {
  it('renders the question text, user, and a Question label', () => {
    const wrapper = mountQuestion()
    expect(wrapper.text()).toContain('Why?')
    expect(wrapper.text()).toContain('bob')
    expect(wrapper.text()).toContain('Question')
  })

  it('labels an approval and offers a review button', () => {
    const wrapper = mountQuestion({ kind: 'approval' })
    expect(wrapper.text()).toContain('Approval')
    expect(button(wrapper, 'Review approval')).toBeTruthy()
  })

  // Due dates are compared against the real clock, so anchor them to Date.now().
  const past = Date.now() - 100000
  const future = Date.now() + 100000000

  it('flags an overdue unanswered question', () => {
    const wrapper = mountQuestion({ dueDate: past, answered: false })
    expect(wrapper.find('.va-chip').text()).toBe('Overdue')
  })

  it('uses "Expired" for an overdue approval', () => {
    const wrapper = mountQuestion({ kind: 'approval', dueDate: past, answered: false })
    expect(wrapper.find('.va-chip').text()).toBe('Expired')
  })

  it('does not flag a past-due question that is already answered', () => {
    const wrapper = mountQuestion({ dueDate: past, answered: true })
    expect(wrapper.find('.va-chip').exists()).toBe(false)
  })

  it('does not flag a question with a future due date', () => {
    const wrapper = mountQuestion({ dueDate: future, answered: false })
    expect(wrapper.find('.va-chip').exists()).toBe(false)
  })
})

describe('Question answering', () => {
  it('shows a validation error and does not call the API when the answer is empty', async () => {
    const wrapper = mountQuestion({ answerMode: true })

    await button(wrapper, 'Submit').trigger('click')

    expect(axios.post).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('Please enter an answer')
  })

  it('posts the answer and emits answeredQuestion on success', async () => {
    axios.post.mockResolvedValue({ data: { id: 'a1', text: 'because' } })
    const wrapper = mountQuestion({ answerMode: true })

    await wrapper.find('.va-textarea').setValue('because')
    await button(wrapper, 'Submit').trigger('click')
    await flushPromises()

    expect(axios.post).toHaveBeenCalledWith('/api/question/q1/answer', { text: 'because' })
    expect(wrapper.emitted('answeredQuestion')[0][0]).toEqual({ id: 'a1', text: 'because' })
  })

  it('defaults an approval decision text and includes approved=true', async () => {
    axios.post.mockResolvedValue({ data: { id: 'a2' } })
    const wrapper = mountQuestion({ kind: 'approval', answerMode: true })

    await button(wrapper, 'Approve').trigger('click')
    await flushPromises()

    expect(axios.post).toHaveBeenCalledWith('/api/question/q1/answer', { text: 'Approved', approved: true })
  })

  it('surfaces an error message when the API call fails', async () => {
    axios.post.mockRejectedValue(new Error('boom'))
    const wrapper = mountQuestion({ answerMode: true })

    await wrapper.find('.va-textarea').setValue('because')
    await button(wrapper, 'Submit').trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('Error submitting response')
  })
})
