package android.common;

import android.app.Activity;
import android.app.HwKeyguardManager;
import android.app.IHwActivitySplitterImpl;
import android.app.IHwChangeButtonWindowCtrl;
import android.app.IHwWallpaperInfoStub;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.app.admin.HwDeviceAdminInfoDummy;
import android.app.admin.IHwDeviceAdminInfo;
import android.aps.IApsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.IHwPackageParser;
import android.content.res.AssetManager;
import android.content.res.IHwConfiguration;
import android.content.res.IHwPCResourcesUtils;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.cover.HwCoverManagerDummy;
import android.cover.IHwCoverManager;
import android.encrypt.PasswordUtil;
import android.hdm.HwDeviceManager.IHwDeviceManager;
import android.hsm.HwSystemManager.HsmInterface;
import android.hwgallerycache.IHwGalleryCacheManagerFactory;
import android.hwnotification.HwNotificationResource.IHwNotificationResource;
import android.hwtheme.IHwThemeManagerFactory;
import android.inputmethodservice.IHwInputMethodService;
import android.location.IHwInnerLocationManager;
import android.media.HwAudioRecordDummy;
import android.media.HwMediaMonitorDummy;
import android.media.HwMediaRecorderDummy;
import android.media.IHwAudioRecord;
import android.media.IHwMediaMonitor;
import android.media.IHwMediaRecorder;
import android.media.session.HwMediaSessionManager;
import android.media.session.HwMediaSessionManagerDummy;
import android.mtp.HwMtpDatabaseManager;
import android.mtp.HwMtpDatabaseManagerDummy;
import android.net.HwInnerConnectivityManager;
import android.net.INetworkPolicyManager;
import android.net.NetworkPolicyManager;
import android.net.wifi.DummyHwInnerNetworkManager;
import android.net.wifi.HwInnerNetworkManager;
import android.net.wifi.HwInnerWifiManager;
import android.net.wifi.p2p.HwInnerWifiP2pManager;
import android.os.Handler;
import android.os.IFreezeScreenApplicationMonitor;
import android.os.IHwHandler;
import android.pc.HwPCManager;
import android.provider.HwMediaStoreDummy;
import android.provider.IHwMediaStore;
import android.rms.HwSysResource;
import android.telephony.HwInnerTelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LogException;
import android.view.IHwAdCleaner;
import android.view.IHwNsdImpl;
import android.view.IHwView;
import android.view.IHwViewRootImpl;
import android.view.animation.Interpolator;
import android.view.inputmethod.IHwSecImmHelper;
import android.vrsystem.IVRSystemServiceManager;
import android.widget.FrameLayout;
import android.widget.HwWidgetManager;
import com.android.internal.app.HwLocalePickerManager;
import com.android.internal.os.HwPowerProfileManagerDummy;
import com.android.internal.os.IHwPowerProfileManager;
import com.android.internal.policy.IPressGestureDetector;
import com.android.internal.telephony.HwBaseInnerSmsManager;
import com.android.internal.view.IInputMethodManager;
import com.huawei.forcerotation.IForceRotationManager;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.hwperformance.HwPerformanceDummy;
import huawei.android.animation.HwStateListAnimator;
import huawei.android.animation.HwStateListAnimatorDummy;
import huawei.power.AudioEffectLowPowerTask;

public class HwFrameworkFactory {
    private static final String TAG = "HwFrameworkFactory";
    private static final Object mLock = new Object();
    private static Factory obj = null;

    public interface Factory {
        Interpolator createHwInterpolator(String str, Context context, AttributeSet attributeSet);

        Interpolator createHwInterpolator(String str, Resources resources, Theme theme, AttributeSet attributeSet);

        IFreezeScreenApplicationMonitor getAppFreezeScreenMonitor();

        IApsManager getApsManager();

        AudioEffectLowPowerTask getAudioEffectLowPowerTaskImpl(Context context);

        IHwCoverManager getCoverManager();

        IForceRotationManager getForceRotationManager();

        Intent getHuaweiChooserIntentImpl();

        IHwDeviceManager getHuaweiDevicePolicyManagerImpl();

        String getHuaweiResolverActivityImpl(Context context);

        IHwWallpaperManager getHuaweiWallpaperManager();

        IHwActivitySplitterImpl getHwActivitySplitterImpl(Activity activity, boolean z);

        HwActivityThread getHwActivityThread();

        IHwAdCleaner getHwAdCleaner();

