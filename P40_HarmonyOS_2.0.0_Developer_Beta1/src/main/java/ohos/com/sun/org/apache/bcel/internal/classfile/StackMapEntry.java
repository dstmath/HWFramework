package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class StackMapEntry implements Cloneable {
    private int byte_code_offset;
    private ConstantPool constant_pool;
    private int number_of_locals;
    private int number_of_stack_items;
    private StackMapType[] types_of_locals;
    private StackMapType[] types_of_stack_items;

    StackMapEntry(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        this(dataInputStream.readShort(), dataInputStream.readShort(), null, -1, null, constantPool);
        this.types_of_locals = new StackMapType[this.number_of_locals];
        for (int i = 0; i < this.number_of_locals; i++) {
            this.types_of_locals[i] = new StackMapType(dataInputStream, constantPool);
        }
        this.number_of_stack_items = dataInputStream.readShort();
        this.types_of_stack_items = new StackMapType[this.number_of_stack_items];
        for (int i2 = 0; i2 < this.number_of_stack_items; i2++) {
            this.types_of_stack_items[i2] = new StackMapType(dataInputStream, constantPool);
        }
    }

    public StackMapEntry(int i, int i2, StackMapType[] stackMapTypeArr, int i3, StackMapType[] stackMapTypeArr2, ConstantPool constantPool) {
        this.byte_code_offset = i;
        this.number_of_locals = i2;
        this.types_of_locals = stackMapTypeArr;
        this.number_of_stack_items = i3;
        this.types_of_stack_items = stackMapTypeArr2;
        this.constant_pool = constantPool;
    }

    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeShort(this.byte_code_offset);
        dataOutputStream.writeShort(this.number_of_locals);
        for (int i = 0; i < this.number_of_locals; i++) {
            this.types_of_locals[i].dump(dataOutputStream);
        }
        dataOutputStream.writeShort(this.number_of_stack_items);
        for (int i2 = 0; i2 < this.number_of_stack_items; i2++) {
            this.types_of_stack_items[i2].dump(dataOutputStream);
        }
    }

    @Override // java.lang.Object
    public final String toString() {
        StringBuffer stringBuffer = new StringBuffer("(offset=" + this.byte_code_offset);
        if (this.number_of_locals > 0) {
            stringBuffer.append(", locals={");
            for (int i = 0; i < this.number_of_locals; i++) {
                stringBuffer.append(this.types_of_locals[i]);
                if (i < this.number_of_locals - 1) {
                    stringBuffer.append(", ");
                }
            }
            stringBuffer.append("}");
        }
        if (this.number_of_stack_items > 0) {
            stringBuffer.append(", stack items={");
            for (int i2 = 0; i2 < this.number_of_stack_items; i2++) {
                stringBuffer.append(this.types_of_stack_items[i2]);
                if (i2 < this.number_of_stack_items - 1) {
                    stringBuffer.append(", ");
                }
            }
            stringBuffer.append("}");
        }
        stringBuffer.append(")");
        return stringBuffer.toString();
    }

    public void setByteCodeOffset(int i) {
        this.byte_code_offset = i;
    }

    public int getByteCodeOffset() {
        return this.byte_code_offset;
    }

    public void setNumberOfLocals(int i) {
        this.number_of_locals = i;
    }

    public int getNumberOfLocals() {
        return this.number_of_locals;
    }

    public void setTypesOfLocals(StackMapType[] stackMapTypeArr) {
        this.types_of_locals = stackMapTypeArr;
    }

    public StackMapType[] getTypesOfLocals() {
        return this.types_of_locals;
    }

    public void setNumberOfStackItems(int i) {
        this.number_of_stack_items = i;
    }

    public int getNumberOfStackItems() {
        return this.number_of_stack_items;
    }

    public void setTypesOfStackItems(StackMapType[] stackMapTypeArr) {
        this.types_of_stack_items = stackMapTypeArr;
    }

    public StackMapType[] getTypesOfStackItems() {
        return this.types_of_stack_items;
    }

    public StackMapEntry copy() {
        try {
            return (StackMapEntry) clone();
        } catch (CloneNotSupportedException unused) {
            return null;
        }
    }

    public void accept(Visitor visitor) {
        visitor.visitStackMapEntry(this);
    }

    public final ConstantPool getConstantPool() {
        return this.constant_pool;
    }

    public final void setConstantPool(ConstantPool constantPool) {
        this.constant_pool = constantPool;
    }
}
