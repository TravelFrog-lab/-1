/**
 * 待办「操作」跳转：物业费 → 命名路由 AlipayPayment，并带 autoPay=1 由支付页一键拉起沙箱。
 */
function parsePropertyFeeId(row) {
  if (row.propertyFeeId != null) return Number(row.propertyFeeId)
  const id = row.id
  if (typeof id === 'string' && id.startsWith('pf-')) {
    const n = Number(id.slice(3))
    return Number.isFinite(n) ? n : null
  }
  return null
}

function parseAmountFromDescription(desc) {
  if (!desc || typeof desc !== 'string') return NaN
  const m = desc.match(/金额[：:]\s*[¥￥]?\s*([\d.]+)/)
  return m ? parseFloat(m[1]) : NaN
}

export function navigateTodoItem(router, row) {
  if (!row || !row.action) return
  if (row.action === '/property-fee') {
    let amountRaw = row.feeAmount != null ? row.feeAmount : row.amount
    let amountNum = amountRaw != null && amountRaw !== '' ? Number(amountRaw) : NaN
    if (!Number.isFinite(amountNum) || amountNum <= 0) {
      amountNum = parseAmountFromDescription(row.description)
    }
    const pid = parsePropertyFeeId(row)
    if (!Number.isFinite(amountNum) || amountNum <= 0) {
      router.push({ name: 'PropertyFee' })
      return
    }
    let ym = row.feeMonthYm != null ? String(row.feeMonthYm) : ''
    if (!ym && row.deadline && String(row.deadline).length >= 7) {
      ym = String(row.deadline).slice(0, 7)
    }
    const businessOrderNo = row.orderNo ? String(row.orderNo) : (pid != null ? `ID:${pid}` : '')
    const subject = ym ? `物业费缴纳 - ${ym}` : '物业费缴纳'
    router.push({
      name: 'AlipayPayment',
      query: {
        amount: String(amountNum),
        subject,
        businessType: 'PROPERTY_FEE',
        businessOrderNo,
        autoPay: '1'
      }
    })
    return
  }
  router.push(row.action)
}
