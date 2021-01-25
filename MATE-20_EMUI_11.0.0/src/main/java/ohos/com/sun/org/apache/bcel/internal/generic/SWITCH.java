package ohos.com.sun.org.apache.bcel.internal.generic;

public final class SWITCH implements CompoundInstruction {
    private Select instruction;
    private int[] match;
    private int match_length;
    private InstructionHandle[] targets;

    public SWITCH(int[] iArr, InstructionHandle[] instructionHandleArr, InstructionHandle instructionHandle, int i) {
        this.match = (int[]) iArr.clone();
        this.targets = (InstructionHandle[]) instructionHandleArr.clone();
        int length = iArr.length;
        this.match_length = length;
        if (length < 2) {
            this.instruction = new TABLESWITCH(iArr, instructionHandleArr, instructionHandle);
            return;
        }
        sort(0, this.match_length - 1);
        if (matchIsOrdered(i)) {
            fillup(i, instructionHandle);
            this.instruction = new TABLESWITCH(this.match, this.targets, instructionHandle);
            return;
        }
        this.instruction = new LOOKUPSWITCH(this.match, this.targets, instructionHandle);
    }

    public SWITCH(int[] iArr, InstructionHandle[] instructionHandleArr, InstructionHandle instructionHandle) {
        this(iArr, instructionHandleArr, instructionHandle, 1);
    }

    private final void fillup(int i, InstructionHandle instructionHandle) {
        int i2 = this.match_length;
        int i3 = i2 + (i * i2);
        int[] iArr = new int[i3];
        InstructionHandle[] instructionHandleArr = new InstructionHandle[i3];
        iArr[0] = this.match[0];
        instructionHandleArr[0] = this.targets[0];
        int i4 = 1;
        for (int i5 = 1; i5 < this.match_length; i5++) {
            int[] iArr2 = this.match;
            int i6 = iArr2[i5 - 1];
            int i7 = iArr2[i5] - i6;
            int i8 = i4;
            for (int i9 = 1; i9 < i7; i9++) {
                iArr[i8] = i6 + i9;
                instructionHandleArr[i8] = instructionHandle;
                i8++;
            }
            iArr[i8] = this.match[i5];
            instructionHandleArr[i8] = this.targets[i5];
            i4 = i8 + 1;
        }
        this.match = new int[i4];
        this.targets = new InstructionHandle[i4];
        System.arraycopy(iArr, 0, this.match, 0, i4);
        System.arraycopy(instructionHandleArr, 0, this.targets, 0, i4);
    }

    private final void sort(int i, int i2) {
        int[] iArr;
        int i3 = this.match[(i + i2) / 2];
        int i4 = i;
        int i5 = i2;
        while (true) {
            if (this.match[i4] < i3) {
                i4++;
            } else {
                while (true) {
                    iArr = this.match;
                    if (i3 >= iArr[i5]) {
                        break;
                    }
                    i5--;
                }
                if (i4 <= i5) {
                    int i6 = iArr[i4];
                    iArr[i4] = iArr[i5];
                    iArr[i5] = i6;
                    InstructionHandle[] instructionHandleArr = this.targets;
                    InstructionHandle instructionHandle = instructionHandleArr[i4];
                    instructionHandleArr[i4] = instructionHandleArr[i5];
                    instructionHandleArr[i5] = instructionHandle;
                    i4++;
                    i5--;
                }
                if (i4 > i5) {
                    break;
                }
            }
        }
        if (i < i5) {
            sort(i, i5);
        }
        if (i4 < i2) {
            sort(i4, i2);
        }
    }

    private final boolean matchIsOrdered(int i) {
        for (int i2 = 1; i2 < this.match_length; i2++) {
            int[] iArr = this.match;
            if (iArr[i2] - iArr[i2 - 1] > i) {
                return false;
            }
        }
        return true;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.CompoundInstruction
    public final InstructionList getInstructionList() {
        return new InstructionList((BranchInstruction) this.instruction);
    }

    public final Instruction getInstruction() {
        return this.instruction;
    }
}
