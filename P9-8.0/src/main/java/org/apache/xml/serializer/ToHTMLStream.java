package org.apache.xml.serializer;

import java.io.IOException;
import java.io.Writer;
import java.util.Properties;
import org.apache.xalan.templates.Constants;
import org.apache.xml.dtm.DTMFilter;
import org.apache.xml.serializer.utils.Utils;
import org.apache.xpath.axes.WalkerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ToHTMLStream extends ToStream {
    private static final ElemDesc m_dummy = new ElemDesc(8);
    static final Trie m_elementFlags = new Trie();
    private Trie m_htmlInfo;
    private final CharInfo m_htmlcharInfo;
    private boolean m_inBlockElem;
    protected boolean m_inDTD;
    private boolean m_omitMetaTag;
    private boolean m_specialEscapeURLs;

    static class Trie {
        public static final int ALPHA_SIZE = 128;
        final Node m_Root;
        private char[] m_charBuffer;
        private final boolean m_lowerCaseOnly;

        private class Node {
            Object m_Value = null;
            final Node[] m_nextChar = new Node[128];

            Node() {
            }
        }

        public Trie() {
            this.m_charBuffer = new char[0];
            this.m_Root = new Node();
            this.m_lowerCaseOnly = false;
        }

        public Trie(boolean lowerCaseOnly) {
            this.m_charBuffer = new char[0];
            this.m_Root = new Node();
            this.m_lowerCaseOnly = lowerCaseOnly;
        }

        public Object put(String key, Object value) {
            Object ret;
            int len = key.length();
            if (len > this.m_charBuffer.length) {
                this.m_charBuffer = new char[len];
            }
            Node node = this.m_Root;
            int i = 0;
            while (i < len) {
                Node nextNode = node.m_nextChar[Character.toLowerCase(key.charAt(i))];
                if (nextNode != null) {
                    node = nextNode;
                    i++;
                } else {
                    while (i < len) {
                        Node newNode = new Node();
                        if (this.m_lowerCaseOnly) {
                            node.m_nextChar[Character.toLowerCase(key.charAt(i))] = newNode;
                        } else {
                            node.m_nextChar[Character.toUpperCase(key.charAt(i))] = newNode;
                            node.m_nextChar[Character.toLowerCase(key.charAt(i))] = newNode;
                        }
                        node = newNode;
                        i++;
                    }
                    ret = node.m_Value;
                    node.m_Value = value;
                    return ret;
                }
            }
            ret = node.m_Value;
            node.m_Value = value;
            return ret;
        }

        public Object get(String key) {
            int len = key.length();
            if (this.m_charBuffer.length < len) {
                return null;
            }
            Node node = this.m_Root;
            char ch;
            switch (len) {
                case 0:
                    return null;
                case 1:
                    ch = key.charAt(0);
                    if (ch < 128) {
                        node = node.m_nextChar[ch];
                        if (node != null) {
                            return node.m_Value;
                        }
                    }
                    return null;
                default:
                    for (int i = 0; i < len; i++) {
                        ch = key.charAt(i);
                        if (128 <= ch) {
                            return null;
                        }
                        node = node.m_nextChar[ch];
                        if (node == null) {
                            return null;
                        }
                    }
                    return node.m_Value;
            }
        }

        public Trie(Trie existingTrie) {
            this.m_charBuffer = new char[0];
            this.m_Root = existingTrie.m_Root;
            this.m_lowerCaseOnly = existingTrie.m_lowerCaseOnly;
            this.m_charBuffer = new char[existingTrie.getLongestKeyLength()];
        }

        public Object get2(String key) {
            int len = key.length();
            if (this.m_charBuffer.length < len) {
                return null;
            }
            Node node = this.m_Root;
            char ch;
            switch (len) {
                case 0:
                    return null;
                case 1:
                    ch = key.charAt(0);
                    if (ch < 128) {
                        node = node.m_nextChar[ch];
                        if (node != null) {
                            return node.m_Value;
                        }
                    }
                    return null;
                default:
                    key.getChars(0, len, this.m_charBuffer, 0);
                    for (int i = 0; i < len; i++) {
                        ch = this.m_charBuffer[i];
                        if (128 <= ch) {
                            return null;
                        }
                        node = node.m_nextChar[ch];
                        if (node == null) {
                            return null;
                        }
                    }
                    return node.m_Value;
            }
        }

        public int getLongestKeyLength() {
            return this.m_charBuffer.length;
        }
    }

    static {
        initTagReference(m_elementFlags);
    }

    static void initTagReference(Trie m_elementFlags) {
        m_elementFlags.put("BASEFONT", new ElemDesc(2));
        m_elementFlags.put("FRAME", new ElemDesc(10));
        m_elementFlags.put("FRAMESET", new ElemDesc(8));
        m_elementFlags.put("NOFRAMES", new ElemDesc(8));
        m_elementFlags.put("ISINDEX", new ElemDesc(10));
        m_elementFlags.put("APPLET", new ElemDesc(WalkerFactory.BIT_NAMESPACE));
        m_elementFlags.put("CENTER", new ElemDesc(8));
        m_elementFlags.put("DIR", new ElemDesc(8));
        m_elementFlags.put("MENU", new ElemDesc(8));
        m_elementFlags.put("TT", new ElemDesc(4096));
        m_elementFlags.put("I", new ElemDesc(4096));
        m_elementFlags.put("B", new ElemDesc(4096));
        m_elementFlags.put("BIG", new ElemDesc(4096));
        m_elementFlags.put("SMALL", new ElemDesc(4096));
        m_elementFlags.put("EM", new ElemDesc(WalkerFactory.BIT_ANCESTOR));
        m_elementFlags.put("STRONG", new ElemDesc(WalkerFactory.BIT_ANCESTOR));
        m_elementFlags.put("DFN", new ElemDesc(WalkerFactory.BIT_ANCESTOR));
        m_elementFlags.put("CODE", new ElemDesc(WalkerFactory.BIT_ANCESTOR));
        m_elementFlags.put("SAMP", new ElemDesc(WalkerFactory.BIT_ANCESTOR));
        m_elementFlags.put("KBD", new ElemDesc(WalkerFactory.BIT_ANCESTOR));
        m_elementFlags.put("VAR", new ElemDesc(WalkerFactory.BIT_ANCESTOR));
        m_elementFlags.put("CITE", new ElemDesc(WalkerFactory.BIT_ANCESTOR));
        m_elementFlags.put("ABBR", new ElemDesc(WalkerFactory.BIT_ANCESTOR));
        m_elementFlags.put("ACRONYM", new ElemDesc(WalkerFactory.BIT_ANCESTOR));
        m_elementFlags.put("SUP", new ElemDesc(98304));
        m_elementFlags.put("SUB", new ElemDesc(98304));
        m_elementFlags.put("SPAN", new ElemDesc(98304));
        m_elementFlags.put("BDO", new ElemDesc(98304));
        m_elementFlags.put("BR", new ElemDesc(98314));
        m_elementFlags.put("BODY", new ElemDesc(8));
        m_elementFlags.put("ADDRESS", new ElemDesc(56));
        m_elementFlags.put("DIV", new ElemDesc(56));
        m_elementFlags.put("A", new ElemDesc(WalkerFactory.BIT_ATTRIBUTE));
        m_elementFlags.put("MAP", new ElemDesc(98312));
        m_elementFlags.put("AREA", new ElemDesc(10));
        m_elementFlags.put("LINK", new ElemDesc(131082));
        m_elementFlags.put("IMG", new ElemDesc(2195458));
        m_elementFlags.put("OBJECT", new ElemDesc(2326528));
        m_elementFlags.put("PARAM", new ElemDesc(2));
        m_elementFlags.put("HR", new ElemDesc(58));
        m_elementFlags.put("P", new ElemDesc(56));
        m_elementFlags.put("H1", new ElemDesc(262152));
        m_elementFlags.put("H2", new ElemDesc(262152));
        m_elementFlags.put("H3", new ElemDesc(262152));
        m_elementFlags.put("H4", new ElemDesc(262152));
        m_elementFlags.put("H5", new ElemDesc(262152));
        m_elementFlags.put("H6", new ElemDesc(262152));
        m_elementFlags.put("PRE", new ElemDesc(1048584));
        m_elementFlags.put("Q", new ElemDesc(98304));
        m_elementFlags.put("BLOCKQUOTE", new ElemDesc(56));
        m_elementFlags.put("INS", new ElemDesc(0));
        m_elementFlags.put("DEL", new ElemDesc(0));
        m_elementFlags.put("DL", new ElemDesc(56));
        m_elementFlags.put("DT", new ElemDesc(8));
        m_elementFlags.put("DD", new ElemDesc(8));
        m_elementFlags.put("OL", new ElemDesc(524296));
        m_elementFlags.put("UL", new ElemDesc(524296));
        m_elementFlags.put("LI", new ElemDesc(8));
        m_elementFlags.put("FORM", new ElemDesc(8));
        m_elementFlags.put("LABEL", new ElemDesc(WalkerFactory.BIT_ANCESTOR_OR_SELF));
        m_elementFlags.put("INPUT", new ElemDesc(18434));
        m_elementFlags.put("SELECT", new ElemDesc(18432));
        m_elementFlags.put("OPTGROUP", new ElemDesc(0));
        m_elementFlags.put("OPTION", new ElemDesc(0));
        m_elementFlags.put("TEXTAREA", new ElemDesc(18432));
        m_elementFlags.put("FIELDSET", new ElemDesc(24));
        m_elementFlags.put("LEGEND", new ElemDesc(0));
        m_elementFlags.put("BUTTON", new ElemDesc(18432));
        m_elementFlags.put("TABLE", new ElemDesc(56));
        m_elementFlags.put("CAPTION", new ElemDesc(8));
        m_elementFlags.put("THEAD", new ElemDesc(8));
        m_elementFlags.put("TFOOT", new ElemDesc(8));
        m_elementFlags.put("TBODY", new ElemDesc(8));
        m_elementFlags.put("COLGROUP", new ElemDesc(8));
        m_elementFlags.put("COL", new ElemDesc(10));
        m_elementFlags.put("TR", new ElemDesc(8));
        m_elementFlags.put("TH", new ElemDesc(0));
        m_elementFlags.put("TD", new ElemDesc(0));
        m_elementFlags.put("HEAD", new ElemDesc(4194312));
        m_elementFlags.put("TITLE", new ElemDesc(8));
        m_elementFlags.put("BASE", new ElemDesc(10));
        m_elementFlags.put("META", new ElemDesc(131082));
        m_elementFlags.put("STYLE", new ElemDesc(131336));
        m_elementFlags.put("SCRIPT", new ElemDesc(229632));
        m_elementFlags.put("NOSCRIPT", new ElemDesc(56));
        m_elementFlags.put("HTML", new ElemDesc(8388616));
        m_elementFlags.put("FONT", new ElemDesc(4096));
        m_elementFlags.put("S", new ElemDesc(4096));
        m_elementFlags.put("STRIKE", new ElemDesc(4096));
        m_elementFlags.put("U", new ElemDesc(4096));
        m_elementFlags.put("NOBR", new ElemDesc(4096));
        m_elementFlags.put("IFRAME", new ElemDesc(56));
        m_elementFlags.put("LAYER", new ElemDesc(56));
        m_elementFlags.put("ILAYER", new ElemDesc(56));
        ElemDesc elemDesc = (ElemDesc) m_elementFlags.get("a");
        elemDesc.setAttr("HREF", 2);
        elemDesc.setAttr("NAME", 2);
        elemDesc = (ElemDesc) m_elementFlags.get("area");
        elemDesc.setAttr("HREF", 2);
        elemDesc.setAttr("NOHREF", 4);
        ((ElemDesc) m_elementFlags.get("base")).setAttr("HREF", 2);
        ((ElemDesc) m_elementFlags.get("button")).setAttr("DISABLED", 4);
        ((ElemDesc) m_elementFlags.get("blockquote")).setAttr("CITE", 2);
        ((ElemDesc) m_elementFlags.get("del")).setAttr("CITE", 2);
        ((ElemDesc) m_elementFlags.get("dir")).setAttr("COMPACT", 4);
        elemDesc = (ElemDesc) m_elementFlags.get("div");
        elemDesc.setAttr("SRC", 2);
        elemDesc.setAttr("NOWRAP", 4);
        ((ElemDesc) m_elementFlags.get("dl")).setAttr("COMPACT", 4);
        ((ElemDesc) m_elementFlags.get("form")).setAttr("ACTION", 2);
        elemDesc = (ElemDesc) m_elementFlags.get("frame");
        elemDesc.setAttr("SRC", 2);
        elemDesc.setAttr("LONGDESC", 2);
        elemDesc.setAttr("NORESIZE", 4);
        ((ElemDesc) m_elementFlags.get("head")).setAttr("PROFILE", 2);
        ((ElemDesc) m_elementFlags.get("hr")).setAttr("NOSHADE", 4);
        elemDesc = (ElemDesc) m_elementFlags.get("iframe");
        elemDesc.setAttr("SRC", 2);
        elemDesc.setAttr("LONGDESC", 2);
        ((ElemDesc) m_elementFlags.get("ilayer")).setAttr("SRC", 2);
        elemDesc = (ElemDesc) m_elementFlags.get("img");
        elemDesc.setAttr("SRC", 2);
        elemDesc.setAttr("LONGDESC", 2);
        elemDesc.setAttr("USEMAP", 2);
        elemDesc.setAttr("ISMAP", 4);
        elemDesc = (ElemDesc) m_elementFlags.get("input");
        elemDesc.setAttr("SRC", 2);
        elemDesc.setAttr("USEMAP", 2);
        elemDesc.setAttr("CHECKED", 4);
        elemDesc.setAttr("DISABLED", 4);
        elemDesc.setAttr("ISMAP", 4);
        elemDesc.setAttr("READONLY", 4);
        ((ElemDesc) m_elementFlags.get("ins")).setAttr("CITE", 2);
        ((ElemDesc) m_elementFlags.get("layer")).setAttr("SRC", 2);
        ((ElemDesc) m_elementFlags.get("link")).setAttr("HREF", 2);
        ((ElemDesc) m_elementFlags.get("menu")).setAttr("COMPACT", 4);
        elemDesc = (ElemDesc) m_elementFlags.get("object");
        elemDesc.setAttr("CLASSID", 2);
        elemDesc.setAttr("CODEBASE", 2);
        elemDesc.setAttr("DATA", 2);
        elemDesc.setAttr("ARCHIVE", 2);
        elemDesc.setAttr("USEMAP", 2);
        elemDesc.setAttr("DECLARE", 4);
        ((ElemDesc) m_elementFlags.get("ol")).setAttr("COMPACT", 4);
        ((ElemDesc) m_elementFlags.get("optgroup")).setAttr("DISABLED", 4);
        elemDesc = (ElemDesc) m_elementFlags.get("option");
        elemDesc.setAttr("SELECTED", 4);
        elemDesc.setAttr("DISABLED", 4);
        ((ElemDesc) m_elementFlags.get("q")).setAttr("CITE", 2);
        elemDesc = (ElemDesc) m_elementFlags.get(Constants.ELEMNAME_SCRIPT_STRING);
        elemDesc.setAttr("SRC", 2);
        elemDesc.setAttr("FOR", 2);
        elemDesc.setAttr("DEFER", 4);
        elemDesc = (ElemDesc) m_elementFlags.get(Constants.ATTRNAME_SELECT);
        elemDesc.setAttr("DISABLED", 4);
        elemDesc.setAttr("MULTIPLE", 4);
        ((ElemDesc) m_elementFlags.get("table")).setAttr("NOWRAP", 4);
        ((ElemDesc) m_elementFlags.get("td")).setAttr("NOWRAP", 4);
        elemDesc = (ElemDesc) m_elementFlags.get("textarea");
        elemDesc.setAttr("DISABLED", 4);
        elemDesc.setAttr("READONLY", 4);
        ((ElemDesc) m_elementFlags.get("th")).setAttr("NOWRAP", 4);
        ((ElemDesc) m_elementFlags.get("tr")).setAttr("NOWRAP", 4);
        ((ElemDesc) m_elementFlags.get("ul")).setAttr("COMPACT", 4);
    }

    public void setSpecialEscapeURLs(boolean bool) {
        this.m_specialEscapeURLs = bool;
    }

    public void setOmitMetaTag(boolean bool) {
        this.m_omitMetaTag = bool;
    }

    public void setOutputFormat(Properties format) {
        if (format.getProperty(OutputPropertiesFactory.S_USE_URL_ESCAPING) != null) {
            this.m_specialEscapeURLs = OutputPropertyUtils.getBooleanProperty(OutputPropertiesFactory.S_USE_URL_ESCAPING, format);
        }
        if (format.getProperty(OutputPropertiesFactory.S_OMIT_META_TAG) != null) {
            this.m_omitMetaTag = OutputPropertyUtils.getBooleanProperty(OutputPropertiesFactory.S_OMIT_META_TAG, format);
        }
        super.setOutputFormat(format);
    }

    private final boolean getSpecialEscapeURLs() {
        return this.m_specialEscapeURLs;
    }

    private final boolean getOmitMetaTag() {
        return this.m_omitMetaTag;
    }

    public static final ElemDesc getElemDesc(String name) {
        Object obj = m_elementFlags.get(name);
        if (obj != null) {
            return (ElemDesc) obj;
        }
        return m_dummy;
    }

    private ElemDesc getElemDesc2(String name) {
        Object obj = this.m_htmlInfo.get2(name);
        if (obj != null) {
            return (ElemDesc) obj;
        }
        return m_dummy;
    }

    public ToHTMLStream() {
        this.m_inDTD = false;
        this.m_inBlockElem = false;
        this.m_htmlcharInfo = CharInfo.getCharInfo(CharInfo.HTML_ENTITIES_RESOURCE, "html");
        this.m_specialEscapeURLs = true;
        this.m_omitMetaTag = false;
        this.m_htmlInfo = new Trie(m_elementFlags);
        this.m_doIndent = true;
        this.m_charInfo = this.m_htmlcharInfo;
        this.m_prefixMap = new NamespaceMappings();
    }

    protected void startDocumentInternal() throws SAXException {
        super.startDocumentInternal();
        this.m_needToCallStartDocument = false;
        this.m_needToOutputDocTypeDecl = true;
        this.m_startNewLine = false;
        setOmitXMLDeclaration(true);
    }

    private void outputDocTypeDecl(String name) throws SAXException {
        if (this.m_needToOutputDocTypeDecl) {
            String doctypeSystem = getDoctypeSystem();
            String doctypePublic = getDoctypePublic();
            if (!(doctypeSystem == null && doctypePublic == null)) {
                Writer writer = this.m_writer;
                try {
                    writer.write("<!DOCTYPE ");
                    writer.write(name);
                    if (doctypePublic != null) {
                        writer.write(" PUBLIC \"");
                        writer.write(doctypePublic);
                        writer.write(34);
                    }
                    if (doctypeSystem != null) {
                        if (doctypePublic == null) {
                            writer.write(" SYSTEM \"");
                        } else {
                            writer.write(" \"");
                        }
                        writer.write(doctypeSystem);
                        writer.write(34);
                    }
                    writer.write(62);
                    outputLineSep();
                } catch (IOException e) {
                    throw new SAXException(e);
                }
            }
        }
        this.m_needToOutputDocTypeDecl = false;
    }

    public final void endDocument() throws SAXException {
        flushPending();
        if (this.m_doIndent && (this.m_isprevtext ^ 1) != 0) {
            try {
                outputLineSep();
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
        flushWriter();
        if (this.m_tracer != null) {
            super.fireEndDoc();
        }
    }

    public void startElement(String namespaceURI, String localName, String name, Attributes atts) throws SAXException {
        ElemContext elemContext = this.m_elemContext;
        if (elemContext.m_startTagOpen) {
            closeStartTag();
            elemContext.m_startTagOpen = false;
        } else if (this.m_cdataTagOpen) {
            closeCDATA();
            this.m_cdataTagOpen = false;
        } else if (this.m_needToCallStartDocument) {
            startDocumentInternal();
            this.m_needToCallStartDocument = false;
        }
        if (this.m_needToOutputDocTypeDecl) {
            String n = name;
            if (name == null || name.length() == 0) {
                n = localName;
            }
            outputDocTypeDecl(n);
        }
        if (namespaceURI == null || namespaceURI.length() <= 0) {
            try {
                ElemDesc elemDesc = getElemDesc2(name);
                int elemFlags = elemDesc.getFlags();
                if (this.m_doIndent) {
                    boolean isBlockElement = (elemFlags & 8) != 0;
                    if (this.m_ispreserve) {
                        this.m_ispreserve = false;
                    } else if (elemContext.m_elementName != null && (!this.m_inBlockElem || isBlockElement)) {
                        this.m_startNewLine = true;
                        indent();
                    }
                    this.m_inBlockElem = isBlockElement ^ 1;
                }
                if (atts != null) {
                    addAttributes(atts);
                }
                this.m_isprevtext = false;
                Writer writer = this.m_writer;
                writer.write(60);
                writer.write(name);
                if (this.m_tracer != null) {
                    firePseudoAttributes();
                }
                if ((elemFlags & 2) != 0) {
                    this.m_elemContext = elemContext.push();
                    this.m_elemContext.m_elementName = name;
                    this.m_elemContext.m_elementDesc = elemDesc;
                    return;
                }
                elemContext = elemContext.push(namespaceURI, localName, name);
                this.m_elemContext = elemContext;
                elemContext.m_elementDesc = elemDesc;
                elemContext.m_isRaw = (elemFlags & DTMFilter.SHOW_DOCUMENT) != 0;
                if ((WalkerFactory.BIT_PARENT & elemFlags) != 0) {
                    closeStartTag();
                    elemContext.m_startTagOpen = false;
                    if (!this.m_omitMetaTag) {
                        if (this.m_doIndent) {
                            indent();
                        }
                        writer.write("<META http-equiv=\"Content-Type\" content=\"text/html; charset=");
                        writer.write(Encodings.getMimeEncoding(getEncoding()));
                        writer.write("\">");
                    }
                }
                return;
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
        super.startElement(namespaceURI, localName, name, atts);
    }

    public final void endElement(String namespaceURI, String localName, String name) throws SAXException {
        if (this.m_cdataTagOpen) {
            closeCDATA();
        }
        if (namespaceURI == null || namespaceURI.length() <= 0) {
            try {
                ElemContext elemContext = this.m_elemContext;
                int elemFlags = elemContext.m_elementDesc.getFlags();
                boolean elemEmpty = (elemFlags & 2) != 0;
                if (this.m_doIndent) {
                    boolean isBlockElement = (elemFlags & 8) != 0;
                    boolean shouldIndent = false;
                    if (this.m_ispreserve) {
                        this.m_ispreserve = false;
                    } else if (this.m_doIndent && (!this.m_inBlockElem || isBlockElement)) {
                        this.m_startNewLine = true;
                        shouldIndent = true;
                    }
                    if (!elemContext.m_startTagOpen && shouldIndent) {
                        indent(elemContext.m_currentElemDepth - 1);
                    }
                    this.m_inBlockElem = isBlockElement ^ 1;
                }
                Writer writer = this.m_writer;
                if (elemContext.m_startTagOpen) {
                    if (this.m_tracer != null) {
                        super.fireStartElem(name);
                    }
                    int nAttrs = this.m_attributes.getLength();
                    if (nAttrs > 0) {
                        processAttributes(this.m_writer, nAttrs);
                        this.m_attributes.clear();
                    }
                    if (elemEmpty) {
                        writer.write(62);
                    } else {
                        writer.write("></");
                        writer.write(name);
                        writer.write(62);
                    }
                } else {
                    writer.write("</");
                    writer.write(name);
                    writer.write(62);
                }
                if ((WalkerFactory.BIT_NAMESPACE & elemFlags) != 0) {
                    this.m_ispreserve = true;
                }
                this.m_isprevtext = false;
                if (this.m_tracer != null) {
                    super.fireEndElem(name);
                }
                if (elemEmpty) {
                    this.m_elemContext = elemContext.m_prev;
                    return;
                }
                if (!(elemContext.m_startTagOpen || !this.m_doIndent || (this.m_preserves.isEmpty() ^ 1) == 0)) {
                    this.m_preserves.pop();
                }
                this.m_elemContext = elemContext.m_prev;
                return;
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
        super.endElement(namespaceURI, localName, name);
    }

    protected void processAttribute(Writer writer, String name, String value, ElemDesc elemDesc) throws IOException {
        writer.write(32);
        if ((value.length() == 0 || value.equalsIgnoreCase(name)) && elemDesc != null && elemDesc.isAttrFlagSet(name, 4)) {
            writer.write(name);
            return;
        }
        writer.write(name);
        writer.write("=\"");
        if (elemDesc == null || !elemDesc.isAttrFlagSet(name, 2)) {
            writeAttrString(writer, value, getEncoding());
        } else {
            writeAttrURI(writer, value, this.m_specialEscapeURLs);
        }
        writer.write(34);
    }

    private boolean isASCIIDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static String makeHHString(int i) {
        String s = Integer.toHexString(i).toUpperCase();
        if (s.length() == 1) {
            return "0" + s;
        }
        return s;
    }

    private boolean isHHSign(String str) {
        try {
            char parseInt = (char) Integer.parseInt(str, 16);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void writeAttrURI(Writer writer, String string, boolean doURLEscaping) throws IOException {
        int end = string.length();
        if (end > this.m_attrBuff.length) {
            this.m_attrBuff = new char[((end * 2) + 1)];
        }
        string.getChars(0, end, this.m_attrBuff, 0);
        char[] chars = this.m_attrBuff;
        int cleanStart = 0;
        int cleanLength = 0;
        int ch = 0;
        int i = 0;
        while (i < end) {
            ch = chars[i];
            if (ch < 32 || ch > 126) {
                if (cleanLength > 0) {
                    writer.write(chars, cleanStart, cleanLength);
                    cleanLength = 0;
                }
                if (doURLEscaping) {
                    int high;
                    int low;
                    if (ch <= 127) {
                        writer.write(37);
                        writer.write(makeHHString(ch));
                    } else if (ch <= 2047) {
                        high = (ch >> 6) | 192;
                        low = (ch & 63) | 128;
                        writer.write(37);
                        writer.write(makeHHString(high));
                        writer.write(37);
                        writer.write(makeHHString(low));
                    } else if (Encodings.isHighUTF16Surrogate(ch)) {
                        int highSurrogate = ch & 1023;
                        int uuuuu = ((highSurrogate & 960) >> 6) + 1;
                        int zzzz = (highSurrogate & 60) >> 2;
                        int yyyyyy = ((highSurrogate & 3) << 4) & 48;
                        i++;
                        ch = chars[i];
                        int lowSurrogate = ch & 1023;
                        int byte1 = (uuuuu >> 2) | 240;
                        int byte2 = ((((uuuuu & 3) << 4) & 48) | 128) | zzzz;
                        int byte3 = (yyyyyy | ((lowSurrogate & 960) >> 6)) | 128;
                        int byte4 = (lowSurrogate & 63) | 128;
                        writer.write(37);
                        writer.write(makeHHString(byte1));
                        writer.write(37);
                        writer.write(makeHHString(byte2));
                        writer.write(37);
                        writer.write(makeHHString(byte3));
                        writer.write(37);
                        writer.write(makeHHString(byte4));
                    } else {
                        high = (ch >> 12) | 224;
                        int middle = ((ch & 4032) >> 6) | 128;
                        low = (ch & 63) | 128;
                        writer.write(37);
                        writer.write(makeHHString(high));
                        writer.write(37);
                        writer.write(makeHHString(middle));
                        writer.write(37);
                        writer.write(makeHHString(low));
                    }
                } else if (escapingNotNeeded(ch)) {
                    writer.write(ch);
                } else {
                    writer.write("&#");
                    writer.write(Integer.toString(ch));
                    writer.write(59);
                }
                cleanStart = i + 1;
            } else if (ch == 34) {
                if (cleanLength > 0) {
                    writer.write(chars, cleanStart, cleanLength);
                    cleanLength = 0;
                }
                if (doURLEscaping) {
                    writer.write("%22");
                } else {
                    writer.write(SerializerConstants.ENTITY_QUOT);
                }
                cleanStart = i + 1;
            } else if (ch == 38) {
                if (cleanLength > 0) {
                    writer.write(chars, cleanStart, cleanLength);
                    cleanLength = 0;
                }
                writer.write(SerializerConstants.ENTITY_AMP);
                cleanStart = i + 1;
            } else {
                cleanLength++;
            }
            i++;
        }
        if (cleanLength > 1) {
            if (cleanStart == 0) {
                writer.write(string);
            } else {
                writer.write(chars, cleanStart, cleanLength);
            }
        } else if (cleanLength == 1) {
            writer.write(ch);
        }
    }

    public void writeAttrString(Writer writer, String string, String encoding) throws IOException {
        int end = string.length();
        if (end > this.m_attrBuff.length) {
            this.m_attrBuff = new char[((end * 2) + 1)];
        }
        string.getChars(0, end, this.m_attrBuff, 0);
        char[] chars = this.m_attrBuff;
        int cleanStart = 0;
        int cleanLength = 0;
        int ch = 0;
        int i = 0;
        while (i < end) {
            ch = chars[i];
            if (escapingNotNeeded(ch) && (this.m_charInfo.shouldMapAttrChar(ch) ^ 1) != 0) {
                cleanLength++;
            } else if (60 == ch || 62 == ch) {
                cleanLength++;
            } else if (38 == ch && i + 1 < end && '{' == chars[i + 1]) {
                cleanLength++;
            } else {
                if (cleanLength > 0) {
                    writer.write(chars, cleanStart, cleanLength);
                    cleanLength = 0;
                }
                int pos = accumDefaultEntity(writer, ch, i, chars, end, false, true);
                if (i != pos) {
                    i = pos - 1;
                } else {
                    if (Encodings.isHighUTF16Surrogate(ch)) {
                        writeUTF16Surrogate(ch, chars, i, end);
                        i++;
                    }
                    String outputStringForChar = this.m_charInfo.getOutputStringForChar(ch);
                    if (outputStringForChar != null) {
                        writer.write(outputStringForChar);
                    } else if (escapingNotNeeded(ch)) {
                        writer.write(ch);
                    } else {
                        writer.write("&#");
                        writer.write(Integer.toString(ch));
                        writer.write(59);
                    }
                }
                cleanStart = i + 1;
            }
            i++;
        }
        if (cleanLength > 1) {
            if (cleanStart == 0) {
                writer.write(string);
            } else {
                writer.write(chars, cleanStart, cleanLength);
            }
        } else if (cleanLength == 1) {
            writer.write(ch);
        }
    }

    public final void characters(char[] chars, int start, int length) throws SAXException {
        if (this.m_elemContext.m_isRaw) {
            try {
                if (this.m_elemContext.m_startTagOpen) {
                    closeStartTag();
                    this.m_elemContext.m_startTagOpen = false;
                }
                this.m_ispreserve = true;
                writeNormalizedChars(chars, start, length, false, this.m_lineSepUse);
                if (this.m_tracer != null) {
                    super.fireCharEvent(chars, start, length);
                }
                return;
            } catch (IOException ioe) {
                throw new SAXException(Utils.messages.createMessage("ER_OIERROR", null), ioe);
            }
        }
        super.characters(chars, start, length);
    }

    public final void cdata(char[] ch, int start, int length) throws SAXException {
        if (this.m_elemContext.m_elementName == null || !(this.m_elemContext.m_elementName.equalsIgnoreCase("SCRIPT") || this.m_elemContext.m_elementName.equalsIgnoreCase("STYLE"))) {
            super.cdata(ch, start, length);
            return;
        }
        try {
            if (this.m_elemContext.m_startTagOpen) {
                closeStartTag();
                this.m_elemContext.m_startTagOpen = false;
            }
            this.m_ispreserve = true;
            if (shouldIndent()) {
                indent();
            }
            writeNormalizedChars(ch, start, length, true, this.m_lineSepUse);
        } catch (IOException ioe) {
            throw new SAXException(Utils.messages.createMessage("ER_OIERROR", null), ioe);
        }
    }

    public void processingInstruction(String target, String data) throws SAXException {
        flushPending();
        if (target.equals("javax.xml.transform.disable-output-escaping")) {
            startNonEscaping();
        } else if (target.equals("javax.xml.transform.enable-output-escaping")) {
            endNonEscaping();
        } else {
            try {
                if (this.m_elemContext.m_startTagOpen) {
                    closeStartTag();
                    this.m_elemContext.m_startTagOpen = false;
                } else if (this.m_cdataTagOpen) {
                    closeCDATA();
                } else if (this.m_needToCallStartDocument) {
                    startDocumentInternal();
                }
                if (this.m_needToOutputDocTypeDecl) {
                    outputDocTypeDecl("html");
                }
                if (shouldIndent()) {
                    indent();
                }
                Writer writer = this.m_writer;
                writer.write("<?");
                writer.write(target);
                if (data.length() > 0 && (Character.isSpaceChar(data.charAt(0)) ^ 1) != 0) {
                    writer.write(32);
                }
                writer.write(data);
                writer.write(62);
                if (this.m_elemContext.m_currentElemDepth <= 0) {
                    outputLineSep();
                }
                this.m_startNewLine = true;
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
        if (this.m_tracer != null) {
            super.fireEscapingEvent(target, data);
        }
    }

    public final void entityReference(String name) throws SAXException {
        try {
            Writer writer = this.m_writer;
            writer.write(38);
            writer.write(name);
            writer.write(59);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public final void endElement(String elemName) throws SAXException {
        endElement(null, null, elemName);
    }

    public void processAttributes(Writer writer, int nAttrs) throws IOException, SAXException {
        for (int i = 0; i < nAttrs; i++) {
            processAttribute(writer, this.m_attributes.getQName(i), this.m_attributes.getValue(i), this.m_elemContext.m_elementDesc);
        }
    }

    protected void closeStartTag() throws SAXException {
        try {
            if (this.m_tracer != null) {
                super.fireStartElem(this.m_elemContext.m_elementName);
            }
            int nAttrs = this.m_attributes.getLength();
            if (nAttrs > 0) {
                processAttributes(this.m_writer, nAttrs);
                this.m_attributes.clear();
            }
            this.m_writer.write(62);
            if (this.m_CdataElems != null) {
                this.m_elemContext.m_isCdataSection = isCdataSection();
            }
            if (this.m_doIndent) {
                this.m_isprevtext = false;
                this.m_preserves.push(this.m_ispreserve);
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void namespaceAfterStartElement(String prefix, String uri) throws SAXException {
        if (this.m_elemContext.m_elementURI == null && SerializerBase.getPrefixPart(this.m_elemContext.m_elementName) == null && "".equals(prefix)) {
            this.m_elemContext.m_elementURI = uri;
        }
        startPrefixMapping(prefix, uri, false);
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        this.m_inDTD = true;
        super.startDTD(name, publicId, systemId);
    }

    public void endDTD() throws SAXException {
        this.m_inDTD = false;
    }

    public void attributeDecl(String eName, String aName, String type, String valueDefault, String value) throws SAXException {
    }

    public void elementDecl(String name, String model) throws SAXException {
    }

    public void internalEntityDecl(String name, String value) throws SAXException {
    }

    public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
    }

    public void addUniqueAttribute(String name, String value, int flags) throws SAXException {
        try {
            Writer writer = this.m_writer;
            if ((flags & 1) > 0 && this.m_htmlcharInfo.onlyQuotAmpLtGt) {
                writer.write(32);
                writer.write(name);
                writer.write("=\"");
                writer.write(value);
                writer.write(34);
            } else if ((flags & 2) <= 0 || !(value.length() == 0 || value.equalsIgnoreCase(name))) {
                writer.write(32);
                writer.write(name);
                writer.write("=\"");
                if ((flags & 4) > 0) {
                    writeAttrURI(writer, value, this.m_specialEscapeURLs);
                } else {
                    writeAttrString(writer, value, getEncoding());
                }
                writer.write(34);
            } else {
                writer.write(32);
                writer.write(name);
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        if (!this.m_inDTD) {
            if (this.m_elemContext.m_startTagOpen) {
                closeStartTag();
                this.m_elemContext.m_startTagOpen = false;
            } else if (this.m_cdataTagOpen) {
                closeCDATA();
            } else if (this.m_needToCallStartDocument) {
                startDocumentInternal();
            }
            if (this.m_needToOutputDocTypeDecl) {
                outputDocTypeDecl("html");
            }
            super.comment(ch, start, length);
        }
    }

    public boolean reset() {
        if (!super.reset()) {
            return false;
        }
        resetToHTMLStream();
        return true;
    }

    private void resetToHTMLStream() {
        this.m_inBlockElem = false;
        this.m_inDTD = false;
        this.m_omitMetaTag = false;
        this.m_specialEscapeURLs = true;
    }
}
