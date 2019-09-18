package android.common;

import android.app.Activity;
import android.app.HwKeyguardManager;
import android.app.IHwActivitySplitterImpl;
import android.app.IHwNotificationEx;
import android.app.IHwWallpaperInfoStub;
import android.app.IWallpaperManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.app.admin.HwDeviceAdminInfoDummy;
import android.app.admin.IHwDeviceAdminInfo;
import android.aps.IApsManager;
import android.camera.IHwCameraUtil;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.IHwPackageParser;
import android.content.res.AssetManager;
import android.content.res.IHwConfiguration;
import android.content.res.IHwPCResourcesUtils;
import android.content.res.Resources;
import android.cover.HwCoverManagerDummy;
import android.cover.IHwCoverManager;
import android.encrypt.PasswordUtil;
import android.graphics.IAwareBitmapCacher;
import android.hdm.HwDeviceManager;
import android.hsm.HwSystemManager;
import android.hwclipboarddelayread.IHwClipboardReadDelayRegisterFactory;
import android.hwclipboarddelayread.IHwClipboardReadDelayerFactory;
import android.hwgallerycache.IHwGalleryCacheManagerFactory;
import android.hwnotification.HwNotificationResource;
import android.hwtheme.IHwThemeManagerFactory;
import android.location.IHwInnerLocationManager;
import android.media.HwAudioRecordDummy;
import android.media.HwMediaRecorderDummy;
import android.media.IHwAudioRecord;
import android.media.IHwMediaMonitor;
import android.media.IHwMediaRecorder;
import android.media.hwmnote.IHwMnoteInterface;
import android.mtp.HwMtpDatabaseManager;
import android.mtp.HwMtpDatabaseManagerDummy;
import android.net.HwInnerConnectivityManager;
import android.net.INetworkPolicyManager;
import android.net.NetworkPolicyManager;
import android.net.booster.IHwCommBoosterServiceManager;
import android.net.wifi.DummyHwInnerNetworkManager;
import android.net.wifi.HwInnerNetworkManager;
import android.net.wifi.HwInnerWifiManager;
import android.net.wifi.p2p.HwInnerWifiP2pManager;
import android.os.Handler;
import android.os.IFreezeScreenApplicationMonitor;
import android.os.IHwHandler;
import android.pc.HwPCManager;
import android.perf.HwOptPackageParser;
import android.provider.HwMediaStoreDummy;
import android.provider.IHwMediaStore;
import android.rms.HwSysResource;
import android.rms.IHwAppInnerBoost;
import android.scrollerboost.IScrollerBoostMgr;
import android.telephony.HwInnerTelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LogException;
import android.view.IHwAppSceneImpl;
import android.view.IHwApsImpl;
import android.view.IHwView;
import android.view.IHwViewRootImpl;
import android.view.animation.Interpolator;
import android.view.autofill.IHwAutofillHelper;
import android.view.inputmethod.IHwSecImmHelper;
import android.vrsystem.IVRSystemServiceManager;
import android.widget.FrameLayout;
import android.widget.HwWidgetManager;
import android.zrhung.IAppEyeUiProbe;
import android.zrhung.IZrHung;
import com.android.internal.app.HwLocaleHelperManagerEx;
import com.android.internal.app.HwLocalePickerManager;
import com.android.internal.app.IHwLocalePickerInner;
import com.android.internal.app.IHwLocalePickerWithRegionInner;
import com.android.internal.app.IHwLocaleStoreInner;
import com.android.internal.app.IHwSuggestedLocaleAdapterInner;
import com.android.internal.os.HwPowerProfileManagerDummy;
import com.android.internal.os.HwZygoteInit;
import com.android.internal.os.IHwPowerProfileManager;
import com.android.internal.policy.IPressGestureDetector;
import com.android.internal.telephony.HwBaseInnerSmsManager;
import com.android.internal.view.IInputMethodManager;
import com.huawei.android.app.IHwLocaleHelperEx;
import com.huawei.android.app.IHwLocalePickerEx;
import com.huawei.android.app.IHwLocalePickerWithRegionEx;
import com.huawei.android.app.IHwLocaleStoreEx;
import com.huawei.android.app.IHwSuggestedLocaleAdapterEx;
import com.huawei.android.hardware.display.IHwDisplayManagerGlobalEx;
import com.huawei.android.hardware.display.IHwDisplayManagerGlobalInner;
import com.huawei.forcerotation.IForceRotationManager;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.hwperformance.HwPerformanceDummy;
import com.huawei.indexsearch.IIndexClearManager;
import com.huawei.indexsearch.IIndexSearchManager;
import com.huawei.indexsearch.IIndexSearchParser;
import huawei.android.security.IHwBehaviorCollectManager;
import huawei.cust.IHwCarrierConfigPolicy;
import huawei.cust.IHwGetCfgFileConfig;

