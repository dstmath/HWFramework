package ohos.com.sun.org.apache.bcel.internal.generic;

public final class TargetLostException extends Exception {
    private InstructionHandle[] targets;

    TargetLostException(InstructionHandle[] instructionHandleArr, String str) {
        super(str);
        this.targets = instructionHandleArr;
    }

    public InstructionHandle[] getTargets() {
        return this.targets;
    }
}
