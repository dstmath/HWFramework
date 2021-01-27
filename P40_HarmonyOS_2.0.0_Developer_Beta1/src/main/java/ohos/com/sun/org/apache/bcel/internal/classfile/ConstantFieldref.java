package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.IOException;

public final class ConstantFieldref extends ConstantCP {
    public ConstantFieldref(ConstantFieldref constantFieldref) {
        super((byte) 9, constantFieldref.getClassIndex(), constantFieldref.getNameAndTypeIndex());
    }

    ConstantFieldref(DataInputStream dataInputStream) throws IOException {
        super((byte) 9, dataInputStream);
    }

    public ConstantFieldref(int i, int i2) {
        super((byte) 9, i, i2);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Constant, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitConstantFieldref(this);
    }
}
