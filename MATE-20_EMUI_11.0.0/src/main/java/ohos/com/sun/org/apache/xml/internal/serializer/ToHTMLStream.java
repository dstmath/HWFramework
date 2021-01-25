package ohos.com.sun.org.apache.xml.internal.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Properties;
import ohos.com.sun.org.apache.xml.internal.serializer.utils.Utils;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.SAXException;

public final class ToHTMLStream extends ToStream {
    private static final ElemDesc m_dummy = new ElemDesc(8);
    static final Trie m_elementFlags = new Trie();
    private static final CharInfo m_htmlcharInfo = CharInfo.getCharInfoInternal(CharInfo.HTML_ENTITIES_RESOURCE, "html");
    private Trie m_htmlInfo = new Trie(m_elementFlags);
    private boolean m_inBlockElem = false;
    protected boolean m_inDTD = false;
    private boolean m_omitMetaTag = false;
    private boolean m_specialEscapeURLs = true;

    private boolean isASCIIDigit(char c) {
        return c >= '0' && c <= '9';
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public void attributeDecl(String str, String str2, String str3, String str4, String str5) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public void elementDecl(String str, String str2) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public void externalEntityDecl(String str, String str2, String str3) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public void internalEntityDecl(String str, String str2) throws SAXException {
    }

    static {
        initTagReference(m_elementFlags);
    }

    static void initTagReference(Trie trie) {
        trie.put("BASEFONT", new ElemDesc(2));
        trie.put("FRAME", new ElemDesc(10));
        trie.put("FRAMESET", new ElemDesc(8));
        trie.put("NOFRAMES", new ElemDesc(8));
        trie.put("ISINDEX", new ElemDesc(10));
        trie.put("APPLET", new ElemDesc(2097152));
        trie.put("CENTER", new ElemDesc(8));
        trie.put("DIR", new ElemDesc(8));
        trie.put("MENU", new ElemDesc(8));
        trie.put("TT", new ElemDesc(4096));
        trie.put("I", new ElemDesc(4096));
        trie.put("B", new ElemDesc(4096));
        trie.put("BIG", new ElemDesc(4096));
        trie.put("SMALL", new ElemDesc(4096));
        trie.put("EM", new ElemDesc(8192));
        trie.put("STRONG", new ElemDesc(8192));
        trie.put("DFN", new ElemDesc(8192));
        trie.put("CODE", new ElemDesc(8192));
        trie.put("SAMP", new ElemDesc(8192));
        trie.put("KBD", new ElemDesc(8192));
        trie.put("VAR", new ElemDesc(8192));
        trie.put("CITE", new ElemDesc(8192));
        trie.put("ABBR", new ElemDesc(8192));
        trie.put("ACRONYM", new ElemDesc(8192));
        trie.put("SUP", new ElemDesc(98304));
        trie.put("SUB", new ElemDesc(98304));
        trie.put("SPAN", new ElemDesc(98304));
        trie.put("BDO", new ElemDesc(98304));
        trie.put("BR", new ElemDesc(98314));
        trie.put("BODY", new ElemDesc(8));
        trie.put("ADDRESS", new ElemDesc(56));
        trie.put("DIV", new ElemDesc(56));
        trie.put("A", new ElemDesc(32768));
        trie.put("MAP", new ElemDesc(98312));
        trie.put("AREA", new ElemDesc(10));
        trie.put("LINK", new ElemDesc(131082));
        trie.put("IMG", new ElemDesc(2195458));
        trie.put("OBJECT", new ElemDesc(2326528));
        trie.put("PARAM", new ElemDesc(2));
        trie.put("HR", new ElemDesc(58));
        trie.put("P", new ElemDesc(56));
        trie.put("H1", new ElemDesc(262152));
        trie.put("H2", new ElemDesc(262152));
        trie.put("H3", new ElemDesc(262152));
        trie.put("H4", new ElemDesc(262152));
        trie.put("H5", new ElemDesc(262152));
        trie.put("H6", new ElemDesc(262152));
        trie.put("PRE", new ElemDesc(1048584));
        trie.put("Q", new ElemDesc(98304));
        trie.put("BLOCKQUOTE", new ElemDesc(56));
        trie.put("INS", new ElemDesc(0));
        trie.put("DEL", new ElemDesc(0));
        trie.put("DL", new ElemDesc(56));
        trie.put("DT", new ElemDesc(8));
        trie.put("DD", new ElemDesc(8));
        trie.put("OL", new ElemDesc(524296));
        trie.put("UL", new ElemDesc(524296));
        trie.put("LI", new ElemDesc(8));
        trie.put("FORM", new ElemDesc(8));
        trie.put("LABEL", new ElemDesc(16384));
        trie.put("INPUT", new ElemDesc(18434));
        trie.put("SELECT", new ElemDesc(18432));
        trie.put("OPTGROUP", new ElemDesc(0));
        trie.put("OPTION", new ElemDesc(0));
        trie.put("TEXTAREA", new ElemDesc(18432));
        trie.put("FIELDSET", new ElemDesc(24));
        trie.put("LEGEND", new ElemDesc(0));
        trie.put("BUTTON", new ElemDesc(18432));
        trie.put("TABLE", new ElemDesc(56));
        trie.put("CAPTION", new ElemDesc(8));
        trie.put("THEAD", new ElemDesc(8));
        trie.put("TFOOT", new ElemDesc(8));
        trie.put("TBODY", new ElemDesc(8));
        trie.put("COLGROUP", new ElemDesc(8));
        trie.put("COL", new ElemDesc(10));
        trie.put("TR", new ElemDesc(8));
        trie.put("TH", new ElemDesc(0));
        trie.put("TD", new ElemDesc(0));
        trie.put("HEAD", new ElemDesc(4194312));
        trie.put("TITLE", new ElemDesc(8));
        trie.put("BASE", new ElemDesc(10));
        trie.put("META", new ElemDesc(131082));
        trie.put("STYLE", new ElemDesc(131336));
        trie.put("SCRIPT", new ElemDesc(229632));
        trie.put("NOSCRIPT", new ElemDesc(56));
        trie.put("HTML", new ElemDesc(8));
        trie.put("FONT", new ElemDesc(4096));
        trie.put("S", new ElemDesc(4096));
        trie.put("STRIKE", new ElemDesc(4096));
        trie.put("U", new ElemDesc(4096));
        trie.put("NOBR", new ElemDesc(4096));
        trie.put("IFRAME", new ElemDesc(56));
        trie.put("LAYER", new ElemDesc(56));
        trie.put("ILAYER", new ElemDesc(56));
        ElemDesc elemDesc = (ElemDesc) trie.get("A");
        elemDesc.setAttr("HREF", 2);
        elemDesc.setAttr("NAME", 2);
        ElemDesc elemDesc2 = (ElemDesc) trie.get("AREA");
        elemDesc2.setAttr("HREF", 2);
        elemDesc2.setAttr("NOHREF", 4);
        ((ElemDesc) trie.get("BASE")).setAttr("HREF", 2);
        ((ElemDesc) trie.get("BUTTON")).setAttr("DISABLED", 4);
        ((ElemDesc) trie.get("BLOCKQUOTE")).setAttr("CITE", 2);
        ((ElemDesc) trie.get("DEL")).setAttr("CITE", 2);
        ((ElemDesc) trie.get("DIR")).setAttr("COMPACT", 4);
        ElemDesc elemDesc3 = (ElemDesc) trie.get("DIV");
        elemDesc3.setAttr("SRC", 2);
        elemDesc3.setAttr("NOWRAP", 4);
        ((ElemDesc) trie.get("DL")).setAttr("COMPACT", 4);
        ((ElemDesc) trie.get("FORM")).setAttr("ACTION", 2);
        ElemDesc elemDesc4 = (ElemDesc) trie.get("FRAME");
        elemDesc4.setAttr("SRC", 2);
        elemDesc4.setAttr("LONGDESC", 2);
        elemDesc4.setAttr("NORESIZE", 4);
        ((ElemDesc) trie.get("HEAD")).setAttr("PROFILE", 2);
        ((ElemDesc) trie.get("HR")).setAttr("NOSHADE", 4);
        ElemDesc elemDesc5 = (ElemDesc) trie.get("IFRAME");
        elemDesc5.setAttr("SRC", 2);
        elemDesc5.setAttr("LONGDESC", 2);
        ((ElemDesc) trie.get("ILAYER")).setAttr("SRC", 2);
        ElemDesc elemDesc6 = (ElemDesc) trie.get("IMG");
        elemDesc6.setAttr("SRC", 2);
        elemDesc6.setAttr("LONGDESC", 2);
        elemDesc6.setAttr("USEMAP", 2);
        elemDesc6.setAttr("ISMAP", 4);
        ElemDesc elemDesc7 = (ElemDesc) trie.get("INPUT");
        elemDesc7.setAttr("SRC", 2);
        elemDesc7.setAttr("USEMAP", 2);
        elemDesc7.setAttr("CHECKED", 4);
        elemDesc7.setAttr("DISABLED", 4);
        elemDesc7.setAttr("ISMAP", 4);
        elemDesc7.setAttr("READONLY", 4);
        ((ElemDesc) trie.get("INS")).setAttr("CITE", 2);
        ((ElemDesc) trie.get("LAYER")).setAttr("SRC", 2);
        ((ElemDesc) trie.get("LINK")).setAttr("HREF", 2);
        ((ElemDesc) trie.get("MENU")).setAttr("COMPACT", 4);
        ElemDesc elemDesc8 = (ElemDesc) trie.get("OBJECT");
        elemDesc8.setAttr("CLASSID", 2);
        elemDesc8.setAttr("CODEBASE", 2);
        elemDesc8.setAttr("DATA", 2);
        elemDesc8.setAttr("ARCHIVE", 2);
        elemDesc8.setAttr("USEMAP", 2);
        elemDesc8.setAttr("DECLARE", 4);
        ((ElemDesc) trie.get("OL")).setAttr("COMPACT", 4);
        ((ElemDesc) trie.get("OPTGROUP")).setAttr("DISABLED", 4);
        ElemDesc elemDesc9 = (ElemDesc) trie.get("OPTION");
        elemDesc9.setAttr("SELECTED", 4);
        elemDesc9.setAttr("DISABLED", 4);
        ((ElemDesc) trie.get("Q")).setAttr("CITE", 2);
        ElemDesc elemDesc10 = (ElemDesc) trie.get("SCRIPT");
        elemDesc10.setAttr("SRC", 2);
        elemDesc10.setAttr("FOR", 2);
        elemDesc10.setAttr("DEFER", 4);
        ElemDesc elemDesc11 = (ElemDesc) trie.get("SELECT");
        elemDesc11.setAttr("DISABLED", 4);
        elemDesc11.setAttr("MULTIPLE", 4);
        ((ElemDesc) trie.get("TABLE")).setAttr("NOWRAP", 4);
        ((ElemDesc) trie.get("TD")).setAttr("NOWRAP", 4);
        ElemDesc elemDesc12 = (ElemDesc) trie.get("TEXTAREA");
        elemDesc12.setAttr("DISABLED", 4);
        elemDesc12.setAttr("READONLY", 4);
        ((ElemDesc) trie.get("TH")).setAttr("NOWRAP", 4);
        ((ElemDesc) trie.get("TR")).setAttr("NOWRAP", 4);
        ((ElemDesc) trie.get("UL")).setAttr("COMPACT", 4);
    }

    public void setSpecialEscapeURLs(boolean z) {
        this.m_specialEscapeURLs = z;
    }

    public void setOmitMetaTag(boolean z) {
        this.m_omitMetaTag = z;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream, ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public void setOutputFormat(Properties properties) {
        this.m_specialEscapeURLs = OutputPropertyUtils.getBooleanProperty(OutputPropertiesFactory.S_USE_URL_ESCAPING, properties);
        this.m_omitMetaTag = OutputPropertyUtils.getBooleanProperty(OutputPropertiesFactory.S_OMIT_META_TAG, properties);
        super.setOutputFormat(properties);
    }

    private final boolean getSpecialEscapeURLs() {
        return this.m_specialEscapeURLs;
    }

    private final boolean getOmitMetaTag() {
        return this.m_omitMetaTag;
    }

    public static final ElemDesc getElemDesc(String str) {
        Object obj = m_elementFlags.get(str);
        if (obj != null) {
            return (ElemDesc) obj;
        }
        return m_dummy;
    }

    private ElemDesc getElemDesc2(String str) {
        Object r0 = this.m_htmlInfo.get2(str);
        if (r0 != null) {
            return (ElemDesc) r0;
        }
        return m_dummy;
    }

    public ToHTMLStream() {
        this.m_charInfo = m_htmlcharInfo;
        this.m_prefixMap = new NamespaceMappings();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase
    public void startDocumentInternal() throws SAXException {
        super.startDocumentInternal();
        this.m_needToCallStartDocument = false;
        this.m_needToOutputDocTypeDecl = true;
        this.m_startNewLine = false;
        setOmitXMLDeclaration(true);
        if (true == this.m_needToOutputDocTypeDecl) {
            String doctypeSystem = getDoctypeSystem();
            String doctypePublic = getDoctypePublic();
            if (!(doctypeSystem == null && doctypePublic == null)) {
                Writer writer = this.m_writer;
                try {
                    writer.write("<!DOCTYPE html");
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

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
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
        if (str == null || str.length() <= 0) {
            try {
                ElemDesc elemDesc2 = getElemDesc2(str3);
                int flags = elemDesc2.getFlags();
                boolean z = true;
                if (this.m_doIndent) {
                    boolean z2 = (flags & 8) != 0;
                    if (this.m_ispreserve) {
                        this.m_ispreserve = false;
                    } else if (elemContext.m_elementName != null && (!this.m_inBlockElem || z2)) {
                        this.m_startNewLine = true;
                        indent();
                    }
                    this.m_inBlockElem = !z2;
                }
                if (attributes != null) {
                    addAttributes(attributes);
                }
                this.m_isprevtext = false;
                Writer writer = this.m_writer;
                writer.write(60);
                writer.write(str3);
                if (this.m_tracer != null) {
                    firePseudoAttributes();
                }
                if ((flags & 2) != 0) {
                    this.m_elemContext = elemContext.push();
                    this.m_elemContext.m_elementName = str3;
                    this.m_elemContext.m_elementDesc = elemDesc2;
                    return;
                }
                ElemContext push = elemContext.push(str, str2, str3);
                this.m_elemContext = push;
                push.m_elementDesc = elemDesc2;
                if ((flags & 256) == 0) {
                    z = false;
                }
                push.m_isRaw = z;
                if ((4194304 & flags) != 0) {
                    closeStartTag();
                    push.m_startTagOpen = false;
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
            super.startElement(str, str2, str3, attributes);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x0053 A[Catch:{ IOException -> 0x00c7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0055 A[Catch:{ IOException -> 0x00c7 }] */
    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public final void endElement(String str, String str2, String str3) throws SAXException {
        boolean z;
        if (this.m_cdataTagOpen) {
            closeCDATA();
        }
        if (str == null || str.length() <= 0) {
            try {
                ElemContext elemContext = this.m_elemContext;
                int flags = elemContext.m_elementDesc.getFlags();
                boolean z2 = (flags & 2) != 0;
                if (this.m_doIndent) {
                    boolean z3 = (flags & 8) != 0;
                    if (this.m_ispreserve) {
                        this.m_ispreserve = false;
                    } else if (this.m_doIndent && (!this.m_inBlockElem || z3)) {
                        this.m_startNewLine = true;
                        z = true;
                        if (!elemContext.m_startTagOpen && z) {
                            indent(elemContext.m_currentElemDepth - 1);
                        }
                        this.m_inBlockElem = z3;
                    }
                    z = false;
                    indent(elemContext.m_currentElemDepth - 1);
                    this.m_inBlockElem = z3;
                }
                Writer writer = this.m_writer;
                if (!elemContext.m_startTagOpen) {
                    writer.write("</");
                    writer.write(str3);
                    writer.write(62);
                } else {
                    if (this.m_tracer != null) {
                        super.fireStartElem(str3);
                    }
                    int length = this.m_attributes.getLength();
                    if (length > 0) {
                        processAttributes(this.m_writer, length);
                        this.m_attributes.clear();
                    }
                    if (!z2) {
                        writer.write("></");
                        writer.write(str3);
                        writer.write(62);
                    } else {
                        writer.write(62);
                    }
                }
                if ((flags & 2097152) != 0) {
                    this.m_ispreserve = true;
                }
                this.m_isprevtext = false;
                if (this.m_tracer != null) {
                    super.fireEndElem(str3);
                }
                if (z2) {
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
            super.endElement(str, str2, str3);
        }
    }

    /* access modifiers changed from: protected */
    public void processAttribute(Writer writer, String str, String str2, ElemDesc elemDesc) throws IOException, SAXException {
        writer.write(32);
        if ((str2.length() == 0 || str2.equalsIgnoreCase(str)) && elemDesc != null && elemDesc.isAttrFlagSet(str, 4)) {
            writer.write(str);
            return;
        }
        writer.write(str);
        writer.write("=\"");
        if (elemDesc == null || !elemDesc.isAttrFlagSet(str, 2)) {
            writeAttrString(writer, str2, getEncoding());
        } else {
            writeAttrURI(writer, str2, this.m_specialEscapeURLs);
        }
        writer.write(34);
    }

    private static String makeHHString(int i) {
        String upperCase = Integer.toHexString(i).toUpperCase();
        if (upperCase.length() != 1) {
            return upperCase;
        }
        return "0" + upperCase;
    }

    private boolean isHHSign(String str) {
        try {
            Integer.parseInt(str, 16);
            return true;
        } catch (NumberFormatException unused) {
            return false;
        }
    }

    public void writeAttrURI(Writer writer, String str, boolean z) throws IOException {
        char c;
        int length = str.length();
        if (length > this.m_attrBuff.length) {
            this.m_attrBuff = new char[((length * 2) + 1)];
        }
        str.getChars(0, length, this.m_attrBuff, 0);
        char[] cArr = this.m_attrBuff;
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        char c2 = 0;
        while (i < length) {
            c2 = cArr[i];
            if (c2 < ' ' || c2 > '~') {
                if (i2 > 0) {
                    writer.write(cArr, i3, i2);
                    i2 = 0;
                }
                if (z) {
                    if (c2 <= 127) {
                        writer.write(37);
                        writer.write(makeHHString(c2));
                    } else if (c2 <= 2047) {
                        writer.write(37);
                        writer.write(makeHHString((c2 >> 6) | 192));
                        writer.write(37);
                        writer.write(makeHHString((c2 & '?') | 128));
                    } else if (Encodings.isHighUTF16Surrogate(c2)) {
                        int i4 = c2 & 1023;
                        int i5 = ((i4 & 960) >> 6) + 1;
                        i++;
                        c = cArr[i];
                        int i6 = c & 1023;
                        writer.write(37);
                        writer.write(makeHHString((i5 >> 2) | 240));
                        writer.write(37);
                        writer.write(makeHHString((((i5 & 3) << 4) & 48) | 128 | ((i4 & 60) >> 2)));
                        writer.write(37);
                        writer.write(makeHHString((((i4 & 3) << 4) & 48) | ((i6 & 960) >> 6) | 128));
                        writer.write(37);
                        writer.write(makeHHString((i6 & 63) | 128));
                        i3 = i + 1;
                        c2 = c;
                    } else {
                        writer.write(37);
                        writer.write(makeHHString((c2 >> '\f') | 224));
                        writer.write(37);
                        writer.write(makeHHString(((c2 & 4032) >> 6) | 128));
                        writer.write(37);
                        writer.write(makeHHString((c2 & '?') | 128));
                    }
                } else if (escapingNotNeeded(c2)) {
                    writer.write(c2);
                } else {
                    writer.write("&#");
                    writer.write(Integer.toString(c2));
                    writer.write(59);
                }
                c = c2;
                i3 = i + 1;
                c2 = c;
            } else {
                if (c2 == '\"') {
                    if (i2 > 0) {
                        writer.write(cArr, i3, i2);
                        i2 = 0;
                    }
                    if (z) {
                        writer.write("%22");
                    } else {
                        writer.write(SerializerConstants.ENTITY_QUOT);
                    }
                } else if (c2 == '&') {
                    if (i2 > 0) {
                        writer.write(cArr, i3, i2);
                        i2 = 0;
                    }
                    writer.write(SerializerConstants.ENTITY_AMP);
                } else {
                    i2++;
                }
                i3 = i + 1;
            }
            i++;
        }
        if (i2 > 1) {
            if (i3 == 0) {
                writer.write(str);
            } else {
                writer.write(cArr, i3, i2);
            }
        } else if (i2 == 1) {
            writer.write(c2);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public void writeAttrString(Writer writer, String str, String str2) throws IOException, SAXException {
        int i;
        int i2;
        int length = str.length();
        if (length > this.m_attrBuff.length) {
            this.m_attrBuff = new char[((length * 2) + 1)];
        }
        str.getChars(0, length, this.m_attrBuff, 0);
        char[] cArr = this.m_attrBuff;
        int i3 = 0;
        int i4 = 0;
        char c = 0;
        int i5 = 0;
        while (i5 < length) {
            char c2 = cArr[i5];
            if (!escapingNotNeeded(c2) || this.m_charInfo.isSpecialAttrChar(c2)) {
                if ('<' == c2 || '>' == c2) {
                    c = c2;
                    i3++;
                    i5++;
                } else if (!('&' == c2 && (i2 = i5 + 1) < length && '{' == cArr[i2])) {
                    if (i3 > 0) {
                        writer.write(cArr, i4, i3);
                        i = 0;
                    } else {
                        i = i3;
                    }
                    int accumDefaultEntity = accumDefaultEntity(writer, c2, i5, cArr, length, false, true);
                    if (i5 != accumDefaultEntity) {
                        c = c2;
                        i5 = accumDefaultEntity - 1;
                    } else {
                        if (Encodings.isHighUTF16Surrogate(c2) || Encodings.isLowUTF16Surrogate(c2)) {
                            c = c2;
                            if (writeUTF16Surrogate(c, cArr, i5, length) >= 0 && Encodings.isHighUTF16Surrogate(c)) {
                                i5++;
                            }
                        } else {
                            c = c2;
                        }
                        String outputStringForChar = this.m_charInfo.getOutputStringForChar(c);
                        if (outputStringForChar != null) {
                            writer.write(outputStringForChar);
                        } else if (escapingNotNeeded(c)) {
                            writer.write(c);
                        } else {
                            writer.write("&#");
                            writer.write(Integer.toString(c));
                            writer.write(59);
                        }
                    }
                    i4 = i5 + 1;
                    i3 = i;
                    i5++;
                }
            }
            i3++;
            c = c2;
            i5++;
        }
        if (i3 > 1) {
            if (i4 == 0) {
                writer.write(str);
            } else {
                writer.write(cArr, i4, i3);
            }
        } else if (i3 == 1) {
            writer.write(c);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public final void characters(char[] cArr, int i, int i2) throws SAXException {
        if (this.m_elemContext.m_isRaw) {
            try {
                if (this.m_elemContext.m_startTagOpen) {
                    closeStartTag();
                    this.m_elemContext.m_startTagOpen = false;
                }
                this.m_ispreserve = true;
                writeNormalizedChars(cArr, i, i2, false, this.m_lineSepUse);
                if (this.m_tracer != null) {
                    super.fireCharEvent(cArr, i, i2);
                }
            } catch (IOException e) {
                throw new SAXException(Utils.messages.createMessage("ER_OIERROR", null), e);
            }
        } else {
            super.characters(cArr, i, i2);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public final void cdata(char[] cArr, int i, int i2) throws SAXException {
        if (this.m_elemContext.m_elementName == null || (!this.m_elemContext.m_elementName.equalsIgnoreCase("SCRIPT") && !this.m_elemContext.m_elementName.equalsIgnoreCase("STYLE"))) {
            super.cdata(cArr, i, i2);
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
            writeNormalizedChars(cArr, i, i2, true, this.m_lineSepUse);
        } catch (IOException e) {
            throw new SAXException(Utils.messages.createMessage("ER_OIERROR", null), e);
        }
    }

    public void processingInstruction(String str, String str2) throws SAXException {
        flushPending();
        if (str.equals("javax.xml.transform.disable-output-escaping")) {
            startNonEscaping();
        } else if (str.equals("javax.xml.transform.enable-output-escaping")) {
            endNonEscaping();
        } else {
            try {
                if (this.m_elemContext.m_startTagOpen) {
                    closeStartTag();
                    this.m_elemContext.m_startTagOpen = false;
                } else if (this.m_needToCallStartDocument) {
                    startDocumentInternal();
                }
                if (shouldIndent()) {
                    indent();
                }
                Writer writer = this.m_writer;
                writer.write("<?");
                writer.write(str);
                if (str2.length() > 0 && !Character.isSpaceChar(str2.charAt(0))) {
                    writer.write(32);
                }
                writer.write(str2);
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
            super.fireEscapingEvent(str, str2);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public final void entityReference(String str) throws SAXException {
        try {
            Writer writer = this.m_writer;
            writer.write(38);
            writer.write(str);
            writer.write(59);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public final void endElement(String str) throws SAXException {
        endElement(null, null, str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public void processAttributes(Writer writer, int i) throws IOException, SAXException {
        for (int i2 = 0; i2 < i; i2++) {
            processAttribute(writer, this.m_attributes.getQName(i2), this.m_attributes.getValue(i2), this.m_elemContext.m_elementDesc);
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public void closeStartTag() throws SAXException {
        try {
            if (this.m_tracer != null) {
                super.fireStartElem(this.m_elemContext.m_elementName);
            }
            int length = this.m_attributes.getLength();
            if (length > 0) {
                processAttributes(this.m_writer, length);
                this.m_attributes.clear();
            }
            this.m_writer.write(62);
            if (this.m_cdataSectionElements != null) {
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

    /* access modifiers changed from: protected */
    public synchronized void init(OutputStream outputStream, Properties properties) throws UnsupportedEncodingException {
        if (properties == null) {
            properties = OutputPropertiesFactory.getDefaultMethodProperties("html");
        }
        super.init(outputStream, properties, false);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream, ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public void setOutputStream(OutputStream outputStream) {
        Properties properties;
        try {
            if (this.m_format == null) {
                properties = OutputPropertiesFactory.getDefaultMethodProperties("html");
            } else {
                properties = this.m_format;
            }
            init(outputStream, properties, true);
        } catch (UnsupportedEncodingException unused) {
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void namespaceAfterStartElement(String str, String str2) throws SAXException {
        if (this.m_elemContext.m_elementURI == null && getPrefixPart(this.m_elemContext.m_elementName) == null && "".equals(str)) {
            this.m_elemContext.m_elementURI = str2;
        }
        startPrefixMapping(str, str2, false);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public void startDTD(String str, String str2, String str3) throws SAXException {
        this.m_inDTD = true;
        super.startDTD(str, str2, str3);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public void endDTD() throws SAXException {
        this.m_inDTD = false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void addUniqueAttribute(String str, String str2, int i) throws SAXException {
        try {
            Writer writer = this.m_writer;
            if ((i & 1) > 0 && m_htmlcharInfo.onlyQuotAmpLtGt) {
                writer.write(32);
                writer.write(str);
                writer.write("=\"");
                writer.write(str2);
                writer.write(34);
            } else if ((i & 2) <= 0 || (str2.length() != 0 && !str2.equalsIgnoreCase(str))) {
                writer.write(32);
                writer.write(str);
                writer.write("=\"");
                if ((i & 4) > 0) {
                    writeAttrURI(writer, str2, this.m_specialEscapeURLs);
                } else {
                    writeAttrString(writer, str2, getEncoding());
                }
                writer.write(34);
            } else {
                writer.write(32);
                writer.write(str);
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream
    public void comment(char[] cArr, int i, int i2) throws SAXException {
        if (!this.m_inDTD) {
            super.comment(cArr, i, i2);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ToStream, ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public boolean reset() {
        if (!super.reset()) {
            return false;
        }
        initToHTMLStream();
        return true;
    }

    private void initToHTMLStream() {
        this.m_inBlockElem = false;
        this.m_inDTD = false;
        this.m_omitMetaTag = false;
        this.m_specialEscapeURLs = true;
    }

    /* access modifiers changed from: package-private */
    public static class Trie {
        public static final int ALPHA_SIZE = 128;
        final Node m_Root;
        private char[] m_charBuffer;
        private final boolean m_lowerCaseOnly;

        public Trie() {
            this.m_charBuffer = new char[0];
            this.m_Root = new Node();
            this.m_lowerCaseOnly = false;
        }

        public Trie(boolean z) {
            this.m_charBuffer = new char[0];
            this.m_Root = new Node();
            this.m_lowerCaseOnly = z;
        }

        public Object put(String str, Object obj) {
            int length = str.length();
            if (length > this.m_charBuffer.length) {
                this.m_charBuffer = new char[length];
            }
            Node node = this.m_Root;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                Node node2 = node.m_nextChar[Character.toLowerCase(str.charAt(i))];
                if (node2 != null) {
                    i++;
                    node = node2;
                } else {
                    while (i < length) {
                        Node node3 = new Node();
                        if (this.m_lowerCaseOnly) {
                            node.m_nextChar[Character.toLowerCase(str.charAt(i))] = node3;
                        } else {
                            node.m_nextChar[Character.toUpperCase(str.charAt(i))] = node3;
                            node.m_nextChar[Character.toLowerCase(str.charAt(i))] = node3;
                        }
                        i++;
                        node = node3;
                    }
                }
            }
            Object obj2 = node.m_Value;
            node.m_Value = obj;
            return obj2;
        }

        public Object get(String str) {
            Node node;
            int length = str.length();
            if (this.m_charBuffer.length < length) {
                return null;
            }
            Node node2 = this.m_Root;
            if (length != 0) {
                if (length != 1) {
                    for (int i = 0; i < length; i++) {
                        char charAt = str.charAt(i);
                        if (128 <= charAt || (node2 = node2.m_nextChar[charAt]) == null) {
                            return null;
                        }
                    }
                    return node2.m_Value;
                }
                char charAt2 = str.charAt(0);
                if (charAt2 < 128 && (node = node2.m_nextChar[charAt2]) != null) {
                    return node.m_Value;
                }
            }
            return null;
        }

        /* access modifiers changed from: private */
        public class Node {
            Object m_Value = null;
            final Node[] m_nextChar = new Node[128];

            Node() {
            }
        }

        public Trie(Trie trie) {
            this.m_charBuffer = new char[0];
            this.m_Root = trie.m_Root;
            this.m_lowerCaseOnly = trie.m_lowerCaseOnly;
            this.m_charBuffer = new char[trie.getLongestKeyLength()];
        }

        public Object get2(String str) {
            Node node;
            int length = str.length();
            char[] cArr = this.m_charBuffer;
            if (cArr.length < length) {
                return null;
            }
            Node node2 = this.m_Root;
            if (length != 0) {
                if (length != 1) {
                    str.getChars(0, length, cArr, 0);
                    for (int i = 0; i < length; i++) {
                        char c = this.m_charBuffer[i];
                        if (128 <= c || (node2 = node2.m_nextChar[c]) == null) {
                            return null;
                        }
                    }
                    return node2.m_Value;
                }
                char charAt = str.charAt(0);
                if (charAt < 128 && (node = node2.m_nextChar[charAt]) != null) {
                    return node.m_Value;
                }
            }
            return null;
        }

        public int getLongestKeyLength() {
            return this.m_charBuffer.length;
        }
    }
}
