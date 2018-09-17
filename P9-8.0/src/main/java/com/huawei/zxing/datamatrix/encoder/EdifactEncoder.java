package com.huawei.zxing.datamatrix.encoder;

final class EdifactEncoder implements Encoder {
    EdifactEncoder() {
    }

    public int getEncodingMode() {
        return 4;
    }

    public void encode(EncoderContext context) {
        StringBuilder buffer = new StringBuilder();
        while (context.hasMoreCharacters()) {
            encodeChar(context.getCurrentChar(), buffer);
            context.pos++;
            if (buffer.length() >= 4) {
                context.writeCodewords(encodeToCodewords(buffer, 0));
                buffer.delete(0, 4);
                if (HighLevelEncoder.lookAheadTest(context.getMessage(), context.pos, getEncodingMode()) != getEncodingMode()) {
                    context.signalEncoderChange(0);
                    break;
                }
            }
        }
        buffer.append(31);
        handleEOD(context, buffer);
    }

    private static void handleEOD(EncoderContext context, CharSequence buffer) {
        try {
            int count = buffer.length();
            if (count != 0) {
                if (count == 1) {
                    context.updateSymbolInfo();
                    int available = context.getSymbolInfo().getDataCapacity() - context.getCodewordCount();
                    if (context.getRemainingCharacters() == 0 && available <= 2) {
                        context.signalEncoderChange(0);
                        return;
                    }
                }
                if (count > 4) {
                    throw new IllegalStateException("Count must not exceed 4");
                }
                int restChars = count - 1;
                String encoded = encodeToCodewords(buffer, 0);
                boolean restInAscii = (context.hasMoreCharacters() ^ 1) && restChars <= 2;
                if (restChars <= 2) {
                    context.updateSymbolInfo(context.getCodewordCount() + restChars);
                    if (context.getSymbolInfo().getDataCapacity() - context.getCodewordCount() >= 3) {
                        restInAscii = false;
                        context.updateSymbolInfo(context.getCodewordCount() + encoded.length());
                    }
                }
                if (restInAscii) {
                    context.resetSymbolInfo();
                    context.pos -= restChars;
                } else {
                    context.writeCodewords(encoded);
                }
                context.signalEncoderChange(0);
            }
        } finally {
            context.signalEncoderChange(0);
        }
    }

    private static void encodeChar(char c, StringBuilder sb) {
        if (c >= ' ' && c <= '?') {
            sb.append(c);
        } else if (c < '@' || c > '^') {
            HighLevelEncoder.illegalCharacter(c);
        } else {
            sb.append((char) (c - 64));
        }
    }

    private static String encodeToCodewords(CharSequence sb, int startPos) {
        int len = sb.length() - startPos;
        if (len == 0) {
            throw new IllegalStateException("StringBuilder must not be empty");
        }
        int v = (((sb.charAt(startPos) << 18) + ((len >= 2 ? sb.charAt(startPos + 1) : 0) << 12)) + ((len >= 3 ? sb.charAt(startPos + 2) : 0) << 6)) + (len >= 4 ? sb.charAt(startPos + 3) : 0);
        char cw1 = (char) ((v >> 16) & 255);
        char cw2 = (char) ((v >> 8) & 255);
        char cw3 = (char) (v & 255);
        StringBuilder res = new StringBuilder(3);
        res.append(cw1);
        if (len >= 2) {
            res.append(cw2);
        }
        if (len >= 3) {
            res.append(cw3);
        }
        return res.toString();
    }
}
