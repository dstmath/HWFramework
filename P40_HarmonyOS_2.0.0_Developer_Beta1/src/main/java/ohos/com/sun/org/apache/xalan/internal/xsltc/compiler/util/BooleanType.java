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
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.ISTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;

public final class BooleanType extends Type {
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
        return Constants.HASIDCALL_INDEX_SIG;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toString() {
        return "boolean";
    }

    protected BooleanType() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Type toJCType() {
        return Type.BOOLEAN;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, Type type) {
        if (type == Type.String) {
            translateTo(classGenerator, methodGenerator, (StringType) type);
        } else if (type == Type.Real) {
            translateTo(classGenerator, methodGenerator, (RealType) type);
        } else if (type == Type.Reference) {
            translateTo(classGenerator, methodGenerator, (ReferenceType) type);
        } else {
            classGenerator.getParser().reportError(2, new ErrorMsg("DATA_CONVERSION_ERR", toString(), type.toString()));
        }
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, StringType stringType) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        BranchHandle append = instructionList.append((BranchInstruction) new IFEQ(null));
        instructionList.append(new PUSH(constantPool, "true"));
        BranchHandle append2 = instructionList.append((BranchInstruction) new GOTO(null));
        append.setTarget(instructionList.append(new PUSH(constantPool, "false")));
        append2.setTarget(instructionList.append(NOP));
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, RealType realType) {
        methodGenerator.getInstructionList().append(I2D);
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, ReferenceType referenceType) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        instructionList.append(new NEW(constantPool.addClass(Constants.BOOLEAN_CLASS)));
        instructionList.append(DUP_X1);
        instructionList.append(SWAP);
        instructionList.append(new INVOKESPECIAL(constantPool.addMethodref(Constants.BOOLEAN_CLASS, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, "(Z)V")));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, Class cls) {
        if (cls == Boolean.TYPE) {
            methodGenerator.getInstructionList().append(NOP);
        } else if (cls.isAssignableFrom(Boolean.class)) {
            translateTo(classGenerator, methodGenerator, Type.Reference);
        } else {
            classGenerator.getParser().reportError(2, new ErrorMsg("DATA_CONVERSION_ERR", toString(), cls.getName()));
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateFrom(ClassGenerator classGenerator, MethodGenerator methodGenerator, Class cls) {
        translateTo(classGenerator, methodGenerator, cls);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateBox(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        translateTo(classGenerator, methodGenerator, Type.Reference);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateUnBox(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        instructionList.append(new CHECKCAST(constantPool.addClass(Constants.BOOLEAN_CLASS)));
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.BOOLEAN_CLASS, Constants.BOOLEAN_VALUE, Constants.BOOLEAN_VALUE_SIG)));
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
