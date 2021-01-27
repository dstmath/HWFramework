package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;
import ohos.com.sun.java_cup.internal.runtime.Symbol;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xalan.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodType;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util.TypeCheckError;
import ohos.com.sun.org.apache.xml.internal.serializer.utils.SystemIDResolver;
import ohos.com.sun.org.apache.xpath.internal.compiler.Keywords;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.global.icu.impl.locale.LanguageTag;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.XMLReader;
import ohos.org.xml.sax.helpers.AttributesImpl;

public class Parser implements Constants, ContentHandler {
    private static final String TRANSLET = "translet";
    private static final String XSL = "xsl";
    private String _PIcharset = null;
    private String _PImedia = null;
    private String _PItitle = null;
    private int _currentImportPrecedence;
    private Stylesheet _currentStylesheet;
    private ArrayList<ErrorMsg> _errors;
    private QName _excludeResultPrefixes;
    private QName _extensionElementPrefixes;
    private Map<String, String[]> _instructionAttrs;
    private Map<String, String> _instructionClasses;
    private Locator _locator = null;
    private Map<String, Map<String, QName>> _namespaces;
    private Output _output;
    private boolean _overrideDefaultParser;
    private Stack<SyntaxTreeNode> _parentStack = null;
    private Map<String, String> _prefixMapping = null;
    private Map<String, QName> _qNames;
    private SyntaxTreeNode _root;
    private boolean _rootNamespaceDef;
    private SymbolTable _symbolTable;
    private String _target;
    private Template _template;
    private int _templateIndex = 0;
    private QName _useAttributeSets;
    private Map<String, Object> _variableScope;
    private ArrayList<ErrorMsg> _warnings;
    private XPathParser _xpathParser;
    private XSLTC _xsltc;
    private boolean versionIsOne = true;

    public void endDocument() {
    }

    public void endPrefixMapping(String str) {
    }

    public void ignorableWhitespace(char[] cArr, int i, int i2) {
    }

    public void skippedEntity(String str) {
    }

    public Parser(XSLTC xsltc, boolean z) {
        this._xsltc = xsltc;
        this._overrideDefaultParser = z;
    }

    public void init() {
        this._qNames = new HashMap(512);
        this._namespaces = new HashMap();
        this._instructionClasses = new HashMap();
        this._instructionAttrs = new HashMap();
        this._variableScope = new HashMap();
        this._template = null;
        this._errors = new ArrayList<>();
        this._warnings = new ArrayList<>();
        this._symbolTable = new SymbolTable();
        this._xpathParser = new XPathParser(this);
        this._currentStylesheet = null;
        this._output = null;
        this._root = null;
        this._rootNamespaceDef = false;
        this._currentImportPrecedence = 1;
        initStdClasses();
        initInstructionAttrs();
        initExtClasses();
        initSymbolTable();
        this._useAttributeSets = getQName("http://www.w3.org/1999/XSL/Transform", XSL, Constants.ATTRNAME_USEATTRIBUTESETS);
        this._excludeResultPrefixes = getQName("http://www.w3.org/1999/XSL/Transform", XSL, Constants.ATTRNAME_EXCLUDE_RESULT_PREFIXES);
        this._extensionElementPrefixes = getQName("http://www.w3.org/1999/XSL/Transform", XSL, Constants.ATTRNAME_EXTENSIONELEMENTPREFIXES);
    }

    public void setOutput(Output output) {
        Output output2 = this._output;
        if (output2 == null) {
            this._output = output;
        } else if (output2.getImportPrecedence() <= output.getImportPrecedence()) {
            output.mergeOutput(this._output);
            this._output.disable();
            this._output = output;
        } else {
            output.disable();
        }
    }

    public Output getOutput() {
        return this._output;
    }

    public Properties getOutputProperties() {
        return getTopLevelStylesheet().getOutputProperties();
    }

    public void addVariable(Variable variable) {
        addVariableOrParam(variable);
    }

    public void addParameter(Param param) {
        addVariableOrParam(param);
    }

    private void addVariableOrParam(VariableBase variableBase) {
        Object obj = this._variableScope.get(variableBase.getName().getStringRep());
        if (obj == null) {
            this._variableScope.put(variableBase.getName().getStringRep(), variableBase);
        } else if (obj instanceof Stack) {
            ((Stack) obj).push(variableBase);
        } else if (obj instanceof VariableBase) {
            Stack stack = new Stack();
            stack.push((VariableBase) obj);
            stack.push(variableBase);
            this._variableScope.put(variableBase.getName().getStringRep(), stack);
        }
    }

    public void removeVariable(QName qName) {
        Object obj = this._variableScope.get(qName.getStringRep());
        if (obj instanceof Stack) {
            Stack stack = (Stack) obj;
            if (!stack.isEmpty()) {
                stack.pop();
            }
            if (!stack.isEmpty()) {
                return;
            }
        }
        this._variableScope.remove(qName.getStringRep());
    }

    public VariableBase lookupVariable(QName qName) {
        Object obj = this._variableScope.get(qName.getStringRep());
        if (obj instanceof VariableBase) {
            return (VariableBase) obj;
        }
        if (obj instanceof Stack) {
            return (VariableBase) ((Stack) obj).peek();
        }
        return null;
    }

    public void setXSLTC(XSLTC xsltc) {
        this._xsltc = xsltc;
    }

    public XSLTC getXSLTC() {
        return this._xsltc;
    }

    public int getCurrentImportPrecedence() {
        return this._currentImportPrecedence;
    }

    public int getNextImportPrecedence() {
        int i = this._currentImportPrecedence + 1;
        this._currentImportPrecedence = i;
        return i;
    }

    public void setCurrentStylesheet(Stylesheet stylesheet) {
        this._currentStylesheet = stylesheet;
    }

    public Stylesheet getCurrentStylesheet() {
        return this._currentStylesheet;
    }

    public Stylesheet getTopLevelStylesheet() {
        return this._xsltc.getStylesheet();
    }

