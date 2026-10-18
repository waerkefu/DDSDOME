package com.daijia.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口所需角色，可标注在类或方法上（方法级优先于类级）
 */
@Target({ElementType.METHOD, ElementType.TYPE})// 可标注在类或方法上
@Retention(RetentionPolicy.RUNTIME)// 运行时注解，可被反射获取
//自定义注解
//用于标注接口所需角色，可标注在类或方法上（方法级优先于类级）默认要求同时具备全部角色
public @interface RequireRole {

    /** 允许访问的角色编码 */
    String[] value();

    /** 是否要求同时具备全部角色，默认具备其一即可 */
    boolean requireAll() default false;
}