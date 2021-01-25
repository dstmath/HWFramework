package ohos.com.sun.xml.internal.stream.writers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializerConstants;

public class WriterUtility {
    public static final String CLOSE_EMPTY_ELEMENT = "/>";
    public static final char CLOSE_END_TAG = '>';
    public static final char CLOSE_START_TAG = '>';
    static final boolean DEBUG_XML_CONTENT = false;
    public static final String DEFAULT_ENCODING = " encoding=\"utf-8\"";
    public static final String DEFAULT_XMLDECL = "<?xml version=\"1.0\" ?>";
    public static final String DEFAULT_XML_VERSION = "1.0";
    public static final String END_CDATA = "]]>";
    public static final String END_COMMENT = "-->";
    public static final String OPEN_END_TAG = "</";
    public static final char OPEN_START_TAG = '<';
    public static final String SPACE = " ";
    public static final String START_CDATA = "<![CDATA[";
    public static final String START_COMMENT = "<!--";
    public static final String UTF_8 = "utf-8";
    CharsetEncoder fEncoder;
    boolean fEscapeCharacters;
    Writer fWriter;

    public WriterUtility() {
        this.fEscapeCharacters = true;
        this.fWriter = null;
        this.fEncoder = getDefaultEncoder();
    }

    public WriterUtility(Writer writer) {
        this.fEscapeCharacters = true;
        this.fWriter = null;
        this.fWriter = writer;
        if (writer instanceof OutputStreamWriter) {
            String encoding = ((OutputStreamWriter) writer).getEncoding();
            if (encoding != null) {
                this.fEncoder = Charset.forName(encoding).newEncoder();
            }
        } else if (writer instanceof FileWriter) {
            String encoding2 = ((FileWriter) writer).getEncoding();
            if (encoding2 != null) {
                this.fEncoder = Charset.forName(encoding2).newEncoder();
            }
        } else {
            this.fEncoder = getDefaultEncoder();
        }
    }

    public void setWriter(Writer writer) {
        this.fWriter = writer;
    }

    public void setEscapeCharacters(boolean z) {
        this.fEscapeCharacters = z;
    }

    public boolean getEscapeCharacters() {
        return this.fEscapeCharacters;
    }

    public void writeXMLContent(char[] cArr, int i, int i2) throws IOException {
        writeXMLContent(cArr, i, i2, getEscapeCharacters());
    }

    private void writeXMLContent(char[] cArr, int i, int i2, boolean z) throws IOException {
        int i3 = i2 + i;
        int i4 = i;
        while (i < i3) {
            char c = cArr[i];
            CharsetEncoder charsetEncoder = this.fEncoder;
            if (charsetEncoder != null && !charsetEncoder.canEncode(c)) {
                this.fWriter.write(cArr, i4, i - i4);
                this.fWriter.write("&#x");
                this.fWriter.write(Integer.toHexString(c));
                this.fWriter.write(59);
                i4 = i + 1;
            }
            if (c != '&') {
                if (c != '<') {
                    if (c == '>' && z) {
                        this.fWriter.write(cArr, i4, i - i4);
                        this.fWriter.write(SerializerConstants.ENTITY_GT);
                    }
                    i++;
                } else if (z) {
                    this.fWriter.write(cArr, i4, i - i4);
                    this.fWriter.write(SerializerConstants.ENTITY_LT);
                } else {
                    i++;
                }
            } else if (z) {
                this.fWriter.write(cArr, i4, i - i4);
                this.fWriter.write(SerializerConstants.ENTITY_AMP);
            } else {
                i++;
            }
            i4 = i + 1;
            i++;
        }
        this.fWriter.write(cArr, i4, i3 - i4);
    }

    public void writeXMLContent(String str) throws IOException {
        if (str != null && str.length() != 0) {
            writeXMLContent(str.toCharArray(), 0, str.length());
        }
    }

    public void writeXMLAttributeValue(String str) throws IOException {
        writeXMLContent(str.toCharArray(), 0, str.length(), true);
    }

    private CharsetEncoder getDefaultEncoder() {
        try {
            String systemProperty = SecuritySupport.getSystemProperty("file.encoding");
            if (systemProperty != null) {
                return Charset.forName(systemProperty).newEncoder();
            }
            return null;
        } catch (Exception unused) {
            return null;
        }
    }
}
