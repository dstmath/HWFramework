package ohos.com.sun.org.apache.bcel.internal.generic;

public class FLOAD extends LoadInstruction {
    FLOAD() {
        super(23, 34);
    }

    public FLOAD(int i) {
        super(23, 34, i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.LoadInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        super.accept(visitor);
        visitor.visitFLOAD(this);
    }
}
