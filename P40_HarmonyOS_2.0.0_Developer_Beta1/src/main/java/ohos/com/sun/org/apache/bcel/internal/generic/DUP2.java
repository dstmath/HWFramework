package ohos.com.sun.org.apache.bcel.internal.generic;

public class DUP2 extends StackInstruction implements PushInstruction {
    public DUP2() {
        super(92);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackProducer(this);
        visitor.visitPushInstruction(this);
        visitor.visitStackInstruction(this);
        visitor.visitDUP2(this);
    }
}
