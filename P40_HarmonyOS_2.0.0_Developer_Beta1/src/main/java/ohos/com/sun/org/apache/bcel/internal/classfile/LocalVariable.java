package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import ohos.com.sun.org.apache.bcel.internal.Constants;

public final class LocalVariable implements Constants, Cloneable, Node, Serializable {
    private ConstantPool constant_pool;
    private int index;
    private int length;
    private int name_index;
    private int signature_index;
    private int start_pc;

    public LocalVariable(LocalVariable localVariable) {
        this(localVariable.getStartPC(), localVariable.getLength(), localVariable.getNameIndex(), localVariable.getSignatureIndex(), localVariable.getIndex(), localVariable.getConstantPool());
    }

    LocalVariable(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        this(dataInputStream.readUnsignedShort(), dataInputStream.readUnsignedShort(), dataInputStream.readUnsignedShort(), dataInputStream.readUnsignedShort(), dataInputStream.readUnsignedShort(), constantPool);
    }

    public LocalVariable(int i, int i2, int i3, int i4, int i5, ConstantPool constantPool) {
        this.start_pc = i;
        this.length = i2;
        this.name_index = i3;
        this.signature_index = i4;
        this.index = i5;
        this.constant_pool = constantPool;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitLocalVariable(this);
    }

    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeShort(this.start_pc);
        dataOutputStream.writeShort(this.length);
        dataOutputStream.writeShort(this.name_index);
        dataOutputStream.writeShort(this.signature_index);
        dataOutputStream.writeShort(this.index);
    }

    public final ConstantPool getConstantPool() {
        return this.constant_pool;
    }

    public final int getLength() {
        return this.length;
    }

    public final String getName() {
        return ((ConstantUtf8) this.constant_pool.getConstant(this.name_index, (byte) 1)).getBytes();
    }

    public final int getNameIndex() {
        return this.name_index;
    }

    public final String getSignature() {
        return ((ConstantUtf8) this.constant_pool.getConstant(this.signature_index, (byte) 1)).getBytes();
    }

    public final int getSignatureIndex() {
        return this.signature_index;
    }

    public final int getIndex() {
        return this.index;
    }

    public final int getStartPC() {
        return this.start_pc;
    }

    public final void setConstantPool(ConstantPool constantPool) {
        this.constant_pool = constantPool;
    }

    public final void setLength(int i) {
        this.length = i;
    }

    public final void setNameIndex(int i) {
        this.name_index = i;
    }

    public final void setSignatureIndex(int i) {
        this.signature_index = i;
    }

    public final void setIndex(int i) {
        this.index = i;
    }

    public final void setStartPC(int i) {
        this.start_pc = i;
    }

    @Override // java.lang.Object
    public final String toString() {
        String name = getName();
        String signatureToString = Utility.signatureToString(getSignature());
        return "LocalVariable(start_pc = " + this.start_pc + ", length = " + this.length + ", index = " + this.index + ":" + signatureToString + " " + name + ")";
    }

    public LocalVariable copy() {
        try {
            return (LocalVariable) clone();
        } catch (CloneNotSupportedException unused) {
            return null;
        }
    }
}
