package android.hardware.camera2.utils;

public class UncheckedThrow {
    public static void throwAnyException(Exception e) {
        throwAnyImpl(e);
    }

    public static void throwAnyException(Throwable e) {
        throwAnyImpl(e);
    }

    private static <T extends Throwable> void throwAnyImpl(Throwable e) throws Throwable {
        throw e;
    }
}
