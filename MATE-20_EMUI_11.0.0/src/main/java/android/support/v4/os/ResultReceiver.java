package android.support.v4.os;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.annotation.RestrictTo;
import android.support.v4.os.IResultReceiver;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public class ResultReceiver implements Parcelable {
    public static final Parcelable.Creator<ResultReceiver> CREATOR = new Parcelable.Creator<ResultReceiver>() {
        /* class android.support.v4.os.ResultReceiver.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ResultReceiver createFromParcel(Parcel in) {
            return new ResultReceiver(in);
        }

        @Override // android.os.Parcelable.Creator
        public ResultReceiver[] newArray(int size) {
            return new ResultReceiver[size];
        }
    };
    final Handler mHandler;
    final boolean mLocal;
    IResultReceiver mReceiver;

    class MyRunnable implements Runnable {
        final int mResultCode;
        final Bundle mResultData;

        MyRunnable(int resultCode, Bundle resultData) {
            this.mResultCode = resultCode;
            this.mResultData = resultData;
        }

        @Override // java.lang.Runnable
        public void run() {
            ResultReceiver.this.onReceiveResult(this.mResultCode, this.mResultData);
        }
    }

    class MyResultReceiver extends IResultReceiver.Stub {
        MyResultReceiver() {
        }

        @Override // android.support.v4.os.IResultReceiver
        public void send(int resultCode, Bundle resultData) {
            if (ResultReceiver.this.mHandler != null) {
                ResultReceiver.this.mHandler.post(new MyRunnable(resultCode, resultData));
            } else {
                ResultReceiver.this.onReceiveResult(resultCode, resultData);
            }
        }
    }

    public ResultReceiver(Handler handler) {
        this.mLocal = true;
        this.mHandler = handler;
    }

    public void send(int resultCode, Bundle resultData) {
        if (this.mLocal) {
            if (this.mHandler != null) {
                this.mHandler.post(new MyRunnable(resultCode, resultData));
            } else {
                onReceiveResult(resultCode, resultData);
            }
        } else if (this.mReceiver != null) {
            try {
                this.mReceiver.send(resultCode, resultData);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onReceiveResult(int resultCode, Bundle resultData) {
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        synchronized (this) {
            if (this.mReceiver == null) {
                this.mReceiver = new MyResultReceiver();
            }
            out.writeStrongBinder(this.mReceiver.asBinder());
        }
    }

    ResultReceiver(Parcel in) {
        this.mLocal = false;
        this.mHandler = null;
        this.mReceiver = IResultReceiver.Stub.asInterface(in.readStrongBinder());
    }
}
