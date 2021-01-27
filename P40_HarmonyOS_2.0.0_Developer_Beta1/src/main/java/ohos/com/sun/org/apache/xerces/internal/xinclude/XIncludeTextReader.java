package ohos.com.sun.org.apache.xerces.internal.xinclude;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.impl.io.ASCIIReader;
import ohos.com.sun.org.apache.xerces.internal.impl.io.UTF8Reader;
import ohos.com.sun.org.apache.xerces.internal.util.EncodingMap;
import ohos.com.sun.org.apache.xerces.internal.util.HTTPInputSource;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

public class XIncludeTextReader {
    private XMLErrorReporter fErrorReporter;
    private XIncludeHandler fHandler;
    private Reader fReader;
    private XMLInputSource fSource;
    private XMLString fTempString = new XMLString();

    public XIncludeTextReader(XMLInputSource xMLInputSource, XIncludeHandler xIncludeHandler, int i) throws IOException {
        this.fHandler = xIncludeHandler;
        this.fSource = xMLInputSource;
        this.fTempString = new XMLString(new char[(i + 1)], 0, 0);
    }

    public void setErrorReporter(XMLErrorReporter xMLErrorReporter) {
        this.fErrorReporter = xMLErrorReporter;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00f8  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00fe  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x011e  */
    public Reader getReader(XMLInputSource xMLInputSource) throws IOException {
        InputStream inputStream;
        String str;
        String str2;
        if (xMLInputSource.getCharacterStream() != null) {
            return xMLInputSource.getCharacterStream();
        }
        String encoding = xMLInputSource.getEncoding();
        if (encoding == null) {
            encoding = "UTF-8";
        }
        if (xMLInputSource.getByteStream() != null) {
            inputStream = xMLInputSource.getByteStream();
            if (!(inputStream instanceof BufferedInputStream)) {
                inputStream = new BufferedInputStream(inputStream, this.fTempString.ch.length);
            }
        } else {
            URLConnection openConnection = new URL(XMLEntityManager.expandSystemId(xMLInputSource.getSystemId(), xMLInputSource.getBaseSystemId(), false)).openConnection();
            if ((openConnection instanceof HttpURLConnection) && (xMLInputSource instanceof HTTPInputSource)) {
                HttpURLConnection httpURLConnection = (HttpURLConnection) openConnection;
                HTTPInputSource hTTPInputSource = (HTTPInputSource) xMLInputSource;
                Iterator<Map.Entry<String, String>> hTTPRequestProperties = hTTPInputSource.getHTTPRequestProperties();
                while (hTTPRequestProperties.hasNext()) {
                    Map.Entry<String, String> next = hTTPRequestProperties.next();
                    httpURLConnection.setRequestProperty(next.getKey(), next.getValue());
                }
                boolean followHTTPRedirects = hTTPInputSource.getFollowHTTPRedirects();
                if (!followHTTPRedirects) {
                    XMLEntityManager.setInstanceFollowRedirects(httpURLConnection, followHTTPRedirects);
                }
            }
            inputStream = new BufferedInputStream(openConnection.getInputStream());
            String contentType = openConnection.getContentType();
            int indexOf = contentType != null ? contentType.indexOf(59) : -1;
            if (indexOf != -1) {
                str = contentType.substring(0, indexOf).trim();
                String trim = contentType.substring(indexOf + 1).trim();
                if (trim.startsWith("charset=")) {
                    str2 = trim.substring(8).trim();
                    if ((str2.charAt(0) == '\"' && str2.charAt(str2.length() - 1) == '\"') || (str2.charAt(0) == '\'' && str2.charAt(str2.length() - 1) == '\'')) {
                        str2 = str2.substring(1, str2.length() - 1);
                    }
                    if (!str.equals("text/xml")) {
                        if (str2 == null) {
                            str2 = "US-ASCII";
                        }
                    } else if (!str.equals("application/xml")) {
                        str2 = str.endsWith("+xml") ? getEncodingName(inputStream) : null;
                    } else if (str2 == null) {
                        str2 = getEncodingName(inputStream);
                    }
                    if (str2 != null) {
                        encoding = str2;
                    }
                }
            } else {
                str = contentType.trim();
            }
            str2 = null;
            if (!str.equals("text/xml")) {
            }
            if (str2 != null) {
            }
        }
        String consumeBOM = consumeBOM(inputStream, encoding.toUpperCase(Locale.ENGLISH));
        if (consumeBOM.equals("UTF-8")) {
            return new UTF8Reader(inputStream, this.fTempString.ch.length, this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210"), this.fErrorReporter.getLocale());
        }
        String iANA2JavaMapping = EncodingMap.getIANA2JavaMapping(consumeBOM);
        if (iANA2JavaMapping == null) {
            throw new IOException(this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210").formatMessage(this.fErrorReporter.getLocale(), "EncodingDeclInvalid", new Object[]{consumeBOM}));
        } else if (iANA2JavaMapping.equals("ASCII")) {
            return new ASCIIReader(inputStream, this.fTempString.ch.length, this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210"), this.fErrorReporter.getLocale());
        } else {
            return new InputStreamReader(inputStream, iANA2JavaMapping);
        }
    }

    /* access modifiers changed from: protected */
    public String getEncodingName(InputStream inputStream) throws IOException {
        byte[] bArr = new byte[4];
        inputStream.mark(4);
        int read = inputStream.read(bArr, 0, 4);
        inputStream.reset();
        if (read == 4) {
            return getEncodingName(bArr);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public String consumeBOM(InputStream inputStream, String str) throws IOException {
        byte[] bArr = new byte[3];
        inputStream.mark(3);
        if (str.equals("UTF-8")) {
            if (inputStream.read(bArr, 0, 3) == 3) {
                int i = bArr[0] & 255;
                int i2 = bArr[1] & 255;
                int i3 = bArr[2] & 255;
                if (!(i == 239 && i2 == 187 && i3 == 191)) {
                    inputStream.reset();
                }
            } else {
                inputStream.reset();
            }
        } else if (str.startsWith("UTF-16")) {
            if (inputStream.read(bArr, 0, 2) == 2) {
                int i4 = bArr[0] & 255;
                int i5 = bArr[1] & 255;
                if (i4 == 254 && i5 == 255) {
                    return "UTF-16BE";
                }
                if (i4 == 255 && i5 == 254) {
                    return "UTF-16LE";
                }
            }
            inputStream.reset();
        }
        return str;
    }

    /* access modifiers changed from: protected */
    public String getEncodingName(byte[] bArr) {
        int i = bArr[0] & 255;
        int i2 = bArr[1] & 255;
        if (i == 254 && i2 == 255) {
            return "UTF-16BE";
        }
        if (i == 255 && i2 == 254) {
            return "UTF-16LE";
        }
        int i3 = bArr[2] & 255;
        if (i == 239 && i2 == 187 && i3 == 191) {
            return "UTF-8";
        }
        int i4 = bArr[3] & 255;
        if (i == 0 && i2 == 0 && i3 == 0 && i4 == 60) {
            return "ISO-10646-UCS-4";
        }
        if (i == 60 && i2 == 0 && i3 == 0 && i4 == 0) {
            return "ISO-10646-UCS-4";
        }
        if (i == 0 && i2 == 0 && i3 == 60 && i4 == 0) {
            return "ISO-10646-UCS-4";
        }
        if (i == 0 && i2 == 60 && i3 == 0 && i4 == 0) {
            return "ISO-10646-UCS-4";
        }
        if (i == 0 && i2 == 60 && i3 == 0 && i4 == 63) {
            return "UTF-16BE";
        }
        if (i == 60 && i2 == 0 && i3 == 63 && i4 == 0) {
            return "UTF-16LE";
        }
        if (i == 76 && i2 == 111 && i3 == 167 && i4 == 148) {
            return "CP037";
        }
        return null;
    }

    public void parse() throws IOException {
        char c;
        this.fReader = getReader(this.fSource);
        this.fSource = null;
        int read = this.fReader.read(this.fTempString.ch, 0, this.fTempString.ch.length - 1);
        while (read != -1) {
            int i = read;
            int i2 = 0;
            while (i2 < i) {
                char c2 = this.fTempString.ch[i2];
                if (!isValid(c2)) {
                    if (XMLChar.isHighSurrogate(c2)) {
                        i2++;
                        if (i2 < i) {
                            c = this.fTempString.ch[i2];
                        } else {
                            int read2 = this.fReader.read();
                            c = read2;
                            if (read2 != -1) {
                                this.fTempString.ch[i] = (char) read2;
                                i++;
                                c = read2;
                            }
                        }
                        if (XMLChar.isLowSurrogate(c)) {
                            int supplemental = XMLChar.supplemental(c2, (char) c);
                            if (!isValid(supplemental)) {
                                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidCharInContent", new Object[]{Integer.toString(supplemental, 16)}, 2);
                            }
                        } else {
                            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidCharInContent", new Object[]{Integer.toString(c, 16)}, 2);
                        }
                    } else {
                        this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidCharInContent", new Object[]{Integer.toString(c2, 16)}, 2);
                    }
                }
                i2++;
            }
            XIncludeHandler xIncludeHandler = this.fHandler;
            if (xIncludeHandler != null && i > 0) {
                XMLString xMLString = this.fTempString;
                xMLString.offset = 0;
                xMLString.length = i;
                xIncludeHandler.characters(xMLString, xIncludeHandler.modifyAugmentations(null, true));
            }
            read = this.fReader.read(this.fTempString.ch, 0, this.fTempString.ch.length - 1);
        }
    }

    public void setInputSource(XMLInputSource xMLInputSource) {
        this.fSource = xMLInputSource;
    }

    public void close() throws IOException {
        Reader reader = this.fReader;
        if (reader != null) {
            reader.close();
            this.fReader = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isValid(int i) {
        return XMLChar.isValid(i);
    }

    /* access modifiers changed from: protected */
    public void setBufferSize(int i) {
        int i2 = i + 1;
        if (this.fTempString.ch.length != i2) {
            this.fTempString.ch = new char[i2];
        }
    }
}
