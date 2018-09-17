package org.ccil.cowan.tagsoup;

import gov.nist.core.Separators;
import gov.nist.javax.sip.header.ims.AuthorizationHeaderIms;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.NamespaceSupport;
import org.xml.sax.helpers.XMLFilterImpl;

public class XMLWriter extends XMLFilterImpl implements LexicalHandler {
    public static final String CDATA_SECTION_ELEMENTS = "cdata-section-elements";
    public static final String DOCTYPE_PUBLIC = "doctype-public";
    public static final String DOCTYPE_SYSTEM = "doctype-system";
    public static final String ENCODING = "encoding";
    public static final String INDENT = "indent";
    public static final String MEDIA_TYPE = "media-type";
    public static final String METHOD = "method";
    public static final String OMIT_XML_DECLARATION = "omit-xml-declaration";
    public static final String STANDALONE = "standalone";
    public static final String VERSION = "version";
    private final Attributes EMPTY_ATTS;
    private String[] booleans;
    private boolean cdataElement;
    private Hashtable doneDeclTable;
    private int elementLevel;
    private boolean forceDTD;
    private Hashtable forcedDeclTable;
    private boolean hasOutputDTD;
    private boolean htmlMode;
    private NamespaceSupport nsSupport;
    private Writer output;
    private String outputEncoding;
    private Properties outputProperties;
    private String overridePublic;
    private String overrideSystem;
    private int prefixCounter;
    private Hashtable prefixTable;
    private String standalone;
    private boolean unicodeMode;
    private String version;

    public XMLWriter() {
        this.booleans = new String[]{"checked", "compact", "declare", "defer", "disabled", "ismap", "multiple", "nohref", "noresize", "noshade", "nowrap", "readonly", "selected"};
        this.EMPTY_ATTS = new AttributesImpl();
        this.elementLevel = 0;
        this.prefixCounter = 0;
        this.unicodeMode = false;
        this.outputEncoding = "";
        this.htmlMode = false;
        this.forceDTD = false;
        this.hasOutputDTD = false;
        this.overridePublic = null;
        this.overrideSystem = null;
        this.version = null;
        this.standalone = null;
        this.cdataElement = false;
        init(null);
    }

    public XMLWriter(Writer writer) {
        this.booleans = new String[]{"checked", "compact", "declare", "defer", "disabled", "ismap", "multiple", "nohref", "noresize", "noshade", "nowrap", "readonly", "selected"};
        this.EMPTY_ATTS = new AttributesImpl();
        this.elementLevel = 0;
        this.prefixCounter = 0;
        this.unicodeMode = false;
        this.outputEncoding = "";
        this.htmlMode = false;
        this.forceDTD = false;
        this.hasOutputDTD = false;
        this.overridePublic = null;
        this.overrideSystem = null;
        this.version = null;
        this.standalone = null;
        this.cdataElement = false;
        init(writer);
    }

    public XMLWriter(XMLReader xmlreader) {
        super(xmlreader);
        this.booleans = new String[]{"checked", "compact", "declare", "defer", "disabled", "ismap", "multiple", "nohref", "noresize", "noshade", "nowrap", "readonly", "selected"};
        this.EMPTY_ATTS = new AttributesImpl();
        this.elementLevel = 0;
        this.prefixCounter = 0;
        this.unicodeMode = false;
        this.outputEncoding = "";
        this.htmlMode = false;
        this.forceDTD = false;
        this.hasOutputDTD = false;
        this.overridePublic = null;
        this.overrideSystem = null;
        this.version = null;
        this.standalone = null;
        this.cdataElement = false;
        init(null);
    }

    public XMLWriter(XMLReader xmlreader, Writer writer) {
        super(xmlreader);
        this.booleans = new String[]{"checked", "compact", "declare", "defer", "disabled", "ismap", "multiple", "nohref", "noresize", "noshade", "nowrap", "readonly", "selected"};
        this.EMPTY_ATTS = new AttributesImpl();
        this.elementLevel = 0;
        this.prefixCounter = 0;
        this.unicodeMode = false;
        this.outputEncoding = "";
        this.htmlMode = false;
        this.forceDTD = false;
        this.hasOutputDTD = false;
        this.overridePublic = null;
        this.overrideSystem = null;
        this.version = null;
        this.standalone = null;
        this.cdataElement = false;
        init(writer);
    }

