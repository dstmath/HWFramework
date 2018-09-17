package org.apache.xml.serializer.dom3;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Properties;
import org.apache.xalan.templates.Constants;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.serializer.utils.Messages;
import org.apache.xml.serializer.utils.MsgKey;
import org.apache.xml.serializer.utils.Utils;
import org.apache.xml.serializer.utils.XML11Char;
import org.apache.xml.serializer.utils.XMLChar;
import org.apache.xpath.compiler.OpCodes;
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
    private static final Hashtable s_propKeys = null;
    private Properties fDOMConfigProperties;
    private int fElementDepth;
    private DOMErrorHandler fErrorHandler;
    private int fFeatures;
    private LSSerializerFilter fFilter;
    private boolean fInEntityRef;
    private boolean fIsLevel3DOM;
    private boolean fIsXMLVersion11;
    private LexicalHandler fLexicalHandler;
    protected NamespaceSupport fLocalNSBinder;
    private LocatorImpl fLocator;
    protected NamespaceSupport fNSBinder;
    private String fNewLine;
    boolean fNextIsRaw;
    private SerializationHandler fSerializer;
    private int fWhatToShowFilter;
    private String fXMLVersion;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xml.serializer.dom3.DOM3TreeWalker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xml.serializer.dom3.DOM3TreeWalker.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xml.serializer.dom3.DOM3TreeWalker.<clinit>():void");
    }

    protected void initProperties(java.util.Properties r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xml.serializer.dom3.DOM3TreeWalker.initProperties(java.util.Properties):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xml.serializer.dom3.DOM3TreeWalker.initProperties(java.util.Properties):void");
    }

    DOM3TreeWalker(SerializationHandler serialHandler, DOMErrorHandler errHandler, LSSerializerFilter filter, String newLine) {
        this.fSerializer = null;
        this.fLocator = new LocatorImpl();
        this.fErrorHandler = null;
        this.fFilter = null;
        this.fLexicalHandler = null;
        this.fNewLine = null;
        this.fDOMConfigProperties = null;
        this.fInEntityRef = false;
        this.fXMLVersion = null;
        this.fIsXMLVersion11 = false;
        this.fIsLevel3DOM = false;
        this.fFeatures = 0;
        this.fNextIsRaw = false;
        this.fElementDepth = 0;
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
            this.fLocator.setSystemId(System.getProperty("user.dir") + File.separator + "dummy.xsl");
        } catch (SecurityException e) {
        }
    }

    public void traverse(Node pos) throws SAXException {
        this.fSerializer.startDocument();
        if (pos.getNodeType() != (short) 9) {
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
        Node top = pos;
        while (pos != null) {
            startNode(pos);
            Node nextNode = pos.getFirstChild();
            while (nextNode == null) {
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
                        nextNode = null;
                    }
                }
            }
            pos = nextNode;
        }
        this.fSerializer.endDocument();
    }

    public void traverse(Node pos, Node top) throws SAXException {
        this.fSerializer.startDocument();
        if (pos.getNodeType() != (short) 9) {
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
            while (nextNode == null) {
                endNode(pos);
                if (top != null && top.equals(pos)) {
                    break;
                }
                nextNode = pos.getNextSibling();
                if (nextNode == null) {
                    pos = pos.getParentNode();
                    if (pos == null || (top != null && top.equals(pos))) {
                        nextNode = null;
                        break;
                    }
                }
            }
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

    protected void startNode(Node node) throws SAXException {
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
            case CANONICAL /*1*/:
                serializeElement((Element) node, true);
            case OpCodes.OP_AND /*3*/:
                serializeText((Text) node);
            case CHARNORMALIZE /*4*/:
                serializeCDATASection((CDATASection) node);
            case OpCodes.OP_EQUALS /*5*/:
                serializeEntityReference((EntityReference) node, true);
            case OpCodes.OP_LT /*7*/:
                serializePI((ProcessingInstruction) node);
            case COMMENTS /*8*/:
                serializeComment((Comment) node);
            case OpCodes.OP_PLUS /*10*/:
                serializeDocType((DocumentType) node, true);
            default:
        }
    }

    protected void endNode(Node node) throws SAXException {
        switch (node.getNodeType()) {
            case CANONICAL /*1*/:
                serializeElement((Element) node, false);
            case OpCodes.OP_EQUALS /*5*/:
                serializeEntityReference((EntityReference) node, false);
            case OpCodes.OP_PLUS /*10*/:
                serializeDocType((DocumentType) node, false);
            default:
        }
    }

    protected boolean applyFilter(Node node, int nodeType) {
        if (!(this.fFilter == null || (this.fWhatToShowFilter & nodeType) == 0)) {
            switch (this.fFilter.acceptNode(node)) {
                case CDATA /*2*/:
                case OpCodes.OP_AND /*3*/:
                    return false;
            }
        }
        return true;
    }

    protected void serializeDocType(DocumentType node, boolean bStart) throws SAXException {
        String docTypeName = node.getNodeName();
        String publicId = node.getPublicId();
        String systemId = node.getSystemId();
        String internalSubset = node.getInternalSubset();
        if (internalSubset == null || SerializerConstants.EMPTYSTRING.equals(internalSubset)) {
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

    protected void serializeComment(Comment node) throws SAXException {
        if ((this.fFeatures & COMMENTS) != 0) {
            String data = node.getData();
            if ((this.fFeatures & WELLFORMED) != 0) {
                isCommentWellFormed(data);
            }
            if (this.fLexicalHandler != null && applyFilter(node, INFOSET)) {
                this.fLexicalHandler.comment(data.toCharArray(), 0, data.length());
            }
        }
    }

    protected void serializeElement(Element node, boolean bStart) throws SAXException {
        if (bStart) {
            this.fElementDepth += CANONICAL;
            if ((this.fFeatures & WELLFORMED) != 0) {
                isElementWellFormed(node);
            }
            if (applyFilter(node, CANONICAL)) {
                if ((this.fFeatures & NAMESPACES) != 0) {
                    this.fNSBinder.pushContext();
                    this.fLocalNSBinder.reset();
                    recordLocalNSDecl(node);
                    fixupElementNS(node);
                }
                this.fSerializer.startElement(node.getNamespaceURI(), node.getLocalName(), node.getNodeName());
                serializeAttList(node);
            } else {
                return;
            }
        }
        this.fElementDepth--;
        if (applyFilter(node, CANONICAL)) {
            this.fSerializer.endElement(node.getNamespaceURI(), node.getLocalName(), node.getNodeName());
            if ((this.fFeatures & NAMESPACES) != 0) {
                this.fNSBinder.popContext();
            }
        }
    }

    protected void serializeAttList(Element node) throws SAXException {
        NamedNodeMap atts = node.getAttributes();
        int nAttrs = atts.getLength();
        for (int i = 0; i < nAttrs; i += CANONICAL) {
            String attrName;
            Node attr = atts.item(i);
            String localName = attr.getLocalName();
            String attrName2 = attr.getNodeName();
            String attrPrefix = attr.getPrefix() == null ? SerializerConstants.EMPTYSTRING : attr.getPrefix();
            String attrValue = attr.getNodeValue();
            String type = null;
            if (this.fIsLevel3DOM) {
                type = ((Attr) attr).getSchemaTypeInfo().getTypeName();
            }
            if (type == null) {
                type = "CDATA";
            }
            String namespaceURI = attr.getNamespaceURI();
            if (namespaceURI == null || namespaceURI.length() != 0) {
                attrName = attrName2;
            } else {
                namespaceURI = null;
                attrName = attr.getLocalName();
            }
            boolean isSpecified = ((Attr) attr).getSpecified();
            boolean addAttr = true;
            boolean applyFilter = false;
            boolean startsWith = !attrName.equals(XMLNS_PREFIX) ? attrName.startsWith(Constants.ATTRNAME_XMLNS) : true;
            if ((this.fFeatures & WELLFORMED) != 0) {
                isAttributeWellFormed(attr);
            }
            if ((this.fFeatures & NAMESPACES) == 0 || startsWith) {
                attrName2 = attrName;
            } else if (namespaceURI != null) {
                if (attrPrefix == null) {
                    attrPrefix = SerializerConstants.EMPTYSTRING;
                }
                String declAttrPrefix = this.fNSBinder.getPrefix(namespaceURI);
                String declAttrNS = this.fNSBinder.getURI(attrPrefix);
                if (!SerializerConstants.EMPTYSTRING.equals(attrPrefix) && !SerializerConstants.EMPTYSTRING.equals(declAttrPrefix) && attrPrefix.equals(declAttrPrefix)) {
                    attrName2 = attrName;
                } else if (declAttrPrefix != null && !SerializerConstants.EMPTYSTRING.equals(declAttrPrefix)) {
                    attrPrefix = declAttrPrefix;
                    attrName2 = declAttrPrefix.length() > 0 ? declAttrPrefix + ":" + localName : localName;
                } else if (attrPrefix == null || SerializerConstants.EMPTYSTRING.equals(attrPrefix) || declAttrNS != null) {
                    StringBuilder append = new StringBuilder().append("NS");
                    int counter = CDATA;
                    attrPrefix = append.append(CANONICAL).toString();
                    while (this.fLocalNSBinder.getURI(attrPrefix) != null) {
                        int counter2 = counter + CANONICAL;
                        attrPrefix = "NS" + counter;
                        counter = counter2;
                    }
                    attrName2 = attrPrefix + ":" + localName;
                    if ((this.fFeatures & NAMESPACEDECLS) != 0) {
                        this.fSerializer.addAttribute(XMLNS_URI, attrPrefix, Constants.ATTRNAME_XMLNS + attrPrefix, "CDATA", namespaceURI);
                        this.fNSBinder.declarePrefix(attrPrefix, namespaceURI);
                        this.fLocalNSBinder.declarePrefix(attrPrefix, namespaceURI);
                    }
                } else {
                    if ((this.fFeatures & NAMESPACEDECLS) != 0) {
                        this.fSerializer.addAttribute(XMLNS_URI, attrPrefix, Constants.ATTRNAME_XMLNS + attrPrefix, "CDATA", namespaceURI);
                        this.fNSBinder.declarePrefix(attrPrefix, namespaceURI);
                        this.fLocalNSBinder.declarePrefix(attrPrefix, namespaceURI);
                    }
                    attrName2 = attrName;
                }
            } else {
                if (localName == null) {
                    Messages messages = Utils.messages;
                    String str = MsgKey.ER_NULL_LOCAL_ELEMENT_NAME;
                    Object[] objArr = new Object[CANONICAL];
                    objArr[0] = attrName;
                    String msg = messages.createMessage(str, objArr);
                    if (this.fErrorHandler != null) {
                        this.fErrorHandler.handleError(new DOMErrorImpl((short) 2, msg, MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, null, null, null));
                        attrName2 = attrName;
                    }
                }
                attrName2 = attrName;
            }
            if (((this.fFeatures & DISCARDDEFAULT) == 0 || !isSpecified) && (this.fFeatures & DISCARDDEFAULT) != 0) {
                addAttr = false;
            } else {
                applyFilter = true;
            }
            if (!(!applyFilter || this.fFilter == null || (this.fFilter.getWhatToShow() & CDATA) == 0 || startsWith)) {
                switch (this.fFilter.acceptNode(attr)) {
                    case CDATA /*2*/:
                    case OpCodes.OP_AND /*3*/:
                        addAttr = false;
                        break;
                }
            }
            if (addAttr && startsWith) {
                if (!((this.fFeatures & NAMESPACEDECLS) == 0 || localName == null || SerializerConstants.EMPTYSTRING.equals(localName))) {
                    this.fSerializer.addAttribute(namespaceURI, localName, attrName2, type, attrValue);
                }
            } else if (addAttr && !startsWith) {
                if ((this.fFeatures & NAMESPACEDECLS) == 0 || namespaceURI == null) {
                    this.fSerializer.addAttribute(SerializerConstants.EMPTYSTRING, localName, attrName2, type, attrValue);
                } else {
                    this.fSerializer.addAttribute(namespaceURI, localName, attrName2, type, attrValue);
                }
            }
            if (startsWith && (this.fFeatures & NAMESPACEDECLS) != 0) {
                String prefix;
                int index = attrName2.indexOf(":");
                if (index < 0) {
                    prefix = SerializerConstants.EMPTYSTRING;
                } else {
                    prefix = attrName2.substring(index + CANONICAL);
                }
                if (!SerializerConstants.EMPTYSTRING.equals(prefix)) {
                    this.fSerializer.namespaceAfterStartElement(prefix, attrValue);
                }
            }
        }
    }

    protected void serializePI(ProcessingInstruction node) throws SAXException {
        ProcessingInstruction pi = node;
        String name = node.getNodeName();
        if ((this.fFeatures & WELLFORMED) != 0) {
            isPIWellFormed(node);
        }
        if (applyFilter(node, ENTITIES)) {
            if (name.equals("xslt-next-is-raw")) {
                this.fNextIsRaw = true;
            } else {
                this.fSerializer.processingInstruction(name, node.getData());
            }
        }
    }

    protected void serializeCDATASection(CDATASection node) throws SAXException {
        if ((this.fFeatures & WELLFORMED) != 0) {
            isCDATASectionWellFormed(node);
        }
        if ((this.fFeatures & CDATA) != 0) {
            String nodeValue = node.getNodeValue();
            int endIndex = nodeValue.indexOf(SerializerConstants.CDATA_DELIMITER_CLOSE);
            String relatedData;
            String msg;
            if ((this.fFeatures & SPLITCDATA) != 0) {
                if (endIndex >= 0) {
                    relatedData = nodeValue.substring(0, endIndex + CDATA);
                    msg = Utils.messages.createMessage(MsgKey.ER_CDATA_SECTIONS_SPLIT, null);
                    if (this.fErrorHandler != null) {
                        this.fErrorHandler.handleError(new DOMErrorImpl((short) 1, msg, MsgKey.ER_CDATA_SECTIONS_SPLIT, null, relatedData, null));
                    }
                }
            } else if (endIndex >= 0) {
                relatedData = nodeValue.substring(0, endIndex + CDATA);
                msg = Utils.messages.createMessage(MsgKey.ER_CDATA_SECTIONS_SPLIT, null);
                if (this.fErrorHandler != null) {
                    this.fErrorHandler.handleError(new DOMErrorImpl((short) 2, msg, MsgKey.ER_CDATA_SECTIONS_SPLIT));
                }
                return;
            }
            if (applyFilter(node, COMMENTS)) {
                if (this.fLexicalHandler != null) {
                    this.fLexicalHandler.startCDATA();
                }
                dispatachChars(node);
                if (this.fLexicalHandler != null) {
                    this.fLexicalHandler.endCDATA();
                }
            } else {
                return;
            }
        }
        dispatachChars(node);
    }

    protected void serializeText(Text node) throws SAXException {
        if (this.fNextIsRaw) {
            this.fNextIsRaw = false;
            this.fSerializer.processingInstruction("javax.xml.transform.disable-output-escaping", SerializerConstants.EMPTYSTRING);
            dispatachChars(node);
            this.fSerializer.processingInstruction("javax.xml.transform.enable-output-escaping", SerializerConstants.EMPTYSTRING);
        } else {
            boolean bDispatch = false;
            if ((this.fFeatures & WELLFORMED) != 0) {
                isTextWellFormed(node);
            }
            boolean isElementContentWhitespace = false;
            if (this.fIsLevel3DOM) {
                isElementContentWhitespace = node.isElementContentWhitespace();
            }
            if (!isElementContentWhitespace) {
                bDispatch = true;
            } else if ((this.fFeatures & ELEM_CONTENT_WHITESPACE) != 0) {
                bDispatch = true;
            }
            if (applyFilter(node, CHARNORMALIZE) && bDispatch) {
                dispatachChars(node);
            }
        }
    }

    protected void serializeEntityReference(EntityReference node, boolean bStart) throws SAXException {
        if (bStart) {
            EntityReference eref = node;
            if ((this.fFeatures & ENTITIES) != 0) {
                if ((this.fFeatures & WELLFORMED) != 0) {
                    isEntityReferneceWellFormed(node);
                }
                if ((this.fFeatures & NAMESPACES) != 0) {
                    checkUnboundPrefixInEntRef(node);
                }
            }
            if (this.fLexicalHandler != null) {
                this.fLexicalHandler.startEntity(node.getNodeName());
                return;
            }
            return;
        }
        eref = node;
        if (this.fLexicalHandler != null) {
            this.fLexicalHandler.endEntity(node.getNodeName());
        }
    }

    protected boolean isXMLName(String s, boolean xml11Version) {
        if (s == null) {
            return false;
        }
        if (xml11Version) {
            return XML11Char.isXML11ValidName(s);
        }
        return XMLChar.isValidName(s);
    }

    protected boolean isValidQName(String prefix, String local, boolean xml11Version) {
        if (local == null) {
            return false;
        }
        boolean validNCName;
        if (xml11Version) {
            if (prefix == null || XML11Char.isXML11ValidNCName(prefix)) {
                validNCName = XML11Char.isXML11ValidNCName(local);
            } else {
                validNCName = false;
            }
        } else if (prefix == null || XMLChar.isValidNCName(prefix)) {
            validNCName = XMLChar.isValidNCName(local);
        } else {
            validNCName = false;
        }
        return validNCName;
    }

    protected boolean isWFXMLChar(String chardata, Character refInvalidChar) {
        if (chardata == null || chardata.length() == 0) {
            return true;
        }
        char[] dataarray = chardata.toCharArray();
        int datalength = dataarray.length;
        int i;
        int i2;
        char ch;
        char ch2;
        if (this.fIsXMLVersion11) {
            i = 0;
            while (i < datalength) {
                i2 = i + CANONICAL;
                if (XML11Char.isXML11Invalid(dataarray[i])) {
                    ch = dataarray[i2 - 1];
                    if (XMLChar.isHighSurrogate(ch) && i2 < datalength) {
                        i = i2 + CANONICAL;
                        ch2 = dataarray[i2];
                        if (!XMLChar.isLowSurrogate(ch2)) {
                        } else if (!XMLChar.isSupplemental(XMLChar.supplemental(ch, ch2))) {
                            i2 = i;
                        }
                    }
                    refInvalidChar = new Character(ch);
                    return false;
                }
                i = i2;
            }
        } else {
            i = 0;
            while (i < datalength) {
                i2 = i + CANONICAL;
                if (XMLChar.isInvalid(dataarray[i])) {
                    ch = dataarray[i2 - 1];
                    if (XMLChar.isHighSurrogate(ch) && i2 < datalength) {
                        i = i2 + CANONICAL;
                        ch2 = dataarray[i2];
                        if (!XMLChar.isLowSurrogate(ch2)) {
                        } else if (!XMLChar.isSupplemental(XMLChar.supplemental(ch, ch2))) {
                            i2 = i;
                        }
                    }
                    refInvalidChar = new Character(ch);
                    return false;
                }
                i = i2;
            }
        }
        return true;
    }

    protected Character isWFXMLChar(String chardata) {
        if (chardata == null || chardata.length() == 0) {
            return null;
        }
        char[] dataarray = chardata.toCharArray();
        int datalength = dataarray.length;
        int i;
        int i2;
        char ch;
        char ch2;
        if (this.fIsXMLVersion11) {
            i = 0;
            while (i < datalength) {
                i2 = i + CANONICAL;
                if (XML11Char.isXML11Invalid(dataarray[i])) {
                    ch = dataarray[i2 - 1];
                    if (XMLChar.isHighSurrogate(ch) && i2 < datalength) {
                        i = i2 + CANONICAL;
                        ch2 = dataarray[i2];
                        if (!XMLChar.isLowSurrogate(ch2)) {
                        } else if (!XMLChar.isSupplemental(XMLChar.supplemental(ch, ch2))) {
                            i2 = i;
                        }
                    }
                    return new Character(ch);
                }
                i = i2;
            }
        } else {
            i = 0;
            while (i < datalength) {
                i2 = i + CANONICAL;
                if (XMLChar.isInvalid(dataarray[i])) {
                    ch = dataarray[i2 - 1];
                    if (XMLChar.isHighSurrogate(ch) && i2 < datalength) {
                        i = i2 + CANONICAL;
                        ch2 = dataarray[i2];
                        if (!XMLChar.isLowSurrogate(ch2)) {
                        } else if (!XMLChar.isSupplemental(XMLChar.supplemental(ch, ch2))) {
                            i2 = i;
                        }
                    }
                    return new Character(ch);
                }
                i = i2;
            }
        }
        return null;
    }

    protected void isCommentWellFormed(String data) {
        if (data != null && data.length() != 0) {
            char[] dataarray = data.toCharArray();
            int datalength = dataarray.length;
            int i;
            int i2;
            char c;
            char c2;
            Messages messages;
            String str;
            Object[] objArr;
            String msg;
            if (this.fIsXMLVersion11) {
                i = 0;
                while (i < datalength) {
                    i2 = i + CANONICAL;
                    c = dataarray[i];
                    if (XML11Char.isXML11Invalid(c)) {
                        if (XMLChar.isHighSurrogate(c) && i2 < datalength) {
                            i = i2 + CANONICAL;
                            c2 = dataarray[i2];
                            if (!XMLChar.isLowSurrogate(c2)) {
                                i2 = i;
                            } else if (!XMLChar.isSupplemental(XMLChar.supplemental(c, c2))) {
                                i2 = i;
                            }
                        }
                        messages = Utils.messages;
                        str = MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT;
                        objArr = new Object[CANONICAL];
                        objArr[0] = new Character(c);
                        msg = messages.createMessage(str, objArr);
                        if (this.fErrorHandler != null) {
                            this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null));
                        }
                    } else if (c == '-' && i2 < datalength && dataarray[i2] == '-') {
                        msg = Utils.messages.createMessage(MsgKey.ER_WF_DASH_IN_COMMENT, null);
                        if (this.fErrorHandler != null) {
                            this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null));
                        }
                    }
                    i = i2;
                }
            } else {
                i = 0;
                while (i < datalength) {
                    i2 = i + CANONICAL;
                    c = dataarray[i];
                    if (XMLChar.isInvalid(c)) {
                        if (XMLChar.isHighSurrogate(c) && i2 < datalength) {
                            i = i2 + CANONICAL;
                            c2 = dataarray[i2];
                            if (!XMLChar.isLowSurrogate(c2)) {
                                i2 = i;
                            } else if (!XMLChar.isSupplemental(XMLChar.supplemental(c, c2))) {
                                i2 = i;
                            }
                        }
                        messages = Utils.messages;
                        str = MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT;
                        objArr = new Object[CANONICAL];
                        objArr[0] = new Character(c);
                        msg = messages.createMessage(str, objArr);
                        if (this.fErrorHandler != null) {
                            this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null));
                        }
                    } else if (c == '-' && i2 < datalength && dataarray[i2] == '-') {
                        msg = Utils.messages.createMessage(MsgKey.ER_WF_DASH_IN_COMMENT, null);
                        if (this.fErrorHandler != null) {
                            this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null));
                        }
                    }
                    i = i2;
                }
            }
        }
    }

    protected void isElementWellFormed(Node node) {
        boolean isNameWF;
        if ((this.fFeatures & NAMESPACES) != 0) {
            isNameWF = isValidQName(node.getPrefix(), node.getLocalName(), this.fIsXMLVersion11);
        } else {
            isNameWF = isXMLName(node.getNodeName(), this.fIsXMLVersion11);
        }
        if (!isNameWF) {
            Messages messages = Utils.messages;
            String str = MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME;
            Object[] objArr = new Object[CDATA];
            objArr[0] = "Element";
            objArr[CANONICAL] = node.getNodeName();
            String msg = messages.createMessage(str, objArr);
            if (this.fErrorHandler != null) {
                this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, null, null, null));
            }
        }
    }

    protected void isAttributeWellFormed(Node node) {
        boolean isNameWF;
        String msg;
        if ((this.fFeatures & NAMESPACES) != 0) {
            isNameWF = isValidQName(node.getPrefix(), node.getLocalName(), this.fIsXMLVersion11);
        } else {
            isNameWF = isXMLName(node.getNodeName(), this.fIsXMLVersion11);
        }
        if (!isNameWF) {
            Messages messages = Utils.messages;
            String str = MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME;
            Object[] objArr = new Object[CDATA];
            objArr[0] = "Attr";
            objArr[CANONICAL] = node.getNodeName();
            msg = messages.createMessage(str, objArr);
            if (this.fErrorHandler != null) {
                this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, null, null, null));
            }
        }
        if (node.getNodeValue().indexOf(60) >= 0) {
            Messages messages2 = Utils.messages;
            String str2 = MsgKey.ER_WF_LT_IN_ATTVAL;
            Object[] objArr2 = new Object[CDATA];
            objArr2[0] = ((Attr) node).getOwnerElement().getNodeName();
            objArr2[CANONICAL] = node.getNodeName();
            msg = messages2.createMessage(str2, objArr2);
            if (this.fErrorHandler != null) {
                this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_LT_IN_ATTVAL, null, null, null));
            }
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i += CANONICAL) {
            Node child = children.item(i);
            if (child != null) {
                switch (child.getNodeType()) {
                    case OpCodes.OP_AND /*3*/:
                        isTextWellFormed((Text) child);
                        break;
                    case OpCodes.OP_EQUALS /*5*/:
                        isEntityReferneceWellFormed((EntityReference) child);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    protected void isPIWellFormed(ProcessingInstruction node) {
        String msg;
        if (!isXMLName(node.getNodeName(), this.fIsXMLVersion11)) {
            Messages messages = Utils.messages;
            String str = MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME;
            Object[] objArr = new Object[CDATA];
            objArr[0] = "ProcessingInstruction";
            objArr[CANONICAL] = node.getTarget();
            msg = messages.createMessage(str, objArr);
            if (this.fErrorHandler != null) {
                this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, null, null, null));
            }
        }
        Character invalidChar = isWFXMLChar(node.getData());
        if (invalidChar != null) {
            messages = Utils.messages;
            str = MsgKey.ER_WF_INVALID_CHARACTER_IN_PI;
            objArr = new Object[CANONICAL];
            objArr[0] = Integer.toHexString(Character.getNumericValue(invalidChar.charValue()));
            msg = messages.createMessage(str, objArr);
            if (this.fErrorHandler != null) {
                this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null));
            }
        }
    }

    protected void isCDATASectionWellFormed(CDATASection node) {
        Character invalidChar = isWFXMLChar(node.getData());
        if (invalidChar != null) {
            Messages messages = Utils.messages;
            String str = MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA;
            Object[] objArr = new Object[CANONICAL];
            objArr[0] = Integer.toHexString(Character.getNumericValue(invalidChar.charValue()));
            String msg = messages.createMessage(str, objArr);
            if (this.fErrorHandler != null) {
                this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null));
            }
        }
    }

    protected void isTextWellFormed(Text node) {
        Character invalidChar = isWFXMLChar(node.getData());
        if (invalidChar != null) {
            Messages messages = Utils.messages;
            String str = MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT;
            Object[] objArr = new Object[CANONICAL];
            objArr[0] = Integer.toHexString(Character.getNumericValue(invalidChar.charValue()));
            String msg = messages.createMessage(str, objArr);
            if (this.fErrorHandler != null) {
                this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null));
            }
        }
    }

    protected void isEntityReferneceWellFormed(EntityReference node) {
        String msg;
        if (!isXMLName(node.getNodeName(), this.fIsXMLVersion11)) {
            Messages messages = Utils.messages;
            String str = MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME;
            Object[] objArr = new Object[CDATA];
            objArr[0] = "EntityReference";
            objArr[CANONICAL] = node.getNodeName();
            msg = messages.createMessage(str, objArr);
            if (this.fErrorHandler != null) {
                this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, null, null, null));
            }
        }
        Node parent = node.getParentNode();
        DocumentType docType = node.getOwnerDocument().getDoctype();
        if (docType != null) {
            NamedNodeMap entities = docType.getEntities();
            for (int i = 0; i < entities.getLength(); i += CANONICAL) {
                Entity ent = (Entity) entities.item(i);
                String nodeName = node.getNodeName() == null ? SerializerConstants.EMPTYSTRING : node.getNodeName();
                String nodeNamespaceURI;
                if (node.getNamespaceURI() == null) {
                    nodeNamespaceURI = SerializerConstants.EMPTYSTRING;
                } else {
                    nodeNamespaceURI = node.getNamespaceURI();
                }
                String entName = ent.getNodeName() == null ? SerializerConstants.EMPTYSTRING : ent.getNodeName();
                String entNamespaceURI = ent.getNamespaceURI() == null ? SerializerConstants.EMPTYSTRING : ent.getNamespaceURI();
                if (parent.getNodeType() == (short) 1 && entNamespaceURI.equals(nodeNamespaceURI) && entName.equals(nodeName) && ent.getNotationName() != null) {
                    messages = Utils.messages;
                    str = MsgKey.ER_WF_REF_TO_UNPARSED_ENT;
                    objArr = new Object[CANONICAL];
                    objArr[0] = node.getNodeName();
                    msg = messages.createMessage(str, objArr);
                    if (this.fErrorHandler != null) {
                        this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_REF_TO_UNPARSED_ENT, null, null, null));
                    }
                }
                if (parent.getNodeType() == (short) 2 && entNamespaceURI.equals(nodeNamespaceURI) && entName.equals(nodeName)) {
                    if (ent.getPublicId() == null && ent.getSystemId() == null) {
                        if (ent.getNotationName() != null) {
                        }
                    }
                    messages = Utils.messages;
                    str = MsgKey.ER_WF_REF_TO_EXTERNAL_ENT;
                    objArr = new Object[CANONICAL];
                    objArr[0] = node.getNodeName();
                    msg = messages.createMessage(str, objArr);
                    if (this.fErrorHandler != null) {
                        this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, null, null, null));
                    }
                }
            }
        }
    }

    protected void checkUnboundPrefixInEntRef(Node node) {
        Node child = node.getFirstChild();
        while (child != null) {
            Node next = child.getNextSibling();
            if (child.getNodeType() == (short) 1) {
                String msg;
                String prefix = child.getPrefix();
                if (prefix != null && this.fNSBinder.getURI(prefix) == null) {
                    msg = Utils.messages.createMessage(MsgKey.ER_ELEM_UNBOUND_PREFIX_IN_ENTREF, new Object[]{node.getNodeName(), child.getNodeName(), prefix});
                    if (this.fErrorHandler != null) {
                        this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_ELEM_UNBOUND_PREFIX_IN_ENTREF, null, null, null));
                    }
                }
                NamedNodeMap attrs = child.getAttributes();
                for (int i = 0; i < attrs.getLength(); i += CANONICAL) {
                    String attrPrefix = attrs.item(i).getPrefix();
                    if (attrPrefix != null && this.fNSBinder.getURI(attrPrefix) == null) {
                        msg = Utils.messages.createMessage(MsgKey.ER_ELEM_UNBOUND_PREFIX_IN_ENTREF, new Object[]{node.getNodeName(), child.getNodeName(), attrs.item(i)});
                        if (this.fErrorHandler != null) {
                            this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_ELEM_UNBOUND_PREFIX_IN_ENTREF, null, null, null));
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

    protected void recordLocalNSDecl(Node node) {
        NamedNodeMap atts = ((Element) node).getAttributes();
        int length = atts.getLength();
        for (int i = 0; i < length; i += CANONICAL) {
            Node attr = atts.item(i);
            String localName = attr.getLocalName();
            String attrPrefix = attr.getPrefix();
            String attrValue = attr.getNodeValue();
            String attrNS = attr.getNamespaceURI();
            if (localName == null || XMLNS_PREFIX.equals(localName)) {
                localName = SerializerConstants.EMPTYSTRING;
            }
            if (attrPrefix == null) {
                attrPrefix = SerializerConstants.EMPTYSTRING;
            }
            if (attrValue == null) {
                attrValue = SerializerConstants.EMPTYSTRING;
            }
            if (attrNS == null) {
                attrNS = SerializerConstants.EMPTYSTRING;
            }
            if (XMLNS_URI.equals(attrNS)) {
                if (XMLNS_URI.equals(attrValue)) {
                    Messages messages = Utils.messages;
                    String str = MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND;
                    Object[] objArr = new Object[CDATA];
                    objArr[0] = attrPrefix;
                    objArr[CANONICAL] = XMLNS_URI;
                    String msg = messages.createMessage(str, objArr);
                    if (this.fErrorHandler != null) {
                        DOMErrorHandler dOMErrorHandler = this.fErrorHandler;
                        r16.handleError(new DOMErrorImpl((short) 2, msg, MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, null, null, null));
                    }
                } else if (!XMLNS_PREFIX.equals(attrPrefix)) {
                    this.fNSBinder.declarePrefix(SerializerConstants.EMPTYSTRING, attrValue);
                } else if (attrValue.length() != 0) {
                    this.fNSBinder.declarePrefix(localName, attrValue);
                }
            }
        }
    }

    protected void fixupElementNS(Node node) throws SAXException {
        String namespaceURI = ((Element) node).getNamespaceURI();
        String prefix = ((Element) node).getPrefix();
        String localName = ((Element) node).getLocalName();
        if (namespaceURI != null) {
            if (prefix == null) {
                prefix = SerializerConstants.EMPTYSTRING;
            }
            String inScopeNamespaceURI = this.fNSBinder.getURI(prefix);
            if (inScopeNamespaceURI == null || !inScopeNamespaceURI.equals(namespaceURI)) {
                if ((this.fFeatures & NAMESPACEDECLS) != 0) {
                    if (SerializerConstants.EMPTYSTRING.equals(prefix) || SerializerConstants.EMPTYSTRING.equals(namespaceURI)) {
                        ((Element) node).setAttributeNS(XMLNS_URI, XMLNS_PREFIX, namespaceURI);
                    } else {
                        ((Element) node).setAttributeNS(XMLNS_URI, Constants.ATTRNAME_XMLNS + prefix, namespaceURI);
                    }
                }
                this.fLocalNSBinder.declarePrefix(prefix, namespaceURI);
                this.fNSBinder.declarePrefix(prefix, namespaceURI);
            }
        } else if (localName == null || SerializerConstants.EMPTYSTRING.equals(localName)) {
            Messages messages = Utils.messages;
            String str = MsgKey.ER_NULL_LOCAL_ELEMENT_NAME;
            Object[] objArr = new Object[CANONICAL];
            objArr[0] = node.getNodeName();
            String msg = messages.createMessage(str, objArr);
            if (this.fErrorHandler != null) {
                this.fErrorHandler.handleError(new DOMErrorImpl((short) 2, msg, MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, null, null, null));
            }
        } else {
            namespaceURI = this.fNSBinder.getURI(SerializerConstants.EMPTYSTRING);
            if (namespaceURI != null && namespaceURI.length() > 0) {
                ((Element) node).setAttributeNS(XMLNS_URI, XMLNS_PREFIX, SerializerConstants.EMPTYSTRING);
                this.fLocalNSBinder.declarePrefix(SerializerConstants.EMPTYSTRING, SerializerConstants.EMPTYSTRING);
                this.fNSBinder.declarePrefix(SerializerConstants.EMPTYSTRING, SerializerConstants.EMPTYSTRING);
            }
        }
    }
}
