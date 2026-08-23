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
  },
  'va-radio': {
    props: ['modelValue', 'option', 'label', 'name'],
    emits: ['update:modelValue'],
    template:
      '<label class="va-radio" :data-name="name" @click="$emit(\'update:modelValue\', option)">{{ label }}</label>'
  },
  'va-checkbox': {
    props: ['modelValue', 'arrayValue', 'label'],
    emits: ['update:modelValue'],
    methods: {
      toggle() {
        const current = Array.isArray(this.modelValue) ? this.modelValue : []
        const next = current.includes(this.arrayValue)
          ? current.filter((value) => value !== this.arrayValue)
          : [...current, this.arrayValue]
        this.$emit('update:modelValue', next)
      }
    },
    template: '<label class="va-checkbox" @click="toggle">{{ label }}</label>'
  }
}

const colorChoices = [
  { value: 'red', label: 'Red' },
  { value: 'green', label: 'Green' }
]

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

function choice(wrapper, selector, label) {
  return wrapper.findAll(selector).find((c) => c.text().trim() === label)
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

    expect(axios.post).toHaveBeenCalledWith('/api/question/q1/answer', {
      text: 'because',
      selectedChoices: []
    })
    expect(wrapper.emitted('answeredQuestion')[0][0]).toEqual({ id: 'a1', text: 'because' })
  })

  it('defaults an approval decision text and includes approved=true', async () => {
    axios.post.mockResolvedValue({ data: { id: 'a2' } })
    const wrapper = mountQuestion({ kind: 'approval', answerMode: true })

    await button(wrapper, 'Approve').trigger('click')
    await flushPromises()

    expect(axios.post).toHaveBeenCalledWith('/api/question/q1/answer', {
      text: 'Approved',
      approved: true,
      selectedChoices: []
    })
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

describe('Question multiple choice', () => {
  it('labels a question with choices as a Poll and renders a radio per choice', () => {
    const wrapper = mountQuestion({ choices: colorChoices, answerMode: true })

    expect(wrapper.text()).toContain('Poll')
    expect(wrapper.text()).toContain('Select one')
    expect(wrapper.findAll('.va-radio')).toHaveLength(2)
    expect(wrapper.findAll('.va-checkbox')).toHaveLength(0)
  })

  it('scopes the radio group name to the question so two cards do not collide', () => {
    const wrapper = mountQuestion({ choices: colorChoices, answerMode: true })

    expect(wrapper.find('.va-radio').attributes('data-name')).toBe('choice-q1')
  })

  it('renders checkboxes when multiple answers are allowed', () => {
    const wrapper = mountQuestion({
      choices: colorChoices,
      allowMultipleAnswers: true,
      answerMode: true
    })

    expect(wrapper.text()).toContain('Select all that apply')
    expect(wrapper.findAll('.va-checkbox')).toHaveLength(2)
    expect(wrapper.findAll('.va-radio')).toHaveLength(0)
  })

  it('posts the selected choice with no comment', async () => {
    axios.post.mockResolvedValue({ data: { id: 'a3', text: 'Green' } })
    const wrapper = mountQuestion({ choices: colorChoices, answerMode: true })

    await choice(wrapper, '.va-radio', 'Green').trigger('click')
    await button(wrapper, 'Submit').trigger('click')
    await flushPromises()

    expect(axios.post).toHaveBeenCalledWith('/api/question/q1/answer', {
      text: '',
      selectedChoices: ['green']
    })
  })

  it('posts an optional comment alongside the selected choice', async () => {
    axios.post.mockResolvedValue({ data: { id: 'a4' } })
    const wrapper = mountQuestion({ choices: colorChoices, answerMode: true })

    await choice(wrapper, '.va-radio', 'Red').trigger('click')
    await wrapper.find('.va-textarea').setValue('it is faster')
    await button(wrapper, 'Submit').trigger('click')
    await flushPromises()

    expect(axios.post).toHaveBeenCalledWith('/api/question/q1/answer', {
      text: 'it is faster',
      selectedChoices: ['red']
    })
  })

  it('posts every checked choice when multiple answers are allowed', async () => {
    axios.post.mockResolvedValue({ data: { id: 'a5' } })
    const wrapper = mountQuestion({
      choices: colorChoices,
      allowMultipleAnswers: true,
      answerMode: true
    })

    await choice(wrapper, '.va-checkbox', 'Red').trigger('click')
    await choice(wrapper, '.va-checkbox', 'Green').trigger('click')
    await button(wrapper, 'Submit').trigger('click')
    await flushPromises()

    expect(axios.post).toHaveBeenCalledWith('/api/question/q1/answer', {
      text: '',
      selectedChoices: ['red', 'green']
    })
  })

  it('does not call the API when nothing is selected', async () => {
    const wrapper = mountQuestion({ choices: colorChoices, answerMode: true })

    await button(wrapper, 'Submit').trigger('click')

    expect(axios.post).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('Please select an option')
  })

  it('carries the selection on an approval decision without defaulting the reason', async () => {
    axios.post.mockResolvedValue({ data: { id: 'a6' } })
    const wrapper = mountQuestion({
      kind: 'approval',
      choices: [
        { value: 'yes', label: 'Yes' },
        { value: 'changes', label: 'Needs changes' }
      ],
      answerMode: true
    })

    await choice(wrapper, '.va-radio', 'Needs changes').trigger('click')
    await button(wrapper, 'Approve').trigger('click')
    await flushPromises()

    expect(axios.post).toHaveBeenCalledWith('/api/question/q1/answer', {
      text: '',
      approved: true,
      selectedChoices: ['changes']
    })
  })
})
