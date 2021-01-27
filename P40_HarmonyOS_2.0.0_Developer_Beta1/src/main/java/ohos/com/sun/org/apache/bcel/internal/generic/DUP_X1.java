package ohos.com.sun.org.apache.bcel.internal.generic;

public class DUP_X1 extends StackInstruction {
    public DUP_X1() {
        super(90);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackInstruction(this);
        visitor.visitDUP_X1(this);
    }
}
