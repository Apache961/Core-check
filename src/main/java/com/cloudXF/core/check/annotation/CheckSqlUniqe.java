package com.cloudXF.core.check.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: CheckSqlUniqe
 * @Description: 验证数据库是否唯一
 * @Type: CharSequence子类型（CharBuffer、String、StringBuffer、StringBuilder）
 * @Group 2
 * @Author: MaoWei
 * @Date: 2020/1/3 14:44
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckSqlUniqe {
    /**
     * 自定义校验信息
     * @return
     */
    String message() default "";

    /**
     * 分组
     * @return
     */
    String[] groups() default {};

    /**
     * 表名
     * @return
     */
    String tableName();

    /**
     * 字段
     * @return
     */
    String columName();

}
