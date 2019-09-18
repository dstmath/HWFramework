package android.os;

import android.annotation.SystemApi;
import android.os.IRemoteCallback;
import android.os.Parcelable;

@SystemApi
public final class RemoteCallback implements Parcelable {
    public static final Parcelable.Creator<RemoteCallback> CREATOR = new Parcelable.Creator<RemoteCallback>() {
        public RemoteCallback createFromParcel(Parcel parcel) {
            return new RemoteCallback(parcel);
        }

        public RemoteCallback[] newArray(int size) {
            return new RemoteCallback[size];
        }
    };
    private final IRemoteCallback mCallback;
    private final Handler mHandler;
    /* access modifiers changed from: private */
    public final OnResultListener mListener;

    public interface OnResultListener {
        void onResult(Bundle bundle);
    }

    public RemoteCallback(OnResultListener listener) {
        this(listener, null);
    }

    public RemoteCallback(OnResultListener listener, Handler handler) {
        if (listener != null) {
            this.mListener = listener;
            this.mHandler = handler;
            this.mCallback = new IRemoteCallback.Stub() {
                public void sendResult(Bundle data) {
                    RemoteCallback.this.sendResult(data);
                }
            };
            return;
        }
        throw new NullPointerException("listener cannot be null");
    }

    RemoteCallback(Parcel parcel) {
        this.mListener = null;
        this.mHandler = null;
        this.mCallback = IRemoteCallback.Stub.asInterface(parcel.readStrongBinder());
    }

    public void sendResult(final Bundle result) {
        if (this.mListener == null) {
            try {
                this.mCallback.sendResult(result);
            } catch (RemoteException e) {
            }
        } else if (this.mHandler != null) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    RemoteCallback.this.mListener.onResult(result);
                }
            });
        } else {
            this.mListener.onResult(result);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeStrongBinder(this.mCallback.asBinder());
    }
}
