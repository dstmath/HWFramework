package org.apache.xml.serializer;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Transformer;
import org.apache.xalan.templates.Constants;
import org.apache.xml.serializer.dom3.DOM3SerializerImpl;
import org.apache.xml.serializer.utils.MsgKey;
import org.apache.xml.serializer.utils.Utils;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class SerializerBase implements SerializationHandler, SerializerConstants {
    public static final String PKG_NAME;
    public static final String PKG_PATH;
    protected Hashtable m_CdataElems = null;
    private HashMap m_OutputProps;
    private HashMap m_OutputPropsDefault;
    protected String m_StringOfCDATASections = null;
    protected char[] m_attrBuff = new char[30];
    protected AttributesImplSerializer m_attributes = new AttributesImplSerializer();
    protected boolean m_cdataTagOpen = false;
    protected char[] m_charsBuff = new char[60];
    protected boolean m_doIndent = false;
    boolean m_docIsEmpty = true;
    protected String m_doctypePublic;
    protected String m_doctypeSystem;
    protected ElemContext m_elemContext = new ElemContext();
    protected boolean m_inEntityRef = false;
    protected boolean m_inExternalDTD = false;
    protected int m_indentAmount = 0;
    protected String m_mediatype;
    protected boolean m_needToCallStartDocument = true;
    boolean m_needToOutputDocTypeDecl = true;
    protected NamespaceMappings m_prefixMap;
    protected boolean m_shouldNotWriteXMLHeader = false;
    protected SourceLocator m_sourceLocator;
    private String m_standalone;
    protected boolean m_standaloneWasSpecified = false;
    protected SerializerTrace m_tracer;
    private Transformer m_transformer;
    protected String m_version = null;
    protected Writer m_writer = null;

    SerializerBase() {
    }

    static {
        String fullyQualifiedName = SerializerBase.class.getName();
        int lastDot = fullyQualifiedName.lastIndexOf(46);
        if (lastDot < 0) {
            PKG_NAME = "";
        } else {
            PKG_NAME = fullyQualifiedName.substring(0, lastDot);
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < PKG_NAME.length(); i++) {
            char ch = PKG_NAME.charAt(i);
            if (ch == '.') {
                sb.append('/');
            } else {
                sb.append(ch);
            }
        }
        PKG_PATH = sb.toString();
    }

    protected void fireEndElem(String name) throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
            this.m_tracer.fireGenerateEvent(4, name, (Attributes) null);
        }
    }

    protected void fireCharEvent(char[] chars, int start, int length) throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
            this.m_tracer.fireGenerateEvent(5, chars, start, length);
        }
    }

    public void comment(String data) throws SAXException {
        this.m_docIsEmpty = false;
        int length = data.length();
        if (length > this.m_charsBuff.length) {
            this.m_charsBuff = new char[((length * 2) + 1)];
        }
        data.getChars(0, length, this.m_charsBuff, 0);
        comment(this.m_charsBuff, 0, length);
    }

    protected String patchName(String qname) {
        int lastColon = qname.lastIndexOf(58);
        if (lastColon > 0) {
            int firstColon = qname.indexOf(58);
            String prefix = qname.substring(0, firstColon);
            String localName = qname.substring(lastColon + 1);
            String uri = this.m_prefixMap.lookupNamespace(prefix);
            if (uri != null && uri.length() == 0) {
                return localName;
            }
            if (firstColon != lastColon) {
                return prefix + ':' + localName;
            }
        }
        return qname;
    }

    protected static String getLocalName(String qname) {
        int col = qname.lastIndexOf(58);
        return col > 0 ? qname.substring(col + 1) : qname;
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void addAttribute(String uri, String localName, String rawName, String type, String value, boolean XSLAttribute) throws SAXException {
        if (this.m_elemContext.m_startTagOpen) {
            addAttributeAlways(uri, localName, rawName, type, value, XSLAttribute);
        }
    }

    public boolean addAttributeAlways(String uri, String localName, String rawName, String type, String value, boolean XSLAttribute) {
        int index;
        if (localName == null || uri == null || uri.length() == 0) {
            index = this.m_attributes.getIndex(rawName);
        } else {
            index = this.m_attributes.getIndex(uri, localName);
        }
        if (index >= 0) {
            this.m_attributes.setValue(index, value);
            return false;
        }
        this.m_attributes.addAttribute(uri, localName, rawName, type, value);
        return true;
    }

    public void addAttribute(String name, String value) {
        if (this.m_elemContext.m_startTagOpen) {
            String patchedName = patchName(name);
            addAttributeAlways(getNamespaceURI(patchedName, false), getLocalName(patchedName), patchedName, "CDATA", value, false);
        }
    }

    public void addXSLAttribute(String name, String value, String uri) {
        if (this.m_elemContext.m_startTagOpen) {
            String patchedName = patchName(name);
            addAttributeAlways(uri, getLocalName(patchedName), patchedName, "CDATA", value, true);
        }
    }

    public void addAttributes(Attributes atts) throws SAXException {
        int nAtts = atts.getLength();
        for (int i = 0; i < nAtts; i++) {
            String uri = atts.getURI(i);
            if (uri == null) {
                uri = "";
            }
            addAttributeAlways(uri, atts.getLocalName(i), atts.getQName(i), atts.getType(i), atts.getValue(i), false);
        }
    }

    public ContentHandler asContentHandler() throws IOException {
        return this;
    }

    public void endEntity(String name) throws SAXException {
        if (name.equals("[dtd]")) {
            this.m_inExternalDTD = false;
        }
        this.m_inEntityRef = false;
        if (this.m_tracer != null) {
            fireEndEntity(name);
        }
    }

    public void close() {
    }

    protected void initCDATA() {
    }

    public String getEncoding() {
        return getOutputProperty("encoding");
    }

    public void setEncoding(String encoding) {
        setOutputProperty("encoding", encoding);
    }

    public void setOmitXMLDeclaration(boolean b) {
        setOutputProperty("omit-xml-declaration", b ? "yes" : "no");
    }

    public boolean getOmitXMLDeclaration() {
        return this.m_shouldNotWriteXMLHeader;
    }

    public String getDoctypePublic() {
        return this.m_doctypePublic;
    }

    public void setDoctypePublic(String doctypePublic) {
        setOutputProperty(Constants.ATTRNAME_OUTPUT_DOCTYPE_PUBLIC, doctypePublic);
    }

    public String getDoctypeSystem() {
        return this.m_doctypeSystem;
    }

    public void setDoctypeSystem(String doctypeSystem) {
        setOutputProperty(Constants.ATTRNAME_OUTPUT_DOCTYPE_SYSTEM, doctypeSystem);
    }

    public void setDoctype(String doctypeSystem, String doctypePublic) {
        setOutputProperty(Constants.ATTRNAME_OUTPUT_DOCTYPE_SYSTEM, doctypeSystem);
        setOutputProperty(Constants.ATTRNAME_OUTPUT_DOCTYPE_PUBLIC, doctypePublic);
    }

    public void setStandalone(String standalone) {
        setOutputProperty(Constants.ATTRNAME_OUTPUT_STANDALONE, standalone);
    }

    protected void setStandaloneInternal(String standalone) {
        if ("yes".equals(standalone)) {
            this.m_standalone = "yes";
        } else {
            this.m_standalone = "no";
        }
    }

    public String getStandalone() {
        return this.m_standalone;
    }

    public boolean getIndent() {
        return this.m_doIndent;
    }

    public String getMediaType() {
        return this.m_mediatype;
    }

    public String getVersion() {
        return this.m_version;
    }

    public void setVersion(String version) {
        setOutputProperty("version", version);
    }

    public void setMediaType(String mediaType) {
        setOutputProperty(Constants.ATTRNAME_OUTPUT_MEDIATYPE, mediaType);
    }

    public int getIndentAmount() {
        return this.m_indentAmount;
    }

    public void setIndentAmount(int m_indentAmount) {
        this.m_indentAmount = m_indentAmount;
    }

    public void setIndent(boolean doIndent) {
        setOutputProperty("indent", doIndent ? "yes" : "no");
    }

    public void namespaceAfterStartElement(String uri, String prefix) throws SAXException {
    }

    public DOMSerializer asDOMSerializer() throws IOException {
        return this;
    }

    private static final boolean subPartMatch(String p, String t) {
        if (p != t) {
            return p != null ? p.equals(t) : false;
        } else {
            return true;
        }
    }

    protected static final String getPrefixPart(String qname) {
        int col = qname.indexOf(58);
        return col > 0 ? qname.substring(0, col) : null;
    }

    public NamespaceMappings getNamespaceMappings() {
        return this.m_prefixMap;
    }

    public String getPrefix(String namespaceURI) {
        return this.m_prefixMap.lookupPrefix(namespaceURI);
    }

    public String getNamespaceURI(String qname, boolean isElement) {
        String uri = "";
        int col = qname.lastIndexOf(58);
        String prefix = col > 0 ? qname.substring(0, col) : "";
        if ((!"".equals(prefix) || isElement) && this.m_prefixMap != null) {
            uri = this.m_prefixMap.lookupNamespace(prefix);
            if (uri == null && (prefix.equals("xmlns") ^ 1) != 0) {
                throw new RuntimeException(Utils.messages.createMessage(MsgKey.ER_NAMESPACE_PREFIX, new Object[]{qname.substring(0, col)}));
            }
        }
        return uri;
    }

    public String getNamespaceURIFromPrefix(String prefix) {
        if (this.m_prefixMap != null) {
            return this.m_prefixMap.lookupNamespace(prefix);
        }
        return null;
    }

    public void entityReference(String name) throws SAXException {
        flushPending();
        startEntity(name);
        endEntity(name);
        if (this.m_tracer != null) {
            fireEntityReference(name);
        }
    }

    public void setTransformer(Transformer t) {
        this.m_transformer = t;
        if ((this.m_transformer instanceof SerializerTrace) && ((SerializerTrace) this.m_transformer).hasTraceListeners()) {
            this.m_tracer = (SerializerTrace) this.m_transformer;
        } else {
            this.m_tracer = null;
        }
    }

    public Transformer getTransformer() {
        return this.m_transformer;
    }

    public void characters(Node node) throws SAXException {
        flushPending();
        String data = node.getNodeValue();
        if (data != null) {
            int length = data.length();
            if (length > this.m_charsBuff.length) {
                this.m_charsBuff = new char[((length * 2) + 1)];
            }
            data.getChars(0, length, this.m_charsBuff, 0);
            characters(this.m_charsBuff, 0, length);
        }
    }

    public void error(SAXParseException exc) throws SAXException {
    }

    public void fatalError(SAXParseException exc) throws SAXException {
        this.m_elemContext.m_startTagOpen = false;
    }

    public void warning(SAXParseException exc) throws SAXException {
    }

    protected void fireStartEntity(String name) throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
            this.m_tracer.fireGenerateEvent(9, name);
        }
    }

    private void flushMyWriter() {
        if (this.m_writer != null) {
            try {
                this.m_writer.flush();
            } catch (IOException e) {
            }
        }
    }

    protected void fireCDATAEvent(char[] chars, int start, int length) throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
            this.m_tracer.fireGenerateEvent(10, chars, start, length);
        }
    }

    protected void fireCommentEvent(char[] chars, int start, int length) throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
            this.m_tracer.fireGenerateEvent(8, new String(chars, start, length));
        }
    }

    public void fireEndEntity(String name) throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
        }
    }

    protected void fireStartDoc() throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
            this.m_tracer.fireGenerateEvent(1);
        }
    }

    protected void fireEndDoc() throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
            this.m_tracer.fireGenerateEvent(2);
        }
    }

    protected void fireStartElem(String elemName) throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
            this.m_tracer.fireGenerateEvent(3, elemName, this.m_attributes);
        }
    }

    protected void fireEscapingEvent(String name, String data) throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
            this.m_tracer.fireGenerateEvent(7, name, data);
        }
    }

    protected void fireEntityReference(String name) throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
            this.m_tracer.fireGenerateEvent(9, name, (Attributes) null);
        }
    }

    public void startDocument() throws SAXException {
        startDocumentInternal();
        this.m_needToCallStartDocument = false;
    }

    protected void startDocumentInternal() throws SAXException {
        if (this.m_tracer != null) {
            fireStartDoc();
        }
    }

    public void setSourceLocator(SourceLocator locator) {
        this.m_sourceLocator = locator;
    }

    public void setNamespaceMappings(NamespaceMappings mappings) {
        this.m_prefixMap = mappings;
    }

    public boolean reset() {
        resetSerializerBase();
        return true;
    }

    private void resetSerializerBase() {
        this.m_attributes.clear();
        this.m_CdataElems = null;
        this.m_cdataTagOpen = false;
        this.m_docIsEmpty = true;
        this.m_doctypePublic = null;
        this.m_doctypeSystem = null;
        this.m_doIndent = false;
        this.m_elemContext = new ElemContext();
        this.m_indentAmount = 0;
        this.m_inEntityRef = false;
        this.m_inExternalDTD = false;
        this.m_mediatype = null;
        this.m_needToCallStartDocument = true;
        this.m_needToOutputDocTypeDecl = false;
        if (this.m_OutputProps != null) {
            this.m_OutputProps.clear();
        }
        if (this.m_OutputPropsDefault != null) {
            this.m_OutputPropsDefault.clear();
        }
        if (this.m_prefixMap != null) {
            this.m_prefixMap.reset();
        }
        this.m_shouldNotWriteXMLHeader = false;
        this.m_sourceLocator = null;
        this.m_standalone = null;
        this.m_standaloneWasSpecified = false;
        this.m_StringOfCDATASections = null;
        this.m_tracer = null;
        this.m_transformer = null;
        this.m_version = null;
    }

    final boolean inTemporaryOutputState() {
        return getEncoding() == null;
    }

    public void addAttribute(String uri, String localName, String rawName, String type, String value) throws SAXException {
        if (this.m_elemContext.m_startTagOpen) {
            addAttributeAlways(uri, localName, rawName, type, value, false);
        }
    }

    public void notationDecl(String arg0, String arg1, String arg2) throws SAXException {
    }

    public void unparsedEntityDecl(String arg0, String arg1, String arg2, String arg3) throws SAXException {
    }

    public void setDTDEntityExpansion(boolean expand) {
    }

    void initCdataElems(String s) {
        if (s != null) {
            String localName;
            int max = s.length();
            boolean inCurly = false;
            boolean foundURI = false;
            StringBuffer buf = new StringBuffer();
            String uri = null;
            for (int i = 0; i < max; i++) {
                char c = s.charAt(i);
                if (Character.isWhitespace(c)) {
                    if (inCurly) {
                        buf.append(c);
                    } else if (buf.length() > 0) {
                        localName = buf.toString();
                        if (!foundURI) {
                            uri = "";
                        }
                        addCDATAElement(uri, localName);
                        buf.setLength(0);
                        foundURI = false;
                    }
                } else if ('{' == c) {
                    inCurly = true;
                } else if ('}' == c) {
                    foundURI = true;
                    uri = buf.toString();
                    buf.setLength(0);
                    inCurly = false;
                } else {
                    buf.append(c);
                }
            }
            if (buf.length() > 0) {
                localName = buf.toString();
                if (!foundURI) {
                    uri = "";
                }
                addCDATAElement(uri, localName);
            }
        }
    }

    private void addCDATAElement(String uri, String localName) {
        if (this.m_CdataElems == null) {
            this.m_CdataElems = new Hashtable();
        }
        Hashtable h = (Hashtable) this.m_CdataElems.get(localName);
        if (h == null) {
            h = new Hashtable();
            this.m_CdataElems.put(localName, h);
        }
        h.put(uri, uri);
    }

    public boolean documentIsEmpty() {
        return this.m_docIsEmpty && this.m_elemContext.m_currentElemDepth == 0;
    }

    protected boolean isCdataSection() {
        if (this.m_StringOfCDATASections == null) {
            return false;
        }
        if (this.m_elemContext.m_elementLocalName == null) {
            this.m_elemContext.m_elementLocalName = getLocalName(this.m_elemContext.m_elementName);
        }
        if (this.m_elemContext.m_elementURI == null) {
            this.m_elemContext.m_elementURI = getElementURI();
        } else if (this.m_elemContext.m_elementURI.length() == 0) {
            if (this.m_elemContext.m_elementName == null) {
                this.m_elemContext.m_elementName = this.m_elemContext.m_elementLocalName;
            } else if (this.m_elemContext.m_elementLocalName.length() < this.m_elemContext.m_elementName.length()) {
                this.m_elemContext.m_elementURI = getElementURI();
            }
        }
        Hashtable h = (Hashtable) this.m_CdataElems.get(this.m_elemContext.m_elementLocalName);
        if (h == null || h.get(this.m_elemContext.m_elementURI) == null) {
            return false;
        }
        return true;
    }

    private String getElementURI() {
        String uri;
        String prefix = getPrefixPart(this.m_elemContext.m_elementName);
        if (prefix == null) {
            uri = this.m_prefixMap.lookupNamespace("");
        } else {
            uri = this.m_prefixMap.lookupNamespace(prefix);
        }
        if (uri == null) {
            return "";
        }
        return uri;
    }

    public String getOutputProperty(String name) {
        String val = getOutputPropertyNonDefault(name);
        if (val == null) {
            return getOutputPropertyDefault(name);
        }
        return val;
    }

    public String getOutputPropertyNonDefault(String name) {
        return getProp(name, false);
    }

    public Object asDOM3Serializer() throws IOException {
        return new DOM3SerializerImpl(this);
    }

    public String getOutputPropertyDefault(String name) {
        return getProp(name, true);
    }

    public void setOutputProperty(String name, String val) {
        setProp(name, val, false);
    }

    public void setOutputPropertyDefault(String name, String val) {
        setProp(name, val, true);
    }

    Set getOutputPropDefaultKeys() {
        return this.m_OutputPropsDefault.keySet();
    }

    Set getOutputPropKeys() {
        return this.m_OutputProps.keySet();
    }

    private String getProp(String name, boolean defaultVal) {
        if (this.m_OutputProps == null) {
            this.m_OutputProps = new HashMap();
            this.m_OutputPropsDefault = new HashMap();
        }
        if (defaultVal) {
            return (String) this.m_OutputPropsDefault.get(name);
        }
        return (String) this.m_OutputProps.get(name);
    }

    void setProp(String name, String val, boolean defaultVal) {
        if (this.m_OutputProps == null) {
            this.m_OutputProps = new HashMap();
            this.m_OutputPropsDefault = new HashMap();
        }
        if (defaultVal) {
            this.m_OutputPropsDefault.put(name, val);
        } else if (!Constants.ATTRNAME_OUTPUT_CDATA_SECTION_ELEMENTS.equals(name) || val == null) {
            this.m_OutputProps.put(name, val);
        } else {
            String newVal;
            initCdataElems(val);
            String oldVal = (String) this.m_OutputProps.get(name);
            if (oldVal == null) {
                newVal = oldVal + ' ' + val;
            } else {
                newVal = val;
            }
            this.m_OutputProps.put(name, newVal);
        }
    }

    static char getFirstCharLocName(String name) {
        int i = name.indexOf(125);
        if (i < 0) {
            return name.charAt(0);
        }
        return name.charAt(i + 1);
    }
}
