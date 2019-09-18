package android.icu.impl;

import android.icu.lang.UCharacterEnums;
import android.icu.util.AnnualTimeZoneRule;
import android.icu.util.BasicTimeZone;
import android.icu.util.DateTimeRule;
import android.icu.util.InitialTimeZoneRule;
import android.icu.util.SimpleTimeZone;
import android.icu.util.TimeArrayTimeZoneRule;
import android.icu.util.TimeZone;
import android.icu.util.TimeZoneRule;
import android.icu.util.TimeZoneTransition;
import android.icu.util.UResourceBundle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.MissingResourceException;

public class OlsonTimeZone extends BasicTimeZone {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final boolean DEBUG = ICUDebug.enabled("olson");
    private static final int MAX_OFFSET_SECONDS = 86400;
    private static final int SECONDS_PER_DAY = 86400;
    private static final String ZONEINFORES = "zoneinfo64";
    private static final int currentSerialVersion = 1;
    static final long serialVersionUID = -6281977362477515376L;
    private volatile String canonicalID = null;
    private double finalStartMillis = Double.MAX_VALUE;
    private int finalStartYear = Integer.MAX_VALUE;
    private SimpleTimeZone finalZone = null;
    private transient SimpleTimeZone finalZoneWithStartYear;
    private transient TimeZoneTransition firstFinalTZTransition;
    private transient TimeZoneTransition firstTZTransition;
    private transient int firstTZTransitionIdx;
    private transient TimeArrayTimeZoneRule[] historicRules;
    private transient InitialTimeZoneRule initialRule;
    private volatile transient boolean isFrozen = false;
    private int serialVersionOnStream = 1;
    private int transitionCount;
    private transient boolean transitionRulesInitialized;
    private long[] transitionTimes64;
    private int typeCount;
    private byte[] typeMapData;
    private int[] typeOffsets;

