package android.text;

public class Hyphenator {
    private static native void nInit();

    private Hyphenator() {
    }

    public static void init() {
        nInit();
    }
}
