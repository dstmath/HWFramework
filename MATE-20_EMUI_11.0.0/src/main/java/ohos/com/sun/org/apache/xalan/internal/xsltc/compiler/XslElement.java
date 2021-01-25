package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xml.internal.utils.XML11Char;

/* access modifiers changed from: package-private */
public final class XslElement extends Instruction {
    private boolean _ignore = false;
    private boolean _isLiteralName = true;
    private AttributeValueTemplate _name;
    private AttributeValueTemplate _namespace;
    private String _prefix;

    XslElement() {
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        Util.println("Element " + this._name);
        displayContents(i + 4);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        AttributeValueTemplate attributeValueTemplate;
        SymbolTable symbolTable = parser.getSymbolTable();
        String attribute = getAttribute("name");
        if (attribute == "") {
            parser.reportError(4, new ErrorMsg(ErrorMsg.ILLEGAL_ELEM_NAME_ERR, (Object) attribute, (SyntaxTreeNode) this));
            parseChildren(parser);
            this._ignore = true;
            return;
        }
        String attribute2 = getAttribute(Constants.ATTRNAME_NAMESPACE);
        this._isLiteralName = Util.isLiteral(attribute);
        if (!this._isLiteralName) {
            if (attribute2 == "") {
                attributeValueTemplate = null;
            } else {
                attributeValueTemplate = new AttributeValueTemplate(attribute2, parser, this);
            }
            this._namespace = attributeValueTemplate;
        } else if (!XML11Char.isXML11ValidQName(attribute)) {
            parser.reportError(4, new ErrorMsg(ErrorMsg.ILLEGAL_ELEM_NAME_ERR, (Object) attribute, (SyntaxTreeNode) this));
            parseChildren(parser);
            this._ignore = true;
            return;
        } else {
            QName qNameSafe = parser.getQNameSafe(attribute);
            String prefix = qNameSafe.getPrefix();
            String localPart = qNameSafe.getLocalPart();
            if (prefix == null) {
                prefix = "";
            }
            if (!hasAttribute(Constants.ATTRNAME_NAMESPACE)) {
                String lookupNamespace = lookupNamespace(prefix);
                if (lookupNamespace == null) {
                    parser.reportError(4, new ErrorMsg(ErrorMsg.NAMESPACE_UNDEF_ERR, (Object) prefix, (SyntaxTreeNode) this));
                    parseChildren(parser);
                    this._ignore = true;
                    return;
                }
                this._prefix = prefix;
                this._namespace = new AttributeValueTemplate(lookupNamespace, parser, this);
            } else {
                if (prefix == "") {
                    if (Util.isLiteral(attribute2)) {
                        String lookupPrefix = lookupPrefix(attribute2);
                        prefix = lookupPrefix == null ? symbolTable.generateNamespacePrefix() : lookupPrefix;
                    }
                    StringBuffer stringBuffer = new StringBuffer(prefix);
                    if (prefix != "") {
                        stringBuffer.append(':');
                    }
                    stringBuffer.append(localPart);
                    attribute = stringBuffer.toString();
                }
                this._prefix = prefix;
                this._namespace = new AttributeValueTemplate(attribute2, parser, this);
            }
        }
        this._name = new AttributeValueTemplate(attribute, parser, this);
        String attribute3 = getAttribute(Constants.ATTRNAME_USEATTRIBUTESETS);
        if (attribute3.length() > 0) {
            if (!Util.isValidQNames(attribute3)) {
                parser.reportError(3, new ErrorMsg("INVALID_QNAME_ERR", (Object) attribute3, (SyntaxTreeNode) this));
            }
            setFirstElement(new UseAttributeSets(attribute3, parser));
        }
        parseChildren(parser);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        if (!this._ignore) {
            this._name.typeCheck(symbolTable);
            AttributeValueTemplate attributeValueTemplate = this._namespace;
            if (attributeValueTemplate != null) {
                attributeValueTemplate.typeCheck(symbolTable);
            }
        }
        typeCheckContents(symbolTable);
        return Type.Void;
    }

    public void translateLiteral(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (!this._ignore) {
            instructionList.append(methodGenerator.loadHandler());
            this._name.translate(classGenerator, methodGenerator);
            instructionList.append(DUP2);
            instructionList.append(methodGenerator.startElement());
            if (this._namespace != null) {
                instructionList.append(methodGenerator.loadHandler());
                instructionList.append(new PUSH(constantPool, this._prefix));
                this._namespace.translate(classGenerator, methodGenerator);
                instructionList.append(methodGenerator.namespace());
            }
        }
        translateContents(classGenerator, methodGenerator);
        if (!this._ignore) {
            instructionList.append(methodGenerator.endElement());
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (this._isLiteralName) {
            translateLiteral(classGenerator, methodGenerator);
            return;
        }
        if (!this._ignore) {
            LocalVariableGen addLocalVariable2 = methodGenerator.addLocalVariable2("nameValue", Util.getJCRefType(Constants.STRING_SIG), null);
            this._name.translate(classGenerator, methodGenerator);
            addLocalVariable2.setStart(instructionList.append(new ASTORE(addLocalVariable2.getIndex())));
            instructionList.append(new ALOAD(addLocalVariable2.getIndex()));
            instructionList.append(new INVOKESTATIC(constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "checkQName", "(Ljava/lang/String;)V")));
            instructionList.append(methodGenerator.loadHandler());
            addLocalVariable2.setEnd(instructionList.append(new ALOAD(addLocalVariable2.getIndex())));
            AttributeValueTemplate attributeValueTemplate = this._namespace;
            if (attributeValueTemplate != null) {
                attributeValueTemplate.translate(classGenerator, methodGenerator);
            } else {
                instructionList.append(ACONST_NULL);
            }
            instructionList.append(methodGenerator.loadHandler());
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(methodGenerator.loadCurrentNode());
            instructionList.append(new INVOKESTATIC(constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "startXslElement", "(Ljava/lang/String;Ljava/lang/String;Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;I)Ljava/lang/String;")));
        }
        translateContents(classGenerator, methodGenerator);
        if (!this._ignore) {
            instructionList.append(methodGenerator.endElement());
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translateContents(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        int elementCount = elementCount();
        for (int i = 0; i < elementCount; i++) {
            SyntaxTreeNode syntaxTreeNode = getContents().get(i);
            if (!this._ignore || !(syntaxTreeNode instanceof XslAttribute)) {
                syntaxTreeNode.translate(classGenerator, methodGenerator);
            }
        }
    }
}
