package xyz.wagyourtail.minimap.api.config;

import java.lang.annotation.*;

/**
 * annotate that a class is a valid config class/subclass.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SettingsContainer {

    /**
     * @return translation key for setting groups
     */
    String value() default "gui.wagyourminimap.settings.general";
}
