package ohos.com.sun.org.apache.bcel.internal.generic;

public final class ArrayType extends ReferenceType {
    private Type basic_type;
    private int dimensions;

    public ArrayType(byte b, int i) {
        this(BasicType.getType(b), i);
    }

    public ArrayType(String str, int i) {
        this(new ObjectType(str), i);
    }

    public ArrayType(Type type, int i) {
        super((byte) 13, "<dummy>");
        if (i < 1 || i > 255) {
            throw new ClassGenException("Invalid number of dimensions: " + i);
        }
        byte type2 = type.getType();
        if (type2 != 12) {
            if (type2 != 13) {
                this.dimensions = i;
                this.basic_type = type;
            } else {
                ArrayType arrayType = (ArrayType) type;
                this.dimensions = i + arrayType.dimensions;
                this.basic_type = arrayType.basic_type;
            }
            StringBuffer stringBuffer = new StringBuffer();
            for (int i2 = 0; i2 < this.dimensions; i2++) {
                stringBuffer.append('[');
            }
            stringBuffer.append(this.basic_type.getSignature());
            this.signature = stringBuffer.toString();
            return;
        }
        throw new ClassGenException("Invalid type: void[]");
    }

    public Type getBasicType() {
        return this.basic_type;
    }

    public Type getElementType() {
        int i = this.dimensions;
        if (i == 1) {
            return this.basic_type;
        }
        return new ArrayType(this.basic_type, i - 1);
    }

    public int getDimensions() {
        return this.dimensions;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return this.dimensions ^ this.basic_type.hashCode();
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (!(obj instanceof ArrayType)) {
            return false;
        }
        ArrayType arrayType = (ArrayType) obj;
        if (arrayType.dimensions != this.dimensions || !arrayType.basic_type.equals(this.basic_type)) {
            return false;
        }
        return true;
    }
}
