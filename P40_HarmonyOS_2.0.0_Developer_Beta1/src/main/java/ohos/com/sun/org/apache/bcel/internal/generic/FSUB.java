package ohos.com.sun.org.apache.bcel.internal.generic;

public class FSUB extends ArithmeticInstruction {
    public FSUB() {
        super(102);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitTypedInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitStackConsumer(this);
        visitor.visitArithmeticInstruction(this);
        visitor.visitFSUB(this);
    }
}
