package com.huawei.zxing.datamatrix.encoder;

/* access modifiers changed from: package-private */
public final class X12Encoder extends C40Encoder {
    X12Encoder() {
    }

    @Override // com.huawei.zxing.datamatrix.encoder.C40Encoder, com.huawei.zxing.datamatrix.encoder.Encoder
    public int getEncodingMode() {
        return 3;
    }

    @Override // com.huawei.zxing.datamatrix.encoder.C40Encoder, com.huawei.zxing.datamatrix.encoder.Encoder
    public void encode(EncoderContext context) {
        StringBuilder buffer = new StringBuilder();
        while (true) {
            if (!context.hasMoreCharacters()) {
                break;
            }
            char c = context.getCurrentChar();
            context.pos++;
            encodeChar(c, buffer);
            if (buffer.length() % 3 == 0) {
                writeNextTriplet(context, buffer);
                int newMode = HighLevelEncoder.lookAheadTest(context.getMessage(), context.pos, getEncodingMode());
                if (newMode != getEncodingMode()) {
                    context.signalEncoderChange(newMode);
                    break;
                }
            }
        }
        handleEOD(context, buffer);
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.zxing.datamatrix.encoder.C40Encoder
    public int encodeChar(char c, StringBuilder sb) {
        if (c == '\r') {
            sb.append((char) 0);
        } else if (c == '*') {
            sb.append((char) 1);
        } else if (c == '>') {
            sb.append((char) 2);
        } else if (c == ' ') {
            sb.append((char) 3);
        } else if (c >= '0' && c <= '9') {
            sb.append((char) ((c - '0') + 4));
        } else if (c < 'A' || c > 'Z') {
            HighLevelEncoder.illegalCharacter(c);
        } else {
            sb.append((char) ((c - 'A') + 14));
        }
        return 1;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.zxing.datamatrix.encoder.C40Encoder
    public void handleEOD(EncoderContext context, StringBuilder buffer) {
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
