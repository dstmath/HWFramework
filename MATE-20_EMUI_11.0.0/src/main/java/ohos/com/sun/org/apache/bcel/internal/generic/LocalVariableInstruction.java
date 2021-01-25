package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.DataOutputStream;
import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.util.ByteSequence;

public abstract class LocalVariableInstruction extends Instruction implements TypedInstruction, IndexedInstruction {
    private short c_tag = -1;
    private short canon_tag = -1;
    protected int n = -1;

    private final boolean wide() {
        return this.n > 255;
    }

    LocalVariableInstruction(short s, short s2) {
        this.canon_tag = s;
        this.c_tag = s2;
    }

    LocalVariableInstruction() {
    }

    protected LocalVariableInstruction(short s, short s2, int i) {
        super(s, 2);
        this.c_tag = s2;
        this.canon_tag = s;
        setIndex(i);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void dump(DataOutputStream dataOutputStream) throws IOException {
        if (wide()) {
            dataOutputStream.writeByte(196);
        }
        dataOutputStream.writeByte(this.opcode);
        if (this.length <= 1) {
            return;
        }
        if (wide()) {
            dataOutputStream.writeShort(this.n);
        } else {
            dataOutputStream.writeByte(this.n);
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public String toString(boolean z) {
        if ((this.opcode >= 26 && this.opcode <= 45) || (this.opcode >= 59 && this.opcode <= 78)) {
            return super.toString(z);
        }
        return super.toString(z) + " " + this.n;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void initFromFile(ByteSequence byteSequence, boolean z) throws IOException {
        if (z) {
            this.n = byteSequence.readUnsignedShort();
            this.length = 4;
        } else if ((this.opcode >= 21 && this.opcode <= 25) || (this.opcode >= 54 && this.opcode <= 58)) {
            this.n = byteSequence.readUnsignedByte();
            this.length = 2;
        } else if (this.opcode <= 45) {
            this.n = (this.opcode - 26) % 4;
            this.length = 1;
        } else {
            this.n = (this.opcode - 59) % 4;
            this.length = 1;
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.IndexedInstruction
    public final int getIndex() {
        return this.n;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.IndexedInstruction
    public void setIndex(int i) {
        if (i < 0 || i > 65535) {
            throw new ClassGenException("Illegal value: " + i);
        }
        this.n = i;
        if (i < 0 || i > 3) {
            this.opcode = this.canon_tag;
            if (wide()) {
                this.length = 4;
            } else {
                this.length = 2;
            }
        } else {
            this.opcode = (short) (this.c_tag + i);
            this.length = 1;
        }
    }

    public short getCanonicalTag() {
        return this.canon_tag;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x002a  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x002d  */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x0021  */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x0024  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0027  */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.TypedInstruction
    public Type getType(ConstantPoolGen constantPoolGen) {
        short s = this.canon_tag;
        switch (s) {
            case 21:
                return Type.INT;
            case 22:
                return Type.LONG;
            case 23:
                return Type.FLOAT;
            case 24:
                return Type.DOUBLE;
            case 25:
                return Type.OBJECT;
            default:
                switch (s) {
                    case 54:
                        break;
                    case 55:
                        break;
                    case 56:
                        break;
                    case 57:
                        break;
                    case 58:
                        break;
                    default:
                        throw new ClassGenException("Oops: unknown case in switch" + ((int) this.canon_tag));
                }
        }
    }
}
