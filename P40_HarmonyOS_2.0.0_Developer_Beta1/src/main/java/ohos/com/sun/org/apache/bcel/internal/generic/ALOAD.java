package ohos.com.sun.org.apache.bcel.internal.generic;

public class ALOAD extends LoadInstruction {
    ALOAD() {
        super(25, 42);
    }

    public ALOAD(int i) {
        super(25, 42, i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.LoadInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        super.accept(visitor);
        visitor.visitALOAD(this);
    }
}
