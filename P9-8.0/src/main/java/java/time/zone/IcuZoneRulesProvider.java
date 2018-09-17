package java.time.zone;

import android.icu.impl.OlsonTimeZone;
import android.icu.impl.ZoneMeta;
import android.icu.util.AnnualTimeZoneRule;
import android.icu.util.DateTimeRule;
import android.icu.util.InitialTimeZoneRule;
import android.icu.util.TimeZone;
import android.icu.util.TimeZone.SystemTimeZoneType;
import android.icu.util.TimeZoneRule;
import android.icu.util.TimeZoneTransition;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransitionRule.TimeDefinition;
import java.util.ArrayList;
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

        protected ZoneRules create(String zoneId) {
            String canonicalId = TimeZone.getCanonicalID(zoneId);
            if (canonicalId.equals(zoneId)) {
                return IcuZoneRulesProvider.generateZoneRules(zoneId);
            }
            return (ZoneRules) get(canonicalId);
        }
    }

    protected Set<String> provideZoneIds() {
        Set<String> zoneIds = new HashSet(ZoneMeta.getAvailableIDs(SystemTimeZoneType.ANY, null, null));
        zoneIds.remove("GMT+0");
        zoneIds.remove("GMT-0");
        return zoneIds;
    }

    protected ZoneRules provideRules(String zoneId, boolean forCaching) {
        return (ZoneRules) this.cache.get(zoneId);
    }

    protected NavigableMap<String, ZoneRules> provideVersions(String zoneId) {
        return new TreeMap(Collections.singletonMap(TimeZone.getTZDataVersion(), provideRules(zoneId, false)));
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0199  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static ZoneRules generateZoneRules(String zoneId) {
        TimeZone timeZone = TimeZone.getFrozenTimeZone(zoneId);
        verify(timeZone instanceof OlsonTimeZone, zoneId, "Unexpected time zone class " + timeZone.getClass());
        OlsonTimeZone tz = (OlsonTimeZone) timeZone;
        InitialTimeZoneRule initial = tz.getTimeZoneRules()[0];
        ZoneOffset baseStandardOffset = millisToOffset(initial.getRawOffset());
        ZoneOffset baseWallOffset = millisToOffset(initial.getRawOffset() + initial.getDSTSavings());
        List<ZoneOffsetTransition> standardOffsetTransitionList = new ArrayList();
        List<ZoneOffsetTransition> transitionList = new ArrayList();
        List<ZoneOffsetTransitionRule> lastRules = new ArrayList();
        int preLastDstSavings = 0;
        AnnualTimeZoneRule last1 = null;
        AnnualTimeZoneRule last2 = null;
        TimeZoneTransition transition = tz.getNextTransition(Long.MIN_VALUE, false);
        int transitionCount = 1;
        while (transition != null) {
            TimeZoneRule from = transition.getFrom();
            TimeZoneRule to = transition.getTo();
            boolean hadEffect = false;
            if (from.getRawOffset() != to.getRawOffset()) {
                standardOffsetTransitionList.add(new ZoneOffsetTransition(TimeUnit.MILLISECONDS.toSeconds(transition.getTime()), millisToOffset(from.getRawOffset()), millisToOffset(to.getRawOffset())));
                hadEffect = true;
            }
            int fromTotalOffset = from.getRawOffset() + from.getDSTSavings();
            int toTotalOffset = to.getRawOffset() + to.getDSTSavings();
            if (fromTotalOffset != toTotalOffset) {
                transitionList.add(new ZoneOffsetTransition(TimeUnit.MILLISECONDS.toSeconds(transition.getTime()), millisToOffset(fromTotalOffset), millisToOffset(toTotalOffset)));
                hadEffect = true;
            }
            verify(hadEffect, zoneId, "Transition changed neither total nor raw offset.");
            if (!(to instanceof AnnualTimeZoneRule)) {
                verify(last1 == null, zoneId, "Unexpected rule after AnnualTimeZoneRule.");
            } else if (last1 == null) {
                preLastDstSavings = from.getDSTSavings();
                last1 = (AnnualTimeZoneRule) to;
                verify(last1.getEndYear() == Integer.MAX_VALUE, zoneId, "AnnualTimeZoneRule is not permanent.");
            } else {
                last2 = (AnnualTimeZoneRule) to;
                verify(last2.getEndYear() == Integer.MAX_VALUE, zoneId, "AnnualTimeZoneRule is not permanent.");
                verify(tz.getNextTransition(transition.getTime(), false).getTo() == last1, zoneId, "Unexpected rule after 2 AnnualTimeZoneRules.");
                if (last1 != null) {
                    verify(last2 != null, zoneId, "Only one AnnualTimeZoneRule.");
                    lastRules.add(toZoneOffsetTransitionRule(last1, preLastDstSavings));
                    lastRules.add(toZoneOffsetTransitionRule(last2, last1.getDSTSavings()));
                }
                return ZoneRules.of(baseStandardOffset, baseWallOffset, standardOffsetTransitionList, transitionList, lastRules);
            }
            verify(transitionCount <= MAX_TRANSITIONS, zoneId, "More than 10000 transitions.");
            transition = tz.getNextTransition(transition.getTime(), false);
            transitionCount++;
        }
        if (last1 != null) {
        }
        return ZoneRules.of(baseStandardOffset, baseWallOffset, standardOffsetTransitionList, transitionList, lastRules);
    }

    private static void verify(boolean check, String zoneId, String message) {
        if (!check) {
            throw new ZoneRulesException(String.format("Failed verification of zone %s: %s", zoneId, message));
        }
    }

    private static ZoneOffsetTransitionRule toZoneOffsetTransitionRule(AnnualTimeZoneRule rule, int dstSavingMillisBefore) {
        int dayOfMonthIndicator;
        LocalTime time;
        boolean timeEndOfDay;
        TimeDefinition timeDefinition;
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
        int secondOfDay = (int) TimeUnit.MILLISECONDS.toSeconds((long) dateTimeRule.getRuleMillisInDay());
        if (secondOfDay == SECONDS_IN_DAY) {
            time = LocalTime.MIDNIGHT;
            timeEndOfDay = true;
        } else {
            time = LocalTime.ofSecondOfDay((long) secondOfDay);
            timeEndOfDay = false;
        }
        switch (dateTimeRule.getTimeRuleType()) {
            case 0:
                timeDefinition = TimeDefinition.WALL;
                break;
            case 1:
                timeDefinition = TimeDefinition.STANDARD;
                break;
            case 2:
                timeDefinition = TimeDefinition.UTC;
                break;
            default:
                throw new ZoneRulesException("Unexpected time rule type " + dateTimeRule.getTimeRuleType());
        }
        return ZoneOffsetTransitionRule.of(month, dayOfMonthIndicator, dayOfWeek, time, timeEndOfDay, timeDefinition, millisToOffset(rule.getRawOffset()), millisToOffset(rule.getRawOffset() + dstSavingMillisBefore), millisToOffset(rule.getRawOffset() + rule.getDSTSavings()));
    }

    private static ZoneOffset millisToOffset(int offset) {
        return ZoneOffset.ofTotalSeconds((int) TimeUnit.MILLISECONDS.toSeconds((long) offset));
    }
}
