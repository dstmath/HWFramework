package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;

public class IXOR extends ArithmeticInstruction {
    public IXOR() {
        super(Constants.IXOR);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitTypedInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitStackConsumer(this);
        visitor.visitArithmeticInstruction(this);
        visitor.visitIXOR(this);
    }
}
