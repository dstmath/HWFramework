package ohos.com.sun.org.apache.bcel.internal.generic;

import java.io.Serializable;
import java.util.HashMap;
import ohos.com.sun.org.apache.bcel.internal.classfile.Constant;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantCP;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantClass;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantDouble;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantFieldref;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantFloat;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantInteger;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantInterfaceMethodref;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantLong;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantMethodref;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantNameAndType;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantPool;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantString;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantUtf8;

public class ConstantPoolGen implements Serializable {
    private static final String FIELDREF_DELIM = "&";
    private static final String IMETHODREF_DELIM = "#";
    private static final String METHODREF_DELIM = ":";
    private static final String NAT_DELIM = "%";
    private HashMap class_table;
    protected Constant[] constants;
    private HashMap cp_table;
    protected int index;
    private HashMap n_a_t_table;
    protected int size;
    private HashMap string_table;
    private HashMap utf8_table;

    /* access modifiers changed from: private */
    public static class Index implements Serializable {
        int index;

        Index(int i) {
            this.index = i;
        }
    }

    public ConstantPoolGen(Constant[] constantArr) {
        String str;
        this.size = 1024;
        this.constants = new Constant[this.size];
        this.index = 1;
        this.string_table = new HashMap();
        this.class_table = new HashMap();
        this.utf8_table = new HashMap();
        this.n_a_t_table = new HashMap();
        this.cp_table = new HashMap();
        if (constantArr.length > this.size) {
            this.size = constantArr.length;
            this.constants = new Constant[this.size];
        }
        System.arraycopy(constantArr, 0, this.constants, 0, constantArr.length);
        if (constantArr.length > 0) {
            this.index = constantArr.length;
        }
        for (int i = 1; i < this.index; i++) {
            Constant[] constantArr2 = this.constants;
            Constant constant = constantArr2[i];
            if (constant instanceof ConstantString) {
                this.string_table.put(((ConstantUtf8) constantArr2[((ConstantString) constant).getStringIndex()]).getBytes(), new Index(i));
            } else if (constant instanceof ConstantClass) {
                this.class_table.put(((ConstantUtf8) constantArr2[((ConstantClass) constant).getNameIndex()]).getBytes(), new Index(i));
            } else if (constant instanceof ConstantNameAndType) {
                ConstantNameAndType constantNameAndType = (ConstantNameAndType) constant;
                HashMap hashMap = this.n_a_t_table;
                hashMap.put(((ConstantUtf8) constantArr2[constantNameAndType.getNameIndex()]).getBytes() + NAT_DELIM + ((ConstantUtf8) this.constants[constantNameAndType.getSignatureIndex()]).getBytes(), new Index(i));
            } else if (constant instanceof ConstantUtf8) {
                this.utf8_table.put(((ConstantUtf8) constant).getBytes(), new Index(i));
            } else if (constant instanceof ConstantCP) {
                ConstantCP constantCP = (ConstantCP) constant;
                ConstantNameAndType constantNameAndType2 = (ConstantNameAndType) this.constants[constantCP.getNameAndTypeIndex()];
                String replace = ((ConstantUtf8) this.constants[((ConstantClass) constantArr2[constantCP.getClassIndex()]).getNameIndex()]).getBytes().replace('/', '.');
                String bytes = ((ConstantUtf8) this.constants[constantNameAndType2.getNameIndex()]).getBytes();
                String bytes2 = ((ConstantUtf8) this.constants[constantNameAndType2.getSignatureIndex()]).getBytes();
                if (constant instanceof ConstantInterfaceMethodref) {
                    str = IMETHODREF_DELIM;
                } else {
                    str = constant instanceof ConstantFieldref ? FIELDREF_DELIM : METHODREF_DELIM;
                }
                HashMap hashMap2 = this.cp_table;
                hashMap2.put(replace + str + bytes + str + bytes2, new Index(i));
            }
        }
    }

    public ConstantPoolGen(ConstantPool constantPool) {
        this(constantPool.getConstantPool());
    }

    public ConstantPoolGen() {
        this.size = 1024;
        this.constants = new Constant[this.size];
        this.index = 1;
        this.string_table = new HashMap();
        this.class_table = new HashMap();
        this.utf8_table = new HashMap();
        this.n_a_t_table = new HashMap();
        this.cp_table = new HashMap();
    }

