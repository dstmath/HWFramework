package android.common;

import android.app.Activity;
import android.app.HwKeyguardManager;
import android.app.IHwActivitySplitterImpl;
import android.app.IHwNotificationEx;
import android.app.IHwWallpaperInfoStub;
import android.app.IHwWallpaperManagerEx;
import android.app.IWallpaperManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.app.admin.HwDeviceAdminInfoDummy;
import android.app.admin.IHwDeviceAdminInfo;
import android.aps.IApsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.IHwPackageParser;
import android.content.res.IHwConfiguration;
import android.content.res.Resources;
import android.cover.IHwCoverManager;
import android.database.sqlite.IHwSQLiteDatabase;
import android.database.sqlite.IHwSQLiteSession;
import android.encrypt.PasswordUtil;
import android.graphics.IAwareBitmapCacher;
import android.graphics.Rect;
import android.hdm.HwDeviceManager;
import android.hwclipboarddelayread.IHwClipboardReadDelayRegisterFactory;
import android.hwclipboarddelayread.IHwClipboardReadDelayerFactory;
import android.hwgallerycache.IHwGalleryCacheManagerFactory;
import android.hwnotification.HwNotificationResource;
import android.hwtheme.IHwThemeManagerFactory;
import android.iawareperf.IHwRtgSchedImpl;
import android.location.IHwInnerLocationManager;
import android.magicwin.HwMagicWindow;
import android.net.HwInnerConnectivityManager;
import android.net.INetworkPolicyManager;
import android.net.NetworkPolicyManager;
import android.net.booster.HwDataServiceQoe;
import android.net.booster.IHwCommBoosterServiceManager;
import android.net.wifi.DummyHwInnerNetworkManager;
import android.net.wifi.HwInnerNetworkManager;
import android.net.wifi.HwInnerWifiManager;
import android.net.wifi.p2p.HwInnerWifiP2pManager;
import android.os.IBlockMonitor;
import android.os.IFreezeScreenApplicationMonitor;
import android.pc.HwPCManager;
import android.perf.HwOptPackageParser;
import android.provider.HwMediaStoreDummy;
import android.provider.IHwMediaStore;
import android.rms.HwSysResource;
import android.rms.IHwAppInnerBoost;
import android.scrollerboostmanager.IScrollerBoostMgr;
import android.telephony.HwInnerTelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LogException;
import android.view.IHwView;
import android.view.IHwViewRootImpl;
import android.view.animation.Animation;
import android.view.animation.ClipRectAnimation;
import android.view.animation.Interpolator;
import android.view.autofill.IHwAutofillHelper;
import android.view.inputmethod.IHwSecImmHelper;
import android.vrsystem.IVRSystemServiceManager;
import android.widget.FrameLayout;
import android.widget.HwWidgetManager;
import android.zrhung.IAppEyeUiProbe;
import android.zrhung.IZrHung;
import com.android.internal.app.HwLocalePickerManager;
import com.android.internal.app.IHwLocalePickerInner;
import com.android.internal.app.IHwLocalePickerWithRegionInner;
import com.android.internal.app.IHwLocaleStoreInner;
import com.android.internal.app.IHwSuggestedLocaleAdapterInner;
import com.android.internal.os.HwPowerProfileManagerDummy;
import com.android.internal.os.HwZygoteInit;
import com.android.internal.os.IHwPowerProfileManager;
import com.android.internal.os.IHwZygoteEx;
import com.android.internal.os.IHwZygoteProcessEx;
import com.android.internal.policy.IPressGestureDetector;
import com.android.internal.telephony.HwBaseInnerSmsManager;
import com.android.internal.view.IInputMethodManager;
import com.huawei.android.app.IHwLocaleHelperEx;
import com.huawei.android.app.IHwLocalePickerEx;
import com.huawei.android.app.IHwLocalePickerWithRegionEx;
import com.huawei.android.app.IHwLocaleStoreEx;
import com.huawei.android.app.IHwSuggestedLocaleAdapterEx;
import com.huawei.android.app.IWallpaperManagerEx;
import com.huawei.android.hardware.display.IHwDisplayManagerGlobalEx;
import com.huawei.android.hardware.display.IHwDisplayManagerGlobalInner;
import com.huawei.android.hwforcedark.HwForceDarkManagerDummy;
import com.huawei.android.hwforcedark.IHwForceDarkManager;
import com.huawei.android.view.IHwDisplaySideRegion;
import com.huawei.android.view.IHwExtDisplaySizeUtil;
import com.huawei.forcerotation.IForceRotationManager;
import com.huawei.hwaps.IHwApsImpl;
import com.huawei.indexsearch.IIndexSearchManager;
import com.huawei.sidetouch.IHwSideStatusManager;
import huawei.android.hwperformance.HwPerformance;
import huawei.android.hwperformance.HwPerformanceDummy;
import huawei.android.security.IHwBehaviorCollectManager;
import huawei.cust.IHwCarrierConfigPolicy;
import huawei.cust.IHwGetCfgFileConfig;
import huawei.hiview.HiTrace;
import huawei.hiview.HiTraceDummy;
import huawei.hiview.HiTraceHandler;
import huawei.hiview.HiTraceHandlerDummy;
import huawei.hiview.HiTraceId;
import huawei.hiview.HiTraceIdDummy;

