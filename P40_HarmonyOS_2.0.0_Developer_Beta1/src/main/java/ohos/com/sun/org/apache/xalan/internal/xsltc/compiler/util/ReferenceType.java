package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.IFEQ;
import ohos.com.sun.org.apache.bcel.internal.generic.ILOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FlowList;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;

public final class ReferenceType extends Type {
    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public boolean identicalTo(Type type) {
        return this == type;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toSignature() {
        return Constants.OBJECT_SIG;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toString() {
        return "reference";
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateBox(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateUnBox(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
    }

    protected ReferenceType() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Type toJCType() {
        return Type.OBJECT;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, Type type) {
        if (type == Type.String) {
            translateTo(classGenerator, methodGenerator, (StringType) type);
        } else if (type == Type.Real) {
            translateTo(classGenerator, methodGenerator, (RealType) type);
        } else if (type == Type.Boolean) {
            translateTo(classGenerator, methodGenerator, (BooleanType) type);
        } else if (type == Type.NodeSet) {
            translateTo(classGenerator, methodGenerator, (NodeSetType) type);
        } else if (type == Type.Node) {
            translateTo(classGenerator, methodGenerator, (NodeType) type);
        } else if (type == Type.ResultTree) {
            translateTo(classGenerator, methodGenerator, (ResultTreeType) type);
        } else if (type == Type.Object) {
            translateTo(classGenerator, methodGenerator, (ObjectType) type);
        } else if (type != Type.Reference) {
            classGenerator.getParser().reportError(2, new ErrorMsg(ErrorMsg.INTERNAL_ERR, type.toString()));
        }
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, StringType stringType) {
        int localIndex = methodGenerator.getLocalIndex(Keywords.FUNC_CURRENT_STRING);
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (localIndex < 0) {
            instructionList.append(new PUSH(constantPool, 0));
        } else {
            instructionList.append(new ILOAD(localIndex));
        }
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(new INVOKESTATIC(constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "stringF", "(Ljava/lang/Object;ILohos.com.sun.org.apache.xalan.internal.xsltc.DOM;)Ljava/lang/String;")));
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, RealType realType) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(new INVOKESTATIC(constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "numberF", "(Ljava/lang/Object;Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;)D")));
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, BooleanType booleanType) {
        methodGenerator.getInstructionList().append(new INVOKESTATIC(classGenerator.getConstantPool().addMethodref(Constants.BASIS_LIBRARY_CLASS, "booleanF", "(Ljava/lang/Object;)Z")));
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, NodeSetType nodeSetType) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        instructionList.append(new INVOKESTATIC(constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "referenceToNodeSet", "(Ljava/lang/Object;)Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;")));
        instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref(Constants.NODE_ITERATOR, Constants.RESET, "()Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), 1));
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, NodeType nodeType) {
        translateTo(classGenerator, methodGenerator, Type.NodeSet);
        Type.NodeSet.translateTo(classGenerator, methodGenerator, nodeType);
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, ResultTreeType resultTreeType) {
        methodGenerator.getInstructionList().append(new INVOKESTATIC(classGenerator.getConstantPool().addMethodref(Constants.BASIS_LIBRARY_CLASS, "referenceToResultTree", "(Ljava/lang/Object;)Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;")));
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, ObjectType objectType) {
        methodGenerator.getInstructionList().append(NOP);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, Class cls) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int addMethodref = constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "referenceToLong", "(Ljava/lang/Object;)J");
        int addMethodref2 = constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "referenceToDouble", "(Ljava/lang/Object;)D");
        int addMethodref3 = constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "referenceToBoolean", "(Ljava/lang/Object;)Z");
        if (cls.getName().equals(Constants.OBJECT_CLASS)) {
            instructionList.append(NOP);
        } else if (cls == Double.TYPE) {
            instructionList.append(new INVOKESTATIC(addMethodref2));
        } else if (cls.getName().equals(Constants.DOUBLE_CLASS)) {
            instructionList.append(new INVOKESTATIC(addMethodref2));
            Type.Real.translateTo(classGenerator, methodGenerator, Type.Reference);
        } else if (cls == Float.TYPE) {
            instructionList.append(new INVOKESTATIC(addMethodref2));
            instructionList.append(D2F);
        } else if (cls.getName().equals("java.lang.String")) {
            int addMethodref4 = constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "referenceToString", "(Ljava/lang/Object;Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;)Ljava/lang/String;");
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(new INVOKESTATIC(addMethodref4));
        } else if (cls.getName().equals("ohos.org.w3c.dom.Node")) {
            int addMethodref5 = constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "referenceToNode", "(Ljava/lang/Object;Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;)Lohos.org.w3c.dom.Node;");
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(new INVOKESTATIC(addMethodref5));
        } else if (cls.getName().equals("ohos.org.w3c.dom.NodeList")) {
            int addMethodref6 = constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "referenceToNodeList", "(Ljava/lang/Object;Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;)Lohos.org.w3c.dom.NodeList;");
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(new INVOKESTATIC(addMethodref6));
        } else if (cls.getName().equals("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM")) {
            translateTo(classGenerator, methodGenerator, Type.ResultTree);
        } else if (cls == Long.TYPE) {
            instructionList.append(new INVOKESTATIC(addMethodref));
        } else if (cls == Integer.TYPE) {
            instructionList.append(new INVOKESTATIC(addMethodref));
            instructionList.append(L2I);
        } else if (cls == Short.TYPE) {
            instructionList.append(new INVOKESTATIC(addMethodref));
            instructionList.append(L2I);
            instructionList.append(I2S);
        } else if (cls == Byte.TYPE) {
            instructionList.append(new INVOKESTATIC(addMethodref));
            instructionList.append(L2I);
            instructionList.append(I2B);
        } else if (cls == Character.TYPE) {
            instructionList.append(new INVOKESTATIC(addMethodref));
            instructionList.append(L2I);
            instructionList.append(I2C);
        } else if (cls == Boolean.TYPE) {
            instructionList.append(new INVOKESTATIC(addMethodref3));
        } else if (cls.getName().equals(Constants.BOOLEAN_CLASS)) {
            instructionList.append(new INVOKESTATIC(addMethodref3));
            Type.Boolean.translateTo(classGenerator, methodGenerator, Type.Reference);
        } else {
            classGenerator.getParser().reportError(2, new ErrorMsg("DATA_CONVERSION_ERR", toString(), cls.getName()));
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateFrom(ClassGenerator classGenerator, MethodGenerator methodGenerator, Class cls) {
        if (cls.getName().equals(Constants.OBJECT_CLASS)) {
            methodGenerator.getInstructionList().append(NOP);
            return;
        }
        classGenerator.getParser().reportError(2, new ErrorMsg("DATA_CONVERSION_ERR", toString(), cls.getName()));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public FlowList translateToDesynthesized(ClassGenerator classGenerator, MethodGenerator methodGenerator, BooleanType booleanType) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        translateTo(classGenerator, methodGenerator, booleanType);
        return new FlowList(instructionList.append((BranchInstruction) new IFEQ(null)));
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
