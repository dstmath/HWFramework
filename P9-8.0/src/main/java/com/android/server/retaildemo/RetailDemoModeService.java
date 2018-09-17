package com.android.server.retaildemo;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AppGlobals;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RetailDemoModeServiceInternal;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.CallLog.Calls;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.KeyValueListParser;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.os.BackgroundThread;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.LocalServices;
import com.android.server.PreloadsFileCacheExpirationJobService;
import com.android.server.ServiceThread;
import com.android.server.SystemService;
import com.android.server.am.ActivityManagerService;
import java.io.File;
import java.util.ArrayList;

public class RetailDemoModeService extends SystemService {
    private static final String ACTION_RESET_DEMO = "com.android.server.retaildemo.ACTION_RESET_DEMO";
    private static final boolean DEBUG = false;
    private static final String DEMO_SESSION_COUNT = "retail_demo_session_count";
    private static final String DEMO_SESSION_DURATION = "retail_demo_session_duration";
    private static final String DEMO_USER_NAME = "Demo";
    private static final long MILLIS_PER_SECOND = 1000;
    private static final int MSG_INACTIVITY_TIME_OUT = 1;
    private static final int MSG_START_NEW_SESSION = 2;
    private static final int MSG_TURN_SCREEN_ON = 0;
    private static final long SCREEN_WAKEUP_DELAY = 2500;
    static final String SYSTEM_PROPERTY_RETAIL_DEMO_ENABLED = "sys.retaildemo.enabled";
    private static final String TAG = RetailDemoModeService.class.getSimpleName();
    private static final long USER_INACTIVITY_TIMEOUT_DEFAULT = 90000;
    private static final long USER_INACTIVITY_TIMEOUT_MIN = 10000;
    static final int[] VOLUME_STREAMS_TO_MUTE = new int[]{2, 3};
    private static final long WARNING_DIALOG_TIMEOUT_DEFAULT = 0;
    final Object mActivityLock;
    private IntentReceiver mBroadcastReceiver;
    private String[] mCameraIdsWithFlash;
    int mCurrentUserId;
    boolean mDeviceInDemoMode;
    @GuardedBy("mActivityLock")
    long mFirstUserActivityTime;
    Handler mHandler;
    private ServiceThread mHandlerThread;
    private Injector mInjector;
    boolean mIsCarrierDemoMode;
    @GuardedBy("mActivityLock")
    long mLastUserActivityTime;
    private RetailDemoModeServiceInternal mLocalService;
    private int mPackageVerifierEnableInitialState;
    private PreloadAppsInstaller mPreloadAppsInstaller;
    private boolean mSafeBootRestrictionInitialState;
    long mUserInactivityTimeout;
    @GuardedBy("mActivityLock")
    boolean mUserUntouched;
    long mWarningDialogTimeout;

    static class Injector {
        private ActivityManagerInternal mAmi;
        private ActivityManagerService mAms;
        private AudioManager mAudioManager;
        private CameraManager mCameraManager;
        private Context mContext;
        private NotificationManager mNm;
        private PackageManager mPm;
        private PowerManager mPowerManager;
        private PreloadAppsInstaller mPreloadAppsInstaller;
        private PendingIntent mResetDemoPendingIntent;
        private Configuration mSystemUserConfiguration;
        private UserManager mUm;
        private WakeLock mWakeLock;
        private WifiManager mWifiManager;

        Injector(Context context) {
            this.mContext = context;
        }

        Context getContext() {
            return this.mContext;
        }

        WifiManager getWifiManager() {
            if (this.mWifiManager == null) {
                this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
            }
            return this.mWifiManager;
        }

        UserManager getUserManager() {
            if (this.mUm == null) {
                this.mUm = (UserManager) getContext().getSystemService(UserManager.class);
            }
            return this.mUm;
        }

        void switchUser(int userId) {
            if (this.mAms == null) {
                this.mAms = (ActivityManagerService) ActivityManager.getService();
            }
            this.mAms.switchUser(userId);
        }