public class HwFrameworkFactory {
    private static final String TAG = "HwFrameworkFactory";
    private static final Object mLock = new Object();
    private static IHwRtgSchedImpl mRtgImpl = null;
    private static Factory obj = null;

    public interface Factory {
        Animation createHwClipRectAnimation(Context context, AttributeSet attributeSet);

        Interpolator createHwInterpolator(String str, Context context, AttributeSet attributeSet);

        Interpolator createHwInterpolator(String str, Resources resources, Resources.Theme theme, AttributeSet attributeSet);

        IAppEyeUiProbe getAppEyeUiProbe();

        IFreezeScreenApplicationMonitor getAppFreezeScreenMonitor();

        IApsManager getApsManager();

        IBlockMonitor getBlockMonitor();

        IForceRotationManager getForceRotationManager();

        HiTrace getHiTrace();

        HiTraceHandler getHiTraceHandler();

        HiTraceId getHiTraceId();

        HiTraceId getHiTraceId(byte[] bArr);

        Intent getHuaweiChooserIntentImpl();

        HwDeviceManager.IHwDeviceManager getHuaweiDevicePolicyManagerImpl();

        String getHuaweiResolverActivityImpl(Context context);

        IHwActivitySplitterImpl getHwActivitySplitterImpl(Activity activity, boolean z);

        HwAnimationManager getHwAnimationManager();

        IHwApiCacheManagerEx getHwApiCacheManagerEx();

        IHwAppInnerBoost getHwAppInnerBoostImpl();

        IHwApsImpl getHwApsImpl();

        IHwAutofillHelper getHwAutofillHelper();

        IHwCarrierConfigPolicy getHwCarrierConfigPolicy();

        IHwGetCfgFileConfig getHwCfgFileConfig();

        IHwClipboardReadDelayRegisterFactory getHwClipboardReadDelayRegisterFactory();

        IHwClipboardReadDelayerFactory getHwClipboardReadDelayerFactory();

        IHwCommBoosterServiceManager getHwCommBoosterServiceManager();

        HwDataServiceQoe getHwDataServiceQoe();

        IHwDeviceAdminInfo getHwDeviceAdminInfo(Context context, ActivityInfo activityInfo);

        IHwDisplayManagerGlobalEx getHwDisplayManagerGlobalEx(IHwDisplayManagerGlobalInner iHwDisplayManagerGlobalInner);

        IHwDisplaySideRegion getHwDisplaySideRegion(Rect rect);

        IHwExtDisplaySizeUtil getHwExtDisplaySizeUtil();

        ClassLoader getHwFLClassLoaderParent(String str);

        IHwFeatureLoader getHwFeatureLoader();

        HwFlogManager getHwFlogManager();

        IHwForceDarkManager getHwForceDarkManager();

        HwFrameworkMonitor getHwFrameworkMonitor();

        IHwGalleryCacheManagerFactory getHwGalleryCacheManagerFactory();

        IAwareBitmapCacher getHwIAwareBitmapCacher();

        HwInnerConnectivityManager getHwInnerConnectivityManager();

        IHwInnerLocationManager getHwInnerLocationManager();

        HwInnerNetworkManager getHwInnerNetworkManager();

        HwInnerWifiManager getHwInnerWifiManager();

        HwInnerWifiP2pManager getHwInnerWifiP2pManager();

        HwKeyguardManager getHwKeyguardManager();

        IHwLoadedApk getHwLoadedApk();

        IHwLocaleHelperEx getHwLocaleHelperEx();

        IHwLocaleHelperEx getHwLocaleHelperEx(IHwLocaleStoreInner iHwLocaleStoreInner);

        IHwLocalePickerEx getHwLocalePickerEx();

        IHwLocalePickerEx getHwLocalePickerEx(IHwLocalePickerInner iHwLocalePickerInner, Context context);

        HwLocalePickerManager getHwLocalePickerManager();

        IHwLocalePickerWithRegionEx getHwLocalePickerWithRegionEx(IHwLocalePickerWithRegionInner iHwLocalePickerWithRegionInner);

        IHwLocaleStoreEx getHwLocaleStoreEx();

        HwMagicWindow getHwMagicWindow();

        HwMediaScannerManager getHwMediaScannerManager();

        IHwMediaStore getHwMediaStoreManager();

        IHwNetworkPolicyManager getHwNetworkPolicyManager();

