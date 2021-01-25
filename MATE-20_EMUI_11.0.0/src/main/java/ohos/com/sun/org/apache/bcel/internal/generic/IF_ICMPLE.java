package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;

public class IF_ICMPLE extends IfInstruction {
    IF_ICMPLE() {
    }

    public IF_ICMPLE(InstructionHandle instructionHandle) {
        super(Constants.IF_ICMPLE, instructionHandle);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.IfInstruction
    public IfInstruction negate() {
        return new IF_ICMPGT(this.target);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackConsumer(this);
        visitor.visitBranchInstruction(this);
        visitor.visitIfInstruction(this);
        visitor.visitIF_ICMPLE(this);
    }
}
