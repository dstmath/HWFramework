package ohos.com.sun.org.apache.bcel.internal.generic;

public class CASTORE extends ArrayInstruction implements StackConsumer {
    public CASTORE() {
        super(85);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackConsumer(this);
        visitor.visitExceptionThrower(this);
        visitor.visitTypedInstruction(this);
        visitor.visitArrayInstruction(this);
        visitor.visitCASTORE(this);
    }
}
