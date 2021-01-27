package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.CHECKCAST;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.DLOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.DSTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO;
import ohos.com.sun.org.apache.bcel.internal.generic.IFEQ;
import ohos.com.sun.org.apache.bcel.internal.generic.IFNE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionConstants;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FlowList;

public final class RealType extends NumberType {
    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public boolean identicalTo(Type type) {
        return this == type;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toSignature() {
        return "D";
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toString() {
        return "real";
    }

    protected RealType() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Type toJCType() {
        return Type.DOUBLE;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public int distanceTo(Type type) {
        if (type == this) {
            return 0;
        }
        return type == Type.Int ? 1 : Integer.MAX_VALUE;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, Type type) {
        if (type == Type.String) {
            translateTo(classGenerator, methodGenerator, (StringType) type);
        } else if (type == Type.Boolean) {
            translateTo(classGenerator, methodGenerator, (BooleanType) type);
        } else if (type == Type.Reference) {
            translateTo(classGenerator, methodGenerator, (ReferenceType) type);
        } else if (type == Type.Int) {
            translateTo(classGenerator, methodGenerator, (IntType) type);
        } else {
            classGenerator.getParser().reportError(2, new ErrorMsg("DATA_CONVERSION_ERR", toString(), type.toString()));
        }
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, StringType stringType) {
        methodGenerator.getInstructionList().append(new INVOKESTATIC(classGenerator.getConstantPool().addMethodref(Constants.BASIS_LIBRARY_CLASS, "realToString", "(D)Ljava/lang/String;")));
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, BooleanType booleanType) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        FlowList translateToDesynthesized = translateToDesynthesized(classGenerator, methodGenerator, booleanType);
        instructionList.append(ICONST_1);
        BranchHandle append = instructionList.append((BranchInstruction) new GOTO(null));
        translateToDesynthesized.backPatch(instructionList.append(ICONST_0));
        append.setTarget(instructionList.append(NOP));
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, IntType intType) {
        methodGenerator.getInstructionList().append(new INVOKESTATIC(classGenerator.getConstantPool().addMethodref(Constants.BASIS_LIBRARY_CLASS, "realToInt", "(D)I")));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public FlowList translateToDesynthesized(ClassGenerator classGenerator, MethodGenerator methodGenerator, BooleanType booleanType) {
        FlowList flowList = new FlowList();
        classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        instructionList.append(DUP2);
        LocalVariableGen addLocalVariable = methodGenerator.addLocalVariable("real_to_boolean_tmp", Type.DOUBLE, null, null);
        addLocalVariable.setStart(instructionList.append(new DSTORE(addLocalVariable.getIndex())));
        instructionList.append(DCONST_0);
        instructionList.append(DCMPG);
        flowList.add(instructionList.append((BranchInstruction) new IFEQ(null)));
        instructionList.append(new DLOAD(addLocalVariable.getIndex()));
        addLocalVariable.setEnd(instructionList.append(new DLOAD(addLocalVariable.getIndex())));
        instructionList.append(DCMPG);
        flowList.add(instructionList.append((BranchInstruction) new IFNE(null)));
        return flowList;
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, ReferenceType referenceType) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        instructionList.append(new NEW(constantPool.addClass(Constants.DOUBLE_CLASS)));
        instructionList.append(DUP_X2);
        instructionList.append(DUP_X2);
        instructionList.append(POP);
        instructionList.append(new INVOKESPECIAL(constantPool.addMethodref(Constants.DOUBLE_CLASS, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, "(D)V")));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, Class cls) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (cls == Character.TYPE) {
            instructionList.append(D2I);
            instructionList.append(I2C);
        } else if (cls == Byte.TYPE) {
            instructionList.append(D2I);
            instructionList.append(I2B);
        } else if (cls == Short.TYPE) {
            instructionList.append(D2I);
            instructionList.append(I2S);
        } else if (cls == Integer.TYPE) {
            instructionList.append(D2I);
        } else if (cls == Long.TYPE) {
            instructionList.append(D2L);
        } else if (cls == Float.TYPE) {
            instructionList.append(D2F);
        } else if (cls == Double.TYPE) {
            instructionList.append(NOP);
        } else if (cls.isAssignableFrom(Double.class)) {
            translateTo(classGenerator, methodGenerator, Type.Reference);
        } else {
            classGenerator.getParser().reportError(2, new ErrorMsg("DATA_CONVERSION_ERR", toString(), cls.getName()));
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateFrom(ClassGenerator classGenerator, MethodGenerator methodGenerator, Class cls) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (cls == Character.TYPE || cls == Byte.TYPE || cls == Short.TYPE || cls == Integer.TYPE) {
            instructionList.append(I2D);
        } else if (cls == Long.TYPE) {
            instructionList.append(L2D);
        } else if (cls == Float.TYPE) {
            instructionList.append(F2D);
        } else if (cls == Double.TYPE) {
            instructionList.append(NOP);
        } else {
            classGenerator.getParser().reportError(2, new ErrorMsg("DATA_CONVERSION_ERR", toString(), cls.getName()));
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateBox(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        translateTo(classGenerator, methodGenerator, Type.Reference);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateUnBox(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        instructionList.append(new CHECKCAST(constantPool.addClass(Constants.DOUBLE_CLASS)));
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.DOUBLE_CLASS, Constants.DOUBLE_VALUE, Constants.DOUBLE_VALUE_SIG)));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction ADD() {
        return InstructionConstants.DADD;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction SUB() {
        return InstructionConstants.DSUB;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction MUL() {
        return InstructionConstants.DMUL;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction DIV() {
        return InstructionConstants.DDIV;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction REM() {
        return InstructionConstants.DREM;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction NEG() {
        return InstructionConstants.DNEG;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction LOAD(int i) {
        return new DLOAD(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction STORE(int i) {
        return new DSTORE(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction POP() {
        return POP2;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction CMP(boolean z) {
        return z ? InstructionConstants.DCMPG : InstructionConstants.DCMPL;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction DUP() {
        return DUP2;
    }
}