    public QName getQNameSafe(String str) {
        String str2;
        String str3;
        int lastIndexOf = str.lastIndexOf(58);
        if (lastIndexOf != -1) {
            String substring = str.substring(0, lastIndexOf);
            String substring2 = str.substring(lastIndexOf + 1);
            if (!substring.equals("xmlns")) {
                str3 = this._symbolTable.lookupNamespace(substring);
                if (str3 == null) {
                    str3 = "";
                }
            } else {
                str3 = null;
            }
            return getQName(str3, substring, substring2);
        }
        if (str.equals("xmlns")) {
            str2 = null;
        } else {
            str2 = this._symbolTable.lookupNamespace("");
        }
        return getQName(str2, (String) null, str);
    }

    public QName getQName(String str) {
        return getQName(str, true, false);
    }

    public QName getQNameIgnoreDefaultNs(String str) {
        return getQName(str, true, true);
    }

    public QName getQName(String str, boolean z) {
        return getQName(str, z, false);
    }

    private QName getQName(String str, boolean z, boolean z2) {
        String str2;
        int lastIndexOf = str.lastIndexOf(58);
        String str3 = null;
        if (lastIndexOf != -1) {
            String substring = str.substring(0, lastIndexOf);
            String substring2 = str.substring(lastIndexOf + 1);
            if (!substring.equals("xmlns") && (str3 = this._symbolTable.lookupNamespace(substring)) == null && z) {
                reportError(3, new ErrorMsg(ErrorMsg.NAMESPACE_UNDEF_ERR, getLineNumber(), substring));
            }
            return getQName(str3, substring, substring2);
        }
        if (str.equals("xmlns")) {
            z2 = true;
        }
        if (z2) {
            str2 = null;
        } else {
            str2 = this._symbolTable.lookupNamespace("");
        }
        return getQName(str2, (String) null, str);
    }

    public QName getQName(String str, String str2, String str3) {
        String str4;
        if (str == null || str.equals("")) {
            QName qName = this._qNames.get(str3);
            if (qName != null) {
                return qName;
            }
            QName qName2 = new QName(null, str2, str3);
            this._qNames.put(str3, qName2);
            return qName2;
        }
        Map<String, QName> map = this._namespaces.get(str);
        if (str2 == null || str2.length() == 0) {
            str4 = str3;
        } else {
            str4 = str2 + ':' + str3;
        }
        if (map == null) {
            QName qName3 = new QName(str, str2, str3);
            Map<String, Map<String, QName>> map2 = this._namespaces;
            HashMap hashMap = new HashMap();
            map2.put(str, hashMap);
            hashMap.put(str4, qName3);
            return qName3;
        }
        QName qName4 = map.get(str4);
        if (qName4 != null) {
            return qName4;
        }
        QName qName5 = new QName(str, str2, str3);
        map.put(str4, qName5);
        return qName5;
    }

    public QName getQName(String str, String str2) {
        return getQName(str + str2);
    }

    public QName getQName(QName qName, QName qName2) {
        return getQName(qName.toString() + qName2.toString());
    }

    public QName getUseAttributeSets() {
        return this._useAttributeSets;
    }

    public QName getExtensionElementPrefixes() {
        return this._extensionElementPrefixes;
    }

    public QName getExcludeResultPrefixes() {
        return this._excludeResultPrefixes;
    }

    public Stylesheet makeStylesheet(SyntaxTreeNode syntaxTreeNode) throws CompilerException {
        Stylesheet stylesheet;
        try {
            if (syntaxTreeNode instanceof Stylesheet) {
                stylesheet = (Stylesheet) syntaxTreeNode;
            } else {
                Stylesheet stylesheet2 = new Stylesheet();
                stylesheet2.setSimplified();
                stylesheet2.addElement(syntaxTreeNode);
                stylesheet2.setAttributes((AttributesImpl) syntaxTreeNode.getAttributes());
                if (syntaxTreeNode.lookupNamespace("") == null) {
                    syntaxTreeNode.addPrefixMapping("", "");
                }
                stylesheet = stylesheet2;
            }
            stylesheet.setParser(this);
            return stylesheet;
        } catch (ClassCastException unused) {
            throw new CompilerException(new ErrorMsg(ErrorMsg.NOT_STYLESHEET_ERR, syntaxTreeNode).toString());
        }
    }

    public void createAST(Stylesheet stylesheet) {
        if (stylesheet != null) {
            try {
                stylesheet.parseContents(this);
                Iterator<SyntaxTreeNode> elements = stylesheet.elements();
                while (elements.hasNext()) {
                    if (elements.next() instanceof Text) {
                        reportError(3, new ErrorMsg(ErrorMsg.ILLEGAL_TEXT_NODE_ERR, getLineNumber(), (Object) null));
                    }
                }
                if (!errorsFound()) {
                    stylesheet.typeCheck(this._symbolTable);
                }
            } catch (TypeCheckError e) {
                reportError(3, new ErrorMsg(ErrorMsg.JAXP_COMPILE_ERR, (Throwable) e));
            }
        }
    }

    public SyntaxTreeNode parse(XMLReader xMLReader, InputSource inputSource) {
        try {
            xMLReader.setContentHandler(this);
            xMLReader.parse(inputSource);
            return getStylesheet(this._root);
        } catch (IOException e) {
            if (this._xsltc.debug()) {
                e.printStackTrace();
            }
            this.reportError(3, new ErrorMsg(ErrorMsg.JAXP_COMPILE_ERR, (Throwable) e));
            return null;
        } catch (SAXException e2) {
            Exception exception = e2.getException();
            if (this._xsltc.debug()) {
                e2.printStackTrace();
                if (exception != null) {
                    exception.printStackTrace();
                }
            }
            this.reportError(3, new ErrorMsg(ErrorMsg.JAXP_COMPILE_ERR, (Throwable) e2));
            return null;
        } catch (CompilerException e3) {
            if (this._xsltc.debug()) {
                e3.printStackTrace();
            }
            this.reportError(3, new ErrorMsg(ErrorMsg.JAXP_COMPILE_ERR, (Throwable) e3));
            return null;
        } catch (Exception e4) {
            if (this._xsltc.debug()) {
                e4.printStackTrace();
            }
            this.reportError(3, new ErrorMsg(ErrorMsg.JAXP_COMPILE_ERR, (Throwable) e4));
            return null;
        }
    }

