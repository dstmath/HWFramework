package android_maps_conflict_avoidance.com.google.common.util.text;

import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.OverlayItem;
import java.util.Vector;

public final class TextUtil {
    static final Boolean FALSE = null;
    static final Boolean TRUE = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.common.util.text.TextUtil.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.common.util.text.TextUtil.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.common.util.text.TextUtil.<clinit>():void");
    }

    private TextUtil() {
    }

    public static String[] split(String target, String separator) {
        int separatorInstances = 0;
        int targetLength = target.length();
        int index = target.indexOf(separator, 0);
        while (index != -1 && index < targetLength) {
            separatorInstances++;
            if (index >= 0) {
                index += separator.length();
            }
            index = target.indexOf(separator, index);
        }
        String[] results = new String[(separatorInstances + 1)];
        int beginIndex = 0;
        for (int i = 0; i < separatorInstances; i++) {
            int endIndex = target.indexOf(separator, beginIndex);
            results[i] = target.substring(beginIndex, endIndex);
            beginIndex = endIndex + separator.length();
        }
        results[separatorInstances] = target.substring(beginIndex);
        return results;
    }

    public static String[] split(String target, char separator) {
        return split(target, String.valueOf(separator));
    }

    public static boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }

    public static int replace(String target, String replacement, StringBuffer buffer) {
        int replacementCount = 0;
        int targetLength = target.length();
        int replacementLength = replacement.length();
        int i = 0;
        while (i <= buffer.length() - targetLength) {
            for (int j = 0; j < targetLength; j++) {
                if (buffer.charAt(i + j) != target.charAt(j)) {
                    i++;
                    break;
                }
            }
            buffer.delete(i, i + targetLength);
            buffer.insert(i, replacement);
            replacementCount++;
            i += replacementLength;
            if (targetLength == 0) {
                i++;
            }
        }
        return replacementCount;
    }

    public static String join(Vector target, String separator) {
        switch (target.size()) {
            case LayoutParams.MODE_MAP /*0*/:
                return "";
            case OverlayItem.ITEM_STATE_PRESSED_MASK /*1*/:
                return String.valueOf(target.firstElement());
            default:
                StringBuffer out = new StringBuffer();
                for (int i = 0; i < target.size(); i++) {
                    if (i != 0) {
                        out.append(separator);
                    }
                    out.append(target.elementAt(i));
                }
                return out.toString();
        }
    }

    public static String e6ToString(int numE6) {
        int mantissa = numE6 / 1000000;
        int frac = Math.abs(numE6 - (mantissa * 1000000));
        StringBuffer sb = new StringBuffer();
        if (numE6 < 0 && mantissa == 0) {
            sb.append("-");
        }
        sb.append(mantissa);
        if (frac > 0) {
            sb.append(".");
            sb.append(String.valueOf(frac + 1000000).substring(1));
        }
        return sb.toString();
    }
}
