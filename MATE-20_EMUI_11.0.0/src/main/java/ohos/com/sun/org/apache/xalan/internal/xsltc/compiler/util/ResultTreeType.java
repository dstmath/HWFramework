package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.CHECKCAST;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GETFIELD;
import ohos.com.sun.org.apache.bcel.internal.generic.IFEQ;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.FlowList;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;

public final class ResultTreeType extends Type {
    private final String _methodName;

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String getClassName() {
        return "ohos.com.sun.org.apache.xalan.internal.xsltc.DOM";
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toSignature() {
        return Constants.DOM_INTF_SIG;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public String toString() {
        return "result-tree";
    }

    protected ResultTreeType() {
        this._methodName = null;
    }

    public ResultTreeType(String str) {
        this._methodName = str;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public boolean identicalTo(Type type) {
        return type instanceof ResultTreeType;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public Type toJCType() {
        return Util.getJCRefType(toSignature());
    }

    public String getMethodName() {
        return this._methodName;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public boolean implementedAsMethod() {
        return this._methodName != null;
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

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, BooleanType booleanType) {
        classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        instructionList.append(POP);
        instructionList.append(ICONST_1);
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, StringType stringType) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (this._methodName == null) {
            instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getStringValue", "()Ljava/lang/String;"), 1));
            return;
        }
        String className = classGenerator.getClassName();
        methodGenerator.getLocalIndex(Keywords.FUNC_CURRENT_STRING);
        instructionList.append(classGenerator.loadTranslet());
        if (classGenerator.isExternal()) {
            instructionList.append(new CHECKCAST(constantPool.addClass(className)));
        }
        instructionList.append(DUP);
        instructionList.append(new GETFIELD(constantPool.addFieldref(className, Constants.DOM_FIELD, Constants.DOM_INTF_SIG)));
        int addMethodref = constantPool.addMethodref(Constants.STRING_VALUE_HANDLER, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, "()V");
        instructionList.append(new NEW(constantPool.addClass(Constants.STRING_VALUE_HANDLER)));
        instructionList.append(DUP);
        instructionList.append(DUP);
        instructionList.append(new INVOKESPECIAL(addMethodref));
        LocalVariableGen addLocalVariable = methodGenerator.addLocalVariable("rt_to_string_handler", Util.getJCRefType(Constants.STRING_VALUE_HANDLER_SIG), null, null);
        addLocalVariable.setStart(instructionList.append(new ASTORE(addLocalVariable.getIndex())));
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(className, this._methodName, "(Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;)V")));
        addLocalVariable.setEnd(instructionList.append(new ALOAD(addLocalVariable.getIndex())));
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.STRING_VALUE_HANDLER, "getValue", "()Ljava/lang/String;")));
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, RealType realType) {
        translateTo(classGenerator, methodGenerator, Type.String);
        Type.String.translateTo(classGenerator, methodGenerator, Type.Real);
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, ReferenceType referenceType) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (this._methodName == null) {
            instructionList.append(NOP);
            return;
        }
        String className = classGenerator.getClassName();
        methodGenerator.getLocalIndex(Keywords.FUNC_CURRENT_STRING);
        instructionList.append(classGenerator.loadTranslet());
        if (classGenerator.isExternal()) {
            instructionList.append(new CHECKCAST(constantPool.addClass(className)));
        }
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(methodGenerator.loadDOM());
        int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getResultTreeFrag", "(IZ)Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;");
        instructionList.append(new PUSH(constantPool, 32));
        instructionList.append(new PUSH(constantPool, false));
        instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 3));
        instructionList.append(DUP);
        LocalVariableGen addLocalVariable = methodGenerator.addLocalVariable("rt_to_reference_dom", Util.getJCRefType(Constants.DOM_INTF_SIG), null, null);
        instructionList.append(new CHECKCAST(constantPool.addClass(Constants.DOM_INTF_SIG)));
        addLocalVariable.setStart(instructionList.append(new ASTORE(addLocalVariable.getIndex())));
        instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getOutputDomBuilder", "()Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;"), 1));
        instructionList.append(DUP);
        instructionList.append(DUP);
        LocalVariableGen addLocalVariable2 = methodGenerator.addLocalVariable("rt_to_reference_handler", Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;"), null, null);
        addLocalVariable2.setStart(instructionList.append(new ASTORE(addLocalVariable2.getIndex())));
        instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", "startDocument", "()V"), 1));
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(className, this._methodName, "(Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;)V")));
        addLocalVariable2.setEnd(instructionList.append(new ALOAD(addLocalVariable2.getIndex())));
        instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler", "endDocument", "()V"), 1));
        addLocalVariable.setEnd(instructionList.append(new ALOAD(addLocalVariable.getIndex())));
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, NodeSetType nodeSetType) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        instructionList.append(DUP);
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new GETFIELD(constantPool.addFieldref(Constants.TRANSLET_CLASS, Constants.NAMES_INDEX, "[Ljava/lang/String;")));
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new GETFIELD(constantPool.addFieldref(Constants.TRANSLET_CLASS, Constants.URIS_INDEX, "[Ljava/lang/String;")));
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new GETFIELD(constantPool.addFieldref(Constants.TRANSLET_CLASS, Constants.TYPES_INDEX, Constants.TYPES_INDEX_SIG)));
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new GETFIELD(constantPool.addFieldref(Constants.TRANSLET_CLASS, Constants.NAMESPACE_INDEX, "[Ljava/lang/String;")));
        instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "setupMapping", "([Ljava/lang/String;[Ljava/lang/String;[I[Ljava/lang/String;)V"), 5));
        instructionList.append(DUP);
        instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getIterator", "()Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), 1));
    }

    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, ObjectType objectType) {
        methodGenerator.getInstructionList().append(NOP);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public FlowList translateToDesynthesized(ClassGenerator classGenerator, MethodGenerator methodGenerator, BooleanType booleanType) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        translateTo(classGenerator, methodGenerator, Type.Boolean);
        return new FlowList(instructionList.append((BranchInstruction) new IFEQ(null)));
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type
    public void translateTo(ClassGenerator classGenerator, MethodGenerator methodGenerator, Class cls) {
        String name = cls.getName();
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (name.equals("ohos.org.w3c.dom.Node")) {
            translateTo(classGenerator, methodGenerator, Type.NodeSet);
            instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.MAKE_NODE, Constants.MAKE_NODE_SIG2), 2));
        } else if (name.equals("ohos.org.w3c.dom.NodeList")) {
            translateTo(classGenerator, methodGenerator, Type.NodeSet);
            instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.MAKE_NODE_LIST, Constants.MAKE_NODE_LIST_SIG2), 2));
        } else if (name.equals(Constants.OBJECT_CLASS)) {
            instructionList.append(NOP);
        } else if (name.equals("java.lang.String")) {
            translateTo(classGenerator, methodGenerator, Type.String);
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
