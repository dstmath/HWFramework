package android.icu.util;

import android.icu.math.BigDecimal;

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
    private static final TimeScaleData[] timeScaleTable = new TimeScaleData[]{new TimeScaleData(milliseconds, 621355968000000000L, -9223372036854774999L, 9223372036854774999L, -984472800485477L, 860201606885477L), new TimeScaleData(seconds, 621355968000000000L, Long.MIN_VALUE, Long.MAX_VALUE, -984472800485L, 860201606885L), new TimeScaleData(milliseconds, 621355968000000000L, -9223372036854774999L, 9223372036854774999L, -984472800485477L, 860201606885477L), new TimeScaleData(ticks, 504911232000000000L, -8718460804854775808L, Long.MAX_VALUE, Long.MIN_VALUE, 8718460804854775807L), new TimeScaleData(ticks, 0, Long.MIN_VALUE, Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE), new TimeScaleData(seconds, 600527520000000000L, Long.MIN_VALUE, Long.MAX_VALUE, -982389955685L, 862284451685L), new TimeScaleData(seconds, 631139040000000000L, Long.MIN_VALUE, Long.MAX_VALUE, -985451107685L, 859223299685L), new TimeScaleData(days, 599265216000000000L, Long.MIN_VALUE, Long.MAX_VALUE, -11368793, 9981605), new TimeScaleData(days, 599265216000000000L, Long.MIN_VALUE, Long.MAX_VALUE, -11368793, 9981605), new TimeScaleData(microseconds, 621355968000000000L, -9223372036854775804L, 9223372036854775804L, -984472800485477580L, 860201606885477580L)};

    private static final class TimeScaleData {
        long epochOffset;
        long epochOffsetM1;
        long epochOffsetP1;
        long fromMax;
        long fromMin;
        long maxRound = (Long.MAX_VALUE - this.unitsRound);
        long minRound = (this.unitsRound - Long.MIN_VALUE);
        long toMax;
        long toMin;
        long units;
        long unitsRound;

        TimeScaleData(long theUnits, long theEpochOffset, long theToMin, long theToMax, long theFromMin, long theFromMax) {
            this.units = theUnits;
            this.unitsRound = theUnits / 2;
            this.epochOffset = theEpochOffset / theUnits;
            if (theUnits == UniversalTimeScale.ticks) {
                long j = this.epochOffset;
                this.epochOffsetM1 = j;
                this.epochOffsetP1 = j;
            } else {
                this.epochOffsetP1 = this.epochOffset + UniversalTimeScale.ticks;
                this.epochOffsetM1 = this.epochOffset - UniversalTimeScale.ticks;
            }
            this.toMin = theToMin;
            this.toMax = theToMax;
            this.fromMin = theFromMin;
            this.fromMax = theFromMax;
        }
    }

    private UniversalTimeScale() {
    }

    public static long from(long otherTime, int timeScale) {
        TimeScaleData data = fromRangeCheck(otherTime, timeScale);
        return (data.epochOffset + otherTime) * data.units;
    }

    public static BigDecimal bigDecimalFrom(double otherTime, int timeScale) {
        TimeScaleData data = getTimeScaleData(timeScale);
        BigDecimal other = new BigDecimal(String.valueOf(otherTime));
        return other.add(new BigDecimal(data.epochOffset)).multiply(new BigDecimal(data.units));
    }

    public static BigDecimal bigDecimalFrom(long otherTime, int timeScale) {
        TimeScaleData data = getTimeScaleData(timeScale);
        BigDecimal other = new BigDecimal(otherTime);
        return other.add(new BigDecimal(data.epochOffset)).multiply(new BigDecimal(data.units));
    }

    public static BigDecimal bigDecimalFrom(BigDecimal otherTime, int timeScale) {
        TimeScaleData data = getTimeScaleData(timeScale);
        return otherTime.add(new BigDecimal(data.epochOffset)).multiply(new BigDecimal(data.units));
    }

    public static long toLong(long universalTime, int timeScale) {
        TimeScaleData data = toRangeCheck(universalTime, timeScale);
        if (universalTime < 0) {
            if (universalTime < data.minRound) {
                return ((data.unitsRound + universalTime) / data.units) - data.epochOffsetP1;
            }
            return ((universalTime - data.unitsRound) / data.units) - data.epochOffset;
        } else if (universalTime > data.maxRound) {
            return ((universalTime - data.unitsRound) / data.units) - data.epochOffsetM1;
        } else {
            return ((data.unitsRound + universalTime) / data.units) - data.epochOffset;
        }
    }

    public static BigDecimal toBigDecimal(long universalTime, int timeScale) {
        TimeScaleData data = getTimeScaleData(timeScale);
        BigDecimal universal = new BigDecimal(universalTime);
        BigDecimal units = new BigDecimal(data.units);
        return universal.divide(units, 4).subtract(new BigDecimal(data.epochOffset));
    }

    public static BigDecimal toBigDecimal(BigDecimal universalTime, int timeScale) {
        TimeScaleData data = getTimeScaleData(timeScale);
        BigDecimal units = new BigDecimal(data.units);
        return universalTime.divide(units, 4).subtract(new BigDecimal(data.epochOffset));
    }

    private static TimeScaleData getTimeScaleData(int scale) {
        if (scale >= 0 && scale < 10) {
            return timeScaleTable[scale];
        }
        throw new IllegalArgumentException("scale out of range: " + scale);
    }

    public static long getTimeScaleValue(int scale, int value) {
        TimeScaleData data = getTimeScaleData(scale);
        switch (value) {
            case 0:
                return data.units;
            case 1:
                return data.epochOffset;
            case 2:
                return data.fromMin;
            case 3:
                return data.fromMax;
            case 4:
                return data.toMin;
            case 5:
                return data.toMax;
            case 6:
                return data.epochOffsetP1;
            case 7:
                return data.epochOffsetM1;
            case 8:
                return data.unitsRound;
            case 9:
                return data.minRound;
            case 10:
                return data.maxRound;
            default:
                throw new IllegalArgumentException("value out of range: " + value);
        }
    }

    private static TimeScaleData toRangeCheck(long universalTime, int scale) {
        TimeScaleData data = getTimeScaleData(scale);
        if (universalTime >= data.toMin && universalTime <= data.toMax) {
            return data;
        }
        throw new IllegalArgumentException("universalTime out of range:" + universalTime);
    }

    private static TimeScaleData fromRangeCheck(long otherTime, int scale) {
        TimeScaleData data = getTimeScaleData(scale);
        if (otherTime >= data.fromMin && otherTime <= data.fromMax) {
            return data;
        }
        throw new IllegalArgumentException("otherTime out of range:" + otherTime);
    }

    @Deprecated
    public static BigDecimal toBigDecimalTrunc(BigDecimal universalTime, int timeScale) {
        TimeScaleData data = getTimeScaleData(timeScale);
        BigDecimal units = new BigDecimal(data.units);
        return universalTime.divide(units, 1).subtract(new BigDecimal(data.epochOffset));
    }
}
