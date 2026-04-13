/**
 * 待办表格「操作」按钮文案：物业费强调缴费动作；报修/投诉仅跳转列表查看记录，无独立进度条，统一为「查看」。
 */
export function todoActionButtonLabel(row) {
  if (!row) return '去处理'
  const action = row.action
  if (action === '/property-fee') return '去缴费'
  if (action === '/repair' || action === '/complaint') return '查看'
  return '去处理'
}
