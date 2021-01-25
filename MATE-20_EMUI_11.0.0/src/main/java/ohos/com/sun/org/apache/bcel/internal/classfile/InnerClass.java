package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class InnerClass implements Cloneable, Node {
    private int inner_access_flags;
    private int inner_class_index;
    private int inner_name_index;
    private int outer_class_index;

    public InnerClass(InnerClass innerClass) {
        this(innerClass.getInnerClassIndex(), innerClass.getOuterClassIndex(), innerClass.getInnerNameIndex(), innerClass.getInnerAccessFlags());
    }

    InnerClass(DataInputStream dataInputStream) throws IOException {
        this(dataInputStream.readUnsignedShort(), dataInputStream.readUnsignedShort(), dataInputStream.readUnsignedShort(), dataInputStream.readUnsignedShort());
    }

    public InnerClass(int i, int i2, int i3, int i4) {
        this.inner_class_index = i;
        this.outer_class_index = i2;
        this.inner_name_index = i3;
        this.inner_access_flags = i4;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitInnerClass(this);
    }

    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeShort(this.inner_class_index);
        dataOutputStream.writeShort(this.outer_class_index);
        dataOutputStream.writeShort(this.inner_name_index);
        dataOutputStream.writeShort(this.inner_access_flags);
    }

    public final int getInnerAccessFlags() {
        return this.inner_access_flags;
    }

    public final int getInnerClassIndex() {
        return this.inner_class_index;
    }

    public final int getInnerNameIndex() {
        return this.inner_name_index;
    }

    public final int getOuterClassIndex() {
        return this.outer_class_index;
    }

    public final void setInnerAccessFlags(int i) {
        this.inner_access_flags = i;
    }

    public final void setInnerClassIndex(int i) {
        this.inner_class_index = i;
    }

    public final void setInnerNameIndex(int i) {
        this.inner_name_index = i;
    }

    public final void setOuterClassIndex(int i) {
        this.outer_class_index = i;
    }

    @Override // java.lang.Object
    public final String toString() {
        return "InnerClass(" + this.inner_class_index + ", " + this.outer_class_index + ", " + this.inner_name_index + ", " + this.inner_access_flags + ")";
    }

    public final String toString(ConstantPool constantPool) {
        String compactClassName = Utility.compactClassName(constantPool.getConstantString(this.inner_class_index, (byte) 7));
        int i = this.outer_class_index;
        String compactClassName2 = i != 0 ? Utility.compactClassName(constantPool.getConstantString(i, (byte) 7)) : "<not a member>";
        int i2 = this.inner_name_index;
        String bytes = i2 != 0 ? ((ConstantUtf8) constantPool.getConstant(i2, (byte) 1)).getBytes() : "<anonymous>";
        String accessToString = Utility.accessToString(this.inner_access_flags, true);
        String str = "";
        if (!accessToString.equals(str)) {
            str = accessToString + " ";
        }
        return "InnerClass:" + str + compactClassName + "(\"" + compactClassName2 + "\", \"" + bytes + "\")";
    }

    public InnerClass copy() {
        try {
            return (InnerClass) clone();
        } catch (CloneNotSupportedException unused) {
            return null;
        }
    }
}