    /* access modifiers changed from: protected */
    public void adjustSize() {
        int i = this.index;
        int i2 = i + 3;
        int i3 = this.size;
        if (i2 >= i3) {
            Constant[] constantArr = this.constants;
            this.size = i3 * 2;
            this.constants = new Constant[this.size];
            System.arraycopy(constantArr, 0, this.constants, 0, i);
        }
    }

    public int lookupString(String str) {
        Index index2 = (Index) this.string_table.get(str);
        if (index2 != null) {
            return index2.index;
        }
        return -1;
    }

    public int addString(String str) {
        int lookupString = lookupString(str);
        if (lookupString != -1) {
            return lookupString;
        }
        int addUtf8 = addUtf8(str);
        adjustSize();
        ConstantString constantString = new ConstantString(addUtf8);
        int i = this.index;
        Constant[] constantArr = this.constants;
        this.index = i + 1;
        constantArr[i] = constantString;
        this.string_table.put(str, new Index(i));
        return i;
    }

    public int lookupClass(String str) {
        Index index2 = (Index) this.class_table.get(str.replace('.', '/'));
        if (index2 != null) {
            return index2.index;
        }
        return -1;
    }

    private int addClass_(String str) {
        int lookupClass = lookupClass(str);
        if (lookupClass != -1) {
            return lookupClass;
        }
        adjustSize();
        ConstantClass constantClass = new ConstantClass(addUtf8(str));
        int i = this.index;
        Constant[] constantArr = this.constants;
        this.index = i + 1;
        constantArr[i] = constantClass;
        this.class_table.put(str, new Index(i));
        return i;
    }

    public int addClass(String str) {
        return addClass_(str.replace('.', '/'));
    }

    public int addClass(ObjectType objectType) {
        return addClass(objectType.getClassName());
    }

    public int addArrayClass(ArrayType arrayType) {
        return addClass_(arrayType.getSignature());
    }

    public int lookupInteger(int i) {
        for (int i2 = 1; i2 < this.index; i2++) {
            Constant[] constantArr = this.constants;
            if ((constantArr[i2] instanceof ConstantInteger) && ((ConstantInteger) constantArr[i2]).getBytes() == i) {
                return i2;
            }
        }
        return -1;
    }

    public int addInteger(int i) {
        int lookupInteger = lookupInteger(i);
        if (lookupInteger != -1) {
            return lookupInteger;
        }
        adjustSize();
        int i2 = this.index;
        Constant[] constantArr = this.constants;
        this.index = i2 + 1;
        constantArr[i2] = new ConstantInteger(i);
        return i2;
    }

    public int lookupFloat(float f) {
        int floatToIntBits = Float.floatToIntBits(f);
        for (int i = 1; i < this.index; i++) {
            Constant[] constantArr = this.constants;
            if ((constantArr[i] instanceof ConstantFloat) && Float.floatToIntBits(((ConstantFloat) constantArr[i]).getBytes()) == floatToIntBits) {
                return i;
            }
        }
        return -1;
    }

    public int addFloat(float f) {
        int lookupFloat = lookupFloat(f);
        if (lookupFloat != -1) {
            return lookupFloat;
        }
        adjustSize();
        int i = this.index;
        Constant[] constantArr = this.constants;
        this.index = i + 1;
        constantArr[i] = new ConstantFloat(f);
        return i;
    }

    public int lookupUtf8(String str) {
        Index index2 = (Index) this.utf8_table.get(str);
        if (index2 != null) {
            return index2.index;
        }
        return -1;
    }

    public int addUtf8(String str) {
        int lookupUtf8 = lookupUtf8(str);
        if (lookupUtf8 != -1) {
            return lookupUtf8;
        }
        adjustSize();
        int i = this.index;
        Constant[] constantArr = this.constants;
        this.index = i + 1;
        constantArr[i] = new ConstantUtf8(str);
        this.utf8_table.put(str, new Index(i));
        return i;
    }

    public int lookupLong(long j) {
        for (int i = 1; i < this.index; i++) {
            Constant[] constantArr = this.constants;
            if ((constantArr[i] instanceof ConstantLong) && ((ConstantLong) constantArr[i]).getBytes() == j) {
                return i;
            }
        }
        return -1;
    }

