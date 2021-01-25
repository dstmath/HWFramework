package ohos.global.icu.util;

import java.util.BitSet;
import java.util.Date;
import java.util.LinkedList;
import ohos.global.icu.impl.Grego;

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

    public boolean hasEquivalentTransitions(TimeZone timeZone, long j, long j2) {
        return hasEquivalentTransitions(timeZone, j, j2, false);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003e, code lost:
        if (r3[1] == r1[1]) goto L_0x0042;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0165, code lost:
        return false;
     */
    public boolean hasEquivalentTransitions(TimeZone timeZone, long j, long j2, boolean z) {
        if (this == timeZone) {
            return true;
        }
        if (!(timeZone instanceof BasicTimeZone)) {
            return false;
        }
        int[] iArr = new int[2];
        int[] iArr2 = new int[2];
        getOffset(j, false, iArr);
        timeZone.getOffset(j, false, iArr2);
        if (z) {
            if (iArr[0] + iArr[1] != iArr2[0] + iArr2[1] || ((iArr[1] != 0 && iArr2[1] == 0) || (iArr[1] == 0 && iArr2[1] != 0))) {
                return false;
            }
            while (true) {
                TimeZoneTransition nextTransition = getNextTransition(j, false);
                BasicTimeZone basicTimeZone = (BasicTimeZone) timeZone;
                TimeZoneTransition nextTransition2 = basicTimeZone.getNextTransition(j, false);
                if (z) {
                    while (nextTransition != null && nextTransition.getTime() <= j2 && nextTransition.getFrom().getRawOffset() + nextTransition.getFrom().getDSTSavings() == nextTransition.getTo().getRawOffset() + nextTransition.getTo().getDSTSavings() && nextTransition.getFrom().getDSTSavings() != 0 && nextTransition.getTo().getDSTSavings() != 0) {
                        nextTransition = getNextTransition(nextTransition.getTime(), false);
                    }
                    while (nextTransition2 != null && nextTransition2.getTime() <= j2 && nextTransition2.getFrom().getRawOffset() + nextTransition2.getFrom().getDSTSavings() == nextTransition2.getTo().getRawOffset() + nextTransition2.getTo().getDSTSavings() && nextTransition2.getFrom().getDSTSavings() != 0 && nextTransition2.getTo().getDSTSavings() != 0) {
                        nextTransition2 = basicTimeZone.getNextTransition(nextTransition2.getTime(), false);
                    }
                }
                boolean z2 = nextTransition != null && nextTransition.getTime() <= j2;
                boolean z3 = nextTransition2 != null && nextTransition2.getTime() <= j2;
                if (!z2 && !z3) {
                    return true;
                }
                if (!z2 || !z3 || nextTransition.getTime() != nextTransition2.getTime()) {
                    return false;
                }
                if (!z) {
                    if (nextTransition.getTo().getRawOffset() != nextTransition2.getTo().getRawOffset() || nextTransition.getTo().getDSTSavings() != nextTransition2.getTo().getDSTSavings()) {
                        break;
                    }
                } else if (nextTransition.getTo().getRawOffset() + nextTransition.getTo().getDSTSavings() != nextTransition2.getTo().getRawOffset() + nextTransition2.getTo().getDSTSavings() || ((nextTransition.getTo().getDSTSavings() != 0 && nextTransition2.getTo().getDSTSavings() == 0) || (nextTransition.getTo().getDSTSavings() == 0 && nextTransition2.getTo().getDSTSavings() != 0))) {
                    break;
                }
                j = nextTransition.getTime();
            }
        } else if (iArr[0] == iArr2[0]) {
        }
        return false;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r11v4 */
    /* JADX WARN: Type inference failed for: r11v5, types: [int] */
    public TimeZoneRule[] getTimeZoneRules(long j) {
        boolean z;
        BitSet bitSet;
        BitSet bitSet2;
        boolean z2;
        TimeZoneTransition nextTransition;
        BasicTimeZone basicTimeZone = this;
        TimeZoneRule[] timeZoneRules = getTimeZoneRules();
        int i = 1;
        TimeZoneTransition previousTransition = basicTimeZone.getPreviousTransition(j, true);
        if (previousTransition == null) {
            return timeZoneRules;
        }
        BitSet bitSet3 = new BitSet(timeZoneRules.length);
        LinkedList linkedList = new LinkedList();
        InitialTimeZoneRule initialTimeZoneRule = new InitialTimeZoneRule(previousTransition.getTo().getName(), previousTransition.getTo().getRawOffset(), previousTransition.getTo().getDSTSavings());
        linkedList.add(initialTimeZoneRule);
        boolean z3 = false;
        bitSet3.set(0);
        for (int i2 = 1; i2 < timeZoneRules.length; i2++) {
            if (timeZoneRules[i2].getNextStart(j, initialTimeZoneRule.getRawOffset(), initialTimeZoneRule.getDSTSavings(), false) == null) {
                bitSet3.set(i2);
            }
        }
        long j2 = j;
        boolean z4 = false;
        boolean z5 = false;
        while (true) {
            if (z4 && z5) {
                break;
            }
            TimeZoneTransition nextTransition2 = basicTimeZone.getNextTransition(j2, z3);
            if (nextTransition2 == null) {
                break;
            }
            long time = nextTransition2.getTime();
            TimeZoneRule to = nextTransition2.getTo();
            int i3 = i;
            while (i3 < timeZoneRules.length && !timeZoneRules[i3].equals(to)) {
                i3++;
            }
            if (i3 >= timeZoneRules.length) {
                throw new IllegalStateException("The rule was not found");
            } else if (bitSet3.get(i3)) {
                j2 = time;
            } else {
                if (to instanceof TimeArrayTimeZoneRule) {
                    TimeArrayTimeZoneRule timeArrayTimeZoneRule = (TimeArrayTimeZoneRule) to;
                    bitSet2 = bitSet3;
                    long j3 = j;
                    while (true) {
                        nextTransition = basicTimeZone.getNextTransition(j3, z3);
                        if (nextTransition != null && !nextTransition.getTo().equals(timeArrayTimeZoneRule)) {
                            j3 = nextTransition.getTime();
                            z3 = false;
                            basicTimeZone = this;
                        }
                    }
                    if (nextTransition != null) {
                        if (timeArrayTimeZoneRule.getFirstStart(nextTransition.getFrom().getRawOffset(), nextTransition.getFrom().getDSTSavings()).getTime() > j) {
                            linkedList.add(timeArrayTimeZoneRule);
                        } else {
                            long[] startTimes = timeArrayTimeZoneRule.getStartTimes();
                            int timeType = timeArrayTimeZoneRule.getTimeType();
                            int i4 = z3;
                            while (true) {
                                if (i4 >= startTimes.length) {
                                    z2 = z4;
                                    break;
                                }
                                long j4 = startTimes[i4 == true ? 1 : 0];
                                if (timeType == 1) {
                                    z2 = z4;
                                    j4 -= (long) nextTransition.getFrom().getRawOffset();
                                } else {
                                    z2 = z4;
                                }
                                if (timeType == 0) {
                                    j4 -= (long) nextTransition.getFrom().getDSTSavings();
                                }
                                if (j4 > j) {
                                    break;
                                }
                                z4 = z2;
                                i4++;
                            }
                            int length = startTimes.length - (i4 == true ? 1 : 0);
                            if (length > 0) {
                                long[] jArr = new long[length];
                                System.arraycopy(startTimes, i4, jArr, 0, length);
                                linkedList.add(new TimeArrayTimeZoneRule(timeArrayTimeZoneRule.getName(), timeArrayTimeZoneRule.getRawOffset(), timeArrayTimeZoneRule.getDSTSavings(), jArr, timeArrayTimeZoneRule.getTimeType()));
                            }
                        }
                    }
                    z2 = z4;
                } else {
                    z2 = z4;
                    bitSet2 = bitSet3;
                    if (to instanceof AnnualTimeZoneRule) {
                        AnnualTimeZoneRule annualTimeZoneRule = (AnnualTimeZoneRule) to;
                        if (annualTimeZoneRule.getFirstStart(nextTransition2.getFrom().getRawOffset(), nextTransition2.getFrom().getDSTSavings()).getTime() == nextTransition2.getTime()) {
                            linkedList.add(annualTimeZoneRule);
                            z = false;
                        } else {
                            int[] iArr = new int[6];
                            Grego.timeToFields(nextTransition2.getTime(), iArr);
                            z = false;
                            linkedList.add(new AnnualTimeZoneRule(annualTimeZoneRule.getName(), annualTimeZoneRule.getRawOffset(), annualTimeZoneRule.getDSTSavings(), annualTimeZoneRule.getRule(), iArr[0], annualTimeZoneRule.getEndYear()));
                        }
                        if (annualTimeZoneRule.getEndYear() == Integer.MAX_VALUE) {
                            if (annualTimeZoneRule.getDSTSavings() == 0) {
                                bitSet = bitSet2;
                                z4 = true;
                            } else {
                                z4 = z2;
                                bitSet = bitSet2;
                                z5 = true;
                            }
                            bitSet.set(i3);
                            bitSet3 = bitSet;
                            z3 = z;
                            j2 = time;
                            i = 1;
                            basicTimeZone = this;
                        }
                        z4 = z2;
                        bitSet = bitSet2;
                        bitSet.set(i3);
                        bitSet3 = bitSet;
                        z3 = z;
                        j2 = time;
                        i = 1;
                        basicTimeZone = this;
                    }
                }
                z = false;
                z4 = z2;
                bitSet = bitSet2;
                bitSet.set(i3);
                bitSet3 = bitSet;
                z3 = z;
                j2 = time;
                i = 1;
                basicTimeZone = this;
            }
        }
        return (TimeZoneRule[]) linkedList.toArray(new TimeZoneRule[linkedList.size()]);
    }

    public TimeZoneRule[] getSimpleTimeZoneRulesNear(long j) {
        AnnualTimeZoneRule[] annualTimeZoneRuleArr;
        int i;
        InitialTimeZoneRule initialTimeZoneRule;
        String str;
        int i2;
        int i3;
        String str2;
        int i4;
        AnnualTimeZoneRule[] annualTimeZoneRuleArr2;
        long j2;
        int i5;
        char c;
        TimeZoneTransition nextTransition;
        TimeZoneTransition nextTransition2 = getNextTransition(j, false);
        if (nextTransition2 != null) {
            String name = nextTransition2.getFrom().getName();
            int rawOffset = nextTransition2.getFrom().getRawOffset();
            int dSTSavings = nextTransition2.getFrom().getDSTSavings();
            long time = nextTransition2.getTime();
            if (((nextTransition2.getFrom().getDSTSavings() != 0 || nextTransition2.getTo().getDSTSavings() == 0) && (nextTransition2.getFrom().getDSTSavings() == 0 || nextTransition2.getTo().getDSTSavings() != 0)) || j + MILLIS_PER_YEAR <= time) {
                i2 = rawOffset;
                str2 = name;
                i4 = dSTSavings;
            } else {
                AnnualTimeZoneRule[] annualTimeZoneRuleArr3 = new AnnualTimeZoneRule[2];
                str2 = name;
                int[] timeToFields = Grego.timeToFields(((long) nextTransition2.getFrom().getRawOffset()) + time + ((long) nextTransition2.getFrom().getDSTSavings()), (int[]) null);
                annualTimeZoneRuleArr3[0] = new AnnualTimeZoneRule(nextTransition2.getTo().getName(), rawOffset, nextTransition2.getTo().getDSTSavings(), new DateTimeRule(timeToFields[1], Grego.getDayOfWeekInMonth(timeToFields[0], timeToFields[1], timeToFields[2]), timeToFields[3], timeToFields[5], 0), timeToFields[0], Integer.MAX_VALUE);
                if (nextTransition2.getTo().getRawOffset() != rawOffset || (nextTransition = getNextTransition(time, false)) == null || (((nextTransition.getFrom().getDSTSavings() != 0 || nextTransition.getTo().getDSTSavings() == 0) && (nextTransition.getFrom().getDSTSavings() == 0 || nextTransition.getTo().getDSTSavings() != 0)) || time + MILLIS_PER_YEAR <= nextTransition.getTime())) {
                    j2 = time;
                    annualTimeZoneRuleArr2 = annualTimeZoneRuleArr3;
                    i4 = dSTSavings;
                    i5 = rawOffset;
                } else {
                    timeToFields = Grego.timeToFields(nextTransition.getTime() + ((long) nextTransition.getFrom().getRawOffset()) + ((long) nextTransition.getFrom().getDSTSavings()), timeToFields);
                    AnnualTimeZoneRule annualTimeZoneRule = new AnnualTimeZoneRule(nextTransition.getTo().getName(), nextTransition.getTo().getRawOffset(), nextTransition.getTo().getDSTSavings(), new DateTimeRule(timeToFields[1], Grego.getDayOfWeekInMonth(timeToFields[0], timeToFields[1], timeToFields[2]), timeToFields[3], timeToFields[5], 0), timeToFields[0] - 1, Integer.MAX_VALUE);
                    j2 = time;
                    annualTimeZoneRuleArr2 = annualTimeZoneRuleArr3;
                    i4 = dSTSavings;
                    i5 = rawOffset;
                    Date previousStart = annualTimeZoneRule.getPreviousStart(j, nextTransition.getFrom().getRawOffset(), nextTransition.getFrom().getDSTSavings(), true);
                    if (previousStart != null && previousStart.getTime() <= j && i5 == nextTransition.getTo().getRawOffset() && i4 == nextTransition.getTo().getDSTSavings()) {
                        annualTimeZoneRuleArr2[1] = annualTimeZoneRule;
                    }
                }
                if (annualTimeZoneRuleArr2[1] == null) {
                    TimeZoneTransition previousTransition = getPreviousTransition(j, true);
                    if (previousTransition == null || ((previousTransition.getFrom().getDSTSavings() != 0 || previousTransition.getTo().getDSTSavings() == 0) && (previousTransition.getFrom().getDSTSavings() == 0 || previousTransition.getTo().getDSTSavings() != 0))) {
                        i2 = i5;
                    } else {
                        int[] timeToFields2 = Grego.timeToFields(previousTransition.getTime() + ((long) previousTransition.getFrom().getRawOffset()) + ((long) previousTransition.getFrom().getDSTSavings()), timeToFields);
                        i2 = i5;
                        AnnualTimeZoneRule annualTimeZoneRule2 = new AnnualTimeZoneRule(previousTransition.getTo().getName(), i2, i4, new DateTimeRule(timeToFields2[1], Grego.getDayOfWeekInMonth(timeToFields2[0], timeToFields2[1], timeToFields2[2]), timeToFields2[3], timeToFields2[5], 0), annualTimeZoneRuleArr2[0].getStartYear() - 1, Integer.MAX_VALUE);
                        if (annualTimeZoneRule2.getNextStart(j, previousTransition.getFrom().getRawOffset(), previousTransition.getFrom().getDSTSavings(), false).getTime() > j2) {
                            c = 1;
                            annualTimeZoneRuleArr2[1] = annualTimeZoneRule2;
                        }
                    }
                    c = 1;
                } else {
                    c = 1;
                    i2 = i5;
                }
                if (annualTimeZoneRuleArr2[c] != null) {
                    str = annualTimeZoneRuleArr2[0].getName();
                    int rawOffset2 = annualTimeZoneRuleArr2[0].getRawOffset();
                    i3 = annualTimeZoneRuleArr2[0].getDSTSavings();
                    i2 = rawOffset2;
                    annualTimeZoneRuleArr = annualTimeZoneRuleArr2;
                    initialTimeZoneRule = new InitialTimeZoneRule(str, i2, i3);
                    i = 1;
                }
            }
            i3 = i4;
            str = str2;
            annualTimeZoneRuleArr = null;
            initialTimeZoneRule = new InitialTimeZoneRule(str, i2, i3);
            i = 1;
        } else {
            TimeZoneTransition previousTransition2 = getPreviousTransition(j, true);
            if (previousTransition2 != null) {
                initialTimeZoneRule = new InitialTimeZoneRule(previousTransition2.getTo().getName(), previousTransition2.getTo().getRawOffset(), previousTransition2.getTo().getDSTSavings());
                i = 1;
            } else {
                int[] iArr = new int[2];
                getOffset(j, false, iArr);
                i = 1;
                initialTimeZoneRule = new InitialTimeZoneRule(getID(), iArr[0], iArr[1]);
            }
            annualTimeZoneRuleArr = null;
        }
        if (annualTimeZoneRuleArr == null) {
            TimeZoneRule[] timeZoneRuleArr = new TimeZoneRule[i];
            timeZoneRuleArr[0] = initialTimeZoneRule;
            return timeZoneRuleArr;
        }
        TimeZoneRule[] timeZoneRuleArr2 = new TimeZoneRule[3];
        timeZoneRuleArr2[0] = initialTimeZoneRule;
        timeZoneRuleArr2[i] = annualTimeZoneRuleArr[0];
        timeZoneRuleArr2[2] = annualTimeZoneRuleArr[i];
        return timeZoneRuleArr2;
    }

    @Deprecated
    public void getOffsetFromLocal(long j, int i, int i2, int[] iArr) {
        throw new IllegalStateException("Not implemented");
    }

    protected BasicTimeZone() {
    }

    @Deprecated
    protected BasicTimeZone(String str) {
        super(str);
    }
}
