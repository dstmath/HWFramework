package huawei.android.common;

import android.app.Activity;
import android.app.HwActivitySplitterImpl;
import android.app.HwKeyguardManager;
import android.app.HwNotificationEx;
import android.app.IHwNotificationEx;
import android.app.admin.IHwDeviceAdminInfo;
import android.aps.HwApsManager;
import android.aps.IApsManager;
import android.common.HwAnimationManager;
import android.common.HwFlogManager;
import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.common.HwMediaScannerManager;
import android.common.HwPartPowerOfficeFactory;
import android.common.HwSettingsManager;
import android.common.IHwLoadedApk;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.sqlite.HwSQLiteDatabase;
import android.database.sqlite.HwSQLiteSession;
import android.database.sqlite.IHwSQLiteDatabase;
import android.database.sqlite.IHwSQLiteSession;
import android.encrypt.HwPasswordUtil;
import android.encrypt.PasswordUtil;
import android.graphics.AwareBitmapCacher;
import android.graphics.IAwareBitmapCacher;
import android.graphics.Rect;
import android.hdm.HwDeviceManager;
import android.hwclipboarddelayread.IHwClipboardReadDelayRegisterFactory;
import android.hwclipboarddelayread.IHwClipboardReadDelayerFactory;
import android.hwgallerycache.IHwGalleryCacheManagerFactory;
import android.iawareperf.RtgSched;
import android.location.DefaultHwInnerLocationManager;
import android.location.IHwInnerLocationManager;
import android.magicwin.HwMagicWindow;
import android.magicwin.HwMagicWindowImpl;
import android.media.HwMediaScannerImpl;
import android.net.HwInnerConnectivityManager;
import android.net.HwInnerConnectivityManagerImpl;
import android.net.HwNetworkPolicyManager;
import android.net.INetworkPolicyManager;
import android.net.NetworkPolicyManager;
import android.net.booster.HwCommBoosterServiceManager;
import android.net.booster.HwDataServiceQoe;
import android.net.booster.HwDataServiceQoeImpl;
import android.net.booster.IHwCommBoosterServiceManager;
import android.net.wifi.HwInnerNetworkManager;
import android.net.wifi.HwInnerNetworkManagerImpl;
import android.net.wifi.HwInnerWifiManager;
import android.net.wifi.HwInnerWifiManagerImpl;
import android.net.wifi.p2p.HwInnerWifiP2pManager;
import android.net.wifi.p2p.HwInnerWifiP2pManagerImpl;
import android.os.BlockMonitor;
import android.os.FreezeScreenApplicationMonitor;
import android.os.HwZygoteProcessEx;
import android.os.IBlockMonitor;
import android.os.IFreezeScreenApplicationMonitor;
import android.pc.HwPCManager;
import android.perf.HwOptPackageParser;
import android.perf.HwOptPackageParserImpl;
import android.provider.IHwMediaStore;
import android.rms.HwAppInnerBoostImpl;
import android.rms.HwSysResImpl;
import android.rms.HwSysResource;
import android.scrollerboostmanager.IScrollerBoostMgr;
import android.util.AttributeSet;
import android.util.HwLogExceptionInner;
import android.util.Log;
import android.util.LogException;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.autofill.IHwAutofillHelper;
import android.vrsystem.IVRSystemServiceManager;
import android.vrsystem.VRSystemServiceManager;
import android.widget.FrameLayout;
import android.widget.HwWidgetManager;
import android.zrhung.IAppEyeUiProbe;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungImpl;
import android.zrhung.appeye.AppEyeUiProbe;
import com.android.ims.HwImsManagerInner;
import com.android.internal.app.HwLocalePickerManager;
import com.android.internal.app.IHwLocalePickerInner;
import com.android.internal.app.IHwLocalePickerWithRegionInner;
import com.android.internal.app.IHwLocaleStoreInner;
import com.android.internal.app.IHwSuggestedLocaleAdapterInner;
import com.android.internal.os.HwZygoteEx;
import com.android.internal.os.HwZygoteInit;
import com.android.internal.os.HwZygoteInitImpl;
import com.android.internal.os.IHwPowerProfileManager;
import com.android.internal.view.IInputMethodManager;
import com.huawei.android.app.IHwLocaleHelperEx;
import com.huawei.android.app.IHwLocalePickerEx;
import com.huawei.android.app.IHwLocalePickerWithRegionEx;
import com.huawei.android.app.IHwLocaleStoreEx;
import com.huawei.android.app.IHwSuggestedLocaleAdapterEx;
import com.huawei.android.hardware.display.HwDisplayManagerGlobalEx;
import com.huawei.android.hardware.display.IHwDisplayManagerGlobalEx;
import com.huawei.android.hardware.display.IHwDisplayManagerGlobalInner;
import com.huawei.android.hwforcedark.IHwForceDarkManager;
import com.huawei.android.view.HwDisplaySideRegion;
import com.huawei.android.view.HwExtDisplaySizeUtil;
import com.huawei.android.view.IHwDisplaySideRegion;
import com.huawei.android.view.IHwExtDisplaySizeUtil;
import com.huawei.forcerotation.HwForceRotationManager;
import com.huawei.forcerotation.IForceRotationManager;
import com.huawei.hwanimation.AnimUtil;
import com.huawei.hwaps.HwApsImpl;
import com.huawei.hwforcedark.HwForceDarkManager;
import com.huawei.indexsearch.IIndexSearchManager;
import com.huawei.indexsearch.IndexSearchManager;
import com.huawei.sidetouch.HwSideStatusManager;
import com.huawei.sidetouch.IHwSideStatusManager;
import huawei.android.app.HwApiCacheMangerEx;
import huawei.android.app.HwKeyguardManagerImpl;
import huawei.android.app.HwLoadedApk;
import huawei.android.app.admin.HwDeviceAdminInfo;
import huawei.android.app.admin.HwDeviceManagerImpl;
import huawei.android.hwanimation.HwAnimationManagerImpl;
import huawei.android.hwclipboarddelayread.HwClipboardReadDelayRegisterFactoryImpl;
import huawei.android.hwclipboarddelayread.HwClipboardReadDelayerFactoryImpl;
import huawei.android.hwgallerycache.HwGalleryCacheManagerFactoryImpl;
import huawei.android.hwnotification.HwNotificationResourceImpl;
import huawei.android.hwperformance.HwPerformance;
import huawei.android.hwperformance.HwPerformanceImpl;
import huawei.android.hwscrollerboost.HwScrollerBoostMgrImpl;
import huawei.android.provider.HwMediaStoreImpl;
import huawei.android.provider.HwSettingsManagerImpl;
import huawei.android.utils.HwFlogManagerImpl;
import huawei.android.view.autofill.HwAutofillHelper;
import huawei.android.view.inputmethod.HwSecImmHelper;
import huawei.android.widget.HwWidgetManagerImpl;
import huawei.com.android.internal.app.HwLocaleHelperEx;
import huawei.com.android.internal.app.HwLocalePickerEx;
import huawei.com.android.internal.app.HwLocalePickerManagerImpl;
import huawei.com.android.internal.app.HwLocalePickerWithRegionEx;
import huawei.com.android.internal.app.HwLocaleStoreEx;
import huawei.com.android.internal.app.HwSuggestedLocaleAdapterEx;
import huawei.com.android.internal.os.HwFLClassLoader;
import huawei.com.android.internal.os.HwFeatureLoader;
import huawei.com.android.internal.os.HwPowerProfileManagerImpl;
import huawei.com.android.internal.policy.PressGestureDetector;
import huawei.cust.HwCarrierConfigPolicy;
import huawei.cust.HwGetCfgFile;
import huawei.cust.IHwCarrierConfigPolicy;
import huawei.cust.IHwGetCfgFileConfig;
import huawei.hiview.HiTrace;
import huawei.hiview.HiTraceHandler;
import huawei.hiview.HiTraceHandlerImpl;
import huawei.hiview.HiTraceId;
import huawei.hiview.HiTraceIdImpl;
import huawei.hiview.HiTraceImpl;

