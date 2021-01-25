package ohos.global.icu.util;

import java.util.Date;
import ohos.global.icu.util.ULocale;

@Deprecated
public class DangiCalendar extends ChineseCalendar {
    private static final int DANGI_EPOCH_YEAR = -2332;
    private static final TimeZone KOREA_ZONE;
    private static final long serialVersionUID = 8156297445349501985L;

    @Override // ohos.global.icu.util.ChineseCalendar, ohos.global.icu.util.Calendar
    @Deprecated
    public String getType() {
        return "dangi";
    }

    static {
        InitialTimeZoneRule initialTimeZoneRule = new InitialTimeZoneRule("GMT+8", 28800000, 0);
        TimeArrayTimeZoneRule timeArrayTimeZoneRule = new TimeArrayTimeZoneRule("Korean 1897", 25200000, 0, new long[]{-2302128000000L}, 1);
        TimeArrayTimeZoneRule timeArrayTimeZoneRule2 = new TimeArrayTimeZoneRule("Korean 1898-1911", 28800000, 0, new long[]{-2270592000000L}, 1);
        TimeArrayTimeZoneRule timeArrayTimeZoneRule3 = new TimeArrayTimeZoneRule("Korean 1912-", 32400000, 0, new long[]{-1829088000000L}, 1);
        RuleBasedTimeZone ruleBasedTimeZone = new RuleBasedTimeZone("KOREA_ZONE", initialTimeZoneRule);
        ruleBasedTimeZone.addTransitionRule(timeArrayTimeZoneRule);
        ruleBasedTimeZone.addTransitionRule(timeArrayTimeZoneRule2);
        ruleBasedTimeZone.addTransitionRule(timeArrayTimeZoneRule3);
        ruleBasedTimeZone.freeze();
        KOREA_ZONE = ruleBasedTimeZone;
    }

    @Deprecated
    public DangiCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
    }

    @Deprecated
    public DangiCalendar(Date date) {
        this(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        setTime(date);
    }

    @Deprecated
    public DangiCalendar(TimeZone timeZone, ULocale uLocale) {
        super(timeZone, uLocale, (int) DANGI_EPOCH_YEAR, KOREA_ZONE);
    }
}
