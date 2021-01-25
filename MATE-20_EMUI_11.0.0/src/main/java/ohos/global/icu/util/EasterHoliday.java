package ohos.global.icu.util;

public class EasterHoliday extends Holiday {
    public static final EasterHoliday ASCENSION = new EasterHoliday(39, "Ascension");
    public static final EasterHoliday ASH_WEDNESDAY = new EasterHoliday(-47, "Ash Wednesday");
    public static final EasterHoliday CORPUS_CHRISTI = new EasterHoliday(60, "Corpus Christi");
    public static final EasterHoliday EASTER_MONDAY = new EasterHoliday(1, "Easter Monday");
    public static final EasterHoliday EASTER_SUNDAY = new EasterHoliday(0, "Easter Sunday");
    public static final EasterHoliday GOOD_FRIDAY = new EasterHoliday(-2, "Good Friday");
    public static final EasterHoliday MAUNDY_THURSDAY = new EasterHoliday(-3, "Maundy Thursday");
    public static final EasterHoliday PALM_SUNDAY = new EasterHoliday(-7, "Palm Sunday");
    public static final EasterHoliday PENTECOST = new EasterHoliday(49, "Pentecost");
    public static final EasterHoliday SHROVE_TUESDAY = new EasterHoliday(-48, "Shrove Tuesday");
    public static final EasterHoliday WHIT_MONDAY = new EasterHoliday(50, "Whit Monday");
    public static final EasterHoliday WHIT_SUNDAY = new EasterHoliday(49, "Whit Sunday");

    public EasterHoliday(String str) {
        super(str, new EasterRule(0, false));
    }

    public EasterHoliday(int i, String str) {
        super(str, new EasterRule(i, false));
    }

    public EasterHoliday(int i, boolean z, String str) {
        super(str, new EasterRule(i, z));
    }
}
