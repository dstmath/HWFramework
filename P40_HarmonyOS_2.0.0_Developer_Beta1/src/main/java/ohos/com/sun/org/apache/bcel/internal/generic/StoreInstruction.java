package ohos.com.sun.org.apache.bcel.internal.generic;

public abstract class StoreInstruction extends LocalVariableInstruction implements PopInstruction {
    StoreInstruction(short s, short s2) {
        super(s, s2);
    }

    protected StoreInstruction(short s, short s2, int i) {
        super(s, s2, i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackConsumer(this);
        visitor.visitPopInstruction(this);
        visitor.visitTypedInstruction(this);
        visitor.visitLocalVariableInstruction(this);
        visitor.visitStoreInstruction(this);
    }
}
