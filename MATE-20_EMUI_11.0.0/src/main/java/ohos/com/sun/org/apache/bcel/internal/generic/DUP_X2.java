package ohos.com.sun.org.apache.bcel.internal.generic;

public class DUP_X2 extends StackInstruction {
    public DUP_X2() {
        super(91);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackInstruction(this);
        visitor.visitDUP_X2(this);
    }
}
