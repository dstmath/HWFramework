package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.classfile.Constant;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantDouble;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantLong;

public class LDC2_W extends CPInstruction implements PushInstruction, TypedInstruction {
    LDC2_W() {
    }

    public LDC2_W(int i) {
        super(20, i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.CPInstruction, ohos.com.sun.org.apache.bcel.internal.generic.TypedInstruction
    public Type getType(ConstantPoolGen constantPoolGen) {
        byte tag = constantPoolGen.getConstantPool().getConstant(this.index).getTag();
        if (tag == 5) {
            return Type.LONG;
        }
        if (tag == 6) {
            return Type.DOUBLE;
        }
        throw new RuntimeException("Unknown constant type " + ((int) this.opcode));
    }

    public Number getValue(ConstantPoolGen constantPoolGen) {
        Constant constant = constantPoolGen.getConstantPool().getConstant(this.index);
        byte tag = constant.getTag();
        if (tag == 5) {
            return new Long(((ConstantLong) constant).getBytes());
        }
        if (tag == 6) {
            return new Double(((ConstantDouble) constant).getBytes());
        }
        throw new RuntimeException("Unknown or invalid constant type at " + this.index);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackProducer(this);
        visitor.visitPushInstruction(this);
        visitor.visitTypedInstruction(this);
        visitor.visitCPInstruction(this);
        visitor.visitLDC2_W(this);
    }
}
