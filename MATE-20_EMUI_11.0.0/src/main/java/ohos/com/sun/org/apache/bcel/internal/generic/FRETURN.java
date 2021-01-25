package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;

public class FRETURN extends ReturnInstruction {
    public FRETURN() {
        super(Constants.FRETURN);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitExceptionThrower(this);
        visitor.visitTypedInstruction(this);
        visitor.visitStackConsumer(this);
        visitor.visitReturnInstruction(this);
        visitor.visitFRETURN(this);
    }
}
