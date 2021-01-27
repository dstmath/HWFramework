package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.DataOutputStream;
import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.util.ByteSequence;

public class IINC extends LocalVariableInstruction {
    private int c;
    private boolean wide;

    IINC() {
    }

    public IINC(int i, int i2) {
        this.opcode = Constants.IINC;
        this.length = 3;
        setIndex(i);
        setIncrement(i2);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void dump(DataOutputStream dataOutputStream) throws IOException {
        if (this.wide) {
            dataOutputStream.writeByte(196);
        }
        dataOutputStream.writeByte(this.opcode);
        if (this.wide) {
            dataOutputStream.writeShort(this.n);
            dataOutputStream.writeShort(this.c);
            return;
        }
        dataOutputStream.writeByte(this.n);
        dataOutputStream.writeByte(this.c);
    }

    private final void setWide() {
        boolean z = this.n > 65535 || Math.abs(this.c) > 127;
        this.wide = z;
        if (z) {
            this.length = 6;
        } else {
            this.length = 3;
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void initFromFile(ByteSequence byteSequence, boolean z) throws IOException {
        this.wide = z;
        if (z) {
            this.length = 6;
            this.n = byteSequence.readUnsignedShort();
            this.c = byteSequence.readShort();
            return;
        }
        this.length = 3;
        this.n = byteSequence.readUnsignedByte();
        this.c = byteSequence.readByte();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public String toString(boolean z) {
        return super.toString(z) + " " + this.c;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableInstruction, ohos.com.sun.org.apache.bcel.internal.generic.IndexedInstruction
    public final void setIndex(int i) {
        if (i >= 0) {
            this.n = i;
            setWide();
            return;
        }
        throw new ClassGenException("Negative index value: " + i);
    }

    public final int getIncrement() {
        return this.c;
    }

    public final void setIncrement(int i) {
        this.c = i;
        setWide();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableInstruction, ohos.com.sun.org.apache.bcel.internal.generic.TypedInstruction
    public Type getType(ConstantPoolGen constantPoolGen) {
        return Type.INT;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitLocalVariableInstruction(this);
        visitor.visitIINC(this);
    }
}
