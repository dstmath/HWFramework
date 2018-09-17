package java.net;

import java.io.UnsupportedEncodingException;

public class URLDecoder {
    static String dfltEncName = URLEncoder.dfltEncName;

    @Deprecated
    public static String decode(String s) {
        String str = null;
        try {
            return decode(s, dfltEncName);
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    public static String decode(String s, String enc) throws UnsupportedEncodingException {
        int i;
        boolean needToChange = false;
        int numChars = s.length();
        if (numChars > 500) {
            i = numChars / 2;
        } else {
            i = numChars;
        }
        StringBuffer sb = new StringBuffer(i);
        int i2 = 0;
        if (enc.length() == 0) {
            throw new UnsupportedEncodingException("URLDecoder: empty string enc parameter");
        }
        byte[] bArr = null;
        while (i2 < numChars) {
            char c = s.charAt(i2);
            switch (c) {
                case '%':
                    if (bArr == null) {
                        try {
                            bArr = new byte[((numChars - i2) / 3)];
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("URLDecoder: Illegal hex characters in escape (%) pattern - " + e.getMessage());
                        }
                    }
                    int pos = 0;
                    while (true) {
                        int pos2 = pos;
                        if (i2 + 2 >= numChars || c != '%') {
                            if (i2 >= numChars || c != '%') {
                                sb.append(new String(bArr, 0, pos2, enc));
                                needToChange = true;
                                break;
                            }
                            throw new IllegalArgumentException("URLDecoder: Incomplete trailing escape (%) pattern");
                        } else if (isValidHexChar(s.charAt(i2 + 1)) && (isValidHexChar(s.charAt(i2 + 2)) ^ 1) == 0) {
                            int v = Integer.parseInt(s.substring(i2 + 1, i2 + 3), 16);
                            if (v < 0) {
                                throw new IllegalArgumentException("URLDecoder: Illegal hex characters in escape (%) pattern - negative value : " + s.substring(i2, i2 + 3));
                            }
                            pos = pos2 + 1;
                            bArr[pos2] = (byte) v;
                            i2 += 3;
                            if (i2 < numChars) {
                                c = s.charAt(i2);
                            }
                        }
                    }
                    throw new IllegalArgumentException("URLDecoder: Illegal hex characters in escape (%) pattern : " + s.substring(i2, i2 + 3));
                case '+':
                    sb.append(' ');
                    i2++;
                    needToChange = true;
                    break;
                default:
                    sb.append(c);
                    i2++;
                    break;
            }
        }
        return needToChange ? sb.toString() : s;
    }

    private static boolean isValidHexChar(char c) {
        if ('0' <= c && c <= '9') {
            return true;
        }
        if ('a' > c || c > 'f') {
            return 'A' <= c && c <= 'F';
        } else {
            return true;
        }
    }
}
