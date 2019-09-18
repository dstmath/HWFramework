package org.apache.xml.serializer.dom3;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import org.apache.xalan.templates.Constants;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.serializer.utils.MsgKey;
import org.apache.xml.serializer.utils.Utils;
import org.apache.xml.serializer.utils.XML11Char;
import org.apache.xml.serializer.utils.XMLChar;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.ls.LSSerializerFilter;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.LocatorImpl;

final class DOM3TreeWalker {
    private static final int CANONICAL = 1;
    private static final int CDATA = 2;
    private static final int CHARNORMALIZE = 4;
    private static final int COMMENTS = 8;
    private static final int DISCARDDEFAULT = 32768;
    private static final int DTNORMALIZE = 16;
    private static final int ELEM_CONTENT_WHITESPACE = 32;
    private static final int ENTITIES = 64;
    private static final int IGNORE_CHAR_DENORMALIZE = 131072;
    private static final int INFOSET = 128;
    private static final int NAMESPACEDECLS = 512;
    private static final int NAMESPACES = 256;
    private static final int NORMALIZECHARS = 1024;
    private static final int PRETTY_PRINT = 65536;
    private static final int SCHEMAVALIDATE = 8192;
    private static final int SPLITCDATA = 2048;
    private static final int VALIDATE = 4096;
    private static final int WELLFORMED = 16384;
    private static final int XMLDECL = 262144;
    private static final String XMLNS_PREFIX = "xmlns";
    private static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";
    private static final String XML_PREFIX = "xml";
    private static final String XML_URI = "http://www.w3.org/XML/1998/namespace";
    private static final Hashtable s_propKeys = new Hashtable();
    private Properties fDOMConfigProperties = null;
    private int fElementDepth = 0;
    private DOMErrorHandler fErrorHandler = null;
    private int fFeatures = 0;
    private LSSerializerFilter fFilter = null;
    private boolean fInEntityRef = false;
    private boolean fIsLevel3DOM = false;
    private boolean fIsXMLVersion11 = false;
    private LexicalHandler fLexicalHandler = null;
    protected NamespaceSupport fLocalNSBinder;
    private LocatorImpl fLocator = new LocatorImpl();
    protected NamespaceSupport fNSBinder;
    private String fNewLine = null;
    boolean fNextIsRaw = false;
    private SerializationHandler fSerializer = null;
    private int fWhatToShowFilter;
    private String fXMLVersion = null;

