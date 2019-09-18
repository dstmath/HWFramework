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

    /* JADX WARNING: Code restructure failed: missing block: B:85:0x01b2, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x01b4, code lost:
        return false;
     */
    public boolean hasEquivalentTransitions(TimeZone tz, long start, long end, boolean ignoreDstAmount) {
        BasicTimeZone basicTimeZone = this;
        TimeZone timeZone = tz;
        long j = start;
        if (basicTimeZone == timeZone) {
            return true;
        }
        boolean z = false;
        if (!(timeZone instanceof BasicTimeZone)) {
            return false;
        }
        int[] offsets1 = new int[2];
        int[] offsets2 = new int[2];
        basicTimeZone.getOffset(j, false, offsets1);
        timeZone.getOffset(j, false, offsets2);
        if (ignoreDstAmount) {
            if (offsets1[0] + offsets1[1] != offsets2[0] + offsets2[1] || ((offsets1[1] != 0 && offsets2[1] == 0) || (offsets1[1] == 0 && offsets2[1] != 0))) {
                return false;
            }
        } else if (!(offsets1[0] == offsets2[0] && offsets1[1] == offsets2[1])) {
            return false;
        }
        long time = j;
        while (true) {
            TimeZoneTransition tr1 = basicTimeZone.getNextTransition(time, z);
            TimeZoneTransition tr2 = ((BasicTimeZone) timeZone).getNextTransition(time, z);
            if (ignoreDstAmount) {
                while (tr1 != null && tr1.getTime() <= end && tr1.getFrom().getRawOffset() + tr1.getFrom().getDSTSavings() == tr1.getTo().getRawOffset() + tr1.getTo().getDSTSavings() && tr1.getFrom().getDSTSavings() != 0 && tr1.getTo().getDSTSavings() != 0) {
                    tr1 = basicTimeZone.getNextTransition(tr1.getTime(), false);
                    long j2 = start;
                }
                while (tr2 != null && tr2.getTime() <= end && tr2.getFrom().getRawOffset() + tr2.getFrom().getDSTSavings() == tr2.getTo().getRawOffset() + tr2.getTo().getDSTSavings() && tr2.getFrom().getDSTSavings() != 0 && tr2.getTo().getDSTSavings() != 0) {
                    tr2 = ((BasicTimeZone) timeZone).getNextTransition(tr2.getTime(), false);
                    timeZone = tz;
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
            if (!inRange1 && !inRange2) {
                return true;
            }
            if (inRange1 && inRange2) {
                if (tr1.getTime() != tr2.getTime()) {
                    return false;
                }
                if (ignoreDstAmount) {
                    if (tr1.getTo().getRawOffset() + tr1.getTo().getDSTSavings() != tr2.getTo().getRawOffset() + tr2.getTo().getDSTSavings() || ((tr1.getTo().getDSTSavings() != 0 && tr2.getTo().getDSTSavings() == 0) || (tr1.getTo().getDSTSavings() == 0 && tr2.getTo().getDSTSavings() != 0))) {
                    }
                } else if (tr1.getTo().getRawOffset() == tr2.getTo().getRawOffset() && tr1.getTo().getDSTSavings() == tr2.getTo().getDSTSavings()) {
                }
                time = tr1.getTime();
                basicTimeZone = this;
                timeZone = tz;
                long j3 = start;
                z = false;
            }
        }
        return false;
    }

    public TimeZoneRule[] getTimeZoneRules(long start) {
        boolean bFinalDst;
        long time;
        boolean bFinalDst2;
        boolean bFinalStd;
        long t;
        TimeZoneTransition tzt;
        Date firstStart;
        BasicTimeZone basicTimeZone = this;
        long time2 = start;
        TimeZoneRule[] all = getTimeZoneRules();
        TimeZoneTransition tzt2 = basicTimeZone.getPreviousTransition(time2, true);
        if (tzt2 == null) {
            return all;
        }
        BitSet isProcessed = new BitSet(all.length);
        List<TimeZoneRule> filteredRules = new LinkedList<>();
        TimeZoneRule initial = new InitialTimeZoneRule(tzt2.getTo().getName(), tzt2.getTo().getRawOffset(), tzt2.getTo().getDSTSavings());
        filteredRules.add(initial);
        boolean z = false;
        isProcessed.set(0);
        int i = 1;
        while (true) {
            int i2 = i;
            if (i2 >= all.length) {
                break;
            }
            int i3 = i2;
            if (all[i2].getNextStart(time2, initial.getRawOffset(), initial.getDSTSavings(), false) == null) {
                isProcessed.set(i3);
            }
            i = i3 + 1;
        }
        boolean bFinalStd2 = false;
        long time3 = time2;
        boolean bFinalDst3 = false;
        while (true) {
            if (bFinalStd2 && bFinalDst3) {
                break;
            }
            TimeZoneTransition tzt3 = basicTimeZone.getNextTransition(time3, z);
            if (tzt3 == null) {
                break;
            }
            time3 = tzt3.getTime();
            TimeZoneRule toRule = tzt3.getTo();
            int ruleIdx = 1;
            while (ruleIdx < all.length && !all[ruleIdx].equals(toRule)) {
                ruleIdx++;
            }
            if (ruleIdx >= all.length) {
                boolean z2 = bFinalStd2;
                long j = time3;
                throw new IllegalStateException("The rule was not found");
            } else if (!isProcessed.get(ruleIdx)) {
                if (toRule instanceof TimeArrayTimeZoneRule) {
                    TimeArrayTimeZoneRule tar = (TimeArrayTimeZoneRule) toRule;
                    long t2 = time2;
                    while (true) {
                        bFinalStd = bFinalStd2;
                        time = time3;
                        t = t2;
                        tzt = basicTimeZone.getNextTransition(t, z);
                        if (tzt != null && !tzt.getTo().equals(tar)) {
                            t2 = tzt.getTime();
                            bFinalStd2 = bFinalStd;
                            time3 = time;
                            basicTimeZone = this;
                            z = false;
                        }
                    }
                    if (tzt != null) {
                        Date firstStart2 = tar.getFirstStart(tzt.getFrom().getRawOffset(), tzt.getFrom().getDSTSavings());
                        if (firstStart2.getTime() > time2) {
                            filteredRules.add(tar);
                        } else {
                            long[] times = tar.getStartTimes();
                            int timeType = tar.getTimeType();
                            long j2 = t;
                            int idx = 0;
                            while (true) {
                                if (idx >= times.length) {
                                    break;
                                }
                                long t3 = times[idx];
                                if (timeType == 1) {
                                    firstStart = firstStart2;
                                    t3 -= (long) tzt.getFrom().getRawOffset();
                                } else {
                                    firstStart = firstStart2;
                                }
                                if (timeType == 0) {
                                    t3 -= (long) tzt.getFrom().getDSTSavings();
                                }
                                if (t3 > time2) {
                                    break;
                                }
                                idx++;
                                firstStart2 = firstStart;
                            }
                            int asize = times.length - idx;
                            if (asize > 0) {
                                long[] newtimes = new long[asize];
                                int i4 = timeType;
                                System.arraycopy(times, idx, newtimes, 0, asize);
                                TimeArrayTimeZoneRule timeArrayTimeZoneRule = new TimeArrayTimeZoneRule(tar.getName(), tar.getRawOffset(), tar.getDSTSavings(), newtimes, tar.getTimeType());
                                filteredRules.add(timeArrayTimeZoneRule);
                            }
                        }
                    }
                    bFinalDst = bFinalDst3;
                    bFinalStd2 = bFinalStd;
                    bFinalDst2 = false;
                } else {
                    boolean bFinalStd3 = bFinalStd2;
                    time = time3;
                    if (toRule instanceof AnnualTimeZoneRule) {
                        AnnualTimeZoneRule ar = (AnnualTimeZoneRule) toRule;
                        if (ar.getFirstStart(tzt3.getFrom().getRawOffset(), tzt3.getFrom().getDSTSavings()).getTime() == tzt3.getTime()) {
                            filteredRules.add(ar);
                            bFinalDst = bFinalDst3;
                            bFinalDst2 = false;
                        } else {
                            int[] dfields = new int[6];
                            bFinalDst = bFinalDst3;
                            Grego.timeToFields(tzt3.getTime(), dfields);
                            bFinalDst2 = false;
                            AnnualTimeZoneRule annualTimeZoneRule = new AnnualTimeZoneRule(ar.getName(), ar.getRawOffset(), ar.getDSTSavings(), ar.getRule(), dfields[0], ar.getEndYear());
                            filteredRules.add(annualTimeZoneRule);
                        }
                        if (ar.getEndYear() == Integer.MAX_VALUE) {
                            if (ar.getDSTSavings() == 0) {
                                bFinalStd2 = true;
                            } else {
                                bFinalDst = true;
                            }
                        }
                    } else {
                        bFinalDst = bFinalDst3;
                        bFinalDst2 = false;
                    }
                    bFinalStd2 = bFinalStd3;
                }
                isProcessed.set(ruleIdx);
                z = bFinalDst2;
                time3 = time;
                bFinalDst3 = bFinalDst;
                basicTimeZone = this;
            }
        }
        return (TimeZoneRule[]) filteredRules.toArray(new TimeZoneRule[filteredRules.size()]);
    }

    /* JADX WARNING: Removed duplicated region for block: B:70:0x02ed  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x02f3  */
    public TimeZoneRule[] getSimpleTimeZoneRulesNear(long date) {
        InitialTimeZoneRule initialTimeZoneRule;
        int i;
        int initialDst;
        int initialRaw;
        String initialName;
        String initialName2;
        int initialRaw2;
        long nextTransitionTime;
        AnnualTimeZoneRule[] annualRules;
        int initialRaw3;
        char c;
        long j = date;
        AnnualTimeZoneRule[] annualRules2 = null;
        TimeZoneTransition tr = getNextTransition(j, false);
        if (tr != null) {
            String initialName3 = tr.getFrom().getName();
            int initialRaw4 = tr.getFrom().getRawOffset();
            int initialDst2 = tr.getFrom().getDSTSavings();
            long nextTransitionTime2 = tr.getTime();
            if (((tr.getFrom().getDSTSavings() != 0 || tr.getTo().getDSTSavings() == 0) && (tr.getFrom().getDSTSavings() == 0 || tr.getTo().getDSTSavings() != 0)) || j + MILLIS_PER_YEAR <= nextTransitionTime2) {
                initialDst = initialDst2;
                initialRaw2 = initialRaw4;
                initialName2 = initialName3;
                TimeZoneTransition timeZoneTransition = tr;
            } else {
                AnnualTimeZoneRule[] annualRules3 = new AnnualTimeZoneRule[2];
                initialName2 = initialName3;
                int[] dtfields = Grego.timeToFields(((long) tr.getFrom().getRawOffset()) + nextTransitionTime2 + ((long) tr.getFrom().getDSTSavings()), null);
                DateTimeRule dtr = new DateTimeRule(dtfields[1], Grego.getDayOfWeekInMonth(dtfields[0], dtfields[1], dtfields[2]), dtfields[3], dtfields[5], 0);
                AnnualTimeZoneRule annualTimeZoneRule = new AnnualTimeZoneRule(tr.getTo().getName(), initialRaw4, tr.getTo().getDSTSavings(), dtr, dtfields[0], Integer.MAX_VALUE);
                annualRules3[0] = annualTimeZoneRule;
                if (tr.getTo().getRawOffset() == initialRaw4) {
                    TimeZoneTransition tr2 = getNextTransition(nextTransitionTime2, false);
                    if (tr2 == null || (((tr2.getFrom().getDSTSavings() != 0 || tr2.getTo().getDSTSavings() == 0) && (tr2.getFrom().getDSTSavings() == 0 || tr2.getTo().getDSTSavings() != 0)) || nextTransitionTime2 + MILLIS_PER_YEAR <= tr2.getTime())) {
                        annualRules = annualRules3;
                        nextTransitionTime = nextTransitionTime2;
                        initialDst = initialDst2;
                        initialRaw3 = initialRaw4;
                    } else {
                        dtfields = Grego.timeToFields(((long) tr2.getFrom().getDSTSavings()) + tr2.getTime() + ((long) tr2.getFrom().getRawOffset()), dtfields);
                        DateTimeRule dateTimeRule = new DateTimeRule(dtfields[1], Grego.getDayOfWeekInMonth(dtfields[0], dtfields[1], dtfields[2]), dtfields[3], dtfields[5], 0);
                        DateTimeRule dtr2 = dateTimeRule;
                        AnnualTimeZoneRule annualTimeZoneRule2 = new AnnualTimeZoneRule(tr2.getTo().getName(), tr2.getTo().getRawOffset(), tr2.getTo().getDSTSavings(), dtr2, dtfields[0] - 1, Integer.MAX_VALUE);
                        AnnualTimeZoneRule secondRule = annualTimeZoneRule2;
                        annualRules = annualRules3;
                        nextTransitionTime = nextTransitionTime2;
                        int rawOffset = tr2.getFrom().getRawOffset();
                        initialDst = initialDst2;
                        initialRaw3 = initialRaw4;
                        Date d = secondRule.getPreviousStart(j, rawOffset, tr2.getFrom().getDSTSavings(), true);
                        if (d != null && d.getTime() <= j && initialRaw3 == tr2.getTo().getRawOffset() && initialDst == tr2.getTo().getDSTSavings()) {
                            annualRules[1] = secondRule;
                        }
                        DateTimeRule dateTimeRule2 = dtr2;
                    }
                } else {
                    annualRules = annualRules3;
                    nextTransitionTime = nextTransitionTime2;
                    initialDst = initialDst2;
                    initialRaw3 = initialRaw4;
                    TimeZoneTransition timeZoneTransition2 = tr;
                }
                if (annualRules[1] == null) {
                    TimeZoneTransition tr3 = getPreviousTransition(j, true);
                    if (tr3 == null || ((tr3.getFrom().getDSTSavings() != 0 || tr3.getTo().getDSTSavings() == 0) && (tr3.getFrom().getDSTSavings() == 0 || tr3.getTo().getDSTSavings() != 0))) {
                        initialRaw2 = initialRaw3;
                        c = 1;
                    } else {
                        int[] dtfields2 = Grego.timeToFields(tr3.getTime() + ((long) tr3.getFrom().getRawOffset()) + ((long) tr3.getFrom().getDSTSavings()), dtfields);
                        DateTimeRule dtr3 = new DateTimeRule(dtfields2[1], Grego.getDayOfWeekInMonth(dtfields2[0], dtfields2[1], dtfields2[2]), dtfields2[3], dtfields2[5], 0);
                        initialRaw2 = initialRaw3;
                        AnnualTimeZoneRule annualTimeZoneRule3 = new AnnualTimeZoneRule(tr3.getTo().getName(), initialRaw2, initialDst, dtr3, annualRules[0].getStartYear() - 1, Integer.MAX_VALUE);
                        AnnualTimeZoneRule secondRule2 = annualTimeZoneRule3;
                        if (secondRule2.getNextStart(j, tr3.getFrom().getRawOffset(), tr3.getFrom().getDSTSavings(), false).getTime() > nextTransitionTime) {
                            c = 1;
                            annualRules[1] = secondRule2;
                        } else {
                            c = 1;
                        }
                    }
                } else {
                    c = 1;
                    initialRaw2 = initialRaw3;
                }
                if (annualRules[c] == null) {
                    annualRules2 = null;
                } else {
                    initialName = annualRules[0].getName();
                    initialRaw = annualRules[0].getRawOffset();
                    initialDst = annualRules[0].getDSTSavings();
                    annualRules2 = annualRules;
                    initialTimeZoneRule = new InitialTimeZoneRule(initialName, initialRaw, initialDst);
                    InitialTimeZoneRule initialTimeZoneRule2 = initialTimeZoneRule;
                }
            }
            initialRaw = initialRaw2;
            initialName = initialName2;
            initialTimeZoneRule = new InitialTimeZoneRule(initialName, initialRaw, initialDst);
            InitialTimeZoneRule initialTimeZoneRule22 = initialTimeZoneRule;
        } else {
            TimeZoneTransition tr4 = getPreviousTransition(j, true);
            if (tr4 != null) {
                initialTimeZoneRule = new InitialTimeZoneRule(tr4.getTo().getName(), tr4.getTo().getRawOffset(), tr4.getTo().getDSTSavings());
            } else {
                int[] offsets = new int[2];
                getOffset(j, false, offsets);
                i = 1;
                initialTimeZoneRule = new InitialTimeZoneRule(getID(), offsets[0], offsets[1]);
                if (annualRules2 != null) {
                    TimeZoneRule[] result = new TimeZoneRule[i];
                    result[0] = initialTimeZoneRule;
                    return result;
                }
                TimeZoneRule[] result2 = new TimeZoneRule[3];
                result2[0] = initialTimeZoneRule;
                result2[i] = annualRules2[0];
                result2[2] = annualRules2[i];
                return result2;
            }
        }
        i = 1;
        if (annualRules2 != null) {
        }
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
