package android.text;

public class Hyphenator {
    private static native void nInit();

    public static void init() {
        nInit();
    }
}