    public SyntaxTreeNode parse(InputSource inputSource) {
        SAXException e;
        XMLReader xMLReader = JdkXmlUtils.getXMLReader(this._overrideDefaultParser, this._xsltc.isSecureProcessing());
        JdkXmlUtils.setXMLReaderPropertyIfSupport(xMLReader, "http://ohos.javax.xml.XMLConstants/property/accessExternalDTD", this._xsltc.getProperty("http://ohos.javax.xml.XMLConstants/property/accessExternalDTD"), true);
        String str = "";
        try {
            XMLSecurityManager xMLSecurityManager = (XMLSecurityManager) this._xsltc.getProperty("http://apache.org/xml/properties/security-manager");
            XMLSecurityManager.Limit[] values = XMLSecurityManager.Limit.values();
            for (XMLSecurityManager.Limit limit : values) {
                xMLReader.setProperty(limit.apiProperty(), xMLSecurityManager.getLimitValueAsString(limit));
            }
            if (xMLSecurityManager.printEntityCountInfo()) {
                try {
                    xMLReader.setProperty("http://www.oracle.com/xml/jaxp/properties/getEntityCountInfo", "yes");
                } catch (SAXException e2) {
                    str = "http://www.oracle.com/xml/jaxp/properties/getEntityCountInfo";
                    e = e2;
                }
            }
        } catch (SAXException e3) {
            e = e3;
            XMLSecurityManager.printWarning(xMLReader.getClass().getName(), str, e);
            return parse(xMLReader, inputSource);
        }
        return parse(xMLReader, inputSource);
    }

    public SyntaxTreeNode getDocumentRoot() {
        return this._root;
    }

    /* access modifiers changed from: protected */
    public void setPIParameters(String str, String str2, String str3) {
        this._PImedia = str;
        this._PItitle = str2;
        this._PIcharset = str3;
    }

    private SyntaxTreeNode getStylesheet(SyntaxTreeNode syntaxTreeNode) throws CompilerException {
        String str = this._target;
        if (str == null) {
            if (this._rootNamespaceDef) {
                return syntaxTreeNode;
            }
            throw new CompilerException(new ErrorMsg(ErrorMsg.MISSING_XSLT_URI_ERR).toString());
        } else if (str.charAt(0) == '#') {
            SyntaxTreeNode findStylesheet = findStylesheet(syntaxTreeNode, this._target.substring(1));
            if (findStylesheet != null) {
                return findStylesheet;
            }
            throw new CompilerException(new ErrorMsg(ErrorMsg.MISSING_XSLT_TARGET_ERR, (Object) this._target, syntaxTreeNode).toString());
        } else {
            try {
                String str2 = this._target;
                if (str2.indexOf(":") == -1) {
                    str2 = "file:" + str2;
                }
                String checkAccess = SecuritySupport.checkAccess(SystemIDResolver.getAbsoluteURI(str2), (String) this._xsltc.getProperty("http://ohos.javax.xml.XMLConstants/property/accessExternalStylesheet"), "all");
                if (checkAccess == null) {
                    return loadExternalStylesheet(this._target);
                }
                throw new CompilerException(new ErrorMsg(ErrorMsg.ACCESSING_XSLT_TARGET_ERR, SecuritySupport.sanitizePath(this._target), checkAccess, syntaxTreeNode).toString());
            } catch (IOException e) {
                throw new CompilerException(e);
            }
        }
    }

    private SyntaxTreeNode findStylesheet(SyntaxTreeNode syntaxTreeNode, String str) {
        if (syntaxTreeNode == null) {
            return null;
        }
        if ((syntaxTreeNode instanceof Stylesheet) && syntaxTreeNode.getAttribute("id").equals(str)) {
            return syntaxTreeNode;
        }
        List<SyntaxTreeNode> contents = syntaxTreeNode.getContents();
        if (contents != null) {
            int size = contents.size();
            for (int i = 0; i < size; i++) {
                SyntaxTreeNode findStylesheet = findStylesheet(contents.get(i), str);
                if (findStylesheet != null) {
                    return findStylesheet;
                }
            }
        }
        return null;
    }

    private SyntaxTreeNode loadExternalStylesheet(String str) throws CompilerException {
        InputSource inputSource;
        if (new File(str).exists()) {
            inputSource = new InputSource("file:" + str);
        } else {
            inputSource = new InputSource(str);
        }
        return parse(inputSource);
    }

    private void initAttrTable(String str, String[] strArr) {
        this._instructionAttrs.put(getQName("http://www.w3.org/1999/XSL/Transform", XSL, str).getStringRep(), strArr);
    }

