package ohos.com.sun.org.apache.bcel.internal.generic;

public class FADD extends ArithmeticInstruction {
    public FADD() {
        super(98);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitTypedInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitStackConsumer(this);
        visitor.visitArithmeticInstruction(this);
        visitor.visitFADD(this);
    }
}
