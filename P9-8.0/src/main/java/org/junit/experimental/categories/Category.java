package org.junit.experimental.categories;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.validator.ValidateWith;

@Inherited
@ValidateWith(CategoryValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Category {
    Class<?>[] value();
}
