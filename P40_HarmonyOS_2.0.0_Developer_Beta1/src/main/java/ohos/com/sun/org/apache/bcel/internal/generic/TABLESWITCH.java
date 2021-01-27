package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.DataOutputStream;
import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.util.ByteSequence;

public class TABLESWITCH extends Select {
    TABLESWITCH() {
    }

    public TABLESWITCH(int[] iArr, InstructionHandle[] instructionHandleArr, InstructionHandle instructionHandle) {
        super(Constants.TABLESWITCH, iArr, instructionHandleArr, instructionHandle);
        this.length = (short) ((this.match_length * 4) + 13);
        this.fixed_length = this.length;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Select, ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void dump(DataOutputStream dataOutputStream) throws IOException {
        super.dump(dataOutputStream);
        dataOutputStream.writeInt(this.match_length > 0 ? this.match[0] : 0);
        dataOutputStream.writeInt(this.match_length > 0 ? this.match[this.match_length - 1] : 0);
        for (int i = 0; i < this.match_length; i++) {
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
        int readInt = byteSequence.readInt();
        int readInt2 = byteSequence.readInt();
        this.match_length = (readInt2 - readInt) + 1;
        this.fixed_length = (short) ((this.match_length * 4) + 13);
        this.length = (short) (this.fixed_length + this.padding);
        this.match = new int[this.match_length];
        this.indices = new int[this.match_length];
        this.targets = new InstructionHandle[this.match_length];
        for (int i = readInt; i <= readInt2; i++) {
            this.match[i - readInt] = i;
        }
        for (int i2 = 0; i2 < this.match_length; i2++) {
            this.indices[i2] = byteSequence.readInt();
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitVariableLengthInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitBranchInstruction(this);
        visitor.visitSelect(this);
        visitor.visitTABLESWITCH(this);
    }
}
