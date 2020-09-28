package android.os;

import android.annotation.SystemApi;
import android.os.IRemoteCallback;
import android.os.Parcelable;

@SystemApi
public final class RemoteCallback implements Parcelable {
    public static final Parcelable.Creator<RemoteCallback> CREATOR = new Parcelable.Creator<RemoteCallback>() {
        /* class android.os.RemoteCallback.AnonymousClass3 */

        @Override // android.os.Parcelable.Creator
        public RemoteCallback createFromParcel(Parcel parcel) {
            return new RemoteCallback(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RemoteCallback[] newArray(int size) {
            return new RemoteCallback[size];
        }
    };
    private final IRemoteCallback mCallback;
    private final Handler mHandler;
    private final OnResultListener mListener;

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
                /* class android.os.RemoteCallback.AnonymousClass1 */

                @Override // android.os.IRemoteCallback
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
        OnResultListener onResultListener = this.mListener;
        if (onResultListener != null) {
            Handler handler = this.mHandler;
            if (handler != null) {
                handler.post(new Runnable() {
                    /* class android.os.RemoteCallback.AnonymousClass2 */

                    public void run() {
                        RemoteCallback.this.mListener.onResult(result);
                    }
                });
            } else {
                onResultListener.onResult(result);
            }
        } else {
            try {
                this.mCallback.sendResult(result);
            } catch (RemoteException e) {
            }
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeStrongBinder(this.mCallback.asBinder());
    }
}
