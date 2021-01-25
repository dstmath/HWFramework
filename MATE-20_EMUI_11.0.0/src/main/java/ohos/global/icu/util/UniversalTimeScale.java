package ohos.global.icu.util;

import ohos.global.icu.math.BigDecimal;

public final class UniversalTimeScale {
    public static final int DB2_TIME = 8;
    public static final int DOTNET_DATE_TIME = 4;
    @Deprecated
    public static final int EPOCH_OFFSET_MINUS_1_VALUE = 7;
    public static final int EPOCH_OFFSET_PLUS_1_VALUE = 6;
    public static final int EPOCH_OFFSET_VALUE = 1;
    public static final int EXCEL_TIME = 7;
    public static final int FROM_MAX_VALUE = 3;
    public static final int FROM_MIN_VALUE = 2;
    public static final int ICU4C_TIME = 2;
    public static final int JAVA_TIME = 0;
    public static final int MAC_OLD_TIME = 5;
    public static final int MAC_TIME = 6;
    @Deprecated
    public static final int MAX_ROUND_VALUE = 10;
    @Deprecated
    public static final int MAX_SCALE = 10;
    @Deprecated
    public static final int MAX_SCALE_VALUE = 11;
    @Deprecated
    public static final int MIN_ROUND_VALUE = 9;
    public static final int TO_MAX_VALUE = 5;
    public static final int TO_MIN_VALUE = 4;
    @Deprecated
    public static final int UNITS_ROUND_VALUE = 8;
    public static final int UNITS_VALUE = 0;
    public static final int UNIX_MICROSECONDS_TIME = 9;
    public static final int UNIX_TIME = 1;
    public static final int WINDOWS_FILE_TIME = 3;
    private static final long days = 864000000000L;
    private static final long hours = 36000000000L;
    private static final long microseconds = 10;
    private static final long milliseconds = 10000;
    private static final long minutes = 600000000;
    private static final long seconds = 10000000;
    private static final long ticks = 1;
    private static final TimeScaleData[] timeScaleTable = {new TimeScaleData(milliseconds, 621355968000000000L, -9223372036854774999L, 9223372036854774999L, -984472800485477L, 860201606885477L), new TimeScaleData(seconds, 621355968000000000L, Long.MIN_VALUE, Long.MAX_VALUE, -984472800485L, 860201606885L), new TimeScaleData(milliseconds, 621355968000000000L, -9223372036854774999L, 9223372036854774999L, -984472800485477L, 860201606885477L), new TimeScaleData(1, 504911232000000000L, -8718460804854775808L, Long.MAX_VALUE, Long.MIN_VALUE, 8718460804854775807L), new TimeScaleData(1, 0, Long.MIN_VALUE, Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE), new TimeScaleData(seconds, 600527520000000000L, Long.MIN_VALUE, Long.MAX_VALUE, -982389955685L, 862284451685L), new TimeScaleData(seconds, 631139040000000000L, Long.MIN_VALUE, Long.MAX_VALUE, -985451107685L, 859223299685L), new TimeScaleData(days, 599265216000000000L, Long.MIN_VALUE, Long.MAX_VALUE, -11368793, 9981605), new TimeScaleData(days, 599265216000000000L, Long.MIN_VALUE, Long.MAX_VALUE, -11368793, 9981605), new TimeScaleData(microseconds, 621355968000000000L, -9223372036854775804L, 9223372036854775804L, -984472800485477580L, 860201606885477580L)};

    /* access modifiers changed from: private */
    public static final class TimeScaleData {
        long epochOffset;
        long epochOffsetM1;
        long epochOffsetP1;
        long fromMax;
        long fromMin;
        long maxRound;
        long minRound;
        long toMax;
        long toMin;
        long units;
        long unitsRound;

        TimeScaleData(long j, long j2, long j3, long j4, long j5, long j6) {
            this.units = j;
            this.unitsRound = j / 2;
            long j7 = this.unitsRound;
            this.minRound = Long.MIN_VALUE + j7;
            this.maxRound = Long.MAX_VALUE - j7;
            this.epochOffset = j2 / j;
            if (j == 1) {
                long j8 = this.epochOffset;
                this.epochOffsetM1 = j8;
                this.epochOffsetP1 = j8;
            } else {
                long j9 = this.epochOffset;
                this.epochOffsetP1 = j9 + 1;
                this.epochOffsetM1 = j9 - 1;
            }
            this.toMin = j3;
            this.toMax = j4;
            this.fromMin = j5;
            this.fromMax = j6;
        }
    }

    private UniversalTimeScale() {
    }

    public static long from(long j, int i) {
        TimeScaleData fromRangeCheck = fromRangeCheck(j, i);
        return (j + fromRangeCheck.epochOffset) * fromRangeCheck.units;
    }

