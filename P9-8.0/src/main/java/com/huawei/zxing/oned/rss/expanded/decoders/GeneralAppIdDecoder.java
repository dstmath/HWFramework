package com.huawei.zxing.oned.rss.expanded.decoders;

import com.huawei.android.telephony.DisconnectCauseEx;
import com.huawei.android.util.JlogConstantsEx;
import com.huawei.zxing.FormatException;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.common.BitArray;

final class GeneralAppIdDecoder {
    private final StringBuilder buffer = new StringBuilder();
    private final CurrentParsingState current = new CurrentParsingState();
    private final BitArray information;

    GeneralAppIdDecoder(BitArray information) {
        this.information = information;
    }

    String decodeAllCodes(StringBuilder buff, int initialPosition) throws NotFoundException, FormatException {
        int currentPosition = initialPosition;
        String remaining = null;
        while (true) {
            DecodedInformation info = decodeGeneralPurposeField(currentPosition, remaining);
            String parsedFields = FieldParser.parseFieldsInGeneralPurpose(info.getNewString());
            if (parsedFields != null) {
                buff.append(parsedFields);
            }
            if (info.isRemaining()) {
                remaining = String.valueOf(info.getRemainingValue());
            } else {
                remaining = null;
            }
            if (currentPosition == info.getNewPosition()) {
                return buff.toString();
            }
            currentPosition = info.getNewPosition();
        }
    }

    private boolean isStillNumeric(int pos) {
        boolean z = true;
        if (pos + 7 > this.information.getSize()) {
            if (pos + 4 > this.information.getSize()) {
                z = false;
            }
            return z;
        }
        for (int i = pos; i < pos + 3; i++) {
            if (this.information.get(i)) {
                return true;
            }
        }
        return this.information.get(pos + 3);
    }

    private DecodedNumeric decodeNumeric(int pos) throws FormatException {
        int numeric;
        if (pos + 7 > this.information.getSize()) {
            numeric = extractNumericValueFromBitArray(pos, 4);
            if (numeric == 0) {
                return new DecodedNumeric(this.information.getSize(), 10, 10);
            }
            return new DecodedNumeric(this.information.getSize(), numeric - 1, 10);
        }
        numeric = extractNumericValueFromBitArray(pos, 7);
        return new DecodedNumeric(pos + 7, (numeric - 8) / 11, (numeric - 8) % 11);
    }

    int extractNumericValueFromBitArray(int pos, int bits) {
        return extractNumericValueFromBitArray(this.information, pos, bits);
    }

    static int extractNumericValueFromBitArray(BitArray information, int pos, int bits) {
        int value = 0;
        for (int i = 0; i < bits; i++) {
            if (information.get(pos + i)) {
                value |= 1 << ((bits - i) - 1);
            }
        }
        return value;
    }

    DecodedInformation decodeGeneralPurposeField(int pos, String remaining) throws FormatException {
        this.buffer.setLength(0);
        if (remaining != null) {
            this.buffer.append(remaining);
        }
        this.current.setPosition(pos);
        DecodedInformation lastDecoded = parseBlocks();
        if (lastDecoded == null || !lastDecoded.isRemaining()) {
            return new DecodedInformation(this.current.getPosition(), this.buffer.toString());
        }
        return new DecodedInformation(this.current.getPosition(), this.buffer.toString(), lastDecoded.getRemainingValue());
    }

    private DecodedInformation parseBlocks() throws FormatException {
        BlockParsedResult result;
        while (true) {
            int initialPosition = this.current.getPosition();
            boolean isFinished;
            if (this.current.isAlpha()) {
                result = parseAlphaBlock();
                isFinished = result.isFinished();
            } else if (this.current.isIsoIec646()) {
                result = parseIsoIec646Block();
                isFinished = result.isFinished();
            } else {
                result = parseNumericBlock();
                isFinished = result.isFinished();
            }
            if (((initialPosition != this.current.getPosition()) || (isFinished ^ 1) == 0) && !isFinished) {
            }
        }
        return result.getDecodedInformation();
    }

