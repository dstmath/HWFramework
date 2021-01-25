package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;

public class FMUL extends ArithmeticInstruction {
    public FMUL() {
        super(Constants.FMUL);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitTypedInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitStackConsumer(this);
        visitor.visitArithmeticInstruction(this);
        visitor.visitFMUL(this);
    }
}
