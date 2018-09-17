package libcore.util;

import android.icu.impl.Grego;
import android.icu.text.BreakIterator;
import android.icu.util.AnnualTimeZoneRule;
import dalvik.bytecode.Opcodes;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConstants;
import libcore.io.BufferIterator;
import org.w3c.dom.traversal.NodeFilter;

public final class ZoneInfo extends TimeZone {
    private static final int[] LEAP = null;
    private static final long MILLISECONDS_PER_400_YEARS = 12622780800000L;
    private static final long MILLISECONDS_PER_DAY = 86400000;
    private static final int[] NORMAL = null;
    private static final long UNIX_OFFSET = 62167219200000L;
    static final long serialVersionUID = -4598738130123921552L;
    private int mDstSavings;
    private final int mEarliestRawOffset;
    private final byte[] mIsDsts;
    private final int[] mOffsets;
    private int mRawOffset;
    private final long[] mTransitions;
    private final byte[] mTypes;
    private final boolean mUseDst;

    private static class CheckedArithmeticException extends Exception {
        private CheckedArithmeticException() {
        }
    }

    static class OffsetInterval {
        private final int endWallTimeSeconds;
        private final int isDst;
        private final int startWallTimeSeconds;
        private final int totalOffsetSeconds;

        public static OffsetInterval create(ZoneInfo timeZone, int transitionIndex) throws CheckedArithmeticException {
            if (transitionIndex < -1 || transitionIndex >= timeZone.mTransitions.length) {
                return null;
            }
            int rawOffsetSeconds = timeZone.mRawOffset / Grego.MILLIS_PER_SECOND;
            if (transitionIndex == -1) {
                return new OffsetInterval(DatatypeConstants.FIELD_UNDEFINED, ZoneInfo.checkedAdd(timeZone.mTransitions[0], rawOffsetSeconds), 0, rawOffsetSeconds);
            }
            int endWallTimeSeconds;
            int type = timeZone.mTypes[transitionIndex] & Opcodes.OP_CONST_CLASS_JUMBO;
            int totalOffsetSeconds = timeZone.mOffsets[type] + rawOffsetSeconds;
            if (transitionIndex == timeZone.mTransitions.length - 1) {
                endWallTimeSeconds = AnnualTimeZoneRule.MAX_YEAR;
            } else {
                endWallTimeSeconds = ZoneInfo.checkedAdd(timeZone.mTransitions[transitionIndex + 1], totalOffsetSeconds);
            }
            return new OffsetInterval(ZoneInfo.checkedAdd(timeZone.mTransitions[transitionIndex], totalOffsetSeconds), endWallTimeSeconds, timeZone.mIsDsts[type], totalOffsetSeconds);
        }

        private OffsetInterval(int startWallTimeSeconds, int endWallTimeSeconds, int isDst, int totalOffsetSeconds) {
            this.startWallTimeSeconds = startWallTimeSeconds;
            this.endWallTimeSeconds = endWallTimeSeconds;
            this.isDst = isDst;
            this.totalOffsetSeconds = totalOffsetSeconds;
        }

        public boolean containsWallTime(long wallTimeSeconds) {
            return wallTimeSeconds >= ((long) this.startWallTimeSeconds) && wallTimeSeconds < ((long) this.endWallTimeSeconds);
        }

        public int getIsDst() {
            return this.isDst;
        }

        public int getTotalOffsetSeconds() {
            return this.totalOffsetSeconds;
        }

        public long getEndWallTimeSeconds() {
            return (long) this.endWallTimeSeconds;
        }

        public long getStartWallTimeSeconds() {
            return (long) this.startWallTimeSeconds;
        }
    }

    public static class WallTime {
        private final GregorianCalendar calendar;
        private int gmtOffsetSeconds;
        private int hour;
        private int isDst;
        private int minute;
        private int month;
        private int monthDay;
        private int second;
        private int weekDay;
        private int year;
        private int yearDay;