public class HwFrameworkFactory {
    private static final String TAG = "HwFrameworkFactory";
    private static final Object mLock = new Object();
    private static Factory obj = null;

    public interface Factory {
        Interpolator createHwInterpolator(String str, Context context, AttributeSet attributeSet);

        Interpolator createHwInterpolator(String str, Resources resources, Resources.Theme theme, AttributeSet attributeSet);

        IAppEyeUiProbe getAppEyeUiProbe();

        IFreezeScreenApplicationMonitor getAppFreezeScreenMonitor();

        IApsManager getApsManager();

        IHwCoverManager getCoverManager();

        IForceRotationManager getForceRotationManager();

        Intent getHuaweiChooserIntentImpl();

        HwDeviceManager.IHwDeviceManager getHuaweiDevicePolicyManagerImpl();

        String getHuaweiResolverActivityImpl(Context context);

        IHwWallpaperManager getHuaweiWallpaperManager();

        IHwActivitySplitterImpl getHwActivitySplitterImpl(Activity activity, boolean z);

        HwActivityThread getHwActivityThread();

        HwAnimationManager getHwAnimationManager();

        IHwApiCacheManagerEx getHwApiCacheManagerEx();

        IHwAppInnerBoost getHwAppInnerBoostImpl();

        IHwAppSceneImpl getHwAppSceneImpl();

        HwPackageManager getHwApplicationPackageManager();

        IHwApsImpl getHwApsImpl();

        IHwAutofillHelper getHwAutofillHelper();

        HwBaseInnerSmsManager getHwBaseInnerSmsManager();

        IHwBehaviorCollectManager getHwBehaviorCollectManager();

        IHwCameraUtil getHwCameraUtil();

        IHwCarrierConfigPolicy getHwCarrierConfigPolicy();

        IHwGetCfgFileConfig getHwCfgFileConfig();

        IHwClipboardReadDelayRegisterFactory getHwClipboardReadDelayRegisterFactory();

        IHwClipboardReadDelayerFactory getHwClipboardReadDelayerFactory();

        IHwCommBoosterServiceManager getHwCommBoosterServiceManager();

        IHwConfiguration getHwConfiguration();

        IHwDeviceAdminInfo getHwDeviceAdminInfo(Context context, ActivityInfo activityInfo);

        IHwDisplayManagerGlobalEx getHwDisplayManagerGlobalEx(IHwDisplayManagerGlobalInner iHwDisplayManagerGlobalInner);

        HwDrmManager getHwDrmManager();

        HwFlogManager getHwFlogManager();

        HwFrameworkMonitor getHwFrameworkMonitor();

        IHwGalleryCacheManagerFactory getHwGalleryCacheManagerFactory();

        IHwHandler getHwHandler();

        IHwAudioRecord getHwHwAudioRecord();

        IAwareBitmapCacher getHwIAwareBitmapCacher();

        HwInnerConnectivityManager getHwInnerConnectivityManager();

        IHwInnerLocationManager getHwInnerLocationManager();

        HwInnerNetworkManager getHwInnerNetworkManager();

        HwInnerTelephonyManager getHwInnerTelephonyManager();

        HwInnerWifiManager getHwInnerWifiManager();

        HwInnerWifiP2pManager getHwInnerWifiP2pManager();

        HwKeyguardManager getHwKeyguardManager();

        IHwLocaleHelperEx getHwLocaleHelperEx(IHwLocaleStoreInner iHwLocaleStoreInner);

        HwLocaleHelperManagerEx getHwLocaleHelperManagerEx();

        IHwLocalePickerEx getHwLocalePickerEx(IHwLocalePickerInner iHwLocalePickerInner, Context context);

        HwLocalePickerManager getHwLocalePickerManager();

        IHwLocalePickerWithRegionEx getHwLocalePickerWithRegionEx(IHwLocalePickerWithRegionInner iHwLocalePickerWithRegionInner);

        IHwLocaleStoreEx getHwLocaleStoreEx();

        IHwMediaMonitor getHwMediaMonitor();

        IHwMediaRecorder getHwMediaRecorder();

        HwMediaScannerManager getHwMediaScannerManager();

