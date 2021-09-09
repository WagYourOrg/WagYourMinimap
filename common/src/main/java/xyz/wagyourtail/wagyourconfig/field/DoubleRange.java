package xyz.wagyourtail.wagyourconfig.field;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DoubleRange {
    double from();

    double to();

    int steps() default 20;

}
