package android.icu.impl;

import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.util.MissingResourceException;

public class CalendarUtil {
    private static final String CALKEY = "calendar";
    private static ICUCache<String, String> CALTYPE_CACHE = null;
    private static final String DEFCAL = "gregorian";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.CalendarUtil.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.CalendarUtil.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.CalendarUtil.<clinit>():void");
    }

    public static String getCalendarType(ULocale loc) {
        String calType = loc.getKeywordValue(CALKEY);
        if (calType != null) {
            return calType;
        }
        String baseLoc = loc.getBaseName();
        calType = (String) CALTYPE_CACHE.get(baseLoc);
        if (calType != null) {
            return calType;
        }
        ULocale canonical = ULocale.createCanonical(loc.toString());
        calType = canonical.getKeywordValue(CALKEY);
        if (calType == null) {
            String region = canonical.getCountry();
            if (region.length() == 0) {
                region = ULocale.addLikelySubtags(canonical).getCountry();
            }
            try {
                UResourceBundle order;
                UResourceBundle calPref = UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("calendarPreferenceData");
                try {
                    order = calPref.get(region);
                } catch (MissingResourceException e) {
                    order = calPref.get("001");
                }
                calType = order.getString(0);
            } catch (MissingResourceException e2) {
            }
            if (calType == null) {
                calType = DEFCAL;
            }
        }
        CALTYPE_CACHE.put(baseLoc, calType);
        return calType;
    }
}
