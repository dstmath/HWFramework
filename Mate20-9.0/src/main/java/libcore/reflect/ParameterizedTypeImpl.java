package libcore.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

public final class ParameterizedTypeImpl implements ParameterizedType {
    private final ListOfTypes args;
    private final ClassLoader loader;
    private final ParameterizedTypeImpl ownerType0;
    private Type ownerTypeRes;
    private Class rawType;
    private final String rawTypeName;

    public ParameterizedTypeImpl(ParameterizedTypeImpl ownerType, String rawTypeName2, ListOfTypes args2, ClassLoader loader2) {
        if (args2 != null) {
            this.ownerType0 = ownerType;
            this.rawTypeName = rawTypeName2;
            this.args = args2;
            this.loader = loader2;
            return;
        }
        throw new NullPointerException();
    }

    public Type[] getActualTypeArguments() {
        return (Type[]) this.args.getResolvedTypes().clone();
    }

    public Type getOwnerType() {
        if (this.ownerTypeRes == null) {
            if (this.ownerType0 != null) {
                this.ownerTypeRes = this.ownerType0.getResolvedType();
            } else {
                this.ownerTypeRes = getRawType().getDeclaringClass();
            }
        }
        return this.ownerTypeRes;
    }

    public Class getRawType() {
        if (this.rawType == null) {
            try {
                this.rawType = Class.forName(this.rawTypeName, false, this.loader);
            } catch (ClassNotFoundException e) {
                throw new TypeNotPresentException(this.rawTypeName, e);
            }
        }
        return this.rawType;
    }

    /* access modifiers changed from: package-private */
    public Type getResolvedType() {
        if (this.args.getResolvedTypes().length == 0) {
            return getRawType();
        }
        return this;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof ParameterizedType)) {
            return false;
        }
        ParameterizedType that = (ParameterizedType) o;
        if (Objects.equals(getRawType(), that.getRawType()) && Objects.equals(getOwnerType(), that.getOwnerType()) && Arrays.equals(this.args.getResolvedTypes(), that.getActualTypeArguments())) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return (31 * ((Objects.hashCode(getRawType()) * 31) + Objects.hashCode(getOwnerType()))) + Arrays.hashCode(this.args.getResolvedTypes());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.rawTypeName);
        if (this.args.length() > 0) {
            sb.append("<");
            sb.append(this.args);
            sb.append(">");
        }
        return sb.toString();
    }
}
