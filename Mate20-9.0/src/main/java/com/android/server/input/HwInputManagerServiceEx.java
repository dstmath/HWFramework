package com.android.server.input;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IHwBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.SomeArgs;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.huawei.android.hardware.input.IHwTHPEventListener;
import java.util.HashMap;
import vendor.huawei.hardware.tp.V1_0.ITPCallback;
import vendor.huawei.hardware.tp.V1_0.ITouchscreen;

public final class HwInputManagerServiceEx implements IHwInputManagerServiceEx {
    public static final String[] CALLING_PACKAGES = {"com.baidu.input_huawei"};
    private static final String CMD_EXE_SUCCESS = "OK";
    private static final String CMD_INFORM_APP_CRASH = "THP_InformUserAppCrash";
    protected static final boolean DEBUG = Log.HWINFO;
    private static final String HAS_SHOW_DIALOG = "has_show_dialog";
    private static final String IS_TABLET = SystemProperties.get("ro.build.characteristics", "");
    private static final String TAG = "HwInputManagerServiceEx";
    private static final int THP_HAL_DEATH_COOKIE = 1000;
    private AlertDialog mAlterSoftInputDialog = null;
    /* access modifiers changed from: private */
    public final Callbacks mCallbacks;
    final ArrayMap<IBinder, ClientState> mClients = new ArrayMap<>();
    private final Context mContext;
    private boolean mHasShow = false;
    private IHwInputManagerInner mImsInner = null;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private String mResult = null;
    private final ServiceNotification mServiceNotification = new ServiceNotification();
    private HALCallback mTHPCallback;
    /* access modifiers changed from: private */
    public ITouchscreen mTPHal = null;

    private static class Callbacks extends Handler {
        private static final int MSG_THP_INPUT_EVENT = 1;
        private final RemoteCallbackList<IHwTHPEventListener> mCallbacks = new RemoteCallbackList<>();

        public Callbacks(Looper looper) {
            super(looper);
        }

        public void register(IHwTHPEventListener callback) {
            this.mCallbacks.register(callback);
        }

        public void unregister(IHwTHPEventListener callback) {
            this.mCallbacks.unregister(callback);
        }

