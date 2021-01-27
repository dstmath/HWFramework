package ohos.com.sun.org.apache.bcel.internal.generic;

import java.util.ArrayList;
import ohos.com.sun.org.apache.bcel.internal.classfile.AccessFlags;
import ohos.com.sun.org.apache.bcel.internal.classfile.Attribute;

public abstract class FieldGenOrMethodGen extends AccessFlags implements NamedAndTyped, Cloneable {
    private ArrayList attribute_vec = new ArrayList();
    protected ConstantPoolGen cp;
    protected String name;
    protected Type type;

    public abstract String getSignature();

    protected FieldGenOrMethodGen() {
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.NamedAndTyped
    public void setType(Type type2) {
        if (type2.getType() != 16) {
            this.type = type2;
            return;
        }
        throw new IllegalArgumentException("Type can not be " + type2);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.NamedAndTyped
    public Type getType() {
        return this.type;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.NamedAndTyped
    public String getName() {
        return this.name;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.NamedAndTyped
    public void setName(String str) {
        this.name = str;
    }

    public ConstantPoolGen getConstantPool() {
        return this.cp;
    }

    public void setConstantPool(ConstantPoolGen constantPoolGen) {
        this.cp = constantPoolGen;
    }

    public void addAttribute(Attribute attribute) {
        this.attribute_vec.add(attribute);
    }

    public void removeAttribute(Attribute attribute) {
        this.attribute_vec.remove(attribute);
    }

    public void removeAttributes() {
        this.attribute_vec.clear();
    }

    public Attribute[] getAttributes() {
        Attribute[] attributeArr = new Attribute[this.attribute_vec.size()];
        this.attribute_vec.toArray(attributeArr);
        return attributeArr;
    }

    @Override // java.lang.Object
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            System.err.println(e);
            return null;
        }
    }
}
