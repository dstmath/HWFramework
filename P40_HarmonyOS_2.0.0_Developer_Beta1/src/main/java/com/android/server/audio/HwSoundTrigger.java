package com.android.server.audio;

import android.app.ActivityManagerNative;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.soundtrigger.SoundTrigger;
import android.media.AudioSystem;
import android.media.IAudioRoutesObserver;
import android.media.soundtrigger.SoundTriggerDetector;
import android.media.soundtrigger.SoundTriggerManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelUuid;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Slog;
import com.android.internal.app.ISoundTriggerService;
import com.android.internal.content.PackageMonitor;
import com.android.server.DeviceIdleController;
import com.android.server.LocalServices;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.server.am.ProcessListEx;
import java.util.UUID;

public class HwSoundTrigger {
    private static final String ACTION_AI_WAKEUP_INFO = "com.huawei.ai.wakeup.service.WAKEUP_REPORT";
    private static final String ACTION_AI_WAKEUP_SERVICE = "com.huawei.ai.wakeup.service.WAKEUP2";
    private static final String ACTION_WAKEUP_SERVICE = "com.huawei.wakeup.services.WakeupService";
    private static final String ASSISTANT_VENDOR = SystemProperties.get("hw_sc.assistant.vendor", "huawei");
    private static final String DEV_PATH_SMART_HOLDER = "DEVPATH=/devices/virtual/misc/wakeup";
    private static final String DEV_PATH_SMART_HOLDER_VAR_INFO = "wakeup_info";
    private static final String DEV_PATH_SMART_HOLDER_VAR_REPORT = "wakeup_report";
    private static final String KEY_SOUNDTRIGGER_SWITCH = "hw_soundtrigger_enabled";
    private static final UUID MODEL_UUID = UUID.fromString("7dc67ab3-eab6-4e34-b62a-f4fa3788092a");
    private static final String PKG_AI_VASSISTANT = "com.huawei.hishow";
    private static final String PKG_TV_WAKEUP = "com.huawei.wakeup";
    private static final String PKG_VASSISTANT = "com.huawei.vassistant";
    private static final String PKG_VASSISTANT_OVE = "com.huawei.hiassistantoversea";
    private static final String PKG_VASSISTANT_WATCH = "com.huawei.watch.vassistant";
    private static final String PKG_YANDEX_ASSISTANT = "ru.yandex.searchplugin";
    private static final String PRODUCT_REGION = SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, "");
    private static final String PRODUCT_TYPE = SystemProperties.get("ro.build.characteristics", AppActConstant.VALUE_DEFAULT);
    private static final String SESSION_ID = "session_id";
    private static final int SOUNDTRIGGER_STATUS_ON = 1;
    private static final String TAG = "HwSoundTrigger";
    private IBinder mBinder;
    private Context mContext;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.android.server.audio.HwSoundTrigger.AnonymousClass1 */

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Slog.e(HwSoundTrigger.TAG, "binderDied");
            HwSoundTrigger.this.startSoundTriggerV2(true);
        }
    };
    private boolean mIsLinkDeathRecipient = false;
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        /* class com.android.server.audio.HwSoundTrigger.AnonymousClass3 */

        public void onPackageRemoved(String packageName, int uid) {
            String assistantPkg = HwSoundTrigger.this.getMonitorAssistant();
            if (packageName.equals(assistantPkg)) {
                Slog.i(HwSoundTrigger.TAG, "onPackageRemoved uid :" + uid);
                HwSoundTrigger.this.updateVaVersionCode(assistantPkg);
            }
        }

        public void onPackageDataCleared(String packageName, int uid) {
            if (packageName.equals(HwSoundTrigger.this.getMonitorAssistant())) {
                Slog.i(HwSoundTrigger.TAG, "onPackageDataCleared uid : " + uid);
                HwSoundTrigger.this.resetVaSoundTrigger();
            }
        }

        public void onPackageAppeared(String packageName, int reason) {
            String assistantPkg = HwSoundTrigger.this.getMonitorAssistant();
            if (packageName.equals(assistantPkg)) {
                Slog.i(HwSoundTrigger.TAG, "onPackageUpdateStarted reason : " + reason);
                HwSoundTrigger.this.updateVaVersionCode(assistantPkg);
            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.audio.HwSoundTrigger.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
                    HwSoundTrigger.this.onUserUnlocked(intent.getIntExtra("android.intent.extra.user_handle", ProcessListEx.INVALID_ADJ));
                    return;
                }
                Slog.v(HwSoundTrigger.TAG, "action does not match");
            }
        }
    };
    private final UEventObserver mSmartHolderObserver = new UEventObserver() {
        /* class com.android.server.audio.HwSoundTrigger.AnonymousClass4 */

        public void onUEvent(UEventObserver.UEvent event) {
            if (event != null) {
                String shState = event.get(HwSoundTrigger.DEV_PATH_SMART_HOLDER_VAR_REPORT);
                String info = event.get(HwSoundTrigger.DEV_PATH_SMART_HOLDER_VAR_INFO);
                if (info != null && info.length() > 0) {
                    HwSoundTrigger.this.startAiWakeupReport(info);
                }
                if (shState != null && shState.length() > 0) {
                    HwSoundTrigger.this.startAiWakeupService(shState);
                }
            }
        }
    };
    private SoundTriggerDetector.Callback mSoundTriggerCallBack = new SoundTriggerDetector.Callback() {
        /* class com.android.server.audio.HwSoundTrigger.AnonymousClass5 */

        public void onAvailabilityChanged(int var1) {
        }

        public void onDetected(SoundTriggerDetector.EventPayload var1) {
            Slog.i(HwSoundTrigger.TAG, "onDetected() called with: eventPayload = [" + var1 + "]");
            if (var1.getCaptureSession() != null) {
                HwSoundTrigger.this.startWakeupService(var1.getCaptureSession().intValue());
            } else {
                Slog.e(HwSoundTrigger.TAG, "session invalid!");
            }
        }

        public void onError() {
            Slog.e(HwSoundTrigger.TAG, "onError()");
        }

        public void onRecognitionPaused() {
        }

        public void onRecognitionResumed() {
        }
    };
    private SoundTriggerDetector mSoundTriggerDetector;
    private ISoundTriggerService mSoundTriggerService;
    private UserManager mUserManager;
    private int mVaVersionCode;
    private String mVassistantPkg;

    public HwSoundTrigger(Context context) {
        Slog.i(TAG, TAG);
        if (context == null) {
            Slog.e(TAG, "null context!");
            return;
        }
        this.mContext = context;
        setVassistantPackageName(context);
        registerReceivers(context);
    }

    public boolean start(boolean isStart) {
        return startSoundTriggerV2(isStart);
    }

    public boolean startWatchingRoutes(IAudioRoutesObserver observer) {
        Slog.d(TAG, "startWatchingRoutes");
        return linkVassistantToDeath(observer);
    }

    public void stopWatchingRoutes() {
        IBinder iBinder = this.mBinder;
        if (iBinder != null && this.mIsLinkDeathRecipient) {
            iBinder.unlinkToDeath(this.mDeathRecipient, 0);
            this.mBinder = null;
            this.mIsLinkDeathRecipient = false;
        }
    }

    private void setVassistantPackageName(Context context) {
        if (isWatch()) {
            this.mVassistantPkg = PKG_VASSISTANT_WATCH;
            return;
        }
        this.mVassistantPkg = PKG_VASSISTANT;
        if (isOversea() && !isAppInstalled(context, this.mVassistantPkg)) {
            this.mVassistantPkg = PKG_VASSISTANT_OVE;
        }
    }

    private boolean isAppInstalled(Context context, String packageName) {
        for (PackageInfo installedPackage : context.getPackageManager().getInstalledPackages(0)) {
            if (installedPackage.packageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void registerReceivers(Context context) {
        context.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.USER_UNLOCKED"), null, null);
        this.mSmartHolderObserver.startObserving(DEV_PATH_SMART_HOLDER);
        this.mPackageMonitor.register(context, context.getMainLooper(), UserHandle.SYSTEM, false);
    }

    private static boolean isOversea() {
        return !"CN".equalsIgnoreCase(PRODUCT_REGION);
    }

    private boolean isTv() {
        return "tv".equals(PRODUCT_TYPE);
    }

    private boolean isTablet() {
        return "tablet".equals(PRODUCT_TYPE);
    }

    private boolean isWatch() {
        PackageManager pm;
        Context context = this.mContext;
        if (context == null || (pm = context.getPackageManager()) == null) {
            return false;
        }
        return pm.hasSystemFeature("android.hardware.type.watch");
    }

    private String getAiWakeupPackage() {
        if (isTv()) {
            return PKG_TV_WAKEUP;
        }
        return isTablet() ? PKG_AI_VASSISTANT : PKG_AI_VASSISTANT;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getMonitorAssistant() {
        String assistantPkg = this.mVassistantPkg;
        if ("yandex".equals(ASSISTANT_VENDOR)) {
            return PKG_YANDEX_ASSISTANT;
        }
        return assistantPkg;
    }

    /* JADX INFO: finally extract failed */
    private static int getCurrentUserId() {
        long ident = Binder.clearCallingIdentity();
        try {
            int i = ActivityManagerNative.getDefault().getCurrentUser().id;
            Binder.restoreCallingIdentity(ident);
            return i;
        } catch (RemoteException e) {
            Slog.w(TAG, "Activity manager not running, nothing we can do assume user 0.");
            Binder.restoreCallingIdentity(ident);
            return 0;
        } catch (Throwable currentUser) {
            Binder.restoreCallingIdentity(ident);
            throw currentUser;
        }
    }

    private void addPowerSaveTempWhitelistApp(String packageName) {
        DeviceIdleController.LocalService idleController = (DeviceIdleController.LocalService) LocalServices.getService(DeviceIdleController.LocalService.class);
        if (idleController != null) {
            Slog.i(TAG, "addPowerSaveTempWhitelistApp#va");
            idleController.addPowerSaveTempWhitelistApp(Process.myUid(), packageName, 10000, getCurrentUserId(), false, "hivoice");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startWakeupService(int session) {
        Slog.d(TAG, "startWakeupService: " + this.mVassistantPkg);
        addPowerSaveTempWhitelistApp(this.mVassistantPkg);
        Intent intent = new Intent(ACTION_WAKEUP_SERVICE);
        intent.setPackage(this.mVassistantPkg);
        intent.putExtra(SESSION_ID, session);
        Context context = this.mContext;
        if (context != null) {
            try {
                context.startService(intent);
            } catch (IllegalStateException | SecurityException e) {
                Slog.w(TAG, "startWakeupService failed");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startAiWakeupService(String wakeupInfo) {
        String packageName = getAiWakeupPackage();
        addPowerSaveTempWhitelistApp(packageName);
        Intent intent = new Intent(ACTION_AI_WAKEUP_SERVICE);
        intent.setPackage(packageName);
        intent.putExtra(DEV_PATH_SMART_HOLDER_VAR_INFO, wakeupInfo);
        Context context = this.mContext;
        if (context != null) {
            try {
                context.startService(intent);
                Slog.i(TAG, "Hal Audio Wakeup2");
            } catch (IllegalStateException | SecurityException e) {
                Slog.w(TAG, "startAIWakeupService failed");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startAiWakeupReport(String wakeupInfo) {
        String packageName = getAiWakeupPackage();
        Intent intent = new Intent(ACTION_AI_WAKEUP_INFO);
        intent.setPackage(packageName);
        intent.putExtra(DEV_PATH_SMART_HOLDER_VAR_INFO, wakeupInfo);
        Context context = this.mContext;
        if (context != null) {
            try {
                context.startService(intent);
            } catch (IllegalStateException | SecurityException e) {
                Slog.w(TAG, "startAiWakeupReport failed");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUserUnlocked(int userId) {
        if (userId != -10000) {
            boolean isPrimary = isPrimaryUser(userId);
            Slog.i(TAG, "user unlocked, start soundtrigger! isPrimary : " + isPrimary);
            if (isPrimary) {
                updateVaVersionCode(getMonitorAssistant());
                startSoundTriggerV2(true);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateVaVersionCode(String packageName) {
        Context context = this.mContext;
        if (context != null) {
            int versionCode = 0;
            try {
                versionCode = context.getPackageManager().getPackageInfo(packageName, 128).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                Slog.e(TAG, "vassistant not found");
            }
            Slog.i(TAG, "versionCode : " + versionCode + " mVaVersionCode : " + this.mVaVersionCode);
            if (versionCode < this.mVaVersionCode) {
                resetVaSoundTrigger();
            }
            this.mVaVersionCode = versionCode;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetVaSoundTrigger() {
        if (this.mContext != null) {
            startSoundTriggerV2(false);
            Settings.Secure.putInt(this.mContext.getContentResolver(), KEY_SOUNDTRIGGER_SWITCH, 0);
        }
    }

    private boolean isSoundTriggerOn() {
        Context context = this.mContext;
        if (context != null && Settings.Secure.getInt(context.getContentResolver(), KEY_SOUNDTRIGGER_SWITCH, 0) == 1) {
            return true;
        }
        return false;
    }

    private static boolean isSupportWakeUpV2() {
        String version = AudioSystem.getParameters("audio_capability#soundtrigger_version");
        Slog.i(TAG, "wakeupV2 : " + version);
        return !"".equals(version);
    }

    private void createUserManager() {
        Context context = this.mContext;
        if (context != null && this.mUserManager == null && (context.getSystemService("user") instanceof UserManager)) {
            this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        }
    }

    private boolean isPrimaryUser(int userId) {
        createUserManager();
        UserManager userManager = this.mUserManager;
        if (userManager != null) {
            return userManager.getUserInfo(userId).isPrimary();
        }
        return false;
    }

    private boolean isUserUnlocked() {
        createUserManager();
        UserManager userManager = this.mUserManager;
        if (userManager != null) {
            return userManager.isUserUnlocked(UserHandle.SYSTEM);
        }
        return false;
    }

    private void createSoundTrigger() {
        Context context = this.mContext;
        if (context != null) {
            if (this.mSoundTriggerDetector == null && (context.getSystemService("soundtrigger") instanceof SoundTriggerManager)) {
                this.mSoundTriggerDetector = ((SoundTriggerManager) this.mContext.getSystemService("soundtrigger")).createSoundTriggerDetector(MODEL_UUID, this.mSoundTriggerCallBack, new Handler(Looper.getMainLooper()));
            }
            if (this.mSoundTriggerService == null) {
                this.mSoundTriggerService = ISoundTriggerService.Stub.asInterface(ServiceManager.getService("soundtrigger"));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean startSoundTriggerV2(boolean isStart) {
        if (!isSupportWakeUpV2()) {
            Slog.i(TAG, "startSoundTriggerV2 not support wakeup v2");
            return false;
        }
        createSoundTrigger();
        if (!isStart) {
            return stopSoundTriggerDetector();
        }
        boolean isUserUnlocked = isUserUnlocked();
        boolean isSoundTriggerOn = isSoundTriggerOn();
        Slog.i(TAG, "isSoundTriggerOn : " + isSoundTriggerOn + " isUserUnlocked : " + isUserUnlocked);
        if (!isUserUnlocked || !isSoundTriggerOn) {
            return false;
        }
        return startSoundTriggerDetector();
    }

    private boolean startSoundTriggerDetector() {
        SoundTrigger.GenericSoundModel model = getCurrentModel();
        Slog.i(TAG, "startSoundTriggerDetector model : " + model);
        long ident = Binder.clearCallingIdentity();
        if (model != null) {
            try {
                if (this.mSoundTriggerDetector != null && this.mSoundTriggerDetector.startRecognition(1)) {
                    Slog.i(TAG, "start recognition successfully!");
                    return true;
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        Slog.e(TAG, "start recognition failed!");
        Binder.restoreCallingIdentity(ident);
        return false;
    }

    private boolean stopSoundTriggerDetector() {
        SoundTrigger.GenericSoundModel model = getCurrentModel();
        Slog.i(TAG, "stopSoundTriggerDetector model : " + model);
        long ident = Binder.clearCallingIdentity();
        if (model != null) {
            try {
                if (this.mSoundTriggerDetector != null && this.mSoundTriggerDetector.stopRecognition()) {
                    Slog.i(TAG, "stop recognition successfully!");
                    return true;
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        Slog.e(TAG, "stop recognition failed!");
        Binder.restoreCallingIdentity(ident);
        return false;
    }

    private SoundTrigger.GenericSoundModel getCurrentModel() {
        try {
            if (this.mSoundTriggerService != null) {
                return this.mSoundTriggerService.getSoundModel(new ParcelUuid(MODEL_UUID));
            }
            Slog.e(TAG, "getCurrentModel service is null!");
            return null;
        } catch (Exception e) {
            Slog.e(TAG, "getCurrentModel error");
            return null;
        }
    }

    private boolean linkVassistantToDeath(IAudioRoutesObserver observer) {
        PackageManager pm;
        String[] packages;
        int uid = Binder.getCallingUid();
        Context context = this.mContext;
        if (context == null || (pm = context.getPackageManager()) == null || (packages = pm.getPackagesForUid(uid)) == null) {
            return false;
        }
        for (String packageName : packages) {
            if (this.mVassistantPkg.equals(packageName)) {
                Slog.d(TAG, "linkToDeath");
                this.mBinder = observer.asBinder();
                try {
                    this.mBinder.linkToDeath(this.mDeathRecipient, 0);
                    this.mIsLinkDeathRecipient = true;
                    return true;
                } catch (RemoteException e) {
                    Slog.e(TAG, "linkToDeath error");
                }
            }
        }
        return false;
    }
}
