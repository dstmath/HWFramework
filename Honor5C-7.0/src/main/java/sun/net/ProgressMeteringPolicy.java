package sun.net;

import java.net.URL;

public interface ProgressMeteringPolicy {
    int getProgressUpdateThreshold();

    boolean shouldMeterInput(URL url, String str);
}
