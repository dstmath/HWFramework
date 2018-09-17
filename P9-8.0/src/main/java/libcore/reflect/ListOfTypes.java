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
        this.types = new ArrayList(capacity);
    }

    ListOfTypes(Type[] types) {
        this.types = new ArrayList(types.length);
        for (Type type : types) {
            this.types.add(type);
        }
    }

    void add(Type type) {
        if (type == null) {
            throw new NullPointerException("type == null");
        }
        this.types.add(type);
    }

    int length() {
        return this.types.size();
    }

    public Type[] getResolvedTypes() {
        Type[] result = this.resolvedTypes;
        if (result != null) {
            return result;
        }
        result = resolveTypes(this.types);
        this.resolvedTypes = result;
        return result;
    }

    private Type[] resolveTypes(List<Type> unresolved) {
        int size = unresolved.size();
        if (size == 0) {
            return EmptyArray.TYPE;
        }
        Type[] result = new Type[size];
        for (int i = 0; i < size; i++) {
            Type type = (Type) unresolved.get(i);
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
