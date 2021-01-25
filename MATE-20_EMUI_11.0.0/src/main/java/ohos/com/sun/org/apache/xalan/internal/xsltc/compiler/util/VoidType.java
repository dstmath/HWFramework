package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;

public final class VoidType extends Type {
    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public boolean identicalTo(Type type) {
        return this == type;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Type toJCType() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toSignature() {
        return "V";
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toString() {
        return "void";
    }

    protected VoidType() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction POP() {
        return NOP;
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
        methodGenerator.getInstructionList().append(new PUSH(classGenerator.getConstantPool(), ""));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateFrom(ClassGenerator classGenerator, MethodGenerator methodGenerator, Class cls) {
        if (!cls.getName().equals("void")) {
            classGenerator.getParser().reportError(2, new ErrorMsg("DATA_CONVERSION_ERR", toString(), cls.getName()));
        }
    }
}
