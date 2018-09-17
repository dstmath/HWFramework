package android.icu.text;

import java.util.Arrays;

abstract class CharsetRecog_mbcs extends CharsetRecognizer {

    static class CharsetRecog_big5 extends CharsetRecog_mbcs {
        static int[] commonChars = new int[]{41280, 41281, 41282, 41283, 41287, 41289, 41333, 41334, 42048, 42054, 42055, 42056, 42065, 42068, 42071, 42084, 42090, 42092, 42103, 42147, 42148, 42151, 42177, 42190, 42193, 42207, 42216, 42237, 42304, 42312, 42328, 42345, 42445, 42471, 42583, 42593, 42594, 42600, 42608, 42664, 42675, 42681, 42707, 42715, 42726, 42738, 42816, 42833, 42841, 42970, 43171, 43173, 43181, 43217, 43219, 43236, 43260, 43456, 43474, 43507, 43627, 43706, 43710, 43724, 43772, 44103, 44111, 44208, 44242, 44377, 44745, 45024, 45290, 45423, 45747, 45764, 45935, 46156, 46158, 46412, 46501, 46525, 46544, 46552, 46705, 47085, 47207, 47428, 47832, 47940, 48033, 48593, 49860, 50105, 50240, 50271};

        CharsetRecog_big5() {
        }

        boolean nextChar(iteratedChar it, CharsetDetector det) {
            it.error = false;
            int firstByte = it.nextByte(det);
            it.charValue = firstByte;
            if (firstByte < 0) {
                return false;
            }
            if (firstByte <= 127 || firstByte == 255) {
                return true;
            }
            int secondByte = it.nextByte(det);
            if (secondByte < 0) {
                return false;
            }
            it.charValue = (it.charValue << 8) | secondByte;
            if (secondByte < 64 || secondByte == 127 || secondByte == 255) {
                it.error = true;
            }
            return true;
        }

        CharsetMatch match(CharsetDetector det) {
            int confidence = match(det, commonChars);
            return confidence == 0 ? null : new CharsetMatch(det, this, confidence);
        }

        String getName() {
            return "Big5";
        }

        public String getLanguage() {
            return "zh";
        }
    }

    static abstract class CharsetRecog_euc extends CharsetRecog_mbcs {

        static class CharsetRecog_euc_jp extends CharsetRecog_euc {
            static int[] commonChars = new int[]{41377, 41378, 41379, 41382, 41404, 41418, 41419, 41430, 41431, 42146, 42148, 42150, 42152, 42154, 42155, 42156, 42157, 42159, 42161, 42163, 42165, 42167, 42169, 42171, 42173, 42175, 42176, 42177, 42179, 42180, 42182, 42183, 42184, 42185, 42186, 42187, 42190, 42191, 42192, 42206, 42207, 42209, 42210, 42212, 42216, 42217, 42218, 42219, 42220, 42223, 42226, 42227, 42402, 42403, 42404, 42406, 42407, 42410, 42413, 42415, 42416, 42419, 42421, 42423, 42424, 42425, 42431, 42435, 42438, 42439, 42440, 42441, 42443, 42448, 42453, 42454, 42455, 42462, 42464, 42465, 42469, 42473, 42474, 42475, 42476, 42477, 42483, 47273, 47572, 47854, 48072, 48880, 49079, 50410, 50940, 51133, 51896, 51955, 52188, 52689};

            CharsetRecog_euc_jp() {
            }

            String getName() {
                return "EUC-JP";
            }

            CharsetMatch match(CharsetDetector det) {
                int confidence = match(det, commonChars);
                return confidence == 0 ? null : new CharsetMatch(det, this, confidence);
            }

            public String getLanguage() {
                return "ja";
            }
        }

