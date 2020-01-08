package com.cloudXF.core.check.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: CheckPattern
 * @Description: 验证注解的元素值与指定的正则表达式匹配
 * @Type: CharSequence子类型（CharBuffer、String、StringBuffer、StringBuilder）
 * @Group 2
 * @Author: MaoWei
 * @Date: 2020/1/3 14:44
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckPattern {
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
     * 正则表达式
     * @return
     */
    String regexp();

    /**
     * 正则模式 find matches
     * @return
     */
    String mode();

}
