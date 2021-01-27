package ohos.com.sun.org.apache.bcel.internal.generic;

public abstract class LoadInstruction extends LocalVariableInstruction implements PushInstruction {
    LoadInstruction(short s, short s2) {
        super(s, s2);
    }

    protected LoadInstruction(short s, short s2, int i) {
        super(s, s2, i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackProducer(this);
        visitor.visitPushInstruction(this);
        visitor.visitTypedInstruction(this);
        visitor.visitLocalVariableInstruction(this);
        visitor.visitLoadInstruction(this);
    }
}
