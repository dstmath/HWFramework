package ohos.com.sun.org.apache.bcel.internal.generic;

public class ISTORE extends StoreInstruction {
    ISTORE() {
        super(54, 59);
    }

    public ISTORE(int i) {
        super(54, 59, i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.StoreInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        super.accept(visitor);
        visitor.visitISTORE(this);
    }
}
