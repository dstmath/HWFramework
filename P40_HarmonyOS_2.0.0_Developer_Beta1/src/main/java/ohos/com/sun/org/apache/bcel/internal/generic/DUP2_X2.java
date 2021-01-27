package ohos.com.sun.org.apache.bcel.internal.generic;

public class DUP2_X2 extends StackInstruction {
    public DUP2_X2() {
        super(94);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackInstruction(this);
        visitor.visitDUP2_X2(this);
    }
}
