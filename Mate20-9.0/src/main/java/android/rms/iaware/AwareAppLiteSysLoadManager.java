package android.rms.iaware;

import android.app.Application;
import android.app.ApplicationPackageManager;
import android.content.Context;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.rms.iaware.ISceneCallback;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AwareAppLiteSysLoadManager {
    private static final int DELAY_TIME = 60000;
    private static final int MSG_REINT = 1;
    private static final int REINIT_TIME = 60000;
    private static final String TAG = "AwareAppLiteSysLoadManager";
    private static AwareAppLiteSysLoadManager sInstance;
    /* access modifiers changed from: private */
    public AtomicInteger mAppType = new AtomicInteger(-1);
    private Context mContext;
    private volatile SysLoadHandler mHandler = null;
    private HandlerThread mHandlerThread;
    /* access modifiers changed from: private */
    public AtomicBoolean mIAwareLiteEnabled = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public AtomicBoolean mIsGameScene = new AtomicBoolean(false);
    private AtomicBoolean mIsInited = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public String mPackageName;
    /* access modifiers changed from: private */
    public SysLoadSDKCallback mSDKCallback;
    private IAwareSceneCallback mSceneCallback;

    private final class IAwareSceneCallback extends ISceneCallback.Stub {
        private IAwareSceneCallback() {
        }

        public void onSceneChanged(int scene, boolean start, int uid, int pid, String pkg) {
            AwareLog.d(AwareAppLiteSysLoadManager.TAG, "onSceneChanged: secne =" + scene + " start=" + start);
            if ((scene & 2) != 0) {
                AwareAppLiteSysLoadManager.this.mIsGameScene.set(start && AwareAppLiteSysLoadManager.this.mIAwareLiteEnabled.get());
                DisplayManagerGlobal.getInstance().setIAwareCacheEnable(AwareAppLiteSysLoadManager.this.mIsGameScene.get());
            }
        }
    }

    private final class SysLoadHandler extends Handler {
        public SysLoadHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                IAwareSdk.asyncReportDataWithCallback(3031, AwareAppLiteSysLoadManager.this.mPackageName, AwareAppLiteSysLoadManager.this.mSDKCallback, System.currentTimeMillis());
            }
        }
    }

    private class SysLoadSDKCallback extends Binder implements IInterface {
        private static final String SDK_CALLBACK_DESCRIPTOR = "android.rms.iaware.SysLoadSDKCallback";
        private static final int TRANSACTION_initSysLoadPolicy = 1;

        public SysLoadSDKCallback() {
            attachInterface(this, SDK_CALLBACK_DESCRIPTOR);
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code < 1 || code > 16777215) {
                return super.onTransact(code, data, reply, flags);
            }
            boolean z = false;
            if (code != 1) {
                return false;
            }
            try {
                data.enforceInterface(SDK_CALLBACK_DESCRIPTOR);
                AtomicBoolean access$000 = AwareAppLiteSysLoadManager.this.mIAwareLiteEnabled;
                if (data.readInt() == 1) {
                    z = true;
                }
                access$000.set(z);
                AwareAppLiteSysLoadManager.this.mAppType.set(data.readInt());
                AwareAppLiteSysLoadManager.this.registerIAwareSceneCallback();
                ApplicationPackageManager.setUseCache(AwareAppLiteSysLoadManager.this.mIAwareLiteEnabled.get());
                AwareAppLiteSysLoadManager.this.finishInit();
                return true;
            } catch (SecurityException e) {
                AwareLog.e(AwareAppLiteSysLoadManager.TAG, "enforceInterface SDK_CALLBACK_DESCRIPTOR failed");
                return false;
            }
        }

        public IBinder asBinder() {
            return this;
        }
    }

    public static synchronized AwareAppLiteSysLoadManager getInstance() {
        AwareAppLiteSysLoadManager awareAppLiteSysLoadManager;
        synchronized (AwareAppLiteSysLoadManager.class) {
            if (sInstance == null) {
                sInstance = new AwareAppLiteSysLoadManager();
            }
            awareAppLiteSysLoadManager = sInstance;
        }
        return awareAppLiteSysLoadManager;
    }

    private AwareAppLiteSysLoadManager() {
    }

    public boolean isLiteSysLoadEnable() {
        return this.mIAwareLiteEnabled.get();
    }

    public boolean isInSysLoadScene(int scene) {
        if (scene != 2) {
            return false;
        }
        return this.mIsGameScene.get();
    }

    public void init(String processName, Application app) {
        if (this.mIsInited.get()) {
            AwareLog.w(TAG, "has init");
            return;
        }
        this.mIsInited.set(true);
        if (processName != null && app != null) {
            this.mContext = app.getBaseContext();
            if (this.mContext != null) {
                this.mPackageName = this.mContext.getPackageName();
                this.mSDKCallback = new SysLoadSDKCallback();
                reInit();
                IAwareSdk.asyncReportDataWithCallback(3031, this.mPackageName, this.mSDKCallback, System.currentTimeMillis());
            }
        }
    }

    /* JADX WARNING: type inference failed for: r3v0, types: [android.rms.iaware.AwareAppLiteSysLoadManager$IAwareSceneCallback, android.os.IBinder] */
    /* access modifiers changed from: private */
    public void registerIAwareSceneCallback() {
        if (this.mIAwareLiteEnabled.get() && this.mSceneCallback == null) {
            int scenes = getScenes();
            if (scenes > 0) {
                this.mSceneCallback = new IAwareSceneCallback();
                IAwareSdk.asyncReportDataWithCallback(3032, String.valueOf(scenes), this.mSceneCallback, System.currentTimeMillis());
            }
        }
    }

    private int getScenes() {
        int type = this.mAppType.get();
        if (type == 9 || type == 305) {
            return 2;
        }
        return -1;
    }

    private void reInit() {
        if (SystemClock.elapsedRealtime() <= 60000) {
            if (this.mHandlerThread == null) {
                this.mHandlerThread = new HandlerThread("LiteSysLoadStart", 10);
            }
            this.mHandlerThread.start();
            if (this.mHandler == null) {
                this.mHandler = new SysLoadHandler(this.mHandlerThread.getLooper());
            }
            this.mHandler.sendEmptyMessageDelayed(1, 60000);
        }
    }

    /* access modifiers changed from: private */
    public void finishInit() {
        AwareLog.d(TAG, "finishInit");
        if (this.mHandler != null) {
            this.mHandler.removeMessages(1);
            this.mHandler = null;
        }
        if (this.mHandlerThread != null) {
            this.mHandlerThread.quit();
            this.mHandlerThread = null;
        }
    }
}
