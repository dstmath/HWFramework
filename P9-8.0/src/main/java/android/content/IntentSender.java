package android.content;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.content.IIntentReceiver.Stub;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.AndroidException;

public class IntentSender implements Parcelable {
    public static final Creator<IntentSender> CREATOR = new Creator<IntentSender>() {
        public IntentSender createFromParcel(Parcel in) {
            IBinder target = in.readStrongBinder();
            if (target != null) {
                return new IntentSender(target);
            }
            return null;
        }

        public IntentSender[] newArray(int size) {
            return new IntentSender[size];
        }
    };
    private final IIntentSender mTarget;
    IBinder mWhitelistToken;

    private static class FinishedDispatcher extends Stub implements Runnable {
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

        public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean serialized, boolean sticky, int sendingUser) {
            this.mIntent = intent;
            this.mResultCode = resultCode;
            this.mResultData = data;
            this.mResultExtras = extras;
            if (this.mHandler == null) {
                run();
            } else {
                this.mHandler.post(this);
            }
        }

        public void run() {
            this.mWho.onSendFinished(this.mIntentSender, this.mIntent, this.mResultCode, this.mResultData, this.mResultExtras);
        }
    }

    public interface OnFinished {
        void onSendFinished(IntentSender intentSender, Intent intent, int i, String str, Bundle bundle);
    }

    public static class SendIntentException extends AndroidException {
        public SendIntentException(String name) {
            super(name);
        }

        public SendIntentException(Exception cause) {
            super(cause);
        }
    }

    public void sendIntent(Context context, int code, Intent intent, OnFinished onFinished, Handler handler) throws SendIntentException {
        sendIntent(context, code, intent, onFinished, handler, null);
    }

    public void sendIntent(Context context, int code, Intent intent, OnFinished onFinished, Handler handler, String requiredPermission) throws SendIntentException {
        String resolvedType;
        IIntentReceiver finishedDispatcher;
        if (intent != null) {
            try {
                resolvedType = intent.resolveTypeIfNeeded(context.getContentResolver());
            } catch (RemoteException e) {
                throw new SendIntentException();
            }
        }
        resolvedType = null;
        IActivityManager service = ActivityManager.getService();
        IIntentSender iIntentSender = this.mTarget;
        IBinder iBinder = this.mWhitelistToken;
        if (onFinished != null) {
            finishedDispatcher = new FinishedDispatcher(this, onFinished, handler);
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
            return uid > 0 ? new UserHandle(UserHandle.getUserId(uid)) : null;
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
        Object obj = null;
        StringBuilder sb = new StringBuilder(128);
        sb.append("IntentSender{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(": ");
        if (this.mTarget != null) {
            obj = this.mTarget.asBinder();
        }
        sb.append(obj);
        sb.append('}');
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeStrongBinder(this.mTarget.asBinder());
    }

    public static void writeIntentSenderOrNullToParcel(IntentSender sender, Parcel out) {
        IBinder iBinder = null;
        if (sender != null) {
            iBinder = sender.mTarget.asBinder();
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

    public IIntentSender getTarget() {
        return this.mTarget;
    }

    public IBinder getWhitelistToken() {
        return this.mWhitelistToken;
    }

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
