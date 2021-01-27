package ohos.com.sun.org.apache.bcel.internal.generic;

public class BASTORE extends ArrayInstruction implements StackConsumer {
    public BASTORE() {
        super(84);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackConsumer(this);
        visitor.visitExceptionThrower(this);
        visitor.visitTypedInstruction(this);
        visitor.visitArrayInstruction(this);
        visitor.visitBASTORE(this);
    }
}
