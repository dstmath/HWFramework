package com.huawei.zxing.datamatrix.encoder;

import huawei.android.widget.DialogContentHelper.Dex;
import huawei.android.widget.ViewDragHelper;

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
                    case ViewDragHelper.STATE_DRAGGING /*1*/:
                        context.writeCodeword('\u00e6');
                        context.signalEncoderChange(1);
                    case ViewDragHelper.STATE_SETTLING /*2*/:
                        context.writeCodeword('\u00ef');
                        context.signalEncoderChange(2);
                        break;
                    case ViewDragHelper.DIRECTION_ALL /*3*/:
                        context.writeCodeword('\u00ee');
                        context.signalEncoderChange(3);
                        break;
                    case ViewDragHelper.EDGE_TOP /*4*/:
                        context.writeCodeword('\u00f0');
                        context.signalEncoderChange(4);
                        break;
                    case Dex.DIALOG_BODY_TWO_IMAGES /*5*/:
                        context.writeCodeword('\u00e7');
                        context.signalEncoderChange(5);
                    default:
                        throw new IllegalStateException("Illegal mode: " + newMode);
                }
            } else if (HighLevelEncoder.isExtendedASCII(c)) {
                context.writeCodeword('\u00eb');
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
