package org.junit.validator;

import java.util.Collections;
import java.util.List;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

public abstract class AnnotationValidator {
    private static final List<Exception> NO_VALIDATION_ERRORS = Collections.emptyList();

    public List<Exception> validateAnnotatedClass(TestClass testClass) {
        return NO_VALIDATION_ERRORS;
    }

    public List<Exception> validateAnnotatedField(FrameworkField field) {
        return NO_VALIDATION_ERRORS;
    }

    public List<Exception> validateAnnotatedMethod(FrameworkMethod method) {
        return NO_VALIDATION_ERRORS;
    }
}
