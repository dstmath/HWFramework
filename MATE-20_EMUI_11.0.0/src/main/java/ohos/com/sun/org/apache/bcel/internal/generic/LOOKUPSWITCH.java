package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.DataOutputStream;
import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.util.ByteSequence;

public class LOOKUPSWITCH extends Select {
    LOOKUPSWITCH() {
    }

    public LOOKUPSWITCH(int[] iArr, InstructionHandle[] instructionHandleArr, InstructionHandle instructionHandle) {
        super(Constants.LOOKUPSWITCH, iArr, instructionHandleArr, instructionHandle);
        this.length = (short) ((this.match_length * 8) + 9);
        this.fixed_length = this.length;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Select, ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void dump(DataOutputStream dataOutputStream) throws IOException {
        super.dump(dataOutputStream);
        dataOutputStream.writeInt(this.match_length);
        for (int i = 0; i < this.match_length; i++) {
            dataOutputStream.writeInt(this.match[i]);
            int[] iArr = this.indices;
            int targetOffset = getTargetOffset(this.targets[i]);
            iArr[i] = targetOffset;
            dataOutputStream.writeInt(targetOffset);
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Select, ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void initFromFile(ByteSequence byteSequence, boolean z) throws IOException {
        super.initFromFile(byteSequence, z);
        this.match_length = byteSequence.readInt();
        this.fixed_length = (short) ((this.match_length * 8) + 9);
        this.length = (short) (this.fixed_length + this.padding);
        this.match = new int[this.match_length];
        this.indices = new int[this.match_length];
        this.targets = new InstructionHandle[this.match_length];
        for (int i = 0; i < this.match_length; i++) {
            this.match[i] = byteSequence.readInt();
            this.indices[i] = byteSequence.readInt();
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitVariableLengthInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitBranchInstruction(this);
        visitor.visitSelect(this);
        visitor.visitLOOKUPSWITCH(this);
    }
}
