package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class ExceptionTable extends Attribute {
    private int[] exception_index_table;
    private int number_of_exceptions;

    public ExceptionTable(ExceptionTable exceptionTable) {
        this(exceptionTable.getNameIndex(), exceptionTable.getLength(), exceptionTable.getExceptionIndexTable(), exceptionTable.getConstantPool());
    }

    public ExceptionTable(int i, int i2, int[] iArr, ConstantPool constantPool) {
        super((byte) 3, i, i2, constantPool);
        setExceptionIndexTable(iArr);
    }

    ExceptionTable(int i, int i2, DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        this(i, i2, (int[]) null, constantPool);
        this.number_of_exceptions = dataInputStream.readUnsignedShort();
        this.exception_index_table = new int[this.number_of_exceptions];
        for (int i3 = 0; i3 < this.number_of_exceptions; i3++) {
            this.exception_index_table[i3] = dataInputStream.readUnsignedShort();
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitExceptionTable(this);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        super.dump(dataOutputStream);
        dataOutputStream.writeShort(this.number_of_exceptions);
        for (int i = 0; i < this.number_of_exceptions; i++) {
            dataOutputStream.writeShort(this.exception_index_table[i]);
        }
    }

    public final int[] getExceptionIndexTable() {
        return this.exception_index_table;
    }

    public final int getNumberOfExceptions() {
        return this.number_of_exceptions;
    }

    public final String[] getExceptionNames() {
        String[] strArr = new String[this.number_of_exceptions];
        for (int i = 0; i < this.number_of_exceptions; i++) {
            strArr[i] = this.constant_pool.getConstantString(this.exception_index_table[i], (byte) 7).replace('/', '.');
        }
        return strArr;
    }

    public final void setExceptionIndexTable(int[] iArr) {
        int i;
        this.exception_index_table = iArr;
        if (iArr == null) {
            i = 0;
        } else {
            i = iArr.length;
        }
        this.number_of_exceptions = i;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, java.lang.Object
    public final String toString() {
        StringBuffer stringBuffer = new StringBuffer("");
        for (int i = 0; i < this.number_of_exceptions; i++) {
            stringBuffer.append(Utility.compactClassName(this.constant_pool.getConstantString(this.exception_index_table[i], (byte) 7), false));
            if (i < this.number_of_exceptions - 1) {
                stringBuffer.append(", ");
            }
        }
        return stringBuffer.toString();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public Attribute copy(ConstantPool constantPool) {
        ExceptionTable exceptionTable = (ExceptionTable) clone();
        exceptionTable.exception_index_table = (int[]) this.exception_index_table.clone();
        exceptionTable.constant_pool = constantPool;
        return exceptionTable;
    }
}
