package android.os;

import android.annotation.SystemApi;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SystemApi
public final class BugreportParams {
    public static final int BUGREPORT_MODE_FULL = 0;
    public static final int BUGREPORT_MODE_INTERACTIVE = 1;
    public static final int BUGREPORT_MODE_REMOTE = 2;
    public static final int BUGREPORT_MODE_TELEPHONY = 4;
    public static final int BUGREPORT_MODE_WEAR = 3;
    public static final int BUGREPORT_MODE_WIFI = 5;
    private final int mMode;

    @Retention(RetentionPolicy.SOURCE)
    public @interface BugreportMode {
    }

    public BugreportParams(int mode) {
        this.mMode = mode;
    }

    public int getMode() {
        return this.mMode;
    }
}
