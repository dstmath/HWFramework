package ohos.com.sun.org.apache.bcel.internal.generic;

public class NOP extends Instruction {
    public NOP() {
        super(0, 1);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitNOP(this);
    }
}
