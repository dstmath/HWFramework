package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import ohos.com.sun.org.apache.bcel.internal.Constants;
import ohos.com.sun.org.apache.bcel.internal.generic.ANEWARRAY;
import ohos.com.sun.org.apache.bcel.internal.generic.BasicType;
import ohos.com.sun.org.apache.bcel.internal.generic.CHECKCAST;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.DUP_X1;
import ohos.com.sun.org.apache.bcel.internal.generic.GETFIELD;
import ohos.com.sun.org.apache.bcel.internal.generic.ICONST;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.bcel.internal.generic.NEWARRAY;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.helpers.AttributesImpl;

public abstract class SyntaxTreeNode implements Constants {
    protected static final SyntaxTreeNode Dummy = new AbsolutePathPattern(null);
    protected static final int IndentIncrement = 4;
    private static final char[] _spaces = "                                                       ".toCharArray();
    protected AttributesImpl _attributes;
    private final List<SyntaxTreeNode> _contents;
    private int _line;
    protected SyntaxTreeNode _parent;
    private Parser _parser;
    private Map<String, String> _prefixMapping;
    protected QName _qname;
    private Stylesheet _stylesheet;
    private Template _template;

    /* access modifiers changed from: protected */
    public boolean contextDependent() {
        return true;
    }

