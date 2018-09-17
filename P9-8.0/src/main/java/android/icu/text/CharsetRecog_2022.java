package android.icu.text;

import android.icu.lang.UCharacterEnums.ECharacterCategory;

abstract class CharsetRecog_2022 extends CharsetRecognizer {

    static class CharsetRecog_2022CN extends CharsetRecog_2022 {
        private byte[][] escapeSequences = new byte[][]{new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 36, (byte) 41, (byte) 65}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 36, (byte) 41, (byte) 71}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 36, (byte) 42, (byte) 72}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 36, (byte) 41, (byte) 69}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 36, (byte) 43, (byte) 73}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 36, (byte) 43, (byte) 74}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 36, (byte) 43, (byte) 75}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 36, (byte) 43, (byte) 76}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 36, (byte) 43, (byte) 77}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 78}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 79}};

        CharsetRecog_2022CN() {
        }

        String getName() {
            return "ISO-2022-CN";
        }

        CharsetMatch match(CharsetDetector det) {
            int confidence = match(det.fInputBytes, det.fInputLen, this.escapeSequences);
            return confidence == 0 ? null : new CharsetMatch(det, this, confidence);
        }
    }

    static class CharsetRecog_2022JP extends CharsetRecog_2022 {
        private byte[][] escapeSequences = new byte[][]{new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 36, (byte) 40, (byte) 67}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 36, (byte) 40, (byte) 68}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 36, (byte) 64}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 36, (byte) 65}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 36, (byte) 66}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 38, (byte) 64}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 40, (byte) 66}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 40, (byte) 72}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 40, (byte) 73}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 40, (byte) 74}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 46, (byte) 65}, new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 46, (byte) 70}};

        CharsetRecog_2022JP() {
        }

        String getName() {
            return "ISO-2022-JP";
        }

        CharsetMatch match(CharsetDetector det) {
            int confidence = match(det.fInputBytes, det.fInputLen, this.escapeSequences);
            return confidence == 0 ? null : new CharsetMatch(det, this, confidence);
        }
    }

    static class CharsetRecog_2022KR extends CharsetRecog_2022 {
        private byte[][] escapeSequences = new byte[][]{new byte[]{ECharacterCategory.OTHER_SYMBOL, (byte) 36, (byte) 41, (byte) 67}};

        CharsetRecog_2022KR() {
        }

        String getName() {
            return "ISO-2022-KR";
        }

        CharsetMatch match(CharsetDetector det) {
            int confidence = match(det.fInputBytes, det.fInputLen, this.escapeSequences);
            return confidence == 0 ? null : new CharsetMatch(det, this, confidence);
        }
    }

    CharsetRecog_2022() {
    }

    int match(byte[] text, int textLen, byte[][] escapeSequences) {
        int hits = 0;
        int misses = 0;
        int shifts = 0;
        int i = 0;
        while (i < textLen) {
            if (text[i] == ECharacterCategory.OTHER_SYMBOL) {
                for (byte[] seq : escapeSequences) {
                    if (textLen - i >= seq.length) {
                        int j = 1;
                        while (j < seq.length) {
                            if (seq[j] == text[i + j]) {
                                j++;
                            }
                        }
                        hits++;
                        i += seq.length - 1;
                        break;
                    }
                }
                misses++;
            }
            if (text[i] == (byte) 14 || text[i] == (byte) 15) {
                shifts++;
                i++;
            } else {
                i++;
            }
        }
        if (hits == 0) {
            return 0;
        }
        int quality = ((hits * 100) - (misses * 100)) / (hits + misses);
        if (hits + shifts < 5) {
            quality -= (5 - (hits + shifts)) * 10;
        }
        if (quality < 0) {
            quality = 0;
        }
        return quality;
    }
}
