package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.DataOutputStream;
import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.Constants;

public class JSR extends JsrInstruction implements VariableLengthInstruction {
    JSR() {
    }

    public JSR(InstructionHandle instructionHandle) {
        super(Constants.JSR, instructionHandle);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void dump(DataOutputStream dataOutputStream) throws IOException {
        this.index = getTargetOffset();
        if (this.opcode == 168) {
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
        this.opcode = Constants.JSR_W;
        this.length = 5;
        return 2;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackProducer(this);
        visitor.visitVariableLengthInstruction(this);
        visitor.visitBranchInstruction(this);
        visitor.visitJsrInstruction(this);
        visitor.visitJSR(this);
    }
}
