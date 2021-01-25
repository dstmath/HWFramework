package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LocalVariableTypeTable extends Attribute {
    private static final long serialVersionUID = -1082157891095177114L;
    private LocalVariable[] local_variable_type_table;
    private int local_variable_type_table_length;

    public LocalVariableTypeTable(LocalVariableTypeTable localVariableTypeTable) {
        this(localVariableTypeTable.getNameIndex(), localVariableTypeTable.getLength(), localVariableTypeTable.getLocalVariableTypeTable(), localVariableTypeTable.getConstantPool());
    }

    public LocalVariableTypeTable(int i, int i2, LocalVariable[] localVariableArr, ConstantPool constantPool) {
        super((byte) 12, i, i2, constantPool);
        setLocalVariableTable(localVariableArr);
    }

    LocalVariableTypeTable(int i, int i2, DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        this(i, i2, (LocalVariable[]) null, constantPool);
        this.local_variable_type_table_length = dataInputStream.readUnsignedShort();
        this.local_variable_type_table = new LocalVariable[this.local_variable_type_table_length];
        for (int i3 = 0; i3 < this.local_variable_type_table_length; i3++) {
            this.local_variable_type_table[i3] = new LocalVariable(dataInputStream, constantPool);
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitLocalVariableTypeTable(this);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        super.dump(dataOutputStream);
        dataOutputStream.writeShort(this.local_variable_type_table_length);
        for (int i = 0; i < this.local_variable_type_table_length; i++) {
            this.local_variable_type_table[i].dump(dataOutputStream);
        }
    }

    public final LocalVariable[] getLocalVariableTypeTable() {
        return this.local_variable_type_table;
    }

    public final LocalVariable getLocalVariable(int i) {
        for (int i2 = 0; i2 < this.local_variable_type_table_length; i2++) {
            if (this.local_variable_type_table[i2].getIndex() == i) {
                return this.local_variable_type_table[i2];
            }
        }
        return null;
    }

    public final void setLocalVariableTable(LocalVariable[] localVariableArr) {
        int i;
        this.local_variable_type_table = localVariableArr;
        if (localVariableArr == null) {
            i = 0;
        } else {
            i = localVariableArr.length;
        }
        this.local_variable_type_table_length = i;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, java.lang.Object
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.local_variable_type_table_length; i++) {
            sb.append(this.local_variable_type_table[i].toString());
            if (i < this.local_variable_type_table_length - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public Attribute copy(ConstantPool constantPool) {
        LocalVariableTypeTable localVariableTypeTable = (LocalVariableTypeTable) clone();
        localVariableTypeTable.local_variable_type_table = new LocalVariable[this.local_variable_type_table_length];
        for (int i = 0; i < this.local_variable_type_table_length; i++) {
            localVariableTypeTable.local_variable_type_table[i] = this.local_variable_type_table[i].copy();
        }
        localVariableTypeTable.constant_pool = constantPool;
        return localVariableTypeTable;
    }

    public final int getTableLength() {
        return this.local_variable_type_table_length;
    }
}
