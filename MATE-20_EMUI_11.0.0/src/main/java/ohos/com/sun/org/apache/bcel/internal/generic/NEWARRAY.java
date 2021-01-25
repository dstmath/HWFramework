package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.DataOutputStream;
import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.ExceptionConstants;
import ohos.com.sun.org.apache.bcel.internal.util.ByteSequence;

public class NEWARRAY extends Instruction implements AllocationInstruction, ExceptionThrower, StackProducer {
    private byte type;

    NEWARRAY() {
    }

    public NEWARRAY(byte b) {
        super(Constants.NEWARRAY, 2);
        this.type = b;
    }

    public NEWARRAY(BasicType basicType) {
        this(basicType.getType());
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.opcode);
        dataOutputStream.writeByte(this.type);
    }

    public final byte getTypecode() {
        return this.type;
    }

    public final Type getType() {
        return new ArrayType(BasicType.getType(this.type), 1);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public String toString(boolean z) {
        return super.toString(z) + " " + Constants.TYPE_NAMES[this.type];
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void initFromFile(ByteSequence byteSequence, boolean z) throws IOException {
        this.type = byteSequence.readByte();
        this.length = 2;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.ExceptionThrower
    public Class[] getExceptions() {
        return new Class[]{ExceptionConstants.NEGATIVE_ARRAY_SIZE_EXCEPTION};
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitAllocationInstruction(this);
        visitor.visitExceptionThrower(this);
        visitor.visitStackProducer(this);
        visitor.visitNEWARRAY(this);
    }
}
