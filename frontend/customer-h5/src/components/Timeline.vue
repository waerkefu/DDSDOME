<template>
  <div class="timeline">
    <el-timeline v-if="items.length">
      <el-timeline-item
        v-for="(log, index) in items"
        :key="index"
        :type="index === 0 ? 'primary' : 'info'"
        :timestamp="formatDateTime(log.createTime)"
        placement="top"
        :hollow="index !== 0"
      >
        <div class="timeline__title">{{ log.toStatusDesc || orderStatusText(log.toStatus) }}</div>
        <div class="timeline__sub">
          <span v-if="log.fromStatusDesc || log.fromStatus">
            {{ log.fromStatusDesc || orderStatusText(log.fromStatus) }} →
          </span>
          <span>{{ log.toStatusDesc || orderStatusText(log.toStatus) }}</span>
        </div>
        <div v-if="log.operatorName" class="timeline__sub">操作人：{{ log.operatorName }}</div>
        <div v-if="log.remark" class="timeline__sub">备注：{{ log.remark }}</div>
      </el-timeline-item>
    </el-timeline>
    <div v-else class="empty">暂无状态流转记录</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { formatDateTime, orderStatusText } from '@/utils/format'

const props = defineProps({
  // 后端 OrderStatusLogVO 列表
  statusLogs: {
    type: Array,
    default: () => []
  }
})

// 最新的状态流转排在最前
const items = computed(() =>
  [...props.statusLogs].sort((a, b) => {
    const ta = a.createTime ? new Date(a.createTime).getTime() : 0
    const tb = b.createTime ? new Date(b.createTime).getTime() : 0
    return tb - ta
  })
)
</script>

<style scoped>
.timeline__title {
  font-weight: 600;
}

.timeline__sub {
  font-size: 12px;
  color: var(--color-text-sub);
  margin-top: 2px;
}
</style>
