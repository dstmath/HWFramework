package ohos.com.sun.org.apache.bcel.internal.generic;

public class FASTORE extends ArrayInstruction implements StackConsumer {
    public FASTORE() {
        super(81);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackConsumer(this);
        visitor.visitExceptionThrower(this);
        visitor.visitTypedInstruction(this);
        visitor.visitArrayInstruction(this);
        visitor.visitFASTORE(this);
    }
}
