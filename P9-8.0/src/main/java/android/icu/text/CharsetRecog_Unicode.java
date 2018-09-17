package android.icu.text;

abstract class CharsetRecog_Unicode extends CharsetRecognizer {

    static class CharsetRecog_UTF_16_BE extends CharsetRecog_Unicode {
        CharsetRecog_UTF_16_BE() {
        }

        String getName() {
            return "UTF-16BE";
        }

        CharsetMatch match(CharsetDetector det) {
            byte[] input = det.fRawInput;
            int confidence = 10;
            int bytesToCheck = Math.min(input.length, 30);
            int charIndex = 0;
            while (charIndex < bytesToCheck - 1) {
                int codeUnit = CharsetRecog_Unicode.codeUnit16FromBytes(input[charIndex], input[charIndex + 1]);
                if (charIndex != 0 || codeUnit != 65279) {
                    confidence = CharsetRecog_Unicode.adjustConfidence(codeUnit, confidence);
                    if (confidence == 0 || confidence == 100) {
                        break;
                    }
                    charIndex += 2;
                } else {
                    confidence = 100;
                    break;
                }
            }
            if (bytesToCheck < 4 && confidence < 100) {
                confidence = 0;
            }
            if (confidence > 0) {
                return new CharsetMatch(det, this, confidence);
            }
            return null;
        }
    }

    static class CharsetRecog_UTF_16_LE extends CharsetRecog_Unicode {
        CharsetRecog_UTF_16_LE() {
        }

        String getName() {
            return "UTF-16LE";
        }

        CharsetMatch match(CharsetDetector det) {
            byte[] input = det.fRawInput;
            int confidence = 10;
            int bytesToCheck = Math.min(input.length, 30);
            int charIndex = 0;
            while (charIndex < bytesToCheck - 1) {
                int codeUnit = CharsetRecog_Unicode.codeUnit16FromBytes(input[charIndex + 1], input[charIndex]);
                if (charIndex != 0 || codeUnit != 65279) {
                    confidence = CharsetRecog_Unicode.adjustConfidence(codeUnit, confidence);
                    if (confidence == 0 || confidence == 100) {
                        break;
                    }
                    charIndex += 2;
                } else {
                    confidence = 100;
                    break;
                }
            }
            if (bytesToCheck < 4 && confidence < 100) {
                confidence = 0;
            }
            if (confidence > 0) {
                return new CharsetMatch(det, this, confidence);
            }
            return null;
        }
    }

    static abstract class CharsetRecog_UTF_32 extends CharsetRecog_Unicode {
        abstract int getChar(byte[] bArr, int i);

        abstract String getName();

        CharsetRecog_UTF_32() {
        }

        CharsetMatch match(CharsetDetector det) {
            CharsetMatch charsetMatch = null;
            byte[] input = det.fRawInput;
            int limit = (det.fRawLength / 4) * 4;
            int numValid = 0;
            int numInvalid = 0;
            boolean hasBOM = false;
            int confidence = 0;
            if (limit == 0) {
                return null;
            }
            if (getChar(input, 0) == 65279) {
                hasBOM = true;
            }
            for (int i = 0; i < limit; i += 4) {
                int ch = getChar(input, i);
                if (ch < 0 || ch >= 1114111 || (ch >= 55296 && ch <= 57343)) {
                    numInvalid++;
                } else {
                    numValid++;
                }
            }
            if (hasBOM && numInvalid == 0) {
                confidence = 100;
            } else if (hasBOM && numValid > numInvalid * 10) {
                confidence = 80;
            } else if (numValid > 3 && numInvalid == 0) {
                confidence = 100;
            } else if (numValid > 0 && numInvalid == 0) {
                confidence = 80;
            } else if (numValid > numInvalid * 10) {
                confidence = 25;
            }
            if (confidence != 0) {
                charsetMatch = new CharsetMatch(det, this, confidence);
            }
            return charsetMatch;
        }
    }

    static class CharsetRecog_UTF_32_BE extends CharsetRecog_UTF_32 {
        CharsetRecog_UTF_32_BE() {
        }

        int getChar(byte[] input, int index) {
            return ((((input[index + 0] & 255) << 24) | ((input[index + 1] & 255) << 16)) | ((input[index + 2] & 255) << 8)) | (input[index + 3] & 255);
        }

        String getName() {
            return "UTF-32BE";
        }
    }

    static class CharsetRecog_UTF_32_LE extends CharsetRecog_UTF_32 {
        CharsetRecog_UTF_32_LE() {
        }

        int getChar(byte[] input, int index) {
            return ((((input[index + 3] & 255) << 24) | ((input[index + 2] & 255) << 16)) | ((input[index + 1] & 255) << 8)) | (input[index + 0] & 255);
        }

        String getName() {
            return "UTF-32LE";
        }
    }

    abstract String getName();

    abstract CharsetMatch match(CharsetDetector charsetDetector);

    CharsetRecog_Unicode() {
    }

    static int codeUnit16FromBytes(byte hi, byte lo) {
        return ((hi & 255) << 8) | (lo & 255);
    }

    static int adjustConfidence(int codeUnit, int confidence) {
        if (codeUnit == 0) {
            confidence -= 10;
        } else if ((codeUnit >= 32 && codeUnit <= 255) || codeUnit == 10) {
            confidence += 10;
        }
        if (confidence < 0) {
            return 0;
        }
        if (confidence > 100) {
            return 100;
        }
        return confidence;
    }
}
