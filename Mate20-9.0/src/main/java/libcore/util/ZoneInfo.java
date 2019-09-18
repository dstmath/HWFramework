package libcore.util;

import android.icu.lang.UCharacter;
import android.icu.lang.UCharacterEnums;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import libcore.io.BufferIterator;

public final class ZoneInfo extends TimeZone {
    private static final int[] LEAP = {0, 31, 60, 91, 121, 152, 182, 213, 244, UCharacter.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_F_ID, 305, 335};
    private static final long MILLISECONDS_PER_400_YEARS = 12622780800000L;
    private static final long MILLISECONDS_PER_DAY = 86400000;
    private static final int[] NORMAL = {0, 31, 59, 90, 120, 151, 181, 212, 243, UCharacter.UnicodeBlock.TANGUT_COMPONENTS_ID, 304, 334};
    private static final long UNIX_OFFSET = 62167219200000L;
    static final long serialVersionUID = -4598738130123921552L;
    private int mDstSavings;
    private final int mEarliestRawOffset;
    /* access modifiers changed from: private */
    public final byte[] mIsDsts;
    /* access modifiers changed from: private */
    public final int[] mOffsets;
    /* access modifiers changed from: private */
    public int mRawOffset;
    /* access modifiers changed from: private */
    public final long[] mTransitions;
    /* access modifiers changed from: private */
    public final byte[] mTypes;
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
            int endWallTimeSeconds2;
            if (transitionIndex < -1 || transitionIndex >= timeZone.mTransitions.length) {
                return null;
            }
            int rawOffsetSeconds = timeZone.mRawOffset / 1000;
            if (transitionIndex == -1) {
                return new OffsetInterval(Integer.MIN_VALUE, ZoneInfo.checkedAdd(timeZone.mTransitions[0], rawOffsetSeconds), 0, rawOffsetSeconds);
            }
            int type = timeZone.mTypes[transitionIndex] & 255;
            int totalOffsetSeconds2 = timeZone.mOffsets[type] + rawOffsetSeconds;
            if (transitionIndex == timeZone.mTransitions.length - 1) {
                endWallTimeSeconds2 = Integer.MAX_VALUE;
            } else {
                endWallTimeSeconds2 = ZoneInfo.checkedAdd(timeZone.mTransitions[transitionIndex + 1], totalOffsetSeconds2);
            }
            return new OffsetInterval(ZoneInfo.checkedAdd(timeZone.mTransitions[transitionIndex], totalOffsetSeconds2), endWallTimeSeconds2, timeZone.mIsDsts[type], totalOffsetSeconds2);
        }

        private OffsetInterval(int startWallTimeSeconds2, int endWallTimeSeconds2, int isDst2, int totalOffsetSeconds2) {
            this.startWallTimeSeconds = startWallTimeSeconds2;
            this.endWallTimeSeconds = endWallTimeSeconds2;
            this.isDst = isDst2;
            this.totalOffsetSeconds = totalOffsetSeconds2;
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
            GregorianCalendar gregorianCalendar = new GregorianCalendar(0, 0, 0, 0, 0, 0);
            this.calendar = gregorianCalendar;
            this.calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        public void localtime(int timeSeconds, ZoneInfo zoneInfo) {
            byte isDst2;
            try {
                int offsetSeconds = zoneInfo.mRawOffset / 1000;
                if (zoneInfo.mTransitions.length == 0) {
                    isDst2 = 0;
                } else {
                    int offsetIndex = zoneInfo.findOffsetIndexForTimeInSeconds((long) timeSeconds);
                    if (offsetIndex == -1) {
                        isDst2 = 0;
                    } else {
                        offsetSeconds += zoneInfo.mOffsets[offsetIndex];
                        isDst2 = zoneInfo.mIsDsts[offsetIndex];
                    }
                }
                this.calendar.setTimeInMillis(((long) ZoneInfo.checkedAdd((long) timeSeconds, offsetSeconds)) * 1000);
                copyFieldsFromCalendar();
                this.isDst = isDst2;
                this.gmtOffsetSeconds = offsetSeconds;
            } catch (CheckedArithmeticException e) {
            }
        }

        public int mktime(ZoneInfo zoneInfo) {
            int i;
            if (this.isDst > 0) {
                this.isDst = 1;
                i = 1;
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
                int rawOffsetSeconds = zoneInfo.mRawOffset / 1000;
                int rawTimeSeconds = ZoneInfo.checkedSubtract(wallTimeSeconds, rawOffsetSeconds);
                if (zoneInfo.mTransitions.length != 0) {
                    int initialTransitionIndex = zoneInfo.findTransitionIndex((long) rawTimeSeconds);
                    if (this.isDst < 0) {
                        Integer result = doWallTimeSearch(zoneInfo, initialTransitionIndex, wallTimeSeconds, true);
                        return result == null ? -1 : result.intValue();
                    }
                    Integer result2 = doWallTimeSearch(zoneInfo, initialTransitionIndex, wallTimeSeconds, true);
                    if (result2 == null) {
                        result2 = doWallTimeSearch(zoneInfo, initialTransitionIndex, wallTimeSeconds, false);
                    }
                    if (result2 == null) {
                        result2 = -1;
                    }
                    return result2.intValue();
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
            int j = 0;
            while (j < offsetsToTry.length) {
                int i = offsetsToTry[j];
                int targetIntervalOffsetSeconds = targetInterval.getTotalOffsetSeconds();
                int adjustedWallTimeSeconds = ZoneInfo.checkedAdd((long) oldWallTimeSeconds, targetIntervalOffsetSeconds - (i + (zoneInfo.mRawOffset / 1000)));
                if (targetInterval.containsWallTime((long) adjustedWallTimeSeconds)) {
                    int returnValue = ZoneInfo.checkedSubtract(adjustedWallTimeSeconds, targetIntervalOffsetSeconds);
                    this.calendar.setTimeInMillis(((long) adjustedWallTimeSeconds) * 1000);
                    copyFieldsFromCalendar();
                    this.isDst = targetInterval.getIsDst();
                    this.gmtOffsetSeconds = targetIntervalOffsetSeconds;
                    return Integer.valueOf(returnValue);
                }
                j++;
                ZoneInfo zoneInfo2 = zoneInfo;
                int i2 = transitionIndex;
            }
            int i3 = oldWallTimeSeconds;
            OffsetInterval offsetInterval = targetInterval;
            return null;
        }

        private static int[] getOffsetsOfType(ZoneInfo zoneInfo, int startIndex, int isDst2) {
            int[] offsets = new int[(zoneInfo.mOffsets.length + 1)];
            boolean[] seen = new boolean[zoneInfo.mOffsets.length];
            int delta = 0;
            boolean clampTop = false;
            int numFound = 0;
            boolean clampBottom = false;
            while (true) {
                delta *= -1;
                if (delta >= 0) {
                    delta++;
                }
                int transitionIndex = startIndex + delta;
                if (delta < 0 && transitionIndex < -1) {
                    clampBottom = true;
                } else if (delta > 0 && transitionIndex >= zoneInfo.mTypes.length) {
                    clampTop = true;
                } else if (transitionIndex != -1) {
                    int type = zoneInfo.mTypes[transitionIndex] & 255;
                    if (!seen[type]) {
                        if (zoneInfo.mIsDsts[type] == isDst2) {
                            offsets[numFound] = zoneInfo.mOffsets[type];
                            numFound++;
                        }
                        seen[type] = true;
                    }
                } else if (isDst2 == 0) {
                    offsets[numFound] = 0;
                    numFound++;
                }
                if (clampTop && clampBottom) {
                    int[] toReturn = new int[numFound];
                    System.arraycopy(offsets, 0, toReturn, 0, numFound);
                    return toReturn;
                }
            }
        }

        private Integer doWallTimeSearch(ZoneInfo zoneInfo, int initialTransitionIndex, int wallTimeSeconds, boolean mustMatchDst) throws CheckedArithmeticException {
            boolean clampTop;
            OffsetInterval offsetInterval;
            OffsetInterval offsetInterval2;
            int isDstToFind = wallTimeSeconds;
            boolean clampTop2 = false;
            boolean clampBottom = false;
            int loop = 0;
            while (true) {
                int transitionIndexDelta = (loop + 1) / 2;
                boolean endSearch = true;
                if (loop % 2 == 1) {
                    transitionIndexDelta *= -1;
                }
                int transitionIndexDelta2 = transitionIndexDelta;
                int loop2 = loop + 1;
                if ((transitionIndexDelta2 <= 0 || !clampTop2) && (transitionIndexDelta2 >= 0 || !clampBottom)) {
                    int currentTransitionIndex = initialTransitionIndex + transitionIndexDelta2;
                    ZoneInfo zoneInfo2 = zoneInfo;
                    offsetInterval = OffsetInterval.create(zoneInfo2, currentTransitionIndex);
                    if (offsetInterval == null) {
                        boolean clampTop3 = (transitionIndexDelta2 > 0) | clampTop2;
                        if (transitionIndexDelta2 >= 0) {
                            endSearch = false;
                        }
                        clampTop2 = clampTop3;
                        clampBottom |= endSearch;
                    } else {
                        if (mustMatchDst) {
                            if (!offsetInterval.containsWallTime((long) isDstToFind) || !(this.isDst == -1 || offsetInterval.getIsDst() == this.isDst)) {
                                offsetInterval2 = offsetInterval;
                                int i = currentTransitionIndex;
                            }
                        } else if (this.isDst != offsetInterval.getIsDst()) {
                            offsetInterval2 = offsetInterval;
                            int i2 = currentTransitionIndex;
                            Integer returnValue = tryOffsetAdjustments(zoneInfo2, isDstToFind, offsetInterval, currentTransitionIndex, this.isDst);
                            if (returnValue != null) {
                                return returnValue;
                            }
                        } else {
                            offsetInterval2 = offsetInterval;
                            int i3 = currentTransitionIndex;
                        }
                        if (transitionIndexDelta2 > 0) {
                            boolean clampTop4 = clampTop2;
                            if (offsetInterval2.getEndWallTimeSeconds() - ((long) isDstToFind) <= 86400) {
                                endSearch = false;
                            }
                            clampTop2 = endSearch ? true : clampTop4;
                        } else {
                            clampTop = clampTop2;
                            OffsetInterval offsetInterval3 = offsetInterval2;
                            if (transitionIndexDelta2 < 0) {
                                if (((long) isDstToFind) - offsetInterval3.getStartWallTimeSeconds() < 86400) {
                                    endSearch = false;
                                }
                                if (endSearch) {
                                    clampBottom = true;
                                    clampTop2 = clampTop;
                                }
                            }
                        }
                    }
                    if (!clampTop2 && clampBottom) {
                        return null;
                    }
                    loop = loop2;
                } else {
                    clampTop = clampTop2;
                }
                clampTop2 = clampTop;
                if (!clampTop2) {
                }
                loop = loop2;
            }
            int totalOffsetSeconds = offsetInterval.getTotalOffsetSeconds();
            int returnValue2 = ZoneInfo.checkedSubtract(isDstToFind, totalOffsetSeconds);
            copyFieldsFromCalendar();
            this.isDst = offsetInterval.getIsDst();
            this.gmtOffsetSeconds = totalOffsetSeconds;
            return Integer.valueOf(returnValue2);
        }

        public void setYear(int year2) {
            this.year = year2;
        }

        public void setMonth(int month2) {
            this.month = month2;
        }

        public void setMonthDay(int monthDay2) {
            this.monthDay = monthDay2;
        }

        public void setHour(int hour2) {
            this.hour = hour2;
        }

        public void setMinute(int minute2) {
            this.minute = minute2;
        }

        public void setSecond(int second2) {
            this.second = second2;
        }

        public void setWeekDay(int weekDay2) {
            this.weekDay = weekDay2;
        }

        public void setYearDay(int yearDay2) {
            this.yearDay = yearDay2;
        }

        public void setIsDst(int isDst2) {
            this.isDst = isDst2;
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

    public static ZoneInfo readTimeZone(String id, BufferIterator it, long currentTimeMillis) throws IOException {
        String str = id;
        BufferIterator bufferIterator = it;
        if (it.readInt() == 1415211366) {
            bufferIterator.skip(28);
            int tzh_timecnt = it.readInt();
            char c = 2000;
            if (tzh_timecnt < 0 || tzh_timecnt > 2000) {
                throw new IOException("Timezone id=" + str + " has an invalid number of transitions=" + tzh_timecnt);
            }
            int tzh_typecnt = it.readInt();
            if (tzh_typecnt < 1) {
                throw new IOException("ZoneInfo requires at least one type to be provided for each timezone but could not find one for '" + str + "'");
            } else if (tzh_typecnt <= 256) {
                bufferIterator.skip(4);
                int[] transitions32 = new int[tzh_timecnt];
                int i = 0;
                bufferIterator.readIntArray(transitions32, 0, transitions32.length);
                long[] transitions64 = new long[tzh_timecnt];
                int i2 = 0;
                while (i2 < tzh_timecnt) {
                    transitions64[i2] = (long) transitions32[i2];
                    if (i2 <= 0 || transitions64[i2] > transitions64[i2 - 1]) {
                        i2++;
                    } else {
                        throw new IOException(str + " transition at " + i2 + " is not sorted correctly, is " + transitions64[i2] + ", previous is " + transitions64[i2 - 1]);
                    }
                }
                byte[] type = new byte[tzh_timecnt];
                bufferIterator.readByteArray(type, 0, type.length);
                int i3 = 0;
                while (i3 < type.length) {
                    if ((type[i3] & 255) < tzh_typecnt) {
                        i3++;
                    } else {
                        throw new IOException(str + " type at " + i3 + " is not < " + tzh_typecnt + ", is " + typeIndex);
                    }
                }
                int[] gmtOffsets = new int[tzh_typecnt];
                byte[] isDsts = new byte[tzh_typecnt];
                while (true) {
                    int i4 = i;
                    if (i4 < tzh_typecnt) {
                        gmtOffsets[i4] = it.readInt();
                        byte isDst = it.readByte();
                        if (isDst == 0 || isDst == 1) {
                            isDsts[i4] = isDst;
                            bufferIterator.skip(1);
                            i = i4 + 1;
                            c = c;
                        } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append(str);
                            char c2 = c;
                            sb.append(" dst at ");
                            sb.append(i4);
                            sb.append(" is not 0 or 1, is ");
                            sb.append(isDst);
                            throw new IOException(sb.toString());
                        }
                    } else {
                        int MAX_TRANSITIONS = c;
                        int[] iArr = gmtOffsets;
                        long[] jArr = transitions64;
                        byte[] bArr = type;
                        ZoneInfo zoneInfo = new ZoneInfo(str, transitions64, type, gmtOffsets, isDsts, currentTimeMillis);
                        return zoneInfo;
                    }
                }
            } else {
                throw new IOException("Timezone with id " + str + " has too many types=" + tzh_typecnt);
            }
        } else {
            throw new IOException("Timezone id=" + str + " has an invalid header=" + tzh_magic);
        }
    }

    private ZoneInfo(String name, long[] transitions, byte[] types, int[] gmtOffsets, byte[] isDsts, long currentTimeMillis) {
        String str = name;
        int[] iArr = gmtOffsets;
        if (iArr.length != 0) {
            this.mTransitions = transitions;
            this.mTypes = types;
            this.mIsDsts = isDsts;
            setID(name);
            int lastStd = -1;
            int lastDst = -1;
            int i = this.mTransitions.length - 1;
            while (true) {
                if ((lastStd == -1 || lastDst == -1) && i >= 0) {
                    int type = this.mTypes[i] & 255;
                    if (lastStd == -1 && this.mIsDsts[type] == 0) {
                        lastStd = i;
                    }
                    if (lastDst == -1 && this.mIsDsts[type] != 0) {
                        lastDst = i;
                    }
                    i--;
                }
            }
            int i2 = 0;
            if (this.mTransitions.length == 0) {
                this.mRawOffset = iArr[0];
            } else if (lastStd != -1) {
                this.mRawOffset = iArr[this.mTypes[lastStd] & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED];
            } else {
                throw new IllegalStateException("ZoneInfo requires at least one non-DST transition to be provided for each timezone that has at least one transition but could not find one for '" + str + "'");
            }
            if (lastDst != -1 && this.mTransitions[lastDst] < roundUpMillisToSeconds(currentTimeMillis)) {
                lastDst = -1;
            }
            if (lastDst == -1) {
                this.mDstSavings = 0;
                this.mUseDst = false;
            } else {
                this.mDstSavings = (iArr[this.mTypes[lastDst] & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED] - iArr[this.mTypes[lastStd] & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED]) * 1000;
                this.mUseDst = true;
            }
            int firstStd = -1;
            int i3 = 0;
            while (true) {
                if (i3 >= this.mTransitions.length) {
                    break;
                } else if (this.mIsDsts[this.mTypes[i3] & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED] == 0) {
                    firstStd = i3;
                    break;
                } else {
                    i3++;
                }
            }
            int earliestRawOffset = firstStd != -1 ? iArr[this.mTypes[firstStd] & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED] : this.mRawOffset;
            this.mOffsets = iArr;
            while (true) {
                int i4 = i2;
                if (i4 < this.mOffsets.length) {
                    int[] iArr2 = this.mOffsets;
                    iArr2[i4] = iArr2[i4] - this.mRawOffset;
                    i2 = i4 + 1;
                } else {
                    this.mRawOffset *= 1000;
                    this.mEarliestRawOffset = earliestRawOffset * 1000;
                    return;
                }
            }
        } else {
            long[] jArr = transitions;
            byte[] bArr = types;
            byte[] bArr2 = isDsts;
            throw new IllegalArgumentException("ZoneInfo requires at least one offset to be provided for each timezone but could not find one for '" + str + "'");
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (!this.mUseDst && this.mDstSavings != 0) {
            this.mDstSavings = 0;
        }
    }

    public int getOffset(int era, int year, int month, int day, int dayOfWeek, int millis) {
        int year2 = year % 400;
        long calc = (((long) (year / 400)) * MILLISECONDS_PER_400_YEARS) + (((long) year2) * 31536000000L) + (((long) ((year2 + 3) / 4)) * 86400000);
        if (year2 > 0) {
            calc -= ((long) ((year2 - 1) / 100)) * 86400000;
        }
        return getOffset(((((calc + (((long) (year2 == 0 || (year2 % 4 == 0 && year2 % 100 != 0) ? LEAP : NORMAL)[month]) * 86400000)) + (((long) (day - 1)) * 86400000)) + ((long) millis)) - ((long) this.mRawOffset)) - UNIX_OFFSET);
    }

    public int findTransitionIndex(long seconds) {
        int transition = Arrays.binarySearch(this.mTransitions, seconds);
        if (transition < 0) {
            transition = (~transition) - 1;
            if (transition < 0) {
                return -1;
            }
        }
        return transition;
    }

    /* access modifiers changed from: package-private */
    public int findOffsetIndexForTimeInSeconds(long seconds) {
        int transition = findTransitionIndex(seconds);
        if (transition < 0) {
            return -1;
        }
        return this.mTypes[transition] & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED;
    }

    /* access modifiers changed from: package-private */
    public int findOffsetIndexForTimeInMilliseconds(long millis) {
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
        int totalOffset;
        int type;
        int rawOffset;
        int transitionIndex = findTransitionIndex(roundDownMillisToSeconds(utcTimeInMillis));
        if (transitionIndex == -1) {
            rawOffset = this.mEarliestRawOffset;
            type = 0;
            totalOffset = rawOffset;
        } else {
            int type2 = this.mTypes[transitionIndex] & 255;
            totalOffset = this.mRawOffset + (this.mOffsets[type2] * 1000);
            if (this.mIsDsts[type2] == 0) {
                rawOffset = totalOffset;
                type = 0;
            } else {
                int rawOffset2 = -1;
                while (true) {
                    transitionIndex--;
                    if (transitionIndex < 0) {
                        break;
                    }
                    int type3 = this.mTypes[transitionIndex] & 255;
                    if (this.mIsDsts[type3] == 0) {
                        rawOffset2 = this.mRawOffset + (this.mOffsets[type3] * 1000);
                        break;
                    }
                }
                if (rawOffset2 == -1) {
                    rawOffset = this.mEarliestRawOffset;
                } else {
                    rawOffset = rawOffset2;
                }
                type = totalOffset - rawOffset;
            }
        }
        offsets[0] = rawOffset;
        offsets[1] = type;
        return totalOffset;
    }

    public int getOffset(long when) {
        int offsetIndex = findOffsetIndexForTimeInMilliseconds(when);
        if (offsetIndex == -1) {
            return this.mEarliestRawOffset;
        }
        return this.mRawOffset + (this.mOffsets[offsetIndex] * 1000);
    }

    public boolean inDaylightTime(Date time) {
        int offsetIndex = findOffsetIndexForTimeInMilliseconds(time.getTime());
        boolean z = false;
        if (offsetIndex == -1) {
            return false;
        }
        if (this.mIsDsts[offsetIndex] == 1) {
            z = true;
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
        if (!this.mUseDst) {
            if (this.mRawOffset == other.mRawOffset) {
                z = true;
            }
            return z;
        }
        if (this.mRawOffset == other.mRawOffset && Arrays.equals(this.mOffsets, other.mOffsets) && Arrays.equals(this.mIsDsts, other.mIsDsts) && Arrays.equals(this.mTypes, other.mTypes) && Arrays.equals(this.mTransitions, other.mTransitions)) {
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
        if (getID().equals(other.getID()) && hasSameRules(other)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return (31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * 1) + getID().hashCode())) + Arrays.hashCode(this.mOffsets))) + Arrays.hashCode(this.mIsDsts))) + this.mRawOffset)) + Arrays.hashCode(this.mTransitions))) + Arrays.hashCode(this.mTypes))) + (this.mUseDst ? 1231 : 1237);
    }

    public String toString() {
        return getClass().getName() + "[id=\"" + getID() + "\",mRawOffset=" + this.mRawOffset + ",mEarliestRawOffset=" + this.mEarliestRawOffset + ",mUseDst=" + this.mUseDst + ",mDstSavings=" + this.mDstSavings + ",transitions=" + this.mTransitions.length + "]";
    }

    public Object clone() {
        return super.clone();
    }

    /* access modifiers changed from: private */
    public static int checkedAdd(long a, int b) throws CheckedArithmeticException {
        long result = ((long) b) + a;
        if (result == ((long) ((int) result))) {
            return (int) result;
        }
        throw new CheckedArithmeticException();
    }

    /* access modifiers changed from: private */
    public static int checkedSubtract(int a, int b) throws CheckedArithmeticException {
        long result = ((long) a) - ((long) b);
        if (result == ((long) ((int) result))) {
            return (int) result;
        }
        throw new CheckedArithmeticException();
    }
}
