<template>
  <div ref="container" class="editor"></div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { EditorState, Compartment } from '@codemirror/state'
import { EditorView, basicSetup } from 'codemirror'
import { yaml } from '@codemirror/lang-yaml'
import { linter } from '@codemirror/lint'
import { oneDark } from '@codemirror/theme-one-dark'
import YAML from 'yaml'

const props = defineProps({
  modelValue: { type: String, default: '' },
  format: { type: String, default: 'yaml' }
})
const emit = defineEmits(['update:modelValue'])

const container = ref(null)
let view = null
const languageComp = new Compartment()

/**
 * 基于 yaml 解析器的实时校验，出错位置标红并提示原因。
 */
const yamlLinter = linter(editorView => {
  const text = editorView.state.doc.toString()
  if (!text.trim()) return []
  const diagnostics = []
  try {
    const doc = YAML.parseDocument(text)
    for (const error of doc.errors) {
      const pos = error.linePos?.[0]
      if (pos && pos.line <= editorView.state.doc.lines) {
        const line = editorView.state.doc.line(Math.min(pos.line, editorView.state.doc.lines))
        const from = Math.min(line.from + Math.max(0, pos.col - 1), line.to)
        diagnostics.push({ from, to: line.to, severity: 'error', message: error.message })
      } else {
        diagnostics.push({ from: 0, to: text.length, severity: 'error', message: error.message })
      }
    }
  } catch (e) {
    diagnostics.push({ from: 0, to: text.length, severity: 'error', message: e.message })
  }
  return diagnostics
})

onMounted(() => {
  view = new EditorView({
    state: EditorState.create({
      doc: props.modelValue,
      extensions: [
        basicSetup,
        oneDark,
        EditorView.lineWrapping,
        languageComp.of(languageExtensions()),
        EditorView.updateListener.of(update => {
          if (update.docChanged) {
            emit('update:modelValue', update.state.doc.toString())
          }
        })
      ]
    }),
    parent: container.value
  })
})

/**
 * 按格式装配语言扩展：yaml 带高亮与校验，properties 仅纯文本。
 */
function languageExtensions() {
  return props.format === 'properties' ? [] : [yaml(), yamlLinter]
}

watch(() => props.modelValue, value => {
  if (view && value !== view.state.doc.toString()) {
    view.dispatch({ changes: { from: 0, to: view.state.doc.length, insert: value } })
  }
})

watch(() => props.format, () => {
  if (view) {
    view.dispatch({ effects: languageComp.reconfigure(languageExtensions()) })
  }
})

onUnmounted(() => view?.destroy())
</script>

<style scoped>
.editor :deep(.cm-editor) {
  height: 320px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}
.editor :deep(.cm-scroller) {
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
}
</style>
