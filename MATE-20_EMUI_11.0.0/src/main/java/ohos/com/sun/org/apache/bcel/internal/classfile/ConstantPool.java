package ohos.com.sun.org.apache.bcel.internal.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import ohos.com.sun.org.apache.bcel.internal.Constants;

public class ConstantPool implements Cloneable, Node, Serializable {
    private Constant[] constant_pool;
    private int constant_pool_count;

    public ConstantPool(Constant[] constantArr) {
        setConstantPool(constantArr);
    }

    ConstantPool(DataInputStream dataInputStream) throws IOException, ClassFormatException {
        this.constant_pool_count = dataInputStream.readUnsignedShort();
        this.constant_pool = new Constant[this.constant_pool_count];
        int i = 1;
        while (i < this.constant_pool_count) {
            this.constant_pool[i] = Constant.readConstant(dataInputStream);
            byte tag = this.constant_pool[i].getTag();
            if (tag == 6 || tag == 5) {
                i++;
            }
            i++;
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.classfile.Node
    public void accept(Visitor visitor) {
        visitor.visitConstantPool(this);
    }

    public String constantToString(Constant constant) throws ClassFormatException {
        byte tag = constant.getTag();
        switch (tag) {
            case 1:
                return ((ConstantUtf8) constant).getBytes();
            case 2:
            default:
                throw new RuntimeException("Unknown constant type " + ((int) tag));
            case 3:
                return "" + ((ConstantInteger) constant).getBytes();
            case 4:
                return "" + ((ConstantFloat) constant).getBytes();
            case 5:
                return "" + ((ConstantLong) constant).getBytes();
            case 6:
                return "" + ((ConstantDouble) constant).getBytes();
            case 7:
                return Utility.compactClassName(((ConstantUtf8) getConstant(((ConstantClass) constant).getNameIndex(), (byte) 1)).getBytes(), false);
            case 8:
                Constant constant2 = getConstant(((ConstantString) constant).getStringIndex(), (byte) 1);
                return "\"" + escape(((ConstantUtf8) constant2).getBytes()) + "\"";
            case 9:
            case 10:
            case 11:
                StringBuilder sb = new StringBuilder();
                ConstantCP constantCP = (ConstantCP) constant;
                sb.append(constantToString(constantCP.getClassIndex(), (byte) 7));
                sb.append(".");
                sb.append(constantToString(constantCP.getNameAndTypeIndex(), (byte) 12));
                return sb.toString();
            case 12:
                StringBuilder sb2 = new StringBuilder();
                ConstantNameAndType constantNameAndType = (ConstantNameAndType) constant;
                sb2.append(constantToString(constantNameAndType.getNameIndex(), (byte) 1));
                sb2.append(" ");
                sb2.append(constantToString(constantNameAndType.getSignatureIndex(), (byte) 1));
                return sb2.toString();
        }
    }

    private static final String escape(String str) {
        int length = str.length();
        StringBuffer stringBuffer = new StringBuffer(length + 5);
        char[] charArray = str.toCharArray();
        for (int i = 0; i < length; i++) {
            char c = charArray[i];
            if (c == '\r') {
                stringBuffer.append("\\r");
            } else if (c != '\"') {
                switch (c) {
                    case '\b':
                        stringBuffer.append("\\b");
                        continue;
                    case '\t':
                        stringBuffer.append("\\t");
                        continue;
                    case '\n':
                        stringBuffer.append("\\n");
                        continue;
                    default:
                        stringBuffer.append(charArray[i]);
                        continue;
                }
            } else {
                stringBuffer.append("\\\"");
            }
        }
        return stringBuffer.toString();
    }

    public String constantToString(int i, byte b) throws ClassFormatException {
        return constantToString(getConstant(i, b));
    }

    public void dump(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeShort(this.constant_pool_count);
        for (int i = 1; i < this.constant_pool_count; i++) {
            Constant[] constantArr = this.constant_pool;
            if (constantArr[i] != null) {
                constantArr[i].dump(dataOutputStream);
            }
        }
    }

    public Constant getConstant(int i) {
        Constant[] constantArr = this.constant_pool;
        if (i < constantArr.length && i >= 0) {
            return constantArr[i];
        }
        throw new ClassFormatException("Invalid constant pool reference: " + i + ". Constant pool size is: " + this.constant_pool.length);
    }

    public Constant getConstant(int i, byte b) throws ClassFormatException {
        Constant constant = getConstant(i);
        if (constant == null) {
            throw new ClassFormatException("Constant pool at index " + i + " is null.");
        } else if (constant.getTag() == b) {
            return constant;
        } else {
            throw new ClassFormatException("Expected class `" + Constants.CONSTANT_NAMES[b] + "' at index " + i + " and got " + constant);
        }
    }

    public Constant[] getConstantPool() {
        return this.constant_pool;
    }

    public String getConstantString(int i, byte b) throws ClassFormatException {
        int i2;
        Constant constant = getConstant(i, b);
        if (b == 7) {
            i2 = ((ConstantClass) constant).getNameIndex();
        } else if (b == 8) {
            i2 = ((ConstantString) constant).getStringIndex();
        } else {
            throw new RuntimeException("getConstantString called with illegal tag " + ((int) b));
        }
        return ((ConstantUtf8) getConstant(i2, (byte) 1)).getBytes();
    }

    public int getLength() {
        return this.constant_pool_count;
    }

    public void setConstant(int i, Constant constant) {
        this.constant_pool[i] = constant;
    }

    public void setConstantPool(Constant[] constantArr) {
        int i;
        this.constant_pool = constantArr;
        if (constantArr == null) {
            i = 0;
        } else {
            i = constantArr.length;
        }
        this.constant_pool_count = i;
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 1; i < this.constant_pool_count; i++) {
            stringBuffer.append(i + ")" + this.constant_pool[i] + "\n");
        }
        return stringBuffer.toString();
    }

    public ConstantPool copy() {
        ConstantPool constantPool;
        try {
            constantPool = (ConstantPool) clone();
        } catch (CloneNotSupportedException unused) {
            constantPool = null;
        }
        constantPool.constant_pool = new Constant[this.constant_pool_count];
        for (int i = 1; i < this.constant_pool_count; i++) {
            Constant[] constantArr = this.constant_pool;
            if (constantArr[i] != null) {
                constantPool.constant_pool[i] = constantArr[i].copy();
            }
        }
        return constantPool;
    }
}
