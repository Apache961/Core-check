package com.cloudXF.core.check.enumd;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: SystemTypeEnum
 * @Description: 系统类型枚举
 * @Author: MaoWei
 * @Date: 2020/1/4 9:42
 **/
public enum SystemTypeEnum {
    // 整型类型
    BYTE_BASIC(byte.class, 1),
    SHORT_BASIC(short.class, 1),
    INT_BASIC(int.class, 1),
    LONG_BASIC(long.class, 1),
    BYTE(Byte.class, 1),
    SHORT(Short.class, 1),
    INT(Integer.class, 1),
    LONG(Long.class, 1),
    // 布尔类型
    BOOLEAN(Boolean.class, 6),
    BOOLEAN_BASIC(boolean.class, 6),
    // 字符类型
    STRING(String.class, 2),
    STRINGBUFFER(StringBuffer.class, 2),
    STRINGBUILDER(StringBuilder.class, 2),
    // 日期类型
    DATE(java.util.Date.class, 3),
    // 浮点类型
    BIGINTEGER(java.math.BigInteger.class, 4),
    BIGDECIMAL(java.math.BigDecimal.class, 4),
    // 集合类型
    COLLECTION(java.util.Collection.class, 5),
    LIST(java.util.List.class, 5),
    SET(java.util.Set.class, 5),
    MAP(java.util.Map.class, 5),
    ARRAYLIST(java.util.ArrayList.class, 5),
    LINKEDLIST(java.util.LinkedList.class, 5),
    HASHSET(java.util.HashSet.class, 5),
    LINKEDHASHSET(java.util.LinkedHashSet.class, 5),
    HASHMAP(java.util.HashMap.class, 5),
    LINKEDHASHMAP(java.util.LinkedHashMap.class, 5)
    ;

    private Class<?> type;

    private int group;

    SystemTypeEnum(Class<?> type, int group) {
        this.type = type;
        this.group = group;
    }

    public Class<?> getType() {
        return type;
    }

    public SystemTypeEnum setType(Class<?> type) {
        this.type = type;
        return this;
    }

    public int getGroup() {
        return group;
    }

    public SystemTypeEnum setGroup(int group) {
        this.group = group;
        return this;
    }

    public static List<Class<?>> getAll() {
        List<Class<?>> classList = new ArrayList<Class<?>>();
        SystemTypeEnum[] values = SystemTypeEnum.values();
        for (SystemTypeEnum val : values) {
            classList.add(val.getType());
        }
        return classList;
    }

    public static List<Class<?>> getAllCollection() {
        List<Class<?>> classList = new ArrayList<Class<?>>();
        SystemTypeEnum[] values = SystemTypeEnum.values();
        for (SystemTypeEnum val : values) {
            if (val.getGroup() == 5) {
                classList.add(val.getType());
            }
        }
        return classList;
    }

    public static int getGroup(Class<?> clazz) {
        SystemTypeEnum[] values = SystemTypeEnum.values();
        for (SystemTypeEnum val : values) {
            if (clazz.equals(val.getType())) {
                return val.getGroup();
            }
        }
        return 0;
    }
}
