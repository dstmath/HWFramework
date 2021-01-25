package ohos.com.sun.org.apache.bcel.internal.generic;

public class LASTORE extends ArrayInstruction implements StackConsumer {
    public LASTORE() {
        super(80);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackConsumer(this);
        visitor.visitExceptionThrower(this);
        visitor.visitTypedInstruction(this);
        visitor.visitArrayInstruction(this);
        visitor.visitLASTORE(this);
    }
}
