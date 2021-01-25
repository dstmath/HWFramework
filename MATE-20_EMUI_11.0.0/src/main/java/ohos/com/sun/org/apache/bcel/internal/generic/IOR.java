package ohos.com.sun.org.apache.bcel.internal.generic;

public class IOR extends ArithmeticInstruction {
    public IOR() {
        super(128);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitTypedInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitStackConsumer(this);
        visitor.visitArithmeticInstruction(this);
        visitor.visitIOR(this);
    }
}
