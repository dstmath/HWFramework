package java.io;

import java.lang.reflect.Field;
import java.sql.Types;
import sun.reflect.CallerSensitive;

public class ObjectStreamField implements Comparable<Object> {
    private final Field field;
    private final String name;
    private int offset;
    private final String signature;
    private final Class<?> type;
    private final boolean unshared;

    public ObjectStreamField(String name, Class<?> type) {
        this(name, (Class) type, false);
    }

    public ObjectStreamField(String name, Class<?> type, boolean unshared) {
        this.offset = 0;
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.type = type;
        this.unshared = unshared;
        this.signature = getClassSignature(type).intern();
        this.field = null;
    }

    ObjectStreamField(String name, String signature, boolean unshared) {
        this.offset = 0;
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.signature = signature.intern();
        this.unshared = unshared;
        this.field = null;
        switch (signature.charAt(0)) {
            case 'B':
                this.type = Byte.TYPE;
                return;
            case 'C':
                this.type = Character.TYPE;
                return;
            case 'D':
                this.type = Double.TYPE;
                return;
            case Types.DATALINK /*70*/:
                this.type = Float.TYPE;
                return;
            case 'I':
                this.type = Integer.TYPE;
                return;
            case 'J':
                this.type = Long.TYPE;
                return;
            case 'L':
            case Types.DATE /*91*/:
                this.type = Object.class;
                return;
            case 'S':
                this.type = Short.TYPE;
                return;
            case 'Z':
                this.type = Boolean.TYPE;
                return;
            default:
                throw new IllegalArgumentException("illegal signature");
        }
    }

    ObjectStreamField(Field field, boolean unshared, boolean showType) {
        this.offset = 0;
        this.field = field;
        this.unshared = unshared;
        this.name = field.getName();
        Class<?> ftype = field.getType();
        Class cls = (showType || ftype.isPrimitive()) ? ftype : Object.class;
        this.type = cls;
        this.signature = getClassSignature(ftype).intern();
    }

    public String getName() {
        return this.name;
    }

    @CallerSensitive
    public Class<?> getType() {
        return this.type;
    }

    public char getTypeCode() {
        return this.signature.charAt(0);
    }

    public String getTypeString() {
        return isPrimitive() ? null : this.signature;
    }

    public int getOffset() {
        return this.offset;
    }

    protected void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean isPrimitive() {
        char tcode = this.signature.charAt(0);
        if (tcode == 'L' || tcode == '[') {
            return false;
        }
        return true;
    }

    public boolean isUnshared() {
        return this.unshared;
    }

    public int compareTo(Object obj) {
        ObjectStreamField other = (ObjectStreamField) obj;
        boolean isPrim = isPrimitive();
        if (isPrim == other.isPrimitive()) {
            return this.name.compareTo(other.name);
        }
        return isPrim ? -1 : 1;
    }

    public String toString() {
        return this.signature + ' ' + this.name;
    }

    Field getField() {
        return this.field;
    }

    String getSignature() {
        return this.signature;
    }

    private static String getClassSignature(Class<?> cl) {
        StringBuilder sbuf = new StringBuilder();
        while (cl.isArray()) {
            sbuf.append('[');
            cl = cl.getComponentType();
        }
        if (!cl.isPrimitive()) {
            sbuf.append('L').append(cl.getName().replace('.', '/')).append(';');
        } else if (cl == Integer.TYPE) {
            sbuf.append('I');
        } else if (cl == Byte.TYPE) {
            sbuf.append('B');
        } else if (cl == Long.TYPE) {
            sbuf.append('J');
        } else if (cl == Float.TYPE) {
            sbuf.append('F');
        } else if (cl == Double.TYPE) {
            sbuf.append('D');
        } else if (cl == Short.TYPE) {
            sbuf.append('S');
        } else if (cl == Character.TYPE) {
            sbuf.append('C');
        } else if (cl == Boolean.TYPE) {
            sbuf.append('Z');
        } else if (cl == Void.TYPE) {
            sbuf.append('V');
        } else {
            throw new InternalError();
        }
        return sbuf.toString();
    }
}
