package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.BranchHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.BranchInstruction;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GOTO;
import ohos.com.sun.org.apache.bcel.internal.generic.IFEQ;
import ohos.com.sun.org.apache.bcel.internal.generic.IFGE;
import ohos.com.sun.org.apache.bcel.internal.generic.IFGT;
import ohos.com.sun.org.apache.bcel.internal.generic.ILOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.ISTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeSetType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xml.internal.utils.XML11Char;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;

/* access modifiers changed from: package-private */
public final class Key extends TopLevelElement {
    private Pattern _match;
    private QName _name;
    private Expression _use;
    private Type _useType;

    Key() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        String attribute = getAttribute("name");
        if (!XML11Char.isXML11ValidQName(attribute)) {
            parser.reportError(3, new ErrorMsg("INVALID_QNAME_ERR", (Object) attribute, (SyntaxTreeNode) this));
        }
        this._name = parser.getQNameIgnoreDefaultNs(attribute);
        getSymbolTable().addKey(this._name, this);
        this._match = parser.parsePattern(this, Constants.ATTRNAME_MATCH, null);
        this._use = parser.parseExpression(this, "use", null);
        if (this._name == null) {
            reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "name");
        } else if (this._match.isDummy()) {
            reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, Constants.ATTRNAME_MATCH);
        } else if (this._use.isDummy()) {
            reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "use");
        }
    }

    public String getName() {
        return this._name.toString();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        this._match.typeCheck(symbolTable);
        this._useType = this._use.typeCheck(symbolTable);
        Type type = this._useType;
        if (!(type instanceof StringType) && !(type instanceof NodeSetType)) {
            this._use = new CastExpr(this._use, Type.String);
        }
        return Type.Void;
    }

    public void traverseNodeSet(ClassGenerator classGenerator, MethodGenerator methodGenerator, int i) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", Constants.GET_NODE_VALUE, "(I)Ljava/lang/String;");
        constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getNodeIdent", Constants.GET_PARENT_SIG);
        int addMethodref = constantPool.addMethodref(Constants.TRANSLET_CLASS, "setKeyIndexDom", "(Ljava/lang/String;Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;)V");
        LocalVariableGen addLocalVariable = methodGenerator.addLocalVariable("parentNode", Util.getJCRefType("I"), null, null);
        addLocalVariable.setStart(instructionList.append(new ISTORE(addLocalVariable.getIndex())));
        instructionList.append(methodGenerator.loadCurrentNode());
        instructionList.append(methodGenerator.loadIterator());
        this._use.translate(classGenerator, methodGenerator);
        this._use.startIterator(classGenerator, methodGenerator);
        instructionList.append(methodGenerator.storeIterator());
        BranchHandle append = instructionList.append((BranchInstruction) new GOTO(null));
        InstructionHandle append2 = instructionList.append(NOP);
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new PUSH(constantPool, this._name.toString()));
        addLocalVariable.setEnd(instructionList.append(new ILOAD(addLocalVariable.getIndex())));
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(methodGenerator.loadCurrentNode());
        instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 2));
        instructionList.append(new INVOKEVIRTUAL(i));
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new PUSH(constantPool, getName()));
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(new INVOKEVIRTUAL(addMethodref));
        append.setTarget(instructionList.append(methodGenerator.loadIterator()));
        instructionList.append(methodGenerator.nextNode());
        instructionList.append(DUP);
        instructionList.append(methodGenerator.storeCurrentNode());
        instructionList.append((BranchInstruction) new IFGE(append2));
        instructionList.append(methodGenerator.storeIterator());
        instructionList.append(methodGenerator.storeCurrentNode());
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        methodGenerator.getLocalIndex(Keywords.FUNC_CURRENT_STRING);
        int addMethodref = constantPool.addMethodref(Constants.TRANSLET_CLASS, "buildKeyIndex", "(Ljava/lang/String;ILjava/lang/String;)V");
        int addMethodref2 = constantPool.addMethodref(Constants.TRANSLET_CLASS, "setKeyIndexDom", "(Ljava/lang/String;Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;)V");
        constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getNodeIdent", Constants.GET_PARENT_SIG);
        int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getAxisIterator", "(I)Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;");
        instructionList.append(methodGenerator.loadCurrentNode());
        instructionList.append(methodGenerator.loadIterator());
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(new PUSH(constantPool, 4));
        instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 2));
        instructionList.append(methodGenerator.loadCurrentNode());
        instructionList.append(methodGenerator.setStartNode());
        instructionList.append(methodGenerator.storeIterator());
        BranchHandle append = instructionList.append((BranchInstruction) new GOTO(null));
        InstructionHandle append2 = instructionList.append(NOP);
        instructionList.append(methodGenerator.loadCurrentNode());
        this._match.translate(classGenerator, methodGenerator);
        this._match.synthesize(classGenerator, methodGenerator);
        BranchHandle append3 = instructionList.append((BranchInstruction) new IFEQ(null));
        if (this._useType instanceof NodeSetType) {
            instructionList.append(methodGenerator.loadCurrentNode());
            traverseNodeSet(classGenerator, methodGenerator, addMethodref);
        } else {
            instructionList.append(classGenerator.loadTranslet());
            instructionList.append(DUP);
            instructionList.append(new PUSH(constantPool, this._name.toString()));
            instructionList.append(DUP_X1);
            instructionList.append(methodGenerator.loadCurrentNode());
            this._use.translate(classGenerator, methodGenerator);
            instructionList.append(new INVOKEVIRTUAL(addMethodref));
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(new INVOKEVIRTUAL(addMethodref2));
        }
        InstructionHandle append4 = instructionList.append(NOP);
        instructionList.append(methodGenerator.loadIterator());
        instructionList.append(methodGenerator.nextNode());
        instructionList.append(DUP);
        instructionList.append(methodGenerator.storeCurrentNode());
        instructionList.append((BranchInstruction) new IFGT(append2));
        instructionList.append(methodGenerator.storeIterator());
        instructionList.append(methodGenerator.storeCurrentNode());
        append.setTarget(append4);
        append3.setTarget(append4);
    }
}
