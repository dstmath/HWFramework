package ohos.com.sun.org.apache.bcel.internal.generic;

public abstract class StackInstruction extends Instruction {
    StackInstruction() {
    }

    protected StackInstruction(short s) {
        super(s, 1);
    }

    public Type getType(ConstantPoolGen constantPoolGen) {
        return Type.UNKNOWN;
    }
}
