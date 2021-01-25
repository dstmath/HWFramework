package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;

/* access modifiers changed from: package-private */
public class OutlineableChunkStart extends MarkerInstruction {
    public static final Instruction OUTLINEABLECHUNKSTART = new OutlineableChunkStart();

    private OutlineableChunkStart() {
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public String getName() {
        return OutlineableChunkStart.class.getName();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction, java.lang.Object
    public String toString() {
        return getName();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public String toString(boolean z) {
        return getName();
    }
}
