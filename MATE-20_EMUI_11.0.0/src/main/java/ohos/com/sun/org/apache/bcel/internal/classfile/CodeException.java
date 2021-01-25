package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import ohos.com.sun.org.apache.bcel.internal.Constants;

public final class CodeException implements Cloneable, Constants, Node, Serializable {
    private int catch_type;
    private int end_pc;
    private int handler_pc;
    private int start_pc;

    public CodeException(CodeException codeException) {
        this(codeException.getStartPC(), codeException.getEndPC(), codeException.getHandlerPC(), codeException.getCatchType());
    }

    CodeException(DataInputStream dataInputStream) throws IOException {
        this(dataInputStream.readUnsignedShort(), dataInputStream.readUnsignedShort(), dataInputStream.readUnsignedShort(), dataInputStream.readUnsignedShort());
    }

    public CodeException(int i, int i2, int i3, int i4) {
        this.start_pc = i;
        this.end_pc = i2;
        this.handler_pc = i3;
        this.catch_type = i4;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitCodeException(this);
    }

    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeShort(this.start_pc);
        dataOutputStream.writeShort(this.end_pc);
        dataOutputStream.writeShort(this.handler_pc);
        dataOutputStream.writeShort(this.catch_type);
    }

    public final int getCatchType() {
        return this.catch_type;
    }

    public final int getEndPC() {
        return this.end_pc;
    }

    public final int getHandlerPC() {
        return this.handler_pc;
    }

    public final int getStartPC() {
        return this.start_pc;
    }

    public final void setCatchType(int i) {
        this.catch_type = i;
    }

    public final void setEndPC(int i) {
        this.end_pc = i;
    }

    public final void setHandlerPC(int i) {
        this.handler_pc = i;
    }

    public final void setStartPC(int i) {
        this.start_pc = i;
    }

    @Override // java.lang.Object
    public final String toString() {
        return "CodeException(start_pc = " + this.start_pc + ", end_pc = " + this.end_pc + ", handler_pc = " + this.handler_pc + ", catch_type = " + this.catch_type + ")";
    }

    public final String toString(ConstantPool constantPool, boolean z) {
        String str;
        String str2;
        if (this.catch_type == 0) {
            str = "<Any exception>(0)";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(Utility.compactClassName(constantPool.getConstantString(this.catch_type, (byte) 7), false));
            if (z) {
                str2 = "(" + this.catch_type + ")";
            } else {
                str2 = "";
            }
            sb.append(str2);
            str = sb.toString();
        }
        return this.start_pc + "\t" + this.end_pc + "\t" + this.handler_pc + "\t" + str;
    }

    public final String toString(ConstantPool constantPool) {
        return toString(constantPool, true);
    }

    public CodeException copy() {
        try {
            return (CodeException) clone();
        } catch (CloneNotSupportedException unused) {
            return null;
        }
    }
}
