package java.net;

import java.io.UnsupportedEncodingException;

public class URLDecoder {
    static String dfltEncName = URLEncoder.dfltEncName;

    @Deprecated
    public static String decode(String s) {
        try {
            return decode(s, dfltEncName);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String decode(String s, String enc) throws UnsupportedEncodingException {
        boolean needToChange = false;
        int numChars = s.length();
        StringBuffer sb = new StringBuffer(numChars > 500 ? numChars / 2 : numChars);
        int i = 0;
        if (enc.length() != 0) {
            byte[] bytes = null;
            while (i < numChars) {
                char c = s.charAt(i);
                if (c == '%') {
                    if (bytes == null) {
                        try {
                            bytes = new byte[((numChars - i) / 3)];
                        } catch (NumberFormatException e) {
                            e = e;
                            char c2 = c;
                            int i2 = i;
                            throw new IllegalArgumentException("URLDecoder: Illegal hex characters in escape (%) pattern - " + e.getMessage());
                        }
                    }
                    char c3 = c;
                    int i3 = i;
                    int pos = 0;
                    while (i3 + 2 < numChars && c3 == '%') {
                        try {
                            if (!isValidHexChar(s.charAt(i3 + 1)) || !isValidHexChar(s.charAt(i3 + 2))) {
                                throw new IllegalArgumentException("URLDecoder: Illegal hex characters in escape (%) pattern : " + s.substring(i3, i3 + 3));
                            }
                            int v = Integer.parseInt(s.substring(i3 + 1, i3 + 3), 16);
                            if (v >= 0) {
                                int pos2 = pos + 1;
                                bytes[pos] = (byte) v;
                                i3 += 3;
                                if (i3 < numChars) {
                                    c3 = s.charAt(i3);
                                }
                                pos = pos2;
                            } else {
                                throw new IllegalArgumentException("URLDecoder: Illegal hex characters in escape (%) pattern - negative value : " + s.substring(i3, i3 + 3));
                            }
                        } catch (NumberFormatException e2) {
                            e = e2;
                            throw new IllegalArgumentException("URLDecoder: Illegal hex characters in escape (%) pattern - " + e.getMessage());
                        }
                    }
                    if (i3 < numChars) {
                        if (c3 == '%') {
                            throw new IllegalArgumentException("URLDecoder: Incomplete trailing escape (%) pattern");
                        }
                    }
                    sb.append(new String(bytes, 0, pos, enc));
                    needToChange = true;
                    i = i3;
                } else if (c != '+') {
                    sb.append(c);
                    i++;
                } else {
                    sb.append(' ');
                    i++;
                    needToChange = true;
                }
            }
            return needToChange ? sb.toString() : s;
        }
        throw new UnsupportedEncodingException("URLDecoder: empty string enc parameter");
    }

    private static boolean isValidHexChar(char c) {
        return ('0' <= c && c <= '9') || ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F');
    }
}
