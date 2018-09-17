package java.net;

import java.io.CharArrayWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.security.AccessController;
import java.util.BitSet;
import sun.security.action.GetPropertyAction;

public class URLEncoder {
    static final int caseDiff = 32;
    static String dfltEncName;
    static BitSet dontNeedEncoding = new BitSet(256);

    static {
        int i;
        dfltEncName = null;
        for (i = 97; i <= 122; i++) {
            dontNeedEncoding.set(i);
        }
        for (i = 65; i <= 90; i++) {
            dontNeedEncoding.set(i);
        }
        for (i = 48; i <= 57; i++) {
            dontNeedEncoding.set(i);
        }
        dontNeedEncoding.set(32);
        dontNeedEncoding.set(45);
        dontNeedEncoding.set(95);
        dontNeedEncoding.set(46);
        dontNeedEncoding.set(42);
        dfltEncName = (String) AccessController.doPrivileged(new GetPropertyAction("file.encoding"));
    }

    private URLEncoder() {
    }

    @Deprecated
    public static String encode(String s) {
        String str = null;
        try {
            return encode(s, dfltEncName);
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    public static String encode(String s, String enc) throws UnsupportedEncodingException {
        boolean needToChange = false;
        StringBuffer out = new StringBuffer(s.length());
        CharArrayWriter charArrayWriter = new CharArrayWriter();
        if (enc == null) {
            throw new NullPointerException("charsetName");
        }
        try {
            Charset charset = Charset.forName(enc);
            int i = 0;
            while (i < s.length()) {
                int c = s.charAt(i);
                if (dontNeedEncoding.get(c)) {
                    if (c == 32) {
                        c = 43;
                        needToChange = true;
                    }
                    out.append((char) c);
                    i++;
                } else {
                    BitSet bitSet;
                    do {
                        charArrayWriter.write(c);
                        if (c >= 55296 && c <= 56319 && i + 1 < s.length()) {
                            int d = s.charAt(i + 1);
                            if (d >= 56320 && d <= 57343) {
                                charArrayWriter.write(d);
                                i++;
                            }
                        }
                        i++;
                        if (i >= s.length()) {
                            break;
                        }
                        bitSet = dontNeedEncoding;
                        c = s.charAt(i);
                    } while ((bitSet.get(c) ^ 1) != 0);
                    charArrayWriter.flush();
                    byte[] ba = new String(charArrayWriter.toCharArray()).getBytes(charset);
                    for (int j = 0; j < ba.length; j++) {
                        out.append('%');
                        char ch = Character.forDigit((ba[j] >> 4) & 15, 16);
                        if (Character.isLetter(ch)) {
                            ch = (char) (ch - 32);
                        }
                        out.append(ch);
                        ch = Character.forDigit(ba[j] & 15, 16);
                        if (Character.isLetter(ch)) {
                            ch = (char) (ch - 32);
                        }
                        out.append(ch);
                    }
                    charArrayWriter.reset();
                    needToChange = true;
                }
            }
            return needToChange ? out.toString() : s;
        } catch (IllegalCharsetNameException e) {
            throw new UnsupportedEncodingException(enc);
        } catch (UnsupportedCharsetException e2) {
            throw new UnsupportedEncodingException(enc);
        }
    }
}
