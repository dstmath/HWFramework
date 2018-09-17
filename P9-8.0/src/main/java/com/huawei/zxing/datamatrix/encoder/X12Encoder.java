package com.huawei.zxing.datamatrix.encoder;

final class X12Encoder extends C40Encoder {
    X12Encoder() {
    }

    public int getEncodingMode() {
        return 3;
    }

    public void encode(EncoderContext context) {
        StringBuilder buffer = new StringBuilder();
        while (context.hasMoreCharacters()) {
            char c = context.getCurrentChar();
            context.pos++;
            encodeChar(c, buffer);
            if (buffer.length() % 3 == 0) {
                C40Encoder.writeNextTriplet(context, buffer);
                int newMode = HighLevelEncoder.lookAheadTest(context.getMessage(), context.pos, getEncodingMode());
                if (newMode != getEncodingMode()) {
                    context.signalEncoderChange(newMode);
                    break;
                }
            }
        }
        handleEOD(context, buffer);
    }

    int encodeChar(char c, StringBuilder sb) {
        if (c == 13) {
            sb.append(0);
        } else if (c == '*') {
            sb.append(1);
        } else if (c == '>') {
            sb.append(2);
        } else if (c == ' ') {
            sb.append(3);
        } else if (c >= '0' && c <= '9') {
            sb.append((char) ((c - 48) + 4));
        } else if (c < 'A' || c > 'Z') {
            HighLevelEncoder.illegalCharacter(c);
        } else {
            sb.append((char) ((c - 65) + 14));
        }
        return 1;
    }

    void handleEOD(EncoderContext context, StringBuilder buffer) {
        context.updateSymbolInfo();
        int available = context.getSymbolInfo().getDataCapacity() - context.getCodewordCount();
        int count = buffer.length();
        if (count == 2) {
            context.writeCodeword(254);
            context.pos -= 2;
            context.signalEncoderChange(0);
        } else if (count == 1) {
            context.pos--;
            if (available > 1) {
                context.writeCodeword(254);
            }
            context.signalEncoderChange(0);
        }
    }
}
