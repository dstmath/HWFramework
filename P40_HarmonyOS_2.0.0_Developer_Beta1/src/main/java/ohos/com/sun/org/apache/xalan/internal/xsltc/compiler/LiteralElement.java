package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xml.internal.serializer.ElemDesc;
import ohos.com.sun.org.apache.xml.internal.serializer.ToHTMLStream;

/* access modifiers changed from: package-private */
public final class LiteralElement extends Instruction {
    private Map<String, String> _accessedPrefixes = null;
    private boolean _allAttributesUnique = false;
    private List<SyntaxTreeNode> _attributeElements = null;
    private LiteralElement _literalElemParent = null;
    private String _name;

    LiteralElement() {
    }

    public QName getName() {
        return this._qname;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        Util.println("LiteralElement name = " + this._name);
        displayContents(i + 4);
    }

    private String accessedNamespace(String str) {
        String accessedNamespace;
        LiteralElement literalElement = this._literalElemParent;
        if (literalElement != null && (accessedNamespace = literalElement.accessedNamespace(str)) != null) {
            return accessedNamespace;
        }
        Map<String, String> map = this._accessedPrefixes;
        if (map != null) {
            return map.get(str);
        }
        return null;
    }

    public void registerNamespace(String str, String str2, SymbolTable symbolTable, boolean z) {
        String str3;
        String accessedNamespace;
        LiteralElement literalElement = this._literalElemParent;
        if (literalElement == null || (accessedNamespace = literalElement.accessedNamespace(str)) == null || !accessedNamespace.equals(str2)) {
            Map<String, String> map = this._accessedPrefixes;
            if (map == null) {
                this._accessedPrefixes = new Hashtable();
            } else if (!z && (str3 = map.get(str)) != null) {
                if (!str3.equals(str2)) {
                    str = symbolTable.generateNamespacePrefix();
                } else {
                    return;
                }
            }
            if (!str.equals("xml")) {
                this._accessedPrefixes.put(str, str2);
            }
        }
    }

    private String translateQName(QName qName, SymbolTable symbolTable) {
        String localPart = qName.getLocalPart();
        String prefix = qName.getPrefix();
        if (prefix == null) {
            prefix = "";
        } else if (prefix.equals("xmlns")) {
            return "xmlns";
        }
        String lookupPrefixAlias = symbolTable.lookupPrefixAlias(prefix);
        if (lookupPrefixAlias != null) {
            symbolTable.excludeNamespaces(prefix);
            prefix = lookupPrefixAlias;
        }
        String lookupNamespace = lookupNamespace(prefix);
        if (lookupNamespace == null) {
            return localPart;
        }
        registerNamespace(prefix, lookupNamespace, symbolTable, false);
        if (prefix == "") {
            return localPart;
        }
        return prefix + ":" + localPart;
    }

    public void addAttribute(SyntaxTreeNode syntaxTreeNode) {
        if (this._attributeElements == null) {
            this._attributeElements = new ArrayList(2);
        }
        this._attributeElements.add(syntaxTreeNode);
    }

    public void setFirstAttribute(SyntaxTreeNode syntaxTreeNode) {
        if (this._attributeElements == null) {
            this._attributeElements = new ArrayList(2);
        }
        this._attributeElements.add(0, syntaxTreeNode);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        List<SyntaxTreeNode> list = this._attributeElements;
        if (list != null) {
            for (SyntaxTreeNode syntaxTreeNode : list) {
                syntaxTreeNode.typeCheck(symbolTable);
            }
        }
        typeCheckContents(symbolTable);
        return Type.Void;
    }

