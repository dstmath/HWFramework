package libcore.icu;

import android.icu.text.DateFormat;
import android.icu.text.DateTimePatternGenerator;
import android.icu.text.DisplayContext;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.ULocale;
import libcore.util.BasicLruCache;

public class DateTimeFormat {
    private static final FormatterCache CACHED_FORMATTERS = null;

    static class FormatterCache extends BasicLruCache<String, DateFormat> {
        FormatterCache() {
            super(8);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: libcore.icu.DateTimeFormat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: libcore.icu.DateTimeFormat.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: libcore.icu.DateTimeFormat.<clinit>():void");
    }

    private DateTimeFormat() {
    }

    public static String format(ULocale icuLocale, Calendar time, int flags, DisplayContext displayContext) {
        String format;
        String skeleton = DateUtilsBridge.toSkeleton(time, flags);
        String key = skeleton + "\t" + icuLocale + "\t" + time.getTimeZone();
        synchronized (CACHED_FORMATTERS) {
            DateFormat formatter = (DateFormat) CACHED_FORMATTERS.get(key);
            if (formatter == null) {
                formatter = new SimpleDateFormat(DateTimePatternGenerator.getInstance(icuLocale).getBestPattern(skeleton), icuLocale);
                CACHED_FORMATTERS.put(key, formatter);
            }
            formatter.setContext(displayContext);
            format = formatter.format(time);
        }
        return format;
    }
}
