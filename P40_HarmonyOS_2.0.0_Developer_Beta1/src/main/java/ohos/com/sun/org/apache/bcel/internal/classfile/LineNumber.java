package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

public final class LineNumber implements Cloneable, Node, Serializable {
    private int line_number;
    private int start_pc;

    public LineNumber(LineNumber lineNumber) {
        this(lineNumber.getStartPC(), lineNumber.getLineNumber());
    }

    LineNumber(DataInputStream dataInputStream) throws IOException {
        this(dataInputStream.readUnsignedShort(), dataInputStream.readUnsignedShort());
    }

    public LineNumber(int i, int i2) {
        this.start_pc = i;
        this.line_number = i2;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitLineNumber(this);
    }

    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeShort(this.start_pc);
        dataOutputStream.writeShort(this.line_number);
    }

    public final int getLineNumber() {
        return this.line_number;
    }

    public final int getStartPC() {
        return this.start_pc;
    }

    public final void setLineNumber(int i) {
        this.line_number = i;
    }

    public final void setStartPC(int i) {
        this.start_pc = i;
    }

    @Override // java.lang.Object
    public final String toString() {
        return "LineNumber(" + this.start_pc + ", " + this.line_number + ")";
    }

    public LineNumber copy() {
        try {
            return (LineNumber) clone();
        } catch (CloneNotSupportedException unused) {
            return null;
        }
    }
}
