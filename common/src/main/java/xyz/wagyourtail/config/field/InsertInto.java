package xyz.wagyourtail.config.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * inserts this setting/subsetting into a different class's settings list,
 * this is useful for grouping settings together from different parts/components
 * <br>
 * this does not work for inserting from SettingContainer lists (or subsettings of said lists)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InsertInto {
    Class<?>[] value() default {};
}
