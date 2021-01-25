package ohos.com.sun.org.apache.bcel.internal.generic;

public class LSUB extends ArithmeticInstruction {
    public LSUB() {
        super(101);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitTypedInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitStackConsumer(this);
        visitor.visitArithmeticInstruction(this);
        visitor.visitLSUB(this);
    }
}
