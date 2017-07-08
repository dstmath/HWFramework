package java.net;

import java.io.CharArrayWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.BitSet;

public class URLEncoder {
    static final int caseDiff = 32;
    static String dfltEncName;
    static BitSet dontNeedEncoding;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.net.URLEncoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.net.URLEncoder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.net.URLEncoder.<clinit>():void");
    }

    private URLEncoder() {
    }

    @Deprecated
    public static String encode(String s) {
        String str = null;
        try {
            str = encode(s, dfltEncName);
        } catch (UnsupportedEncodingException e) {
        }
        return str;
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
                    if (c == caseDiff) {
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
                    } while (!bitSet.get(c));
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
