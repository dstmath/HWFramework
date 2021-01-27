package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class ConstantInteger extends Constant implements ConstantObject {
    private int bytes;

    public ConstantInteger(int i) {
        super((byte) 3);
        this.bytes = i;
    }

    public ConstantInteger(ConstantInteger constantInteger) {
        this(constantInteger.getBytes());
    }

    ConstantInteger(DataInputStream dataInputStream) throws IOException {
        this(dataInputStream.readInt());
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitConstantInteger(this);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant
    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.tag);
        dataOutputStream.writeInt(this.bytes);
    }

    public final int getBytes() {
        return this.bytes;
    }

    public final void setBytes(int i) {
        this.bytes = i;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant, java.lang.Object
    public final String toString() {
        return super.toString() + "(bytes = " + this.bytes + ")";
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.ConstantObject
    public Object getConstantValue(ConstantPool constantPool) {
        return new Integer(this.bytes);
    }
}
