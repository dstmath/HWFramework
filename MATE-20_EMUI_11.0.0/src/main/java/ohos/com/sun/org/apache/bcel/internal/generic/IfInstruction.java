package ohos.com.sun.org.apache.bcel.internal.generic;

public abstract class IfInstruction extends BranchInstruction implements StackConsumer {
    public abstract IfInstruction negate();

    IfInstruction() {
    }

    protected IfInstruction(short s, InstructionHandle instructionHandle) {
        super(s, instructionHandle);
    }
}
