package ohos.com.sun.org.apache.bcel.internal.generic;

public class DASTORE extends ArrayInstruction implements StackConsumer {
    public DASTORE() {
        super(82);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackConsumer(this);
        visitor.visitExceptionThrower(this);
        visitor.visitTypedInstruction(this);
        visitor.visitArrayInstruction(this);
        visitor.visitDASTORE(this);
    }
}
