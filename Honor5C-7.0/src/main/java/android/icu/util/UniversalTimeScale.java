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
    private static final TimeScaleData[] timeScaleTable = null;

    private static final class TimeScaleData {
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

        TimeScaleData(long theUnits, long theEpochOffset, long theToMin, long theToMax, long theFromMin, long theFromMax) {
            this.units = theUnits;
            this.unitsRound = theUnits / 2;
            this.minRound = this.unitsRound - Long.MIN_VALUE;
            this.maxRound = Long.MAX_VALUE - this.unitsRound;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.UniversalTimeScale.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.UniversalTimeScale.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.UniversalTimeScale.<clinit>():void");
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
        return universal.divide(units, (int) TO_MIN_VALUE).subtract(new BigDecimal(data.epochOffset));
    }

    public static BigDecimal toBigDecimal(BigDecimal universalTime, int timeScale) {
        TimeScaleData data = getTimeScaleData(timeScale);
        BigDecimal units = new BigDecimal(data.units);
        return universalTime.divide(units, (int) TO_MIN_VALUE).subtract(new BigDecimal(data.epochOffset));
    }

    private static TimeScaleData getTimeScaleData(int scale) {
        if (scale >= 0 && scale < MAX_SCALE) {
            return timeScaleTable[scale];
        }
        throw new IllegalArgumentException("scale out of range: " + scale);
    }

    public static long getTimeScaleValue(int scale, int value) {
        TimeScaleData data = getTimeScaleData(scale);
        switch (value) {
            case UNITS_VALUE /*0*/:
                return data.units;
            case UNIX_TIME /*1*/:
                return data.epochOffset;
            case ICU4C_TIME /*2*/:
                return data.fromMin;
            case WINDOWS_FILE_TIME /*3*/:
                return data.fromMax;
            case TO_MIN_VALUE /*4*/:
                return data.toMin;
            case TO_MAX_VALUE /*5*/:
                return data.toMax;
            case MAC_TIME /*6*/:
                return data.epochOffsetP1;
            case EXCEL_TIME /*7*/:
                return data.epochOffsetM1;
            case UNITS_ROUND_VALUE /*8*/:
                return data.unitsRound;
            case UNIX_MICROSECONDS_TIME /*9*/:
                return data.minRound;
            case MAX_SCALE /*10*/:
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
        return universalTime.divide(units, (int) UNIX_TIME).subtract(new BigDecimal(data.epochOffset));
    }
}