public class HwFrameworkFactoryImpl implements HwFrameworkFactory.Factory {
    public static final String ACTION_HW_CHOOSER = "com.huawei.intent.action.hwCHOOSER";
    private static final String TAG = "HwFrameworkFactoryImpl";

    public HwFrameworkFactory.IHwNetworkPolicyManager getHwNetworkPolicyManager() {
        return new HwNetworkPolicyManagerImpl();
    }

    public static class HwNetworkPolicyManagerImpl implements HwFrameworkFactory.IHwNetworkPolicyManager {
        public NetworkPolicyManager getInstance(Context ctx, INetworkPolicyManager service) {
            return new HwNetworkPolicyManager(ctx, service);
        }
    }

    public LogException getLogException() {
        return HwLogExceptionInner.getInstance();
    }

    public PasswordUtil getPasswordUtil() {
        return HwPasswordUtil.getInstance();
    }

    public HwLocalePickerManager getHwLocalePickerManager() {
        return HwLocalePickerManagerImpl.getDefault();
    }

    public HwPCManager getHwPCManager() {
        return HwPartPowerOfficeFactory.loadFactory().getHwPCManager();
    }

    public HwMagicWindow getHwMagicWindow() {
        return HwMagicWindowImpl.getDefault();
    }

    public HwKeyguardManager getHwKeyguardManager() {
        return HwKeyguardManagerImpl.getDefault();
    }

