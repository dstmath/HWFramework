package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.CHECKCAST;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ReferenceType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xml.internal.utils.XML11Char;

final class WithParam extends Instruction {
    private boolean _doParameterOptimization = false;
    private LocalVariableGen _domAdapter;
    protected String _escapedName;
    private QName _name;
    private Expression _select;

    WithParam() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        Util.println("with-param " + this._name);
        if (this._select != null) {
            indent(i + 4);
            Util.println("select " + this._select.toString());
        }
        displayContents(i + 4);
    }

    public String getEscapedName() {
        return this._escapedName;
    }

    public QName getName() {
        return this._name;
    }

    public void setName(QName qName) {
        this._name = qName;
        this._escapedName = Util.escape(qName.getStringRep());
    }

    public void setDoParameterOptimization(boolean z) {
        this._doParameterOptimization = z;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        String attribute = getAttribute("name");
        if (attribute.length() > 0) {
            if (!XML11Char.isXML11ValidQName(attribute)) {
                parser.reportError(3, new ErrorMsg("INVALID_QNAME_ERR", (Object) attribute, (SyntaxTreeNode) this));
            }
            setName(parser.getQNameIgnoreDefaultNs(attribute));
        } else {
            reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, "name");
        }
        if (getAttribute(Constants.ATTRNAME_SELECT).length() > 0) {
            this._select = parser.parseExpression(this, Constants.ATTRNAME_SELECT, null);
        }
        parseChildren(parser);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        Expression expression = this._select;
        if (expression == null) {
            typeCheckContents(symbolTable);
        } else if (!(expression.typeCheck(symbolTable) instanceof ReferenceType)) {
            this._select = new CastExpr(this._select, Type.Reference);
        }
        return Type.Void;
    }

    public void translateValue(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        Expression expression = this._select;
        if (expression != null) {
            expression.translate(classGenerator, methodGenerator);
            this._select.startIterator(classGenerator, methodGenerator);
        } else if (hasContents()) {
            InstructionList instructionList = methodGenerator.getInstructionList();
            compileResultTree(classGenerator, methodGenerator);
            this._domAdapter = methodGenerator.addLocalVariable2("@" + this._escapedName, Type.ResultTree.toJCType(), instructionList.getEnd());
            instructionList.append(DUP);
            instructionList.append(new ASTORE(this._domAdapter.getIndex()));
        } else {
            methodGenerator.getInstructionList().append(new PUSH(classGenerator.getConstantPool(), ""));
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (this._doParameterOptimization) {
            translateValue(classGenerator, methodGenerator);
            return;
        }
        String escape = Util.escape(getEscapedName());
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new PUSH(constantPool, escape));
        translateValue(classGenerator, methodGenerator);
        instructionList.append(new PUSH(constantPool, false));
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.TRANSLET_CLASS, Constants.ADD_PARAMETER, Constants.ADD_PARAMETER_SIG)));
        instructionList.append(POP);
    }

    public void releaseResultTree(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        if (this._domAdapter != null) {
            ConstantPoolGen constantPool = classGenerator.getConstantPool();
            InstructionList instructionList = methodGenerator.getInstructionList();
            if (classGenerator.getStylesheet().callsNodeset() && classGenerator.getDOMClass().equals(Constants.MULTI_DOM_CLASS)) {
                int addMethodref = constantPool.addMethodref(Constants.MULTI_DOM_CLASS, "removeDOMAdapter", "(Lohos.com.sun.org.apache.xalan.internal.xsltc.dom.DOMAdapter;)V");
                instructionList.append(methodGenerator.loadDOM());
                instructionList.append(new CHECKCAST(constantPool.addClass(Constants.MULTI_DOM_CLASS)));
                instructionList.append(new ALOAD(this._domAdapter.getIndex()));
                instructionList.append(new CHECKCAST(constantPool.addClass(Constants.DOM_ADAPTER_CLASS)));
                instructionList.append(new INVOKEVIRTUAL(addMethodref));
            }
            int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "release", "()V");
            instructionList.append(new ALOAD(this._domAdapter.getIndex()));
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 1));
            this._domAdapter.setEnd(instructionList.getEnd());
            methodGenerator.removeLocalVariable(this._domAdapter);
            this._domAdapter = null;
        }
    }
}
