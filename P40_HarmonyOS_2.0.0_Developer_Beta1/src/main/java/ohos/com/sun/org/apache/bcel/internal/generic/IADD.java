package ohos.com.sun.org.apache.bcel.internal.generic;

public class IADD extends ArithmeticInstruction {
    public IADD() {
        super(96);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitTypedInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitStackConsumer(this);
        visitor.visitArithmeticInstruction(this);
        visitor.visitIADD(this);
    }
}
