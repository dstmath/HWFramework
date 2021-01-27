package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO;
import ohos.com.sun.org.apache.bcel.internal.generic.IFNULL;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;
import ohos.com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;

public final class ObjectType extends Type {
    private Class _clazz = Object.class;
    private String _javaClassName = Constants.OBJECT_CLASS;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public boolean identicalTo(Type type) {
        return this == type;
    }

    protected ObjectType(String str) {
        this._javaClassName = str;
        try {
            this._clazz = ObjectFactory.findProviderClass(str, true);
        } catch (ClassNotFoundException unused) {
            this._clazz = null;
        }
    }

    protected ObjectType(Class cls) {
        this._clazz = cls;
        this._javaClassName = cls.getName();
    }

    public int hashCode() {
        return Object.class.hashCode();
    }

    public boolean equals(Object obj) {
        return obj instanceof ObjectType;
    }

    public String getJavaClassName() {
        return this._javaClassName;
    }

    public Class getJavaClass() {
        return this._clazz;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toString() {
        return this._javaClassName;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toSignature() {
        StringBuffer stringBuffer = new StringBuffer("L");
        stringBuffer.append(this._javaClassName.replace('.', '/'));
        stringBuffer.append(';');
        return stringBuffer.toString();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Type toJCType() {
        return Util.getJCRefType(toSignature());
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, Type type) {
        if (type == Type.String) {
            translateTo(classGenerator, methodGenerator, (StringType) type);
            return;
        }
        classGenerator.getParser().reportError(2, new ErrorMsg("DATA_CONVERSION_ERR", toString(), type.toString()));
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, StringType stringType) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        instructionList.append(DUP);
        BranchHandle append = instructionList.append((BranchInstruction) new IFNULL(null));
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(this._javaClassName, "toString", "()Ljava/lang/String;")));
        BranchHandle append2 = instructionList.append((BranchInstruction) new GOTO(null));
        append.setTarget(instructionList.append(POP));
        instructionList.append(new PUSH(constantPool, ""));
        append2.setTarget(instructionList.append(NOP));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, Class cls) {
        if (cls.isAssignableFrom(this._clazz)) {
            methodGenerator.getInstructionList().append(NOP);
            return;
        }
        classGenerator.getParser().reportError(2, new ErrorMsg("DATA_CONVERSION_ERR", toString(), cls.getClass().toString()));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateFrom(ClassGenerator classGenerator, MethodGenerator methodGenerator, Class cls) {
        methodGenerator.getInstructionList().append(NOP);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction LOAD(int i) {
        return new ALOAD(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction STORE(int i) {
        return new ASTORE(i);
    }
}
