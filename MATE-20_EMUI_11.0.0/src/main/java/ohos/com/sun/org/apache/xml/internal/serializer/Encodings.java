package ohos.com.sun.org.apache.xml.internal.serializer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xml.internal.serializer.utils.WrappedRuntimeException;
import ohos.global.icu.text.UTF16;

public final class Encodings {
    static final String DEFAULT_MIME_ENCODING = "UTF-8";
    private static final String ENCODINGS_FILE = "ohos.com.sun.org.apache.xml.internal.serializer.Encodings.properties";
    private static final String ENCODINGS_PROP = "com.sun.org.apache.xalan.internal.serialize.encodings";
    private static final EncodingInfos _encodingInfos = new EncodingInfos();
    private static final int m_defaultLastPrintable = 127;

    public static int getLastPrintable() {
        return 127;
    }

    static boolean isHighUTF16Surrogate(char c) {
        return 55296 <= c && c <= 56319;
    }

    static boolean isLowUTF16Surrogate(char c) {
        return 56320 <= c && c <= 57343;
    }

    static int toCodePoint(char c) {
        return c;
    }

    static int toCodePoint(char c, char c2) {
        return ((c - 55296) << 10) + (c2 - UTF16.TRAIL_SURROGATE_MIN_VALUE) + 65536;
    }

    static Writer getWriter(OutputStream outputStream, String str) throws UnsupportedEncodingException {
        EncodingInfo findEncoding = _encodingInfos.findEncoding(toUpperCaseFast(str));
        if (findEncoding != null) {
            try {
                return new BufferedWriter(new OutputStreamWriter(outputStream, findEncoding.javaName));
            } catch (UnsupportedEncodingException unused) {
            }
        }
        return new BufferedWriter(new OutputStreamWriter(outputStream, str));
    }

    static EncodingInfo getEncodingInfo(String str) {
        String upperCaseFast = toUpperCaseFast(str);
        EncodingInfo findEncoding = _encodingInfos.findEncoding(upperCaseFast);
        if (findEncoding != null) {
            return findEncoding;
        }
        try {
            String name = Charset.forName(str).name();
            EncodingInfo encodingInfo = new EncodingInfo(name, name);
            _encodingInfos.putEncoding(upperCaseFast, encodingInfo);
            return encodingInfo;
        } catch (IllegalCharsetNameException | UnsupportedCharsetException unused) {
            return new EncodingInfo(null, null);
        }
    }

