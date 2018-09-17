package android.common;

import android.app.Activity;
import android.app.HwKeyguardManager;
import android.app.IHwActivitySplitterImpl;
import android.app.IHwWallpaperInfoStub;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.app.admin.HwDeviceAdminInfoDummy;
import android.app.admin.IHwDeviceAdminInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.IHwPackageParser;
import android.content.res.IHwConfiguration;
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
import android.provider.HwMediaStoreDummy;
import android.provider.IHwMediaStore;
import android.rms.HwSysResource;
import android.rog.IRogManager;
import android.telephony.HwInnerTelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LogException;
import android.view.IHwAdCleaner;
import android.view.IHwNsdImpl;
import android.view.IHwPointEventFilter;
import android.view.IHwView;
import android.view.IHwViewRootImpl;
import android.view.animation.Interpolator;
import android.view.inputmethod.IHwSecImmHelper;
import android.vrsystem.IVRSystemServiceManager;
import android.widget.HwWidgetManager;
import com.android.internal.app.HwLocalePickerManager;
import com.android.internal.os.HwPowerProfileManagerDummy;
import com.android.internal.os.IHwPowerProfileManager;
import com.android.internal.view.IInputMethodManager;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.hwperformance.HwPerformanceDummy;
import huawei.android.animation.HwStateListAnimator;
import huawei.android.animation.HwStateListAnimatorDummy;
import huawei.power.AudioEffectLowPowerTask;

public class HwFrameworkFactory {
    private static final String TAG = "HwFrameworkFactory";
    private static final Object mLock = null;
    private static Factory obj;

    public interface Factory {
        Interpolator createHwInterpolator(String str, Context context, AttributeSet attributeSet);

        Interpolator createHwInterpolator(String str, Resources resources, Theme theme, AttributeSet attributeSet);

        IFreezeScreenApplicationMonitor getAppFreezeScreenMonitor();

        AudioEffectLowPowerTask getAudioEffectLowPowerTaskImpl(Context context);

        IHwCoverManager getCoverManager();

        Intent getHuaweiChooserIntentImpl();

        IHwDeviceManager getHuaweiDevicePolicyManagerImpl();

        String getHuaweiResolverActivityImpl(Context context);

        IHwWallpaperManager getHuaweiWallpaperManager();

        IHwActivitySplitterImpl getHwActivitySplitterImpl(Activity activity, boolean z);

        IHwAdCleaner getHwAdCleaner();

        HwAnimationManager getHwAnimationManager();

        HwPackageManager getHwApplicationPackageManager();

        IHwConfiguration getHwConfiguration();

        IHwDeviceAdminInfo getHwDeviceAdminInfo(Context context, ActivityInfo activityInfo);

        HwDrmManager getHwDrmManager();

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

        IHwPackageParser getHwPackageParser();

        HwPerformance getHwPerformance();

        IHwPointEventFilter getHwPointEventFilter();

        IHwPointEventFilter getHwPointEventFilter(String str);

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

        IRogManager getRogManager();

        IHwSecImmHelper getSecImmHelper(IInputMethodManager iInputMethodManager);

        IVRSystemServiceManager getVRSystemServiceManager();
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.common.HwFrameworkFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.common.HwFrameworkFactory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.common.HwFrameworkFactory.<clinit>():void");
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

    public static HwInnerTelephonyManager getHwInnerTelephonyManager() {
        return getImplObject().getHwInnerTelephonyManager();
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

    public static IHwPointEventFilter getHwPointEventFilter(String pkgName) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPointEventFilter(pkgName);
        }
        return null;
    }

    public static IHwPointEventFilter getHwPointEventFilter() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPointEventFilter();
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

    public static IRogManager getRogManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getRogManager();
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
}
