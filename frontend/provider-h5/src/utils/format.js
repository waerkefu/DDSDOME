/** 订单类型中文映射 */
export const ORDER_TYPE_MAP = {
  IMMEDIATE: '即时代驾',
  SCHEDULED: '预约代驾',
  INSPECTION: '年审代办',
  RENEWAL: '换证代办'
}

/** 订单状态中文映射 */
export const ORDER_STATUS_MAP = {
  PENDING_ACCEPT: '待接单',
  ACCEPTED: '已接单',
  IN_SERVICE: '服务中',
  PENDING_PAYMENT: '待支付',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  CLOSED: '超时关闭'
}

/** 订单状态标签颜色（element-plus tag type） */
export const ORDER_STATUS_TAG = {
  PENDING_ACCEPT: 'warning',
  ACCEPTED: 'primary',
  IN_SERVICE: 'primary',
  PENDING_PAYMENT: 'danger',
  COMPLETED: 'success',
  CANCELLED: 'info',
  CLOSED: 'info'
}

/** 我的订单页状态筛选项 */
export const ORDER_STATUS_FILTERS = [
  { label: '全部', value: '' },
  { label: '已接单', value: 'ACCEPTED' },
  { label: '服务中', value: 'IN_SERVICE' },
  { label: '待支付', value: 'PENDING_PAYMENT' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

/**
 * 金额（分）转「¥x.xx」
 */
export function formatYuan(fen) {
  const num = Number(fen)
  if (fen === null || fen === undefined || fen === '' || Number.isNaN(num)) {
    return '¥0.00'
  }
  return `¥${(num / 100).toFixed(2)}`
}

/**
 * 金额优先取「分」字段用 formatYuan 展示；为空时回退后端返回的 Yuan 字符串
 */
export function displayYuan(fen, yuan) {
  if (fen !== null && fen !== undefined && fen !== '') {
    return formatYuan(fen)
  }
  if (yuan !== null && yuan !== undefined && yuan !== '') {
    return `¥${Number(yuan).toFixed(2)}`
  }
  return '-'
}

/**
 * 里程（米）转「x.x 公里」
 */
export function formatDistance(meters) {
  const num = Number(meters)
  if (meters === null || meters === undefined || meters === '' || Number.isNaN(num)) {
    return '0.0 公里'
  }
  return `${(num / 1000).toFixed(1)} 公里`
}

/**
 * 时长（秒）转「x 分钟」
 */
export function formatDuration(seconds) {
  const num = Number(seconds)
  if (seconds === null || seconds === undefined || seconds === '' || Number.isNaN(num)) {
    return '0 分钟'
  }
  return `${Math.max(0, Math.round(num / 60))} 分钟`
}

/** 订单类型文案 */
export function orderTypeText(type, fallback) {
  return ORDER_TYPE_MAP[type] || fallback || type || '-'
}

/** 订单状态文案 */
export function orderStatusText(status, fallback) {
  return ORDER_STATUS_MAP[status] || fallback || status || '-'
}

/** 订单状态标签颜色 */
export function statusTagType(status) {
  return ORDER_STATUS_TAG[status] || 'info'
}

/**
 * 时间字符串格式化 2026-09-10T10:00:00 -> 2026-09-10 10:00:00
 */
export function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}
