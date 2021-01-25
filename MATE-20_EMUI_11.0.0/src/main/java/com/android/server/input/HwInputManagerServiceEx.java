package com.android.server.input;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.HwPCUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.server.gesture.GestureNavConst;
import com.android.server.policy.WindowManagerPolicyEx;
import com.huawei.android.hardware.input.IHwTHPEventListener;
import com.huawei.android.hidl.IServiceManagerHidlAdapter;
import com.huawei.android.hidl.IServiceNotificationHidlAdapter;
import com.huawei.android.hidl.ITPCallbackHidlAdapter;
import com.huawei.android.hidl.ITouchscreenHidlAdapter;
import com.huawei.android.internal.os.BackgroundThreadEx;
import com.huawei.android.internal.os.SomeArgsEx;
import com.huawei.android.os.HwBinderEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.SlogEx;
import com.huawei.server.HwPartIawareUtil;
import com.huawei.sidetouch.HwSideTouchManager;
import com.huawei.util.LogEx;
import com.huawei.utils.HwPartResourceUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public final class HwInputManagerServiceEx extends DefaultHwInputManagerServiceEx {
    private static final String CMD_EXE_SUCCESS = "OK";
    private static final String CMD_THP_INFORM_APP_CRASH = "THP_InformUserAppCrash";
    private static final String CMD_THP_INFORM_TYPING_STATUS = "THP_InformTypingStatus";
    private static final String HAS_SHOW_DIALOG = "has_show_dialog";
    private static final boolean IS_DEBUG_ON = LogEx.getLogHWInfo();
    private static final boolean IS_SUPPORT_AUTO_KEYBOARD_LAYOUT = GestureNavConst.DEVICE_TYPE_TABLET.equals(IS_TABLET);
    private static final String IS_TABLET = SystemPropertiesEx.get("ro.build.characteristics", "");
    private static final String PACKAGE_NAME_SYSTEMUI = "android.uid.systemui";
    private static final String PERMISSION_RUN_TOUCHSCREEN_COMMAND = "com.huawei.permission.RUN_TOUCHSCREEN_COMMAND";
    private static final String TAG = "HwInputManagerServiceEx";
    private static final int THP_HAL_DEATH_COOKIE = 1000;
    private AlertDialog mAlterSoftInputDialog = null;
    private final Callbacks mCallbacks;
    private final Map<IBinder, ClientState> mClients = new ArrayMap();
    private final Context mContext;
    private IHwInputManagerInner mImsInner = null;
    private boolean mIsShow = false;
    private final Object mLock = new Object();
    private String mResult = null;
    private final ServiceNotification mServiceNotification = new ServiceNotification();
    private Toast mSwitchedKeyboardLayoutToast;
    private HalCallback mThpCallback;
    private ITouchscreenHidlAdapter mTpHal = null;

    public HwInputManagerServiceEx(IHwInputManagerInner ims, Context context) {
        super(ims, context);
        this.mImsInner = ims;
        this.mContext = context;
        this.mThpCallback = new HalCallback();
        this.mCallbacks = new Callbacks(BackgroundThreadEx.getHandler().getLooper());
        try {
            IServiceManagerHidlAdapter serviceManager = IServiceManagerHidlAdapter.getService();
            if (serviceManager == null || !serviceManager.registerForNotifications("vendor.huawei.hardware.tp@1.0::ITouchscreen", "", this.mServiceNotification)) {
                SlogEx.e(TAG, "Failed to get serviceManager and register service start notification");
            }
            connectToHidl();
        } catch (RemoteException e) {
            SlogEx.e(TAG, "Failed to register service start notification");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void connectToHidl() {
        synchronized (this.mLock) {
            if (this.mTpHal == null) {
                try {
                    this.mTpHal = ITouchscreenHidlAdapter.getService();
                    if (this.mTpHal == null) {
                        SlogEx.e(TAG, "Failed to get ITouchscreen service");
                        return;
                    }
                    this.mTpHal.linkToDeath(new DeathRecipient(), 1000);
                } catch (RemoteException e) {
                    SlogEx.e(TAG, "Failed to register service start notification Exception ");
                    return;
                } catch (NoSuchElementException e2) {
                    SlogEx.e(TAG, "Failed to register service start notification NoSuchElementException");
                    return;
                }
            } else {
                return;
            }
        }
        SlogEx.i(TAG, "Successfully connect to TP service!");
        synchronized (this.mClients) {
            if (this.mClients.size() >= 1) {
                setThpCallback(this.mThpCallback);
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

    private void enforceCallingPermission(String packageName, int uid, String command) {
        if (!CMD_THP_INFORM_TYPING_STATUS.equals(command)) {
            enforceCallingPermission(packageName, uid);
        }
    }

    private void enforceCallingPermission(String packageName, int uid) {
        String appInfo;
        if (uid != 1000 && this.mContext.checkCallingOrSelfPermission(PERMISSION_RUN_TOUCHSCREEN_COMMAND) != 0) {
            if (packageName != null) {
                appInfo = "Package:" + packageName;
            } else {
                appInfo = "";
            }
            throw new SecurityException(appInfo + " uid : " + uid + " does not have permission to access THP interfaces");
        }
    }

    private String runHwThpCommandInternal(String command, String parameter) {
        String str;
        synchronized (this.mLock) {
            this.mResult = null;
            try {
                if (this.mTpHal != null) {
                    this.mResult = this.mTpHal.hwTsRunCommand(command, parameter);
                } else if (IS_DEBUG_ON) {
                    SlogEx.i(TAG, "runHwTHPCommand failed, TP service has not been initialized yet !!");
                }
            } catch (RemoteException e) {
                SlogEx.e(TAG, "hwTsGetCapacityInfo Exception");
            }
            str = this.mResult;
        }
        return str;
    }

    public String runHwTHPCommand(String command, String parameter) {
        int uid = Binder.getCallingUid();
        String packgeName = this.mContext.getPackageManager().getNameForUid(uid);
        enforceCallingPermission(packgeName, uid, command);
        return runHwThpCommandInternal(command, packgeName + "#" + parameter);
    }

    public int setTouchscreenFeatureConfig(int feature, String config) {
        enforceCallingPermission(null, Binder.getCallingUid());
        return setTouchscreenFeatureConfigInternal(feature, config);
    }

    public void registerListener(IHwTHPEventListener listener, IBinder binder) {
        int uid = Binder.getCallingUid();
        String pkgName = this.mContext.getPackageManager().getNameForUid(uid);
        this.mCallbacks.register(listener);
        synchronized (this.mClients) {
            getClient(binder).addListenerLocked(listener);
            if (this.mClients.size() == 1) {
                setThpCallback(this.mThpCallback);
            }
        }
        if (IS_DEBUG_ON) {
            SlogEx.i(TAG, "registerListener listener = " + listener.asBinder() + ",uid = " + uid + ",calling pkgName = " + pkgName);
        }
    }

    public void unregisterListener(IHwTHPEventListener listener, IBinder binder) {
        int uid = Binder.getCallingUid();
        String pkgName = this.mContext.getPackageManager().getNameForUid(uid);
        this.mCallbacks.unregister(listener);
        synchronized (this.mClients) {
            getClient(binder).removeListenerLocked(listener);
            if (this.mClients.size() == 0) {
                setThpCallback(null);
            }
        }
        if (IS_DEBUG_ON) {
            SlogEx.i(TAG, "unregisterListener listener = " + listener.asBinder() + ",uid = " + uid + ",calling pkgName = " + pkgName);
        }
    }

    public void checkHasShowDismissSoftInputAlertDialog(boolean isEmpty) {
        if (GestureNavConst.DEVICE_TYPE_TABLET.equals(IS_TABLET) && !isMmiTesting() && !isEmpty && !this.mIsShow) {
            if (!(HwPCUtils.enabledInPad() && HwPCUtils.isPcCastMode())) {
                int showIme = Settings.Secure.getInt(this.mContext.getContentResolver(), "show_ime_with_hard_keyboard", 0);
                int showDialog = Settings.Secure.getInt(this.mContext.getContentResolver(), HAS_SHOW_DIALOG, 0);
                Log.i(TAG, "checkHasShowDismissSoftInputAlertDialog - showIme =" + showIme + "showDialog =" + showDialog);
                if (showIme == 0 && showDialog == 0) {
                    showDismissSoftInputAlertDialog(this.mContext);
                    Settings.Secure.putInt(this.mContext.getContentResolver(), HAS_SHOW_DIALOG, 1);
                }
                this.mIsShow = true;
            }
        }
    }

    private boolean isMmiTesting() {
        return "true".equals(SystemPropertiesEx.get("runtime.mmitest.isrunning", "false"));
    }

    private void showDismissSoftInputAlertDialog(Context context) {
        Log.i(TAG, "showDismissSoftInputAlertDialog");
        AlertDialog alertDialog = this.mAlterSoftInputDialog;
        if (alertDialog == null || !alertDialog.isShowing()) {
            AlertDialog.Builder buider = new AlertDialog.Builder(context, 33947691);
            View view = LayoutInflater.from(buider.getContext()).inflate(HwPartResourceUtils.getResourceId("notify_dismiss_softinput"), (ViewGroup) null);
            if (view != null) {
                ImageView imageView = (ImageView) view.findViewById(HwPartResourceUtils.getResourceId("notify_image"));
                TextView textView = (TextView) view.findViewById(HwPartResourceUtils.getResourceId("notify_description"));
                if (imageView != null && textView != null) {
                    imageView.setImageResource(HwPartResourceUtils.getResourceId("image_laptop"));
                    textView.setText(context.getResources().getString(HwPartResourceUtils.getResourceId("notify_content")));
                    this.mAlterSoftInputDialog = buider.setTitle(HwPartResourceUtils.getResourceId("notify_title")).setPositiveButton(HwPartResourceUtils.getResourceId("notify_know"), $$Lambda$HwInputManagerServiceEx$V_BSCEg3NUSndM6YVf5eAFVWsnk.INSTANCE).setView(view).create();
                    this.mAlterSoftInputDialog.getWindow().setType(2008);
                    this.mAlterSoftInputDialog.show();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setThpCallback(HalCallback callback) {
        synchronized (this.mLock) {
            try {
                if (this.mTpHal != null) {
                    if (IS_DEBUG_ON) {
                        SlogEx.i(TAG, "setThpCallback hwTsSetCallback callback = " + callback);
                    }
                    this.mTpHal.hwTsSetCallback(callback);
                } else if (IS_DEBUG_ON) {
                    SlogEx.i(TAG, "setThpCallback failed, TP service has not been initialized yet !!");
                }
            } catch (RemoteException e) {
                SlogEx.e(TAG, "hwTsSetCallback Exception e");
            }
        }
    }

    public void notifyNativeEvent(int eventType, int eventValue, int keyAction, int pid, int uid) {
        HwPartIawareUtil.processNativeEventNotify(eventType, eventValue, keyAction, pid, uid);
    }

    private boolean checkSystemApp(String pkg) {
        Context context;
        if (pkg == null || (context = this.mContext) == null) {
            Log.e(TAG, "pkg or mContext is null");
            return false;
        }
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            Log.e(TAG, "get packageManager is null");
            return false;
        }
        try {
            ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
            if (info == null) {
                Log.e(TAG, "info is null");
                return false;
            }
            boolean isSystemApp = (info.flags & 1) > 0;
            boolean isUpdatedSystemApp = (info.flags & 128) > 0;
            if (isSystemApp || isUpdatedSystemApp) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "get infor error");
            return false;
        }
    }

    public String runSideTouchCommand(String command, String parameter) {
        boolean isPermissioned = this.mContext.checkCallingOrSelfPermission("com.huawei.permission.EXT_DISPLAY_UI_PERMISSION") == 0;
        String packageName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        boolean isSystemApp = false;
        if (!isPermissioned) {
            isSystemApp = checkSystemApp(packageName);
        }
        if (isSystemApp || isPermissioned) {
            StringBuilder sb = new StringBuilder(packageName);
            sb.append("#");
            if (parameter != null) {
                sb.append(parameter);
            }
            return runHwThpCommandInternal(command, sb.toString());
        }
        SlogEx.e(TAG, "package " + packageName + " not permissioned or not system app : " + isSystemApp);
        return null;
    }

    public int[] setTPCommand(int type, Bundle bundle) {
        if (type == 1) {
            notifyVolumePanelStatus(bundle);
        }
        return HwSideTouchManager.getInstance(this.mContext).runSideTouchCommand(type, bundle);
    }

    private void notifyVolumePanelStatus(Bundle bundle) {
        if (bundle != null) {
            boolean isVolumePanelVisible = false;
            int guiState = bundle.getInt("guiState", 0);
            WindowManagerPolicyEx policy = WindowManagerPolicyEx.getInstance();
            if (policy != null) {
                if (guiState == 1) {
                    isVolumePanelVisible = true;
                }
                policy.notifyVolumePanelStatus(isVolumePanelVisible);
            }
        }
    }

    public void showSwitchedKeyboardLayoutToast(String keyboardLayoutLabel, boolean isCurrentAuto) {
        Toast toast = this.mSwitchedKeyboardLayoutToast;
        if (toast != null) {
            toast.cancel();
            this.mSwitchedKeyboardLayoutToast = null;
        }
        String label = keyboardLayoutLabel;
        if (IS_SUPPORT_AUTO_KEYBOARD_LAYOUT && keyboardLayoutLabel == null) {
            label = this.mContext.getString(17041388, this.mContext.getString(17041387));
        }
        if (IS_SUPPORT_AUTO_KEYBOARD_LAYOUT && isCurrentAuto && keyboardLayoutLabel != null) {
            label = this.mContext.getString(17041388, label);
        }
        if (label != null) {
            Context pcContext = HwPCUtils.getDisplayContext(this.mContext, HwPCUtils.getPCDisplayID());
            this.mSwitchedKeyboardLayoutToast = Toast.makeText(pcContext != null ? pcContext : this.mContext, label, 0);
            this.mSwitchedKeyboardLayoutToast.getWindowParams().privateFlags |= 16;
            this.mSwitchedKeyboardLayoutToast.show();
        }
    }

    /* access modifiers changed from: private */
    public static class Callbacks extends Handler {
        private static final int MSG_THP_INPUT_EVENT = 1;
        private static final int MSG_TP_INPUT_EVENT = 2;
        private final RemoteCallbackList<IHwTHPEventListener> mCallbacks = new RemoteCallbackList<>();

        Callbacks(Looper looper) {
            super(looper);
        }

        public void register(IHwTHPEventListener callback) {
            this.mCallbacks.register(callback);
        }

        public void unregister(IHwTHPEventListener callback) {
            this.mCallbacks.unregister(callback);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            SomeArgsEx args = (SomeArgsEx) msg.obj;
            int broadCasts = this.mCallbacks.beginBroadcast();
            for (int i = 0; i < broadCasts; i++) {
                try {
                    invokeCallback(this.mCallbacks.getBroadcastItem(i), msg.what, args);
                } catch (RemoteException e) {
                    SlogEx.i(HwInputManagerServiceEx.TAG, "invoke fail");
                }
            }
            this.mCallbacks.finishBroadcast();
            args.recycle();
        }

        private void invokeCallback(IHwTHPEventListener callback, int what, SomeArgsEx args) throws RemoteException {
            if (what == 1) {
                callback.onHwTHPEvent(((Integer) args.arg1()).intValue());
            } else if (what == 2) {
                callback.onHwTpEvent(((Integer) args.arg1()).intValue(), ((Integer) args.arg2()).intValue(), (String) args.arg3());
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void onHwThpEvent(int event) {
            SomeArgsEx args = SomeArgsEx.obtain();
            args.setArg1(Integer.valueOf(event));
            obtainMessage(1, args).sendToTarget();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void onHwTpEvent(int eventClass, int eventCode, String extraInfo) {
            SomeArgsEx args = SomeArgsEx.obtain();
            args.setArg1(Integer.valueOf(eventClass));
            args.setArg2(Integer.valueOf(eventCode));
            args.setArg3(extraInfo);
            obtainMessage(2, args).sendToTarget();
        }
    }

    final class ServiceNotification extends IServiceNotificationHidlAdapter {
        ServiceNotification() {
        }

        public void onRegistration(String fqName, String name, boolean isPreexisting) {
            HwInputManagerServiceEx.this.connectToHidl();
        }
    }

    public final class ClientState extends Binder implements IBinder.DeathRecipient {
        private final IBinder mAppToken;
        private final Map<IBinder, IHwTHPEventListener> mListeners = new HashMap();
        private final int mPid = Binder.getCallingPid();
        private final int mUid = Binder.getCallingUid();

        public ClientState(IBinder appToken) {
            this.mAppToken = appToken;
            try {
                this.mAppToken.linkToDeath(this, 0);
            } catch (RemoteException e) {
                SlogEx.i(HwInputManagerServiceEx.TAG, "app token exception.");
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

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (HwInputManagerServiceEx.this.mClients) {
                SlogEx.d(HwInputManagerServiceEx.TAG, "Client died: " + this);
                HwInputManagerServiceEx.this.mClients.remove(this.mAppToken);
                if (HwInputManagerServiceEx.this.mClients.size() == 0) {
                    HwInputManagerServiceEx.this.setThpCallback(null);
                }
            }
            String result = HwInputManagerServiceEx.this.runHwTHPCommand(HwInputManagerServiceEx.CMD_THP_INFORM_APP_CRASH, "");
            if (HwInputManagerServiceEx.CMD_EXE_SUCCESS.equals(result)) {
                SlogEx.d(HwInputManagerServiceEx.TAG, "CMD_INFORM_APP_CRASH exec failed, result = " + result);
            }
        }

        @Override // java.lang.Object
        public String toString() {
            return "Client: UID: " + this.mUid + " PID: " + this.mPid + " listener count: " + this.mListeners.size();
        }
    }

    /* access modifiers changed from: private */
    public class HalCallback extends ITPCallbackHidlAdapter {
        private HalCallback() {
        }

        public void notifyTHPEvents(int event, int retval) {
            HwSideTouchManager sideTouchManager;
            if (HwInputManagerServiceEx.IS_DEBUG_ON) {
                SlogEx.i(HwInputManagerServiceEx.TAG, "Receive a THP event from TP driver event = " + event + ",retval = " + retval);
            }
            if (!(HwInputManagerServiceEx.this.mContext == null || (sideTouchManager = HwSideTouchManager.getInstance(HwInputManagerServiceEx.this.mContext)) == null)) {
                sideTouchManager.notifySideTouchManager(event);
            }
            ((HwInputManagerServiceEx) HwInputManagerServiceEx.this).mCallbacks.onHwThpEvent(event);
        }

        public void notifyTPEvents(int eventClass, int eventCode, String extraInfo) {
            Log.i(HwInputManagerServiceEx.TAG, "Receive a TP driver eventClass=" + eventClass + ", eventCode=" + eventCode + ",extraInfo =" + extraInfo);
            HwInputManagerServiceEx.this.mCallbacks.onHwTpEvent(eventClass, eventCode, extraInfo);
        }
    }

    /* access modifiers changed from: package-private */
    public final class DeathRecipient extends HwBinderEx.DeathRecipientEx {
        DeathRecipient() {
        }

        public void serviceDied(long cookie) {
            SlogEx.w(HwInputManagerServiceEx.TAG, "TP service has died");
            if (cookie == 1000) {
                synchronized (HwInputManagerServiceEx.this.mLock) {
                    HwInputManagerServiceEx.this.mTpHal = null;
                }
            }
        }
    }

    private int setTouchscreenFeatureConfigInternal(int feature, String config) {
        synchronized (this.mLock) {
            if (this.mTpHal == null) {
                return -2;
            }
            int result = -1;
            try {
                result = this.mTpHal.hwSetFeatureConfig(feature, config);
            } catch (RemoteException e) {
                SlogEx.e(TAG, "setTouchscreenFeatureConfig Exception");
            }
            if (IS_DEBUG_ON) {
                SlogEx.i(TAG, "setFeatureConfig feature:" + feature + ", config:" + config + ", ret:" + result);
            }
            return result;
        }
    }
}
