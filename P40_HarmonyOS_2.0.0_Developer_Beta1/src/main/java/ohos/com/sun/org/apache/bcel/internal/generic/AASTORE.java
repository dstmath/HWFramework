package ohos.com.sun.org.apache.bcel.internal.generic;

public class AASTORE extends ArrayInstruction implements StackConsumer {
    public AASTORE() {
        super(83);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackConsumer(this);
        visitor.visitExceptionThrower(this);
        visitor.visitTypedInstruction(this);
        visitor.visitArrayInstruction(this);
        visitor.visitAASTORE(this);
    }
}
