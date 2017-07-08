package sun.net;

import java.net.URL;
import java.util.prefs.Preferences;

/* compiled from: ProgressMonitor */
class DefaultProgressMeteringPolicy implements ProgressMeteringPolicy {
    DefaultProgressMeteringPolicy() {
    }

    public boolean shouldMeterInput(URL url, String method) {
        return false;
    }

    public int getProgressUpdateThreshold() {
        return Preferences.MAX_VALUE_LENGTH;
    }
}