    public HwInnerWifiManager getHwInnerWifiManager() {
        return HwInnerWifiManagerImpl.getDefault();
    }

    public HwInnerWifiP2pManager getHwInnerWifiP2pManager() {
        return HwInnerWifiP2pManagerImpl.getDefault();
    }

    public HwWidgetManager getHwWidgetManager() {
        return HwWidgetManagerImpl.getDefault();
    }

    public IHwMediaStore getHwMediaStoreManager() {
        return HwMediaStoreImpl.getDefault();
    }

    public HwMediaScannerManager getHwMediaScannerManager() {
        return HwMediaScannerImpl.getDefault();
    }

    public Interpolator createHwInterpolator(String name, Context c, AttributeSet attrs) {
        return new AnimUtil().getCubicBezierInterpolator(name, c, attrs);
    }

    public Interpolator createHwInterpolator(String name, Resources res, Resources.Theme theme, AttributeSet attrs) {
        return new AnimUtil().getCubicBezierInterpolator(name, res, theme, attrs);
    }

    public Animation createHwClipRectAnimation(Context context, AttributeSet attrs) {
        return new AnimUtil().getHwClipRectAnimation(context, attrs);
    }

    public HwAnimationManager getHwAnimationManager() {
        return HwAnimationManagerImpl.getDefault();
    }

    public HwSettingsManager getHwSettingsManager() {
        return HwSettingsManagerImpl.getDefault();
    }

    public IHwGalleryCacheManagerFactory getHwGalleryCacheManagerFactory() {
        return new HwGalleryCacheManagerFactoryImpl();
    }

    public IHwClipboardReadDelayerFactory getHwClipboardReadDelayerFactory() {
        return new HwClipboardReadDelayerFactoryImpl();
    }

    public IHwClipboardReadDelayRegisterFactory getHwClipboardReadDelayRegisterFactory() {
        return new HwClipboardReadDelayRegisterFactoryImpl();
    }

    public HwFlogManager getHwFlogManager() {
        return HwFlogManagerImpl.getDefault();
    }

    public Intent getHuaweiChooserIntentImpl() {
        return new Intent(ACTION_HW_CHOOSER);
    }

    public String getHuaweiResolverActivityImpl(Context context) {
        return context.getResources().getString(33685937);
    }

    public PressGestureDetector getPressGestureDetector(Context context, FrameLayout docerView, Context contextActivity) {
        return new PressGestureDetector(context, docerView, contextActivity);
    }

