package com.huawei.antirelay;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.securityserver.IGeographyLocation;
import com.huawei.securityserver.IGeographyLocationCallback;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class AntiRelayManager {
    private static final int BIND_SERVICE_ERROR = -2;
    private static final int CORE_SIZE = 1;
    private static final int ENTITY_ID_LEN = 32;
    private static final int MULT_CALL_ERROR = -3;
    private static final int SESSION_ID_LEN = 8;
    private static final int SUCCESS = 0;
    private static final String TAG = "AntiRelayManager";
    private static final long THIRTY_MIN = 1800;
    private static final int WRONG_PARAMETER = -1;
    private static ScheduledExecutorService sServiceController = Executors.newScheduledThreadPool(1);
    private Context mContext;
    private byte[] mEntityIdBytes = new byte[32];
    private ScheduledFuture mFuture;
    private IGeographyLocation mGeographyLocationPlugin;
    private final ReentrantLock mLock = new ReentrantLock();

    public AntiRelayManager(Context context, byte[] entityId) {
        if (context != null && entityId != null && entityId.length == 32) {
            this.mContext = context;
            System.arraycopy(entityId, 0, this.mEntityIdBytes, 0, 32);
        }
    }

    public synchronized int startService(byte[] inSessionId, AntiRelayCallback callback) {
        Log.d(TAG, "StartService begin");
        if (!(inSessionId == null || callback == null)) {
            if (inSessionId.length == 8) {
                if (this.mGeographyLocationPlugin != null) {
                    return -3;
                }
                this.mGeographyLocationPlugin = getGeoLocalPlugin();
                if (this.mGeographyLocationPlugin == null) {
                    Log.e(TAG, "Error, mGeographyLocationPlugin is null.");
                    return -2;
                }
                int ret = -2;
                try {
                    ret = this.mGeographyLocationPlugin.startService(this.mEntityIdBytes, inSessionId, generateAidlCallback(callback));
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException " + e.getMessage());
                }
                runTimerToStopSerivce();
                return ret;
            }
        }
        return -1;
    }

    public synchronized int stopService() {
        Log.d(TAG, "StopService begin");
        if (this.mFuture != null && !this.mFuture.isDone()) {
            this.mFuture.cancel(false);
        }
        if (this.mGeographyLocationPlugin == null) {
            return -3;
        }
        try {
            this.mGeographyLocationPlugin.stopService();
            AntiRelayConnectManager.finishService(this.mContext);
            this.mGeographyLocationPlugin = null;
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "StopService meets remoteException");
            return -2;
        }
    }

    public synchronized int sendAntiRelayCommand(byte[] inSessionId, byte operationId, AntiRelayCallback callback) {
        Log.d(TAG, "SendAntiRelayCommand begin");
        if (!(inSessionId == null || callback == null)) {
            if (inSessionId.length == 8) {
                if (this.mGeographyLocationPlugin == null) {
                    return -2;
                }
                if (this.mFuture != null) {
                    this.mFuture.cancel(false);
                }
                try {
                    IGeographyLocationCallback walletCallback = generateAidlCallback(callback);
                    byte[] sessionId = new byte[8];
                    System.arraycopy(inSessionId, 0, sessionId, 0, 8);
                    this.mGeographyLocationPlugin.sendAntiRelayCommand(this.mEntityIdBytes, sessionId, operationId, walletCallback);
                    runTimerToStopSerivce();
                    return 0;
                } catch (RemoteException e) {
                    Log.e(TAG, "Error, mGeographyLocationPlugin meets RemoteException.");
                    return -2;
                }
            }
        }
        return -1;
    }

    private synchronized int setConfig(HashMap input) {
        if (this.mFuture != null) {
            this.mFuture.cancel(false);
        }
        if (this.mGeographyLocationPlugin != null) {
            try {
                int ret = this.mGeographyLocationPlugin.changeParams(input);
                runTimerToStopSerivce();
                return ret;
            } catch (RemoteException e) {
                Log.e(TAG, "Error, mGeographyLocationPlugin meets RemoteException.");
            }
        }
        return -2;
    }

    private IGeographyLocation getGeoLocalPlugin() {
        this.mLock.lock();
        try {
            if (this.mGeographyLocationPlugin != null) {
                return this.mGeographyLocationPlugin;
            }
            Optional<IGeographyLocation> remoteService = AntiRelayConnectManager.getRemoteService(this.mContext);
            if (!remoteService.isPresent()) {
                Log.e(TAG, "Error, mGeographyLocationPlugin is null.");
            } else {
                this.mGeographyLocationPlugin = remoteService.get();
            }
            this.mLock.unlock();
            return this.mGeographyLocationPlugin;
        } finally {
            this.mLock.unlock();
        }
    }

    private void runTimerToStopSerivce() {
        this.mFuture = sServiceController.schedule(new Runnable() {
            /* class com.huawei.antirelay.AntiRelayManager.AnonymousClass1 */

            public void run() {
                AntiRelayManager.this.stopService();
            }
        }, THIRTY_MIN, TimeUnit.SECONDS);
    }

    private IGeographyLocationCallback generateAidlCallback(final AntiRelayCallback walletCallback) {
        return new IGeographyLocationCallback.Stub() {
            /* class com.huawei.antirelay.AntiRelayManager.AnonymousClass2 */

            @Override // com.huawei.securityserver.IGeographyLocationCallback
            public void antiRelayServiceCb(byte result, byte[] sessionId) throws RemoteException {
                walletCallback.antiRelayServiceCb(result, sessionId);
            }
        };
    }
}
