package ohos.com.sun.org.apache.bcel.internal.generic;

public class BALOAD extends ArrayInstruction implements StackProducer {
    public BALOAD() {
        super(51);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackProducer(this);
        visitor.visitExceptionThrower(this);
        visitor.visitTypedInstruction(this);
        visitor.visitArrayInstruction(this);
        visitor.visitBALOAD(this);
    }
}
