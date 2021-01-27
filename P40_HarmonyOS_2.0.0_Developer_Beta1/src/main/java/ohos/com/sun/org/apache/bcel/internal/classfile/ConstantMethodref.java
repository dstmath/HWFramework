package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.IOException;

public final class ConstantMethodref extends ConstantCP {
    public ConstantMethodref(ConstantMethodref constantMethodref) {
        super((byte) 10, constantMethodref.getClassIndex(), constantMethodref.getNameAndTypeIndex());
    }

    ConstantMethodref(DataInputStream dataInputStream) throws IOException {
        super((byte) 10, dataInputStream);
    }

    public ConstantMethodref(int i, int i2) {
        super((byte) 10, i, i2);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitConstantMethodref(this);
    }
}
