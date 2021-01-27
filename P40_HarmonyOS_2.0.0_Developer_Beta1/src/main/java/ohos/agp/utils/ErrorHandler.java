package ohos.agp.utils;

import java.util.function.Predicate;

public class ErrorHandler {
    private ErrorHandler() {
    }

    public static void handleInvalidParams(String str) {
        throw new IllegalArgumentException(str);
    }

    public static <T> boolean validateParam(T t, Predicate<T> predicate, String str) {
        boolean test = predicate.test(t);
        if (!test) {
            handleInvalidParams(str);
        }
        return test;
    }

    public static <T> boolean validateParamNotNull(T t) {
        return validateParam(t, $$Lambda$ErrorHandler$wemGins1JBTOa6vBYK6EDLxj9Ys.INSTANCE, "argument can't be null");
    }

    static /* synthetic */ boolean lambda$validateParamNonNegative$0(Integer num) {
        return num.intValue() >= 0;
    }

    public static boolean validateParamNonNegative(int i) {
        return validateParam(Integer.valueOf(i), $$Lambda$ErrorHandler$ZsalQwrgs48COf_o9Q79yKWmDwU.INSTANCE, "the value can't be negative");
    }

    static /* synthetic */ boolean lambda$validateParamNonNegative$1(Long l) {
        return l.longValue() >= 0;
    }

    public static boolean validateParamNonNegative(long j) {
        return validateParam(Long.valueOf(j), $$Lambda$ErrorHandler$mEZKZ60GINnv90TjJ6GRxSzdq0U.INSTANCE, "the value can't be negative");
    }

    static /* synthetic */ boolean lambda$validateParamNonNegative$2(float f, Float f2) {
        return f2.floatValue() > (-f);
    }

    public static boolean validateParamNonNegative(float f) {
        return validateParam(Float.valueOf(f), new Predicate(1.0E-8f) {
            /* class ohos.agp.utils.$$Lambda$ErrorHandler$Lxj5XnIAGpZ1L2jeXOPChm8X8 */
            private final /* synthetic */ float f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return ErrorHandler.lambda$validateParamNonNegative$2(this.f$0, (Float) obj);
            }
        }, "the value can't be negative");
    }

    static /* synthetic */ boolean lambda$validateParamIsNaturalNumber$3(Integer num) {
        return num.intValue() > 0;
    }

    public static boolean validateParamIsNaturalNumber(int i) {
        return validateParam(Integer.valueOf(i), $$Lambda$ErrorHandler$fOPZ0v21kBYvORyaTmoJX7T6NEk.INSTANCE, "the value should be > 0");
    }

    static /* synthetic */ boolean lambda$validateParamIsNaturalNumber$4(Long l) {
        return l.longValue() > 0;
    }

    public static boolean validateParamIsNaturalNumber(long j) {
        return validateParam(Long.valueOf(j), $$Lambda$ErrorHandler$t6gkKyyBkQtZzWx6k7z_X9331Z8.INSTANCE, "the value should be > 0");
    }
}
