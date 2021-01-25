package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.Serializable;
import java.util.Objects;
import ohos.com.sun.org.apache.bcel.internal.classfile.LocalVariable;
import ohos.devtools.JLogConstants;

public class LocalVariableGen implements InstructionTargeter, NamedAndTyped, Cloneable, Serializable {
    private InstructionHandle end;
    private final int index;
    private String name;
    private InstructionHandle start;
    private Type type;

    public LocalVariableGen(int i, String str, Type type2, InstructionHandle instructionHandle, InstructionHandle instructionHandle2) {
        if (i < 0 || i > 65535) {
            throw new ClassGenException("Invalid index index: " + i);
        }
        this.name = str;
        this.type = type2;
        this.index = i;
        setStart(instructionHandle);
        setEnd(instructionHandle2);
    }

    public LocalVariable getLocalVariable(ConstantPoolGen constantPoolGen) {
        int position = this.start.getPosition();
        int position2 = this.end.getPosition() - position;
        if (position2 > 0) {
            position2 += this.end.getInstruction().getLength();
        }
        return new LocalVariable(position, position2, constantPoolGen.addUtf8(this.name), constantPoolGen.addUtf8(this.type.getSignature()), this.index, constantPoolGen.getConstantPool());
    }

    public int getIndex() {
        return this.index;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.NamedAndTyped
    public void setName(String str) {
        this.name = str;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.NamedAndTyped
    public String getName() {
        return this.name;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.NamedAndTyped
    public void setType(Type type2) {
        this.type = type2;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.NamedAndTyped
    public Type getType() {
        return this.type;
    }

    public InstructionHandle getStart() {
        return this.start;
    }

    public InstructionHandle getEnd() {
        return this.end;
    }

    /* access modifiers changed from: package-private */
    public void notifyTargetChanging() {
        BranchInstruction.notifyTargetChanging(this.start, this);
        InstructionHandle instructionHandle = this.end;
        if (instructionHandle != this.start) {
            BranchInstruction.notifyTargetChanging(instructionHandle, this);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyTargetChanged() {
        BranchInstruction.notifyTargetChanged(this.start, this);
        InstructionHandle instructionHandle = this.end;
        if (instructionHandle != this.start) {
            BranchInstruction.notifyTargetChanged(instructionHandle, this);
        }
    }

    public final void setStart(InstructionHandle instructionHandle) {
        notifyTargetChanging();
        this.start = instructionHandle;
        notifyTargetChanged();
    }

    public final void setEnd(InstructionHandle instructionHandle) {
        notifyTargetChanging();
        this.end = instructionHandle;
        notifyTargetChanged();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.InstructionTargeter
    public void updateTarget(InstructionHandle instructionHandle, InstructionHandle instructionHandle2) {
        boolean z;
        if (this.start == instructionHandle) {
            setStart(instructionHandle2);
            z = true;
        } else {
            z = false;
        }
        if (this.end == instructionHandle) {
            setEnd(instructionHandle2);
            z = true;
        }
        if (!z) {
            throw new ClassGenException("Not targeting " + instructionHandle + ", but {" + this.start + ", " + this.end + "}");
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.InstructionTargeter
    public boolean containsTarget(InstructionHandle instructionHandle) {
        return this.start == instructionHandle || this.end == instructionHandle;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LocalVariableGen)) {
            return false;
        }
        LocalVariableGen localVariableGen = (LocalVariableGen) obj;
        return localVariableGen.index == this.index && localVariableGen.start == this.start && localVariableGen.end == this.end;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return ((((JLogConstants.JLID_INPUTMETHOD_PRESS_KEY_TIMEOUT + this.index) * 59) + Objects.hashCode(this.start)) * 59) + Objects.hashCode(this.end);
    }

    @Override // java.lang.Object
    public String toString() {
        return "LocalVariableGen(" + this.name + ", " + this.type + ", " + this.start + ", " + this.end + ")";
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
