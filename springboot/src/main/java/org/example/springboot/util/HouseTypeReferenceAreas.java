package org.example.springboot.util;

import org.example.springboot.entity.House;
import org.example.springboot.entity.HouseType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 户型参考建筑面积（㎡）写死配置，与业务约定的四种类型名称严格一致。
 */
public final class HouseTypeReferenceAreas {

    private static final Map<String, BigDecimal> BY_EXACT_NAME;

    static {
        Map<String, BigDecimal> m = new HashMap<>();
        m.put("普通住宅", new BigDecimal("90"));
        m.put("高层A户型", new BigDecimal("120"));
        m.put("高层B户型", new BigDecimal("110"));
        m.put("洋房C户型", new BigDecimal("150"));
        BY_EXACT_NAME = Collections.unmodifiableMap(m);
    }

    private HouseTypeReferenceAreas() {
    }

    /**
     * 按类型名称填充 {@link HouseType#setReferenceArea}；非约定名称不修改（保持库内值或 null）。
     */
    public static void apply(HouseType type) {
        if (type == null || type.getName() == null) {
            return;
        }
        if (type.getReferenceArea() != null) {
            return;
        }
        BigDecimal fixed = BY_EXACT_NAME.get(type.getName().trim());
        if (fixed != null) {
            type.setReferenceArea(fixed);
        }
    }

    public static void apply(List<HouseType> types) {
        if (types == null) {
            return;
        }
        for (HouseType t : types) {
            apply(t);
        }
    }

    /**
     * 物业费计费面积：约定四种户型用写死面积，否则用房屋档案中的建筑面积。
     */
    public static BigDecimal areaForBilling(House house, HouseType type) {
        if (type != null && type.getName() != null) {
            BigDecimal fixed = BY_EXACT_NAME.get(type.getName().trim());
            if (fixed != null) {
                return fixed;
            }
        }
        return house != null ? house.getArea() : null;
    }
}