        HwAnimationManager getHwAnimationManager();

        HwPackageManager getHwApplicationPackageManager();

        HwBaseInnerSmsManager getHwBaseInnerSmsManager();

        IHwChangeButtonWindowCtrl getHwChangeButtonWindowCtrl(Activity activity);

        IHwConfiguration getHwConfiguration();

        IHwDeviceAdminInfo getHwDeviceAdminInfo(Context context, ActivityInfo activityInfo);

        HwDrmManager getHwDrmManager();

        ClassLoader getHwFLClassLoaderParent(String str);

        IHwFeatureLoader getHwFeatureLoader();

        HwFlogManager getHwFlogManager();

        HwFrameworkMonitor getHwFrameworkMonitor();

        IHwGalleryCacheManagerFactory getHwGalleryCacheManagerFactory();

        IHwHandler getHwHandler();

        IHwAudioRecord getHwHwAudioRecord();

        HwInnerConnectivityManager getHwInnerConnectivityManager();

        IHwInnerLocationManager getHwInnerLocationManager();

        HwInnerNetworkManager getHwInnerNetworkManager();

        HwInnerTelephonyManager getHwInnerTelephonyManager();

        HwInnerWifiManager getHwInnerWifiManager();

        HwInnerWifiP2pManager getHwInnerWifiP2pManager();

        IHwInputMethodService getHwInputMethodService(Context context);

        HwKeyguardManager getHwKeyguardManager();

        HwLocalePickerManager getHwLocalePickerManager();

        IHwMediaMonitor getHwMediaMonitor();

        IHwMediaRecorder getHwMediaRecorder();

        HwMediaScannerManager getHwMediaScannerManager();

        HwMediaSessionManager getHwMediaSession();

        IHwMediaStore getHwMediaStoreManager();

        HwMtpDatabaseManager getHwMtpDatabaseManager();

        IHwNetworkPolicyManager getHwNetworkPolicyManager();

        IHwNotificationResource getHwNotificationResource();

        IHwNsdImpl getHwNsdImpl();

        HwPCManager getHwPCManager();

        IHwPCResourcesUtils getHwPCResourcesUtils(AssetManager assetManager);

        IHwPackageParser getHwPackageParser();

        HwPerformance getHwPerformance();

        IHwPowerProfileManager getHwPowerProfileManager();

        HwSysResource getHwResource(int i);

        HwSettingsManager getHwSettingsManager();

        HwStateListAnimator getHwStateListAnimator();

        IHwSystemManager getHwSystemManager();

        IHwThemeManagerFactory getHwThemeManagerFactory();

        IHwView getHwView();

        IHwViewRootImpl getHwViewRootImpl();

        IHwWallpaperInfoStub getHwWallpaperInfoStub(WallpaperInfo wallpaperInfo);

        HwWidgetManager getHwWidgetManager();

        LogException getLogException();

        PasswordUtil getPasswordUtil();

        IPressGestureDetector getPressGestureDetector(Context context, FrameLayout frameLayout, Context context2);

        IHwSecImmHelper getSecImmHelper(IInputMethodManager iInputMethodManager);

        IVRSystemServiceManager getVRSystemServiceManager();

        void updateImsServiceConfig(Context context, int i, boolean z);
    }

    public interface IHwFeatureLoader {
        void addDexPaths();

        void preloadClasses();
    }

    public interface IHwNetworkPolicyManager {
        NetworkPolicyManager getInstance(Context context, INetworkPolicyManager iNetworkPolicyManager);
    }

    public interface IHwSystemManager {
        HsmInterface getHsmInstance();
    }

    public interface IHwWallpaperManager {
        WallpaperManager getInstance(Context context, Handler handler);
    }

