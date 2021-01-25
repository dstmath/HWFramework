package ohos.com.sun.org.apache.bcel.internal.generic;

public class DADD extends ArithmeticInstruction {
    public DADD() {
        super(99);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitTypedInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitStackConsumer(this);
        visitor.visitArithmeticInstruction(this);
        visitor.visitDADD(this);
    }
}
