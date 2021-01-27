package org.junit.runners.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class FrameworkField extends FrameworkMember<FrameworkField> {
    private final Field field;

    FrameworkField(Field field2) {
        if (field2 != null) {
            this.field = field2;
            return;
        }
        throw new NullPointerException("FrameworkField cannot be created without an underlying field.");
    }

    @Override // org.junit.runners.model.FrameworkMember
    public String getName() {
        return getField().getName();
    }

    @Override // org.junit.runners.model.Annotatable
    public Annotation[] getAnnotations() {
        return this.field.getAnnotations();
    }

    @Override // org.junit.runners.model.Annotatable
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return (T) this.field.getAnnotation(annotationType);
    }

    public boolean isShadowedBy(FrameworkField otherMember) {
        return otherMember.getName().equals(getName());
    }

    /* access modifiers changed from: protected */
    @Override // org.junit.runners.model.FrameworkMember
    public int getModifiers() {
        return this.field.getModifiers();
    }

    public Field getField() {
        return this.field;
    }

    @Override // org.junit.runners.model.FrameworkMember
    public Class<?> getType() {
        return this.field.getType();
    }

    @Override // org.junit.runners.model.FrameworkMember
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
