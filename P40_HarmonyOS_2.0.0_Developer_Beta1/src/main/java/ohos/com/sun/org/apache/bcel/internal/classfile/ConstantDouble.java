package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class ConstantDouble extends Constant implements ConstantObject {
    private double bytes;

    public ConstantDouble(double d) {
        super((byte) 6);
        this.bytes = d;
    }

    public ConstantDouble(ConstantDouble constantDouble) {
        this(constantDouble.getBytes());
    }

    ConstantDouble(DataInputStream dataInputStream) throws IOException {
        this(dataInputStream.readDouble());
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitConstantDouble(this);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant
    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.tag);
        dataOutputStream.writeDouble(this.bytes);
    }

    public final double getBytes() {
        return this.bytes;
    }

    public final void setBytes(double d) {
        this.bytes = d;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant, java.lang.Object
    public final String toString() {
        return super.toString() + "(bytes = " + this.bytes + ")";
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.ConstantObject
    public Object getConstantValue(ConstantPool constantPool) {
        return new Double(this.bytes);
    }
}
