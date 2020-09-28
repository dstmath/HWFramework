package android.telephony;

import android.annotation.SystemApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.mbms.GroupCall;
import android.telephony.mbms.GroupCallCallback;
import android.telephony.mbms.InternalGroupCallCallback;
import android.telephony.mbms.InternalGroupCallSessionCallback;
import android.telephony.mbms.MbmsGroupCallSessionCallback;
import android.telephony.mbms.MbmsUtils;
import android.telephony.mbms.vendor.IMbmsGroupCallService;
import android.util.ArraySet;
import android.util.Log;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MbmsGroupCallSession implements AutoCloseable {
    private static final String LOG_TAG = "MbmsGroupCallSession";
    @SystemApi
    public static final String MBMS_GROUP_CALL_SERVICE_ACTION = "android.telephony.action.EmbmsGroupCall";
    public static final String MBMS_GROUP_CALL_SERVICE_OVERRIDE_METADATA = "mbms-group-call-service-override";
    private static AtomicBoolean sIsInitialized = new AtomicBoolean(false);
    private final Context mContext;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class android.telephony.MbmsGroupCallSession.AnonymousClass1 */

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            MbmsGroupCallSession.sIsInitialized.set(false);
            MbmsGroupCallSession.this.mInternalCallback.onError(3, "Received death notification");
        }
    };
    private InternalGroupCallSessionCallback mInternalCallback;
    private Set<GroupCall> mKnownActiveGroupCalls = new ArraySet();
    private AtomicReference<IMbmsGroupCallService> mService = new AtomicReference<>(null);
    private int mSubscriptionId;

    private MbmsGroupCallSession(Context context, Executor executor, int subscriptionId, MbmsGroupCallSessionCallback callback) {
        this.mContext = context;
        this.mSubscriptionId = subscriptionId;
        this.mInternalCallback = new InternalGroupCallSessionCallback(callback, executor);
    }

    public static MbmsGroupCallSession create(Context context, int subscriptionId, Executor executor, final MbmsGroupCallSessionCallback callback) {
        if (sIsInitialized.compareAndSet(false, true)) {
            MbmsGroupCallSession session = new MbmsGroupCallSession(context, executor, subscriptionId, callback);
            final int result = session.bindAndInitialize();
            if (result == 0) {
                return session;
            }
            sIsInitialized.set(false);
            executor.execute(new Runnable() {
                /* class android.telephony.MbmsGroupCallSession.AnonymousClass2 */

                public void run() {
                    MbmsGroupCallSessionCallback.this.onError(result, null);
                }
            });
            return null;
        }
        throw new IllegalStateException("Cannot create two instances of MbmsGroupCallSession");
    }

    public static MbmsGroupCallSession create(Context context, Executor executor, MbmsGroupCallSessionCallback callback) {
        return create(context, SubscriptionManager.getDefaultSubscriptionId(), executor, callback);
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        try {
            IMbmsGroupCallService groupCallService = this.mService.get();
            if (groupCallService == null) {
                this.mService.set(null);
                sIsInitialized.set(false);
                this.mInternalCallback.stop();
                return;
            }
            groupCallService.dispose(this.mSubscriptionId);
            for (GroupCall s : this.mKnownActiveGroupCalls) {
                s.getCallback().stop();
            }
            this.mKnownActiveGroupCalls.clear();
            this.mService.set(null);
            sIsInitialized.set(false);
            this.mInternalCallback.stop();
        } catch (RemoteException e) {
        } catch (Throwable th) {
            this.mService.set(null);
            sIsInitialized.set(false);
            this.mInternalCallback.stop();
            throw th;
        }
    }

    public GroupCall startGroupCall(long tmgi, List<Integer> saiList, List<Integer> frequencyList, Executor executor, GroupCallCallback callback) {
        IMbmsGroupCallService groupCallService = this.mService.get();
        if (groupCallService != null) {
            InternalGroupCallCallback serviceCallback = new InternalGroupCallCallback(callback, executor);
            GroupCall serviceForApp = new GroupCall(this.mSubscriptionId, groupCallService, this, tmgi, serviceCallback);
            this.mKnownActiveGroupCalls.add(serviceForApp);
            try {
                int returnCode = groupCallService.startGroupCall(this.mSubscriptionId, tmgi, saiList, frequencyList, serviceCallback);
                if (returnCode == -1) {
                    close();
                    throw new IllegalStateException("Middleware must not return an unknown error code");
                } else if (returnCode == 0) {
                    return serviceForApp;
                } else {
                    this.mInternalCallback.onError(returnCode, null);
                    return null;
                }
            } catch (RemoteException e) {
                Log.w(LOG_TAG, "Remote process died");
                this.mService.set(null);
                sIsInitialized.set(false);
                this.mInternalCallback.onError(3, null);
                return null;
            }
        } else {
            throw new IllegalStateException("Middleware not yet bound");
        }
    }

    public void onGroupCallStopped(GroupCall service) {
        this.mKnownActiveGroupCalls.remove(service);
    }

    private int bindAndInitialize() {
        return MbmsUtils.startBinding(this.mContext, MBMS_GROUP_CALL_SERVICE_ACTION, new ServiceConnection() {
            /* class android.telephony.MbmsGroupCallSession.AnonymousClass3 */

            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName name, IBinder service) {
                IMbmsGroupCallService groupCallService = IMbmsGroupCallService.Stub.asInterface(service);
                try {
                    int result = groupCallService.initialize(MbmsGroupCallSession.this.mInternalCallback, MbmsGroupCallSession.this.mSubscriptionId);
                    if (result == -1) {
                        MbmsGroupCallSession.this.close();
                        throw new IllegalStateException("Middleware must not return an unknown error code");
                    } else if (result != 0) {
                        MbmsGroupCallSession.this.mInternalCallback.onError(result, "Error returned during initialization");
                        MbmsGroupCallSession.sIsInitialized.set(false);
                    } else {
                        try {
                            groupCallService.asBinder().linkToDeath(MbmsGroupCallSession.this.mDeathRecipient, 0);
                            MbmsGroupCallSession.this.mService.set(groupCallService);
                        } catch (RemoteException e) {
                            MbmsGroupCallSession.this.mInternalCallback.onError(3, "Middleware lost during initialization");
                            MbmsGroupCallSession.sIsInitialized.set(false);
                        }
                    }
                } catch (RemoteException e2) {
                    Log.e(MbmsGroupCallSession.LOG_TAG, "Service died before initialization");
                    MbmsGroupCallSession.this.mInternalCallback.onError(103, e2.toString());
                    MbmsGroupCallSession.sIsInitialized.set(false);
                } catch (RuntimeException e3) {
                    Log.e(MbmsGroupCallSession.LOG_TAG, "Runtime exception during initialization");
                    MbmsGroupCallSession.this.mInternalCallback.onError(103, e3.toString());
                    MbmsGroupCallSession.sIsInitialized.set(false);
                }
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName name) {
                MbmsGroupCallSession.sIsInitialized.set(false);
                MbmsGroupCallSession.this.mService.set(null);
            }
        });
    }
}
