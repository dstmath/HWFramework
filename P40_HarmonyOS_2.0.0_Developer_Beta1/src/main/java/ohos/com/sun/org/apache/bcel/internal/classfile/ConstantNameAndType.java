package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class ConstantNameAndType extends Constant {
    private int name_index;
    private int signature_index;

    public ConstantNameAndType(ConstantNameAndType constantNameAndType) {
        this(constantNameAndType.getNameIndex(), constantNameAndType.getSignatureIndex());
    }

    ConstantNameAndType(DataInputStream dataInputStream) throws IOException {
        this(dataInputStream.readUnsignedShort(), dataInputStream.readUnsignedShort());
    }

    public ConstantNameAndType(int i, int i2) {
        super((byte) 12);
        this.name_index = i;
        this.signature_index = i2;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitConstantNameAndType(this);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant
    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.tag);
        dataOutputStream.writeShort(this.name_index);
        dataOutputStream.writeShort(this.signature_index);
    }

    public final int getNameIndex() {
        return this.name_index;
    }

    public final String getName(ConstantPool constantPool) {
        return constantPool.constantToString(getNameIndex(), (byte) 1);
    }

    public final int getSignatureIndex() {
        return this.signature_index;
    }

    public final String getSignature(ConstantPool constantPool) {
        return constantPool.constantToString(getSignatureIndex(), (byte) 1);
    }

    public final void setNameIndex(int i) {
        this.name_index = i;
    }

    public final void setSignatureIndex(int i) {
        this.signature_index = i;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant, java.lang.Object
    public final String toString() {
        return super.toString() + "(name_index = " + this.name_index + ", signature_index = " + this.signature_index + ")";
    }
}
