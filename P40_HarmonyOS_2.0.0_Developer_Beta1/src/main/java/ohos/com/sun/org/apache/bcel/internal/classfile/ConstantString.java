package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class ConstantString extends Constant implements ConstantObject {
    private int string_index;

    public ConstantString(ConstantString constantString) {
        this(constantString.getStringIndex());
    }

    ConstantString(DataInputStream dataInputStream) throws IOException {
        this(dataInputStream.readUnsignedShort());
    }

    public ConstantString(int i) {
        super((byte) 8);
        this.string_index = i;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitConstantString(this);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant
    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.tag);
        dataOutputStream.writeShort(this.string_index);
    }

    public final int getStringIndex() {
        return this.string_index;
    }

    public final void setStringIndex(int i) {
        this.string_index = i;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant, java.lang.Object
    public final String toString() {
        return super.toString() + "(string_index = " + this.string_index + ")";
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.ConstantObject
    public Object getConstantValue(ConstantPool constantPool) {
        return ((ConstantUtf8) constantPool.getConstant(this.string_index, (byte) 1)).getBytes();
    }

    public String getBytes(ConstantPool constantPool) {
        return (String) getConstantValue(constantPool);
    }
}
