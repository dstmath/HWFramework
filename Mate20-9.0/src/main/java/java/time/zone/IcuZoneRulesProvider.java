package java.time.zone;

import android.icu.util.AnnualTimeZoneRule;
import android.icu.util.BasicTimeZone;
import android.icu.util.DateTimeRule;
import android.icu.util.InitialTimeZoneRule;
import android.icu.util.TimeZone;
import android.icu.util.TimeZoneRule;
import android.icu.util.TimeZoneTransition;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransitionRule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import libcore.util.BasicLruCache;

public class IcuZoneRulesProvider extends ZoneRulesProvider {
    private static final int MAX_TRANSITIONS = 10000;
    private static final int SECONDS_IN_DAY = 86400;
    private final BasicLruCache<String, ZoneRules> cache = new ZoneRulesCache(8);

    private static class ZoneRulesCache extends BasicLruCache<String, ZoneRules> {
        ZoneRulesCache(int maxSize) {
            super(maxSize);
        }

        /* access modifiers changed from: protected */
        public ZoneRules create(String zoneId) {
            String canonicalId = TimeZone.getCanonicalID(zoneId);
            if (!canonicalId.equals(zoneId)) {
                return (ZoneRules) get(canonicalId);
            }
            return IcuZoneRulesProvider.generateZoneRules(zoneId);
        }
    }

    /* access modifiers changed from: protected */
    public Set<String> provideZoneIds() {
        Set<String> zoneIds = new HashSet<>((Collection<? extends String>) TimeZone.getAvailableIDs(TimeZone.SystemTimeZoneType.ANY, null, null));
        zoneIds.remove("GMT+0");
        zoneIds.remove("GMT-0");
        return zoneIds;
    }

    /* access modifiers changed from: protected */
    public ZoneRules provideRules(String zoneId, boolean forCaching) {
        return (ZoneRules) this.cache.get(zoneId);
    }

    /* access modifiers changed from: protected */
    public NavigableMap<String, ZoneRules> provideVersions(String zoneId) {
        return new TreeMap(Collections.singletonMap(TimeZone.getTZDataVersion(), provideRules(zoneId, false)));
    }

    static ZoneRules generateZoneRules(String zoneId) {
        ZoneOffset baseStandardOffset;
        ZoneOffset baseWallOffset;
        List<ZoneOffsetTransitionRule> lastRules;
        boolean z;
        List<ZoneOffsetTransitionRule> lastRules2;
        AnnualTimeZoneRule last2;
        int transitionCount;
        boolean hadEffect;
        String str = zoneId;
        BasicTimeZone timeZone = TimeZone.getFrozenTimeZone(zoneId);
        verify(timeZone instanceof BasicTimeZone, str, "Unexpected time zone class " + timeZone.getClass());
        BasicTimeZone tz = timeZone;
        InitialTimeZoneRule[] timeZoneRules = tz.getTimeZoneRules();
        boolean z2 = false;
        InitialTimeZoneRule initial = timeZoneRules[0];
        ZoneOffset baseStandardOffset2 = millisToOffset(initial.getRawOffset());
        ZoneOffset baseWallOffset2 = millisToOffset(initial.getRawOffset() + initial.getDSTSavings());
        List<ZoneOffsetTransition> standardOffsetTransitionList = new ArrayList<>();
        List<ZoneOffsetTransition> transitionList = new ArrayList<>();
        List<ZoneOffsetTransitionRule> lastRules3 = new ArrayList<>();
        AnnualTimeZoneRule last1 = null;
        AnnualTimeZoneRule last22 = null;
        TimeZoneTransition transition = tz.getNextTransition(Long.MIN_VALUE, false);
        int preLastDstSavings = 0;
        int preLastDstSavings2 = 1;
        while (true) {
            if (transition == null) {
                InitialTimeZoneRule[] initialTimeZoneRuleArr = timeZoneRules;
                InitialTimeZoneRule initialTimeZoneRule = initial;
                baseStandardOffset = baseStandardOffset2;
                baseWallOffset = baseWallOffset2;
                lastRules = lastRules3;
                int i = preLastDstSavings2;
                AnnualTimeZoneRule annualTimeZoneRule = last22;
                z = z2;
                break;
            }
            TimeZoneRule from = transition.getFrom();
            TimeZone timeZone2 = timeZone;
            AnnualTimeZoneRule to = transition.getTo();
            boolean hadEffect2 = false;
            InitialTimeZoneRule[] initialTimeZoneRuleArr2 = timeZoneRules;
            InitialTimeZoneRule initial2 = initial;
            if (from.getRawOffset() != to.getRawOffset()) {
                baseStandardOffset = baseStandardOffset2;
                baseWallOffset = baseWallOffset2;
                last2 = last22;
                standardOffsetTransitionList.add(new ZoneOffsetTransition(TimeUnit.MILLISECONDS.toSeconds(transition.getTime()), millisToOffset(from.getRawOffset()), (ZoneOffset) millisToOffset(to.getRawOffset())));
                hadEffect2 = true;
            } else {
                baseStandardOffset = baseStandardOffset2;
                baseWallOffset = baseWallOffset2;
                last2 = last22;
            }
            int fromTotalOffset = from.getRawOffset() + from.getDSTSavings();
            int toTotalOffset = to.getRawOffset() + to.getDSTSavings();
            if (fromTotalOffset != toTotalOffset) {
                lastRules = lastRules3;
                transitionCount = preLastDstSavings2;
                transitionList.add(new ZoneOffsetTransition(TimeUnit.MILLISECONDS.toSeconds(transition.getTime()), millisToOffset(fromTotalOffset), millisToOffset(toTotalOffset)));
                hadEffect = true;
            } else {
                lastRules = lastRules3;
                transitionCount = preLastDstSavings2;
                hadEffect = hadEffect2;
            }
            verify(hadEffect, str, "Transition changed neither total nor raw offset.");
            if (!(to instanceof AnnualTimeZoneRule)) {
                verify(last1 == null, str, "Unexpected rule after AnnualTimeZoneRule.");
            } else if (last1 == null) {
                int preLastDstSavings3 = from.getDSTSavings();
                AnnualTimeZoneRule last12 = to;
                verify(last12.getEndYear() == Integer.MAX_VALUE, str, "AnnualTimeZoneRule is not permanent.");
                preLastDstSavings = preLastDstSavings3;
                last1 = last12;
            } else {
                last22 = to;
                verify(last22.getEndYear() == Integer.MAX_VALUE, str, "AnnualTimeZoneRule is not permanent.");
                verify(tz.getNextTransition(transition.getTime(), false).getTo() == last1, str, "Unexpected rule after 2 AnnualTimeZoneRules.");
                int i2 = transitionCount;
                z = false;
            }
            int transitionCount2 = transitionCount;
            verify(transitionCount2 <= MAX_TRANSITIONS, str, "More than 10000 transitions.");
            int i3 = fromTotalOffset;
            TimeZoneRule timeZoneRule = from;
            transition = tz.getNextTransition(transition.getTime(), false);
            preLastDstSavings2 = transitionCount2 + 1;
            z2 = false;
            timeZone = timeZone2;
            timeZoneRules = initialTimeZoneRuleArr2;
            initial = initial2;
            baseStandardOffset2 = baseStandardOffset;
            baseWallOffset2 = baseWallOffset;
            last22 = last2;
            lastRules3 = lastRules;
        }
        if (last1 != null) {
            if (last22 != null) {
                z = true;
            }
            verify(z, str, "Only one AnnualTimeZoneRule.");
            lastRules2 = lastRules;
            lastRules2.add(toZoneOffsetTransitionRule(last1, preLastDstSavings));
            lastRules2.add(toZoneOffsetTransitionRule(last22, last1.getDSTSavings()));
        } else {
            lastRules2 = lastRules;
        }
        return ZoneRules.of(baseStandardOffset, baseWallOffset, standardOffsetTransitionList, transitionList, lastRules2);
    }