    private void init(Writer writer) {
        setOutput(writer);
        this.nsSupport = new NamespaceSupport();
        this.prefixTable = new Hashtable();
        this.forcedDeclTable = new Hashtable();
        this.doneDeclTable = new Hashtable();
        this.outputProperties = new Properties();
    }

    public void reset() {
        this.elementLevel = 0;
        this.prefixCounter = 0;
        this.nsSupport.reset();
    }

    public void flush() throws IOException {
        this.output.flush();
    }

    public void setOutput(Writer writer) {
        if (writer == null) {
            this.output = new OutputStreamWriter(System.out);
        } else {
            this.output = writer;
        }
    }

    public void setPrefix(String uri, String prefix) {
        this.prefixTable.put(uri, prefix);
    }

    public String getPrefix(String uri) {
        return (String) this.prefixTable.get(uri);
    }

    public void forceNSDecl(String uri) {
        this.forcedDeclTable.put(uri, Boolean.TRUE);
    }

    public void forceNSDecl(String uri, String prefix) {
        setPrefix(uri, prefix);
        forceNSDecl(uri);
    }

    public void startDocument() throws SAXException {
        reset();
        if (!AuthorizationHeaderIms.YES.equals(this.outputProperties.getProperty(OMIT_XML_DECLARATION, AuthorizationHeaderIms.NO))) {
            write("<?xml");
            if (this.version == null) {
                write(" version=\"1.0\"");
            } else {
                write(" version=\"");
                write(this.version);
                write(Separators.DOUBLE_QUOTE);
            }
            if (!(this.outputEncoding == null || this.outputEncoding == "")) {
                write(" encoding=\"");
                write(this.outputEncoding);
                write(Separators.DOUBLE_QUOTE);
            }
            if (this.standalone == null) {
                write(" standalone=\"yes\"?>\n");
            } else {
                write(" standalone=\"");
                write(this.standalone);
                write(Separators.DOUBLE_QUOTE);
            }
        }
        super.startDocument();
    }

    public void endDocument() throws SAXException {
        write(10);
        super.endDocument();
        try {
            flush();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        this.elementLevel++;
        this.nsSupport.pushContext();
        if (this.forceDTD && (this.hasOutputDTD ^ 1) != 0) {
            startDTD(localName == null ? qName : localName, "", "");
        }
        write('<');
        writeName(uri, localName, qName, true);
        writeAttributes(atts);
        if (this.elementLevel == 1) {
            forceNSDecls();
        }
        writeNSDecls();
        write('>');
        if (this.htmlMode && (qName.equals("script") || qName.equals("style"))) {
            this.cdataElement = true;
        }
        super.startElement(uri, localName, qName, atts);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        boolean equals = (this.htmlMode && (uri.equals("http://www.w3.org/1999/xhtml") || uri.equals(""))) ? (qName.equals("area") || qName.equals("base") || qName.equals("basefont") || qName.equals("br") || qName.equals("col") || qName.equals("frame") || qName.equals("hr") || qName.equals("img") || qName.equals("input") || qName.equals("isindex") || qName.equals("link") || qName.equals("meta")) ? true : qName.equals("param") : false;
        if (!equals) {
            write("</");
            writeName(uri, localName, qName, true);
            write('>');
        }
        if (this.elementLevel == 1) {
            write(10);
        }
        this.cdataElement = false;
        super.endElement(uri, localName, qName);
        this.nsSupport.popContext();
        this.elementLevel--;
    }

    public void characters(char[] ch, int start, int len) throws SAXException {
        if (this.cdataElement) {
            for (int i = start; i < start + len; i++) {
                write(ch[i]);
            }
        } else {
            writeEsc(ch, start, len, false);
        }
        super.characters(ch, start, len);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        writeEsc(ch, start, length, false);
        super.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        write("<?");
        write(target);
        write(' ');
        write(data);
        write("?>");
        if (this.elementLevel < 1) {
            write(10);
        }
        super.processingInstruction(target, data);
    }

    public void emptyElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        this.nsSupport.pushContext();
        write('<');
        writeName(uri, localName, qName, true);
        writeAttributes(atts);
        if (this.elementLevel == 1) {
            forceNSDecls();
        }
        writeNSDecls();
        write("/>");
        super.startElement(uri, localName, qName, atts);
        super.endElement(uri, localName, qName);
    }

