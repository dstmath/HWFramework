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
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final int MSG_HANDLE_TP_EVENT = 1;
    private static final String TAG = "HwTpManager";
    private static volatile HwTpManager mInstance = null;
    /* access modifiers changed from: private */
    public TpEventHandler mHandler;
    private HandlerThread mHandlerThread = new HandlerThread(TAG);
    /* access modifiers changed from: private */
    public boolean mHasSet = false;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private final ServiceNotification mServiceNotification = new ServiceNotification();
    /* access modifiers changed from: private */
    public ITouchscreen mTPHidlService;
    private List<TpCallback> mTpCallbacks = new CopyOnWriteArrayList();
    private String mTpCmdStatus = null;
    private TpHalCallback mTpHalCallback;

    final class HidlDeathRecipient implements IHwBinder.DeathRecipient {
        HidlDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            Log.w(HwTpManager.TAG, "TP service has died");
            synchronized (HwTpManager.this.mLock) {
                boolean unused = HwTpManager.this.mHasSet = false;
                ITouchscreen unused2 = HwTpManager.this.mTPHidlService = null;
            }
        }
    }

    final class ServiceNotification extends IServiceNotification.Stub {
        ServiceNotification() {
        }

        public void onRegistration(String fqName, String name, boolean preexisting) {
            HwTpManager.this.connectToHidlService();
        }
    }

    public interface TpCallback {
        void onTpEvent(int i, int i2);
    }

    private final class TpEventHandler extends Handler {
        TpEventHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            if (msg.what != 1) {
                Log.e(HwTpManager.TAG, "Invalid message");
            } else {
                HwTpManager.this.handleTpEvent(msg.arg1, msg.arg2);
            }
        }
    }

    private final class TpHalCallback extends ITPCallback.Stub {
        private TpHalCallback() {
        }

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
    }

    private HwTpManager() {
        this.mHandlerThread.start();
        this.mHandler = new TpEventHandler(this.mHandlerThread.getLooper());
        registerNotifyForService();
        connectToHidlService();
    }

    public static HwTpManager getInstance() {
        if (mInstance == null) {
            mInstance = new HwTpManager();
        }
        return mInstance;
    }

    /* access modifiers changed from: private */
    public void handleTpEvent(int event, int value) {
        synchronized (this.mTpCallbacks) {
            for (TpCallback cb : this.mTpCallbacks) {
                cb.onTpEvent(event, value);
            }
        }
    }

    private void registerNotifyForService() {
        try {
            if (!IServiceManager.getService().registerForNotifications(ITouchscreen.kInterfaceName, "", this.mServiceNotification)) {
                Log.e(TAG, "Failed to register service notification");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to register service notification, exception:" + e);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002a, code lost:
        android.util.Log.i(TAG, "Successfully connect to TP service.");
        r1 = r5.mTpCallbacks;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0033, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003a, code lost:
        if (r5.mTpCallbacks.size() <= 0) goto L_0x0040;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x003c, code lost:
        setTHPCallback(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0040, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0041, code lost:
        return;
     */
    public void connectToHidlService() {
        synchronized (this.mLock) {
            if (this.mTPHidlService == null) {
                try {
                    this.mTPHidlService = ITouchscreen.getService();
                    if (this.mTPHidlService == null) {
                        Log.e(TAG, "Failed to get ITouchscreen service");
                        return;
                    }
                    this.mTPHidlService.linkToDeath(new HidlDeathRecipient(), 0);
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to get service or start linkToDeath Exception " + e);
                }
            }
        }
    }

    private boolean setTHPCallback(boolean register) {
        synchronized (this.mLock) {
            try {
                if (this.mTPHidlService != null) {
                    TpHalCallback callback = null;
                    if (register) {
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
                    int code = this.mTPHidlService.hwTsSetCallback(callback);
                    if (DEBUG) {
                        Log.i(TAG, "hwTsSetCallback return code:" + code);
                    }
                    this.mHasSet = callback != null;
                    return true;
                }
                Log.w(TAG, "hwTsSetCallback failed, mTPHidlService is null");
            } catch (RemoteException e) {
                Log.e(TAG, "hwTsSetCallback Exception e = " + e);
            }
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002c, code lost:
        return true;
     */
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
                if (this.mTPHidlService != null) {
                    this.mTPHidlService.hwTsRunCommand(command, parameter, new ITouchscreen.hwTsRunCommandCallback(command, parameter) {
                        private final /* synthetic */ String f$1;
                        private final /* synthetic */ String f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void onValues(int i, String str) {
                            HwTpManager.lambda$hwTsRunCommand$0(HwTpManager.this, this.f$1, this.f$2, i, str);
                        }
                    });
                } else {
                    Log.w(TAG, "hwTsRunCommand failed, mTPHidlService is null");
                }
            } catch (RemoteException e) {
                Log.e(TAG, "hwTsRunCommand Exception e = " + e);
            }
            str = this.mTpCmdStatus;
        }
        return str;
    }

    public static /* synthetic */ void lambda$hwTsRunCommand$0(HwTpManager hwTpManager, String command, String parameter, int ret, String status) {
        if (DEBUG) {
            Log.i(TAG, "hwTsRunCommand command : " + command + ",parameter : " + parameter + ", ret = " + ret + ", status = " + status);
        }
        hwTpManager.mTpCmdStatus = status;
    }

    public int hwTsSetAftConfig(String config) {
        int retcode = -1;
        synchronized (this.mLock) {
            this.mTpCmdStatus = null;
            try {
                if (this.mTPHidlService != null) {
                    retcode = this.mTPHidlService.hwTsSetAftConfig(config);
                } else {
                    Log.w(TAG, "hwTsSetAftConfig failed, mTPHidlService is null");
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
}
