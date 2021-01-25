package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;

public class IFLT extends IfInstruction {
    IFLT() {
    }

    public IFLT(InstructionHandle instructionHandle) {
        super(Constants.IFLT, instructionHandle);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.IfInstruction
    public IfInstruction negate() {
        return new IFGE(this.target);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackConsumer(this);
        visitor.visitBranchInstruction(this);
        visitor.visitIfInstruction(this);
        visitor.visitIFLT(this);
    }
}
