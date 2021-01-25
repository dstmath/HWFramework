package ohos.com.sun.org.apache.bcel.internal.generic;

public class IASTORE extends ArrayInstruction implements StackConsumer {
    public IASTORE() {
        super(79);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackConsumer(this);
        visitor.visitExceptionThrower(this);
        visitor.visitTypedInstruction(this);
        visitor.visitArrayInstruction(this);
        visitor.visitIASTORE(this);
    }
}