    public int addLong(long j) {
        int lookupLong = lookupLong(j);
        if (lookupLong != -1) {
            return lookupLong;
        }
        adjustSize();
        int i = this.index;
        this.constants[i] = new ConstantLong(j);
        this.index += 2;
        return i;
    }

    public int lookupDouble(double d) {
        long doubleToLongBits = Double.doubleToLongBits(d);
        for (int i = 1; i < this.index; i++) {
            Constant[] constantArr = this.constants;
            if ((constantArr[i] instanceof ConstantDouble) && Double.doubleToLongBits(((ConstantDouble) constantArr[i]).getBytes()) == doubleToLongBits) {
                return i;
            }
        }
        return -1;
    }

    public int addDouble(double d) {
        int lookupDouble = lookupDouble(d);
        if (lookupDouble != -1) {
            return lookupDouble;
        }
        adjustSize();
        int i = this.index;
        this.constants[i] = new ConstantDouble(d);
        this.index += 2;
        return i;
    }

    public int lookupNameAndType(String str, String str2) {
        HashMap hashMap = this.n_a_t_table;
        Index index2 = (Index) hashMap.get(str + NAT_DELIM + str2);
        if (index2 != null) {
            return index2.index;
        }
        return -1;
    }

    public int addNameAndType(String str, String str2) {
        int lookupNameAndType = lookupNameAndType(str, str2);
        if (lookupNameAndType != -1) {
            return lookupNameAndType;
        }
        adjustSize();
        int addUtf8 = addUtf8(str);
        int addUtf82 = addUtf8(str2);
        int i = this.index;
        Constant[] constantArr = this.constants;
        this.index = i + 1;
        constantArr[i] = new ConstantNameAndType(addUtf8, addUtf82);
        HashMap hashMap = this.n_a_t_table;
        hashMap.put(str + NAT_DELIM + str2, new Index(i));
        return i;
    }

    public int lookupMethodref(String str, String str2, String str3) {
        HashMap hashMap = this.cp_table;
        Index index2 = (Index) hashMap.get(str + METHODREF_DELIM + str2 + METHODREF_DELIM + str3);
        if (index2 != null) {
            return index2.index;
        }
        return -1;
    }

    public int lookupMethodref(MethodGen methodGen) {
        return lookupMethodref(methodGen.getClassName(), methodGen.getName(), methodGen.getSignature());
    }

    public int addMethodref(String str, String str2, String str3) {
        int lookupMethodref = lookupMethodref(str, str2, str3);
        if (lookupMethodref != -1) {
            return lookupMethodref;
        }
        adjustSize();
        int addNameAndType = addNameAndType(str2, str3);
        int addClass = addClass(str);
        int i = this.index;
        Constant[] constantArr = this.constants;
        this.index = i + 1;
        constantArr[i] = new ConstantMethodref(addClass, addNameAndType);
        HashMap hashMap = this.cp_table;
        hashMap.put(str + METHODREF_DELIM + str2 + METHODREF_DELIM + str3, new Index(i));
        return i;
    }

    public int addMethodref(MethodGen methodGen) {
        return addMethodref(methodGen.getClassName(), methodGen.getName(), methodGen.getSignature());
    }

    public int lookupInterfaceMethodref(String str, String str2, String str3) {
        HashMap hashMap = this.cp_table;
        Index index2 = (Index) hashMap.get(str + IMETHODREF_DELIM + str2 + IMETHODREF_DELIM + str3);
        if (index2 != null) {
            return index2.index;
        }
        return -1;
    }

    public int lookupInterfaceMethodref(MethodGen methodGen) {
        return lookupInterfaceMethodref(methodGen.getClassName(), methodGen.getName(), methodGen.getSignature());
    }

    public int addInterfaceMethodref(String str, String str2, String str3) {
        int lookupInterfaceMethodref = lookupInterfaceMethodref(str, str2, str3);
        if (lookupInterfaceMethodref != -1) {
            return lookupInterfaceMethodref;
        }
        adjustSize();
        int addClass = addClass(str);
        int addNameAndType = addNameAndType(str2, str3);
        int i = this.index;
        Constant[] constantArr = this.constants;
        this.index = i + 1;
        constantArr[i] = new ConstantInterfaceMethodref(addClass, addNameAndType);
        HashMap hashMap = this.cp_table;
        hashMap.put(str + IMETHODREF_DELIM + str2 + IMETHODREF_DELIM + str3, new Index(i));
        return i;
    }

