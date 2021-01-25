package ohos.com.sun.org.apache.bcel.internal.generic;

public class ICONST extends Instruction implements ConstantPushInstruction, TypedInstruction {
    private int value;

    ICONST() {
    }

    public ICONST(int i) {
        super(3, 1);
        if (i < -1 || i > 5) {
            throw new ClassGenException("ICONST can be used only for value between -1 and 5: " + i);
        }
        this.opcode = (short) (i + 3);
        this.value = i;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.ConstantPushInstruction
    public Number getValue() {
        return new Integer(this.value);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.TypedInstruction
    public Type getType(ConstantPoolGen constantPoolGen) {
        return Type.INT;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitPushInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitTypedInstruction(this);
        visitor.visitConstantPushInstruction(this);
        visitor.visitICONST(this);
    }
}
