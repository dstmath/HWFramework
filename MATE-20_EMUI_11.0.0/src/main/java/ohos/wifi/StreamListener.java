package ohos.wifi;

import ohos.annotation.SystemApi;

@SystemApi
public interface StreamListener {
    public static final int STREAM_BIDIRECTIONAL = 3;
    public static final int STREAM_DOWN = 1;
    public static final int STREAM_NONE = 0;
    public static final int STREAM_UP = 2;

    void onStreamChanged(int i);
}