    public int addInterfaceMethodref(MethodGen methodGen) {
        return addInterfaceMethodref(methodGen.getClassName(), methodGen.getName(), methodGen.getSignature());
    }

    public int lookupFieldref(String str, String str2, String str3) {
        HashMap hashMap = this.cp_table;
        Index index2 = (Index) hashMap.get(str + FIELDREF_DELIM + str2 + FIELDREF_DELIM + str3);
        if (index2 != null) {
            return index2.index;
        }
        return -1;
    }

    public int addFieldref(String str, String str2, String str3) {
        int lookupFieldref = lookupFieldref(str, str2, str3);
        if (lookupFieldref != -1) {
            return lookupFieldref;
        }
        adjustSize();
        int addClass = addClass(str);
        int addNameAndType = addNameAndType(str2, str3);
        int i = this.index;
        Constant[] constantArr = this.constants;
        this.index = i + 1;
        constantArr[i] = new ConstantFieldref(addClass, addNameAndType);
        HashMap hashMap = this.cp_table;
        hashMap.put(str + FIELDREF_DELIM + str2 + FIELDREF_DELIM + str3, new Index(i));
        return i;
    }

    public Constant getConstant(int i) {
        return this.constants[i];
    }

    public void setConstant(int i, Constant constant) {
        this.constants[i] = constant;
    }

    public ConstantPool getConstantPool() {
        return new ConstantPool(this.constants);
    }

    public int getSize() {
        return this.index;
    }

    public ConstantPool getFinalConstantPool() {
        int i = this.index;
        Constant[] constantArr = new Constant[i];
        System.arraycopy(this.constants, 0, constantArr, 0, i);
        return new ConstantPool(constantArr);
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 1; i < this.index; i++) {
            stringBuffer.append(i + ")" + this.constants[i] + "\n");
        }
        return stringBuffer.toString();
    }

    public int addConstant(Constant constant, ConstantPoolGen constantPoolGen) {
        Constant[] constantPool = constantPoolGen.getConstantPool().getConstantPool();
        switch (constant.getTag()) {
            case 1:
                return addUtf8(((ConstantUtf8) constant).getBytes());
            case 2:
            default:
                throw new RuntimeException("Unknown constant type " + constant);
            case 3:
                return addInteger(((ConstantInteger) constant).getBytes());
            case 4:
                return addFloat(((ConstantFloat) constant).getBytes());
            case 5:
                return addLong(((ConstantLong) constant).getBytes());
            case 6:
                return addDouble(((ConstantDouble) constant).getBytes());
            case 7:
                return addClass(((ConstantUtf8) constantPool[((ConstantClass) constant).getNameIndex()]).getBytes());
            case 8:
                return addString(((ConstantUtf8) constantPool[((ConstantString) constant).getStringIndex()]).getBytes());
            case 9:
            case 10:
            case 11:
                ConstantCP constantCP = (ConstantCP) constant;
                ConstantNameAndType constantNameAndType = (ConstantNameAndType) constantPool[constantCP.getNameAndTypeIndex()];
                String replace = ((ConstantUtf8) constantPool[((ConstantClass) constantPool[constantCP.getClassIndex()]).getNameIndex()]).getBytes().replace('/', '.');
                String bytes = ((ConstantUtf8) constantPool[constantNameAndType.getNameIndex()]).getBytes();
                String bytes2 = ((ConstantUtf8) constantPool[constantNameAndType.getSignatureIndex()]).getBytes();
                switch (constant.getTag()) {
                    case 9:
                        return addFieldref(replace, bytes, bytes2);
                    case 10:
                        return addMethodref(replace, bytes, bytes2);
                    case 11:
                        return addInterfaceMethodref(replace, bytes, bytes2);
                    default:
                        throw new RuntimeException("Unknown constant type " + constant);
                }
            case 12:
                ConstantNameAndType constantNameAndType2 = (ConstantNameAndType) constant;
                return addNameAndType(((ConstantUtf8) constantPool[constantNameAndType2.getNameIndex()]).getBytes(), ((ConstantUtf8) constantPool[constantNameAndType2.getSignatureIndex()]).getBytes());
        }
    }
}
