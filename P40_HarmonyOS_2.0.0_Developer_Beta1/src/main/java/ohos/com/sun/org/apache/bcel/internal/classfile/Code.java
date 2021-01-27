package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class Code extends Attribute {
    private Attribute[] attributes;
    private int attributes_count;
    private byte[] code;
    private int code_length;
    private CodeException[] exception_table;
    private int exception_table_length;
    private int max_locals;
    private int max_stack;

    public Code(Code code2) {
        this(code2.getNameIndex(), code2.getLength(), code2.getMaxStack(), code2.getMaxLocals(), code2.getCode(), code2.getExceptionTable(), code2.getAttributes(), code2.getConstantPool());
    }

    Code(int i, int i2, DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        this(i, i2, dataInputStream.readUnsignedShort(), dataInputStream.readUnsignedShort(), null, null, null, constantPool);
        this.code_length = dataInputStream.readInt();
        this.code = new byte[this.code_length];
        dataInputStream.readFully(this.code);
        this.exception_table_length = dataInputStream.readUnsignedShort();
        this.exception_table = new CodeException[this.exception_table_length];
        for (int i3 = 0; i3 < this.exception_table_length; i3++) {
            this.exception_table[i3] = new CodeException(dataInputStream);
        }
        this.attributes_count = dataInputStream.readUnsignedShort();
        this.attributes = new Attribute[this.attributes_count];
        for (int i4 = 0; i4 < this.attributes_count; i4++) {
            this.attributes[i4] = Attribute.readAttribute(dataInputStream, constantPool);
        }
        this.length = i2;
    }

    public Code(int i, int i2, int i3, int i4, byte[] bArr, CodeException[] codeExceptionArr, Attribute[] attributeArr, ConstantPool constantPool) {
        super((byte) 2, i, i2, constantPool);
        this.max_stack = i3;
        this.max_locals = i4;
        setCode(bArr);
        setExceptionTable(codeExceptionArr);
        setAttributes(attributeArr);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitCode(this);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        super.dump(dataOutputStream);
        dataOutputStream.writeShort(this.max_stack);
        dataOutputStream.writeShort(this.max_locals);
        dataOutputStream.writeInt(this.code_length);
        dataOutputStream.write(this.code, 0, this.code_length);
        dataOutputStream.writeShort(this.exception_table_length);
        for (int i = 0; i < this.exception_table_length; i++) {
            this.exception_table[i].dump(dataOutputStream);
        }
        dataOutputStream.writeShort(this.attributes_count);
        for (int i2 = 0; i2 < this.attributes_count; i2++) {
            this.attributes[i2].dump(dataOutputStream);
        }
    }

    public final Attribute[] getAttributes() {
        return this.attributes;
    }

    public LineNumberTable getLineNumberTable() {
        for (int i = 0; i < this.attributes_count; i++) {
            Attribute[] attributeArr = this.attributes;
            if (attributeArr[i] instanceof LineNumberTable) {
                return (LineNumberTable) attributeArr[i];
            }
        }
        return null;
    }

    public LocalVariableTable getLocalVariableTable() {
        for (int i = 0; i < this.attributes_count; i++) {
            Attribute[] attributeArr = this.attributes;
            if (attributeArr[i] instanceof LocalVariableTable) {
                return (LocalVariableTable) attributeArr[i];
            }
        }
        return null;
    }

    public final byte[] getCode() {
        return this.code;
    }

    public final CodeException[] getExceptionTable() {
        return this.exception_table;
    }

    public final int getMaxLocals() {
        return this.max_locals;
    }

    public final int getMaxStack() {
        return this.max_stack;
    }

    private final int getInternalLength() {
        return this.code_length + 8 + 2 + (this.exception_table_length * 8) + 2;
    }

    private final int calculateLength() {
        int i = 0;
        for (int i2 = 0; i2 < this.attributes_count; i2++) {
            i += this.attributes[i2].length + 6;
        }
        return i + getInternalLength();
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
        this.length = calculateLength();
    }

    public final void setCode(byte[] bArr) {
        int i;
        this.code = bArr;
        if (bArr == null) {
            i = 0;
        } else {
            i = bArr.length;
        }
        this.code_length = i;
    }

    public final void setExceptionTable(CodeException[] codeExceptionArr) {
        int i;
        this.exception_table = codeExceptionArr;
        if (codeExceptionArr == null) {
            i = 0;
        } else {
            i = codeExceptionArr.length;
        }
        this.exception_table_length = i;
    }

    public final void setMaxLocals(int i) {
        this.max_locals = i;
    }

    public final void setMaxStack(int i) {
        this.max_stack = i;
    }

    public final String toString(boolean z) {
        StringBuilder sb = new StringBuilder();
        sb.append("Code(max_stack = ");
        sb.append(this.max_stack);
        sb.append(", max_locals = ");
        sb.append(this.max_locals);
        sb.append(", code_length = ");
        sb.append(this.code_length);
        sb.append(")\n");
        sb.append(Utility.codeToString(this.code, this.constant_pool, 0, -1, z));
        StringBuffer stringBuffer = new StringBuffer(sb.toString());
        if (this.exception_table_length > 0) {
            stringBuffer.append("\nException handler(s) = \nFrom\tTo\tHandler\tType\n");
            for (int i = 0; i < this.exception_table_length; i++) {
                stringBuffer.append(this.exception_table[i].toString(this.constant_pool, z) + "\n");
            }
        }
        if (this.attributes_count > 0) {
            stringBuffer.append("\nAttribute(s) = \n");
            for (int i2 = 0; i2 < this.attributes_count; i2++) {
                stringBuffer.append(this.attributes[i2].toString() + "\n");
            }
        }
        return stringBuffer.toString();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, java.lang.Object
    public final String toString() {
        return toString(true);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public Attribute copy(ConstantPool constantPool) {
        Code code2 = (Code) clone();
        code2.code = (byte[]) this.code.clone();
        code2.constant_pool = constantPool;
        code2.exception_table = new CodeException[this.exception_table_length];
        for (int i = 0; i < this.exception_table_length; i++) {
            code2.exception_table[i] = this.exception_table[i].copy();
        }
        code2.attributes = new Attribute[this.attributes_count];
        for (int i2 = 0; i2 < this.attributes_count; i2++) {
            code2.attributes[i2] = this.attributes[i2].copy(constantPool);
        }
        return code2;
    }
}
