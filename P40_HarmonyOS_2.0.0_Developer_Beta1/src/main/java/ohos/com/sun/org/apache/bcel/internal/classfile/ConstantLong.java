package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class ConstantLong extends Constant implements ConstantObject {
    private long bytes;

    public ConstantLong(long j) {
        super((byte) 5);
        this.bytes = j;
    }

    public ConstantLong(ConstantLong constantLong) {
        this(constantLong.getBytes());
    }

    ConstantLong(DataInputStream dataInputStream) throws IOException {
        this(dataInputStream.readLong());
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitConstantLong(this);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant
    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.tag);
        dataOutputStream.writeLong(this.bytes);
    }

    public final long getBytes() {
        return this.bytes;
    }

    public final void setBytes(long j) {
        this.bytes = j;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant, java.lang.Object
    public final String toString() {
        return super.toString() + "(bytes = " + this.bytes + ")";
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.ConstantObject
    public Object getConstantValue(ConstantPool constantPool) {
        return new Long(this.bytes);
    }
}
