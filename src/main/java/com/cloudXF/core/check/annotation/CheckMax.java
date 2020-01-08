package com.cloudXF.core.check.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: CheckMax
 * @Description: 验证注解的元素值小于等于@Max指定的value值
 * @Type: BigDecimal、BigInteger、byte、short、int、long、float、double等任何Number或CharSequence（存储的是数字）子类型
 * @Group 4/1/2
 * @Author: MaoWei
 * @Date: 2020/1/3 14:44
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckMax {
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
     * 指定数值
     * @return
     */
    String max();

}
