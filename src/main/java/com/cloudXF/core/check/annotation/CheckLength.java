package com.cloudXF.core.check.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: CheckLength
 * @Description: 验证注解的元素值长度在min和max（包含）指定区间之内
 * @Type: CharSequence子类型（CharBuffer、String、StringBuffer、StringBuilder）
 * @Group 2
 * @Author: MaoWei
 * @Date: 2020/1/3 14:44
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckLength {
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
     * 最小
     * @return
     */
    int min() default 0;

    /**
     * 最大
     * @return
     */
    int max() default Integer.MAX_VALUE;
}
