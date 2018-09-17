package com.huawei.zxing.datamatrix.encoder;

final class ASCIIEncoder implements Encoder {
    ASCIIEncoder() {
    }

    public int getEncodingMode() {
        return 0;
    }

    public void encode(EncoderContext context) {
        if (HighLevelEncoder.determineConsecutiveDigitCount(context.getMessage(), context.pos) >= 2) {
            context.writeCodeword(encodeASCIIDigits(context.getMessage().charAt(context.pos), context.getMessage().charAt(context.pos + 1)));
            context.pos += 2;
        } else {
            char c = context.getCurrentChar();
            int newMode = HighLevelEncoder.lookAheadTest(context.getMessage(), context.pos, getEncodingMode());
            if (newMode != getEncodingMode()) {
                switch (newMode) {
                    case 1:
                        context.writeCodeword(230);
                        context.signalEncoderChange(1);
                        return;
                    case 2:
                        context.writeCodeword(239);
                        context.signalEncoderChange(2);
                        break;
                    case 3:
                        context.writeCodeword(238);
                        context.signalEncoderChange(3);
                        break;
                    case 4:
                        context.writeCodeword(240);
                        context.signalEncoderChange(4);
                        break;
                    case 5:
                        context.writeCodeword(231);
                        context.signalEncoderChange(5);
                        return;
                    default:
                        throw new IllegalStateException("Illegal mode: " + newMode);
                }
            } else if (HighLevelEncoder.isExtendedASCII(c)) {
                context.writeCodeword(235);
                context.writeCodeword((char) ((c - 128) + 1));
                context.pos++;
            } else {
                context.writeCodeword((char) (c + 1));
                context.pos++;
            }
        }
    }

    private static char encodeASCIIDigits(char digit1, char digit2) {
        if (HighLevelEncoder.isDigit(digit1) && HighLevelEncoder.isDigit(digit2)) {
            return (char) ((((digit1 - 48) * 10) + (digit2 - 48)) + 130);
        }
        throw new IllegalArgumentException("not digits: " + digit1 + digit2);
    }
}
