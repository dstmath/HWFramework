package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.DataOutputStream;
import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.util.ByteSequence;

public abstract class BranchInstruction extends Instruction implements InstructionTargeter {
    protected int index;
    protected int position;
    protected InstructionHandle target;

    BranchInstruction() {
    }

    protected BranchInstruction(short s, InstructionHandle instructionHandle) {
        super(s, 3);
        setTarget(instructionHandle);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.opcode);
        this.index = getTargetOffset();
        if (Math.abs(this.index) < 32767) {
            dataOutputStream.writeShort(this.index);
            return;
        }
        throw new ClassGenException("Branch target offset too large for short");
    }

    /* access modifiers changed from: protected */
    public int getTargetOffset(InstructionHandle instructionHandle) {
        if (instructionHandle != null) {
            int position2 = instructionHandle.getPosition();
            if (position2 >= 0) {
                return position2 - this.position;
            }
            throw new ClassGenException("Invalid branch target position offset for " + super.toString(true) + ":" + position2 + ":" + instructionHandle);
        }
        throw new ClassGenException("Target of " + super.toString(true) + " is invalid null handle");
    }

    /* access modifiers changed from: protected */
    public int getTargetOffset() {
        return getTargetOffset(this.target);
    }

    /* access modifiers changed from: protected */
    public int updatePosition(int i, int i2) {
        this.position += i;
        return 0;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public String toString(boolean z) {
        String str;
        String instruction = super.toString(z);
        if (z) {
            InstructionHandle instructionHandle = this.target;
            if (instructionHandle != null) {
                str = instructionHandle.getInstruction() == this ? "<points to itself>" : this.target.getInstruction() == null ? "<null instruction!!!?>" : this.target.getInstruction().toString(false);
                return instruction + " -> " + str;
            }
        } else if (this.target != null) {
            this.index = getTargetOffset();
            str = "" + (this.index + this.position);
            return instruction + " -> " + str;
        }
        str = "null";
        return instruction + " -> " + str;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void initFromFile(ByteSequence byteSequence, boolean z) throws IOException {
        this.length = 3;
        this.index = byteSequence.readShort();
    }

    public final int getIndex() {
        return this.index;
    }

    public InstructionHandle getTarget() {
        return this.target;
    }

    public final void setTarget(InstructionHandle instructionHandle) {
        notifyTargetChanging(this.target, this);
        this.target = instructionHandle;
        notifyTargetChanged(this.target, this);
    }

    static void notifyTargetChanging(InstructionHandle instructionHandle, InstructionTargeter instructionTargeter) {
        if (instructionHandle != null) {
            instructionHandle.removeTargeter(instructionTargeter);
        }
    }

    static void notifyTargetChanged(InstructionHandle instructionHandle, InstructionTargeter instructionTargeter) {
        if (instructionHandle != null) {
            instructionHandle.addTargeter(instructionTargeter);
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.InstructionTargeter
    public void updateTarget(InstructionHandle instructionHandle, InstructionHandle instructionHandle2) {
        if (this.target == instructionHandle) {
            setTarget(instructionHandle2);
            return;
        }
        throw new ClassGenException("Not targeting " + instructionHandle + ", but " + this.target);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.InstructionTargeter
    public boolean containsTarget(InstructionHandle instructionHandle) {
        return this.target == instructionHandle;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void dispose() {
        setTarget(null);
        this.index = -1;
        this.position = -1;
    }
}
