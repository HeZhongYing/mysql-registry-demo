<template>
  <div class="console">
    <header class="header">
      <div>
        <h1>MySQL 注册中心控制台</h1>
        <span class="sub">注册情况 / 配置情况 · 每 {{ pollInterval / 1000 }}s 自动刷新</span>
      </div>
      <el-tag :type="connected ? 'success' : 'danger'" effect="dark">
        {{ connected ? '已连接' : '连接断开' }}
      </el-tag>
    </header>

    <div class="cards">
      <el-card shadow="never"><div class="stat"><div class="num">{{ overview.serviceCount }}</div><div class="label">存活服务</div></div></el-card>
      <el-card shadow="never"><div class="stat"><div class="num ok">{{ overview.instanceUp }}</div><div class="label">在线实例 (UP)</div></div></el-card>
      <el-card shadow="never"><div class="stat"><div class="num">{{ overview.instanceOffline }}</div><div class="label">手动下线 (OFFLINE)</div></div></el-card>
      <el-card shadow="never"><div class="stat"><div class="num bad">{{ overview.instanceDown }}</div><div class="label">心跳超时 (DOWN)</div></div></el-card>
      <el-card shadow="never"><div class="stat"><div class="num">{{ overview.configCount }}</div><div class="label">配置项</div></div></el-card>
    </div>

    <el-tabs v-model="tab">
      <el-tab-pane label="服务实例" name="instances">
        <el-table :data="instances" border stripe size="default">
          <el-table-column prop="service_name" label="服务名" width="170">
            <template #default="{ row }">
              <el-icon style="margin-right:4px"><Cpu /></el-icon>{{ row.service_name }}
            </template>
          </el-table-column>
          <el-table-column prop="instance_id" label="实例 ID" width="170" />
          <el-table-column label="地址" width="180">
            <template #default="{ row }">{{ row.host }}:{{ row.port }}</template>
          </el-table-column>
          <el-table-column label="权重" width="100" align="center">
            <template #default="{ row }">
              <el-tag effect="plain">{{ row.weight }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="statusTag(row.status)" effect="dark">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="心跳距今" width="120" align="center">
            <template #default="{ row }">
              <span v-if="row.status === 'UP'" :class="{ stale: secondsAgo(row.last_heartbeat) > 30 }">{{ secondsAgo(row.last_heartbeat) }}s 前</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="registered_at" label="注册时间" min-width="170">
            <template #default="{ row }">{{ formatTime(row.registered_at) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center">
            <template #default="{ row }">
              <el-button v-if="row.status === 'UP'" size="small" type="danger" plain @click="offline(row)">下线</el-button>
              <el-button v-if="row.status === 'OFFLINE'" size="small" type="success" plain @click="online(row)">上线</el-button>
              <el-button size="small" type="primary" plain @click="openWeightDialog(row)">权重</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="配置中心" name="configs">
        <div class="toolbar">
          <el-button type="primary" size="small" @click="openConfigDialog()">新增配置文件</el-button>
        </div>
        <el-table :data="configs" border stripe size="default">
          <el-table-column prop="service_name" label="归属服务" width="170">
            <template #default="{ row }">
              <el-tag :type="row.service_name === 'application' ? 'warning' : 'primary'" effect="plain">{{ row.service_name }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="file_name" label="配置文件" width="220">
            <template #default="{ row }">
              <el-icon style="margin-right:4px"><Document /></el-icon>{{ row.file_name }}
            </template>
          </el-table-column>
          <el-table-column label="内容预览" min-width="240">
            <template #default="{ row }">
              <code class="value preview">{{ row.content }}</code>
            </template>
          </el-table-column>
          <el-table-column prop="format" label="格式" width="100" align="center" />
          <el-table-column prop="version" label="版本" width="80" align="center" />
          <el-table-column prop="updated_at" label="更新时间" min-width="170">
            <template #default="{ row }">{{ formatTime(row.updated_at) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center">
            <template #default="{ row }">
              <el-button size="small" type="primary" plain @click="openConfigDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="deleteConfig(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="weightDialog.visible" :title="`修改权重 - ${weightDialog.instanceId}`" width="360px">
      <el-form label-width="70px">
        <el-form-item label="当前值">{{ weightDialog.oldWeight }}</el-form-item>
        <el-form-item label="新权重">
          <el-input-number v-model="weightDialog.newWeight" :min="1" :max="1000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="weightDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitWeight">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="configDialog.visible" :title="configDialog.isEdit ? '编辑配置文件' : '新增配置文件'" width="640px">
      <el-form label-width="80px">
        <el-form-item label="归属服务">
          <el-select v-if="!configDialog.isEdit" v-model="configDialog.serviceName" filterable allow-create default-first-option style="width: 100%">
            <el-option v-for="name in serviceNames" :key="name" :label="name" :value="name" />
          </el-select>
          <span v-else>{{ configDialog.serviceName }}</span>
        </el-form-item>
        <el-form-item v-if="!configDialog.isEdit" label="文件名">
          <el-input v-model="configDialog.fileName" placeholder="如 order-service.yaml" />
        </el-form-item>
        <el-form-item v-else label="文件名">{{ configDialog.fileName }}</el-form-item>
        <el-form-item v-if="!configDialog.isEdit" label="格式">
          <el-radio-group v-model="configDialog.format">
            <el-radio value="yaml">yaml</el-radio>
            <el-radio value="properties">properties</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="文件内容">
          <el-input v-model="configDialog.content" type="textarea" :rows="12" class="code-editor" spellcheck="false" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitConfig">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Cpu, Document } from '@element-plus/icons-vue'

const pollInterval = 3000
const tab = ref('instances')
const connected = ref(false)
const overview = ref({})
const instances = ref([])
const configs = ref([])
let timer = null
let serverTime = null

const serviceNames = computed(() => [...new Set([...instances.value.map(i => i.service_name), 'application'])])

const weightDialog = reactive({ visible: false, instanceId: '', oldWeight: 100, newWeight: 100 })
const configDialog = reactive({ visible: false, isEdit: false, serviceName: 'application', fileName: '', format: 'yaml', content: '' })

async function request(url, method = 'GET', body) {
  const res = await fetch(url, {
    method,
    headers: body ? { 'Content-Type': 'application/json' } : undefined,
    body: body ? JSON.stringify(body) : undefined
  })
  return res.json()
}

async function fetchAll() {
  try {
    const [ov, ins, cfg] = await Promise.all([
      fetch('api/overview').then(r => r.json()),
      fetch('api/instances').then(r => r.json()),
      fetch('api/configs').then(r => r.json())
    ])
    overview.value = ov
    instances.value = ins
    configs.value = cfg
    serverTime = new Date(ov.serverTime)
    connected.value = true
  } catch (e) {
    connected.value = false
  }
}

async function offline(row) {
  await ElMessageBox.confirm(`确认下线实例 ${row.instance_id}？下线后将不再接收流量`, '手动下线', { type: 'warning' })
  const res = await request(`api/instances/${row.instance_id}/status`, 'PUT', { status: 'OFFLINE' })
  res.updated ? ElMessage.success('已下线') : ElMessage.error('下线失败')
  fetchAll()
}

async function online(row) {
  const res = await request(`api/instances/${row.instance_id}/status`, 'PUT', { status: 'UP' })
  res.updated ? ElMessage.success('已上线') : ElMessage.error('上线失败')
  fetchAll()
}

function openWeightDialog(row) {
  weightDialog.instanceId = row.instance_id
  weightDialog.oldWeight = row.weight
  weightDialog.newWeight = row.weight
  weightDialog.visible = true
}

async function submitWeight() {
  const res = await request(`api/instances/${weightDialog.instanceId}/weight`, 'PUT', { weight: weightDialog.newWeight })
  if (res.updated) {
    ElMessage.success('权重已更新')
    weightDialog.visible = false
    fetchAll()
  } else {
    ElMessage.error('更新失败')
  }
}

function openConfigDialog(row) {
  if (row) {
    configDialog.isEdit = true
    configDialog.serviceName = row.service_name
    configDialog.fileName = row.file_name
    configDialog.format = row.format
    configDialog.content = row.content
  } else {
    configDialog.isEdit = false
    configDialog.serviceName = 'application'
    configDialog.fileName = ''
    configDialog.format = 'yaml'
    configDialog.content = ''
  }
  configDialog.visible = true
}

async function submitConfig() {
  if (!configDialog.serviceName || !configDialog.fileName) {
    ElMessage.warning('服务名与文件名不能为空')
    return
  }
  const body = { serviceName: configDialog.serviceName, fileName: configDialog.fileName, format: configDialog.format, content: configDialog.content }
  const res = configDialog.isEdit
    ? await request('api/configs', 'PUT', body)
    : await request('api/configs', 'POST', body)
  if (res.updated || res.created) {
    ElMessage.success(configDialog.isEdit ? '已保存，订阅服务约 5s 后自动刷新' : '已新增')
    configDialog.visible = false
    fetchAll()
  } else {
    ElMessage.error('保存失败，请检查文件是否已存在')
  }
}

async function deleteConfig(row) {
  await ElMessageBox.confirm(`确认删除配置文件 ${row.service_name} / ${row.file_name}？`, '删除配置文件', { type: 'warning' })
  const res = await request(`api/configs/${row.service_name}/${row.file_name}`, 'DELETE')
  res.deleted ? ElMessage.success('已删除') : ElMessage.error('删除失败')
  fetchAll()
}

function secondsAgo(time) {
  if (!serverTime) return '-'
  return Math.max(0, Math.round((serverTime - new Date(time)) / 1000))
}

function formatTime(time) {
  return new Date(time).toLocaleString('zh-CN', { hour12: false })
}

function statusTag(status) {
  if (status === 'UP') return 'success'
  if (status === 'OFFLINE') return 'warning'
  return 'info'
}

onMounted(() => {
  fetchAll()
  timer = setInterval(fetchAll, pollInterval)
})

onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.console {
  max-width: 1200px;
  margin: 24px auto;
  padding: 0 16px;
  font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.header h1 {
  font-size: 22px;
  margin: 0 0 4px;
}
.sub {
  color: #909399;
  font-size: 13px;
}
.cards {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
.stat {
  text-align: center;
  padding: 8px 0;
}
.num {
  font-size: 28px;
  font-weight: 600;
}
.ok { color: #67c23a; }
.bad { color: #f56c6c; }
.label {
  color: #909399;
  font-size: 13px;
  margin-top: 4px;
}
.stale { color: #f56c6c; }
.value {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}
.toolbar {
  margin-bottom: 12px;
}
.preview {
  display: block;
  max-height: 60px;
  overflow: hidden;
  white-space: pre-wrap;
}
.code-editor :deep(textarea) {
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
}
</style>
