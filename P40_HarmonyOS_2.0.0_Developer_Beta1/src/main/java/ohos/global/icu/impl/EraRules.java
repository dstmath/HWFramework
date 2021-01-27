package ohos.global.icu.impl;

import java.util.Arrays;
import ohos.global.icu.util.ICUException;
import ohos.global.icu.util.TimeZone;
import ohos.global.icu.util.UResourceBundle;
import ohos.global.icu.util.UResourceBundleIterator;

public class EraRules {
    private static final int DAY_MASK = 255;
    private static final int MAX_ENCODED_START_YEAR = 32767;
    public static final int MIN_ENCODED_START = encodeDate(MIN_ENCODED_START_YEAR, 1, 1);
    private static final int MIN_ENCODED_START_YEAR = -32768;
    private static final int MONTH_MASK = 65280;
    private static final int YEAR_MASK = -65536;
    private int currentEra;
    private int numEras;
    private int[] startDates;

    private static int encodeDate(int i, int i2, int i3) {
        return (i << 16) | (i2 << 8) | i3;
    }

    private static boolean isSet(int i) {
        return i != 0;
    }

    private static boolean isValidRuleStartDate(int i, int i2, int i3) {
        return i >= MIN_ENCODED_START_YEAR && i <= MAX_ENCODED_START_YEAR && i2 >= 1 && i2 <= 12 && i3 >= 1 && i3 <= 31;
    }

    private EraRules(int[] iArr, int i) {
        this.startDates = iArr;
        this.numEras = i;
        initCurrentEra();
    }

    public static EraRules getInstance(CalType calType, boolean z) {
        UResourceBundle uResourceBundle = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("calendarData").get(calType.getId()).get("eras");
        int size = uResourceBundle.getSize();
        int[] iArr = new int[size];
        UResourceBundleIterator iterator = uResourceBundle.getIterator();
        int i = Integer.MAX_VALUE;
        while (iterator.hasNext()) {
            UResourceBundle next = iterator.next();
            String key = next.getKey();
            try {
                int parseInt = Integer.parseInt(key);
                if (parseInt < 0 || parseInt >= size) {
                    throw new ICUException("Era rule key:" + key + " in era rule data for " + calType.getId() + " must be in range [0, " + (size - 1) + "]");
                } else if (!isSet(iArr[parseInt])) {
                    UResourceBundleIterator iterator2 = next.getIterator();
                    boolean z2 = true;
                    boolean z3 = false;
                    while (iterator2.hasNext()) {
                        UResourceBundle next2 = iterator2.next();
                        String key2 = next2.getKey();
                        if (key2.equals("start")) {
                            int[] intVector = next2.getIntVector();
                            if (intVector.length != 3 || !isValidRuleStartDate(intVector[0], intVector[1], intVector[2])) {
                                throw new ICUException("Invalid era rule date data:" + Arrays.toString(intVector) + " in era rule data for " + calType.getId());
                            }
                            iArr[parseInt] = encodeDate(intVector[0], intVector[1], intVector[2]);
                        } else if (key2.equals("named")) {
                            if (next2.getString().equals("false")) {
                                z2 = false;
                            }
                        } else if (key2.equals("end")) {
                            z3 = true;
                        }
                    }
                    if (!isSet(iArr[parseInt])) {
                        if (!z3) {
                            throw new ICUException("Missing era start/end rule date for key:" + key + " in era rule data for " + calType.getId());
                        } else if (parseInt == 0) {
                            iArr[parseInt] = MIN_ENCODED_START;
                        } else {
                            throw new ICUException("Era data for " + key + " in era rule data for " + calType.getId() + " has only end rule.");
                        }
                    }
                    if (z2) {
                        if (parseInt >= i) {
                            throw new ICUException("Non-tentative era(" + parseInt + ") must be placed before the first tentative era");
                        }
                    } else if (parseInt < i) {
                        i = parseInt;
                    }
                } else {
                    throw new ICUException("Dupulicated era rule for rule key:" + key + " in era rule data for " + calType.getId());
                }
            } catch (NumberFormatException unused) {
                throw new ICUException("Invald era rule key:" + key + " in era rule data for " + calType.getId());
            }
        }
        if (i >= Integer.MAX_VALUE || z) {
            return new EraRules(iArr, size);
        }
        return new EraRules(iArr, i);
    }

    public int getNumberOfEras() {
        return this.numEras;
    }

    public int[] getStartDate(int i, int[] iArr) {
        if (i >= 0 && i < this.numEras) {
            return decodeDate(this.startDates[i], iArr);
        }
        throw new IllegalArgumentException("eraIdx is out of range");
    }

    public int getStartYear(int i) {
        if (i >= 0 && i < this.numEras) {
            return decodeDate(this.startDates[i], null)[0];
        }
        throw new IllegalArgumentException("eraIdx is out of range");
    }

    public int getEraIndex(int i, int i2, int i3) {
        if (i2 < 1 || i2 > 12 || i3 < 1 || i3 > 31) {
            throw new IllegalArgumentException("Illegal date - year:" + i + "month:" + i2 + "day:" + i3);
        }
        int i4 = this.numEras;
        int currentEraIndex = compareEncodedDateWithYMD(this.startDates[getCurrentEraIndex()], i, i2, i3) <= 0 ? getCurrentEraIndex() : 0;
        while (currentEraIndex < i4 - 1) {
            int i5 = (currentEraIndex + i4) / 2;
            if (compareEncodedDateWithYMD(this.startDates[i5], i, i2, i3) <= 0) {
                currentEraIndex = i5;
            } else {
                i4 = i5;
            }
        }
        return currentEraIndex;
    }

    public int getCurrentEraIndex() {
        return this.currentEra;
    }

    private void initCurrentEra() {
        long currentTimeMillis = System.currentTimeMillis();
        int[] timeToFields = Grego.timeToFields(currentTimeMillis + ((long) TimeZone.getDefault().getOffset(currentTimeMillis)), null);
        int encodeDate = encodeDate(timeToFields[0], timeToFields[1] + 1, timeToFields[2]);
        int i = this.numEras - 1;
        while (i > 0 && encodeDate < this.startDates[i]) {
            i--;
        }
        this.currentEra = i;
    }

    private static int[] decodeDate(int i, int[] iArr) {
        int i2;
        int i3;
        int i4;
        if (i == MIN_ENCODED_START) {
            i2 = Integer.MIN_VALUE;
            i4 = 1;
            i3 = 1;
        } else {
            i3 = (MONTH_MASK & i) >> 8;
            i4 = i & 255;
            i2 = (-65536 & i) >> 16;
        }
        if (iArr == null || iArr.length < 3) {
            return new int[]{i2, i3, i4};
        }
        iArr[0] = i2;
        iArr[1] = i3;
        iArr[2] = i4;
        return iArr;
    }

    private static int compareEncodedDateWithYMD(int i, int i2, int i3, int i4) {
        int encodeDate;
        if (i2 < MIN_ENCODED_START_YEAR) {
            if (i == MIN_ENCODED_START) {
                return (i2 > Integer.MIN_VALUE || i3 > 1 || i4 > 1) ? -1 : 0;
            }
            return 1;
        } else if (i2 <= MAX_ENCODED_START_YEAR && i >= (encodeDate = encodeDate(i2, i3, i4))) {
            return i == encodeDate ? 0 : 1;
        } else {
            return -1;
        }
    }
}
