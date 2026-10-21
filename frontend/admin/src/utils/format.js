/**
 * 金额格式化工具。
 * 后端所有金额字段均以「分」为单位传输。
 */

/** 分 → 元（保留两位小数的字符串） */
export function formatYuan(fen) {
  const value = Number(fen)
  if (fen === null || fen === undefined || fen === '' || Number.isNaN(value)) {
    return '0.00'
  }
  return (value / 100).toFixed(2)
}

/** 分 → 元（数字，用于表单回显） */
export function fenToYuan(fen) {
  return Number(formatYuan(fen))
}

/** 元 → 分（四舍五入取整，用于提交） */
export function yuanToFen(yuan) {
  const value = Number(yuan)
  if (yuan === null || yuan === undefined || yuan === '' || Number.isNaN(value)) {
    return 0
  }
  return Math.round(value * 100)
}

/** 金额展示（带 ¥ 符号） */
export function formatAmount(fen) {
  return `¥${formatYuan(fen)}`
}

/** 时间展示：兼容 ISO 与 yyyy-MM-dd HH:mm:ss */
export function formatDateTime(value) {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ').replace(/\.\d+$/, '')
}
