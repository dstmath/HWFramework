package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.DataOutputStream;
import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.util.ByteSequence;

public abstract class Select extends BranchInstruction implements VariableLengthInstruction, StackProducer {
    protected int fixed_length;
    protected int[] indices;
    protected int[] match;
    protected int match_length;
    protected int padding = 0;
    protected InstructionHandle[] targets;

    Select() {
    }

    Select(short s, int[] iArr, InstructionHandle[] instructionHandleArr, InstructionHandle instructionHandle) {
        super(s, instructionHandle);
        this.targets = instructionHandleArr;
        for (InstructionHandle instructionHandle2 : instructionHandleArr) {
            BranchInstruction.notifyTargetChanged(instructionHandle2, this);
        }
        this.match = iArr;
        int length = iArr.length;
        this.match_length = length;
        if (length == instructionHandleArr.length) {
            this.indices = new int[this.match_length];
            return;
        }
        throw new ClassGenException("Match and target array have not the same length");
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction
    public int updatePosition(int i, int i2) {
        this.position += i;
        short s = this.length;
        this.padding = (4 - ((this.position + 1) % 4)) % 4;
        this.length = (short) (this.fixed_length + this.padding);
        return this.length - s;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.opcode);
        for (int i = 0; i < this.padding; i++) {
            dataOutputStream.writeByte(0);
        }
        this.index = getTargetOffset();
        dataOutputStream.writeInt(this.index);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void initFromFile(ByteSequence byteSequence, boolean z) throws IOException {
        this.padding = (4 - (byteSequence.getIndex() % 4)) % 4;
        for (int i = 0; i < this.padding; i++) {
            byteSequence.readByte();
        }
        this.index = byteSequence.readInt();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public String toString(boolean z) {
        StringBuilder sb = new StringBuilder(super.toString(z));
        if (z) {
            for (int i = 0; i < this.match_length; i++) {
                InstructionHandle[] instructionHandleArr = this.targets;
                String instruction = instructionHandleArr[i] != null ? instructionHandleArr[i].getInstruction().toString() : "null";
                sb.append("(");
                sb.append(this.match[i]);
                sb.append(", ");
                sb.append(instruction);
                sb.append(" = {");
                sb.append(this.indices[i]);
                sb.append("})");
            }
        } else {
            sb.append(" ...");
        }
        return sb.toString();
    }

    public final void setTarget(int i, InstructionHandle instructionHandle) {
        notifyTargetChanging(this.targets[i], this);
        InstructionHandle[] instructionHandleArr = this.targets;
        instructionHandleArr[i] = instructionHandle;
        notifyTargetChanged(instructionHandleArr[i], this);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction, ohos.com.sun.org.apache.bcel.internal.generic.InstructionTargeter
    public void updateTarget(InstructionHandle instructionHandle, InstructionHandle instructionHandle2) {
        boolean z;
        int i = 0;
        if (this.target == instructionHandle) {
            setTarget(instructionHandle2);
            z = true;
        } else {
            z = false;
        }
        while (true) {
            InstructionHandle[] instructionHandleArr = this.targets;
            if (i >= instructionHandleArr.length) {
                break;
            }
            if (instructionHandleArr[i] == instructionHandle) {
                setTarget(i, instructionHandle2);
                z = true;
            }
            i++;
        }
        if (!z) {
            throw new ClassGenException("Not targeting " + instructionHandle);
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction, ohos.com.sun.org.apache.bcel.internal.generic.InstructionTargeter
    public boolean containsTarget(InstructionHandle instructionHandle) {
        if (this.target == instructionHandle) {
            return true;
        }
        int i = 0;
        while (true) {
            InstructionHandle[] instructionHandleArr = this.targets;
            if (i >= instructionHandleArr.length) {
                return false;
            }
            if (instructionHandleArr[i] == instructionHandle) {
                return true;
            }
            i++;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void dispose() {
        super.dispose();
        int i = 0;
        while (true) {
            InstructionHandle[] instructionHandleArr = this.targets;
            if (i < instructionHandleArr.length) {
                instructionHandleArr[i].removeTargeter(this);
                i++;
            } else {
                return;
            }
        }
    }

    public int[] getMatchs() {
        return this.match;
    }

    public int[] getIndices() {
        return this.indices;
    }

    public InstructionHandle[] getTargets() {
        return this.targets;
    }
}
