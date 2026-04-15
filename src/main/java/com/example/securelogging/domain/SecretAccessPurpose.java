package com.example.securelogging.domain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Required marker for code that intentionally reveals raw secret values.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.LOCAL_VARIABLE)
public @interface SecretAccessPurpose {
    String value();
}
