package android.net.wifi;

import android.annotation.SystemApi;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SystemApi
public abstract class EasyConnectStatusCallback {
    public static final int EASY_CONNECT_EVENT_FAILURE_AUTHENTICATION = -2;
    public static final int EASY_CONNECT_EVENT_FAILURE_BUSY = -5;
    public static final int EASY_CONNECT_EVENT_FAILURE_CONFIGURATION = -4;
    public static final int EASY_CONNECT_EVENT_FAILURE_GENERIC = -7;
    public static final int EASY_CONNECT_EVENT_FAILURE_INVALID_NETWORK = -9;
    public static final int EASY_CONNECT_EVENT_FAILURE_INVALID_URI = -1;
    public static final int EASY_CONNECT_EVENT_FAILURE_NOT_COMPATIBLE = -3;
    public static final int EASY_CONNECT_EVENT_FAILURE_NOT_SUPPORTED = -8;
    public static final int EASY_CONNECT_EVENT_FAILURE_TIMEOUT = -6;
    public static final int EASY_CONNECT_EVENT_PROGRESS_AUTHENTICATION_SUCCESS = 0;
    public static final int EASY_CONNECT_EVENT_PROGRESS_RESPONSE_PENDING = 1;
    public static final int EASY_CONNECT_EVENT_SUCCESS_CONFIGURATION_SENT = 0;

    @Retention(RetentionPolicy.SOURCE)
    public @interface EasyConnectFailureStatusCode {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface EasyConnectProgressStatusCode {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface EasyConnectSuccessStatusCode {
    }

    public abstract void onConfiguratorSuccess(int i);

    public abstract void onEnrolleeSuccess(int i);

    public abstract void onFailure(int i);

    public abstract void onProgress(int i);
}
