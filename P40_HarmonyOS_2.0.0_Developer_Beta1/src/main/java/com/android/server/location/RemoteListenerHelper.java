package com.android.server.location;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.util.HwLog;
import android.util.Log;
import com.android.internal.util.Preconditions;
import com.android.server.HwServiceFactory;
import com.android.server.location.RemoteListenerHelper;
import com.huawei.pgmng.log.LogPower;
import java.util.HashMap;
import java.util.Map;

public abstract class RemoteListenerHelper<TListener extends IInterface> {
    protected static final int RESULT_GPS_LOCATION_DISABLED = 3;
    protected static final int RESULT_INTERNAL_ERROR = 4;
    protected static final int RESULT_NOT_ALLOWED = 6;
    protected static final int RESULT_NOT_AVAILABLE = 1;
    protected static final int RESULT_NOT_SUPPORTED = 2;
    protected static final int RESULT_SUCCESS = 0;
    protected static final int RESULT_UNKNOWN = 5;
    protected final AppOpsManager mAppOps;
    protected final Context mContext;
    protected final Handler mHandler;
    private boolean mHasIsSupported;
    private IHwGpsLogServices mHwGpsLogServices;
    private volatile boolean mIsRegistered;
    private boolean mIsSupported;
    private int mLastReportedResult = 5;
    private final Map<IBinder, RemoteListenerHelper<TListener>.IdentifiedListener> mListenerMap = new HashMap();
    private final String mTag;

    /* access modifiers changed from: protected */
    public interface ListenerOperation<TListener extends IInterface> {
        void execute(TListener tlistener, CallerIdentity callerIdentity) throws RemoteException;
    }

    /* access modifiers changed from: protected */
    public abstract ListenerOperation<TListener> getHandlerOperation(int i);

    /* access modifiers changed from: protected */
    public abstract boolean isAvailableInPlatform();

    /* access modifiers changed from: protected */
    public abstract boolean isGpsEnabled();

    /* access modifiers changed from: protected */
    public abstract int registerWithService();

    /* access modifiers changed from: protected */
    public abstract void unregisterFromService();

    protected RemoteListenerHelper(Context context, Handler handler, String name) {
        Preconditions.checkNotNull(name);
        this.mHandler = handler;
        this.mTag = name;
        this.mContext = context;
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        this.mHwGpsLogServices = HwServiceFactory.getHwGpsLogServices(this.mContext);
    }

    public boolean isRegistered() {
        return this.mIsRegistered;
    }

    public void addListener(TListener listener, CallerIdentity callerIdentity) {
        int result;
        Preconditions.checkNotNull(listener, "Attempted to register a 'null' listener.");
        String packageName = callerIdentity != null ? callerIdentity.mPackageName : "";
        if (!"GnssMeasurementsProvider".equals(this.mTag) || !HwServiceFactory.getGpsFreezeProc().isFreeze(packageName)) {
            IBinder binder = listener.asBinder();
            synchronized (this.mListenerMap) {
                if (!this.mListenerMap.containsKey(binder)) {
                    RemoteListenerHelper<TListener>.IdentifiedListener identifiedListener = new IdentifiedListener(listener, callerIdentity);
                    this.mListenerMap.put(binder, identifiedListener);
                    recordRemoteListener(1, packageName, this.mListenerMap.size());
                    if (!isAvailableInPlatform()) {
                        result = 1;
                    } else if (this.mHasIsSupported && !this.mIsSupported) {
                        result = 2;
                    } else if (!isGpsEnabled()) {
                        result = 3;
                    } else if (this.mHasIsSupported && this.mIsSupported) {
                        if ("GnssMeasurementsProvider".equals(this.mTag)) {
                            HwLog.dubaie("DUBAI_TAG_GNSS_MEASUREMENT_STATE", "name=" + identifiedListener.getPackageName() + " state=1");
                            LogPower.push(231, Integer.toString(callerIdentity.mUid), callerIdentity.mPackageName);
                        }
                        tryRegister();
                        result = 0;
                    } else {
                        return;
                    }
                    post(identifiedListener, getHandlerOperation(result));
                    return;
                }
                return;
            }
        }
        String str = this.mTag;
        Log.i(str, "packageName: " + packageName + " is freeze can't add GnssMeasurementsListener");
    }

