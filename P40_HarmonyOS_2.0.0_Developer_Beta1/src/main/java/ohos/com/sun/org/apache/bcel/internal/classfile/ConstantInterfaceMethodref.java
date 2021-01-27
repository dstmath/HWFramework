package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.IOException;

public final class ConstantInterfaceMethodref extends ConstantCP {
    public ConstantInterfaceMethodref(ConstantInterfaceMethodref constantInterfaceMethodref) {
        super((byte) 11, constantInterfaceMethodref.getClassIndex(), constantInterfaceMethodref.getNameAndTypeIndex());
    }

    ConstantInterfaceMethodref(DataInputStream dataInputStream) throws IOException {
        super((byte) 11, dataInputStream);
    }

    public ConstantInterfaceMethodref(int i, int i2) {
        super((byte) 11, i, i2);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitConstantInterfaceMethodref(this);
    }
}
