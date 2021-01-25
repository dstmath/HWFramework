package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.DataOutputStream;
import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.ExceptionConstants;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantPool;
import ohos.com.sun.org.apache.bcel.internal.util.ByteSequence;

public class MULTIANEWARRAY extends CPInstruction implements LoadClass, AllocationInstruction, ExceptionThrower {
    private short dimensions;

    MULTIANEWARRAY() {
    }

    public MULTIANEWARRAY(int i, short s) {
        super(Constants.MULTIANEWARRAY, i);
        if (s >= 1) {
            this.dimensions = s;
            this.length = 4;
            return;
        }
        throw new ClassGenException("Invalid dimensions value: " + ((int) s));
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.CPInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.opcode);
        dataOutputStream.writeShort(this.index);
        dataOutputStream.writeByte(this.dimensions);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.CPInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void initFromFile(ByteSequence byteSequence, boolean z) throws IOException {
        super.initFromFile(byteSequence, z);
        this.dimensions = (short) byteSequence.readByte();
        this.length = 4;
    }

    public final short getDimensions() {
        return this.dimensions;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.CPInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public String toString(boolean z) {
        return super.toString(z) + " " + this.index + " " + ((int) this.dimensions);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.CPInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public String toString(ConstantPool constantPool) {
        return super.toString(constantPool) + " " + ((int) this.dimensions);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction, ohos.com.sun.org.apache.bcel.internal.generic.StackConsumer
    public int consumeStack(ConstantPoolGen constantPoolGen) {
        return this.dimensions;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.ExceptionThrower
    public Class[] getExceptions() {
        Class[] clsArr = new Class[(ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length + 2)];
        System.arraycopy(ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION, 0, clsArr, 0, ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length);
        clsArr[ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length + 1] = ExceptionConstants.NEGATIVE_ARRAY_SIZE_EXCEPTION;
        clsArr[ExceptionConstants.EXCS_CLASS_AND_INTERFACE_RESOLUTION.length] = ExceptionConstants.ILLEGAL_ACCESS_ERROR;
        return clsArr;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.LoadClass
    public ObjectType getLoadClassType(ConstantPoolGen constantPoolGen) {
        Type type = getType(constantPoolGen);
        if (type instanceof ArrayType) {
            type = ((ArrayType) type).getBasicType();
        }
        if (type instanceof ObjectType) {
            return (ObjectType) type;
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitLoadClass(this);
        visitor.visitAllocationInstruction(this);
        visitor.visitExceptionThrower(this);
        visitor.visitTypedInstruction(this);
        visitor.visitCPInstruction(this);
        visitor.visitMULTIANEWARRAY(this);
    }
}
