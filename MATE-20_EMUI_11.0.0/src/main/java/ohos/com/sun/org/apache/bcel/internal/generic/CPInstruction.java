package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.DataOutputStream;
import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.classfile.Constant;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantClass;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantPool;
import ohos.com.sun.org.apache.bcel.internal.util.ByteSequence;

public abstract class CPInstruction extends Instruction implements TypedInstruction, IndexedInstruction {
    protected int index;

    CPInstruction() {
    }

    protected CPInstruction(short s, int i) {
        super(s, 3);
        setIndex(i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.opcode);
        dataOutputStream.writeShort(this.index);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public String toString(boolean z) {
        return super.toString(z) + " " + this.index;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public String toString(ConstantPool constantPool) {
        Constant constant = constantPool.getConstant(this.index);
        String constantToString = constantPool.constantToString(constant);
        if (constant instanceof ConstantClass) {
            constantToString = constantToString.replace('.', '/');
        }
        return Constants.OPCODE_NAMES[this.opcode] + " " + constantToString;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void initFromFile(ByteSequence byteSequence, boolean z) throws IOException {
        setIndex(byteSequence.readUnsignedShort());
        this.length = 3;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.IndexedInstruction
    public final int getIndex() {
        return this.index;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.IndexedInstruction
    public void setIndex(int i) {
        if (i >= 0) {
            this.index = i;
            return;
        }
        throw new ClassGenException("Negative index value: " + i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.TypedInstruction
    public Type getType(ConstantPoolGen constantPoolGen) {
        String constantString = constantPoolGen.getConstantPool().getConstantString(this.index, (byte) 7);
        if (!constantString.startsWith("[")) {
            constantString = "L" + constantString + ";";
        }
        return Type.getType(constantString);
    }
}