    public static BigDecimal bigDecimalFrom(double d, int i) {
        TimeScaleData timeScaleData = getTimeScaleData(i);
        BigDecimal bigDecimal = new BigDecimal(String.valueOf(d));
        return bigDecimal.add(new BigDecimal(timeScaleData.epochOffset)).multiply(new BigDecimal(timeScaleData.units));
    }

    public static BigDecimal bigDecimalFrom(long j, int i) {
        TimeScaleData timeScaleData = getTimeScaleData(i);
        BigDecimal bigDecimal = new BigDecimal(j);
        return bigDecimal.add(new BigDecimal(timeScaleData.epochOffset)).multiply(new BigDecimal(timeScaleData.units));
    }

    public static BigDecimal bigDecimalFrom(BigDecimal bigDecimal, int i) {
        TimeScaleData timeScaleData = getTimeScaleData(i);
        return bigDecimal.add(new BigDecimal(timeScaleData.epochOffset)).multiply(new BigDecimal(timeScaleData.units));
    }

    public static long toLong(long j, int i) {
        long j2;
        long j3;
        TimeScaleData rangeCheck = toRangeCheck(j, i);
        if (j < 0) {
            if (j < rangeCheck.minRound) {
                j2 = (j + rangeCheck.unitsRound) / rangeCheck.units;
                j3 = rangeCheck.epochOffsetP1;
            } else {
                j2 = (j - rangeCheck.unitsRound) / rangeCheck.units;
                j3 = rangeCheck.epochOffset;
            }
        } else if (j > rangeCheck.maxRound) {
            j2 = (j - rangeCheck.unitsRound) / rangeCheck.units;
            j3 = rangeCheck.epochOffsetM1;
        } else {
            j2 = (j + rangeCheck.unitsRound) / rangeCheck.units;
            j3 = rangeCheck.epochOffset;
        }
        return j2 - j3;
    }

    public static BigDecimal toBigDecimal(long j, int i) {
        TimeScaleData timeScaleData = getTimeScaleData(i);
        BigDecimal bigDecimal = new BigDecimal(j);
        BigDecimal bigDecimal2 = new BigDecimal(timeScaleData.units);
        return bigDecimal.divide(bigDecimal2, 4).subtract(new BigDecimal(timeScaleData.epochOffset));
    }

    public static BigDecimal toBigDecimal(BigDecimal bigDecimal, int i) {
        TimeScaleData timeScaleData = getTimeScaleData(i);
        BigDecimal bigDecimal2 = new BigDecimal(timeScaleData.units);
        return bigDecimal.divide(bigDecimal2, 4).subtract(new BigDecimal(timeScaleData.epochOffset));
    }

    private static TimeScaleData getTimeScaleData(int i) {
        if (i >= 0 && i < 10) {
            return timeScaleTable[i];
        }
        throw new IllegalArgumentException("scale out of range: " + i);
    }

    public static long getTimeScaleValue(int i, int i2) {
        TimeScaleData timeScaleData = getTimeScaleData(i);
        switch (i2) {
            case 0:
                return timeScaleData.units;
            case 1:
                return timeScaleData.epochOffset;
            case 2:
                return timeScaleData.fromMin;
            case 3:
                return timeScaleData.fromMax;
            case 4:
                return timeScaleData.toMin;
            case 5:
                return timeScaleData.toMax;
            case 6:
                return timeScaleData.epochOffsetP1;
            case 7:
                return timeScaleData.epochOffsetM1;
            case 8:
                return timeScaleData.unitsRound;
            case 9:
                return timeScaleData.minRound;
            case 10:
                return timeScaleData.maxRound;
            default:
                throw new IllegalArgumentException("value out of range: " + i2);
        }
    }

    private static TimeScaleData toRangeCheck(long j, int i) {
        TimeScaleData timeScaleData = getTimeScaleData(i);
        if (j >= timeScaleData.toMin && j <= timeScaleData.toMax) {
            return timeScaleData;
        }
        throw new IllegalArgumentException("universalTime out of range:" + j);
    }

    private static TimeScaleData fromRangeCheck(long j, int i) {
        TimeScaleData timeScaleData = getTimeScaleData(i);
        if (j >= timeScaleData.fromMin && j <= timeScaleData.fromMax) {
            return timeScaleData;
        }
        throw new IllegalArgumentException("otherTime out of range:" + j);
    }

    @Deprecated
    public static BigDecimal toBigDecimalTrunc(BigDecimal bigDecimal, int i) {
        TimeScaleData timeScaleData = getTimeScaleData(i);
        BigDecimal bigDecimal2 = new BigDecimal(timeScaleData.units);
        return bigDecimal.divide(bigDecimal2, 1).subtract(new BigDecimal(timeScaleData.epochOffset));
    }
}
