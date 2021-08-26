package xyz.wagyourtail.minimap.api.config;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DoubleRange {
    double from();
    double to();
}
