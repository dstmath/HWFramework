package org.junit.runners.model;

import java.lang.reflect.Modifier;
import java.util.List;

public abstract class FrameworkMember<T extends FrameworkMember<T>> implements Annotatable {
    public abstract Class<?> getDeclaringClass();

    protected abstract int getModifiers();

    public abstract String getName();

    public abstract Class<?> getType();

    abstract boolean isShadowedBy(T t);

    boolean isShadowedBy(List<T> members) {
        for (T each : members) {
            if (isShadowedBy((FrameworkMember) each)) {
                return true;
            }
        }
        return false;
    }

    public boolean isStatic() {
        return Modifier.isStatic(getModifiers());
    }

    public boolean isPublic() {
        return Modifier.isPublic(getModifiers());
    }
}
