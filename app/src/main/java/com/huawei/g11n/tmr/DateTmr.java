package com.huawei.g11n.tmr;

import com.huawei.g11n.tmr.datetime.utils.LocaleParam;
import java.util.Date;
import java.util.Locale;

public class DateTmr {
    private static AbstractDateTmrHandle instance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.g11n.tmr.DateTmr.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.g11n.tmr.DateTmr.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.g11n.tmr.DateTmr.<clinit>():void");
    }

    private static synchronized AbstractDateTmrHandle getInstance() {
        AbstractDateTmrHandle abstractDateTmrHandle;
        synchronized (DateTmr.class) {
            String calLocale = calLocale();
            String calBkLocale = calBkLocale(calLocale);
            if (instance == null) {
                instance = new DateTmrHandle(calLocale, calBkLocale);
            } else if (!instance.getLocale().equals(calLocale)) {
                instance = new DateTmrHandle(calLocale, calBkLocale);
            }
            abstractDateTmrHandle = instance;
        }
        return abstractDateTmrHandle;
    }

    private static String calBkLocale(String str) {
        return !str.equals("en") ? "en" : "zh_hans";
    }

    private static String calLocale() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        if (language.equals("in")) {
            language = "id";
        }
        if (language.equals("iw")) {
            language = "he";
        }
        String str = language.toLowerCase(Locale.ENGLISH) + "_" + locale.getCountry().toUpperCase(Locale.ENGLISH);
        if (language.equalsIgnoreCase("zh")) {
            return "zh_hans";
        }
        if (LocaleParam.isSupport(str)) {
            language = str;
        }
        if (!LocaleParam.isSupport(language)) {
            language = "en";
        }
        return language;
    }

    public static int[] getTime(String str) {
        return getInstance().getTime(str);
    }

    public static Date[] convertDate(String str, long j) {
        return getInstance().convertDate(str, j);
    }
}
