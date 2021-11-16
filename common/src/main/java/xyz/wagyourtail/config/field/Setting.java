package xyz.wagyourtail.config.field;

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
     * @return name of function (boolean returning) that determines whether this field should be disabled.
     */
    String enabled() default "";

    /**
     * @return duplicate option boolean (only relevant for Array type)
     */
    boolean allowDuplicateOption() default false;

    /**
     * @return should use getter/setter in the serializer
     */
    boolean useFunctionsToSerialize() default true;

    /**
     * setting gui should use this field type instead, this may require a getter/setter depending on the type
     * you can also use to specify a map/collection implementation, in which case it MUST be an implementation
     * @return type of field
     */
    Class<?> overrideType() default void.class;

    /**
     * @return component type for Map Value and Collections
     */
    Class<?> elementType() default void.class;
}
