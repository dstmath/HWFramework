package android.app;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.AndroidException;
import android.util.ArraySet;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.proto.ProtoOutputStream;
import com.android.internal.os.IResultReceiver;
import com.huawei.android.app.HwActivityTaskManager;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class PendingIntent implements Parcelable {
    public static final Parcelable.Creator<PendingIntent> CREATOR = new Parcelable.Creator<PendingIntent>() {
        /* class android.app.PendingIntent.AnonymousClass2 */

        @Override // android.os.Parcelable.Creator
        public PendingIntent createFromParcel(Parcel in) {
            IBinder target = in.readStrongBinder();
            if (target != null) {
                return new PendingIntent(target, in.getClassCookie(PendingIntent.class));
            }
            return null;
        }

        @Override // android.os.Parcelable.Creator
        public PendingIntent[] newArray(int size) {
            return new PendingIntent[size];
        }
    };
    public static final int FLAG_CANCEL_CURRENT = 268435456;
    public static final int FLAG_IMMUTABLE = 67108864;
    public static final int FLAG_NO_CREATE = 536870912;
    public static final int FLAG_ONE_SHOT = 1073741824;
    public static final int FLAG_UPDATE_CURRENT = 134217728;
    static final boolean IS_DEBUG_VERSION;
    private static final ThreadLocal<OnMarshaledListener> sOnMarshaledListener = new ThreadLocal<>();
    private ArraySet<CancelListener> mCancelListeners;
    private IResultReceiver mCancelReceiver;
    private final IIntentSender mTarget;
    private IBinder mWhitelistToken;

    public interface CancelListener {
        void onCancelled(PendingIntent pendingIntent);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Flags {
    }

    public interface OnFinished {
        void onSendFinished(PendingIntent pendingIntent, Intent intent, int i, String str, Bundle bundle);
    }

    public interface OnMarshaledListener {
        void onMarshaled(PendingIntent pendingIntent, Parcel parcel, int i);
    }

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        IS_DEBUG_VERSION = z;
    }

    public static class CanceledException extends AndroidException {
        public CanceledException() {
        }

        public CanceledException(String name) {
            super(name);
        }

        public CanceledException(Exception cause) {
            super(cause);
        }
    }

    /* access modifiers changed from: private */
    public static class FinishedDispatcher extends IIntentReceiver.Stub implements Runnable {
        private static Handler sDefaultSystemHandler;
        private final Handler mHandler;
        private Intent mIntent;
        private final PendingIntent mPendingIntent;
        private int mResultCode;
        private String mResultData;
        private Bundle mResultExtras;
        private final OnFinished mWho;

        FinishedDispatcher(PendingIntent pi, OnFinished who, Handler handler) {
            this.mPendingIntent = pi;
            this.mWho = who;
            if (handler != null || !ActivityThread.isSystem()) {
                this.mHandler = handler;
                return;
            }
            if (sDefaultSystemHandler == null) {
                sDefaultSystemHandler = new Handler(Looper.getMainLooper());
            }
            this.mHandler = sDefaultSystemHandler;
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

        @Override // java.lang.Runnable
        public void run() {
            this.mWho.onSendFinished(this.mPendingIntent, this.mIntent, this.mResultCode, this.mResultData, this.mResultExtras);
        }
    }

    @UnsupportedAppUsage
    public static void setOnMarshaledListener(OnMarshaledListener listener) {
        sOnMarshaledListener.set(listener);
    }

    public static PendingIntent getActivity(Context context, int requestCode, Intent intent, int flags) {
        return getActivity(context, requestCode, intent, flags, null);
    }

    public static PendingIntent getActivity(Context context, int requestCode, Intent intent, int flags, Bundle options) {
        String resolvedType;
        Bundle options2;
        String packageName = context.getPackageName();
        if (intent != null) {
            resolvedType = intent.resolveTypeIfNeeded(context.getContentResolver());
        } else {
            resolvedType = null;
        }
        if (!HwPCUtils.enabledInPad()) {
            options2 = HwPCUtils.hookStartActivityOptions(context, options);
        } else {
            options2 = options;
        }
        Bundle options3 = HwActivityTaskManager.hookStartActivityOptions(context, options2);
        try {
            intent.migrateExtraStreamToClipData();
            intent.prepareToLeaveProcess(context);
            IIntentSender target = ActivityManager.getService().getIntentSender(2, packageName, null, null, requestCode, new Intent[]{intent}, resolvedType != null ? new String[]{resolvedType} : null, flags, options3, context.getUserId());
            if (target != null) {
                return new PendingIntent(target);
            }
            return null;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public static PendingIntent getActivityAsUser(Context context, int requestCode, Intent intent, int flags, Bundle options, UserHandle user) {
        String resolvedType;
        Bundle options2;
        String packageName = context.getPackageName();
        if (intent != null) {
            resolvedType = intent.resolveTypeIfNeeded(context.getContentResolver());
        } else {
            resolvedType = null;
        }
        if (!HwPCUtils.enabledInPad()) {
            options2 = HwPCUtils.hookStartActivityOptions(context, options);
        } else {
            options2 = options;
        }
        Bundle options3 = HwActivityTaskManager.hookStartActivityOptions(context, options2);
        try {
            intent.migrateExtraStreamToClipData();
            intent.prepareToLeaveProcess(context);
            IIntentSender target = ActivityManager.getService().getIntentSender(2, packageName, null, null, requestCode, new Intent[]{intent}, resolvedType != null ? new String[]{resolvedType} : null, flags, options3, user.getIdentifier());
            if (target != null) {
                return new PendingIntent(target);
            }
            return null;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static PendingIntent getActivities(Context context, int requestCode, Intent[] intents, int flags) {
        return getActivities(context, requestCode, intents, flags, null);
    }

    public static PendingIntent getActivities(Context context, int requestCode, Intent[] intents, int flags, Bundle options) {
        Bundle options2;
        String packageName = context.getPackageName();
        String[] resolvedTypes = new String[intents.length];
        for (int i = 0; i < intents.length; i++) {
            intents[i].migrateExtraStreamToClipData();
            intents[i].prepareToLeaveProcess(context);
            resolvedTypes[i] = intents[i].resolveTypeIfNeeded(context.getContentResolver());
        }
        if (!HwPCUtils.enabledInPad()) {
            options2 = HwPCUtils.hookStartActivityOptions(context, options);
        } else {
            options2 = options;
        }
        try {
            IIntentSender target = ActivityManager.getService().getIntentSender(2, packageName, null, null, requestCode, intents, resolvedTypes, flags, HwActivityTaskManager.hookStartActivityOptions(context, options2), context.getUserId());
            if (target != null) {
                return new PendingIntent(target);
            }
            return null;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static PendingIntent getActivitiesAsUser(Context context, int requestCode, Intent[] intents, int flags, Bundle options, UserHandle user) {
        Bundle options2;
        String packageName = context.getPackageName();
        String[] resolvedTypes = new String[intents.length];
        for (int i = 0; i < intents.length; i++) {
            intents[i].migrateExtraStreamToClipData();
            intents[i].prepareToLeaveProcess(context);
            resolvedTypes[i] = intents[i].resolveTypeIfNeeded(context.getContentResolver());
        }
        if (!HwPCUtils.enabledInPad()) {
            options2 = HwPCUtils.hookStartActivityOptions(context, options);
        } else {
            options2 = options;
        }
        try {
            IIntentSender target = ActivityManager.getService().getIntentSender(2, packageName, null, null, requestCode, intents, resolvedTypes, flags, HwActivityTaskManager.hookStartActivityOptions(context, options2), user.getIdentifier());
            if (target != null) {
                return new PendingIntent(target);
            }
            return null;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static PendingIntent getBroadcast(Context context, int requestCode, Intent intent, int flags) {
        return getBroadcastAsUser(context, requestCode, intent, flags, context.getUser());
    }

    @UnsupportedAppUsage
    public static PendingIntent getBroadcastAsUser(Context context, int requestCode, Intent intent, int flags, UserHandle userHandle) {
        String resolvedType;
        RemoteException e;
        String packageName = context.getPackageName();
        if (intent != null) {
            resolvedType = intent.resolveTypeIfNeeded(context.getContentResolver());
        } else {
            resolvedType = null;
        }
        try {
            if (IS_DEBUG_VERSION && intent != null && Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())) {
                StringBuilder sb = new StringBuilder();
                sb.append("getBroadcastAsUser packageName:");
                sb.append(packageName);
                sb.append(" ,action:");
                sb.append(intent.getAction());
                sb.append(",hasComponent:");
                sb.append(intent.getComponent() != null);
                sb.append(" resolvedType :");
                sb.append(resolvedType);
                sb.append("flags = 0x");
                sb.append(Integer.toHexString(flags));
                sb.append(" requestCode : ");
                try {
                    sb.append(requestCode);
                    sb.append(" called by :");
                    sb.append(Debug.getCallers(8));
                    Log.i("PendingIntent", sb.toString());
                } catch (RemoteException e2) {
                    e = e2;
                    throw e.rethrowFromSystemServer();
                }
            }
            intent.prepareToLeaveProcess(context);
            IIntentSender target = ActivityManager.getService().getIntentSender(1, packageName, null, null, requestCode, new Intent[]{intent}, resolvedType != null ? new String[]{resolvedType} : null, flags, null, userHandle.getIdentifier());
            if (target != null) {
                return new PendingIntent(target);
            }
            return null;
        } catch (RemoteException e3) {
            e = e3;
            throw e.rethrowFromSystemServer();
        }
    }

    public static PendingIntent getService(Context context, int requestCode, Intent intent, int flags) {
        return buildServicePendingIntent(context, requestCode, intent, flags, 4);
    }

    public static PendingIntent getForegroundService(Context context, int requestCode, Intent intent, int flags) {
        return buildServicePendingIntent(context, requestCode, intent, flags, 5);
    }

    private static PendingIntent buildServicePendingIntent(Context context, int requestCode, Intent intent, int flags, int serviceKind) {
        String resolvedType;
        String packageName = context.getPackageName();
        if (intent != null) {
            resolvedType = intent.resolveTypeIfNeeded(context.getContentResolver());
        } else {
            resolvedType = null;
        }
        try {
            intent.prepareToLeaveProcess(context);
            IIntentSender target = ActivityManager.getService().getIntentSender(serviceKind, packageName, null, null, requestCode, new Intent[]{intent}, resolvedType != null ? new String[]{resolvedType} : null, flags, null, context.getUserId());
            if (target != null) {
                return new PendingIntent(target);
            }
            return null;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public IntentSender getIntentSender() {
        return new IntentSender(this.mTarget, this.mWhitelistToken);
    }

    public void cancel() {
        try {
            ActivityManager.getService().cancelIntentSender(this.mTarget);
        } catch (RemoteException e) {
        }
    }

    public void send() throws CanceledException {
        send(null, 0, null, null, null, null, null);
    }

    public void send(int code) throws CanceledException {
        send(null, code, null, null, null, null, null);
    }

    public void send(Context context, int code, Intent intent) throws CanceledException {
        send(context, code, intent, null, null, null, null);
    }

    public void send(int code, OnFinished onFinished, Handler handler) throws CanceledException {
        send(null, code, null, onFinished, handler, null, null);
    }

    public void send(Context context, int code, Intent intent, OnFinished onFinished, Handler handler) throws CanceledException {
        send(context, code, intent, onFinished, handler, null, null);
    }

    public void send(Context context, int code, Intent intent, OnFinished onFinished, Handler handler, String requiredPermission) throws CanceledException {
        send(context, code, intent, onFinished, handler, requiredPermission, null);
    }

    public void send(Context context, int code, Intent intent, OnFinished onFinished, Handler handler, String requiredPermission, Bundle options) throws CanceledException {
        if (sendAndReturnResult(context, code, intent, onFinished, handler, requiredPermission, options) < 0) {
            throw new CanceledException();
        }
    }

    public int sendAndReturnResult(Context context, int code, Intent intent, OnFinished onFinished, Handler handler, String requiredPermission, Bundle options) throws CanceledException {
        RemoteException e;
        String resolvedType;
        FinishedDispatcher finishedDispatcher;
        if (intent != null) {
            try {
                resolvedType = intent.resolveTypeIfNeeded(context.getContentResolver());
            } catch (RemoteException e2) {
                e = e2;
                throw new CanceledException(e);
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
            } catch (RemoteException e3) {
                e = e3;
                throw new CanceledException(e);
            }
        } else {
            finishedDispatcher = null;
        }
        return service.sendIntentSender(iIntentSender, iBinder, code, intent, resolvedType, finishedDispatcher, requiredPermission, options);
    }

    @Deprecated
    public String getTargetPackage() {
        try {
            return ActivityManager.getService().getPackageForIntentSender(this.mTarget);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getCreatorPackage() {
        try {
            return ActivityManager.getService().getPackageForIntentSender(this.mTarget);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getCreatorUid() {
        try {
            return ActivityManager.getService().getUidForIntentSender(this.mTarget);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void registerCancelListener(CancelListener cancelListener) {
        synchronized (this) {
            if (this.mCancelReceiver == null) {
                this.mCancelReceiver = new IResultReceiver.Stub() {
                    /* class android.app.PendingIntent.AnonymousClass1 */

                    @Override // com.android.internal.os.IResultReceiver
                    public void send(int resultCode, Bundle resultData) throws RemoteException {
                        PendingIntent.this.notifyCancelListeners();
                    }
                };
            }
            if (this.mCancelListeners == null) {
                this.mCancelListeners = new ArraySet<>();
            }
            boolean wasEmpty = this.mCancelListeners.isEmpty();
            this.mCancelListeners.add(cancelListener);
            if (wasEmpty) {
                try {
                    ActivityManager.getService().registerIntentSenderCancelListener(this.mTarget, this.mCancelReceiver);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyCancelListeners() {
        ArraySet<CancelListener> cancelListeners;
        synchronized (this) {
            cancelListeners = new ArraySet<>(this.mCancelListeners);
        }
        int size = cancelListeners.size();
        for (int i = 0; i < size; i++) {
            cancelListeners.valueAt(i).onCancelled(this);
        }
    }

    public void unregisterCancelListener(CancelListener cancelListener) {
        synchronized (this) {
            if (this.mCancelListeners != null) {
                boolean wasEmpty = this.mCancelListeners.isEmpty();
                this.mCancelListeners.remove(cancelListener);
                if (this.mCancelListeners.isEmpty() && !wasEmpty) {
                    try {
                        ActivityManager.getService().unregisterIntentSenderCancelListener(this.mTarget, this.mCancelReceiver);
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
            }
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
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isTargetedToPackage() {
        try {
            return ActivityManager.getService().isIntentSenderTargetedToPackage(this.mTarget);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public boolean isActivity() {
        try {
            return ActivityManager.getService().isIntentSenderAnActivity(this.mTarget);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isForegroundService() {
        try {
            return ActivityManager.getService().isIntentSenderAForegroundService(this.mTarget);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isBroadcast() {
        try {
            return ActivityManager.getService().isIntentSenderABroadcast(this.mTarget);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public Intent getIntent() {
        try {
            return ActivityManager.getService().getIntentForIntentSender(this.mTarget);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public String getTag(String prefix) {
        try {
            return ActivityManager.getService().getTagForIntentSender(this.mTarget, prefix);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean equals(Object otherObj) {
        if (otherObj instanceof PendingIntent) {
            return this.mTarget.asBinder().equals(((PendingIntent) otherObj).mTarget.asBinder());
        }
        return false;
    }

    public int hashCode() {
        return this.mTarget.asBinder().hashCode();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("PendingIntent{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(": ");
        IIntentSender iIntentSender = this.mTarget;
        sb.append(iIntentSender != null ? iIntentSender.asBinder() : null);
        sb.append('}');
        return sb.toString();
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        IIntentSender iIntentSender = this.mTarget;
        if (iIntentSender != null) {
            proto.write(1138166333441L, iIntentSender.asBinder().toString());
        }
        proto.end(token);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeStrongBinder(this.mTarget.asBinder());
        OnMarshaledListener listener = sOnMarshaledListener.get();
        if (listener != null) {
            listener.onMarshaled(this, out, flags);
        }
    }

    public static void writePendingIntentOrNullToParcel(PendingIntent sender, Parcel out) {
        OnMarshaledListener listener;
        out.writeStrongBinder(sender != null ? sender.mTarget.asBinder() : null);
        if (sender != null && (listener = sOnMarshaledListener.get()) != null) {
            listener.onMarshaled(sender, out, 0);
        }
    }

    public static PendingIntent readPendingIntentOrNullFromParcel(Parcel in) {
        IBinder b = in.readStrongBinder();
        if (b != null) {
            return new PendingIntent(b, in.getClassCookie(PendingIntent.class));
        }
        return null;
    }

    PendingIntent(IIntentSender target) {
        this.mTarget = target;
    }

    PendingIntent(IBinder target, Object cookie) {
        this.mTarget = IIntentSender.Stub.asInterface(target);
        if (cookie != null) {
            this.mWhitelistToken = (IBinder) cookie;
        }
    }

    public IIntentSender getTarget() {
        return this.mTarget;
    }

    public IBinder getWhitelistToken() {
        return this.mWhitelistToken;
    }
}
