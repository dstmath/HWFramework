package android.app;

import android.content.Context;

public interface HwKeyguardManager {
    public static final String ACTION_KEYGUARD_UNLOCK = "android.intent.action.KEYGUARD_UNLOCK";

    boolean isLockScreenDisabled(Context context);
}
