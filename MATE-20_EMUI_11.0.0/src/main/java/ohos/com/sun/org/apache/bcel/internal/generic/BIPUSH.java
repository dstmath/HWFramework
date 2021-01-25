package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.DataOutputStream;
import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.util.ByteSequence;

public class BIPUSH extends Instruction implements ConstantPushInstruction {
    private byte b;

    BIPUSH() {
    }

    public BIPUSH(byte b2) {
        super(16, 2);
        this.b = b2;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void dump(DataOutputStream dataOutputStream) throws IOException {
        super.dump(dataOutputStream);
        dataOutputStream.writeByte(this.b);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public String toString(boolean z) {
        return super.toString(z) + " " + ((int) this.b);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void initFromFile(ByteSequence byteSequence, boolean z) throws IOException {
        this.length = 2;
        this.b = byteSequence.readByte();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.ConstantPushInstruction
    public Number getValue() {
        return new Integer(this.b);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.TypedInstruction
    public Type getType(ConstantPoolGen constantPoolGen) {
        return Type.BYTE;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitPushInstruction(this);
        visitor.visitStackProducer(this);
        visitor.visitTypedInstruction(this);
        visitor.visitConstantPushInstruction(this);
        visitor.visitBIPUSH(this);
    }
}
