package android.rms.iaware;

import android.app.Application;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.rms.iaware.ISceneCallback;
import com.huawei.android.hardware.display.DisplayManagerGlobalEx;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AwareAppLiteSysLoadManager {
    private static final Object INSTANCE_LOCK = new Object();
    private static final String TAG = "AwareAppLiteSysLoadManager";
    private static AwareAppLiteSysLoadManager sInstance;
    private AtomicInteger mAppType = new AtomicInteger(-1);
    private Context mContext;
    private AtomicBoolean mIsEnabled = new AtomicBoolean(false);
    private AtomicBoolean mIsGameScene = new AtomicBoolean(false);
    private AtomicBoolean mIsInited = new AtomicBoolean(false);
    private String mPackageName;
    private AwareSceneCallback mSceneCallback;
    private SysLoadSdkCallback mSdkCallback;

    private AwareAppLiteSysLoadManager() {
    }

    public static AwareAppLiteSysLoadManager getInstance() {
        AwareAppLiteSysLoadManager awareAppLiteSysLoadManager;
        synchronized (INSTANCE_LOCK) {
            if (sInstance == null) {
                sInstance = new AwareAppLiteSysLoadManager();
            }
            awareAppLiteSysLoadManager = sInstance;
        }
        return awareAppLiteSysLoadManager;
    }

    public void init(String processName, Application app) {
        AwareUiRenderParallelManager.getInstance().init(processName, app);
        if (this.mIsInited.get()) {
            AwareLog.w(TAG, "has init");
            return;
        }
        this.mIsInited.set(true);
        if (processName != null && app != null) {
            this.mContext = app.getBaseContext();
            Context context = this.mContext;
            if (context != null) {
                this.mPackageName = context.getPackageName();
                this.mSdkCallback = new SysLoadSdkCallback();
                IAwareSdk.asyncReportDataWithCallback(3031, this.mPackageName, this.mSdkCallback, System.currentTimeMillis());
            }
        }
    }

    private class SysLoadSdkCallback extends Binder implements IInterface {
        private static final String SDK_CALLBACK_DESCRIPTOR = "android.rms.iaware.SysLoadSDKCallback";
        private static final int TRANSACTION_INIT_SYSLOAD_POLICY = 1;

        public SysLoadSdkCallback() {
            attachInterface(this, SDK_CALLBACK_DESCRIPTOR);
        }

        @Override // android.os.Binder
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
                AtomicBoolean atomicBoolean = AwareAppLiteSysLoadManager.this.mIsEnabled;
                if (data.readInt() == 1) {
                    z = true;
                }
                atomicBoolean.set(z);
                AwareAppLiteSysLoadManager.this.mAppType.set(data.readInt());
                AwareAppLiteSysLoadManager.this.registerAwareSceneCallback();
                return true;
            } catch (SecurityException e) {
                AwareLog.e(AwareAppLiteSysLoadManager.TAG, "enforceInterface SDK_CALLBACK_DESCRIPTOR failed");
                return false;
            }
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }
    }

    /* access modifiers changed from: private */
    public final class AwareSceneCallback extends ISceneCallback.Stub {
        private AwareSceneCallback() {
        }

        public void onSceneChanged(int scene, boolean start, int uid, int pid, String pkg) {
            AwareLog.d(AwareAppLiteSysLoadManager.TAG, "onSceneChanged: secne =" + scene + " start=" + start);
            if ((scene & 2) != 0) {
                AwareAppLiteSysLoadManager.this.mIsGameScene.set(start && AwareAppLiteSysLoadManager.this.mIsEnabled.get());
                DisplayManagerGlobalEx.setIAwareCacheEnable(AwareAppLiteSysLoadManager.this.mIsGameScene.get());
            }
        }
    }

    /* JADX WARN: Type inference failed for: r3v0, types: [android.rms.iaware.AwareAppLiteSysLoadManager$AwareSceneCallback, android.os.IBinder] */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Unknown variable types count: 1 */
    private void registerAwareSceneCallback() {
        int scenes;
        if (this.mIsEnabled.get() && this.mSceneCallback == null && (scenes = getScenes()) > 0) {
            this.mSceneCallback = new AwareSceneCallback();
            IAwareSdk.asyncReportDataWithCallback(3032, String.valueOf(scenes), (IBinder) this.mSceneCallback, System.currentTimeMillis());
        }
    }

    private int getScenes() {
        int type = this.mAppType.get();
        if (type == 9 || type == 305) {
            return 2;
        }
        return -1;
    }
}
