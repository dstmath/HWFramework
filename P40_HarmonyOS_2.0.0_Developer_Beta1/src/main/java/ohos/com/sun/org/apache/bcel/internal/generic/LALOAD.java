package ohos.com.sun.org.apache.bcel.internal.generic;

public class LALOAD extends ArrayInstruction implements StackProducer {
    public LALOAD() {
        super(47);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackProducer(this);
        visitor.visitExceptionThrower(this);
        visitor.visitTypedInstruction(this);
        visitor.visitArrayInstruction(this);
        visitor.visitLALOAD(this);
    }
}
