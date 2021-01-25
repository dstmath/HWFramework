package ohos.com.sun.org.apache.bcel.internal.generic;

public class DSTORE extends StoreInstruction {
    DSTORE() {
        super(57, 71);
    }

    public DSTORE(int i) {
        super(57, 71, i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.StoreInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        super.accept(visitor);
        visitor.visitDSTORE(this);
    }
}
