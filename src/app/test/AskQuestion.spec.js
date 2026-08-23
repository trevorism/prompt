import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import axios from 'axios'
import AskQuestion from '../src/views/AskQuestion.vue'

vi.mock('axios', () => ({ default: { get: vi.fn(), post: vi.fn() } }))
vi.mock('@trevorism/ui-header-bar', () => ({ default: { template: '<div class="header-bar" />' } }))

const checkable = {
  props: ['modelValue', 'label', 'disabled'],
  emits: ['update:modelValue'],
  template:
    '<label class="va-checkbox" :data-disabled="disabled" @click="$emit(\'update:modelValue\', !modelValue)">{{ label }}</label>'
}

const textField = {
  props: ['modelValue', 'label'],
  emits: ['update:modelValue'],
  template:
    '<input class="va-input" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />'
}

const stubs = {
  'header-bar': true,
  'va-form': { template: '<form class="va-form"><slot /></form>' },
  'va-collapse': { template: '<div class="va-collapse"><slot /></div>' },
  'va-select': { props: ['modelValue'], template: '<div class="va-select" />' },
  'va-date-input': { props: ['modelValue'], template: '<div class="va-date-input" />' },
  'va-inner-loading': { template: '<span><slot /></span>' },
  'va-button': { template: '<button class="va-button"><slot /></button>' },
  'va-checkbox': checkable,
  VaCheckbox: checkable,
  VaCollapse: { template: '<div class="va-collapse"><slot /></div>' },
  VaSelect: { props: ['modelValue'], template: '<div class="va-select" />' },
  'va-input': textField,
  'va-textarea': {
    props: ['modelValue'],
    emits: ['update:modelValue'],
    template:
      '<textarea class="va-textarea" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>'
  }
}

function mountAsk() {
  return mount(AskQuestion, {
    global: {
      stubs,
      mocks: { $router: { push: vi.fn() } }
    }
  })
}

function labelled(wrapper, selector, label) {
  return wrapper.findAll(selector).find((element) => element.text().trim() === label)
}

function button(wrapper, label) {
  return wrapper.findAll('.va-button').find((b) => b.text().trim() === label)
}

async function enableMultipleChoice(wrapper) {
  await labelled(wrapper, '.va-checkbox', 'Offer multiple choice answers').trigger('click')
}

async function fillChoices(wrapper, values) {
  const inputs = wrapper.findAll('.va-input')
  for (let index = 0; index < values.length; index++) {
    await inputs[index].setValue(values[index])
  }
}

beforeEach(() => {
  vi.clearAllMocks()
  axios.get.mockResolvedValue({ data: [] })
})

describe('AskQuestion multiple choice', () => {
  it('hides the choice repeater until multiple choice is enabled', async () => {
    const wrapper = mountAsk()
    expect(wrapper.findAll('.va-input')).toHaveLength(0)

    await enableMultipleChoice(wrapper)

    expect(wrapper.findAll('.va-input')).toHaveLength(2)
  })

  it('adds and removes choice rows', async () => {
    const wrapper = mountAsk()
    await enableMultipleChoice(wrapper)

    await button(wrapper, 'Add choice').trigger('click')
    expect(wrapper.findAll('.va-input')).toHaveLength(3)

    await wrapper.findAll('.va-button').find((b) => b.text().trim() === 'Remove').trigger('click')
    expect(wrapper.findAll('.va-input')).toHaveLength(2)
  })

  it('does not offer a remove button while only two choices remain', async () => {
    const wrapper = mountAsk()
    await enableMultipleChoice(wrapper)

    expect(button(wrapper, 'Remove')).toBeUndefined()
  })

  it('posts label-only choices and lets the server derive the values', async () => {
    axios.post.mockResolvedValue({ data: {} })
    const wrapper = mountAsk()

    await wrapper.find('.va-textarea').setValue('Which color?')
    await enableMultipleChoice(wrapper)
    await fillChoices(wrapper, ['Red', 'Green'])
    await button(wrapper, 'Submit').trigger('click')
    await flushPromises()

    expect(axios.post).toHaveBeenCalledWith(
      'api/question',
      expect.objectContaining({
        text: 'Which color?',
        kind: 'question',
        choices: [{ label: 'Red' }, { label: 'Green' }],
        allowMultipleAnswers: false
      })
    )
  })

  it('drops blank rows and flags allowMultipleAnswers', async () => {
    axios.post.mockResolvedValue({ data: {} })
    const wrapper = mountAsk()

    await wrapper.find('.va-textarea').setValue('Which colors?')
    await enableMultipleChoice(wrapper)
    await button(wrapper, 'Add choice').trigger('click')
    await fillChoices(wrapper, ['Red', '   ', 'Green'])
    await labelled(wrapper, '.va-checkbox', 'Allow more than one selection').trigger('click')
    await button(wrapper, 'Submit').trigger('click')
    await flushPromises()

    expect(axios.post).toHaveBeenCalledWith(
      'api/question',
      expect.objectContaining({
        choices: [{ label: 'Red' }, { label: 'Green' }],
        allowMultipleAnswers: true
      })
    )
  })

  it('refuses to submit fewer than two choices', async () => {
    const wrapper = mountAsk()

    await wrapper.find('.va-textarea').setValue('Which color?')
    await enableMultipleChoice(wrapper)
    await fillChoices(wrapper, ['Red'])
    await button(wrapper, 'Submit').trigger('click')

    expect(axios.post).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('Enter at least two choices')
  })

  it('never asks Chat-GPT to answer a multiple choice question', async () => {
    axios.post.mockResolvedValue({ data: {} })
    const wrapper = mountAsk()

    await wrapper.find('.va-textarea').setValue('Which color?')
    await labelled(wrapper, '.va-checkbox', 'Also ask Chat-GPT').trigger('click')
    await enableMultipleChoice(wrapper)
    await fillChoices(wrapper, ['Red', 'Green'])
    await button(wrapper, 'Submit').trigger('click')
    await flushPromises()

    expect(axios.post).toHaveBeenCalledWith(
      'api/question',
      expect.objectContaining({ askChatGpt: false })
    )
    expect(labelled(wrapper, '.va-checkbox', 'Also ask Chat-GPT').attributes('data-disabled')).toBe('true')
  })

  it('sends no choices for a plain free form question', async () => {
    axios.post.mockResolvedValue({ data: {} })
    const wrapper = mountAsk()

    await wrapper.find('.va-textarea').setValue('Why?')
    await button(wrapper, 'Submit').trigger('click')
    await flushPromises()

    expect(axios.post).toHaveBeenCalledWith(
      'api/question',
      expect.objectContaining({ choices: [], allowMultipleAnswers: false })
    )
  })
})
