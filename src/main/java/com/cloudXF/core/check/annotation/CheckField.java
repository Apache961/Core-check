package com.cloudXF.core.check.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: CheckField
 * @Description: 校验字段注解
 * @Author: MaoWei
 * @Date: 2020/1/3 14:44
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckField {
    /**
     * 属性名称
     * @return
     */
    String name();

    /**
     * 属性顺序
     * @return
     */
    int sort();

    /**
     * 属性描述
     * @return
     */
    String describe() default "";
}
