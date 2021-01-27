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
import android.aps.IApsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IHwPackageParser;
import android.content.res.IHwConfiguration;
import android.content.res.Resources;
import android.cover.IHwCoverManager;
import android.database.IHwDatabaseErrorReporter;
import android.database.sqlite.IHwSQLiteDatabase;
import android.database.sqlite.IHwSQLiteSession;
import android.graphics.IAwareBitmapCacher;
import android.graphics.Rect;
import android.hwclipboarddelayread.IHwClipboardReadDelayRegisterFactory;
import android.hwclipboarddelayread.IHwClipboardReadDelayerFactory;
import android.hwgallerycache.IHwGalleryCacheManagerFactory;
import android.hwnotification.HwNotificationResource;
import android.hwtheme.IHwThemeManagerFactory;
import android.iawareperf.IHwRtgSchedImpl;
import android.location.IHwInnerLocationManager;
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
import android.view.DefaultHwRio;
import android.view.IHwBlurWindowManager;
import android.view.IHwRio;
import android.view.IHwView;
import android.view.IHwViewGroup;
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
import com.huawei.android.view.IHwShadowManager;
import com.huawei.forcerotation.IForceRotationManager;
import com.huawei.hwaps.IHwApsImpl;
import com.huawei.indexsearch.IIndexSearchManager;
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
    private static final Object LOCK = new Object();
    private static final String TAG = "HwFrameworkFactory";
    private static IHwRtgSchedImpl mRtgImpl = null;
    private static Factory sFactory = null;

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

        String getHuaweiResolverActivityImpl(Context context);

        IHwActivitySplitterImpl getHwActivitySplitterImpl(Activity activity, boolean z);

        HwAnimationManager getHwAnimationManager();

        IHwApiCacheManagerEx getHwApiCacheManagerEx();

        IHwAppInnerBoost getHwAppInnerBoostImpl();

        IHwApsImpl getHwApsImpl();

        IHwAutofillHelper getHwAutofillHelper();

        IHwBlurWindowManager getHwBlurWindowManager();

        IHwCarrierConfigPolicy getHwCarrierConfigPolicy();

        IHwGetCfgFileConfig getHwCfgFileConfig();

        IHwClipboardReadDelayRegisterFactory getHwClipboardReadDelayRegisterFactory();

        IHwClipboardReadDelayerFactory getHwClipboardReadDelayerFactory();

        IHwCommBoosterServiceManager getHwCommBoosterServiceManager();

        HwDataServiceQoe getHwDataServiceQoe();

        IHwDatabaseErrorReporter getHwDatabaseErrorReporter();

        IHwDisplayManagerGlobalEx getHwDisplayManagerGlobalEx(IHwDisplayManagerGlobalInner iHwDisplayManagerGlobalInner);

        IHwDisplaySideRegion getHwDisplaySideRegion(Rect rect);

        IHwExtDisplaySizeUtil getHwExtDisplaySizeUtil();

        HwFlogManager getHwFlogManager();

        IHwForceDarkManager getHwForceDarkManager();

        HwFrameworkMonitor getHwFrameworkMonitor();

        IHwGalleryCacheManagerFactory getHwGalleryCacheManagerFactory();

        IHwHarmonyServiceManager getHwHarmonyServiceManager();

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

        IHwRio getHwRio();

        IHwRtgSchedImpl getHwRtgSchedImpl();

        IHwSQLiteDatabase getHwSQLiteDatabase();

        IHwSQLiteSession getHwSQLiteSession();

        HwSettingsManager getHwSettingsManager();

        IHwShadowManager getHwShadowManager(Context context);

        IHwSuggestedLocaleAdapterEx getHwSuggestedLocaleAdapterEx(IHwSuggestedLocaleAdapterInner iHwSuggestedLocaleAdapterInner, Context context, boolean z);

        IHwViewGroup getHwViewGroup();

        HwWidgetManager getHwWidgetManager();

        IHwZygoteEx getHwZygoteEx();

        HwZygoteInit getHwZygoteInit();

        IHwZygoteProcessEx getHwZygoteProcessEx();

        IScrollerBoostMgr getIScrollerBoostMgr();

        IIndexSearchManager getIndexSearchManager();

        LogException getLogException();

        IPressGestureDetector getPressGestureDetector(Context context, FrameLayout frameLayout, Context context2);

        IHwSecImmHelper getSecImmHelper(IInputMethodManager iInputMethodManager);

        IVRSystemServiceManager getVRSystemServiceManager();

        IZrHung getZrHung(String str);

        void updateImsServiceConfig(Context context, int i, boolean z);
    }

    public interface IHwNetworkPolicyManager {
        NetworkPolicyManager getInstance(Context context, INetworkPolicyManager iNetworkPolicyManager);
    }

    private HwFrameworkFactory() {
    }

    public static IHwWallpaperManagerEx getHuaweiWallpaperManager(Context context, IWallpaperManager service, WallpaperManager wm) {
        return HwPartBasicPlatformFactory.loadFactory(HwPartBasicPlatformFactory.BASIC_PLATFORM_FACTORY_IMPL_NAME).getHuaweiWallpaperManager(context, new IWallpaperManagerEx(service), wm);
    }

    public static IHwNetworkPolicyManager getHwNetworkPolicyManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwNetworkPolicyManager();
        }
        return null;
    }

    private static Factory getImplObject() {
        synchronized (LOCK) {
            if (sFactory != null) {
                return sFactory;
            }
            try {
                sFactory = (Factory) Class.forName("huawei.android.common.HwFrameworkFactoryImpl").newInstance();
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "class not found.");
            } catch (Exception e2) {
                Log.e(TAG, "reflection exception");
            }
            if (sFactory != null) {
                Log.v(TAG, "successes to get AllImpl object and return....");
                return sFactory;
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

    public static IZrHung getZrHung(String wpName) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getZrHung(wpName);
        }
        return null;
    }

    public static IAppEyeUiProbe getAppEyeUiProbe() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getAppEyeUiProbe();
        }
        return null;
    }

    public static LogException getLogException() {
        return getImplObject().getLogException();
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

    public static HwMediaScannerManager getHwMediaScannerManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwMediaScannerManager();
        }
        return HwMediaScannerManagerDummy.getDefault();
    }

    public static Interpolator createHwInterpolator(String name, Context context, AttributeSet attrs) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.createHwInterpolator(name, context, attrs);
        }
        return null;
    }

    public static Interpolator createHwInterpolator(String name, Resources res, Resources.Theme theme, AttributeSet attrs) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.createHwInterpolator(name, res, theme, attrs);
        }
        return null;
    }

    public static Animation createHwClipRectAnimation(Context context, AttributeSet attrs) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.createHwClipRectAnimation(context, attrs);
        }
        return new ClipRectAnimation(0, 0, 0, 0, 0, 0, 0, 0);
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
        return HwPartBasicPlatformFactory.loadFactory(HwPartBasicPlatformFactory.BASIC_PLATFORM_FACTORY_IMPL_NAME).getHwThemeManagerFactory();
    }

    public static IHwGalleryCacheManagerFactory getHwGalleryCacheManagerFactory() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwGalleryCacheManagerFactory();
        }
        return null;
    }

    public static IHwClipboardReadDelayerFactory getHwClipboardReadDelayerFactory() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwClipboardReadDelayerFactory();
        }
        return null;
    }

    public static IHwClipboardReadDelayRegisterFactory getHwClipboardReadDelayRegisterFactory() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwClipboardReadDelayRegisterFactory();
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

    public static IHwWallpaperInfoStub getHwWallpaperInfoStub(WallpaperInfo ai) {
        return HwPartBasicPlatformFactory.loadFactory(HwPartBasicPlatformFactory.BASIC_PLATFORM_FACTORY_IMPL_NAME).getHwWallpaperInfoStub(ai);
    }

    public static IHwPowerProfileManager getHwPowerProfileManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPowerProfileManager();
        }
        return HwPowerProfileManagerDummy.getDefault();
    }

    public static IHwApsImpl getHwApsImpl() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwApsImpl();
        }
        return null;
    }

    public static IHwAppInnerBoost getHwAppInnerBoostImpl() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwAppInnerBoostImpl();
        }
        return null;
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

    public static HwInnerNetworkManager getHwInnerNetworkManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwInnerNetworkManager();
        }
        return DummyHwInnerNetworkManager.getDefault();
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

    public static HwNotificationResource.IHwNotificationResource getHwNotificationResource() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwNotificationResource();
        }
        return null;
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

    public static IHwActivitySplitterImpl getHwActivitySplitterImpl(Activity activity, boolean isBase) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwActivitySplitterImpl(activity, isBase);
        }
        return null;
    }

    public static IHwView getHwView() {
        return HwPartBasicPlatformFactory.loadFactory(HwPartBasicPlatformFactory.BASIC_PLATFORM_FACTORY_IMPL_NAME).getHwView();
    }

    public static void updateImsServiceConfig(Context context, int subId, boolean isForceUpdate) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.updateImsServiceConfig(context, subId, isForceUpdate);
        }
    }

    public static IForceRotationManager getForceRotationManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getForceRotationManager();
        }
        return null;
    }

    public static HwActivityThread getHwActivityThread() {
        return HwPartBasicPlatformFactory.loadFactory(HwPartBasicPlatformFactory.BASIC_PLATFORM_FACTORY_IMPL_NAME).getHwActivityThread();
    }

    public static IHwApiCacheManagerEx getHwApiCacheManagerEx() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwApiCacheManagerEx();
        }
        return null;
    }

    public static IHwCommBoosterServiceManager getHwCommBoosterServiceManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwCommBoosterServiceManager();
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
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwCarrierConfigPolicy();
        }
        return null;
    }

    public static IHwBehaviorCollectManager getHwBehaviorCollectManager() {
        return HwFrameworkSecurityPartsFactory.getInstance().getInnerHwBehaviorCollectManager();
    }

    public static IHwGetCfgFileConfig getHwCfgFileConfig() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwCfgFileConfig();
        }
        return null;
    }

    public static HwOptPackageParser getHwOptPackageParser() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwOptPackageParser();
        }
        return null;
    }

    public static IHwSQLiteDatabase getHwSQLiteDatabase() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwSQLiteDatabase();
        }
        return null;
    }

    public static IHwSQLiteSession getHwSQLiteSession() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwSQLiteSession();
        }
        return null;
    }

    public static IHwDatabaseErrorReporter getHwDatabaseErrorReporter() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwDatabaseErrorReporter();
        }
        return null;
    }

    public static IIndexSearchManager getIndexSearchManager() {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getIndexSearchManager();
        }
        return null;
    }

    public static IHwAutofillHelper getHwAutofillHelper() {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwAutofillHelper();
        }
        return null;
    }

    public static IAwareBitmapCacher getHwIAwareBitmapCacher() {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwIAwareBitmapCacher();
        }
        return null;
    }

    public static IHwNotificationEx getHwNotificationEx(Context context) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwNotificationEx(context);
        }
        return null;
    }

    public static IHwDisplayManagerGlobalEx getHwDisplayManagerGlobalEx(IHwDisplayManagerGlobalInner dmg) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwDisplayManagerGlobalEx(dmg);
        }
        return null;
    }

    public static IScrollerBoostMgr getIScrollerBoostMgr() {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getIScrollerBoostMgr();
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
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwZygoteInit();
        }
        return null;
    }

    public static IHwLoadedApk getHwLoadedApk() {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwLoadedApk();
        }
        return null;
    }

    public static IHwExtDisplaySizeUtil getHwExtDisplaySizeUtil() {
        Factory factory = getImplObject();
        if (factory != null) {
            return factory.getHwExtDisplaySizeUtil();
        }
        return null;
    }

    public static IHwDisplaySideRegion getHwDisplaySideRegion(Rect rect) {
        Factory factory = getImplObject();
        if (factory != null) {
            return factory.getHwDisplaySideRegion(rect);
        }
        return null;
    }

    public static IHwSuggestedLocaleAdapterEx getHwSuggestedLocaleAdapterEx(IHwSuggestedLocaleAdapterInner inner, Context context, boolean isShowAddedHeaders) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwSuggestedLocaleAdapterEx(inner, context, isShowAddedHeaders);
        }
        return null;
    }

    public static IHwLocalePickerWithRegionEx getHwLocalePickerWithRegionEx(IHwLocalePickerWithRegionInner hwLocalePickerWithRegionInner) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwLocalePickerWithRegionEx(hwLocalePickerWithRegionInner);
        }
        return null;
    }

    public static IHwLocalePickerEx getHwLocalePickerEx(IHwLocalePickerInner inner, Context context) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwLocalePickerEx(inner, context);
        }
        return null;
    }

    public static IHwLocalePickerEx getHwLocalePickerEx() {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwLocalePickerEx();
        }
        return null;
    }

    public static IHwLocaleStoreEx getHwLocaleStoreEx() {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwLocaleStoreEx();
        }
        return null;
    }

    public static IHwLocaleHelperEx getHwLocaleHelperEx(IHwLocaleStoreInner inner) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwLocaleHelperEx(inner);
        }
        return null;
    }

    public static IHwLocaleHelperEx getHwLocaleHelperEx() {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwLocaleHelperEx();
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
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwRtgSchedImpl();
        }
        return null;
    }

    public static IHwZygoteEx getHwZygoteEx() {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwZygoteEx();
        }
        return null;
    }

    public static IHwZygoteProcessEx getHwZygoteProcessEx() {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwZygoteProcessEx();
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

    public static IHwViewGroup getHwViewGroup() {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwViewGroup();
        }
        return null;
    }

    public static IHwBlurWindowManager getHwBlurWindowManager() {
        Factory hwBlurWindowManagerObj = getImplObject();
        if (hwBlurWindowManagerObj != null) {
            return hwBlurWindowManagerObj.getHwBlurWindowManager();
        }
        return null;
    }

    public static IHwRio getHwRioImpl() {
        Factory facObj = getImplObject();
        if (facObj != null) {
            return facObj.getHwRio();
        }
        return DefaultHwRio.getDefault();
    }

    public static IHwHarmonyServiceManager getHwHarmonyServiceManager() {
        Factory hwHarmonyServiceManagerObj = getImplObject();
        if (hwHarmonyServiceManagerObj != null) {
            return hwHarmonyServiceManagerObj.getHwHarmonyServiceManager();
        }
        return null;
    }

    public static IHwShadowManager getHwShadowManager(Context context) {
        Factory hwShadowManagerObj;
        if (context == null || (hwShadowManagerObj = getImplObject()) == null) {
            return null;
        }
        return hwShadowManagerObj.getHwShadowManager(context);
    }
}
