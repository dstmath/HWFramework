package ohos.com.sun.org.apache.bcel.internal.generic;

public interface InstructionTargeter {
    boolean containsTarget(InstructionHandle instructionHandle);

    void updateTarget(InstructionHandle instructionHandle, InstructionHandle instructionHandle2);
}
