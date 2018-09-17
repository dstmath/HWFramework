package org.apache.xalan.processor;

import java.util.EmptyStackException;
import java.util.Stack;
import java.util.Vector;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.TemplatesHandler;
import org.apache.xalan.extensions.ExpressionVisitor;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.ElemForEach;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.FuncDocument;
import org.apache.xalan.templates.FuncFormatNumb;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.templates.StylesheetRoot;
import org.apache.xml.utils.BoolStack;
import org.apache.xml.utils.Constants;
import org.apache.xml.utils.NamespaceSupport2;
import org.apache.xml.utils.NodeConsumer;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.SAXSourceLocator;
import org.apache.xml.utils.XMLCharacterRecognizer;
import org.apache.xpath.XPath;
import org.apache.xpath.compiler.FunctionTable;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.NamespaceSupport;

public class StylesheetHandler extends DefaultHandler implements TemplatesHandler, PrefixResolver, NodeConsumer {
    public static final int STYPE_IMPORT = 3;
    public static final int STYPE_INCLUDE = 2;
    public static final int STYPE_ROOT = 1;
    Stack m_baseIdentifiers = new Stack();
    private int m_docOrderCount = 0;
    private int m_elementID = 0;
    private Stack m_elems = new Stack();
    private int m_fragmentID = 0;
    private String m_fragmentIDString;
    private FunctionTable m_funcTable = new FunctionTable();
    private Stack m_importSourceStack = new Stack();
    private Stack m_importStack = new Stack();
    private boolean m_incremental = false;
    Stylesheet m_lastPoppedStylesheet;
    Stack m_nsSupportStack = new Stack();
    private boolean m_optimize = true;
    private Node m_originatingNode;
    private boolean m_parsingComplete = false;
    private Vector m_prefixMappings = new Vector();
    private Stack m_processors = new Stack();
    private XSLTSchema m_schema = new XSLTSchema();
    private boolean m_shouldProcess = true;
    private boolean m_source_location = false;
    private BoolStack m_spacePreserveStack = new BoolStack();
    private int m_stylesheetLevel = -1;
    private Stack m_stylesheetLocatorStack = new Stack();
    private TransformerFactoryImpl m_stylesheetProcessor;
    StylesheetRoot m_stylesheetRoot;
    private int m_stylesheetType = 1;
    private Stack m_stylesheets = new Stack();
    private boolean warnedAboutOldXSLTNamespace = false;

    public StylesheetHandler(TransformerFactoryImpl processor) throws TransformerConfigurationException {
        this.m_funcTable.installFunction("document", FuncDocument.class);
        this.m_funcTable.installFunction("format-number", FuncFormatNumb.class);
        this.m_optimize = ((Boolean) processor.getAttribute(TransformerFactoryImpl.FEATURE_OPTIMIZE)).booleanValue();
        this.m_incremental = ((Boolean) processor.getAttribute(TransformerFactoryImpl.FEATURE_INCREMENTAL)).booleanValue();
        this.m_source_location = ((Boolean) processor.getAttribute("http://xml.apache.org/xalan/properties/source-location")).booleanValue();
        init(processor);
    }

    void init(TransformerFactoryImpl processor) {
        this.m_stylesheetProcessor = processor;
        this.m_processors.push(this.m_schema.getElementProcessor());
        pushNewNamespaceSupport();
    }

    public XPath createXPath(String str, ElemTemplateElement owningTemplate) throws TransformerException {
        XPath xpath = new XPath(str, owningTemplate, this, 0, this.m_stylesheetProcessor.getErrorListener(), this.m_funcTable);
        xpath.callVisitors(xpath, new ExpressionVisitor(getStylesheetRoot()));
        return xpath;
    }

    XPath createMatchPatternXPath(String str, ElemTemplateElement owningTemplate) throws TransformerException {
        XPath xpath = new XPath(str, owningTemplate, this, 1, this.m_stylesheetProcessor.getErrorListener(), this.m_funcTable);
        xpath.callVisitors(xpath, new ExpressionVisitor(getStylesheetRoot()));
        return xpath;
    }

    public String getNamespaceForPrefix(String prefix) {
        return getNamespaceSupport().getURI(prefix);
    }

