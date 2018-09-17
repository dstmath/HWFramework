package android.icu.util;

import android.icu.impl.Grego;
import java.util.BitSet;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public abstract class BasicTimeZone extends TimeZone {
    @Deprecated
    protected static final int FORMER_LATTER_MASK = 12;
    @Deprecated
    public static final int LOCAL_DST = 3;
    @Deprecated
    public static final int LOCAL_FORMER = 4;
    @Deprecated
    public static final int LOCAL_LATTER = 12;
    @Deprecated
    public static final int LOCAL_STD = 1;
    private static final long MILLIS_PER_YEAR = 31536000000L;
    @Deprecated
    protected static final int STD_DST_MASK = 3;
    private static final long serialVersionUID = -3204278532246180932L;

    public abstract TimeZoneTransition getNextTransition(long j, boolean z);

    public abstract TimeZoneTransition getPreviousTransition(long j, boolean z);

    public abstract TimeZoneRule[] getTimeZoneRules();

    public boolean hasEquivalentTransitions(TimeZone tz, long start, long end) {
        return hasEquivalentTransitions(tz, start, end, false);
    }

    /* JADX WARNING: Missing block: B:68:0x0124, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:92:0x01ae, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hasEquivalentTransitions(TimeZone tz, long start, long end, boolean ignoreDstAmount) {
        if (this == tz) {
            return true;
        }
        if (!(tz instanceof BasicTimeZone)) {
            return false;
        }
        int[] offsets1 = new int[2];
        int[] offsets2 = new int[2];
        getOffset(start, false, offsets1);
        tz.getOffset(start, false, offsets2);
        if (ignoreDstAmount) {
            if (offsets1[0] + offsets1[1] != offsets2[0] + offsets2[1] || ((offsets1[1] != 0 && offsets2[1] == 0) || (offsets1[1] == 0 && offsets2[1] != 0))) {
                return false;
            }
        } else if (!(offsets1[0] == offsets2[0] && offsets1[1] == offsets2[1])) {
            return false;
        }
        long time = start;
        while (true) {
            TimeZoneTransition tr1 = getNextTransition(time, false);
            TimeZoneTransition tr2 = ((BasicTimeZone) tz).getNextTransition(time, false);
            if (ignoreDstAmount) {
                while (tr1 != null && tr1.getTime() <= end && tr1.getFrom().getRawOffset() + tr1.getFrom().getDSTSavings() == tr1.getTo().getRawOffset() + tr1.getTo().getDSTSavings() && tr1.getFrom().getDSTSavings() != 0 && tr1.getTo().getDSTSavings() != 0) {
                    tr1 = getNextTransition(tr1.getTime(), false);
                }
                while (tr2 != null && tr2.getTime() <= end && tr2.getFrom().getRawOffset() + tr2.getFrom().getDSTSavings() == tr2.getTo().getRawOffset() + tr2.getTo().getDSTSavings() && tr2.getFrom().getDSTSavings() != 0 && tr2.getTo().getDSTSavings() != 0) {
                    tr2 = ((BasicTimeZone) tz).getNextTransition(tr2.getTime(), false);
                }
            }
            boolean inRange1 = false;
            boolean inRange2 = false;
            if (tr1 != null && tr1.getTime() <= end) {
                inRange1 = true;
            }
            if (tr2 != null && tr2.getTime() <= end) {
                inRange2 = true;
            }
            if (!inRange1 && (inRange2 ^ 1) != 0) {
                return true;
            }
            if (inRange1 && (inRange2 ^ 1) == 0) {
                if (tr1.getTime() != tr2.getTime()) {
                    return false;
                }
                if (ignoreDstAmount) {
                    if (tr1.getTo().getRawOffset() + tr1.getTo().getDSTSavings() != tr2.getTo().getRawOffset() + tr2.getTo().getDSTSavings() || ((tr1.getTo().getDSTSavings() != 0 && tr2.getTo().getDSTSavings() == 0) || (tr1.getTo().getDSTSavings() == 0 && tr2.getTo().getDSTSavings() != 0))) {
                    }
                } else if (tr1.getTo().getRawOffset() == tr2.getTo().getRawOffset() && tr1.getTo().getDSTSavings() == tr2.getTo().getDSTSavings()) {
                }
                time = tr1.getTime();
            }
        }
        return false;
    }

    public TimeZoneRule[] getTimeZoneRules(long start) {
        TimeZoneRule[] all = getTimeZoneRules();
        TimeZoneTransition tzt = getPreviousTransition(start, true);
        if (tzt == null) {
            return all;
        }
        BitSet bitSet = new BitSet(all.length);
        List<TimeZoneRule> filteredRules = new LinkedList();
        TimeZoneRule initialTimeZoneRule = new InitialTimeZoneRule(tzt.getTo().getName(), tzt.getTo().getRawOffset(), tzt.getTo().getDSTSavings());
        filteredRules.add(initialTimeZoneRule);
        bitSet.set(0);
        for (int i = 1; i < all.length; i++) {
            if (all[i].getNextStart(start, initialTimeZoneRule.getRawOffset(), initialTimeZoneRule.getDSTSavings(), false) == null) {
                bitSet.set(i);
            }
        }
        long time = start;
        boolean bFinalStd = false;
        boolean bFinalDst = false;
        while (true) {
            if (bFinalStd && (bFinalDst ^ 1) == 0) {
                break;
            }
            tzt = getNextTransition(time, false);
            if (tzt == null) {
                break;
            }
            time = tzt.getTime();
            TimeZoneRule toRule = tzt.getTo();
            int ruleIdx = 1;
            while (ruleIdx < all.length && !all[ruleIdx].equals(toRule)) {
                ruleIdx++;
            }
            if (ruleIdx >= all.length) {
                throw new IllegalStateException("The rule was not found");
            } else if (!bitSet.get(ruleIdx)) {
                if (toRule instanceof TimeArrayTimeZoneRule) {
                    TimeArrayTimeZoneRule tar = (TimeArrayTimeZoneRule) toRule;
                    long t = start;
                    while (true) {
                        tzt = getNextTransition(t, false);
                        if (!(tzt == null || tzt.getTo().equals(tar))) {
                            t = tzt.getTime();
                        }
                    }
                    if (tzt != null) {
                        if (tar.getFirstStart(tzt.getFrom().getRawOffset(), tzt.getFrom().getDSTSavings()).getTime() > start) {
                            filteredRules.add(tar);
                        } else {
                            long[] times = tar.getStartTimes();
                            int timeType = tar.getTimeType();
                            int idx = 0;
                            while (idx < times.length) {
                                t = times[idx];
                                if (timeType == 1) {
                                    t -= (long) tzt.getFrom().getRawOffset();
                                }
                                if (timeType == 0) {
                                    t -= (long) tzt.getFrom().getDSTSavings();
                                }
                                if (t > start) {
                                    break;
                                }
                                idx++;
                            }
                            int asize = times.length - idx;
                            if (asize > 0) {
                                long[] newtimes = new long[asize];
                                System.arraycopy(times, idx, newtimes, 0, asize);
                                filteredRules.add(new TimeArrayTimeZoneRule(tar.getName(), tar.getRawOffset(), tar.getDSTSavings(), newtimes, tar.getTimeType()));
                            }
                        }
                    }
                } else if (toRule instanceof AnnualTimeZoneRule) {
                    AnnualTimeZoneRule ar = (AnnualTimeZoneRule) toRule;
                    if (ar.getFirstStart(tzt.getFrom().getRawOffset(), tzt.getFrom().getDSTSavings()).getTime() == tzt.getTime()) {
                        filteredRules.add(ar);
                    } else {
                        int[] dfields = new int[6];
                        Grego.timeToFields(tzt.getTime(), dfields);
                        filteredRules.add(new AnnualTimeZoneRule(ar.getName(), ar.getRawOffset(), ar.getDSTSavings(), ar.getRule(), dfields[0], ar.getEndYear()));
                    }
                    if (ar.getEndYear() == Integer.MAX_VALUE) {
                        if (ar.getDSTSavings() == 0) {
                            bFinalStd = true;
                        } else {
                            bFinalDst = true;
                        }
                    }
                }
                bitSet.set(ruleIdx);
            }
        }
        return (TimeZoneRule[]) filteredRules.toArray(new TimeZoneRule[filteredRules.size()]);
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0180  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x027f  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0220  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0180  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0220  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x027f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public TimeZoneRule[] getSimpleTimeZoneRulesNear(long date) {
        AnnualTimeZoneRule[] annualRules = null;
        TimeZoneTransition tr = getNextTransition(date, false);
        TimeZoneRule initialTimeZoneRule;
        if (tr != null) {
            String initialName = tr.getFrom().getName();
            int initialRaw = tr.getFrom().getRawOffset();
            int initialDst = tr.getFrom().getDSTSavings();
            long nextTransitionTime = tr.getTime();
            if (((tr.getFrom().getDSTSavings() == 0 && tr.getTo().getDSTSavings() != 0) || (tr.getFrom().getDSTSavings() != 0 && tr.getTo().getDSTSavings() == 0)) && MILLIS_PER_YEAR + date > nextTransitionTime) {
                AnnualTimeZoneRule annualTimeZoneRule;
                annualRules = new AnnualTimeZoneRule[2];
                int[] dtfields = Grego.timeToFields((((long) tr.getFrom().getRawOffset()) + nextTransitionTime) + ((long) tr.getFrom().getDSTSavings()), null);
                annualRules[0] = new AnnualTimeZoneRule(tr.getTo().getName(), initialRaw, tr.getTo().getDSTSavings(), new DateTimeRule(dtfields[1], Grego.getDayOfWeekInMonth(dtfields[0], dtfields[1], dtfields[2]), dtfields[3], dtfields[5], 0), dtfields[0], Integer.MAX_VALUE);
                if (tr.getTo().getRawOffset() == initialRaw) {
                    tr = getNextTransition(nextTransitionTime, false);
                    if (tr != null) {
                        if (((tr.getFrom().getDSTSavings() != 0 || tr.getTo().getDSTSavings() == 0) && (tr.getFrom().getDSTSavings() == 0 || tr.getTo().getDSTSavings() != 0)) || MILLIS_PER_YEAR + nextTransitionTime <= tr.getTime()) {
                            annualTimeZoneRule = null;
                            if (annualRules[1] == null) {
                                tr = getPreviousTransition(date, true);
                                if (tr != null && ((tr.getFrom().getDSTSavings() == 0 && tr.getTo().getDSTSavings() != 0) || (tr.getFrom().getDSTSavings() != 0 && tr.getTo().getDSTSavings() == 0))) {
                                    dtfields = Grego.timeToFields((tr.getTime() + ((long) tr.getFrom().getRawOffset())) + ((long) tr.getFrom().getDSTSavings()), dtfields);
                                    DateTimeRule dateTimeRule = new DateTimeRule(dtfields[1], Grego.getDayOfWeekInMonth(dtfields[0], dtfields[1], dtfields[2]), dtfields[3], dtfields[5], 0);
                                    annualTimeZoneRule = new AnnualTimeZoneRule(tr.getTo().getName(), initialRaw, initialDst, dateTimeRule, annualRules[0].getStartYear() - 1, Integer.MAX_VALUE);
                                    if (annualTimeZoneRule.getNextStart(date, tr.getFrom().getRawOffset(), tr.getFrom().getDSTSavings(), false).getTime() > nextTransitionTime) {
                                        annualRules[1] = annualTimeZoneRule;
                                    }
                                }
                            }
                            if (annualRules[1] == null) {
                                annualRules = null;
                            } else {
                                initialName = annualRules[0].getName();
                                initialRaw = annualRules[0].getRawOffset();
                                initialDst = annualRules[0].getDSTSavings();
                            }
                        } else {
                            dtfields = Grego.timeToFields((tr.getTime() + ((long) tr.getFrom().getRawOffset())) + ((long) tr.getFrom().getDSTSavings()), dtfields);
                            annualTimeZoneRule = new AnnualTimeZoneRule(tr.getTo().getName(), tr.getTo().getRawOffset(), tr.getTo().getDSTSavings(), new DateTimeRule(dtfields[1], Grego.getDayOfWeekInMonth(dtfields[0], dtfields[1], dtfields[2]), dtfields[3], dtfields[5], 0), dtfields[0] - 1, Integer.MAX_VALUE);
                            Date d = annualTimeZoneRule.getPreviousStart(date, tr.getFrom().getRawOffset(), tr.getFrom().getDSTSavings(), true);
                            if (d != null && d.getTime() <= date && initialRaw == tr.getTo().getRawOffset() && initialDst == tr.getTo().getDSTSavings()) {
                                annualRules[1] = annualTimeZoneRule;
                            }
                            if (annualRules[1] == null) {
                            }
                            if (annualRules[1] == null) {
                            }
                        }
                    }
                }
                annualTimeZoneRule = null;
                if (annualRules[1] == null) {
                }
                if (annualRules[1] == null) {
                }
            }
            initialTimeZoneRule = new InitialTimeZoneRule(initialName, initialRaw, initialDst);
        } else {
            tr = getPreviousTransition(date, true);
            if (tr != null) {
                initialTimeZoneRule = new InitialTimeZoneRule(tr.getTo().getName(), tr.getTo().getRawOffset(), tr.getTo().getDSTSavings());
            } else {
                int[] offsets = new int[2];
                getOffset(date, false, offsets);
                initialTimeZoneRule = new InitialTimeZoneRule(getID(), offsets[0], offsets[1]);
            }
        }
        if (annualRules == null) {
            return new TimeZoneRule[]{initialRule};
        }
        return new TimeZoneRule[]{initialRule, annualRules[0], annualRules[1]};
    }

    @Deprecated
    public void getOffsetFromLocal(long date, int nonExistingTimeOpt, int duplicatedTimeOpt, int[] offsets) {
        throw new IllegalStateException("Not implemented");
    }

    protected BasicTimeZone() {
    }

    @Deprecated
    protected BasicTimeZone(String ID) {
        super(ID);
    }
}
