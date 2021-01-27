package android.telephony;

import android.annotation.SystemApi;
import android.os.RemoteException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

@SystemApi
public class NetworkServiceCallback {
    public static final int RESULT_ERROR_BUSY = 3;
    public static final int RESULT_ERROR_FAILED = 5;
    public static final int RESULT_ERROR_ILLEGAL_STATE = 4;
    public static final int RESULT_ERROR_INVALID_ARG = 2;
    public static final int RESULT_ERROR_UNSUPPORTED = 1;
    public static final int RESULT_SUCCESS = 0;
    private static final String mTag = NetworkServiceCallback.class.getSimpleName();
    private final WeakReference<INetworkServiceCallback> mCallback;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Result {
    }

    public NetworkServiceCallback(INetworkServiceCallback callback) {
        this.mCallback = new WeakReference<>(callback);
    }

    public void onRequestNetworkRegistrationInfoComplete(int result, NetworkRegistrationInfo state) {
        INetworkServiceCallback callback = this.mCallback.get();
        if (callback != null) {
            try {
                callback.onRequestNetworkRegistrationInfoComplete(result, state);
            } catch (RemoteException e) {
                Rlog.e(mTag, "Failed to onRequestNetworkRegistrationInfoComplete on the remote");
            }
        } else {
            Rlog.e(mTag, "Weak reference of callback is null.");
        }
    }
}
