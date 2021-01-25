package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;

public class ISHR extends ArithmeticInstruction {
    public ISHR() {
        super(Constants.ISHR);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitTypedInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitStackConsumer(this);
        visitor.visitArithmeticInstruction(this);
        visitor.visitISHR(this);
    }
}
