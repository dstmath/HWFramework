package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class InnerClasses extends Attribute {
    private InnerClass[] inner_classes;
    private int number_of_classes;

    public InnerClasses(InnerClasses innerClasses) {
        this(innerClasses.getNameIndex(), innerClasses.getLength(), innerClasses.getInnerClasses(), innerClasses.getConstantPool());
    }

    public InnerClasses(int i, int i2, InnerClass[] innerClassArr, ConstantPool constantPool) {
        super((byte) 6, i, i2, constantPool);
        setInnerClasses(innerClassArr);
    }

    InnerClasses(int i, int i2, DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        this(i, i2, (InnerClass[]) null, constantPool);
        this.number_of_classes = dataInputStream.readUnsignedShort();
        this.inner_classes = new InnerClass[this.number_of_classes];
        for (int i3 = 0; i3 < this.number_of_classes; i3++) {
            this.inner_classes[i3] = new InnerClass(dataInputStream);
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitInnerClasses(this);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        super.dump(dataOutputStream);
        dataOutputStream.writeShort(this.number_of_classes);
        for (int i = 0; i < this.number_of_classes; i++) {
            this.inner_classes[i].dump(dataOutputStream);
        }
    }

    public final InnerClass[] getInnerClasses() {
        return this.inner_classes;
    }

    public final void setInnerClasses(InnerClass[] innerClassArr) {
        int i;
        this.inner_classes = innerClassArr;
        if (innerClassArr == null) {
            i = 0;
        } else {
            i = innerClassArr.length;
        }
        this.number_of_classes = i;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, java.lang.Object
    public final String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < this.number_of_classes; i++) {
            stringBuffer.append(this.inner_classes[i].toString(this.constant_pool) + "\n");
        }
        return stringBuffer.toString();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public Attribute copy(ConstantPool constantPool) {
        InnerClasses innerClasses = (InnerClasses) clone();
        innerClasses.inner_classes = new InnerClass[this.number_of_classes];
        for (int i = 0; i < this.number_of_classes; i++) {
            innerClasses.inner_classes[i] = this.inner_classes[i].copy();
        }
        innerClasses.constant_pool = constantPool;
        return innerClasses;
    }
}
