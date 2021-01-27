package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.Serializable;
import ohos.com.sun.org.apache.bcel.internal.classfile.CodeException;

public final class CodeExceptionGen implements InstructionTargeter, Cloneable, Serializable {
    private ObjectType catch_type;
    private InstructionHandle end_pc;
    private InstructionHandle handler_pc;
    private InstructionHandle start_pc;

    public CodeExceptionGen(InstructionHandle instructionHandle, InstructionHandle instructionHandle2, InstructionHandle instructionHandle3, ObjectType objectType) {
        setStartPC(instructionHandle);
        setEndPC(instructionHandle2);
        setHandlerPC(instructionHandle3);
        this.catch_type = objectType;
    }

    public CodeException getCodeException(ConstantPoolGen constantPoolGen) {
        int position = this.start_pc.getPosition();
        int position2 = this.end_pc.getPosition() + this.end_pc.getInstruction().getLength();
        int position3 = this.handler_pc.getPosition();
        ObjectType objectType = this.catch_type;
        return new CodeException(position, position2, position3, objectType == null ? 0 : constantPoolGen.addClass(objectType));
    }

    public final void setStartPC(InstructionHandle instructionHandle) {
        BranchInstruction.notifyTargetChanging(this.start_pc, this);
        this.start_pc = instructionHandle;
        BranchInstruction.notifyTargetChanged(this.start_pc, this);
    }

    public final void setEndPC(InstructionHandle instructionHandle) {
        BranchInstruction.notifyTargetChanging(this.end_pc, this);
        this.end_pc = instructionHandle;
        BranchInstruction.notifyTargetChanged(this.end_pc, this);
    }

    public final void setHandlerPC(InstructionHandle instructionHandle) {
        BranchInstruction.notifyTargetChanging(this.handler_pc, this);
        this.handler_pc = instructionHandle;
        BranchInstruction.notifyTargetChanged(this.handler_pc, this);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.InstructionTargeter
    public void updateTarget(InstructionHandle instructionHandle, InstructionHandle instructionHandle2) {
        boolean z;
        if (this.start_pc == instructionHandle) {
            setStartPC(instructionHandle2);
            z = true;
        } else {
            z = false;
        }
        if (this.end_pc == instructionHandle) {
            setEndPC(instructionHandle2);
            z = true;
        }
        if (this.handler_pc == instructionHandle) {
            setHandlerPC(instructionHandle2);
            z = true;
        }
        if (!z) {
            throw new ClassGenException("Not targeting " + instructionHandle + ", but {" + this.start_pc + ", " + this.end_pc + ", " + this.handler_pc + "}");
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.InstructionTargeter
    public boolean containsTarget(InstructionHandle instructionHandle) {
        return this.start_pc == instructionHandle || this.end_pc == instructionHandle || this.handler_pc == instructionHandle;
    }

    public void setCatchType(ObjectType objectType) {
        this.catch_type = objectType;
    }

    public ObjectType getCatchType() {
        return this.catch_type;
    }

    public InstructionHandle getStartPC() {
        return this.start_pc;
    }

    public InstructionHandle getEndPC() {
        return this.end_pc;
    }

    public InstructionHandle getHandlerPC() {
        return this.handler_pc;
    }

    @Override // java.lang.Object
    public String toString() {
        return "CodeExceptionGen(" + this.start_pc + ", " + this.end_pc + ", " + this.handler_pc + ")";
    }

    @Override // java.lang.Object
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            System.err.println(e);
            return null;
        }
    }
}