    public abstract void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator);

    public abstract Type typeCheck(SymbolTable symbolTable) throws TypeCheckError;

    public SyntaxTreeNode() {
        this._contents = new ArrayList(2);
        this._attributes = null;
        this._prefixMapping = null;
        this._line = 0;
        this._qname = null;
    }

    public SyntaxTreeNode(int i) {
        this._contents = new ArrayList(2);
        this._attributes = null;
        this._prefixMapping = null;
        this._line = i;
        this._qname = null;
    }

    public SyntaxTreeNode(String str, String str2, String str3) {
        this._contents = new ArrayList(2);
        this._attributes = null;
        this._prefixMapping = null;
        this._line = 0;
        setQName(str, str2, str3);
    }

    /* access modifiers changed from: protected */
    public final void setLineNumber(int i) {
        this._line = i;
    }

    public final int getLineNumber() {
        int i = this._line;
        if (i > 0) {
            return i;
        }
        SyntaxTreeNode parent = getParent();
        if (parent != null) {
            return parent.getLineNumber();
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public void setQName(QName qName) {
        this._qname = qName;
    }

    /* access modifiers changed from: protected */
    public void setQName(String str, String str2, String str3) {
        this._qname = new QName(str, str2, str3);
    }

    /* access modifiers changed from: protected */
    public QName getQName() {
        return this._qname;
    }

    /* access modifiers changed from: protected */
    public void setAttributes(AttributesImpl attributesImpl) {
        this._attributes = attributesImpl;
    }

    /* access modifiers changed from: protected */
    public String getAttribute(String str) {
        AttributesImpl attributesImpl = this._attributes;
        if (attributesImpl == null) {
            return "";
        }
        String value = attributesImpl.getValue(str);
        if (value == null || value.equals("")) {
            return "";
        }
        return value;
    }

    /* access modifiers changed from: protected */
    public String getAttribute(String str, String str2) {
        return getAttribute(str + ':' + str2);
    }

    /* access modifiers changed from: protected */
    public boolean hasAttribute(String str) {
        AttributesImpl attributesImpl = this._attributes;
        return (attributesImpl == null || attributesImpl.getValue(str) == null) ? false : true;
    }

    /* access modifiers changed from: protected */
    public void addAttribute(String str, String str2) {
        int index = this._attributes.getIndex(str);
        if (index != -1) {
            this._attributes.setAttribute(index, "", Util.getLocalName(str), str, "CDATA", str2);
        } else {
            this._attributes.addAttribute("", Util.getLocalName(str), str, "CDATA", str2);
        }
    }

    /* access modifiers changed from: protected */
    public Attributes getAttributes() {
        return this._attributes;
    }

    /* access modifiers changed from: protected */
    public void setPrefixMapping(Map<String, String> map) {
        this._prefixMapping = map;
    }

    /* access modifiers changed from: protected */
    public Map<String, String> getPrefixMapping() {
        return this._prefixMapping;
    }

    /* access modifiers changed from: protected */
    public void addPrefixMapping(String str, String str2) {
        if (this._prefixMapping == null) {
            this._prefixMapping = new HashMap();
        }
        this._prefixMapping.put(str, str2);
    }

    /* access modifiers changed from: protected */
    public String lookupNamespace(String str) {
        SyntaxTreeNode syntaxTreeNode;
        Map<String, String> map = this._prefixMapping;
        String str2 = map != null ? map.get(str) : null;
        if (str2 != null || (syntaxTreeNode = this._parent) == null) {
            return str2;
        }
        String lookupNamespace = syntaxTreeNode.lookupNamespace(str);
        if (str == "" && lookupNamespace == null) {
            return "";
        }
        return lookupNamespace;
    }

    /* access modifiers changed from: protected */
    public String lookupPrefix(String str) {
        Map<String, String> map = this._prefixMapping;
        if (map == null || !map.containsValue(str)) {
            SyntaxTreeNode syntaxTreeNode = this._parent;
            if (syntaxTreeNode == null) {
                return null;
            }
            String lookupPrefix = syntaxTreeNode.lookupPrefix(str);
            if (str == "" && lookupPrefix == null) {
                return "";
            }
            return lookupPrefix;
        }
        String str2 = null;
        for (Map.Entry<String, String> entry : this._prefixMapping.entrySet()) {
            str2 = entry.getKey();
            if (entry.getValue().equals(str)) {
                return str2;
            }
        }
        return str2;
    }

    /* access modifiers changed from: protected */
    public void setParser(Parser parser) {
        this._parser = parser;
    }

    public final Parser getParser() {
        return this._parser;
    }

    /* access modifiers changed from: protected */
    public void setParent(SyntaxTreeNode syntaxTreeNode) {
        if (this._parent == null) {
            this._parent = syntaxTreeNode;
        }
    }

    /* access modifiers changed from: protected */
    public final SyntaxTreeNode getParent() {
        return this._parent;
    }

    /* access modifiers changed from: protected */
    public final boolean isDummy() {
        return this == Dummy;
    }

    /* access modifiers changed from: protected */
    public int getImportPrecedence() {
        Stylesheet stylesheet = getStylesheet();
        if (stylesheet == null) {
            return Integer.MIN_VALUE;
        }
        return stylesheet.getImportPrecedence();
    }

    public Stylesheet getStylesheet() {
        if (this._stylesheet == null) {
            SyntaxTreeNode syntaxTreeNode = this;
            while (syntaxTreeNode != null) {
                if (syntaxTreeNode instanceof Stylesheet) {
                    return (Stylesheet) syntaxTreeNode;
                }
                syntaxTreeNode = syntaxTreeNode.getParent();
            }
            this._stylesheet = (Stylesheet) syntaxTreeNode;
        }
        return this._stylesheet;
    }

    /* access modifiers changed from: protected */
    public Template getTemplate() {
        if (this._template == null) {
            SyntaxTreeNode syntaxTreeNode = this;
            while (syntaxTreeNode != null && !(syntaxTreeNode instanceof Template)) {
                syntaxTreeNode = syntaxTreeNode.getParent();
            }
            this._template = (Template) syntaxTreeNode;
        }
        return this._template;
    }

    /* access modifiers changed from: protected */
    public final XSLTC getXSLTC() {
        return this._parser.getXSLTC();
    }

    /* access modifiers changed from: protected */
    public final SymbolTable getSymbolTable() {
        Parser parser = this._parser;
        if (parser == null) {
            return null;
        }
        return parser.getSymbolTable();
    }

    public void parseContents(Parser parser) {
        parseChildren(parser);
    }

    /* access modifiers changed from: protected */
    public final void parseChildren(Parser parser) {
        ArrayList<QName> arrayList = null;
        for (SyntaxTreeNode syntaxTreeNode : this._contents) {
            parser.getSymbolTable().setCurrentNode(syntaxTreeNode);
            syntaxTreeNode.parseContents(parser);
            QName updateScope = updateScope(parser, syntaxTreeNode);
            if (updateScope != null) {
                if (arrayList == null) {
                    arrayList = new ArrayList(2);
                }
                arrayList.add(updateScope);
            }
        }
        parser.getSymbolTable().setCurrentNode(this);
        if (arrayList != null) {
            for (QName qName : arrayList) {
                parser.removeVariable(qName);
            }
        }
    }

    /* access modifiers changed from: protected */
    public QName updateScope(Parser parser, SyntaxTreeNode syntaxTreeNode) {
        if (syntaxTreeNode instanceof Variable) {
            Variable variable = (Variable) syntaxTreeNode;
            parser.addVariable(variable);
            return variable.getName();
        } else if (!(syntaxTreeNode instanceof Param)) {
            return null;
        } else {
            Param param = (Param) syntaxTreeNode;
            parser.addParameter(param);
            return param.getName();
        }
    }

    /* access modifiers changed from: protected */
    public Type typeCheckContents(SymbolTable symbolTable) throws TypeCheckError {
        for (SyntaxTreeNode syntaxTreeNode : this._contents) {
            syntaxTreeNode.typeCheck(symbolTable);
        }
        return Type.Void;
    }

    /* access modifiers changed from: protected */
    public void translateContents(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        int elementCount = elementCount();
        for (SyntaxTreeNode syntaxTreeNode : this._contents) {
            methodGenerator.markChunkStart();
            syntaxTreeNode.translate(classGenerator, methodGenerator);
            methodGenerator.markChunkEnd();
        }
        for (int i = 0; i < elementCount; i++) {
            if (this._contents.get(i) instanceof VariableBase) {
                ((VariableBase) this._contents.get(i)).unmapRegister(classGenerator, methodGenerator);
            }
        }
    }

    private boolean isSimpleRTF(SyntaxTreeNode syntaxTreeNode) {
        for (SyntaxTreeNode syntaxTreeNode2 : syntaxTreeNode.getContents()) {
            if (!isTextElement(syntaxTreeNode2, false)) {
                return false;
            }
        }
        return true;
    }

    private boolean isAdaptiveRTF(SyntaxTreeNode syntaxTreeNode) {
        for (SyntaxTreeNode syntaxTreeNode2 : syntaxTreeNode.getContents()) {
            if (!isTextElement(syntaxTreeNode2, true)) {
                return false;
            }
        }
        return true;
    }

    private boolean isTextElement(SyntaxTreeNode syntaxTreeNode, boolean z) {
        if ((syntaxTreeNode instanceof ValueOf) || (syntaxTreeNode instanceof Number) || (syntaxTreeNode instanceof Text)) {
            return true;
        }
        if (syntaxTreeNode instanceof If) {
            return z ? isAdaptiveRTF(syntaxTreeNode) : isSimpleRTF(syntaxTreeNode);
        }
        if (syntaxTreeNode instanceof Choose) {
            for (SyntaxTreeNode syntaxTreeNode2 : syntaxTreeNode.getContents()) {
                if (!(syntaxTreeNode2 instanceof Text) && (!((syntaxTreeNode2 instanceof When) || (syntaxTreeNode2 instanceof Otherwise)) || (!(z && isAdaptiveRTF(syntaxTreeNode2)) && (z || !isSimpleRTF(syntaxTreeNode2))))) {
                    return false;
                }
            }
            return true;
        } else if (!z || (!(syntaxTreeNode instanceof CallTemplate) && !(syntaxTreeNode instanceof ApplyTemplates))) {
            return false;
        } else {
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void compileResultTree(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = methodGenerator.getInstructionList();
        Stylesheet stylesheet = classGenerator.getStylesheet();
        boolean isSimpleRTF = isSimpleRTF(this);
        int i = isSimpleRTF ? 0 : !isSimpleRTF ? isAdaptiveRTF(this) : false ? 1 : 2;
        instructionList.append(methodGenerator.loadHandler());
        String dOMClass = classGenerator.getDOMClass();
        instructionList.append(methodGenerator.loadDOM());
        int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getResultTreeFrag", "(IIZ)Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;");
        instructionList.append(new PUSH(constantPool, 32));
        instructionList.append(new PUSH(constantPool, i));
        instructionList.append(new PUSH(constantPool, stylesheet.callsNodeset()));
        instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 4));
        instructionList.append(DUP);
        instructionList.append(new INVOKEINTERFACE(constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getOutputDomBuilder", "()Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;"), 1));
        instructionList.append(DUP);
        instructionList.append(methodGenerator.storeHandler());
        instructionList.append(methodGenerator.startDocument());
        translateContents(classGenerator, methodGenerator);
        instructionList.append(methodGenerator.loadHandler());
        instructionList.append(methodGenerator.endDocument());
        if (stylesheet.callsNodeset() && !dOMClass.equals("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM")) {
            int addMethodref = constantPool.addMethodref(Constants.DOM_ADAPTER_CLASS, Constants.CONSTRUCTOR_NAME, "(Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;[Ljava/lang/String;[Ljava/lang/String;[I[Ljava/lang/String;)V");
            instructionList.append(new NEW(constantPool.addClass(Constants.DOM_ADAPTER_CLASS)));
            instructionList.append(new DUP_X1());
            instructionList.append(SWAP);
            if (!stylesheet.callsNodeset()) {
                instructionList.append(new ICONST(0));
                instructionList.append(new ANEWARRAY(constantPool.addClass("java.lang.String")));
                instructionList.append(DUP);
                instructionList.append(DUP);
                instructionList.append(new ICONST(0));
                instructionList.append(new NEWARRAY(BasicType.INT));
                instructionList.append(SWAP);
                instructionList.append(new INVOKESPECIAL(addMethodref));
            } else {
                instructionList.append(ALOAD_0);
                instructionList.append(new GETFIELD(constantPool.addFieldref(Constants.TRANSLET_CLASS, Constants.NAMES_INDEX, "[Ljava/lang/String;")));
                instructionList.append(ALOAD_0);
                instructionList.append(new GETFIELD(constantPool.addFieldref(Constants.TRANSLET_CLASS, Constants.URIS_INDEX, "[Ljava/lang/String;")));
                instructionList.append(ALOAD_0);
                instructionList.append(new GETFIELD(constantPool.addFieldref(Constants.TRANSLET_CLASS, Constants.TYPES_INDEX, Constants.TYPES_INDEX_SIG)));
                instructionList.append(ALOAD_0);
                instructionList.append(new GETFIELD(constantPool.addFieldref(Constants.TRANSLET_CLASS, Constants.NAMESPACE_INDEX, "[Ljava/lang/String;")));
                instructionList.append(new INVOKESPECIAL(addMethodref));
                instructionList.append(DUP);
                instructionList.append(methodGenerator.loadDOM());
                instructionList.append(new CHECKCAST(constantPool.addClass(classGenerator.getDOMClass())));
                instructionList.append(SWAP);
                instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.MULTI_DOM_CLASS, "addDOMAdapter", "(Lohos.com.sun.org.apache.xalan.internal.xsltc.dom.DOMAdapter;)I")));
                instructionList.append(POP);
            }
        }
        instructionList.append(SWAP);
        instructionList.append(methodGenerator.storeHandler());
    }

    /* access modifiers changed from: protected */
    public boolean dependentContents() {
        for (SyntaxTreeNode syntaxTreeNode : this._contents) {
            if (syntaxTreeNode.contextDependent()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public final void addElement(SyntaxTreeNode syntaxTreeNode) {
        this._contents.add(syntaxTreeNode);
        syntaxTreeNode.setParent(this);
    }

    /* access modifiers changed from: protected */
    public final void setFirstElement(SyntaxTreeNode syntaxTreeNode) {
        this._contents.add(0, syntaxTreeNode);
        syntaxTreeNode.setParent(this);
    }

    /* access modifiers changed from: protected */
    public final void removeElement(SyntaxTreeNode syntaxTreeNode) {
        this._contents.remove(syntaxTreeNode);
        syntaxTreeNode.setParent(null);
    }

    /* access modifiers changed from: protected */
    public final List<SyntaxTreeNode> getContents() {
        return this._contents;
    }

    /* access modifiers changed from: protected */
    public final boolean hasContents() {
        return elementCount() > 0;
    }

    /* access modifiers changed from: protected */
    public final int elementCount() {
        return this._contents.size();
    }

    /* access modifiers changed from: protected */
    public final Iterator<SyntaxTreeNode> elements() {
        return this._contents.iterator();
    }

    /* access modifiers changed from: protected */
    public final SyntaxTreeNode elementAt(int i) {
        return this._contents.get(i);
    }

    /* access modifiers changed from: protected */
    public final SyntaxTreeNode lastChild() {
        if (this._contents.isEmpty()) {
            return null;
        }
        List<SyntaxTreeNode> list = this._contents;
        return list.get(list.size() - 1);
    }

    public void display(int i) {
        displayContents(i);
    }

    /* access modifiers changed from: protected */
    public void displayContents(int i) {
        for (SyntaxTreeNode syntaxTreeNode : this._contents) {
            syntaxTreeNode.display(i);
        }
    }

    /* access modifiers changed from: protected */
    public final void indent(int i) {
        System.out.print(new String(_spaces, 0, i));
    }

    /* access modifiers changed from: protected */
    public void reportError(SyntaxTreeNode syntaxTreeNode, Parser parser, String str, String str2) {
        parser.reportError(3, new ErrorMsg(str, (Object) str2, syntaxTreeNode));
    }

    /* access modifiers changed from: protected */
    public void reportWarning(SyntaxTreeNode syntaxTreeNode, Parser parser, String str, String str2) {
        parser.reportError(4, new ErrorMsg(str, (Object) str2, syntaxTreeNode));
    }
}
