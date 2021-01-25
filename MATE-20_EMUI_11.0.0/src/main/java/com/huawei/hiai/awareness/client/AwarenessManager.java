package com.huawei.hiai.awareness.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.hiai.awareness.log.Logger;
import com.huawei.hiai.awareness.service.IAwarenessService;
import com.huawei.hiai.awareness.util.ClientUtil;
import java.util.concurrent.locks.ReentrantLock;

public class AwarenessManager {
    private static final String TAG = "AwarenessManager";
    private IAwarenessService awarenessService;
    private AwarenessServiceConnection connection;
    private Context context;
    private boolean isConnected = false;
    private final ReentrantLock lock = new ReentrantLock();
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        /* class com.huawei.hiai.awareness.client.AwarenessManager.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.i(AwarenessManager.TAG, "onServiceConnected");
            AwarenessManager.this.lock.lock();
            try {
                AwarenessManager.this.awarenessService = IAwarenessService.Stub.asInterface(service);
                AwarenessManager.this.isConnected = true;
                if (AwarenessManager.this.connection != null) {
                    AwarenessManager.this.connection.onConnected();
                }
            } finally {
                AwarenessManager.this.lock.unlock();
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            Logger.i(AwarenessManager.TAG, "onServiceDisconnected");
            AwarenessManager.this.lock.lock();
            try {
                AwarenessManager.this.awarenessService = null;
                AwarenessManager.this.isConnected = false;
                if (AwarenessManager.this.connection != null) {
                    AwarenessManager.this.connection.onDisconnected();
                }
            } finally {
                AwarenessManager.this.lock.unlock();
            }
        }
    };

    public AwarenessManager(Context context2) {
        Logger.i(TAG, "AwarenessManager constructor");
        this.context = context2;
    }

    public boolean connectService(AwarenessServiceConnection awarenessServiceConnection) {
        Logger.i(TAG, "connectService");
        if (awarenessServiceConnection == null) {
            Logger.e(TAG, "AwarenessServiceConnection could not be null");
            return false;
        }
        Context context2 = this.context;
        if (context2 == null) {
            Logger.e(TAG, "Initialization context could not be null");
            return false;
        } else if (!ClientUtil.checkAwarenessApkInstalled(context2)) {
            Logger.e(TAG, "HiAiEngine CA apk is not installed");
            return false;
        } else if (this.isConnected) {
            Logger.e(TAG, "Service is already connected.");
            return true;
        } else {
            this.lock.lock();
            try {
                this.connection = awarenessServiceConnection;
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.huawei.hiai", "com.huawei.hiai.awareness.service.AwarenessService"));
                intent.setAction("com.huawei.hiai.awareness.IAwarenessService");
                intent.putExtra("LAUNCH_AWARENESS_PACKAGE_NAME", this.context.getPackageName());
                try {
                    return this.context.bindService(intent, this.serviceConnection, 1);
                } catch (SecurityException e) {
                    Logger.e(TAG, "Exception in binding the service");
                    this.lock.unlock();
                    return false;
                }
            } finally {
                this.lock.unlock();
            }
        }
    }

    public boolean disconnectService() {
        Logger.i(TAG, "disconnectService");
        if (this.context == null) {
            Logger.e(TAG, "Context is null, could not disconnect");
            return false;
        } else if (!this.isConnected) {
            Logger.e(TAG, "Service not connected yet, could not disconnect");
            return true;
        } else {
            this.lock.lock();
            try {
                this.context.unbindService(this.serviceConnection);
                this.awarenessService = null;
                this.isConnected = false;
                if (this.connection != null) {
                    this.connection.onDisconnected();
                    this.connection = null;
                }
                return true;
            } finally {
                this.lock.unlock();
            }
        }
    }

    public boolean dispatch(AwarenessRequest request) {
        Logger.i(TAG, "dispatch");
        if (request == null) {
            Logger.e(TAG, "Not allowed nullable AwarenessRequest!");
            return false;
        } else if (!this.isConnected) {
            Logger.e(TAG, "AwarenessService is not connected!");
            return false;
        } else {
            String packageName = this.context.getPackageName();
            request.setPackageName(packageName);
            Logger.i(TAG, "dispatch request, messageType=", request.getMessageType(), ", packageName=", packageName);
            boolean ret = false;
            try {
                ret = this.awarenessService.accept(request.toEnvelope());
            } catch (RemoteException e) {
                Logger.e(TAG, "RemoteException in dispatch");
            }
            Logger.i(TAG, "dispatch request, return=", Boolean.valueOf(ret));
            return ret;
        }
    }
}
