package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import ohos.com.sun.org.apache.bcel.internal.generic.ANEWARRAY;
import ohos.com.sun.org.apache.bcel.internal.generic.BasicType;
import ohos.com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import ohos.com.sun.org.apache.bcel.internal.generic.FieldGen;
import ohos.com.sun.org.apache.bcel.internal.generic.GETFIELD;
import ohos.com.sun.org.apache.bcel.internal.generic.GETSTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL;
import ohos.com.sun.org.apache.bcel.internal.generic.ISTORE;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import ohos.com.sun.org.apache.bcel.internal.generic.InstructionList;
import ohos.com.sun.org.apache.bcel.internal.generic.LocalVariableGen;
import ohos.com.sun.org.apache.bcel.internal.generic.NEW;
import ohos.com.sun.org.apache.bcel.internal.generic.NEWARRAY;
import ohos.com.sun.org.apache.bcel.internal.generic.PUSH;
import ohos.com.sun.org.apache.bcel.internal.generic.PUTFIELD;
import ohos.com.sun.org.apache.bcel.internal.generic.PUTSTATIC;
import ohos.com.sun.org.apache.bcel.internal.generic.TargetLostException;
import ohos.com.sun.org.apache.bcel.internal.util.InstructionFinder;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import ohos.com.sun.org.apache.xml.internal.utils.SystemIDResolver;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;

public final class Stylesheet extends SyntaxTreeNode {
    public static final int HTML_OUTPUT = 2;
    public static final int TEXT_OUTPUT = 3;
    public static final int UNKNOWN_OUTPUT = 0;
    public static final int XML_OUTPUT = 1;
    private Vector _allValidTemplates = null;
    private boolean _callsNodeset = false;
    private String _className;
    private Mode _defaultMode;
    private final Map<String, String> _extensions = new HashMap();
    private Vector _globals = new Vector();
    private boolean _hasIdCall = false;
    private Boolean _hasLocalParams = null;
    private int _importPrecedence = 1;
    public Stylesheet _importedFrom = null;
    public Stylesheet _includedFrom = null;
    private Vector _includedStylesheets = null;
    private Map<String, Key> _keys = new HashMap();
    private Output _lastOutputElement = null;
    private SourceLoader _loader = null;
    private int _minimumDescendantPrecedence = -1;
    private final Map<String, Mode> _modes = new HashMap();
    private boolean _multiDocument = false;
    private QName _name;
    private int _nextModeSerial = 1;
    private boolean _numberFormattingUsed = false;
    private int _outputMethod = 0;
    private Properties _outputProperties = null;
    private Stylesheet _parentStylesheet;
    private boolean _simplified = false;
    private String _systemId;
    private boolean _templateInlining = false;
    private final Vector _templates = new Vector();
    private String _version;

    public int getOutputMethod() {
        return this._outputMethod;
    }

    private void checkOutputMethod() {
        String outputMethod;
        Output output = this._lastOutputElement;
        if (output != null && (outputMethod = output.getOutputMethod()) != null) {
            if (outputMethod.equals("xml")) {
                this._outputMethod = 1;
            } else if (outputMethod.equals("html")) {
                this._outputMethod = 2;
            } else if (outputMethod.equals("text")) {
                this._outputMethod = 3;
            }
        }
    }

    public boolean getTemplateInlining() {
        return this._templateInlining;
    }

    public void setTemplateInlining(boolean z) {
        this._templateInlining = z;
    }

    public boolean isSimplified() {
        return this._simplified;
    }

    public void setSimplified() {
        this._simplified = true;
    }

    public void setHasIdCall(boolean z) {
        this._hasIdCall = z;
    }

    public void setOutputProperty(String str, String str2) {
        if (this._outputProperties == null) {
            this._outputProperties = new Properties();
        }
        this._outputProperties.setProperty(str, str2);
    }

    public void setOutputProperties(Properties properties) {
        this._outputProperties = properties;
    }

    public Properties getOutputProperties() {
        return this._outputProperties;
    }

    public Output getLastOutputElement() {
        return this._lastOutputElement;
    }

    public void setMultiDocument(boolean z) {
        this._multiDocument = z;
    }

    public boolean isMultiDocument() {
        return this._multiDocument;
    }

    public void setCallsNodeset(boolean z) {
        if (z) {
            setMultiDocument(z);
        }
        this._callsNodeset = z;
    }

    public boolean callsNodeset() {
        return this._callsNodeset;
    }

    public void numberFormattingUsed() {
        this._numberFormattingUsed = true;
        Stylesheet parentStylesheet = getParentStylesheet();
        if (parentStylesheet != null) {
            parentStylesheet.numberFormattingUsed();
        }
    }

