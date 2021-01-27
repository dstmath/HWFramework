package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class SourceFile extends Attribute {
    private int sourcefile_index;

    public SourceFile(SourceFile sourceFile) {
        this(sourceFile.getNameIndex(), sourceFile.getLength(), sourceFile.getSourceFileIndex(), sourceFile.getConstantPool());
    }

    SourceFile(int i, int i2, DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        this(i, i2, dataInputStream.readUnsignedShort(), constantPool);
    }

    public SourceFile(int i, int i2, int i3, ConstantPool constantPool) {
        super((byte) 0, i, i2, constantPool);
        this.sourcefile_index = i3;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitSourceFile(this);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        super.dump(dataOutputStream);
        dataOutputStream.writeShort(this.sourcefile_index);
    }

    public final int getSourceFileIndex() {
        return this.sourcefile_index;
    }

    public final void setSourceFileIndex(int i) {
        this.sourcefile_index = i;
    }

    public final String getSourceFileName() {
        return ((ConstantUtf8) this.constant_pool.getConstant(this.sourcefile_index, (byte) 1)).getBytes();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, java.lang.Object
    public final String toString() {
        return "SourceFile(" + getSourceFileName() + ")";
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public Attribute copy(ConstantPool constantPool) {
        return (SourceFile) clone();
    }
}
