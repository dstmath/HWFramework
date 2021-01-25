package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.DataOutputStream;
import java.io.IOException;
import ohos.com.sun.org.apache.bcel.internal.ExceptionConstants;
import ohos.com.sun.org.apache.bcel.internal.classfile.Constant;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantFloat;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantInteger;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantString;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantUtf8;
import ohos.com.sun.org.apache.bcel.internal.util.ByteSequence;

public class LDC extends CPInstruction implements PushInstruction, ExceptionThrower, TypedInstruction {
    LDC() {
    }

    public LDC(int i) {
        super(19, i);
        setSize();
    }

    /* access modifiers changed from: protected */
    public final void setSize() {
        if (this.index <= 255) {
            this.opcode = 18;
            this.length = 2;
            return;
        }
        this.opcode = 19;
        this.length = 3;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.CPInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(this.opcode);
        if (this.length == 2) {
            dataOutputStream.writeByte(this.index);
        } else {
            dataOutputStream.writeShort(this.index);
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.CPInstruction, ohos.com.sun.org.apache.bcel.internal.generic.IndexedInstruction
    public final void setIndex(int i) {
        super.setIndex(i);
        setSize();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.bcel.internal.generic.CPInstruction, ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void initFromFile(ByteSequence byteSequence, boolean z) throws IOException {
        this.length = 2;
        this.index = byteSequence.readUnsignedByte();
    }

    public Object getValue(ConstantPoolGen constantPoolGen) {
        Constant constant = constantPoolGen.getConstantPool().getConstant(this.index);
        byte tag = constant.getTag();
        if (tag == 3) {
            return new Integer(((ConstantInteger) constant).getBytes());
        }
        if (tag == 4) {
            return new Float(((ConstantFloat) constant).getBytes());
        }
        if (tag == 8) {
            return ((ConstantUtf8) constantPoolGen.getConstantPool().getConstant(((ConstantString) constant).getStringIndex())).getBytes();
        }
        throw new RuntimeException("Unknown or invalid constant type at " + this.index);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.CPInstruction, ohos.com.sun.org.apache.bcel.internal.generic.TypedInstruction
    public Type getType(ConstantPoolGen constantPoolGen) {
        byte tag = constantPoolGen.getConstantPool().getConstant(this.index).getTag();
        if (tag == 3) {
            return Type.INT;
        }
        if (tag == 4) {
            return Type.FLOAT;
        }
        if (tag == 8) {
            return Type.STRING;
        }
        throw new RuntimeException("Unknown or invalid constant type at " + this.index);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.ExceptionThrower
    public Class[] getExceptions() {
        return ExceptionConstants.EXCS_STRING_RESOLUTION;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.Instruction
    public void accept(Visitor visitor) {
        visitor.visitStackProducer(this);
        visitor.visitPushInstruction(this);
        visitor.visitExceptionThrower(this);
        visitor.visitTypedInstruction(this);
        visitor.visitCPInstruction(this);
        visitor.visitLDC(this);
    }
}
