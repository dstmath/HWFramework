package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.DataOutputStream;
import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.util.ByteSequence;

public class RET extends Instruction implements IndexedInstruction, TypedInstruction {
    private int index;
    private boolean wide;

    RET() {
    }

    public RET(int i) {
        super(Constants.RET, 2);
        setIndex(i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void dump(DataOutputStream dataOutputStream) throws IOException {
        if (this.wide) {
            dataOutputStream.writeByte(196);
        }
        dataOutputStream.writeByte(this.opcode);
        if (this.wide) {
            dataOutputStream.writeShort(this.index);
        } else {
            dataOutputStream.writeByte(this.index);
        }
    }

    private final void setWide() {
        boolean z = this.index > 255;
        this.wide = z;
        if (z) {
            this.length = 4;
        } else {
            this.length = 2;
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void initFromFile(ByteSequence byteSequence, boolean z) throws IOException {
        this.wide = z;
        if (z) {
            this.index = byteSequence.readUnsignedShort();
            this.length = 4;
            return;
        }
        this.index = byteSequence.readUnsignedByte();
        this.length = 2;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.IndexedInstruction
    public final int getIndex() {
        return this.index;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.IndexedInstruction
    public final void setIndex(int i) {
        if (i >= 0) {
            this.index = i;
            setWide();
            return;
        }
        throw new ClassGenException("Negative index value: " + i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public String toString(boolean z) {
        return super.toString(z) + " " + this.index;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.TypedInstruction
    public Type getType(ConstantPoolGen constantPoolGen) {
        return ReturnaddressType.NO_TARGET;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitRET(this);
    }
}
