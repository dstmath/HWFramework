package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO;
import ohos.com.sun.org.apache.bcel.internal.generic.IFEQ;
import ohos.com.sun.org.apache.bcel.internal.generic.IFNONNULL;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FlowList;

public class StringType extends Type {
    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String getClassName() {
        return "java.lang.String";
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public boolean identicalTo(Type type) {
        return this == type;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public boolean isSimple() {
        return true;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toSignature() {
        return Constants.STRING_SIG;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toString() {
        return "string";
    }

    protected StringType() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Type toJCType() {
        return Type.STRING;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, Type type) {
        if (type == Type.Boolean) {
            translateTo(classGenerator, methodGenerator, (BooleanType) type);
        } else if (type == Type.Real) {
            translateTo(classGenerator, methodGenerator, (RealType) type);
        } else if (type == Type.Reference) {
            translateTo(classGenerator, methodGenerator, (ReferenceType) type);
        } else if (type != Type.ObjectString) {
            classGenerator.getParser().reportError(2, new ErrorMsg("DATA_CONVERSION_ERR", toString(), type.toString()));
        }
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, BooleanType booleanType) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        FlowList translateToDesynthesized = translateToDesynthesized(classGenerator, methodGenerator, booleanType);
        instructionList.append(ICONST_1);
        BranchHandle append = instructionList.append((BranchInstruction) new GOTO(null));
        translateToDesynthesized.backPatch(instructionList.append(ICONST_0));
        append.setTarget(instructionList.append(NOP));
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, RealType realType) {
        methodGenerator.getInstructionList().append(new INVOKESTATIC(classGenerator.getConstantPool().addMethodref(Constants.BASIS_LIBRARY_CLASS, Constants.STRING_TO_REAL, Constants.STRING_TO_REAL_SIG)));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public FlowList translateToDesynthesized(ClassGenerator classGenerator, MethodGenerator methodGenerator, BooleanType booleanType) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref("java.lang.String", "length", "()I")));
        return new FlowList(instructionList.append((BranchInstruction) new IFEQ(null)));
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, ReferenceType referenceType) {
        methodGenerator.getInstructionList().append(NOP);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, Class cls) {
        if (cls.isAssignableFrom(String.class)) {
            methodGenerator.getInstructionList().append(NOP);
            return;
        }
        classGenerator.getParser().reportError(2, new ErrorMsg("DATA_CONVERSION_ERR", toString(), cls.getName()));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateFrom(ClassGenerator classGenerator, MethodGenerator methodGenerator, Class cls) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (cls.getName().equals("java.lang.String")) {
            instructionList.append(DUP);
            BranchHandle append = instructionList.append((BranchInstruction) new IFNONNULL(null));
            instructionList.append(POP);
            instructionList.append(new PUSH(constantPool, ""));
            append.setTarget(instructionList.append(NOP));
            return;
        }
        classGenerator.getParser().reportError(2, new ErrorMsg("DATA_CONVERSION_ERR", toString(), cls.getName()));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateBox(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        translateTo(classGenerator, methodGenerator, Type.Reference);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateUnBox(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
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
