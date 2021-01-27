package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.DataOutputStream;
import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.util.ByteSequence;

public class JSR_W extends JsrInstruction {
    JSR_W() {
    }

    public JSR_W(InstructionHandle instructionHandle) {
        super(Constants.JSR_W, instructionHandle);
        this.length = 5;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void dump(DataOutputStream dataOutputStream) throws IOException {
        this.index = getTargetOffset();
        dataOutputStream.writeByte(this.opcode);
        dataOutputStream.writeInt(this.index);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void initFromFile(ByteSequence byteSequence, boolean z) throws IOException {
        this.index = byteSequence.readInt();
        this.length = 5;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackProducer(this);
        visitor.visitBranchInstruction(this);
        visitor.visitJsrInstruction(this);
        visitor.visitJSR_W(this);
    }
}
