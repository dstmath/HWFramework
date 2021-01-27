package ohos.com.sun.org.apache.xml.internal.serializer;

import java.io.IOException;
import java.io.Writer;
import java.util.Vector;
import ohos.com.sun.org.apache.xml.internal.serializer.utils.Utils;
import ohos.data.search.model.IndexType;
import ohos.javax.xml.transform.SourceLocator;
import ohos.javax.xml.transform.Transformer;
import ohos.org.w3c.dom.Node;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXParseException;
import ohos.org.xml.sax.ext.Locator2;

public abstract class SerializerBase implements SerializationHandler, SerializerConstants {
    protected char[] m_attrBuff = new char[30];
    protected AttributesImplSerializer m_attributes = new AttributesImplSerializer();
    protected Vector m_cdataSectionElements = null;
    protected boolean m_cdataTagOpen = false;
    protected char[] m_charsBuff = new char[60];
    protected boolean m_doIndent = false;
    private String m_doctypePublic;
    private String m_doctypeSystem;
    protected ElemContext m_elemContext = new ElemContext();
    private String m_encoding = null;
    protected boolean m_inEntityRef = false;
    protected boolean m_inExternalDTD = false;
    protected int m_indentAmount = 0;
    protected boolean m_isStandalone = false;
    private Locator m_locator = null;
    private String m_mediatype;
    protected boolean m_needToCallSetDocumentInfo = true;
    protected boolean m_needToCallStartDocument = true;
    boolean m_needToOutputDocTypeDecl = true;
    protected NamespaceMappings m_prefixMap;
    private boolean m_shouldNotWriteXMLHeader = false;
    protected SourceLocator m_sourceLocator;
    private String m_standalone;
    protected boolean m_standaloneWasSpecified = false;
    protected SerializerTrace m_tracer;
    private Transformer m_transformer;
    private String m_version = null;
    protected Writer m_writer = null;

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public ContentHandler asContentHandler() throws IOException {
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public DOMSerializer asDOMSerializer() throws IOException {
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public void close() {
    }

    public void error(SAXParseException sAXParseException) throws SAXException {
    }

    /* access modifiers changed from: protected */
    public void initCDATA() {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void namespaceAfterStartElement(String str, String str2) throws SAXException {
    }

    public void notationDecl(String str, String str2, String str3) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public void setDTDEntityExpansion(boolean z) {
    }

    public void unparsedEntityDecl(String str, String str2, String str3, String str4) throws SAXException {
    }

    public void warning(SAXParseException sAXParseException) throws SAXException {
    }

    /* access modifiers changed from: protected */
    public void fireEndElem(String str) throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
            this.m_tracer.fireGenerateEvent(4, str, (Attributes) null);
        }
    }

    /* access modifiers changed from: protected */
    public void fireCharEvent(char[] cArr, int i, int i2) throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
            this.m_tracer.fireGenerateEvent(5, cArr, i, i2);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedLexicalHandler
    public void comment(String str) throws SAXException {
        int length = str.length();
        if (length > this.m_charsBuff.length) {
            this.m_charsBuff = new char[((length * 2) + 1)];
        }
        str.getChars(0, length, this.m_charsBuff, 0);
        comment(this.m_charsBuff, 0, length);
    }

    /* access modifiers changed from: protected */
    public String patchName(String str) {
        int lastIndexOf = str.lastIndexOf(58);
        if (lastIndexOf > 0) {
            int indexOf = str.indexOf(58);
            String substring = str.substring(0, indexOf);
            String substring2 = str.substring(lastIndexOf + 1);
            String lookupNamespace = this.m_prefixMap.lookupNamespace(substring);
            if (lookupNamespace != null && lookupNamespace.length() == 0) {
                return substring2;
            }
            if (indexOf != lastIndexOf) {
                return substring + ':' + substring2;
            }
        }
        return str;
    }

    protected static String getLocalName(String str) {
        int lastIndexOf = str.lastIndexOf(58);
        return lastIndexOf > 0 ? str.substring(lastIndexOf + 1) : str;
    }

    public void setDocumentLocator(Locator locator) {
        this.m_locator = locator;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void addAttribute(String str, String str2, String str3, String str4, String str5, boolean z) throws SAXException {
        if (this.m_elemContext.m_startTagOpen) {
            addAttributeAlways(str, str2, str3, str4, str5, z);
        }
    }

    public boolean addAttributeAlways(String str, String str2, String str3, String str4, String str5, boolean z) {
        int i;
        if (str2 == null || str == null || str.length() == 0) {
            i = this.m_attributes.getIndex(str3);
        } else {
            i = this.m_attributes.getIndex(str, str2);
        }
        if (i >= 0) {
            this.m_attributes.setValue(i, str5);
            return false;
        }
        this.m_attributes.addAttribute(str, str2, str3, str4, str5);
        return true;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void addAttribute(String str, String str2) {
        if (this.m_elemContext.m_startTagOpen) {
            String patchName = patchName(str);
            addAttributeAlways(getNamespaceURI(patchName, false), getLocalName(patchName), patchName, "CDATA", str2, false);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void addXSLAttribute(String str, String str2, String str3) {
        if (this.m_elemContext.m_startTagOpen) {
            String patchName = patchName(str);
            addAttributeAlways(str3, getLocalName(patchName), patchName, "CDATA", str2, true);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void addAttributes(Attributes attributes) throws SAXException {
        int length = attributes.getLength();
        for (int i = 0; i < length; i++) {
            String uri = attributes.getURI(i);
            if (uri == null) {
                uri = "";
            }
            addAttributeAlways(uri, attributes.getLocalName(i), attributes.getQName(i), attributes.getType(i), attributes.getValue(i), false);
        }
    }

    public void endEntity(String str) throws SAXException {
        if (str.equals("[dtd]")) {
            this.m_inExternalDTD = false;
        }
        this.m_inEntityRef = false;
        if (this.m_tracer != null) {
            fireEndEntity(str);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public String getEncoding() {
        return this.m_encoding;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public void setEncoding(String str) {
        this.m_encoding = str;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public void setOmitXMLDeclaration(boolean z) {
        this.m_shouldNotWriteXMLHeader = z;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public boolean getOmitXMLDeclaration() {
        return this.m_shouldNotWriteXMLHeader;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public String getDoctypePublic() {
        return this.m_doctypePublic;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public void setDoctypePublic(String str) {
        this.m_doctypePublic = str;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public String getDoctypeSystem() {
        return this.m_doctypeSystem;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public void setDoctypeSystem(String str) {
        this.m_doctypeSystem = str;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public void setDoctype(String str, String str2) {
        this.m_doctypeSystem = str;
        this.m_doctypePublic = str2;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public void setStandalone(String str) {
        if (str != null) {
            this.m_standaloneWasSpecified = true;
            setStandaloneInternal(str);
        }
    }

    /* access modifiers changed from: protected */
    public void setStandaloneInternal(String str) {
        if ("yes".equals(str)) {
            this.m_standalone = "yes";
        } else {
            this.m_standalone = IndexType.NO;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public String getStandalone() {
        return this.m_standalone;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public boolean getIndent() {
        return this.m_doIndent;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public String getMediaType() {
        return this.m_mediatype;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public String getVersion() {
        return this.m_version;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public void setVersion(String str) {
        this.m_version = str;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public void setMediaType(String str) {
        this.m_mediatype = str;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public int getIndentAmount() {
        return this.m_indentAmount;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public void setIndentAmount(int i) {
        this.m_indentAmount = i;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public void setIndent(boolean z) {
        this.m_doIndent = z;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public void setIsStandalone(boolean z) {
        this.m_isStandalone = z;
    }

    /* access modifiers changed from: protected */
    public boolean isCdataSection() {
        String prefixPart;
        if (this.m_cdataSectionElements == null) {
            return false;
        }
        if (this.m_elemContext.m_elementLocalName == null) {
            ElemContext elemContext = this.m_elemContext;
            elemContext.m_elementLocalName = getLocalName(elemContext.m_elementName);
        }
        if (this.m_elemContext.m_elementURI == null && (prefixPart = getPrefixPart(this.m_elemContext.m_elementName)) != null) {
            this.m_elemContext.m_elementURI = this.m_prefixMap.lookupNamespace(prefixPart);
        }
        if (this.m_elemContext.m_elementURI != null && this.m_elemContext.m_elementURI.length() == 0) {
            this.m_elemContext.m_elementURI = null;
        }
        int size = this.m_cdataSectionElements.size();
        for (int i = 0; i < size; i += 2) {
            String str = (String) this.m_cdataSectionElements.elementAt(i);
            if (((String) this.m_cdataSectionElements.elementAt(i + 1)).equals(this.m_elemContext.m_elementLocalName) && subPartMatch(this.m_elemContext.m_elementURI, str)) {
                return true;
            }
        }
        return false;
    }

    private static final boolean subPartMatch(String str, String str2) {
        return str == str2 || (str != null && str.equals(str2));
    }

    protected static final String getPrefixPart(String str) {
        int indexOf = str.indexOf(58);
        if (indexOf > 0) {
            return str.substring(0, indexOf);
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public NamespaceMappings getNamespaceMappings() {
        return this.m_prefixMap;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public String getPrefix(String str) {
        return this.m_prefixMap.lookupPrefix(str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public String getNamespaceURI(String str, boolean z) {
        NamespaceMappings namespaceMappings;
        int lastIndexOf = str.lastIndexOf(58);
        String str2 = "";
        String substring = lastIndexOf > 0 ? str.substring(0, lastIndexOf) : str2;
        if ((str2.equals(substring) && !z) || (namespaceMappings = this.m_prefixMap) == null || (str2 = namespaceMappings.lookupNamespace(substring)) != null || substring.equals("xmlns")) {
            return str2;
        }
        throw new RuntimeException(Utils.messages.createMessage("ER_NAMESPACE_PREFIX", new Object[]{str.substring(0, lastIndexOf)}));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public String getNamespaceURIFromPrefix(String str) {
        NamespaceMappings namespaceMappings = this.m_prefixMap;
        if (namespaceMappings != null) {
            return namespaceMappings.lookupNamespace(str);
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void entityReference(String str) throws SAXException {
        flushPending();
        startEntity(str);
        endEntity(str);
        if (this.m_tracer != null) {
            fireEntityReference(str);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public void setTransformer(Transformer transformer) {
        this.m_transformer = transformer;
        SerializerTrace serializerTrace = this.m_transformer;
        if (!(serializerTrace instanceof SerializerTrace) || !serializerTrace.hasTraceListeners()) {
            this.m_tracer = null;
        } else {
            this.m_tracer = this.m_transformer;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public Transformer getTransformer() {
        return this.m_transformer;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void characters(Node node) throws SAXException {
        flushPending();
        String nodeValue = node.getNodeValue();
        if (nodeValue != null) {
            int length = nodeValue.length();
            if (length > this.m_charsBuff.length) {
                this.m_charsBuff = new char[((length * 2) + 1)];
            }
            nodeValue.getChars(0, length, this.m_charsBuff, 0);
            characters(this.m_charsBuff, 0, length);
        }
    }

    public void fatalError(SAXParseException sAXParseException) throws SAXException {
        this.m_elemContext.m_startTagOpen = false;
    }

    /* access modifiers changed from: protected */
    public void fireStartEntity(String str) throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
            this.m_tracer.fireGenerateEvent(9, str);
        }
    }

    private void flushMyWriter() {
        Writer writer = this.m_writer;
        if (writer != null) {
            try {
                writer.flush();
            } catch (IOException unused) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public void fireCDATAEvent(char[] cArr, int i, int i2) throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
            this.m_tracer.fireGenerateEvent(10, cArr, i, i2);
        }
    }

    /* access modifiers changed from: protected */
    public void fireCommentEvent(char[] cArr, int i, int i2) throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
            this.m_tracer.fireGenerateEvent(8, new String(cArr, i, i2));
        }
    }

    public void fireEndEntity(String str) throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
        }
    }

    /* access modifiers changed from: protected */
    public void fireStartDoc() throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
            this.m_tracer.fireGenerateEvent(1);
        }
    }

    /* access modifiers changed from: protected */
    public void fireEndDoc() throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
            this.m_tracer.fireGenerateEvent(2);
        }
    }

    /* access modifiers changed from: protected */
    public void fireStartElem(String str) throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
            this.m_tracer.fireGenerateEvent(3, str, (Attributes) this.m_attributes);
        }
    }

    /* access modifiers changed from: protected */
    public void fireEscapingEvent(String str, String str2) throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
            this.m_tracer.fireGenerateEvent(7, str, str2);
        }
    }

    /* access modifiers changed from: protected */
    public void fireEntityReference(String str) throws SAXException {
        if (this.m_tracer != null) {
            flushMyWriter();
            this.m_tracer.fireGenerateEvent(9, str, (Attributes) null);
        }
    }

    public void startDocument() throws SAXException {
        startDocumentInternal();
        this.m_needToCallStartDocument = false;
    }

    /* access modifiers changed from: protected */
    public void startDocumentInternal() throws SAXException {
        if (this.m_tracer != null) {
            fireStartDoc();
        }
    }

    /* access modifiers changed from: protected */
    public void setDocumentInfo() {
        Locator2 locator2 = this.m_locator;
        if (locator2 != null) {
            try {
                String xMLVersion = locator2.getXMLVersion();
                if (xMLVersion != null) {
                    setVersion(xMLVersion);
                }
            } catch (ClassCastException unused) {
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void setSourceLocator(SourceLocator sourceLocator) {
        this.m_sourceLocator = sourceLocator;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public void setNamespaceMappings(NamespaceMappings namespaceMappings) {
        this.m_prefixMap = namespaceMappings;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public boolean reset() {
        resetSerializerBase();
        return true;
    }

    private void resetSerializerBase() {
        this.m_attributes.clear();
        this.m_cdataSectionElements = null;
        this.m_elemContext = new ElemContext();
        this.m_doctypePublic = null;
        this.m_doctypeSystem = null;
        this.m_doIndent = false;
        this.m_encoding = null;
        this.m_indentAmount = 0;
        this.m_inEntityRef = false;
        this.m_inExternalDTD = false;
        this.m_mediatype = null;
        this.m_needToCallStartDocument = true;
        this.m_needToOutputDocTypeDecl = false;
        NamespaceMappings namespaceMappings = this.m_prefixMap;
        if (namespaceMappings != null) {
            namespaceMappings.reset();
        }
        this.m_shouldNotWriteXMLHeader = false;
        this.m_sourceLocator = null;
        this.m_standalone = null;
        this.m_standaloneWasSpecified = false;
        this.m_tracer = null;
        this.m_transformer = null;
        this.m_version = null;
    }

    /* access modifiers changed from: package-private */
    public final boolean inTemporaryOutputState() {
        return getEncoding() == null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void addAttribute(String str, String str2, String str3, String str4, String str5) throws SAXException {
        if (this.m_elemContext.m_startTagOpen) {
            addAttributeAlways(str, str2, str3, str4, str5, false);
        }
    }
}
