package android.telephony;

import android.annotation.SystemApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.mbms.InternalStreamingServiceCallback;
import android.telephony.mbms.InternalStreamingSessionCallback;
import android.telephony.mbms.MbmsStreamingSessionCallback;
import android.telephony.mbms.MbmsUtils;
import android.telephony.mbms.StreamingService;
import android.telephony.mbms.StreamingServiceCallback;
import android.telephony.mbms.StreamingServiceInfo;
import android.telephony.mbms.vendor.IMbmsStreamingService;
import android.util.ArraySet;
import android.util.Log;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MbmsStreamingSession implements AutoCloseable {
    private static final String LOG_TAG = "MbmsStreamingSession";
    @SystemApi
    public static final String MBMS_STREAMING_SERVICE_ACTION = "android.telephony.action.EmbmsStreaming";
    public static final String MBMS_STREAMING_SERVICE_OVERRIDE_METADATA = "mbms-streaming-service-override";
    private static AtomicBoolean sIsInitialized = new AtomicBoolean(false);
    private final Context mContext;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class android.telephony.MbmsStreamingSession.AnonymousClass1 */

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            MbmsStreamingSession.sIsInitialized.set(false);
            MbmsStreamingSession.this.sendErrorToApp(3, "Received death notification");
        }
    };
    private InternalStreamingSessionCallback mInternalCallback;
    private Set<StreamingService> mKnownActiveStreamingServices = new ArraySet();
    private AtomicReference<IMbmsStreamingService> mService = new AtomicReference<>(null);
    private int mSubscriptionId = -1;

    private MbmsStreamingSession(Context context, Executor executor, int subscriptionId, MbmsStreamingSessionCallback callback) {
        this.mContext = context;
        this.mSubscriptionId = subscriptionId;
        this.mInternalCallback = new InternalStreamingSessionCallback(callback, executor);
    }

    public static MbmsStreamingSession create(Context context, Executor executor, int subscriptionId, final MbmsStreamingSessionCallback callback) {
        if (sIsInitialized.compareAndSet(false, true)) {
            MbmsStreamingSession session = new MbmsStreamingSession(context, executor, subscriptionId, callback);
            final int result = session.bindAndInitialize();
            if (result == 0) {
                return session;
            }
            sIsInitialized.set(false);
            executor.execute(new Runnable() {
                /* class android.telephony.MbmsStreamingSession.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    MbmsStreamingSessionCallback.this.onError(result, null);
                }
            });
            return null;
        }
        throw new IllegalStateException("Cannot create two instances of MbmsStreamingSession");
    }

    public static MbmsStreamingSession create(Context context, Executor executor, MbmsStreamingSessionCallback callback) {
        return create(context, executor, SubscriptionManager.getDefaultSubscriptionId(), callback);
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        try {
            IMbmsStreamingService streamingService = this.mService.get();
            if (streamingService == null) {
                this.mService.set(null);
                sIsInitialized.set(false);
                this.mInternalCallback.stop();
                return;
            }
            streamingService.dispose(this.mSubscriptionId);
            for (StreamingService s : this.mKnownActiveStreamingServices) {
                s.getCallback().stop();
            }
            this.mKnownActiveStreamingServices.clear();
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

    public void requestUpdateStreamingServices(List<String> serviceClassList) {
        IMbmsStreamingService streamingService = this.mService.get();
        if (streamingService != null) {
            try {
                int returnCode = streamingService.requestUpdateStreamingServices(this.mSubscriptionId, serviceClassList);
                if (returnCode != -1) {
                    if (returnCode != 0) {
                        sendErrorToApp(returnCode, null);
                    }
                    return;
                }
                close();
                throw new IllegalStateException("Middleware must not return an unknown error code");
            } catch (RemoteException e) {
                Log.w(LOG_TAG, "Remote process died");
                this.mService.set(null);
                sIsInitialized.set(false);
                sendErrorToApp(3, null);
            }
        } else {
            throw new IllegalStateException("Middleware not yet bound");
        }
    }

    public StreamingService startStreaming(StreamingServiceInfo serviceInfo, Executor executor, StreamingServiceCallback callback) {
        IMbmsStreamingService streamingService = this.mService.get();
        if (streamingService != null) {
            InternalStreamingServiceCallback serviceCallback = new InternalStreamingServiceCallback(callback, executor);
            StreamingService serviceForApp = new StreamingService(this.mSubscriptionId, streamingService, this, serviceInfo, serviceCallback);
            this.mKnownActiveStreamingServices.add(serviceForApp);
            try {
                int returnCode = streamingService.startStreaming(this.mSubscriptionId, serviceInfo.getServiceId(), serviceCallback);
                if (returnCode == -1) {
                    close();
                    throw new IllegalStateException("Middleware must not return an unknown error code");
                } else if (returnCode == 0) {
                    return serviceForApp;
                } else {
                    sendErrorToApp(returnCode, null);
                    return null;
                }
            } catch (RemoteException e) {
                Log.w(LOG_TAG, "Remote process died");
                this.mService.set(null);
                sIsInitialized.set(false);
                sendErrorToApp(3, null);
                return null;
            }
        } else {
            throw new IllegalStateException("Middleware not yet bound");
        }
    }

    public void onStreamingServiceStopped(StreamingService service) {
        this.mKnownActiveStreamingServices.remove(service);
    }

    private int bindAndInitialize() {
        return MbmsUtils.startBinding(this.mContext, MBMS_STREAMING_SERVICE_ACTION, new ServiceConnection() {
            /* class android.telephony.MbmsStreamingSession.AnonymousClass3 */

            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName name, IBinder service) {
                IMbmsStreamingService streamingService = IMbmsStreamingService.Stub.asInterface(service);
                try {
                    int result = streamingService.initialize(MbmsStreamingSession.this.mInternalCallback, MbmsStreamingSession.this.mSubscriptionId);
                    if (result == -1) {
                        MbmsStreamingSession.this.close();
                        throw new IllegalStateException("Middleware must not return an unknown error code");
                    } else if (result != 0) {
                        MbmsStreamingSession.this.sendErrorToApp(result, "Error returned during initialization");
                        MbmsStreamingSession.sIsInitialized.set(false);
                    } else {
                        try {
                            streamingService.asBinder().linkToDeath(MbmsStreamingSession.this.mDeathRecipient, 0);
                            MbmsStreamingSession.this.mService.set(streamingService);
                        } catch (RemoteException e) {
                            MbmsStreamingSession.this.sendErrorToApp(3, "Middleware lost during initialization");
                            MbmsStreamingSession.sIsInitialized.set(false);
                        }
                    }
                } catch (RemoteException e2) {
                    Log.e(MbmsStreamingSession.LOG_TAG, "Service died before initialization");
                    MbmsStreamingSession.this.sendErrorToApp(103, e2.toString());
                    MbmsStreamingSession.sIsInitialized.set(false);
                } catch (RuntimeException e3) {
                    Log.e(MbmsStreamingSession.LOG_TAG, "Runtime exception during initialization");
                    MbmsStreamingSession.this.sendErrorToApp(103, e3.toString());
                    MbmsStreamingSession.sIsInitialized.set(false);
                }
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName name) {
                MbmsStreamingSession.sIsInitialized.set(false);
                MbmsStreamingSession.this.mService.set(null);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendErrorToApp(int errorCode, String message) {
        try {
            this.mInternalCallback.onError(errorCode, message);
        } catch (RemoteException e) {
        }
    }
}
