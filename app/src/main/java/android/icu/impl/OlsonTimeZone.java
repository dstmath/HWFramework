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
import dalvik.bytecode.Opcodes;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.MissingResourceException;
import libcore.icu.RelativeDateTimeFormatter;
import org.xmlpull.v1.XmlPullParser;

public class OlsonTimeZone extends BasicTimeZone {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final boolean DEBUG = false;
    private static final int MAX_OFFSET_SECONDS = 86400;
    private static final int SECONDS_PER_DAY = 86400;
    private static final String ZONEINFORES = "zoneinfo64";
    private static final int currentSerialVersion = 1;
    static final long serialVersionUID = -6281977362477515376L;
    private volatile String canonicalID;
    private double finalStartMillis;
    private int finalStartYear;
    private SimpleTimeZone finalZone;
    private transient SimpleTimeZone finalZoneWithStartYear;
    private transient TimeZoneTransition firstFinalTZTransition;
    private transient TimeZoneTransition firstTZTransition;
    private transient int firstTZTransitionIdx;
    private transient TimeArrayTimeZoneRule[] historicRules;
    private transient InitialTimeZoneRule initialRule;
    private volatile transient boolean isFrozen;
    private int serialVersionOnStream;
    private int transitionCount;
    private transient boolean transitionRulesInitialized;
    private long[] transitionTimes64;
    private int typeCount;
    private byte[] typeMapData;
    private int[] typeOffsets;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.OlsonTimeZone.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.OlsonTimeZone.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.OlsonTimeZone.<clinit>():void");
    }

    public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Month is not in the legal range: " + month);
        }
        return getOffset(era, year, month, day, dayOfWeek, milliseconds, Grego.monthLength(year, month));
    }

    public int getOffset(int era, int year, int month, int dom, int dow, int millis, int monthLength) {
        if ((era == currentSerialVersion || era == 0) && month >= 0 && month <= 11 && dom >= currentSerialVersion && dom <= monthLength && dow >= currentSerialVersion && dow <= 7 && millis >= 0 && millis < Grego.MILLIS_PER_DAY && monthLength >= 28 && monthLength <= 31) {
            if (era == 0) {
                year = -year;
            }
            if (this.finalZone != null && year >= this.finalStartYear) {
                return this.finalZone.getOffset(era, year, month, dom, dow, millis);
            }
            int[] offsets = new int[2];
            getHistoricalOffset((Grego.fieldsToDay(year, month, dom) * RelativeDateTimeFormatter.DAY_IN_MILLIS) + ((long) millis), true, 3, currentSerialVersion, offsets);
            return offsets[0] + offsets[currentSerialVersion];
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
                    int length = currentRules.length;
                    if (r0 != 3) {
                        TimeZoneTransition tzt = getPreviousTransition(current, DEBUG);
                        if (tzt != null) {
                            currentRules = getSimpleTimeZoneRulesNear(tzt.getTime() - 1);
                        }
                    }
                    length = currentRules.length;
                    if (r0 == 3) {
                        if (currentRules[currentSerialVersion] instanceof AnnualTimeZoneRule) {
                            if (currentRules[2] instanceof AnnualTimeZoneRule) {
                                DateTimeRule start;
                                DateTimeRule end;
                                int sav;
                                AnnualTimeZoneRule r1 = currentRules[currentSerialVersion];
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
                            }
                        }
                    }
                    simpleTimeZone.setStartRule(0, currentSerialVersion, 0);
                    simpleTimeZone.setEndRule(11, 31, 86399999);
                }
                int[] fields = Grego.timeToFields(current, null);
                this.finalStartYear = fields[0];
                this.finalStartMillis = (double) Grego.fieldsToDay(fields[0], 0, currentSerialVersion);
                if (bDst) {
                    simpleTimeZone.setStartYear(this.finalStartYear);
                }
                this.finalZone = simpleTimeZone;
            } else {
                this.finalZone.setRawOffset(offsetMillis);
            }
            this.transitionRulesInitialized = DEBUG;
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
        getOffset(System.currentTimeMillis(), DEBUG, ret);
        return ret[0];
    }

    public boolean useDaylightTime() {
        long current = System.currentTimeMillis();
        if (this.finalZone == null || ((double) current) < this.finalStartMillis) {
            int[] fields = Grego.timeToFields(current, null);
            long start = Grego.fieldsToDay(fields[0], 0, currentSerialVersion) * 86400;
            long limit = Grego.fieldsToDay(fields[0] + currentSerialVersion, 0, currentSerialVersion) * 86400;
            int i = 0;
            while (i < this.transitionCount && this.transitionTimes64[i] < limit) {
                if ((this.transitionTimes64[i] >= start && dstOffsetAt(i) != 0) || (this.transitionTimes64[i] > start && i > 0 && dstOffsetAt(i - 1) != 0)) {
                    return true;
                }
                i += currentSerialVersion;
            }
            return DEBUG;
        }
        return this.finalZone != null ? this.finalZone.useDaylightTime() : DEBUG;
    }

    public boolean observesDaylightTime() {
        long current = System.currentTimeMillis();
        if (this.finalZone != null) {
            if (this.finalZone.useDaylightTime()) {
                return true;
            }
            if (((double) current) >= this.finalStartMillis) {
                return DEBUG;
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
        return DEBUG;
    }

    public int getDSTSavings() {
        if (this.finalZone != null) {
            return this.finalZone.getDSTSavings();
        }
        return super.getDSTSavings();
    }

    public boolean inDaylightTime(Date date) {
        int[] temp = new int[2];
        getOffset(date.getTime(), DEBUG, temp);
        if (temp[currentSerialVersion] != 0) {
            return true;
        }
        return DEBUG;
    }

    public boolean hasSameRules(TimeZone other) {
        if (this == other) {
            return true;
        }
        if (!super.hasSameRules(other) || !(other instanceof OlsonTimeZone)) {
            return DEBUG;
        }
        OlsonTimeZone o = (OlsonTimeZone) other;
        if (this.finalZone == null) {
            if (o.finalZone != null) {
                return DEBUG;
            }
        } else if (!(o.finalZone != null && this.finalStartYear == o.finalStartYear && this.finalZone.hasSameRules(o.finalZone))) {
            return DEBUG;
        }
        return (this.transitionCount == o.transitionCount && Arrays.equals(this.transitionTimes64, o.transitionTimes64) && this.typeCount == o.typeCount && Arrays.equals(this.typeMapData, o.typeMapData) && Arrays.equals(this.typeOffsets, o.typeOffsets)) ? true : DEBUG;
    }

    public String getCanonicalID() {
        if (this.canonicalID == null) {
            synchronized (this) {
                if (this.canonicalID == null) {
                    this.canonicalID = TimeZone.getCanonicalID(getID());
                    if (!-assertionsDisabled) {
                        if ((this.canonicalID != null ? currentSerialVersion : null) == null) {
                            throw new AssertionError();
                        }
                    }
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
        this.typeCount = currentSerialVersion;
        this.typeOffsets = new int[]{0, 0};
        this.finalZone = null;
        this.finalStartYear = AnnualTimeZoneRule.MAX_YEAR;
        this.finalStartMillis = Double.MAX_VALUE;
        this.transitionRulesInitialized = DEBUG;
    }

    public OlsonTimeZone(UResourceBundle top, UResourceBundle res, String id) {
        super(id);
        this.finalStartYear = AnnualTimeZoneRule.MAX_YEAR;
        this.finalStartMillis = Double.MAX_VALUE;
        this.finalZone = null;
        this.canonicalID = null;
        this.serialVersionOnStream = currentSerialVersion;
        this.isFrozen = DEBUG;
        construct(top, res);
    }

    private void construct(UResourceBundle top, UResourceBundle res) {
        if (top == null || res == null) {
            throw new IllegalArgumentException();
        }
        if (DEBUG) {
            System.out.println("OlsonTimeZone(" + res.getKey() + ")");
        }
        int[] iArr = null;
        int[] iArr2 = null;
        int[] iArr3 = null;
        this.transitionCount = 0;
        try {
            iArr3 = res.get("transPre32").getIntVector();
            if (iArr3.length % 2 != 0) {
                throw new IllegalArgumentException("Invalid Format");
            }
            this.transitionCount += iArr3.length / 2;
            try {
                iArr2 = res.get("trans").getIntVector();
                this.transitionCount += iArr2.length;
            } catch (MissingResourceException e) {
            }
            try {
                iArr = res.get("transPost32").getIntVector();
                if (iArr.length % 2 != 0) {
                    throw new IllegalArgumentException("Invalid Format");
                }
                this.transitionCount += iArr.length / 2;
                if (this.transitionCount > 0) {
                    int i;
                    this.transitionTimes64 = new long[this.transitionCount];
                    int i2 = 0;
                    if (iArr3 != null) {
                        i = 0;
                        while (i < iArr3.length / 2) {
                            this.transitionTimes64[i2] = ((((long) iArr3[i * 2]) & 4294967295L) << 32) | (((long) iArr3[(i * 2) + currentSerialVersion]) & 4294967295L);
                            i += currentSerialVersion;
                            i2 += currentSerialVersion;
                        }
                    }
                    if (iArr2 != null) {
                        i = 0;
                        while (i < iArr2.length) {
                            this.transitionTimes64[i2] = (long) iArr2[i];
                            i += currentSerialVersion;
                            i2 += currentSerialVersion;
                        }
                    }
                    if (iArr != null) {
                        i = 0;
                        while (i < iArr.length / 2) {
                            this.transitionTimes64[i2] = ((((long) iArr[i * 2]) & 4294967295L) << 32) | (((long) iArr[(i * 2) + currentSerialVersion]) & 4294967295L);
                            i += currentSerialVersion;
                            i2 += currentSerialVersion;
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
                    if (this.typeMapData.length != this.transitionCount) {
                        throw new IllegalArgumentException("Invalid Format");
                    }
                }
                this.typeMapData = null;
                this.finalZone = null;
                this.finalStartYear = AnnualTimeZoneRule.MAX_YEAR;
                this.finalStartMillis = Double.MAX_VALUE;
                String str = null;
                try {
                    str = res.getString("finalRule");
                    int ruleRaw = res.get("finalRaw").getInt() * Grego.MILLIS_PER_SECOND;
                    int[] ruleData = loadRule(top, str).getIntVector();
                    if (ruleData == null || ruleData.length != 11) {
                        throw new IllegalArgumentException("Invalid Format");
                    }
                    this.finalZone = new SimpleTimeZone(ruleRaw, XmlPullParser.NO_NAMESPACE, ruleData[0], ruleData[currentSerialVersion], ruleData[2], ruleData[3] * Grego.MILLIS_PER_SECOND, ruleData[4], ruleData[5], ruleData[6], ruleData[7], ruleData[8] * Grego.MILLIS_PER_SECOND, ruleData[9], ruleData[10] * Grego.MILLIS_PER_SECOND);
                    this.finalStartYear = res.get("finalYear").getInt();
                    this.finalStartMillis = (double) (Grego.fieldsToDay(this.finalStartYear, 0, currentSerialVersion) * RelativeDateTimeFormatter.DAY_IN_MILLIS);
                } catch (MissingResourceException e2) {
                    if (str != null) {
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
        this.finalStartYear = AnnualTimeZoneRule.MAX_YEAR;
        this.finalStartMillis = Double.MAX_VALUE;
        this.finalZone = null;
        this.canonicalID = null;
        this.serialVersionOnStream = currentSerialVersion;
        this.isFrozen = DEBUG;
        UResourceBundle top = UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, ZONEINFORES, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
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
            if (!-assertionsDisabled) {
                if (!(this.canonicalID != null ? true : DEBUG)) {
                    throw new AssertionError();
                }
            }
            if (this.canonicalID == null) {
                this.canonicalID = getID();
            }
        }
        if (this.finalZone != null) {
            this.finalZone.setID(id);
        }
        super.setID(id);
        this.transitionRulesInitialized = DEBUG;
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
                        boolean dstBefore = dstOffsetAt(transIdx + -1) != 0 ? true : DEBUG;
                        int offsetAfter = zoneOffsetAt(transIdx);
                        boolean dstAfter = dstOffsetAt(transIdx) != 0 ? true : DEBUG;
                        boolean dstToStd = (!dstBefore || dstAfter) ? DEBUG : true;
                        boolean z = !dstBefore ? dstAfter : DEBUG;
                        transition = offsetAfter - offsetBefore >= 0 ? (((NonExistingTimeOpt & 3) == currentSerialVersion && dstToStd) || ((NonExistingTimeOpt & 3) == 3 && z)) ? transition + ((long) offsetBefore) : (((NonExistingTimeOpt & 3) == currentSerialVersion && z) || ((NonExistingTimeOpt & 3) == 3 && dstToStd)) ? transition + ((long) offsetAfter) : (NonExistingTimeOpt & 12) == 12 ? transition + ((long) offsetBefore) : transition + ((long) offsetAfter) : (((DuplicatedTimeOpt & 3) == currentSerialVersion && dstToStd) || ((DuplicatedTimeOpt & 3) == 3 && z)) ? transition + ((long) offsetAfter) : (((DuplicatedTimeOpt & 3) == currentSerialVersion && z) || ((DuplicatedTimeOpt & 3) == 3 && dstToStd)) ? transition + ((long) offsetBefore) : (DuplicatedTimeOpt & 12) == 4 ? transition + ((long) offsetBefore) : transition + ((long) offsetAfter);
                    }
                    if (sec >= transition) {
                        break;
                    }
                    transIdx--;
                }
                offsets[0] = rawOffsetAt(transIdx) * Grego.MILLIS_PER_SECOND;
                offsets[currentSerialVersion] = dstOffsetAt(transIdx) * Grego.MILLIS_PER_SECOND;
                return;
            }
            offsets[0] = initialRawOffset() * Grego.MILLIS_PER_SECOND;
            offsets[currentSerialVersion] = initialDstOffset() * Grego.MILLIS_PER_SECOND;
            return;
        }
        offsets[0] = initialRawOffset() * Grego.MILLIS_PER_SECOND;
        offsets[currentSerialVersion] = initialDstOffset() * Grego.MILLIS_PER_SECOND;
    }

    private int getInt(byte val) {
        return val & Opcodes.OP_CONST_CLASS_JUMBO;
    }

    private int zoneOffsetAt(int transIdx) {
        int typeIdx = transIdx >= 0 ? getInt(this.typeMapData[transIdx]) * 2 : 0;
        return this.typeOffsets[typeIdx] + this.typeOffsets[typeIdx + currentSerialVersion];
    }

    private int rawOffsetAt(int transIdx) {
        return this.typeOffsets[transIdx >= 0 ? getInt(this.typeMapData[transIdx]) * 2 : 0];
    }

    private int dstOffsetAt(int transIdx) {
        return this.typeOffsets[(transIdx >= 0 ? getInt(this.typeMapData[transIdx]) * 2 : 0) + currentSerialVersion];
    }

    private int initialRawOffset() {
        return this.typeOffsets[0];
    }

    private int initialDstOffset() {
        return this.typeOffsets[currentSerialVersion];
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
            for (i = 0; i < this.transitionTimes64.length; i += currentSerialVersion) {
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
            for (i = 0; i < this.typeOffsets.length; i += currentSerialVersion) {
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
            for (i = 0; i < this.typeMapData.length; i += currentSerialVersion) {
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
            return DEBUG;
        }
        OlsonTimeZone z2 = (OlsonTimeZone) obj;
        if (!Utility.arrayEquals(this.typeMapData, z2.typeMapData)) {
            if (this.finalStartYear != z2.finalStartYear) {
                z = DEBUG;
            } else if (!(this.finalZone == null && z2.finalZone == null)) {
                z = (this.finalZone != null && z2.finalZone != null && this.finalZone.equals(z2.finalZone) && this.transitionCount == z2.transitionCount && this.typeCount == z2.typeCount && Utility.arrayEquals(this.transitionTimes64, z2.transitionTimes64) && Utility.arrayEquals(this.typeOffsets, z2.typeOffsets)) ? Utility.arrayEquals(this.typeMapData, z2.typeMapData) : DEBUG;
            }
        }
        return z;
    }

    public int hashCode() {
        int i;
        int hashCode = (int) (((((long) (this.finalZone == null ? 0 : this.finalZone.hashCode())) + (Double.doubleToLongBits(this.finalStartMillis) + ((long) (this.typeCount >>> 8)))) + ((long) super.hashCode())) ^ ((long) ((this.finalStartYear ^ ((this.finalStartYear >>> 4) + this.transitionCount)) ^ ((this.transitionCount >>> 6) + this.typeCount))));
        if (this.transitionTimes64 != null) {
            for (i = 0; i < this.transitionTimes64.length; i += currentSerialVersion) {
                hashCode = (int) (((long) hashCode) + (this.transitionTimes64[i] ^ (this.transitionTimes64[i] >>> 8)));
            }
        }
        for (i = 0; i < this.typeOffsets.length; i += currentSerialVersion) {
            hashCode += this.typeOffsets[i] ^ (this.typeOffsets[i] >>> 8);
        }
        if (this.typeMapData != null) {
            for (i = 0; i < this.typeMapData.length; i += currentSerialVersion) {
                hashCode += this.typeMapData[i] & Opcodes.OP_CONST_CLASS_JUMBO;
            }
        }
        return hashCode;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
            if (base <= t && (inclusive || base != t)) {
                ttidx--;
            }
        }
        if (ttidx == this.transitionCount - 1) {
            return this.firstFinalTZTransition;
        }
        if (ttidx < this.firstTZTransitionIdx) {
            return this.firstTZTransition;
        }
        TimeZoneRule to = this.historicRules[getInt(this.typeMapData[ttidx + currentSerialVersion])];
        TimeZoneRule from = this.historicRules[getInt(this.typeMapData[ttidx])];
        long startTime = this.transitionTimes64[ttidx + currentSerialVersion] * 1000;
        if (from.getName().equals(to.getName()) && from.getRawOffset() == to.getRawOffset() && from.getDSTSavings() == to.getDSTSavings()) {
            return getNextTransition(startTime, DEBUG);
        }
        return new TimeZoneTransition(startTime, from, to);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
            if (base <= t && !(inclusive && base == t)) {
                ttidx--;
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
            return getPreviousTransition(startTime, DEBUG);
        }
        return new TimeZoneTransition(startTime, from, to);
    }

    public TimeZoneRule[] getTimeZoneRules() {
        int i;
        initTransitionRules();
        int size = currentSerialVersion;
        if (this.historicRules != null) {
            for (i = 0; i < this.historicRules.length; i += currentSerialVersion) {
                if (this.historicRules[i] != null) {
                    size += currentSerialVersion;
                }
            }
        }
        if (this.finalZone != null) {
            if (this.finalZone.useDaylightTime()) {
                size += 2;
            } else {
                size += currentSerialVersion;
            }
        }
        TimeZoneRule[] rules = new TimeZoneRule[size];
        int i2 = currentSerialVersion;
        rules[0] = this.initialRule;
        if (this.historicRules != null) {
            for (i = 0; i < this.historicRules.length; i += currentSerialVersion) {
                if (this.historicRules[i] != null) {
                    int idx = i2 + currentSerialVersion;
                    rules[i2] = this.historicRules[i];
                    i2 = idx;
                }
            }
        }
        if (this.finalZone != null) {
            if (this.finalZone.useDaylightTime()) {
                TimeZoneRule[] stzr = this.finalZoneWithStartYear.getTimeZoneRules();
                idx = i2 + currentSerialVersion;
                rules[i2] = stzr[currentSerialVersion];
                i2 = idx + currentSerialVersion;
                rules[idx] = stzr[2];
            } else {
                idx = i2 + currentSerialVersion;
                String str = getID() + "(STD)";
                int rawOffset = this.finalZone.getRawOffset();
                long[] jArr = new long[currentSerialVersion];
                jArr[0] = (long) this.finalStartMillis;
                rules[i2] = new TimeArrayTimeZoneRule(str, rawOffset, 0, jArr, 2);
                i2 = idx;
            }
        }
        return rules;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
            int dst = initialDstOffset() * Grego.MILLIS_PER_SECOND;
            this.initialRule = new InitialTimeZoneRule(dst == 0 ? stdName : dstName, initialRawOffset() * Grego.MILLIS_PER_SECOND, dst);
            if (this.transitionCount > 0) {
                int transitionIdx = 0;
                while (transitionIdx < this.transitionCount && getInt(this.typeMapData[transitionIdx]) == 0) {
                    this.firstTZTransitionIdx += currentSerialVersion;
                    transitionIdx += currentSerialVersion;
                }
                if (transitionIdx != this.transitionCount) {
                    long[] times = new long[this.transitionCount];
                    for (int typeIdx = 0; typeIdx < this.typeCount; typeIdx += currentSerialVersion) {
                        transitionIdx = this.firstTZTransitionIdx;
                        int nTimes = 0;
                        while (transitionIdx < this.transitionCount) {
                            int nTimes2;
                            if (typeIdx == getInt(this.typeMapData[transitionIdx])) {
                                long tt = this.transitionTimes64[transitionIdx] * 1000;
                                if (((double) tt) < this.finalStartMillis) {
                                    nTimes2 = nTimes + currentSerialVersion;
                                    times[nTimes] = tt;
                                    transitionIdx += currentSerialVersion;
                                    nTimes = nTimes2;
                                }
                            }
                            nTimes2 = nTimes;
                            transitionIdx += currentSerialVersion;
                            nTimes = nTimes2;
                        }
                        if (nTimes > 0) {
                            String str;
                            long[] startTimes = new long[nTimes];
                            System.arraycopy(times, 0, startTimes, 0, nTimes);
                            int raw = this.typeOffsets[typeIdx * 2] * Grego.MILLIS_PER_SECOND;
                            dst = this.typeOffsets[(typeIdx * 2) + currentSerialVersion] * Grego.MILLIS_PER_SECOND;
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
                    TimeZoneTransition tzt = this.finalZoneWithStartYear.getNextTransition(startTime, DEBUG);
                    firstFinalRule = tzt.getTo();
                    startTime = tzt.getTime();
                } else {
                    this.finalZoneWithStartYear = this.finalZone;
                    String id = this.finalZone.getID();
                    int rawOffset = this.finalZone.getRawOffset();
                    long[] jArr = new long[currentSerialVersion];
                    jArr[0] = startTime;
                    firstFinalRule = new TimeArrayTimeZoneRule(id, rawOffset, 0, jArr, 2);
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
        if (this.serialVersionOnStream < currentSerialVersion) {
            boolean initialized = DEBUG;
            String tzid = getID();
            if (tzid != null) {
                try {
                    UResourceBundle top = UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, ZONEINFORES, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
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
        this.transitionRulesInitialized = DEBUG;
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
        tz.isFrozen = DEBUG;
        return tz;
    }
}
