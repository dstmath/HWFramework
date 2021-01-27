package ohos.global.icu.text;

abstract class CharsetRecog_2022 extends CharsetRecognizer {
    CharsetRecog_2022() {
    }

    /* access modifiers changed from: package-private */
    public int match(byte[] bArr, int i, byte[][] bArr2) {
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        while (i2 < i) {
            if (bArr[i2] == 27) {
                for (byte[] bArr3 : bArr2) {
                    if (i - i2 >= bArr3.length) {
                        for (int i6 = 1; i6 < bArr3.length; i6++) {
                            if (bArr3[i6] == bArr[i2 + i6]) {
                            }
                        }
                        i3++;
                        i2 += bArr3.length - 1;
                        break;
                    }
                }
                i4++;
            }
            if (bArr[i2] == 14 || bArr[i2] == 15) {
                i5++;
                i2++;
            } else {
                i2++;
            }
        }
        if (i3 == 0) {
            return 0;
        }
        int i7 = ((i3 * 100) - (i4 * 100)) / (i4 + i3);
        int i8 = i3 + i5;
        if (i8 < 5) {
            i7 -= (5 - i8) * 10;
        }
        if (i7 < 0) {
            return 0;
        }
        return i7;
    }

    static class CharsetRecog_2022JP extends CharsetRecog_2022 {
        private byte[][] escapeSequences = {new byte[]{27, 36, 40, 67}, new byte[]{27, 36, 40, 68}, new byte[]{27, 36, 64}, new byte[]{27, 36, 65}, new byte[]{27, 36, 66}, new byte[]{27, 38, 64}, new byte[]{27, 40, 66}, new byte[]{27, 40, 72}, new byte[]{27, 40, 73}, new byte[]{27, 40, 74}, new byte[]{27, 46, 65}, new byte[]{27, 46, 70}};

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.text.CharsetRecognizer
        public String getName() {
            return "ISO-2022-JP";
        }

        CharsetRecog_2022JP() {
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.text.CharsetRecognizer
        public CharsetMatch match(CharsetDetector charsetDetector) {
            int match = match(charsetDetector.fInputBytes, charsetDetector.fInputLen, this.escapeSequences);
            if (match == 0) {
                return null;
            }
            return new CharsetMatch(charsetDetector, this, match);
        }
    }

    static class CharsetRecog_2022KR extends CharsetRecog_2022 {
        private byte[][] escapeSequences = {new byte[]{27, 36, 41, 67}};

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.text.CharsetRecognizer
        public String getName() {
            return "ISO-2022-KR";
        }

        CharsetRecog_2022KR() {
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.text.CharsetRecognizer
        public CharsetMatch match(CharsetDetector charsetDetector) {
            int match = match(charsetDetector.fInputBytes, charsetDetector.fInputLen, this.escapeSequences);
            if (match == 0) {
                return null;
            }
            return new CharsetMatch(charsetDetector, this, match);
        }
    }

    static class CharsetRecog_2022CN extends CharsetRecog_2022 {
        private byte[][] escapeSequences = {new byte[]{27, 36, 41, 65}, new byte[]{27, 36, 41, 71}, new byte[]{27, 36, 42, 72}, new byte[]{27, 36, 41, 69}, new byte[]{27, 36, 43, 73}, new byte[]{27, 36, 43, 74}, new byte[]{27, 36, 43, 75}, new byte[]{27, 36, 43, 76}, new byte[]{27, 36, 43, 77}, new byte[]{27, 78}, new byte[]{27, 79}};

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.text.CharsetRecognizer
        public String getName() {
            return "ISO-2022-CN";
        }

        CharsetRecog_2022CN() {
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.text.CharsetRecognizer
        public CharsetMatch match(CharsetDetector charsetDetector) {
            int match = match(charsetDetector.fInputBytes, charsetDetector.fInputLen, this.escapeSequences);
            if (match == 0) {
                return null;
            }
            return new CharsetMatch(charsetDetector, this, match);
        }
    }
}