        public void handleMessage(Message msg) {
            SomeArgs args = (SomeArgs) msg.obj;
            int n = this.mCallbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    invokeCallback(this.mCallbacks.getBroadcastItem(i), msg.what, args);
                } catch (RemoteException e) {
                }
            }
            this.mCallbacks.finishBroadcast();
            args.recycle();
        }

        private void invokeCallback(IHwTHPEventListener callback, int what, SomeArgs args) throws RemoteException {
            if (what == 1) {
                callback.onHwTHPEvent(((Integer) args.arg1).intValue());
            }
        }

        /* access modifiers changed from: private */
        public void onHwTHPEvent(int event) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Integer.valueOf(event);
            obtainMessage(1, args).sendToTarget();
        }
    }

    public final class ClientState extends Binder implements IBinder.DeathRecipient {
        private final IBinder mAppToken;
        private final HashMap<IBinder, IHwTHPEventListener> mListeners = new HashMap<>();
        private final int mPid = Binder.getCallingPid();
        private final int mUid = Binder.getCallingUid();

        public ClientState(IBinder appToken) {
            this.mAppToken = appToken;
            try {
                this.mAppToken.linkToDeath(this, 0);
            } catch (RemoteException e) {
            }
        }

        public void addListenerLocked(IHwTHPEventListener listener) {
            this.mListeners.put(listener.asBinder(), listener);
        }

        public void removeListenerLocked(IHwTHPEventListener listener) {
            this.mListeners.remove(listener.asBinder());
            if (this.mListeners.size() == 0) {
                closeClientsLocked();
            }
        }

        private void closeClientsLocked() {
            HwInputManagerServiceEx.this.mClients.remove(this.mAppToken);
            this.mAppToken.unlinkToDeath(this, 0);
        }

        public void binderDied() {
            synchronized (HwInputManagerServiceEx.this.mClients) {
                Slog.d(HwInputManagerServiceEx.TAG, "Client died: " + this);
                HwInputManagerServiceEx.this.mClients.remove(this.mAppToken);
                if (HwInputManagerServiceEx.this.mClients.size() == 0) {
                    HwInputManagerServiceEx.this.setTHPCallback(null);
                }
            }
            String result = HwInputManagerServiceEx.this.runHwTHPCommand(HwInputManagerServiceEx.CMD_INFORM_APP_CRASH, "");
            if (HwInputManagerServiceEx.CMD_EXE_SUCCESS.equals(result)) {
                Slog.d(HwInputManagerServiceEx.TAG, "CMD_INFORM_APP_CRASH exec failed, result = " + result);
            }
        }

        public String toString() {
            return "Client: UID: " + this.mUid + " PID: " + this.mPid + " listener count: " + this.mListeners.size();
        }
    }

    final class DeathRecipient implements IHwBinder.DeathRecipient {
        DeathRecipient() {
        }

        public void serviceDied(long cookie) {
            Slog.w(HwInputManagerServiceEx.TAG, "TP service has died");
            if (cookie == 1000) {
                synchronized (HwInputManagerServiceEx.this.mLock) {
                    ITouchscreen unused = HwInputManagerServiceEx.this.mTPHal = null;
                }
            }
        }
    }

    private class HALCallback extends ITPCallback.Stub {
        private HALCallback() {
        }

        public void notifyTHPEvents(int event, int retval) {
            if (HwInputManagerServiceEx.DEBUG) {
                Slog.i(HwInputManagerServiceEx.TAG, "Receive a THP event from TP driver event = " + event + ",retval = " + retval);
            }
            HwInputManagerServiceEx.this.mCallbacks.onHwTHPEvent(event);
        }
    }

    final class ServiceNotification extends IServiceNotification.Stub {
        ServiceNotification() {
        }

        public void onRegistration(String fqName, String name, boolean preexisting) {
            HwInputManagerServiceEx.this.connectToHidl();
        }
    }

    public HwInputManagerServiceEx(IHwInputManagerInner ims, Context context) {
        this.mImsInner = ims;
        this.mContext = context;
        this.mTHPCallback = new HALCallback();
        this.mCallbacks = new Callbacks(BackgroundThread.getHandler().getLooper());
        try {
            IServiceManager serviceManager = IServiceManager.getService();
            if (serviceManager == null || !serviceManager.registerForNotifications("vendor.huawei.hardware.tp@1.0::ITouchscreen", "", this.mServiceNotification)) {
                Slog.e(TAG, "Failed to get serviceManager and register service start notification");
            }
            connectToHidl();
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to register service start notification", e);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002a, code lost:
        android.util.Slog.i(TAG, "Successfully connect to TP service!");
        r1 = r5.mClients;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0033, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003b, code lost:
        if (r5.mClients.size() < 1) goto L_0x0042;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x003d, code lost:
        setTHPCallback(r5.mTHPCallback);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0042, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0043, code lost:
        return;
     */
    public void connectToHidl() {
        synchronized (this.mLock) {
            if (this.mTPHal == null) {
                try {
                    this.mTPHal = ITouchscreen.getService();
                    if (this.mTPHal == null) {
                        Slog.e(TAG, "Failed to get ITouchscreen service");
                        return;
                    }
                    this.mTPHal.linkToDeath(new DeathRecipient(), 1000);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Failed to register service start notification Exception " + e);
                }
            }
        }
    }

    private ClientState getClient(IBinder token) {
        ClientState client;
        synchronized (this.mClients) {
            client = this.mClients.get(token);
            if (client == null) {
                client = new ClientState(token);
                this.mClients.put(token, client);
            }
        }
        return client;
    }

    private void enforceCallingPermission(String packageName, int uid) {
        if (uid != 1000) {
            int i = 0;
            while (true) {
                if (i >= CALLING_PACKAGES.length) {
                    break;
                } else if (CALLING_PACKAGES[i].equals(packageName)) {
                    try {
                        ApplicationInfo appInfo = this.mContext.getPackageManager().getApplicationInfo(packageName, 0);
                        if (appInfo != null && (appInfo.flags & 1) != 0) {
                            return;
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        Slog.e(TAG, packageName + " not found.");
                    }
                } else {
                    i++;
                }
            }
            throw new SecurityException("Package : " + packageName + ",uid : " + uid + " does not have permission to access THP interfaces");
        }
    }

    private String runHwTHPCommandInternal(String command, String parameter) {
        String str;
        synchronized (this.mLock) {
            this.mResult = null;
            try {
                if (this.mTPHal != null) {
                    this.mTPHal.hwTsRunCommand(command, parameter, new ITouchscreen.hwTsRunCommandCallback(command, parameter) {
                        private final /* synthetic */ String f$1;
                        private final /* synthetic */ String f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void onValues(int i, String str) {
                            HwInputManagerServiceEx.lambda$runHwTHPCommandInternal$0(HwInputManagerServiceEx.this, this.f$1, this.f$2, i, str);
                        }
                    });
                } else if (DEBUG) {
                    Slog.i(TAG, "runHwTHPCommand failed, TP service has not been initialized yet !!");
                }
            } catch (Exception e) {
                Slog.e(TAG, "hwTsGetCapacityInfo Exception e = " + e);
            }
            str = this.mResult;
        }
        return str;
    }

    public static /* synthetic */ void lambda$runHwTHPCommandInternal$0(HwInputManagerServiceEx hwInputManagerServiceEx, String command, String parameter, int ret, String status) {
        if (DEBUG) {
            Slog.i(TAG, "runHwTHPCommand command : " + command + ",parameter : " + parameter + ", ret = " + ret + ", status = " + status);
        }
        hwInputManagerServiceEx.mResult = status;
    }

    public String runHwTHPCommand(String command, String parameter) {
        int uid = Binder.getCallingUid();
        String pacakgeName = this.mContext.getPackageManager().getNameForUid(uid);
        enforceCallingPermission(pacakgeName, uid);
        return runHwTHPCommandInternal(command, pacakgeName + CPUCustBaseConfig.CPUCONFIG_INVALID_STR + parameter);
    }

    public void registerListener(IHwTHPEventListener listener, IBinder iBinder) {
        int uid = Binder.getCallingUid();
        String pkgName = this.mContext.getPackageManager().getNameForUid(uid);
        enforceCallingPermission(pkgName, uid);
        this.mCallbacks.register(listener);
        synchronized (this.mClients) {
            getClient(iBinder).addListenerLocked(listener);
            if (this.mClients.size() == 1) {
                setTHPCallback(this.mTHPCallback);
            }
        }
        if (DEBUG) {
            Slog.i(TAG, "registerListener  listener = " + listener.asBinder() + ",uid = " + uid + ",calling pkgName = " + pkgName);
        }
    }

    public void unregisterListener(IHwTHPEventListener listener, IBinder iBinder) {
        int uid = Binder.getCallingUid();
        String pkgName = this.mContext.getPackageManager().getNameForUid(uid);
        enforceCallingPermission(pkgName, uid);
        this.mCallbacks.unregister(listener);
        synchronized (this.mClients) {
            getClient(iBinder).removeListenerLocked(listener);
            if (this.mClients.size() == 0) {
                setTHPCallback(null);
            }
        }
        if (DEBUG) {
            Slog.i(TAG, "unregisterListener  listener = " + listener.asBinder() + ",uid = " + uid + ",calling pkgName = " + pkgName);
        }
    }

    public void checkHasShowDismissSoftInputAlertDialog(boolean isEmpty) {
        if ("tablet".equals(IS_TABLET) && !isEmpty && !this.mHasShow) {
            if (!(HwPCUtils.enabledInPad() && HwPCUtils.isPcCastMode())) {
                int showIme = Settings.Secure.getInt(this.mContext.getContentResolver(), "show_ime_with_hard_keyboard", 0);
                int showDialog = Settings.Secure.getInt(this.mContext.getContentResolver(), HAS_SHOW_DIALOG, 0);
                Log.i(TAG, "checkHasShowDismissSoftInputAlertDialog - showIme =" + showIme + "showDialog =" + showDialog);
                if (showIme == 0 && showDialog == 0) {
                    showDismissSoftInputAlertDialog(this.mContext);
                    Settings.Secure.putInt(this.mContext.getContentResolver(), HAS_SHOW_DIALOG, 1);
                }
                this.mHasShow = true;
            }
        }
    }

    private void showDismissSoftInputAlertDialog(Context context) {
        Log.i(TAG, "showDismissSoftInputAlertDialog");
        if (this.mAlterSoftInputDialog == null || !this.mAlterSoftInputDialog.isShowing()) {
            AlertDialog.Builder buider = new AlertDialog.Builder(context, 33947691);
            View view = LayoutInflater.from(buider.getContext()).inflate(34013358, null);
            if (view != null) {
                ImageView imageView = (ImageView) view.findViewById(34603352);
                TextView textView = (TextView) view.findViewById(34603350);
                if (imageView != null && textView != null) {
                    imageView.setImageResource(33752045);
                    textView.setText(context.getResources().getString(33686161));
                    this.mAlterSoftInputDialog = buider.setTitle(33686163).setPositiveButton(33686162, $$Lambda$HwInputManagerServiceEx$3Xt6EEjK_GXDs03sW6_dm0X4uo.INSTANCE).setView(view).create();
                    this.mAlterSoftInputDialog.getWindow().setType(HwArbitrationDEFS.MSG_MPLINK_BIND_FAIL);
                    this.mAlterSoftInputDialog.show();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void setTHPCallback(HALCallback callback) {
        synchronized (this.mLock) {
            try {
                if (this.mTPHal != null) {
                    if (DEBUG) {
                        Slog.i(TAG, "setTHPCallback hwTsSetCallback callback = " + callback);
                    }
                    this.mTPHal.hwTsSetCallback(callback);
                } else if (DEBUG) {
                    Slog.i(TAG, "setTHPCallback failed, TP service has not been initialized yet !!");
                }
            } catch (Exception e) {
                Slog.e(TAG, "hwTsSetCallback Exception e = " + e);
            }
        }
    }
}