        static class CharsetRecog_euc_kr extends CharsetRecog_euc {
            static int[] commonChars = new int[]{45217, 45235, 45253, 45261, 45268, 45286, 45293, 45304, 45306, 45308, 45496, 45497, 45511, 45527, 45538, 45994, 46011, 46274, 46287, 46297, 46315, 46501, 46517, 46527, 46535, 46569, 46835, 47023, 47042, 47054, 47270, 47278, 47286, 47288, 47291, 47337, 47531, 47534, 47564, 47566, 47613, 47800, 47822, 47824, 47857, 48103, 48115, 48125, 48301, 48314, 48338, 48374, 48570, 48576, 48579, 48581, 48838, 48840, 48863, 48878, 48888, 48890, 49057, 49065, 49088, 49124, 49131, 49132, 49144, 49319, 49327, 49336, 49338, 49339, 49341, 49351, 49356, 49358, 49359, 49366, 49370, 49381, 49403, 49404, 49572, 49574, 49590, 49622, 49631, 49654, 49656, 50337, 50637, 50862, 51151, 51153, 51154, 51160, 51173, 51373};

            CharsetRecog_euc_kr() {
            }

            String getName() {
                return "EUC-KR";
            }

            CharsetMatch match(CharsetDetector det) {
                int confidence = match(det, commonChars);
                return confidence == 0 ? null : new CharsetMatch(det, this, confidence);
            }

            public String getLanguage() {
                return "ko";
            }
        }

        CharsetRecog_euc() {
        }

        boolean nextChar(iteratedChar it, CharsetDetector det) {
            it.error = false;
            int firstByte = it.nextByte(det);
            it.charValue = firstByte;
            if (firstByte < 0) {
                it.done = true;
            } else if (firstByte > 141) {
                int secondByte = it.nextByte(det);
                it.charValue = (it.charValue << 8) | secondByte;
                if (firstByte < 161 || firstByte > 254) {
                    if (firstByte == 142) {
                        if (secondByte < 161) {
                            it.error = true;
                        }
                    } else if (firstByte == 143) {
                        int thirdByte = it.nextByte(det);
                        it.charValue = (it.charValue << 8) | thirdByte;
                        if (thirdByte < 161) {
                            it.error = true;
                        }
                    }
                } else if (secondByte < 161) {
                    it.error = true;
                }
            }
            if (it.done) {
                return false;
            }
            return true;
        }
    }

    static class CharsetRecog_gb_18030 extends CharsetRecog_mbcs {
        static int[] commonChars = new int[]{41377, 41378, 41379, 41380, 41392, 41393, 41457, 41459, 41889, 41900, 41914, 45480, 45496, 45502, 45755, 46025, 46070, 46323, 46525, 46532, 46563, 46767, 46804, 46816, 47010, 47016, 47037, 47062, 47069, 47284, 47327, 47350, 47531, 47561, 47576, 47610, 47613, 47821, 48039, 48086, 48097, 48122, 48316, 48347, 48382, 48588, 48845, 48861, 49076, 49094, 49097, 49332, 49389, 49611, 49883, 50119, 50396, 50410, 50636, 50935, 51192, 51371, 51403, 51413, 51431, 51663, 51706, 51889, 51893, 51911, 51920, 51926, 51957, 51965, 52460, 52728, 52906, 52932, 52946, 52965, 53173, 53186, 53206, 53442, 53445, 53456, 53460, 53671, 53930, 53938, 53941, 53947, 53972, 54211, 54224, 54269, 54466, 54490, 54754, 54992};

        CharsetRecog_gb_18030() {
        }

        boolean nextChar(iteratedChar it, CharsetDetector det) {
            it.error = false;
            int firstByte = it.nextByte(det);
            it.charValue = firstByte;
            if (firstByte < 0) {
                it.done = true;
            } else if (firstByte > 128) {
                int secondByte = it.nextByte(det);
                it.charValue = (it.charValue << 8) | secondByte;
                if (firstByte >= 129 && firstByte <= 254 && ((secondByte < 64 || secondByte > 126) && (secondByte < 80 || secondByte > 254))) {
                    if (secondByte >= 48 && secondByte <= 57) {
                        int thirdByte = it.nextByte(det);
                        if (thirdByte >= 129 && thirdByte <= 254) {
                            int fourthByte = it.nextByte(det);
                            if (fourthByte >= 48 && fourthByte <= 57) {
                                it.charValue = ((it.charValue << 16) | (thirdByte << 8)) | fourthByte;
                            }
                        }
                    }
                    it.error = true;
                }
            }
            if (it.done) {
                return false;
            }
            return true;
        }

        String getName() {
            return "GB18030";
        }

