package ohos.media.common;

import java.util.Arrays;
import java.util.Vector;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.Dimension;
import ohos.utils.Pair;
import ohos.utils.RationalNumber;
import ohos.utils.Scope;

public class Utils {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(Utils.class);

    public static <T extends Comparable<? super T>> void sortDistinctRanges(Scope<T>[] scopeArr) {
        Arrays.sort(scopeArr, $$Lambda$Utils$PsM771N7ATGuf5gQJM33kBHBow.INSTANCE);
    }

    static /* synthetic */ int lambda$sortDistinctRanges$0(Scope scope, Scope scope2) {
        if (scope.getUpper().compareTo(scope2.getLower()) < 0) {
            return -1;
        }
        if (scope.getLower().compareTo(scope2.getUpper()) > 0) {
            return 1;
        }
        LOGGER.error("sortDistinctRanges ranges invalid", new Object[0]);
        throw new IllegalArgumentException("ranges must be distinct (" + scope + " and " + scope2 + ")");
    }

    public static <T extends Comparable<? super T>> Scope<T>[] intersectSortedRanges(Scope<T>[] scopeArr, Scope<T>[] scopeArr2) {
        Vector vector = new Vector();
        int length = scopeArr2.length;
        int i = 0;
        for (int i2 = 0; i2 < length; i2++) {
            Scope<T> scope = scopeArr2[i2];
            while (i < scopeArr.length && scopeArr[i].getUpper().compareTo(scope.getLower()) < 0) {
                i++;
            }
            while (i < scopeArr.length && scopeArr[i].getUpper().compareTo(scope.getUpper()) < 0) {
                vector.add(scope.intersect(scopeArr[i]));
                i++;
            }
            if (i == scopeArr.length) {
                break;
            }
            if (scopeArr[i].getLower().compareTo(scope.getUpper()) <= 0) {
                vector.add(scope.intersect(scopeArr[i]));
            }
        }
        return (Scope[]) vector.toArray(new Scope[vector.size()]);
    }

    public static <T extends Comparable<? super T>> int searchDistinctRanges(Scope<T>[] scopeArr, T t) {
        return Arrays.binarySearch(scopeArr, Scope.create(t, t), $$Lambda$Utils$BP9kvz2pmpQRF6efyo9ZCGoqHo.INSTANCE);
    }

    static /* synthetic */ int lambda$searchDistinctRanges$1(Scope scope, Scope scope2) {
        if (scope.getUpper().compareTo(scope2.getLower()) < 0) {
            return -1;
        }
        return scope.getLower().compareTo(scope2.getUpper()) > 0 ? 1 : 0;
    }

    private static int caculateGreatCommonDivisor(int i, int i2) {
        if (i == 0 && i2 == 0) {
            return 1;
        }
        if (i < 0) {
            i = -i;
        }
        if (i2 < 0) {
            i2 = -i2;
        }
        while (true) {
            i2 = i;
            if (i2 == 0) {
                return i2;
            }
            i = i2 % i2;
        }
    }

    public static Scope<Integer> factorRange(Scope<Integer> scope, int i) {
        return i == 1 ? scope : Scope.create(Integer.valueOf(divUp(scope.getLower().intValue(), i)), Integer.valueOf(scope.getUpper().intValue() / i));
    }

    public static Scope<Long> factorRange(Scope<Long> scope, long j) {
        return j == 1 ? scope : Scope.create(Long.valueOf(divUp(scope.getLower().longValue(), j)), Long.valueOf(scope.getUpper().longValue() / j));
    }

    private static RationalNumber scaleRatio(RationalNumber rationalNumber, int i, int i2) {
        int caculateGreatCommonDivisor = caculateGreatCommonDivisor(i, i2);
        return new RationalNumber((int) (((double) rationalNumber.getNumerator()) * ((double) (i / caculateGreatCommonDivisor))), (int) (((double) rationalNumber.getDenominator()) * ((double) (i2 / caculateGreatCommonDivisor))));
    }

    public static Scope<RationalNumber> scaleRange(Scope<RationalNumber> scope, int i, int i2) {
        return i == i2 ? scope : Scope.create(scaleRatio(scope.getLower(), i, i2), scaleRatio(scope.getUpper(), i, i2));
    }

