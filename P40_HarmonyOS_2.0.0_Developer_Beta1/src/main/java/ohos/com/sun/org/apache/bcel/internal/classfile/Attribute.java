package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import ohos.com.sun.org.apache.bcel.internal.Constants;

public abstract class Attribute implements Cloneable, Node, Serializable {
    private static HashMap readers = new HashMap();
    protected ConstantPool constant_pool;
    protected int length;
    protected int name_index;
    protected byte tag;

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public abstract void accept(Visitor visitor);

    public abstract Attribute copy(ConstantPool constantPool);

    protected Attribute(byte b, int i, int i2, ConstantPool constantPool) {
        this.tag = b;
        this.name_index = i;
        this.length = i2;
        this.constant_pool = constantPool;
    }

    public void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeShort(this.name_index);
        dataOutputStream.writeInt(this.length);
    }

    public static void addAttributeReader(String str, AttributeReader attributeReader) {
        readers.put(str, attributeReader);
    }

    public static void removeAttributeReader(String str) {
        readers.remove(str);
    }

    public static final Attribute readAttribute(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException, ClassFormatException {
        int readUnsignedShort = dataInputStream.readUnsignedShort();
        String bytes = ((ConstantUtf8) constantPool.getConstant(readUnsignedShort, (byte) 1)).getBytes();
        int readInt = dataInputStream.readInt();
        byte b = 0;
        while (true) {
            if (b >= 13) {
                b = -1;
                break;
            } else if (bytes.equals(Constants.ATTRIBUTE_NAMES[b])) {
                break;
            } else {
                b = (byte) (b + 1);
            }
        }
        switch (b) {
            case -1:
                AttributeReader attributeReader = (AttributeReader) readers.get(bytes);
                if (attributeReader != null) {
                    return attributeReader.createAttribute(readUnsignedShort, readInt, dataInputStream, constantPool);
                }
                return new Unknown(readUnsignedShort, readInt, dataInputStream, constantPool);
            case 0:
                return new SourceFile(readUnsignedShort, readInt, dataInputStream, constantPool);
            case 1:
                return new ConstantValue(readUnsignedShort, readInt, dataInputStream, constantPool);
            case 2:
                return new Code(readUnsignedShort, readInt, dataInputStream, constantPool);
            case 3:
                return new ExceptionTable(readUnsignedShort, readInt, dataInputStream, constantPool);
            case 4:
                return new LineNumberTable(readUnsignedShort, readInt, dataInputStream, constantPool);
            case 5:
                return new LocalVariableTable(readUnsignedShort, readInt, dataInputStream, constantPool);
            case 6:
                return new InnerClasses(readUnsignedShort, readInt, dataInputStream, constantPool);
            case 7:
                return new Synthetic(readUnsignedShort, readInt, dataInputStream, constantPool);
            case 8:
                return new Deprecated(readUnsignedShort, readInt, dataInputStream, constantPool);
            case 9:
                return new PMGClass(readUnsignedShort, readInt, dataInputStream, constantPool);
            case 10:
                return new Signature(readUnsignedShort, readInt, dataInputStream, constantPool);
            case 11:
                return new StackMap(readUnsignedShort, readInt, dataInputStream, constantPool);
            case 12:
                return new LocalVariableTypeTable(readUnsignedShort, readInt, dataInputStream, constantPool);
            default:
                throw new IllegalStateException("Ooops! default case reached.");
        }
    }

    public final int getLength() {
        return this.length;
    }

    public final void setLength(int i) {
        this.length = i;
    }

    public final void setNameIndex(int i) {
        this.name_index = i;
    }

    public final int getNameIndex() {
        return this.name_index;
    }

    public final byte getTag() {
        return this.tag;
    }

    public final ConstantPool getConstantPool() {
        return this.constant_pool;
    }

    public final void setConstantPool(ConstantPool constantPool) {
        this.constant_pool = constantPool;
    }

    @Override // java.lang.Object
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override // java.lang.Object
    public String toString() {
        return Constants.ATTRIBUTE_NAMES[this.tag];
    }
}
