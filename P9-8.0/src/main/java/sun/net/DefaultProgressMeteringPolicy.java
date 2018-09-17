package sun.net;

import java.net.URL;

/* compiled from: ProgressMonitor */
class DefaultProgressMeteringPolicy implements ProgressMeteringPolicy {
    DefaultProgressMeteringPolicy() {
    }

    public boolean shouldMeterInput(URL url, String method) {
        return false;
    }

    public int getProgressUpdateThreshold() {
        return 8192;
    }
}
