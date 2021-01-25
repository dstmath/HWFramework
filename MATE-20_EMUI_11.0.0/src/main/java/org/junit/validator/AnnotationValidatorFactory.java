package org.junit.validator;

import java.util.concurrent.ConcurrentHashMap;

public class AnnotationValidatorFactory {
    private static final ConcurrentHashMap<ValidateWith, AnnotationValidator> VALIDATORS_FOR_ANNOTATION_TYPES = new ConcurrentHashMap<>();

    public AnnotationValidator createAnnotationValidator(ValidateWith validateWithAnnotation) {
        AnnotationValidator validator = VALIDATORS_FOR_ANNOTATION_TYPES.get(validateWithAnnotation);
        if (validator != null) {
            return validator;
        }
        Class<? extends AnnotationValidator> clazz = validateWithAnnotation.value();
        if (clazz != null) {
            try {
                VALIDATORS_FOR_ANNOTATION_TYPES.putIfAbsent(validateWithAnnotation, (AnnotationValidator) clazz.newInstance());
                return VALIDATORS_FOR_ANNOTATION_TYPES.get(validateWithAnnotation);
            } catch (Exception e) {
                throw new RuntimeException("Exception received when creating AnnotationValidator class " + clazz.getName(), e);
            }
        } else {
            throw new IllegalArgumentException("Can't create validator, value is null in annotation " + validateWithAnnotation.getClass().getName());
        }
    }
}
