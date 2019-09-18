package libcore.reflect;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import libcore.util.EmptyArray;

public final class ListOfTypes {
    public static final ListOfTypes EMPTY = new ListOfTypes(0);
    private Type[] resolvedTypes;
    private final ArrayList<Type> types;

    ListOfTypes(int capacity) {
        this.types = new ArrayList<>(capacity);
    }

    ListOfTypes(Type[] types2) {
        this.types = new ArrayList<>(types2.length);
        for (Type type : types2) {
            this.types.add(type);
        }
    }

    /* access modifiers changed from: package-private */
    public void add(Type type) {
        if (type != null) {
            this.types.add(type);
            return;
        }
        throw new NullPointerException("type == null");
    }

    /* access modifiers changed from: package-private */
    public int length() {
        return this.types.size();
    }

    public Type[] getResolvedTypes() {
        Type[] result = this.resolvedTypes;
        if (result != null) {
            return result;
        }
        Type[] result2 = resolveTypes(this.types);
        this.resolvedTypes = result2;
        return result2;
    }

    private Type[] resolveTypes(List<Type> unresolved) {
        int size = unresolved.size();
        if (size == 0) {
            return EmptyArray.TYPE;
        }
        Type[] result = new Type[size];
        for (int i = 0; i < size; i++) {
            Type type = unresolved.get(i);
            if (type instanceof ParameterizedTypeImpl) {
                result[i] = ((ParameterizedTypeImpl) type).getResolvedType();
            } else {
                result[i] = type;
            }
        }
        return result;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < this.types.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(this.types.get(i));
        }
        return result.toString();
    }
}
