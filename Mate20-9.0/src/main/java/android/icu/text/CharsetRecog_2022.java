package android.icu.text;

import android.icu.lang.UCharacterEnums;

abstract class CharsetRecog_2022 extends CharsetRecognizer {

    static class CharsetRecog_2022CN extends CharsetRecog_2022 {
        private byte[][] escapeSequences = {new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 36, 41, 65}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 36, 41, 71}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 36, 42, 72}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 36, 41, 69}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 36, 43, 73}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 36, 43, 74}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 36, 43, 75}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 36, 43, 76}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 36, 43, 77}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 78}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 79}};

        CharsetRecog_2022CN() {
        }

        /* access modifiers changed from: package-private */
        public String getName() {
            return "ISO-2022-CN";
        }

        /* access modifiers changed from: package-private */
        public CharsetMatch match(CharsetDetector det) {
            int confidence = match(det.fInputBytes, det.fInputLen, this.escapeSequences);
            if (confidence == 0) {
                return null;
            }
            return new CharsetMatch(det, this, confidence);
        }
    }

    static class CharsetRecog_2022JP extends CharsetRecog_2022 {
        private byte[][] escapeSequences = {new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 36, 40, 67}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 36, 40, 68}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 36, 64}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 36, 65}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 36, 66}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 38, 64}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 40, 66}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 40, 72}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 40, 73}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 40, 74}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 46, 65}, new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 46, 70}};

        CharsetRecog_2022JP() {
        }

        /* access modifiers changed from: package-private */
        public String getName() {
            return "ISO-2022-JP";
        }

        /* access modifiers changed from: package-private */
        public CharsetMatch match(CharsetDetector det) {
            int confidence = match(det.fInputBytes, det.fInputLen, this.escapeSequences);
            if (confidence == 0) {
                return null;
            }
            return new CharsetMatch(det, this, confidence);
        }
    }

    static class CharsetRecog_2022KR extends CharsetRecog_2022 {
        private byte[][] escapeSequences = {new byte[]{UCharacterEnums.ECharacterCategory.OTHER_SYMBOL, 36, 41, 67}};

        CharsetRecog_2022KR() {
        }

        /* access modifiers changed from: package-private */
        public String getName() {
            return "ISO-2022-KR";
        }

        /* access modifiers changed from: package-private */
        public CharsetMatch match(CharsetDetector det) {
            int confidence = match(det.fInputBytes, det.fInputLen, this.escapeSequences);
            if (confidence == 0) {
                return null;
            }
            return new CharsetMatch(det, this, confidence);
        }
    }

    CharsetRecog_2022() {
    }

    /* access modifiers changed from: package-private */
    public int match(byte[] text, int textLen, byte[][] escapeSequences) {
        int shifts = 0;
        int misses = 0;
        int hits = 0;
        int i = 0;
        while (i < textLen) {
            if (text[i] == 27) {
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
            if (text[i] == 14 || text[i] == 15) {
                shifts++;
                i++;
            } else {
                i++;
            }
        }
        if (hits == 0) {
            return 0;
        }
        int quality = ((100 * hits) - (100 * misses)) / (hits + misses);
        if (hits + shifts < 5) {
            quality -= (5 - (hits + shifts)) * 10;
        }
        if (quality < 0) {
            quality = 0;
        }
        return quality;
    }
}
