package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.io.PrintStream;
import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.CHECKCAST;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.Instruction;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeSetType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ResultTreeType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xml.internal.utils.XML11Char;

/* access modifiers changed from: package-private */
public class VariableBase extends TopLevelElement {
    protected String _escapedName;
    protected boolean _ignore = false;
    protected boolean _isLocal;
    protected Instruction _loadInstruction;
    protected LocalVariableGen _local;
    protected QName _name;
    protected Vector<VariableRefBase> _refs = new Vector<>(2);
    protected Expression _select;
    protected Instruction _storeInstruction;
    protected Type _type;
    protected String select;

    VariableBase() {
    }

    public void disable() {
        this._ignore = true;
    }

    public void addReference(VariableRefBase variableRefBase) {
        this._refs.addElement(variableRefBase);
    }

    public void copyReferences(VariableBase variableBase) {
        int size = this._refs.size();
        for (int i = 0; i < size; i++) {
            variableBase.addReference(this._refs.get(i));
        }
    }

    public void mapRegister(MethodGenerator methodGenerator) {
        if (this._local == null) {
            this._local = methodGenerator.addLocalVariable2(getEscapedName(), this._type.toJCType(), methodGenerator.getInstructionList().getEnd());
        }
    }

    public void unmapRegister(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        if (this._local != null) {
            if (this._type instanceof ResultTreeType) {
                ConstantPoolGen constantPool = classGenerator.getConstantPool();
                InstructionList instructionList = methodGenerator.getInstructionList();
                if (classGenerator.getStylesheet().callsNodeset() && classGenerator.getDOMClass().equals(Constants.MULTI_DOM_CLASS)) {
                    int addMethodref = constantPool.addMethodref(Constants.MULTI_DOM_CLASS, "removeDOMAdapter", "(Lohos.com.sun.org.apache.xalan.internal.xsltc.dom.DOMAdapter;)V");
                    instructionList.append(methodGenerator.loadDOM());
                    instructionList.append(new CHECKCAST(constantPool.addClass(Constants.MULTI_DOM_CLASS)));
                    instructionList.append(loadInstruction());
                    instructionList.append(new CHECKCAST(constantPool.addClass(Constants.DOM_ADAPTER_CLASS)));
                    instructionList.append(new INVOKEVIRTUAL(addMethodref));
                }
                int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "release", "()V");
                instructionList.append(loadInstruction());
                instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 1));
            }
            this._local.setEnd(methodGenerator.getInstructionList().getEnd());
            methodGenerator.removeLocalVariable(this._local);
            this._refs = null;
            this._local = null;
        }
    }

    public Instruction loadInstruction() {
        if (this._loadInstruction == null) {
            this._loadInstruction = this._type.LOAD(this._local.getIndex());
        }
        return this._loadInstruction;
    }

    public Instruction storeInstruction() {
        if (this._storeInstruction == null) {
            this._storeInstruction = this._type.STORE(this._local.getIndex());
        }
        return this._storeInstruction;
    }

    public Expression getExpression() {
        return this._select;
    }

    public String toString() {
        return "variable(" + this._name + ")";
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        PrintStream printStream = System.out;
        printStream.println("Variable " + this._name);
        if (this._select != null) {
            indent(i + 4);
            PrintStream printStream2 = System.out;
            printStream2.println("select " + this._select.toString());
        }
        displayContents(i + 4);
    }

    public Type getType() {
        return this._type;
    }

    public QName getName() {
        return this._name;
    }

    public String getEscapedName() {
        return this._escapedName;
    }

    public void setName(QName qName) {
        this._name = qName;
        this._escapedName = Util.escape(qName.getStringRep());
    }

    public boolean isLocal() {
        return this._isLocal;
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
        VariableBase lookupVariable = parser.lookupVariable(this._name);
        if (lookupVariable != null && lookupVariable.getParent() == getParent()) {
            reportError(this, parser, ErrorMsg.VARIABLE_REDEF_ERR, attribute);
        }
        this.select = getAttribute(Constants.ATTRNAME_SELECT);
        if (this.select.length() > 0) {
            this._select = getParser().parseExpression(this, Constants.ATTRNAME_SELECT, null);
            if (this._select.isDummy()) {
                reportError(this, parser, ErrorMsg.REQUIRED_ATTR_ERR, Constants.ATTRNAME_SELECT);
                return;
            }
        }
        parseChildren(parser);
    }

    public void translateValue(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        Expression expression = this._select;
        if (expression != null) {
            expression.translate(classGenerator, methodGenerator);
            if (this._select.getType() instanceof NodeSetType) {
                ConstantPoolGen constantPool = classGenerator.getConstantPool();
                InstructionList instructionList = methodGenerator.getInstructionList();
                int addMethodref = constantPool.addMethodref(Constants.CACHED_NODE_LIST_ITERATOR_CLASS, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, "(Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;)V");
                instructionList.append(new NEW(constantPool.addClass(Constants.CACHED_NODE_LIST_ITERATOR_CLASS)));
                instructionList.append(DUP_X1);
                instructionList.append(SWAP);
                instructionList.append(new INVOKESPECIAL(addMethodref));
            }
            this._select.startIterator(classGenerator, methodGenerator);
        } else if (hasContents()) {
            compileResultTree(classGenerator, methodGenerator);
        } else {
            methodGenerator.getInstructionList().append(new PUSH(classGenerator.getConstantPool(), ""));
        }
    }
}
