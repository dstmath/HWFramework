package android.content;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.IActivityManager;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.AndroidException;

public class IntentSender implements Parcelable {
    public static final Parcelable.Creator<IntentSender> CREATOR = new Parcelable.Creator<IntentSender>() {
        /* class android.content.IntentSender.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IntentSender createFromParcel(Parcel in) {
            IBinder target = in.readStrongBinder();
            if (target != null) {
                return new IntentSender(target);
            }
            return null;
        }

        @Override // android.os.Parcelable.Creator
        public IntentSender[] newArray(int size) {
            return new IntentSender[size];
        }
    };
    @UnsupportedAppUsage
    private final IIntentSender mTarget;
    IBinder mWhitelistToken;

    public interface OnFinished {
        void onSendFinished(IntentSender intentSender, Intent intent, int i, String str, Bundle bundle);
    }

    public static class SendIntentException extends AndroidException {
        public SendIntentException() {
        }

        public SendIntentException(String name) {
            super(name);
        }

        public SendIntentException(Exception cause) {
            super(cause);
        }
    }

    /* access modifiers changed from: private */
    public static class FinishedDispatcher extends IIntentReceiver.Stub implements Runnable {
        private final Handler mHandler;
        private Intent mIntent;
        private final IntentSender mIntentSender;
        private int mResultCode;
        private String mResultData;
        private Bundle mResultExtras;
        private final OnFinished mWho;

        FinishedDispatcher(IntentSender pi, OnFinished who, Handler handler) {
            this.mIntentSender = pi;
            this.mWho = who;
            this.mHandler = handler;
        }

        @Override // android.content.IIntentReceiver
        public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean serialized, boolean sticky, int sendingUser) {
            this.mIntent = intent;
            this.mResultCode = resultCode;
            this.mResultData = data;
            this.mResultExtras = extras;
            Handler handler = this.mHandler;
            if (handler == null) {
                run();
            } else {
                handler.post(this);
            }
        }

        public void run() {
            this.mWho.onSendFinished(this.mIntentSender, this.mIntent, this.mResultCode, this.mResultData, this.mResultExtras);
        }
    }

    public void sendIntent(Context context, int code, Intent intent, OnFinished onFinished, Handler handler) throws SendIntentException {
        sendIntent(context, code, intent, onFinished, handler, null);
    }

    public void sendIntent(Context context, int code, Intent intent, OnFinished onFinished, Handler handler, String requiredPermission) throws SendIntentException {
        String resolvedType;
        FinishedDispatcher finishedDispatcher;
        if (intent != null) {
            try {
                resolvedType = intent.resolveTypeIfNeeded(context.getContentResolver());
            } catch (RemoteException e) {
                throw new SendIntentException();
            }
        } else {
            resolvedType = null;
        }
        IActivityManager service = ActivityManager.getService();
        IIntentSender iIntentSender = this.mTarget;
        IBinder iBinder = this.mWhitelistToken;
        if (onFinished != null) {
            try {
                finishedDispatcher = new FinishedDispatcher(this, onFinished, handler);
            } catch (RemoteException e2) {
                throw new SendIntentException();
            }
        } else {
            finishedDispatcher = null;
        }
        if (service.sendIntentSender(iIntentSender, iBinder, code, intent, resolvedType, finishedDispatcher, requiredPermission, null) < 0) {
            throw new SendIntentException();
        }
    }

    @Deprecated
    public String getTargetPackage() {
        try {
            return ActivityManager.getService().getPackageForIntentSender(this.mTarget);
        } catch (RemoteException e) {
            return null;
        }
    }

    public String getCreatorPackage() {
        try {
            return ActivityManager.getService().getPackageForIntentSender(this.mTarget);
        } catch (RemoteException e) {
            return null;
        }
    }

    public int getCreatorUid() {
        try {
            return ActivityManager.getService().getUidForIntentSender(this.mTarget);
        } catch (RemoteException e) {
            return -1;
        }
    }

    public UserHandle getCreatorUserHandle() {
        try {
            int uid = ActivityManager.getService().getUidForIntentSender(this.mTarget);
            if (uid > 0) {
                return new UserHandle(UserHandle.getUserId(uid));
            }
            return null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public boolean equals(Object otherObj) {
        if (otherObj instanceof IntentSender) {
            return this.mTarget.asBinder().equals(((IntentSender) otherObj).mTarget.asBinder());
        }
        return false;
    }

    public int hashCode() {
        return this.mTarget.asBinder().hashCode();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("IntentSender{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(": ");
        IIntentSender iIntentSender = this.mTarget;
        sb.append(iIntentSender != null ? iIntentSender.asBinder() : null);
        sb.append('}');
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeStrongBinder(this.mTarget.asBinder());
    }

    public static void writeIntentSenderOrNullToParcel(IntentSender sender, Parcel out) {
        IBinder iBinder;
        if (sender != null) {
            iBinder = sender.mTarget.asBinder();
        } else {
            iBinder = null;
        }
        out.writeStrongBinder(iBinder);
    }

    public static IntentSender readIntentSenderOrNullFromParcel(Parcel in) {
        IBinder b = in.readStrongBinder();
        if (b != null) {
            return new IntentSender(b);
        }
        return null;
    }

    @UnsupportedAppUsage
    public IIntentSender getTarget() {
        return this.mTarget;
    }

    public IBinder getWhitelistToken() {
        return this.mWhitelistToken;
    }

    @UnsupportedAppUsage
    public IntentSender(IIntentSender target) {
        this.mTarget = target;
    }

    public IntentSender(IIntentSender target, IBinder whitelistToken) {
        this.mTarget = target;
        this.mWhitelistToken = whitelistToken;
    }

    public IntentSender(IBinder target) {
        this.mTarget = IIntentSender.Stub.asInterface(target);
    }
}
