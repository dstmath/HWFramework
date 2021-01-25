package ohos.com.sun.org.apache.xml.internal.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xml.internal.serializer.NamespaceMappings;
import ohos.com.sun.org.apache.xml.internal.serializer.utils.Messages;
import ohos.com.sun.org.apache.xml.internal.serializer.utils.Utils;
import ohos.com.sun.org.apache.xml.internal.serializer.utils.WrappedRuntimeException;
import ohos.javax.xml.transform.ErrorListener;
import ohos.javax.xml.transform.Transformer;
import ohos.javax.xml.transform.TransformerException;
import ohos.org.w3c.dom.Node;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.SAXException;

public abstract class ToStream extends SerializerBase {
    private static final String COMMENT_BEGIN = "<!--";
    private static final String COMMENT_END = "-->";
    Method m_canConvertMeth;
    protected boolean m_cdataStartCalled = false;
    protected CharInfo m_charInfo;
    Object m_charToByteConverter = null;
    protected BoolStack m_disableOutputEscapingStates = new BoolStack();
    EncodingInfo m_encodingInfo = new EncodingInfo(null, null);
    private boolean m_escaping = true;
    private boolean m_expandDTDEntities = true;
    protected Properties m_format;
    private char m_highSurrogate = 0;
    protected boolean m_inDoctype = false;
    boolean m_isUTF8 = false;
    protected boolean m_ispreserve = false;
    protected boolean m_isprevtext = false;
    protected char[] m_lineSep = SecuritySupport.getSystemProperty("line.separator").toCharArray();
    protected int m_lineSepLen = this.m_lineSep.length;
    protected boolean m_lineSepUse = true;
    protected int m_maxCharacter = Encodings.getLastPrintable();
    protected BoolStack m_preserves = new BoolStack();
    boolean m_shouldFlush = true;
    protected boolean m_spaceBeforeClose = false;
    boolean m_startNewLine;
    boolean m_triedToGetConverter = false;

    private static boolean isCharacterInC0orC1Range(char c) {
        if (c == '\t' || c == '\n' || c == '\r') {
            return false;
        }
        return (c >= 127 && c <= 159) || (c >= 1 && c <= 31);
    }

    private static boolean isNELorLSEPCharacter(char c) {
        return c == 133 || c == 8232;
    }

    static final boolean isUTF16Surrogate(char c) {
        return (c & 64512) == 55296;
    }

    public void endPrefixMapping(String str) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public void setContentHandler(ContentHandler contentHandler) {
    }

    public void skippedEntity(String str) throws SAXException {
    }

