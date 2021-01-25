package ohos.com.sun.org.apache.bcel.internal.generic;

public class ASTORE extends StoreInstruction {
    ASTORE() {
        super(58, 75);
    }

    public ASTORE(int i) {
        super(58, 75, i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.StoreInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        super.accept(visitor);
        visitor.visitASTORE(this);
    }
}
