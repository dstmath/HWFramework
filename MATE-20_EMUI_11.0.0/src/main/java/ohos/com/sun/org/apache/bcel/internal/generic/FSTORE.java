package ohos.com.sun.org.apache.bcel.internal.generic;

public class FSTORE extends StoreInstruction {
    FSTORE() {
        super(56, 67);
    }

    public FSTORE(int i) {
        super(56, 67, i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.StoreInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        super.accept(visitor);
        visitor.visitFSTORE(this);
    }
}
