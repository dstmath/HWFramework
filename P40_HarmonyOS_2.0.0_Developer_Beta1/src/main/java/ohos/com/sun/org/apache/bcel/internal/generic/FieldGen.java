package ohos.com.sun.org.apache.bcel.internal.generic;

import java.util.ArrayList;
import java.util.Iterator;
import ohos.com.sun.org.apache.bcel.internal.classfile.Attribute;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantObject;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantPool;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantValue;
import ohos.com.sun.org.apache.bcel.internal.classfile.Field;
import ohos.com.sun.org.apache.bcel.internal.classfile.Utility;
import ohos.com.sun.org.apache.xpath.internal.XPath;

public class FieldGen extends FieldGenOrMethodGen {
    private ArrayList observers;
    private Object value;

    public FieldGen(int i, Type type, String str, ConstantPoolGen constantPoolGen) {
        this.value = null;
        setAccessFlags(i);
        setType(type);
        setName(str);
        setConstantPool(constantPoolGen);
    }

    public FieldGen(Field field, ConstantPoolGen constantPoolGen) {
        this(field.getAccessFlags(), Type.getType(field.getSignature()), field.getName(), constantPoolGen);
        Attribute[] attributes = field.getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i] instanceof ConstantValue) {
                setValue(((ConstantValue) attributes[i]).getConstantValueIndex());
            } else {
                addAttribute(attributes[i]);
            }
        }
    }

    private void setValue(int i) {
        ConstantPool constantPool = this.cp.getConstantPool();
        this.value = ((ConstantObject) constantPool.getConstant(i)).getConstantValue(constantPool);
    }

    public void setInitValue(String str) {
        checkType(new ObjectType("java.lang.String"));
        if (str != null) {
            this.value = str;
        }
    }

    public void setInitValue(long j) {
        checkType(Type.LONG);
        if (j != 0) {
            this.value = new Long(j);
        }
    }

    public void setInitValue(int i) {
        checkType(Type.INT);
        if (i != 0) {
            this.value = new Integer(i);
        }
    }

    public void setInitValue(short s) {
        checkType(Type.SHORT);
        if (s != 0) {
            this.value = new Integer(s);
        }
    }

    public void setInitValue(char c) {
        checkType(Type.CHAR);
        if (c != 0) {
            this.value = new Integer(c);
        }
    }

    public void setInitValue(byte b) {
        checkType(Type.BYTE);
        if (b != 0) {
            this.value = new Integer(b);
        }
    }

    public void setInitValue(boolean z) {
        checkType(Type.BOOLEAN);
        if (z) {
            this.value = new Integer(1);
        }
    }

    public void setInitValue(float f) {
        checkType(Type.FLOAT);
        if (((double) f) != XPath.MATCH_SCORE_QNAME) {
            this.value = new Float(f);
        }
    }

    public void setInitValue(double d) {
        checkType(Type.DOUBLE);
        if (d != XPath.MATCH_SCORE_QNAME) {
            this.value = new Double(d);
        }
    }

    public void cancelInitValue() {
        this.value = null;
    }

    private void checkType(Type type) {
        if (this.type == null) {
            throw new ClassGenException("You haven't defined the type of the field yet");
        } else if (!isFinal()) {
            throw new ClassGenException("Only final fields may have an initial value!");
        } else if (!this.type.equals(type)) {
            throw new ClassGenException("Types are not compatible: " + this.type + " vs. " + type);
        }
    }

    public Field getField() {
        String signature = getSignature();
        int addUtf8 = this.cp.addUtf8(this.name);
        int addUtf82 = this.cp.addUtf8(signature);
        if (this.value != null) {
            checkType(this.type);
            addAttribute(new ConstantValue(this.cp.addUtf8("ConstantValue"), 2, addConstant(), this.cp.getConstantPool()));
        }
        return new Field(this.access_flags, addUtf8, addUtf82, getAttributes(), this.cp.getConstantPool());
    }

    private int addConstant() {
        byte type = this.type.getType();
        if (type == 14) {
            return this.cp.addString((String) this.value);
        }
        switch (type) {
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
                return this.cp.addInteger(((Integer) this.value).intValue());
            case 6:
                return this.cp.addFloat(((Float) this.value).floatValue());
            case 7:
                return this.cp.addDouble(((Double) this.value).doubleValue());
            case 11:
                return this.cp.addLong(((Long) this.value).longValue());
            default:
                throw new RuntimeException("Oops: Unhandled : " + ((int) this.type.getType()));
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.FieldGenOrMethodGen
    public String getSignature() {
        return this.type.getSignature();
    }

    public void addObserver(FieldObserver fieldObserver) {
        if (this.observers == null) {
            this.observers = new ArrayList();
        }
        this.observers.add(fieldObserver);
    }

    public void removeObserver(FieldObserver fieldObserver) {
        ArrayList arrayList = this.observers;
        if (arrayList != null) {
            arrayList.remove(fieldObserver);
        }
    }

    public void update() {
        ArrayList arrayList = this.observers;
        if (arrayList != null) {
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                ((FieldObserver) it.next()).notify(this);
            }
        }
    }

    public String getInitValue() {
        Object obj = this.value;
        if (obj != null) {
            return obj.toString();
        }
        return null;
    }

    @Override // java.lang.Object
    public final String toString() {
        String accessToString = Utility.accessToString(this.access_flags);
        String str = "";
        if (!accessToString.equals(str)) {
            str = accessToString + " ";
        }
        StringBuffer stringBuffer = new StringBuffer(str + this.type.toString() + " " + getName());
        String initValue = getInitValue();
        if (initValue != null) {
            stringBuffer.append(" = " + initValue);
        }
        return stringBuffer.toString();
    }

    public FieldGen copy(ConstantPoolGen constantPoolGen) {
        FieldGen fieldGen = (FieldGen) clone();
        fieldGen.setConstantPool(constantPoolGen);
        return fieldGen;
    }
}
