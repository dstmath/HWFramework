package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class FieldOrMethod extends AccessFlags implements Cloneable, Node {
    protected Attribute[] attributes;
    protected int attributes_count;
    protected ConstantPool constant_pool;
    protected int name_index;
    protected int signature_index;

    FieldOrMethod() {
    }

    protected FieldOrMethod(FieldOrMethod fieldOrMethod) {
        this(fieldOrMethod.getAccessFlags(), fieldOrMethod.getNameIndex(), fieldOrMethod.getSignatureIndex(), fieldOrMethod.getAttributes(), fieldOrMethod.getConstantPool());
    }

    protected FieldOrMethod(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException, ClassFormatException {
        this(dataInputStream.readUnsignedShort(), dataInputStream.readUnsignedShort(), dataInputStream.readUnsignedShort(), null, constantPool);
        this.attributes_count = dataInputStream.readUnsignedShort();
        this.attributes = new Attribute[this.attributes_count];
        for (int i = 0; i < this.attributes_count; i++) {
            this.attributes[i] = Attribute.readAttribute(dataInputStream, constantPool);
        }
    }

    protected FieldOrMethod(int i, int i2, int i3, Attribute[] attributeArr, ConstantPool constantPool) {
        this.access_flags = i;
        this.name_index = i2;
        this.signature_index = i3;
        this.constant_pool = constantPool;
        setAttributes(attributeArr);
    }

    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeShort(this.access_flags);
        dataOutputStream.writeShort(this.name_index);
        dataOutputStream.writeShort(this.signature_index);
        dataOutputStream.writeShort(this.attributes_count);
        for (int i = 0; i < this.attributes_count; i++) {
            this.attributes[i].dump(dataOutputStream);
        }
    }

    public final Attribute[] getAttributes() {
        return this.attributes;
    }

    public final void setAttributes(Attribute[] attributeArr) {
        int i;
        this.attributes = attributeArr;
        if (attributeArr == null) {
            i = 0;
        } else {
            i = attributeArr.length;
        }
        this.attributes_count = i;
    }

    public final ConstantPool getConstantPool() {
        return this.constant_pool;
    }

    public final void setConstantPool(ConstantPool constantPool) {
        this.constant_pool = constantPool;
    }

    public final int getNameIndex() {
        return this.name_index;
    }

    public final void setNameIndex(int i) {
        this.name_index = i;
    }

    public final int getSignatureIndex() {
        return this.signature_index;
    }

    public final void setSignatureIndex(int i) {
        this.signature_index = i;
    }

    public final String getName() {
        return ((ConstantUtf8) this.constant_pool.getConstant(this.name_index, (byte) 1)).getBytes();
    }

    public final String getSignature() {
        return ((ConstantUtf8) this.constant_pool.getConstant(this.signature_index, (byte) 1)).getBytes();
    }

    /* access modifiers changed from: protected */
    public FieldOrMethod copy_(ConstantPool constantPool) {
        FieldOrMethod fieldOrMethod;
        try {
            fieldOrMethod = (FieldOrMethod) clone();
        } catch (CloneNotSupportedException unused) {
            fieldOrMethod = null;
        }
        fieldOrMethod.constant_pool = constantPool;
        fieldOrMethod.attributes = new Attribute[this.attributes_count];
        for (int i = 0; i < this.attributes_count; i++) {
            fieldOrMethod.attributes[i] = this.attributes[i].copy(constantPool);
        }
        return fieldOrMethod;
    }
}
