package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class StackMap extends Attribute implements Node {
    private StackMapEntry[] map;
    private int map_length;

    public StackMap(int i, int i2, StackMapEntry[] stackMapEntryArr, ConstantPool constantPool) {
        super((byte) 11, i, i2, constantPool);
        setStackMap(stackMapEntryArr);
    }

    StackMap(int i, int i2, DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        this(i, i2, (StackMapEntry[]) null, constantPool);
        this.map_length = dataInputStream.readUnsignedShort();
        this.map = new StackMapEntry[this.map_length];
        for (int i3 = 0; i3 < this.map_length; i3++) {
            this.map[i3] = new StackMapEntry(dataInputStream, constantPool);
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public final void dump(DataOutputStream dataOutputStream) throws IOException {
        super.dump(dataOutputStream);
        dataOutputStream.writeShort(this.map_length);
        for (int i = 0; i < this.map_length; i++) {
            this.map[i].dump(dataOutputStream);
        }
    }

    public final StackMapEntry[] getStackMap() {
        return this.map;
    }

    public final void setStackMap(StackMapEntry[] stackMapEntryArr) {
        int i;
        this.map = stackMapEntryArr;
        if (stackMapEntryArr == null) {
            i = 0;
        } else {
            i = stackMapEntryArr.length;
        }
        this.map_length = i;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, java.lang.Object
    public final String toString() {
        StringBuffer stringBuffer = new StringBuffer("StackMap(");
        for (int i = 0; i < this.map_length; i++) {
            stringBuffer.append(this.map[i].toString());
            if (i < this.map_length - 1) {
                stringBuffer.append(", ");
            }
        }
        stringBuffer.append(')');
        return stringBuffer.toString();
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute
    public Attribute copy(ConstantPool constantPool) {
        StackMap stackMap = (StackMap) clone();
        stackMap.map = new StackMapEntry[this.map_length];
        for (int i = 0; i < this.map_length; i++) {
            stackMap.map[i] = this.map[i].copy();
        }
        stackMap.constant_pool = constantPool;
        return stackMap;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Attribute, ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitStackMap(this);
    }

    public final int getMapLength() {
        return this.map_length;
    }
}
