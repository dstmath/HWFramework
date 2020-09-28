package android.app;

import android.accounts.AccountManager;
import android.accounts.IAccountManager;
import android.app.IAlarmManager;
import android.app.IWallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.app.admin.IDevicePolicyManager;
import android.app.contentsuggestions.ContentSuggestionsManager;
import android.app.contentsuggestions.IContentSuggestionsManager;
import android.app.job.IJobScheduler;
import android.app.job.JobScheduler;
import android.app.prediction.AppPredictionManager;
import android.app.role.RoleControllerManager;
import android.app.role.RoleManager;
import android.app.slice.SliceManager;
import android.app.timedetector.TimeDetector;
import android.app.timezone.RulesManager;
import android.app.trust.TrustManager;
import android.app.usage.IStorageStatsManager;
import android.app.usage.IUsageStatsManager;
import android.app.usage.NetworkStatsManager;
import android.app.usage.StorageStatsManager;
import android.app.usage.UsageStatsManager;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothManager;
import android.common.HwFrameworkFactory;
import android.companion.CompanionDeviceManager;
import android.companion.ICompanionDeviceManager;
import android.content.ClipboardManager;
import android.content.ContentCaptureOptions;
import android.content.Context;
import android.content.IRestrictionsManager;
import android.content.RestrictionsManager;
import android.content.om.IOverlayManager;
import android.content.om.OverlayManager;
import android.content.pm.CrossProfileApps;
import android.content.pm.ICrossProfileApps;
import android.content.pm.IShortcutService;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutManager;
import android.content.res.Resources;
import android.content.rollback.IRollbackManager;
import android.content.rollback.RollbackManager;
import android.debug.AdbManager;
import android.debug.IAdbManager;
import android.hardware.ConsumerIrManager;
import android.hardware.ISerialManager;
import android.hardware.SensorManager;
import android.hardware.SensorPrivacyManager;
import android.hardware.SerialManager;
import android.hardware.SystemSensorManager;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.IBiometricService;
import android.hardware.camera2.CameraManager;
import android.hardware.display.ColorDisplayManager;
import android.hardware.display.DisplayManager;
import android.hardware.face.FaceManager;
import android.hardware.face.IFaceService;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.IFingerprintService;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.IHdmiControlService;
import android.hardware.input.InputManager;
import android.hardware.iris.IIrisService;
import android.hardware.iris.IrisManager;
import android.hardware.location.ContextHubManager;
import android.hardware.radio.RadioManager;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.UsbManager;
import android.location.CountryDetector;
import android.location.ICountryDetector;
import android.location.ILocationManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.media.midi.IMidiManager;
import android.media.midi.MidiManager;
import android.media.projection.MediaProjectionManager;
import android.media.session.MediaSessionManager;
import android.media.soundtrigger.SoundTriggerManager;
import android.media.tv.ITvInputManager;
import android.media.tv.TvInputManager;
import android.net.ConnectivityManager;
import android.net.ConnectivityThread;
import android.net.EthernetManager;
import android.net.IConnectivityManager;
import android.net.IEthernetManager;
import android.net.IIpSecService;
import android.net.INetworkPolicyManager;
import android.net.ITestNetworkManager;
import android.net.IpSecManager;
import android.net.NetworkPolicyManager;
import android.net.NetworkScoreManager;
import android.net.NetworkWatchlistManager;
import android.net.TestNetworkManager;
import android.net.lowpan.ILowpanManager;
import android.net.lowpan.LowpanManager;
import android.net.nsd.INsdManager;
import android.net.nsd.NsdManager;
import android.net.wifi.IWifiManager;
import android.net.wifi.IWifiScanner;
import android.net.wifi.RttManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiScanner;
import android.net.wifi.aware.IWifiAwareManager;
import android.net.wifi.aware.WifiAwareManager;
import android.net.wifi.p2p.IWifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.rtt.IWifiRttManager;
import android.net.wifi.rtt.WifiRttManager;
import android.nfc.NfcManager;
import android.os.BatteryManager;
import android.os.BatteryStats;
import android.os.BugreportManager;
import android.os.DeviceIdleManager;
import android.os.DropBoxManager;
import android.os.HardwarePropertiesManager;
import android.os.IBatteryPropertiesRegistrar;
import android.os.IBinder;
import android.os.IDeviceIdleController;
import android.os.IDumpstate;
import android.os.IHardwarePropertiesManager;
import android.os.IPowerManager;
import android.os.IRecoverySystem;
import android.os.ISystemUpdateManager;
import android.os.IUserManager;
import android.os.IncidentManager;
import android.os.PowerManager;
import android.os.Process;
import android.os.RecoverySystem;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemUpdateManager;
import android.os.SystemVibrator;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.os.health.SystemHealthManager;
import android.os.image.DynamicSystemManager;
import android.os.image.IDynamicSystemService;
import android.os.storage.StorageManager;
import android.pc.HwPCManager;
import android.permission.PermissionControllerManager;
import android.permission.PermissionManager;
import android.print.IPrintManager;
import android.print.PrintManager;
import android.service.oemlock.IOemLockService;
import android.service.oemlock.OemLockManager;
import android.service.persistentdata.IPersistentDataBlockService;
import android.service.persistentdata.PersistentDataBlockManager;
import android.service.vr.IVrManager;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.euicc.EuiccCardManager;
import android.telephony.euicc.EuiccManager;
import android.telephony.ims.RcsManager;
import android.util.ArrayMap;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.WindowManagerImpl;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.CaptioningManager;
import android.view.autofill.AutofillManager;
import android.view.autofill.IAutoFillManager;
import android.view.contentcapture.ContentCaptureManager;
import android.view.contentcapture.IContentCaptureManager;
import android.view.inputmethod.InputMethodManager;
import android.view.textclassifier.TextClassificationManager;
import android.view.textservice.TextServicesManager;
import android.vrsystem.IVRSystemServiceManager;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.internal.app.ISoundTriggerService;
import com.android.internal.appwidget.IAppWidgetService;
import com.android.internal.net.INetworkWatchlistManager;
import com.android.internal.os.IDropBoxManagerService;
import com.android.internal.policy.HwPolicyFactory;
import java.util.Map;

