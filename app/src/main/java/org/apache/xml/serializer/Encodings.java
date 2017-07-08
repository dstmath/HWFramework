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
import org.apache.xpath.axes.WalkerFactory;

public final class Encodings {
    static final String DEFAULT_MIME_ENCODING = "UTF-8";
    private static final String ENCODINGS_FILE = null;
    private static final Hashtable _encodingTableKeyJava = null;
    private static final Hashtable _encodingTableKeyMime = null;
    private static final EncodingInfo[] _encodings = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xml.serializer.Encodings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xml.serializer.Encodings.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xml.serializer.Encodings.<clinit>():void");
    }

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
            return new EncodingInfo(null, null, '\u0000');
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
                    char intValue;
                    try {
                        intValue = (char) Integer.decode(val.substring(len).trim()).intValue();
                    } catch (NumberFormatException e) {
                        intValue = '\u0000';
                    }
                    StringTokenizer stringTokenizer = new StringTokenizer(val.substring(0, len), ",");
                    boolean first = true;
                    while (stringTokenizer.hasMoreTokens()) {
                        mimeName = stringTokenizer.nextToken();
                        EncodingInfo ei = new EncodingInfo(mimeName, javaName, intValue);
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
        return '\ud800' <= ch && ch <= '\udbff';
    }

    static boolean isLowUTF16Surrogate(char ch) {
        return '\udc00' <= ch && ch <= '\udfff';
    }

    static int toCodePoint(char highSurrogate, char lowSurrogate) {
        return (((highSurrogate - 55296) << 10) + (lowSurrogate - 56320)) + WalkerFactory.BIT_CHILD;
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
        return '\u0000';
    }
}