    private void initInstructionAttrs() {
        initAttrTable(Constants.ELEMNAME_TEMPLATE_STRING, new String[]{Constants.ATTRNAME_MATCH, "name", Constants.ATTRNAME_PRIORITY, Constants.ATTRNAME_MODE});
        initAttrTable(Constants.ELEMNAME_STYLESHEET_STRING, new String[]{"id", "version", Constants.ATTRNAME_EXTENSIONELEMENTPREFIXES, Constants.ATTRNAME_EXCLUDE_RESULT_PREFIXES});
        initAttrTable(Constants.ELEMNAME_TRANSFORM_STRING, new String[]{"id", "version", Constants.ATTRNAME_EXTENSIONELEMENTPREFIXES, Constants.ATTRNAME_EXCLUDE_RESULT_PREFIXES});
        initAttrTable("text", new String[]{Constants.ATTRNAME_DISABLE_OUTPUT_ESCAPING});
        initAttrTable(Constants.ELEMNAME_IF_STRING, new String[]{Constants.ATTRNAME_TEST});
        initAttrTable(Constants.ELEMNAME_CHOOSE_STRING, new String[0]);
        initAttrTable(Constants.ELEMNAME_WHEN_STRING, new String[]{Constants.ATTRNAME_TEST});
        initAttrTable(Constants.ELEMNAME_OTHERWISE_STRING, new String[0]);
        initAttrTable(Constants.ELEMNAME_FOREACH_STRING, new String[]{Constants.ATTRNAME_SELECT});
        initAttrTable("message", new String[]{Constants.ATTRNAME_TERMINATE});
        initAttrTable("number", new String[]{"level", "count", Constants.ATTRNAME_FROM, "value", "format", "lang", Constants.ATTRNAME_LETTERVALUE, Constants.ATTRNAME_GROUPINGSEPARATOR, Constants.ATTRNAME_GROUPINGSIZE});
        initAttrTable(Constants.ELEMNAME_COMMENT_STRING, new String[0]);
        initAttrTable(Constants.ELEMNAME_COPY_STRING, new String[]{Constants.ATTRNAME_USEATTRIBUTESETS});
        initAttrTable(Constants.ELEMNAME_COPY_OF_STRING, new String[]{Constants.ATTRNAME_SELECT});
        initAttrTable(Constants.ELEMNAME_PARAMVARIABLE_STRING, new String[]{"name", Constants.ATTRNAME_SELECT});
        initAttrTable(Constants.ELEMNAME_WITHPARAM_STRING, new String[]{"name", Constants.ATTRNAME_SELECT});
        initAttrTable(Constants.ELEMNAME_VARIABLE_STRING, new String[]{"name", Constants.ATTRNAME_SELECT});
        initAttrTable(Constants.ELEMNAME_OUTPUT_STRING, new String[]{Constants.ATTRNAME_OUTPUT_METHOD, "version", Constants.ATTRNAME_OUTPUT_ENCODING, Constants.ATTRNAME_OUTPUT_OMITXMLDECL, Constants.ATTRNAME_OUTPUT_STANDALONE, Constants.ATTRNAME_OUTPUT_DOCTYPE_PUBLIC, Constants.ATTRNAME_OUTPUT_DOCTYPE_SYSTEM, Constants.ATTRNAME_OUTPUT_CDATA_SECTION_ELEMENTS, Constants.ATTRNAME_OUTPUT_INDENT, Constants.ATTRNAME_OUTPUT_MEDIATYPE});
        initAttrTable(Constants.ELEMNAME_SORT_STRING, new String[]{Constants.ATTRNAME_SELECT, Constants.ATTRNAME_ORDER, Constants.ATTRNAME_CASEORDER, "lang", Constants.ATTRNAME_DATATYPE});
        initAttrTable("key", new String[]{"name", Constants.ATTRNAME_MATCH, "use"});
        initAttrTable(Constants.ELEMNAME_FALLBACK_STRING, new String[0]);
        initAttrTable("attribute", new String[]{"name", Constants.ATTRNAME_NAMESPACE});
        initAttrTable("attribute-set", new String[]{"name", Constants.ATTRNAME_USEATTRIBUTESETS});
        initAttrTable(Constants.ELEMNAME_VALUEOF_STRING, new String[]{Constants.ATTRNAME_SELECT, Constants.ATTRNAME_DISABLE_OUTPUT_ESCAPING});
        initAttrTable("element", new String[]{"name", Constants.ATTRNAME_NAMESPACE, Constants.ATTRNAME_USEATTRIBUTESETS});
        initAttrTable(Constants.ELEMNAME_CALLTEMPLATE_STRING, new String[]{"name"});
        initAttrTable(Constants.ELEMNAME_APPLY_TEMPLATES_STRING, new String[]{Constants.ATTRNAME_SELECT, Constants.ATTRNAME_MODE});
        initAttrTable(Constants.ELEMNAME_APPLY_IMPORTS_STRING, new String[0]);
        initAttrTable(Constants.ELEMNAME_DECIMALFORMAT_STRING, new String[]{"name", Constants.ATTRNAME_DECIMALSEPARATOR, Constants.ATTRNAME_GROUPINGSEPARATOR, Constants.ATTRNAME_INFINITY, Constants.ATTRNAME_MINUSSIGN, "NaN", Constants.ATTRNAME_PERCENT, Constants.ATTRNAME_PERMILLE, Constants.ATTRNAME_ZERODIGIT, Constants.ATTRNAME_DIGIT, Constants.ATTRNAME_PATTERNSEPARATOR});
        initAttrTable(Constants.ELEMNAME_IMPORT_STRING, new String[]{Constants.ATTRNAME_HREF});
        initAttrTable(Constants.ELEMNAME_INCLUDE_STRING, new String[]{Constants.ATTRNAME_HREF});
        initAttrTable(Constants.ELEMNAME_STRIPSPACE_STRING, new String[]{"elements"});
        initAttrTable(Constants.ELEMNAME_PRESERVESPACE_STRING, new String[]{"elements"});
        initAttrTable(Constants.ELEMNAME_PI_STRING, new String[]{"name"});
        initAttrTable(Constants.ELEMNAME_NSALIAS_STRING, new String[]{Constants.ATTRNAME_STYLESHEET_PREFIX, Constants.ATTRNAME_RESULT_PREFIX});
    }

    private void initStdClasses() {
        initStdClass(Constants.ELEMNAME_TEMPLATE_STRING, "Template");
        initStdClass(Constants.ELEMNAME_STYLESHEET_STRING, "Stylesheet");
        initStdClass(Constants.ELEMNAME_TRANSFORM_STRING, "Stylesheet");
        initStdClass("text", "Text");
        initStdClass(Constants.ELEMNAME_IF_STRING, "If");
        initStdClass(Constants.ELEMNAME_CHOOSE_STRING, "Choose");
        initStdClass(Constants.ELEMNAME_WHEN_STRING, "When");
        initStdClass(Constants.ELEMNAME_OTHERWISE_STRING, "Otherwise");
        initStdClass(Constants.ELEMNAME_FOREACH_STRING, "ForEach");
        initStdClass("message", "Message");
        initStdClass("number", "Number");
        initStdClass(Constants.ELEMNAME_COMMENT_STRING, "Comment");
        initStdClass(Constants.ELEMNAME_COPY_STRING, "Copy");
        initStdClass(Constants.ELEMNAME_COPY_OF_STRING, "CopyOf");
        initStdClass(Constants.ELEMNAME_PARAMVARIABLE_STRING, "Param");
        initStdClass(Constants.ELEMNAME_WITHPARAM_STRING, "WithParam");
        initStdClass(Constants.ELEMNAME_VARIABLE_STRING, "Variable");
        initStdClass(Constants.ELEMNAME_OUTPUT_STRING, "Output");
        initStdClass(Constants.ELEMNAME_SORT_STRING, "Sort");
        initStdClass("key", "Key");
        initStdClass(Constants.ELEMNAME_FALLBACK_STRING, "Fallback");
        initStdClass("attribute", "XslAttribute");
        initStdClass("attribute-set", "AttributeSet");
        initStdClass(Constants.ELEMNAME_VALUEOF_STRING, "ValueOf");
        initStdClass("element", "XslElement");
        initStdClass(Constants.ELEMNAME_CALLTEMPLATE_STRING, "CallTemplate");
        initStdClass(Constants.ELEMNAME_APPLY_TEMPLATES_STRING, "ApplyTemplates");
        initStdClass(Constants.ELEMNAME_APPLY_IMPORTS_STRING, "ApplyImports");
        initStdClass(Constants.ELEMNAME_DECIMALFORMAT_STRING, "DecimalFormatting");
        initStdClass(Constants.ELEMNAME_IMPORT_STRING, "Import");
        initStdClass(Constants.ELEMNAME_INCLUDE_STRING, "Include");
        initStdClass(Constants.ELEMNAME_STRIPSPACE_STRING, "Whitespace");
        initStdClass(Constants.ELEMNAME_PRESERVESPACE_STRING, "Whitespace");
        initStdClass(Constants.ELEMNAME_PI_STRING, "ProcessingInstruction");
        initStdClass(Constants.ELEMNAME_NSALIAS_STRING, "NamespaceAlias");
    }

