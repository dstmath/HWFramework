package ohos.com.sun.org.apache.bcel.internal.generic;

public abstract class JsrInstruction extends BranchInstruction implements UnconditionalBranch, TypedInstruction, StackProducer {
    JsrInstruction(short s, InstructionHandle instructionHandle) {
        super(s, instructionHandle);
    }

    JsrInstruction() {
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.TypedInstruction
    public Type getType(ConstantPoolGen constantPoolGen) {
        return new ReturnaddressType(physicalSuccessor());
    }

    public InstructionHandle physicalSuccessor() {
        InstructionHandle instructionHandle = this.target;
        while (instructionHandle.getPrev() != null) {
            instructionHandle = instructionHandle.getPrev();
        }
        while (instructionHandle.getInstruction() != this) {
            instructionHandle = instructionHandle.getNext();
        }
        InstructionHandle instructionHandle2 = instructionHandle;
        while (instructionHandle2 != null) {
            instructionHandle2 = instructionHandle2.getNext();
            if (instructionHandle2 != null && instructionHandle2.getInstruction() == this) {
                throw new RuntimeException("physicalSuccessor() called on a shared JsrInstruction.");
            }
        }
        return instructionHandle.getNext();
    }
}
