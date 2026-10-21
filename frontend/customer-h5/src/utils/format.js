/**
 * 金额与枚举的中文映射、格式化工具。
 * 后端金额统一以「分」为单位返回。
 */

/** 分 -> 「¥x.xx」 */
export function formatYuan(fen) {
  if (fen === null || fen === undefined || fen === '') return '¥0.00'
  const num = Number(fen)
  if (Number.isNaN(num)) return '¥0.00'
  return `¥${(num / 100).toFixed(2)}`
}

/** 订单类型中文 */
export const ORDER_TYPE_MAP = {
  IMMEDIATE: '即时代驾',
  SCHEDULED: '预约代驾',
  INSPECTION: '年审代办',
  RENEWAL: '换证代办'
}

/** 订单状态中文 */
export const ORDER_STATUS_MAP = {
  PENDING_ACCEPT: '待接单',
  ACCEPTED: '已接单',
  IN_SERVICE: '服务中',
  PENDING_PAYMENT: '待支付',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  CLOSED: '超时关闭'
}

/** 支付状态中文 */
export const PAY_STATUS_MAP = {
  UNPAID: '未支付',
  PAYING: '支付中',
  PAID: '已支付',
  REFUNDING: '退款中',
  PARTIAL_REFUNDED: '部分退款',
  REFUNDED: '已退款'
}

/** 订单状态对应的标签颜色（Element Plus el-tag type） */
const STATUS_TAG_TYPE = {
  PENDING_ACCEPT: 'warning',
  ACCEPTED: 'primary',
  IN_SERVICE: 'primary',
  PENDING_PAYMENT: 'danger',
  COMPLETED: 'success',
  CANCELLED: 'info',
  CLOSED: 'info'
}

/** 允许取消的订单状态 */
export const CANCELABLE_STATUS = ['PENDING_ACCEPT', 'ACCEPTED', 'PENDING_PAYMENT']

/** 需要选择预约时间的订单类型 */
export const BOOKING_REQUIRED_TYPES = ['SCHEDULED', 'INSPECTION', 'RENEWAL']

export function orderTypeText(type, fallback) {
  return ORDER_TYPE_MAP[type] || fallback || type || '-'
}

export function orderStatusText(status, fallback) {
  return ORDER_STATUS_MAP[status] || fallback || status || '-'
}

export function payStatusText(status, fallback) {
  return PAY_STATUS_MAP[status] || fallback || status || '-'
}

export function statusTagType(status) {
  return STATUS_TAG_TYPE[status] || 'info'
}

export function canCancel(status) {
  return CANCELABLE_STATUS.includes(status)
}

export function needBookingTime(orderType) {
  return BOOKING_REQUIRED_TYPES.includes(orderType)
}

/** 时间展示：2026-09-10T10:00:00 -> 2026-09-10 10:00 */
export function formatDateTime(value) {
  if (!value) return '-'
  const text = String(value).replace('T', ' ')
  return text.length >= 16 ? text.slice(0, 16) : text
}

/** 米 -> 公里（保留一位小数） */
export function formatKm(distanceM) {
  if (distanceM === null || distanceM === undefined) return '-'
  const num = Number(distanceM)
  if (Number.isNaN(num)) return '-'
  return `${(num / 1000).toFixed(1)} 公里`
}

/** 秒 -> 分钟展示 */
export function formatMinutes(durationS) {
  if (durationS === null || durationS === undefined) return '-'
  const num = Number(durationS)
  if (Number.isNaN(num)) return '-'
  return `${Math.max(1, Math.round(num / 60))} 分钟`
}

/** 生成幂等键 / 请求唯一串 */
export function randomKey(prefix = 'h5') {
  const rand = Math.random().toString(36).slice(2, 10)
  return `${prefix}-${Date.now().toString(36)}-${rand}`
}