    public void removeListener(TListener listener) {
        Preconditions.checkNotNull(listener, "Attempted to remove a 'null' listener.");
        IBinder binder = listener.asBinder();
        synchronized (this.mListenerMap) {
            RemoteListenerHelper<TListener>.IdentifiedListener identifiedListener = this.mListenerMap.get(binder);
            String packageName = "";
            if (identifiedListener != null) {
                packageName = identifiedListener.getPackageName();
                if ("GnssMeasurementsProvider".equals(this.mTag)) {
                    HwLog.dubaie("DUBAI_TAG_GNSS_MEASUREMENT_STATE", "name=" + packageName + " state=0");
                    LogPower.push(232, Integer.toString(identifiedListener.getUid()), packageName);
                }
            }
            this.mListenerMap.remove(listener.asBinder());
            recordRemoteListener(0, packageName, this.mListenerMap.size());
            if (this.mListenerMap.isEmpty()) {
                tryUnregister();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void foreach(ListenerOperation<TListener> operation) {
        synchronized (this.mListenerMap) {
            foreachUnsafe(operation);
        }
    }

    /* access modifiers changed from: protected */
    public void setSupported(boolean value) {
        synchronized (this.mListenerMap) {
            this.mHasIsSupported = true;
            this.mIsSupported = value;
        }
    }

    /* access modifiers changed from: protected */
    public void tryUpdateRegistrationWithService() {
        synchronized (this.mListenerMap) {
            if (!isGpsEnabled()) {
                tryUnregister();
                sendMeasurementStateToPG(false);
            } else if (!this.mListenerMap.isEmpty()) {
                tryRegister();
                sendMeasurementStateToPG(true);
            }
        }
    }

    private void sendMeasurementStateToPG(boolean enable) {
        if ("GnssMeasurementsProvider".equals(this.mTag) && !this.mListenerMap.isEmpty()) {
            for (RemoteListenerHelper<TListener>.IdentifiedListener identifiedListener : this.mListenerMap.values()) {
                LogPower.push(enable ? 231 : 232, Integer.toString(identifiedListener.getUid()), identifiedListener.getPackageName());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateResult() {
        synchronized (this.mListenerMap) {
            int newResult = calculateCurrentResultUnsafe();
            if (this.mLastReportedResult != newResult) {
                foreachUnsafe(getHandlerOperation(newResult));
                this.mLastReportedResult = newResult;
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean hasPermission(Context context, CallerIdentity callerIdentity) {
        return LocationPermissionUtil.doesCallerReportToAppOps(context, callerIdentity) ? this.mAppOps.checkOpNoThrow(1, callerIdentity.mUid, callerIdentity.mPackageName) == 0 : this.mAppOps.noteOpNoThrow(1, callerIdentity.mUid, callerIdentity.mPackageName) == 0;
    }

    /* access modifiers changed from: protected */
    public void logPermissionDisabledEventNotReported(String tag, String packageName, String event) {
        if (Log.isLoggable(tag, 3)) {
            Log.d(tag, "Location permission disabled. Skipping " + event + " reporting for app: " + packageName);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void foreachUnsafe(ListenerOperation<TListener> operation) {
        for (RemoteListenerHelper<TListener>.IdentifiedListener identifiedListener : this.mListenerMap.values()) {
            if (!HwServiceFactory.getGpsFreezeProc().isFreeze(identifiedListener.getPackageName())) {
                post(identifiedListener, operation);
            }
        }
    }

    private void post(RemoteListenerHelper<TListener>.IdentifiedListener identifiedListener, ListenerOperation<TListener> operation) {
        if (operation != null) {
            this.mHandler.post(new HandlerRunnable(identifiedListener, operation));
        }
    }

    private void tryRegister() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.location.RemoteListenerHelper.AnonymousClass1 */
            int registrationState = 4;

            @Override // java.lang.Runnable
            public void run() {
                if (!RemoteListenerHelper.this.mIsRegistered) {
                    this.registrationState = RemoteListenerHelper.this.registerWithService();
                    RemoteListenerHelper.this.mIsRegistered = this.registrationState == 0;
                }
                if (!RemoteListenerHelper.this.mIsRegistered) {
                    RemoteListenerHelper.this.mHandler.post(new Runnable() {
                        /* class com.android.server.location.$$Lambda$RemoteListenerHelper$1$zm4ubOjPyd7USwEJsdJwj1QLhgw */

                        @Override // java.lang.Runnable
                        public final void run() {
                            RemoteListenerHelper.AnonymousClass1.this.lambda$run$0$RemoteListenerHelper$1();
                        }
                    });
                }
            }

            public /* synthetic */ void lambda$run$0$RemoteListenerHelper$1() {
                synchronized (RemoteListenerHelper.this.mListenerMap) {
                    RemoteListenerHelper.this.foreachUnsafe(RemoteListenerHelper.this.getHandlerOperation(this.registrationState));
                }
            }
        });
    }

    private void tryUnregister() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.location.$$Lambda$RemoteListenerHelper$0Rlnad83RE1JdiVK0ULOLm530JM */

            @Override // java.lang.Runnable
            public final void run() {
                RemoteListenerHelper.this.lambda$tryUnregister$0$RemoteListenerHelper();
            }
        });
    }

    public /* synthetic */ void lambda$tryUnregister$0$RemoteListenerHelper() {
        if (this.mIsRegistered) {
            unregisterFromService();
            this.mIsRegistered = false;
        }
    }

    private int calculateCurrentResultUnsafe() {
        if (!isAvailableInPlatform()) {
            return 1;
        }
        if (!this.mHasIsSupported || this.mListenerMap.isEmpty()) {
            return 5;
        }
        if (!this.mIsSupported) {
            return 2;
        }
        if (!isGpsEnabled()) {
            return 3;
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public class IdentifiedListener {
        private final CallerIdentity mCallerIdentity;
        private final TListener mListener;

        private IdentifiedListener(TListener listener, CallerIdentity callerIdentity) {
            this.mListener = listener;
            this.mCallerIdentity = callerIdentity;
        }

        public String getPackageName() {
            CallerIdentity callerIdentity = this.mCallerIdentity;
            if (callerIdentity != null) {
                return callerIdentity.mPackageName;
            }
            return null;
        }

        public int getUid() {
            CallerIdentity callerIdentity = this.mCallerIdentity;
            if (callerIdentity != null) {
                return callerIdentity.mUid;
            }
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public class HandlerRunnable implements Runnable {
        private final RemoteListenerHelper<TListener>.IdentifiedListener mIdentifiedListener;
        private final ListenerOperation<TListener> mOperation;

        private HandlerRunnable(RemoteListenerHelper<TListener>.IdentifiedListener identifiedListener, ListenerOperation<TListener> operation) {
            this.mIdentifiedListener = identifiedListener;
            this.mOperation = operation;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                this.mOperation.execute((TListener) ((IdentifiedListener) this.mIdentifiedListener).mListener, ((IdentifiedListener) this.mIdentifiedListener).mCallerIdentity);
            } catch (RemoteException e) {
                Log.v(RemoteListenerHelper.this.mTag, "Error in monitored listener.", e);
            }
        }
    }

    private void recordRemoteListener(int operationType, String packageName, int size) {
        Bundle bundle = new Bundle();
        bundle.putString("providerType", this.mTag);
        bundle.putInt("size", size);
        this.mHwGpsLogServices.recordGnssStatusStatistics(0, operationType, packageName, bundle);
    }
}
