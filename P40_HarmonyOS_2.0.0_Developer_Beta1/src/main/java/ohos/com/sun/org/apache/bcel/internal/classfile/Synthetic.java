package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class Synthetic extends Attribute {
    private byte[] bytes;

    public Synthetic(Synthetic synthetic) {
        this(synthetic.getNameIndex(), synthetic.getLength(), synthetic.getBytes(), synthetic.getConstantPool());
    }

    public Synthetic(int i, int i2, byte[] bArr, ConstantPool constantPool) {
        super((byte) 7, i, i2, constantPool);
        this.bytes = bArr;
    }

    Synthetic(int i, int i2, DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        this(i, i2, (byte[]) null, constantPool);
        if (i2 > 0) {
            this.bytes = new byte[i2];
            dataInputStream.readFully(this.bytes);
            System.err.println("Synthetic attribute with length > 0");
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitSynthetic(this);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        super.dump(dataOutputStream);
        if (this.length > 0) {
            dataOutputStream.write(this.bytes, 0, this.length);
        }
    }

    public final byte[] getBytes() {
        return this.bytes;
    }

    public final void setBytes(byte[] bArr) {
        this.bytes = bArr;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, java.lang.Object
    public final String toString() {
        StringBuffer stringBuffer = new StringBuffer("Synthetic");
        if (this.length > 0) {
            stringBuffer.append(" " + Utility.toHexString(this.bytes));
        }
        return stringBuffer.toString();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public Attribute copy(ConstantPool constantPool) {
        Synthetic synthetic = (Synthetic) clone();
        byte[] bArr = this.bytes;
        if (bArr != null) {
            synthetic.bytes = (byte[]) bArr.clone();
        }
        synthetic.constant_pool = constantPool;
        return synthetic;
    }
}
