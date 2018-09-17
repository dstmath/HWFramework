package libcore.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import libcore.util.EmptyArray;
import org.xmlpull.v1.XmlPullParser;

public final class Types {
    private static final Map<Class<?>, String> PRIMITIVE_TO_SIGNATURE = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: libcore.reflect.Types.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: libcore.reflect.Types.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: libcore.reflect.Types.<clinit>():void");
    }

    private Types() {
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
            return XmlPullParser.NO_NAMESPACE;
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
