package ohos.com.sun.org.apache.bcel.internal.generic;

public abstract class GotoInstruction extends BranchInstruction implements UnconditionalBranch {
    GotoInstruction(short s, InstructionHandle instructionHandle) {
        super(s, instructionHandle);
    }

    GotoInstruction() {
    }
}
