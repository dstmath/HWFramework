package android.telephony.mbms;

import android.net.Uri;
import android.os.RemoteException;
import android.telephony.MbmsStreamingSession;
import android.telephony.mbms.vendor.IMbmsStreamingService;
import android.util.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class StreamingService implements AutoCloseable {
    public static final int BROADCAST_METHOD = 1;
    private static final String LOG_TAG = "MbmsStreamingService";
    public static final int REASON_BY_USER_REQUEST = 1;
    public static final int REASON_END_OF_SESSION = 2;
    public static final int REASON_FREQUENCY_CONFLICT = 3;
    public static final int REASON_LEFT_MBMS_BROADCAST_AREA = 6;
    public static final int REASON_NONE = 0;
    public static final int REASON_NOT_CONNECTED_TO_HOMECARRIER_LTE = 5;
    public static final int REASON_OUT_OF_MEMORY = 4;
    public static final int STATE_STALLED = 3;
    public static final int STATE_STARTED = 2;
    public static final int STATE_STOPPED = 1;
    public static final int UNICAST_METHOD = 2;
    private final InternalStreamingServiceCallback mCallback;
    private final MbmsStreamingSession mParentSession;
    private IMbmsStreamingService mService;
    private final StreamingServiceInfo mServiceInfo;
    private final int mSubscriptionId;

    @Retention(RetentionPolicy.SOURCE)
    public @interface StreamingState {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface StreamingStateChangeReason {
    }

    public StreamingService(int subscriptionId, IMbmsStreamingService service, MbmsStreamingSession session, StreamingServiceInfo streamingServiceInfo, InternalStreamingServiceCallback callback) {
        this.mSubscriptionId = subscriptionId;
        this.mParentSession = session;
        this.mService = service;
        this.mServiceInfo = streamingServiceInfo;
        this.mCallback = callback;
    }

    public Uri getPlaybackUri() {
        if (this.mService != null) {
            try {
                return this.mService.getPlaybackUri(this.mSubscriptionId, this.mServiceInfo.getServiceId());
            } catch (RemoteException e) {
                Log.w(LOG_TAG, "Remote process died");
                this.mService = null;
                this.mParentSession.onStreamingServiceStopped(this);
                sendErrorToApp(3, null);
                return null;
            }
        } else {
            throw new IllegalStateException("No streaming service attached");
        }
    }

    public StreamingServiceInfo getInfo() {
        return this.mServiceInfo;
    }

    public void close() {
        if (this.mService != null) {
            try {
                this.mService.stopStreaming(this.mSubscriptionId, this.mServiceInfo.getServiceId());
            } catch (RemoteException e) {
                Log.w(LOG_TAG, "Remote process died");
                this.mService = null;
                sendErrorToApp(3, null);
            } catch (Throwable th) {
                this.mParentSession.onStreamingServiceStopped(this);
                throw th;
            }
            this.mParentSession.onStreamingServiceStopped(this);
            return;
        }
        throw new IllegalStateException("No streaming service attached");
    }

    public InternalStreamingServiceCallback getCallback() {
        return this.mCallback;
    }

    private void sendErrorToApp(int errorCode, String message) {
        try {
            this.mCallback.onError(errorCode, message);
        } catch (RemoteException e) {
        }
    }
}
