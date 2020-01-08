package com.cloudXF.core.check.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: EnableCheck
 * @Description: 验证注解的元素值是false
 * @Type: Boolean、boolean
 * @Author: MaoWei
 * @Date: 2020/1/3 14:44
 **/
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableCheck {

    /**
     * 有效分组
     * @return
     */
    String[] effectGroup() default {};

}
