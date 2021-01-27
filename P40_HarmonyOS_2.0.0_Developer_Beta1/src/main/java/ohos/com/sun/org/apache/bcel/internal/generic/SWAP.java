package ohos.com.sun.org.apache.bcel.internal.generic;

public class SWAP extends StackInstruction implements StackConsumer, StackProducer {
    public SWAP() {
        super(95);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackConsumer(this);
        visitor.visitStackProducer(this);
        visitor.visitStackInstruction(this);
        visitor.visitSWAP(this);
    }
}