    public IHwPowerProfileManager getHwPowerProfileManager() {
        return HwPowerProfileManagerImpl.getDefault();
    }

    public HwApsImpl getHwApsImpl() {
        return HwApsImpl.getDefault();
    }

    public HwAppInnerBoostImpl getHwAppInnerBoostImpl() {
        return HwAppInnerBoostImpl.getDefault();
    }

    public HwInnerConnectivityManager getHwInnerConnectivityManager() {
        return HwInnerConnectivityManagerImpl.getDefault();
    }

    public HwSysResource getHwResource(int resourceType) {
        return HwSysResImpl.getResource(resourceType);
    }

    public IZrHung getZrHung(String wpName) {
        return ZrHungImpl.getZrHung(wpName);
    }

    public IAppEyeUiProbe getAppEyeUiProbe() {
        try {
            return AppEyeUiProbe.get();
        } catch (Throwable e) {
            Log.e(TAG, "Get AppEyeUiProbe encounter error", e);
            return null;
        }
    }

    public HwInnerNetworkManager getHwInnerNetworkManager() {
        return HwInnerNetworkManagerImpl.getDefault();
    }

    public HwSecImmHelper getSecImmHelper(IInputMethodManager service) {
        return new HwSecImmHelper(service);
    }

    public HwDeviceManager.IHwDeviceManager getHuaweiDevicePolicyManagerImpl() {
        return new HwDeviceManagerImpl();
    }

    public IHwInnerLocationManager getHwInnerLocationManager() {
        return DefaultHwInnerLocationManager.getDefault();
    }

    public HwPerformance getHwPerformance() {
        return HwPerformanceImpl.getDefault();
    }

    public HwNotificationResourceImpl getHwNotificationResource() {
        return new HwNotificationResourceImpl();
    }

    public IFreezeScreenApplicationMonitor getAppFreezeScreenMonitor() {
        return FreezeScreenApplicationMonitor.getInstance();
    }

    public IVRSystemServiceManager getVRSystemServiceManager() {
        return VRSystemServiceManager.getInstance();
    }

    public IHwDeviceAdminInfo getHwDeviceAdminInfo(Context context, ActivityInfo activityInfo) {
        return new HwDeviceAdminInfo(context, activityInfo);
    }

    public HwFrameworkMonitor getHwFrameworkMonitor() {
        return HwFrameworkMonitorImpl.getInstance();
    }

    public HwActivitySplitterImpl getHwActivitySplitterImpl(Activity a, boolean isBase) {
        return HwActivitySplitterImpl.getDefault(a, isBase);
    }

    public IApsManager getApsManager() {
        return HwApsManager.getDefault();
    }

    public HwFrameworkFactory.IHwFeatureLoader getHwFeatureLoader() {
        return new HwFeatureLoader();
    }

    public HwApiCacheMangerEx getHwApiCacheManagerEx() {
        return HwApiCacheMangerEx.getDefault();
    }

    public ClassLoader getHwFLClassLoaderParent(String dexPath) {
        return HwFLClassLoader.getHwFLClassLoaderParent(dexPath);
    }

    public void updateImsServiceConfig(Context context, int subId, boolean force) {
        HwImsManagerInner.updateImsServiceConfig(context, subId, force);
    }

    public IForceRotationManager getForceRotationManager() {
        return HwForceRotationManager.getDefault();
    }

    public IHwCommBoosterServiceManager getHwCommBoosterServiceManager() {
        return HwCommBoosterServiceManager.getInstance();
    }

    public HwDataServiceQoe getHwDataServiceQoe() {
        return HwDataServiceQoeImpl.getInstance();
    }

    public IHwCarrierConfigPolicy getHwCarrierConfigPolicy() {
        return HwCarrierConfigPolicy.getDefault();
    }

    public IHwGetCfgFileConfig getHwCfgFileConfig() {
        return HwGetCfgFile.getDefault();
    }