    private void initStdClass(String str, String str2) {
        Map<String, String> map = this._instructionClasses;
        String stringRep = getQName("http://www.w3.org/1999/XSL/Transform", XSL, str).getStringRep();
        map.put(stringRep, "com.sun.org.apache.xalan.internal.xsltc.compiler." + str2);
    }

    public boolean elementSupported(String str, String str2) {
        return this._instructionClasses.get(getQName(str, XSL, str2).getStringRep()) != null;
    }

    public boolean functionSupported(String str) {
        return this._symbolTable.lookupPrimop(str) != null;
    }

    private void initExtClasses() {
        initExtClass(Constants.ELEMNAME_OUTPUT_STRING, "TransletOutput");
        initExtClass("http://xml.apache.org/xalan/redirect", "write", "TransletOutput");
    }

    private void initExtClass(String str, String str2) {
        Map<String, String> map = this._instructionClasses;
        String stringRep = getQName(Constants.TRANSLET_URI, "translet", str).getStringRep();
        map.put(stringRep, "com.sun.org.apache.xalan.internal.xsltc.compiler." + str2);
    }

    private void initExtClass(String str, String str2, String str3) {
        Map<String, String> map = this._instructionClasses;
        String stringRep = getQName(str, "translet", str2).getStringRep();
        map.put(stringRep, "com.sun.org.apache.xalan.internal.xsltc.compiler." + str3);
    }

