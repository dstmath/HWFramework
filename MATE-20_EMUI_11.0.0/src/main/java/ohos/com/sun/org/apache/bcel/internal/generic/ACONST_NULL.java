package ohos.com.sun.org.apache.bcel.internal.generic;

public class ACONST_NULL extends Instruction implements PushInstruction, TypedInstruction {
    public ACONST_NULL() {
        super(1, 1);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.TypedInstruction
    public Type getType(ConstantPoolGen constantPoolGen) {
        return Type.NULL;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackProducer(this);
        visitor.visitPushInstruction(this);
        visitor.visitTypedInstruction(this);
        visitor.visitACONST_NULL(this);
    }
}
