package android.icu.util;

import android.icu.util.ULocale.Category;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;

public abstract class Holiday implements DateRule {
    private static Holiday[] noHolidays;
    private String name;
    private DateRule rule;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.Holiday.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.Holiday.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.Holiday.<clinit>():void");
    }

    public static Holiday[] getHolidays() {
        return getHolidays(ULocale.getDefault(Category.FORMAT));
    }

    public static Holiday[] getHolidays(Locale locale) {
        return getHolidays(ULocale.forLocale(locale));
    }

    public static Holiday[] getHolidays(ULocale locale) {
        Holiday[] result = noHolidays;
        try {
            return (Holiday[]) UResourceBundle.getBundleInstance("android.icu.impl.data.HolidayBundle", locale).getObject("holidays");
        } catch (MissingResourceException e) {
            return result;
        }
    }

    public Date firstAfter(Date start) {
        return this.rule.firstAfter(start);
    }

    public Date firstBetween(Date start, Date end) {
        return this.rule.firstBetween(start, end);
    }

    public boolean isOn(Date date) {
        return this.rule.isOn(date);
    }

    public boolean isBetween(Date start, Date end) {
        return this.rule.isBetween(start, end);
    }

    protected Holiday(String name, DateRule rule) {
        this.name = name;
        this.rule = rule;
    }

    public String getDisplayName() {
        return getDisplayName(ULocale.getDefault(Category.DISPLAY));
    }

    public String getDisplayName(Locale locale) {
        return getDisplayName(ULocale.forLocale(locale));
    }

    public String getDisplayName(ULocale locale) {
        String dispName = this.name;
        try {
            dispName = UResourceBundle.getBundleInstance("android.icu.impl.data.HolidayBundle", locale).getString(this.name);
        } catch (MissingResourceException e) {
        }
        return dispName;
    }

    public DateRule getRule() {
        return this.rule;
    }

    public void setRule(DateRule rule) {
        this.rule = rule;
    }
}
