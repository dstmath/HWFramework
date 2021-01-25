package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;

public class IF_ACMPNE extends IfInstruction {
    IF_ACMPNE() {
    }

    public IF_ACMPNE(InstructionHandle instructionHandle) {
        super(Constants.IF_ACMPNE, instructionHandle);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.IfInstruction
    public IfInstruction negate() {
        return new IF_ACMPEQ(this.target);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackConsumer(this);
        visitor.visitBranchInstruction(this);
        visitor.visitIfInstruction(this);
        visitor.visitIF_ACMPNE(this);
    }
}
