package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class LineNumberTable extends Attribute {
    private LineNumber[] line_number_table;
    private int line_number_table_length;

    public LineNumberTable(LineNumberTable lineNumberTable) {
        this(lineNumberTable.getNameIndex(), lineNumberTable.getLength(), lineNumberTable.getLineNumberTable(), lineNumberTable.getConstantPool());
    }

    public LineNumberTable(int i, int i2, LineNumber[] lineNumberArr, ConstantPool constantPool) {
        super((byte) 4, i, i2, constantPool);
        setLineNumberTable(lineNumberArr);
    }

    LineNumberTable(int i, int i2, DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        this(i, i2, (LineNumber[]) null, constantPool);
        this.line_number_table_length = dataInputStream.readUnsignedShort();
        this.line_number_table = new LineNumber[this.line_number_table_length];
        for (int i3 = 0; i3 < this.line_number_table_length; i3++) {
            this.line_number_table[i3] = new LineNumber(dataInputStream);
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitLineNumberTable(this);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        super.dump(dataOutputStream);
        dataOutputStream.writeShort(this.line_number_table_length);
        for (int i = 0; i < this.line_number_table_length; i++) {
            this.line_number_table[i].dump(dataOutputStream);
        }
    }

    public final LineNumber[] getLineNumberTable() {
        return this.line_number_table;
    }

    public final void setLineNumberTable(LineNumber[] lineNumberArr) {
        int i;
        this.line_number_table = lineNumberArr;
        if (lineNumberArr == null) {
            i = 0;
        } else {
            i = lineNumberArr.length;
        }
        this.line_number_table_length = i;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, java.lang.Object
    public final String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer stringBuffer2 = new StringBuffer();
        for (int i = 0; i < this.line_number_table_length; i++) {
            stringBuffer2.append(this.line_number_table[i].toString());
            if (i < this.line_number_table_length - 1) {
                stringBuffer2.append(", ");
            }
            if (stringBuffer2.length() > 72) {
                stringBuffer2.append('\n');
                stringBuffer.append(stringBuffer2);
                stringBuffer2.setLength(0);
            }
        }
        stringBuffer.append(stringBuffer2);
        return stringBuffer.toString();
    }

    public int getSourceLine(int i) {
        int i2 = this.line_number_table_length - 1;
        if (i2 < 0) {
            return -1;
        }
        int i3 = 0;
        int i4 = -1;
        int i5 = -1;
        do {
            int i6 = (i3 + i2) / 2;
            int startPC = this.line_number_table[i6].getStartPC();
            if (startPC == i) {
                return this.line_number_table[i6].getLineNumber();
            }
            if (i < startPC) {
                i2 = i6 - 1;
            } else {
                i3 = i6 + 1;
            }
            if (startPC < i && startPC > i4) {
                i5 = i6;
                i4 = startPC;
                continue;
            }
        } while (i3 <= i2);
        if (i5 < 0) {
            return -1;
        }
        return this.line_number_table[i5].getLineNumber();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public Attribute copy(ConstantPool constantPool) {
        LineNumberTable lineNumberTable = (LineNumberTable) clone();
        lineNumberTable.line_number_table = new LineNumber[this.line_number_table_length];
        for (int i = 0; i < this.line_number_table_length; i++) {
            lineNumberTable.line_number_table[i] = this.line_number_table[i].copy();
        }
        lineNumberTable.constant_pool = constantPool;
        return lineNumberTable;
    }

    public final int getTableLength() {
        return this.line_number_table_length;
    }
}
