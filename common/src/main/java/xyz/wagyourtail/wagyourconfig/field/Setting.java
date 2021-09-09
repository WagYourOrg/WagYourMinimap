package xyz.wagyourtail.wagyourconfig.field;

import java.lang.annotation.*;

/**
 * annotate that a field should be settable by the setting screen in game
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Setting {
    /**
     * @return translation key for setting name
     */
    String value();

    /**
     * @return name of function to get value, required for non-public fields
     */
    String getter() default "";

    /**
     * @return name of function to set value, required for non-public fields
     */
    String setter() default "";

    /**
     * @return name of function to get choices for value, if enum this will default to using enum values if not set
     */
    String options() default "";

    /**
     * @return name of function (boolean returning) that determines wether this field should be disabled.
     */
    String enabled() default "";

}
