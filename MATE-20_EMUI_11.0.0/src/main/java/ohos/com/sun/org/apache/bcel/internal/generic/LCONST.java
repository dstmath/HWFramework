package ohos.com.sun.org.apache.bcel.internal.generic;

public class LCONST extends Instruction implements ConstantPushInstruction, TypedInstruction {
    private long value;

    LCONST() {
    }

    public LCONST(long j) {
        super(9, 1);
        if (j == 0) {
            this.opcode = 9;
        } else if (j == 1) {
            this.opcode = 10;
        } else {
            throw new ClassGenException("LCONST can be used only for 0 and 1: " + j);
        }
        this.value = j;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.ConstantPushInstruction
    public Number getValue() {
        return new Long(this.value);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.TypedInstruction
    public Type getType(ConstantPoolGen constantPoolGen) {
        return Type.LONG;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitPushInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitTypedInstruction(this);
        visitor.visitConstantPushInstruction(this);
        visitor.visitLCONST(this);
    }
}
