package android.icu.text;

import dalvik.bytecode.Opcodes;

abstract class CharsetRecog_sbcs extends CharsetRecognizer {

    static class CharsetRecog_8859_1 extends CharsetRecog_sbcs {
        protected static byte[] byteMap;
        private static NGramsPlusLang[] ngrams_8859_1;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_1.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_1.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_1.<clinit>():void");
        }

        CharsetRecog_8859_1() {
        }

        public CharsetMatch match(CharsetDetector det) {
            String name = det.fC1Bytes ? "windows-1252" : "ISO-8859-1";
            int bestConfidenceSoFar = -1;
            String lang = null;
            for (NGramsPlusLang ngl : ngrams_8859_1) {
                int confidence = match(det, ngl.fNGrams, byteMap);
                if (confidence > bestConfidenceSoFar) {
                    bestConfidenceSoFar = confidence;
                    lang = ngl.fLang;
                }
            }
            return bestConfidenceSoFar <= 0 ? null : new CharsetMatch(det, this, bestConfidenceSoFar, name, lang);
        }

        public String getName() {
            return "ISO-8859-1";
        }
    }

    static class CharsetRecog_8859_2 extends CharsetRecog_sbcs {
        protected static byte[] byteMap;
        private static NGramsPlusLang[] ngrams_8859_2;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_2.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_2.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_2.<clinit>():void");
        }

        CharsetRecog_8859_2() {
        }

        public CharsetMatch match(CharsetDetector det) {
            String name = det.fC1Bytes ? "windows-1250" : "ISO-8859-2";
            int bestConfidenceSoFar = -1;
            String lang = null;
            for (NGramsPlusLang ngl : ngrams_8859_2) {
                int confidence = match(det, ngl.fNGrams, byteMap);
                if (confidence > bestConfidenceSoFar) {
                    bestConfidenceSoFar = confidence;
                    lang = ngl.fLang;
                }
            }
            return bestConfidenceSoFar <= 0 ? null : new CharsetMatch(det, this, bestConfidenceSoFar, name, lang);
        }

        public String getName() {
            return "ISO-8859-2";
        }
    }

    static abstract class CharsetRecog_8859_5 extends CharsetRecog_sbcs {
        protected static byte[] byteMap;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_5.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_5.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_5.<clinit>():void");
        }

        CharsetRecog_8859_5() {
        }

        public String getName() {
            return "ISO-8859-5";
        }
    }

    static class CharsetRecog_8859_5_ru extends CharsetRecog_8859_5 {
        private static int[] ngrams;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_5_ru.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_5_ru.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_5_ru.<clinit>():void");
        }

        CharsetRecog_8859_5_ru() {
        }

        public String getLanguage() {
            return "ru";
        }

        public CharsetMatch match(CharsetDetector det) {
            int confidence = match(det, ngrams, byteMap);
            return confidence == 0 ? null : new CharsetMatch(det, this, confidence);
        }
    }

    static abstract class CharsetRecog_8859_6 extends CharsetRecog_sbcs {
        protected static byte[] byteMap;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_6.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_6.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_6.<clinit>():void");
        }

        CharsetRecog_8859_6() {
        }

        public String getName() {
            return "ISO-8859-6";
        }
    }

    static class CharsetRecog_8859_6_ar extends CharsetRecog_8859_6 {
        private static int[] ngrams;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_6_ar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_6_ar.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_6_ar.<clinit>():void");
        }

        CharsetRecog_8859_6_ar() {
        }

        public String getLanguage() {
            return "ar";
        }

        public CharsetMatch match(CharsetDetector det) {
            int confidence = match(det, ngrams, byteMap);
            return confidence == 0 ? null : new CharsetMatch(det, this, confidence);
        }
    }

    static abstract class CharsetRecog_8859_7 extends CharsetRecog_sbcs {
        protected static byte[] byteMap;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_7.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_7.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_7.<clinit>():void");
        }

        CharsetRecog_8859_7() {
        }

        public String getName() {
            return "ISO-8859-7";
        }
    }

    static class CharsetRecog_8859_7_el extends CharsetRecog_8859_7 {
        private static int[] ngrams;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_7_el.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_7_el.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_7_el.<clinit>():void");
        }

        CharsetRecog_8859_7_el() {
        }

        public String getLanguage() {
            return "el";
        }

        public CharsetMatch match(CharsetDetector det) {
            String name = det.fC1Bytes ? "windows-1253" : "ISO-8859-7";
            int confidence = match(det, ngrams, byteMap);
            return confidence == 0 ? null : new CharsetMatch(det, this, confidence, name, "el");
        }
    }

    static abstract class CharsetRecog_8859_8 extends CharsetRecog_sbcs {
        protected static byte[] byteMap;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_8.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_8.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_8.<clinit>():void");
        }

        CharsetRecog_8859_8() {
        }

        public String getName() {
            return "ISO-8859-8";
        }
    }

    static class CharsetRecog_8859_8_I_he extends CharsetRecog_8859_8 {
        private static int[] ngrams;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_8_I_he.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_8_I_he.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_8_I_he.<clinit>():void");
        }

        CharsetRecog_8859_8_I_he() {
        }

        public String getName() {
            return "ISO-8859-8-I";
        }

        public String getLanguage() {
            return "he";
        }

        public CharsetMatch match(CharsetDetector det) {
            String name = det.fC1Bytes ? "windows-1255" : "ISO-8859-8-I";
            int confidence = match(det, ngrams, byteMap);
            return confidence == 0 ? null : new CharsetMatch(det, this, confidence, name, "he");
        }
    }

    static class CharsetRecog_8859_8_he extends CharsetRecog_8859_8 {
        private static int[] ngrams;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_8_he.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_8_he.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_8_he.<clinit>():void");
        }

        CharsetRecog_8859_8_he() {
        }

        public String getLanguage() {
            return "he";
        }

        public CharsetMatch match(CharsetDetector det) {
            String name = det.fC1Bytes ? "windows-1255" : "ISO-8859-8";
            int confidence = match(det, ngrams, byteMap);
            return confidence == 0 ? null : new CharsetMatch(det, this, confidence, name, "he");
        }
    }

    static abstract class CharsetRecog_8859_9 extends CharsetRecog_sbcs {
        protected static byte[] byteMap;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_9.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_9.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_9.<clinit>():void");
        }

        CharsetRecog_8859_9() {
        }

        public String getName() {
            return "ISO-8859-9";
        }
    }

    static class CharsetRecog_8859_9_tr extends CharsetRecog_8859_9 {
        private static int[] ngrams;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_9_tr.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_9_tr.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_8859_9_tr.<clinit>():void");
        }

        CharsetRecog_8859_9_tr() {
        }

        public String getLanguage() {
            return "tr";
        }

        public CharsetMatch match(CharsetDetector det) {
            String name = det.fC1Bytes ? "windows-1254" : "ISO-8859-9";
            int confidence = match(det, ngrams, byteMap);
            return confidence == 0 ? null : new CharsetMatch(det, this, confidence, name, "tr");
        }
    }

    static abstract class CharsetRecog_IBM420_ar extends CharsetRecog_sbcs {
        protected static byte[] byteMap;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_IBM420_ar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_IBM420_ar.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_IBM420_ar.<clinit>():void");
        }

        CharsetRecog_IBM420_ar() {
        }

        public String getLanguage() {
            return "ar";
        }
    }

    static class CharsetRecog_IBM420_ar_ltr extends CharsetRecog_IBM420_ar {
        private static int[] ngrams;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_IBM420_ar_ltr.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_IBM420_ar_ltr.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_IBM420_ar_ltr.<clinit>():void");
        }

        CharsetRecog_IBM420_ar_ltr() {
        }

        public String getName() {
            return "IBM420_ltr";
        }

        public CharsetMatch match(CharsetDetector det) {
            int confidence = matchIBM420(det, ngrams, byteMap, (byte) 64);
            return confidence == 0 ? null : new CharsetMatch(det, this, confidence);
        }
    }

    static class CharsetRecog_IBM420_ar_rtl extends CharsetRecog_IBM420_ar {
        private static int[] ngrams;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_IBM420_ar_rtl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_IBM420_ar_rtl.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_IBM420_ar_rtl.<clinit>():void");
        }

        CharsetRecog_IBM420_ar_rtl() {
        }

        public String getName() {
            return "IBM420_rtl";
        }

        public CharsetMatch match(CharsetDetector det) {
            int confidence = matchIBM420(det, ngrams, byteMap, (byte) 64);
            return confidence == 0 ? null : new CharsetMatch(det, this, confidence);
        }
    }

    static abstract class CharsetRecog_IBM424_he extends CharsetRecog_sbcs {
        protected static byte[] byteMap;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_IBM424_he.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_IBM424_he.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_IBM424_he.<clinit>():void");
        }

        CharsetRecog_IBM424_he() {
        }

        public String getLanguage() {
            return "he";
        }
    }

    static class CharsetRecog_IBM424_he_ltr extends CharsetRecog_IBM424_he {
        private static int[] ngrams;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_IBM424_he_ltr.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_IBM424_he_ltr.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_IBM424_he_ltr.<clinit>():void");
        }

        CharsetRecog_IBM424_he_ltr() {
        }

        public String getName() {
            return "IBM424_ltr";
        }

        public CharsetMatch match(CharsetDetector det) {
            int confidence = match(det, ngrams, byteMap, (byte) 64);
            return confidence == 0 ? null : new CharsetMatch(det, this, confidence);
        }
    }

    static class CharsetRecog_IBM424_he_rtl extends CharsetRecog_IBM424_he {
        private static int[] ngrams;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_IBM424_he_rtl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_IBM424_he_rtl.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_IBM424_he_rtl.<clinit>():void");
        }

        CharsetRecog_IBM424_he_rtl() {
        }

        public String getName() {
            return "IBM424_rtl";
        }

        public CharsetMatch match(CharsetDetector det) {
            int confidence = match(det, ngrams, byteMap, (byte) 64);
            return confidence == 0 ? null : new CharsetMatch(det, this, confidence);
        }
    }

    static class CharsetRecog_KOI8_R extends CharsetRecog_sbcs {
        private static byte[] byteMap;
        private static int[] ngrams;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_KOI8_R.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_KOI8_R.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_KOI8_R.<clinit>():void");
        }

        CharsetRecog_KOI8_R() {
        }

        public String getName() {
            return "KOI8-R";
        }

        public String getLanguage() {
            return "ru";
        }

        public CharsetMatch match(CharsetDetector det) {
            int confidence = match(det, ngrams, byteMap);
            return confidence == 0 ? null : new CharsetMatch(det, this, confidence);
        }
    }

    static class CharsetRecog_windows_1251 extends CharsetRecog_sbcs {
        private static byte[] byteMap;
        private static int[] ngrams;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_windows_1251.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_windows_1251.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_windows_1251.<clinit>():void");
        }

        CharsetRecog_windows_1251() {
        }

        public String getName() {
            return "windows-1251";
        }

        public String getLanguage() {
            return "ru";
        }

        public CharsetMatch match(CharsetDetector det) {
            int confidence = match(det, ngrams, byteMap);
            return confidence == 0 ? null : new CharsetMatch(det, this, confidence);
        }
    }

    static class CharsetRecog_windows_1256 extends CharsetRecog_sbcs {
        private static byte[] byteMap;
        private static int[] ngrams;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_windows_1256.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.CharsetRecog_windows_1256.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.CharsetRecog_windows_1256.<clinit>():void");
        }

        CharsetRecog_windows_1256() {
        }

        public String getName() {
            return "windows-1256";
        }

        public String getLanguage() {
            return "ar";
        }

        public CharsetMatch match(CharsetDetector det) {
            int confidence = match(det, ngrams, byteMap);
            return confidence == 0 ? null : new CharsetMatch(det, this, confidence);
        }
    }

    static class NGramParser {
        private static final int N_GRAM_MASK = 16777215;
        protected int byteIndex;
        protected byte[] byteMap;
        private int hitCount;
        private int ngram;
        private int ngramCount;
        private int[] ngramList;
        protected byte spaceChar;

        public NGramParser(int[] theNgramList, byte[] theByteMap) {
            this.byteIndex = 0;
            this.ngram = 0;
            this.ngramList = theNgramList;
            this.byteMap = theByteMap;
            this.ngram = 0;
            this.hitCount = 0;
            this.ngramCount = 0;
        }

        private static int search(int[] table, int value) {
            int index = 0;
            if (table[32] <= value) {
                index = 32;
            }
            if (table[index + 16] <= value) {
                index += 16;
            }
            if (table[index + 8] <= value) {
                index += 8;
            }
            if (table[index + 4] <= value) {
                index += 4;
            }
            if (table[index + 2] <= value) {
                index += 2;
            }
            if (table[index + 1] <= value) {
                index++;
            }
            if (table[index] > value) {
                index--;
            }
            if (index < 0 || table[index] != value) {
                return -1;
            }
            return index;
        }

        private void lookup(int thisNgram) {
            this.ngramCount++;
            if (search(this.ngramList, thisNgram) >= 0) {
                this.hitCount++;
            }
        }

        protected void addByte(int b) {
            this.ngram = ((this.ngram << 8) + (b & Opcodes.OP_CONST_CLASS_JUMBO)) & N_GRAM_MASK;
            lookup(this.ngram);
        }

        private int nextByte(CharsetDetector det) {
            if (this.byteIndex >= det.fInputLen) {
                return -1;
            }
            byte[] bArr = det.fInputBytes;
            int i = this.byteIndex;
            this.byteIndex = i + 1;
            return bArr[i] & Opcodes.OP_CONST_CLASS_JUMBO;
        }

        protected void parseCharacters(CharsetDetector det) {
            boolean ignoreSpace = false;
            while (true) {
                int b = nextByte(det);
                if (b >= 0) {
                    byte mb = this.byteMap[b];
                    if (mb != null) {
                        if (mb != this.spaceChar) {
                            ignoreSpace = false;
                        }
                        if (!ignoreSpace) {
                            addByte(mb);
                        }
                        ignoreSpace = mb == this.spaceChar;
                    }
                } else {
                    return;
                }
            }
        }

        public int parse(CharsetDetector det) {
            return parse(det, (byte) 32);
        }

        public int parse(CharsetDetector det, byte spaceCh) {
            this.spaceChar = spaceCh;
            parseCharacters(det);
            addByte(this.spaceChar);
            double rawPercent = ((double) this.hitCount) / ((double) this.ngramCount);
            if (rawPercent > 0.33d) {
                return 98;
            }
            return (int) (300.0d * rawPercent);
        }
    }

    static class NGramParser_IBM420 extends NGramParser {
        protected static byte[] unshapeMap;
        private byte alef;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CharsetRecog_sbcs.NGramParser_IBM420.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CharsetRecog_sbcs.NGramParser_IBM420.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CharsetRecog_sbcs.NGramParser_IBM420.<clinit>():void");
        }

        public NGramParser_IBM420(int[] theNgramList, byte[] theByteMap) {
            super(theNgramList, theByteMap);
            this.alef = (byte) 0;
        }

        private byte isLamAlef(byte b) {
            if (b == -78 || b == -77) {
                return (byte) 71;
            }
            if (b == -76 || b == -75) {
                return (byte) 73;
            }
            if (b == -72 || b == -71) {
                return (byte) 86;
            }
            return (byte) 0;
        }

        private int nextByte(CharsetDetector det) {
            if (this.byteIndex >= det.fInputLen || det.fInputBytes[this.byteIndex] == null) {
                return -1;
            }
            int next;
            this.alef = isLamAlef(det.fInputBytes[this.byteIndex]);
            if (this.alef != null) {
                next = Opcodes.OP_SUB_INT_2ADDR;
            } else {
                next = unshapeMap[det.fInputBytes[this.byteIndex] & Opcodes.OP_CONST_CLASS_JUMBO] & Opcodes.OP_CONST_CLASS_JUMBO;
            }
            this.byteIndex++;
            return next;
        }

        protected void parseCharacters(CharsetDetector det) {
            boolean ignoreSpace = false;
            while (true) {
                int b = nextByte(det);
                if (b >= 0) {
                    byte mb = this.byteMap[b];
                    if (mb != null) {
                        if (mb != this.spaceChar) {
                            ignoreSpace = false;
                        }
                        if (!ignoreSpace) {
                            addByte(mb);
                        }
                        ignoreSpace = mb == this.spaceChar;
                    }
                    if (this.alef != null) {
                        mb = this.byteMap[this.alef & Opcodes.OP_CONST_CLASS_JUMBO];
                        if (mb != null) {
                            if (mb != this.spaceChar) {
                                ignoreSpace = false;
                            }
                            if (!ignoreSpace) {
                                addByte(mb);
                            }
                            ignoreSpace = mb == this.spaceChar;
                        }
                    }
                } else {
                    return;
                }
            }
        }
    }

    static class NGramsPlusLang {
        String fLang;
        int[] fNGrams;

        NGramsPlusLang(String la, int[] ng) {
            this.fLang = la;
            this.fNGrams = ng;
        }
    }

    abstract String getName();

    CharsetRecog_sbcs() {
    }

    int match(CharsetDetector det, int[] ngrams, byte[] byteMap) {
        return match(det, ngrams, byteMap, (byte) 32);
    }

    int match(CharsetDetector det, int[] ngrams, byte[] byteMap, byte spaceChar) {
        return new NGramParser(ngrams, byteMap).parse(det, spaceChar);
    }

    int matchIBM420(CharsetDetector det, int[] ngrams, byte[] byteMap, byte spaceChar) {
        return new NGramParser_IBM420(ngrams, byteMap).parse(det, spaceChar);
    }
}