        AudioManager getAudioManager() {
            if (this.mAudioManager == null) {
                this.mAudioManager = (AudioManager) getContext().getSystemService(AudioManager.class);
            }
            return this.mAudioManager;
        }

        private PowerManager getPowerManager() {
            if (this.mPowerManager == null) {
                this.mPowerManager = (PowerManager) getContext().getSystemService("power");
            }
            return this.mPowerManager;
        }

        NotificationManager getNotificationManager() {
            if (this.mNm == null) {
                this.mNm = NotificationManager.from(getContext());
            }
            return this.mNm;
        }

        ActivityManagerInternal getActivityManagerInternal() {
            if (this.mAmi == null) {
                this.mAmi = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
            }
            return this.mAmi;
        }

        CameraManager getCameraManager() {
            if (this.mCameraManager == null) {
                this.mCameraManager = (CameraManager) getContext().getSystemService("camera");
            }
            return this.mCameraManager;
        }

        PackageManager getPackageManager() {
            if (this.mPm == null) {
                this.mPm = getContext().getPackageManager();
            }
            return this.mPm;
        }

        IPackageManager getIPackageManager() {
            return AppGlobals.getPackageManager();
        }

        ContentResolver getContentResolver() {
            return getContext().getContentResolver();
        }

        PreloadAppsInstaller getPreloadAppsInstaller() {
            if (this.mPreloadAppsInstaller == null) {
                this.mPreloadAppsInstaller = new PreloadAppsInstaller(getContext());
            }
            return this.mPreloadAppsInstaller;
        }

        void systemPropertiesSet(String key, String value) {
            SystemProperties.set(key, value);
        }

        void turnOffAllFlashLights(String[] cameraIdsWithFlash) {
            for (String cameraId : cameraIdsWithFlash) {
                try {
                    getCameraManager().setTorchMode(cameraId, false);
                } catch (CameraAccessException e) {
                    Slog.e(RetailDemoModeService.TAG, "Unable to access camera " + cameraId + " while turning off flash", e);
                }
            }
        }

        void initializeWakeLock() {
            if (this.mWakeLock == null) {
                this.mWakeLock = getPowerManager().newWakeLock(268435482, RetailDemoModeService.TAG);
            }
        }

        void destroyWakeLock() {
            this.mWakeLock = null;
        }

        boolean isWakeLockHeld() {
            return this.mWakeLock != null ? this.mWakeLock.isHeld() : false;
        }

        void acquireWakeLock() {
            this.mWakeLock.acquire();
        }

        void releaseWakeLock() {
            this.mWakeLock.release();
        }

        void logSessionDuration(int duration) {
            MetricsLogger.histogram(getContext(), RetailDemoModeService.DEMO_SESSION_DURATION, duration);
        }

        void logSessionCount(int count) {
            MetricsLogger.count(getContext(), RetailDemoModeService.DEMO_SESSION_COUNT, count);
        }

        Configuration getSystemUsersConfiguration() {
            if (this.mSystemUserConfiguration == null) {
                ContentResolver contentResolver = getContentResolver();
                Configuration configuration = new Configuration();
                this.mSystemUserConfiguration = configuration;
                System.getConfiguration(contentResolver, configuration);
            }
            return this.mSystemUserConfiguration;
        }

        LockPatternUtils getLockPatternUtils() {
            return new LockPatternUtils(getContext());
        }

        Notification createResetNotification() {
            return new Builder(getContext(), SystemNotificationChannels.RETAIL_MODE).setContentTitle(getContext().getString(17040887)).setContentText(getContext().getString(17040886)).setOngoing(true).setSmallIcon(17302933).setShowWhen(false).setVisibility(1).setContentIntent(getResetDemoPendingIntent()).setColor(getContext().getColor(17170769)).build();
        }

        private PendingIntent getResetDemoPendingIntent() {
            if (this.mResetDemoPendingIntent == null) {
                this.mResetDemoPendingIntent = PendingIntent.getBroadcast(getContext(), 0, new Intent(RetailDemoModeService.ACTION_RESET_DEMO), 0);
            }
            return this.mResetDemoPendingIntent;
        }

