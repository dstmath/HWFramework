package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;

public class L2D extends ConversionInstruction {
    public L2D() {
        super(Constants.L2D);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitTypedInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitStackConsumer(this);
        visitor.visitConversionInstruction(this);
        visitor.visitL2D(this);
    }
}