    /* access modifiers changed from: private */
    public static String toUpperCaseFast(String str) {
        int length = str.length();
        char[] cArr = new char[length];
        boolean z = false;
        for (int i = 0; i < length; i++) {
            char charAt = str.charAt(i);
            if ('a' <= charAt && charAt <= 'z') {
                charAt = (char) (charAt - ' ');
                z = true;
            }
            cArr[i] = charAt;
        }
        return z ? String.valueOf(cArr) : str;
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0036  */
    static String getMimeEncoding(String str) {
        String str2;
        if (str != null) {
            return convertJava2MimeEncoding(str);
        }
        try {
            String systemProperty = SecuritySupport.getSystemProperty("file.encoding", "UTF8");
            if (systemProperty == null) {
                return "UTF-8";
            }
            if (!systemProperty.equalsIgnoreCase("Cp1252") && !systemProperty.equalsIgnoreCase("ISO8859_1") && !systemProperty.equalsIgnoreCase("8859_1")) {
                if (!systemProperty.equalsIgnoreCase("UTF8")) {
                    str2 = convertJava2MimeEncoding(systemProperty);
                    if (str2 == null) {
                        str2 = "UTF-8";
                    }
                    return str2;
                }
            }
            str2 = "UTF-8";
            if (str2 == null) {
            }
            return str2;
        } catch (SecurityException unused) {
            return "UTF-8";
        }
    }

    private static String convertJava2MimeEncoding(String str) {
        EncodingInfo encodingFromJavaKey = _encodingInfos.getEncodingFromJavaKey(toUpperCaseFast(str));
        return encodingFromJavaKey != null ? encodingFromJavaKey.name : str;
    }

    public static String convertMime2JavaEncoding(String str) {
        EncodingInfo findEncoding = _encodingInfos.findEncoding(toUpperCaseFast(str));
        return findEncoding != null ? findEncoding.javaName : str;
    }

    /* access modifiers changed from: private */
    public static final class EncodingInfos {
        private final Map<String, EncodingInfo> _encodingDynamicTable;
        private final Map<String, EncodingInfo> _encodingTableKeyJava;
        private final Map<String, EncodingInfo> _encodingTableKeyMime;

        private EncodingInfos() {
            this._encodingTableKeyJava = new HashMap();
            this._encodingTableKeyMime = new HashMap();
            this._encodingDynamicTable = Collections.synchronizedMap(new HashMap());
            loadEncodingInfo();
        }

        private InputStream openEncodingsFileStream() throws MalformedURLException, IOException {
            String str;
            InputStream inputStream = null;
            try {
                str = SecuritySupport.getSystemProperty(Encodings.ENCODINGS_PROP, "");
            } catch (SecurityException unused) {
                str = null;
            }
            if (str != null && str.length() > 0) {
                inputStream = new URL(str).openStream();
            }
            return inputStream == null ? SecuritySupport.getResourceAsStream(Encodings.ENCODINGS_FILE) : inputStream;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x0016, code lost:
            r2 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0017, code lost:
            r0.addSuppressed(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x001a, code lost:
            throw r1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:7:0x0011, code lost:
            r1 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:?, code lost:
            r2.close();
         */
        private Properties loadProperties() throws MalformedURLException, IOException {
            Properties properties = new Properties();
            InputStream openEncodingsFileStream = openEncodingsFileStream();
            if (openEncodingsFileStream != null) {
                properties.load(openEncodingsFileStream);
            }
            if (openEncodingsFileStream != null) {
                openEncodingsFileStream.close();
            }
            return properties;
        }

        private String[] parseMimeTypes(String str) {
            int indexOf = str.indexOf(32);
            int i = 0;
            if (indexOf < 0) {
                return new String[]{str};
            }
            StringTokenizer stringTokenizer = new StringTokenizer(str.substring(0, indexOf), ",");
            String[] strArr = new String[stringTokenizer.countTokens()];
            while (stringTokenizer.hasMoreTokens()) {
                strArr[i] = stringTokenizer.nextToken();
                i++;
            }
            return strArr;
        }

        private String findCharsetNameFor(String str) {
            try {
                return Charset.forName(str).name();
            } catch (Exception unused) {
                return null;
            }
        }

        private String findCharsetNameFor(String str, String[] strArr) {
            String findCharsetNameFor = findCharsetNameFor(str);
            if (findCharsetNameFor != null) {
                return str;
            }
            for (String str2 : strArr) {
                findCharsetNameFor = findCharsetNameFor(str2);
                if (findCharsetNameFor != null) {
                    break;
                }
            }
            return findCharsetNameFor;
        }

        private void loadEncodingInfo() {
            try {
                Properties loadProperties = loadProperties();
                Enumeration keys = loadProperties.keys();
                HashMap hashMap = new HashMap();
                while (keys.hasMoreElements()) {
                    String str = (String) keys.nextElement();
                    String[] parseMimeTypes = parseMimeTypes(loadProperties.getProperty(str));
                    String findCharsetNameFor = findCharsetNameFor(str, parseMimeTypes);
                    if (findCharsetNameFor != null) {
                        String upperCaseFast = Encodings.toUpperCaseFast(str);
                        String upperCaseFast2 = Encodings.toUpperCaseFast(findCharsetNameFor);
                        for (String str2 : parseMimeTypes) {
                            String upperCaseFast3 = Encodings.toUpperCaseFast(str2);
                            EncodingInfo encodingInfo = new EncodingInfo(str2, findCharsetNameFor);
                            this._encodingTableKeyMime.put(upperCaseFast3, encodingInfo);
                            if (!hashMap.containsKey(upperCaseFast2)) {
                                hashMap.put(upperCaseFast2, encodingInfo);
                                this._encodingTableKeyJava.put(upperCaseFast2, encodingInfo);
                            }
                            this._encodingTableKeyJava.put(upperCaseFast, encodingInfo);
                        }
                    }
                }
                for (Map.Entry<String, EncodingInfo> entry : this._encodingTableKeyJava.entrySet()) {
                    entry.setValue((EncodingInfo) hashMap.get(Encodings.toUpperCaseFast(entry.getValue().javaName)));
                }
            } catch (MalformedURLException e) {
                throw new WrappedRuntimeException(e);
            } catch (IOException e2) {
                throw new WrappedRuntimeException(e2);
            }
        }

        /* access modifiers changed from: package-private */
        public EncodingInfo findEncoding(String str) {
            EncodingInfo encodingInfo = this._encodingTableKeyJava.get(str);
            if (encodingInfo == null) {
                encodingInfo = this._encodingTableKeyMime.get(str);
            }
            return encodingInfo == null ? this._encodingDynamicTable.get(str) : encodingInfo;
        }

        /* access modifiers changed from: package-private */
        public EncodingInfo getEncodingFromMimeKey(String str) {
            return this._encodingTableKeyMime.get(str);
        }

        /* access modifiers changed from: package-private */
        public EncodingInfo getEncodingFromJavaKey(String str) {
            return this._encodingTableKeyJava.get(str);
        }

        /* access modifiers changed from: package-private */
        public void putEncoding(String str, EncodingInfo encodingInfo) {
            this._encodingDynamicTable.put(str, encodingInfo);
        }
    }
}
