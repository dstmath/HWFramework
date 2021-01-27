package android.media.soundtrigger;

import android.annotation.SystemApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.soundtrigger.SoundTrigger;
import android.media.soundtrigger.ISoundTriggerDetectionService;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.function.pooled.PooledLambda;
import java.util.UUID;

@SystemApi
public abstract class SoundTriggerDetectionService extends Service {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = SoundTriggerDetectionService.class.getSimpleName();
    @GuardedBy({"mLock"})
    private final ArrayMap<UUID, ISoundTriggerDetectionServiceClient> mClients = new ArrayMap<>();
    private Handler mHandler;
    private final Object mLock = new Object();

    public abstract void onStopOperation(UUID uuid, Bundle bundle, int i);

    /* access modifiers changed from: protected */
    @Override // android.content.ContextWrapper
    public final void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        this.mHandler = new Handler(base.getMainLooper());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void setClient(UUID uuid, Bundle params, ISoundTriggerDetectionServiceClient client) {
        synchronized (this.mLock) {
            this.mClients.put(uuid, client);
        }
        onConnected(uuid, params);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void removeClient(UUID uuid, Bundle params) {
        synchronized (this.mLock) {
            this.mClients.remove(uuid);
        }
        onDisconnected(uuid, params);
    }

    public void onConnected(UUID uuid, Bundle params) {
    }

    public void onDisconnected(UUID uuid, Bundle params) {
    }

    public void onGenericRecognitionEvent(UUID uuid, Bundle params, int opId, SoundTrigger.RecognitionEvent event) {
        operationFinished(uuid, opId);
    }

    public void onError(UUID uuid, Bundle params, int opId, int status) {
        operationFinished(uuid, opId);
    }

    public final void operationFinished(UUID uuid, int opId) {
        try {
            synchronized (this.mLock) {
                ISoundTriggerDetectionServiceClient client = this.mClients.get(uuid);
                if (client == null) {
                    String str = LOG_TAG;
                    Log.w(str, "operationFinished called, but no client for " + uuid + ". Was this called after onDisconnected?");
                    return;
                }
                client.onOpFinished(opId);
            }
        } catch (RemoteException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "operationFinished, remote exception for client " + uuid, e);
        }
    }

    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        return new ISoundTriggerDetectionService.Stub() {
            /* class android.media.soundtrigger.SoundTriggerDetectionService.AnonymousClass1 */
            private final Object mBinderLock = new Object();
            @GuardedBy({"mBinderLock"})
            public final ArrayMap<UUID, Bundle> mParams = new ArrayMap<>();

            @Override // android.media.soundtrigger.ISoundTriggerDetectionService
            public void setClient(ParcelUuid puuid, Bundle params, ISoundTriggerDetectionServiceClient client) {
                UUID uuid = puuid.getUuid();
                synchronized (this.mBinderLock) {
                    this.mParams.put(uuid, params);
                }
                SoundTriggerDetectionService.this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$SoundTriggerDetectionService$1$LlOo7TiZplZCgGhS07DqYHocFcw.INSTANCE, SoundTriggerDetectionService.this, uuid, params, client));
            }

            @Override // android.media.soundtrigger.ISoundTriggerDetectionService
            public void removeClient(ParcelUuid puuid) {
                Bundle params;
                UUID uuid = puuid.getUuid();
                synchronized (this.mBinderLock) {
                    params = this.mParams.remove(uuid);
                }
                SoundTriggerDetectionService.this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$SoundTriggerDetectionService$1$pKR4r0FzOzoVczcnvLQIZNjkZZw.INSTANCE, SoundTriggerDetectionService.this, uuid, params));
            }

            @Override // android.media.soundtrigger.ISoundTriggerDetectionService
            public void onGenericRecognitionEvent(ParcelUuid puuid, int opId, SoundTrigger.GenericRecognitionEvent event) {
                Bundle params;
                UUID uuid = puuid.getUuid();
                synchronized (this.mBinderLock) {
                    params = this.mParams.get(uuid);
                }
                SoundTriggerDetectionService.this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$ISQYIYPBRBIOLBUJy7rrJWSiJg.INSTANCE, SoundTriggerDetectionService.this, uuid, params, Integer.valueOf(opId), event));
            }

            @Override // android.media.soundtrigger.ISoundTriggerDetectionService
            public void onError(ParcelUuid puuid, int opId, int status) {
                Bundle params;
                UUID uuid = puuid.getUuid();
                synchronized (this.mBinderLock) {
                    params = this.mParams.get(uuid);
                }
                SoundTriggerDetectionService.this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$oNgT3sYhSGVWlnU92bECo_ULGeY.INSTANCE, SoundTriggerDetectionService.this, uuid, params, Integer.valueOf(opId), Integer.valueOf(status)));
            }

            @Override // android.media.soundtrigger.ISoundTriggerDetectionService
            public void onStopOperation(ParcelUuid puuid, int opId) {
                Bundle params;
                UUID uuid = puuid.getUuid();
                synchronized (this.mBinderLock) {
                    params = this.mParams.get(uuid);
                }
                SoundTriggerDetectionService.this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$bPGNpvkCtpPW14oaI3pxn1e6JtQ.INSTANCE, SoundTriggerDetectionService.this, uuid, params, Integer.valueOf(opId)));
            }
        };
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        this.mClients.clear();
        return false;
    }
}
