package org.apache.xml.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.xml.serializer.utils.WrappedRuntimeException;

public final class Encodings {
    static final String DEFAULT_MIME_ENCODING = "UTF-8";
    private static final String ENCODINGS_FILE = (SerializerBase.PKG_PATH + "/Encodings.properties");
    private static final Hashtable _encodingTableKeyJava = new Hashtable();
    private static final Hashtable _encodingTableKeyMime = new Hashtable();
    private static final EncodingInfo[] _encodings = loadEncodingInfo();

    static Writer getWriter(OutputStream output, String encoding) throws UnsupportedEncodingException {
        int i = 0;
        while (i < _encodings.length) {
            if (_encodings[i].name.equalsIgnoreCase(encoding)) {
                try {
                    return new OutputStreamWriter(output, _encodings[i].javaName);
                } catch (IllegalArgumentException e) {
                } catch (UnsupportedEncodingException e2) {
                }
            } else {
                i++;
            }
        }
        try {
            return new OutputStreamWriter(output, encoding);
        } catch (IllegalArgumentException e3) {
            throw new UnsupportedEncodingException(encoding);
        }
    }

    static EncodingInfo getEncodingInfo(String encoding) {
        String normalizedEncoding = toUpperCaseFast(encoding);
        EncodingInfo ei = (EncodingInfo) _encodingTableKeyJava.get(normalizedEncoding);
        if (ei == null) {
            ei = (EncodingInfo) _encodingTableKeyMime.get(normalizedEncoding);
        }
        if (ei == null) {
            return new EncodingInfo(null, null, 0);
        }
        return ei;
    }

    public static boolean isRecognizedEncoding(String encoding) {
        String normalizedEncoding = encoding.toUpperCase();
        EncodingInfo ei = (EncodingInfo) _encodingTableKeyJava.get(normalizedEncoding);
        if (ei == null) {
            ei = (EncodingInfo) _encodingTableKeyMime.get(normalizedEncoding);
        }
        if (ei != null) {
            return true;
        }
        return false;
    }

    private static String toUpperCaseFast(String s) {
        boolean different = false;
        int mx = s.length();
        char[] chars = new char[mx];
        for (int i = 0; i < mx; i++) {
            char ch = s.charAt(i);
            if ('a' <= ch && ch <= 'z') {
                ch = (char) (ch - 32);
                different = true;
            }
            chars[i] = ch;
        }
        if (different) {
            return String.valueOf(chars);
        }
        return s;
    }

    static String getMimeEncoding(String encoding) {
        if (encoding != null) {
            return convertJava2MimeEncoding(encoding);
        }
        try {
            encoding = System.getProperty("file.encoding", "UTF8");
            if (encoding == null) {
                return DEFAULT_MIME_ENCODING;
            }
            String jencoding;
            if (encoding.equalsIgnoreCase("Cp1252") || encoding.equalsIgnoreCase("ISO8859_1") || encoding.equalsIgnoreCase("8859_1") || encoding.equalsIgnoreCase("UTF8")) {
                jencoding = DEFAULT_MIME_ENCODING;
            } else {
                jencoding = convertJava2MimeEncoding(encoding);
            }
            if (jencoding != null) {
                return jencoding;
            }
            return DEFAULT_MIME_ENCODING;
        } catch (SecurityException e) {
            return DEFAULT_MIME_ENCODING;
        }
    }

    private static String convertJava2MimeEncoding(String encoding) {
        EncodingInfo enc = (EncodingInfo) _encodingTableKeyJava.get(toUpperCaseFast(encoding));
        if (enc != null) {
            return enc.name;
        }
        return encoding;
    }

    public static String convertMime2JavaEncoding(String encoding) {
        for (int i = 0; i < _encodings.length; i++) {
            if (_encodings[i].name.equalsIgnoreCase(encoding)) {
                return _encodings[i].javaName;
            }
        }
        return encoding;
    }

    private static EncodingInfo[] loadEncodingInfo() {
        try {
            InputStream is = SecuritySupport.getInstance().getResourceAsStream(ObjectFactory.findClassLoader(), ENCODINGS_FILE);
            Properties props = new Properties();
            if (is != null) {
                props.load(is);
                is.close();
            }
            int totalEntries = props.size();
            List encodingInfo_list = new ArrayList();
            Enumeration keys = props.keys();
            for (int i = 0; i < totalEntries; i++) {
                String javaName = (String) keys.nextElement();
                String val = props.getProperty(javaName);
                int len = lengthOfMimeNames(val);
                String mimeName;
                if (len == 0) {
                    mimeName = javaName;
                } else {
                    char highChar;
                    try {
                        highChar = (char) Integer.decode(val.substring(len).trim()).intValue();
                    } catch (NumberFormatException e) {
                        highChar = 0;
                    }
                    StringTokenizer stringTokenizer = new StringTokenizer(val.substring(0, len), ",");
                    boolean first = true;
                    while (stringTokenizer.hasMoreTokens()) {
                        mimeName = stringTokenizer.nextToken();
                        EncodingInfo ei = new EncodingInfo(mimeName, javaName, highChar);
                        encodingInfo_list.add(ei);
                        _encodingTableKeyMime.put(mimeName.toUpperCase(), ei);
                        if (first) {
                            _encodingTableKeyJava.put(javaName.toUpperCase(), ei);
                        }
                        first = false;
                    }
                }
            }
            EncodingInfo[] ret_ei = new EncodingInfo[encodingInfo_list.size()];
            encodingInfo_list.toArray(ret_ei);
            return ret_ei;
        } catch (Exception mue) {
            throw new WrappedRuntimeException(mue);
        } catch (IOException ioe) {
            throw new WrappedRuntimeException(ioe);
        }
    }

    private static int lengthOfMimeNames(String val) {
        int len = val.indexOf(32);
        if (len < 0) {
            return val.length();
        }
        return len;
    }

    static boolean isHighUTF16Surrogate(char ch) {
        return 55296 <= ch && ch <= 56319;
    }

    static boolean isLowUTF16Surrogate(char ch) {
        return 56320 <= ch && ch <= 57343;
    }

    static int toCodePoint(char highSurrogate, char lowSurrogate) {
        return (((highSurrogate - 55296) << 10) + (lowSurrogate - 56320)) + 65536;
    }

    static int toCodePoint(char ch) {
        return ch;
    }

    public static char getHighChar(String encoding) {
        String normalizedEncoding = toUpperCaseFast(encoding);
        EncodingInfo ei = (EncodingInfo) _encodingTableKeyJava.get(normalizedEncoding);
        if (ei == null) {
            ei = (EncodingInfo) _encodingTableKeyMime.get(normalizedEncoding);
        }
        if (ei != null) {
            return ei.getHighChar();
        }
        return 0;
    }
}
