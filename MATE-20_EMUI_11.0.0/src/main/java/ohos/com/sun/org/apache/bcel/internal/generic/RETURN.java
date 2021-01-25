package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;

public class RETURN extends ReturnInstruction {
    public RETURN() {
        super(Constants.RETURN);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitExceptionThrower(this);
        visitor.visitTypedInstruction(this);
        visitor.visitStackConsumer(this);
        visitor.visitReturnInstruction(this);
        visitor.visitRETURN(this);
    }
}
