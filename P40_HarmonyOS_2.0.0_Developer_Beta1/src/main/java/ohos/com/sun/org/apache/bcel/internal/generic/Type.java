package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import ohos.com.sun.org.apache.bcel.internal.classfile.ClassFormatException;
import ohos.com.sun.org.apache.bcel.internal.classfile.Utility;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;

public abstract class Type implements Serializable {
    public static final BasicType BOOLEAN = new BasicType((byte) 4);
    public static final BasicType BYTE = new BasicType((byte) 8);
    public static final BasicType CHAR = new BasicType((byte) 5);
    public static final BasicType DOUBLE = new BasicType((byte) 7);
    public static final BasicType FLOAT = new BasicType((byte) 6);
    public static final BasicType INT = new BasicType((byte) 10);
    public static final BasicType LONG = new BasicType((byte) 11);
    public static final Type[] NO_ARGS = new Type[0];
    public static final ReferenceType NULL = new ReferenceType() {
        /* class ohos.com.sun.org.apache.bcel.internal.generic.Type.AnonymousClass1 */
    };
    public static final ObjectType OBJECT = new ObjectType(Constants.OBJECT_CLASS);
    public static final BasicType SHORT = new BasicType((byte) 9);
    public static final ObjectType STRING = new ObjectType("java.lang.String");
    public static final ObjectType STRINGBUFFER = new ObjectType(Constants.STRING_BUFFER_CLASS);
    public static final ObjectType THROWABLE = new ObjectType("java.lang.Throwable");
    public static final Type UNKNOWN = new Type((byte) 15, "<unknown object>") {
        /* class ohos.com.sun.org.apache.bcel.internal.generic.Type.AnonymousClass2 */
    };
    public static final BasicType VOID = new BasicType((byte) 12);
    private static int consumed_chars = 0;
    protected String signature;
    protected byte type;

    protected Type(byte b, String str) {
        this.type = b;
        this.signature = str;
    }

    public String getSignature() {
        return this.signature;
    }

    public byte getType() {
        return this.type;
    }

    public int getSize() {
        byte b = this.type;
        if (b == 7 || b == 11) {
            return 2;
        }
        return b != 12 ? 1 : 0;
    }

    @Override // java.lang.Object
    public String toString() {
        if (equals(NULL) || this.type >= 15) {
            return this.signature;
        }
        return Utility.signatureToString(this.signature, false);
    }

    public static String getMethodSignature(Type type2, Type[] typeArr) {
        StringBuffer stringBuffer = new StringBuffer("(");
        int length = typeArr == null ? 0 : typeArr.length;
        for (int i = 0; i < length; i++) {
            stringBuffer.append(typeArr[i].getSignature());
        }
        stringBuffer.append(')');
        stringBuffer.append(type2.getSignature());
        return stringBuffer.toString();
    }

    public static final Type getType(String str) throws StringIndexOutOfBoundsException {
        byte typeOfSignature = Utility.typeOfSignature(str);
        if (typeOfSignature <= 12) {
            consumed_chars = 1;
            return BasicType.getType(typeOfSignature);
        } else if (typeOfSignature == 13) {
            int i = 0;
            do {
                i++;
            } while (str.charAt(i) == '[');
            Type type2 = getType(str.substring(i));
            consumed_chars += i;
            return new ArrayType(type2, i);
        } else {
            int indexOf = str.indexOf(59);
            if (indexOf >= 0) {
                consumed_chars = indexOf + 1;
                return new ObjectType(str.substring(1, indexOf).replace('/', '.'));
            }
            throw new ClassFormatException("Invalid signature: " + str);
        }
    }

    public static Type getReturnType(String str) {
        try {
            return getType(str.substring(str.lastIndexOf(41) + 1));
        } catch (StringIndexOutOfBoundsException unused) {
            throw new ClassFormatException("Invalid method signature: " + str);
        }
    }

    public static Type[] getArgumentTypes(String str) {
        ArrayList arrayList = new ArrayList();
        try {
            if (str.charAt(0) == '(') {
                for (int i = 1; str.charAt(i) != ')'; i += consumed_chars) {
                    arrayList.add(getType(str.substring(i)));
                }
                Type[] typeArr = new Type[arrayList.size()];
                arrayList.toArray(typeArr);
                return typeArr;
            }
            throw new ClassFormatException("Invalid method signature: " + str);
        } catch (StringIndexOutOfBoundsException unused) {
            throw new ClassFormatException("Invalid method signature: " + str);
        }
    }

    public static Type getType(Class cls) {
        if (cls == null) {
            throw new IllegalArgumentException("Class must not be null");
        } else if (cls.isArray()) {
            return getType(cls.getName());
        } else {
            if (!cls.isPrimitive()) {
                return new ObjectType(cls.getName());
            }
            if (cls == Integer.TYPE) {
                return INT;
            }
            if (cls == Void.TYPE) {
                return VOID;
            }
            if (cls == Double.TYPE) {
                return DOUBLE;
            }
            if (cls == Float.TYPE) {
                return FLOAT;
            }
            if (cls == Boolean.TYPE) {
                return BOOLEAN;
            }
            if (cls == Byte.TYPE) {
                return BYTE;
            }
            if (cls == Short.TYPE) {
                return SHORT;
            }
            if (cls == Byte.TYPE) {
                return BYTE;
            }
            if (cls == Long.TYPE) {
                return LONG;
            }
            if (cls == Character.TYPE) {
                return CHAR;
            }
            throw new IllegalStateException("Ooops, what primitive type is " + cls);
        }
    }

    public static String getSignature(Method method) {
        Class<?>[] parameterTypes;
        StringBuffer stringBuffer = new StringBuffer("(");
        for (Class<?> cls : method.getParameterTypes()) {
            stringBuffer.append(getType(cls).getSignature());
        }
        stringBuffer.append(")");
        stringBuffer.append(getType(method.getReturnType()).getSignature());
        return stringBuffer.toString();
    }
}
