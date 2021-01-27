package ohos.com.sun.org.apache.bcel.internal.generic;

public final class BranchHandle extends InstructionHandle {
    private static BranchHandle bh_list;
    private BranchInstruction bi;

    private BranchHandle(BranchInstruction branchInstruction) {
        super(branchInstruction);
        this.bi = branchInstruction;
    }

    static final BranchHandle getBranchHandle(BranchInstruction branchInstruction) {
        BranchHandle branchHandle = bh_list;
        if (branchHandle == null) {
            return new BranchHandle(branchInstruction);
        }
        bh_list = (BranchHandle) branchHandle.next;
        branchHandle.setInstruction(branchInstruction);
        return branchHandle;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle
    public void addHandle() {
        this.next = bh_list;
        bh_list = this;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle
    public int getPosition() {
        return this.bi.position;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle
    public void setPosition(int i) {
        this.bi.position = i;
        this.i_position = i;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle
    public int updatePosition(int i, int i2) {
        int updatePosition = this.bi.updatePosition(i, i2);
        this.i_position = this.bi.position;
        return updatePosition;
    }

    public void setTarget(InstructionHandle instructionHandle) {
        this.bi.setTarget(instructionHandle);
    }

    public void updateTarget(InstructionHandle instructionHandle, InstructionHandle instructionHandle2) {
        this.bi.updateTarget(instructionHandle, instructionHandle2);
    }

    public InstructionHandle getTarget() {
        return this.bi.getTarget();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle
    public void setInstruction(Instruction instruction) {
        super.setInstruction(instruction);
        if (instruction instanceof BranchInstruction) {
            this.bi = (BranchInstruction) instruction;
            return;
        }
        throw new ClassGenException("Assigning " + instruction + " to branch handle which is not a branch instruction");
    }
}
