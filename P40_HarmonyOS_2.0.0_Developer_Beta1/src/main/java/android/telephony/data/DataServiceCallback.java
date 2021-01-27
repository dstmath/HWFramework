package android.telephony.data;

import android.annotation.SystemApi;
import android.os.RemoteException;
import android.telephony.Rlog;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.List;

@SystemApi
public class DataServiceCallback {
    public static final int RESULT_ERROR_BUSY = 3;
    public static final int RESULT_ERROR_ILLEGAL_STATE = 4;
    public static final int RESULT_ERROR_INVALID_ARG = 2;
    public static final int RESULT_ERROR_UNSUPPORTED = 1;
    public static final int RESULT_SUCCESS = 0;
    private static final String TAG = DataServiceCallback.class.getSimpleName();
    private final WeakReference<IDataServiceCallback> mCallback;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ResultCode {
    }

    public DataServiceCallback(IDataServiceCallback callback) {
        this.mCallback = new WeakReference<>(callback);
    }

    public void onSetupDataCallComplete(int result, DataCallResponse response) {
        IDataServiceCallback callback = this.mCallback.get();
        if (callback != null) {
            try {
                callback.onSetupDataCallComplete(result, response);
            } catch (RemoteException e) {
                Rlog.e(TAG, "Failed to onSetupDataCallComplete on the remote");
            }
        }
    }

    public void onDeactivateDataCallComplete(int result) {
        IDataServiceCallback callback = this.mCallback.get();
        if (callback != null) {
            try {
                callback.onDeactivateDataCallComplete(result);
            } catch (RemoteException e) {
                Rlog.e(TAG, "Failed to onDeactivateDataCallComplete on the remote");
            }
        }
    }

    public void onSetInitialAttachApnComplete(int result) {
        IDataServiceCallback callback = this.mCallback.get();
        if (callback != null) {
            try {
                callback.onSetInitialAttachApnComplete(result);
            } catch (RemoteException e) {
                Rlog.e(TAG, "Failed to onSetInitialAttachApnComplete on the remote");
            }
        }
    }

    public void onSetDataProfileComplete(int result) {
        IDataServiceCallback callback = this.mCallback.get();
        if (callback != null) {
            try {
                callback.onSetDataProfileComplete(result);
            } catch (RemoteException e) {
                Rlog.e(TAG, "Failed to onSetDataProfileComplete on the remote");
            }
        }
    }

    public void onRequestDataCallListComplete(int result, List<DataCallResponse> dataCallList) {
        IDataServiceCallback callback = this.mCallback.get();
        if (callback != null) {
            try {
                callback.onRequestDataCallListComplete(result, dataCallList);
            } catch (RemoteException e) {
                Rlog.e(TAG, "Failed to onRequestDataCallListComplete on the remote");
            }
        }
    }

    public void onDataCallListChanged(List<DataCallResponse> dataCallList) {
        IDataServiceCallback callback = this.mCallback.get();
        if (callback != null) {
            try {
                callback.onDataCallListChanged(dataCallList);
            } catch (RemoteException e) {
                Rlog.e(TAG, "Failed to onDataCallListChanged on the remote");
            }
        }
    }
}
