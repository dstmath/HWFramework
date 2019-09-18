package javax.xml.transform.stream;

import android.icu.impl.PatternTokenizer;
import java.io.File;
import java.io.UnsupportedEncodingException;

class FilePathToURI {
    private static char[] gAfterEscaping1 = new char[128];
    private static char[] gAfterEscaping2 = new char[128];
    private static char[] gHexChs = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static boolean[] gNeedEscaping = new boolean[128];

    FilePathToURI() {
    }

    static {
        for (int i = 0; i <= 31; i++) {
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
        int i;
        if (path == null) {
            return null;
        }
        String path2 = path.replace(File.separatorChar, '/');
        int len = path2.length();
        StringBuilder buffer = new StringBuilder(len * 3);
        buffer.append("file://");
        int ch = 0;
        if (len >= 2 && path2.charAt(1) == ':') {
            int ch2 = Character.toUpperCase(path2.charAt(0));
            if (ch2 >= 65 && ch2 <= 90) {
                buffer.append('/');
            }
        }
        while (true) {
            i = ch;
            if (i >= len) {
                break;
            }
            int ch3 = path2.charAt(i);
            if (ch3 >= 128) {
                break;
            }
            if (gNeedEscaping[ch3]) {
                buffer.append('%');
                buffer.append(gAfterEscaping1[ch3]);
                buffer.append(gAfterEscaping2[ch3]);
            } else {
                buffer.append((char) ch3);
            }
            ch = i + 1;
        }
        if (i < len) {
            try {
                for (byte b : path2.substring(i).getBytes("UTF-8")) {
                    if (b < 0) {
                        int ch4 = b + 256;
                        buffer.append('%');
                        buffer.append(gHexChs[ch4 >> 4]);
                        buffer.append(gHexChs[ch4 & 15]);
                    } else if (gNeedEscaping[b]) {
                        buffer.append('%');
                        buffer.append(gAfterEscaping1[b]);
                        buffer.append(gAfterEscaping2[b]);
                    } else {
                        buffer.append((char) b);
                    }
                }
            } catch (UnsupportedEncodingException e) {
                return path2;
            }
        }
        return buffer.toString();
    }
}
