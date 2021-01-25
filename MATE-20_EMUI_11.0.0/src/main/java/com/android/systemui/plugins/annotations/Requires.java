package com.android.systemui.plugins.annotations;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Repeatable(Requirements.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Requires {
    Class<?> target();

    int version();
}
