package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.DataOutputStream;
import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.ExceptionConstants;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantPool;
import ohos.com.sun.org.apache.bcel.internal.util.ByteSequence;

public final class INVOKEINTERFACE extends InvokeInstruction {
    private int nargs;

    INVOKEINTERFACE() {
    }

    public INVOKEINTERFACE(int i, int i2) {
        super(Constants.INVOKEINTERFACE, i);
        this.length = 5;
        if (i2 >= 1) {
            this.nargs = i2;
            return;
        }
        throw new ClassGenException("Number of arguments must be > 0 " + i2);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.CPInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.opcode);
        dataOutputStream.writeShort(this.index);
        dataOutputStream.writeByte(this.nargs);
        dataOutputStream.writeByte(0);
    }

    public int getCount() {
        return this.nargs;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.CPInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void initFromFile(ByteSequence byteSequence, boolean z) throws IOException {
        super.initFromFile(byteSequence, z);
        this.length = 5;
        this.nargs = byteSequence.readUnsignedByte();
        byteSequence.readByte();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.InvokeInstruction, ohos.com.sun.org.apache.bcel.internal.generic.CPInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public String toString(ConstantPool constantPool) {
        return super.toString(constantPool) + " " + this.nargs;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.InvokeInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction, ohos.com.sun.org.apache.bcel.internal.generic.StackConsumer
    public int consumeStack(ConstantPoolGen constantPoolGen) {
        return this.nargs;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.ExceptionThrower
    public Class[] getExceptions() {
        Class[] clsArr = new Class[(ExceptionConstants.EXCS_INTERFACE_METHOD_RESOLUTION.length + 4)];
        System.arraycopy(ExceptionConstants.EXCS_INTERFACE_METHOD_RESOLUTION, 0, clsArr, 0, ExceptionConstants.EXCS_INTERFACE_METHOD_RESOLUTION.length);
        clsArr[ExceptionConstants.EXCS_INTERFACE_METHOD_RESOLUTION.length + 3] = ExceptionConstants.INCOMPATIBLE_CLASS_CHANGE_ERROR;
        clsArr[ExceptionConstants.EXCS_INTERFACE_METHOD_RESOLUTION.length + 2] = ExceptionConstants.ILLEGAL_ACCESS_ERROR;
        clsArr[ExceptionConstants.EXCS_INTERFACE_METHOD_RESOLUTION.length + 1] = ExceptionConstants.ABSTRACT_METHOD_ERROR;
        clsArr[ExceptionConstants.EXCS_INTERFACE_METHOD_RESOLUTION.length] = ExceptionConstants.UNSATISFIED_LINK_ERROR;
        return clsArr;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitExceptionThrower(this);
        visitor.visitTypedInstruction(this);
        visitor.visitStackConsumer(this);
        visitor.visitStackProducer(this);
        visitor.visitLoadClass(this);
        visitor.visitCPInstruction(this);
        visitor.visitFieldOrMethod(this);
        visitor.visitInvokeInstruction(this);
        visitor.visitINVOKEINTERFACE(this);
    }
}
