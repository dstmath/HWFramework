package android.telephony;

import android.text.Editable;
import com.android.internal.content.NativeLibraryHelper;

class JapanesePhoneNumberFormatter {
    private static short[] FORMAT_MAP;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.JapanesePhoneNumberFormatter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.JapanesePhoneNumberFormatter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.JapanesePhoneNumberFormatter.<clinit>():void");
    }

    JapanesePhoneNumberFormatter() {
    }

    public static void format(Editable text) {
        int rootIndex = 1;
        int length = text.length();
        if (length > 3 && text.subSequence(0, 3).toString().equals("+81")) {
            rootIndex = 3;
        } else if (length < 1 || text.charAt(0) != '0') {
            return;
        }
        CharSequence saved = text.subSequence(0, length);
        int i = 0;
        while (i < text.length()) {
            if (text.charAt(i) == '-') {
                text.delete(i, i + 1);
            } else {
                i++;
            }
        }
        length = text.length();
        i = rootIndex;
        int base = 0;
        while (i < length) {
            char ch = text.charAt(i);
            if (Character.isDigit(ch)) {
                short value = FORMAT_MAP[(base + ch) - 48];
                if (value >= (short) 0) {
                    short base2 = value;
                    i++;
                } else if (value <= (short) -100) {
                    text.replace(0, length, saved);
                    return;
                } else {
                    int dashPos2 = rootIndex + (Math.abs(value) % 10);
                    if (length > dashPos2) {
                        text.insert(dashPos2, NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                    }
                    int dashPos1 = rootIndex + (Math.abs(value) / 10);
                    if (length > dashPos1) {
                        text.insert(dashPos1, NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                    }
                    if (length > 3 && rootIndex == 3) {
                        text.insert(rootIndex, NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                    }
                }
            }
            text.replace(0, length, saved);
            return;
        }
        text.insert(rootIndex, NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
    }
}
