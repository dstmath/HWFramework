package android.content;

import android.app.ActivityManagerNative;
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
import android.security.keymaster.KeymasterDefs;
import android.util.AndroidException;

public class IntentSender implements Parcelable {
    public static final Creator<IntentSender> CREATOR = null;
    private final IIntentSender mTarget;

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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.IntentSender.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.IntentSender.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.IntentSender.<clinit>():void");
    }

    public void sendIntent(Context context, int code, Intent intent, OnFinished onFinished, Handler handler) throws SendIntentException {
        sendIntent(context, code, intent, onFinished, handler, null);
    }

    public void sendIntent(Context context, int code, Intent intent, OnFinished onFinished, Handler handler, String requiredPermission) throws SendIntentException {
        IIntentReceiver finishedDispatcher;
        if (intent != null) {
            try {
                String resolveTypeIfNeeded = intent.resolveTypeIfNeeded(context.getContentResolver());
            } catch (RemoteException e) {
                throw new SendIntentException();
            }
        }
        resolveTypeIfNeeded = null;
        IActivityManager iActivityManager = ActivityManagerNative.getDefault();
        IIntentSender iIntentSender = this.mTarget;
        if (onFinished != null) {
            finishedDispatcher = new FinishedDispatcher(this, onFinished, handler);
        } else {
            finishedDispatcher = null;
        }
        if (iActivityManager.sendIntentSender(iIntentSender, code, intent, resolveTypeIfNeeded, finishedDispatcher, requiredPermission, null) < 0) {
            throw new SendIntentException();
        }
    }

    @Deprecated
    public String getTargetPackage() {
        try {
            return ActivityManagerNative.getDefault().getPackageForIntentSender(this.mTarget);
        } catch (RemoteException e) {
            return null;
        }
    }

    public String getCreatorPackage() {
        try {
            return ActivityManagerNative.getDefault().getPackageForIntentSender(this.mTarget);
        } catch (RemoteException e) {
            return null;
        }
    }

    public int getCreatorUid() {
        try {
            return ActivityManagerNative.getDefault().getUidForIntentSender(this.mTarget);
        } catch (RemoteException e) {
            return -1;
        }
    }

    public UserHandle getCreatorUserHandle() {
        try {
            int uid = ActivityManagerNative.getDefault().getUidForIntentSender(this.mTarget);
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
        StringBuilder sb = new StringBuilder(KeymasterDefs.KM_ALGORITHM_HMAC);
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

    public IntentSender(IIntentSender target) {
        this.mTarget = target;
    }

    public IntentSender(IBinder target) {
        this.mTarget = IIntentSender.Stub.asInterface(target);
    }
}
