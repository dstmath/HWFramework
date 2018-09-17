package sun.invoke.util;

import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class BytecodeDescriptor {
    private BytecodeDescriptor() {
    }

    public static List<Class<?>> parseMethod(String bytecodeSignature, ClassLoader loader) {
        return parseMethod(bytecodeSignature, 0, bytecodeSignature.length(), loader);
    }

    static List<Class<?>> parseMethod(String bytecodeSignature, int start, int end, ClassLoader loader) {
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        String str = bytecodeSignature;
        int[] i = new int[]{start};
        ArrayList<Class<?>> ptypes = new ArrayList();
        if (i[0] >= end || bytecodeSignature.charAt(i[0]) != '(') {
            parseError(bytecodeSignature, "not a method type");
        } else {
            i[0] = i[0] + 1;
            while (i[0] < end && bytecodeSignature.charAt(i[0]) != ')') {
                Class<?> pt = parseSig(bytecodeSignature, i, end, loader);
                if (pt == null || pt == Void.TYPE) {
                    parseError(bytecodeSignature, "bad argument type");
                }
                ptypes.add(pt);
            }
            i[0] = i[0] + 1;
        }
        Class<?> rtype = parseSig(bytecodeSignature, i, end, loader);
        if (rtype == null || i[0] != end) {
            parseError(bytecodeSignature, "bad return type");
        }
        ptypes.add(rtype);
        return ptypes;
    }

    private static void parseError(String str, String msg) {
        throw new IllegalArgumentException("bad signature: " + str + ": " + msg);
    }

    private static Class<?> parseSig(String str, int[] i, int end, ClassLoader loader) {
        if (i[0] == end) {
            return null;
        }
        int i2 = i[0];
        i[0] = i2 + 1;
        char c = str.charAt(i2);
        if (c == 'L') {
            int begc = i[0];
            int endc = str.indexOf(59, begc);
            if (endc < 0) {
                return null;
            }
            i[0] = endc + 1;
            String name = str.substring(begc, endc).replace('/', '.');
            try {
                return loader.loadClass(name);
            } catch (ClassNotFoundException ex) {
                throw new TypeNotPresentException(name, ex);
            }
        } else if (c != '[') {
            return Wrapper.forBasicType(c).primitiveType();
        } else {
            Class<?> t = parseSig(str, i, end, loader);
            if (t != null) {
                t = Array.newInstance((Class) t, 0).getClass();
            }
            return t;
        }
    }

    public static String unparse(Class<?> type) {
        StringBuilder sb = new StringBuilder();
        unparseSig(type, sb);
        return sb.-java_util_stream_Collectors-mthref-7();
    }

    public static String unparse(MethodType type) {
        return unparseMethod(type.returnType(), type.parameterList());
    }

    public static String unparse(Object type) {
        if (type instanceof Class) {
            return unparse((Class) type);
        }
        if (type instanceof MethodType) {
            return unparse((MethodType) type);
        }
        return (String) type;
    }

    public static String unparseMethod(Class<?> rtype, List<Class<?>> ptypes) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (Class<?> pt : ptypes) {
            unparseSig(pt, sb);
        }
        sb.append(')');
        unparseSig(rtype, sb);
        return sb.-java_util_stream_Collectors-mthref-7();
    }

    private static void unparseSig(Class<?> t, StringBuilder sb) {
        char c = Wrapper.forBasicType((Class) t).basicTypeChar();
        if (c != 'L') {
            sb.append(c);
            return;
        }
        boolean lsemi = t.isArray() ^ 1;
        if (lsemi) {
            sb.append('L');
        }
        sb.append(t.getName().replace('.', '/'));
        if (lsemi) {
            sb.append(';');
        }
    }
}
