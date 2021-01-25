package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.List;
import ohos.com.sun.org.apache.bcel.internal.generic.ALOAD;
import ohos.com.sun.org.apache.bcel.internal.generic.ASTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GETFIELD;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
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
import ohos.com.sun.org.apache.xml.internal.serializer.ElemDesc;
import ohos.com.sun.org.apache.xml.internal.utils.XML11Char;

final class XslAttribute extends Instruction {
    private boolean _ignore = false;
    private boolean _isLiteral = false;
    private AttributeValue _name;
    private AttributeValueTemplate _namespace = null;
    private String _prefix;

    XslAttribute() {
    }

    public AttributeValue getName() {
        return this._name;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        Util.println("Attribute " + this._name);
        displayContents(i + 4);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        boolean z;
        SyntaxTreeNode syntaxTreeNode;
        SymbolTable symbolTable = parser.getSymbolTable();
        String attribute = getAttribute("name");
        String attribute2 = getAttribute(Constants.ATTRNAME_NAMESPACE);
        QName qName = parser.getQName(attribute, false);
        String prefix = qName.getPrefix();
        if ((prefix == null || !prefix.equals("xmlns")) && !attribute.equals("xmlns")) {
            this._isLiteral = Util.isLiteral(attribute);
            if (!this._isLiteral || XML11Char.isXML11ValidQName(attribute)) {
                SyntaxTreeNode parent = getParent();
                List<SyntaxTreeNode> contents = parent.getContents();
                int i = 0;
                while (i < parent.elementCount() && (syntaxTreeNode = contents.get(i)) != this) {
                    if (!(syntaxTreeNode instanceof XslAttribute) && !(syntaxTreeNode instanceof UseAttributeSets) && !(syntaxTreeNode instanceof LiteralAttribute) && !(syntaxTreeNode instanceof Text) && !(syntaxTreeNode instanceof If) && !(syntaxTreeNode instanceof Choose) && !(syntaxTreeNode instanceof CopyOf) && !(syntaxTreeNode instanceof VariableBase)) {
                        reportWarning(this, parser, "STRAY_ATTRIBUTE_ERR", attribute);
                    }
                    i++;
                }
                if (attribute2 != null && attribute2 != "") {
                    this._prefix = lookupPrefix(attribute2);
                    this._namespace = new AttributeValueTemplate(attribute2, parser, this);
                } else if (!(prefix == null || prefix == "")) {
                    this._prefix = prefix;
                    attribute2 = lookupNamespace(prefix);
                    if (attribute2 != null) {
                        this._namespace = new AttributeValueTemplate(attribute2, parser, this);
                    }
                }
                if (this._namespace != null) {
                    String str = this._prefix;
                    if (str == null || str == "") {
                        if (prefix != null) {
                            this._prefix = prefix;
                        } else {
                            this._prefix = symbolTable.generateNamespacePrefix();
                            z = true;
                            String str2 = this._prefix + ":" + qName.getLocalPart();
                            if ((parent instanceof LiteralElement) && !z) {
                                ((LiteralElement) parent).registerNamespace(this._prefix, attribute2, symbolTable, false);
                            }
                            attribute = str2;
                        }
                    } else if (prefix != null && !prefix.equals(str)) {
                        this._prefix = prefix;
                    }
                    z = false;
                    String str22 = this._prefix + ":" + qName.getLocalPart();
                    ((LiteralElement) parent).registerNamespace(this._prefix, attribute2, symbolTable, false);
                    attribute = str22;
                }
                if (parent instanceof LiteralElement) {
                    ((LiteralElement) parent).addAttribute(this);
                }
                this._name = AttributeValue.create(this, attribute, parser);
                parseChildren(parser);
                return;
            }
            reportError(this, parser, ErrorMsg.ILLEGAL_ATTR_NAME_ERR, attribute);
            return;
        }
        reportError(this, parser, ErrorMsg.ILLEGAL_ATTR_NAME_ERR, attribute);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        if (!this._ignore) {
            this._name.typeCheck(symbolTable);
            AttributeValueTemplate attributeValueTemplate = this._namespace;
            if (attributeValueTemplate != null) {
                attributeValueTemplate.typeCheck(symbolTable);
            }
            typeCheckContents(symbolTable);
        }
        return Type.Void;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        if (!this._ignore) {
            this._ignore = true;
            if (this._namespace != null) {
                instructionList.append(methodGenerator.loadHandler());
                instructionList.append(new PUSH(constantPool, this._prefix));
                this._namespace.translate(classGenerator, methodGenerator);
                instructionList.append(methodGenerator.namespace());
            }
            if (!this._isLiteral) {
                LocalVariableGen addLocalVariable2 = methodGenerator.addLocalVariable2("nameValue", Util.getJCRefType(Constants.STRING_SIG), null);
                this._name.translate(classGenerator, methodGenerator);
                addLocalVariable2.setStart(instructionList.append(new ASTORE(addLocalVariable2.getIndex())));
                instructionList.append(new ALOAD(addLocalVariable2.getIndex()));
                instructionList.append(new INVOKESTATIC(constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "checkAttribQName", "(Ljava/lang/String;)V")));
                instructionList.append(methodGenerator.loadHandler());
                instructionList.append(DUP);
                addLocalVariable2.setEnd(instructionList.append(new ALOAD(addLocalVariable2.getIndex())));
            } else {
                instructionList.append(methodGenerator.loadHandler());
                instructionList.append(DUP);
                this._name.translate(classGenerator, methodGenerator);
            }
            if (elementCount() != 1 || !(elementAt(0) instanceof Text)) {
                instructionList.append(classGenerator.loadTranslet());
                instructionList.append(new GETFIELD(constantPool.addFieldref(Constants.TRANSLET_CLASS, "stringValueHandler", Constants.STRING_VALUE_HANDLER_SIG)));
                instructionList.append(DUP);
                instructionList.append(methodGenerator.storeHandler());
                translateContents(classGenerator, methodGenerator);
                instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.STRING_VALUE_HANDLER, "getValue", "()Ljava/lang/String;")));
            } else {
                instructionList.append(new PUSH(constantPool, ((Text) elementAt(0)).getText()));
            }
            SyntaxTreeNode parent = getParent();
            if (parent instanceof LiteralElement) {
                LiteralElement literalElement = (LiteralElement) parent;
                if (literalElement.allAttributesUnique()) {
                    ElemDesc elemDesc = literalElement.getElemDesc();
                    int i = 2;
                    if (elemDesc != null) {
                        AttributeValue attributeValue = this._name;
                        if (attributeValue instanceof SimpleAttributeValue) {
                            String simpleAttributeValue = ((SimpleAttributeValue) attributeValue).toString();
                            if (!elemDesc.isAttrFlagSet(simpleAttributeValue, 4)) {
                                if (elemDesc.isAttrFlagSet(simpleAttributeValue, 2)) {
                                    i = 4;
                                }
                            }
                            instructionList.append(new PUSH(constantPool, i));
                            instructionList.append(methodGenerator.uniqueAttribute());
                            instructionList.append(methodGenerator.storeHandler());
                        }
                    }
                    i = 0;
                    instructionList.append(new PUSH(constantPool, i));
                    instructionList.append(methodGenerator.uniqueAttribute());
                    instructionList.append(methodGenerator.storeHandler());
                }
            }
            instructionList.append(methodGenerator.attribute());
            instructionList.append(methodGenerator.storeHandler());
        }
    }
}
