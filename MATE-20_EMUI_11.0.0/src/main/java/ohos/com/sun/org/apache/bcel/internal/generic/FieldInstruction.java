package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantPool;

public abstract class FieldInstruction extends FieldOrMethod implements TypedInstruction {
    FieldInstruction() {
    }

    protected FieldInstruction(short s, int i) {
        super(s, i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.CPInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public String toString(ConstantPool constantPool) {
        return Constants.OPCODE_NAMES[this.opcode] + " " + constantPool.constantToString(this.index, (byte) 9);
    }

    /* access modifiers changed from: protected */
    public int getFieldSize(ConstantPoolGen constantPoolGen) {
        return getType(constantPoolGen).getSize();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.CPInstruction, ohos.com.sun.org.apache.bcel.internal.generic.TypedInstruction
    public Type getType(ConstantPoolGen constantPoolGen) {
        return getFieldType(constantPoolGen);
    }

    public Type getFieldType(ConstantPoolGen constantPoolGen) {
        return Type.getType(getSignature(constantPoolGen));
    }

    public String getFieldName(ConstantPoolGen constantPoolGen) {
        return getName(constantPoolGen);
    }
}
