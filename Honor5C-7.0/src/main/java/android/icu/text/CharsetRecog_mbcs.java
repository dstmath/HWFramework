package android.icu.text;

import dalvik.bytecode.Opcodes;
import java.util.Arrays;
import org.w3c.dom.traversal.NodeFilter;

abstract class CharsetRecog_mbcs extends CharsetRecognizer {

    static class CharsetRecog_big5 extends CharsetRecog_mbcs {
        static int[] commonChars;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_mbcs.CharsetRecog_big5.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_mbcs.CharsetRecog_big5.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_mbcs.CharsetRecog_big5.<clinit>():void");
        }

        CharsetRecog_big5() {
        }

        boolean nextChar(iteratedChar it, CharsetDetector det) {
            it.index = it.nextIndex;
            it.error = false;
            int firstByte = it.nextByte(det);
            it.charValue = firstByte;
            if (firstByte < 0) {
                return false;
            }
            if (firstByte <= Opcodes.OP_NEG_FLOAT || firstByte == Opcodes.OP_CONST_CLASS_JUMBO) {
                return true;
            }
            int secondByte = it.nextByte(det);
            if (secondByte < 0) {
                return false;
            }
            it.charValue = (it.charValue << 8) | secondByte;
            if (secondByte >= 64 && secondByte != Opcodes.OP_NEG_FLOAT) {
                if (secondByte == Opcodes.OP_CONST_CLASS_JUMBO) {
                }
                return true;
            }
            it.error = true;
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
            static int[] commonChars;

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_mbcs.CharsetRecog_euc.CharsetRecog_euc_jp.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_mbcs.CharsetRecog_euc.CharsetRecog_euc_jp.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_mbcs.CharsetRecog_euc.CharsetRecog_euc_jp.<clinit>():void");
            }

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
            static int[] commonChars;

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_mbcs.CharsetRecog_euc.CharsetRecog_euc_kr.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_mbcs.CharsetRecog_euc.CharsetRecog_euc_kr.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_mbcs.CharsetRecog_euc.CharsetRecog_euc_kr.<clinit>():void");
            }

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
            it.index = it.nextIndex;
            it.error = false;
            int firstByte = it.nextByte(det);
            it.charValue = firstByte;
            if (firstByte < 0) {
                it.done = true;
            } else if (firstByte > Opcodes.OP_INT_TO_BYTE) {
                int secondByte = it.nextByte(det);
                it.charValue = (it.charValue << 8) | secondByte;
                if (firstByte < Opcodes.OP_OR_LONG || firstByte > SCSU.KATAKANAINDEX) {
                    if (firstByte == Opcodes.OP_INT_TO_CHAR) {
                        if (secondByte < Opcodes.OP_OR_LONG) {
                            it.error = true;
                        }
                    } else if (firstByte == Opcodes.OP_INT_TO_SHORT) {
                        int thirdByte = it.nextByte(det);
                        it.charValue = (it.charValue << 8) | thirdByte;
                        if (thirdByte < Opcodes.OP_OR_LONG) {
                            it.error = true;
                        }
                    }
                } else if (secondByte < Opcodes.OP_OR_LONG) {
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
        static int[] commonChars;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_mbcs.CharsetRecog_gb_18030.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_mbcs.CharsetRecog_gb_18030.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_mbcs.CharsetRecog_gb_18030.<clinit>():void");
        }

        CharsetRecog_gb_18030() {
        }

        boolean nextChar(iteratedChar it, CharsetDetector det) {
            it.index = it.nextIndex;
            it.error = false;
            int firstByte = it.nextByte(det);
            it.charValue = firstByte;
            if (firstByte < 0) {
                it.done = true;
            } else if (firstByte > NodeFilter.SHOW_COMMENT) {
                int secondByte = it.nextByte(det);
                it.charValue = (it.charValue << 8) | secondByte;
                if (firstByte >= Opcodes.OP_INT_TO_LONG && firstByte <= SCSU.KATAKANAINDEX && ((secondByte < 64 || secondByte > Opcodes.OP_NOT_LONG) && (secondByte < 80 || secondByte > SCSU.KATAKANAINDEX))) {
                    if (secondByte >= 48 && secondByte <= 57) {
                        int thirdByte = it.nextByte(det);
                        if (thirdByte >= Opcodes.OP_INT_TO_LONG && thirdByte <= SCSU.KATAKANAINDEX) {
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
        static int[] commonChars;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_mbcs.CharsetRecog_sjis.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_mbcs.CharsetRecog_sjis.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_mbcs.CharsetRecog_sjis.<clinit>():void");
        }

        CharsetRecog_sjis() {
        }

        boolean nextChar(iteratedChar it, CharsetDetector det) {
            it.index = it.nextIndex;
            it.error = false;
            int firstByte = it.nextByte(det);
            it.charValue = firstByte;
            if (firstByte < 0) {
                return false;
            }
            if (firstByte <= Opcodes.OP_NEG_FLOAT || (firstByte > Opcodes.OP_AND_LONG && firstByte <= Opcodes.OP_XOR_INT_LIT8)) {
                return true;
            }
            int secondByte = it.nextByte(det);
            if (secondByte < 0) {
                return false;
            }
            it.charValue = (firstByte << 8) | secondByte;
            if ((secondByte < 64 || secondByte > Opcodes.OP_NEG_FLOAT) && (secondByte < NodeFilter.SHOW_COMMENT || secondByte > Opcodes.OP_CONST_CLASS_JUMBO)) {
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
        int charValue;
        boolean done;
        boolean error;
        int index;
        int nextIndex;

        iteratedChar() {
            this.charValue = 0;
            this.index = 0;
            this.nextIndex = 0;
            this.error = false;
            this.done = false;
        }

        void reset() {
            this.charValue = 0;
            this.index = -1;
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
            return bArr[i] & Opcodes.OP_CONST_CLASS_JUMBO;
        }
    }

    abstract String getName();

    abstract boolean nextChar(iteratedChar android_icu_text_CharsetRecog_mbcs_iteratedChar, CharsetDetector charsetDetector);

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
