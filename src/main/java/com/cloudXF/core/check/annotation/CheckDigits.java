package com.cloudXF.core.check.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: CheckDigits
 * @Description: 验证注解的元素值的整数位数和小数位数上限
 * @Type: BigDecimal、BigInteger、byte、short、int、long、float、double等任何Number或CharSequence（存储的是数字）子类型
 * @Group 4/1/2
 * @Author: MaoWei
 * @Date: 2020/1/3 14:44
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckDigits {
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
     * 整数上限
     * @return
     */
    int integer();

    /**
     * 小数上限
     * @return
     */
    int fraction();

}
