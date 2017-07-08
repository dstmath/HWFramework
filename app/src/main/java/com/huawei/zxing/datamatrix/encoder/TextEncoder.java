package com.huawei.zxing.datamatrix.encoder;

final class TextEncoder extends C40Encoder {
    TextEncoder() {
    }

    public int getEncodingMode() {
        return 2;
    }

    int encodeChar(char c, StringBuilder sb) {
        if (c == ' ') {
            sb.append('\u0003');
            return 1;
        } else if (c >= '0' && c <= '9') {
            sb.append((char) ((c - 48) + 4));
            return 1;
        } else if (c >= 'a' && c <= 'z') {
            sb.append((char) ((c - 97) + 14));
            return 1;
        } else if (c >= '\u0000' && c <= '\u001f') {
            sb.append('\u0000');
            sb.append(c);
            return 2;
        } else if (c >= '!' && c <= '/') {
            sb.append('\u0001');
            sb.append((char) (c - 33));
            return 2;
        } else if (c >= ':' && c <= '@') {
            sb.append('\u0001');
            sb.append((char) ((c - 58) + 15));
            return 2;
        } else if (c >= '[' && c <= '_') {
            sb.append('\u0001');
            sb.append((char) ((c - 91) + 22));
            return 2;
        } else if (c == '`') {
            sb.append('\u0002');
            sb.append((char) (c - 96));
            return 2;
        } else if (c >= 'A' && c <= 'Z') {
            sb.append('\u0002');
            sb.append((char) ((c - 65) + 1));
            return 2;
        } else if (c >= '{' && c <= '\u007f') {
            sb.append('\u0002');
            sb.append((char) ((c - 123) + 27));
            return 2;
        } else if (c >= '\u0080') {
            sb.append("\u0001\u001e");
            return encodeChar((char) (c - 128), sb) + 2;
        } else {
            HighLevelEncoder.illegalCharacter(c);
            return -1;
        }
    }
}
