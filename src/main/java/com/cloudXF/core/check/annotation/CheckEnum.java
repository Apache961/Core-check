package com.cloudXF.core.check.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: CheckEnum
 * @Description: 验证枚举匹配
 * @Type: CharSequence子类型（CharBuffer、String、StringBuffer、StringBuilder）、byte、short、int、long及封装类型
 * @Group 2/1
 * @Author: MaoWei
 * @Date: 2020/1/3 14:44
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckEnum {
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
     * 枚举存在的类别
     * @return
     */
    String[] values();
}
