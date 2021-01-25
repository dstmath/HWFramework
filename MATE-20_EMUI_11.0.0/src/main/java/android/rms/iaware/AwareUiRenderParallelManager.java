package android.rms.iaware;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.view.InputEvent;
import android.view.MotionEvent;
import com.huawei.android.os.SystemPropertiesEx;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareUiRenderParallelManager {
    private static final String LAUNCHER_PKG = "com.huawei.android.launcher";
    private static final Object LOCK = new Object();
    private static final boolean LOG_SWITCH = SystemPropertiesEx.getBoolean("persist.sys.iaware.uirender.log.switch", false);
    private static final int MSG_RETRY_CONNECT_AWARE = 1;
    private static final int RETRY_CONNECT_AWARE_DELAY_TIME = 60000;
    private static final String TAG = "AwareUiRenderParallelManager";
    private static final String THREAD_NAME = "queued-work-looper-ui-render-parallel";
    private static AwareUiRenderParallelManager sInstance = null;
    private long mDispatchInterval = 0;
    private UiRenderHandler mHandler = null;
    private HandlerThread mHandlerThread;
    private AtomicBoolean mIsAnimInAdvance = new AtomicBoolean(false);
    private AtomicBoolean mIsConnected = new AtomicBoolean(false);
    private AtomicBoolean mIsInited = new AtomicBoolean(false);
    private AtomicBoolean mIsInputInAdvance = new AtomicBoolean(false);
    private AtomicBoolean mIsTouchDown = new AtomicBoolean(false);
    private String mPkgName;
    private UiRenderSdkCallback mUiRenderSdkCallback = null;

    public static AwareUiRenderParallelManager getInstance() {
        AwareUiRenderParallelManager awareUiRenderParallelManager;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new AwareUiRenderParallelManager();
            }
            awareUiRenderParallelManager = sInstance;
        }
        return awareUiRenderParallelManager;
    }

    public void init(String processName, Application app) {
        if (processName != null && app != null) {
            Context context = app.getBaseContext();
            if (context != null) {
                ApplicationInfo appInfo = context.getApplicationInfo();
                if (appInfo != null && appInfo.uid >= 10000) {
                    this.mPkgName = context.getPackageName();
                    if (isStrEmpty(this.mPkgName) || !isMainProcess(processName, this.mPkgName)) {
                        if (LOG_SWITCH) {
                            Log.d(TAG, "is empty pkg or not in main process!");
                        }
                    } else if (!this.mIsInited.get()) {
                        if (LOG_SWITCH) {
                            Log.d(TAG, "init AwareUiRenderParallel, processName: " + processName + ", pkgName: " + this.mPkgName);
                        }
                        this.mIsInited.set(true);
                        this.mUiRenderSdkCallback = new UiRenderSdkCallback();
                        processIfInitBeforeAware();
                        IAwareSdk.asyncReportDataWithCallback(3040, this.mPkgName, this.mUiRenderSdkCallback, System.currentTimeMillis());
                    }
                } else if (LOG_SWITCH) {
                    Log.d(TAG, "uid is less than 10000!");
                }
            } else if (LOG_SWITCH) {
                Log.d(TAG, "context is null!");
            }
        } else if (LOG_SWITCH) {
            Log.d(TAG, "processName is null");
        }
    }

    private void processIfInitBeforeAware() {
        if (LAUNCHER_PKG.equals(this.mPkgName)) {
            this.mHandlerThread = new HandlerThread(THREAD_NAME, 10);
            this.mHandlerThread.start();
            Looper looper = this.mHandlerThread.getLooper();
            if (looper != null) {
                this.mHandler = new UiRenderHandler(looper);
                this.mHandler.sendEmptyMessageDelayed(1, 60000);
            }
        }
    }

    private boolean isStrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private boolean isMainProcess(String processName, String pkgName) {
        return processName.equals(pkgName);
    }

    public boolean isInputInAdvance() {
        return this.mIsInputInAdvance.get() && !DynBufManagerImpl.getDefault().isLastVyncMultiView() && !DynBufManagerImpl.getDefault().isLastVsyncSurfaceTextureUpdate();
    }

    public boolean isAnimInAdvance() {
        return this.mIsAnimInAdvance.get() && !DynBufManagerImpl.getDefault().isLastVyncMultiView() && !DynBufManagerImpl.getDefault().isLastVsyncSurfaceTextureUpdate();
    }

    public boolean isTouchDownEvent() {
        return this.mIsTouchDown.get();
    }

    public void notifyInputEvent(InputEvent event) {
        if (event instanceof MotionEvent) {
            int action = ((MotionEvent) event).getAction();
            if (action != 0) {
                if (action != 1) {
                    if (action != 2) {
                        if (action != 3) {
                            return;
                        }
                    }
                }
                this.mIsTouchDown.set(false);
                return;
            } else if (LOG_SWITCH) {
                Log.d(TAG, "touch down event");
            }
            if (!this.mIsTouchDown.get()) {
                this.mIsTouchDown.set(true);
            }
        }
    }

    /* access modifiers changed from: private */
    public class UiRenderSdkCallback extends Binder implements IInterface {
        private static final String SDK_CALLBACK_DESCRIPTOR = "android.rms.iaware.UiRenderSdkCallback";
        private static final int TRANSACTION_INIT_UI_RENDER_POLICY = 1;

        public UiRenderSdkCallback() {
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
                if (AwareUiRenderParallelManager.this.mIsInited.get()) {
                    AwareUiRenderParallelManager.this.mIsInputInAdvance.set(data.readInt() == 1);
                    AtomicBoolean atomicBoolean = AwareUiRenderParallelManager.this.mIsAnimInAdvance;
                    if (data.readInt() == 1) {
                        z = true;
                    }
                    atomicBoolean.set(z);
                    AwareUiRenderParallelManager.this.mIsConnected.set(true);
                    AwareUiRenderParallelManager.this.printSwitch();
                }
                AwareUiRenderParallelManager.this.finishHandlerIfNeed();
                return true;
            } catch (SecurityException e) {
                Log.e(AwareUiRenderParallelManager.TAG, "enforceInterface SDK_CALLBACK_DESCRIPTOR failed");
                return false;
            }
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }
    }

    /* access modifiers changed from: private */
    public class UiRenderHandler extends Handler {
        public UiRenderHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                AwareUiRenderParallelManager.this.retryConnectAware();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void printSwitch() {
        if (LOG_SWITCH) {
            Log.d(TAG, "mIsInputInAdvance: " + this.mIsInputInAdvance.get() + " ,mIsAnimInAdvance: " + this.mIsAnimInAdvance.get());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void retryConnectAware() {
        if (!this.mIsConnected.get()) {
            IAwareSdk.asyncReportDataWithCallback(3040, this.mPkgName, this.mUiRenderSdkCallback, System.currentTimeMillis());
            Log.d(TAG, "reconnect iaware sdk, pkg: " + this.mPkgName);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void finishHandlerIfNeed() {
        if (this.mHandler != null) {
            this.mHandler = null;
            Log.d(TAG, "finish handler");
        }
        HandlerThread handlerThread = this.mHandlerThread;
        if (handlerThread != null) {
            handlerThread.quit();
            this.mHandlerThread = null;
            Log.d(TAG, "finish handlerThread");
        }
    }
}
