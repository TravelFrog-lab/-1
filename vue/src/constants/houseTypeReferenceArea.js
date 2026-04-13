/**
 * 户型参考建筑面积（㎡）写死配置，须与后端 HouseTypeReferenceAreas 及库中类型名称一致。
 */
export const HOUSE_TYPE_REFERENCE_AREA_M2 = Object.freeze({
  普通住宅: 90,
  高层A户型: 120,
  高层B户型: 110,
  洋房C户型: 150
})

export function getHouseTypeReferenceAreaM2ByName (name) {
  if (name == null || typeof name !== 'string') return null
  const key = name.trim()
  return Object.prototype.hasOwnProperty.call(HOUSE_TYPE_REFERENCE_AREA_M2, key)
    ? HOUSE_TYPE_REFERENCE_AREA_M2[key]
    : null
}

/** 列表/导出展示用 */
export function formatHouseTypeReferenceArea (name) {
  const v = getHouseTypeReferenceAreaM2ByName(name)
  return v != null ? `${v} ㎡` : '-'
}

/**
 * 物业费/房屋信息：优先约定户型面积，否则用单套房屋面积
 */
export function displayHouseAreaM2ForHouse (house) {
  if (!house) return '-'
  const hard = house.houseType && getHouseTypeReferenceAreaM2ByName(house.houseType.name)
  if (hard != null) return hard
  if (house.area != null && house.area !== '') return house.area
  return '-'
}