    private BlockParsedResult parseNumericBlock() throws FormatException {
        while (isStillNumeric(this.current.getPosition())) {
            DecodedNumeric numeric = decodeNumeric(this.current.getPosition());
            this.current.setPosition(numeric.getNewPosition());
            if (numeric.isFirstDigitFNC1()) {
                DecodedInformation information;
                if (numeric.isSecondDigitFNC1()) {
                    information = new DecodedInformation(this.current.getPosition(), this.buffer.toString());
                } else {
                    information = new DecodedInformation(this.current.getPosition(), this.buffer.toString(), numeric.getSecondDigit());
                }
                return new BlockParsedResult(information, true);
            }
            this.buffer.append(numeric.getFirstDigit());
            if (numeric.isSecondDigitFNC1()) {
                return new BlockParsedResult(new DecodedInformation(this.current.getPosition(), this.buffer.toString()), true);
            }
            this.buffer.append(numeric.getSecondDigit());
        }
        if (isNumericToAlphaNumericLatch(this.current.getPosition())) {
            this.current.setAlpha();
            this.current.incrementPosition(4);
        }
        return new BlockParsedResult(false);
    }

    private BlockParsedResult parseIsoIec646Block() throws FormatException {
        while (isStillIsoIec646(this.current.getPosition())) {
            DecodedChar iso = decodeIsoIec646(this.current.getPosition());
            this.current.setPosition(iso.getNewPosition());
            if (iso.isFNC1()) {
                return new BlockParsedResult(new DecodedInformation(this.current.getPosition(), this.buffer.toString()), true);
            }
            this.buffer.append(iso.getValue());
        }
        if (isAlphaOr646ToNumericLatch(this.current.getPosition())) {
            this.current.incrementPosition(3);
            this.current.setNumeric();
        } else if (isAlphaTo646ToAlphaLatch(this.current.getPosition())) {
            if (this.current.getPosition() + 5 < this.information.getSize()) {
                this.current.incrementPosition(5);
            } else {
                this.current.setPosition(this.information.getSize());
            }
            this.current.setAlpha();
        }
        return new BlockParsedResult(false);
    }

    private BlockParsedResult parseAlphaBlock() {
        while (isStillAlpha(this.current.getPosition())) {
            DecodedChar alpha = decodeAlphanumeric(this.current.getPosition());
            this.current.setPosition(alpha.getNewPosition());
            if (alpha.isFNC1()) {
                return new BlockParsedResult(new DecodedInformation(this.current.getPosition(), this.buffer.toString()), true);
            }
            this.buffer.append(alpha.getValue());
        }
        if (isAlphaOr646ToNumericLatch(this.current.getPosition())) {
            this.current.incrementPosition(3);
            this.current.setNumeric();
        } else if (isAlphaTo646ToAlphaLatch(this.current.getPosition())) {
            if (this.current.getPosition() + 5 < this.information.getSize()) {
                this.current.incrementPosition(5);
            } else {
                this.current.setPosition(this.information.getSize());
            }
            this.current.setIsoIec646();
        }
        return new BlockParsedResult(false);
    }

    private boolean isStillIsoIec646(int pos) {
        boolean z = true;
        if (pos + 5 > this.information.getSize()) {
            return false;
        }
        int fiveBitValue = extractNumericValueFromBitArray(pos, 5);
        if (fiveBitValue >= 5 && fiveBitValue < 16) {
            return true;
        }
        if (pos + 7 > this.information.getSize()) {
            return false;
        }
        int sevenBitValue = extractNumericValueFromBitArray(pos, 7);
        if (sevenBitValue >= 64 && sevenBitValue < JlogConstantsEx.JLID_CONTACT_BIND_EDITOR_FOR_NEW) {
            return true;
        }
        if (pos + 8 > this.information.getSize()) {
            return false;
        }
        int eightBitValue = extractNumericValueFromBitArray(pos, 8);
        if (eightBitValue < 232 || eightBitValue >= 253) {
            z = false;
        }
        return z;
    }

