package libcore.icu;

import android.icu.util.Calendar;
import android.icu.util.ULocale;
import java.text.FieldPosition;
import java.util.TimeZone;
import libcore.util.BasicLruCache;

public final class DateIntervalFormat {
    private static final BasicLruCache<String, android.icu.text.DateIntervalFormat> CACHED_FORMATTERS = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: libcore.icu.DateIntervalFormat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: libcore.icu.DateIntervalFormat.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: libcore.icu.DateIntervalFormat.<clinit>():void");
    }

    private DateIntervalFormat() {
    }

    public static String formatDateRange(long startMs, long endMs, int flags, String olsonId) {
        if ((flags & DateUtilsBridge.FORMAT_UTC) != 0) {
            olsonId = "UTC";
        }
        return formatDateRange(ULocale.getDefault(), DateUtilsBridge.icuTimeZone(olsonId != null ? TimeZone.getTimeZone(olsonId) : TimeZone.getDefault()), startMs, endMs, flags);
    }

    public static String formatDateRange(ULocale icuLocale, android.icu.util.TimeZone icuTimeZone, long startMs, long endMs, int flags) {
        Calendar endCalendar;
        String stringBuffer;
        Calendar startCalendar = DateUtilsBridge.createIcuCalendar(icuTimeZone, icuLocale, startMs);
        if (startMs == endMs) {
            endCalendar = startCalendar;
        } else {
            endCalendar = DateUtilsBridge.createIcuCalendar(icuTimeZone, icuLocale, endMs);
        }
        boolean endsAtMidnight = isMidnight(endCalendar);
        if (startMs != endMs && endsAtMidnight && ((flags & 1) == 0 || DateUtilsBridge.dayDistance(startCalendar, endCalendar) <= 1)) {
            endCalendar.add(5, -1);
        }
        String skeleton = DateUtilsBridge.toSkeleton(startCalendar, endCalendar, flags);
        synchronized (CACHED_FORMATTERS) {
            stringBuffer = getFormatter(skeleton, icuLocale, icuTimeZone).format(startCalendar, endCalendar, new StringBuffer(), new FieldPosition(0)).toString();
        }
        return stringBuffer;
    }

    private static android.icu.text.DateIntervalFormat getFormatter(String skeleton, ULocale locale, android.icu.util.TimeZone icuTimeZone) {
        String key = skeleton + "\t" + locale + "\t" + icuTimeZone;
        android.icu.text.DateIntervalFormat formatter = (android.icu.text.DateIntervalFormat) CACHED_FORMATTERS.get(key);
        if (formatter != null) {
            return formatter;
        }
        formatter = android.icu.text.DateIntervalFormat.getInstance(skeleton, locale);
        formatter.setTimeZone(icuTimeZone);
        CACHED_FORMATTERS.put(key, formatter);
        return formatter;
    }

    private static boolean isMidnight(Calendar c) {
        if (c.get(11) == 0 && c.get(12) == 0 && c.get(13) == 0 && c.get(14) == 0) {
            return true;
        }
        return false;
    }
}