    public void startElement(String uri, String localName) throws SAXException {
        startElement(uri, localName, "", this.EMPTY_ATTS);
    }

    public void startElement(String localName) throws SAXException {
        startElement("", localName, "", this.EMPTY_ATTS);
    }

    public void endElement(String uri, String localName) throws SAXException {
        endElement(uri, localName, "");
    }

    public void endElement(String localName) throws SAXException {
        endElement("", localName, "");
    }

    public void emptyElement(String uri, String localName) throws SAXException {
        emptyElement(uri, localName, "", this.EMPTY_ATTS);
    }

    public void emptyElement(String localName) throws SAXException {
        emptyElement("", localName, "", this.EMPTY_ATTS);
    }

    public void dataElement(String uri, String localName, String qName, Attributes atts, String content) throws SAXException {
        startElement(uri, localName, qName, atts);
        characters(content);
        endElement(uri, localName, qName);
    }

    public void dataElement(String uri, String localName, String content) throws SAXException {
        dataElement(uri, localName, "", this.EMPTY_ATTS, content);
    }

    public void dataElement(String localName, String content) throws SAXException {
        dataElement("", localName, "", this.EMPTY_ATTS, content);
    }

    public void characters(String data) throws SAXException {
        char[] ch = data.toCharArray();
        characters(ch, 0, ch.length);
    }

    private void forceNSDecls() {
        Enumeration prefixes = this.forcedDeclTable.keys();
        while (prefixes.hasMoreElements()) {
            doPrefix((String) prefixes.nextElement(), null, true);
        }
    }

    private String doPrefix(String uri, String qName, boolean isElement) {
        String defaultNS = this.nsSupport.getURI("");
        if ("".equals(uri)) {
            if (isElement && defaultNS != null) {
                this.nsSupport.declarePrefix("", "");
            }
            return null;
        }
        String prefix;
        if (isElement && defaultNS != null && uri.equals(defaultNS)) {
            prefix = "";
        } else {
            prefix = this.nsSupport.getPrefix(uri);
        }
        if (prefix != null) {
            return prefix;
        }
        prefix = (String) this.doneDeclTable.get(uri);
        if (prefix != null && ((!(isElement && defaultNS == null) && "".equals(prefix)) || this.nsSupport.getURI(prefix) != null)) {
            prefix = null;
        }
        if (prefix == null) {
            prefix = (String) this.prefixTable.get(uri);
            if (prefix != null && ((!(isElement && defaultNS == null) && "".equals(prefix)) || this.nsSupport.getURI(prefix) != null)) {
                prefix = null;
            }
        }
        if (!(prefix != null || qName == null || ("".equals(qName) ^ 1) == 0)) {
            int i = qName.indexOf(58);
            if (i != -1) {
                prefix = qName.substring(0, i);
            } else if (isElement && defaultNS == null) {
                prefix = "";
            }
        }
        while (true) {
            if (prefix == null || this.nsSupport.getURI(prefix) != null) {
                StringBuilder append = new StringBuilder().append("__NS");
                int i2 = this.prefixCounter + 1;
                this.prefixCounter = i2;
                prefix = append.append(i2).toString();
            } else {
                this.nsSupport.declarePrefix(prefix, uri);
                this.doneDeclTable.put(uri, prefix);
                return prefix;
            }
        }
    }

