package ohos.com.sun.org.apache.bcel.internal.generic;

public class POP extends StackInstruction implements PopInstruction {
    public POP() {
        super(87);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackConsumer(this);
        visitor.visitPopInstruction(this);
        visitor.visitStackInstruction(this);
        visitor.visitPOP(this);
    }
}
