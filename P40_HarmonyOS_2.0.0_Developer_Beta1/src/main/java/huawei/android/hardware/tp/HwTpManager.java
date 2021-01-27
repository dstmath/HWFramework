package huawei.android.hardware.tp;

import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IHwBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import vendor.huawei.hardware.tp.V1_0.ITPCallback;
import vendor.huawei.hardware.tp.V1_0.ITouchscreen;

public class HwTpManager {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final int MSG_HANDLE_TP_EVENT = 1;
    private static final String TAG = "HwTpManager";
    private static volatile HwTpManager sInstance = null;
    private TpEventHandler mHandler;
    private HandlerThread mHandlerThread = new HandlerThread(TAG);
    private boolean mHasSet = false;
    private final Object mLock = new Object();
    private final ServiceNotification mServiceNotification = new ServiceNotification();
    private List<TpCallback> mTpCallbacks = new CopyOnWriteArrayList();
    private String mTpCmdStatus = null;
    private TpHalCallback mTpHalCallback;
    private ITouchscreen mTpHidlService;

    public interface TpCallback {
        void onTpEvent(int i, int i2);
    }

    private HwTpManager() {
        this.mHandlerThread.start();
        this.mHandler = new TpEventHandler(this.mHandlerThread.getLooper());
        registerNotifyForService();
        connectToHidlService();
    }

