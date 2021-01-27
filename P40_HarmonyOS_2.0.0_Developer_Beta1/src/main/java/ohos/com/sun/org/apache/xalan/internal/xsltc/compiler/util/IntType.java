package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.CHECKCAST;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO;
import ohos.com.sun.org.apache.bcel.internal.generic.IFEQ;
import ohos.com.sun.org.apache.bcel.internal.generic.IFGE;
import ohos.com.sun.org.apache.bcel.internal.generic.IFGT;
import ohos.com.sun.org.apache.bcel.internal.generic.IFLE;
import ohos.com.sun.org.apache.bcel.internal.generic.IFLT;
import ohos.com.sun.org.apache.bcel.internal.generic.IF_ICMPGE;
import ohos.com.sun.org.apache.bcel.internal.generic.IF_ICMPGT;
import ohos.com.sun.org.apache.bcel.internal.generic.IF_ICMPLE;
import ohos.com.sun.org.apache.bcel.internal.generic.IF_ICMPLT;
import ohos.com.sun.org.apache.bcel.internal.generic.ILOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.ISTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionConstants;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FlowList;

public final class IntType extends NumberType {
    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public boolean identicalTo(Type type) {
        return this == type;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toSignature() {
        return "I";
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toString() {
        return "int";
    }

    protected IntType() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Type toJCType() {
        return Type.INT;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public int distanceTo(Type type) {
        if (type == this) {
            return 0;
        }
        return type == Type.Real ? 1 : Integer.MAX_VALUE;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, Type type) {
        if (type == Type.Real) {
            translateTo(classGenerator, methodGenerator, (RealType) type);
        } else if (type == Type.String) {
            translateTo(classGenerator, methodGenerator, (StringType) type);
        } else if (type == Type.Boolean) {
            translateTo(classGenerator, methodGenerator, (BooleanType) type);
        } else if (type == Type.Reference) {
            translateTo(classGenerator, methodGenerator, (ReferenceType) type);
        } else {
            classGenerator.getParser().reportError(2, new ErrorMsg("DATA_CONVERSION_ERR", toString(), type.toString()));
        }
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, RealType realType) {
        methodGenerator.getInstructionList().append(I2D);
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, StringType stringType) {
        methodGenerator.getInstructionList().append(new INVOKESTATIC(classGenerator.getConstantPool().addMethodref(Constants.INTEGER_CLASS, "toString", "(I)Ljava/lang/String;")));
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, BooleanType booleanType) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        BranchHandle append = instructionList.append((BranchInstruction) new IFEQ(null));
        instructionList.append(ICONST_1);
        BranchHandle append2 = instructionList.append((BranchInstruction) new GOTO(null));
        append.setTarget(instructionList.append(ICONST_0));
        append2.setTarget(instructionList.append(NOP));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public FlowList translateToDesynthesized(ClassGenerator classGenerator, MethodGenerator methodGenerator, BooleanType booleanType) {
        return new FlowList(methodGenerator.getInstructionList().append((BranchInstruction) new IFEQ(null)));
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, ReferenceType referenceType) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        instructionList.append(new NEW(constantPool.addClass(Constants.INTEGER_CLASS)));
        instructionList.append(DUP_X1);
        instructionList.append(SWAP);
        instructionList.append(new INVOKESPECIAL(constantPool.addMethodref(Constants.INTEGER_CLASS, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, "(I)V")));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, Class cls) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (cls == Character.TYPE) {
            instructionList.append(I2C);
        } else if (cls == Byte.TYPE) {
            instructionList.append(I2B);
        } else if (cls == Short.TYPE) {
            instructionList.append(I2S);
        } else if (cls == Integer.TYPE) {
            instructionList.append(NOP);
        } else if (cls == Long.TYPE) {
            instructionList.append(I2L);
        } else if (cls == Float.TYPE) {
            instructionList.append(I2F);
        } else if (cls == Double.TYPE) {
            instructionList.append(I2D);
        } else if (cls.isAssignableFrom(Double.class)) {
            instructionList.append(I2D);
            Type.Real.translateTo(classGenerator, methodGenerator, Type.Reference);
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
        instructionList.append(new CHECKCAST(constantPool.addClass(Constants.INTEGER_CLASS)));
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.INTEGER_CLASS, Constants.INT_VALUE, "()I")));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction ADD() {
        return InstructionConstants.IADD;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction SUB() {
        return InstructionConstants.ISUB;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction MUL() {
        return InstructionConstants.IMUL;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction DIV() {
        return InstructionConstants.IDIV;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction REM() {
        return InstructionConstants.IREM;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction NEG() {
        return InstructionConstants.INEG;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction LOAD(int i) {
        return new ILOAD(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction STORE(int i) {
        return new ISTORE(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public BranchInstruction GT(boolean z) {
        if (z) {
            return new IFGT(null);
        }
        return new IF_ICMPGT(null);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public BranchInstruction GE(boolean z) {
        if (z) {
            return new IFGE(null);
        }
        return new IF_ICMPGE(null);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public BranchInstruction LT(boolean z) {
        if (z) {
            return new IFLT(null);
        }
        return new IF_ICMPLT(null);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public BranchInstruction LE(boolean z) {
        if (z) {
            return new IFLE(null);
        }
        return new IF_ICMPLE(null);
    }
}