    private void initSymbolTable() {
        MethodType methodType = new MethodType(Type.Int, Type.Void);
        new MethodType(Type.Int, Type.Real);
        MethodType methodType2 = new MethodType(Type.Int, Type.String);
        MethodType methodType3 = new MethodType(Type.Int, Type.NodeSet);
        new MethodType(Type.Real, Type.Int);
        MethodType methodType4 = new MethodType(Type.Real, Type.Void);
        MethodType methodType5 = new MethodType(Type.Real, Type.Real);
        MethodType methodType6 = new MethodType(Type.Real, Type.NodeSet);
        MethodType methodType7 = new MethodType(Type.Real, Type.Reference);
        MethodType methodType8 = new MethodType(Type.Int, Type.Int);
        MethodType methodType9 = new MethodType(Type.NodeSet, Type.Reference);
        MethodType methodType10 = new MethodType(Type.NodeSet, Type.Void);
        MethodType methodType11 = new MethodType(Type.NodeSet, Type.String);
        MethodType methodType12 = new MethodType(Type.NodeSet, Type.NodeSet);
        MethodType methodType13 = new MethodType(Type.Node, Type.Void);
        MethodType methodType14 = new MethodType(Type.String, Type.Void);
        MethodType methodType15 = new MethodType(Type.String, Type.String);
        MethodType methodType16 = new MethodType(Type.String, Type.Node);
        MethodType methodType17 = new MethodType(Type.String, Type.NodeSet);
        MethodType methodType18 = new MethodType(Type.String, Type.Reference);
        MethodType methodType19 = new MethodType(Type.Boolean, Type.Reference);
        MethodType methodType20 = new MethodType(Type.Boolean, Type.Void);
        MethodType methodType21 = new MethodType(Type.Boolean, Type.Boolean);
        MethodType methodType22 = new MethodType(Type.Boolean, Type.String);
        new MethodType(Type.NodeSet, Type.Object);
        MethodType methodType23 = new MethodType(Type.Real, Type.Real, Type.Real);
        MethodType methodType24 = new MethodType(Type.Int, Type.Int, Type.Int);
        MethodType methodType25 = new MethodType(Type.Boolean, Type.Real, Type.Real);
        MethodType methodType26 = new MethodType(Type.Boolean, Type.Int, Type.Int);
        MethodType methodType27 = new MethodType(Type.String, Type.String, Type.String);
        MethodType methodType28 = new MethodType(Type.String, Type.Real, Type.String);
        MethodType methodType29 = new MethodType(Type.String, Type.String, Type.Real);
        MethodType methodType30 = new MethodType(Type.Reference, Type.String, Type.Reference);
        MethodType methodType31 = new MethodType(Type.NodeSet, Type.String, Type.String);
        MethodType methodType32 = new MethodType(Type.NodeSet, Type.String, Type.NodeSet);
        MethodType methodType33 = new MethodType(Type.Boolean, Type.Boolean, Type.Boolean);
        MethodType methodType34 = new MethodType(Type.Boolean, Type.String, Type.String);
        new MethodType(Type.String, Type.String, Type.NodeSet);
        MethodType methodType35 = new MethodType(Type.String, Type.Real, Type.String, Type.String);
        MethodType methodType36 = new MethodType(Type.String, Type.String, Type.Real, Type.Real);
        MethodType methodType37 = new MethodType(Type.String, Type.String, Type.String, Type.String);
        this._symbolTable.addPrimop(Keywords.FUNC_CURRENT_STRING, methodType13);
        this._symbolTable.addPrimop(Keywords.FUNC_LAST_STRING, methodType);
        this._symbolTable.addPrimop(Keywords.FUNC_POSITION_STRING, methodType);
        this._symbolTable.addPrimop("true", methodType20);
        this._symbolTable.addPrimop("false", methodType20);
        this._symbolTable.addPrimop(Keywords.FUNC_NOT_STRING, methodType21);
        this._symbolTable.addPrimop("name", methodType14);
        this._symbolTable.addPrimop("name", methodType16);
        this._symbolTable.addPrimop(Keywords.FUNC_GENERATE_ID_STRING, methodType14);
        this._symbolTable.addPrimop(Keywords.FUNC_GENERATE_ID_STRING, methodType16);
        this._symbolTable.addPrimop(Keywords.FUNC_CEILING_STRING, methodType5);
        this._symbolTable.addPrimop(Keywords.FUNC_FLOOR_STRING, methodType5);
        this._symbolTable.addPrimop(Keywords.FUNC_ROUND_STRING, methodType5);
        this._symbolTable.addPrimop(Keywords.FUNC_CONTAINS_STRING, methodType34);
        this._symbolTable.addPrimop("number", methodType7);
        this._symbolTable.addPrimop("number", methodType4);
        this._symbolTable.addPrimop("boolean", methodType19);
        this._symbolTable.addPrimop("string", methodType18);
        this._symbolTable.addPrimop("string", methodType14);
        this._symbolTable.addPrimop(Keywords.FUNC_TRANSLATE_STRING, methodType37);
        this._symbolTable.addPrimop(Keywords.FUNC_STRING_LENGTH_STRING, methodType);
        this._symbolTable.addPrimop(Keywords.FUNC_STRING_LENGTH_STRING, methodType2);
        this._symbolTable.addPrimop(Keywords.FUNC_STARTS_WITH_STRING, methodType34);
        this._symbolTable.addPrimop("format-number", methodType28);
        this._symbolTable.addPrimop("format-number", methodType35);
        this._symbolTable.addPrimop(Keywords.FUNC_UNPARSED_ENTITY_URI_STRING, methodType15);
        this._symbolTable.addPrimop("key", methodType31);
        this._symbolTable.addPrimop("key", methodType32);
        this._symbolTable.addPrimop("id", methodType11);
        this._symbolTable.addPrimop("id", methodType12);
        this._symbolTable.addPrimop(Keywords.FUNC_NAMESPACE_STRING, methodType14);
        this._symbolTable.addPrimop(Keywords.FUNC_EXT_FUNCTION_AVAILABLE_STRING, methodType22);
        this._symbolTable.addPrimop(Keywords.FUNC_EXT_ELEM_AVAILABLE_STRING, methodType22);
        this._symbolTable.addPrimop(Constants.DOCUMENT_PNAME, methodType11);
        this._symbolTable.addPrimop(Constants.DOCUMENT_PNAME, methodType10);
        this._symbolTable.addPrimop("count", methodType3);
        this._symbolTable.addPrimop(Keywords.FUNC_SUM_STRING, methodType6);
        this._symbolTable.addPrimop(Keywords.FUNC_LOCAL_PART_STRING, methodType14);
        this._symbolTable.addPrimop(Keywords.FUNC_LOCAL_PART_STRING, methodType17);
        this._symbolTable.addPrimop(Keywords.FUNC_NAMESPACE_STRING, methodType14);
        this._symbolTable.addPrimop(Keywords.FUNC_NAMESPACE_STRING, methodType17);
        this._symbolTable.addPrimop(Keywords.FUNC_SUBSTRING_STRING, methodType29);
        this._symbolTable.addPrimop(Keywords.FUNC_SUBSTRING_STRING, methodType36);
        this._symbolTable.addPrimop(Keywords.FUNC_SUBSTRING_AFTER_STRING, methodType27);
        this._symbolTable.addPrimop(Keywords.FUNC_SUBSTRING_BEFORE_STRING, methodType27);
        this._symbolTable.addPrimop(Keywords.FUNC_NORMALIZE_SPACE_STRING, methodType14);
        this._symbolTable.addPrimop(Keywords.FUNC_NORMALIZE_SPACE_STRING, methodType15);
        this._symbolTable.addPrimop(Keywords.FUNC_SYSTEM_PROPERTY_STRING, methodType15);
        this._symbolTable.addPrimop("nodeset", methodType9);
        this._symbolTable.addPrimop("objectType", methodType18);
        this._symbolTable.addPrimop("cast", methodType30);
        this._symbolTable.addPrimop("+", methodType23);
        this._symbolTable.addPrimop(LanguageTag.SEP, methodType23);
        this._symbolTable.addPrimop("*", methodType23);
        this._symbolTable.addPrimop(PsuedoNames.PSEUDONAME_ROOT, methodType23);
        this._symbolTable.addPrimop("%", methodType23);
        this._symbolTable.addPrimop("+", methodType24);
        this._symbolTable.addPrimop(LanguageTag.SEP, methodType24);
        this._symbolTable.addPrimop("*", methodType24);
        this._symbolTable.addPrimop("<", methodType25);
        this._symbolTable.addPrimop("<=", methodType25);
        this._symbolTable.addPrimop(">", methodType25);
        this._symbolTable.addPrimop(">=", methodType25);
        this._symbolTable.addPrimop("<", methodType26);
        this._symbolTable.addPrimop("<=", methodType26);
        this._symbolTable.addPrimop(">", methodType26);
        this._symbolTable.addPrimop(">=", methodType26);
        this._symbolTable.addPrimop("<", methodType33);
        this._symbolTable.addPrimop("<=", methodType33);
        this._symbolTable.addPrimop(">", methodType33);
        this._symbolTable.addPrimop(">=", methodType33);
        this._symbolTable.addPrimop("or", methodType33);
        this._symbolTable.addPrimop("and", methodType33);
        this._symbolTable.addPrimop("u-", methodType5);
        this._symbolTable.addPrimop("u-", methodType8);
    }

    public SymbolTable getSymbolTable() {
        return this._symbolTable;
    }

    public Template getTemplate() {
        return this._template;
    }

    public void setTemplate(Template template) {
        this._template = template;
    }

    public int getTemplateIndex() {
        int i = this._templateIndex;
        this._templateIndex = i + 1;
        return i;
    }

