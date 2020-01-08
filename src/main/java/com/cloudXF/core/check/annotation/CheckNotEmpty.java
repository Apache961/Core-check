package com.cloudXF.core.check.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: CheckNotEmpty
 * @Description: 验证注解的元素值不为null且不为空（字符串长度不为0、集合大小不为0）
 * @Type: CharSequence子类型（CharBuffer、String、StringBuffer、StringBuilder）、Collection、Map、数组
 * @Group 2/5
 * @Author: MaoWei
 * @Date: 2020/1/3 14:44
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckNotEmpty {
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
}
