package javax.xml.transform.stream;

import android.icu.impl.PatternTokenizer;
import java.io.File;
import java.io.UnsupportedEncodingException;

class FilePathToURI {
    private static char[] gAfterEscaping1 = new char[128];
    private static char[] gAfterEscaping2 = new char[128];
    private static char[] gHexChs = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static boolean[] gNeedEscaping = new boolean[128];

    FilePathToURI() {
    }

    static {
        int i;
        for (i = 0; i <= 31; i++) {
            gNeedEscaping[i] = true;
            gAfterEscaping1[i] = gHexChs[i >> 4];
            gAfterEscaping2[i] = gHexChs[i & 15];
        }
        gNeedEscaping[127] = true;
        gAfterEscaping1[127] = '7';
        gAfterEscaping2[127] = 'F';
        for (char ch : new char[]{' ', '<', '>', '#', '%', '\"', '{', '}', '|', PatternTokenizer.BACK_SLASH, '^', '~', '[', ']', '`'}) {
            gNeedEscaping[ch] = true;
            gAfterEscaping1[ch] = gHexChs[ch >> 4];
            gAfterEscaping2[ch] = gHexChs[ch & 15];
        }
    }

    public static String filepath2URI(String path) {
        if (path == null) {
            return null;
        }
        int ch;
        path = path.replace(File.separatorChar, '/');
        int len = path.length();
        StringBuilder buffer = new StringBuilder(len * 3);
        buffer.append("file://");
        if (len >= 2 && path.charAt(1) == ':') {
            ch = Character.toUpperCase(path.charAt(0));
            if (ch >= 65 && ch <= 90) {
                buffer.append('/');
            }
        }
        int i = 0;
        while (i < len) {
            ch = path.charAt(i);
            if (ch >= 128) {
                break;
            }
            if (gNeedEscaping[ch]) {
                buffer.append('%');
                buffer.append(gAfterEscaping1[ch]);
                buffer.append(gAfterEscaping2[ch]);
            } else {
                buffer.append((char) ch);
            }
            i++;
        }
        if (i < len) {
            try {
                for (byte b : path.substring(i).getBytes("UTF-8")) {
                    if (b < (byte) 0) {
                        ch = b + 256;
                        buffer.append('%');
                        buffer.append(gHexChs[ch >> 4]);
                        buffer.append(gHexChs[ch & 15]);
                    } else if (gNeedEscaping[b]) {
                        buffer.append('%');
                        buffer.append(gAfterEscaping1[b]);
                        buffer.append(gAfterEscaping2[b]);
                    } else {
                        buffer.append((char) b);
                    }
                }
            } catch (UnsupportedEncodingException e) {
                return path;
            }
        }
        return buffer.toString();
    }
}
