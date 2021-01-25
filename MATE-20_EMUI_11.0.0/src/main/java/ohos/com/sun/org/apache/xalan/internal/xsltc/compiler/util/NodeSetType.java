package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO;
import ohos.com.sun.org.apache.bcel.internal.generic.IFLT;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.ObjectType;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FlowList;

public final class NodeSetType extends Type {
    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String getClassName() {
        return Constants.NODE_ITERATOR;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public boolean identicalTo(Type type) {
        return this == type;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toSignature() {
        return "Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;";
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toString() {
        return "node-set";
    }

    protected NodeSetType() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Type toJCType() {
        return new ObjectType(Constants.NODE_ITERATOR);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, Type type) {
        if (type == Type.String) {
            translateTo(classGenerator, methodGenerator, (StringType) type);
        } else if (type == Type.Boolean) {
            translateTo(classGenerator, methodGenerator, (BooleanType) type);
        } else if (type == Type.Real) {
            translateTo(classGenerator, methodGenerator, (RealType) type);
        } else if (type == Type.Node) {
            translateTo(classGenerator, methodGenerator, (NodeType) type);
        } else if (type == Type.Reference) {
            translateTo(classGenerator, methodGenerator, (ReferenceType) type);
        } else if (type == Type.Object) {
            translateTo(classGenerator, methodGenerator, (ObjectType) type);
        } else {
            classGenerator.getParser().reportError(2, new ErrorMsg("DATA_CONVERSION_ERR", toString(), type.toString()));
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateFrom(ClassGenerator classGenerator, MethodGenerator methodGenerator, Class cls) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        if (cls.getName().equals("ohos.org.w3c.dom.NodeList")) {
            instructionList.append(classGenerator.loadTranslet());
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(new INVOKESTATIC(constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "nodeList2Iterator", "(Lohos.org.w3c.dom.NodeList;Lohos.com.sun.org.apache.xalan.internal.xsltc.Translet;Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;)Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;")));
        } else if (cls.getName().equals("ohos.org.w3c.dom.Node")) {
            instructionList.append(classGenerator.loadTranslet());
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(new INVOKESTATIC(constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "node2Iterator", "(Lohos.org.w3c.dom.Node;Lohos.com.sun.org.apache.xalan.internal.xsltc.Translet;Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;)Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;")));
        } else {
            classGenerator.getParser().reportError(2, new ErrorMsg("DATA_CONVERSION_ERR", toString(), cls.getName()));
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

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, StringType stringType) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        getFirstNode(classGenerator, methodGenerator);
        instructionList.append(DUP);
        BranchHandle append = instructionList.append((BranchInstruction) new IFLT(null));
        Type.Node.translateTo(classGenerator, methodGenerator, stringType);
        BranchHandle append2 = instructionList.append((BranchInstruction) new GOTO(null));
        append.setTarget(instructionList.append(POP));
        instructionList.append(new PUSH(classGenerator.getConstantPool(), ""));
        append2.setTarget(instructionList.append(NOP));
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, RealType realType) {
        translateTo(classGenerator, methodGenerator, Type.String);
        Type.String.translateTo(classGenerator, methodGenerator, Type.Real);
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, NodeType nodeType) {
        getFirstNode(classGenerator, methodGenerator);
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, ObjectType objectType) {
        methodGenerator.getInstructionList().append(NOP);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public FlowList translateToDesynthesized(ClassGenerator classGenerator, MethodGenerator methodGenerator, BooleanType booleanType) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        getFirstNode(classGenerator, methodGenerator);
        return new FlowList(instructionList.append((BranchInstruction) new IFLT(null)));
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, ReferenceType referenceType) {
        methodGenerator.getInstructionList().append(NOP);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, Class cls) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        String name = cls.getName();
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(SWAP);
        if (name.equals("ohos.org.w3c.dom.Node")) {
            instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.MAKE_NODE, Constants.MAKE_NODE_SIG2), 2));
        } else if (name.equals("ohos.org.w3c.dom.NodeList") || name.equals(Constants.OBJECT_CLASS)) {
            instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.MAKE_NODE_LIST, Constants.MAKE_NODE_LIST_SIG2), 2));
        } else if (name.equals("java.lang.String")) {
            int addInterfaceMethodref = constantPool.addInterfaceMethodref(Constants.NODE_ITERATOR, Constants.NEXT, "()I");
            int addInterfaceMethodref2 = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.GET_NODE_VALUE, "(I)Ljava/lang/String;");
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 1));
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref2, 2));
        } else {
            classGenerator.getParser().reportError(2, new ErrorMsg("DATA_CONVERSION_ERR", toString(), name));
        }
    }

    private void getFirstNode(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        methodGenerator.getInstructionList().append(new INVOKEINTERFACE(classGenerator.getConstantPool().addInterfaceMethodref(Constants.NODE_ITERATOR, Constants.NEXT, "()I"), 1));
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