    public SyntaxTreeNode makeInstance(String str, String str2, String str3, Attributes attributes) {
        SyntaxTreeNode syntaxTreeNode;
        Exception e;
        QName qName = getQName(str, str2, str3);
        String str4 = this._instructionClasses.get(qName.getStringRep());
        UnsupportedElement unsupportedElement = null;
        if (str4 != null) {
            try {
                syntaxTreeNode = (SyntaxTreeNode) ObjectFactory.findProviderClass(str4, true).newInstance();
                try {
                    syntaxTreeNode.setQName(qName);
                    syntaxTreeNode.setParser(this);
                    if (this._locator != null) {
                        syntaxTreeNode.setLineNumber(getLineNumber());
                    }
                    if (syntaxTreeNode instanceof Stylesheet) {
                        this._xsltc.setStylesheet((Stylesheet) syntaxTreeNode);
                    }
                    checkForSuperfluousAttributes(syntaxTreeNode, attributes);
                } catch (ClassNotFoundException unused) {
                    reportError(3, new ErrorMsg(ErrorMsg.CLASS_NOT_FOUND_ERR, syntaxTreeNode));
                    ((LiteralElement) syntaxTreeNode).setQName(qName);
                    return syntaxTreeNode;
                } catch (Exception e2) {
                    e = e2;
                    reportError(2, new ErrorMsg(ErrorMsg.INTERNAL_ERR, (Object) e.getMessage(), syntaxTreeNode));
                    ((LiteralElement) syntaxTreeNode).setQName(qName);
                    return syntaxTreeNode;
                }
            } catch (ClassNotFoundException unused2) {
                syntaxTreeNode = null;
                reportError(3, new ErrorMsg(ErrorMsg.CLASS_NOT_FOUND_ERR, syntaxTreeNode));
                ((LiteralElement) syntaxTreeNode).setQName(qName);
                return syntaxTreeNode;
            } catch (Exception e3) {
                e = e3;
                syntaxTreeNode = null;
                reportError(2, new ErrorMsg(ErrorMsg.INTERNAL_ERR, (Object) e.getMessage(), syntaxTreeNode));
                ((LiteralElement) syntaxTreeNode).setQName(qName);
                return syntaxTreeNode;
            }
        } else {
            if (str != null) {
                if (str.equals("http://www.w3.org/1999/XSL/Transform")) {
                    unsupportedElement = new UnsupportedElement(str, str2, str3, false);
                    ErrorMsg errorMsg = new ErrorMsg("UNSUPPORTED_XSL_ERR", getLineNumber(), str3);
                    unsupportedElement.setErrorMessage(errorMsg);
                    if (this.versionIsOne) {
                        reportError(1, errorMsg);
                    }
                } else if (str.equals(Constants.TRANSLET_URI)) {
                    unsupportedElement = new UnsupportedElement(str, str2, str3, true);
                    unsupportedElement.setErrorMessage(new ErrorMsg("UNSUPPORTED_EXT_ERR", getLineNumber(), str3));
                } else {
                    Stylesheet stylesheet = this._xsltc.getStylesheet();
                    if (!(stylesheet == null || !stylesheet.isExtension(str) || stylesheet == this._parentStack.peek())) {
                        unsupportedElement = new UnsupportedElement(str, str2, str3, true);
                        unsupportedElement.setErrorMessage(new ErrorMsg("UNSUPPORTED_EXT_ERR", getLineNumber(), str2 + ":" + str3));
                    }
                }
            }
            syntaxTreeNode = unsupportedElement;
            if (syntaxTreeNode == null) {
                syntaxTreeNode = new LiteralElement();
                syntaxTreeNode.setLineNumber(getLineNumber());
            }
        }
        if (syntaxTreeNode != null && (syntaxTreeNode instanceof LiteralElement)) {
            ((LiteralElement) syntaxTreeNode).setQName(qName);
        }
        return syntaxTreeNode;
    }

