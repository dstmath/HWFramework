package ohos.com.sun.org.apache.xerces.internal.xinclude;

import java.util.Enumeration;
import java.util.Stack;
import java.util.StringTokenizer;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.DTDGrammar;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;

public class XPointerElementHandler implements XPointerSchema {
    protected static final String ENTITY_RESOLVER = "http://apache.org/xml/properties/internal/entity-resolver";
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    private static final Boolean[] FEATURE_DEFAULTS = new Boolean[0];
    protected static final String GRAMMAR_POOL = "http://apache.org/xml/properties/internal/grammar-pool";
    private static final int INITIAL_SIZE = 8;
    private static final Object[] PROPERTY_DEFAULTS = {null, null, null, null};
    private static final String[] RECOGNIZED_FEATURES = new String[0];
    private static final String[] RECOGNIZED_PROPERTIES = {"http://apache.org/xml/properties/internal/error-reporter", "http://apache.org/xml/properties/internal/grammar-pool", "http://apache.org/xml/properties/internal/entity-resolver", XPOINTER_SCHEMA};
    protected static final String XPOINTER_SCHEMA = "http://apache.org/xml/properties/xpointer-schema";
    private int elemCount = 0;
    int fCurrentToken;
    String fCurrentTokenString = null;
    int fCurrentTokenType = 0;
    int fCurrentTokenint = 0;
    protected DTDGrammar fDTDGrammar;
    private int fDepth = 0;
    protected XMLLocator fDocLocation;
    protected XMLDocumentHandler fDocumentHandler;
    protected XMLDocumentSource fDocumentSource;
    int fElementCount = 0;
    protected XMLEntityResolver fEntityResolver;
    protected XMLErrorReporter fErrorReporter;
    protected XMLGrammarDescription fGrammarDesc;
    protected XMLGrammarPool fGrammarPool;
    protected XIncludeNamespaceSupport fNamespaceContext;
    protected XIncludeHandler fParentXIncludeHandler;
    protected StringBuffer fPointer;
    Stack fPointerToken = new Stack();
    private int fRootDepth = 0;
    private boolean[] fSawFallback = new boolean[8];
    private boolean[] fSawInclude = new boolean[8];
    String fSchemaName;
    String fSchemaPointer;
    protected ParserConfigurationSettings fSettings;
    private int[] fState = new int[8];
    boolean fSubResourceIdentified;
    QName foundElement = null;
    Stack ftempCurrentElement = new Stack();
    boolean includeElement;
    boolean skip = false;

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void doctypeDecl(String str, String str2, String str3, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endDocument(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void xmlDecl(String str, String str2, String str3, Augmentations augmentations) throws XNIException {
    }

    public XPointerElementHandler() {
        boolean[] zArr = this.fSawFallback;
        int i = this.fDepth;
        zArr[i] = false;
        this.fSawInclude[i] = false;
        this.fSchemaName = "element";
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xinclude.XPointerSchema
    public void reset() {
        this.elemCount = 0;
        this.fPointerToken = null;
        this.fCurrentTokenint = 0;
        this.fCurrentTokenString = null;
        this.fCurrentTokenType = 0;
        this.fElementCount = 0;
        this.fCurrentToken = 0;
        this.includeElement = false;
        this.foundElement = null;
        this.skip = false;
        this.fSubResourceIdentified = false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void reset(XMLComponentManager xMLComponentManager) throws XNIException {
        this.fNamespaceContext = null;
        this.elemCount = 0;
        this.fDepth = 0;
        this.fRootDepth = 0;
        this.fPointerToken = null;
        this.fCurrentTokenint = 0;
        this.fCurrentTokenString = null;
        this.fCurrentTokenType = 0;
        this.foundElement = null;
        this.includeElement = false;
        this.skip = false;
        this.fSubResourceIdentified = false;
        try {
            setErrorReporter((XMLErrorReporter) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/error-reporter"));
        } catch (XMLConfigurationException unused) {
            this.fErrorReporter = null;
        }
        try {
            this.fGrammarPool = (XMLGrammarPool) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/grammar-pool");
        } catch (XMLConfigurationException unused2) {
            this.fGrammarPool = null;
        }
        try {
            this.fEntityResolver = (XMLEntityResolver) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/entity-resolver");
        } catch (XMLConfigurationException unused3) {
            this.fEntityResolver = null;
        }
        this.fSettings = new ParserConfigurationSettings();
        Enumeration xercesFeatures = Constants.getXercesFeatures();
        while (xercesFeatures.hasMoreElements()) {
            String str = (String) xercesFeatures.nextElement();
            this.fSettings.addRecognizedFeatures(new String[]{str});
            try {
                this.fSettings.setFeature(str, xMLComponentManager.getFeature(str));
            } catch (XMLConfigurationException unused4) {
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public String[] getRecognizedFeatures() {
        return RECOGNIZED_FEATURES;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setFeature(String str, boolean z) throws XMLConfigurationException {
        ParserConfigurationSettings parserConfigurationSettings = this.fSettings;
        if (parserConfigurationSettings != null) {
            parserConfigurationSettings.setFeature(str, z);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public String[] getRecognizedProperties() {
        return RECOGNIZED_PROPERTIES;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setProperty(String str, Object obj) throws XMLConfigurationException {
        if (str.equals("http://apache.org/xml/properties/internal/error-reporter")) {
            setErrorReporter((XMLErrorReporter) obj);
        }
        if (str.equals("http://apache.org/xml/properties/internal/grammar-pool")) {
            this.fGrammarPool = (XMLGrammarPool) obj;
        }
        if (str.equals("http://apache.org/xml/properties/internal/entity-resolver")) {
            this.fEntityResolver = (XMLEntityResolver) obj;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public Boolean getFeatureDefault(String str) {
        int i = 0;
        while (true) {
            String[] strArr = RECOGNIZED_FEATURES;
            if (i >= strArr.length) {
                return null;
            }
            if (strArr[i].equals(str)) {
                return FEATURE_DEFAULTS[i];
            }
            i++;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public Object getPropertyDefault(String str) {
        int i = 0;
        while (true) {
            String[] strArr = RECOGNIZED_PROPERTIES;
            if (i >= strArr.length) {
                return null;
            }
            if (strArr[i].equals(str)) {
                return PROPERTY_DEFAULTS[i];
            }
            i++;
        }
    }

    private void setErrorReporter(XMLErrorReporter xMLErrorReporter) {
        this.fErrorReporter = xMLErrorReporter;
        XMLErrorReporter xMLErrorReporter2 = this.fErrorReporter;
        if (xMLErrorReporter2 != null) {
            xMLErrorReporter2.putMessageFormatter(XIncludeMessageFormatter.XINCLUDE_DOMAIN, new XIncludeMessageFormatter());
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource
    public void setDocumentHandler(XMLDocumentHandler xMLDocumentHandler) {
        this.fDocumentHandler = xMLDocumentHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource
    public XMLDocumentHandler getDocumentHandler() {
        return this.fDocumentHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xinclude.XPointerSchema
    public void setXPointerSchemaName(String str) {
        this.fSchemaName = str;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xinclude.XPointerSchema
    public String getXpointerSchemaName() {
        return this.fSchemaName;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xinclude.XPointerSchema
    public void setParent(Object obj) {
        this.fParentXIncludeHandler = (XIncludeHandler) obj;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xinclude.XPointerSchema
    public Object getParent() {
        return this.fParentXIncludeHandler;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xinclude.XPointerSchema
    public void setXPointerSchemaPointer(String str) {
        this.fSchemaPointer = str;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xinclude.XPointerSchema
    public String getXPointerSchemaPointer() {
        return this.fSchemaPointer;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xinclude.XPointerSchema
    public boolean isSubResourceIndentified() {
        return this.fSubResourceIdentified;
    }

    public void getTokens() {
        String str = this.fSchemaPointer;
        this.fSchemaPointer = str.substring(str.indexOf("(") + 1, this.fSchemaPointer.length());
        StringTokenizer stringTokenizer = new StringTokenizer(this.fSchemaPointer, PsuedoNames.PSEUDONAME_ROOT);
        Stack stack = new Stack();
        if (this.fPointerToken == null) {
            this.fPointerToken = new Stack();
        }
        while (stringTokenizer.hasMoreTokens()) {
            String nextToken = stringTokenizer.nextToken();
            try {
                stack.push(Integer.valueOf(nextToken));
            } catch (NumberFormatException unused) {
                stack.push(nextToken);
            }
        }
        while (!stack.empty()) {
            this.fPointerToken.push(stack.pop());
        }
    }

    public boolean hasMoreToken() {
        return !this.fPointerToken.isEmpty();
    }

    public boolean getNextToken() {
        if (this.fPointerToken.isEmpty()) {
            return false;
        }
        Object pop = this.fPointerToken.pop();
        if (pop instanceof Integer) {
            this.fCurrentTokenint = ((Integer) pop).intValue();
            this.fCurrentTokenType = 1;
        } else {
            this.fCurrentTokenString = ((String) pop).toString();
            this.fCurrentTokenType = 2;
        }
        return true;
    }

    private boolean isIdAttribute(XMLAttributes xMLAttributes, Augmentations augmentations, int i) {
        Object item = augmentations.getItem(Constants.ID_ATTRIBUTE);
        if (item instanceof Boolean) {
            return ((Boolean) item).booleanValue();
        }
        return SchemaSymbols.ATTVAL_ID.equals(xMLAttributes.getType(i));
    }

    public boolean checkStringToken(QName qName, XMLAttributes xMLAttributes) {
        QName qName2 = new QName();
        int length = xMLAttributes.getLength();
        for (int i = 0; i < length; i++) {
            Augmentations augmentations = xMLAttributes.getAugmentations(i);
            xMLAttributes.getName(i, qName2);
            String type = xMLAttributes.getType(i);
            String value = xMLAttributes.getValue(i);
            if (type != null && value != null && isIdAttribute(xMLAttributes, augmentations, i) && value.equals(this.fCurrentTokenString)) {
                if (hasMoreToken()) {
                    this.fCurrentTokenType = 0;
                    this.fCurrentTokenString = null;
                    return true;
                } else {
                    this.foundElement = qName;
                    this.includeElement = true;
                    this.fCurrentTokenType = 0;
                    this.fCurrentTokenString = null;
                    this.fSubResourceIdentified = true;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkIntegerToken(QName qName) {
        if (!this.skip) {
            this.fElementCount++;
            if (this.fCurrentTokenint != this.fElementCount) {
                addQName(qName);
                this.skip = true;
            } else if (hasMoreToken()) {
                this.fElementCount = 0;
                this.fCurrentTokenType = 0;
                return true;
            } else {
                this.foundElement = qName;
                this.includeElement = true;
                this.fCurrentTokenType = 0;
                this.fElementCount = 0;
                this.fSubResourceIdentified = true;
                return true;
            }
        }
        return false;
    }

    public void addQName(QName qName) {
        this.ftempCurrentElement.push(new QName(qName));
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startDocument(XMLLocator xMLLocator, String str, NamespaceContext namespaceContext, Augmentations augmentations) throws XNIException {
        getTokens();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void comment(XMLString xMLString, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && this.includeElement) {
            xMLDocumentHandler.comment(xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void processingInstruction(String str, XMLString xMLString, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && this.includeElement) {
            xMLDocumentHandler.processingInstruction(str, xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        boolean z;
        if (this.fCurrentTokenType == 0) {
            getNextToken();
        }
        int i = this.fCurrentTokenType;
        if (i == 1) {
            z = checkIntegerToken(qName);
        } else {
            z = i == 2 ? checkStringToken(qName, xMLAttributes) : false;
        }
        if (z && hasMoreToken()) {
            getNextToken();
        }
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && this.includeElement) {
            this.elemCount++;
            xMLDocumentHandler.startElement(qName, xMLAttributes, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endElement(QName qName, Augmentations augmentations) throws XNIException {
        if (this.includeElement && this.foundElement != null) {
            int i = this.elemCount;
            if (i > 0) {
                this.elemCount = i - 1;
            }
            this.fDocumentHandler.endElement(qName, augmentations);
            if (this.elemCount == 0) {
                this.includeElement = false;
            }
        } else if (!this.ftempCurrentElement.empty() && ((QName) this.ftempCurrentElement.peek()).equals(qName)) {
            this.ftempCurrentElement.pop();
            this.skip = false;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void emptyElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && this.includeElement) {
            xMLDocumentHandler.emptyElement(qName, xMLAttributes, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startGeneralEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && this.includeElement) {
            xMLDocumentHandler.startGeneralEntity(str, xMLResourceIdentifier, str2, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void textDecl(String str, String str2, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && this.includeElement) {
            xMLDocumentHandler.textDecl(str, str2, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endGeneralEntity(String str, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null) {
            xMLDocumentHandler.endGeneralEntity(str, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void characters(XMLString xMLString, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && this.includeElement) {
            xMLDocumentHandler.characters(xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void ignorableWhitespace(XMLString xMLString, Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && this.includeElement) {
            xMLDocumentHandler.ignorableWhitespace(xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startCDATA(Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && this.includeElement) {
            xMLDocumentHandler.startCDATA(augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endCDATA(Augmentations augmentations) throws XNIException {
        XMLDocumentHandler xMLDocumentHandler = this.fDocumentHandler;
        if (xMLDocumentHandler != null && this.includeElement) {
            xMLDocumentHandler.endCDATA(augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void setDocumentSource(XMLDocumentSource xMLDocumentSource) {
        this.fDocumentSource = xMLDocumentSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public XMLDocumentSource getDocumentSource() {
        return this.fDocumentSource;
    }

    /* access modifiers changed from: protected */
    public void reportFatalError(String str) {
        reportFatalError(str, null);
    }

    /* access modifiers changed from: protected */
    public void reportFatalError(String str, Object[] objArr) {
        XMLErrorReporter xMLErrorReporter = this.fErrorReporter;
        if (xMLErrorReporter != null) {
            xMLErrorReporter.reportError(this.fDocLocation, XIncludeMessageFormatter.XINCLUDE_DOMAIN, str, objArr, (short) 2);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isRootDocument() {
        return this.fParentXIncludeHandler == null;
    }
}
