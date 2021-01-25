package ohos.com.sun.org.apache.bcel.internal.generic;

import java.util.Objects;

public class ReturnaddressType extends Type {
    public static final ReturnaddressType NO_TARGET = new ReturnaddressType();
    private InstructionHandle returnTarget;

    private ReturnaddressType() {
        super((byte) 16, "<return address>");
    }

    public ReturnaddressType(InstructionHandle instructionHandle) {
        super((byte) 16, "<return address targeting " + instructionHandle + ">");
        this.returnTarget = instructionHandle;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hashCode(this.returnTarget);
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (!(obj instanceof ReturnaddressType)) {
            return false;
        }
        return ((ReturnaddressType) obj).returnTarget.equals(this.returnTarget);
    }

    public InstructionHandle getTarget() {
        return this.returnTarget;
    }
}
