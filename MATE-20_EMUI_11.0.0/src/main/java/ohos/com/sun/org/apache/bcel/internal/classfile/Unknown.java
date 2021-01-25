package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import ohos.global.icu.text.PluralRules;

public final class Unknown extends Attribute {
    private static HashMap unknown_attributes = new HashMap();
    private byte[] bytes;
    private String name;

    static Unknown[] getUnknownAttributes() {
        Unknown[] unknownArr = new Unknown[unknown_attributes.size()];
        int i = 0;
        for (Unknown unknown : unknown_attributes.values()) {
            unknownArr[i] = unknown;
            i++;
        }
        unknown_attributes.clear();
        return unknownArr;
    }

    public Unknown(Unknown unknown) {
        this(unknown.getNameIndex(), unknown.getLength(), unknown.getBytes(), unknown.getConstantPool());
    }

    public Unknown(int i, int i2, byte[] bArr, ConstantPool constantPool) {
        super((byte) -1, i, i2, constantPool);
        this.bytes = bArr;
        this.name = ((ConstantUtf8) constantPool.getConstant(i, (byte) 1)).getBytes();
        unknown_attributes.put(this.name, this);
    }

    Unknown(int i, int i2, DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        this(i, i2, (byte[]) null, constantPool);
        if (i2 > 0) {
            this.bytes = new byte[i2];
            dataInputStream.readFully(this.bytes);
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitUnknown(this);
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

    public final String getName() {
        return this.name;
    }

    public final void setBytes(byte[] bArr) {
        this.bytes = bArr;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, java.lang.Object
    public final String toString() {
        String str;
        if (this.length == 0 || this.bytes == null) {
            return "(Unknown attribute " + this.name + ")";
        }
        if (this.length > 10) {
            byte[] bArr = new byte[10];
            System.arraycopy(this.bytes, 0, bArr, 0, 10);
            str = Utility.toHexString(bArr) + "... (truncated)";
        } else {
            str = Utility.toHexString(this.bytes);
        }
        return "(Unknown attribute " + this.name + PluralRules.KEYWORD_RULE_SEPARATOR + str + ")";
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public Attribute copy(ConstantPool constantPool) {
        Unknown unknown = (Unknown) clone();
        byte[] bArr = this.bytes;
        if (bArr != null) {
            unknown.bytes = (byte[]) bArr.clone();
        }
        unknown.constant_pool = constantPool;
        return unknown;
    }
}
