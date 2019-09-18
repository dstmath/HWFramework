package java.lang.reflect;

import java.lang.annotation.Annotation;
import java.util.Objects;
import libcore.reflect.AnnotatedElements;
import libcore.reflect.GenericSignatureParser;
import sun.reflect.CallerSensitive;

public final class Field extends AccessibleObject implements Member {
    private int accessFlags;
    private Class<?> declaringClass;
    private int dexFieldIndex;
    private int offset;
    private Class<?> type;

    private native <A extends Annotation> A getAnnotationNative(Class<A> cls);

    private native String getNameInternal();

    private native String[] getSignatureAnnotation();

    private native boolean isAnnotationPresentNative(Class<? extends Annotation> cls);

    @CallerSensitive
    public native Object get(Object obj) throws IllegalArgumentException, IllegalAccessException;

    public native long getArtField();

    @CallerSensitive
    public native boolean getBoolean(Object obj) throws IllegalArgumentException, IllegalAccessException;

    @CallerSensitive
    public native byte getByte(Object obj) throws IllegalArgumentException, IllegalAccessException;

    @CallerSensitive
    public native char getChar(Object obj) throws IllegalArgumentException, IllegalAccessException;

    public native Annotation[] getDeclaredAnnotations();

    @CallerSensitive
    public native double getDouble(Object obj) throws IllegalArgumentException, IllegalAccessException;

    @CallerSensitive
    public native float getFloat(Object obj) throws IllegalArgumentException, IllegalAccessException;

    @CallerSensitive
    public native int getInt(Object obj) throws IllegalArgumentException, IllegalAccessException;

    @CallerSensitive
    public native long getLong(Object obj) throws IllegalArgumentException, IllegalAccessException;

    @CallerSensitive
    public native short getShort(Object obj) throws IllegalArgumentException, IllegalAccessException;

    @CallerSensitive
    public native void set(Object obj, Object obj2) throws IllegalArgumentException, IllegalAccessException;

    @CallerSensitive
    public native void setBoolean(Object obj, boolean z) throws IllegalArgumentException, IllegalAccessException;

    @CallerSensitive
    public native void setByte(Object obj, byte b) throws IllegalArgumentException, IllegalAccessException;

    @CallerSensitive
    public native void setChar(Object obj, char c) throws IllegalArgumentException, IllegalAccessException;

    @CallerSensitive
    public native void setDouble(Object obj, double d) throws IllegalArgumentException, IllegalAccessException;

    @CallerSensitive
    public native void setFloat(Object obj, float f) throws IllegalArgumentException, IllegalAccessException;

    @CallerSensitive
    public native void setInt(Object obj, int i) throws IllegalArgumentException, IllegalAccessException;

    @CallerSensitive
    public native void setLong(Object obj, long j) throws IllegalArgumentException, IllegalAccessException;

    @CallerSensitive
    public native void setShort(Object obj, short s) throws IllegalArgumentException, IllegalAccessException;

    private Field() {
    }

    public Class<?> getDeclaringClass() {
        return this.declaringClass;
    }

    public String getName() {
        if (this.dexFieldIndex != -1) {
            return getNameInternal();
        }
        if (this.declaringClass.isProxy()) {
            return "throws";
        }
        throw new AssertionError();
    }

    public int getModifiers() {
        return this.accessFlags & 65535;
    }

    public boolean isEnumConstant() {
        return (getModifiers() & 16384) != 0;
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
        String str;
        int mod = getModifiers();
        StringBuilder sb = new StringBuilder();
        if (mod == 0) {
            str = "";
        } else {
            str = Modifier.toString(mod) + " ";
        }
        sb.append(str);
        sb.append(getType().getTypeName());
        sb.append(" ");
        sb.append(getDeclaringClass().getTypeName());
        sb.append(".");
        sb.append(getName());
        return sb.toString();
    }

    public String toGenericString() {
        String str;
        int mod = getModifiers();
        Type fieldType = getGenericType();
        StringBuilder sb = new StringBuilder();
        if (mod == 0) {
            str = "";
        } else {
            str = Modifier.toString(mod) + " ";
        }
        sb.append(str);
        sb.append(fieldType.getTypeName());
        sb.append(" ");
        sb.append(getDeclaringClass().getTypeName());
        sb.append(".");
        sb.append(getName());
        return sb.toString();
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        Objects.requireNonNull(annotationClass);
        return getAnnotationNative(annotationClass);
    }

    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return AnnotatedElements.getDirectOrIndirectAnnotationsByType(this, annotationClass);
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