    DOM3TreeWalker(SerializationHandler serialHandler, DOMErrorHandler errHandler, LSSerializerFilter filter, String newLine) {
        this.fSerializer = serialHandler;
        this.fErrorHandler = errHandler;
        this.fFilter = filter;
        this.fLexicalHandler = null;
        this.fNewLine = newLine;
        this.fNSBinder = new NamespaceSupport();
        this.fLocalNSBinder = new NamespaceSupport();
        this.fDOMConfigProperties = this.fSerializer.getOutputFormat();
        this.fSerializer.setDocumentLocator(this.fLocator);
        initProperties(this.fDOMConfigProperties);
        try {
            LocatorImpl locatorImpl = this.fLocator;
            locatorImpl.setSystemId(System.getProperty("user.dir") + File.separator + "dummy.xsl");
        } catch (SecurityException e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0079  */
    public void traverse(Node top) throws SAXException {
        this.fSerializer.startDocument();
        if (top.getNodeType() != 9) {
            Document ownerDoc = top.getOwnerDocument();
            if (ownerDoc != null && ownerDoc.getImplementation().hasFeature("Core", "3.0")) {
                this.fIsLevel3DOM = true;
            }
        } else if (((Document) top).getImplementation().hasFeature("Core", "3.0")) {
            this.fIsLevel3DOM = true;
        }
        if (this.fSerializer instanceof LexicalHandler) {
            this.fLexicalHandler = this.fSerializer;
        }
        if (this.fFilter != null) {
            this.fWhatToShowFilter = this.fFilter.getWhatToShow();
        }
        Node pos = top;
        while (pos != null) {
            startNode(pos);
            Node nextNode = pos.getFirstChild();
            while (true) {
                if (nextNode != null) {
                    break;
                }
                endNode(pos);
                if (top.equals(pos)) {
                    break;
                }
                nextNode = pos.getNextSibling();
                if (nextNode == null) {
                    pos = pos.getParentNode();
                    if (pos == null || top.equals(pos)) {
                        if (pos != null) {
                            endNode(pos);
                        }
                    }
                }
            }
            if (pos != null) {
            }
            nextNode = null;
            pos = nextNode;
        }
        this.fSerializer.endDocument();
    }

    public void traverse(Node pos, Node top) throws SAXException {
        this.fSerializer.startDocument();
        if (pos.getNodeType() != 9) {
            Document ownerDoc = pos.getOwnerDocument();
            if (ownerDoc != null && ownerDoc.getImplementation().hasFeature("Core", "3.0")) {
                this.fIsLevel3DOM = true;
            }
        } else if (((Document) pos).getImplementation().hasFeature("Core", "3.0")) {
            this.fIsLevel3DOM = true;
        }
        if (this.fSerializer instanceof LexicalHandler) {
            this.fLexicalHandler = this.fSerializer;
        }
        if (this.fFilter != null) {
            this.fWhatToShowFilter = this.fFilter.getWhatToShow();
        }
        while (pos != null) {
            startNode(pos);
            Node nextNode = pos.getFirstChild();
            while (true) {
                if (nextNode != null) {
                    break;
                }
                endNode(pos);
                if (top != null && top.equals(pos)) {
                    break;
                }
                nextNode = pos.getNextSibling();
                if (nextNode == null) {
                    pos = pos.getParentNode();
                    if (pos == null || (top != null && top.equals(pos))) {
                        nextNode = null;
                    }
                }
            }
            nextNode = null;
            pos = nextNode;
        }
        this.fSerializer.endDocument();
    }

    private final void dispatachChars(Node node) throws SAXException {
        if (this.fSerializer != null) {
            this.fSerializer.characters(node);
            return;
        }
        String data = ((Text) node).getData();
        this.fSerializer.characters(data.toCharArray(), 0, data.length());
    }

    /* access modifiers changed from: protected */
    public void startNode(Node node) throws SAXException {
        if (node instanceof Locator) {
            Locator loc = (Locator) node;
            this.fLocator.setColumnNumber(loc.getColumnNumber());
            this.fLocator.setLineNumber(loc.getLineNumber());
            this.fLocator.setPublicId(loc.getPublicId());
            this.fLocator.setSystemId(loc.getSystemId());
        } else {
            this.fLocator.setColumnNumber(0);
            this.fLocator.setLineNumber(0);
        }
        switch (node.getNodeType()) {
            case 1:
                serializeElement((Element) node, true);
                return;
            case 3:
                serializeText((Text) node);
                return;
            case 4:
                serializeCDATASection((CDATASection) node);
                return;
            case 5:
                serializeEntityReference((EntityReference) node, true);
                return;
            case 7:
                serializePI((ProcessingInstruction) node);
                return;
            case 8:
                serializeComment((Comment) node);
                return;
            case 10:
                serializeDocType((DocumentType) node, true);
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: protected */
    public void endNode(Node node) throws SAXException {
        switch (node.getNodeType()) {
            case 1:
                serializeElement((Element) node, false);
                return;
            case 5:
                serializeEntityReference((EntityReference) node, false);
                return;
            case 10:
                serializeDocType((DocumentType) node, false);
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: protected */
    public boolean applyFilter(Node node, int nodeType) {
        if (!(this.fFilter == null || (this.fWhatToShowFilter & nodeType) == 0)) {
            switch (this.fFilter.acceptNode(node)) {
                case 2:
                case 3:
                    return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void serializeDocType(DocumentType node, boolean bStart) throws SAXException {
        String docTypeName = node.getNodeName();
        String publicId = node.getPublicId();
        String systemId = node.getSystemId();
        String internalSubset = node.getInternalSubset();
        if (internalSubset == null || "".equals(internalSubset)) {
            if (bStart) {
                if (this.fLexicalHandler != null) {
                    this.fLexicalHandler.startDTD(docTypeName, publicId, systemId);
                }
            } else if (this.fLexicalHandler != null) {
                this.fLexicalHandler.endDTD();
            }
        } else if (bStart) {
            try {
                Writer writer = this.fSerializer.getWriter();
                StringBuffer dtd = new StringBuffer();
                dtd.append("<!DOCTYPE ");
                dtd.append(docTypeName);
                if (publicId != null) {
                    dtd.append(" PUBLIC \"");
                    dtd.append(publicId);
                    dtd.append('\"');
                }
                if (systemId != null) {
                    if (publicId == null) {
                        dtd.append(" SYSTEM \"");
                    } else {
                        dtd.append(" \"");
                    }
                    dtd.append(systemId);
                    dtd.append('\"');
                }
                dtd.append(" [ ");
                dtd.append(this.fNewLine);
                dtd.append(internalSubset);
                dtd.append("]>");
                dtd.append(new String(this.fNewLine));
                writer.write(dtd.toString());
                writer.flush();
            } catch (IOException e) {
                throw new SAXException(Utils.messages.createMessage(MsgKey.ER_WRITING_INTERNAL_SUBSET, null), e);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void serializeComment(Comment node) throws SAXException {
        if ((this.fFeatures & 8) != 0) {
            String data = node.getData();
            if ((this.fFeatures & 16384) != 0) {
                isCommentWellFormed(data);
            }
            if (this.fLexicalHandler != null && applyFilter(node, 128)) {
                this.fLexicalHandler.comment(data.toCharArray(), 0, data.length());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void serializeElement(Element node, boolean bStart) throws SAXException {
        if (bStart) {
            this.fElementDepth++;
            if ((this.fFeatures & 16384) != 0) {
                isElementWellFormed(node);
            }
            if (applyFilter(node, 1)) {
                if ((this.fFeatures & 256) != 0) {
                    this.fNSBinder.pushContext();
                    this.fLocalNSBinder.reset();
                    recordLocalNSDecl(node);
                    fixupElementNS(node);
                }
                this.fSerializer.startElement(node.getNamespaceURI(), node.getLocalName(), node.getNodeName());
                serializeAttList(node);
            }
        } else {
            this.fElementDepth--;
            if (applyFilter(node, 1)) {
                this.fSerializer.endElement(node.getNamespaceURI(), node.getLocalName(), node.getNodeName());
                if ((this.fFeatures & 256) != 0) {
                    this.fNSBinder.popContext();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x027b  */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x027e  */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x028c  */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x0291 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x0217  */
    public void serializeAttList(Element node) throws SAXException {
        String attrName;
        int nAttrs;
        NamedNodeMap atts;
        String attrPrefix;
        boolean addAttr;
        String attrValue;
        int indexOf;
        String prefix;
        NamedNodeMap atts2;
        int nAttrs2;
        NamedNodeMap atts3 = node.getAttributes();
        int nAttrs3 = atts3.getLength();
        int i = 0;
        while (i < nAttrs3) {
            Node attr = atts3.item(i);
            String localName = attr.getLocalName();
            String attrName2 = attr.getNodeName();
            String attrPrefix2 = attr.getPrefix() == null ? "" : attr.getPrefix();
            String attrValue2 = attr.getNodeValue();
            String type = null;
            if (this.fIsLevel3DOM) {
                type = ((Attr) attr).getSchemaTypeInfo().getTypeName();
            }
            String type2 = type == null ? "CDATA" : type;
            String attrNS = attr.getNamespaceURI();
            if (attrNS != null && attrNS.length() == 0) {
                attrNS = null;
                attrName2 = attr.getLocalName();
            }
            String attrNS2 = attrNS;
            boolean isSpecified = ((Attr) attr).getSpecified();
            boolean addAttr2 = true;
            boolean applyFilter = false;
            boolean xmlnsAttr = attrName.equals("xmlns") || attrName.startsWith(Constants.ATTRNAME_XMLNS);
            if ((this.fFeatures & 16384) != 0) {
                isAttributeWellFormed(attr);
            }
            if ((this.fFeatures & 256) == 0 || xmlnsAttr) {
                atts2 = atts3;
                nAttrs2 = nAttrs3;
            } else if (attrNS2 != null) {
                String attrPrefix3 = attrPrefix2 == null ? "" : attrPrefix2;
                String declAttrPrefix = this.fNSBinder.getPrefix(attrNS2);
                String declAttrNS = this.fNSBinder.getURI(attrPrefix3);
                if (!"".equals(attrPrefix3) && !"".equals(declAttrPrefix) && attrPrefix3.equals(declAttrPrefix)) {
                    atts = atts3;
                    nAttrs = nAttrs3;
                } else if (declAttrPrefix == null || "".equals(declAttrPrefix)) {
                    atts = atts3;
                    if (attrPrefix3 == null || "".equals(attrPrefix3) || declAttrNS != null) {
                        nAttrs = nAttrs3;
                        StringBuilder sb = new StringBuilder();
                        sb.append("NS");
                        int counter = 1 + 1;
                        sb.append(1);
                        String attrPrefix4 = sb.toString();
                        while (this.fLocalNSBinder.getURI(attrPrefix4) != null) {
                            attrPrefix4 = "NS" + counter;
                            counter++;
                        }
                        attrName = attrPrefix4 + ":" + localName;
                        if ((this.fFeatures & 512) != 0) {
                            SerializationHandler serializationHandler = this.fSerializer;
                            StringBuilder sb2 = new StringBuilder();
                            int i2 = counter;
                            sb2.append(Constants.ATTRNAME_XMLNS);
                            sb2.append(attrPrefix4);
                            serializationHandler.addAttribute("http://www.w3.org/2000/xmlns/", attrPrefix4, sb2.toString(), "CDATA", attrNS2);
                            this.fNSBinder.declarePrefix(attrPrefix4, attrNS2);
                            this.fLocalNSBinder.declarePrefix(attrPrefix4, attrNS2);
                        }
                        attrPrefix3 = attrPrefix4;
                    } else if ((this.fFeatures & 512) != 0) {
                        SerializationHandler serializationHandler2 = this.fSerializer;
                        StringBuilder sb3 = new StringBuilder();
                        nAttrs = nAttrs3;
                        sb3.append(Constants.ATTRNAME_XMLNS);
                        sb3.append(attrPrefix3);
                        serializationHandler2.addAttribute("http://www.w3.org/2000/xmlns/", attrPrefix3, sb3.toString(), "CDATA", attrNS2);
                        this.fNSBinder.declarePrefix(attrPrefix3, attrNS2);
                        this.fLocalNSBinder.declarePrefix(attrPrefix3, attrNS2);
                    } else {
                        nAttrs = nAttrs3;
                    }
                } else {
                    String str = declAttrPrefix;
                    if (declAttrPrefix.length() > 0) {
                        StringBuilder sb4 = new StringBuilder();
                        sb4.append(declAttrPrefix);
                        atts = atts3;
                        sb4.append(":");
                        sb4.append(localName);
                        attrName = sb4.toString();
                    } else {
                        atts = atts3;
                        attrName = localName;
                    }
                    nAttrs = nAttrs3;
                    attrPrefix3 = str;
                }
                attrPrefix = attrName;
                String str2 = attrPrefix3;
                if (((this.fFeatures & 32768) != 0 || !isSpecified) && (this.fFeatures & 32768) != 0) {
                    addAttr2 = false;
                } else {
                    applyFilter = true;
                }
                if (applyFilter && this.fFilter != null && (this.fFilter.getWhatToShow() & 2) != 0 && !xmlnsAttr) {
                    switch (this.fFilter.acceptNode(attr)) {
                        case 2:
                        case 3:
                            addAttr2 = false;
                            break;
                    }
                }
                addAttr = addAttr2;
                if (!addAttr && xmlnsAttr) {
                    if (!((this.fFeatures & 512) == 0 || localName == null || "".equals(localName))) {
                        this.fSerializer.addAttribute(attrNS2, localName, attrPrefix, type2, attrValue2);
                    }
                    attrValue = attrValue2;
                } else if (addAttr || xmlnsAttr) {
                    attrValue = attrValue2;
                    String str3 = localName;
                } else if ((this.fFeatures & 512) == 0 || attrNS2 == null) {
                    attrValue = attrValue2;
                    this.fSerializer.addAttribute("", localName, attrPrefix, type2, attrValue);
                } else {
                    String str4 = attrNS2;
                    attrValue = attrValue2;
                    String str5 = localName;
                    this.fSerializer.addAttribute(attrNS2, localName, attrPrefix, type2, attrValue);
                }
                if (xmlnsAttr && (this.fFeatures & 512) != 0) {
                    indexOf = attrPrefix.indexOf(":");
                    int index = indexOf;
                    if (indexOf >= 0) {
                        prefix = "";
                    } else {
                        prefix = attrPrefix.substring(index + 1);
                    }
                    if ("".equals(prefix)) {
                        this.fSerializer.namespaceAfterStartElement(prefix, attrValue);
                    }
                }
                i++;
                atts3 = atts;
                nAttrs3 = nAttrs;
            } else {
                atts2 = atts3;
                nAttrs2 = nAttrs3;
                if (localName == null) {
                    String msg = Utils.messages.createMessage(MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, new Object[]{attrName});
                    if (this.fErrorHandler != null) {
                        DOMErrorHandler dOMErrorHandler = this.fErrorHandler;
                        DOMErrorImpl dOMErrorImpl = new DOMErrorImpl(2, msg, MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, null, null, null);
                        dOMErrorHandler.handleError(dOMErrorImpl);
                    }
                }
            }
            attrPrefix = attrName;
            String str6 = attrPrefix2;
            if ((this.fFeatures & 32768) != 0) {
            }
            addAttr2 = false;
            switch (this.fFilter.acceptNode(attr)) {
                case 2:
                case 3:
                    break;
            }
            addAttr = addAttr2;
            if (!addAttr) {
            }
            if (addAttr) {
            }
            attrValue = attrValue2;
            String str32 = localName;
            indexOf = attrPrefix.indexOf(":");
            int index2 = indexOf;
            if (indexOf >= 0) {
            }
            if ("".equals(prefix)) {
            }
            i++;
            atts3 = atts;
            nAttrs3 = nAttrs;
        }
        int i3 = nAttrs3;
    }

    /* access modifiers changed from: protected */
    public void serializePI(ProcessingInstruction node) throws SAXException {
        ProcessingInstruction pi = node;
        String name = pi.getNodeName();
        if ((this.fFeatures & 16384) != 0) {
            isPIWellFormed(node);
        }
        if (applyFilter(node, 64)) {
            if (name.equals("xslt-next-is-raw")) {
                this.fNextIsRaw = true;
            } else {
                this.fSerializer.processingInstruction(name, pi.getData());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void serializeCDATASection(CDATASection node) throws SAXException {
        if ((this.fFeatures & 16384) != 0) {
            isCDATASectionWellFormed(node);
        }
        if ((this.fFeatures & 2) != 0) {
            String nodeValue = node.getNodeValue();
            int endIndex = nodeValue.indexOf(SerializerConstants.CDATA_DELIMITER_CLOSE);
            if ((this.fFeatures & 2048) != 0) {
                if (endIndex >= 0) {
                    String relatedData = nodeValue.substring(0, endIndex + 2);
                    String msg = Utils.messages.createMessage(MsgKey.ER_CDATA_SECTIONS_SPLIT, null);
                    if (this.fErrorHandler != null) {
                        DOMErrorHandler dOMErrorHandler = this.fErrorHandler;
                        DOMErrorImpl dOMErrorImpl = new DOMErrorImpl(1, msg, MsgKey.ER_CDATA_SECTIONS_SPLIT, null, relatedData, null);
                        dOMErrorHandler.handleError(dOMErrorImpl);
                    }
                }
            } else if (endIndex >= 0) {
                String substring = nodeValue.substring(0, endIndex + 2);
                String msg2 = Utils.messages.createMessage(MsgKey.ER_CDATA_SECTIONS_SPLIT, null);
                if (this.fErrorHandler != null) {
                    this.fErrorHandler.handleError(new DOMErrorImpl(2, msg2, MsgKey.ER_CDATA_SECTIONS_SPLIT));
                }
                return;
            }
            if (applyFilter(node, 8)) {
                if (this.fLexicalHandler != null) {
                    this.fLexicalHandler.startCDATA();
                }
                dispatachChars(node);
                if (this.fLexicalHandler != null) {
                    this.fLexicalHandler.endCDATA();
                }
            }
        } else {
            dispatachChars(node);
        }
    }

    /* access modifiers changed from: protected */
    public void serializeText(Text node) throws SAXException {
        if (this.fNextIsRaw) {
            this.fNextIsRaw = false;
            this.fSerializer.processingInstruction("javax.xml.transform.disable-output-escaping", "");
            dispatachChars(node);
            this.fSerializer.processingInstruction("javax.xml.transform.enable-output-escaping", "");
        } else {
            boolean bDispatch = false;
            if ((this.fFeatures & 16384) != 0) {
                isTextWellFormed(node);
            }
            boolean isElementContentWhitespace = false;
            if (this.fIsLevel3DOM) {
                isElementContentWhitespace = node.isElementContentWhitespace();
            }
            if (!isElementContentWhitespace) {
                bDispatch = true;
            } else if ((this.fFeatures & 32) != 0) {
                bDispatch = true;
            }
            if (applyFilter(node, 4) && bDispatch) {
                dispatachChars(node);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void serializeEntityReference(EntityReference node, boolean bStart) throws SAXException {
        if (bStart) {
            EntityReference eref = node;
            if ((this.fFeatures & 64) != 0) {
                if ((this.fFeatures & 16384) != 0) {
                    isEntityReferneceWellFormed(node);
                }
                if ((this.fFeatures & 256) != 0) {
                    checkUnboundPrefixInEntRef(node);
                }
            }
            if (this.fLexicalHandler != null) {
                this.fLexicalHandler.startEntity(eref.getNodeName());
                return;
            }
            return;
        }
        EntityReference eref2 = node;
        if (this.fLexicalHandler != null) {
            this.fLexicalHandler.endEntity(eref2.getNodeName());
        }
    }

    /* access modifiers changed from: protected */
    public boolean isXMLName(String s, boolean xml11Version) {
        if (s == null) {
            return false;
        }
        if (!xml11Version) {
            return XMLChar.isValidName(s);
        }
        return XML11Char.isXML11ValidName(s);
    }

    /* access modifiers changed from: protected */
    public boolean isValidQName(String prefix, String local, boolean xml11Version) {
        boolean validNCName = false;
        if (local == null) {
            return false;
        }
        if (!xml11Version) {
            if ((prefix == null || XMLChar.isValidNCName(prefix)) && XMLChar.isValidNCName(local)) {
                validNCName = true;
            }
        } else if ((prefix == null || XML11Char.isXML11ValidNCName(prefix)) && XML11Char.isXML11ValidNCName(local)) {
            validNCName = true;
        }
        return validNCName;
    }

    /* access modifiers changed from: protected */
    public boolean isWFXMLChar(String chardata, Character refInvalidChar) {
        if (chardata == null || chardata.length() == 0) {
            return true;
        }
        char[] dataarray = chardata.toCharArray();
        int datalength = dataarray.length;
        if (this.fIsXMLVersion11) {
            int i = 0;
            while (i < datalength) {
                int i2 = i + 1;
                if (XML11Char.isXML11Invalid(dataarray[i]) != 0) {
                    char ch = dataarray[i2 - 1];
                    if (XMLChar.isHighSurrogate(ch) && i2 < datalength) {
                        int i3 = i2 + 1;
                        char ch2 = dataarray[i2];
                        if (!XMLChar.isLowSurrogate(ch2) || !XMLChar.isSupplemental(XMLChar.supplemental(ch, ch2))) {
                            int i4 = i3;
                        } else {
                            i = i3;
                        }
                    }
                    new Character(ch);
                    return false;
                }
                i = i2;
            }
        } else {
            int i5 = 0;
            while (i5 < datalength) {
                int i6 = i5 + 1;
                if (XMLChar.isInvalid(dataarray[i5]) != 0) {
                    char ch3 = dataarray[i6 - 1];
                    if (XMLChar.isHighSurrogate(ch3) && i6 < datalength) {
                        int i7 = i6 + 1;
                        char ch22 = dataarray[i6];
                        if (!XMLChar.isLowSurrogate(ch22) || !XMLChar.isSupplemental(XMLChar.supplemental(ch3, ch22))) {
                            int i8 = i7;
                        } else {
                            i5 = i7;
                        }
                    }
                    new Character(ch3);
                    return false;
                }
                i5 = i6;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public Character isWFXMLChar(String chardata) {
        char ch;
        int i;
        char ch2;
        int i2;
        if (chardata == null || chardata.length() == 0) {
            return null;
        }
        char[] dataarray = chardata.toCharArray();
        int datalength = dataarray.length;
        int i3 = 0;
        if (this.fIsXMLVersion11) {
            while (true) {
                int i4 = i3;
                if (i4 >= datalength) {
                    break;
                }
                i3 = i4 + 1;
                if (XML11Char.isXML11Invalid(dataarray[i4]) != 0) {
                    ch2 = dataarray[i3 - 1];
                    if (!XMLChar.isHighSurrogate(ch2) || i3 >= datalength) {
                        break;
                    }
                    i2 = i3 + 1;
                    char ch22 = dataarray[i3];
                    if (!XMLChar.isLowSurrogate(ch22) || !XMLChar.isSupplemental(XMLChar.supplemental(ch2, ch22))) {
                        int i5 = i2;
                    } else {
                        i3 = i2;
                    }
                }
            }
            int i52 = i2;
            return new Character(ch2);
        }
        while (true) {
            int i6 = i3;
            if (i6 >= datalength) {
                break;
            }
            int i7 = i6 + 1;
            if (XMLChar.isInvalid(dataarray[i6]) != 0) {
                ch = dataarray[i7 - 1];
                if (!XMLChar.isHighSurrogate(ch) || i7 >= datalength) {
                    break;
                }
                i = i7 + 1;
                char ch23 = dataarray[i7];
                if (!XMLChar.isLowSurrogate(ch23) || !XMLChar.isSupplemental(XMLChar.supplemental(ch, ch23))) {
                    int i8 = i;
                } else {
                    i7 = i;
                }
            }
        }
        int i82 = i;
        return new Character(ch);
        return null;
    }

    /* access modifiers changed from: protected */
    public void isCommentWellFormed(String data) {
        if (data != null && data.length() != 0) {
            char[] dataarray = data.toCharArray();
            int datalength = dataarray.length;
            Object[] objArr = null;
            char c = '-';
            if (this.fIsXMLVersion11) {
                int i = 0;
                while (i < datalength) {
                    int i2 = i + 1;
                    char i3 = dataarray[i];
                    if (XML11Char.isXML11Invalid(i3)) {
                        if (XMLChar.isHighSurrogate(i3) && i2 < datalength) {
                            int i4 = i2 + 1;
                            char i5 = dataarray[i2];
                            if (!XMLChar.isLowSurrogate(i5) || !XMLChar.isSupplemental(XMLChar.supplemental(i3, i5))) {
                                i2 = i4;
                            } else {
                                i = i4;
                            }
                        }
                        String msg = Utils.messages.createMessage(MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, new Object[]{new Character(i3)});
                        if (this.fErrorHandler != null) {
                            DOMErrorHandler dOMErrorHandler = this.fErrorHandler;
                            DOMErrorImpl dOMErrorImpl = new DOMErrorImpl(3, msg, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null);
                            dOMErrorHandler.handleError(dOMErrorImpl);
                        }
                    } else if (i3 == '-' && i2 < datalength && dataarray[i2] == '-') {
                        String msg2 = Utils.messages.createMessage(MsgKey.ER_WF_DASH_IN_COMMENT, objArr);
                        if (this.fErrorHandler != null) {
                            DOMErrorHandler dOMErrorHandler2 = this.fErrorHandler;
                            DOMErrorImpl dOMErrorImpl2 = r11;
                            DOMErrorImpl dOMErrorImpl3 = new DOMErrorImpl(3, msg2, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null);
                            dOMErrorHandler2.handleError(dOMErrorImpl2);
                        }
                    }
                    i = i2;
                    objArr = null;
                }
            } else {
                int i6 = 0;
                while (i6 < datalength) {
                    int i7 = i6 + 1;
                    char i8 = dataarray[i6];
                    if (XMLChar.isInvalid(i8)) {
                        if (XMLChar.isHighSurrogate(i8) && i7 < datalength) {
                            int i9 = i7 + 1;
                            char i10 = dataarray[i7];
                            if (!XMLChar.isLowSurrogate(i10) || !XMLChar.isSupplemental(XMLChar.supplemental(i8, i10))) {
                                i7 = i9;
                            } else {
                                i6 = i9;
                            }
                        }
                        String msg3 = Utils.messages.createMessage(MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, new Object[]{new Character(i8)});
                        if (this.fErrorHandler != null) {
                            DOMErrorHandler dOMErrorHandler3 = this.fErrorHandler;
                            DOMErrorImpl dOMErrorImpl4 = new DOMErrorImpl(3, msg3, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null);
                            dOMErrorHandler3.handleError(dOMErrorImpl4);
                        }
                        i6 = i7;
                    } else {
                        if (i8 == c && i7 < datalength && dataarray[i7] == c) {
                            String msg4 = Utils.messages.createMessage(MsgKey.ER_WF_DASH_IN_COMMENT, null);
                            if (this.fErrorHandler != null) {
                                DOMErrorHandler dOMErrorHandler4 = this.fErrorHandler;
                                DOMErrorImpl dOMErrorImpl5 = r11;
                                DOMErrorImpl dOMErrorImpl6 = new DOMErrorImpl(3, msg4, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null);
                                dOMErrorHandler4.handleError(dOMErrorImpl5);
                            }
                        }
                        i6 = i7;
                    }
                    c = '-';
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void isElementWellFormed(Node node) {
        boolean isNameWF;
        if ((this.fFeatures & 256) != 0) {
            isNameWF = isValidQName(node.getPrefix(), node.getLocalName(), this.fIsXMLVersion11);
        } else {
            isNameWF = isXMLName(node.getNodeName(), this.fIsXMLVersion11);
        }
        if (!isNameWF) {
            String msg = Utils.messages.createMessage(MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, new Object[]{"Element", node.getNodeName()});
            if (this.fErrorHandler != null) {
                DOMErrorHandler dOMErrorHandler = this.fErrorHandler;
                DOMErrorImpl dOMErrorImpl = new DOMErrorImpl(3, msg, MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, null, null, null);
                dOMErrorHandler.handleError(dOMErrorImpl);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void isAttributeWellFormed(Node node) {
        boolean isNameWF;
        if ((this.fFeatures & 256) != 0) {
            isNameWF = isValidQName(node.getPrefix(), node.getLocalName(), this.fIsXMLVersion11);
        } else {
            isNameWF = isXMLName(node.getNodeName(), this.fIsXMLVersion11);
        }
        int i = 0;
        if (!isNameWF) {
            String msg = Utils.messages.createMessage(MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, new Object[]{"Attr", node.getNodeName()});
            if (this.fErrorHandler != null) {
                DOMErrorHandler dOMErrorHandler = this.fErrorHandler;
                DOMErrorImpl dOMErrorImpl = new DOMErrorImpl(3, msg, MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, null, null, null);
                dOMErrorHandler.handleError(dOMErrorImpl);
            }
        }
        if (node.getNodeValue().indexOf(60) >= 0) {
            String msg2 = Utils.messages.createMessage(MsgKey.ER_WF_LT_IN_ATTVAL, new Object[]{((Attr) node).getOwnerElement().getNodeName(), node.getNodeName()});
            if (this.fErrorHandler != null) {
                DOMErrorHandler dOMErrorHandler2 = this.fErrorHandler;
                DOMErrorImpl dOMErrorImpl2 = new DOMErrorImpl(3, msg2, MsgKey.ER_WF_LT_IN_ATTVAL, null, null, null);
                dOMErrorHandler2.handleError(dOMErrorImpl2);
            }
        }
        NodeList children = node.getChildNodes();
        while (true) {
            int i2 = i;
            if (i2 < children.getLength()) {
                Node child = children.item(i2);
                if (child != null) {
                    short nodeType = child.getNodeType();
                    if (nodeType == 3) {
                        isTextWellFormed((Text) child);
                    } else if (nodeType == 5) {
                        isEntityReferneceWellFormed((EntityReference) child);
                    }
                }
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void isPIWellFormed(ProcessingInstruction node) {
        if (!isXMLName(node.getNodeName(), this.fIsXMLVersion11)) {
            String msg = Utils.messages.createMessage(MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, new Object[]{"ProcessingInstruction", node.getTarget()});
            if (this.fErrorHandler != null) {
                DOMErrorHandler dOMErrorHandler = this.fErrorHandler;
                DOMErrorImpl dOMErrorImpl = new DOMErrorImpl(3, msg, MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, null, null, null);
                dOMErrorHandler.handleError(dOMErrorImpl);
            }
        }
        Character invalidChar = isWFXMLChar(node.getData());
        if (invalidChar != null) {
            String msg2 = Utils.messages.createMessage(MsgKey.ER_WF_INVALID_CHARACTER_IN_PI, new Object[]{Integer.toHexString(Character.getNumericValue(invalidChar.charValue()))});
            if (this.fErrorHandler != null) {
                DOMErrorHandler dOMErrorHandler2 = this.fErrorHandler;
                DOMErrorImpl dOMErrorImpl2 = new DOMErrorImpl(3, msg2, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null);
                dOMErrorHandler2.handleError(dOMErrorImpl2);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void isCDATASectionWellFormed(CDATASection node) {
        Character invalidChar = isWFXMLChar(node.getData());
        if (invalidChar != null) {
            String msg = Utils.messages.createMessage(MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA, new Object[]{Integer.toHexString(Character.getNumericValue(invalidChar.charValue()))});
            if (this.fErrorHandler != null) {
                DOMErrorHandler dOMErrorHandler = this.fErrorHandler;
                DOMErrorImpl dOMErrorImpl = new DOMErrorImpl(3, msg, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null);
                dOMErrorHandler.handleError(dOMErrorImpl);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void isTextWellFormed(Text node) {
        Character invalidChar = isWFXMLChar(node.getData());
        if (invalidChar != null) {
            String msg = Utils.messages.createMessage(MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT, new Object[]{Integer.toHexString(Character.getNumericValue(invalidChar.charValue()))});
            if (this.fErrorHandler != null) {
                DOMErrorHandler dOMErrorHandler = this.fErrorHandler;
                DOMErrorImpl dOMErrorImpl = new DOMErrorImpl(3, msg, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null);
                dOMErrorHandler.handleError(dOMErrorImpl);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void isEntityReferneceWellFormed(EntityReference node) {
        String nodeNamespaceURI;
        if (!isXMLName(node.getNodeName(), this.fIsXMLVersion11)) {
            String msg = Utils.messages.createMessage(MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, new Object[]{"EntityReference", node.getNodeName()});
            if (this.fErrorHandler != null) {
                DOMErrorHandler dOMErrorHandler = this.fErrorHandler;
                DOMErrorImpl dOMErrorImpl = new DOMErrorImpl(3, msg, MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, null, null, null);
                dOMErrorHandler.handleError(dOMErrorImpl);
            }
        }
        Node parent = node.getParentNode();
        DocumentType docType = node.getOwnerDocument().getDoctype();
        if (docType != null) {
            NamedNodeMap entities = docType.getEntities();
            for (int i = 0; i < entities.getLength(); i++) {
                Entity ent = (Entity) entities.item(i);
                String nodeName = node.getNodeName() == null ? "" : node.getNodeName();
                if (node.getNamespaceURI() == null) {
                    nodeNamespaceURI = "";
                } else {
                    nodeNamespaceURI = node.getNamespaceURI();
                }
                String entName = ent.getNodeName() == null ? "" : ent.getNodeName();
                String entNamespaceURI = ent.getNamespaceURI() == null ? "" : ent.getNamespaceURI();
                if (parent.getNodeType() == 1 && entNamespaceURI.equals(nodeNamespaceURI) && entName.equals(nodeName) && ent.getNotationName() != null) {
                    String msg2 = Utils.messages.createMessage(MsgKey.ER_WF_REF_TO_UNPARSED_ENT, new Object[]{node.getNodeName()});
                    if (this.fErrorHandler != null) {
                        DOMErrorHandler dOMErrorHandler2 = this.fErrorHandler;
                        DOMErrorImpl dOMErrorImpl2 = new DOMErrorImpl(3, msg2, MsgKey.ER_WF_REF_TO_UNPARSED_ENT, null, null, null);
                        dOMErrorHandler2.handleError(dOMErrorImpl2);
                    }
                }
                if (parent.getNodeType() == 2 && entNamespaceURI.equals(nodeNamespaceURI) && entName.equals(nodeName) && !(ent.getPublicId() == null && ent.getSystemId() == null && ent.getNotationName() == null)) {
                    String msg3 = Utils.messages.createMessage(MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, new Object[]{node.getNodeName()});
                    if (this.fErrorHandler != null) {
                        DOMErrorHandler dOMErrorHandler3 = this.fErrorHandler;
                        DOMErrorImpl dOMErrorImpl3 = new DOMErrorImpl(3, msg3, MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, null, null, null);
                        dOMErrorHandler3.handleError(dOMErrorImpl3);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void checkUnboundPrefixInEntRef(Node node) {
        Node child = node.getFirstChild();
        while (child != null) {
            Node next = child.getNextSibling();
            if (child.getNodeType() == 1) {
                String prefix = child.getPrefix();
                if (prefix != null && this.fNSBinder.getURI(prefix) == null) {
                    String msg = Utils.messages.createMessage("unbound-prefix-in-entity-reference", new Object[]{node.getNodeName(), child.getNodeName(), prefix});
                    if (this.fErrorHandler != null) {
                        DOMErrorHandler dOMErrorHandler = this.fErrorHandler;
                        DOMErrorImpl dOMErrorImpl = new DOMErrorImpl(3, msg, "unbound-prefix-in-entity-reference", null, null, null);
                        dOMErrorHandler.handleError(dOMErrorImpl);
                    }
                }
                NamedNodeMap attrs = child.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    String attrPrefix = attrs.item(i).getPrefix();
                    if (attrPrefix != null && this.fNSBinder.getURI(attrPrefix) == null) {
                        String msg2 = Utils.messages.createMessage("unbound-prefix-in-entity-reference", new Object[]{node.getNodeName(), child.getNodeName(), attrs.item(i)});
                        if (this.fErrorHandler != null) {
                            DOMErrorHandler dOMErrorHandler2 = this.fErrorHandler;
                            DOMErrorImpl dOMErrorImpl2 = new DOMErrorImpl(3, msg2, "unbound-prefix-in-entity-reference", null, null, null);
                            dOMErrorHandler2.handleError(dOMErrorImpl2);
                        }
                    }
                }
            }
            if (child.hasChildNodes()) {
                checkUnboundPrefixInEntRef(child);
            }
            child = next;
        }
    }

    /* access modifiers changed from: protected */
    public void recordLocalNSDecl(Node node) {
        NamedNodeMap atts = ((Element) node).getAttributes();
        int length = atts.getLength();
        for (int i = 0; i < length; i++) {
            Node attr = atts.item(i);
            String localName = attr.getLocalName();
            String attrPrefix = attr.getPrefix();
            String attrValue = attr.getNodeValue();
            String attrNS = attr.getNamespaceURI();
            String localName2 = (localName == null || "xmlns".equals(localName)) ? "" : localName;
            String attrPrefix2 = attrPrefix == null ? "" : attrPrefix;
            String attrValue2 = attrValue == null ? "" : attrValue;
            if ("http://www.w3.org/2000/xmlns/".equals(attrNS == null ? "" : attrNS)) {
                if ("http://www.w3.org/2000/xmlns/".equals(attrValue2)) {
                    String msg = Utils.messages.createMessage(MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, new Object[]{attrPrefix2, "http://www.w3.org/2000/xmlns/"});
                    if (this.fErrorHandler != null) {
                        DOMErrorHandler dOMErrorHandler = this.fErrorHandler;
                        DOMErrorImpl dOMErrorImpl = new DOMErrorImpl(2, msg, MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, null, null, null);
                        dOMErrorHandler.handleError(dOMErrorImpl);
                    }
                } else if (!"xmlns".equals(attrPrefix2)) {
                    this.fNSBinder.declarePrefix("", attrValue2);
                } else if (attrValue2.length() != 0) {
                    this.fNSBinder.declarePrefix(localName2, attrValue2);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void fixupElementNS(Node node) throws SAXException {
        String namespaceURI = ((Element) node).getNamespaceURI();
        String prefix = ((Element) node).getPrefix();
        String localName = ((Element) node).getLocalName();
        if (namespaceURI != null) {
            String prefix2 = prefix == null ? "" : prefix;
            String inScopeNamespaceURI = this.fNSBinder.getURI(prefix2);
            if (inScopeNamespaceURI == null || !inScopeNamespaceURI.equals(namespaceURI)) {
                if ((this.fFeatures & 512) != 0) {
                    if ("".equals(prefix2) || "".equals(namespaceURI)) {
                        ((Element) node).setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", namespaceURI);
                    } else {
                        ((Element) node).setAttributeNS("http://www.w3.org/2000/xmlns/", Constants.ATTRNAME_XMLNS + prefix2, namespaceURI);
                    }
                }
                this.fLocalNSBinder.declarePrefix(prefix2, namespaceURI);
                this.fNSBinder.declarePrefix(prefix2, namespaceURI);
            }
        } else if (localName == null || "".equals(localName)) {
            String msg = Utils.messages.createMessage(MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, new Object[]{node.getNodeName()});
            if (this.fErrorHandler != null) {
                DOMErrorHandler dOMErrorHandler = this.fErrorHandler;
                DOMErrorImpl dOMErrorImpl = new DOMErrorImpl(2, msg, MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, null, null, null);
                dOMErrorHandler.handleError(dOMErrorImpl);
            }
        } else {
            String namespaceURI2 = this.fNSBinder.getURI("");
            if (namespaceURI2 != null && namespaceURI2.length() > 0) {
                ((Element) node).setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "");
                this.fLocalNSBinder.declarePrefix("", "");
                this.fNSBinder.declarePrefix("", "");
            }
        }
    }

    static {
        s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}cdata-sections", new Integer(2));
        s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}comments", new Integer(8));
        s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}element-content-whitespace", new Integer(32));
        s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}entities", new Integer(64));
        s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}namespaces", new Integer(256));
        s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}namespace-declarations", new Integer(512));
        s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}split-cdata-sections", new Integer(2048));
        s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}well-formed", new Integer(16384));
        s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}discard-default-content", new Integer(32768));
        s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}format-pretty-print", "");
        s_propKeys.put("omit-xml-declaration", "");
        s_propKeys.put("{http://xml.apache.org/xerces-2j}xml-version", "");
        s_propKeys.put("encoding", "");
        s_propKeys.put("{http://xml.apache.org/xerces-2j}entities", "");
    }

    /* access modifiers changed from: protected */
    public void initProperties(Properties properties) {
        Enumeration keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            Object iobj = s_propKeys.get(key);
            if (iobj != null) {
                if (iobj instanceof Integer) {
                    int BITFLAG = ((Integer) iobj).intValue();
                    if (properties.getProperty(key).endsWith("yes")) {
                        this.fFeatures |= BITFLAG;
                    } else {
                        this.fFeatures &= ~BITFLAG;
                    }
                } else if ("{http://www.w3.org/TR/DOM-Level-3-LS}format-pretty-print".equals(key)) {
                    if (properties.getProperty(key).endsWith("yes")) {
                        this.fSerializer.setIndent(true);
                        this.fSerializer.setIndentAmount(3);
                    } else {
                        this.fSerializer.setIndent(false);
                    }
                } else if ("omit-xml-declaration".equals(key)) {
                    if (properties.getProperty(key).endsWith("yes")) {
                        this.fSerializer.setOmitXMLDeclaration(true);
                    } else {
                        this.fSerializer.setOmitXMLDeclaration(false);
                    }
                } else if ("{http://xml.apache.org/xerces-2j}xml-version".equals(key)) {
                    String version = properties.getProperty(key);
                    if (SerializerConstants.XMLVERSION11.equals(version)) {
                        this.fIsXMLVersion11 = true;
                        this.fSerializer.setVersion(version);
                    } else {
                        this.fSerializer.setVersion(SerializerConstants.XMLVERSION10);
                    }
                } else if ("encoding".equals(key)) {
                    String encoding = properties.getProperty(key);
                    if (encoding != null) {
                        this.fSerializer.setEncoding(encoding);
                    }
                } else if ("{http://xml.apache.org/xerces-2j}entities".equals(key)) {
                    if (properties.getProperty(key).endsWith("yes")) {
                        this.fSerializer.setDTDEntityExpansion(false);
                    } else {
                        this.fSerializer.setDTDEntityExpansion(true);
                    }
                }
            }
        }
        if (this.fNewLine != null) {
            this.fSerializer.setOutputProperty(OutputPropertiesFactory.S_KEY_LINE_SEPARATOR, this.fNewLine);
        }
    }
}
