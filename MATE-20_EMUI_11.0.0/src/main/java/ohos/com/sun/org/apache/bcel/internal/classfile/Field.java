package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;

public final class Field extends FieldOrMethod {
    public Field(Field field) {
        super(field);
    }

    Field(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException, ClassFormatException {
        super(dataInputStream, constantPool);
    }

    public Field(int i, int i2, int i3, Attribute[] attributeArr, ConstantPool constantPool) {
        super(i, i2, i3, attributeArr, constantPool);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitField(this);
    }

    public final ConstantValue getConstantValue() {
        for (int i = 0; i < this.attributes_count; i++) {
            if (this.attributes[i].getTag() == 1) {
                return (ConstantValue) this.attributes[i];
            }
        }
        return null;
    }

    @Override // java.lang.Object
    public final String toString() {
        String accessToString = Utility.accessToString(this.access_flags);
        String str = "";
        if (!accessToString.equals(str)) {
            str = accessToString + " ";
        }
        StringBuffer stringBuffer = new StringBuffer(str + Utility.signatureToString(getSignature()) + " " + getName());
        ConstantValue constantValue = getConstantValue();
        if (constantValue != null) {
            stringBuffer.append(" = " + constantValue);
        }
        for (int i = 0; i < this.attributes_count; i++) {
            Attribute attribute = this.attributes[i];
            if (!(attribute instanceof ConstantValue)) {
                stringBuffer.append(" [" + attribute.toString() + "]");
            }
        }
        return stringBuffer.toString();
    }

    public final Field copy(ConstantPool constantPool) {
        return (Field) copy_(constantPool);
    }

    public Type getType() {
        return Type.getReturnType(getSignature());
    }
}