    public void setImportPrecedence(int i) {
        Stylesheet includedStylesheet;
        this._importPrecedence = i;
        Iterator<SyntaxTreeNode> elements = elements();
        while (elements.hasNext()) {
            SyntaxTreeNode next = elements.next();
            if ((next instanceof Include) && (includedStylesheet = ((Include) next).getIncludedStylesheet()) != null && includedStylesheet._includedFrom == this) {
                includedStylesheet.setImportPrecedence(i);
            }
        }
        Stylesheet stylesheet = this._importedFrom;
        if (stylesheet == null) {
            Stylesheet stylesheet2 = this._includedFrom;
            if (stylesheet2 != null && stylesheet2.getImportPrecedence() != i) {
                this._includedFrom.setImportPrecedence(i);
            }
        } else if (stylesheet.getImportPrecedence() < i) {
            this._importedFrom.setImportPrecedence(getParser().getNextImportPrecedence());
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public int getImportPrecedence() {
        return this._importPrecedence;
    }

    public int getMinimumDescendantPrecedence() {
        if (this._minimumDescendantPrecedence == -1) {
            int importPrecedence = getImportPrecedence();
            Vector vector = this._includedStylesheets;
            int size = vector != null ? vector.size() : 0;
            for (int i = 0; i < size; i++) {
                int minimumDescendantPrecedence = ((Stylesheet) this._includedStylesheets.elementAt(i)).getMinimumDescendantPrecedence();
                if (minimumDescendantPrecedence < importPrecedence) {
                    importPrecedence = minimumDescendantPrecedence;
                }
            }
            this._minimumDescendantPrecedence = importPrecedence;
        }
        return this._minimumDescendantPrecedence;
    }

    public boolean checkForLoop(String str) {
        String str2 = this._systemId;
        if (str2 != null && str2.equals(str)) {
            return true;
        }
        Stylesheet stylesheet = this._parentStylesheet;
        if (stylesheet != null) {
            return stylesheet.checkForLoop(str);
        }
        return false;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void setParser(Parser parser) {
        super.setParser(parser);
        this._name = makeStylesheetName("__stylesheet_");
    }

    public void setParentStylesheet(Stylesheet stylesheet) {
        this._parentStylesheet = stylesheet;
    }

    public Stylesheet getParentStylesheet() {
        return this._parentStylesheet;
    }

    public void setImportingStylesheet(Stylesheet stylesheet) {
        this._importedFrom = stylesheet;
        stylesheet.addIncludedStylesheet(this);
    }

    public void setIncludingStylesheet(Stylesheet stylesheet) {
        this._includedFrom = stylesheet;
        stylesheet.addIncludedStylesheet(this);
    }

    public void addIncludedStylesheet(Stylesheet stylesheet) {
        if (this._includedStylesheets == null) {
            this._includedStylesheets = new Vector();
        }
        this._includedStylesheets.addElement(stylesheet);
    }

    public void setSystemId(String str) {
        if (str != null) {
            this._systemId = SystemIDResolver.getAbsoluteURI(str);
        }
    }

    public String getSystemId() {
        return this._systemId;
    }

    public void setSourceLoader(SourceLoader sourceLoader) {
        this._loader = sourceLoader;
    }

    public SourceLoader getSourceLoader() {
        return this._loader;
    }

    private QName makeStylesheetName(String str) {
        Parser parser = getParser();
        return parser.getQName(str + getXSLTC().nextStylesheetSerial());
    }

    public boolean hasGlobals() {
        return this._globals.size() > 0;
    }

    public boolean hasLocalParams() {
        Boolean bool = this._hasLocalParams;
        if (bool != null) {
            return bool.booleanValue();
        }
        Vector allValidTemplates = getAllValidTemplates();
        int size = allValidTemplates.size();
        for (int i = 0; i < size; i++) {
            if (((Template) allValidTemplates.elementAt(i)).hasParams()) {
                this._hasLocalParams = Boolean.TRUE;
                return true;
            }
        }
        this._hasLocalParams = Boolean.FALSE;
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void addPrefixMapping(String str, String str2) {
        if (!str.equals("") || !str2.equals("http://www.w3.org/1999/xhtml")) {
            super.addPrefixMapping(str, str2);
        }
    }

    private void extensionURI(String str, SymbolTable symbolTable) {
        if (str != null) {
            StringTokenizer stringTokenizer = new StringTokenizer(str);
            while (stringTokenizer.hasMoreTokens()) {
                String nextToken = stringTokenizer.nextToken();
                String lookupNamespace = lookupNamespace(nextToken);
                if (lookupNamespace != null) {
                    this._extensions.put(lookupNamespace, nextToken);
                }
            }
        }
    }

    public boolean isExtension(String str) {
        return this._extensions.get(str) != null;
    }

    public void declareExtensionPrefixes(Parser parser) {
        extensionURI(getAttribute(Constants.ATTRNAME_EXTENSIONELEMENTPREFIXES), parser.getSymbolTable());
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void parseContents(Parser parser) {
        SymbolTable symbolTable = parser.getSymbolTable();
        addPrefixMapping("xml", "http://www.w3.org/XML/1998/namespace");
        if (symbolTable.addStylesheet(this._name, this) != null) {
            parser.reportError(3, new ErrorMsg(ErrorMsg.MULTIPLE_STYLESHEET_ERR, (SyntaxTreeNode) this));
        }
        if (this._simplified) {
            symbolTable.excludeURI("http://www.w3.org/1999/XSL/Transform");
            new Template().parseSimplified(this, parser);
            return;
        }
        parseOwnChildren(parser);
    }

    public final void parseOwnChildren(Parser parser) {
        SymbolTable symbolTable = parser.getSymbolTable();
        String attribute = getAttribute(Constants.ATTRNAME_EXCLUDE_RESULT_PREFIXES);
        String attribute2 = getAttribute(Constants.ATTRNAME_EXTENSIONELEMENTPREFIXES);
        symbolTable.pushExcludedNamespacesContext();
        symbolTable.excludeURI("http://www.w3.org/1999/XSL/Transform");
        symbolTable.excludeNamespaces(attribute);
        symbolTable.excludeNamespaces(attribute2);
        List<SyntaxTreeNode> contents = getContents();
        int size = contents.size();
        for (int i = 0; i < size; i++) {
            SyntaxTreeNode syntaxTreeNode = contents.get(i);
            if ((syntaxTreeNode instanceof VariableBase) || (syntaxTreeNode instanceof NamespaceAlias)) {
                parser.getSymbolTable().setCurrentNode(syntaxTreeNode);
                syntaxTreeNode.parseContents(parser);
            }
        }
        for (int i2 = 0; i2 < size; i2++) {
            SyntaxTreeNode syntaxTreeNode2 = contents.get(i2);
            if (!(syntaxTreeNode2 instanceof VariableBase) && !(syntaxTreeNode2 instanceof NamespaceAlias)) {
                parser.getSymbolTable().setCurrentNode(syntaxTreeNode2);
                syntaxTreeNode2.parseContents(parser);
            }
            if (!this._templateInlining && (syntaxTreeNode2 instanceof Template)) {
                Template template = (Template) syntaxTreeNode2;
                template.setName(parser.getQName("template$dot$" + template.getPosition()));
            }
        }
        symbolTable.popExcludedNamespacesContext();
    }

    public void processModes() {
        if (this._defaultMode == null) {
            this._defaultMode = new Mode(null, this, "");
        }
        this._defaultMode.processPatterns(this._keys);
        for (Mode mode : this._modes.values()) {
            mode.processPatterns(this._keys);
        }
    }

    private void compileModes(ClassGenerator classGenerator) {
        this._defaultMode.compileApplyTemplates(classGenerator);
        for (Mode mode : this._modes.values()) {
            mode.compileApplyTemplates(classGenerator);
        }
    }

    public Mode getMode(QName qName) {
        if (qName == null) {
            if (this._defaultMode == null) {
                this._defaultMode = new Mode(null, this, "");
            }
            return this._defaultMode;
        }
        Mode mode = this._modes.get(qName.getStringRep());
        if (mode != null) {
            return mode;
        }
        int i = this._nextModeSerial;
        this._nextModeSerial = i + 1;
        String num = Integer.toString(i);
        Map<String, Mode> map = this._modes;
        String stringRep = qName.getStringRep();
        Mode mode2 = new Mode(qName, this, num);
        map.put(stringRep, mode2);
        return mode2;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public Type typeCheck(SymbolTable symbolTable) throws TypeCheckError {
        int size = this._globals.size();
        for (int i = 0; i < size; i++) {
            ((VariableBase) this._globals.elementAt(i)).typeCheck(symbolTable);
        }
        return typeCheckContents(symbolTable);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void translate(ClassGenerator classGenerator, MethodGenerator methodGenerator) {
        translate();
    }

    private void addDOMField(ClassGenerator classGenerator) {
        classGenerator.addField(new FieldGen(1, Util.getJCRefType(Constants.DOM_INTF_SIG), Constants.DOM_FIELD, classGenerator.getConstantPool()).getField());
    }

    private void addStaticField(ClassGenerator classGenerator, String str, String str2) {
        classGenerator.addField(new FieldGen(12, Util.getJCRefType(str), str2, classGenerator.getConstantPool()).getField());
    }

    public void translate() {
        this._className = getXSLTC().getClassName();
        ClassGenerator classGenerator = new ClassGenerator(this._className, Constants.TRANSLET_CLASS, "", 33, null, this);
        addDOMField(classGenerator);
        compileTransform(classGenerator);
        Iterator<SyntaxTreeNode> elements = elements();
        while (elements.hasNext()) {
            SyntaxTreeNode next = elements.next();
            if (next instanceof Template) {
                Template template = (Template) next;
                getMode(template.getModeName()).addTemplate(template);
            } else if (next instanceof AttributeSet) {
                ((AttributeSet) next).translate(classGenerator, null);
            } else if (next instanceof Output) {
                Output output = (Output) next;
                if (output.enabled()) {
                    this._lastOutputElement = output;
                }
            }
        }
        checkOutputMethod();
        processModes();
        compileModes(classGenerator);
        compileStaticInitializer(classGenerator);
        compileConstructor(classGenerator, this._lastOutputElement);
        if (!getParser().errorsFound()) {
            getXSLTC().dumpClass(classGenerator.getJavaClass());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x00b8  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x00bb  */
    private void compileStaticInitializer(ClassGenerator classGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = new InstructionList();
        MethodGenerator methodGenerator = new MethodGenerator(9, ohos.com.sun.org.apache.bcel.internal.generic.Type.VOID, null, null, ohos.com.sun.org.apache.bcel.internal.Constants.STATIC_INITIALIZER_NAME, this._className, instructionList, constantPool);
        addStaticField(classGenerator, "[Ljava/lang/String;", Constants.STATIC_NAMES_ARRAY_FIELD);
        addStaticField(classGenerator, "[Ljava/lang/String;", Constants.STATIC_URIS_ARRAY_FIELD);
        String str = Constants.STATIC_TYPES_ARRAY_FIELD;
        String str2 = Constants.TYPES_INDEX_SIG;
        addStaticField(classGenerator, str2, str);
        String str3 = Constants.STATIC_NAMESPACE_ARRAY_FIELD;
        addStaticField(classGenerator, "[Ljava/lang/String;", str3);
        int characterDataCount = getXSLTC().getCharacterDataCount();
        for (int i = 0; i < characterDataCount; i++) {
            addStaticField(classGenerator, Constants.STATIC_CHAR_DATA_FIELD_SIG, Constants.STATIC_CHAR_DATA_FIELD + i);
        }
        Vector namesIndex = getXSLTC().getNamesIndex();
        int size = namesIndex.size();
        String[] strArr = new String[size];
        String[] strArr2 = new String[size];
        int[] iArr = new int[size];
        int i2 = 0;
        while (i2 < size) {
            String str4 = (String) namesIndex.elementAt(i2);
            int lastIndexOf = str4.lastIndexOf(58);
            if (lastIndexOf > -1) {
                strArr2[i2] = str4.substring(0, lastIndexOf);
            }
            int i3 = lastIndexOf + 1;
            if (str4.charAt(i3) == '@') {
                iArr[i2] = 2;
            } else if (str4.charAt(i3) == '?') {
                iArr[i2] = 13;
            } else {
                iArr[i2] = 1;
                if (i3 != 0) {
                    strArr[i2] = str4;
                } else {
                    strArr[i2] = str4.substring(i3);
                }
                i2++;
                str3 = str3;
                namesIndex = namesIndex;
                str = str;
                str2 = str2;
            }
            i3++;
            if (i3 != 0) {
            }
            i2++;
            str3 = str3;
            namesIndex = namesIndex;
            str = str;
            str2 = str2;
        }
        methodGenerator.markChunkStart();
        instructionList.append(new PUSH(constantPool, size));
        instructionList.append(new ANEWARRAY(constantPool.addClass("java.lang.String")));
        int addFieldref = constantPool.addFieldref(this._className, Constants.STATIC_NAMES_ARRAY_FIELD, "[Ljava/lang/String;");
        instructionList.append(new PUTSTATIC(addFieldref));
        methodGenerator.markChunkEnd();
        for (int i4 = 0; i4 < size; i4++) {
            String str5 = strArr[i4];
            methodGenerator.markChunkStart();
            instructionList.append(new GETSTATIC(addFieldref));
            instructionList.append(new PUSH(constantPool, i4));
            instructionList.append(new PUSH(constantPool, str5));
            instructionList.append(AASTORE);
            methodGenerator.markChunkEnd();
        }
        methodGenerator.markChunkStart();
        instructionList.append(new PUSH(constantPool, size));
        instructionList.append(new ANEWARRAY(constantPool.addClass("java.lang.String")));
        int addFieldref2 = constantPool.addFieldref(this._className, Constants.STATIC_URIS_ARRAY_FIELD, "[Ljava/lang/String;");
        instructionList.append(new PUTSTATIC(addFieldref2));
        methodGenerator.markChunkEnd();
        for (int i5 = 0; i5 < size; i5++) {
            String str6 = strArr2[i5];
            methodGenerator.markChunkStart();
            instructionList.append(new GETSTATIC(addFieldref2));
            instructionList.append(new PUSH(constantPool, i5));
            instructionList.append(new PUSH(constantPool, str6));
            instructionList.append(AASTORE);
            methodGenerator.markChunkEnd();
        }
        methodGenerator.markChunkStart();
        instructionList.append(new PUSH(constantPool, size));
        instructionList.append(new NEWARRAY(BasicType.INT));
        int addFieldref3 = constantPool.addFieldref(this._className, str, str2);
        instructionList.append(new PUTSTATIC(addFieldref3));
        methodGenerator.markChunkEnd();
        for (int i6 = 0; i6 < size; i6++) {
            int i7 = iArr[i6];
            methodGenerator.markChunkStart();
            instructionList.append(new GETSTATIC(addFieldref3));
            instructionList.append(new PUSH(constantPool, i6));
            instructionList.append(new PUSH(constantPool, i7));
            instructionList.append(IASTORE);
        }
        Vector namespaceIndex = getXSLTC().getNamespaceIndex();
        methodGenerator.markChunkStart();
        instructionList.append(new PUSH(constantPool, namespaceIndex.size()));
        instructionList.append(new ANEWARRAY(constantPool.addClass("java.lang.String")));
        int addFieldref4 = constantPool.addFieldref(this._className, str3, "[Ljava/lang/String;");
        instructionList.append(new PUTSTATIC(addFieldref4));
        methodGenerator.markChunkEnd();
        for (int i8 = 0; i8 < namespaceIndex.size(); i8++) {
            methodGenerator.markChunkStart();
            instructionList.append(new GETSTATIC(addFieldref4));
            instructionList.append(new PUSH(constantPool, i8));
            instructionList.append(new PUSH(constantPool, (String) namespaceIndex.elementAt(i8)));
            instructionList.append(AASTORE);
            methodGenerator.markChunkEnd();
        }
        int characterDataCount2 = getXSLTC().getCharacterDataCount();
        int addMethodref = constantPool.addMethodref("java.lang.String", "toCharArray", "()[C");
        for (int i9 = 0; i9 < characterDataCount2; i9++) {
            methodGenerator.markChunkStart();
            instructionList.append(new PUSH(constantPool, getXSLTC().getCharacterData(i9)));
            instructionList.append(new INVOKEVIRTUAL(addMethodref));
            String str7 = this._className;
            instructionList.append(new PUTSTATIC(constantPool.addFieldref(str7, Constants.STATIC_CHAR_DATA_FIELD + i9, Constants.STATIC_CHAR_DATA_FIELD_SIG)));
            methodGenerator.markChunkEnd();
        }
        instructionList.append(RETURN);
        classGenerator.addMethod(methodGenerator);
    }

    private void compileConstructor(ClassGenerator classGenerator, Output output) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        InstructionList instructionList = new InstructionList();
        MethodGenerator methodGenerator = new MethodGenerator(1, ohos.com.sun.org.apache.bcel.internal.generic.Type.VOID, null, null, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, this._className, instructionList, constantPool);
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new INVOKESPECIAL(constantPool.addMethodref(Constants.TRANSLET_CLASS, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, "()V")));
        methodGenerator.markChunkStart();
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new GETSTATIC(constantPool.addFieldref(this._className, Constants.STATIC_NAMES_ARRAY_FIELD, "[Ljava/lang/String;")));
        instructionList.append(new PUTFIELD(constantPool.addFieldref(Constants.TRANSLET_CLASS, Constants.NAMES_INDEX, "[Ljava/lang/String;")));
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new GETSTATIC(constantPool.addFieldref(this._className, Constants.STATIC_URIS_ARRAY_FIELD, "[Ljava/lang/String;")));
        instructionList.append(new PUTFIELD(constantPool.addFieldref(Constants.TRANSLET_CLASS, Constants.URIS_INDEX, "[Ljava/lang/String;")));
        methodGenerator.markChunkEnd();
        methodGenerator.markChunkStart();
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new GETSTATIC(constantPool.addFieldref(this._className, Constants.STATIC_TYPES_ARRAY_FIELD, Constants.TYPES_INDEX_SIG)));
        instructionList.append(new PUTFIELD(constantPool.addFieldref(Constants.TRANSLET_CLASS, Constants.TYPES_INDEX, Constants.TYPES_INDEX_SIG)));
        methodGenerator.markChunkEnd();
        methodGenerator.markChunkStart();
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new GETSTATIC(constantPool.addFieldref(this._className, Constants.STATIC_NAMESPACE_ARRAY_FIELD, "[Ljava/lang/String;")));
        instructionList.append(new PUTFIELD(constantPool.addFieldref(Constants.TRANSLET_CLASS, Constants.NAMESPACE_INDEX, "[Ljava/lang/String;")));
        methodGenerator.markChunkEnd();
        methodGenerator.markChunkStart();
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new PUSH(constantPool, 101));
        instructionList.append(new PUTFIELD(constantPool.addFieldref(Constants.TRANSLET_CLASS, Constants.TRANSLET_VERSION_INDEX, "I")));
        methodGenerator.markChunkEnd();
        if (this._hasIdCall) {
            methodGenerator.markChunkStart();
            instructionList.append(classGenerator.loadTranslet());
            instructionList.append(new PUSH(constantPool, Boolean.TRUE));
            instructionList.append(new PUTFIELD(constantPool.addFieldref(Constants.TRANSLET_CLASS, Constants.HASIDCALL_INDEX, Constants.HASIDCALL_INDEX_SIG)));
            methodGenerator.markChunkEnd();
        }
        if (output != null) {
            methodGenerator.markChunkStart();
            output.translate(classGenerator, methodGenerator);
            methodGenerator.markChunkEnd();
        }
        if (this._numberFormattingUsed) {
            methodGenerator.markChunkStart();
            DecimalFormatting.translateDefaultDFS(classGenerator, methodGenerator);
            methodGenerator.markChunkEnd();
        }
        instructionList.append(RETURN);
        classGenerator.addMethod(methodGenerator);
    }

    private String compileTopLevel(ClassGenerator classGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        ohos.com.sun.org.apache.bcel.internal.generic.Type[] typeArr = {Util.getJCRefType(Constants.DOM_INTF_SIG), Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;")};
        String[] strArr = {Constants.DOCUMENT_PNAME, Constants.ITERATOR_PNAME, Constants.TRANSLET_OUTPUT_PNAME};
        InstructionList instructionList = new InstructionList();
        MethodGenerator methodGenerator = new MethodGenerator(1, ohos.com.sun.org.apache.bcel.internal.generic.Type.VOID, typeArr, strArr, "topLevel", this._className, instructionList, classGenerator.getConstantPool());
        methodGenerator.addException("ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException");
        LocalVariableGen addLocalVariable = methodGenerator.addLocalVariable(Keywords.FUNC_CURRENT_STRING, ohos.com.sun.org.apache.bcel.internal.generic.Type.INT, null, null);
        int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "setFilter", "(Lohos.com.sun.org.apache.xalan.internal.xsltc.StripFilter;)V");
        int addInterfaceMethodref2 = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getIterator", "()Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;");
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref2, 1));
        instructionList.append(methodGenerator.nextNode());
        addLocalVariable.setStart(instructionList.append(new ISTORE(addLocalVariable.getIndex())));
        Vector vector = new Vector(this._globals);
        Iterator<SyntaxTreeNode> elements = elements();
        while (elements.hasNext()) {
            SyntaxTreeNode next = elements.next();
            if (next instanceof Key) {
                vector.add(next);
            }
        }
        Vector resolveDependencies = resolveDependencies(vector);
        int size = resolveDependencies.size();
        for (int i = 0; i < size; i++) {
            TopLevelElement topLevelElement = (TopLevelElement) resolveDependencies.elementAt(i);
            topLevelElement.translate(classGenerator, methodGenerator);
            if (topLevelElement instanceof Key) {
                Key key = (Key) topLevelElement;
                this._keys.put(key.getName(), key);
            }
        }
        Vector vector2 = new Vector();
        Iterator<SyntaxTreeNode> elements2 = elements();
        while (elements2.hasNext()) {
            SyntaxTreeNode next2 = elements2.next();
            if (next2 instanceof DecimalFormatting) {
                ((DecimalFormatting) next2).translate(classGenerator, methodGenerator);
            } else if (next2 instanceof Whitespace) {
                vector2.addAll(((Whitespace) next2).getRules());
            }
        }
        if (vector2.size() > 0) {
            Whitespace.translateRules(vector2, classGenerator);
        }
        if (classGenerator.containsMethod(Constants.STRIP_SPACE, Constants.STRIP_SPACE_PARAMS) != null) {
            instructionList.append(methodGenerator.loadDOM());
            instructionList.append(classGenerator.loadTranslet());
            instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 2));
        }
        instructionList.append(RETURN);
        classGenerator.addMethod(methodGenerator);
        return "(Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;)V";
    }

    private Vector resolveDependencies(Vector vector) {
        Vector vector2 = new Vector();
        while (true) {
            if (vector.size() <= 0) {
                break;
            }
            int i = 0;
            boolean z = false;
            while (i < vector.size()) {
                TopLevelElement topLevelElement = (TopLevelElement) vector.elementAt(i);
                Vector dependencies = topLevelElement.getDependencies();
                if (dependencies == null || vector2.containsAll(dependencies)) {
                    vector2.addElement(topLevelElement);
                    vector.remove(i);
                    z = true;
                } else {
                    i++;
                }
            }
            if (!z) {
                getParser().reportError(3, new ErrorMsg(ErrorMsg.CIRCULAR_VARIABLE_ERR, (Object) vector.toString(), (SyntaxTreeNode) this));
                break;
            }
        }
        return vector2;
    }

    private String compileBuildKeys(ClassGenerator classGenerator) {
        classGenerator.getConstantPool();
        ohos.com.sun.org.apache.bcel.internal.generic.Type[] typeArr = {Util.getJCRefType(Constants.DOM_INTF_SIG), Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;"), ohos.com.sun.org.apache.bcel.internal.generic.Type.INT};
        String[] strArr = {Constants.DOCUMENT_PNAME, Constants.ITERATOR_PNAME, Constants.TRANSLET_OUTPUT_PNAME, Keywords.FUNC_CURRENT_STRING};
        InstructionList instructionList = new InstructionList();
        MethodGenerator methodGenerator = new MethodGenerator(1, ohos.com.sun.org.apache.bcel.internal.generic.Type.VOID, typeArr, strArr, "buildKeys", this._className, instructionList, classGenerator.getConstantPool());
        methodGenerator.addException("ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException");
        Iterator<SyntaxTreeNode> elements = elements();
        while (elements.hasNext()) {
            SyntaxTreeNode next = elements.next();
            if (next instanceof Key) {
                Key key = (Key) next;
                key.translate(classGenerator, methodGenerator);
                this._keys.put(key.getName(), key);
            }
        }
        instructionList.append(RETURN);
        methodGenerator.stripAttributes(true);
        methodGenerator.setMaxLocals();
        methodGenerator.setMaxStack();
        methodGenerator.removeNOPs();
        classGenerator.addMethod(methodGenerator.getMethod());
        return Constants.ATTR_SET_SIG;
    }

    private void compileTransform(ClassGenerator classGenerator) {
        ConstantPoolGen constantPool = classGenerator.getConstantPool();
        ohos.com.sun.org.apache.bcel.internal.generic.Type[] typeArr = {Util.getJCRefType(Constants.DOM_INTF_SIG), Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;"), Util.getJCRefType("Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;")};
        String[] strArr = {Constants.DOCUMENT_PNAME, Constants.ITERATOR_PNAME, Constants.TRANSLET_OUTPUT_PNAME};
        InstructionList instructionList = new InstructionList();
        MethodGenerator methodGenerator = new MethodGenerator(1, ohos.com.sun.org.apache.bcel.internal.generic.Type.VOID, typeArr, strArr, Constants.ELEMNAME_TRANSFORM_STRING, this._className, instructionList, classGenerator.getConstantPool());
        methodGenerator.addException("ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException");
        instructionList.append(new INVOKESTATIC(constantPool.addMethodref(Constants.BASIS_LIBRARY_CLASS, "resetPrefixIndex", "()V")));
        LocalVariableGen addLocalVariable = methodGenerator.addLocalVariable(Keywords.FUNC_CURRENT_STRING, ohos.com.sun.org.apache.bcel.internal.generic.Type.INT, null, null);
        int addMethodref = constantPool.addMethodref(getClassName(), Constants.APPLY_TEMPLATES, classGenerator.getApplyTemplatesSig());
        int addFieldref = constantPool.addFieldref(getClassName(), Constants.DOM_FIELD, Constants.DOM_INTF_SIG);
        instructionList.append(classGenerator.loadTranslet());
        if (isMultiDocument()) {
            instructionList.append(new NEW(constantPool.addClass(Constants.MULTI_DOM_CLASS)));
            instructionList.append(DUP);
        }
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.TRANSLET_CLASS, "makeDOMAdapter", "(Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;)Lohos.com.sun.org.apache.xalan.internal.xsltc.dom.DOMAdapter;")));
        if (isMultiDocument()) {
            instructionList.append(new INVOKESPECIAL(constantPool.addMethodref(Constants.MULTI_DOM_CLASS, ohos.com.sun.org.apache.bcel.internal.Constants.CONSTRUCTOR_NAME, "(Lohos.com.sun.org.apache.xalan.internal.xsltc.DOM;)V")));
        }
        instructionList.append(new PUTFIELD(addFieldref));
        int addInterfaceMethodref = constantPool.addInterfaceMethodref("ohos.com.sun.org.apache.xalan.internal.xsltc.DOM", "getIterator", "()Lohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;");
        instructionList.append(methodGenerator.loadDOM());
        instructionList.append(new INVOKEINTERFACE(addInterfaceMethodref, 1));
        instructionList.append(methodGenerator.nextNode());
        addLocalVariable.setStart(instructionList.append(new ISTORE(addLocalVariable.getIndex())));
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(methodGenerator.loadHandler());
        instructionList.append(new INVOKEVIRTUAL(constantPool.addMethodref(Constants.TRANSLET_CLASS, "transferOutputSettings", "(Lohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;)V")));
        constantPool.addMethodref(getClassName(), "buildKeys", compileBuildKeys(classGenerator));
        Iterator<SyntaxTreeNode> elements = elements();
        if (this._globals.size() > 0 || elements.hasNext()) {
            int addMethodref2 = constantPool.addMethodref(getClassName(), "topLevel", compileTopLevel(classGenerator));
            instructionList.append(classGenerator.loadTranslet());
            instructionList.append(classGenerator.loadTranslet());
            instructionList.append(new GETFIELD(addFieldref));
            instructionList.append(methodGenerator.loadIterator());
            instructionList.append(methodGenerator.loadHandler());
            instructionList.append(new INVOKEVIRTUAL(addMethodref2));
        }
        instructionList.append(methodGenerator.loadHandler());
        instructionList.append(methodGenerator.startDocument());
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(classGenerator.loadTranslet());
        instructionList.append(new GETFIELD(addFieldref));
        instructionList.append(methodGenerator.loadIterator());
        instructionList.append(methodGenerator.loadHandler());
        instructionList.append(new INVOKEVIRTUAL(addMethodref));
        instructionList.append(methodGenerator.loadHandler());
        instructionList.append(methodGenerator.endDocument());
        instructionList.append(RETURN);
        classGenerator.addMethod(methodGenerator);
    }

    private void peepHoleOptimization(MethodGenerator methodGenerator) {
        InstructionList instructionList = methodGenerator.getInstructionList();
        Iterator search = new InstructionFinder(instructionList).search("`aload'`pop'`instruction'");
        while (search.hasNext()) {
            InstructionHandle[] instructionHandleArr = (InstructionHandle[]) search.next();
            try {
                instructionList.delete(instructionHandleArr[0], instructionHandleArr[1]);
            } catch (TargetLostException unused) {
            }
        }
    }

    public int addParam(Param param) {
        this._globals.addElement(param);
        return this._globals.size() - 1;
    }

    public int addVariable(Variable variable) {
        this._globals.addElement(variable);
        return this._globals.size() - 1;
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode
    public void display(int i) {
        indent(i);
        Util.println("Stylesheet");
        displayContents(i + 4);
    }

    public String getNamespace(String str) {
        return lookupNamespace(str);
    }

    public String getClassName() {
        return this._className;
    }

    public Vector getTemplates() {
        return this._templates;
    }

    public Vector getAllValidTemplates() {
        if (this._includedStylesheets == null) {
            return this._templates;
        }
        if (this._allValidTemplates == null) {
            Vector vector = new Vector();
            vector.addAll(this._templates);
            int size = this._includedStylesheets.size();
            for (int i = 0; i < size; i++) {
                vector.addAll(((Stylesheet) this._includedStylesheets.elementAt(i)).getAllValidTemplates());
            }
            if (this._parentStylesheet != null) {
                return vector;
            }
            this._allValidTemplates = vector;
        }
        return this._allValidTemplates;
    }

    /* access modifiers changed from: protected */
    public void addTemplate(Template template) {
        this._templates.addElement(template);
    }
}
