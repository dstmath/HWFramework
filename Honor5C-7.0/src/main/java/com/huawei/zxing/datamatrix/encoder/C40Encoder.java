package com.huawei.zxing.datamatrix.encoder;

import com.huawei.immersion.Vibetonz;
import com.huawei.lcagent.client.MetricConstant;

class C40Encoder implements Encoder {
    C40Encoder() {
    }

    public int getEncodingMode() {
        return 1;
    }

    public void encode(EncoderContext context) {
        StringBuilder buffer = new StringBuilder();
        while (context.hasMoreCharacters()) {
            char c = context.getCurrentChar();
            context.pos++;
            int lastCharSize = encodeChar(c, buffer);
            int curCodewordCount = context.getCodewordCount() + ((buffer.length() / 3) * 2);
            context.updateSymbolInfo(curCodewordCount);
            int available = context.getSymbolInfo().getDataCapacity() - curCodewordCount;
            if (!context.hasMoreCharacters()) {
                StringBuilder removed = new StringBuilder();
                if (buffer.length() % 3 == 2 && (available < 2 || available > 2)) {
                    lastCharSize = backtrackOneCharacter(context, buffer, removed, lastCharSize);
                }
                while (buffer.length() % 3 == 1 && ((lastCharSize <= 3 && available != 1) || lastCharSize > 3)) {
                    lastCharSize = backtrackOneCharacter(context, buffer, removed, lastCharSize);
                }
            } else if (buffer.length() % 3 == 0) {
                int newMode = HighLevelEncoder.lookAheadTest(context.getMessage(), context.pos, getEncodingMode());
                if (newMode != getEncodingMode()) {
                    context.signalEncoderChange(newMode);
                    break;
                }
            }
        }
        handleEOD(context, buffer);
    }

    private int backtrackOneCharacter(EncoderContext context, StringBuilder buffer, StringBuilder removed, int lastCharSize) {
        int count = buffer.length();
        buffer.delete(count - lastCharSize, count);
        context.pos--;
        lastCharSize = encodeChar(context.getCurrentChar(), removed);
        context.resetSymbolInfo();
        return lastCharSize;
    }

    static void writeNextTriplet(EncoderContext context, StringBuilder buffer) {
        context.writeCodewords(encodeToCodewords(buffer, 0));
        buffer.delete(0, 3);
    }

    void handleEOD(EncoderContext context, StringBuilder buffer) {
        int rest = buffer.length() % 3;
        int curCodewordCount = context.getCodewordCount() + ((buffer.length() / 3) * 2);
        context.updateSymbolInfo(curCodewordCount);
        int available = context.getSymbolInfo().getDataCapacity() - curCodewordCount;
        if (rest == 2) {
            buffer.append('\u0000');
            while (buffer.length() >= 3) {
                writeNextTriplet(context, buffer);
            }
            if (context.hasMoreCharacters()) {
                context.writeCodeword('\u00fe');
            }
        } else if (available == 1 && rest == 1) {
            while (buffer.length() >= 3) {
                writeNextTriplet(context, buffer);
            }
            if (context.hasMoreCharacters()) {
                context.writeCodeword('\u00fe');
            }
            context.pos--;
        } else if (rest == 0) {
            while (buffer.length() >= 3) {
                writeNextTriplet(context, buffer);
            }
            if (available > 0 || context.hasMoreCharacters()) {
                context.writeCodeword('\u00fe');
            }
        } else {
            throw new IllegalStateException("Unexpected case. Please report!");
        }
        context.signalEncoderChange(0);
    }

    int encodeChar(char c, StringBuilder sb) {
        if (c == ' ') {
            sb.append('\u0003');
            return 1;
        } else if (c >= '0' && c <= '9') {
            sb.append((char) ((c - 48) + 4));
            return 1;
        } else if (c >= 'A' && c <= 'Z') {
            sb.append((char) ((c - 65) + 14));
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
        } else if (c >= '`' && c <= '\u007f') {
            sb.append('\u0002');
            sb.append((char) (c - 96));
            return 2;
        } else if (c >= '\u0080') {
            sb.append("\u0001\u001e");
            return encodeChar((char) (c - 128), sb) + 2;
        } else {
            throw new IllegalArgumentException("Illegal character: " + c);
        }
    }

    private static String encodeToCodewords(CharSequence sb, int startPos) {
        char c1 = sb.charAt(startPos);
        char c2 = sb.charAt(startPos + 1);
        int v = (((c1 * Vibetonz.HAPTIC_EVENT_CONTACT_ALPHA_SWITCH) + (c2 * 40)) + sb.charAt(startPos + 2)) + 1;
        char cw1 = (char) (v / MetricConstant.METRIC_ID_MAX);
        char cw2 = (char) (v % MetricConstant.METRIC_ID_MAX);
        return new String(new char[]{cw1, cw2});
    }
}
