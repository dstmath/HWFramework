package java.lang.reflect;

import com.android.dex.Dex;
import java.lang.annotation.Annotation;
import libcore.reflect.GenericSignatureParser;

public final class Field extends AccessibleObject implements Member {
    private int accessFlags;
    private Class<?> declaringClass;
    private int dexFieldIndex;
    private int offset;
    private Class<?> type;

    private native <A extends Annotation> A getAnnotationNative(Class<A> cls);

    private native String[] getSignatureAnnotation();

    private native boolean isAnnotationPresentNative(Class<? extends Annotation> cls);

    public native Object get(Object obj) throws IllegalAccessException, IllegalArgumentException;

    public native boolean getBoolean(Object obj) throws IllegalAccessException, IllegalArgumentException;

    public native byte getByte(Object obj) throws IllegalAccessException, IllegalArgumentException;

    public native char getChar(Object obj) throws IllegalAccessException, IllegalArgumentException;

    public native Annotation[] getDeclaredAnnotations();

    public native double getDouble(Object obj) throws IllegalAccessException, IllegalArgumentException;

    public native float getFloat(Object obj) throws IllegalAccessException, IllegalArgumentException;

    public native int getInt(Object obj) throws IllegalAccessException, IllegalArgumentException;

    public native long getLong(Object obj) throws IllegalAccessException, IllegalArgumentException;

    public native short getShort(Object obj) throws IllegalAccessException, IllegalArgumentException;

    public native void set(Object obj, Object obj2) throws IllegalAccessException, IllegalArgumentException;

    public native void setBoolean(Object obj, boolean z) throws IllegalAccessException, IllegalArgumentException;

    public native void setByte(Object obj, byte b) throws IllegalAccessException, IllegalArgumentException;

    public native void setChar(Object obj, char c) throws IllegalAccessException, IllegalArgumentException;

    public native void setDouble(Object obj, double d) throws IllegalAccessException, IllegalArgumentException;

    public native void setFloat(Object obj, float f) throws IllegalAccessException, IllegalArgumentException;

    public native void setInt(Object obj, int i) throws IllegalAccessException, IllegalArgumentException;

    public native void setLong(Object obj, long j) throws IllegalAccessException, IllegalArgumentException;

    public native void setShort(Object obj, short s) throws IllegalAccessException, IllegalArgumentException;

    private Field() {
    }

    public Class<?> getDeclaringClass() {
        return this.declaringClass;
    }

    public String getName() {
        if (this.dexFieldIndex != -1) {
            Dex dex = this.declaringClass.getDex();
            return this.declaringClass.getDexCacheString(dex, dex.nameIndexFromFieldIndex(this.dexFieldIndex));
        } else if (this.declaringClass.isProxy()) {
            return "throws";
        } else {
            throw new AssertionError();
        }
    }

    public int getModifiers() {
        return this.accessFlags & 65535;
    }

    public boolean isEnumConstant() {
        return (getModifiers() & Record.maxDataSize) != 0;
    }

    public boolean isSynthetic() {
        return Modifier.isSynthetic(getModifiers());
    }

    public Class<?> getType() {
        return this.type;
    }

    public Type getGenericType() {
        String signatureAttribute = getSignatureAttribute();
        GenericSignatureParser parser = new GenericSignatureParser(this.declaringClass.getClassLoader());
        parser.parseForField(this.declaringClass, signatureAttribute);
        Type genericType = parser.fieldType;
        if (genericType == null) {
            return getType();
        }
        return genericType;
    }

    private String getSignatureAttribute() {
        String[] annotation = getSignatureAnnotation();
        if (annotation == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (String s : annotation) {
            result.append(s);
        }
        return result.toString();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null || !(obj instanceof Field)) {
            return false;
        }
        Field other = (Field) obj;
        if (getDeclaringClass() == other.getDeclaringClass() && getName() == other.getName() && getType() == other.getType()) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
    }

    public String toString() {
        int mod = getModifiers();
        return (mod == 0 ? "" : Modifier.toString(mod) + " ") + getTypeName(getType()) + " " + getTypeName(getDeclaringClass()) + "." + getName();
    }

    public String toGenericString() {
        int mod = getModifiers();
        Type fieldType = getGenericType();
        return (mod == 0 ? "" : Modifier.toString(mod) + " ") + (fieldType instanceof Class ? getTypeName((Class) fieldType) : fieldType.toString()) + " " + getTypeName(getDeclaringClass()) + "." + getName();
    }

    static String getTypeName(Class<?> type) {
        if (type.isArray()) {
            Class<?> cl = type;
            int dimensions = 0;
            while (cl.isArray()) {
                try {
                    dimensions++;
                    cl = cl.getComponentType();
                } catch (Throwable th) {
                }
            }
            StringBuffer sb = new StringBuffer();
            sb.append(cl.getName());
            for (int i = 0; i < dimensions; i++) {
                sb.append("[]");
            }
            return sb.toString();
        }
        return type.getName();
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        if (annotationType != null) {
            return getAnnotationNative(annotationType);
        }
        throw new NullPointerException("annotationType == null");
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        if (annotationType != null) {
            return isAnnotationPresentNative(annotationType);
        }
        throw new NullPointerException("annotationType == null");
    }

    public int getDexFieldIndex() {
        return this.dexFieldIndex;
    }

    public int getOffset() {
        return this.offset;
    }
}
