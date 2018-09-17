package android.icu.impl;

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
    static final /* synthetic */ boolean -assertionsDisabled = (OlsonTimeZone.class.desiredAssertionStatus() ^ 1);
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
        if ((era == 1 || era == 0) && month >= 0 && month <= 11 && dom >= 1 && dom <= monthLength && dow >= 1 && dow <= 7 && millis >= 0 && millis < Grego.MILLIS_PER_DAY && monthLength >= 28 && monthLength <= 31) {
            if (era == 0) {
                year = -year;
            }
            if (this.finalZone != null && year >= this.finalStartYear) {
                return this.finalZone.getOffset(era, year, month, dom, dow, millis);
            }
            int[] offsets = new int[2];
            getHistoricalOffset((Grego.fieldsToDay(year, month, dom) * 86400000) + ((long) millis), true, 3, 1, offsets);
            return offsets[0] + offsets[1];
        }
        throw new IllegalArgumentException();
    }

    public void setRawOffset(int offsetMillis) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen OlsonTimeZone instance.");
        } else if (getRawOffset() != offsetMillis) {
            long current = System.currentTimeMillis();
            if (((double) current) < this.finalStartMillis) {
                SimpleTimeZone simpleTimeZone = new SimpleTimeZone(offsetMillis, getID());
                boolean bDst = useDaylightTime();
                if (bDst) {
                    TimeZoneRule[] currentRules = getSimpleTimeZoneRulesNear(current);
                    if (currentRules.length != 3) {
                        TimeZoneTransition tzt = getPreviousTransition(current, false);
                        if (tzt != null) {
                            currentRules = getSimpleTimeZoneRulesNear(tzt.getTime() - 1);
                        }
                    }
                    if (currentRules.length == 3 && (currentRules[1] instanceof AnnualTimeZoneRule) && (currentRules[2] instanceof AnnualTimeZoneRule)) {
                        DateTimeRule start;
                        DateTimeRule end;
                        int sav;
                        AnnualTimeZoneRule r1 = currentRules[1];
                        AnnualTimeZoneRule r2 = currentRules[2];
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
                        simpleTimeZone.setStartRule(start.getRuleMonth(), start.getRuleWeekInMonth(), start.getRuleDayOfWeek(), start.getRuleMillisInDay());
                        simpleTimeZone.setEndRule(end.getRuleMonth(), end.getRuleWeekInMonth(), end.getRuleDayOfWeek(), end.getRuleMillisInDay());
                        simpleTimeZone.setDSTSavings(sav);
                    } else {
                        simpleTimeZone.setStartRule(0, 1, 0);
                        simpleTimeZone.setEndRule(11, 31, 86399999);
                    }
                }
                int[] fields = Grego.timeToFields(current, null);
                this.finalStartYear = fields[0];
                this.finalStartMillis = (double) Grego.fieldsToDay(fields[0], 0, 1);
                if (bDst) {
                    simpleTimeZone.setStartYear(this.finalStartYear);
                }
                this.finalZone = simpleTimeZone;
            } else {
                this.finalZone.setRawOffset(offsetMillis);
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
        return this.finalZone != null ? this.finalZone.useDaylightTime() : false;
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
        if (temp[1] != 0) {
            return true;
        }
        return false;
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
        } else if (!(o.finalZone != null && this.finalStartYear == o.finalStartYear && (this.finalZone.hasSameRules(o.finalZone) ^ 1) == 0)) {
            return false;
        }
        return this.transitionCount == o.transitionCount && (Arrays.equals(this.transitionTimes64, o.transitionTimes64) ^ 1) == 0 && this.typeCount == o.typeCount && (Arrays.equals(this.typeMapData, o.typeMapData) ^ 1) == 0 && (Arrays.equals(this.typeOffsets, o.typeOffsets) ^ 1) == 0;
    }

    public String getCanonicalID() {
        if (this.canonicalID == null) {
            synchronized (this) {
                if (this.canonicalID == null) {
                    this.canonicalID = TimeZone.getCanonicalID(getID());
                    if (!-assertionsDisabled && this.canonicalID == null) {
                        throw new AssertionError();
                    } else if (this.canonicalID == null) {
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
        if (top == null || res == null) {
            throw new IllegalArgumentException();
        }
        if (DEBUG) {
            System.out.println("OlsonTimeZone(" + res.getKey() + ")");
        }
        int[] transPost32 = null;
        int[] iArr = null;
        int[] transPre32 = null;
        this.transitionCount = 0;
        try {
            transPre32 = res.get("transPre32").getIntVector();
            if (transPre32.length % 2 != 0) {
                throw new IllegalArgumentException("Invalid Format");
            }
            this.transitionCount += transPre32.length / 2;
            try {
                iArr = res.get("trans").getIntVector();
                this.transitionCount += iArr.length;
            } catch (MissingResourceException e) {
            }
            try {
                transPost32 = res.get("transPost32").getIntVector();
                if (transPost32.length % 2 != 0) {
                    throw new IllegalArgumentException("Invalid Format");
                }
                this.transitionCount += transPost32.length / 2;
                if (this.transitionCount > 0) {
                    int i;
                    this.transitionTimes64 = new long[this.transitionCount];
                    int idx = 0;
                    if (transPre32 != null) {
                        i = 0;
                        while (i < transPre32.length / 2) {
                            this.transitionTimes64[idx] = ((((long) transPre32[i * 2]) & 4294967295L) << 32) | (((long) transPre32[(i * 2) + 1]) & 4294967295L);
                            i++;
                            idx++;
                        }
                    }
                    if (iArr != null) {
                        i = 0;
                        while (i < iArr.length) {
                            this.transitionTimes64[idx] = (long) iArr[i];
                            i++;
                            idx++;
                        }
                    }
                    if (transPost32 != null) {
                        i = 0;
                        while (i < transPost32.length / 2) {
                            this.transitionTimes64[idx] = ((((long) transPost32[i * 2]) & 4294967295L) << 32) | (((long) transPost32[(i * 2) + 1]) & 4294967295L);
                            i++;
                            idx++;
                        }
                    }
                } else {
                    this.transitionTimes64 = null;
                }
                this.typeOffsets = res.get("typeOffsets").getIntVector();
                if (this.typeOffsets.length < 2 || this.typeOffsets.length > Normalizer2Impl.COMP_1_TRAIL_MASK || this.typeOffsets.length % 2 != 0) {
                    throw new IllegalArgumentException("Invalid Format");
                }
                this.typeCount = this.typeOffsets.length / 2;
                if (this.transitionCount > 0) {
                    this.typeMapData = res.get("typeMap").getBinary(null);
                    if (this.typeMapData == null || this.typeMapData.length != this.transitionCount) {
                        throw new IllegalArgumentException("Invalid Format");
                    }
                }
                this.typeMapData = null;
                this.finalZone = null;
                this.finalStartYear = Integer.MAX_VALUE;
                this.finalStartMillis = Double.MAX_VALUE;
                String ruleID = null;
                try {
                    ruleID = res.getString("finalRule");
                    int ruleRaw = res.get("finalRaw").getInt() * 1000;
                    int[] ruleData = loadRule(top, ruleID).getIntVector();
                    if (ruleData == null || ruleData.length != 11) {
                        throw new IllegalArgumentException("Invalid Format");
                    }
                    this.finalZone = new SimpleTimeZone(ruleRaw, "", ruleData[0], ruleData[1], ruleData[2], ruleData[3] * 1000, ruleData[4], ruleData[5], ruleData[6], ruleData[7], ruleData[8] * 1000, ruleData[9], ruleData[10] * 1000);
                    this.finalStartYear = res.get("finalYear").getInt();
                    this.finalStartMillis = (double) (Grego.fieldsToDay(this.finalStartYear, 0, 1) * 86400000);
                } catch (MissingResourceException e2) {
                    if (ruleID != null) {
                        throw new IllegalArgumentException("Invalid Format");
                    }
                }
            } catch (MissingResourceException e3) {
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
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen OlsonTimeZone instance.");
        }
        if (this.canonicalID == null) {
            this.canonicalID = TimeZone.getCanonicalID(getID());
            if (!-assertionsDisabled && this.canonicalID == null) {
                throw new AssertionError();
            } else if (this.canonicalID == null) {
                this.canonicalID = getID();
            }
        }
        if (this.finalZone != null) {
            this.finalZone.setID(id);
        }
        super.setID(id);
        this.transitionRulesInitialized = false;
    }

    private void getHistoricalOffset(long date, boolean local, int NonExistingTimeOpt, int DuplicatedTimeOpt, int[] offsets) {
        if (this.transitionCount != 0) {
            long sec = Grego.floorDivide(date, 1000);
            if (local || sec >= this.transitionTimes64[0]) {
                int transIdx = this.transitionCount - 1;
                while (transIdx >= 0) {
                    long transition = this.transitionTimes64[transIdx];
                    if (local && sec >= transition - 86400) {
                        int offsetBefore = zoneOffsetAt(transIdx - 1);
                        boolean dstBefore = dstOffsetAt(transIdx + -1) != 0;
                        int offsetAfter = zoneOffsetAt(transIdx);
                        boolean dstAfter = dstOffsetAt(transIdx) != 0;
                        int dstToStd = dstBefore ? dstAfter ^ 1 : 0;
                        boolean stdToDst = !dstBefore ? dstAfter : false;
                        transition = offsetAfter - offsetBefore >= 0 ? (((NonExistingTimeOpt & 3) != 1 || dstToStd == 0) && !((NonExistingTimeOpt & 3) == 3 && stdToDst)) ? (!((NonExistingTimeOpt & 3) == 1 && stdToDst) && ((NonExistingTimeOpt & 3) != 3 || dstToStd == 0)) ? (NonExistingTimeOpt & 12) == 12 ? transition + ((long) offsetBefore) : transition + ((long) offsetAfter) : transition + ((long) offsetAfter) : transition + ((long) offsetBefore) : (((DuplicatedTimeOpt & 3) != 1 || dstToStd == 0) && !((DuplicatedTimeOpt & 3) == 3 && stdToDst)) ? (!((DuplicatedTimeOpt & 3) == 1 && stdToDst) && ((DuplicatedTimeOpt & 3) != 3 || dstToStd == 0)) ? (DuplicatedTimeOpt & 12) == 4 ? transition + ((long) offsetBefore) : transition + ((long) offsetAfter) : transition + ((long) offsetBefore) : transition + ((long) offsetAfter);
                    }
                    if (sec >= transition) {
                        break;
                    }
                    transIdx--;
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
        return val & 255;
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
        int i;
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append('[');
        buf.append("transitionCount=").append(this.transitionCount);
        buf.append(",typeCount=").append(this.typeCount);
        buf.append(",transitionTimes=");
        if (this.transitionTimes64 != null) {
            buf.append('[');
            for (i = 0; i < this.transitionTimes64.length; i++) {
                if (i > 0) {
                    buf.append(',');
                }
                buf.append(Long.toString(this.transitionTimes64[i]));
            }
            buf.append(']');
        } else {
            buf.append("null");
        }
        buf.append(",typeOffsets=");
        if (this.typeOffsets != null) {
            buf.append('[');
            for (i = 0; i < this.typeOffsets.length; i++) {
                if (i > 0) {
                    buf.append(',');
                }
                buf.append(Integer.toString(this.typeOffsets[i]));
            }
            buf.append(']');
        } else {
            buf.append("null");
        }
        buf.append(",typeMapData=");
        if (this.typeMapData != null) {
            buf.append('[');
            for (i = 0; i < this.typeMapData.length; i++) {
                if (i > 0) {
                    buf.append(',');
                }
                buf.append(Byte.toString(this.typeMapData[i]));
            }
        } else {
            buf.append("null");
        }
        buf.append(",finalStartYear=").append(this.finalStartYear);
        buf.append(",finalStartMillis=").append(this.finalStartMillis);
        buf.append(",finalZone=").append(this.finalZone);
        buf.append(']');
        return buf.toString();
    }

    private static UResourceBundle loadRule(UResourceBundle top, String ruleid) {
        return top.get("Rules").get(ruleid);
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (!super.equals(obj)) {
            return false;
        }
        OlsonTimeZone z2 = (OlsonTimeZone) obj;
        if (!Utility.arrayEquals(this.typeMapData, z2.typeMapData)) {
            if (this.finalStartYear != z2.finalStartYear) {
                z = false;
            } else if (!(this.finalZone == null && z2.finalZone == null)) {
                z = (this.finalZone != null && z2.finalZone != null && this.finalZone.equals(z2.finalZone) && this.transitionCount == z2.transitionCount && this.typeCount == z2.typeCount && Utility.arrayEquals(this.transitionTimes64, z2.transitionTimes64) && Utility.arrayEquals(this.typeOffsets, z2.typeOffsets)) ? Utility.arrayEquals(this.typeMapData, z2.typeMapData) : false;
            }
        }
        return z;
    }

    public int hashCode() {
        int i;
        int ret = (int) (((((long) (this.finalZone == null ? 0 : this.finalZone.hashCode())) + (Double.doubleToLongBits(this.finalStartMillis) + ((long) (this.typeCount >>> 8)))) + ((long) super.hashCode())) ^ ((long) ((this.finalStartYear ^ ((this.finalStartYear >>> 4) + this.transitionCount)) ^ ((this.transitionCount >>> 6) + this.typeCount))));
        if (this.transitionTimes64 != null) {
            for (i = 0; i < this.transitionTimes64.length; i++) {
                ret = (int) (((long) ret) + (this.transitionTimes64[i] ^ (this.transitionTimes64[i] >>> 8)));
            }
        }
        for (i = 0; i < this.typeOffsets.length; i++) {
            ret += this.typeOffsets[i] ^ (this.typeOffsets[i] >>> 8);
        }
        if (this.typeMapData != null) {
            for (byte b : this.typeMapData) {
                ret += b & 255;
            }
        }
        return ret;
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
        int ttidx = this.transitionCount - 1;
        while (ttidx >= this.firstTZTransitionIdx) {
            long t = this.transitionTimes64[ttidx] * 1000;
            if (base > t || (!inclusive && base == t)) {
                break;
            }
            ttidx--;
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
        int ttidx = this.transitionCount - 1;
        while (ttidx >= this.firstTZTransitionIdx) {
            long t = this.transitionTimes64[ttidx] * 1000;
            if (base > t || (inclusive && base == t)) {
                break;
            }
            ttidx--;
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
        int i;
        int idx;
        initTransitionRules();
        int size = 1;
        if (this.historicRules != null) {
            for (TimeArrayTimeZoneRule timeArrayTimeZoneRule : this.historicRules) {
                if (timeArrayTimeZoneRule != null) {
                    size++;
                }
            }
        }
        if (this.finalZone != null) {
            if (this.finalZone.useDaylightTime()) {
                size += 2;
            } else {
                size++;
            }
        }
        TimeZoneRule[] rules = new TimeZoneRule[size];
        int idx2 = 1;
        rules[0] = this.initialRule;
        if (this.historicRules != null) {
            for (i = 0; i < this.historicRules.length; i++) {
                if (this.historicRules[i] != null) {
                    idx = idx2 + 1;
                    rules[idx2] = this.historicRules[i];
                    idx2 = idx;
                }
            }
        }
        if (this.finalZone != null) {
            if (this.finalZone.useDaylightTime()) {
                TimeZoneRule[] stzr = this.finalZoneWithStartYear.getTimeZoneRules();
                idx = idx2 + 1;
                rules[idx2] = stzr[1];
                idx2 = idx + 1;
                rules[idx] = stzr[2];
            } else {
                idx = idx2 + 1;
                rules[idx2] = new TimeArrayTimeZoneRule(getID() + "(STD)", this.finalZone.getRawOffset(), 0, new long[]{(long) this.finalStartMillis}, 2);
                idx2 = idx;
            }
        }
        return rules;
    }

    private synchronized void initTransitionRules() {
        if (!this.transitionRulesInitialized) {
            this.initialRule = null;
            this.firstTZTransition = null;
            this.firstFinalTZTransition = null;
            this.historicRules = null;
            this.firstTZTransitionIdx = 0;
            this.finalZoneWithStartYear = null;
            String stdName = getID() + "(STD)";
            String dstName = getID() + "(DST)";
            int dst = initialDstOffset() * 1000;
            this.initialRule = new InitialTimeZoneRule(dst == 0 ? stdName : dstName, initialRawOffset() * 1000, dst);
            if (this.transitionCount > 0) {
                int transitionIdx = 0;
                while (transitionIdx < this.transitionCount) {
                    if (getInt(this.typeMapData[transitionIdx]) != 0) {
                        break;
                    }
                    this.firstTZTransitionIdx++;
                    transitionIdx++;
                }
                if (transitionIdx != this.transitionCount) {
                    long[] times = new long[this.transitionCount];
                    for (int typeIdx = 0; typeIdx < this.typeCount; typeIdx++) {
                        int nTimes;
                        int nTimes2 = 0;
                        transitionIdx = this.firstTZTransitionIdx;
                        while (true) {
                            nTimes = nTimes2;
                            if (transitionIdx >= this.transitionCount) {
                                break;
                            }
                            if (typeIdx == getInt(this.typeMapData[transitionIdx])) {
                                long tt = this.transitionTimes64[transitionIdx] * 1000;
                                if (((double) tt) < this.finalStartMillis) {
                                    nTimes2 = nTimes + 1;
                                    times[nTimes] = tt;
                                    transitionIdx++;
                                }
                            }
                            nTimes2 = nTimes;
                            transitionIdx++;
                        }
                        if (nTimes > 0) {
                            String str;
                            long[] startTimes = new long[nTimes];
                            System.arraycopy(times, 0, startTimes, 0, nTimes);
                            int raw = this.typeOffsets[typeIdx * 2] * 1000;
                            dst = this.typeOffsets[(typeIdx * 2) + 1] * 1000;
                            if (this.historicRules == null) {
                                this.historicRules = new TimeArrayTimeZoneRule[this.typeCount];
                            }
                            TimeArrayTimeZoneRule[] timeArrayTimeZoneRuleArr = this.historicRules;
                            if (dst == 0) {
                                str = stdName;
                            } else {
                                str = dstName;
                            }
                            timeArrayTimeZoneRuleArr[typeIdx] = new TimeArrayTimeZoneRule(str, raw, dst, startTimes, 2);
                        }
                    }
                    this.firstTZTransition = new TimeZoneTransition(this.transitionTimes64[this.firstTZTransitionIdx] * 1000, this.initialRule, this.historicRules[getInt(this.typeMapData[this.firstTZTransitionIdx])]);
                }
            }
            if (this.finalZone != null) {
                TimeZoneRule firstFinalRule;
                long startTime = (long) this.finalStartMillis;
                if (this.finalZone.useDaylightTime()) {
                    this.finalZoneWithStartYear = (SimpleTimeZone) this.finalZone.clone();
                    this.finalZoneWithStartYear.setStartYear(this.finalStartYear);
                    TimeZoneTransition tzt = this.finalZoneWithStartYear.getNextTransition(startTime, false);
                    firstFinalRule = tzt.getTo();
                    startTime = tzt.getTime();
                } else {
                    this.finalZoneWithStartYear = this.finalZone;
                    firstFinalRule = new TimeArrayTimeZoneRule(this.finalZone.getID(), this.finalZone.getRawOffset(), 0, new long[]{startTime}, 2);
                }
                TimeZoneRule prevRule = null;
                if (this.transitionCount > 0) {
                    prevRule = this.historicRules[getInt(this.typeMapData[this.transitionCount - 1])];
                }
                if (prevRule == null) {
                    prevRule = this.initialRule;
                }
                this.firstFinalTZTransition = new TimeZoneTransition(startTime, prevRule, firstFinalRule);
            }
            this.transitionRulesInitialized = true;
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
