package me.liuwentao.rpc.core.annotation;import java.lang.annotation.*;/** * Created by liuwentao on 2021/7/21 16:59 * * 这个类暂时未使用 */@Target(ElementType.FIELD)@Retention(RetentionPolicy.RUNTIME)@Inheritedpublic @interface RpcReference {    public String group() default "defaultGroup";    public String version() default "defaultVersion";}