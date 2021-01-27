package ohos.com.sun.org.apache.bcel.internal.generic;

public class SALOAD extends ArrayInstruction implements StackProducer {
    public SALOAD() {
        super(53);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackProducer(this);
        visitor.visitExceptionThrower(this);
        visitor.visitTypedInstruction(this);
        visitor.visitArrayInstruction(this);
        visitor.visitSALOAD(this);
    }
}
