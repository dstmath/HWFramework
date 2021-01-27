package com.huawei.android.biometric;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.huawei.android.biometric.IFingerprintStateListener;

public class FingerprintStateNotifierEx {
    private static final String AOD_DOZE_STATE = "aod_doze_state";
    public static final int AUTH_FAIL = 3;
    public static final int AUTH_SUCCESS = 2;
    private static final Object CREATE_LOCK = new Object();
    public static final int ENTER_HBM = 0;
    public static final int EXIT_HBM = 1;
    private static final String TAG = FingerprintStateNotifierEx.class.getSimpleName();
    private static FingerprintStateNotifierEx sInstance;
    private ContentObserver mAodDozeCount = new ContentObserver(new Handler()) {
        /* class com.huawei.android.biometric.FingerprintStateNotifierEx.AnonymousClass2 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            int dozeState = Settings.Secure.getIntForUser(FingerprintStateNotifierEx.this.mContext.getContentResolver(), FingerprintStateNotifierEx.AOD_DOZE_STATE, 0, -2);
            String str = FingerprintStateNotifierEx.TAG;
            Log.i(str, "mAodDozeCount onChange dozeState=" + dozeState);
            if (dozeState != 1) {
                FingerprintStateNotifierEx.this.unInit();
            } else if (FingerprintStateNotifierEx.this.mFingerprintStateListener == null) {
                FingerprintStateNotifierEx.this.init();
            }
        }
    };
    private final Context mContext;
    private volatile IFingerprintStateListener mFingerprintStateListener;
    private final Object mLock = new Object();
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /* class com.huawei.android.biometric.FingerprintStateNotifierEx.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.w(FingerprintStateNotifierEx.TAG, "AODService connected success");
            FingerprintStateNotifierEx.this.mFingerprintStateListener = IFingerprintStateListener.Stub.asInterface(service);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            Log.w(FingerprintStateNotifierEx.TAG, "onServiceDisconnected");
            FingerprintStateNotifierEx.this.mFingerprintStateListener = null;
        }
    };

    private FingerprintStateNotifierEx(Context ctx) {
        this.mContext = ctx;
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(AOD_DOZE_STATE), true, this.mAodDozeCount, -1);
    }

    public static FingerprintStateNotifierEx getInstance(Context ctx) {
        synchronized (CREATE_LOCK) {
            if (sInstance == null) {
                sInstance = new FingerprintStateNotifierEx(ctx);
            }
        }
        return sInstance;
    }

    public void init() {
        Log.i(TAG, "Init");
        synchronized (this.mLock) {
            fetchRemoteListenerLocked();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unInit() {
        Log.i(TAG, "unInit");
        synchronized (this.mLock) {
            releaseRemoteListenerLocked();
        }
    }

    public void notifyStateChange(int newState) {
        if (this.mFingerprintStateListener == null) {
            synchronized (this.mLock) {
                if (this.mFingerprintStateListener == null) {
                    fetchRemoteListenerLocked();
                }
            }
        }
        IFingerprintStateListener fsl = this.mFingerprintStateListener;
        if (fsl != null) {
            String str = TAG;
            Log.i(str, "Notify state to remote listener: " + newState);
            try {
                fsl.onStateChange(newState);
            } catch (RemoteException e) {
                Log.i(TAG, "Notify state to remote listener failed");
            }
        } else {
            Log.i(TAG, "Null state listener");
        }
    }

    private void fetchRemoteListenerLocked() {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.huawei.aod", "com.huawei.aod.AODService"));
            this.mContext.bindServiceAsUser(intent, this.mServiceConnection, 1, UserHandle.CURRENT);
        } catch (RuntimeException e) {
            Log.e(TAG, "fetch remote listener failed");
        }
    }

    private void releaseRemoteListenerLocked() {
        try {
            this.mContext.unbindService(this.mServiceConnection);
            this.mFingerprintStateListener = null;
        } catch (RuntimeException e) {
            Log.e(TAG, "release remote listener failed");
        }
    }
}
