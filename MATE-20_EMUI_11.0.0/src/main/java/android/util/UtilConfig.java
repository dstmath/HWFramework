package android.util;

public class UtilConfig {
    static boolean sThrowExceptionForUpperArrayOutOfBounds = true;

    public static void setThrowExceptionForUpperArrayOutOfBounds(boolean check) {
        sThrowExceptionForUpperArrayOutOfBounds = check;
    }
}
