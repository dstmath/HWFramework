package android.icu.text;

import android.icu.lang.UCharacterEnums;

class CharsetRecog_UTF8 extends CharsetRecognizer {
    CharsetRecog_UTF8() {
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return "UTF-8";
    }

    /* access modifiers changed from: package-private */
    public CharsetMatch match(CharsetDetector det) {
        int trailBytes;
        boolean hasBOM = false;
        int numValid = 0;
        int numInvalid = 0;
        byte[] input = det.fRawInput;
        int b = 0;
        if (det.fRawLength >= 3 && (input[0] & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED) == 239 && (input[1] & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED) == 187 && (input[2] & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED) == 191) {
            hasBOM = true;
        }
        while (true) {
            int i = b;
            if (i >= det.fRawLength) {
                break;
            }
            byte b2 = input[i];
            if ((b2 & Bidi.LEVEL_OVERRIDE) != 0) {
                if ((b2 & 224) == 192) {
                    trailBytes = 1;
                } else if ((b2 & 240) == 224) {
                    trailBytes = 2;
                } else if ((b2 & 248) == 240) {
                    trailBytes = 3;
                } else {
                    numInvalid++;
                }
                while (true) {
                    i++;
                    if (i < det.fRawLength) {
                        if ((input[i] & 192) == 128) {
                            trailBytes--;
                            if (trailBytes == 0) {
                                numValid++;
                                break;
                            }
                        } else {
                            numInvalid++;
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            b = i + 1;
        }
        int confidence = 0;
        if (hasBOM && numInvalid == 0) {
            confidence = 100;
        } else if (hasBOM && numValid > numInvalid * 10) {
            confidence = 80;
        } else if (numValid > 3 && numInvalid == 0) {
            confidence = 100;
        } else if (numValid > 0 && numInvalid == 0) {
            confidence = 80;
        } else if (numValid == 0 && numInvalid == 0) {
            confidence = 15;
        } else if (numValid > numInvalid * 10) {
            confidence = 25;
        }
        if (confidence == 0) {
            return null;
        }
        return new CharsetMatch(det, this, confidence);
    }
}
