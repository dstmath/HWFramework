package com.huawei.zxing.datamatrix.encoder;

/* access modifiers changed from: package-private */
public class C40Encoder implements Encoder {
    C40Encoder() {
    }

    @Override // com.huawei.zxing.datamatrix.encoder.Encoder
    public int getEncodingMode() {
        return 1;
    }

    @Override // com.huawei.zxing.datamatrix.encoder.Encoder
    public void encode(EncoderContext context) {
        int newMode;
        StringBuilder buffer = new StringBuilder();
        while (true) {
            if (!context.hasMoreCharacters()) {
                break;
            }
            char c = context.getCurrentChar();
            context.pos++;
            int lastCharSize = encodeChar(c, buffer);
            int curCodewordCount = context.getCodewordCount() + ((buffer.length() / 3) * 2);
            context.updateSymbolInfo(curCodewordCount);
            int available = context.getSymbolInfo().getDataCapacity() - curCodewordCount;
            if (context.hasMoreCharacters()) {
                if (buffer.length() % 3 == 0 && (newMode = HighLevelEncoder.lookAheadTest(context.getMessage(), context.pos, getEncodingMode())) != getEncodingMode()) {
                    context.signalEncoderChange(newMode);
                    break;
                }
            } else {
                StringBuilder removed = new StringBuilder();
                if (buffer.length() % 3 == 2 && (available < 2 || available > 2)) {
                    lastCharSize = backtrackOneCharacter(context, buffer, removed, lastCharSize);
                }
                while (buffer.length() % 3 == 1 && ((lastCharSize <= 3 && available != 1) || lastCharSize > 3)) {
                    lastCharSize = backtrackOneCharacter(context, buffer, removed, lastCharSize);
                }
            }
        }
        handleEOD(context, buffer);
    }

    private int backtrackOneCharacter(EncoderContext context, StringBuilder buffer, StringBuilder removed, int lastCharSize) {
        int count = buffer.length();
        buffer.delete(count - lastCharSize, count);
        context.pos--;
        int lastCharSize2 = encodeChar(context.getCurrentChar(), removed);
        context.resetSymbolInfo();
        return lastCharSize2;
    }

    static void writeNextTriplet(EncoderContext context, StringBuilder buffer) {
        context.writeCodewords(encodeToCodewords(buffer, 0));
        buffer.delete(0, 3);
    }

    /* access modifiers changed from: package-private */
    public void handleEOD(EncoderContext context, StringBuilder buffer) {
        int rest = buffer.length() % 3;
        int curCodewordCount = context.getCodewordCount() + ((buffer.length() / 3) * 2);
        context.updateSymbolInfo(curCodewordCount);
        int available = context.getSymbolInfo().getDataCapacity() - curCodewordCount;
        if (rest == 2) {
            buffer.append((char) 0);
            while (buffer.length() >= 3) {
                writeNextTriplet(context, buffer);
            }
            if (context.hasMoreCharacters()) {
                context.writeCodeword(254);
            }
        } else if (available == 1 && rest == 1) {
            while (buffer.length() >= 3) {
                writeNextTriplet(context, buffer);
            }
            if (context.hasMoreCharacters()) {
                context.writeCodeword(254);
            }
            context.pos--;
        } else if (rest == 0) {
            while (buffer.length() >= 3) {
                writeNextTriplet(context, buffer);
            }
            if (available > 0 || context.hasMoreCharacters()) {
                context.writeCodeword(254);
            }
        } else {
            throw new IllegalStateException("Unexpected case. Please report!");
        }
        context.signalEncoderChange(0);
    }

    /* access modifiers changed from: package-private */
    public int encodeChar(char c, StringBuilder sb) {
        if (c == ' ') {
            sb.append((char) 3);
            return 1;
        } else if (c >= '0' && c <= '9') {
            sb.append((char) ((c - '0') + 4));
            return 1;
        } else if (c >= 'A' && c <= 'Z') {
            sb.append((char) ((c - 'A') + 14));
            return 1;
        } else if (c >= 0 && c <= 31) {
            sb.append((char) 0);
            sb.append(c);
            return 2;
        } else if (c >= '!' && c <= '/') {
            sb.append((char) 1);
            sb.append((char) (c - '!'));
            return 2;
        } else if (c >= ':' && c <= '@') {
            sb.append((char) 1);
            sb.append((char) ((c - ':') + 15));
            return 2;
        } else if (c >= '[' && c <= '_') {
            sb.append((char) 1);
            sb.append((char) ((c - '[') + 22));
            return 2;
        } else if (c >= '`' && c <= 127) {
            sb.append((char) 2);
            sb.append((char) (c - '`'));
            return 2;
        } else if (c >= 128) {
            sb.append("\u0001\u001e");
            return 2 + encodeChar((char) (c - 128), sb);
        } else {
            throw new IllegalArgumentException("Illegal character: " + c);
        }
    }

    private static String encodeToCodewords(CharSequence sb, int startPos) {
        int v = (sb.charAt(startPos) * 1600) + (sb.charAt(startPos + 1) * '(') + sb.charAt(startPos + 2) + 1;
        return new String(new char[]{(char) (v / 256), (char) (v % 256)});
    }
}
