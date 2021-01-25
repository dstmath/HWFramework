package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class ConstantUtf8 extends Constant {
    private String bytes;

    public ConstantUtf8(ConstantUtf8 constantUtf8) {
        this(constantUtf8.getBytes());
    }

    ConstantUtf8(DataInputStream dataInputStream) throws IOException {
        super((byte) 1);
        this.bytes = dataInputStream.readUTF();
    }

    public ConstantUtf8(String str) {
        super((byte) 1);
        if (str != null) {
            this.bytes = str;
            return;
        }
        throw new IllegalArgumentException("bytes must not be null!");
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitConstantUtf8(this);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant
    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.tag);
        dataOutputStream.writeUTF(this.bytes);
    }

    public final String getBytes() {
        return this.bytes;
    }

    public final void setBytes(String str) {
        this.bytes = str;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant, java.lang.Object
    public final String toString() {
        return super.toString() + "(\"" + Utility.replace(this.bytes, "\n", "\\n") + "\")";
    }
}
