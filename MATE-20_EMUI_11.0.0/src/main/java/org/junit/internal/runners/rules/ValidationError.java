package org.junit.internal.runners.rules;

import java.lang.annotation.Annotation;
import org.junit.runners.model.FrameworkMember;

class ValidationError extends Exception {
    public ValidationError(FrameworkMember<?> member, Class<? extends Annotation> annotation, String suffix) {
        super(String.format("The @%s '%s' %s", annotation.getSimpleName(), member.getName(), suffix));
    }
}
