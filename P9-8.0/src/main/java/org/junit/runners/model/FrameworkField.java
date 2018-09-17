package org.junit.runners.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class FrameworkField extends FrameworkMember<FrameworkField> {
    private final Field field;

    FrameworkField(Field field) {
        if (field == null) {
            throw new NullPointerException("FrameworkField cannot be created without an underlying field.");
        }
        this.field = field;
    }

    public String getName() {
        return getField().getName();
    }

    public Annotation[] getAnnotations() {
        return this.field.getAnnotations();
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return this.field.getAnnotation(annotationType);
    }

    public boolean isShadowedBy(FrameworkField otherMember) {
        return otherMember.getName().equals(getName());
    }

    protected int getModifiers() {
        return this.field.getModifiers();
    }

    public Field getField() {
        return this.field;
    }

    public Class<?> getType() {
        return this.field.getType();
    }

    public Class<?> getDeclaringClass() {
        return this.field.getDeclaringClass();
    }

    public Object get(Object target) throws IllegalArgumentException, IllegalAccessException {
        return this.field.get(target);
    }

    public String toString() {
        return this.field.toString();
    }
}
