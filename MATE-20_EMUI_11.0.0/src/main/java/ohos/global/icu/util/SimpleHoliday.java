package ohos.global.icu.util;

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

    public SimpleHoliday(int i, int i2, String str) {
        super(str, new SimpleDateRule(i, i2));
    }

    public SimpleHoliday(int i, int i2, String str, int i3) {
        super(str, rangeRule(i3, 0, new SimpleDateRule(i, i2)));
    }

    public SimpleHoliday(int i, int i2, String str, int i3, int i4) {
        super(str, rangeRule(i3, i4, new SimpleDateRule(i, i2)));
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public SimpleHoliday(int i, int i2, int i3, String str) {
        super(str, new SimpleDateRule(i, i2, i3 > 0 ? i3 : -i3, i3 > 0));
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public SimpleHoliday(int i, int i2, int i3, String str, int i4) {
        super(str, rangeRule(i4, 0, new SimpleDateRule(i, i2, i3 > 0 ? i3 : -i3, i3 > 0)));
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public SimpleHoliday(int i, int i2, int i3, String str, int i4, int i5) {
        super(str, rangeRule(i4, i5, new SimpleDateRule(i, i2, i3 > 0 ? i3 : -i3, i3 > 0)));
    }

    private static DateRule rangeRule(int i, int i2, DateRule dateRule) {
        if (i == 0 && i2 == 0) {
            return dateRule;
        }
        RangeDateRule rangeDateRule = new RangeDateRule();
        if (i != 0) {
            rangeDateRule.add(new GregorianCalendar(i, 0, 1).getTime(), dateRule);
        } else {
            rangeDateRule.add(dateRule);
        }
        if (i2 != 0) {
            rangeDateRule.add(new GregorianCalendar(i2, 11, 31).getTime(), null);
        }
        return rangeDateRule;
    }
}
