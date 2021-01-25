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
    private static final List<AnnotatableValidator<?>> VALIDATORS = Arrays.asList(new ClassValidator(), new MethodValidator(), new FieldValidator());

    @Override // org.junit.validator.TestClassValidator
    public List<Exception> validateTestClass(TestClass testClass) {
        List<Exception> validationErrors = new ArrayList<>();
        for (AnnotatableValidator<?> validator : VALIDATORS) {
            validationErrors.addAll(validator.validateTestClass(testClass));
        }
        return validationErrors;
    }

    private static abstract class AnnotatableValidator<T extends Annotatable> {
        private static final AnnotationValidatorFactory ANNOTATION_VALIDATOR_FACTORY = new AnnotationValidatorFactory();

        /* access modifiers changed from: package-private */
        public abstract Iterable<T> getAnnotatablesForTestClass(TestClass testClass);

        /* access modifiers changed from: package-private */
        public abstract List<Exception> validateAnnotatable(AnnotationValidator annotationValidator, T t);

        private AnnotatableValidator() {
        }

        public List<Exception> validateTestClass(TestClass testClass) {
            List<Exception> validationErrors = new ArrayList<>();
            for (T annotatable : getAnnotatablesForTestClass(testClass)) {
                validationErrors.addAll(validateAnnotatable(annotatable));
            }
            return validationErrors;
        }

        private List<Exception> validateAnnotatable(T annotatable) {
            List<Exception> validationErrors = new ArrayList<>();
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
        private ClassValidator() {
            super();
        }

        /* access modifiers changed from: package-private */
        @Override // org.junit.validator.AnnotationsValidator.AnnotatableValidator
        public Iterable<TestClass> getAnnotatablesForTestClass(TestClass testClass) {
            return Collections.singletonList(testClass);
        }

        /* access modifiers changed from: package-private */
        public List<Exception> validateAnnotatable(AnnotationValidator validator, TestClass testClass) {
            return validator.validateAnnotatedClass(testClass);
        }
    }

    private static class MethodValidator extends AnnotatableValidator<FrameworkMethod> {
        private MethodValidator() {
            super();
        }

        /* access modifiers changed from: package-private */
        @Override // org.junit.validator.AnnotationsValidator.AnnotatableValidator
        public Iterable<FrameworkMethod> getAnnotatablesForTestClass(TestClass testClass) {
            return testClass.getAnnotatedMethods();
        }

        /* access modifiers changed from: package-private */
        public List<Exception> validateAnnotatable(AnnotationValidator validator, FrameworkMethod method) {
            return validator.validateAnnotatedMethod(method);
        }
    }

    private static class FieldValidator extends AnnotatableValidator<FrameworkField> {
        private FieldValidator() {
            super();
        }

        /* access modifiers changed from: package-private */
        @Override // org.junit.validator.AnnotationsValidator.AnnotatableValidator
        public Iterable<FrameworkField> getAnnotatablesForTestClass(TestClass testClass) {
            return testClass.getAnnotatedFields();
        }

        /* access modifiers changed from: package-private */
        public List<Exception> validateAnnotatable(AnnotationValidator validator, FrameworkField field) {
            return validator.validateAnnotatedField(field);
        }
    }
}