    public HwOptPackageParser getHwOptPackageParser() {
        return new HwOptPackageParserImpl();
    }

    public IHwSQLiteDatabase getHwSQLiteDatabase() {
        return HwSQLiteDatabase.getInstance();
    }

    public IHwSQLiteSession getHwSQLiteSession() {
        return HwSQLiteSession.getInstance();
    }

    public IIndexSearchManager getIndexSearchManager() {
        return IndexSearchManager.getInstance();
    }

    public IHwAutofillHelper getHwAutofillHelper() {
        return new HwAutofillHelper();
    }

    public IHwLocalePickerEx getHwLocalePickerEx(IHwLocalePickerInner inner, Context context) {
        return new HwLocalePickerEx(inner, context);
    }

    public IHwLocalePickerEx getHwLocalePickerEx() {
        return new HwLocalePickerEx();
    }

    public IHwSuggestedLocaleAdapterEx getHwSuggestedLocaleAdapterEx(IHwSuggestedLocaleAdapterInner inner, Context context, boolean isShowAddedHeaders) {
        return new HwSuggestedLocaleAdapterEx(inner, context, isShowAddedHeaders);
    }

    public IHwLocalePickerWithRegionEx getHwLocalePickerWithRegionEx(IHwLocalePickerWithRegionInner lpw) {
        return new HwLocalePickerWithRegionEx(lpw);
    }

    public IHwLocaleStoreEx getHwLocaleStoreEx() {
        return new HwLocaleStoreEx();
    }

    public IHwLocaleHelperEx getHwLocaleHelperEx(IHwLocaleStoreInner inner) {
        return new HwLocaleHelperEx(inner);
    }

    public IHwLocaleHelperEx getHwLocaleHelperEx() {
        return new HwLocaleHelperEx();
    }

    public IAwareBitmapCacher getHwIAwareBitmapCacher() {
        return AwareBitmapCacher.getDefault();
    }

    public IHwDisplayManagerGlobalEx getHwDisplayManagerGlobalEx(IHwDisplayManagerGlobalInner dmg) {
        return new HwDisplayManagerGlobalEx(dmg);
    }

    public IScrollerBoostMgr getIScrollerBoostMgr() {
        return HwScrollerBoostMgrImpl.getDefault();
    }

    public IHwNotificationEx getHwNotificationEx(Context mContext) {
        return new HwNotificationEx(mContext);
    }

    public IBlockMonitor getBlockMonitor() {
        return BlockMonitor.getInstance();
    }

    public HwZygoteInit getHwZygoteInit() {
        return HwZygoteInitImpl.getDefault();
    }

    public IHwLoadedApk getHwLoadedApk() {
        return HwLoadedApk.getDefault();
    }

    public IHwExtDisplaySizeUtil getHwExtDisplaySizeUtil() {
        return HwExtDisplaySizeUtil.getInstance();
    }

    public IHwDisplaySideRegion getHwDisplaySideRegion(Rect rect) {
        return new HwDisplaySideRegion(rect);
    }

    public RtgSched getHwRtgSchedImpl() {
        return RtgSched.getInstance();
    }

    public HwZygoteEx getHwZygoteEx() {
        return HwZygoteEx.getDefault();
    }

    public HwZygoteProcessEx getHwZygoteProcessEx() {
        return HwZygoteProcessEx.getDefault();
    }

    public IHwSideStatusManager getHwSideStatusManager(Context context) {
        return HwSideStatusManager.getInstance(context);
    }

    public IHwForceDarkManager getHwForceDarkManager() {
        return HwForceDarkManager.getDefault();
    }

    public HiTraceId getHiTraceId() {
        return new HiTraceIdImpl();
    }

    public HiTraceId getHiTraceId(byte[] idArray) {
        return new HiTraceIdImpl(idArray);
    }

    public HiTrace getHiTrace() {
        return HiTraceImpl.getInstance();
    }

    public HiTraceHandler getHiTraceHandler() {
        return HiTraceHandlerImpl.getInstance();
    }
}
