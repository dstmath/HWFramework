package ohos.com.sun.org.apache.bcel.internal.generic;

public class ISUB extends ArithmeticInstruction {
    public ISUB() {
        super(100);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitTypedInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitStackConsumer(this);
        visitor.visitArithmeticInstruction(this);
        visitor.visitISUB(this);
    }
}
