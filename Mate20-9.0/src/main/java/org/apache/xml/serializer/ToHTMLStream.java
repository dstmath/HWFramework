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
    private Trie m_htmlInfo = new Trie(m_elementFlags);
    private final CharInfo m_htmlcharInfo = CharInfo.getCharInfo(CharInfo.HTML_ENTITIES_RESOURCE, "html");
    private boolean m_inBlockElem = false;
    protected boolean m_inDTD = false;
    private boolean m_omitMetaTag = false;
    private boolean m_specialEscapeURLs = true;

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
            int len = key.length();
            if (len > this.m_charBuffer.length) {
                this.m_charBuffer = new char[len];
            }
            Node node = this.m_Root;
            int i = 0;
            while (true) {
                if (i >= len) {
                    break;
                }
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
                }
            }
            Object ret = node.m_Value;
            node.m_Value = value;
            return ret;
        }

        public Object get(String key) {
            int len = key.length();
            if (this.m_charBuffer.length < len) {
                return null;
            }
            Node node = this.m_Root;
            switch (len) {
                case 0:
                    return null;
                case 1:
                    char ch = key.charAt(0);
                    if (ch < 128) {
                        Node node2 = node.m_nextChar[ch];
                        if (node2 != null) {
                            return node2.m_Value;
                        }
                    }
                    return null;
            }
            for (int i = 0; i < len; i++) {
                char ch2 = key.charAt(i);
                if (128 <= ch2) {
                    return null;
                }
                node = node.m_nextChar[ch2];
                if (node == null) {
                    return null;
                }
            }
            return node.m_Value;
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
            switch (len) {
                case 0:
                    return null;
                case 1:
                    char ch = key.charAt(0);
                    if (ch < 128) {
                        Node node2 = node.m_nextChar[ch];
                        if (node2 != null) {
                            return node2.m_Value;
                        }
                    }
                    return null;
                default:
                    key.getChars(0, len, this.m_charBuffer, 0);
                    for (int i = 0; i < len; i++) {
                        char ch2 = this.m_charBuffer[i];
                        if (128 <= ch2) {
                            return null;
                        }
                        node = node.m_nextChar[ch2];
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

    static void initTagReference(Trie m_elementFlags2) {
        m_elementFlags2.put("BASEFONT", new ElemDesc(2));
        m_elementFlags2.put("FRAME", new ElemDesc(10));
        m_elementFlags2.put("FRAMESET", new ElemDesc(8));
        m_elementFlags2.put("NOFRAMES", new ElemDesc(8));
        m_elementFlags2.put("ISINDEX", new ElemDesc(10));
        m_elementFlags2.put("APPLET", new ElemDesc(WalkerFactory.BIT_NAMESPACE));
        m_elementFlags2.put("CENTER", new ElemDesc(8));
        m_elementFlags2.put("DIR", new ElemDesc(8));
        m_elementFlags2.put("MENU", new ElemDesc(8));
        m_elementFlags2.put("TT", new ElemDesc(4096));
        m_elementFlags2.put("I", new ElemDesc(4096));
        m_elementFlags2.put("B", new ElemDesc(4096));
        m_elementFlags2.put("BIG", new ElemDesc(4096));
        m_elementFlags2.put("SMALL", new ElemDesc(4096));
        m_elementFlags2.put("EM", new ElemDesc(WalkerFactory.BIT_ANCESTOR));
        m_elementFlags2.put("STRONG", new ElemDesc(WalkerFactory.BIT_ANCESTOR));
        m_elementFlags2.put("DFN", new ElemDesc(WalkerFactory.BIT_ANCESTOR));
        m_elementFlags2.put("CODE", new ElemDesc(WalkerFactory.BIT_ANCESTOR));
        m_elementFlags2.put("SAMP", new ElemDesc(WalkerFactory.BIT_ANCESTOR));
        m_elementFlags2.put("KBD", new ElemDesc(WalkerFactory.BIT_ANCESTOR));
        m_elementFlags2.put("VAR", new ElemDesc(WalkerFactory.BIT_ANCESTOR));
        m_elementFlags2.put("CITE", new ElemDesc(WalkerFactory.BIT_ANCESTOR));
        m_elementFlags2.put("ABBR", new ElemDesc(WalkerFactory.BIT_ANCESTOR));
        m_elementFlags2.put("ACRONYM", new ElemDesc(WalkerFactory.BIT_ANCESTOR));
        m_elementFlags2.put("SUP", new ElemDesc(98304));
        m_elementFlags2.put("SUB", new ElemDesc(98304));
        m_elementFlags2.put("SPAN", new ElemDesc(98304));
        m_elementFlags2.put("BDO", new ElemDesc(98304));
        m_elementFlags2.put("BR", new ElemDesc(98314));
        m_elementFlags2.put("BODY", new ElemDesc(8));
        m_elementFlags2.put("ADDRESS", new ElemDesc(56));
        m_elementFlags2.put("DIV", new ElemDesc(56));
        m_elementFlags2.put("A", new ElemDesc(WalkerFactory.BIT_ATTRIBUTE));
        m_elementFlags2.put("MAP", new ElemDesc(98312));
        m_elementFlags2.put("AREA", new ElemDesc(10));
        m_elementFlags2.put("LINK", new ElemDesc(131082));
        m_elementFlags2.put("IMG", new ElemDesc(2195458));
        m_elementFlags2.put("OBJECT", new ElemDesc(2326528));
        m_elementFlags2.put("PARAM", new ElemDesc(2));
        m_elementFlags2.put("HR", new ElemDesc(58));
        m_elementFlags2.put("P", new ElemDesc(56));
        m_elementFlags2.put("H1", new ElemDesc(262152));
        m_elementFlags2.put("H2", new ElemDesc(262152));
        m_elementFlags2.put("H3", new ElemDesc(262152));
        m_elementFlags2.put("H4", new ElemDesc(262152));
        m_elementFlags2.put("H5", new ElemDesc(262152));
        m_elementFlags2.put("H6", new ElemDesc(262152));
        m_elementFlags2.put("PRE", new ElemDesc(1048584));
        m_elementFlags2.put("Q", new ElemDesc(98304));
        m_elementFlags2.put("BLOCKQUOTE", new ElemDesc(56));
        m_elementFlags2.put("INS", new ElemDesc(0));
        m_elementFlags2.put("DEL", new ElemDesc(0));
        m_elementFlags2.put("DL", new ElemDesc(56));
        m_elementFlags2.put("DT", new ElemDesc(8));
        m_elementFlags2.put("DD", new ElemDesc(8));
        m_elementFlags2.put("OL", new ElemDesc(524296));
        m_elementFlags2.put("UL", new ElemDesc(524296));
        m_elementFlags2.put("LI", new ElemDesc(8));
        m_elementFlags2.put("FORM", new ElemDesc(8));
        m_elementFlags2.put("LABEL", new ElemDesc(WalkerFactory.BIT_ANCESTOR_OR_SELF));
        m_elementFlags2.put("INPUT", new ElemDesc(18434));
        m_elementFlags2.put("SELECT", new ElemDesc(18432));
        m_elementFlags2.put("OPTGROUP", new ElemDesc(0));
        m_elementFlags2.put("OPTION", new ElemDesc(0));
        m_elementFlags2.put("TEXTAREA", new ElemDesc(18432));
        m_elementFlags2.put("FIELDSET", new ElemDesc(24));
        m_elementFlags2.put("LEGEND", new ElemDesc(0));
        m_elementFlags2.put("BUTTON", new ElemDesc(18432));
        m_elementFlags2.put("TABLE", new ElemDesc(56));
        m_elementFlags2.put("CAPTION", new ElemDesc(8));
        m_elementFlags2.put("THEAD", new ElemDesc(8));
        m_elementFlags2.put("TFOOT", new ElemDesc(8));
        m_elementFlags2.put("TBODY", new ElemDesc(8));
        m_elementFlags2.put("COLGROUP", new ElemDesc(8));
        m_elementFlags2.put("COL", new ElemDesc(10));
        m_elementFlags2.put("TR", new ElemDesc(8));
        m_elementFlags2.put("TH", new ElemDesc(0));
        m_elementFlags2.put("TD", new ElemDesc(0));
        m_elementFlags2.put("HEAD", new ElemDesc(4194312));
        m_elementFlags2.put("TITLE", new ElemDesc(8));
        m_elementFlags2.put("BASE", new ElemDesc(10));
        m_elementFlags2.put("META", new ElemDesc(131082));
        m_elementFlags2.put("STYLE", new ElemDesc(131336));
        m_elementFlags2.put("SCRIPT", new ElemDesc(229632));
        m_elementFlags2.put("NOSCRIPT", new ElemDesc(56));
        m_elementFlags2.put("HTML", new ElemDesc(8388616));
        m_elementFlags2.put("FONT", new ElemDesc(4096));
        m_elementFlags2.put("S", new ElemDesc(4096));
        m_elementFlags2.put("STRIKE", new ElemDesc(4096));
        m_elementFlags2.put("U", new ElemDesc(4096));
        m_elementFlags2.put("NOBR", new ElemDesc(4096));
        m_elementFlags2.put("IFRAME", new ElemDesc(56));
        m_elementFlags2.put("LAYER", new ElemDesc(56));
        m_elementFlags2.put("ILAYER", new ElemDesc(56));
        ElemDesc elemDesc = (ElemDesc) m_elementFlags2.get("a");
        elemDesc.setAttr("HREF", 2);
        elemDesc.setAttr("NAME", 2);
        ElemDesc elemDesc2 = (ElemDesc) m_elementFlags2.get("area");
        elemDesc2.setAttr("HREF", 2);
        elemDesc2.setAttr("NOHREF", 4);
        ((ElemDesc) m_elementFlags2.get("base")).setAttr("HREF", 2);
        ((ElemDesc) m_elementFlags2.get("button")).setAttr("DISABLED", 4);
        ((ElemDesc) m_elementFlags2.get("blockquote")).setAttr("CITE", 2);
        ((ElemDesc) m_elementFlags2.get("del")).setAttr("CITE", 2);
        ((ElemDesc) m_elementFlags2.get("dir")).setAttr("COMPACT", 4);
        ElemDesc elemDesc3 = (ElemDesc) m_elementFlags2.get("div");
        elemDesc3.setAttr("SRC", 2);
        elemDesc3.setAttr("NOWRAP", 4);
        ((ElemDesc) m_elementFlags2.get("dl")).setAttr("COMPACT", 4);
        ((ElemDesc) m_elementFlags2.get("form")).setAttr("ACTION", 2);
        ElemDesc elemDesc4 = (ElemDesc) m_elementFlags2.get("frame");
        elemDesc4.setAttr("SRC", 2);
        elemDesc4.setAttr("LONGDESC", 2);
        elemDesc4.setAttr("NORESIZE", 4);
        ((ElemDesc) m_elementFlags2.get("head")).setAttr("PROFILE", 2);
        ((ElemDesc) m_elementFlags2.get("hr")).setAttr("NOSHADE", 4);
        ElemDesc elemDesc5 = (ElemDesc) m_elementFlags2.get("iframe");
        elemDesc5.setAttr("SRC", 2);
        elemDesc5.setAttr("LONGDESC", 2);
        ((ElemDesc) m_elementFlags2.get("ilayer")).setAttr("SRC", 2);
        ElemDesc elemDesc6 = (ElemDesc) m_elementFlags2.get("img");
        elemDesc6.setAttr("SRC", 2);
        elemDesc6.setAttr("LONGDESC", 2);
        elemDesc6.setAttr("USEMAP", 2);
        elemDesc6.setAttr("ISMAP", 4);
        ElemDesc elemDesc7 = (ElemDesc) m_elementFlags2.get("input");
        elemDesc7.setAttr("SRC", 2);
        elemDesc7.setAttr("USEMAP", 2);
        elemDesc7.setAttr("CHECKED", 4);
        elemDesc7.setAttr("DISABLED", 4);
        elemDesc7.setAttr("ISMAP", 4);
        elemDesc7.setAttr("READONLY", 4);
        ((ElemDesc) m_elementFlags2.get("ins")).setAttr("CITE", 2);
        ((ElemDesc) m_elementFlags2.get("layer")).setAttr("SRC", 2);
        ((ElemDesc) m_elementFlags2.get("link")).setAttr("HREF", 2);
        ((ElemDesc) m_elementFlags2.get("menu")).setAttr("COMPACT", 4);
        ElemDesc elemDesc8 = (ElemDesc) m_elementFlags2.get("object");
        elemDesc8.setAttr("CLASSID", 2);
        elemDesc8.setAttr("CODEBASE", 2);
        elemDesc8.setAttr("DATA", 2);
        elemDesc8.setAttr("ARCHIVE", 2);
        elemDesc8.setAttr("USEMAP", 2);
        elemDesc8.setAttr("DECLARE", 4);
        ((ElemDesc) m_elementFlags2.get("ol")).setAttr("COMPACT", 4);
        ((ElemDesc) m_elementFlags2.get("optgroup")).setAttr("DISABLED", 4);
        ElemDesc elemDesc9 = (ElemDesc) m_elementFlags2.get("option");
        elemDesc9.setAttr("SELECTED", 4);
        elemDesc9.setAttr("DISABLED", 4);
        ((ElemDesc) m_elementFlags2.get("q")).setAttr("CITE", 2);
        ElemDesc elemDesc10 = (ElemDesc) m_elementFlags2.get(Constants.ELEMNAME_SCRIPT_STRING);
        elemDesc10.setAttr("SRC", 2);
        elemDesc10.setAttr("FOR", 2);
        elemDesc10.setAttr("DEFER", 4);
        ElemDesc elemDesc11 = (ElemDesc) m_elementFlags2.get(Constants.ATTRNAME_SELECT);
        elemDesc11.setAttr("DISABLED", 4);
        elemDesc11.setAttr("MULTIPLE", 4);
        ((ElemDesc) m_elementFlags2.get("table")).setAttr("NOWRAP", 4);
        ((ElemDesc) m_elementFlags2.get("td")).setAttr("NOWRAP", 4);
        ElemDesc elemDesc12 = (ElemDesc) m_elementFlags2.get("textarea");
        elemDesc12.setAttr("DISABLED", 4);
        elemDesc12.setAttr("READONLY", 4);
        ((ElemDesc) m_elementFlags2.get("th")).setAttr("NOWRAP", 4);
        ((ElemDesc) m_elementFlags2.get("tr")).setAttr("NOWRAP", 4);
        ((ElemDesc) m_elementFlags2.get("ul")).setAttr("COMPACT", 4);
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
        this.m_doIndent = true;
        this.m_charInfo = this.m_htmlcharInfo;
        this.m_prefixMap = new NamespaceMappings();
    }

    /* access modifiers changed from: protected */
    public void startDocumentInternal() throws SAXException {
        super.startDocumentInternal();
        this.m_needToCallStartDocument = false;
        this.m_needToOutputDocTypeDecl = true;
        this.m_startNewLine = false;
        setOmitXMLDeclaration(true);
    }

    private void outputDocTypeDecl(String name) throws SAXException {
        if (true == this.m_needToOutputDocTypeDecl) {
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
        if (this.m_doIndent && !this.m_isprevtext) {
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
            if (n == null || n.length() == 0) {
                n = localName;
            }
            outputDocTypeDecl(n);
        }
        if (namespaceURI == null || namespaceURI.length() <= 0) {
            try {
                ElemDesc elemDesc = getElemDesc2(name);
                int elemFlags = elemDesc.getFlags();
                boolean z = true;
                if (this.m_doIndent) {
                    boolean isBlockElement = (elemFlags & 8) != 0;
                    if (this.m_ispreserve) {
                        this.m_ispreserve = false;
                    } else if (elemContext.m_elementName != null && (!this.m_inBlockElem || isBlockElement)) {
                        this.m_startNewLine = true;
                        indent();
                    }
                    this.m_inBlockElem = !isBlockElement;
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
                ElemContext elemContext2 = elemContext.push(namespaceURI, localName, name);
                this.m_elemContext = elemContext2;
                elemContext2.m_elementDesc = elemDesc;
                if ((elemFlags & DTMFilter.SHOW_DOCUMENT) == 0) {
                    z = false;
                }
                elemContext2.m_isRaw = z;
                if ((4194304 & elemFlags) != 0) {
                    closeStartTag();
                    elemContext2.m_startTagOpen = false;
                    if (!this.m_omitMetaTag) {
                        if (this.m_doIndent) {
                            indent();
                        }
                        writer.write("<META http-equiv=\"Content-Type\" content=\"text/html; charset=");
                        writer.write(Encodings.getMimeEncoding(getEncoding()));
                        writer.write("\">");
                    }
                }
            } catch (IOException e) {
                throw new SAXException(e);
            }
        } else {
            super.startElement(namespaceURI, localName, name, atts);
        }
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
                    this.m_inBlockElem = !isBlockElement;
                }
                Writer writer = this.m_writer;
                if (!elemContext.m_startTagOpen) {
                    writer.write("</");
                    writer.write(name);
                    writer.write(62);
                } else {
                    if (this.m_tracer != null) {
                        super.fireStartElem(name);
                    }
                    int nAttrs = this.m_attributes.getLength();
                    if (nAttrs > 0) {
                        processAttributes(this.m_writer, nAttrs);
                        this.m_attributes.clear();
                    }
                    if (!elemEmpty) {
                        writer.write("></");
                        writer.write(name);
                        writer.write(62);
                    } else {
                        writer.write(62);
                    }
                }
                if ((2097152 & elemFlags) != 0) {
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
                if (!elemContext.m_startTagOpen && this.m_doIndent && !this.m_preserves.isEmpty()) {
                    this.m_preserves.pop();
                }
                this.m_elemContext = elemContext.m_prev;
            } catch (IOException e) {
                throw new SAXException(e);
            }
        } else {
            super.endElement(namespaceURI, localName, name);
        }
    }

    /* access modifiers changed from: protected */
    public void processAttribute(Writer writer, String name, String value, ElemDesc elemDesc) throws IOException {
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
        if (s.length() != 1) {
            return s;
        }
        return "0" + s;
    }

    private boolean isHHSign(String str) {
        try {
            Integer.parseInt(str, 16);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void writeAttrURI(Writer writer, String string, boolean doURLEscaping) throws IOException {
        int end;
        Writer writer2 = writer;
        int end2 = string.length();
        int i = 1;
        if (end2 > this.m_attrBuff.length) {
            this.m_attrBuff = new char[((end2 * 2) + 1)];
        }
        int i2 = 0;
        string.getChars(0, end2, this.m_attrBuff, 0);
        char[] chars = this.m_attrBuff;
        int cleanStart = 0;
        int cleanLength = 0;
        char ch = 0;
        while (i2 < end2) {
            ch = chars[i2];
            if (ch < ' ' || ch > '~') {
                if (cleanLength > 0) {
                    writer2.write(chars, cleanStart, cleanLength);
                    cleanLength = 0;
                }
                if (doURLEscaping) {
                    if (ch <= 127) {
                        writer2.write(37);
                        writer2.write(makeHHString(ch));
                    } else if (ch <= 2047) {
                        writer2.write(37);
                        writer2.write(makeHHString((ch >> 6) | 192));
                        writer2.write(37);
                        writer2.write(makeHHString(128 | (ch & 63)));
                    } else if (Encodings.isHighUTF16Surrogate(ch)) {
                        int highSurrogate = ch & 1023;
                        int uuuuu = ((highSurrogate & 960) >> 6) + 1;
                        int i3 = i2 + 1;
                        ch = chars[i3];
                        int i4 = ch & 1023;
                        end = end2;
                        int i5 = i4;
                        writer2.write(37);
                        writer2.write(makeHHString(240 | (uuuuu >> 2)));
                        writer2.write(37);
                        writer2.write(makeHHString(128 | (((uuuuu & 3) << 4) & 48) | ((highSurrogate & 60) >> 2)));
                        writer2.write(37);
                        writer2.write(makeHHString(128 | (((highSurrogate & 3) << 4) & 48) | ((i4 & 960) >> 6)));
                        writer2.write(37);
                        writer2.write(makeHHString(128 | (i4 & 63)));
                        i2 = i3;
                    } else {
                        end = end2;
                        writer2.write(37);
                        writer2.write(makeHHString((ch >> 12) | 224));
                        writer2.write(37);
                        writer2.write(makeHHString(((ch & 4032) >> 6) | 128));
                        writer2.write(37);
                        writer2.write(makeHHString(128 | (ch & 63)));
                    }
                    end = end2;
                } else {
                    end = end2;
                    if (escapingNotNeeded(ch)) {
                        writer2.write(ch);
                    } else {
                        writer2.write("&#");
                        writer2.write(Integer.toString(ch));
                        writer2.write(59);
                    }
                }
                cleanStart = i2 + 1;
            } else {
                if (ch == '\"') {
                    if (cleanLength > 0) {
                        writer2.write(chars, cleanStart, cleanLength);
                        cleanLength = 0;
                    }
                    if (doURLEscaping) {
                        writer2.write("%22");
                    } else {
                        writer2.write(SerializerConstants.ENTITY_QUOT);
                    }
                    cleanStart = i2 + 1;
                } else if (ch == '&') {
                    if (cleanLength > 0) {
                        writer2.write(chars, cleanStart, cleanLength);
                        cleanLength = 0;
                    }
                    writer2.write(SerializerConstants.ENTITY_AMP);
                    cleanStart = i2 + 1;
                } else {
                    cleanLength++;
                }
                end = end2;
            }
            i2++;
            i = 1;
            end2 = end;
        }
        int i6 = i;
        if (cleanLength > i6) {
            if (cleanStart == 0) {
                writer.write(string);
            } else {
                writer2.write(chars, cleanStart, cleanLength);
            }
        } else if (cleanLength == i6) {
            writer2.write(ch);
        }
    }

    public void writeAttrString(Writer writer, String string, String encoding) throws IOException {
        char ch;
        Writer writer2 = writer;
        int end = string.length();
        int ch2 = 1;
        if (end > this.m_attrBuff.length) {
            this.m_attrBuff = new char[((end * 2) + 1)];
        }
        int i = 0;
        string.getChars(0, end, this.m_attrBuff, 0);
        char[] chars = this.m_attrBuff;
        int cleanLength = 0;
        char ch3 = 0;
        int cleanStart = 0;
        while (true) {
            int i2 = i;
            if (i2 >= end) {
                break;
            }
            char ch4 = chars[i2];
            if (!escapingNotNeeded(ch4) || this.m_charInfo.shouldMapAttrChar(ch4)) {
                if ('<' == ch4) {
                    ch = ch4;
                } else if ('>' == ch4) {
                    ch = ch4;
                } else if ('&' == ch4 && i2 + 1 < end && '{' == chars[i2 + 1]) {
                    cleanLength++;
                } else {
                    if (cleanLength > 0) {
                        writer2.write(chars, cleanStart, cleanLength);
                        cleanLength = 0;
                    }
                    int cleanLength2 = cleanLength;
                    ch = ch4;
                    int pos = accumDefaultEntity(writer2, ch4, i2, chars, end, false, true);
                    if (i2 != pos) {
                        i2 = pos - 1;
                    } else {
                        if (Encodings.isHighUTF16Surrogate(ch) != 0) {
                            writeUTF16Surrogate(ch, chars, i2, end);
                            i2++;
                        }
                        String outputStringForChar = this.m_charInfo.getOutputStringForChar(ch);
                        if (outputStringForChar != null) {
                            writer2.write(outputStringForChar);
                        } else if (escapingNotNeeded(ch)) {
                            writer2.write(ch);
                        } else {
                            writer2.write("&#");
                            writer2.write(Integer.toString(ch));
                            writer2.write(59);
                        }
                    }
                    cleanStart = i2 + 1;
                    cleanLength = cleanLength2;
                    i = i2 + 1;
                    ch3 = ch;
                    ch2 = 1;
                }
                cleanLength++;
                i = i2 + 1;
                ch3 = ch;
                ch2 = 1;
            } else {
                cleanLength++;
            }
            ch = ch4;
            i = i2 + 1;
            ch3 = ch;
            ch2 = 1;
        }
        int i3 = ch2;
        if (cleanLength > i3) {
            if (cleanStart == 0) {
                writer.write(string);
            } else {
                writer2.write(chars, cleanStart, cleanLength);
            }
        } else if (cleanLength == i3) {
            writer2.write(ch3);
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
            } catch (IOException ioe) {
                throw new SAXException(Utils.messages.createMessage("ER_OIERROR", null), ioe);
            }
        } else {
            super.characters(chars, start, length);
        }
    }

    public final void cdata(char[] ch, int start, int length) throws SAXException {
        if (this.m_elemContext.m_elementName == null || (!this.m_elemContext.m_elementName.equalsIgnoreCase("SCRIPT") && !this.m_elemContext.m_elementName.equalsIgnoreCase("STYLE"))) {
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
                if (true == this.m_needToOutputDocTypeDecl) {
                    outputDocTypeDecl("html");
                }
                if (shouldIndent()) {
                    indent();
                }
                Writer writer = this.m_writer;
                writer.write("<?");
                writer.write(target);
                if (data.length() > 0 && !Character.isSpaceChar(data.charAt(0))) {
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

    /* access modifiers changed from: protected */
    public void closeStartTag() throws SAXException {
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
        if (this.m_elemContext.m_elementURI == null && getPrefixPart(this.m_elemContext.m_elementName) == null && "".equals(prefix)) {
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
            } else if ((flags & 2) <= 0 || (value.length() != 0 && !value.equalsIgnoreCase(name))) {
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
