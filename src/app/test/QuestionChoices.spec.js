import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createVuestic } from 'vuestic-ui'
import axios from 'axios'
import Question from '../src/components/Question.vue'

vi.mock('axios', () => ({ default: { post: vi.fn() } }))

global.ResizeObserver = class {
  observe() {}
  unobserve() {}
  disconnect() {}
}

const choices = [
  { value: 'red', label: 'Red' },
  { value: 'green', label: 'Green' }
]

function mountQuestion(props) {
  return mount(Question, {
    props: { id: 'q1', text: 'Which color?', user: 'bob', date: 1751683200000, ...props },
    global: { plugins: [createVuestic()] }
  })
}

function submit(wrapper) {
  return wrapper.findAll('button').find((b) => b.text().trim() === 'Submit').trigger('click')
}

beforeEach(() => {
  vi.clearAllMocks()
})

describe('Question choices against real Vuestic', () => {
  it('renders one radio per choice for a single select question', () => {
    const wrapper = mountQuestion({ choices, answerMode: true })

    expect(wrapper.findAll('input[type="radio"]')).toHaveLength(2)
    expect(wrapper.findAll('input[type="checkbox"]')).toHaveLength(0)
    expect(wrapper.text()).toContain('Red')
    expect(wrapper.text()).toContain('Green')
  })

  it('posts the radio the user selected', async () => {
    axios.post.mockResolvedValue({ data: { id: 'a1' } })
    const wrapper = mountQuestion({ choices, answerMode: true })

    await wrapper.findAll('input[type="radio"]')[1].setValue()
    await submit(wrapper)
    await flushPromises()

    expect(axios.post).toHaveBeenCalledWith('/api/question/q1/answer', {
      text: '',
      selectedChoices: ['green']
    })
  })

  it('accumulates every checked box when multiple answers are allowed', async () => {
    axios.post.mockResolvedValue({ data: { id: 'a2' } })
    const wrapper = mountQuestion({ choices, allowMultipleAnswers: true, answerMode: true })

    const containers = wrapper.findAll('.va-checkbox__input-container')
    expect(containers).toHaveLength(2)
    await containers[0].trigger('click')
    await containers[1].trigger('click')
    await submit(wrapper)
    await flushPromises()

    expect(axios.post).toHaveBeenCalledWith('/api/question/q1/answer', {
      text: '',
      selectedChoices: ['red', 'green']
    })
  })

  it('unchecking a box removes it from the selection', async () => {
    axios.post.mockResolvedValue({ data: { id: 'a3' } })
    const wrapper = mountQuestion({ choices, allowMultipleAnswers: true, answerMode: true })

    const containers = wrapper.findAll('.va-checkbox__input-container')
    await containers[0].trigger('click')
    await containers[1].trigger('click')
    await containers[0].trigger('click')
    await submit(wrapper)
    await flushPromises()

    expect(axios.post).toHaveBeenCalledWith('/api/question/q1/answer', {
      text: '',
      selectedChoices: ['green']
    })
  })
})
