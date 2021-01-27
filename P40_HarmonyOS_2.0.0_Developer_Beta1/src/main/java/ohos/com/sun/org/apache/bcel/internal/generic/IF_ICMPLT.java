package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;

public class IF_ICMPLT extends IfInstruction {
    IF_ICMPLT() {
    }

    public IF_ICMPLT(InstructionHandle instructionHandle) {
        super(Constants.IF_ICMPLT, instructionHandle);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.IfInstruction
    public IfInstruction negate() {
        return new IF_ICMPGE(this.target);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackConsumer(this);
        visitor.visitBranchInstruction(this);
        visitor.visitIfInstruction(this);
        visitor.visitIF_ICMPLT(this);
    }
}
