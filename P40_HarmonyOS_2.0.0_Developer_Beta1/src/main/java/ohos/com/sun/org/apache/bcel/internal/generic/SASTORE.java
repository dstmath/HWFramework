package ohos.com.sun.org.apache.bcel.internal.generic;

public class SASTORE extends ArrayInstruction implements StackConsumer {
    public SASTORE() {
        super(86);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackConsumer(this);
        visitor.visitExceptionThrower(this);
        visitor.visitTypedInstruction(this);
        visitor.visitArrayInstruction(this);
        visitor.visitSASTORE(this);
    }
}
