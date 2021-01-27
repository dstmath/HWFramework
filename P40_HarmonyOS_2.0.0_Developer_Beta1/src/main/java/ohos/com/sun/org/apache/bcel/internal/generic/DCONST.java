package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.xpath.internal.XPath;

public class DCONST extends Instruction implements ConstantPushInstruction, TypedInstruction {
    private double value;

    DCONST() {
    }

    public DCONST(double d) {
        super(14, 1);
        if (d == XPath.MATCH_SCORE_QNAME) {
            this.opcode = 14;
        } else if (d == 1.0d) {
            this.opcode = 15;
        } else {
            throw new ClassGenException("DCONST can be used only for 0.0 and 1.0: " + d);
        }
        this.value = d;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.ConstantPushInstruction
    public Number getValue() {
        return new Double(this.value);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.TypedInstruction
    public Type getType(ConstantPoolGen constantPoolGen) {
        return Type.DOUBLE;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitPushInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitTypedInstruction(this);
        visitor.visitConstantPushInstruction(this);
        visitor.visitDCONST(this);
    }
}
