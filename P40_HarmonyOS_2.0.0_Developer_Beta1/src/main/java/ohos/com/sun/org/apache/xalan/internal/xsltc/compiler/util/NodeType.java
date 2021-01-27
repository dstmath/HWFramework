package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.CHECKCAST;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GETFIELD;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO;
import ohos.com.sun.org.apache.bcel.internal.generic.IFEQ;
import ohos.com.sun.org.apache.bcel.internal.generic.ILOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.ISTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FlowList;

public final class NodeType extends Type {
    private final int _type;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String getClassName() {
        return Constants.RUNTIME_NODE_CLASS;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toSignature() {
        return "I";
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toString() {
        return "node-type";
    }

    protected NodeType() {
        this(-1);
    }

    protected NodeType(int i) {
        this._type = i;
    }

    public int getType() {
        return this._type;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public boolean identicalTo(Type type) {
        return type instanceof NodeType;
    }

    public int hashCode() {
        return this._type;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Type toJCType() {
        return Type.INT;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, Type type) {
        if (type == Type.String) {
            translateTo(classGenerator, methodGenerator, (StringType) type);
        } else if (type == Type.Boolean) {
            translateTo(classGenerator, methodGenerator, (BooleanType) type);
        } else if (type == Type.Real) {
            translateTo(classGenerator, methodGenerator, (RealType) type);
        } else if (type == Type.NodeSet) {
            translateTo(classGenerator, methodGenerator, (NodeSetType) type);
        } else if (type == Type.Reference) {
            translateTo(classGenerator, methodGenerator, (ReferenceType) type);
        } else if (type == Type.Object) {
            translateTo(classGenerator, methodGenerator, (ObjectType) type);
        } else {
            classGenerator.getParser().reportError(2, new ErrorMsg("DATA_CONVERSION_ERR", toString(), type.toString()));
        }
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, StringType stringType) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int i = this._type;
        if (i != -1) {
            if (i != 1) {
                if (!(i == 2 || i == 7 || i == 8)) {
                    if (i != 9) {
                        classGenerator.getParser().reportError(2, new ErrorMsg("DATA_CONVERSION_ERR", toString(), stringType.toString()));
                        return;
                    }
                }
            }
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(SWAP);
            instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.GET_ELEMENT_VALUE, "(I)Ljava/lang/String;"), 2));
            return;
        }
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(SWAP);
        instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.GET_NODE_VALUE, "(I)Ljava/lang/String;"), 2));
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
        translateTo(classGenerator, methodGenerator, Type.String);
        Type.String.translateTo(classGenerator, methodGenerator, Type.Real);
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, NodeSetType nodeSetType) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        instructionList.append(new NEW(constantPool.addClass(Constants.SINGLETON_ITERATOR)));
        instructionList.append(DUP_X1);
        instructionList.append(SWAP);
        instructionList.append(new INVOKESPECIAL(constantPool.addMethodref(Constants.SINGLETON_ITERATOR, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, "(I)V")));
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, ObjectType objectType) {
        methodGenerator.getInstructionList().append(NOP);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public FlowList translateToDesynthesized(ClassGenerator classGenerator, MethodGenerator methodGenerator, BooleanType booleanType) {
        return new FlowList(methodGenerator.getInstructionList().append((BranchInstruction) new IFEQ(null)));
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, ReferenceType referenceType) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        instructionList.append(new NEW(constantPool.addClass(Constants.RUNTIME_NODE_CLASS)));
        instructionList.append(DUP_X1);
        instructionList.append(SWAP);
        instructionList.append(new PUSH(constantPool, this._type));
        instructionList.append(new INVOKESPECIAL(constantPool.addMethodref(Constants.RUNTIME_NODE_CLASS, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, "(II)V")));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, Class cls) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        String name = cls.getName();
        if (name.equals("java.lang.String")) {
            translateTo(classGenerator, methodGenerator, Type.String);
            return;
        }
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(SWAP);
        if (name.equals("ohos.org.w3c.dom.Node") || name.equals(Constants.OBJECT_CLASS)) {
            instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.MAKE_NODE, Constants.MAKE_NODE_SIG), 2));
        } else if (name.equals("ohos.org.w3c.dom.NodeList")) {
            instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.MAKE_NODE_LIST, Constants.MAKE_NODE_LIST_SIG), 2));
        } else {
            classGenerator.getParser().reportError(2, new ErrorMsg("DATA_CONVERSION_ERR", toString(), name));
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
        instructionList.append(new CHECKCAST(constantPool.addClass(Constants.RUNTIME_NODE_CLASS)));
        instructionList.append(new GETFIELD(constantPool.addFieldref(Constants.RUNTIME_NODE_CLASS, "node", "I")));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction LOAD(int i) {
        return new ILOAD(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Instruction STORE(int i) {
        return new ISTORE(i);
    }
}
