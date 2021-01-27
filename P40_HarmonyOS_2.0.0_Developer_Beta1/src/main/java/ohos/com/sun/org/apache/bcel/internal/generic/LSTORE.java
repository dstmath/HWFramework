package ohos.com.sun.org.apache.bcel.internal.generic;

public class LSTORE extends StoreInstruction {
    LSTORE() {
        super(55, 63);
    }

    public LSTORE(int i) {
        super(55, 63, i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.StoreInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        super.accept(visitor);
        visitor.visitLSTORE(this);
    }
}