        CharsetMatch match(CharsetDetector det) {
            int confidence = match(det, commonChars);
            return confidence == 0 ? null : new CharsetMatch(det, this, confidence);
        }

        public String getLanguage() {
            return "zh";
        }
    }

    static class CharsetRecog_sjis extends CharsetRecog_mbcs {
        static int[] commonChars = new int[]{33088, 33089, 33090, 33093, 33115, 33129, 33130, 33141, 33142, 33440, 33442, 33444, 33449, 33450, 33451, 33453, 33455, 33457, 33459, 33461, 33463, 33469, 33470, 33473, 33476, 33477, 33478, 33480, 33481, 33484, 33485, 33500, 33504, 33511, 33512, 33513, 33514, 33520, 33521, 33601, 33603, 33614, 33615, 33624, 33630, 33634, 33639, 33653, 33654, 33673, 33674, 33675, 33677, 33683, 36502, 37882, 38314};

        CharsetRecog_sjis() {
        }

        boolean nextChar(iteratedChar it, CharsetDetector det) {
            it.error = false;
            int firstByte = it.nextByte(det);
            it.charValue = firstByte;
            if (firstByte < 0) {
                return false;
            }
            if (firstByte <= 127 || (firstByte > 160 && firstByte <= 223)) {
                return true;
            }
            int secondByte = it.nextByte(det);
            if (secondByte < 0) {
                return false;
            }
            it.charValue = (firstByte << 8) | secondByte;
            if ((secondByte < 64 || secondByte > 127) && (secondByte < 128 || secondByte > 255)) {
                it.error = true;
            }
            return true;
        }

        CharsetMatch match(CharsetDetector det) {
            int confidence = match(det, commonChars);
            return confidence == 0 ? null : new CharsetMatch(det, this, confidence);
        }

        String getName() {
            return "Shift_JIS";
        }

        public String getLanguage() {
            return "ja";
        }
    }

    static class iteratedChar {
        int charValue = 0;
        boolean done = false;
        boolean error = false;
        int nextIndex = 0;

        iteratedChar() {
        }

        void reset() {
            this.charValue = 0;
            this.nextIndex = 0;
            this.error = false;
            this.done = false;
        }

        int nextByte(CharsetDetector det) {
            if (this.nextIndex >= det.fRawLength) {
                this.done = true;
                return -1;
            }
            byte[] bArr = det.fRawInput;
            int i = this.nextIndex;
            this.nextIndex = i + 1;
            return bArr[i] & 255;
        }
    }

    abstract String getName();

    abstract boolean nextChar(iteratedChar iteratedchar, CharsetDetector charsetDetector);

    CharsetRecog_mbcs() {
    }

    int match(CharsetDetector det, int[] commonChars) {
        int singleByteCharCount = 0;
        int doubleByteCharCount = 0;
        int commonCharCount = 0;
        int badCharCount = 0;
        int totalCharCount = 0;
        iteratedChar iter = new iteratedChar();
        iter.reset();
        while (nextChar(iter, det)) {
            totalCharCount++;
            if (iter.error) {
                badCharCount++;
            } else {
                long cv = ((long) iter.charValue) & 4294967295L;
                if (cv <= 255) {
                    singleByteCharCount++;
                } else {
                    doubleByteCharCount++;
                    if (commonChars != null) {
                        if (Arrays.binarySearch(commonChars, (int) cv) >= 0) {
                            commonCharCount++;
                        }
                    }
                }
            }
            if (badCharCount >= 2 && badCharCount * 5 >= doubleByteCharCount) {
                return 0;
            }
        }
        if (doubleByteCharCount > 10 || badCharCount != 0) {
            if (doubleByteCharCount < badCharCount * 20) {
                return 0;
            }
            if (commonChars == null) {
                int confidence = (doubleByteCharCount + 30) - (badCharCount * 20);
                if (confidence > 100) {
                    return 100;
                }
                return confidence;
            }
            return Math.min((int) ((Math.log((double) (commonCharCount + 1)) * (90.0d / Math.log((double) (((float) doubleByteCharCount) / 4.0f)))) + 10.0d), 100);
        } else if (doubleByteCharCount != 0 || totalCharCount >= 10) {
            return 10;
        } else {
            return 0;
        }
    }
}
