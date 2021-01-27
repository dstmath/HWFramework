package ohos.media.photokit.common;

import ohos.utils.net.Uri;

public interface AVLoggerConnectionClient {
    void onLogCompleted(String str, Uri uri);

    void onLoggerConnected();
}