    public static IHwWallpaperManager getHuaweiWallpaperManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHuaweiWallpaperManager();
        }
        return null;
    }

    public static IHwNetworkPolicyManager getHwNetworkPolicyManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwNetworkPolicyManager();
        }
        return null;
    }

    private static Factory getImplObject() {
        if (obj != null) {
            return obj;
        }
        synchronized (mLock) {
            try {
                obj = (Factory) Class.forName("huawei.android.common.HwFrameworkFactoryImpl").newInstance();
            } catch (Exception e) {
                Log.e(TAG, ": reflection exception is " + e);
            }
        }
        if (obj != null) {
            Log.v(TAG, ": successes to get AllImpl object and return....");
            return obj;
        }
        Log.e(TAG, ": failes to get AllImpl object");
        return null;
    }

    public static HwMediaSessionManager getHwMediaSession() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwMediaSession();
        }
        return HwMediaSessionManagerDummy.getDefault();
    }

    public static IHwPackageParser getHwPackageParser() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPackageParser();
        }
        return null;
    }

    public static IHwViewRootImpl getHwViewRootImpl() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwViewRootImpl();
        }
        return null;
    }

    public static IPressGestureDetector getPressGestureDetector(Context context, FrameLayout docerView, Context contextActivity) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getPressGestureDetector(context, docerView, contextActivity);
        }
        return null;
    }

    public static HwSysResource getHwResource(int resourceType) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwResource(resourceType);
        }
        return null;
    }

    public static LogException getLogException() {
        return getImplObject().getLogException();
    }

    public static PasswordUtil getPasswordUtil() {
        return getImplObject().getPasswordUtil();
    }

    public static HwLocalePickerManager getHwLocalePickerManager() {
        return getImplObject().getHwLocalePickerManager();
    }

    public static HwPCManager getHwPCManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPCManager();
        }
        return null;
    }

    public static HwInnerTelephonyManager getHwInnerTelephonyManager() {
        return getImplObject().getHwInnerTelephonyManager();
    }

    public static HwBaseInnerSmsManager getHwBaseInnerSmsManager() {
        return getImplObject().getHwBaseInnerSmsManager();
    }

    public static HwKeyguardManager getHwKeyguardManager() {
        return getImplObject().getHwKeyguardManager();
    }

    public static HwInnerWifiManager getHwInnerWifiManager() {
        return getImplObject().getHwInnerWifiManager();
    }

    public static HwInnerWifiP2pManager getHwInnerWifiP2pManager() {
        return getImplObject().getHwInnerWifiP2pManager();
    }

    public static IVRSystemServiceManager getVRSystemServiceManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getVRSystemServiceManager();
        }
        return null;
    }

    public static IFreezeScreenApplicationMonitor getAppFreezeScreenMonitor() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getAppFreezeScreenMonitor();
        }
        return null;
    }

    public static IHwSystemManager getHwSystemManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwSystemManager();
        }
        return null;
    }

    public static HwWidgetManager getHwWidgetManager() {
        return getImplObject().getHwWidgetManager();
    }

    public static IHwMediaStore getHwMediaStoreManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwMediaStoreManager();
        }
        return HwMediaStoreDummy.getDefault();
    }

    public static HwMtpDatabaseManager getHwMtpDatabaseManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwMtpDatabaseManager();
        }
        return HwMtpDatabaseManagerDummy.getDefault();
    }

    public static HwMediaScannerManager getHwMediaScannerManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwMediaScannerManager();
        }
        return HwMediaScannerManagerDummy.getDefault();
    }

    public static Interpolator createHwInterpolator(String name, Context c, AttributeSet attrs) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.createHwInterpolator(name, c, attrs);
        }
        return null;
    }

    public static Interpolator createHwInterpolator(String name, Resources res, Theme theme, AttributeSet attrs) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.createHwInterpolator(name, res, theme, attrs);
        }
        return null;
    }

    public static HwAnimationManager getHwAnimationManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwAnimationManager();
        }
        return HwAnimationManagerDummy.getDefault();
    }

    public static HwSettingsManager getHwSettingsManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwSettingsManager();
        }
        return HwSettingsManagerDummy.getDefault();
    }

    public static IHwThemeManagerFactory getHwThemeManagerFactory() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwThemeManagerFactory();
        }
        return null;
    }

    public static IHwGalleryCacheManagerFactory getHwGalleryCacheManagerFactory() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwGalleryCacheManagerFactory();
        }
        return null;
    }

    public static IHwConfiguration getHwConfiguration() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwConfiguration();
        }
        return null;
    }

    public static IHwCoverManager getCoverManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getCoverManager();
        }
        return HwCoverManagerDummy.getDefault();
    }

    public static HwDrmManager getHwDrmManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwDrmManager();
        }
        return HwDrmManagerDummy.getDefault();
    }

    public static HwPackageManager getHwPackageManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwApplicationPackageManager();
        }
        return null;
    }

    public static Intent getHuaweiChooserIntent() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHuaweiChooserIntentImpl();
        }
        return null;
    }

    public static String getHuaweiResolverActivity(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHuaweiResolverActivityImpl(context);
        }
        return null;
    }

    public static AudioEffectLowPowerTask getAudioEffectLowPowerTask(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getAudioEffectLowPowerTaskImpl(context);
        }
        return null;
    }

    public static IHwWallpaperInfoStub getHwWallpaperInfoStub(WallpaperInfo ai) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwWallpaperInfoStub(ai);
        }
        return null;
    }

    public static IHwPowerProfileManager getHwPowerProfileManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPowerProfileManager();
        }
        return HwPowerProfileManagerDummy.getDefault();
    }

    public static IHwNsdImpl getHwNsdImpl() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwNsdImpl();
        }
        return null;
    }

    public static IHwMediaRecorder getHwMediaRecorder() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwMediaRecorder();
        }
        return HwMediaRecorderDummy.getDefault();
    }

    public static IHwAudioRecord getHwHwAudioRecord() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwHwAudioRecord();
        }
        return HwAudioRecordDummy.getDefault();
    }

    public static HwFlogManager getHwFlogManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwFlogManager();
        }
        return HwFlogManagerDummy.getDefault();
    }

    public static HwInnerConnectivityManager getHwInnerConnectivityManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwInnerConnectivityManager();
        }
        return null;
    }

    public static IHwDeviceManager getHuaweiDevicePolicyManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHuaweiDevicePolicyManagerImpl();
        }
        return null;
    }

    public static HwInnerNetworkManager getHwInnerNetworkManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwInnerNetworkManager();
        }
        return DummyHwInnerNetworkManager.getDefault();
    }

    public static HwStateListAnimator getHwStateListAnimator() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwStateListAnimator();
        }
        return HwStateListAnimatorDummy.getDefault();
    }

    public static IHwInputMethodService getHwInputMethodService(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwInputMethodService(context);
        }
        return null;
    }

    public static IHwSecImmHelper getSecImmHelper(IInputMethodManager service) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getSecImmHelper(service);
        }
        return null;
    }

    public static IHwInnerLocationManager getHwInnerLocationManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwInnerLocationManager();
        }
        return null;
    }

    public static HwPerformance getHwPerformance() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPerformance();
        }
        return HwPerformanceDummy.getDefault();
    }

    public static IHwMediaMonitor getHwMediaMonitor() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwMediaMonitor();
        }
        return HwMediaMonitorDummy.getDefault();
    }

    public static IHwNotificationResource getHwNotificationResource() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwNotificationResource();
        }
        return null;
    }

    public static IHwAdCleaner getHwAdCleaner() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwAdCleaner();
        }
        return null;
    }

    public static IHwDeviceAdminInfo getHwDeviceAdminInfo(Context context, ActivityInfo activityInfo) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwDeviceAdminInfo(context, activityInfo);
        }
        return HwDeviceAdminInfoDummy.getDefault();
    }

    public static HwFrameworkMonitor getHwFrameworkMonitor() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwFrameworkMonitor();
        }
        return null;
    }

    public static IApsManager getApsManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getApsManager();
        }
        return null;
    }

    public static IHwActivitySplitterImpl getHwActivitySplitterImpl(Activity a, boolean isBase) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwActivitySplitterImpl(a, isBase);
        }
        return null;
    }

    public static IHwHandler getHwHandler() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwHandler();
        }
        return null;
    }

    public static IHwView getHwView() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwView();
        }
        return null;
    }

    public static IHwFeatureLoader getHwFeatureLoader() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwFeatureLoader();
        }
        return null;
    }

    public static ClassLoader getHwFLClassLoaderParent(String dexPath) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwFLClassLoaderParent(dexPath);
        }
        return null;
    }

    public static void updateImsServiceConfig(Context context, int subId, boolean force) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.updateImsServiceConfig(context, subId, force);
        }
    }

    public static IForceRotationManager getForceRotationManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getForceRotationManager();
        }
        return null;
    }

    public static IHwPCResourcesUtils getHwPCResourcesUtils(AssetManager assetManager) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPCResourcesUtils(assetManager);
        }
        return null;
    }

    public static HwActivityThread getHwActivityThread() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwActivityThread();
        }
        return null;
    }

    public static IHwChangeButtonWindowCtrl getHwChangeButtonWindowCtrl(Activity a) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwChangeButtonWindowCtrl(a);
        }
        return null;
    }
}
