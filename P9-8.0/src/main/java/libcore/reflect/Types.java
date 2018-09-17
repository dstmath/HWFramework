package libcore.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import libcore.util.EmptyArray;

public final class Types {
    private static final Map<Class<?>, String> PRIMITIVE_TO_SIGNATURE = new HashMap(9);

    private Types() {
    }

    static {
        PRIMITIVE_TO_SIGNATURE.put(Byte.TYPE, "B");
        PRIMITIVE_TO_SIGNATURE.put(Character.TYPE, "C");
        PRIMITIVE_TO_SIGNATURE.put(Short.TYPE, "S");
        PRIMITIVE_TO_SIGNATURE.put(Integer.TYPE, "I");
        PRIMITIVE_TO_SIGNATURE.put(Long.TYPE, "J");
        PRIMITIVE_TO_SIGNATURE.put(Float.TYPE, "F");
        PRIMITIVE_TO_SIGNATURE.put(Double.TYPE, "D");
        PRIMITIVE_TO_SIGNATURE.put(Void.TYPE, "V");
        PRIMITIVE_TO_SIGNATURE.put(Boolean.TYPE, "Z");
    }

    public static Type[] getTypeArray(ListOfTypes types, boolean clone) {
        if (types.length() == 0) {
            return EmptyArray.TYPE;
        }
        Type[] result = types.getResolvedTypes();
        return clone ? (Type[]) result.clone() : result;
    }

    public static Type getType(Type type) {
        if (type instanceof ParameterizedTypeImpl) {
            return ((ParameterizedTypeImpl) type).getResolvedType();
        }
        return type;
    }

    public static String getSignature(Class<?> clazz) {
        String primitiveSignature = (String) PRIMITIVE_TO_SIGNATURE.get(clazz);
        if (primitiveSignature != null) {
            return primitiveSignature;
        }
        if (clazz.isArray()) {
            return "[" + getSignature(clazz.getComponentType());
        }
        return "L" + clazz.getName() + ";";
    }

    public static String toString(Class<?>[] types) {
        if (types.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        appendTypeName(result, types[0]);
        for (int i = 1; i < types.length; i++) {
            result.append(',');
            appendTypeName(result, types[i]);
        }
        return result.toString();
    }

    public static void appendTypeName(StringBuilder out, Class<?> c) {
        int dimensions = 0;
        while (c.isArray()) {
            c = c.getComponentType();
            dimensions++;
        }
        out.append(c.getName());
        for (int d = 0; d < dimensions; d++) {
            out.append("[]");
        }
    }

    public static void appendArrayGenericType(StringBuilder out, Type[] types) {
        if (types.length != 0) {
            appendGenericType(out, types[0]);
            for (int i = 1; i < types.length; i++) {
                out.append(',');
                appendGenericType(out, types[i]);
            }
        }
    }

    public static void appendGenericType(StringBuilder out, Type type) {
        if (type instanceof TypeVariable) {
            out.append(((TypeVariable) type).getName());
        } else if (type instanceof ParameterizedType) {
            out.append(type.toString());
        } else if (type instanceof GenericArrayType) {
            appendGenericType(out, ((GenericArrayType) type).getGenericComponentType());
            out.append("[]");
        } else if (type instanceof Class) {
            Class c = (Class) type;
            if (c.isArray()) {
                String[] as = c.getName().split("\\[");
                int len = as.length - 1;
                if (as[len].length() > 1) {
                    out.append(as[len].substring(1, as[len].length() - 1));
                } else {
                    char ch = as[len].charAt(0);
                    if (ch == 'I') {
                        out.append("int");
                    } else if (ch == 'B') {
                        out.append("byte");
                    } else if (ch == 'J') {
                        out.append("long");
                    } else if (ch == 'F') {
                        out.append("float");
                    } else if (ch == 'D') {
                        out.append("double");
                    } else if (ch == 'S') {
                        out.append("short");
                    } else if (ch == 'C') {
                        out.append("char");
                    } else if (ch == 'Z') {
                        out.append("boolean");
                    } else if (ch == 'V') {
                        out.append("void");
                    }
                }
                for (int i = 0; i < len; i++) {
                    out.append("[]");
                }
                return;
            }
            out.append(c.getName());
        }
    }
}