        IHwNotificationEx getHwNotificationEx(Context context);

        HwNotificationResource.IHwNotificationResource getHwNotificationResource();

        HwOptPackageParser getHwOptPackageParser();

        HwPCManager getHwPCManager();

        HwPerformance getHwPerformance();

        IHwPowerProfileManager getHwPowerProfileManager();

        HwSysResource getHwResource(int i);

        IHwRtgSchedImpl getHwRtgSchedImpl();

        IHwSQLiteDatabase getHwSQLiteDatabase();

        IHwSQLiteSession getHwSQLiteSession();

        HwSettingsManager getHwSettingsManager();

        IHwSideStatusManager getHwSideStatusManager(Context context);

        IHwSuggestedLocaleAdapterEx getHwSuggestedLocaleAdapterEx(IHwSuggestedLocaleAdapterInner iHwSuggestedLocaleAdapterInner, Context context, boolean z);

        HwWidgetManager getHwWidgetManager();

        IHwZygoteEx getHwZygoteEx();

        HwZygoteInit getHwZygoteInit();

        IHwZygoteProcessEx getHwZygoteProcessEx();

        IScrollerBoostMgr getIScrollerBoostMgr();

        IIndexSearchManager getIndexSearchManager();

        LogException getLogException();

        PasswordUtil getPasswordUtil();

        IPressGestureDetector getPressGestureDetector(Context context, FrameLayout frameLayout, Context context2);

        IHwSecImmHelper getSecImmHelper(IInputMethodManager iInputMethodManager);

        IVRSystemServiceManager getVRSystemServiceManager();

        IZrHung getZrHung(String str);

        void updateImsServiceConfig(Context context, int i, boolean z);
    }

    public interface IHwFeatureLoader {
        void addDexPaths();

        void preloadClasses();
    }

    public interface IHwNetworkPolicyManager {
        NetworkPolicyManager getInstance(Context context, INetworkPolicyManager iNetworkPolicyManager);
    }

    public static IHwWallpaperManagerEx getHuaweiWallpaperManager(Context context, IWallpaperManager service, WallpaperManager wm) {
        return HwPartBasicPlatformFactory.loadFactory(HwPartBasicPlatformFactory.BASIC_PLATFORM_FACTORY_IMPL_NAME).getHuaweiWallpaperManager(context, new IWallpaperManagerEx(service), wm);
    }