    private void checkForSuperfluousAttributes(SyntaxTreeNode syntaxTreeNode, Attributes attributes) {
        boolean z = syntaxTreeNode instanceof Stylesheet;
        String[] strArr = this._instructionAttrs.get(syntaxTreeNode.getQName().getStringRep());
        if (this.versionIsOne && strArr != null) {
            int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                String qName = attributes.getQName(i);
                if (z && qName.equals("version")) {
                    this.versionIsOne = attributes.getValue(i).equals("1.0");
                }
                if (!qName.startsWith("xml") && qName.indexOf(58) <= 0) {
                    int i2 = 0;
                    while (i2 < strArr.length && !qName.equalsIgnoreCase(strArr[i2])) {
                        i2++;
                    }
                    if (i2 == strArr.length) {
                        ErrorMsg errorMsg = new ErrorMsg(ErrorMsg.ILLEGAL_ATTRIBUTE_ERR, (Object) qName, syntaxTreeNode);
                        errorMsg.setWarningError(true);
                        reportError(4, errorMsg);
                    }
                }
            }
        }
    }

    public Expression parseExpression(SyntaxTreeNode syntaxTreeNode, String str) {
        return (Expression) parseTopLevel(syntaxTreeNode, "<EXPRESSION>" + str, null);
    }

    public Expression parseExpression(SyntaxTreeNode syntaxTreeNode, String str, String str2) {
        String attribute = syntaxTreeNode.getAttribute(str);
        if (attribute.length() == 0 && str2 != null) {
            attribute = str2;
        }
        return (Expression) parseTopLevel(syntaxTreeNode, "<EXPRESSION>" + attribute, attribute);
    }

    public Pattern parsePattern(SyntaxTreeNode syntaxTreeNode, String str) {
        return (Pattern) parseTopLevel(syntaxTreeNode, "<PATTERN>" + str, str);
    }

    public Pattern parsePattern(SyntaxTreeNode syntaxTreeNode, String str, String str2) {
        String attribute = syntaxTreeNode.getAttribute(str);
        if (attribute.length() == 0 && str2 != null) {
            attribute = str2;
        }
        return (Pattern) parseTopLevel(syntaxTreeNode, "<PATTERN>" + attribute, attribute);
    }

    private SyntaxTreeNode parseTopLevel(SyntaxTreeNode syntaxTreeNode, String str, String str2) {
        SyntaxTreeNode syntaxTreeNode2;
        int lineNumber = getLineNumber();
        try {
            this._xpathParser.setScanner(new XPathLexer(new StringReader(str)));
            Symbol parse = this._xpathParser.parse(str2, lineNumber);
            if (parse == null || (syntaxTreeNode2 = (SyntaxTreeNode) parse.value) == null) {
                reportError(3, new ErrorMsg(ErrorMsg.XPATH_PARSER_ERR, (Object) str2, syntaxTreeNode));
                SyntaxTreeNode.Dummy.setParser(this);
                return SyntaxTreeNode.Dummy;
            }
            syntaxTreeNode2.setParser(this);
            syntaxTreeNode2.setParent(syntaxTreeNode);
            syntaxTreeNode2.setLineNumber(lineNumber);
            return syntaxTreeNode2;
        } catch (Exception e) {
            if (this._xsltc.debug()) {
                e.printStackTrace();
            }
            reportError(3, new ErrorMsg(ErrorMsg.XPATH_PARSER_ERR, (Object) str2, syntaxTreeNode));
        }
    }

    public boolean errorsFound() {
        return this._errors.size() > 0;
    }

    public void printErrors() {
        int size = this._errors.size();
        if (size > 0) {
            System.err.println(new ErrorMsg(ErrorMsg.COMPILER_ERROR_KEY));
            for (int i = 0; i < size; i++) {
                PrintStream printStream = System.err;
                printStream.println("  " + this._errors.get(i));
            }
        }
    }

    public void printWarnings() {
        int size = this._warnings.size();
        if (size > 0) {
            System.err.println(new ErrorMsg(ErrorMsg.COMPILER_WARNING_KEY));
            for (int i = 0; i < size; i++) {
                PrintStream printStream = System.err;
                printStream.println("  " + this._warnings.get(i));
            }
        }
    }

    public void reportError(int i, ErrorMsg errorMsg) {
        if (i == 0) {
            this._errors.add(errorMsg);
        } else if (i == 1) {
            this._errors.add(errorMsg);
        } else if (i == 2) {
            this._errors.add(errorMsg);
        } else if (i == 3) {
            this._errors.add(errorMsg);
        } else if (i == 4) {
            this._warnings.add(errorMsg);
        }
    }

    public ArrayList<ErrorMsg> getErrors() {
        return this._errors;
    }

    public ArrayList<ErrorMsg> getWarnings() {
        return this._warnings;
    }

    public void startDocument() {
        this._root = null;
        this._target = null;
        this._prefixMapping = null;
        this._parentStack = new Stack<>();
    }

    public void startPrefixMapping(String str, String str2) {
        if (this._prefixMapping == null) {
            this._prefixMapping = new HashMap();
        }
        this._prefixMapping.put(str, str2);
    }

    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        String str4;
        int lastIndexOf = str3.lastIndexOf(58);
        if (lastIndexOf == -1) {
            str4 = null;
        } else {
            str4 = str3.substring(0, lastIndexOf);
        }
        SyntaxTreeNode makeInstance = makeInstance(str, str4, str2, attributes);
        if (makeInstance != null) {
            if (this._root == null) {
                Map<String, String> map = this._prefixMapping;
                if (map == null || !map.containsValue("http://www.w3.org/1999/XSL/Transform")) {
                    this._rootNamespaceDef = false;
                } else {
                    this._rootNamespaceDef = true;
                }
                this._root = makeInstance;
            } else {
                SyntaxTreeNode peek = this._parentStack.peek();
                peek.addElement(makeInstance);
                makeInstance.setParent(peek);
            }
            makeInstance.setAttributes(new AttributesImpl(attributes));
            makeInstance.setPrefixMapping(this._prefixMapping);
            if (makeInstance instanceof Stylesheet) {
                getSymbolTable().setCurrentNode(makeInstance);
                ((Stylesheet) makeInstance).declareExtensionPrefixes(this);
            }
            this._prefixMapping = null;
            this._parentStack.push(makeInstance);
            return;
        }
        throw new SAXException(new ErrorMsg(ErrorMsg.ELEMENT_PARSE_ERR, str4 + ':' + str2).toString());
    }

    public void endElement(String str, String str2, String str3) {
        this._parentStack.pop();
    }

    public void characters(char[] cArr, int i, int i2) {
        String str = new String(cArr, i, i2);
        SyntaxTreeNode peek = this._parentStack.peek();
        if (str.length() != 0) {
            if (peek instanceof Text) {
                ((Text) peek).setText(str);
            } else if (!(peek instanceof Stylesheet)) {
                SyntaxTreeNode lastChild = peek.lastChild();
                if (lastChild != null && (lastChild instanceof Text)) {
                    Text text = (Text) lastChild;
                    if (!text.isTextElement() && (i2 > 1 || cArr[0] < 256)) {
                        text.setText(str);
                        return;
                    }
                }
                peek.addElement(new Text(str));
            }
        }
    }

    private String getTokenValue(String str) {
        return str.substring(str.indexOf(34) + 1, str.lastIndexOf(34));
    }

    public void processingInstruction(String str, String str2) {
        if (this._target == null && str.equals("xml-stylesheet")) {
            StringTokenizer stringTokenizer = new StringTokenizer(str2);
            String str3 = null;
            String str4 = null;
            String str5 = null;
            String str6 = null;
            while (stringTokenizer.hasMoreElements()) {
                String str7 = (String) stringTokenizer.nextElement();
                if (str7.startsWith(Constants.ATTRNAME_HREF)) {
                    str6 = getTokenValue(str7);
                } else if (str7.startsWith("media")) {
                    str3 = getTokenValue(str7);
                } else if (str7.startsWith("title")) {
                    str4 = getTokenValue(str7);
                } else if (str7.startsWith("charset")) {
                    str5 = getTokenValue(str7);
                }
            }
            String str8 = this._PImedia;
            if (str8 != null && !str8.equals(str3)) {
                return;
            }
            if (this._PItitle != null && !this._PImedia.equals(str4)) {
                return;
            }
            if (this._PIcharset == null || this._PImedia.equals(str5)) {
                this._target = str6;
            }
        }
    }

    public void setDocumentLocator(Locator locator) {
        this._locator = locator;
    }

    private int getLineNumber() {
        Locator locator = this._locator;
        if (locator != null) {
            return locator.getLineNumber();
        }
        return 0;
    }
}