        File getDataPreloadsDirectory() {
            return Environment.getDataPreloadsDirectory();
        }

        File getDataPreloadsFileCacheDirectory() {
            return Environment.getDataPreloadsFileCacheDirectory();
        }

        void publishLocalService(RetailDemoModeService service, RetailDemoModeServiceInternal localService) {
            service.-wrap2(RetailDemoModeServiceInternal.class, localService);
        }
    }

    private final class IntentReceiver extends BroadcastReceiver {
        /* synthetic */ IntentReceiver(RetailDemoModeService this$0, IntentReceiver -this1) {
            this();
        }

        private IntentReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (RetailDemoModeService.this.mDeviceInDemoMode) {
                String action = intent.getAction();
                if (action.equals("android.intent.action.SCREEN_OFF")) {
                    RetailDemoModeService.this.mHandler.removeMessages(0);
                    RetailDemoModeService.this.mHandler.sendEmptyMessageDelayed(0, RetailDemoModeService.SCREEN_WAKEUP_DELAY);
                } else if (action.equals(RetailDemoModeService.ACTION_RESET_DEMO)) {
                    RetailDemoModeService.this.mHandler.sendEmptyMessage(2);
                }
            }
        }
    }

    final class MainHandler extends Handler {
        MainHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int i = 0;
            if (RetailDemoModeService.this.mDeviceInDemoMode) {
                switch (msg.what) {
                    case 0:
                        if (RetailDemoModeService.this.mInjector.isWakeLockHeld()) {
                            RetailDemoModeService.this.mInjector.releaseWakeLock();
                        }
                        RetailDemoModeService.this.mInjector.acquireWakeLock();
                        break;
                    case 1:
                        if (!RetailDemoModeService.this.mIsCarrierDemoMode && RetailDemoModeService.this.isDemoLauncherDisabled()) {
                            Slog.i(RetailDemoModeService.TAG, "User inactivity timeout reached");
                            RetailDemoModeService.this.showInactivityCountdownDialog();
                            break;
                        }
                    case 2:
                        removeMessages(2);
                        removeMessages(1);
                        if (!(RetailDemoModeService.this.mIsCarrierDemoMode || RetailDemoModeService.this.mCurrentUserId == 0)) {
                            RetailDemoModeService.this.logSessionDuration();
                        }
                        UserManager um = RetailDemoModeService.this.mInjector.getUserManager();
                        UserInfo demoUser = null;
                        if (RetailDemoModeService.this.mIsCarrierDemoMode) {
                            for (UserInfo user : um.getUsers()) {
                                if (user.isDemo()) {
                                    demoUser = user;
                                }
                            }
                        }
                        if (demoUser == null) {
                            if (!RetailDemoModeService.this.mIsCarrierDemoMode) {
                                i = 256;
                            }
                            demoUser = um.createUser(RetailDemoModeService.DEMO_USER_NAME, i | 512);
                        }
                        if (!(demoUser == null || RetailDemoModeService.this.mCurrentUserId == demoUser.id)) {
                            RetailDemoModeService.this.setupDemoUser(demoUser);
                            RetailDemoModeService.this.mInjector.switchUser(demoUser.id);
                            break;
                        }
                }
            }
        }
    }

    class SettingsObserver extends ContentObserver {
        private static final String KEY_USER_INACTIVITY_TIMEOUT = "user_inactivity_timeout_ms";
        private static final String KEY_WARNING_DIALOG_TIMEOUT = "warning_dialog_timeout_ms";
        private final Uri mDeviceDemoModeUri = Global.getUriFor("device_demo_mode");
        private final Uri mDeviceProvisionedUri = Global.getUriFor("device_provisioned");
        private final KeyValueListParser mParser = new KeyValueListParser(',');
        private final Uri mRetailDemoConstantsUri = Global.getUriFor("retail_demo_mode_constants");

        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void register() {
            ContentResolver cr = RetailDemoModeService.this.mInjector.getContentResolver();
            cr.registerContentObserver(this.mDeviceDemoModeUri, false, this, 0);
            cr.registerContentObserver(this.mDeviceProvisionedUri, false, this, 0);
            cr.registerContentObserver(this.mRetailDemoConstantsUri, false, this, 0);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (this.mRetailDemoConstantsUri.equals(uri)) {
                refreshTimeoutConstants();
                return;
            }
            if (RetailDemoModeService.this.isDeviceProvisioned()) {
                if (UserManager.isDeviceInDemoMode(RetailDemoModeService.this.getContext())) {
                    RetailDemoModeService.this.startDemoMode();
                } else {
                    RetailDemoModeService.this.mInjector.systemPropertiesSet(RetailDemoModeService.SYSTEM_PROPERTY_RETAIL_DEMO_ENABLED, "0");
                    BackgroundThread.getHandler().post(new -$Lambda$Eet96o4-xHqZHfXzz3aTeTUSaVA(this));
                    RetailDemoModeService.this.stopDemoMode();
                    if (RetailDemoModeService.this.mInjector.isWakeLockHeld()) {
                        RetailDemoModeService.this.mInjector.releaseWakeLock();
                    }
                }
            }
        }

        /* synthetic */ void lambda$-com_android_server_retaildemo_RetailDemoModeService$SettingsObserver_10389() {
            if (!RetailDemoModeService.this.deletePreloadsFolderContents()) {
                Slog.w(RetailDemoModeService.TAG, "Failed to delete preloads folder contents");
            }
            PreloadsFileCacheExpirationJobService.schedule(RetailDemoModeService.this.mInjector.getContext());
        }

        private void refreshTimeoutConstants() {
            try {
                this.mParser.setString(Global.getString(RetailDemoModeService.this.mInjector.getContentResolver(), "retail_demo_mode_constants"));
            } catch (IllegalArgumentException e) {
                Slog.e(RetailDemoModeService.TAG, "Invalid string passed to KeyValueListParser");
            }
            RetailDemoModeService.this.mWarningDialogTimeout = this.mParser.getLong(KEY_WARNING_DIALOG_TIMEOUT, 0);
            RetailDemoModeService.this.mUserInactivityTimeout = this.mParser.getLong(KEY_USER_INACTIVITY_TIMEOUT, RetailDemoModeService.USER_INACTIVITY_TIMEOUT_DEFAULT);
            RetailDemoModeService.this.mUserInactivityTimeout = Math.max(RetailDemoModeService.this.mUserInactivityTimeout, 10000);
        }
    }

    private void showInactivityCountdownDialog() {
        UserInactivityCountdownDialog dialog = new UserInactivityCountdownDialog(getContext(), this.mWarningDialogTimeout, 1000);
        dialog.setNegativeButtonClickListener(null);
        dialog.setPositiveButtonClickListener(new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                RetailDemoModeService.this.mHandler.sendEmptyMessage(2);
            }
        });
        dialog.setOnCountDownExpiredListener(new OnCountDownExpiredListener() {
            public void onCountDownExpired() {
                RetailDemoModeService.this.mHandler.sendEmptyMessage(2);
            }
        });
        dialog.show();
    }

    public RetailDemoModeService(Context context) {
        this(new Injector(context));
    }

    RetailDemoModeService(Injector injector) {
        super(injector.getContext());
        this.mCurrentUserId = 0;
        this.mActivityLock = new Object();
        this.mBroadcastReceiver = null;
        this.mLocalService = new RetailDemoModeServiceInternal() {
            private static final long USER_ACTIVITY_DEBOUNCE_TIME = 2000;

            /* JADX WARNING: Missing block: B:20:0x004a, code:
            r9.this$0.mHandler.removeMessages(1);
            r9.this$0.mHandler.sendEmptyMessageDelayed(1, r9.this$0.mUserInactivityTimeout);
     */
            /* JADX WARNING: Missing block: B:21:0x005c, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onUserActivity() {
                if (RetailDemoModeService.this.mDeviceInDemoMode && !RetailDemoModeService.this.mIsCarrierDemoMode) {
                    long timeOfActivity = SystemClock.uptimeMillis();
                    synchronized (RetailDemoModeService.this.mActivityLock) {
                        if (timeOfActivity < RetailDemoModeService.this.mLastUserActivityTime + USER_ACTIVITY_DEBOUNCE_TIME) {
                            return;
                        }
                        RetailDemoModeService.this.mLastUserActivityTime = timeOfActivity;
                        if (RetailDemoModeService.this.mUserUntouched && RetailDemoModeService.this.isDemoLauncherDisabled()) {
                            Slog.d(RetailDemoModeService.TAG, "retail_demo first touch");
                            RetailDemoModeService.this.mUserUntouched = false;
                            RetailDemoModeService.this.mFirstUserActivityTime = timeOfActivity;
                        }
                    }
                }
            }
        };
        this.mInjector = injector;
        synchronized (this.mActivityLock) {
            long uptimeMillis = SystemClock.uptimeMillis();
            this.mLastUserActivityTime = uptimeMillis;
            this.mFirstUserActivityTime = uptimeMillis;
        }
    }

    boolean isDemoLauncherDisabled() {
        int enabledState = 0;
        try {
            enabledState = this.mInjector.getIPackageManager().getComponentEnabledSetting(ComponentName.unflattenFromString(getContext().getString(17039774)), this.mCurrentUserId);
        } catch (RemoteException re) {
            Slog.e(TAG, "Error retrieving demo launcher enabled setting", re);
        }
        return enabledState == 2;
    }

    private void setupDemoUser(UserInfo userInfo) {
        UserManager um = this.mInjector.getUserManager();
        UserHandle user = UserHandle.of(userInfo.id);
        um.setUserRestriction("no_config_wifi", true, user);
        um.setUserRestriction("no_install_unknown_sources", true, user);
        um.setUserRestriction("no_config_mobile_networks", true, user);
        um.setUserRestriction("no_usb_file_transfer", true, user);
        um.setUserRestriction("no_modify_accounts", true, user);
        um.setUserRestriction("no_config_bluetooth", true, user);
        um.setUserRestriction("no_outgoing_calls", false, user);
        um.setUserRestriction("no_safe_boot", true, UserHandle.SYSTEM);
        if (this.mIsCarrierDemoMode) {
            um.setUserRestriction("no_sms", false, user);
        }
        Secure.putIntForUser(this.mInjector.getContentResolver(), "skip_first_use_hints", 1, userInfo.id);
        Global.putInt(this.mInjector.getContentResolver(), "package_verifier_enable", 0);
        grantRuntimePermissionToCamera(user);
        clearPrimaryCallLog();
        IPackageManager iPm;
        String packageName;
        if (this.mIsCarrierDemoMode) {
            String[] packageNames;
            Secure.putIntForUser(getContext().getContentResolver(), getContext().getString(17039756), 1, userInfo.id);
            String packageList = getContext().getString(17039754);
            if (packageList == null) {
                packageNames = new String[0];
            } else {
                packageNames = TextUtils.split(packageList, ",");
            }
            iPm = AppGlobals.getPackageManager();
            int i = 0;
            int length = packageNames.length;
            while (true) {
                int i2 = i;
                if (i2 < length) {
                    packageName = packageNames[i2];
                    try {
                        iPm.setApplicationEnabledSetting(packageName, 1, 0, userInfo.id, null);
                    } catch (RemoteException re) {
                        Slog.e(TAG, "Error enabling application: " + packageName, re);
                    }
                    i = i2 + 1;
                } else {
                    return;
                }
            }
        }
        String demoLauncher = getContext().getString(17039774);
        if (!TextUtils.isEmpty(demoLauncher)) {
            ComponentName componentToEnable = ComponentName.unflattenFromString(demoLauncher);
            packageName = componentToEnable.getPackageName();
            try {
                iPm = AppGlobals.getPackageManager();
                iPm.setComponentEnabledSetting(componentToEnable, 1, 0, userInfo.id);
                iPm.setApplicationEnabledSetting(packageName, 1, 0, userInfo.id, null);
            } catch (RemoteException e) {
            }
        }
    }

    private void grantRuntimePermissionToCamera(UserHandle user) {
        Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        PackageManager pm = this.mInjector.getPackageManager();
        ResolveInfo handler = pm.resolveActivityAsUser(cameraIntent, 786432, user.getIdentifier());
        if (handler != null && handler.activityInfo != null) {
            try {
                pm.grantRuntimePermission(handler.activityInfo.packageName, "android.permission.ACCESS_FINE_LOCATION", user);
            } catch (Exception e) {
            }
        }
    }

    private void clearPrimaryCallLog() {
        try {
            this.mInjector.getContentResolver().delete(Calls.CONTENT_URI, null, null);
        } catch (Exception e) {
            Slog.w(TAG, "Deleting call log failed: " + e);
        }
    }

    void logSessionDuration() {
        int sessionDuration;
        synchronized (this.mActivityLock) {
            sessionDuration = (int) ((this.mLastUserActivityTime - this.mFirstUserActivityTime) / 1000);
        }
        this.mInjector.logSessionDuration(sessionDuration);
    }

    private boolean isDeviceProvisioned() {
        if (Global.getInt(this.mInjector.getContentResolver(), "device_provisioned", 0) != 0) {
            return true;
        }
        return false;
    }

    private boolean deletePreloadsFolderContents() {
        File dir = this.mInjector.getDataPreloadsDirectory();
        File[] files = FileUtils.listFilesOrEmpty(dir);
        File fileCacheDirectory = this.mInjector.getDataPreloadsFileCacheDirectory();
        Slog.i(TAG, "Deleting contents of " + dir);
        boolean success = true;
        for (File file : files) {
            if (file.isFile()) {
                if (!file.delete()) {
                    success = false;
                    Slog.w(TAG, "Cannot delete file " + file);
                }
            } else if (file.equals(fileCacheDirectory)) {
                Slog.i(TAG, "Skipping directory with file cache " + file);
            } else if (!FileUtils.deleteContentsAndDir(file)) {
                success = false;
                Slog.w(TAG, "Cannot delete dir and its content " + file);
            }
        }
        return success;
    }

    private void registerBroadcastReceiver() {
        if (this.mBroadcastReceiver == null) {
            IntentFilter filter = new IntentFilter();
            if (!this.mIsCarrierDemoMode) {
                filter.addAction("android.intent.action.SCREEN_OFF");
            }
            filter.addAction(ACTION_RESET_DEMO);
            this.mBroadcastReceiver = new IntentReceiver(this, null);
            getContext().registerReceiver(this.mBroadcastReceiver, filter);
        }
    }

    private void unregisterBroadcastReceiver() {
        if (this.mBroadcastReceiver != null) {
            getContext().unregisterReceiver(this.mBroadcastReceiver);
            this.mBroadcastReceiver = null;
        }
    }

    private String[] getCameraIdsWithFlash() {
        ArrayList<String> cameraIdsList = new ArrayList();
        CameraManager cm = this.mInjector.getCameraManager();
        if (cm != null) {
            try {
                for (String cameraId : cm.getCameraIdList()) {
                    if (Boolean.TRUE.equals(cm.getCameraCharacteristics(cameraId).get(CameraCharacteristics.FLASH_INFO_AVAILABLE))) {
                        cameraIdsList.add(cameraId);
                    }
                }
            } catch (CameraAccessException e) {
                Slog.e(TAG, "Unable to access camera while getting camera id list", e);
            }
        }
        return (String[]) cameraIdsList.toArray(new String[cameraIdsList.size()]);
    }

    private void muteVolumeStreams() {
        for (int stream : VOLUME_STREAMS_TO_MUTE) {
            this.mInjector.getAudioManager().setStreamVolume(stream, this.mInjector.getAudioManager().getStreamMinVolume(stream), 0);
        }
    }

    private void startDemoMode() {
        boolean z = false;
        this.mDeviceInDemoMode = true;
        this.mPreloadAppsInstaller = this.mInjector.getPreloadAppsInstaller();
        this.mInjector.initializeWakeLock();
        if (this.mCameraIdsWithFlash == null) {
            this.mCameraIdsWithFlash = getCameraIdsWithFlash();
        }
        registerBroadcastReceiver();
        String carrierDemoModeSetting = getContext().getString(17039756);
        if (!TextUtils.isEmpty(carrierDemoModeSetting) && Secure.getInt(getContext().getContentResolver(), carrierDemoModeSetting, 0) == 1) {
            z = true;
        }
        this.mIsCarrierDemoMode = z;
        this.mInjector.systemPropertiesSet(SYSTEM_PROPERTY_RETAIL_DEMO_ENABLED, "1");
        this.mHandler.sendEmptyMessage(2);
        this.mSafeBootRestrictionInitialState = this.mInjector.getUserManager().hasUserRestriction("no_safe_boot", UserHandle.SYSTEM);
        this.mPackageVerifierEnableInitialState = Global.getInt(this.mInjector.getContentResolver(), "package_verifier_enable", 1);
    }

    private void stopDemoMode() {
        this.mPreloadAppsInstaller = null;
        this.mCameraIdsWithFlash = null;
        this.mInjector.destroyWakeLock();
        unregisterBroadcastReceiver();
        if (this.mDeviceInDemoMode) {
            this.mInjector.getUserManager().setUserRestriction("no_safe_boot", this.mSafeBootRestrictionInitialState, UserHandle.SYSTEM);
            Global.putInt(this.mInjector.getContentResolver(), "package_verifier_enable", this.mPackageVerifierEnableInitialState);
        }
        this.mDeviceInDemoMode = false;
        this.mIsCarrierDemoMode = false;
    }

    public void onStart() {
        this.mHandlerThread = new ServiceThread(TAG, -2, false);
        this.mHandlerThread.start();
        this.mHandler = new MainHandler(this.mHandlerThread.getLooper());
        this.mInjector.publishLocalService(this, this.mLocalService);
    }

    public void onBootPhase(int bootPhase) {
        switch (bootPhase) {
            case 600:
                SettingsObserver settingsObserver = new SettingsObserver(this.mHandler);
                settingsObserver.register();
                settingsObserver.refreshTimeoutConstants();
                return;
            case 1000:
                if (UserManager.isDeviceInDemoMode(getContext())) {
                    startDemoMode();
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void onSwitchUser(final int userId) {
        if (!this.mDeviceInDemoMode) {
            return;
        }
        if (this.mInjector.getUserManager().getUserInfo(userId).isDemo()) {
            if (!(this.mIsCarrierDemoMode || (this.mInjector.isWakeLockHeld() ^ 1) == 0)) {
                this.mInjector.acquireWakeLock();
            }
            this.mCurrentUserId = userId;
            this.mInjector.getActivityManagerInternal().updatePersistentConfigurationForUser(this.mInjector.getSystemUsersConfiguration(), userId);
            this.mInjector.turnOffAllFlashLights(this.mCameraIdsWithFlash);
            muteVolumeStreams();
            if (!this.mInjector.getWifiManager().isWifiEnabled()) {
                this.mInjector.getWifiManager().setWifiEnabled(true);
            }
            this.mInjector.getLockPatternUtils().setLockScreenDisabled(true, userId);
            if (!this.mIsCarrierDemoMode) {
                this.mInjector.getNotificationManager().notifyAsUser(TAG, 24, this.mInjector.createResetNotification(), UserHandle.of(userId));
                synchronized (this.mActivityLock) {
                    this.mUserUntouched = true;
                }
                this.mInjector.logSessionCount(1);
                this.mHandler.removeMessages(1);
                this.mHandler.post(new Runnable() {
                    public void run() {
                        RetailDemoModeService.this.mPreloadAppsInstaller.installApps(userId);
                    }
                });
            }
            return;
        }
        Slog.wtf(TAG, "Should not allow switch to non-demo user in demo mode");
    }
}