    public static IHwNetworkPolicyManager getHwNetworkPolicyManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwNetworkPolicyManager();
        }
        return null;
    }

    private static Factory getImplObject() {
        synchronized (mLock) {
            if (obj != null) {
                return obj;
            }
            try {
                obj = (Factory) Class.forName("huawei.android.common.HwFrameworkFactoryImpl").newInstance();
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "class not found.");
            } catch (Exception e2) {
                Log.e(TAG, "reflection exception");
            }
            if (obj != null) {
                Log.v(TAG, "successes to get AllImpl object and return....");
                return obj;
            }
            Log.e(TAG, "failes to get AllImpl object");
            return null;
        }
    }

    public static IHwPackageParser getHwPackageParser() {
        return HwPartBasicPlatformFactory.loadFactory(HwPartBasicPlatformFactory.BASIC_PLATFORM_FACTORY_IMPL_NAME).getHwPackageParser();
    }

    public static IHwViewRootImpl getHwViewRootImpl() {
        return HwPartBasicPlatformFactory.loadFactory(HwPartBasicPlatformFactory.BASIC_PLATFORM_FACTORY_IMPL_NAME).getHwViewRootImpl();
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

    public static HwMagicWindow getHwMagicWindow() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwMagicWindow();
        }
        return null;
    }

    public static HwInnerTelephonyManager getHwInnerTelephonyManager() {
        return HwPartBaseTelephonyFactory.loadFactory().createHwInnerTelephonyManager();
    }

    public static HwBaseInnerSmsManager getHwBaseInnerSmsManager() {
        return HwPartBaseTelephonyFactory.loadFactory().createHwBaseInnerSmsManager();
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

    public static Animation createHwClipRectAnimation(Context context, AttributeSet attrs) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.createHwClipRectAnimation(context, attrs);
        }
        return new ClipRectAnimation(0, 0, 0, 0, 0, 0, 0, 0);
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
        return HwPartBasicPlatformFactory.loadFactory(HwPartBasicPlatformFactory.BASIC_PLATFORM_FACTORY_IMPL_NAME).getHwThemeManagerFactory();
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
        return HwPartBasicPlatformFactory.loadFactory(HwPartBasicPlatformFactory.BASIC_PLATFORM_FACTORY_IMPL_NAME).createHwConfiguration();
    }

    public static IHwCoverManager getCoverManager() {
        return HwPartBasicPlatformFactory.loadFactory(HwPartBasicPlatformFactory.BASIC_PLATFORM_FACTORY_IMPL_NAME).getCoverManager();
    }

    public static HwPackageManager getHwPackageManager() {
        return HwPartBasicPlatformFactory.loadFactory(HwPartBasicPlatformFactory.BASIC_PLATFORM_FACTORY_IMPL_NAME).getHwApplicationPackageManager();
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
        return HwPartBasicPlatformFactory.loadFactory(HwPartBasicPlatformFactory.BASIC_PLATFORM_FACTORY_IMPL_NAME).getHwWallpaperInfoStub(ai);
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

    public static IHwAppInnerBoost getHwAppInnerBoostImpl() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwAppInnerBoostImpl();
        }
        return null;
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

    public static IHwView getHwView() {
        return HwPartBasicPlatformFactory.loadFactory(HwPartBasicPlatformFactory.BASIC_PLATFORM_FACTORY_IMPL_NAME).getHwView();
    }

    public static IHwFeatureLoader getHwFeatureLoader() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwFeatureLoader();
        }
        return null;
    }

    public static ClassLoader getHwFLClassLoaderParent(String dexPath) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwFLClassLoaderParent(dexPath);
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

    public static HwActivityThread getHwActivityThread() {
        return HwPartBasicPlatformFactory.loadFactory(HwPartBasicPlatformFactory.BASIC_PLATFORM_FACTORY_IMPL_NAME).getHwActivityThread();
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

    public static HwDataServiceQoe getHwDataServiceQoe() {
        Factory object = getImplObject();
        if (object != null) {
            return object.getHwDataServiceQoe();
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
        return HwFrameworkSecurityPartsFactory.getInstance().getInnerHwBehaviorCollectManager();
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

    public static IHwSQLiteDatabase getHwSQLiteDatabase() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwSQLiteDatabase();
        }
        return null;
    }

    public static IHwSQLiteSession getHwSQLiteSession() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwSQLiteSession();
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

    public static IScrollerBoostMgr getIScrollerBoostMgr() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getIScrollerBoostMgr();
        }
        return null;
    }

    public static IBlockMonitor getBlockMonitor() {
        Factory factory = getImplObject();
        if (factory != null) {
            return factory.getBlockMonitor();
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

    public static IHwLoadedApk getHwLoadedApk() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwLoadedApk();
        }
        return null;
    }

    public static IHwExtDisplaySizeUtil getHwExtDisplaySizeUtil() {
        getImplObject();
        Factory factory = obj;
        if (factory != null) {
            return factory.getHwExtDisplaySizeUtil();
        }
        return null;
    }

    public static IHwDisplaySideRegion getHwDisplaySideRegion(Rect rect) {
        getImplObject();
        Factory factory = obj;
        if (factory != null) {
            return factory.getHwDisplaySideRegion(rect);
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

    public static IHwLocalePickerEx getHwLocalePickerEx() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwLocalePickerEx();
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

    public static IHwLocaleHelperEx getHwLocaleHelperEx() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwLocaleHelperEx();
        }
        return null;
    }

    public static void setRtgSchedImpl(IHwRtgSchedImpl ins) {
        mRtgImpl = ins;
    }

    public static IHwRtgSchedImpl getHwRtgSchedImpl() {
        return mRtgImpl;
    }

    public static IHwRtgSchedImpl getHwRtgSchedInstance() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwRtgSchedImpl();
        }
        return null;
    }

    public static IHwZygoteEx getHwZygoteEx() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwZygoteEx();
        }
        return null;
    }

    public static IHwZygoteProcessEx getHwZygoteProcessEx() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwZygoteProcessEx();
        }
        return null;
    }

    public static IHwSideStatusManager getHwSideStatusManager(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwSideStatusManager(context);
        }
        return null;
    }

    public static IHwForceDarkManager getHwForceDarkManager() {
        Factory factory = getImplObject();
        if (factory != null) {
            return factory.getHwForceDarkManager();
        }
        return HwForceDarkManagerDummy.getDefault();
    }

    public static HiTraceId getHiTraceId() {
        Factory facObj = getImplObject();
        if (facObj != null) {
            return facObj.getHiTraceId();
        }
        return new HiTraceIdDummy();
    }

    public static HiTraceId getHiTraceId(byte[] idArray) {
        Factory facObj = getImplObject();
        if (facObj != null) {
            return facObj.getHiTraceId(idArray);
        }
        return new HiTraceIdDummy();
    }

    public static HiTrace getHiTrace() {
        Factory facObj = getImplObject();
        if (facObj != null) {
            return facObj.getHiTrace();
        }
        return new HiTraceDummy();
    }

    public static HiTraceHandler getHiTraceHandler() {
        Factory facObj = getImplObject();
        if (facObj != null) {
            return facObj.getHiTraceHandler();
        }
        return new HiTraceHandlerDummy();
    }
}