    /* access modifiers changed from: protected */
    public void closeCDATA() throws SAXException {
        try {
            this.m_writer.write("]]>");
            this.m_cdataTagOpen = false;
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler, ohos.com.sun.org.apache.xml.internal.serializer.DOMSerializer
    public void serialize(Node node) throws IOException {
        try {
            new TreeWalker(this).traverse(node);
        } catch (SAXException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /* access modifiers changed from: protected */
    public final void flushWriter() throws SAXException {
        Writer writer = this.m_writer;
        if (writer != null) {
            try {
                if (writer instanceof WriterToUTF8Buffered) {
                    if (this.m_shouldFlush) {
                        ((WriterToUTF8Buffered) writer).flush();
                    } else {
                        ((WriterToUTF8Buffered) writer).flushBuffer();
                    }
                }
                if (!(writer instanceof WriterToASCI)) {
                    writer.flush();
                } else if (this.m_shouldFlush) {
                    writer.flush();
                }
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public OutputStream getOutputStream() {
        if (this.m_writer instanceof WriterToUTF8Buffered) {
            return ((WriterToUTF8Buffered) this.m_writer).getOutputStream();
        }
        if (this.m_writer instanceof WriterToASCI) {
            return ((WriterToASCI) this.m_writer).getOutputStream();
        }
        return null;
    }

    public void elementDecl(String str, String str2) throws SAXException {
        if (!this.m_inExternalDTD) {
            try {
                Writer writer = this.m_writer;
                DTDprolog();
                writer.write("<!ELEMENT ");
                writer.write(str);
                writer.write(32);
                writer.write(str2);
                writer.write(62);
                writer.write(this.m_lineSep, 0, this.m_lineSepLen);
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    public void internalEntityDecl(String str, String str2) throws SAXException {
        if (!this.m_inExternalDTD) {
            try {
                DTDprolog();
                outputEntityDecl(str, str2);
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void outputEntityDecl(String str, String str2) throws IOException {
        Writer writer = this.m_writer;
        writer.write("<!ENTITY ");
        writer.write(str);
        writer.write(" \"");
        writer.write(str2);
        writer.write("\">");
        writer.write(this.m_lineSep, 0, this.m_lineSepLen);
    }

    /* access modifiers changed from: protected */
    public final void outputLineSep() throws IOException {
        this.m_writer.write(this.m_lineSep, 0, this.m_lineSepLen);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public void setOutputFormat(Properties properties) {
        boolean z = this.m_shouldFlush;
        init(this.m_writer, properties, false, false);
        this.m_shouldFlush = z;
    }

    private synchronized void init(Writer writer, Properties properties, boolean z, boolean z2) {
        this.m_shouldFlush = z2;
        if (this.m_tracer == null || (writer instanceof SerializerTraceWriter)) {
            this.m_writer = writer;
        } else {
            this.m_writer = new SerializerTraceWriter(writer, this.m_tracer);
        }
        this.m_format = properties;
        setCdataSectionElements(Constants.ATTRNAME_OUTPUT_CDATA_SECTION_ELEMENTS, properties);
        setIndentAmount(OutputPropertyUtils.getIntProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, properties));
        setIndent(OutputPropertyUtils.getBooleanProperty(Constants.ATTRNAME_OUTPUT_INDENT, properties));
        String property = properties.getProperty(OutputPropertiesFactory.S_KEY_LINE_SEPARATOR);
        if (property != null) {
            this.m_lineSep = property.toCharArray();
            this.m_lineSepLen = property.length();
        }
        setOmitXMLDeclaration(OutputPropertyUtils.getBooleanProperty(Constants.ATTRNAME_OUTPUT_OMITXMLDECL, properties));
        setDoctypeSystem(properties.getProperty(Constants.ATTRNAME_OUTPUT_DOCTYPE_SYSTEM));
        String property2 = properties.getProperty(Constants.ATTRNAME_OUTPUT_DOCTYPE_PUBLIC);
        setDoctypePublic(property2);
        if (properties.get(Constants.ATTRNAME_OUTPUT_STANDALONE) != null) {
            String property3 = properties.getProperty(Constants.ATTRNAME_OUTPUT_STANDALONE);
            if (z) {
                setStandaloneInternal(property3);
            } else {
                setStandalone(property3);
            }
        }
        setMediaType(properties.getProperty(Constants.ATTRNAME_OUTPUT_MEDIATYPE));
        if (property2 != null && property2.startsWith("-//W3C//DTD XHTML")) {
            this.m_spaceBeforeClose = true;
        }
        if (getVersion() == null) {
            setVersion(properties.getProperty("version"));
        }
        String encoding = getEncoding();
        if (encoding == null) {
            encoding = Encodings.getMimeEncoding(properties.getProperty(Constants.ATTRNAME_OUTPUT_ENCODING));
            setEncoding(encoding);
        }
        this.m_isUTF8 = encoding.equals("UTF-8");
        String str = (String) properties.get(OutputPropertiesFactory.S_KEY_ENTITIES);
        if (str != null) {
            this.m_charInfo = CharInfo.getCharInfo(str, (String) properties.get(Constants.ATTRNAME_OUTPUT_METHOD));
        }
    }

    private synchronized void init(Writer writer, Properties properties) {
        init(writer, properties, false, false);
    }

    /* access modifiers changed from: protected */
    public synchronized void init(OutputStream outputStream, Properties properties, boolean z) throws UnsupportedEncodingException {
        Writer writer;
        String encoding = getEncoding();
        if (encoding == null) {
            encoding = Encodings.getMimeEncoding(properties.getProperty(Constants.ATTRNAME_OUTPUT_ENCODING));
            setEncoding(encoding);
        }
        if (encoding.equalsIgnoreCase("UTF-8")) {
            this.m_isUTF8 = true;
            init(new WriterToUTF8Buffered(outputStream), properties, z, true);
        } else if (encoding.equals("WINDOWS-1250") || encoding.equals("US-ASCII") || encoding.equals("ASCII")) {
            init(new WriterToASCI(outputStream), properties, z, true);
        } else {
            try {
                writer = Encodings.getWriter(outputStream, encoding);
            } catch (UnsupportedEncodingException unused) {
                PrintStream printStream = System.out;
                printStream.println("Warning: encoding \"" + encoding + "\" not supported, using UTF-8");
                setEncoding("UTF-8");
                writer = Encodings.getWriter(outputStream, "UTF-8");
            }
            init(writer, properties, z, true);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public Properties getOutputFormat() {
        return this.m_format;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public void setWriter(Writer writer) {
        if (this.m_tracer == null || (writer instanceof SerializerTraceWriter)) {
            this.m_writer = writer;
        } else {
            this.m_writer = new SerializerTraceWriter(writer, this.m_tracer);
        }
    }

    public boolean setLineSepUse(boolean z) {
        boolean z2 = this.m_lineSepUse;
        this.m_lineSepUse = z;
        return z2;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public void setOutputStream(OutputStream outputStream) {
        Properties properties;
        try {
            if (this.m_format == null) {
                properties = OutputPropertiesFactory.getDefaultMethodProperties("xml");
            } else {
                properties = this.m_format;
            }
            init(outputStream, properties, true);
        } catch (UnsupportedEncodingException unused) {
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public boolean setEscaping(boolean z) {
        boolean z2 = this.m_escaping;
        this.m_escaping = z;
        return z2;
    }

    /* access modifiers changed from: protected */
    public void indent(int i) throws IOException {
        if (this.m_startNewLine) {
            outputLineSep();
        }
        if (this.m_indentAmount > 0) {
            printSpace(i * this.m_indentAmount);
        }
    }

    /* access modifiers changed from: protected */
    public void indent() throws IOException {
        indent(this.m_elemContext.m_currentElemDepth);
    }

    private void printSpace(int i) throws IOException {
        Writer writer = this.m_writer;
        for (int i2 = 0; i2 < i; i2++) {
            writer.write(32);
        }
    }

    public void attributeDecl(String str, String str2, String str3, String str4, String str5) throws SAXException {
        if (!this.m_inExternalDTD) {
            try {
                Writer writer = this.m_writer;
                DTDprolog();
                writer.write("<!ATTLIST ");
                writer.write(str);
                writer.write(32);
                writer.write(str2);
                writer.write(32);
                writer.write(str3);
                if (str4 != null) {
                    writer.write(32);
                    writer.write(str4);
                }
                writer.write(62);
                writer.write(this.m_lineSep, 0, this.m_lineSepLen);
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public Writer getWriter() {
        return this.m_writer;
    }

    public void externalEntityDecl(String str, String str2, String str3) throws SAXException {
        try {
            DTDprolog();
            this.m_writer.write("<!ENTITY ");
            this.m_writer.write(str);
            if (str2 != null) {
                this.m_writer.write(" PUBLIC \"");
                this.m_writer.write(str2);
            } else {
                this.m_writer.write(" SYSTEM \"");
                this.m_writer.write(str3);
            }
            this.m_writer.write("\" >");
            this.m_writer.write(this.m_lineSep, 0, this.m_lineSepLen);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    public boolean escapingNotNeeded(char c) {
        if (c < 127) {
            return c >= ' ' || '\n' == c || '\r' == c || '\t' == c;
        }
        return this.m_encodingInfo.isInEncoding(c);
    }

    /* access modifiers changed from: protected */
    public int writeUTF16Surrogate(char c, char[] cArr, int i, int i2) throws IOException, SAXException {
        int i3 = i + 1;
        int i4 = -1;
        if (i3 >= i2) {
            this.m_highSurrogate = c;
            return -1;
        }
        char c2 = this.m_highSurrogate;
        if (c2 == 0) {
            c2 = c;
            c = cArr[i3];
            i4 = 0;
        } else {
            this.m_highSurrogate = 0;
        }
        if (!Encodings.isLowUTF16Surrogate(c)) {
            throwIOE(c2, c);
        }
        Writer writer = this.m_writer;
        if (this.m_encodingInfo.isInEncoding(c2, c)) {
            writer.write(new char[]{c2, c}, 0, 2);
            return i4;
        } else if (getEncoding() != null) {
            return writeCharRef(writer, c2, c);
        } else {
            writer.write(new char[]{c2, c}, 0, 2);
            return i4;
        }
    }

    /* access modifiers changed from: protected */
    public int accumDefaultEntity(Writer writer, char c, int i, char[] cArr, int i2, boolean z, boolean z2) throws IOException {
        String outputStringForChar;
        if (!z2 && '\n' == c) {
            writer.write(this.m_lineSep, 0, this.m_lineSepLen);
        } else if (((!z || !this.m_charInfo.isSpecialTextChar(c)) && (z || !this.m_charInfo.isSpecialAttrChar(c))) || (outputStringForChar = this.m_charInfo.getOutputStringForChar(c)) == null) {
            return i;
        } else {
            writer.write(outputStringForChar);
        }
        return i + 1;
    }

    /* access modifiers changed from: package-private */
    public void writeNormalizedChars(char[] cArr, int i, int i2, boolean z, boolean z2) throws IOException, SAXException {
        Writer writer = this.m_writer;
        int i3 = i2 + i;
        int i4 = i;
        while (i4 < i3) {
            char c = cArr[i4];
            if ('\n' == c && z2) {
                writer.write(this.m_lineSep, 0, this.m_lineSepLen);
            } else if (!z || escapingNotNeeded(c)) {
                if (z && i4 < i3 - 2 && ']' == c && ']' == cArr[i4 + 1]) {
                    int i5 = i4 + 2;
                    if ('>' == cArr[i5]) {
                        writer.write(SerializerConstants.CDATA_CONTINUE);
                        i4 = i5;
                    }
                }
                if (escapingNotNeeded(c)) {
                    if (z && !this.m_cdataTagOpen) {
                        writer.write("<![CDATA[");
                        this.m_cdataTagOpen = true;
                    }
                    writer.write(c);
                } else {
                    i4 = handleEscaping(writer, c, cArr, i4, i3);
                }
            } else {
                i4 = handleEscaping(writer, c, cArr, i4, i3);
            }
            i4++;
        }
    }

    private int handleEscaping(Writer writer, char c, char[] cArr, int i, int i2) throws IOException, SAXException {
        if (Encodings.isHighUTF16Surrogate(c) || Encodings.isLowUTF16Surrogate(c)) {
            return (writeUTF16Surrogate(c, cArr, i, i2) < 0 || !Encodings.isHighUTF16Surrogate(c)) ? i : i + 1;
        }
        writeCharRef(writer, c);
        return i;
    }

    public void endNonEscaping() throws SAXException {
        this.m_disableOutputEscapingStates.pop();
    }

    public void startNonEscaping() throws SAXException {
        this.m_disableOutputEscapingStates.push(true);
    }

    /* access modifiers changed from: protected */
    public void cdata(char[] cArr, int i, int i2) throws SAXException {
        try {
            boolean z = false;
            if (this.m_elemContext.m_startTagOpen) {
                closeStartTag();
                this.m_elemContext.m_startTagOpen = false;
            }
            this.m_ispreserve = true;
            if (!this.m_cdataTagOpen && shouldIndent()) {
                indent();
            }
            if (i2 >= 1 && escapingNotNeeded(cArr[i])) {
                z = true;
            }
            if (z && !this.m_cdataTagOpen) {
                this.m_writer.write("<![CDATA[");
                this.m_cdataTagOpen = true;
            }
            if (isEscapingDisabled()) {
                charactersRaw(cArr, i, i2);
            } else {
                writeNormalizedChars(cArr, i, i2, true, this.m_lineSepUse);
            }
            if (z && cArr[(i + i2) - 1] == ']') {
                closeCDATA();
            }
            if (this.m_tracer != null) {
                super.fireCDATAEvent(cArr, i, i2);
            }
        } catch (IOException e) {
            throw new SAXException(Utils.messages.createMessage("ER_OIERROR", null), e);
        }
    }

    private boolean isEscapingDisabled() {
        return this.m_disableOutputEscapingStates.peekOrFalse();
    }

    /* access modifiers changed from: protected */
    public void charactersRaw(char[] cArr, int i, int i2) throws SAXException {
        if (!this.m_inEntityRef) {
            try {
                if (this.m_elemContext.m_startTagOpen) {
                    closeStartTag();
                    this.m_elemContext.m_startTagOpen = false;
                }
                this.m_ispreserve = true;
                this.m_writer.write(cArr, i, i2);
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    public void characters(char[] cArr, int i, int i2) throws SAXException {
        if (i2 == 0) {
            return;
        }
        if (!this.m_inEntityRef || this.m_expandDTDEntities) {
            if (this.m_elemContext.m_startTagOpen) {
                closeStartTag();
                this.m_elemContext.m_startTagOpen = false;
            } else if (this.m_needToCallStartDocument) {
                startDocumentInternal();
            }
            if (this.m_cdataStartCalled || this.m_elemContext.m_isCdataSection) {
                cdata(cArr, i, i2);
                return;
            }
            if (this.m_cdataTagOpen) {
                closeCDATA();
            }
            if (this.m_disableOutputEscapingStates.peekOrFalse() || !this.m_escaping) {
                charactersRaw(cArr, i, i2);
                if (this.m_tracer != null) {
                    super.fireCharEvent(cArr, i, i2);
                    return;
                }
                return;
            }
            if (this.m_elemContext.m_startTagOpen) {
                closeStartTag();
                this.m_elemContext.m_startTagOpen = false;
            }
            int i3 = i + i2;
            int i4 = i;
            int i5 = i - 1;
            while (i4 < i3) {
                try {
                    char c = cArr[i4];
                    if (c != ' ' && ((c != '\n' || !this.m_lineSepUse) && c != '\r' && c != '\t')) {
                        break;
                    }
                    if (!this.m_charInfo.isTextASCIIClean(c)) {
                        i4 = processDirty(cArr, i3, i4, c, i5, true);
                        i5 = i4;
                    }
                    i4++;
                } catch (IOException e) {
                    throw new SAXException(e);
                }
            }
            if (i4 < i3) {
                this.m_ispreserve = true;
            }
            boolean equals = "1.0".equals(getVersion());
            while (true) {
                if (i4 >= i3) {
                    break;
                }
                while (i4 < i3) {
                    char c2 = cArr[i4];
                    if (c2 >= 127 || !this.m_charInfo.isTextASCIIClean(c2)) {
                        break;
                    }
                    i4++;
                }
                if (i4 == i3) {
                    break;
                }
                char c3 = cArr[i4];
                if (isCharacterInC0orC1Range(c3) || ((!equals && isNELorLSEPCharacter(c3)) || !escapingNotNeeded(c3) || this.m_charInfo.isSpecialTextChar(c3))) {
                    if ('\"' != c3) {
                        i5 = processDirty(cArr, i3, i4, c3, i5, true);
                        i4 = i5;
                    }
                }
                i4++;
            }
            int i6 = i5 + 1;
            if (i4 > i6) {
                this.m_writer.write(cArr, i6, i4 - i6);
            }
            this.m_isprevtext = true;
            if (this.m_tracer != null) {
                super.fireCharEvent(cArr, i, i2);
            }
        }
    }

    private int processDirty(char[] cArr, int i, int i2, char c, int i3, boolean z) throws IOException, SAXException {
        int i4 = i3 + 1;
        if (i2 > i4) {
            this.m_writer.write(cArr, i4, i2 - i4);
        }
        if ('\n' != c || !z) {
            return accumDefaultEscape(this.m_writer, c, i2, cArr, i, z, false) - 1;
        }
        this.m_writer.write(this.m_lineSep, 0, this.m_lineSepLen);
        return i2;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void characters(String str) throws SAXException {
        if (!this.m_inEntityRef || this.m_expandDTDEntities) {
            int length = str.length();
            if (length > this.m_charsBuff.length) {
                this.m_charsBuff = new char[((length * 2) + 1)];
            }
            str.getChars(0, length, this.m_charsBuff, 0);
            characters(this.m_charsBuff, 0, length);
        }
    }

    /* access modifiers changed from: protected */
    public int accumDefaultEscape(Writer writer, char c, int i, char[] cArr, int i2, boolean z, boolean z2) throws IOException, SAXException {
        int accumDefaultEntity = accumDefaultEntity(writer, c, i, cArr, i2, z, z2);
        if (i != accumDefaultEntity) {
            return accumDefaultEntity;
        }
        if (this.m_highSurrogate != 0) {
            if (!Encodings.isLowUTF16Surrogate(c)) {
                throwIOE(this.m_highSurrogate, c);
            }
            writeCharRef(writer, this.m_highSurrogate, c);
            this.m_highSurrogate = 0;
            return accumDefaultEntity + 1;
        }
        if (Encodings.isHighUTF16Surrogate(c)) {
            int i3 = i + 1;
            if (i3 >= i2) {
                this.m_highSurrogate = c;
            } else {
                char c2 = cArr[i3];
                if (!Encodings.isLowUTF16Surrogate(c2)) {
                    throwIOE(c, c2);
                }
                writeCharRef(writer, c, c2);
                return accumDefaultEntity + 2;
            }
        } else if (isCharacterInC0orC1Range(c) || (SerializerConstants.XMLVERSION11.equals(getVersion()) && isNELorLSEPCharacter(c))) {
            writeCharRef(writer, c);
        } else if ((!escapingNotNeeded(c) || ((z && this.m_charInfo.isSpecialTextChar(c)) || (!z && this.m_charInfo.isSpecialAttrChar(c)))) && this.m_elemContext.m_currentElemDepth > 0) {
            writeCharRef(writer, c);
        } else {
            writer.write(c);
        }
        return accumDefaultEntity + 1;
    }

    private void writeCharRef(Writer writer, char c) throws IOException, SAXException {
        if (this.m_cdataTagOpen) {
            closeCDATA();
        }
        writer.write("&#");
        writer.write(Integer.toString(c));
        writer.write(59);
    }

    private int writeCharRef(Writer writer, char c, char c2) throws IOException, SAXException {
        if (this.m_cdataTagOpen) {
            closeCDATA();
        }
        int codePoint = Encodings.toCodePoint(c, c2);
        writer.write("&#");
        writer.write(Integer.toString(codePoint));
        writer.write(59);
        return codePoint;
    }

    private void throwIOE(char c, char c2) throws IOException {
        Messages messages = Utils.messages;
        throw new IOException(messages.createMessage("ER_INVALID_UTF16_SURROGATE", new Object[]{Integer.toHexString(c) + " " + Integer.toHexString(c2)}));
    }

    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        if (!this.m_inEntityRef) {
            if (this.m_needToCallStartDocument) {
                startDocumentInternal();
                this.m_needToCallStartDocument = false;
            } else if (this.m_cdataTagOpen) {
                closeCDATA();
            }
            try {
                if (true == this.m_needToOutputDocTypeDecl && getDoctypeSystem() != null) {
                    outputDocTypeDecl(str3, true);
                }
                this.m_needToOutputDocTypeDecl = false;
                if (this.m_elemContext.m_startTagOpen) {
                    closeStartTag();
                    this.m_elemContext.m_startTagOpen = false;
                }
                if (str != null) {
                    ensurePrefixIsDeclared(str, str3);
                }
                this.m_ispreserve = false;
                if (shouldIndent() && this.m_startNewLine) {
                    indent();
                }
                this.m_startNewLine = true;
                Writer writer = this.m_writer;
                writer.write(60);
                writer.write(str3);
                if (attributes != null) {
                    addAttributes(attributes);
                }
                this.m_elemContext = this.m_elemContext.push(str, str2, str3);
                this.m_isprevtext = false;
                if (this.m_tracer != null) {
                    firePseudoAttributes();
                }
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void startElement(String str, String str2, String str3) throws SAXException {
        startElement(str, str2, str3, null);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void startElement(String str) throws SAXException {
        startElement(null, null, str, null);
    }

    /* access modifiers changed from: package-private */
    public void outputDocTypeDecl(String str, boolean z) throws SAXException {
        if (this.m_cdataTagOpen) {
            closeCDATA();
        }
        try {
            Writer writer = this.m_writer;
            writer.write("<!DOCTYPE ");
            writer.write(str);
            String doctypePublic = getDoctypePublic();
            if (doctypePublic != null) {
                writer.write(" PUBLIC \"");
                writer.write(doctypePublic);
                writer.write(34);
            }
            String doctypeSystem = getDoctypeSystem();
            if (doctypeSystem != null) {
                if (doctypePublic == null) {
                    writer.write(" SYSTEM \"");
                } else {
                    writer.write(" \"");
                }
                writer.write(doctypeSystem);
                if (z) {
                    writer.write("\">");
                    writer.write(this.m_lineSep, 0, this.m_lineSepLen);
                    return;
                }
                writer.write(34);
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void processAttributes(Writer writer, int i) throws IOException, SAXException {
        String encoding = getEncoding();
        for (int i2 = 0; i2 < i; i2++) {
            String qName = this.m_attributes.getQName(i2);
            String value = this.m_attributes.getValue(i2);
            writer.write(32);
            writer.write(qName);
            writer.write("=\"");
            writeAttrString(writer, value, encoding);
            writer.write(34);
        }
    }

    public void writeAttrString(Writer writer, String str, String str2) throws IOException, SAXException {
        int length = str.length();
        if (length > this.m_attrBuff.length) {
            this.m_attrBuff = new char[((length * 2) + 1)];
        }
        str.getChars(0, length, this.m_attrBuff, 0);
        char[] cArr = this.m_attrBuff;
        int i = 0;
        while (i < length) {
            char c = cArr[i];
            if (!escapingNotNeeded(c) || this.m_charInfo.isSpecialAttrChar(c)) {
                i = accumDefaultEscape(writer, c, i, cArr, length, false, true);
            } else {
                writer.write(c);
                i++;
            }
        }
    }

    public void endElement(String str, String str2, String str3) throws SAXException {
        if (!this.m_inEntityRef) {
            this.m_prefixMap.popNamespaces(this.m_elemContext.m_currentElemDepth, null);
            try {
                Writer writer = this.m_writer;
                if (this.m_elemContext.m_startTagOpen) {
                    if (this.m_tracer != null) {
                        super.fireStartElem(this.m_elemContext.m_elementName);
                    }
                    int length = this.m_attributes.getLength();
                    if (length > 0) {
                        processAttributes(this.m_writer, length);
                        this.m_attributes.clear();
                    }
                    if (this.m_spaceBeforeClose) {
                        writer.write(" />");
                    } else {
                        writer.write("/>");
                    }
                } else {
                    if (this.m_cdataTagOpen) {
                        closeCDATA();
                    }
                    if (shouldIndent()) {
                        indent(this.m_elemContext.m_currentElemDepth - 1);
                    }
                    writer.write(60);
                    writer.write(47);
                    writer.write(str3);
                    writer.write(62);
                }
                if (!this.m_elemContext.m_startTagOpen && this.m_doIndent) {
                    this.m_ispreserve = this.m_preserves.isEmpty() ? false : this.m_preserves.pop();
                }
                this.m_isprevtext = false;
                if (this.m_tracer != null) {
                    super.fireEndElem(str3);
                }
                this.m_elemContext = this.m_elemContext.m_prev;
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void endElement(String str) throws SAXException {
        endElement(null, null, str);
    }

    public void startPrefixMapping(String str, String str2) throws SAXException {
        startPrefixMapping(str, str2, true);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public boolean startPrefixMapping(String str, String str2, boolean z) throws SAXException {
        int i;
        if (z) {
            flushPending();
            i = this.m_elemContext.m_currentElemDepth + 1;
        } else {
            i = this.m_elemContext.m_currentElemDepth;
        }
        boolean pushNamespace = this.m_prefixMap.pushNamespace(str, str2, i);
        if (pushNamespace) {
            if ("".equals(str)) {
                addAttributeAlways("http://www.w3.org/2000/xmlns/", "xmlns", "xmlns", "CDATA", str2, false);
            } else if (!"".equals(str2)) {
                addAttributeAlways("http://www.w3.org/2000/xmlns/", str, "xmlns:" + str, "CDATA", str2, false);
            }
        }
        return pushNamespace;
    }

    public void comment(char[] cArr, int i, int i2) throws SAXException {
        if (!this.m_inEntityRef) {
            if (this.m_elemContext.m_startTagOpen) {
                closeStartTag();
                this.m_elemContext.m_startTagOpen = false;
            } else if (this.m_needToCallStartDocument) {
                startDocumentInternal();
                this.m_needToCallStartDocument = false;
            }
            try {
                if (shouldIndent() && this.m_isStandalone) {
                    indent();
                }
                int i3 = i + i2;
                if (this.m_cdataTagOpen) {
                    closeCDATA();
                }
                if (shouldIndent() && !this.m_isStandalone) {
                    indent();
                }
                Writer writer = this.m_writer;
                writer.write("<!--");
                int i4 = i;
                int i5 = i4;
                boolean z = false;
                while (i4 < i3) {
                    if (z && cArr[i4] == '-') {
                        writer.write(cArr, i5, i4 - i5);
                        writer.write(" -");
                        i5 = i4 + 1;
                    }
                    z = cArr[i4] == '-';
                    i4++;
                }
                if (i2 > 0) {
                    int i6 = i3 - i5;
                    if (i6 > 0) {
                        writer.write(cArr, i5, i6);
                    }
                    if (cArr[i3 - 1] == '-') {
                        writer.write(32);
                    }
                }
                writer.write("-->");
                this.m_startNewLine = true;
                if (this.m_tracer != null) {
                    super.fireCommentEvent(cArr, i, i2);
                }
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    public void endCDATA() throws SAXException {
        if (this.m_cdataTagOpen) {
            closeCDATA();
        }
        this.m_cdataStartCalled = false;
    }

    public void endDTD() throws SAXException {
        try {
            if (!this.m_needToCallStartDocument) {
                if (this.m_needToOutputDocTypeDecl) {
                    outputDocTypeDecl(this.m_elemContext.m_elementName, false);
                    this.m_needToOutputDocTypeDecl = false;
                }
                Writer writer = this.m_writer;
                if (!this.m_inDoctype) {
                    writer.write("]>");
                } else {
                    writer.write(62);
                }
                writer.write(this.m_lineSep, 0, this.m_lineSepLen);
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
        if (i2 != 0) {
            characters(cArr, i, i2);
        }
    }

    public void startCDATA() throws SAXException {
        this.m_cdataStartCalled = true;
    }

    public void startEntity(String str) throws SAXException {
        if (str.equals("[dtd]")) {
            this.m_inExternalDTD = true;
        }
        if (!this.m_expandDTDEntities && !this.m_inExternalDTD) {
            startNonEscaping();
            characters("&" + str + ';');
            endNonEscaping();
        }
        this.m_inEntityRef = true;
    }

    /* access modifiers changed from: protected */
    public void closeStartTag() throws SAXException {
        if (this.m_elemContext.m_startTagOpen) {
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
    }

    public void startDTD(String str, String str2, String str3) throws SAXException {
        setDoctypeSystem(str3);
        setDoctypePublic(str2);
        this.m_elemContext.m_elementName = str;
        this.m_inDoctype = true;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public int getIndentAmount() {
        return this.m_indentAmount;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public void setIndentAmount(int i) {
        this.m_indentAmount = i;
    }

    /* access modifiers changed from: protected */
    public boolean shouldIndent() {
        return this.m_doIndent && !this.m_ispreserve && !this.m_isprevtext && (this.m_elemContext.m_currentElemDepth > 0 || this.m_isStandalone);
    }

    private void setCdataSectionElements(String str, Properties properties) {
        String property = properties.getProperty(str);
        if (property != null) {
            Vector vector = new Vector();
            int length = property.length();
            StringBuffer stringBuffer = new StringBuffer();
            boolean z = false;
            for (int i = 0; i < length; i++) {
                char charAt = property.charAt(i);
                if (Character.isWhitespace(charAt)) {
                    if (!z) {
                        if (stringBuffer.length() > 0) {
                            addCdataSectionElement(stringBuffer.toString(), vector);
                            stringBuffer.setLength(0);
                        }
                    }
                } else if ('{' == charAt) {
                    z = true;
                } else if ('}' == charAt) {
                    z = false;
                }
                stringBuffer.append(charAt);
            }
            if (stringBuffer.length() > 0) {
                addCdataSectionElement(stringBuffer.toString(), vector);
                stringBuffer.setLength(0);
            }
            setCdataSectionElements(vector);
        }
    }

    private void addCdataSectionElement(String str, Vector vector) {
        StringTokenizer stringTokenizer = new StringTokenizer(str, "{}", false);
        String nextToken = stringTokenizer.nextToken();
        String nextToken2 = stringTokenizer.hasMoreTokens() ? stringTokenizer.nextToken() : null;
        if (nextToken2 == null) {
            vector.addElement(null);
            vector.addElement(nextToken);
            return;
        }
        vector.addElement(nextToken);
        vector.addElement(nextToken2);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public void setCdataSectionElements(Vector vector) {
        this.m_cdataSectionElements = vector;
    }

    /* access modifiers changed from: protected */
    public String ensureAttributesNamespaceIsDeclared(String str, String str2, String str3) throws SAXException {
        String str4;
        if (str == null || str.length() <= 0) {
            return null;
        }
        int indexOf = str3.indexOf(":");
        if (indexOf < 0) {
            str4 = "";
        } else {
            str4 = str3.substring(0, indexOf);
        }
        if (indexOf > 0) {
            String lookupNamespace = this.m_prefixMap.lookupNamespace(str4);
            if (lookupNamespace != null && lookupNamespace.equals(str)) {
                return null;
            }
            startPrefixMapping(str4, str, false);
            addAttribute("http://www.w3.org/2000/xmlns/", str4, "xmlns:" + str4, "CDATA", str, false);
            return str4;
        }
        String lookupPrefix = this.m_prefixMap.lookupPrefix(str);
        if (lookupPrefix != null) {
            return lookupPrefix;
        }
        String generateNextPrefix = this.m_prefixMap.generateNextPrefix();
        startPrefixMapping(generateNextPrefix, str, false);
        addAttribute("http://www.w3.org/2000/xmlns/", generateNextPrefix, "xmlns:" + generateNextPrefix, "CDATA", str, false);
        return generateNextPrefix;
    }

    /* access modifiers changed from: package-private */
    public void ensurePrefixIsDeclared(String str, String str2) throws SAXException {
        String str3;
        if (str != null && str.length() > 0) {
            int indexOf = str2.indexOf(":");
            boolean z = indexOf < 0;
            if (z) {
                str3 = "";
            } else {
                str3 = str2.substring(0, indexOf);
            }
            if (str3 != null) {
                String lookupNamespace = this.m_prefixMap.lookupNamespace(str3);
                if (lookupNamespace == null || !lookupNamespace.equals(str)) {
                    startPrefixMapping(str3, str);
                    String str4 = "xmlns";
                    String str5 = z ? str4 : str3;
                    if (!z) {
                        str4 = "xmlns:" + str3;
                    }
                    addAttributeAlways("http://www.w3.org/2000/xmlns/", str5, str4, "CDATA", str, false);
                }
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public void flushPending() throws SAXException {
        if (this.m_needToCallStartDocument) {
            startDocumentInternal();
            this.m_needToCallStartDocument = false;
        }
        if (this.m_elemContext.m_startTagOpen) {
            closeStartTag();
            this.m_elemContext.m_startTagOpen = false;
        }
        if (this.m_cdataTagOpen) {
            closeCDATA();
            this.m_cdataTagOpen = false;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase
    public boolean addAttributeAlways(String str, String str2, String str3, String str4, String str5, boolean z) {
        NamespaceMappings.MappingRecord mappingFromPrefix;
        int index = this.m_attributes.getIndex(str3);
        boolean z2 = false;
        if (index >= 0) {
            String str6 = null;
            if (this.m_tracer != null) {
                String value = this.m_attributes.getValue(index);
                if (!str5.equals(value)) {
                    str6 = value;
                }
            }
            this.m_attributes.setValue(index, str5);
            if (str6 != null) {
                firePseudoAttributes();
            }
        } else {
            if (z) {
                int indexOf = str3.indexOf(58);
                if (indexOf > 0 && (mappingFromPrefix = this.m_prefixMap.getMappingFromPrefix(str3.substring(0, indexOf))) != null && mappingFromPrefix.m_declarationDepth == this.m_elemContext.m_currentElemDepth && !mappingFromPrefix.m_uri.equals(str)) {
                    String lookupPrefix = this.m_prefixMap.lookupPrefix(str);
                    if (lookupPrefix == null) {
                        lookupPrefix = this.m_prefixMap.generateNextPrefix();
                    }
                    str3 = lookupPrefix + ':' + str2;
                }
                try {
                    ensureAttributesNamespaceIsDeclared(str, str2, str3);
                } catch (SAXException e) {
                    e.printStackTrace();
                }
            }
            this.m_attributes.addAttribute(str, str2, str3, str4, str5);
            z2 = true;
            if (this.m_tracer != null) {
                firePseudoAttributes();
            }
        }
        return z2;
    }

    /* access modifiers changed from: protected */
    public void firePseudoAttributes() {
        if (this.m_tracer != null) {
            try {
                this.m_writer.flush();
                StringBuffer stringBuffer = new StringBuffer();
                int length = this.m_attributes.getLength();
                if (length > 0) {
                    processAttributes(new WritertoStringBuffer(stringBuffer), length);
                }
                stringBuffer.append('>');
                char[] charArray = stringBuffer.toString().toCharArray();
                this.m_tracer.fireGenerateEvent(11, charArray, 0, charArray.length);
            } catch (IOException | SAXException unused) {
            }
        }
    }

    /* access modifiers changed from: private */
    public class WritertoStringBuffer extends Writer {
        private final StringBuffer m_stringbuf;

        @Override // java.io.Writer, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
        }

        @Override // java.io.Writer, java.io.Flushable
        public void flush() throws IOException {
        }

        WritertoStringBuffer(StringBuffer stringBuffer) {
            this.m_stringbuf = stringBuffer;
        }

        @Override // java.io.Writer
        public void write(char[] cArr, int i, int i2) throws IOException {
            this.m_stringbuf.append(cArr, i, i2);
        }

        @Override // java.io.Writer
        public void write(int i) {
            this.m_stringbuf.append((char) i);
        }

        @Override // java.io.Writer
        public void write(String str) {
            this.m_stringbuf.append(str);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public void setTransformer(Transformer transformer) {
        super.setTransformer(transformer);
        if (this.m_tracer != null && !(this.m_writer instanceof SerializerTraceWriter)) {
            this.m_writer = new SerializerTraceWriter(this.m_writer, this.m_tracer);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.Serializer
    public boolean reset() {
        if (!super.reset()) {
            return false;
        }
        resetToStream();
        return true;
    }

    private void resetToStream() {
        this.m_cdataStartCalled = false;
        this.m_disableOutputEscapingStates.clear();
        this.m_escaping = true;
        this.m_inDoctype = false;
        this.m_ispreserve = false;
        this.m_ispreserve = false;
        this.m_isprevtext = false;
        this.m_isUTF8 = false;
        this.m_preserves.clear();
        this.m_shouldFlush = true;
        this.m_spaceBeforeClose = false;
        this.m_startNewLine = false;
        this.m_lineSepUse = true;
        this.m_expandDTDEntities = true;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.XSLOutputAttributes
    public void setEncoding(String str) {
        String encoding = getEncoding();
        super.setEncoding(str);
        if (encoding == null || !encoding.equals(str)) {
            this.m_encodingInfo = Encodings.getEncodingInfo(str);
            if (str != null && this.m_encodingInfo.name == null) {
                String createMessage = Utils.messages.createMessage("ER_ENCODING_NOT_SUPPORTED", new Object[]{str});
                try {
                    Transformer transformer = super.getTransformer();
                    if (transformer != null) {
                        ErrorListener errorListener = transformer.getErrorListener();
                        if (errorListener == null || this.m_sourceLocator == null) {
                            System.out.println(createMessage);
                        } else {
                            errorListener.warning(new TransformerException(createMessage, this.m_sourceLocator));
                        }
                    } else {
                        System.out.println(createMessage);
                    }
                } catch (Exception unused) {
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static final class BoolStack {
        private int m_allocatedSize;
        private int m_index;
        private boolean[] m_values;

        public BoolStack() {
            this(32);
        }

        public BoolStack(int i) {
            this.m_allocatedSize = i;
            this.m_values = new boolean[i];
            this.m_index = -1;
        }

        public final int size() {
            return this.m_index + 1;
        }

        public final void clear() {
            this.m_index = -1;
        }

        public final boolean push(boolean z) {
            if (this.m_index == this.m_allocatedSize - 1) {
                grow();
            }
            boolean[] zArr = this.m_values;
            int i = this.m_index + 1;
            this.m_index = i;
            zArr[i] = z;
            return z;
        }

        public final boolean pop() {
            boolean[] zArr = this.m_values;
            int i = this.m_index;
            this.m_index = i - 1;
            return zArr[i];
        }

        public final boolean popAndTop() {
            this.m_index--;
            int i = this.m_index;
            if (i >= 0) {
                return this.m_values[i];
            }
            return false;
        }

        public final void setTop(boolean z) {
            this.m_values[this.m_index] = z;
        }

        public final boolean peek() {
            return this.m_values[this.m_index];
        }

        public final boolean peekOrFalse() {
            int i = this.m_index;
            if (i > -1) {
                return this.m_values[i];
            }
            return false;
        }

        public final boolean peekOrTrue() {
            int i = this.m_index;
            if (i > -1) {
                return this.m_values[i];
            }
            return true;
        }

        public boolean isEmpty() {
            return this.m_index == -1;
        }

        private void grow() {
            this.m_allocatedSize *= 2;
            boolean[] zArr = new boolean[this.m_allocatedSize];
            System.arraycopy(this.m_values, 0, zArr, 0, this.m_index + 1);
            this.m_values = zArr;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase
    public void notationDecl(String str, String str2, String str3) throws SAXException {
        try {
            DTDprolog();
            this.m_writer.write("<!NOTATION ");
            this.m_writer.write(str);
            if (str2 != null) {
                this.m_writer.write(" PUBLIC \"");
                this.m_writer.write(str2);
            } else {
                this.m_writer.write(" SYSTEM \"");
                this.m_writer.write(str3);
            }
            this.m_writer.write("\" >");
            this.m_writer.write(this.m_lineSep, 0, this.m_lineSepLen);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase
    public void unparsedEntityDecl(String str, String str2, String str3, String str4) throws SAXException {
        try {
            DTDprolog();
            this.m_writer.write("<!ENTITY ");
            this.m_writer.write(str);
            if (str2 != null) {
                this.m_writer.write(" PUBLIC \"");
                this.m_writer.write(str2);
            } else {
                this.m_writer.write(" SYSTEM \"");
                this.m_writer.write(str3);
            }
            this.m_writer.write("\" NDATA ");
            this.m_writer.write(str4);
            this.m_writer.write(" >");
            this.m_writer.write(this.m_lineSep, 0, this.m_lineSepLen);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void DTDprolog() throws SAXException, IOException {
        Writer writer = this.m_writer;
        if (this.m_needToOutputDocTypeDecl) {
            outputDocTypeDecl(this.m_elemContext.m_elementName, false);
            this.m_needToOutputDocTypeDecl = false;
        }
        if (this.m_inDoctype) {
            writer.write(" [");
            writer.write(this.m_lineSep, 0, this.m_lineSepLen);
            this.m_inDoctype = false;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.SerializerBase, ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public void setDTDEntityExpansion(boolean z) {
        this.m_expandDTDEntities = z;
    }
}
