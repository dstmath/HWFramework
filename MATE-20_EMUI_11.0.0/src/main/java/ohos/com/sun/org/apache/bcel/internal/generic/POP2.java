package ohos.com.sun.org.apache.bcel.internal.generic;

public class POP2 extends StackInstruction implements PopInstruction {
    public POP2() {
        super(88);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackConsumer(this);
        visitor.visitPopInstruction(this);
        visitor.visitStackInstruction(this);
        visitor.visitPOP2(this);
    }
}