    public static Scope<Integer> alignRange(Scope<Integer> scope, int i) {
        return scope.intersect(Integer.valueOf(divUp(scope.getLower().intValue(), i) * i), Integer.valueOf((scope.getUpper().intValue() / i) * i));
    }

    public static int divUp(int i, int i2) {
        return ((i + i2) - 1) / i2;
    }

    public static long divUp(long j, long j2) {
        return ((j + j2) - 1) / j2;
    }

    public static Scope<Integer> makeIntRange(double d) {
        return Scope.create(Integer.valueOf((int) d), Integer.valueOf((int) Math.ceil(d)));
    }

    public static Scope<Long> makeLongRange(double d) {
        return Scope.create(Long.valueOf((long) d), Long.valueOf((long) Math.ceil(d)));
    }

    public static Dimension parseDimension(Object obj, Dimension dimension) {
        try {
            if (!(obj instanceof String)) {
                return dimension;
            }
            return Dimension.parseDimension(((String) obj).replace("x", "*"));
        } catch (ClassCastException | NumberFormatException unused) {
            return dimension;
        }
    }

    public static int parseInt(Object obj, int i) {
        try {
            if (!(obj instanceof String)) {
                return i;
            }
            return Integer.parseInt((String) obj);
        } catch (ClassCastException | NumberFormatException unused) {
            return i;
        }
    }

    public static Scope<Integer> parseIntRange(Object obj, Scope<Integer> scope) {
        try {
            if (!(obj instanceof String)) {
                return scope;
            }
            String str = (String) obj;
            int indexOf = str.indexOf(45);
            if (indexOf >= 0) {
                return Scope.create(Integer.valueOf(Integer.parseInt(str.substring(0, indexOf), 10)), Integer.valueOf(Integer.parseInt(str.substring(indexOf + 1), 10)));
            }
            int parseInt = Integer.parseInt(str);
            return Scope.create(Integer.valueOf(parseInt), Integer.valueOf(parseInt));
        } catch (ClassCastException | NumberFormatException unused) {
            return scope;
        }
    }

    public static Scope<Long> parseLongRange(Object obj, Scope<Long> scope) {
        try {
            if (!(obj instanceof String)) {
                return scope;
            }
            String str = (String) obj;
            int indexOf = str.indexOf(45);
            if (indexOf >= 0) {
                return Scope.create(Long.valueOf(Long.parseLong(str.substring(0, indexOf), 10)), Long.valueOf(Long.parseLong(str.substring(indexOf + 1), 10)));
            }
            long parseLong = Long.parseLong(str);
            return Scope.create(Long.valueOf(parseLong), Long.valueOf(parseLong));
        } catch (ClassCastException | NumberFormatException unused) {
            return scope;
        }
    }

    public static Scope<RationalNumber> parseRationalRange(Object obj, Scope<RationalNumber> scope) {
        try {
            if (!(obj instanceof String)) {
                return scope;
            }
            String str = (String) obj;
            int indexOf = str.indexOf(45);
            if (indexOf >= 0) {
                return Scope.create(RationalNumber.createRationalFromString(str.substring(0, indexOf)), RationalNumber.createRationalFromString(str.substring(indexOf + 1)));
            }
            RationalNumber createRationalFromString = RationalNumber.createRationalFromString(str);
            return Scope.create(createRationalFromString, createRationalFromString);
        } catch (ClassCastException | NumberFormatException unused) {
            return scope;
        }
    }

    public static Pair<Dimension, Dimension> parseDimensionRange(Object obj) {
        try {
            if (!(obj instanceof String)) {
                return null;
            }
            String replace = ((String) obj).replace("x", "*");
            int indexOf = replace.indexOf(45);
            if (indexOf >= 0) {
                return Pair.create(Dimension.parseDimension(replace.substring(0, indexOf)), Dimension.parseDimension(replace.substring(indexOf + 1)));
            }
            Dimension parseDimension = Dimension.parseDimension(replace);
            return Pair.create(parseDimension, parseDimension);
        } catch (ClassCastException | IllegalArgumentException unused) {
            return null;
        }
    }
}
