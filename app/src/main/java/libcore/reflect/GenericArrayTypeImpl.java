package libcore.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

public final class GenericArrayTypeImpl implements GenericArrayType {
    private final Type componentType;

    public GenericArrayTypeImpl(Type componentType) {
        this.componentType = componentType;
    }

    public Type getGenericComponentType() {
        try {
            return ((ParameterizedTypeImpl) this.componentType).getResolvedType();
        } catch (ClassCastException e) {
            return this.componentType;
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof GenericArrayType)) {
            return false;
        }
        return Objects.equals(getGenericComponentType(), ((GenericArrayType) o).getGenericComponentType());
    }

    public int hashCode() {
        return Objects.hashCode(getGenericComponentType());
    }

    public String toString() {
        return this.componentType.toString() + "[]";
    }
}
