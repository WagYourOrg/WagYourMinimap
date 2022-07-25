package xyz.wagyourtail.config.field;

import com.mojang.brigadier.arguments.ArgumentType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface BrigadierOptionsOverride {
    Class<? extends ArgumentType> value();
    String constructor() default "<init>";
    String[] getter();
}
