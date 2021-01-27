package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.Serializable;
import ohos.com.sun.org.apache.bcel.internal.classfile.LineNumber;

public class LineNumberGen implements InstructionTargeter, Cloneable, Serializable {
    private InstructionHandle ih;
    private int src_line;

    public LineNumberGen(InstructionHandle instructionHandle, int i) {
        setInstruction(instructionHandle);
        setSourceLine(i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.InstructionTargeter
    public boolean containsTarget(InstructionHandle instructionHandle) {
        return this.ih == instructionHandle;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.InstructionTargeter
    public void updateTarget(InstructionHandle instructionHandle, InstructionHandle instructionHandle2) {
        if (instructionHandle == this.ih) {
            setInstruction(instructionHandle2);
            return;
        }
        throw new ClassGenException("Not targeting " + instructionHandle + ", but " + this.ih + "}");
    }

    public LineNumber getLineNumber() {
        return new LineNumber(this.ih.getPosition(), this.src_line);
    }

    public final void setInstruction(InstructionHandle instructionHandle) {
        BranchInstruction.notifyTargetChanging(this.ih, this);
        this.ih = instructionHandle;
        BranchInstruction.notifyTargetChanged(this.ih, this);
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

    public InstructionHandle getInstruction() {
        return this.ih;
    }

    public void setSourceLine(int i) {
        this.src_line = i;
    }

    public int getSourceLine() {
        return this.src_line;
    }
}
