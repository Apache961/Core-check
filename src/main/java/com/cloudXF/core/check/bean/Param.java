package com.cloudXF.core.check.bean;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @ClassName: Param
 * @Description: 字段详细信息
 * @Author: MaoWei
 * @Date: 2020/1/3 16:00
 **/
public class Param {
    /**
     * 类名
     */
    private String className;
    /**
     * 字段名
     */
    private String fieldName;
    /**
     * 顺序
     */
    private int sort;
    /**
     * 字段属性
     */
    private Field field;
    /**
     * 属性值
     */
    private Object value;

    public Param() {
        super();
    }

    public Param(String className, String fieldName, int sort, Field field, Object value) {
        this.className = className;
        this.fieldName = fieldName;
        this.sort = sort;
        this.field = field;
        this.value = value;
    }

    public String getClassName() {
        return className;
    }

    public Param setClassName(String className) {
        this.className = className;
        return this;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Param setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public int getSort() {
        return sort;
    }

    public Param setSort(int sort) {
        this.sort = sort;
        return this;
    }

    public Field getField() {
        return field;
    }

    public Param setField(Field field) {
        this.field = field;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public Param setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return "Param{" +
                "className='" + className + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", sort=" + sort +
                ", field=" + field +
                ", value=" + value +
                '}';
    }
}
