package org.junit.validator;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.runners.model.Annotatable;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

public final class AnnotationsValidator implements TestClassValidator {
    private static final List<AnnotatableValidator<?>> VALIDATORS = Arrays.asList(new AnnotatableValidator[]{new ClassValidator(), new MethodValidator(), new FieldValidator()});

    private static abstract class AnnotatableValidator<T extends Annotatable> {
        private static final AnnotationValidatorFactory ANNOTATION_VALIDATOR_FACTORY = new AnnotationValidatorFactory();

        /* synthetic */ AnnotatableValidator(AnnotatableValidator -this0) {
            this();
        }

        abstract Iterable<T> getAnnotatablesForTestClass(TestClass testClass);

        abstract List<Exception> validateAnnotatable(AnnotationValidator annotationValidator, T t);

        private AnnotatableValidator() {
        }

        public List<Exception> validateTestClass(TestClass testClass) {
            List<Exception> validationErrors = new ArrayList();
            for (Annotatable annotatable : getAnnotatablesForTestClass(testClass)) {
                validationErrors.addAll(validateAnnotatable(annotatable));
            }
            return validationErrors;
        }

        private List<Exception> validateAnnotatable(T annotatable) {
            List<Exception> validationErrors = new ArrayList();
            for (Annotation annotation : annotatable.getAnnotations()) {
                ValidateWith validateWith = (ValidateWith) annotation.annotationType().getAnnotation(ValidateWith.class);
                if (validateWith != null) {
                    validationErrors.addAll(validateAnnotatable(ANNOTATION_VALIDATOR_FACTORY.createAnnotationValidator(validateWith), annotatable));
                }
            }
            return validationErrors;
        }
    }

    private static class ClassValidator extends AnnotatableValidator<TestClass> {
        /* synthetic */ ClassValidator(ClassValidator -this0) {
            this();
        }

        private ClassValidator() {
            super();
        }

        Iterable<TestClass> getAnnotatablesForTestClass(TestClass testClass) {
            return Collections.singletonList(testClass);
        }

        List<Exception> validateAnnotatable(AnnotationValidator validator, TestClass testClass) {
            return validator.validateAnnotatedClass(testClass);
        }
    }

    private static class FieldValidator extends AnnotatableValidator<FrameworkField> {
        /* synthetic */ FieldValidator(FieldValidator -this0) {
            this();
        }

        private FieldValidator() {
            super();
        }

        Iterable<FrameworkField> getAnnotatablesForTestClass(TestClass testClass) {
            return testClass.getAnnotatedFields();
        }

        List<Exception> validateAnnotatable(AnnotationValidator validator, FrameworkField field) {
            return validator.validateAnnotatedField(field);
        }
    }

    private static class MethodValidator extends AnnotatableValidator<FrameworkMethod> {
        /* synthetic */ MethodValidator(MethodValidator -this0) {
            this();
        }

        private MethodValidator() {
            super();
        }

        Iterable<FrameworkMethod> getAnnotatablesForTestClass(TestClass testClass) {
            return testClass.getAnnotatedMethods();
        }

        List<Exception> validateAnnotatable(AnnotationValidator validator, FrameworkMethod method) {
            return validator.validateAnnotatedMethod(method);
        }
    }

    public List<Exception> validateTestClass(TestClass testClass) {
        List<Exception> validationErrors = new ArrayList();
        for (AnnotatableValidator<?> validator : VALIDATORS) {
            validationErrors.addAll(validator.validateTestClass(testClass));
        }
        return validationErrors;
    }
}