        IHwMediaStore getHwMediaStoreManager();

        IHwMnoteInterface getHwMnoteInterface();

        HwMtpDatabaseManager getHwMtpDatabaseManager();

        IHwNetworkPolicyManager getHwNetworkPolicyManager();

        IHwNotificationEx getHwNotificationEx(Context context);

        HwNotificationResource.IHwNotificationResource getHwNotificationResource();

        HwOptPackageParser getHwOptPackageParser();

        HwPCManager getHwPCManager();

        IHwPCResourcesUtils getHwPCResourcesUtils(AssetManager assetManager);

        IHwPackageParser getHwPackageParser();

        HwPerformance getHwPerformance();

        IHwPowerProfileManager getHwPowerProfileManager();

        HwSysResource getHwResource(int i);

        HwSettingsManager getHwSettingsManager();

        IHwSuggestedLocaleAdapterEx getHwSuggestedLocaleAdapterEx(IHwSuggestedLocaleAdapterInner iHwSuggestedLocaleAdapterInner, Context context, boolean z);

        IHwSystemManager getHwSystemManager();

        IHwThemeManagerFactory getHwThemeManagerFactory();

        IHwView getHwView();

        IHwViewRootImpl getHwViewRootImpl();

        IHwWallpaperInfoStub getHwWallpaperInfoStub(WallpaperInfo wallpaperInfo);

        HwWidgetManager getHwWidgetManager();

        HwZygoteInit getHwZygoteInit();

        IScrollerBoostMgr getIScrollerBoostMgr();

        IIndexClearManager getIndexClearManager();

        IIndexSearchManager getIndexSearchManager();

        IIndexSearchParser getIndexSearchParser();

        LogException getLogException();

        PasswordUtil getPasswordUtil();

        IPressGestureDetector getPressGestureDetector(Context context, FrameLayout frameLayout, Context context2);

        IHwSecImmHelper getSecImmHelper(IInputMethodManager iInputMethodManager);

        IVRSystemServiceManager getVRSystemServiceManager();

        IZrHung getZrHung(String str);

        void updateImsServiceConfig(Context context, int i, boolean z);
    }

    public interface IHwNetworkPolicyManager {
        NetworkPolicyManager getInstance(Context context, INetworkPolicyManager iNetworkPolicyManager);
    }

    public interface IHwSystemManager {
        HwSystemManager.HsmInterface getHsmInstance();
    }

    public interface IHwWallpaperManager {
        WallpaperManager getInstance(IWallpaperManager iWallpaperManager, Context context, Handler handler);
    }

