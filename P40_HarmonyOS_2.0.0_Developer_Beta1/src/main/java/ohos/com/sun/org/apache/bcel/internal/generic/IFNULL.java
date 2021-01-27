package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;

public class IFNULL extends IfInstruction {
    IFNULL() {
    }

    public IFNULL(InstructionHandle instructionHandle) {
        super(Constants.IFNULL, instructionHandle);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.IfInstruction
    public IfInstruction negate() {
        return new IFNONNULL(this.target);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackConsumer(this);
        visitor.visitBranchInstruction(this);
        visitor.visitIfInstruction(this);
        visitor.visitIFNULL(this);
    }
}
