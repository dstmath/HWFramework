package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.DataOutputStream;
import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.Constants;

public class GOTO extends GotoInstruction implements VariableLengthInstruction {
    GOTO() {
    }

    public GOTO(InstructionHandle instructionHandle) {
        super(Constants.GOTO, instructionHandle);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void dump(DataOutputStream dataOutputStream) throws IOException {
        this.index = getTargetOffset();
        if (this.opcode == 167) {
            super.dump(dataOutputStream);
            return;
        }
        this.index = getTargetOffset();
        dataOutputStream.writeByte(this.opcode);
        dataOutputStream.writeInt(this.index);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction
    public int updatePosition(int i, int i2) {
        int targetOffset = getTargetOffset();
        this.position += i;
        if (Math.abs(targetOffset) < 32767 - i2) {
            return 0;
        }
        this.opcode = Constants.GOTO_W;
        this.length = 5;
        return 2;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitVariableLengthInstruction(this);
        visitor.visitUnconditionalBranch(this);
        visitor.visitBranchInstruction(this);
        visitor.visitGotoInstruction(this);
        visitor.visitGOTO(this);
    }
}