    public static IHwWallpaperManager getHuaweiWallpaperManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHuaweiWallpaperManager();
        }
        return null;
    }

    public static IHwNetworkPolicyManager getHwNetworkPolicyManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwNetworkPolicyManager();
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

    public static IHwPackageParser getHwPackageParser() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwPackageParser();
        }
        return null;
    }

    public static IHwViewRootImpl getHwViewRootImpl() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwViewRootImpl();
        }
        return null;
    }

    public static IPressGestureDetector getPressGestureDetector(Context context, FrameLayout docerView, Context contextActivity) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getPressGestureDetector(context, docerView, contextActivity);
        }
        return null;
    }

    public static HwSysResource getHwResource(int resourceType) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwResource(resourceType);
        }
        return null;
    }

    public static IZrHung getZrHung(String wpName) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getZrHung(wpName);
        }
        return null;
    }

    public static IAppEyeUiProbe getAppEyeUiProbe() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getAppEyeUiProbe();
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
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwPCManager();
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
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getVRSystemServiceManager();
        }
        return null;
    }

    public static IFreezeScreenApplicationMonitor getAppFreezeScreenMonitor() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getAppFreezeScreenMonitor();
        }
        return null;
    }

    public static IHwSystemManager getHwSystemManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwSystemManager();
        }
        return null;
    }

    public static HwWidgetManager getHwWidgetManager() {
        return getImplObject().getHwWidgetManager();
    }

    public static IHwMediaStore getHwMediaStoreManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwMediaStoreManager();
        }
        return HwMediaStoreDummy.getDefault();
    }

    public static HwMtpDatabaseManager getHwMtpDatabaseManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwMtpDatabaseManager();
        }
        return HwMtpDatabaseManagerDummy.getDefault();
    }

    public static HwMediaScannerManager getHwMediaScannerManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwMediaScannerManager();
        }
        return HwMediaScannerManagerDummy.getDefault();
    }

    public static Interpolator createHwInterpolator(String name, Context c, AttributeSet attrs) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.createHwInterpolator(name, c, attrs);
        }
        return null;
    }

    public static Interpolator createHwInterpolator(String name, Resources res, Resources.Theme theme, AttributeSet attrs) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.createHwInterpolator(name, res, theme, attrs);
        }
        return null;
    }

    public static HwAnimationManager getHwAnimationManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwAnimationManager();
        }
        return HwAnimationManagerDummy.getDefault();
    }

    public static HwSettingsManager getHwSettingsManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwSettingsManager();
        }
        return HwSettingsManagerDummy.getDefault();
    }

    public static IHwThemeManagerFactory getHwThemeManagerFactory() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwThemeManagerFactory();
        }
        return null;
    }

    public static IHwGalleryCacheManagerFactory getHwGalleryCacheManagerFactory() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwGalleryCacheManagerFactory();
        }
        return null;
    }

    public static IHwClipboardReadDelayerFactory getHwClipboardReadDelayerFactory() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwClipboardReadDelayerFactory();
        }
        return null;
    }

    public static IHwClipboardReadDelayRegisterFactory getHwClipboardReadDelayRegisterFactory() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwClipboardReadDelayRegisterFactory();
        }
        return null;
    }

    public static IHwConfiguration getHwConfiguration() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwConfiguration();
        }
        return null;
    }

    public static IHwCoverManager getCoverManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getCoverManager();
        }
        return HwCoverManagerDummy.getDefault();
    }

    public static HwDrmManager getHwDrmManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwDrmManager();
        }
        return HwDrmManagerDummy.getDefault();
    }

    public static HwPackageManager getHwPackageManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwApplicationPackageManager();
        }
        return null;
    }

    public static Intent getHuaweiChooserIntent() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHuaweiChooserIntentImpl();
        }
        return null;
    }

    public static String getHuaweiResolverActivity(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHuaweiResolverActivityImpl(context);
        }
        return null;
    }

    public static IHwWallpaperInfoStub getHwWallpaperInfoStub(WallpaperInfo ai) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwWallpaperInfoStub(ai);
        }
        return null;
    }

    public static IHwPowerProfileManager getHwPowerProfileManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwPowerProfileManager();
        }
        return HwPowerProfileManagerDummy.getDefault();
    }

    public static IHwApsImpl getHwApsImpl() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwApsImpl();
        }
        return null;
    }

    public static IHwAppSceneImpl getHwAppSceneImpl() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwAppSceneImpl();
        }
        return null;
    }

    public static IHwAppInnerBoost getHwAppInnerBoostImpl() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwAppInnerBoostImpl();
        }
        return null;
    }

    public static IHwMediaRecorder getHwMediaRecorder() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwMediaRecorder();
        }
        return HwMediaRecorderDummy.getDefault();
    }

    public static IHwAudioRecord getHwHwAudioRecord() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwHwAudioRecord();
        }
        return HwAudioRecordDummy.getDefault();
    }

    public static HwFlogManager getHwFlogManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwFlogManager();
        }
        return HwFlogManagerDummy.getDefault();
    }

    public static HwInnerConnectivityManager getHwInnerConnectivityManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwInnerConnectivityManager();
        }
        return null;
    }

    public static HwDeviceManager.IHwDeviceManager getHuaweiDevicePolicyManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHuaweiDevicePolicyManagerImpl();
        }
        return null;
    }

    public static HwInnerNetworkManager getHwInnerNetworkManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwInnerNetworkManager();
        }
        return DummyHwInnerNetworkManager.getDefault();
    }

    public static IHwSecImmHelper getSecImmHelper(IInputMethodManager service) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getSecImmHelper(service);
        }
        return null;
    }

    public static IHwInnerLocationManager getHwInnerLocationManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwInnerLocationManager();
        }
        return null;
    }

    public static HwPerformance getHwPerformance() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwPerformance();
        }
        return HwPerformanceDummy.getDefault();
    }

    public static IHwMediaMonitor getHwMediaMonitor() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwMediaMonitor();
        }
        return null;
    }

    public static HwNotificationResource.IHwNotificationResource getHwNotificationResource() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwNotificationResource();
        }
        return null;
    }

    public static IHwDeviceAdminInfo getHwDeviceAdminInfo(Context context, ActivityInfo activityInfo) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwDeviceAdminInfo(context, activityInfo);
        }
        return HwDeviceAdminInfoDummy.getDefault();
    }

    public static HwFrameworkMonitor getHwFrameworkMonitor() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwFrameworkMonitor();
        }
        return null;
    }

    public static IApsManager getApsManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getApsManager();
        }
        return null;
    }

    public static IHwActivitySplitterImpl getHwActivitySplitterImpl(Activity a, boolean isBase) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwActivitySplitterImpl(a, isBase);
        }
        return null;
    }

    public static IHwHandler getHwHandler() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwHandler();
        }
        return null;
    }

    public static IHwView getHwView() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwView();
        }
        return null;
    }

    public static void updateImsServiceConfig(Context context, int subId, boolean force) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.updateImsServiceConfig(context, subId, force);
        }
    }

    public static IForceRotationManager getForceRotationManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getForceRotationManager();
        }
        return null;
    }

    public static IHwPCResourcesUtils getHwPCResourcesUtils(AssetManager assetManager) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwPCResourcesUtils(assetManager);
        }
        return null;
    }

    public static HwActivityThread getHwActivityThread() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwActivityThread();
        }
        return null;
    }

    public static IHwApiCacheManagerEx getHwApiCacheManagerEx() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwApiCacheManagerEx();
        }
        return null;
    }

    public static IHwCommBoosterServiceManager getHwCommBoosterServiceManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwCommBoosterServiceManager();
        }
        return null;
    }

    public static IHwMnoteInterface getHwMnoteInterface() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwMnoteInterface();
        }
        return null;
    }

    public static IHwCarrierConfigPolicy getHwCarrierConfigPolicy() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwCarrierConfigPolicy();
        }
        return null;
    }

    public static IHwBehaviorCollectManager getHwBehaviorCollectManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwBehaviorCollectManager();
        }
        return null;
    }

    public static IHwGetCfgFileConfig getHwCfgFileConfig() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwCfgFileConfig();
        }
        return null;
    }

    public static HwOptPackageParser getHwOptPackageParser() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwOptPackageParser();
        }
        return null;
    }

    public static IIndexSearchManager getIndexSearchManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getIndexSearchManager();
        }
        return null;
    }

    public static IIndexSearchParser getIndexSearchParser() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getIndexSearchParser();
        }
        return null;
    }

    public static IIndexClearManager getIndexClearManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getIndexClearManager();
        }
        return null;
    }

    public static IHwAutofillHelper getHwAutofillHelper() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwAutofillHelper();
        }
        return null;
    }

    public static IAwareBitmapCacher getHwIAwareBitmapCacher() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwIAwareBitmapCacher();
        }
        return null;
    }

    public static IHwNotificationEx getHwNotificationEx(Context mContext) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwNotificationEx(mContext);
        }
        return null;
    }

    public static IHwDisplayManagerGlobalEx getHwDisplayManagerGlobalEx(IHwDisplayManagerGlobalInner dmg) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwDisplayManagerGlobalEx(dmg);
        }
        return null;
    }

    public static IHwCameraUtil getHwCameraUtil() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwCameraUtil();
        }
        return null;
    }

    public static HwZygoteInit getHwZygoteInit() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwZygoteInit();
        }
        return null;
    }

    public static IScrollerBoostMgr getIScrollerBoostMgr() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getIScrollerBoostMgr();
        }
        return null;
    }

    public static IHwSuggestedLocaleAdapterEx getHwSuggestedLocaleAdapterEx(IHwSuggestedLocaleAdapterInner inner, Context context, boolean isShowAddedHeaders) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwSuggestedLocaleAdapterEx(inner, context, isShowAddedHeaders);
        }
        return null;
    }

    public static IHwLocalePickerWithRegionEx getHwLocalePickerWithRegionEx(IHwLocalePickerWithRegionInner lpw) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwLocalePickerWithRegionEx(lpw);
        }
        return null;
    }

    public static IHwLocalePickerEx getHwLocalePickerEx(IHwLocalePickerInner inner, Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwLocalePickerEx(inner, context);
        }
        return null;
    }

    public static IHwLocaleStoreEx getHwLocaleStoreEx() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwLocaleStoreEx();
        }
        return null;
    }

    public static IHwLocaleHelperEx getHwLocaleHelperEx(IHwLocaleStoreInner inner) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwLocaleHelperEx(inner);
        }
        return null;
    }

    public static HwLocaleHelperManagerEx getHwLocaleHelperManagerEx() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwLocaleHelperManagerEx();
        }
        return null;
    }
}
