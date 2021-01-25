package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class PMGClass extends Attribute {
    private int pmg_class_index;
    private int pmg_index;

    public PMGClass(PMGClass pMGClass) {
        this(pMGClass.getNameIndex(), pMGClass.getLength(), pMGClass.getPMGIndex(), pMGClass.getPMGClassIndex(), pMGClass.getConstantPool());
    }

    PMGClass(int i, int i2, DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        this(i, i2, dataInputStream.readUnsignedShort(), dataInputStream.readUnsignedShort(), constantPool);
    }

    public PMGClass(int i, int i2, int i3, int i4, ConstantPool constantPool) {
        super((byte) 9, i, i2, constantPool);
        this.pmg_index = i3;
        this.pmg_class_index = i4;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        System.err.println("Visiting non-standard PMGClass object");
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        super.dump(dataOutputStream);
        dataOutputStream.writeShort(this.pmg_index);
        dataOutputStream.writeShort(this.pmg_class_index);
    }

    public final int getPMGClassIndex() {
        return this.pmg_class_index;
    }

    public final void setPMGClassIndex(int i) {
        this.pmg_class_index = i;
    }

    public final int getPMGIndex() {
        return this.pmg_index;
    }

    public final void setPMGIndex(int i) {
        this.pmg_index = i;
    }

    public final String getPMGName() {
        return ((ConstantUtf8) this.constant_pool.getConstant(this.pmg_index, (byte) 1)).getBytes();
    }

    public final String getPMGClassName() {
        return ((ConstantUtf8) this.constant_pool.getConstant(this.pmg_class_index, (byte) 1)).getBytes();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, java.lang.Object
    public final String toString() {
        return "PMGClass(" + getPMGName() + ", " + getPMGClassName() + ")";
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public Attribute copy(ConstantPool constantPool) {
        return (PMGClass) clone();
    }
}
