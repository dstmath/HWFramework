package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;

public class IF_ICMPEQ extends IfInstruction {
    IF_ICMPEQ() {
    }

    public IF_ICMPEQ(InstructionHandle instructionHandle) {
        super(Constants.IF_ICMPEQ, instructionHandle);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.IfInstruction
    public IfInstruction negate() {
        return new IF_ICMPNE(this.target);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackConsumer(this);
        visitor.visitBranchInstruction(this);
        visitor.visitIfInstruction(this);
        visitor.visitIF_ICMPEQ(this);
    }
}
