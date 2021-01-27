package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class ConstantCP extends Constant {
    protected int class_index;
    protected int name_and_type_index;

    public ConstantCP(ConstantCP constantCP) {
        this(constantCP.getTag(), constantCP.getClassIndex(), constantCP.getNameAndTypeIndex());
    }

    ConstantCP(byte b, DataInputStream dataInputStream) throws IOException {
        this(b, dataInputStream.readUnsignedShort(), dataInputStream.readUnsignedShort());
    }

    protected ConstantCP(byte b, int i, int i2) {
        super(b);
        this.class_index = i;
        this.name_and_type_index = i2;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant
    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.tag);
        dataOutputStream.writeShort(this.class_index);
        dataOutputStream.writeShort(this.name_and_type_index);
    }

    public final int getClassIndex() {
        return this.class_index;
    }

    public final int getNameAndTypeIndex() {
        return this.name_and_type_index;
    }

    public final void setClassIndex(int i) {
        this.class_index = i;
    }

    public String getClass(ConstantPool constantPool) {
        return constantPool.constantToString(this.class_index, (byte) 7);
    }

    public final void setNameAndTypeIndex(int i) {
        this.name_and_type_index = i;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant, java.lang.Object
    public final String toString() {
        return super.toString() + "(class_index = " + this.class_index + ", name_and_type_index = " + this.name_and_type_index + ")";
    }
}
