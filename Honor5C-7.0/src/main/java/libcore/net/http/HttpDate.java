package libcore.net.http;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class HttpDate {
    private static final String[] BROWSER_COMPATIBLE_DATE_FORMATS = null;
    private static final ThreadLocal<DateFormat> STANDARD_DATE_FORMAT = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: libcore.net.http.HttpDate.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: libcore.net.http.HttpDate.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: libcore.net.http.HttpDate.<clinit>():void");
    }

    public static Date parse(String value) {
        try {
            return ((DateFormat) STANDARD_DATE_FORMAT.get()).parse(value);
        } catch (ParseException e) {
            String[] strArr = BROWSER_COMPATIBLE_DATE_FORMATS;
            int i = 0;
            int length = strArr.length;
            while (i < length) {
                try {
                    return new SimpleDateFormat(strArr[i], Locale.US).parse(value);
                } catch (ParseException e2) {
                    i++;
                }
            }
            return null;
        }
    }

    public static String format(Date value) {
        return ((DateFormat) STANDARD_DATE_FORMAT.get()).format(value);
    }
}
