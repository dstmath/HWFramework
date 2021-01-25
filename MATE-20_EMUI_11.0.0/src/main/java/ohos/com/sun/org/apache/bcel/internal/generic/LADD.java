package ohos.com.sun.org.apache.bcel.internal.generic;

public class LADD extends ArithmeticInstruction {
    public LADD() {
        super(97);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitTypedInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitStackConsumer(this);
        visitor.visitArithmeticInstruction(this);
        visitor.visitLADD(this);
    }
}