    private static void verify(boolean check, String zoneId, String message) {
        if (!check) {
            throw new ZoneRulesException(String.format("Failed verification of zone %s: %s", zoneId, message));
        }
    }

    private static ZoneOffsetTransitionRule toZoneOffsetTransitionRule(AnnualTimeZoneRule rule, int dstSavingMillisBefore) {
        int dayOfMonthIndicator;
        LocalTime time;
        boolean z;
        ZoneOffsetTransitionRule.TimeDefinition timeDefinition;
        DateTimeRule dateTimeRule = rule.getRule();
        Month month = Month.JANUARY.plus((long) dateTimeRule.getRuleMonth());
        DayOfWeek dayOfWeek = DayOfWeek.SATURDAY.plus((long) dateTimeRule.getRuleDayOfWeek());
        switch (dateTimeRule.getDateRuleType()) {
            case 0:
                dayOfMonthIndicator = dateTimeRule.getRuleDayOfMonth();
                dayOfWeek = null;
                break;
            case 1:
                throw new ZoneRulesException("Date rule type DOW is unsupported");
            case 2:
                dayOfMonthIndicator = dateTimeRule.getRuleDayOfMonth();
                break;
            case 3:
                dayOfMonthIndicator = ((-month.maxLength()) + dateTimeRule.getRuleDayOfMonth()) - 1;
                break;
            default:
                throw new ZoneRulesException("Unexpected date rule type: " + dateTimeRule.getDateRuleType());
        }
        int dayOfMonthIndicator2 = dayOfMonthIndicator;
        int secondOfDay = (int) TimeUnit.MILLISECONDS.toSeconds((long) dateTimeRule.getRuleMillisInDay());
        if (secondOfDay == SECONDS_IN_DAY) {
            time = LocalTime.MIDNIGHT;
            z = true;
        } else {
            time = LocalTime.ofSecondOfDay((long) secondOfDay);
            z = false;
        }
        LocalTime time2 = time;
        boolean timeEndOfDay = z;
        switch (dateTimeRule.getTimeRuleType()) {
            case 0:
                timeDefinition = ZoneOffsetTransitionRule.TimeDefinition.WALL;
                break;
            case 1:
                timeDefinition = ZoneOffsetTransitionRule.TimeDefinition.STANDARD;
                break;
            case 2:
                timeDefinition = ZoneOffsetTransitionRule.TimeDefinition.UTC;
                break;
            default:
                throw new ZoneRulesException("Unexpected time rule type " + dateTimeRule.getTimeRuleType());
        }
        return ZoneOffsetTransitionRule.of(month, dayOfMonthIndicator2, dayOfWeek, time2, timeEndOfDay, timeDefinition, millisToOffset(rule.getRawOffset()), millisToOffset(rule.getRawOffset() + dstSavingMillisBefore), millisToOffset(rule.getRawOffset() + rule.getDSTSavings()));
    }

    private static ZoneOffset millisToOffset(int offset) {
        return ZoneOffset.ofTotalSeconds((int) TimeUnit.MILLISECONDS.toSeconds((long) offset));
    }
}
