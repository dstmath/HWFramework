package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.Constants;

public final class StackMapType implements Cloneable {
    private ConstantPool constant_pool;
    private int index;
    private byte type;

    StackMapType(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        this(dataInputStream.readByte(), -1, constantPool);
        if (hasIndex()) {
            setIndex(dataInputStream.readShort());
        }
        setConstantPool(constantPool);
    }

    public StackMapType(byte b, int i, ConstantPool constantPool) {
        this.index = -1;
        setType(b);
        setIndex(i);
        setConstantPool(constantPool);
    }

    public void setType(byte b) {
        if (b < 0 || b > 8) {
            throw new RuntimeException("Illegal type for StackMapType: " + ((int) b));
        }
        this.type = b;
    }

    public byte getType() {
        return this.type;
    }

    public void setIndex(int i) {
        this.index = i;
    }

    public int getIndex() {
        return this.index;
    }

    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.type);
        if (hasIndex()) {
            dataOutputStream.writeShort(getIndex());
        }
    }

    public final boolean hasIndex() {
        byte b = this.type;
        return b == 7 || b == 8;
    }

    private String printIndex() {
        byte b = this.type;
        if (b == 7) {
            return ", class=" + this.constant_pool.constantToString(this.index, (byte) 7);
        } else if (b != 8) {
            return "";
        } else {
            return ", offset=" + this.index;
        }
    }

    @Override // java.lang.Object
    public final String toString() {
        return "(type=" + Constants.ITEM_NAMES[this.type] + printIndex() + ")";
    }

    public StackMapType copy() {
        try {
            return (StackMapType) clone();
        } catch (CloneNotSupportedException unused) {
            return null;
        }
    }

    public final ConstantPool getConstantPool() {
        return this.constant_pool;
    }

    public final void setConstantPool(ConstantPool constantPool) {
        this.constant_pool = constantPool;
    }
}