        public WallTime() {
            this.calendar = new GregorianCalendar(0, 0, 0, 0, 0, 0);
            this.calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        public void localtime(int timeSeconds, ZoneInfo zoneInfo) {
            try {
                int i;
                int offsetSeconds = zoneInfo.mRawOffset / Grego.MILLIS_PER_SECOND;
                if (zoneInfo.mTransitions.length == 0) {
                    i = 0;
                } else {
                    int offsetIndex = zoneInfo.findOffsetIndexForTimeInSeconds((long) timeSeconds);
                    if (offsetIndex == -1) {
                        i = 0;
                    } else {
                        offsetSeconds += zoneInfo.mOffsets[offsetIndex];
                        i = zoneInfo.mIsDsts[offsetIndex];
                    }
                }
                this.calendar.setTimeInMillis(((long) ZoneInfo.checkedAdd((long) timeSeconds, offsetSeconds)) * 1000);
                copyFieldsFromCalendar();
                this.isDst = i;
                this.gmtOffsetSeconds = offsetSeconds;
            } catch (CheckedArithmeticException e) {
            }
        }

        public int mktime(ZoneInfo zoneInfo) {
            int i = 1;
            int i2 = -1;
            if (this.isDst > 0) {
                this.isDst = 1;
            } else if (this.isDst < 0) {
                this.isDst = -1;
                i = -1;
            } else {
                i = 0;
            }
            this.isDst = i;
            copyFieldsToCalendar();
            long longWallTimeSeconds = this.calendar.getTimeInMillis() / 1000;
            if (-2147483648L > longWallTimeSeconds || longWallTimeSeconds > 2147483647L) {
                return -1;
            }
            int wallTimeSeconds = (int) longWallTimeSeconds;
            try {
                int rawOffsetSeconds = zoneInfo.mRawOffset / Grego.MILLIS_PER_SECOND;
                int rawTimeSeconds = ZoneInfo.checkedSubtract(wallTimeSeconds, rawOffsetSeconds);
                if (zoneInfo.mTransitions.length != 0) {
                    int initialTransitionIndex = zoneInfo.findTransitionIndex((long) rawTimeSeconds);
                    Integer result;
                    if (this.isDst < 0) {
                        result = doWallTimeSearch(zoneInfo, initialTransitionIndex, wallTimeSeconds, true);
                        if (result != null) {
                            i2 = result.intValue();
                        }
                        return i2;
                    }
                    result = doWallTimeSearch(zoneInfo, initialTransitionIndex, wallTimeSeconds, true);
                    if (result == null) {
                        result = doWallTimeSearch(zoneInfo, initialTransitionIndex, wallTimeSeconds, false);
                    }
                    if (result == null) {
                        result = Integer.valueOf(-1);
                    }
                    return result.intValue();
                } else if (this.isDst > 0) {
                    return -1;
                } else {
                    copyFieldsFromCalendar();
                    this.isDst = 0;
                    this.gmtOffsetSeconds = rawOffsetSeconds;
                    return rawTimeSeconds;
                }
            } catch (CheckedArithmeticException e) {
                return -1;
            }
        }

        private Integer tryOffsetAdjustments(ZoneInfo zoneInfo, int oldWallTimeSeconds, OffsetInterval targetInterval, int transitionIndex, int isDstToFind) throws CheckedArithmeticException {
            int[] offsetsToTry = getOffsetsOfType(zoneInfo, transitionIndex, isDstToFind);
            for (int -get2 : offsetsToTry) {
                int jOffsetSeconds = (zoneInfo.mRawOffset / Grego.MILLIS_PER_SECOND) + -get2;
                int targetIntervalOffsetSeconds = targetInterval.getTotalOffsetSeconds();
                int adjustedWallTimeSeconds = ZoneInfo.checkedAdd((long) oldWallTimeSeconds, targetIntervalOffsetSeconds - jOffsetSeconds);
                if (targetInterval.containsWallTime((long) adjustedWallTimeSeconds)) {
                    int returnValue = ZoneInfo.checkedSubtract(adjustedWallTimeSeconds, targetIntervalOffsetSeconds);
                    this.calendar.setTimeInMillis(((long) adjustedWallTimeSeconds) * 1000);
                    copyFieldsFromCalendar();
                    this.isDst = targetInterval.getIsDst();
                    this.gmtOffsetSeconds = targetIntervalOffsetSeconds;
                    return Integer.valueOf(returnValue);
                }
            }
            return null;
        }

        private static int[] getOffsetsOfType(ZoneInfo zoneInfo, int startIndex, int isDst) {
            int[] offsets = new int[(zoneInfo.mOffsets.length + 1)];
            boolean[] seen = new boolean[zoneInfo.mOffsets.length];
            int delta = 0;
            boolean clampTop = false;
            boolean clampBottom = false;
            int numFound = 0;
            while (true) {
                int numFound2;
                boolean z;
                delta *= -1;
                if (delta >= 0) {
                    delta++;
                }
                int transitionIndex = startIndex + delta;
                if (delta < 0 && transitionIndex < -1) {
                    clampBottom = true;
                    numFound2 = numFound;
                } else if (delta <= 0 || transitionIndex < zoneInfo.mTypes.length) {
                    if (transitionIndex != -1) {
                        int type = zoneInfo.mTypes[transitionIndex] & Opcodes.OP_CONST_CLASS_JUMBO;
                        if (!seen[type]) {
                            if (zoneInfo.mIsDsts[type] == isDst) {
                                numFound2 = numFound + 1;
                                offsets[numFound] = zoneInfo.mOffsets[type];
                            } else {
                                numFound2 = numFound;
                            }
                            seen[type] = true;
                        }
                    } else if (isDst == 0) {
                        numFound2 = numFound + 1;
                        offsets[numFound] = 0;
                    }
                    numFound2 = numFound;
                } else {
                    clampTop = true;
                    numFound2 = numFound;
                }
                if (clampTop) {
                    z = clampBottom;
                } else {
                    z = false;
                }
                if (z) {
                    int[] toReturn = new int[numFound2];
                    System.arraycopy(offsets, 0, toReturn, 0, numFound2);
                    return toReturn;
                }
                numFound = numFound2;
            }
        }

        private Integer doWallTimeSearch(ZoneInfo zoneInfo, int initialTransitionIndex, int wallTimeSeconds, boolean mustMatchDst) throws CheckedArithmeticException {
            boolean clampTop = false;
            boolean clampBottom = false;
            int loop = 0;
            boolean z;
            do {
                int transitionIndexDelta = (loop + 1) / 2;
                if (loop % 2 == 1) {
                    transitionIndexDelta *= -1;
                }
                loop++;
                if ((transitionIndexDelta <= 0 || !clampTop) && (transitionIndexDelta >= 0 || !clampBottom)) {
                    int currentTransitionIndex = initialTransitionIndex + transitionIndexDelta;
                    OffsetInterval offsetInterval = OffsetInterval.create(zoneInfo, currentTransitionIndex);
                    if (offsetInterval == null) {
                        clampTop |= transitionIndexDelta > 0 ? 1 : 0;
                        clampBottom |= transitionIndexDelta < 0 ? 1 : 0;
                    } else {
                        if (mustMatchDst) {
                            if (offsetInterval.containsWallTime((long) wallTimeSeconds) && (this.isDst == -1 || offsetInterval.getIsDst() == this.isDst)) {
                                int totalOffsetSeconds = offsetInterval.getTotalOffsetSeconds();
                                int returnValue = ZoneInfo.checkedSubtract(wallTimeSeconds, totalOffsetSeconds);
                                copyFieldsFromCalendar();
                                this.isDst = offsetInterval.getIsDst();
                                this.gmtOffsetSeconds = totalOffsetSeconds;
                                return Integer.valueOf(returnValue);
                            }
                        } else if (this.isDst != offsetInterval.getIsDst()) {
                            Integer returnValue2 = tryOffsetAdjustments(zoneInfo, wallTimeSeconds, offsetInterval, currentTransitionIndex, this.isDst);
                            if (returnValue2 != null) {
                                return returnValue2;
                            }
                        }
                        if (transitionIndexDelta > 0) {
                            if (offsetInterval.getEndWallTimeSeconds() - ((long) wallTimeSeconds) > 86400) {
                                clampTop = true;
                            }
                        } else if (transitionIndexDelta < 0) {
                            if (((long) wallTimeSeconds) - offsetInterval.getStartWallTimeSeconds() >= 86400) {
                                clampBottom = true;
                            }
                        }
                    }
                }
                if (clampTop) {
                    z = clampBottom;
                    continue;
                } else {
                    z = false;
                    continue;
                }
            } while (!z);
            return null;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public void setMonthDay(int monthDay) {
            this.monthDay = monthDay;
        }

        public void setHour(int hour) {
            this.hour = hour;
        }

        public void setMinute(int minute) {
            this.minute = minute;
        }

        public void setSecond(int second) {
            this.second = second;
        }

        public void setWeekDay(int weekDay) {
            this.weekDay = weekDay;
        }

        public void setYearDay(int yearDay) {
            this.yearDay = yearDay;
        }

        public void setIsDst(int isDst) {
            this.isDst = isDst;
        }

        public void setGmtOffset(int gmtoff) {
            this.gmtOffsetSeconds = gmtoff;
        }

        public int getYear() {
            return this.year;
        }

        public int getMonth() {
            return this.month;
        }

        public int getMonthDay() {
            return this.monthDay;
        }

        public int getHour() {
            return this.hour;
        }

        public int getMinute() {
            return this.minute;
        }

        public int getSecond() {
            return this.second;
        }

        public int getWeekDay() {
            return this.weekDay;
        }

        public int getYearDay() {
            return this.yearDay;
        }

        public int getGmtOffset() {
            return this.gmtOffsetSeconds;
        }

        public int getIsDst() {
            return this.isDst;
        }

        private void copyFieldsToCalendar() {
            this.calendar.set(1, this.year);
            this.calendar.set(2, this.month);
            this.calendar.set(5, this.monthDay);
            this.calendar.set(11, this.hour);
            this.calendar.set(12, this.minute);
            this.calendar.set(13, this.second);
            this.calendar.set(14, 0);
        }

        private void copyFieldsFromCalendar() {
            this.year = this.calendar.get(1);
            this.month = this.calendar.get(2);
            this.monthDay = this.calendar.get(5);
            this.hour = this.calendar.get(11);
            this.minute = this.calendar.get(12);
            this.second = this.calendar.get(13);
            this.weekDay = this.calendar.get(7) - 1;
            this.yearDay = this.calendar.get(6) - 1;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: libcore.util.ZoneInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: libcore.util.ZoneInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: libcore.util.ZoneInfo.<clinit>():void");
    }

    public int findTransitionIndex(long r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: libcore.util.ZoneInfo.findTransitionIndex(long):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: libcore.util.ZoneInfo.findTransitionIndex(long):int");
    }

    public static ZoneInfo makeTimeZone(String id, BufferIterator it) {
        return makeTimeZone(id, it, System.currentTimeMillis());
    }

    public static ZoneInfo makeTimeZone(String id, BufferIterator it, long currentTimeMillis) {
        if (it.readInt() != 1415211366) {
            return null;
        }
        it.skip(28);
        int tzh_timecnt = it.readInt();
        int tzh_typecnt = it.readInt();
        if (tzh_typecnt > NodeFilter.SHOW_DOCUMENT) {
            throw new IllegalStateException(id + " has more than 256 different types");
        }
        int i;
        it.skip(4);
        int[] transitions32 = new int[tzh_timecnt];
        it.readIntArray(transitions32, 0, transitions32.length);
        long[] transitions64 = new long[tzh_timecnt];
        for (i = 0; i < tzh_timecnt; i++) {
            transitions64[i] = (long) transitions32[i];
        }
        byte[] type = new byte[tzh_timecnt];
        it.readByteArray(type, 0, type.length);
        int[] gmtOffsets = new int[tzh_typecnt];
        byte[] isDsts = new byte[tzh_typecnt];
        i = 0;
        while (i < tzh_typecnt) {
            gmtOffsets[i] = it.readInt();
            byte b = it.readByte();
            if (b == null || b == 1) {
                isDsts[i] = b;
                it.skip(1);
                i++;
            } else {
                throw new IllegalStateException(id + " dst at " + i + " is not 0 or 1, is " + b);
            }
        }
        return new ZoneInfo(id, transitions64, type, gmtOffsets, isDsts, currentTimeMillis);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ZoneInfo(String name, long[] transitions, byte[] types, int[] gmtOffsets, byte[] isDsts, long currentTimeMillis) {
        if (gmtOffsets.length == 0) {
            throw new IllegalStateException("ZoneInfo requires at least one offset to be provided for each timezone but could not find one for '" + name + "'");
        }
        this.mTransitions = transitions;
        this.mTypes = types;
        this.mIsDsts = isDsts;
        setID(name);
        int lastStd = -1;
        int lastDst = -1;
        int i = this.mTransitions.length - 1;
        while (true) {
            if ((lastStd == -1 || lastDst == -1) && i >= 0) {
                int type = this.mTypes[i] & Opcodes.OP_CONST_CLASS_JUMBO;
                if (lastStd == -1 && this.mIsDsts[type] == null) {
                    lastStd = i;
                }
                if (lastDst == -1 && this.mIsDsts[type] != null) {
                    lastDst = i;
                }
                i--;
            }
        }
        if (this.mTransitions.length == 0) {
            this.mRawOffset = gmtOffsets[0];
        } else if (lastStd == -1) {
            throw new IllegalStateException("ZoneInfo requires at least one non-DST transition to be provided for each timezone that has at least one transition but could not find one for '" + name + "'");
        } else {
            this.mRawOffset = gmtOffsets[this.mTypes[lastStd] & Opcodes.OP_CONST_CLASS_JUMBO];
        }
        if (lastDst != -1 && this.mTransitions[lastDst] < roundUpMillisToSeconds(currentTimeMillis)) {
            lastDst = -1;
        }
        if (lastDst == -1) {
            this.mDstSavings = 0;
            this.mUseDst = false;
        } else {
            this.mDstSavings = Math.abs(gmtOffsets[this.mTypes[lastStd] & Opcodes.OP_CONST_CLASS_JUMBO] - gmtOffsets[this.mTypes[lastDst] & Opcodes.OP_CONST_CLASS_JUMBO]) * Grego.MILLIS_PER_SECOND;
            this.mUseDst = true;
        }
        int firstStd = -1;
        for (i = 0; i < this.mTransitions.length; i++) {
            if (this.mIsDsts[this.mTypes[i] & Opcodes.OP_CONST_CLASS_JUMBO] == null) {
                firstStd = i;
                break;
            }
        }
        int earliestRawOffset = firstStd != -1 ? gmtOffsets[this.mTypes[firstStd] & Opcodes.OP_CONST_CLASS_JUMBO] : this.mRawOffset;
        this.mOffsets = gmtOffsets;
        for (i = 0; i < this.mOffsets.length; i++) {
            int[] iArr = this.mOffsets;
            iArr[i] = iArr[i] - this.mRawOffset;
        }
        this.mRawOffset *= Grego.MILLIS_PER_SECOND;
        this.mEarliestRawOffset = earliestRawOffset * Grego.MILLIS_PER_SECOND;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (!this.mUseDst && this.mDstSavings != 0) {
            this.mDstSavings = 0;
        }
    }

    public int getOffset(int era, int year, int month, int day, int dayOfWeek, int millis) {
        year %= BreakIterator.WORD_KANA_LIMIT;
        long calc = ((((long) (year / BreakIterator.WORD_KANA_LIMIT)) * MILLISECONDS_PER_400_YEARS) + (((long) year) * 31536000000L)) + (((long) ((year + 3) / 4)) * MILLISECONDS_PER_DAY);
        if (year > 0) {
            calc -= ((long) ((year - 1) / 100)) * MILLISECONDS_PER_DAY;
        }
        boolean isLeap = year == 0 || (year % 4 == 0 && year % 100 != 0);
        return getOffset(((((calc + (((long) (isLeap ? LEAP : NORMAL)[month]) * MILLISECONDS_PER_DAY)) + (((long) (day - 1)) * MILLISECONDS_PER_DAY)) + ((long) millis)) - ((long) this.mRawOffset)) - UNIX_OFFSET);
    }

    int findOffsetIndexForTimeInSeconds(long seconds) {
        int transition = findTransitionIndex(seconds);
        if (transition < 0) {
            return -1;
        }
        return this.mTypes[transition] & Opcodes.OP_CONST_CLASS_JUMBO;
    }

    int findOffsetIndexForTimeInMilliseconds(long millis) {
        return findOffsetIndexForTimeInSeconds(roundDownMillisToSeconds(millis));
    }

    static long roundDownMillisToSeconds(long millis) {
        if (millis < 0) {
            return (millis - 999) / 1000;
        }
        return millis / 1000;
    }

    static long roundUpMillisToSeconds(long millis) {
        if (millis > 0) {
            return (999 + millis) / 1000;
        }
        return millis / 1000;
    }

    public int getOffsetsByUtcTime(long utcTimeInMillis, int[] offsets) {
        int rawOffset;
        int dstOffset;
        int totalOffset;
        int transitionIndex = findTransitionIndex(roundDownMillisToSeconds(utcTimeInMillis));
        if (transitionIndex == -1) {
            rawOffset = this.mEarliestRawOffset;
            dstOffset = 0;
            totalOffset = rawOffset;
        } else {
            int type = this.mTypes[transitionIndex] & Opcodes.OP_CONST_CLASS_JUMBO;
            totalOffset = this.mRawOffset + (this.mOffsets[type] * Grego.MILLIS_PER_SECOND);
            if (this.mIsDsts[type] == null) {
                rawOffset = totalOffset;
                dstOffset = 0;
            } else {
                rawOffset = -1;
                for (transitionIndex--; transitionIndex >= 0; transitionIndex--) {
                    type = this.mTypes[transitionIndex] & Opcodes.OP_CONST_CLASS_JUMBO;
                    if (this.mIsDsts[type] == null) {
                        rawOffset = this.mRawOffset + (this.mOffsets[type] * Grego.MILLIS_PER_SECOND);
                        break;
                    }
                }
                if (rawOffset == -1) {
                    rawOffset = this.mEarliestRawOffset;
                }
                dstOffset = totalOffset - rawOffset;
            }
        }
        offsets[0] = rawOffset;
        offsets[1] = dstOffset;
        return totalOffset;
    }

    public int getOffset(long when) {
        int offsetIndex = findOffsetIndexForTimeInMilliseconds(when);
        if (offsetIndex == -1) {
            return this.mEarliestRawOffset;
        }
        return this.mRawOffset + (this.mOffsets[offsetIndex] * Grego.MILLIS_PER_SECOND);
    }

    public boolean inDaylightTime(Date time) {
        boolean z = true;
        int offsetIndex = findOffsetIndexForTimeInMilliseconds(time.getTime());
        if (offsetIndex == -1) {
            return false;
        }
        if (this.mIsDsts[offsetIndex] != (byte) 1) {
            z = false;
        }
        return z;
    }

    public int getRawOffset() {
        return this.mRawOffset;
    }

    public void setRawOffset(int off) {
        this.mRawOffset = off;
    }

    public int getDSTSavings() {
        return this.mDstSavings;
    }

    public boolean useDaylightTime() {
        return this.mUseDst;
    }

    public boolean hasSameRules(TimeZone timeZone) {
        boolean z = false;
        if (!(timeZone instanceof ZoneInfo)) {
            return false;
        }
        ZoneInfo other = (ZoneInfo) timeZone;
        if (this.mUseDst != other.mUseDst) {
            return false;
        }
        if (this.mUseDst) {
            if (this.mRawOffset == other.mRawOffset && Arrays.equals(this.mOffsets, other.mOffsets) && Arrays.equals(this.mIsDsts, other.mIsDsts) && Arrays.equals(this.mTypes, other.mTypes)) {
                z = Arrays.equals(this.mTransitions, other.mTransitions);
            }
            return z;
        }
        if (this.mRawOffset == other.mRawOffset) {
            z = true;
        }
        return z;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof ZoneInfo)) {
            return false;
        }
        ZoneInfo other = (ZoneInfo) obj;
        if (getID().equals(other.getID())) {
            z = hasSameRules(other);
        }
        return z;
    }

    public int hashCode() {
        return ((((((((((((getID().hashCode() + 31) * 31) + Arrays.hashCode(this.mOffsets)) * 31) + Arrays.hashCode(this.mIsDsts)) * 31) + this.mRawOffset) * 31) + Arrays.hashCode(this.mTransitions)) * 31) + Arrays.hashCode(this.mTypes)) * 31) + (this.mUseDst ? 1231 : 1237);
    }

    public String toString() {
        return getClass().getName() + "[id=\"" + getID() + "\"" + ",mRawOffset=" + this.mRawOffset + ",mEarliestRawOffset=" + this.mEarliestRawOffset + ",mUseDst=" + this.mUseDst + ",mDstSavings=" + this.mDstSavings + ",transitions=" + this.mTransitions.length + "]";
    }

    public Object clone() {
        return super.clone();
    }

    private static int checkedAdd(long a, int b) throws CheckedArithmeticException {
        long result = a + ((long) b);
        if (result == ((long) ((int) result))) {
            return (int) result;
        }
        throw new CheckedArithmeticException();
    }

    private static int checkedSubtract(int a, int b) throws CheckedArithmeticException {
        long result = ((long) a) - ((long) b);
        if (result == ((long) ((int) result))) {
            return (int) result;
        }
        throw new CheckedArithmeticException();
    }
}
