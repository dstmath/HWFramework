package sun.net;

import java.util.EventListener;

public interface ProgressListener extends EventListener {
    void progressFinish(ProgressEvent progressEvent);

    void progressStart(ProgressEvent progressEvent);

    void progressUpdate(ProgressEvent progressEvent);
}
