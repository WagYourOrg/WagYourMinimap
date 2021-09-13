package xyz.wagyourtail.config.field;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IntRange {
    int from();

    int to();

    int stepVal() default 1;

}
