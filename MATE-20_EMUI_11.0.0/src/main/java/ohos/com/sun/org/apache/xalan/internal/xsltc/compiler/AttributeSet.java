package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.Iterator;
import java.util.List;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.AttributeSetMethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xml.internal.utils.XML11Char;

/* access modifiers changed from: package-private */
public final class AttributeSet extends TopLevelElement {
    private static final String AttributeSetPrefix = "$as$";
    private boolean _ignore = false;
    private AttributeSet _mergeSet;
    private String _method;
    private QName _name;
    private UseAttributeSets _useSets;

    AttributeSet() {
    }

    public QName getName() {
        return this._name;
    }

    public String getMethodName() {
        return this._method;
    }

    public void ignore() {
        this._ignore = true;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        String attribute = getAttribute("name");
        if (!XML11Char.isXML11ValidQName(attribute)) {
            parser.reportError(3, new ErrorMsg("INVALID_QNAME_ERR", (Object) attribute, (SyntaxTreeNode) this));
        }
        this._name = parser.getQNameIgnoreDefaultNs(attribute);
        QName qName = this._name;
        if (qName == null || qName.equals("")) {
            parser.reportError(3, new ErrorMsg(ErrorMsg.UNNAMED_ATTRIBSET_ERR, (SyntaxTreeNode) this));
        }
        String attribute2 = getAttribute(Constants.ATTRNAME_USEATTRIBUTESETS);
        if (attribute2.length() > 0) {
            if (!Util.isValidQNames(attribute2)) {
                parser.reportError(3, new ErrorMsg("INVALID_QNAME_ERR", (Object) attribute2, (SyntaxTreeNode) this));
            }
            this._useSets = new UseAttributeSets(attribute2, parser);
        }
        List<SyntaxTreeNode> contents = getContents();
        int size = contents.size();
        for (int i = 0; i < size; i++) {
            SyntaxTreeNode syntaxTreeNode = contents.get(i);
            if (syntaxTreeNode instanceof XslAttribute) {
                parser.getSymbolTable().setCurrentNode(syntaxTreeNode);
                syntaxTreeNode.parseContents(parser);
            } else if (!(syntaxTreeNode instanceof Text)) {
                parser.reportError(3, new ErrorMsg(ErrorMsg.ILLEGAL_CHILD_ERR, (SyntaxTreeNode) this));
            }
        }
        parser.getSymbolTable().setCurrentNode(this);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        if (this._ignore) {
            return Type.Void;
        }
        this._mergeSet = symbolTable.addAttributeSet(this);
        this._method = AttributeSetPrefix + getXSLTC().nextAttributeSetSerial();
        UseAttributeSets useAttributeSets = this._useSets;
        if (useAttributeSets != null) {
            useAttributeSets.typeCheck(symbolTable);
        }
        typeCheckContents(symbolTable);
        return Type.Void;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        if (!this._ignore) {
            AttributeSetMethodGenerator attributeSetMethodGenerator = new AttributeSetMethodGenerator(this._method, classGenerator);
            if (this._mergeSet != null) {
                ConstantPoolGen constantPool = classGenerator.getConstantPool();
                InstructionList instructionList = attributeSetMethodGenerator.getInstructionList();
                String methodName = this._mergeSet.getMethodName();
                instructionList.append(classGenerator.loadTranslet());
                instructionList.append(attributeSetMethodGenerator.loadDOM());
                instructionList.append(attributeSetMethodGenerator.loadIterator());
                instructionList.append(attributeSetMethodGenerator.loadHandler());
                instructionList.append(attributeSetMethodGenerator.loadCurrentNode());
                instructionList.append(new INVOKESPECIAL(constantPool.addMethodref(classGenerator.getClassName(), methodName, Constants.ATTR_SET_SIG)));
            }
            UseAttributeSets useAttributeSets = this._useSets;
            if (useAttributeSets != null) {
                useAttributeSets.translate(classGenerator, attributeSetMethodGenerator);
            }
            Iterator<SyntaxTreeNode> elements = elements();
            while (elements.hasNext()) {
                SyntaxTreeNode next = elements.next();
                if (next instanceof XslAttribute) {
                    ((XslAttribute) next).translate(classGenerator, attributeSetMethodGenerator);
                }
            }
            attributeSetMethodGenerator.getInstructionList().append(RETURN);
            classGenerator.addMethod(attributeSetMethodGenerator);
        }
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer("attribute-set: ");
        Iterator<SyntaxTreeNode> elements = elements();
        while (elements.hasNext()) {
            stringBuffer.append((XslAttribute) elements.next());
        }
        return stringBuffer.toString();
    }
}
