package com.huawei.android.text;

import android.text.SpannableString;
import android.text.SpannedString;
import java.util.HashMap;

public class HwTextUtils {
    private static HashMap<Character, CharSequence> SyrillicLatinMap;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.text.HwTextUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.text.HwTextUtils.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.text.HwTextUtils.<clinit>():void");
    }

    private static java.util.HashMap<java.lang.Character, java.lang.CharSequence> SyrillicToLatin() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.text.HwTextUtils.SyrillicToLatin():java.util.HashMap<java.lang.Character, java.lang.CharSequence>
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.text.HwTextUtils.SyrillicToLatin():java.util.HashMap<java.lang.Character, java.lang.CharSequence>
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.text.HwTextUtils.SyrillicToLatin():java.util.HashMap<java.lang.Character, java.lang.CharSequence>");
    }

    private static boolean isSyrillic(String chs, int len) {
        for (int i = 0; i < len; i++) {
            char c = chs.charAt(i);
            if (c > '\u0400' && c < '\u0460') {
                return true;
            }
        }
        return false;
    }

    private static StringBuilder getLatinString(String chs, int len) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < len; i++) {
            char c = chs.charAt(i);
            if (c <= '\u0400' || c >= '\u0460') {
                out.append(c);
            } else {
                out.append((CharSequence) SyrillicLatinMap.get(Character.valueOf(c)));
            }
        }
        return out;
    }

    private static int getLatinStringLen(CharSequence chs, int len) {
        return getLatinString(chs.toString(), len).length();
    }

    public static String serbianSyrillic2Latin(String text) {
        if (text == null) {
            return null;
        }
        int len = text.length();
        if (isSyrillic(text, len)) {
            return getLatinString(text, len).toString();
        }
        return text;
    }

    public static CharSequence serbianSyrillic2Latin(CharSequence text) {
        if (text == null) {
            return null;
        }
        if (text instanceof String) {
            return serbianSyrillic2Latin((String) text);
        }
        if (!(text instanceof SpannedString)) {
            return text;
        }
        int len = text.length();
        if (!isSyrillic(text.toString(), len)) {
            return text;
        }
        SpannableString newText = new SpannableString(getLatinString(text.toString(), len));
        SpannedString sp = (SpannedString) text;
        int end = sp.length();
        Object[] spans = sp.getSpans(0, end, Object.class);
        for (int i = 0; i < spans.length; i++) {
            int st = sp.getSpanStart(spans[i]);
            int en = sp.getSpanEnd(spans[i]);
            int fl = sp.getSpanFlags(spans[i]);
            if (st < 0) {
                st = 0;
            }
            if (en > end) {
                en = end;
            }
            newText.setSpan(spans[i], getLatinStringLen(text.subSequence(0, st), st), getLatinStringLen(text.subSequence(0, en), en), fl);
        }
        return newText;
    }
}
