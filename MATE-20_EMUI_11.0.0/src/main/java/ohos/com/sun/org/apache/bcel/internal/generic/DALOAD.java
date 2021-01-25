package ohos.com.sun.org.apache.bcel.internal.generic;

public class DALOAD extends ArrayInstruction implements StackProducer {
    public DALOAD() {
        super(49);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackProducer(this);
        visitor.visitExceptionThrower(this);
        visitor.visitTypedInstruction(this);
        visitor.visitArrayInstruction(this);
        visitor.visitDALOAD(this);
    }
}
