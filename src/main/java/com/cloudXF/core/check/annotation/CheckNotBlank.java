package com.cloudXF.core.check.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: CheckNotBlank
 * @Description: 验证注解的元素值不为空（不为null、去除首尾空格后长度不为0），不同于@NotEmpty，@NotBlank只应用于字符串且在比较时会去除字符串的首尾空格
 * @Type: CharSequence子类型（CharBuffer、String、StringBuffer、StringBuilder）
 * @Group 2
 * @Author: MaoWei
 * @Date: 2020/1/3 14:44
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckNotBlank {
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
