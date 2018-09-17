package java.lang.invoke;

import java.lang.invoke.MethodHandles.Lookup;

public class LambdaMetafactory {
    public static final int FLAG_BRIDGES = 4;
    public static final int FLAG_MARKERS = 2;
    public static final int FLAG_SERIALIZABLE = 1;

    public static CallSite metafactory(Lookup caller, String invokedName, MethodType invokedType, MethodType samMethodType, MethodHandle implMethod, MethodType instantiatedMethodType) throws LambdaConversionException {
        return null;
    }

    public static CallSite altMetafactory(Lookup caller, String invokedName, MethodType invokedType, Object... args) throws LambdaConversionException {
        return null;
    }
}