    public static HwTpManager getInstance() {
        if (sInstance == null) {
            sInstance = new HwTpManager();
        }
        return sInstance;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleTpEvent(int event, int value) {
        synchronized (this.mTpCallbacks) {
            for (TpCallback cb : this.mTpCallbacks) {
                cb.onTpEvent(event, value);
            }
        }
    }

    private void registerNotifyForService() {
        boolean isRegisted = false;
        try {
            IServiceManager serviceManager = IServiceManager.getService();
            if (serviceManager != null) {
                isRegisted = serviceManager.registerForNotifications(ITouchscreen.kInterfaceName, "", this.mServiceNotification);
            }
            if (!isRegisted) {
                Log.e(TAG, "Failed to register service notification");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to register service notification, exception:" + e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void connectToHidlService() {
        synchronized (this.mLock) {
            if (this.mTpHidlService == null) {
                try {
                    this.mTpHidlService = ITouchscreen.getService();
                    if (this.mTpHidlService == null) {
                        Log.e(TAG, "Failed to get ITouchscreen service");
                        return;
                    }
                    this.mTpHidlService.linkToDeath(new HidlDeathRecipient(), 0);
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to get service or start linkToDeath Exception " + e);
                    return;
                }
            } else {
                return;
            }
        }
        Log.i(TAG, "Successfully connect to TP service.");
        synchronized (this.mTpCallbacks) {
            if (this.mTpCallbacks.size() > 0) {
                setTHPCallback(true);
            }
        }
    }

    private boolean setTHPCallback(boolean isRegister) {
        synchronized (this.mLock) {
            try {
                if (this.mTpHidlService != null) {
                    TpHalCallback callback = null;
                    if (isRegister) {
                        if (this.mHasSet) {
                            if (DEBUG) {
                                Log.i(TAG, "hwTsSetCallback has already set.");
                            }
                            return true;
                        }
                        if (this.mTpHalCallback == null) {
                            this.mTpHalCallback = new TpHalCallback();
                        }
                        callback = this.mTpHalCallback;
                    }
                    int code = this.mTpHidlService.hwTsSetCallback(callback);
                    if (DEBUG) {
                        Log.i(TAG, "hwTsSetCallback return code:" + code);
                    }
                    this.mHasSet = callback != null;
                    return true;
                }
                Log.w(TAG, "hwTsSetCallback failed, mTpHidlService is null");
                return false;
            } catch (RemoteException e) {
                Log.e(TAG, "hwTsSetCallback Exception e = " + e);
            }
        }
    }

    public boolean registerTpCallback(TpCallback cb) {
        synchronized (this.mTpCallbacks) {
            if (this.mTpCallbacks.contains(cb)) {
                Log.i(TAG, "registerTpCallback cb alrady registered.");
                return true;
            } else if (!setTHPCallback(true)) {
                return false;
            } else {
                this.mTpCallbacks.add(cb);
                if (DEBUG) {
                    Log.i(TAG, "registerTpCallback cb successfully.");
                }
                return true;
            }
        }
    }

    public boolean unregisterTpCallback(TpCallback cb) {
        synchronized (this.mTpCallbacks) {
            if (!this.mTpCallbacks.contains(cb)) {
                Log.w(TAG, "unregisterTpCallback cb not registered.");
                return false;
            } else if (!setTHPCallback(false)) {
                return false;
            } else {
                this.mTpCallbacks.remove(cb);
                if (DEBUG) {
                    Log.i(TAG, "unregisterTpCallback cb successfully.");
                }
                return true;
            }
        }
    }

    public String hwTsRunCommand(String command, String parameter) {
        String str;
        synchronized (this.mLock) {
            this.mTpCmdStatus = null;
            try {
                if (this.mTpHidlService != null) {
                    this.mTpHidlService.hwTsRunCommand(command, parameter, new ITouchscreen.hwTsRunCommandCallback(command, parameter) {
                        /* class huawei.android.hardware.tp.$$Lambda$HwTpManager$PuGfrMz_pIKDw283eE1svlyjvV4 */
                        private final /* synthetic */ String f$1;
                        private final /* synthetic */ String f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // vendor.huawei.hardware.tp.V1_0.ITouchscreen.hwTsRunCommandCallback
                        public final void onValues(int i, String str) {
                            HwTpManager.this.lambda$hwTsRunCommand$0$HwTpManager(this.f$1, this.f$2, i, str);
                        }
                    });
                } else {
                    Log.w(TAG, "hwTsRunCommand failed, mTpHidlService is null");
                }
            } catch (RemoteException e) {
                Log.e(TAG, "hwTsRunCommand Exception e = " + e);
            }
            str = this.mTpCmdStatus;
        }
        return str;
    }

    public /* synthetic */ void lambda$hwTsRunCommand$0$HwTpManager(String command, String parameter, int ret, String status) {
        if (DEBUG) {
            Log.i(TAG, "hwTsRunCommand command : " + command + ",parameter : " + parameter + ", ret = " + ret + ", status = " + status);
        }
        this.mTpCmdStatus = status;
    }

    public int hwTsSetAftConfig(String config) {
        int retcode = -1;
        synchronized (this.mLock) {
            this.mTpCmdStatus = null;
            try {
                if (this.mTpHidlService != null) {
                    retcode = this.mTpHidlService.hwTsSetAftConfig(config);
                } else {
                    Log.w(TAG, "hwTsSetAftConfig failed, mTpHidlService is null");
                }
            } catch (RemoteException e) {
                Log.e(TAG, "hwTsSetAftConfig Exception e = " + e);
            }
            if (DEBUG) {
                Log.i(TAG, "hwTsSetAftConfig config:" + config + ", retcode=" + retcode);
            }
        }
        return retcode;
    }

    /* access modifiers changed from: private */
    public final class TpEventHandler extends Handler {
        TpEventHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what != 1) {
                Log.e(HwTpManager.TAG, "Invalid message");
            } else {
                HwTpManager.this.handleTpEvent(msg.arg1, msg.arg2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class ServiceNotification extends IServiceNotification.Stub {
        ServiceNotification() {
        }

        public void onRegistration(String fqName, String name, boolean isPreexisting) {
            HwTpManager.this.connectToHidlService();
        }
    }

    /* access modifiers changed from: package-private */
    public final class HidlDeathRecipient implements IHwBinder.DeathRecipient {
        HidlDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            Log.w(HwTpManager.TAG, "TP service has died");
            synchronized (HwTpManager.this.mLock) {
                HwTpManager.this.mHasSet = false;
                HwTpManager.this.mTpHidlService = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public final class TpHalCallback extends ITPCallback.Stub {
        private TpHalCallback() {
        }

        @Override // vendor.huawei.hardware.tp.V1_0.ITPCallback
        public void notifyTHPEvents(int event, int value) {
            if (HwTpManager.DEBUG) {
                Log.i(HwTpManager.TAG, "Receive a TP driver event=" + event + ", value=" + value);
            }
            HwTpManager.this.mHandler.removeMessages(1);
            Message msg = HwTpManager.this.mHandler.obtainMessage(1);
            msg.arg1 = event;
            msg.arg2 = value;
            HwTpManager.this.mHandler.sendMessage(msg);
        }

        @Override // vendor.huawei.hardware.tp.V1_0.ITPCallback
        public void notifyTPEvents(int eventClass, int eventCode, String extraInfo) {
            if (HwTpManager.DEBUG) {
                Log.i(HwTpManager.TAG, "Receive a TP driver eventClass=" + eventClass + ", eventCode=" + eventCode + ",extraInfo =" + extraInfo);
            }
        }
    }
}
