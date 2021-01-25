package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.xpath.internal.XPath;

public class FCONST extends Instruction implements ConstantPushInstruction, TypedInstruction {
    private float value;

    FCONST() {
    }

    public FCONST(float f) {
        super(11, 1);
        double d = (double) f;
        if (d == XPath.MATCH_SCORE_QNAME) {
            this.opcode = 11;
        } else if (d == 1.0d) {
            this.opcode = 12;
        } else if (d == 2.0d) {
            this.opcode = 13;
        } else {
            throw new ClassGenException("FCONST can be used only for 0.0, 1.0 and 2.0: " + f);
        }
        this.value = f;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.ConstantPushInstruction
    public Number getValue() {
        return new Float(this.value);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.TypedInstruction
    public Type getType(ConstantPoolGen constantPoolGen) {
        return Type.FLOAT;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitPushInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitTypedInstruction(this);
        visitor.visitConstantPushInstruction(this);
        visitor.visitFCONST(this);
    }
}
