package ohos.com.sun.org.apache.bcel.internal.generic;

public class AALOAD extends ArrayInstruction implements StackProducer {
    public AALOAD() {
        super(50);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackProducer(this);
        visitor.visitExceptionThrower(this);
        visitor.visitTypedInstruction(this);
        visitor.visitArrayInstruction(this);
        visitor.visitAALOAD(this);
    }
}
