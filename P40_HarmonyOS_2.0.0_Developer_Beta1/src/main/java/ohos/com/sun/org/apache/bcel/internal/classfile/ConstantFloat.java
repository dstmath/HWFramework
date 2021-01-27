package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class ConstantFloat extends Constant implements ConstantObject {
    private float bytes;

    public ConstantFloat(float f) {
        super((byte) 4);
        this.bytes = f;
    }

    public ConstantFloat(ConstantFloat constantFloat) {
        this(constantFloat.getBytes());
    }

    ConstantFloat(DataInputStream dataInputStream) throws IOException {
        this(dataInputStream.readFloat());
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitConstantFloat(this);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant
    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.tag);
        dataOutputStream.writeFloat(this.bytes);
    }

    public final float getBytes() {
        return this.bytes;
    }

    public final void setBytes(float f) {
        this.bytes = f;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant, java.lang.Object
    public final String toString() {
        return super.toString() + "(bytes = " + this.bytes + ")";
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.ConstantObject
    public Object getConstantValue(ConstantPool constantPool) {
        return new Float(this.bytes);
    }
}
