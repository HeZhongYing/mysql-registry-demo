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
      <el-card shadow="never"><div class="stat"><div class="num bad">{{ overview.instanceDown }}</div><div class="label">离线实例 (DOWN)</div></div></el-card>
      <el-card shadow="never"><div class="stat"><div class="num">{{ overview.configCount }}</div><div class="label">配置项</div></div></el-card>
    </div>

    <el-tabs v-model="tab">
      <el-tab-pane label="服务实例" name="instances">
        <el-table :data="instances" border stripe size="default">
          <el-table-column prop="service_name" label="服务名" width="180">
            <template #default="{ row }">
              <el-icon style="margin-right:4px"><Cpu /></el-icon>{{ row.service_name }}
            </template>
          </el-table-column>
          <el-table-column prop="instance_id" label="实例 ID" width="180" />
          <el-table-column label="地址" width="200">
            <template #default="{ row }">{{ row.host }}:{{ row.port }}</template>
          </el-table-column>
          <el-table-column label="状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'UP' ? 'success' : 'info'" effect="dark">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="心跳距今" width="140" align="center">
            <template #default="{ row }">
              <span :class="{ stale: secondsAgo(row.last_heartbeat) > 30 }">{{ secondsAgo(row.last_heartbeat) }}s 前</span>
            </template>
          </el-table-column>
          <el-table-column prop="registered_at" label="注册时间" min-width="200">
            <template #default="{ row }">{{ formatTime(row.registered_at) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="配置中心" name="configs">
        <el-table :data="configs" border stripe size="default">
          <el-table-column prop="service_name" label="归属服务" width="180">
            <template #default="{ row }">
              <el-tag :type="row.service_name === 'application' ? 'warning' : 'primary'" effect="plain">{{ row.service_name }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="config_key" label="配置键" width="240" />
          <el-table-column prop="config_value" label="配置值" min-width="240">
            <template #default="{ row }">
              <code class="value">{{ row.config_value }}</code>
            </template>
          </el-table-column>
          <el-table-column prop="version" label="版本" width="90" align="center" />
          <el-table-column prop="updated_at" label="更新时间" min-width="200">
            <template #default="{ row }">{{ formatTime(row.updated_at) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { Cpu } from '@element-plus/icons-vue'

const pollInterval = 3000
const tab = ref('instances')
const connected = ref(false)
const overview = ref({})
const instances = ref([])
const configs = ref([])
let timer = null
let serverTime = null

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

function secondsAgo(time) {
  if (!serverTime) return '-'
  return Math.max(0, Math.round((serverTime - new Date(time)) / 1000))
}

function formatTime(time) {
  return new Date(time).toLocaleString('zh-CN', { hour12: false })
}

onMounted(() => {
  fetchAll()
  timer = setInterval(fetchAll, pollInterval)
})

onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.console {
  max-width: 1100px;
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
  grid-template-columns: repeat(4, 1fr);
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
</style>