    public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Month is not in the legal range: " + month);
        }
        return getOffset(era, year, month, day, dayOfWeek, milliseconds, Grego.monthLength(year, month));
    }

    public int getOffset(int era, int year, int month, int dom, int dow, int millis, int monthLength) {
        int year2;
        int i = era;
        int i2 = month;
        int i3 = dom;
        int i4 = dow;
        int i5 = millis;
        int i6 = monthLength;
        if ((i == 1 || i == 0) && i2 >= 0 && i2 <= 11 && i3 >= 1 && i3 <= i6 && i4 >= 1 && i4 <= 7 && i5 >= 0 && i5 < 86400000 && i6 >= 28 && i6 <= 31) {
            if (i == 0) {
                year2 = -year;
            } else {
                year2 = year;
            }
            int year3 = year2;
            if (this.finalZone != null && year3 >= this.finalStartYear) {
                return this.finalZone.getOffset(i, year3, i2, i3, i4, i5);
            }
            int[] offsets = new int[2];
            int[] offsets2 = offsets;
            getHistoricalOffset((Grego.fieldsToDay(year3, i2, i3) * 86400000) + ((long) i5), true, 3, 1, offsets);
            return offsets2[0] + offsets2[1];
        }
        int i7 = year;
        throw new IllegalArgumentException();
    }

    public void setRawOffset(int offsetMillis) {
        int sav;
        DateTimeRule end;
        DateTimeRule start;
        int i = offsetMillis;
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen OlsonTimeZone instance.");
        } else if (getRawOffset() != i) {
            long current = System.currentTimeMillis();
            if (((double) current) < this.finalStartMillis) {
                SimpleTimeZone stz = new SimpleTimeZone(i, getID());
                boolean bDst = useDaylightTime();
                if (bDst) {
                    TimeZoneRule[] currentRules = getSimpleTimeZoneRulesNear(current);
                    if (currentRules.length != 3) {
                        TimeZoneTransition tzt = getPreviousTransition(current, false);
                        if (tzt != null) {
                            currentRules = getSimpleTimeZoneRulesNear(tzt.getTime() - 1);
                        }
                    }
                    if (currentRules.length != 3 || !(currentRules[1] instanceof AnnualTimeZoneRule) || !(currentRules[2] instanceof AnnualTimeZoneRule)) {
                        stz.setStartRule(0, 1, 0);
                        stz.setEndRule(11, 31, 86399999);
                    } else {
                        AnnualTimeZoneRule r1 = (AnnualTimeZoneRule) currentRules[1];
                        AnnualTimeZoneRule r2 = (AnnualTimeZoneRule) currentRules[2];
                        int offset1 = r1.getRawOffset() + r1.getDSTSavings();
                        int offset2 = r2.getRawOffset() + r2.getDSTSavings();
                        if (offset1 > offset2) {
                            start = r1.getRule();
                            end = r2.getRule();
                            sav = offset1 - offset2;
                        } else {
                            start = r2.getRule();
                            end = r1.getRule();
                            sav = offset2 - offset1;
                        }
                        TimeZoneRule[] timeZoneRuleArr = currentRules;
                        AnnualTimeZoneRule annualTimeZoneRule = r2;
                        stz.setStartRule(start.getRuleMonth(), start.getRuleWeekInMonth(), start.getRuleDayOfWeek(), start.getRuleMillisInDay());
                        stz.setEndRule(end.getRuleMonth(), end.getRuleWeekInMonth(), end.getRuleDayOfWeek(), end.getRuleMillisInDay());
                        stz.setDSTSavings(sav);
                    }
                }
                int[] fields = Grego.timeToFields(current, null);
                this.finalStartYear = fields[0];
                this.finalStartMillis = (double) Grego.fieldsToDay(fields[0], 0, 1);
                if (bDst) {
                    stz.setStartYear(this.finalStartYear);
                }
                this.finalZone = stz;
            } else {
                this.finalZone.setRawOffset(i);
            }
            this.transitionRulesInitialized = false;
        }
    }

    public Object clone() {
        if (isFrozen()) {
            return this;
        }
        return cloneAsThawed();
    }

    public void getOffset(long date, boolean local, int[] offsets) {
        if (this.finalZone == null || ((double) date) < this.finalStartMillis) {
            getHistoricalOffset(date, local, 4, 12, offsets);
        } else {
            this.finalZone.getOffset(date, local, offsets);
        }
    }

    public void getOffsetFromLocal(long date, int nonExistingTimeOpt, int duplicatedTimeOpt, int[] offsets) {
        if (this.finalZone == null || ((double) date) < this.finalStartMillis) {
            getHistoricalOffset(date, true, nonExistingTimeOpt, duplicatedTimeOpt, offsets);
        } else {
            this.finalZone.getOffsetFromLocal(date, nonExistingTimeOpt, duplicatedTimeOpt, offsets);
        }
    }

    public int getRawOffset() {
        int[] ret = new int[2];
        getOffset(System.currentTimeMillis(), false, ret);
        return ret[0];
    }

    public boolean useDaylightTime() {
        long current = System.currentTimeMillis();
        boolean z = false;
        if (this.finalZone == null || ((double) current) < this.finalStartMillis) {
            int[] fields = Grego.timeToFields(current, null);
            long start = Grego.fieldsToDay(fields[0], 0, 1) * 86400;
            long limit = Grego.fieldsToDay(fields[0] + 1, 0, 1) * 86400;
            int i = 0;
            while (i < this.transitionCount && this.transitionTimes64[i] < limit) {
                if ((this.transitionTimes64[i] >= start && dstOffsetAt(i) != 0) || (this.transitionTimes64[i] > start && i > 0 && dstOffsetAt(i - 1) != 0)) {
                    return true;
                }
                i++;
            }
            return false;
        }
        if (this.finalZone != null && this.finalZone.useDaylightTime()) {
            z = true;
        }
        return z;
    }

    public boolean observesDaylightTime() {
        long current = System.currentTimeMillis();
        if (this.finalZone != null) {
            if (this.finalZone.useDaylightTime()) {
                return true;
            }
            if (((double) current) >= this.finalStartMillis) {
                return false;
            }
        }
        long currentSec = Grego.floorDivide(current, 1000);
        int trsIdx = this.transitionCount - 1;
        if (dstOffsetAt(trsIdx) != 0) {
            return true;
        }
        while (trsIdx >= 0 && this.transitionTimes64[trsIdx] > currentSec) {
            if (dstOffsetAt(trsIdx - 1) != 0) {
                return true;
            }
            trsIdx--;
        }
        return false;
    }

    public int getDSTSavings() {
        if (this.finalZone != null) {
            return this.finalZone.getDSTSavings();
        }
        return super.getDSTSavings();
    }

    public boolean inDaylightTime(Date date) {
        int[] temp = new int[2];
        getOffset(date.getTime(), false, temp);
        return temp[1] != 0;
    }

    public boolean hasSameRules(TimeZone other) {
        if (this == other) {
            return true;
        }
        if (!super.hasSameRules(other) || !(other instanceof OlsonTimeZone)) {
            return false;
        }
        OlsonTimeZone o = (OlsonTimeZone) other;
        if (this.finalZone == null) {
            if (o.finalZone != null) {
                return false;
            }
        } else if (o.finalZone == null || this.finalStartYear != o.finalStartYear || !this.finalZone.hasSameRules(o.finalZone)) {
            return false;
        }
        if (this.transitionCount != o.transitionCount || !Arrays.equals(this.transitionTimes64, o.transitionTimes64) || this.typeCount != o.typeCount || !Arrays.equals(this.typeMapData, o.typeMapData) || !Arrays.equals(this.typeOffsets, o.typeOffsets)) {
            return false;
        }
        return true;
    }

    public String getCanonicalID() {
        if (this.canonicalID == null) {
            synchronized (this) {
                if (this.canonicalID == null) {
                    this.canonicalID = getCanonicalID(getID());
                    if (this.canonicalID == null) {
                        this.canonicalID = getID();
                    }
                }
            }
        }
        return this.canonicalID;
    }

    private void constructEmpty() {
        this.transitionCount = 0;
        this.transitionTimes64 = null;
        this.typeMapData = null;
        this.typeCount = 1;
        this.typeOffsets = new int[]{0, 0};
        this.finalZone = null;
        this.finalStartYear = Integer.MAX_VALUE;
        this.finalStartMillis = Double.MAX_VALUE;
        this.transitionRulesInitialized = false;
    }

    public OlsonTimeZone(UResourceBundle top, UResourceBundle res, String id) {
        super(id);
        construct(top, res);
    }

    private void construct(UResourceBundle top, UResourceBundle res) {
        SimpleTimeZone simpleTimeZone;
        int[] transPost32;
        int idx;
        UResourceBundle uResourceBundle = top;
        UResourceBundle uResourceBundle2 = res;
        if (uResourceBundle == null || uResourceBundle2 == null) {
            throw new IllegalArgumentException();
        }
        if (DEBUG) {
            System.out.println("OlsonTimeZone(" + res.getKey() + ")");
        }
        int[] transPost322 = null;
        int[] trans32 = null;
        int[] transPre32 = null;
        this.transitionCount = 0;
        int i = 2;
        try {
            transPre32 = uResourceBundle2.get("transPre32").getIntVector();
            if (transPre32.length % 2 == 0) {
                this.transitionCount += transPre32.length / 2;
                try {
                    trans32 = uResourceBundle2.get("trans").getIntVector();
                    this.transitionCount += trans32.length;
                } catch (MissingResourceException e) {
                }
                try {
                    transPost322 = uResourceBundle2.get("transPost32").getIntVector();
                    if (transPost322.length % 2 == 0) {
                        this.transitionCount += transPost322.length / 2;
                        int i2 = 1;
                        if (this.transitionCount > 0) {
                            this.transitionTimes64 = new long[this.transitionCount];
                            char c = ' ';
                            if (transPre32 != null) {
                                idx = 0;
                                int i3 = 0;
                                while (i3 < transPre32.length / 2) {
                                    this.transitionTimes64[idx] = ((((long) transPre32[i3 * 2]) & 4294967295L) << c) | (((long) transPre32[(i3 * 2) + i2]) & 4294967295L);
                                    i3++;
                                    idx++;
                                    transPost322 = transPost322;
                                    i2 = 1;
                                    c = ' ';
                                }
                                transPost32 = transPost322;
                            } else {
                                transPost32 = transPost322;
                                idx = 0;
                            }
                            if (trans32 != null) {
                                int i4 = 0;
                                while (i4 < trans32.length) {
                                    this.transitionTimes64[idx] = (long) trans32[i4];
                                    i4++;
                                    idx++;
                                }
                            }
                            if (transPost32 != null) {
                                int i5 = 0;
                                while (true) {
                                    int[] transPost323 = transPost32;
                                    if (i5 >= transPost323.length / i) {
                                        break;
                                    }
                                    this.transitionTimes64[idx] = (((long) transPost323[(i5 * 2) + 1]) & 4294967295L) | ((((long) transPost323[i5 * 2]) & 4294967295L) << 32);
                                    i5++;
                                    idx++;
                                    transPost32 = transPost323;
                                    i = 2;
                                }
                            }
                        } else {
                            this.transitionTimes64 = null;
                        }
                        UResourceBundle r = uResourceBundle2.get("typeOffsets");
                        this.typeOffsets = r.getIntVector();
                        if (this.typeOffsets.length < 2 || this.typeOffsets.length > 32766 || this.typeOffsets.length % 2 != 0) {
                            throw new IllegalArgumentException("Invalid Format");
                        }
                        this.typeCount = this.typeOffsets.length / 2;
                        if (this.transitionCount > 0) {
                            UResourceBundle r2 = uResourceBundle2.get("typeMap");
                            this.typeMapData = r2.getBinary(null);
                            if (this.typeMapData == null || this.typeMapData.length != this.transitionCount) {
                                throw new IllegalArgumentException("Invalid Format");
                            }
                            UResourceBundle uResourceBundle3 = r2;
                            simpleTimeZone = null;
                        } else {
                            simpleTimeZone = null;
                            this.typeMapData = null;
                            UResourceBundle uResourceBundle4 = r;
                        }
                        this.finalZone = simpleTimeZone;
                        this.finalStartYear = Integer.MAX_VALUE;
                        this.finalStartMillis = Double.MAX_VALUE;
                        try {
                            String ruleID = uResourceBundle2.getString("finalRule");
                            int ruleRaw = uResourceBundle2.get("finalRaw").getInt() * 1000;
                            int[] ruleData = loadRule(uResourceBundle, ruleID).getIntVector();
                            if (ruleData == null || ruleData.length != 11) {
                                throw new IllegalArgumentException("Invalid Format");
                            }
                            SimpleTimeZone simpleTimeZone2 = new SimpleTimeZone(ruleRaw, "", ruleData[0], ruleData[1], ruleData[2], ruleData[3] * 1000, ruleData[4], ruleData[5], ruleData[6], ruleData[7], ruleData[8] * 1000, ruleData[9], ruleData[10] * 1000);
                            this.finalZone = simpleTimeZone2;
                            this.finalStartYear = uResourceBundle2.get("finalYear").getInt();
                            this.finalStartMillis = (double) (Grego.fieldsToDay(this.finalStartYear, 0, 1) * 86400000);
                        } catch (MissingResourceException e2) {
                            if (simpleTimeZone != null) {
                                throw new IllegalArgumentException("Invalid Format");
                            }
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid Format");
                    }
                } catch (MissingResourceException e3) {
                }
            } else {
                throw new IllegalArgumentException("Invalid Format");
            }
        } catch (MissingResourceException e4) {
        }
    }

    public OlsonTimeZone(String id) {
        super(id);
        UResourceBundle top = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, ZONEINFORES, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        construct(top, ZoneMeta.openOlsonResource(top, id));
        if (this.finalZone != null) {
            this.finalZone.setID(id);
        }
    }

    public void setID(String id) {
        if (!isFrozen()) {
            if (this.canonicalID == null) {
                this.canonicalID = getCanonicalID(getID());
                if (this.canonicalID == null) {
                    this.canonicalID = getID();
                }
            }
            if (this.finalZone != null) {
                this.finalZone.setID(id);
            }
            super.setID(id);
            this.transitionRulesInitialized = false;
            return;
        }
        throw new UnsupportedOperationException("Attempt to modify a frozen OlsonTimeZone instance.");
    }

    private void getHistoricalOffset(long date, boolean local, int NonExistingTimeOpt, int DuplicatedTimeOpt, int[] offsets) {
        boolean z = false;
        boolean z2 = true;
        if (this.transitionCount != 0) {
            long sec = Grego.floorDivide(date, 1000);
            if (local || sec >= this.transitionTimes64[0]) {
                int transIdx = this.transitionCount - 1;
                while (transIdx >= 0) {
                    long transition = this.transitionTimes64[transIdx];
                    if (local && sec >= transition - 86400) {
                        int offsetBefore = zoneOffsetAt(transIdx - 1);
                        boolean dstBefore = dstOffsetAt(transIdx + -1) != 0 ? z2 : z;
                        int offsetAfter = zoneOffsetAt(transIdx);
                        boolean dstAfter = dstOffsetAt(transIdx) != 0 ? z2 : false;
                        boolean dstToStd = (!dstBefore || dstAfter) ? false : z2;
                        boolean stdToDst = (dstBefore || !dstAfter) ? false : z2;
                        transition = offsetAfter - offsetBefore >= 0 ? (((NonExistingTimeOpt & 3) != 1 || !dstToStd) && ((NonExistingTimeOpt & 3) != 3 || !stdToDst)) ? (((NonExistingTimeOpt & 3) != 1 || !stdToDst) && ((NonExistingTimeOpt & 3) != 3 || !dstToStd)) ? (NonExistingTimeOpt & 12) == 12 ? transition + ((long) offsetBefore) : transition + ((long) offsetAfter) : transition + ((long) offsetAfter) : transition + ((long) offsetBefore) : (((DuplicatedTimeOpt & 3) != 1 || !dstToStd) && ((DuplicatedTimeOpt & 3) != 3 || !stdToDst)) ? (((DuplicatedTimeOpt & 3) != 1 || !stdToDst) && ((DuplicatedTimeOpt & 3) != 3 || !dstToStd)) ? (DuplicatedTimeOpt & 12) == 4 ? transition + ((long) offsetBefore) : transition + ((long) offsetAfter) : transition + ((long) offsetBefore) : transition + ((long) offsetAfter);
                    }
                    if (sec >= transition) {
                        break;
                    }
                    transIdx--;
                    z = false;
                    z2 = true;
                    long j = date;
                }
                offsets[0] = rawOffsetAt(transIdx) * 1000;
                offsets[1] = dstOffsetAt(transIdx) * 1000;
                return;
            }
            offsets[0] = initialRawOffset() * 1000;
            offsets[1] = initialDstOffset() * 1000;
            return;
        }
        offsets[0] = initialRawOffset() * 1000;
        offsets[1] = initialDstOffset() * 1000;
    }

    private int getInt(byte val) {
        return val & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED;
    }

    private int zoneOffsetAt(int transIdx) {
        int typeIdx = transIdx >= 0 ? getInt(this.typeMapData[transIdx]) * 2 : 0;
        return this.typeOffsets[typeIdx] + this.typeOffsets[typeIdx + 1];
    }

    private int rawOffsetAt(int transIdx) {
        return this.typeOffsets[transIdx >= 0 ? getInt(this.typeMapData[transIdx]) * 2 : 0];
    }

    private int dstOffsetAt(int transIdx) {
        return this.typeOffsets[(transIdx >= 0 ? getInt(this.typeMapData[transIdx]) * 2 : 0) + 1];
    }

    private int initialRawOffset() {
        return this.typeOffsets[0];
    }

    private int initialDstOffset() {
        return this.typeOffsets[1];
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append('[');
        buf.append("transitionCount=" + this.transitionCount);
        buf.append(",typeCount=" + this.typeCount);
        buf.append(",transitionTimes=");
        int i = 0;
        if (this.transitionTimes64 != null) {
            buf.append('[');
            for (int i2 = 0; i2 < this.transitionTimes64.length; i2++) {
                if (i2 > 0) {
                    buf.append(',');
                }
                buf.append(Long.toString(this.transitionTimes64[i2]));
            }
            buf.append(']');
        } else {
            buf.append("null");
        }
        buf.append(",typeOffsets=");
        if (this.typeOffsets != null) {
            buf.append('[');
            for (int i3 = 0; i3 < this.typeOffsets.length; i3++) {
                if (i3 > 0) {
                    buf.append(',');
                }
                buf.append(Integer.toString(this.typeOffsets[i3]));
            }
            buf.append(']');
        } else {
            buf.append("null");
        }
        buf.append(",typeMapData=");
        if (this.typeMapData != null) {
            buf.append('[');
            while (true) {
                int i4 = i;
                if (i4 >= this.typeMapData.length) {
                    break;
                }
                if (i4 > 0) {
                    buf.append(',');
                }
                buf.append(Byte.toString(this.typeMapData[i4]));
                i = i4 + 1;
            }
        } else {
            buf.append("null");
        }
        buf.append(",finalStartYear=" + this.finalStartYear);
        buf.append(",finalStartMillis=" + this.finalStartMillis);
        buf.append(",finalZone=" + this.finalZone);
        buf.append(']');
        return buf.toString();
    }

    private static UResourceBundle loadRule(UResourceBundle top, String ruleid) {
        return top.get("Rules").get(ruleid);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!super.equals(obj)) {
            return false;
        }
        OlsonTimeZone z2 = (OlsonTimeZone) obj;
        if (Utility.arrayEquals(this.typeMapData, (Object) z2.typeMapData) || (this.finalStartYear == z2.finalStartYear && ((this.finalZone == null && z2.finalZone == null) || (this.finalZone != null && z2.finalZone != null && this.finalZone.equals(z2.finalZone) && this.transitionCount == z2.transitionCount && this.typeCount == z2.typeCount && Utility.arrayEquals((Object) this.transitionTimes64, (Object) z2.transitionTimes64) && Utility.arrayEquals(this.typeOffsets, (Object) z2.typeOffsets) && Utility.arrayEquals(this.typeMapData, (Object) z2.typeMapData))))) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        int i = 0;
        int i2 = (int) (((long) ((this.finalStartYear ^ ((this.finalStartYear >>> 4) + this.transitionCount)) ^ ((this.transitionCount >>> 6) + this.typeCount))) ^ (((((long) (this.typeCount >>> 8)) + Double.doubleToLongBits(this.finalStartMillis)) + ((long) (this.finalZone == null ? 0 : this.finalZone.hashCode()))) + ((long) super.hashCode())));
        if (this.transitionTimes64 != null) {
            int ret = i2;
            for (int i3 = 0; i3 < this.transitionTimes64.length; i3++) {
                ret = (int) (((long) ret) + (this.transitionTimes64[i3] ^ (this.transitionTimes64[i3] >>> 8)));
            }
            i2 = ret;
        }
        int ret2 = i2;
        for (int i4 = 0; i4 < this.typeOffsets.length; i4++) {
            ret2 += this.typeOffsets[i4] ^ (this.typeOffsets[i4] >>> 8);
        }
        if (this.typeMapData != null) {
            while (true) {
                int i5 = i;
                if (i5 >= this.typeMapData.length) {
                    break;
                }
                ret2 += this.typeMapData[i5] & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED;
                i = i5 + 1;
            }
        }
        return ret2;
    }

    public TimeZoneTransition getNextTransition(long base, boolean inclusive) {
        initTransitionRules();
        if (this.finalZone != null) {
            if (inclusive && base == this.firstFinalTZTransition.getTime()) {
                return this.firstFinalTZTransition;
            }
            if (base >= this.firstFinalTZTransition.getTime()) {
                if (this.finalZone.useDaylightTime()) {
                    return this.finalZoneWithStartYear.getNextTransition(base, inclusive);
                }
                return null;
            }
        }
        if (this.historicRules == null) {
            return null;
        }
        int ttidx = this.transitionCount;
        while (true) {
            ttidx--;
            if (ttidx < this.firstTZTransitionIdx) {
                break;
            }
            long t = this.transitionTimes64[ttidx] * 1000;
            if (base > t || (!inclusive && base == t)) {
                break;
            }
        }
        if (ttidx == this.transitionCount - 1) {
            return this.firstFinalTZTransition;
        }
        if (ttidx < this.firstTZTransitionIdx) {
            return this.firstTZTransition;
        }
        TimeZoneRule to = this.historicRules[getInt(this.typeMapData[ttidx + 1])];
        TimeZoneRule from = this.historicRules[getInt(this.typeMapData[ttidx])];
        long startTime = this.transitionTimes64[ttidx + 1] * 1000;
        if (from.getName().equals(to.getName()) && from.getRawOffset() == to.getRawOffset() && from.getDSTSavings() == to.getDSTSavings()) {
            return getNextTransition(startTime, false);
        }
        return new TimeZoneTransition(startTime, from, to);
    }

    public TimeZoneTransition getPreviousTransition(long base, boolean inclusive) {
        initTransitionRules();
        if (this.finalZone != null) {
            if (inclusive && base == this.firstFinalTZTransition.getTime()) {
                return this.firstFinalTZTransition;
            }
            if (base > this.firstFinalTZTransition.getTime()) {
                if (this.finalZone.useDaylightTime()) {
                    return this.finalZoneWithStartYear.getPreviousTransition(base, inclusive);
                }
                return this.firstFinalTZTransition;
            }
        }
        if (this.historicRules == null) {
            return null;
        }
        int ttidx = this.transitionCount;
        while (true) {
            ttidx--;
            if (ttidx < this.firstTZTransitionIdx) {
                break;
            }
            long t = this.transitionTimes64[ttidx] * 1000;
            if (base > t || (inclusive && base == t)) {
                break;
            }
        }
        if (ttidx < this.firstTZTransitionIdx) {
            return null;
        }
        if (ttidx == this.firstTZTransitionIdx) {
            return this.firstTZTransition;
        }
        TimeZoneRule to = this.historicRules[getInt(this.typeMapData[ttidx])];
        TimeZoneRule from = this.historicRules[getInt(this.typeMapData[ttidx - 1])];
        long startTime = this.transitionTimes64[ttidx] * 1000;
        if (from.getName().equals(to.getName()) && from.getRawOffset() == to.getRawOffset() && from.getDSTSavings() == to.getDSTSavings()) {
            return getPreviousTransition(startTime, false);
        }
        return new TimeZoneTransition(startTime, from, to);
    }

    public TimeZoneRule[] getTimeZoneRules() {
        initTransitionRules();
        int i = 1;
        if (this.historicRules != null) {
            int size = 1;
            for (TimeArrayTimeZoneRule timeArrayTimeZoneRule : this.historicRules) {
                if (timeArrayTimeZoneRule != null) {
                    size++;
                }
            }
            i = size;
        }
        if (this.finalZone != null) {
            if (this.finalZone.useDaylightTime()) {
                i += 2;
            } else {
                i++;
            }
        }
        TimeZoneRule[] rules = new TimeZoneRule[i];
        int idx = 0 + 1;
        rules[0] = this.initialRule;
        if (this.historicRules != null) {
            for (int i2 = 0; i2 < this.historicRules.length; i2++) {
                if (this.historicRules[i2] != null) {
                    rules[idx] = this.historicRules[i2];
                    idx++;
                }
            }
        }
        if (this.finalZone != null) {
            if (this.finalZone.useDaylightTime()) {
                TimeZoneRule[] stzr = this.finalZoneWithStartYear.getTimeZoneRules();
                int idx2 = idx + 1;
                rules[idx] = stzr[1];
                int idx3 = idx2 + 1;
                rules[idx2] = stzr[2];
            } else {
                TimeArrayTimeZoneRule timeArrayTimeZoneRule2 = new TimeArrayTimeZoneRule(getID() + "(STD)", this.finalZone.getRawOffset(), 0, new long[]{(long) this.finalStartMillis}, 2);
                rules[idx] = timeArrayTimeZoneRule2;
                int idx4 = idx + 1;
            }
        }
        return rules;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v0, resolved type: android.icu.util.InitialTimeZoneRule} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v1, resolved type: android.icu.util.TimeArrayTimeZoneRule} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v2, resolved type: android.icu.util.InitialTimeZoneRule} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v3, resolved type: android.icu.util.InitialTimeZoneRule} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v4, resolved type: android.icu.util.TimeArrayTimeZoneRule} */
    /* JADX WARNING: type inference failed for: r8v11, types: [android.icu.util.TimeZoneRule] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    private synchronized void initTransitionRules() {
        TimeArrayTimeZoneRule timeArrayTimeZoneRule;
        String dstName;
        synchronized (this) {
            if (!this.transitionRulesInitialized) {
                this.initialRule = null;
                this.firstTZTransition = null;
                this.firstFinalTZTransition = null;
                this.historicRules = null;
                this.firstTZTransitionIdx = 0;
                this.finalZoneWithStartYear = null;
                String stdName = getID() + "(STD)";
                String dstName2 = getID() + "(DST)";
                int raw = initialRawOffset() * 1000;
                int dst = initialDstOffset() * 1000;
                this.initialRule = new InitialTimeZoneRule(dst == 0 ? stdName : dstName2, raw, dst);
                if (this.transitionCount > 0) {
                    int transitionIdx = 0;
                    while (true) {
                        if (transitionIdx >= this.transitionCount) {
                            break;
                        } else if (getInt(this.typeMapData[transitionIdx]) != 0) {
                            break;
                        } else {
                            this.firstTZTransitionIdx++;
                            transitionIdx++;
                        }
                    }
                    if (transitionIdx == this.transitionCount) {
                        String str = dstName2;
                    } else {
                        long[] times = new long[this.transitionCount];
                        int i = transitionIdx;
                        int dst2 = dst;
                        int dst3 = raw;
                        int typeIdx = 0;
                        while (true) {
                            long j = 1000;
                            if (typeIdx >= this.typeCount) {
                                break;
                            }
                            int nTimes = 0;
                            int transitionIdx2 = this.firstTZTransitionIdx;
                            while (transitionIdx2 < this.transitionCount) {
                                if (typeIdx == getInt(this.typeMapData[transitionIdx2])) {
                                    long tt = this.transitionTimes64[transitionIdx2] * j;
                                    dstName = dstName2;
                                    if (((double) tt) < this.finalStartMillis) {
                                        times[nTimes] = tt;
                                        nTimes++;
                                    }
                                } else {
                                    dstName = dstName2;
                                }
                                transitionIdx2++;
                                dstName2 = dstName;
                                j = 1000;
                            }
                            String dstName3 = dstName2;
                            if (nTimes > 0) {
                                long[] startTimes = new long[nTimes];
                                System.arraycopy(times, 0, startTimes, 0, nTimes);
                                int raw2 = this.typeOffsets[typeIdx * 2] * 1000;
                                int dst4 = this.typeOffsets[(typeIdx * 2) + 1] * 1000;
                                if (this.historicRules == null) {
                                    this.historicRules = new TimeArrayTimeZoneRule[this.typeCount];
                                }
                                TimeArrayTimeZoneRule[] timeArrayTimeZoneRuleArr = this.historicRules;
                                TimeArrayTimeZoneRule timeArrayTimeZoneRule2 = new TimeArrayTimeZoneRule(dst4 == 0 ? stdName : dstName3, raw2, dst4, startTimes, 2);
                                timeArrayTimeZoneRuleArr[typeIdx] = timeArrayTimeZoneRule2;
                                dst2 = dst4;
                                dst3 = raw2;
                            }
                            typeIdx++;
                            dstName2 = dstName3;
                        }
                        this.firstTZTransition = new TimeZoneTransition(this.transitionTimes64[this.firstTZTransitionIdx] * 1000, this.initialRule, this.historicRules[getInt(this.typeMapData[this.firstTZTransitionIdx])]);
                        int i2 = dst3;
                        int raw3 = dst2;
                    }
                }
                if (this.finalZone != null) {
                    long startTime = (long) this.finalStartMillis;
                    if (this.finalZone.useDaylightTime()) {
                        this.finalZoneWithStartYear = (SimpleTimeZone) this.finalZone.clone();
                        this.finalZoneWithStartYear.setStartYear(this.finalStartYear);
                        TimeZoneTransition tzt = this.finalZoneWithStartYear.getNextTransition(startTime, false);
                        ? to = tzt.getTo();
                        startTime = tzt.getTime();
                        timeArrayTimeZoneRule = to;
                    } else {
                        this.finalZoneWithStartYear = this.finalZone;
                        timeArrayTimeZoneRule = new TimeArrayTimeZoneRule(this.finalZone.getID(), this.finalZone.getRawOffset(), 0, new long[]{startTime}, 2);
                    }
                    TimeZoneRule prevRule = null;
                    if (this.transitionCount > 0) {
                        prevRule = this.historicRules[getInt(this.typeMapData[this.transitionCount - 1])];
                    }
                    if (prevRule == null) {
                        prevRule = this.initialRule;
                    }
                    this.firstFinalTZTransition = new TimeZoneTransition(startTime, prevRule, timeArrayTimeZoneRule);
                }
                this.transitionRulesInitialized = true;
            }
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (this.serialVersionOnStream < 1) {
            boolean initialized = false;
            String tzid = getID();
            if (tzid != null) {
                try {
                    UResourceBundle top = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, ZONEINFORES, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
                    construct(top, ZoneMeta.openOlsonResource(top, tzid));
                    if (this.finalZone != null) {
                        this.finalZone.setID(tzid);
                    }
                    initialized = true;
                } catch (Exception e) {
                }
            }
            if (!initialized) {
                constructEmpty();
            }
        }
        this.transitionRulesInitialized = false;
    }

    public boolean isFrozen() {
        return this.isFrozen;
    }

    public TimeZone freeze() {
        this.isFrozen = true;
        return this;
    }

    public TimeZone cloneAsThawed() {
        OlsonTimeZone tz = (OlsonTimeZone) super.cloneAsThawed();
        if (this.finalZone != null) {
            this.finalZone.setID(getID());
            tz.finalZone = (SimpleTimeZone) this.finalZone.clone();
        }
        tz.isFrozen = false;
        return tz;
    }
}
