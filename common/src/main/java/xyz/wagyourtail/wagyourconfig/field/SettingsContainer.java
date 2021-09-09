package xyz.wagyourtail.wagyourconfig.field;

import java.lang.annotation.*;

/**
 * annotate that a field is a valid pure subconfig.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SettingsContainer {

    /**
     * @return translation key for class
     */
    String value();

}
