package org.junit.runners.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import org.junit.internal.runners.model.ReflectiveCallable;

public class FrameworkMethod extends FrameworkMember<FrameworkMethod> {
    private final Method method;

    public FrameworkMethod(Method method) {
        if (method == null) {
            throw new NullPointerException("FrameworkMethod cannot be created without an underlying method.");
        }
        this.method = method;
    }

    public Method getMethod() {
        return this.method;
    }

    public Object invokeExplosively(final Object target, final Object... params) throws Throwable {
        return new ReflectiveCallable() {
            protected Object runReflectiveCall() throws Throwable {
                return FrameworkMethod.this.method.invoke(target, params);
            }
        }.run();
    }

    public String getName() {
        return this.method.getName();
    }

    public void validatePublicVoidNoArg(boolean isStatic, List<Throwable> errors) {
        validatePublicVoid(isStatic, errors);
        if (this.method.getParameterTypes().length != 0) {
            errors.add(new Exception("Method " + this.method.getName() + " should have no parameters"));
        }
    }

    public void validatePublicVoid(boolean isStatic, List<Throwable> errors) {
        if (isStatic() != isStatic) {
            errors.add(new Exception("Method " + this.method.getName() + "() " + (isStatic ? "should" : "should not") + " be static"));
        }
        if (!isPublic()) {
            errors.add(new Exception("Method " + this.method.getName() + "() should be public"));
        }
        if (this.method.getReturnType() != Void.TYPE) {
            errors.add(new Exception("Method " + this.method.getName() + "() should be void"));
        }
    }

    protected int getModifiers() {
        return this.method.getModifiers();
    }

    public Class<?> getReturnType() {
        return this.method.getReturnType();
    }

    public Class<?> getType() {
        return getReturnType();
    }

    public Class<?> getDeclaringClass() {
        return this.method.getDeclaringClass();
    }

    public void validateNoTypeParametersOnArgs(List<Throwable> errors) {
        new NoGenericTypeParametersValidator(this.method).validate(errors);
    }

    public boolean isShadowedBy(FrameworkMethod other) {
        if (!other.getName().equals(getName()) || other.getParameterTypes().length != getParameterTypes().length) {
            return false;
        }
        for (int i = 0; i < other.getParameterTypes().length; i++) {
            if (!other.getParameterTypes()[i].equals(getParameterTypes()[i])) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object obj) {
        if (FrameworkMethod.class.isInstance(obj)) {
            return ((FrameworkMethod) obj).method.equals(this.method);
        }
        return false;
    }

    public int hashCode() {
        return this.method.hashCode();
    }

    @Deprecated
    public boolean producesType(Type type) {
        if (getParameterTypes().length == 0 && (type instanceof Class)) {
            return ((Class) type).isAssignableFrom(this.method.getReturnType());
        }
        return false;
    }

    private Class<?>[] getParameterTypes() {
        return this.method.getParameterTypes();
    }

    public Annotation[] getAnnotations() {
        return this.method.getAnnotations();
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return this.method.getAnnotation(annotationType);
    }

    public String toString() {
        return this.method.toString();
    }
}