    public String getNamespaceForPrefix(String prefix, Node context) {
        assertion(true, "can't process a context node in StylesheetHandler!");
        return null;
    }

    private boolean stackContains(Stack stack, String url) {
        int n = stack.size();
        for (int i = 0; i < n; i++) {
            if (((String) stack.elementAt(i)).equals(url)) {
                return true;
            }
        }
        return false;
    }

    public Templates getTemplates() {
        return getStylesheetRoot();
    }

    public void setSystemId(String baseID) {
        pushBaseIndentifier(baseID);
    }

    public String getSystemId() {
        return getBaseIdentifier();
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
        return getCurrentProcessor().resolveEntity(this, publicId, systemId);
    }

    public void notationDecl(String name, String publicId, String systemId) {
        getCurrentProcessor().notationDecl(this, name, publicId, systemId);
    }

    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) {
        getCurrentProcessor().unparsedEntityDecl(this, name, publicId, systemId, notationName);
    }

    XSLTElementProcessor getProcessorFor(String uri, String localName, String rawName) throws SAXException {
        XSLTElementProcessor currentProcessor = getCurrentProcessor();
        XSLTElementDef def = currentProcessor.getElemDef();
        XSLTElementProcessor elemProcessor = def.getProcessorFor(uri, localName);
        if (elemProcessor == null && ((currentProcessor instanceof ProcessorStylesheetDoc) ^ 1) != 0 && (getStylesheet() == null || Double.valueOf(getStylesheet().getVersion()).doubleValue() > 1.0d || ((!uri.equals(Constants.S_XSLNAMESPACEURL) && (currentProcessor instanceof ProcessorStylesheetElement)) || getElemVersion() > 1.0d))) {
            elemProcessor = def.getProcessorForUnknown(uri, localName);
        }
        if (elemProcessor == null) {
            error(XSLMessages.createMessage(XSLTErrorResources.ER_NOT_ALLOWED_IN_POSITION, new Object[]{rawName}), null);
        }
        return elemProcessor;
    }

    public void setDocumentLocator(Locator locator) {
        this.m_stylesheetLocatorStack.push(new SAXSourceLocator(locator));
    }

    public void startDocument() throws SAXException {
        this.m_stylesheetLevel++;
        pushSpaceHandling(false);
    }

    public boolean isStylesheetParsingComplete() {
        return this.m_parsingComplete;
    }

    public void endDocument() throws SAXException {
        boolean z = false;
        try {
            if (getStylesheetRoot() != null) {
                if (this.m_stylesheetLevel == 0) {
                    getStylesheetRoot().recompose();
                }
                XSLTElementProcessor elemProcessor = getCurrentProcessor();
                if (elemProcessor != null) {
                    elemProcessor.startNonText(this);
                }
                this.m_stylesheetLevel--;
                popSpaceHandling();
                if (this.m_stylesheetLevel < 0) {
                    z = true;
                }
                this.m_parsingComplete = z;
                return;
            }
            throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_STYLESHEETROOT, null));
        } catch (TransformerException te) {
            throw new SAXException(te);
        }
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        this.m_prefixMappings.addElement(prefix);
        this.m_prefixMappings.addElement(uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    private void flushCharacters() throws SAXException {
        XSLTElementProcessor elemProcessor = getCurrentProcessor();
        if (elemProcessor != null) {
            elemProcessor.startNonText(this);
        }
    }

    public void startElement(String uri, String localName, String rawName, Attributes attributes) throws SAXException {
        NamespaceSupport nssupport = getNamespaceSupport();
        nssupport.pushContext();
        int n = this.m_prefixMappings.size();
        int i = 0;
        while (i < n) {
            int i2 = i + 1;
            nssupport.declarePrefix((String) this.m_prefixMappings.elementAt(i), (String) this.m_prefixMappings.elementAt(i2));
            i = i2 + 1;
        }
        this.m_prefixMappings.removeAllElements();
        this.m_elementID++;
        checkForFragmentID(attributes);
        if (this.m_shouldProcess) {
            flushCharacters();
            pushSpaceHandling(attributes);
            XSLTElementProcessor elemProcessor = getProcessorFor(uri, localName, rawName);
            if (elemProcessor != null) {
                pushProcessor(elemProcessor);
                elemProcessor.startElement(this, uri, localName, rawName, attributes);
            } else {
                this.m_shouldProcess = false;
                popSpaceHandling();
            }
        }
    }

    public void endElement(String uri, String localName, String rawName) throws SAXException {
        this.m_elementID--;
        if (this.m_shouldProcess) {
            if (this.m_elementID + 1 == this.m_fragmentID) {
                this.m_shouldProcess = false;
            }
            flushCharacters();
            popSpaceHandling();
            getCurrentProcessor().endElement(this, uri, localName, rawName);
            popProcessor();
            getNamespaceSupport().popContext();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.m_shouldProcess) {
            XSLTElementProcessor elemProcessor = getCurrentProcessor();
            XSLTElementDef def = elemProcessor.getElemDef();
            if (def.getType() != 2) {
                elemProcessor = def.getProcessorFor(null, "text()");
            }
            if (elemProcessor != null) {
                elemProcessor.characters(this, ch, start, length);
            } else if (!XMLCharacterRecognizer.isWhiteSpace(ch, start, length)) {
                error(XSLMessages.createMessage(XSLTErrorResources.ER_NONWHITESPACE_NOT_ALLOWED_IN_POSITION, null), null);
            }
        }
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        if (this.m_shouldProcess) {
            getCurrentProcessor().ignorableWhitespace(this, ch, start, length);
        }
    }

    public void processingInstruction(String target, String data) throws SAXException {
        if (this.m_shouldProcess) {
            String prefix = "";
            String ns = "";
            String localName = target;
            int colon = target.indexOf(58);
            if (colon >= 0) {
                ns = getNamespaceForPrefix(target.substring(0, colon));
                localName = target.substring(colon + 1);
            }
            try {
                if ("xalan-doc-cache-off".equals(target) || "xalan:doc-cache-off".equals(target) || ("doc-cache-off".equals(localName) && ns.equals("org.apache.xalan.xslt.extensions.Redirect"))) {
                    if (this.m_elems.peek() instanceof ElemForEach) {
                        ((ElemForEach) this.m_elems.peek()).m_doc_cache_off = true;
                    } else {
                        throw new TransformerException("xalan:doc-cache-off not allowed here!", getLocator());
                    }
                }
            } catch (Exception e) {
            }
            flushCharacters();
            getCurrentProcessor().processingInstruction(this, target, data);
        }
    }

    public void skippedEntity(String name) throws SAXException {
        if (this.m_shouldProcess) {
            getCurrentProcessor().skippedEntity(this, name);
        }
    }

    public void warn(String msg, Object[] args) throws SAXException {
        String formattedMsg = XSLMessages.createWarning(msg, args);
        SAXSourceLocator locator = getLocator();
        ErrorListener handler = this.m_stylesheetProcessor.getErrorListener();
        if (handler != null) {
            try {
                handler.warning(new TransformerException(formattedMsg, locator));
            } catch (TransformerException te) {
                throw new SAXException(te);
            }
        }
    }

    private void assertion(boolean condition, String msg) throws RuntimeException {
        if (!condition) {
            throw new RuntimeException(msg);
        }
    }

    protected void error(String msg, Exception e) throws SAXException {
        TransformerException pe;
        SAXSourceLocator locator = getLocator();
        ErrorListener handler = this.m_stylesheetProcessor.getErrorListener();
        if (e instanceof TransformerException) {
            pe = (TransformerException) e;
        } else if (e == null) {
            pe = new TransformerException(msg, locator);
        } else {
            pe = new TransformerException(msg, locator, e);
        }
        if (handler != null) {
            try {
                handler.error(pe);
                return;
            } catch (TransformerException te) {
                throw new SAXException(te);
            }
        }
        throw new SAXException(pe);
    }

    protected void error(String msg, Object[] args, Exception e) throws SAXException {
        error(XSLMessages.createMessage(msg, args), e);
    }

    public void warning(SAXParseException e) throws SAXException {
        String formattedMsg = e.getMessage();
        SAXSourceLocator locator = getLocator();
        try {
            this.m_stylesheetProcessor.getErrorListener().warning(new TransformerException(formattedMsg, locator));
        } catch (TransformerException te) {
            throw new SAXException(te);
        }
    }

    public void error(SAXParseException e) throws SAXException {
        String formattedMsg = e.getMessage();
        SAXSourceLocator locator = getLocator();
        try {
            this.m_stylesheetProcessor.getErrorListener().error(new TransformerException(formattedMsg, locator));
        } catch (TransformerException te) {
            throw new SAXException(te);
        }
    }

    public void fatalError(SAXParseException e) throws SAXException {
        String formattedMsg = e.getMessage();
        SAXSourceLocator locator = getLocator();
        try {
            this.m_stylesheetProcessor.getErrorListener().fatalError(new TransformerException(formattedMsg, locator));
        } catch (TransformerException te) {
            throw new SAXException(te);
        }
    }

    private void checkForFragmentID(Attributes attributes) {
        if (!this.m_shouldProcess && attributes != null && this.m_fragmentIDString != null) {
            int n = attributes.getLength();
            int i = 0;
            while (i < n) {
                if (attributes.getQName(i).equals("id") && attributes.getValue(i).equalsIgnoreCase(this.m_fragmentIDString)) {
                    this.m_shouldProcess = true;
                    this.m_fragmentID = this.m_elementID;
                }
                i++;
            }
        }
    }

    public TransformerFactoryImpl getStylesheetProcessor() {
        return this.m_stylesheetProcessor;
    }

    int getStylesheetType() {
        return this.m_stylesheetType;
    }

    void setStylesheetType(int type) {
        this.m_stylesheetType = type;
    }

    Stylesheet getStylesheet() {
        return this.m_stylesheets.size() == 0 ? null : (Stylesheet) this.m_stylesheets.peek();
    }

    Stylesheet getLastPoppedStylesheet() {
        return this.m_lastPoppedStylesheet;
    }

    public StylesheetRoot getStylesheetRoot() {
        if (this.m_stylesheetRoot != null) {
            this.m_stylesheetRoot.setOptimizer(this.m_optimize);
            this.m_stylesheetRoot.setIncremental(this.m_incremental);
            this.m_stylesheetRoot.setSource_location(this.m_source_location);
        }
        return this.m_stylesheetRoot;
    }

    public void pushStylesheet(Stylesheet s) {
        if (this.m_stylesheets.size() == 0) {
            this.m_stylesheetRoot = (StylesheetRoot) s;
        }
        this.m_stylesheets.push(s);
    }

    Stylesheet popStylesheet() {
        if (!this.m_stylesheetLocatorStack.isEmpty()) {
            this.m_stylesheetLocatorStack.pop();
        }
        if (!this.m_stylesheets.isEmpty()) {
            this.m_lastPoppedStylesheet = (Stylesheet) this.m_stylesheets.pop();
        }
        return this.m_lastPoppedStylesheet;
    }

    XSLTElementProcessor getCurrentProcessor() {
        return (XSLTElementProcessor) this.m_processors.peek();
    }

    void pushProcessor(XSLTElementProcessor processor) {
        this.m_processors.push(processor);
    }

    XSLTElementProcessor popProcessor() {
        return (XSLTElementProcessor) this.m_processors.pop();
    }

    public XSLTSchema getSchema() {
        return this.m_schema;
    }

    ElemTemplateElement getElemTemplateElement() {
        try {
            return (ElemTemplateElement) this.m_elems.peek();
        } catch (EmptyStackException e) {
            return null;
        }
    }

    int nextUid() {
        int i = this.m_docOrderCount;
        this.m_docOrderCount = i + 1;
        return i;
    }

    void pushElemTemplateElement(ElemTemplateElement elem) {
        if (elem.getUid() == -1) {
            elem.setUid(nextUid());
        }
        this.m_elems.push(elem);
    }

    ElemTemplateElement popElemTemplateElement() {
        return (ElemTemplateElement) this.m_elems.pop();
    }

    void pushBaseIndentifier(String baseID) {
        if (baseID != null) {
            int posOfHash = baseID.indexOf(35);
            if (posOfHash > -1) {
                this.m_fragmentIDString = baseID.substring(posOfHash + 1);
                this.m_shouldProcess = false;
            } else {
                this.m_shouldProcess = true;
            }
        } else {
            this.m_shouldProcess = true;
        }
        this.m_baseIdentifiers.push(baseID);
    }

    String popBaseIndentifier() {
        return (String) this.m_baseIdentifiers.pop();
    }

    public String getBaseIdentifier() {
        String base = null;
        if (!this.m_baseIdentifiers.isEmpty()) {
            base = this.m_baseIdentifiers.peek();
        }
        base = base;
        if (base != null) {
            return base;
        }
        SourceLocator locator = getLocator();
        return locator == null ? "" : locator.getSystemId();
    }

    public SAXSourceLocator getLocator() {
        if (!this.m_stylesheetLocatorStack.isEmpty()) {
            return (SAXSourceLocator) this.m_stylesheetLocatorStack.peek();
        }
        SAXSourceLocator locator = new SAXSourceLocator();
        locator.setSystemId(getStylesheetProcessor().getDOMsystemID());
        return locator;
    }

    void pushImportURL(String hrefUrl) {
        this.m_importStack.push(hrefUrl);
    }

    void pushImportSource(Source sourceFromURIResolver) {
        this.m_importSourceStack.push(sourceFromURIResolver);
    }

    boolean importStackContains(String hrefUrl) {
        return stackContains(this.m_importStack, hrefUrl);
    }

    String popImportURL() {
        return (String) this.m_importStack.pop();
    }

    String peekImportURL() {
        return (String) this.m_importStack.peek();
    }

    Source peekSourceFromURIResolver() {
        return (Source) this.m_importSourceStack.peek();
    }

    Source popImportSource() {
        return (Source) this.m_importSourceStack.pop();
    }

    void pushNewNamespaceSupport() {
        this.m_nsSupportStack.push(new NamespaceSupport2());
    }

    void popNamespaceSupport() {
        this.m_nsSupportStack.pop();
    }

    NamespaceSupport getNamespaceSupport() {
        return (NamespaceSupport) this.m_nsSupportStack.peek();
    }

    public void setOriginatingNode(Node n) {
        this.m_originatingNode = n;
    }

    public Node getOriginatingNode() {
        return this.m_originatingNode;
    }

    boolean isSpacePreserve() {
        return this.m_spacePreserveStack.peek();
    }

    void popSpaceHandling() {
        this.m_spacePreserveStack.pop();
    }

    void pushSpaceHandling(boolean b) throws SAXParseException {
        this.m_spacePreserveStack.push(b);
    }

    void pushSpaceHandling(Attributes attrs) throws SAXParseException {
        String value = attrs.getValue(org.apache.xalan.templates.Constants.ATTRNAME_XMLSPACE);
        if (value == null) {
            this.m_spacePreserveStack.push(this.m_spacePreserveStack.peekOrFalse());
        } else if (value.equals("preserve")) {
            this.m_spacePreserveStack.push(true);
        } else if (value.equals(org.apache.xalan.templates.Constants.ATTRNAME_DEFAULT)) {
            this.m_spacePreserveStack.push(false);
        } else {
            SAXSourceLocator locator = getLocator();
            try {
                this.m_stylesheetProcessor.getErrorListener().error(new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_ILLEGAL_XMLSPACE_VALUE, null), locator));
                this.m_spacePreserveStack.push(this.m_spacePreserveStack.peek());
            } catch (TransformerException te) {
                throw new SAXParseException(te.getMessage(), locator, te);
            }
        }
    }

    private double getElemVersion() {
        ElemTemplateElement elem = getElemTemplateElement();
        double version = -1.0d;
        while (true) {
            if ((version == -1.0d || version == 1.0d) && elem != null) {
                try {
                    version = Double.valueOf(elem.getXmlVersion()).doubleValue();
                } catch (Exception e) {
                    version = -1.0d;
                }
                elem = elem.getParentElem();
            }
        }
        return version == -1.0d ? 1.0d : version;
    }

    public boolean handlesNullPrefixes() {
        return false;
    }

    public boolean getOptimize() {
        return this.m_optimize;
    }

    public boolean getIncremental() {
        return this.m_incremental;
    }

    public boolean getSource_location() {
        return this.m_source_location;
    }
}
