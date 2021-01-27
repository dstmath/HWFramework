package ohos.com.sun.org.apache.bcel.internal.generic;

public interface InstructionComparator {
    public static final InstructionComparator DEFAULT = new InstructionComparator() {
        /* class ohos.com.sun.org.apache.bcel.internal.generic.InstructionComparator.AnonymousClass1 */

        @Override // ohos.com.sun.org.apache.bcel.internal.generic.InstructionComparator
        public boolean equals(Instruction instruction, Instruction instruction2) {
            if (instruction.opcode == instruction2.opcode) {
                if (instruction instanceof Select) {
                    InstructionHandle[] targets = ((Select) instruction).getTargets();
                    InstructionHandle[] targets2 = ((Select) instruction2).getTargets();
                    if (targets.length == targets2.length) {
                        for (int i = 0; i < targets.length; i++) {
                            if (targets[i] != targets2[i]) {
                                return false;
                            }
                        }
                        return true;
                    }
                } else if (instruction instanceof BranchInstruction) {
                    if (((BranchInstruction) instruction).target == ((BranchInstruction) instruction2).target) {
                        return true;
                    }
                    return false;
                } else if (instruction instanceof ConstantPushInstruction) {
                    return ((ConstantPushInstruction) instruction).getValue().equals(((ConstantPushInstruction) instruction2).getValue());
                } else {
                    if (instruction instanceof IndexedInstruction) {
                        if (((IndexedInstruction) instruction).getIndex() == ((IndexedInstruction) instruction2).getIndex()) {
                            return true;
                        }
                        return false;
                    } else if (!(instruction instanceof NEWARRAY) || ((NEWARRAY) instruction).getTypecode() == ((NEWARRAY) instruction2).getTypecode()) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            return false;
        }
    };

    boolean equals(Instruction instruction, Instruction instruction2);
}
