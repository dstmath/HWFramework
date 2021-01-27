package com.android.internal.telephony;

import android.os.Handler;
import java.util.concurrent.atomic.AtomicBoolean;

public interface ISmsUsageMonitorInner {
    int checkDestination(String str, String str2);

    AtomicBoolean getmCheckEnabledHw();

    Handler getmSettingsObserverHandlerHw();
}
