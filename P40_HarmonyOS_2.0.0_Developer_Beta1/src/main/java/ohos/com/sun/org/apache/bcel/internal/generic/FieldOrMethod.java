package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantCP;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantNameAndType;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantPool;
import ohos.com.sun.org.apache.bcel.internal.classfile.ConstantUtf8;

public abstract class FieldOrMethod extends CPInstruction implements LoadClass {
    FieldOrMethod() {
    }

    protected FieldOrMethod(short s, int i) {
        super(s, i);
    }

    public String getSignature(ConstantPoolGen constantPoolGen) {
        ConstantPool constantPool = constantPoolGen.getConstantPool();
        return ((ConstantUtf8) constantPool.getConstant(((ConstantNameAndType) constantPool.getConstant(((ConstantCP) constantPool.getConstant(this.index)).getNameAndTypeIndex())).getSignatureIndex())).getBytes();
    }

    public String getName(ConstantPoolGen constantPoolGen) {
        ConstantPool constantPool = constantPoolGen.getConstantPool();
        return ((ConstantUtf8) constantPool.getConstant(((ConstantNameAndType) constantPool.getConstant(((ConstantCP) constantPool.getConstant(this.index)).getNameAndTypeIndex())).getNameIndex())).getBytes();
    }

    public String getClassName(ConstantPoolGen constantPoolGen) {
        ConstantPool constantPool = constantPoolGen.getConstantPool();
        return constantPool.getConstantString(((ConstantCP) constantPool.getConstant(this.index)).getClassIndex(), (byte) 7).replace('/', '.');
    }

    public ObjectType getClassType(ConstantPoolGen constantPoolGen) {
        return new ObjectType(getClassName(constantPoolGen));
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.generic.LoadClass
    public ObjectType getLoadClassType(ConstantPoolGen constantPoolGen) {
        return getClassType(constantPoolGen);
    }
}
