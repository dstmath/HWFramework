package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class ConstantValue extends Attribute {
    private int constantvalue_index;

    public ConstantValue(ConstantValue constantValue) {
        this(constantValue.getNameIndex(), constantValue.getLength(), constantValue.getConstantValueIndex(), constantValue.getConstantPool());
    }

    ConstantValue(int i, int i2, DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        this(i, i2, dataInputStream.readUnsignedShort(), constantPool);
    }

    public ConstantValue(int i, int i2, int i3, ConstantPool constantPool) {
        super((byte) 1, i, i2, constantPool);
        this.constantvalue_index = i3;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitConstantValue(this);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        super.dump(dataOutputStream);
        dataOutputStream.writeShort(this.constantvalue_index);
    }

    public final int getConstantValueIndex() {
        return this.constantvalue_index;
    }

    public final void setConstantValueIndex(int i) {
        this.constantvalue_index = i;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, java.lang.Object
    public final String toString() {
        Constant constant = this.constant_pool.getConstant(this.constantvalue_index);
        byte tag = constant.getTag();
        if (tag == 3) {
            return "" + ((ConstantInteger) constant).getBytes();
        } else if (tag == 4) {
            return "" + ((ConstantFloat) constant).getBytes();
        } else if (tag == 5) {
            return "" + ((ConstantLong) constant).getBytes();
        } else if (tag == 6) {
            return "" + ((ConstantDouble) constant).getBytes();
        } else if (tag == 8) {
            Constant constant2 = this.constant_pool.getConstant(((ConstantString) constant).getStringIndex(), (byte) 1);
            return "\"" + Utility.convertString(((ConstantUtf8) constant2).getBytes()) + "\"";
        } else {
            throw new IllegalStateException("Type of ConstValue invalid: " + constant);
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public Attribute copy(ConstantPool constantPool) {
        ConstantValue constantValue = (ConstantValue) clone();
        constantValue.constant_pool = constantPool;
        return constantValue;
    }
}