    public Set<Map.Entry<String, String>> getNamespaceScope(SyntaxTreeNode syntaxTreeNode) {
        HashMap hashMap = new HashMap();
        while (syntaxTreeNode != null) {
            Map<String, String> prefixMapping = syntaxTreeNode.getPrefixMapping();
            if (prefixMapping != null) {
                for (String str : prefixMapping.keySet()) {
                    if (!hashMap.containsKey(str)) {
                        hashMap.put(str, prefixMapping.get(str));
                    }
                }
            }
            syntaxTreeNode = syntaxTreeNode.getParent();
        }
        return hashMap.entrySet();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        String lookupNamespace;
        SymbolTable symbolTable = parser.getSymbolTable();
        symbolTable.setCurrentNode(this);
        SyntaxTreeNode parent = getParent();
        if (parent != null && (parent instanceof LiteralElement)) {
            this._literalElemParent = (LiteralElement) parent;
        }
        this._name = translateQName(this._qname, symbolTable);
        int length = this._attributes.getLength();
        for (int i = 0; i < length; i++) {
            QName qName = parser.getQName(this._attributes.getQName(i));
            String namespace = qName.getNamespace();
            String value = this._attributes.getValue(i);
            if (qName.equals(parser.getUseAttributeSets())) {
                if (!Util.isValidQNames(value)) {
                    parser.reportError(3, new ErrorMsg("INVALID_QNAME_ERR", (Object) value, (SyntaxTreeNode) this));
                }
                setFirstAttribute(new UseAttributeSets(value, parser));
            } else if (qName.equals(parser.getExtensionElementPrefixes())) {
                symbolTable.excludeNamespaces(value);
            } else if (qName.equals(parser.getExcludeResultPrefixes())) {
                symbolTable.excludeNamespaces(value);
            } else {
                String prefix = qName.getPrefix();
                if ((prefix == null || !prefix.equals("xmlns")) && ((prefix != null || !qName.getLocalPart().equals("xmlns")) && (namespace == null || !namespace.equals("http://www.w3.org/1999/XSL/Transform")))) {
                    LiteralAttribute literalAttribute = new LiteralAttribute(translateQName(qName, symbolTable), value, parser, this);
                    addAttribute(literalAttribute);
                    literalAttribute.setParent(this);
                    literalAttribute.parseContents(parser);
                }
            }
        }
        for (Map.Entry<String, String> entry : getNamespaceScope(this)) {
            String key = entry.getKey();
            if (!key.equals("xml") && (lookupNamespace = lookupNamespace(key)) != null && !symbolTable.isExcludedNamespace(lookupNamespace)) {
                registerNamespace(key, lookupNamespace, symbolTable, true);
            }
        }
        parseChildren(parser);
        for (int i2 = 0; i2 < length; i2++) {
            QName qName2 = parser.getQName(this._attributes.getQName(i2));
            String value2 = this._attributes.getValue(i2);
            if (qName2.equals(parser.getExtensionElementPrefixes())) {
                symbolTable.unExcludeNamespaces(value2);
            } else if (qName2.equals(parser.getExcludeResultPrefixes())) {
                symbolTable.unExcludeNamespaces(value2);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public boolean contextDependent() {
        return dependentContents();
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.Instruction, ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        this._allAttributesUnique = checkAttributesUnique();
        instructionList.append(methodGenerator.loadHandler());
        instructionList.append(new PUSH(constantPool, this._name));
        instructionList.append(DUP2);
        instructionList.append(methodGenerator.startElement());
        for (int i = 0; i < elementCount(); i++) {
            SyntaxTreeNode elementAt = elementAt(i);
            if (elementAt instanceof Variable) {
                elementAt.translate(classGenerator, methodGenerator);
            }
        }
        Map<String, String> map = this._accessedPrefixes;
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                instructionList.append(methodGenerator.loadHandler());
                instructionList.append(new PUSH(constantPool, entry.getKey()));
                instructionList.append(new PUSH(constantPool, entry.getValue()));
                instructionList.append(methodGenerator.namespace());
            }
        }
        List<SyntaxTreeNode> list = this._attributeElements;
        if (list != null) {
            for (SyntaxTreeNode syntaxTreeNode : list) {
                if (!(syntaxTreeNode instanceof XslAttribute)) {
                    syntaxTreeNode.translate(classGenerator, methodGenerator);
                }
            }
        }
        translateContents(classGenerator, methodGenerator);
        instructionList.append(methodGenerator.endElement());
    }

    private boolean isHTMLOutput() {
        return getStylesheet().getOutputMethod() == 2;
    }

    public ElemDesc getElemDesc() {
        if (isHTMLOutput()) {
            return ToHTMLStream.getElemDesc(this._name);
        }
        return null;
    }

    public boolean allAttributesUnique() {
        return this._allAttributesUnique;
    }

    private boolean checkAttributesUnique() {
        if (canProduceAttributeNodes(this, true)) {
            return false;
        }
        List<SyntaxTreeNode> list = this._attributeElements;
        if (list != null) {
            int size = list.size();
            HashMap hashMap = null;
            for (int i = 0; i < size; i++) {
                SyntaxTreeNode syntaxTreeNode = this._attributeElements.get(i);
                if (syntaxTreeNode instanceof UseAttributeSets) {
                    return false;
                }
                if (syntaxTreeNode instanceof XslAttribute) {
                    if (hashMap == null) {
                        hashMap = new HashMap();
                        for (int i2 = 0; i2 < i; i2++) {
                            SyntaxTreeNode syntaxTreeNode2 = this._attributeElements.get(i2);
                            if (syntaxTreeNode2 instanceof LiteralAttribute) {
                                LiteralAttribute literalAttribute = (LiteralAttribute) syntaxTreeNode2;
                                hashMap.put(literalAttribute.getName(), literalAttribute);
                            }
                        }
                    }
                    XslAttribute xslAttribute = (XslAttribute) syntaxTreeNode;
                    AttributeValue name = xslAttribute.getName();
                    if (name instanceof AttributeValueTemplate) {
                        return false;
                    }
                    if (!(name instanceof SimpleAttributeValue)) {
                        continue;
                    } else {
                        String simpleAttributeValue = ((SimpleAttributeValue) name).toString();
                        if (!(simpleAttributeValue == null || hashMap.get(simpleAttributeValue) == null)) {
                            return false;
                        }
                        if (simpleAttributeValue != null) {
                            hashMap.put(simpleAttributeValue, xslAttribute);
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean canProduceAttributeNodes(SyntaxTreeNode syntaxTreeNode, boolean z) {
        for (SyntaxTreeNode syntaxTreeNode2 : syntaxTreeNode.getContents()) {
            if (!(syntaxTreeNode2 instanceof Text)) {
                if ((syntaxTreeNode2 instanceof LiteralElement) || (syntaxTreeNode2 instanceof ValueOf) || (syntaxTreeNode2 instanceof XslElement) || (syntaxTreeNode2 instanceof Comment) || (syntaxTreeNode2 instanceof Number) || (syntaxTreeNode2 instanceof ProcessingInstruction)) {
                    break;
                } else if (syntaxTreeNode2 instanceof XslAttribute) {
                    if (!z) {
                        return true;
                    }
                } else if ((syntaxTreeNode2 instanceof CallTemplate) || (syntaxTreeNode2 instanceof ApplyTemplates) || (syntaxTreeNode2 instanceof Copy) || (syntaxTreeNode2 instanceof CopyOf) || (((syntaxTreeNode2 instanceof If) || (syntaxTreeNode2 instanceof ForEach)) && canProduceAttributeNodes(syntaxTreeNode2, false))) {
                    return true;
                } else {
                    if (syntaxTreeNode2 instanceof Choose) {
                        for (SyntaxTreeNode syntaxTreeNode3 : syntaxTreeNode2.getContents()) {
                            if (((syntaxTreeNode3 instanceof When) || (syntaxTreeNode3 instanceof Otherwise)) && canProduceAttributeNodes(syntaxTreeNode3, false)) {
                                return true;
                            }
                        }
                        continue;
                    } else {
                        continue;
                    }
                }
            } else if (!((Text) syntaxTreeNode2).isIgnore()) {
                return false;
            }
        }
        return false;
    }
}