/* access modifiers changed from: package-private */
public final class SystemServiceRegistry {
    private static final Map<String, ServiceFetcher<?>> SYSTEM_SERVICE_FETCHERS = new ArrayMap();
    private static final Map<Class<?>, String> SYSTEM_SERVICE_NAMES = new ArrayMap();
    private static final String TAG = "SystemServiceRegistry";
    private static int sServiceCacheSize;

    /* access modifiers changed from: package-private */
    public interface ServiceFetcher<T> {
        T getService(ContextImpl contextImpl);
    }

    static /* synthetic */ int access$008() {
        int i = sServiceCacheSize;
        sServiceCacheSize = i + 1;
        return i;
    }

    static {
        registerService(Context.ACCESSIBILITY_SERVICE, AccessibilityManager.class, new CachedServiceFetcher<AccessibilityManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass1 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public AccessibilityManager createService(ContextImpl ctx) {
                return AccessibilityManager.getInstance(ctx);
            }
        });
        registerService(Context.CAPTIONING_SERVICE, CaptioningManager.class, new CachedServiceFetcher<CaptioningManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass2 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public CaptioningManager createService(ContextImpl ctx) {
                return new CaptioningManager(ctx);
            }
        });
        registerService("account", AccountManager.class, new CachedServiceFetcher<AccountManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass3 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public AccountManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new AccountManager(ctx, IAccountManager.Stub.asInterface(ServiceManager.getServiceOrThrow("account")));
            }
        });
        registerService(Context.ACTIVITY_SERVICE, ActivityManager.class, new CachedServiceFetcher<ActivityManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass4 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public ActivityManager createService(ContextImpl ctx) {
                return new ActivityManager(ctx.getOuterContext(), ctx.mMainThread.getHandler());
            }
        });
        registerService(Context.ACTIVITY_TASK_SERVICE, ActivityTaskManager.class, new CachedServiceFetcher<ActivityTaskManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass5 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public ActivityTaskManager createService(ContextImpl ctx) {
                return new ActivityTaskManager(ctx.getOuterContext(), ctx.mMainThread.getHandler());
            }
        });
        registerService(Context.URI_GRANTS_SERVICE, UriGrantsManager.class, new CachedServiceFetcher<UriGrantsManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass6 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public UriGrantsManager createService(ContextImpl ctx) {
                return new UriGrantsManager(ctx.getOuterContext(), ctx.mMainThread.getHandler());
            }
        });
        registerService("alarm", AlarmManager.class, new CachedServiceFetcher<AlarmManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass7 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public AlarmManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new AlarmManager(IAlarmManager.Stub.asInterface(ServiceManager.getServiceOrThrow("alarm")), ctx);
            }
        });
        registerService("audio", AudioManager.class, new CachedServiceFetcher<AudioManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass8 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public AudioManager createService(ContextImpl ctx) {
                return new AudioManager(ctx);
            }
        });
        registerService(Context.MEDIA_ROUTER_SERVICE, MediaRouter.class, new CachedServiceFetcher<MediaRouter>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass9 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public MediaRouter createService(ContextImpl ctx) {
                return new MediaRouter(ctx);
            }
        });
        registerService("bluetooth", BluetoothManager.class, new CachedServiceFetcher<BluetoothManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass10 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public BluetoothManager createService(ContextImpl ctx) {
                return new BluetoothManager(ctx);
            }
        });
        registerService(IVRSystemServiceManager.VR_MANAGER, IVRSystemServiceManager.class, new CachedServiceFetcher<IVRSystemServiceManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass11 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public IVRSystemServiceManager createService(ContextImpl ctx) {
                return HwFrameworkFactory.getVRSystemServiceManager();
            }
        });
        registerService(Context.HDMI_CONTROL_SERVICE, HdmiControlManager.class, new StaticServiceFetcher<HdmiControlManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass12 */

            @Override // android.app.SystemServiceRegistry.StaticServiceFetcher
            public HdmiControlManager createService() throws ServiceManager.ServiceNotFoundException {
                return new HdmiControlManager(IHdmiControlService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.HDMI_CONTROL_SERVICE)));
            }
        });
        registerService(Context.TEXT_CLASSIFICATION_SERVICE, TextClassificationManager.class, new CachedServiceFetcher<TextClassificationManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass13 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public TextClassificationManager createService(ContextImpl ctx) {
                return new TextClassificationManager(ctx);
            }
        });
        registerService(Context.CLIPBOARD_SERVICE, ClipboardManager.class, new CachedServiceFetcher<ClipboardManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass14 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public ClipboardManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new ClipboardManager(ctx.getOuterContext(), ctx.mMainThread.getHandler());
            }
        });
        SYSTEM_SERVICE_NAMES.put(android.text.ClipboardManager.class, Context.CLIPBOARD_SERVICE);
        registerService("connectivity", ConnectivityManager.class, new StaticApplicationContextServiceFetcher<ConnectivityManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass15 */

            @Override // android.app.SystemServiceRegistry.StaticApplicationContextServiceFetcher
            public ConnectivityManager createService(Context context) throws ServiceManager.ServiceNotFoundException {
                return new ConnectivityManager(context, IConnectivityManager.Stub.asInterface(ServiceManager.getServiceOrThrow("connectivity")));
            }
        });
        registerService(Context.NETD_SERVICE, IBinder.class, new StaticServiceFetcher<IBinder>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass16 */

            @Override // android.app.SystemServiceRegistry.StaticServiceFetcher
            public IBinder createService() throws ServiceManager.ServiceNotFoundException {
                return ServiceManager.getServiceOrThrow(Context.NETD_SERVICE);
            }
        });
        registerService(Context.IPSEC_SERVICE, IpSecManager.class, new CachedServiceFetcher<IpSecManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass17 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public IpSecManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new IpSecManager(ctx, IIpSecService.Stub.asInterface(ServiceManager.getService(Context.IPSEC_SERVICE)));
            }
        });
        registerService(Context.TEST_NETWORK_SERVICE, TestNetworkManager.class, new StaticApplicationContextServiceFetcher<TestNetworkManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass18 */

            @Override // android.app.SystemServiceRegistry.StaticApplicationContextServiceFetcher
            public TestNetworkManager createService(Context context) throws ServiceManager.ServiceNotFoundException {
                try {
                    return new TestNetworkManager(ITestNetworkManager.Stub.asInterface(IConnectivityManager.Stub.asInterface(ServiceManager.getServiceOrThrow("connectivity")).startOrGetTestNetworkService()));
                } catch (RemoteException e) {
                    throw new ServiceManager.ServiceNotFoundException(Context.TEST_NETWORK_SERVICE);
                }
            }
        });
        registerService(Context.COUNTRY_DETECTOR, CountryDetector.class, new StaticServiceFetcher<CountryDetector>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass19 */

            @Override // android.app.SystemServiceRegistry.StaticServiceFetcher
            public CountryDetector createService() throws ServiceManager.ServiceNotFoundException {
                return new CountryDetector(ICountryDetector.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.COUNTRY_DETECTOR)));
            }
        });
        registerService(Context.DEVICE_POLICY_SERVICE, DevicePolicyManager.class, new CachedServiceFetcher<DevicePolicyManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass20 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public DevicePolicyManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new DevicePolicyManager(ctx, IDevicePolicyManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.DEVICE_POLICY_SERVICE)));
            }
        });
        registerService(Context.DOWNLOAD_SERVICE, DownloadManager.class, new CachedServiceFetcher<DownloadManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass21 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public DownloadManager createService(ContextImpl ctx) {
                return new DownloadManager(ctx);
            }
        });
        registerService(Context.BATTERY_SERVICE, BatteryManager.class, new CachedServiceFetcher<BatteryManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass22 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public BatteryManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new BatteryManager(ctx, IBatteryStats.Stub.asInterface(ServiceManager.getServiceOrThrow(BatteryStats.SERVICE_NAME)), IBatteryPropertiesRegistrar.Stub.asInterface(ServiceManager.getServiceOrThrow("batteryproperties")));
            }
        });
        registerService("nfc", NfcManager.class, new CachedServiceFetcher<NfcManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass23 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public NfcManager createService(ContextImpl ctx) {
                return new NfcManager(ctx);
            }
        });
        registerService(Context.DROPBOX_SERVICE, DropBoxManager.class, new CachedServiceFetcher<DropBoxManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass24 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public DropBoxManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new DropBoxManager(ctx, IDropBoxManagerService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.DROPBOX_SERVICE)));
            }
        });
        registerService("input", InputManager.class, new StaticServiceFetcher<InputManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass25 */

            @Override // android.app.SystemServiceRegistry.StaticServiceFetcher
            public InputManager createService() {
                return InputManager.getInstance();
            }
        });
        registerService(Context.DISPLAY_SERVICE, DisplayManager.class, new CachedServiceFetcher<DisplayManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass26 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public DisplayManager createService(ContextImpl ctx) {
                return new DisplayManager(ctx.getOuterContext());
            }
        });
        registerService(Context.COLOR_DISPLAY_SERVICE, ColorDisplayManager.class, new CachedServiceFetcher<ColorDisplayManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass27 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public ColorDisplayManager createService(ContextImpl ctx) {
                return new ColorDisplayManager();
            }
        });
        registerService(Context.INPUT_METHOD_SERVICE, InputMethodManager.class, new ServiceFetcher<InputMethodManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass28 */

            @Override // android.app.SystemServiceRegistry.ServiceFetcher
            public InputMethodManager getService(ContextImpl ctx) {
                return InputMethodManager.forContext(ctx.getOuterContext());
            }
        });
        registerService(Context.TEXT_SERVICES_MANAGER_SERVICE, TextServicesManager.class, new CachedServiceFetcher<TextServicesManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass29 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public TextServicesManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return TextServicesManager.createInstance(ctx);
            }
        });
        registerService(Context.KEYGUARD_SERVICE, KeyguardManager.class, new CachedServiceFetcher<KeyguardManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass30 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public KeyguardManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new KeyguardManager(ctx);
            }
        });
        registerService(Context.LAYOUT_INFLATER_SERVICE, LayoutInflater.class, new CachedServiceFetcher<LayoutInflater>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass31 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public LayoutInflater createService(ContextImpl ctx) {
                return HwPolicyFactory.getHwPhoneLayoutInflater(ctx.getOuterContext());
            }
        });
        registerService("location", LocationManager.class, new CachedServiceFetcher<LocationManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass32 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public LocationManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new LocationManager(ctx, ILocationManager.Stub.asInterface(ServiceManager.getServiceOrThrow("location")));
            }
        });
        registerService(Context.NETWORK_POLICY_SERVICE, NetworkPolicyManager.class, new CachedServiceFetcher<NetworkPolicyManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass33 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public NetworkPolicyManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                HwFrameworkFactory.IHwNetworkPolicyManager inpm = HwFrameworkFactory.getHwNetworkPolicyManager();
                if (inpm != null) {
                    return inpm.getInstance(ctx, INetworkPolicyManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.NETWORK_POLICY_SERVICE)));
                }
                return new NetworkPolicyManager(ctx, INetworkPolicyManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.NETWORK_POLICY_SERVICE)));
            }
        });
        registerService("notification", NotificationManager.class, new CachedServiceFetcher<NotificationManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass34 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public NotificationManager createService(ContextImpl ctx) {
                Context outerContext = ctx.getOuterContext();
                return new NotificationManager(new ContextThemeWrapper(outerContext, Resources.selectSystemTheme(0, outerContext.getApplicationInfo().targetSdkVersion, 16973835, 16973935, 16974126, 16974130)), ctx.mMainThread.getHandler());
            }
        });
        registerService(Context.NSD_SERVICE, NsdManager.class, new CachedServiceFetcher<NsdManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass35 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public NsdManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new NsdManager(ctx.getOuterContext(), INsdManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.NSD_SERVICE)));
            }
        });
        registerService(Context.POWER_SERVICE, PowerManager.class, new CachedServiceFetcher<PowerManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass36 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public PowerManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new PowerManager(ctx.getOuterContext(), IPowerManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.POWER_SERVICE)), ctx.mMainThread.getHandler());
            }
        });
        registerService("recovery", RecoverySystem.class, new CachedServiceFetcher<RecoverySystem>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass37 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public RecoverySystem createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new RecoverySystem(IRecoverySystem.Stub.asInterface(ServiceManager.getServiceOrThrow("recovery")));
            }
        });
        registerService("search", SearchManager.class, new CachedServiceFetcher<SearchManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass38 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public SearchManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new SearchManager(ctx.getOuterContext(), ctx.mMainThread.getHandler());
            }
        });
        registerService(Context.SENSOR_SERVICE, SensorManager.class, new CachedServiceFetcher<SensorManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass39 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public SensorManager createService(ContextImpl ctx) {
                return new SystemSensorManager(ctx.getOuterContext(), ctx.mMainThread.getHandler().getLooper());
            }
        });
        registerService(Context.SENSOR_PRIVACY_SERVICE, SensorPrivacyManager.class, new CachedServiceFetcher<SensorPrivacyManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass40 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public SensorPrivacyManager createService(ContextImpl ctx) {
                return SensorPrivacyManager.getInstance(ctx);
            }
        });
        registerService(Context.STATS_MANAGER, StatsManager.class, new CachedServiceFetcher<StatsManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass41 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public StatsManager createService(ContextImpl ctx) {
                return new StatsManager(ctx.getOuterContext());
            }
        });
        registerService(Context.STATUS_BAR_SERVICE, StatusBarManager.class, new CachedServiceFetcher<StatusBarManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass42 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public StatusBarManager createService(ContextImpl ctx) {
                return new StatusBarManager(ctx.getOuterContext());
            }
        });
        registerService("storage", StorageManager.class, new CachedServiceFetcher<StorageManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass43 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public StorageManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new StorageManager(ctx, ctx.mMainThread.getHandler().getLooper());
            }
        });
        registerService(Context.STORAGE_STATS_SERVICE, StorageStatsManager.class, new CachedServiceFetcher<StorageStatsManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass44 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public StorageStatsManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new StorageStatsManager(ctx, IStorageStatsManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.STORAGE_STATS_SERVICE)));
            }
        });
        registerService(Context.SYSTEM_UPDATE_SERVICE, SystemUpdateManager.class, new CachedServiceFetcher<SystemUpdateManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass45 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public SystemUpdateManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new SystemUpdateManager(ISystemUpdateManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.SYSTEM_UPDATE_SERVICE)));
            }
        });
        registerService("phone", TelephonyManager.class, new CachedServiceFetcher<TelephonyManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass46 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public TelephonyManager createService(ContextImpl ctx) {
                return new TelephonyManager(ctx.getOuterContext());
            }
        });
        registerService(Context.TELEPHONY_SUBSCRIPTION_SERVICE, SubscriptionManager.class, new CachedServiceFetcher<SubscriptionManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass47 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public SubscriptionManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new SubscriptionManager(ctx.getOuterContext());
            }
        });
        registerService(Context.TELEPHONY_RCS_SERVICE, RcsManager.class, new CachedServiceFetcher<RcsManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass48 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public RcsManager createService(ContextImpl ctx) {
                return new RcsManager(ctx.getOuterContext());
            }
        });
        registerService(Context.CARRIER_CONFIG_SERVICE, CarrierConfigManager.class, new CachedServiceFetcher<CarrierConfigManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass49 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public CarrierConfigManager createService(ContextImpl ctx) {
                return new CarrierConfigManager(ctx.getOuterContext());
            }
        });
        registerService(Context.TELECOM_SERVICE, TelecomManager.class, new CachedServiceFetcher<TelecomManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass50 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public TelecomManager createService(ContextImpl ctx) {
                return new TelecomManager(ctx.getOuterContext());
            }
        });
        registerService(Context.EUICC_SERVICE, EuiccManager.class, new CachedServiceFetcher<EuiccManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass51 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public EuiccManager createService(ContextImpl ctx) {
                return new EuiccManager(ctx.getOuterContext());
            }
        });
        registerService(Context.EUICC_CARD_SERVICE, EuiccCardManager.class, new CachedServiceFetcher<EuiccCardManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass52 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public EuiccCardManager createService(ContextImpl ctx) {
                return new EuiccCardManager(ctx.getOuterContext());
            }
        });
        registerService(Context.UI_MODE_SERVICE, UiModeManager.class, new CachedServiceFetcher<UiModeManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass53 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public UiModeManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new UiModeManager();
            }
        });
        registerService(Context.USB_SERVICE, UsbManager.class, new CachedServiceFetcher<UsbManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass54 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public UsbManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new UsbManager(ctx, IUsbManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.USB_SERVICE)));
            }
        });
        registerService("adb", AdbManager.class, new CachedServiceFetcher<AdbManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass55 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public AdbManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new AdbManager(ctx, IAdbManager.Stub.asInterface(ServiceManager.getServiceOrThrow("adb")));
            }
        });
        registerService("serial", SerialManager.class, new CachedServiceFetcher<SerialManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass56 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public SerialManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new SerialManager(ctx, ISerialManager.Stub.asInterface(ServiceManager.getServiceOrThrow("serial")));
            }
        });
        registerService(Context.VIBRATOR_SERVICE, Vibrator.class, new CachedServiceFetcher<Vibrator>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass57 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public Vibrator createService(ContextImpl ctx) {
                return new SystemVibrator(ctx);
            }
        });
        registerService("wallpaper", WallpaperManager.class, new CachedServiceFetcher<WallpaperManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass58 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public WallpaperManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                IBinder b;
                if (ctx.getApplicationInfo().targetSdkVersion >= 28) {
                    b = ServiceManager.getServiceOrThrow("wallpaper");
                } else {
                    b = ServiceManager.getService("wallpaper");
                }
                return new WallpaperManager(IWallpaperManager.Stub.asInterface(b), ctx.getOuterContext(), ctx.mMainThread.getHandler());
            }
        });
        registerService("lowpan", LowpanManager.class, new CachedServiceFetcher<LowpanManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass59 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public LowpanManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new LowpanManager(ctx.getOuterContext(), ILowpanManager.Stub.asInterface(ServiceManager.getServiceOrThrow("lowpan")), ConnectivityThread.getInstanceLooper());
            }
        });
        registerService("wifi", WifiManager.class, new CachedServiceFetcher<WifiManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass60 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public WifiManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new WifiManager(ctx.getOuterContext(), IWifiManager.Stub.asInterface(ServiceManager.getServiceOrThrow("wifi")), ConnectivityThread.getInstanceLooper());
            }
        });
        registerService(Context.WIFI_P2P_SERVICE, WifiP2pManager.class, new StaticServiceFetcher<WifiP2pManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass61 */

            @Override // android.app.SystemServiceRegistry.StaticServiceFetcher
            public WifiP2pManager createService() throws ServiceManager.ServiceNotFoundException {
                return new WifiP2pManager(IWifiP2pManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.WIFI_P2P_SERVICE)));
            }
        });
        registerService(Context.WIFI_AWARE_SERVICE, WifiAwareManager.class, new CachedServiceFetcher<WifiAwareManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass62 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public WifiAwareManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                IWifiAwareManager service = IWifiAwareManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.WIFI_AWARE_SERVICE));
                if (service == null) {
                    return null;
                }
                return new WifiAwareManager(ctx.getOuterContext(), service);
            }
        });
        registerService(Context.WIFI_SCANNING_SERVICE, WifiScanner.class, new CachedServiceFetcher<WifiScanner>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass63 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public WifiScanner createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new WifiScanner(ctx.getOuterContext(), IWifiScanner.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.WIFI_SCANNING_SERVICE)), ConnectivityThread.getInstanceLooper());
            }
        });
        registerService(Context.WIFI_RTT_SERVICE, RttManager.class, new CachedServiceFetcher<RttManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass64 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public RttManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new RttManager(ctx.getOuterContext(), new WifiRttManager(ctx.getOuterContext(), IWifiRttManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.WIFI_RTT_RANGING_SERVICE))));
            }
        });
        registerService(Context.WIFI_RTT_RANGING_SERVICE, WifiRttManager.class, new CachedServiceFetcher<WifiRttManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass65 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public WifiRttManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new WifiRttManager(ctx.getOuterContext(), IWifiRttManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.WIFI_RTT_RANGING_SERVICE)));
            }
        });
        registerService(Context.ETHERNET_SERVICE, EthernetManager.class, new CachedServiceFetcher<EthernetManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass66 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public EthernetManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new EthernetManager(ctx.getOuterContext(), IEthernetManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.ETHERNET_SERVICE)));
            }
        });
        registerService("window", WindowManager.class, new CachedServiceFetcher<WindowManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass67 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public WindowManager createService(ContextImpl ctx) {
                return new WindowManagerImpl(ctx);
            }
        });
        registerService("user", UserManager.class, new CachedServiceFetcher<UserManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass68 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public UserManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new UserManager(ctx, IUserManager.Stub.asInterface(ServiceManager.getServiceOrThrow("user")));
            }
        });
        registerService(Context.APP_OPS_SERVICE, AppOpsManager.class, new CachedServiceFetcher<AppOpsManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass69 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public AppOpsManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new AppOpsManager(ctx, IAppOpsService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.APP_OPS_SERVICE)));
            }
        });
        registerService(Context.CAMERA_SERVICE, CameraManager.class, new CachedServiceFetcher<CameraManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass70 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public CameraManager createService(ContextImpl ctx) {
                return new CameraManager(ctx);
            }
        });
        registerService(Context.LAUNCHER_APPS_SERVICE, LauncherApps.class, new CachedServiceFetcher<LauncherApps>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass71 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public LauncherApps createService(ContextImpl ctx) {
                return new LauncherApps(ctx);
            }
        });
        registerService(Context.RESTRICTIONS_SERVICE, RestrictionsManager.class, new CachedServiceFetcher<RestrictionsManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass72 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public RestrictionsManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new RestrictionsManager(ctx, IRestrictionsManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.RESTRICTIONS_SERVICE)));
            }
        });
        registerService(Context.PRINT_SERVICE, PrintManager.class, new CachedServiceFetcher<PrintManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass73 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public PrintManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                IPrintManager service = null;
                if (ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_PRINTING)) {
                    service = IPrintManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.PRINT_SERVICE));
                }
                return new PrintManager(ctx.getOuterContext(), service, ctx.getUserId(), UserHandle.getAppId(ctx.getApplicationInfo().uid));
            }
        });
        registerService(Context.COMPANION_DEVICE_SERVICE, CompanionDeviceManager.class, new CachedServiceFetcher<CompanionDeviceManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass74 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public CompanionDeviceManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                ICompanionDeviceManager service = null;
                if (ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_COMPANION_DEVICE_SETUP)) {
                    service = ICompanionDeviceManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.COMPANION_DEVICE_SERVICE));
                }
                return new CompanionDeviceManager(service, ctx.getOuterContext());
            }
        });
        registerService(Context.CONSUMER_IR_SERVICE, ConsumerIrManager.class, new CachedServiceFetcher<ConsumerIrManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass75 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public ConsumerIrManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new ConsumerIrManager(ctx);
            }
        });
        registerService(Context.MEDIA_SESSION_SERVICE, MediaSessionManager.class, new CachedServiceFetcher<MediaSessionManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass76 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public MediaSessionManager createService(ContextImpl ctx) {
                return new MediaSessionManager(ctx);
            }
        });
        registerService(Context.TRUST_SERVICE, TrustManager.class, new StaticServiceFetcher<TrustManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass77 */

            @Override // android.app.SystemServiceRegistry.StaticServiceFetcher
            public TrustManager createService() throws ServiceManager.ServiceNotFoundException {
                return new TrustManager(ServiceManager.getServiceOrThrow(Context.TRUST_SERVICE));
            }
        });
        registerService(Context.FINGERPRINT_SERVICE, FingerprintManager.class, new CachedServiceFetcher<FingerprintManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass78 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public FingerprintManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                IBinder binder;
                if (ctx.getApplicationInfo().targetSdkVersion >= 26) {
                    binder = ServiceManager.getServiceOrThrow(Context.FINGERPRINT_SERVICE);
                } else {
                    binder = ServiceManager.getService(Context.FINGERPRINT_SERVICE);
                }
                return new FingerprintManager(ctx.getOuterContext(), IFingerprintService.Stub.asInterface(binder));
            }
        });
        registerService(Context.FACE_SERVICE, FaceManager.class, new CachedServiceFetcher<FaceManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass79 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public FaceManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                IBinder binder;
                if (ctx.getApplicationInfo().targetSdkVersion >= 26) {
                    binder = ServiceManager.getServiceOrThrow(Context.FACE_SERVICE);
                } else {
                    binder = ServiceManager.getService(Context.FACE_SERVICE);
                }
                return new FaceManager(ctx.getOuterContext(), IFaceService.Stub.asInterface(binder));
            }
        });
        registerService(Context.IRIS_SERVICE, IrisManager.class, new CachedServiceFetcher<IrisManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass80 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public IrisManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new IrisManager(ctx.getOuterContext(), IIrisService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.IRIS_SERVICE)));
            }
        });
        registerService(Context.BIOMETRIC_SERVICE, BiometricManager.class, new CachedServiceFetcher<BiometricManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass81 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public BiometricManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                if (!BiometricManager.hasBiometrics(ctx)) {
                    return new BiometricManager(ctx.getOuterContext(), null);
                }
                return new BiometricManager(ctx.getOuterContext(), IBiometricService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.BIOMETRIC_SERVICE)));
            }
        });
        registerService(Context.TV_INPUT_SERVICE, TvInputManager.class, new CachedServiceFetcher<TvInputManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass82 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public TvInputManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new TvInputManager(ITvInputManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.TV_INPUT_SERVICE)), ctx.getUserId());
            }
        });
        registerService(Context.NETWORK_SCORE_SERVICE, NetworkScoreManager.class, new CachedServiceFetcher<NetworkScoreManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass83 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public NetworkScoreManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new NetworkScoreManager(ctx);
            }
        });
        registerService(Context.USAGE_STATS_SERVICE, UsageStatsManager.class, new CachedServiceFetcher<UsageStatsManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass84 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public UsageStatsManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new UsageStatsManager(ctx.getOuterContext(), IUsageStatsManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.USAGE_STATS_SERVICE)));
            }
        });
        registerService(Context.NETWORK_STATS_SERVICE, NetworkStatsManager.class, new CachedServiceFetcher<NetworkStatsManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass85 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public NetworkStatsManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new NetworkStatsManager(ctx.getOuterContext());
            }
        });
        registerService(Context.JOB_SCHEDULER_SERVICE, JobScheduler.class, new StaticServiceFetcher<JobScheduler>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass86 */

            @Override // android.app.SystemServiceRegistry.StaticServiceFetcher
            public JobScheduler createService() throws ServiceManager.ServiceNotFoundException {
                return new JobSchedulerImpl(IJobScheduler.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.JOB_SCHEDULER_SERVICE)));
            }
        });
        registerService(Context.PERSISTENT_DATA_BLOCK_SERVICE, PersistentDataBlockManager.class, new StaticServiceFetcher<PersistentDataBlockManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass87 */

            @Override // android.app.SystemServiceRegistry.StaticServiceFetcher
            public PersistentDataBlockManager createService() throws ServiceManager.ServiceNotFoundException {
                IPersistentDataBlockService persistentDataBlockService = IPersistentDataBlockService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.PERSISTENT_DATA_BLOCK_SERVICE));
                if (persistentDataBlockService != null) {
                    return new PersistentDataBlockManager(persistentDataBlockService);
                }
                return null;
            }
        });
        registerService(Context.OEM_LOCK_SERVICE, OemLockManager.class, new StaticServiceFetcher<OemLockManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass88 */

            @Override // android.app.SystemServiceRegistry.StaticServiceFetcher
            public OemLockManager createService() throws ServiceManager.ServiceNotFoundException {
                IOemLockService oemLockService = IOemLockService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.OEM_LOCK_SERVICE));
                if (oemLockService != null) {
                    return new OemLockManager(oemLockService);
                }
                return null;
            }
        });
        registerService(Context.MEDIA_PROJECTION_SERVICE, MediaProjectionManager.class, new CachedServiceFetcher<MediaProjectionManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass89 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public MediaProjectionManager createService(ContextImpl ctx) {
                return new MediaProjectionManager(ctx);
            }
        });
        registerService(Context.APPWIDGET_SERVICE, AppWidgetManager.class, new CachedServiceFetcher<AppWidgetManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass90 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public AppWidgetManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new AppWidgetManager(ctx, IAppWidgetService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.APPWIDGET_SERVICE)));
            }
        });
        registerService("midi", MidiManager.class, new CachedServiceFetcher<MidiManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass91 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public MidiManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new MidiManager(IMidiManager.Stub.asInterface(ServiceManager.getServiceOrThrow("midi")));
            }
        });
        registerService(Context.RADIO_SERVICE, RadioManager.class, new CachedServiceFetcher<RadioManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass92 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public RadioManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new RadioManager(ctx);
            }
        });
        registerService(Context.HARDWARE_PROPERTIES_SERVICE, HardwarePropertiesManager.class, new CachedServiceFetcher<HardwarePropertiesManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass93 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public HardwarePropertiesManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new HardwarePropertiesManager(ctx, IHardwarePropertiesManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.HARDWARE_PROPERTIES_SERVICE)));
            }
        });
        registerService(Context.SOUND_TRIGGER_SERVICE, SoundTriggerManager.class, new CachedServiceFetcher<SoundTriggerManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass94 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public SoundTriggerManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new SoundTriggerManager(ctx, ISoundTriggerService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.SOUND_TRIGGER_SERVICE)));
            }
        });
        registerService("shortcut", ShortcutManager.class, new CachedServiceFetcher<ShortcutManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass95 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public ShortcutManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new ShortcutManager(ctx, IShortcutService.Stub.asInterface(ServiceManager.getServiceOrThrow("shortcut")));
            }
        });
        registerService(Context.OVERLAY_SERVICE, OverlayManager.class, new CachedServiceFetcher<OverlayManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass96 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public OverlayManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new OverlayManager(ctx, IOverlayManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.OVERLAY_SERVICE)));
            }
        });
        registerService(Context.NETWORK_WATCHLIST_SERVICE, NetworkWatchlistManager.class, new CachedServiceFetcher<NetworkWatchlistManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass97 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public NetworkWatchlistManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new NetworkWatchlistManager(ctx, INetworkWatchlistManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.NETWORK_WATCHLIST_SERVICE)));
            }
        });
        registerService(Context.SYSTEM_HEALTH_SERVICE, SystemHealthManager.class, new CachedServiceFetcher<SystemHealthManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass98 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public SystemHealthManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new SystemHealthManager(IBatteryStats.Stub.asInterface(ServiceManager.getServiceOrThrow(BatteryStats.SERVICE_NAME)));
            }
        });
        registerService(Context.CONTEXTHUB_SERVICE, ContextHubManager.class, new CachedServiceFetcher<ContextHubManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass99 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public ContextHubManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new ContextHubManager(ctx.getOuterContext(), ctx.mMainThread.getHandler().getLooper());
            }
        });
        registerService(Context.INCIDENT_SERVICE, IncidentManager.class, new CachedServiceFetcher<IncidentManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass100 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public IncidentManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new IncidentManager(ctx);
            }
        });
        registerService(Context.BUGREPORT_SERVICE, BugreportManager.class, new CachedServiceFetcher<BugreportManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass101 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public BugreportManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new BugreportManager(ctx.getOuterContext(), IDumpstate.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.BUGREPORT_SERVICE)));
            }
        });
        registerService("autofill", AutofillManager.class, new CachedServiceFetcher<AutofillManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass102 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public AutofillManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new AutofillManager(ctx.getOuterContext(), IAutoFillManager.Stub.asInterface(ServiceManager.getService("autofill")));
            }
        });
        registerService("content_capture", ContentCaptureManager.class, new CachedServiceFetcher<ContentCaptureManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass103 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public ContentCaptureManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                IContentCaptureManager service;
                Context outerContext = ctx.getOuterContext();
                ContentCaptureOptions options = outerContext.getContentCaptureOptions();
                if (options == null) {
                    return null;
                }
                if ((options.lite || options.isWhitelisted(outerContext)) && (service = IContentCaptureManager.Stub.asInterface(ServiceManager.getService("content_capture"))) != null) {
                    return new ContentCaptureManager(outerContext, service, options);
                }
                return null;
            }
        });
        registerService(Context.APP_PREDICTION_SERVICE, AppPredictionManager.class, new CachedServiceFetcher<AppPredictionManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass104 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public AppPredictionManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new AppPredictionManager(ctx);
            }
        });
        registerService(Context.CONTENT_SUGGESTIONS_SERVICE, ContentSuggestionsManager.class, new CachedServiceFetcher<ContentSuggestionsManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass105 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public ContentSuggestionsManager createService(ContextImpl ctx) {
                return new ContentSuggestionsManager(ctx.getUserId(), IContentSuggestionsManager.Stub.asInterface(ServiceManager.getService(Context.CONTENT_SUGGESTIONS_SERVICE)));
            }
        });
        registerService(Context.VR_SERVICE, VrManager.class, new CachedServiceFetcher<VrManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass106 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public VrManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new VrManager(IVrManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.VR_SERVICE)));
            }
        });
        registerService(Context.TIME_ZONE_RULES_MANAGER_SERVICE, RulesManager.class, new CachedServiceFetcher<RulesManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass107 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public RulesManager createService(ContextImpl ctx) {
                return new RulesManager(ctx.getOuterContext());
            }
        });
        registerService(Context.CROSS_PROFILE_APPS_SERVICE, CrossProfileApps.class, new CachedServiceFetcher<CrossProfileApps>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass108 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public CrossProfileApps createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new CrossProfileApps(ctx.getOuterContext(), ICrossProfileApps.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.CROSS_PROFILE_APPS_SERVICE)));
            }
        });
        registerService("slice", SliceManager.class, new CachedServiceFetcher<SliceManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass109 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public SliceManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new SliceManager(ctx.getOuterContext(), ctx.mMainThread.getHandler());
            }
        });
        registerService(Context.DEVICE_IDLE_CONTROLLER, DeviceIdleManager.class, new CachedServiceFetcher<DeviceIdleManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass110 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public DeviceIdleManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new DeviceIdleManager(ctx.getOuterContext(), IDeviceIdleController.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.DEVICE_IDLE_CONTROLLER)));
            }
        });
        registerService(Context.TIME_DETECTOR_SERVICE, TimeDetector.class, new CachedServiceFetcher<TimeDetector>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass111 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public TimeDetector createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new TimeDetector();
            }
        });
        registerService("permission", PermissionManager.class, new CachedServiceFetcher<PermissionManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass112 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public PermissionManager createService(ContextImpl ctx) {
                return new PermissionManager(ctx.getOuterContext(), AppGlobals.getPackageManager());
            }
        });
        registerService(Context.PERMISSION_CONTROLLER_SERVICE, PermissionControllerManager.class, new CachedServiceFetcher<PermissionControllerManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass113 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public PermissionControllerManager createService(ContextImpl ctx) {
                return new PermissionControllerManager(ctx.getOuterContext(), ctx.getMainThreadHandler());
            }
        });
        registerService(Context.ROLE_SERVICE, RoleManager.class, new CachedServiceFetcher<RoleManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass114 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public RoleManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new RoleManager(ctx.getOuterContext());
            }
        });
        registerService("hwPcManager", HwPCManager.class, new StaticApplicationContextServiceFetcher<HwPCManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass115 */

            @Override // android.app.SystemServiceRegistry.StaticApplicationContextServiceFetcher
            public HwPCManager createService(Context context) throws ServiceManager.ServiceNotFoundException {
                return HwFrameworkFactory.getHwPCManager();
            }
        });
        registerService(Context.ROLE_CONTROLLER_SERVICE, RoleControllerManager.class, new CachedServiceFetcher<RoleControllerManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass116 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public RoleControllerManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new RoleControllerManager(ctx.getOuterContext());
            }
        });
        registerService("rollback", RollbackManager.class, new CachedServiceFetcher<RollbackManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass117 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public RollbackManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new RollbackManager(ctx.getOuterContext(), IRollbackManager.Stub.asInterface(ServiceManager.getServiceOrThrow("rollback")));
            }
        });
        registerService(Context.DYNAMIC_SYSTEM_SERVICE, DynamicSystemManager.class, new CachedServiceFetcher<DynamicSystemManager>() {
            /* class android.app.SystemServiceRegistry.AnonymousClass118 */

            @Override // android.app.SystemServiceRegistry.CachedServiceFetcher
            public DynamicSystemManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new DynamicSystemManager(IDynamicSystemService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.DYNAMIC_SYSTEM_SERVICE)));
            }
        });
    }

    private SystemServiceRegistry() {
    }

    public static Object[] createServiceCache() {
        return new Object[sServiceCacheSize];
    }

    public static Object getSystemService(ContextImpl ctx, String name) {
        ServiceFetcher<?> fetcher = SYSTEM_SERVICE_FETCHERS.get(name);
        if (fetcher != null) {
            return fetcher.getService(ctx);
        }
        return null;
    }

    public static String getSystemServiceName(Class<?> serviceClass) {
        return SYSTEM_SERVICE_NAMES.get(serviceClass);
    }

    private static <T> void registerService(String serviceName, Class<T> serviceClass, ServiceFetcher<T> serviceFetcher) {
        SYSTEM_SERVICE_NAMES.put(serviceClass, serviceName);
        SYSTEM_SERVICE_FETCHERS.put(serviceName, serviceFetcher);
    }

    static abstract class CachedServiceFetcher<T> implements ServiceFetcher<T> {
        private final int mCacheIndex = SystemServiceRegistry.access$008();

        public abstract T createService(ContextImpl contextImpl) throws ServiceManager.ServiceNotFoundException;

        CachedServiceFetcher() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x002e, code lost:
            if (r2 == false) goto L_0x0073;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
            r5 = createService(r8);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0037, code lost:
            monitor-enter(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
            r0[r7.mCacheIndex] = r5;
            r1[r7.mCacheIndex] = 2;
            r0.notifyAll();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0043, code lost:
            monitor-exit(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:0x004a, code lost:
            r5 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x004c, code lost:
            r5 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
            android.app.SystemServiceRegistry.onServiceNotFound(r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x0050, code lost:
            monitor-enter(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
            r0[r7.mCacheIndex] = null;
            r1[r7.mCacheIndex] = 3;
            r0.notifyAll();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x0062, code lost:
            monitor-enter(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
            r0[r7.mCacheIndex] = null;
            r1[r7.mCacheIndex] = 3;
            r0.notifyAll();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:44:0x006f, code lost:
            throw r5;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:48:0x0073, code lost:
            monitor-enter(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:51:0x0078, code lost:
            if (r1[r7.mCacheIndex] >= 2) goto L_0x0090;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:53:?, code lost:
            r0.wait();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:55:0x007f, code lost:
            android.util.Log.w(android.app.SystemServiceRegistry.TAG, "getService() interrupted");
            java.lang.Thread.currentThread().interrupt();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:57:0x008f, code lost:
            return null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:71:?, code lost:
            return r5;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:72:?, code lost:
            return null;
         */
        @Override // android.app.SystemServiceRegistry.ServiceFetcher
        public final T getService(ContextImpl ctx) {
            T service;
            Object[] cache = ctx.mServiceCache;
            int[] gates = ctx.mServiceInitializationStateArray;
            while (true) {
                boolean doInitialize = false;
                synchronized (cache) {
                    service = (T) cache[this.mCacheIndex];
                    if (service != null) {
                        break;
                    } else if (gates[this.mCacheIndex] == 3) {
                        break;
                    } else {
                        if (gates[this.mCacheIndex] == 2) {
                            gates[this.mCacheIndex] = 0;
                        }
                        if (gates[this.mCacheIndex] == 0) {
                            doInitialize = true;
                            gates[this.mCacheIndex] = 1;
                        }
                    }
                }
            }
            return service;
        }
    }

    static abstract class StaticServiceFetcher<T> implements ServiceFetcher<T> {
        private T mCachedInstance;

        public abstract T createService() throws ServiceManager.ServiceNotFoundException;

        StaticServiceFetcher() {
        }

        @Override // android.app.SystemServiceRegistry.ServiceFetcher
        public final T getService(ContextImpl ctx) {
            T t;
            synchronized (this) {
                if (this.mCachedInstance == null) {
                    try {
                        this.mCachedInstance = createService();
                    } catch (ServiceManager.ServiceNotFoundException e) {
                        SystemServiceRegistry.onServiceNotFound(e);
                    }
                }
                t = this.mCachedInstance;
            }
            return t;
        }
    }

    static abstract class StaticApplicationContextServiceFetcher<T> implements ServiceFetcher<T> {
        private T mCachedInstance;

        public abstract T createService(Context context) throws ServiceManager.ServiceNotFoundException;

        StaticApplicationContextServiceFetcher() {
        }

        @Override // android.app.SystemServiceRegistry.ServiceFetcher
        public final T getService(ContextImpl ctx) {
            T t;
            synchronized (this) {
                if (this.mCachedInstance == null) {
                    Context appContext = ctx.getApplicationContext();
                    try {
                        this.mCachedInstance = createService(appContext != null ? appContext : ctx);
                    } catch (ServiceManager.ServiceNotFoundException e) {
                        SystemServiceRegistry.onServiceNotFound(e);
                    }
                }
                t = this.mCachedInstance;
            }
            return t;
        }
    }

    public static void onServiceNotFound(ServiceManager.ServiceNotFoundException e) {
        if (Process.myUid() < 10000) {
            Log.wtf(TAG, e.getMessage(), e);
        } else {
            Log.w(TAG, e.getMessage());
        }
    }
}
