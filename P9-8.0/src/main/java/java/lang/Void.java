package java.lang;

public final class Void {
    public static final Class<Void> TYPE = lookupType();

    private static native Class<Void> lookupType();

    private Void() {
    }
}