    private DecodedChar decodeIsoIec646(int pos) throws FormatException {
        int fiveBitValue = extractNumericValueFromBitArray(pos, 5);
        if (fiveBitValue == 15) {
            return new DecodedChar(pos + 5, '$');
        }
        if (fiveBitValue >= 5 && fiveBitValue < 15) {
            return new DecodedChar(pos + 5, (char) ((fiveBitValue + 48) - 5));
        }
        int sevenBitValue = extractNumericValueFromBitArray(pos, 7);
        if (sevenBitValue >= 64 && sevenBitValue < 90) {
            return new DecodedChar(pos + 7, (char) (sevenBitValue + 1));
        }
        if (sevenBitValue >= 90 && sevenBitValue < JlogConstantsEx.JLID_CONTACT_BIND_EDITOR_FOR_NEW) {
            return new DecodedChar(pos + 7, (char) (sevenBitValue + 7));
        }
        char c;
        switch (extractNumericValueFromBitArray(pos, 8)) {
            case 232:
                c = '!';
                break;
            case 233:
                c = '\"';
                break;
            case 234:
                c = '%';
                break;
            case 235:
                c = '&';
                break;
            case 236:
                c = '\'';
                break;
            case 237:
                c = '(';
                break;
            case 238:
                c = ')';
                break;
            case 239:
                c = '*';
                break;
            case 240:
                c = '+';
                break;
            case 241:
                c = ',';
                break;
            case 242:
                c = '-';
                break;
            case 243:
                c = '.';
                break;
            case 244:
                c = '/';
                break;
            case 245:
                c = ':';
                break;
            case 246:
                c = ';';
                break;
            case 247:
                c = '<';
                break;
            case 248:
                c = '=';
                break;
            case 249:
                c = '>';
                break;
            case 250:
                c = '?';
                break;
            case 251:
                c = '_';
                break;
            case 252:
                c = ' ';
                break;
            default:
                throw FormatException.getFormatInstance();
        }
        return new DecodedChar(pos + 8, c);
    }

    private boolean isStillAlpha(int pos) {
        boolean z = true;
        if (pos + 5 > this.information.getSize()) {
            return false;
        }
        int fiveBitValue = extractNumericValueFromBitArray(pos, 5);
        if (fiveBitValue >= 5 && fiveBitValue < 16) {
            return true;
        }
        if (pos + 6 > this.information.getSize()) {
            return false;
        }
        int sixBitValue = extractNumericValueFromBitArray(pos, 6);
        if (sixBitValue < 16 || sixBitValue >= 63) {
            z = false;
        }
        return z;
    }

    private DecodedChar decodeAlphanumeric(int pos) {
        int fiveBitValue = extractNumericValueFromBitArray(pos, 5);
        if (fiveBitValue == 15) {
            return new DecodedChar(pos + 5, '$');
        }
        if (fiveBitValue >= 5 && fiveBitValue < 15) {
            return new DecodedChar(pos + 5, (char) ((fiveBitValue + 48) - 5));
        }
        int sixBitValue = extractNumericValueFromBitArray(pos, 6);
        if (sixBitValue >= 32 && sixBitValue < 58) {
            return new DecodedChar(pos + 6, (char) (sixBitValue + 33));
        }
        char c;
        switch (sixBitValue) {
            case 58:
                c = '*';
                break;
            case 59:
                c = ',';
                break;
            case 60:
                c = '-';
                break;
            case DisconnectCauseEx.CALL_FAIL_NO_ANSWER_FROM_USER /*61*/:
                c = '.';
                break;
            case DisconnectCauseEx.CALL_FAIL_DESTINATION_OUT_OF_ORDER /*62*/:
                c = '/';
                break;
            default:
                throw new IllegalStateException("Decoding invalid alphanumeric value: " + sixBitValue);
        }
        return new DecodedChar(pos + 6, c);
    }

    private boolean isAlphaTo646ToAlphaLatch(int pos) {
        if (pos + 1 > this.information.getSize()) {
            return false;
        }
        int i = 0;
        while (i < 5 && i + pos < this.information.getSize()) {
            if (i == 2) {
                if (!this.information.get(pos + 2)) {
                    return false;
                }
            } else if (this.information.get(pos + i)) {
                return false;
            }
            i++;
        }
        return true;
    }

    private boolean isAlphaOr646ToNumericLatch(int pos) {
        if (pos + 3 > this.information.getSize()) {
            return false;
        }
        for (int i = pos; i < pos + 3; i++) {
            if (this.information.get(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean isNumericToAlphaNumericLatch(int pos) {
        if (pos + 1 > this.information.getSize()) {
            return false;
        }
        int i = 0;
        while (i < 4 && i + pos < this.information.getSize()) {
            if (this.information.get(pos + i)) {
                return false;
            }
            i++;
        }
        return true;
    }
}
