package org.apache.xml.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import org.apache.xalan.templates.Constants;
import org.apache.xml.serializer.NamespaceMappings;
import org.apache.xml.serializer.utils.Messages;
import org.apache.xml.serializer.utils.Utils;
import org.apache.xml.serializer.utils.WrappedRuntimeException;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public abstract class ToStream extends SerializerBase {
    private static final String COMMENT_BEGIN = "<!--";
    private static final String COMMENT_END = "-->";
    private static final char[] s_systemLineSep = SecuritySupport.getInstance().getSystemProperty("line.separator").toCharArray();
    protected boolean m_cdataStartCalled = false;
    protected CharInfo m_charInfo;
    protected BoolStack m_disableOutputEscapingStates = new BoolStack();
    EncodingInfo m_encodingInfo = new EncodingInfo(null, null, 0);
    protected boolean m_escaping = true;
    private boolean m_expandDTDEntities = true;
    protected boolean m_inDoctype = false;
    boolean m_isUTF8 = false;
    protected boolean m_ispreserve = false;
    protected boolean m_isprevtext = false;
    protected char[] m_lineSep = s_systemLineSep;
    protected int m_lineSepLen = this.m_lineSep.length;
    protected boolean m_lineSepUse = true;
    OutputStream m_outputStream;
    protected BoolStack m_preserves = new BoolStack();
    boolean m_shouldFlush = true;
    protected boolean m_spaceBeforeClose = false;
    boolean m_startNewLine;
    private boolean m_writer_set_by_user;

    static final class BoolStack {
        private int m_allocatedSize;
        private int m_index;
        private boolean[] m_values;

        public BoolStack() {
            this(32);
        }

        public BoolStack(int size) {
            this.m_allocatedSize = size;
            this.m_values = new boolean[size];
            this.m_index = -1;
        }

        public final int size() {
            return this.m_index + 1;
        }

        public final void clear() {
            this.m_index = -1;
        }

        public final boolean push(boolean val) {
            if (this.m_index == this.m_allocatedSize - 1) {
                grow();
            }
            boolean[] zArr = this.m_values;
            int i = this.m_index + 1;
            this.m_index = i;
            zArr[i] = val;
            return val;
        }

        public final boolean pop() {
            boolean[] zArr = this.m_values;
            int i = this.m_index;
            this.m_index = i - 1;
            return zArr[i];
        }

        public final boolean popAndTop() {
            this.m_index--;
            if (this.m_index >= 0) {
                return this.m_values[this.m_index];
            }
            return false;
        }

        public final void setTop(boolean b) {
            this.m_values[this.m_index] = b;
        }

        public final boolean peek() {
            return this.m_values[this.m_index];
        }

        public final boolean peekOrFalse() {
            if (this.m_index > -1) {
                return this.m_values[this.m_index];
            }
            return false;
        }

        public final boolean peekOrTrue() {
            if (this.m_index > -1) {
                return this.m_values[this.m_index];
            }
            return true;
        }

        public boolean isEmpty() {
            return this.m_index == -1;
        }

        private void grow() {
            this.m_allocatedSize *= 2;
            boolean[] newVector = new boolean[this.m_allocatedSize];
            System.arraycopy(this.m_values, 0, newVector, 0, this.m_index + 1);
            this.m_values = newVector;
        }
    }

    private class WritertoStringBuffer extends Writer {
        private final StringBuffer m_stringbuf;

        WritertoStringBuffer(StringBuffer sb) {
            this.m_stringbuf = sb;
        }

        public void write(char[] arg0, int arg1, int arg2) throws IOException {
            this.m_stringbuf.append(arg0, arg1, arg2);
        }

        public void flush() throws IOException {
        }

        public void close() throws IOException {
        }

        public void write(int i) {
            this.m_stringbuf.append((char) i);
        }

        public void write(String s) {
            this.m_stringbuf.append(s);
        }
    }

    /* access modifiers changed from: protected */
    public void closeCDATA() throws SAXException {
        try {
            this.m_writer.write(SerializerConstants.CDATA_DELIMITER_CLOSE);
            this.m_cdataTagOpen = false;
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void serialize(Node node) throws IOException {
        try {
            new TreeWalker(this).traverse(node);
        } catch (SAXException se) {
            throw new WrappedRuntimeException(se);
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
            } catch (IOException ioe) {
                throw new SAXException(ioe);
            }
        }
    }

    public OutputStream getOutputStream() {
        return this.m_outputStream;
    }

    public void elementDecl(String name, String model) throws SAXException {
        if (!this.m_inExternalDTD) {
            try {
                Writer writer = this.m_writer;
                DTDprolog();
                writer.write("<!ELEMENT ");
                writer.write(name);
                writer.write(32);
                writer.write(model);
                writer.write(62);
                writer.write(this.m_lineSep, 0, this.m_lineSepLen);
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    public void internalEntityDecl(String name, String value) throws SAXException {
        if (!this.m_inExternalDTD) {
            try {
                DTDprolog();
                outputEntityDecl(name, value);
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void outputEntityDecl(String name, String value) throws IOException {
        Writer writer = this.m_writer;
        writer.write("<!ENTITY ");
        writer.write(name);
        writer.write(" \"");
        writer.write(value);
        writer.write("\">");
        writer.write(this.m_lineSep, 0, this.m_lineSepLen);
    }

    /* access modifiers changed from: protected */
    public final void outputLineSep() throws IOException {
        this.m_writer.write(this.m_lineSep, 0, this.m_lineSepLen);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    public void setProp(String name, String val, boolean defaultVal) {
        String val2;
        String str = name;
        String cdataSectionNames = val;
        boolean z = defaultVal;
        if (cdataSectionNames != null) {
            char first = getFirstCharLocName(name);
            if (first != 'i') {
                if (first != 'o') {
                    if (first != 's') {
                        if (first != 'v') {
                            switch (first) {
                                case 'c':
                                    if (Constants.ATTRNAME_OUTPUT_CDATA_SECTION_ELEMENTS.equals(str)) {
                                        addCdataSectionElements(cdataSectionNames);
                                    }
                                case 'd':
                                    if (Constants.ATTRNAME_OUTPUT_DOCTYPE_SYSTEM.equals(str)) {
                                        this.m_doctypeSystem = cdataSectionNames;
                                    } else if (Constants.ATTRNAME_OUTPUT_DOCTYPE_PUBLIC.equals(str)) {
                                        this.m_doctypePublic = cdataSectionNames;
                                        if (cdataSectionNames.startsWith("-//W3C//DTD XHTML")) {
                                            this.m_spaceBeforeClose = true;
                                        }
                                    }
                                case 'e':
                                    String newEncoding = cdataSectionNames;
                                    if ("encoding".equals(str)) {
                                        String possible_encoding = Encodings.getMimeEncoding(val);
                                        if (possible_encoding != null) {
                                            super.setProp("mime-name", possible_encoding, z);
                                        }
                                        String oldExplicitEncoding = getOutputPropertyNonDefault("encoding");
                                        String oldDefaultEncoding = getOutputPropertyDefault("encoding");
                                        if ((z && (oldDefaultEncoding == null || !oldDefaultEncoding.equalsIgnoreCase(newEncoding))) || (!z && (oldExplicitEncoding == null || !oldExplicitEncoding.equalsIgnoreCase(newEncoding)))) {
                                            EncodingInfo encodingInfo = Encodings.getEncodingInfo(newEncoding);
                                            if (newEncoding == null || encodingInfo.name != null) {
                                                val2 = cdataSectionNames;
                                            } else {
                                                String msg = Utils.messages.createMessage("ER_ENCODING_NOT_SUPPORTED", new Object[]{newEncoding});
                                                String msg2 = "Warning: encoding \"" + newEncoding + "\" not supported, using " + "UTF-8";
                                                try {
                                                    Transformer tran = super.getTransformer();
                                                    if (tran != null) {
                                                        ErrorListener errHandler = tran.getErrorListener();
                                                        if (errHandler == null || this.m_sourceLocator == null) {
                                                            System.out.println(msg);
                                                            System.out.println(msg2);
                                                        } else {
                                                            errHandler.warning(new TransformerException(msg, this.m_sourceLocator));
                                                            errHandler.warning(new TransformerException(msg2, this.m_sourceLocator));
                                                        }
                                                    } else {
                                                        System.out.println(msg);
                                                        System.out.println(msg2);
                                                    }
                                                } catch (Exception e) {
                                                }
                                                newEncoding = "UTF-8";
                                                val2 = "UTF-8";
                                                encodingInfo = Encodings.getEncodingInfo(newEncoding);
                                            }
                                            if (!z || oldExplicitEncoding == null) {
                                                this.m_encodingInfo = encodingInfo;
                                                if (newEncoding != null) {
                                                    this.m_isUTF8 = newEncoding.equals("UTF-8");
                                                }
                                                OutputStream os = getOutputStream();
                                                if (os != null) {
                                                    Writer w = getWriter();
                                                    String oldEncoding = getOutputProperty("encoding");
                                                    if ((w == null || !this.m_writer_set_by_user) && !newEncoding.equalsIgnoreCase(oldEncoding)) {
                                                        super.setProp(str, val2, z);
                                                        setOutputStreamInternal(os, false);
                                                        break;
                                                    }
                                                }
                                            }
                                        } else {
                                            val2 = cdataSectionNames;
                                            break;
                                        }
                                    }
                                    break;
                                default:
                                    switch (first) {
                                        case 'l':
                                            if (OutputPropertiesFactory.S_KEY_LINE_SEPARATOR.equals(str)) {
                                                this.m_lineSep = val.toCharArray();
                                                this.m_lineSepLen = this.m_lineSep.length;
                                            }
                                        case 'm':
                                            if (Constants.ATTRNAME_OUTPUT_MEDIATYPE.equals(str)) {
                                                this.m_mediatype = cdataSectionNames;
                                            }
                                    }
                            }
                        } else if ("version".equals(str)) {
                            this.m_version = cdataSectionNames;
                        }
                    } else if (Constants.ATTRNAME_OUTPUT_STANDALONE.equals(str)) {
                        if (z) {
                            setStandaloneInternal(cdataSectionNames);
                        } else {
                            this.m_standaloneWasSpecified = true;
                            setStandaloneInternal(cdataSectionNames);
                        }
                    }
                } else if ("omit-xml-declaration".equals(str)) {
                    this.m_shouldNotWriteXMLHeader = "yes".equals(cdataSectionNames);
                }
            } else if (OutputPropertiesFactory.S_KEY_INDENT_AMOUNT.equals(str)) {
                setIndentAmount(Integer.parseInt(val));
            } else if ("indent".equals(str)) {
                this.m_doIndent = "yes".equals(cdataSectionNames);
            }
            val2 = cdataSectionNames;
            super.setProp(str, val2, z);
            return;
        }
        String val3 = cdataSectionNames;
    }

    public void setOutputFormat(Properties format) {
        boolean shouldFlush = this.m_shouldFlush;
        if (format != null) {
            Enumeration propNames = format.propertyNames();
            while (propNames.hasMoreElements()) {
                String key = (String) propNames.nextElement();
                String value = format.getProperty(key);
                String explicitValue = (String) format.get(key);
                if (explicitValue == null && value != null) {
                    setOutputPropertyDefault(key, value);
                }
                if (explicitValue != null) {
                    setOutputProperty(key, explicitValue);
                }
            }
        }
        String entitiesFileName = (String) format.get(OutputPropertiesFactory.S_KEY_ENTITIES);
        if (entitiesFileName != null) {
            this.m_charInfo = CharInfo.getCharInfo(entitiesFileName, (String) format.get(Constants.ATTRNAME_OUTPUT_METHOD));
        }
        this.m_shouldFlush = shouldFlush;
    }

    public Properties getOutputFormat() {
        Properties def = new Properties();
        for (String key : getOutputPropDefaultKeys()) {
            def.put(key, getOutputPropertyDefault(key));
        }
        Properties props = new Properties(def);
        for (String key2 : getOutputPropKeys()) {
            String val = getOutputPropertyNonDefault(key2);
            if (val != null) {
                props.put(key2, val);
            }
        }
        return props;
    }

    public void setWriter(Writer writer) {
        setWriterInternal(writer, true);
    }

    private void setWriterInternal(Writer writer, boolean setByUser) {
        this.m_writer_set_by_user = setByUser;
        this.m_writer = writer;
        if (this.m_tracer != null) {
            boolean noTracerYet = true;
            Writer w2 = this.m_writer;
            while (true) {
                if (!(w2 instanceof WriterChain)) {
                    break;
                } else if (w2 instanceof SerializerTraceWriter) {
                    noTracerYet = false;
                    break;
                } else {
                    w2 = ((WriterChain) w2).getWriter();
                }
            }
            if (noTracerYet) {
                this.m_writer = new SerializerTraceWriter(this.m_writer, this.m_tracer);
            }
        }
    }

    public boolean setLineSepUse(boolean use_sytem_line_break) {
        boolean oldValue = this.m_lineSepUse;
        this.m_lineSepUse = use_sytem_line_break;
        return oldValue;
    }

    public void setOutputStream(OutputStream output) {
        setOutputStreamInternal(output, true);
    }

    private void setOutputStreamInternal(OutputStream output, boolean setByUser) {
        Writer osw;
        this.m_outputStream = output;
        String encoding = getOutputProperty("encoding");
        if ("UTF-8".equalsIgnoreCase(encoding)) {
            setWriterInternal(new WriterToUTF8Buffered(output), false);
        } else if ("WINDOWS-1250".equals(encoding) || "US-ASCII".equals(encoding) || "ASCII".equals(encoding)) {
            setWriterInternal(new WriterToASCI(output), false);
        } else if (encoding != null) {
            try {
                osw = Encodings.getWriter(output, encoding);
            } catch (UnsupportedEncodingException e) {
                osw = null;
            }
            if (osw == null) {
                PrintStream printStream = System.out;
                printStream.println("Warning: encoding \"" + encoding + "\" not supported, using " + "UTF-8");
                setEncoding("UTF-8");
                try {
                    osw = Encodings.getWriter(output, "UTF-8");
                } catch (UnsupportedEncodingException e2) {
                    e2.printStackTrace();
                }
            }
            setWriterInternal(osw, false);
        } else {
            setWriterInternal(new OutputStreamWriter(output), false);
        }
    }

    public boolean setEscaping(boolean escape) {
        boolean temp = this.m_escaping;
        this.m_escaping = escape;
        return temp;
    }

    /* access modifiers changed from: protected */
    public void indent(int depth) throws IOException {
        if (this.m_startNewLine) {
            outputLineSep();
        }
        if (this.m_indentAmount > 0) {
            printSpace(this.m_indentAmount * depth);
        }
    }

    /* access modifiers changed from: protected */
    public void indent() throws IOException {
        indent(this.m_elemContext.m_currentElemDepth);
    }

    private void printSpace(int n) throws IOException {
        Writer writer = this.m_writer;
        for (int i = 0; i < n; i++) {
            writer.write(32);
        }
    }

    public void attributeDecl(String eName, String aName, String type, String valueDefault, String value) throws SAXException {
        if (!this.m_inExternalDTD) {
            try {
                Writer writer = this.m_writer;
                DTDprolog();
                writer.write("<!ATTLIST ");
                writer.write(eName);
                writer.write(32);
                writer.write(aName);
                writer.write(32);
                writer.write(type);
                if (valueDefault != null) {
                    writer.write(32);
                    writer.write(valueDefault);
                }
                writer.write(62);
                writer.write(this.m_lineSep, 0, this.m_lineSepLen);
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    public Writer getWriter() {
        return this.m_writer;
    }

    public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
        try {
            DTDprolog();
            this.m_writer.write("<!ENTITY ");
            this.m_writer.write(name);
            if (publicId != null) {
                this.m_writer.write(" PUBLIC \"");
                this.m_writer.write(publicId);
            } else {
                this.m_writer.write(" SYSTEM \"");
                this.m_writer.write(systemId);
            }
            this.m_writer.write("\" >");
            this.m_writer.write(this.m_lineSep, 0, this.m_lineSepLen);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    public boolean escapingNotNeeded(char ch) {
        if (ch >= 127) {
            return this.m_encodingInfo.isInEncoding(ch);
        }
        if (ch >= ' ' || 10 == ch || 13 == ch || 9 == ch) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public int writeUTF16Surrogate(char c, char[] ch, int i, int end) throws IOException {
        if (i + 1 < end) {
            char high = c;
            char low = ch[i + 1];
            if (Encodings.isLowUTF16Surrogate(low)) {
                Writer writer = this.m_writer;
                if (this.m_encodingInfo.isInEncoding(c, low)) {
                    writer.write(ch, i, 2);
                    return 0;
                } else if (getEncoding() != null) {
                    int codePoint = Encodings.toCodePoint(high, low);
                    writer.write(38);
                    writer.write(35);
                    writer.write(Integer.toString(codePoint));
                    writer.write(59);
                    return codePoint;
                } else {
                    writer.write(ch, i, 2);
                    return 0;
                }
            } else {
                Messages messages = Utils.messages;
                throw new IOException(messages.createMessage("ER_INVALID_UTF16_SURROGATE", new Object[]{Integer.toHexString(c) + " " + Integer.toHexString(low)}));
            }
        } else {
            throw new IOException(Utils.messages.createMessage("ER_INVALID_UTF16_SURROGATE", new Object[]{Integer.toHexString(c)}));
        }
    }

    /* access modifiers changed from: package-private */
    public int accumDefaultEntity(Writer writer, char ch, int i, char[] chars, int len, boolean fromTextNode, boolean escLF) throws IOException {
        if (!escLF && 10 == ch) {
            writer.write(this.m_lineSep, 0, this.m_lineSepLen);
        } else if ((!fromTextNode || !this.m_charInfo.shouldMapTextChar(ch)) && (fromTextNode || !this.m_charInfo.shouldMapAttrChar(ch))) {
            return i;
        } else {
            String outputStringForChar = this.m_charInfo.getOutputStringForChar(ch);
            if (outputStringForChar == null) {
                return i;
            }
            writer.write(outputStringForChar);
        }
        return i + 1;
    }

    /* access modifiers changed from: package-private */
    public void writeNormalizedChars(char[] ch, int start, int length, boolean isCData, boolean useSystemLineSeparator) throws IOException, SAXException {
        Writer writer = this.m_writer;
        int end = start + length;
        int i = start;
        while (i < end) {
            char c = ch[i];
            if (10 == c && useSystemLineSeparator) {
                writer.write(this.m_lineSep, 0, this.m_lineSepLen);
            } else if (isCData && !escapingNotNeeded(c)) {
                if (this.m_cdataTagOpen) {
                    closeCDATA();
                }
                if (Encodings.isHighUTF16Surrogate(c)) {
                    writeUTF16Surrogate(c, ch, i, end);
                    i++;
                } else {
                    writer.write("&#");
                    writer.write(Integer.toString(c));
                    writer.write(59);
                }
            } else if (isCData && i < end - 2 && ']' == c && ']' == ch[i + 1] && '>' == ch[i + 2]) {
                writer.write(SerializerConstants.CDATA_CONTINUE);
                i += 2;
            } else if (escapingNotNeeded(c)) {
                if (isCData && !this.m_cdataTagOpen) {
                    writer.write(SerializerConstants.CDATA_DELIMITER_OPEN);
                    this.m_cdataTagOpen = true;
                }
                writer.write(c);
            } else if (Encodings.isHighUTF16Surrogate(c)) {
                if (this.m_cdataTagOpen) {
                    closeCDATA();
                }
                writeUTF16Surrogate(c, ch, i, end);
                i++;
            } else {
                if (this.m_cdataTagOpen) {
                    closeCDATA();
                }
                writer.write("&#");
                writer.write(Integer.toString(c));
                writer.write(59);
            }
            i++;
        }
    }

    public void endNonEscaping() throws SAXException {
        this.m_disableOutputEscapingStates.pop();
    }

    public void startNonEscaping() throws SAXException {
        this.m_disableOutputEscapingStates.push(true);
    }

    /* access modifiers changed from: protected */
    public void cdata(char[] ch, int start, int length) throws SAXException {
        int old_start = start;
        try {
            boolean writeCDataBrackets = false;
            if (this.m_elemContext.m_startTagOpen) {
                closeStartTag();
                this.m_elemContext.m_startTagOpen = false;
            }
            this.m_ispreserve = true;
            if (shouldIndent()) {
                indent();
            }
            if (length >= 1 && escapingNotNeeded(ch[start])) {
                writeCDataBrackets = true;
            }
            if (writeCDataBrackets && !this.m_cdataTagOpen) {
                this.m_writer.write(SerializerConstants.CDATA_DELIMITER_OPEN);
                this.m_cdataTagOpen = true;
            }
            if (isEscapingDisabled()) {
                charactersRaw(ch, start, length);
            } else {
                writeNormalizedChars(ch, start, length, true, this.m_lineSepUse);
            }
            if (writeCDataBrackets && ch[(start + length) - 1] == ']') {
                closeCDATA();
            }
            if (this.m_tracer != null) {
                super.fireCDATAEvent(ch, old_start, length);
            }
        } catch (IOException ioe) {
            throw new SAXException(Utils.messages.createMessage("ER_OIERROR", null), ioe);
        }
    }

    private boolean isEscapingDisabled() {
        return this.m_disableOutputEscapingStates.peekOrFalse();
    }

    /* access modifiers changed from: protected */
    public void charactersRaw(char[] ch, int start, int length) throws SAXException {
        if (!this.m_inEntityRef) {
            try {
                if (this.m_elemContext.m_startTagOpen) {
                    closeStartTag();
                    this.m_elemContext.m_startTagOpen = false;
                }
                this.m_ispreserve = true;
                this.m_writer.write(ch, start, length);
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    public void characters(char[] chars, int start, int length) throws SAXException {
        if (length != 0 && (!this.m_inEntityRef || this.m_expandDTDEntities)) {
            this.m_docIsEmpty = false;
            if (this.m_elemContext.m_startTagOpen) {
                closeStartTag();
                this.m_elemContext.m_startTagOpen = false;
            } else if (this.m_needToCallStartDocument) {
                startDocumentInternal();
            }
            if (this.m_cdataStartCalled || this.m_elemContext.m_isCdataSection) {
                cdata(chars, start, length);
                return;
            }
            if (this.m_cdataTagOpen) {
                closeCDATA();
            }
            if (this.m_disableOutputEscapingStates.peekOrFalse() || !this.m_escaping) {
                charactersRaw(chars, start, length);
                if (this.m_tracer != null) {
                    super.fireCharEvent(chars, start, length);
                }
                return;
            }
            if (this.m_elemContext.m_startTagOpen) {
                closeStartTag();
                this.m_elemContext.m_startTagOpen = false;
            }
            int end = start + length;
            int lastDirtyCharProcessed = start - 1;
            try {
                Writer writer = this.m_writer;
                boolean isAllWhitespace = true;
                int lastDirtyCharProcessed2 = lastDirtyCharProcessed;
                int i = start;
                while (i < end && isAllWhitespace) {
                    char ch1 = chars[i];
                    if (this.m_charInfo.shouldMapTextChar(ch1)) {
                        writeOutCleanChars(chars, i, lastDirtyCharProcessed2);
                        writer.write(this.m_charInfo.getOutputStringForChar(ch1));
                        isAllWhitespace = false;
                        lastDirtyCharProcessed2 = i;
                        i++;
                    } else if (ch1 == 13) {
                        writeOutCleanChars(chars, i, lastDirtyCharProcessed2);
                        writer.write("&#13;");
                        lastDirtyCharProcessed2 = i;
                        i++;
                    } else if (ch1 != ' ') {
                        switch (ch1) {
                            case 9:
                                i++;
                                break;
                            case 10:
                                lastDirtyCharProcessed2 = processLineFeed(chars, i, lastDirtyCharProcessed2, writer);
                                i++;
                                break;
                            default:
                                isAllWhitespace = false;
                                break;
                        }
                    } else {
                        i++;
                    }
                }
                if (i < end || !isAllWhitespace) {
                    this.m_ispreserve = true;
                }
                while (i < end) {
                    char ch = chars[i];
                    if (this.m_charInfo.shouldMapTextChar(ch)) {
                        writeOutCleanChars(chars, i, lastDirtyCharProcessed2);
                        writer.write(this.m_charInfo.getOutputStringForChar(ch));
                        lastDirtyCharProcessed2 = i;
                    } else if (ch <= 31) {
                        if (ch != 13) {
                            switch (ch) {
                                case 9:
                                    break;
                                case 10:
                                    lastDirtyCharProcessed2 = processLineFeed(chars, i, lastDirtyCharProcessed2, writer);
                                    break;
                                default:
                                    writeOutCleanChars(chars, i, lastDirtyCharProcessed2);
                                    writer.write("&#");
                                    writer.write(Integer.toString(ch));
                                    writer.write(59);
                                    lastDirtyCharProcessed2 = i;
                                    break;
                            }
                        } else {
                            writeOutCleanChars(chars, i, lastDirtyCharProcessed2);
                            writer.write("&#13;");
                            lastDirtyCharProcessed2 = i;
                        }
                    } else if (ch >= 127) {
                        if (ch <= 159) {
                            writeOutCleanChars(chars, i, lastDirtyCharProcessed2);
                            writer.write("&#");
                            writer.write(Integer.toString(ch));
                            writer.write(59);
                            lastDirtyCharProcessed2 = i;
                        } else if (ch == 8232) {
                            writeOutCleanChars(chars, i, lastDirtyCharProcessed2);
                            writer.write("&#8232;");
                            lastDirtyCharProcessed2 = i;
                        } else if (!this.m_encodingInfo.isInEncoding(ch)) {
                            writeOutCleanChars(chars, i, lastDirtyCharProcessed2);
                            writer.write("&#");
                            writer.write(Integer.toString(ch));
                            writer.write(59);
                            lastDirtyCharProcessed2 = i;
                        }
                    }
                    i++;
                }
                int startClean = lastDirtyCharProcessed2 + 1;
                if (i > startClean) {
                    this.m_writer.write(chars, startClean, i - startClean);
                }
                this.m_isprevtext = true;
                if (this.m_tracer != null) {
                    super.fireCharEvent(chars, start, length);
                }
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    private int processLineFeed(char[] chars, int i, int lastProcessed, Writer writer) throws IOException {
        if (!this.m_lineSepUse) {
            return lastProcessed;
        }
        if (this.m_lineSepLen == 1 && this.m_lineSep[0] == 10) {
            return lastProcessed;
        }
        writeOutCleanChars(chars, i, lastProcessed);
        writer.write(this.m_lineSep, 0, this.m_lineSepLen);
        return i;
    }

    private void writeOutCleanChars(char[] chars, int i, int lastProcessed) throws IOException {
        int startClean = lastProcessed + 1;
        if (startClean < i) {
            this.m_writer.write(chars, startClean, i - startClean);
        }
    }

    private static boolean isCharacterInC0orC1Range(char ch) {
        boolean z = false;
        if (ch == 9 || ch == 10 || ch == 13) {
            return false;
        }
        if ((ch >= 127 && ch <= 159) || (ch >= 1 && ch <= 31)) {
            z = true;
        }
        return z;
    }

    private static boolean isNELorLSEPCharacter(char ch) {
        return ch == 133 || ch == 8232;
    }

    private int processDirty(char[] chars, int end, int i, char ch, int lastDirty, boolean fromTextNode) throws IOException {
        char[] cArr;
        int i2 = i;
        int startClean = lastDirty + 1;
        if (i2 > startClean) {
            cArr = chars;
            this.m_writer.write(cArr, startClean, i2 - startClean);
        } else {
            cArr = chars;
        }
        char c = ch;
        if (10 != c || !fromTextNode) {
            return accumDefaultEscape(this.m_writer, c, i2, cArr, end, fromTextNode, false) - 1;
        }
        this.m_writer.write(this.m_lineSep, 0, this.m_lineSepLen);
        return i2;
    }

    public void characters(String s) throws SAXException {
        if (!this.m_inEntityRef || this.m_expandDTDEntities) {
            int length = s.length();
            if (length > this.m_charsBuff.length) {
                this.m_charsBuff = new char[((length * 2) + 1)];
            }
            s.getChars(0, length, this.m_charsBuff, 0);
            characters(this.m_charsBuff, 0, length);
        }
    }

    private int accumDefaultEscape(Writer writer, char ch, int i, char[] chars, int len, boolean fromTextNode, boolean escLF) throws IOException {
        Writer writer2 = writer;
        char c = ch;
        int i2 = i;
        int pos = accumDefaultEntity(writer, ch, i, chars, len, fromTextNode, escLF);
        if (i2 != pos) {
            int i3 = len;
            return pos;
        } else if (!Encodings.isHighUTF16Surrogate(ch)) {
            int i4 = len;
            if (isCharacterInC0orC1Range(ch) || isNELorLSEPCharacter(ch)) {
                writer2.write("&#");
                writer2.write(Integer.toString(ch));
                writer2.write(59);
            } else if ((!escapingNotNeeded(c) || ((fromTextNode && this.m_charInfo.shouldMapTextChar(c)) || (!fromTextNode && this.m_charInfo.shouldMapAttrChar(c)))) && this.m_elemContext.m_currentElemDepth > 0) {
                writer2.write("&#");
                writer2.write(Integer.toString(ch));
                writer2.write(59);
            } else {
                writer.write(ch);
            }
            return pos + 1;
        } else if (i2 + 1 < len) {
            char next = chars[i2 + 1];
            if (Encodings.isLowUTF16Surrogate(next)) {
                int codePoint = Encodings.toCodePoint(c, next);
                writer2.write("&#");
                writer2.write(Integer.toString(codePoint));
                writer2.write(59);
                return pos + 2;
            }
            Messages messages = Utils.messages;
            throw new IOException(messages.createMessage("ER_INVALID_UTF16_SURROGATE", new Object[]{Integer.toHexString(ch) + " " + Integer.toHexString(next)}));
        } else {
            throw new IOException(Utils.messages.createMessage("ER_INVALID_UTF16_SURROGATE", new Object[]{Integer.toHexString(ch)}));
        }
    }

    public void startElement(String namespaceURI, String localName, String name, Attributes atts) throws SAXException {
        if (!this.m_inEntityRef) {
            if (this.m_needToCallStartDocument) {
                startDocumentInternal();
                this.m_needToCallStartDocument = false;
                this.m_docIsEmpty = false;
            } else if (this.m_cdataTagOpen) {
                closeCDATA();
            }
            try {
                if (this.m_needToOutputDocTypeDecl) {
                    if (getDoctypeSystem() != null) {
                        outputDocTypeDecl(name, true);
                    }
                    this.m_needToOutputDocTypeDecl = false;
                }
                if (this.m_elemContext.m_startTagOpen) {
                    closeStartTag();
                    this.m_elemContext.m_startTagOpen = false;
                }
                if (namespaceURI != null) {
                    ensurePrefixIsDeclared(namespaceURI, name);
                }
                this.m_ispreserve = false;
                if (shouldIndent() && this.m_startNewLine) {
                    indent();
                }
                this.m_startNewLine = true;
                Writer writer = this.m_writer;
                writer.write(60);
                writer.write(name);
                if (atts != null) {
                    addAttributes(atts);
                }
                this.m_elemContext = this.m_elemContext.push(namespaceURI, localName, name);
                this.m_isprevtext = false;
                if (this.m_tracer != null) {
                    firePseudoAttributes();
                }
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    public void startElement(String elementNamespaceURI, String elementLocalName, String elementName) throws SAXException {
        startElement(elementNamespaceURI, elementLocalName, elementName, null);
    }

    public void startElement(String elementName) throws SAXException {
        startElement(null, null, elementName, null);
    }

    /* access modifiers changed from: package-private */
    public void outputDocTypeDecl(String name, boolean closeDecl) throws SAXException {
        if (this.m_cdataTagOpen) {
            closeCDATA();
        }
        try {
            Writer writer = this.m_writer;
            writer.write("<!DOCTYPE ");
            writer.write(name);
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
                if (closeDecl) {
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

    public void processAttributes(Writer writer, int nAttrs) throws IOException, SAXException {
        String encoding = getEncoding();
        for (int i = 0; i < nAttrs; i++) {
            String name = this.m_attributes.getQName(i);
            String value = this.m_attributes.getValue(i);
            writer.write(32);
            writer.write(name);
            writer.write("=\"");
            writeAttrString(writer, value, encoding);
            writer.write(34);
        }
    }

    public void writeAttrString(Writer writer, String string, String encoding) throws IOException {
        int len = string.length();
        if (len > this.m_attrBuff.length) {
            this.m_attrBuff = new char[((len * 2) + 1)];
        }
        int i = 0;
        string.getChars(0, len, this.m_attrBuff, 0);
        char[] stringChars = this.m_attrBuff;
        while (true) {
            int i2 = i;
            if (i2 < len) {
                char ch = stringChars[i2];
                if (this.m_charInfo.shouldMapAttrChar(ch)) {
                    accumDefaultEscape(writer, ch, i2, stringChars, len, false, true);
                } else if (ch < 0 || ch > 31) {
                    if (ch < 127) {
                        writer.write(ch);
                    } else if (ch <= 159) {
                        writer.write("&#");
                        writer.write(Integer.toString(ch));
                        writer.write(59);
                    } else if (ch == 8232) {
                        writer.write("&#8232;");
                    } else if (this.m_encodingInfo.isInEncoding(ch)) {
                        writer.write(ch);
                    } else {
                        writer.write("&#");
                        writer.write(Integer.toString(ch));
                        writer.write(59);
                    }
                } else if (ch != 13) {
                    switch (ch) {
                        case 9:
                            writer.write("&#9;");
                            break;
                        case 10:
                            writer.write("&#10;");
                            break;
                        default:
                            writer.write("&#");
                            writer.write(Integer.toString(ch));
                            writer.write(59);
                            break;
                    }
                } else {
                    writer.write("&#13;");
                }
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    public void endElement(String namespaceURI, String localName, String name) throws SAXException {
        if (!this.m_inEntityRef) {
            this.m_prefixMap.popNamespaces(this.m_elemContext.m_currentElemDepth, null);
            try {
                Writer writer = this.m_writer;
                if (this.m_elemContext.m_startTagOpen) {
                    if (this.m_tracer != null) {
                        super.fireStartElem(this.m_elemContext.m_elementName);
                    }
                    int nAttrs = this.m_attributes.getLength();
                    if (nAttrs > 0) {
                        processAttributes(this.m_writer, nAttrs);
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
                    writer.write(name);
                    writer.write(62);
                }
                if (!this.m_elemContext.m_startTagOpen && this.m_doIndent) {
                    this.m_ispreserve = this.m_preserves.isEmpty() ? false : this.m_preserves.pop();
                }
                this.m_isprevtext = false;
                if (this.m_tracer != null) {
                    super.fireEndElem(name);
                }
                this.m_elemContext = this.m_elemContext.m_prev;
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }

    public void endElement(String name) throws SAXException {
        endElement(null, null, name);
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        startPrefixMapping(prefix, uri, true);
    }

    public boolean startPrefixMapping(String prefix, String uri, boolean shouldFlush) throws SAXException {
        int pushDepth;
        if (shouldFlush) {
            flushPending();
            pushDepth = this.m_elemContext.m_currentElemDepth + 1;
        } else {
            pushDepth = this.m_elemContext.m_currentElemDepth;
        }
        boolean pushed = this.m_prefixMap.pushNamespace(prefix, uri, pushDepth);
        if (pushed) {
            if ("".equals(prefix)) {
                addAttributeAlways(SerializerConstants.XMLNS_URI, "xmlns", "xmlns", "CDATA", uri, false);
            } else if (!"".equals(uri)) {
                addAttributeAlways(SerializerConstants.XMLNS_URI, prefix, Constants.ATTRNAME_XMLNS + prefix, "CDATA", uri, false);
            }
        }
        return pushed;
    }

    public void comment(char[] ch, int i, int length) throws SAXException {
        int start_old = i;
        if (!this.m_inEntityRef) {
            if (this.m_elemContext.m_startTagOpen) {
                closeStartTag();
                this.m_elemContext.m_startTagOpen = false;
            } else if (this.m_needToCallStartDocument) {
                startDocumentInternal();
                this.m_needToCallStartDocument = false;
            }
            int limit = i + length;
            boolean wasDash = false;
            try {
                if (this.m_cdataTagOpen) {
                    closeCDATA();
                }
                if (shouldIndent()) {
                    indent();
                }
                Writer writer = this.m_writer;
                writer.write(COMMENT_BEGIN);
                int start = i;
                while (true) {
                    boolean z = true;
                    if (i >= limit) {
                        break;
                    }
                    if (wasDash) {
                        try {
                            if (ch[i] == '-') {
                                writer.write(ch, start, i - start);
                                writer.write(" -");
                                start = i + 1;
                            }
                        } catch (IOException e) {
                            e = e;
                            throw new SAXException(e);
                        }
                    }
                    if (ch[i] != '-') {
                        z = false;
                    }
                    wasDash = z;
                    i++;
                }
                if (length > 0) {
                    int remainingChars = limit - start;
                    if (remainingChars > 0) {
                        writer.write(ch, start, remainingChars);
                    }
                    if (ch[limit - 1] == '-') {
                        writer.write(32);
                    }
                }
                writer.write(COMMENT_END);
                this.m_startNewLine = true;
                if (this.m_tracer != null) {
                    super.fireCommentEvent(ch, start_old, length);
                }
            } catch (IOException e2) {
                e = e2;
                int i2 = i;
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
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        if (length != 0) {
            characters(ch, start, length);
        }
    }

    public void skippedEntity(String name) throws SAXException {
    }

    public void startCDATA() throws SAXException {
        this.m_cdataStartCalled = true;
    }

    public void startEntity(String name) throws SAXException {
        if (name.equals("[dtd]")) {
            this.m_inExternalDTD = true;
        }
        if (!this.m_expandDTDEntities && !this.m_inExternalDTD) {
            startNonEscaping();
            characters("&" + name + ';');
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
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        setDoctypeSystem(systemId);
        setDoctypePublic(publicId);
        this.m_elemContext.m_elementName = name;
        this.m_inDoctype = true;
    }

    public int getIndentAmount() {
        return this.m_indentAmount;
    }

    public void setIndentAmount(int m_indentAmount) {
        this.m_indentAmount = m_indentAmount;
    }

    /* access modifiers changed from: protected */
    public boolean shouldIndent() {
        return this.m_doIndent && !this.m_ispreserve && !this.m_isprevtext && this.m_elemContext.m_currentElemDepth > 0;
    }

    private void setCdataSectionElements(String key, Properties props) {
        String s = props.getProperty(key);
        if (s != null) {
            Vector v = new Vector();
            int l = s.length();
            StringBuffer buf = new StringBuffer();
            boolean inCurly = false;
            for (int i = 0; i < l; i++) {
                char c = s.charAt(i);
                if (Character.isWhitespace(c)) {
                    if (!inCurly) {
                        if (buf.length() > 0) {
                            addCdataSectionElement(buf.toString(), v);
                            buf.setLength(0);
                        }
                    }
                } else if ('{' == c) {
                    inCurly = true;
                } else if ('}' == c) {
                    inCurly = false;
                }
                buf.append(c);
            }
            if (buf.length() > 0) {
                addCdataSectionElement(buf.toString(), v);
                buf.setLength(0);
            }
            setCdataSectionElements(v);
        }
    }

    private void addCdataSectionElement(String URI_and_localName, Vector v) {
        StringTokenizer tokenizer = new StringTokenizer(URI_and_localName, "{}", false);
        String s1 = tokenizer.nextToken();
        String s2 = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
        if (s2 == null) {
            v.addElement(null);
            v.addElement(s1);
            return;
        }
        v.addElement(s1);
        v.addElement(s2);
    }

    public void setCdataSectionElements(Vector URI_and_localNames) {
        if (URI_and_localNames != null) {
            int len = URI_and_localNames.size() - 1;
            if (len > 0) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < len; i += 2) {
                    if (i != 0) {
                        sb.append(' ');
                    }
                    String uri = (String) URI_and_localNames.elementAt(i);
                    String localName = (String) URI_and_localNames.elementAt(i + 1);
                    if (uri != null) {
                        sb.append('{');
                        sb.append(uri);
                        sb.append('}');
                    }
                    sb.append(localName);
                }
                this.m_StringOfCDATASections = sb.toString();
            }
        }
        initCdataElems(this.m_StringOfCDATASections);
    }

    /* access modifiers changed from: protected */
    public String ensureAttributesNamespaceIsDeclared(String ns, String localName, String rawName) throws SAXException {
        String prefixFromRawName;
        if (ns == null || ns.length() <= 0) {
            return null;
        }
        int indexOf = rawName.indexOf(":");
        int index = indexOf;
        if (indexOf < 0) {
            prefixFromRawName = "";
        } else {
            prefixFromRawName = rawName.substring(0, index);
        }
        if (index > 0) {
            String uri = this.m_prefixMap.lookupNamespace(prefixFromRawName);
            if (uri != null && uri.equals(ns)) {
                return null;
            }
            startPrefixMapping(prefixFromRawName, ns, false);
            addAttribute(SerializerConstants.XMLNS_URI, prefixFromRawName, Constants.ATTRNAME_XMLNS + prefixFromRawName, "CDATA", ns, false);
            return prefixFromRawName;
        }
        String prefix = this.m_prefixMap.lookupPrefix(ns);
        if (prefix == null) {
            prefix = this.m_prefixMap.generateNextPrefix();
            startPrefixMapping(prefix, ns, false);
            addAttribute(SerializerConstants.XMLNS_URI, prefix, Constants.ATTRNAME_XMLNS + prefix, "CDATA", ns, false);
        }
        return prefix;
    }

    /* access modifiers changed from: package-private */
    public void ensurePrefixIsDeclared(String ns, String rawName) throws SAXException {
        String str;
        String str2;
        if (ns != null && ns.length() > 0) {
            int indexOf = rawName.indexOf(":");
            int index = indexOf;
            boolean no_prefix = indexOf < 0;
            String prefix = no_prefix ? "" : rawName.substring(0, index);
            if (prefix != null) {
                String foundURI = this.m_prefixMap.lookupNamespace(prefix);
                if (foundURI == null || !foundURI.equals(ns)) {
                    startPrefixMapping(prefix, ns);
                    if (no_prefix) {
                        str = "xmlns";
                    } else {
                        str = prefix;
                    }
                    if (no_prefix) {
                        str2 = "xmlns";
                    } else {
                        str2 = Constants.ATTRNAME_XMLNS + prefix;
                    }
                    addAttributeAlways(SerializerConstants.XMLNS_URI, str, str2, "CDATA", ns, false);
                }
            }
        }
    }

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
        if (this.m_writer != null) {
            try {
                this.m_writer.flush();
            } catch (IOException e) {
            }
        }
    }

    public void setContentHandler(ContentHandler ch) {
    }

    public boolean addAttributeAlways(String uri, String localName, String rawName, String type, String value, boolean xslAttribute) {
        int index;
        boolean was_added;
        if (uri == null || localName == null || uri.length() == 0) {
            index = this.m_attributes.getIndex(rawName);
        } else {
            index = this.m_attributes.getIndex(uri, localName);
        }
        if (index >= 0) {
            String old_value = null;
            if (this.m_tracer != null) {
                old_value = this.m_attributes.getValue(index);
                if (value.equals(old_value)) {
                    old_value = null;
                }
            }
            this.m_attributes.setValue(index, value);
            was_added = false;
            if (old_value != null) {
                firePseudoAttributes();
            }
        } else {
            if (xslAttribute) {
                int colonIndex = rawName.indexOf(58);
                if (colonIndex > 0) {
                    NamespaceMappings.MappingRecord existing_mapping = this.m_prefixMap.getMappingFromPrefix(rawName.substring(0, colonIndex));
                    if (existing_mapping != null && existing_mapping.m_declarationDepth == this.m_elemContext.m_currentElemDepth && !existing_mapping.m_uri.equals(uri)) {
                        String prefix = this.m_prefixMap.lookupPrefix(uri);
                        if (prefix == null) {
                            prefix = this.m_prefixMap.generateNextPrefix();
                        }
                        rawName = prefix + ':' + localName;
                    }
                }
                try {
                    ensureAttributesNamespaceIsDeclared(uri, localName, rawName);
                } catch (SAXException e) {
                    e.printStackTrace();
                }
            }
            this.m_attributes.addAttribute(uri, localName, rawName, type, value);
            was_added = true;
            if (this.m_tracer != null) {
                firePseudoAttributes();
            }
        }
        return was_added;
    }

    /* access modifiers changed from: protected */
    public void firePseudoAttributes() {
        if (this.m_tracer != null) {
            try {
                this.m_writer.flush();
                StringBuffer sb = new StringBuffer();
                int nAttrs = this.m_attributes.getLength();
                if (nAttrs > 0) {
                    processAttributes(new WritertoStringBuffer(sb), nAttrs);
                }
                sb.append('>');
                char[] ch = sb.toString().toCharArray();
                this.m_tracer.fireGenerateEvent(11, ch, 0, ch.length);
            } catch (IOException | SAXException e) {
            }
        }
    }

    public void setTransformer(Transformer transformer) {
        super.setTransformer(transformer);
        if (this.m_tracer != null && !(this.m_writer instanceof SerializerTraceWriter)) {
            setWriterInternal(new SerializerTraceWriter(this.m_writer, this.m_tracer), false);
        }
    }

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
        this.m_expandDTDEntities = true;
        this.m_inDoctype = false;
        this.m_ispreserve = false;
        this.m_isprevtext = false;
        this.m_isUTF8 = false;
        this.m_lineSep = s_systemLineSep;
        this.m_lineSepLen = s_systemLineSep.length;
        this.m_lineSepUse = true;
        this.m_preserves.clear();
        this.m_shouldFlush = true;
        this.m_spaceBeforeClose = false;
        this.m_startNewLine = false;
        this.m_writer_set_by_user = false;
    }

    public void setEncoding(String encoding) {
        setOutputProperty("encoding", encoding);
    }

    public void notationDecl(String name, String pubID, String sysID) throws SAXException {
        try {
            DTDprolog();
            this.m_writer.write("<!NOTATION ");
            this.m_writer.write(name);
            if (pubID != null) {
                this.m_writer.write(" PUBLIC \"");
                this.m_writer.write(pubID);
            } else {
                this.m_writer.write(" SYSTEM \"");
                this.m_writer.write(sysID);
            }
            this.m_writer.write("\" >");
            this.m_writer.write(this.m_lineSep, 0, this.m_lineSepLen);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unparsedEntityDecl(String name, String pubID, String sysID, String notationName) throws SAXException {
        try {
            DTDprolog();
            this.m_writer.write("<!ENTITY ");
            this.m_writer.write(name);
            if (pubID != null) {
                this.m_writer.write(" PUBLIC \"");
                this.m_writer.write(pubID);
            } else {
                this.m_writer.write(" SYSTEM \"");
                this.m_writer.write(sysID);
            }
            this.m_writer.write("\" NDATA ");
            this.m_writer.write(notationName);
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

    public void setDTDEntityExpansion(boolean expand) {
        this.m_expandDTDEntities = expand;
    }

    public void setNewLine(char[] eolChars) {
        this.m_lineSep = eolChars;
        this.m_lineSepLen = eolChars.length;
    }

    public void addCdataSectionElements(String URI_and_localNames) {
        if (URI_and_localNames != null) {
            initCdataElems(URI_and_localNames);
        }
        if (this.m_StringOfCDATASections == null) {
            this.m_StringOfCDATASections = URI_and_localNames;
            return;
        }
        this.m_StringOfCDATASections += " " + URI_and_localNames;
    }
}
