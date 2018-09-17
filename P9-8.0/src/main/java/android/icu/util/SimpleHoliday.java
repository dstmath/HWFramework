package android.icu.util;

public class SimpleHoliday extends Holiday {
    public static final SimpleHoliday ALL_SAINTS_DAY = new SimpleHoliday(10, 1, "All Saints' Day");
    public static final SimpleHoliday ALL_SOULS_DAY = new SimpleHoliday(10, 2, "All Souls' Day");
    public static final SimpleHoliday ASSUMPTION = new SimpleHoliday(7, 15, "Assumption");
    public static final SimpleHoliday BOXING_DAY = new SimpleHoliday(11, 26, "Boxing Day");
    public static final SimpleHoliday CHRISTMAS = new SimpleHoliday(11, 25, "Christmas");
    public static final SimpleHoliday CHRISTMAS_EVE = new SimpleHoliday(11, 24, "Christmas Eve");
    public static final SimpleHoliday EPIPHANY = new SimpleHoliday(0, 6, "Epiphany");
    public static final SimpleHoliday IMMACULATE_CONCEPTION = new SimpleHoliday(11, 8, "Immaculate Conception");
    public static final SimpleHoliday MAY_DAY = new SimpleHoliday(4, 1, "May Day");
    public static final SimpleHoliday NEW_YEARS_DAY = new SimpleHoliday(0, 1, "New Year's Day");
    public static final SimpleHoliday NEW_YEARS_EVE = new SimpleHoliday(11, 31, "New Year's Eve");
    public static final SimpleHoliday ST_STEPHENS_DAY = new SimpleHoliday(11, 26, "St. Stephen's Day");

    public SimpleHoliday(int month, int dayOfMonth, String name) {
        super(name, new SimpleDateRule(month, dayOfMonth));
    }

    public SimpleHoliday(int month, int dayOfMonth, String name, int startYear) {
        super(name, rangeRule(startYear, 0, new SimpleDateRule(month, dayOfMonth)));
    }

    public SimpleHoliday(int month, int dayOfMonth, String name, int startYear, int endYear) {
        super(name, rangeRule(startYear, endYear, new SimpleDateRule(month, dayOfMonth)));
    }

    public SimpleHoliday(int month, int dayOfMonth, int dayOfWeek, String name) {
        boolean z = false;
        int i = dayOfWeek > 0 ? dayOfWeek : -dayOfWeek;
        if (dayOfWeek > 0) {
            z = true;
        }
        super(name, new SimpleDateRule(month, dayOfMonth, i, z));
    }

    public SimpleHoliday(int month, int dayOfMonth, int dayOfWeek, String name, int startYear) {
        boolean z;
        int i = dayOfWeek > 0 ? dayOfWeek : -dayOfWeek;
        if (dayOfWeek > 0) {
            z = true;
        } else {
            z = false;
        }
        super(name, rangeRule(startYear, 0, new SimpleDateRule(month, dayOfMonth, i, z)));
    }

    public SimpleHoliday(int month, int dayOfMonth, int dayOfWeek, String name, int startYear, int endYear) {
        boolean z = false;
        int i = dayOfWeek > 0 ? dayOfWeek : -dayOfWeek;
        if (dayOfWeek > 0) {
            z = true;
        }
        super(name, rangeRule(startYear, endYear, new SimpleDateRule(month, dayOfMonth, i, z)));
    }

    private static DateRule rangeRule(int startYear, int endYear, DateRule rule) {
        if (startYear == 0 && endYear == 0) {
            return rule;
        }
        RangeDateRule rangeRule = new RangeDateRule();
        if (startYear != 0) {
            rangeRule.add(new GregorianCalendar(startYear, 0, 1).getTime(), rule);
        } else {
            rangeRule.add(rule);
        }
        if (endYear != 0) {
            rangeRule.add(new GregorianCalendar(endYear, 11, 31).getTime(), null);
        }
        return rangeRule;
    }
}