    private void write(char c) throws SAXException {
        try {
            this.output.write(c);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    private void write(String s) throws SAXException {
        try {
            this.output.write(s);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    private void writeAttributes(Attributes atts) throws SAXException {
        int len = atts.getLength();
        int i = 0;
        while (i < len) {
            char[] ch = atts.getValue(i).toCharArray();
            write(' ');
            writeName(atts.getURI(i), atts.getLocalName(i), atts.getQName(i), false);
            if (!this.htmlMode || !booleanAttribute(atts.getLocalName(i), atts.getQName(i), atts.getValue(i))) {
                write("=\"");
                writeEsc(ch, 0, ch.length, true);
                write('\"');
                i++;
            } else {
                return;
            }
        }
    }

    private boolean booleanAttribute(String localName, String qName, String value) {
        String name = localName;
        if (localName == null) {
            int i = qName.indexOf(58);
            if (i != -1) {
                name = qName.substring(i + 1, qName.length());
            }
        }
        if (!name.equals(value)) {
            return false;
        }
        for (Object equals : this.booleans) {
            if (name.equals(equals)) {
                return true;
            }
        }
        return false;
    }

    private void writeEsc(char[] ch, int start, int length, boolean isAttVal) throws SAXException {
        int i = start;
        while (i < start + length) {
            switch (ch[i]) {
                case '\"':
                    if (!isAttVal) {
                        write('\"');
                        break;
                    } else {
                        write("&quot;");
                        break;
                    }
                case '&':
                    write("&amp;");
                    break;
                case '<':
                    write("&lt;");
                    break;
                case '>':
                    write("&gt;");
                    break;
                default:
                    if (!this.unicodeMode && ch[i] > 127) {
                        write("&#");
                        write(Integer.toString(ch[i]));
                        write(';');
                        break;
                    }
                    write(ch[i]);
                    break;
            }
            i++;
        }
    }

    private void writeNSDecls() throws SAXException {
        Enumeration prefixes = this.nsSupport.getDeclaredPrefixes();
        while (prefixes.hasMoreElements()) {
            String prefix = (String) prefixes.nextElement();
            String uri = this.nsSupport.getURI(prefix);
            if (uri == null) {
                uri = "";
            }
            char[] ch = uri.toCharArray();
            write(' ');
            if ("".equals(prefix)) {
                write("xmlns=\"");
            } else {
                write("xmlns:");
                write(prefix);
                write("=\"");
            }
            writeEsc(ch, 0, ch.length, true);
            write('\"');
        }
    }

    private void writeName(String uri, String localName, String qName, boolean isElement) throws SAXException {
        String prefix = doPrefix(uri, qName, isElement);
        if (!(prefix == null || ("".equals(prefix) ^ 1) == 0)) {
            write(prefix);
            write(':');
        }
        if (localName == null || ("".equals(localName) ^ 1) == 0) {
            write(qName.substring(qName.indexOf(58) + 1, qName.length()));
        } else {
            write(localName);
        }
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        write("<!--");
        int i = start;
        while (i < start + length) {
            write(ch[i]);
            if (ch[i] == '-' && i + 1 <= start + length && ch[i + 1] == '-') {
                write(' ');
            }
            i++;
        }
        write("-->");
    }

    public void endCDATA() throws SAXException {
    }

    public void endDTD() throws SAXException {
    }

    public void endEntity(String name) throws SAXException {
    }

    public void startCDATA() throws SAXException {
    }

    public void startDTD(String name, String publicid, String systemid) throws SAXException {
        boolean z = true;
        if (name != null && !this.hasOutputDTD) {
            this.hasOutputDTD = true;
            write("<!DOCTYPE ");
            write(name);
            if (systemid == null) {
                systemid = "";
            }
            if (this.overrideSystem != null) {
                systemid = this.overrideSystem;
            }
            char sysquote = systemid.indexOf(34) != -1 ? '\'' : '\"';
            if (this.overridePublic != null) {
                publicid = this.overridePublic;
            }
            if (publicid != null) {
                z = "".equals(publicid);
            }
            if (z) {
                write(" SYSTEM ");
            } else {
                char pubquote = publicid.indexOf(34) != -1 ? '\'' : '\"';
                write(" PUBLIC ");
                write(pubquote);
                write(publicid);
                write(pubquote);
                write(' ');
            }
            write(sysquote);
            write(systemid);
            write(sysquote);
            write(">\n");
        }
    }

    public void startEntity(String name) throws SAXException {
    }

    public String getOutputProperty(String key) {
        return this.outputProperties.getProperty(key);
    }

    public void setOutputProperty(String key, String value) {
        this.outputProperties.setProperty(key, value);
        if (key.equals(ENCODING)) {
            this.outputEncoding = value;
            this.unicodeMode = value.substring(0, 3).equalsIgnoreCase("utf");
        } else if (key.equals(METHOD)) {
            this.htmlMode = value.equals("html");
        } else if (key.equals(DOCTYPE_PUBLIC)) {
            this.overridePublic = value;
            this.forceDTD = true;
        } else if (key.equals(DOCTYPE_SYSTEM)) {
            this.overrideSystem = value;
            this.forceDTD = true;
        } else if (key.equals("version")) {
            this.version = value;
        } else if (key.equals(STANDALONE)) {
            this.standalone = value;
        }
    }
}
