package android.app;

import android.accounts.AccountManager;
import android.accounts.IAccountManager;
import android.app.IAlarmManager;
import android.app.IWallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.app.admin.IDevicePolicyManager;
import android.app.job.IJobScheduler;
import android.app.job.JobScheduler;
import android.app.slice.SliceManager;
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
import android.content.Context;
import android.content.IRestrictionsManager;
import android.content.RestrictionsManager;
import android.content.pm.CrossProfileApps;
import android.content.pm.ICrossProfileApps;
import android.content.pm.IShortcutService;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutManager;
import android.content.res.Resources;
import android.hardware.ConsumerIrManager;
import android.hardware.ISerialManager;
import android.hardware.SensorManager;
import android.hardware.SerialManager;
import android.hardware.SystemSensorManager;
import android.hardware.camera2.CameraManager;
import android.hardware.display.DisplayManager;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.IFingerprintService;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.IHdmiControlService;
import android.hardware.input.InputManager;
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
import android.media.dtv.DTVServiceManager;
import android.media.dtv.IDTVService;
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
import android.net.IpSecManager;
import android.net.NetworkPolicyManager;
import android.net.NetworkScoreManager;
import android.net.NetworkWatchlistManager;
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
import android.os.DeviceIdleManager;
import android.os.DropBoxManager;
import android.os.HardwarePropertiesManager;
import android.os.IBatteryPropertiesRegistrar;
import android.os.IBinder;
import android.os.IDeviceIdleController;
import android.os.IHardwarePropertiesManager;
import android.os.IPowerManager;
import android.os.IRecoverySystem;
import android.os.ISystemUpdateManager;
import android.os.IUserManager;
import android.os.IncidentManager;
import android.os.PowerManager;
import android.os.Process;
import android.os.RecoverySystem;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.SystemUpdateManager;
import android.os.SystemVibrator;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.os.health.SystemHealthManager;
import android.os.storage.StorageManager;
import android.pc.HwPCManager;
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
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.WindowManagerImpl;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.CaptioningManager;
import android.view.autofill.AutofillManager;
import android.view.autofill.IAutoFillManager;
import android.view.inputmethod.InputMethodManager;
import android.view.textclassifier.TextClassificationManager;
import android.view.textservice.TextServicesManager;
import android.vr.IVRManagerService;
import android.vr.VrServiceManager;
import android.vrsystem.IVRSystemServiceManager;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.internal.app.ISoundTriggerService;
import com.android.internal.appwidget.IAppWidgetService;
import com.android.internal.net.INetworkWatchlistManager;
import com.android.internal.os.IDropBoxManagerService;
import com.android.internal.policy.HwPolicyFactory;
import java.util.HashMap;

final class SystemServiceRegistry {
    private static final HashMap<String, ServiceFetcher<?>> SYSTEM_SERVICE_FETCHERS = new HashMap<>();
    private static final HashMap<Class<?>, String> SYSTEM_SERVICE_NAMES = new HashMap<>();
    private static final String TAG = "SystemServiceRegistry";
    private static int sServiceCacheSize;

    static abstract class CachedServiceFetcher<T> implements ServiceFetcher<T> {
        private final int mCacheIndex = SystemServiceRegistry.access$008();

        public abstract T createService(ContextImpl contextImpl) throws ServiceManager.ServiceNotFoundException;

        CachedServiceFetcher() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x002e, code lost:
            if (r2 == false) goto L_0x0074;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0030, code lost:
            r3 = null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
            r5 = createService(r9);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0037, code lost:
            monitor-enter(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
            r0[r8.mCacheIndex] = r5;
            r1[r8.mCacheIndex] = 2;
            r0.notifyAll();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0043, code lost:
            monitor-exit(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:0x0044, code lost:
            r3 = r5;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:0x004b, code lost:
            r5 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x004d, code lost:
            r5 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
            android.app.SystemServiceRegistry.onServiceNotFound(r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x0051, code lost:
            monitor-enter(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
            r0[r8.mCacheIndex] = null;
            r1[r8.mCacheIndex] = 3;
            r0.notifyAll();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:41:0x0063, code lost:
            monitor-enter(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
            r0[r8.mCacheIndex] = null;
            r1[r8.mCacheIndex] = 3;
            r0.notifyAll();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:45:0x0070, code lost:
            throw r5;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:50:0x0074, code lost:
            monitor-enter(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:53:0x0079, code lost:
            if (r1[r8.mCacheIndex] >= 2) goto L_0x0091;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:55:?, code lost:
            r0.wait();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:59:?, code lost:
            android.util.Log.w(android.app.SystemServiceRegistry.TAG, "getService() interrupted");
            java.lang.Thread.currentThread().interrupt();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:61:0x0090, code lost:
            return null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:62:0x0091, code lost:
            monitor-exit(r0);
         */
        public final T getService(ContextImpl ctx) {
            T service;
            T service2;
            T[] cache = ctx.mServiceCache;
            int[] gates = ctx.mServiceInitializationStateArray;
            while (true) {
                boolean doInitialize = false;
                synchronized (cache) {
                    service = cache[this.mCacheIndex];
                    if (service == null) {
                        if (gates[this.mCacheIndex] == 3) {
                            break;
                        }
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
            return service2;
        }
    }

    interface ServiceFetcher<T> {
        T getService(ContextImpl contextImpl);
    }

    static abstract class StaticApplicationContextServiceFetcher<T> implements ServiceFetcher<T> {
        private T mCachedInstance;

        public abstract T createService(Context context) throws ServiceManager.ServiceNotFoundException;

        StaticApplicationContextServiceFetcher() {
        }

        public final T getService(ContextImpl ctx) {
            Context appContext;
            synchronized (this) {
                if (this.mCachedInstance == null) {
                    Context appContext2 = ctx.getApplicationContext();
                    try {
                        this.mCachedInstance = createService(appContext2 != null ? appContext2 : ctx);
                    } catch (ServiceManager.ServiceNotFoundException e) {
                        SystemServiceRegistry.onServiceNotFound(e);
                    }
                }
                appContext = this.mCachedInstance;
            }
            return appContext;
        }
    }

    static abstract class StaticServiceFetcher<T> implements ServiceFetcher<T> {
        private T mCachedInstance;

        public abstract T createService() throws ServiceManager.ServiceNotFoundException;

        StaticServiceFetcher() {
        }

        public final T getService(ContextImpl ctx) {
            ServiceManager.ServiceNotFoundException e;
            synchronized (this) {
                if (this.mCachedInstance == null) {
                    try {
                        this.mCachedInstance = createService();
                    } catch (ServiceManager.ServiceNotFoundException e2) {
                        SystemServiceRegistry.onServiceNotFound(e2);
                    }
                }
                e = this.mCachedInstance;
            }
            return e;
        }
    }

    static /* synthetic */ int access$008() {
        int i = sServiceCacheSize;
        sServiceCacheSize = i + 1;
        return i;
    }

    static {
        registerService(Context.ACCESSIBILITY_SERVICE, AccessibilityManager.class, new CachedServiceFetcher<AccessibilityManager>() {
            public AccessibilityManager createService(ContextImpl ctx) {
                return AccessibilityManager.getInstance(ctx);
            }
        });
        registerService(Context.CAPTIONING_SERVICE, CaptioningManager.class, new CachedServiceFetcher<CaptioningManager>() {
            public CaptioningManager createService(ContextImpl ctx) {
                return new CaptioningManager(ctx);
            }
        });
        registerService("account", AccountManager.class, new CachedServiceFetcher<AccountManager>() {
            public AccountManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new AccountManager(ctx, IAccountManager.Stub.asInterface(ServiceManager.getServiceOrThrow("account")));
            }
        });
        registerService(Context.ACTIVITY_SERVICE, ActivityManager.class, new CachedServiceFetcher<ActivityManager>() {
            public ActivityManager createService(ContextImpl ctx) {
                return new ActivityManager(ctx.getOuterContext(), ctx.mMainThread.getHandler());
            }
        });
        registerService("alarm", AlarmManager.class, new CachedServiceFetcher<AlarmManager>() {
            public AlarmManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new AlarmManager(IAlarmManager.Stub.asInterface(ServiceManager.getServiceOrThrow("alarm")), ctx);
            }
        });
        registerService("audio", AudioManager.class, new CachedServiceFetcher<AudioManager>() {
            public AudioManager createService(ContextImpl ctx) {
                return new AudioManager(ctx);
            }
        });
        registerService(Context.MEDIA_ROUTER_SERVICE, MediaRouter.class, new CachedServiceFetcher<MediaRouter>() {
            public MediaRouter createService(ContextImpl ctx) {
                return new MediaRouter(ctx);
            }
        });
        registerService(Context.BLUETOOTH_SERVICE, BluetoothManager.class, new CachedServiceFetcher<BluetoothManager>() {
            public BluetoothManager createService(ContextImpl ctx) {
                return new BluetoothManager(ctx);
            }
        });
        registerService("vr_system", IVRSystemServiceManager.class, new CachedServiceFetcher<IVRSystemServiceManager>() {
            public IVRSystemServiceManager createService(ContextImpl ctx) {
                return HwFrameworkFactory.getVRSystemServiceManager();
            }
        });
        if (SystemProperties.getBoolean("ro.vr_display.service", false)) {
            registerService("vr_display", VrServiceManager.class, new CachedServiceFetcher<VrServiceManager>() {
                public VrServiceManager createService(ContextImpl ctx) {
                    return new VrServiceManager(IVRManagerService.Stub.asInterface(ServiceManager.getService("vr_display")), ctx);
                }
            });
        }
        registerService(Context.HDMI_CONTROL_SERVICE, HdmiControlManager.class, new StaticServiceFetcher<HdmiControlManager>() {
            public HdmiControlManager createService() throws ServiceManager.ServiceNotFoundException {
                return new HdmiControlManager(IHdmiControlService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.HDMI_CONTROL_SERVICE)));
            }
        });
        registerService(Context.TEXT_CLASSIFICATION_SERVICE, TextClassificationManager.class, new CachedServiceFetcher<TextClassificationManager>() {
            public TextClassificationManager createService(ContextImpl ctx) {
                return new TextClassificationManager(ctx);
            }
        });
        registerService(Context.CLIPBOARD_SERVICE, ClipboardManager.class, new CachedServiceFetcher<ClipboardManager>() {
            public ClipboardManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new ClipboardManager(ctx.getOuterContext(), ctx.mMainThread.getHandler());
            }
        });
        SYSTEM_SERVICE_NAMES.put(android.text.ClipboardManager.class, Context.CLIPBOARD_SERVICE);
        registerService(Context.CONNECTIVITY_SERVICE, ConnectivityManager.class, new StaticApplicationContextServiceFetcher<ConnectivityManager>() {
            public ConnectivityManager createService(Context context) throws ServiceManager.ServiceNotFoundException {
                return new ConnectivityManager(context, IConnectivityManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.CONNECTIVITY_SERVICE)));
            }
        });
        registerService("ipsec", IpSecManager.class, new CachedServiceFetcher<IpSecManager>() {
            public IpSecManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new IpSecManager(ctx, IIpSecService.Stub.asInterface(ServiceManager.getService("ipsec")));
            }
        });
        registerService(Context.COUNTRY_DETECTOR, CountryDetector.class, new StaticServiceFetcher<CountryDetector>() {
            public CountryDetector createService() throws ServiceManager.ServiceNotFoundException {
                return new CountryDetector(ICountryDetector.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.COUNTRY_DETECTOR)));
            }
        });
        registerService(Context.DEVICE_POLICY_SERVICE, DevicePolicyManager.class, new CachedServiceFetcher<DevicePolicyManager>() {
            public DevicePolicyManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new DevicePolicyManager(ctx, IDevicePolicyManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.DEVICE_POLICY_SERVICE)));
            }
        });
        registerService(Context.DOWNLOAD_SERVICE, DownloadManager.class, new CachedServiceFetcher<DownloadManager>() {
            public DownloadManager createService(ContextImpl ctx) {
                return new DownloadManager(ctx);
            }
        });
        registerService(Context.BATTERY_SERVICE, BatteryManager.class, new CachedServiceFetcher<BatteryManager>() {
            public BatteryManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new BatteryManager(ctx, IBatteryStats.Stub.asInterface(ServiceManager.getServiceOrThrow("batterystats")), IBatteryPropertiesRegistrar.Stub.asInterface(ServiceManager.getServiceOrThrow("batteryproperties")));
            }
        });
        registerService(Context.NFC_SERVICE, NfcManager.class, new CachedServiceFetcher<NfcManager>() {
            public NfcManager createService(ContextImpl ctx) {
                return new NfcManager(ctx);
            }
        });
        registerService(Context.DROPBOX_SERVICE, DropBoxManager.class, new CachedServiceFetcher<DropBoxManager>() {
            public DropBoxManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new DropBoxManager(ctx, IDropBoxManagerService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.DROPBOX_SERVICE)));
            }
        });
        registerService("input", InputManager.class, new StaticServiceFetcher<InputManager>() {
            public InputManager createService() {
                return InputManager.getInstance();
            }
        });
        registerService(Context.DISPLAY_SERVICE, DisplayManager.class, new CachedServiceFetcher<DisplayManager>() {
            public DisplayManager createService(ContextImpl ctx) {
                return new DisplayManager(ctx.getOuterContext());
            }
        });
        registerService(Context.INPUT_METHOD_SERVICE, InputMethodManager.class, new StaticServiceFetcher<InputMethodManager>() {
            public InputMethodManager createService() {
                return InputMethodManager.getInstance();
            }
        });
        registerService(Context.TEXT_SERVICES_MANAGER_SERVICE, TextServicesManager.class, new StaticServiceFetcher<TextServicesManager>() {
            public TextServicesManager createService() {
                return TextServicesManager.getInstance();
            }
        });
        registerService(Context.KEYGUARD_SERVICE, KeyguardManager.class, new CachedServiceFetcher<KeyguardManager>() {
            public KeyguardManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new KeyguardManager(ctx);
            }
        });
        registerService(Context.LAYOUT_INFLATER_SERVICE, LayoutInflater.class, new CachedServiceFetcher<LayoutInflater>() {
            public LayoutInflater createService(ContextImpl ctx) {
                return HwPolicyFactory.getHwPhoneLayoutInflater(ctx.getOuterContext());
            }
        });
        registerService("location", LocationManager.class, new CachedServiceFetcher<LocationManager>() {
            public LocationManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new LocationManager(ctx, ILocationManager.Stub.asInterface(ServiceManager.getServiceOrThrow("location")));
            }
        });
        registerService(Context.NETWORK_POLICY_SERVICE, NetworkPolicyManager.class, new CachedServiceFetcher<NetworkPolicyManager>() {
            public NetworkPolicyManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                HwFrameworkFactory.IHwNetworkPolicyManager inpm = HwFrameworkFactory.getHwNetworkPolicyManager();
                if (inpm != null) {
                    return inpm.getInstance(ctx, INetworkPolicyManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.NETWORK_POLICY_SERVICE)));
                }
                return new NetworkPolicyManager(ctx, INetworkPolicyManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.NETWORK_POLICY_SERVICE)));
            }
        });
        registerService(Context.NOTIFICATION_SERVICE, NotificationManager.class, new CachedServiceFetcher<NotificationManager>() {
            public NotificationManager createService(ContextImpl ctx) {
                Context outerContext = ctx.getOuterContext();
                return new NotificationManager(new ContextThemeWrapper(outerContext, Resources.selectSystemTheme(0, outerContext.getApplicationInfo().targetSdkVersion, 16973835, 16973935, 16974126, 16974130)), ctx.mMainThread.getHandler());
            }
        });
        registerService(Context.NSD_SERVICE, NsdManager.class, new CachedServiceFetcher<NsdManager>() {
            public NsdManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new NsdManager(ctx.getOuterContext(), INsdManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.NSD_SERVICE)));
            }
        });
        registerService(Context.POWER_SERVICE, PowerManager.class, new CachedServiceFetcher<PowerManager>() {
            public PowerManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new PowerManager(ctx.getOuterContext(), IPowerManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.POWER_SERVICE)), ctx.mMainThread.getHandler());
            }
        });
        registerService(Context.RECOVERY_SERVICE, RecoverySystem.class, new CachedServiceFetcher<RecoverySystem>() {
            public RecoverySystem createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new RecoverySystem(IRecoverySystem.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.RECOVERY_SERVICE)));
            }
        });
        registerService(Context.SEARCH_SERVICE, SearchManager.class, new CachedServiceFetcher<SearchManager>() {
            public SearchManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new SearchManager(ctx.getOuterContext(), ctx.mMainThread.getHandler());
            }
        });
        registerService(Context.SENSOR_SERVICE, SensorManager.class, new CachedServiceFetcher<SensorManager>() {
            public SensorManager createService(ContextImpl ctx) {
                return new SystemSensorManager(ctx.getOuterContext(), ctx.mMainThread.getHandler().getLooper());
            }
        });
        registerService(Context.STATS_MANAGER, StatsManager.class, new CachedServiceFetcher<StatsManager>() {
            public StatsManager createService(ContextImpl ctx) {
                return new StatsManager(ctx.getOuterContext());
            }
        });
        registerService(Context.STATUS_BAR_SERVICE, StatusBarManager.class, new CachedServiceFetcher<StatusBarManager>() {
            public StatusBarManager createService(ContextImpl ctx) {
                return new StatusBarManager(ctx.getOuterContext());
            }
        });
        registerService(Context.STORAGE_SERVICE, StorageManager.class, new CachedServiceFetcher<StorageManager>() {
            public StorageManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new StorageManager(ctx, ctx.mMainThread.getHandler().getLooper());
            }
        });
        registerService(Context.STORAGE_STATS_SERVICE, StorageStatsManager.class, new CachedServiceFetcher<StorageStatsManager>() {
            public StorageStatsManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new StorageStatsManager(ctx, IStorageStatsManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.STORAGE_STATS_SERVICE)));
            }
        });
        registerService(Context.SYSTEM_UPDATE_SERVICE, SystemUpdateManager.class, new CachedServiceFetcher<SystemUpdateManager>() {
            public SystemUpdateManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new SystemUpdateManager(ISystemUpdateManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.SYSTEM_UPDATE_SERVICE)));
            }
        });
        registerService(Context.TELEPHONY_SERVICE, TelephonyManager.class, new CachedServiceFetcher<TelephonyManager>() {
            public TelephonyManager createService(ContextImpl ctx) {
                return new TelephonyManager(ctx.getOuterContext());
            }
        });
        registerService(Context.TELEPHONY_SUBSCRIPTION_SERVICE, SubscriptionManager.class, new CachedServiceFetcher<SubscriptionManager>() {
            public SubscriptionManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new SubscriptionManager(ctx.getOuterContext());
            }
        });
        registerService(Context.CARRIER_CONFIG_SERVICE, CarrierConfigManager.class, new CachedServiceFetcher<CarrierConfigManager>() {
            public CarrierConfigManager createService(ContextImpl ctx) {
                return new CarrierConfigManager();
            }
        });
        registerService(Context.TELECOM_SERVICE, TelecomManager.class, new CachedServiceFetcher<TelecomManager>() {
            public TelecomManager createService(ContextImpl ctx) {
                return new TelecomManager(ctx.getOuterContext());
            }
        });
        registerService(Context.EUICC_SERVICE, EuiccManager.class, new CachedServiceFetcher<EuiccManager>() {
            public EuiccManager createService(ContextImpl ctx) {
                return new EuiccManager(ctx.getOuterContext());
            }
        });
        registerService(Context.EUICC_CARD_SERVICE, EuiccCardManager.class, new CachedServiceFetcher<EuiccCardManager>() {
            public EuiccCardManager createService(ContextImpl ctx) {
                return new EuiccCardManager(ctx.getOuterContext());
            }
        });
        registerService(Context.UI_MODE_SERVICE, UiModeManager.class, new CachedServiceFetcher<UiModeManager>() {
            public UiModeManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new UiModeManager();
            }
        });
        registerService(Context.USB_SERVICE, UsbManager.class, new CachedServiceFetcher<UsbManager>() {
            public UsbManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new UsbManager(ctx, IUsbManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.USB_SERVICE)));
            }
        });
        registerService("serial", SerialManager.class, new CachedServiceFetcher<SerialManager>() {
            public SerialManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new SerialManager(ctx, ISerialManager.Stub.asInterface(ServiceManager.getServiceOrThrow("serial")));
            }
        });
        registerService(Context.VIBRATOR_SERVICE, Vibrator.class, new CachedServiceFetcher<Vibrator>() {
            public Vibrator createService(ContextImpl ctx) {
                return new SystemVibrator(ctx);
            }
        });
        registerService("wallpaper", WallpaperManager.class, new CachedServiceFetcher<WallpaperManager>() {
            public WallpaperManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                IBinder b;
                if (ctx.getApplicationInfo().targetSdkVersion >= 28) {
                    b = ServiceManager.getServiceOrThrow("wallpaper");
                } else {
                    b = ServiceManager.getService("wallpaper");
                }
                IWallpaperManager service = IWallpaperManager.Stub.asInterface(b);
                HwFrameworkFactory.IHwWallpaperManager iwm = HwFrameworkFactory.getHuaweiWallpaperManager();
                if (iwm != null) {
                    return iwm.getInstance(service, ctx.getOuterContext(), ctx.mMainThread.getHandler());
                }
                return new WallpaperManager(service, ctx.getOuterContext(), ctx.mMainThread.getHandler());
            }
        });
        registerService("lowpan", LowpanManager.class, new CachedServiceFetcher<LowpanManager>() {
            public LowpanManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new LowpanManager(ctx.getOuterContext(), ILowpanManager.Stub.asInterface(ServiceManager.getServiceOrThrow("lowpan")), ConnectivityThread.getInstanceLooper());
            }
        });
        registerService(Context.WIFI_SERVICE, WifiManager.class, new CachedServiceFetcher<WifiManager>() {
            public WifiManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new WifiManager(ctx.getOuterContext(), IWifiManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.WIFI_SERVICE)), ConnectivityThread.getInstanceLooper());
            }
        });
        registerService(Context.WIFI_P2P_SERVICE, WifiP2pManager.class, new StaticServiceFetcher<WifiP2pManager>() {
            public WifiP2pManager createService() throws ServiceManager.ServiceNotFoundException {
                return new WifiP2pManager(IWifiP2pManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.WIFI_P2P_SERVICE)));
            }
        });
        registerService(Context.WIFI_AWARE_SERVICE, WifiAwareManager.class, new CachedServiceFetcher<WifiAwareManager>() {
            public WifiAwareManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                IWifiAwareManager service = IWifiAwareManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.WIFI_AWARE_SERVICE));
                if (service == null) {
                    return null;
                }
                return new WifiAwareManager(ctx.getOuterContext(), service);
            }
        });
        registerService(Context.WIFI_SCANNING_SERVICE, WifiScanner.class, new CachedServiceFetcher<WifiScanner>() {
            public WifiScanner createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new WifiScanner(ctx.getOuterContext(), IWifiScanner.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.WIFI_SCANNING_SERVICE)), ConnectivityThread.getInstanceLooper());
            }
        });
        registerService(Context.WIFI_RTT_SERVICE, RttManager.class, new CachedServiceFetcher<RttManager>() {
            public RttManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new RttManager(ctx.getOuterContext(), new WifiRttManager(ctx.getOuterContext(), IWifiRttManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.WIFI_RTT_RANGING_SERVICE))));
            }
        });
        registerService(Context.WIFI_RTT_RANGING_SERVICE, WifiRttManager.class, new CachedServiceFetcher<WifiRttManager>() {
            public WifiRttManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new WifiRttManager(ctx.getOuterContext(), IWifiRttManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.WIFI_RTT_RANGING_SERVICE)));
            }
        });
        registerService(Context.ETHERNET_SERVICE, EthernetManager.class, new CachedServiceFetcher<EthernetManager>() {
            public EthernetManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new EthernetManager(ctx.getOuterContext(), IEthernetManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.ETHERNET_SERVICE)));
            }
        });
        registerService(Context.WINDOW_SERVICE, WindowManager.class, new CachedServiceFetcher<WindowManager>() {
            public WindowManager createService(ContextImpl ctx) {
                return new WindowManagerImpl(ctx);
            }
        });
        registerService(Context.USER_SERVICE, UserManager.class, new CachedServiceFetcher<UserManager>() {
            public UserManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new UserManager(ctx, IUserManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.USER_SERVICE)));
            }
        });
        registerService(Context.APP_OPS_SERVICE, AppOpsManager.class, new CachedServiceFetcher<AppOpsManager>() {
            public AppOpsManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new AppOpsManager(ctx, IAppOpsService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.APP_OPS_SERVICE)));
            }
        });
        registerService(Context.CAMERA_SERVICE, CameraManager.class, new CachedServiceFetcher<CameraManager>() {
            public CameraManager createService(ContextImpl ctx) {
                return new CameraManager(ctx);
            }
        });
        registerService(Context.LAUNCHER_APPS_SERVICE, LauncherApps.class, new CachedServiceFetcher<LauncherApps>() {
            public LauncherApps createService(ContextImpl ctx) {
                return new LauncherApps(ctx);
            }
        });
        registerService(Context.RESTRICTIONS_SERVICE, RestrictionsManager.class, new CachedServiceFetcher<RestrictionsManager>() {
            public RestrictionsManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new RestrictionsManager(ctx, IRestrictionsManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.RESTRICTIONS_SERVICE)));
            }
        });
        registerService(Context.PRINT_SERVICE, PrintManager.class, new CachedServiceFetcher<PrintManager>() {
            public PrintManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                IPrintManager service = null;
                if (ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_PRINTING)) {
                    service = IPrintManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.PRINT_SERVICE));
                }
                return new PrintManager(ctx.getOuterContext(), service, ctx.getUserId(), UserHandle.getAppId(ctx.getApplicationInfo().uid));
            }
        });
        registerService(Context.COMPANION_DEVICE_SERVICE, CompanionDeviceManager.class, new CachedServiceFetcher<CompanionDeviceManager>() {
            public CompanionDeviceManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                ICompanionDeviceManager service = null;
                if (ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_COMPANION_DEVICE_SETUP)) {
                    service = ICompanionDeviceManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.COMPANION_DEVICE_SERVICE));
                }
                return new CompanionDeviceManager(service, ctx.getOuterContext());
            }
        });
        registerService(Context.CONSUMER_IR_SERVICE, ConsumerIrManager.class, new CachedServiceFetcher<ConsumerIrManager>() {
            public ConsumerIrManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new ConsumerIrManager(ctx);
            }
        });
        registerService(Context.MEDIA_SESSION_SERVICE, MediaSessionManager.class, new CachedServiceFetcher<MediaSessionManager>() {
            public MediaSessionManager createService(ContextImpl ctx) {
                return new MediaSessionManager(ctx);
            }
        });
        registerService(Context.TRUST_SERVICE, TrustManager.class, new StaticServiceFetcher<TrustManager>() {
            public TrustManager createService() throws ServiceManager.ServiceNotFoundException {
                return new TrustManager(ServiceManager.getServiceOrThrow(Context.TRUST_SERVICE));
            }
        });
        registerService(Context.FINGERPRINT_SERVICE, FingerprintManager.class, new CachedServiceFetcher<FingerprintManager>() {
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
        registerService(Context.TV_INPUT_SERVICE, TvInputManager.class, new CachedServiceFetcher<TvInputManager>() {
            public TvInputManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new TvInputManager(ITvInputManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.TV_INPUT_SERVICE)), ctx.getUserId());
            }
        });
        registerService(Context.NETWORK_SCORE_SERVICE, NetworkScoreManager.class, new CachedServiceFetcher<NetworkScoreManager>() {
            public NetworkScoreManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new NetworkScoreManager(ctx);
            }
        });
        registerService(Context.USAGE_STATS_SERVICE, UsageStatsManager.class, new CachedServiceFetcher<UsageStatsManager>() {
            public UsageStatsManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new UsageStatsManager(ctx.getOuterContext(), IUsageStatsManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.USAGE_STATS_SERVICE)));
            }
        });
        registerService(Context.NETWORK_STATS_SERVICE, NetworkStatsManager.class, new CachedServiceFetcher<NetworkStatsManager>() {
            public NetworkStatsManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new NetworkStatsManager(ctx.getOuterContext());
            }
        });
        registerService(Context.JOB_SCHEDULER_SERVICE, JobScheduler.class, new StaticServiceFetcher<JobScheduler>() {
            public JobScheduler createService() throws ServiceManager.ServiceNotFoundException {
                return new JobSchedulerImpl(IJobScheduler.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.JOB_SCHEDULER_SERVICE)));
            }
        });
        registerService(Context.PERSISTENT_DATA_BLOCK_SERVICE, PersistentDataBlockManager.class, new StaticServiceFetcher<PersistentDataBlockManager>() {
            public PersistentDataBlockManager createService() {
                IPersistentDataBlockService persistentDataBlockService = IPersistentDataBlockService.Stub.asInterface(ServiceManager.getService(Context.PERSISTENT_DATA_BLOCK_SERVICE));
                if (persistentDataBlockService != null) {
                    return new PersistentDataBlockManager(persistentDataBlockService);
                }
                return null;
            }
        });
        registerService(Context.OEM_LOCK_SERVICE, OemLockManager.class, new StaticServiceFetcher<OemLockManager>() {
            public OemLockManager createService() throws ServiceManager.ServiceNotFoundException {
                IOemLockService oemLockService = IOemLockService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.OEM_LOCK_SERVICE));
                if (oemLockService != null) {
                    return new OemLockManager(oemLockService);
                }
                return null;
            }
        });
        registerService(Context.MEDIA_PROJECTION_SERVICE, MediaProjectionManager.class, new CachedServiceFetcher<MediaProjectionManager>() {
            public MediaProjectionManager createService(ContextImpl ctx) {
                return new MediaProjectionManager(ctx);
            }
        });
        registerService(Context.APPWIDGET_SERVICE, AppWidgetManager.class, new CachedServiceFetcher<AppWidgetManager>() {
            public AppWidgetManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new AppWidgetManager(ctx, IAppWidgetService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.APPWIDGET_SERVICE)));
            }
        });
        registerService("midi", MidiManager.class, new CachedServiceFetcher<MidiManager>() {
            public MidiManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new MidiManager(IMidiManager.Stub.asInterface(ServiceManager.getServiceOrThrow("midi")));
            }
        });
        registerService(Context.RADIO_SERVICE, RadioManager.class, new CachedServiceFetcher<RadioManager>() {
            public RadioManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new RadioManager(ctx);
            }
        });
        registerService(Context.HARDWARE_PROPERTIES_SERVICE, HardwarePropertiesManager.class, new CachedServiceFetcher<HardwarePropertiesManager>() {
            public HardwarePropertiesManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new HardwarePropertiesManager(ctx, IHardwarePropertiesManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.HARDWARE_PROPERTIES_SERVICE)));
            }
        });
        registerService(Context.SOUND_TRIGGER_SERVICE, SoundTriggerManager.class, new CachedServiceFetcher<SoundTriggerManager>() {
            public SoundTriggerManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new SoundTriggerManager(ctx, ISoundTriggerService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.SOUND_TRIGGER_SERVICE)));
            }
        });
        registerService("shortcut", ShortcutManager.class, new CachedServiceFetcher<ShortcutManager>() {
            public ShortcutManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new ShortcutManager(ctx, IShortcutService.Stub.asInterface(ServiceManager.getServiceOrThrow("shortcut")));
            }
        });
        registerService(Context.NETWORK_WATCHLIST_SERVICE, NetworkWatchlistManager.class, new CachedServiceFetcher<NetworkWatchlistManager>() {
            public NetworkWatchlistManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new NetworkWatchlistManager(ctx, INetworkWatchlistManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.NETWORK_WATCHLIST_SERVICE)));
            }
        });
        registerService(Context.SYSTEM_HEALTH_SERVICE, SystemHealthManager.class, new CachedServiceFetcher<SystemHealthManager>() {
            public SystemHealthManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new SystemHealthManager(IBatteryStats.Stub.asInterface(ServiceManager.getServiceOrThrow("batterystats")));
            }
        });
        registerService(Context.CONTEXTHUB_SERVICE, ContextHubManager.class, new CachedServiceFetcher<ContextHubManager>() {
            public ContextHubManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new ContextHubManager(ctx.getOuterContext(), ctx.mMainThread.getHandler().getLooper());
            }
        });
        registerService(Context.INCIDENT_SERVICE, IncidentManager.class, new CachedServiceFetcher<IncidentManager>() {
            public IncidentManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new IncidentManager(ctx);
            }
        });
        registerService(Context.AUTOFILL_MANAGER_SERVICE, AutofillManager.class, new CachedServiceFetcher<AutofillManager>() {
            public AutofillManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new AutofillManager(ctx.getOuterContext(), IAutoFillManager.Stub.asInterface(ServiceManager.getService(Context.AUTOFILL_MANAGER_SERVICE)));
            }
        });
        registerService(Context.VR_SERVICE, VrManager.class, new CachedServiceFetcher<VrManager>() {
            public VrManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new VrManager(IVrManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.VR_SERVICE)));
            }
        });
        registerService(Context.TIME_ZONE_RULES_MANAGER_SERVICE, RulesManager.class, new CachedServiceFetcher<RulesManager>() {
            public RulesManager createService(ContextImpl ctx) {
                return new RulesManager(ctx.getOuterContext());
            }
        });
        registerService(Context.CROSS_PROFILE_APPS_SERVICE, CrossProfileApps.class, new CachedServiceFetcher<CrossProfileApps>() {
            public CrossProfileApps createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new CrossProfileApps(ctx.getOuterContext(), ICrossProfileApps.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.CROSS_PROFILE_APPS_SERVICE)));
            }
        });
        registerService("slice", SliceManager.class, new CachedServiceFetcher<SliceManager>() {
            public SliceManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new SliceManager(ctx.getOuterContext(), ctx.mMainThread.getHandler());
            }
        });
        if (SystemProperties.getBoolean("ro.dtv.service", false)) {
            registerService(Context.DTV_SERVICE, DTVServiceManager.class, new CachedServiceFetcher<DTVServiceManager>() {
                public DTVServiceManager createService(ContextImpl ctx) {
                    IDTVService service = IDTVService.Stub.asInterface(ServiceManager.getService(Context.DTV_SERVICE));
                    if (service == null) {
                        return null;
                    }
                    return new DTVServiceManager(ctx, service);
                }
            });
        }
        registerService(Context.DEVICE_IDLE_CONTROLLER, DeviceIdleManager.class, new CachedServiceFetcher<DeviceIdleManager>() {
            public DeviceIdleManager createService(ContextImpl ctx) throws ServiceManager.ServiceNotFoundException {
                return new DeviceIdleManager(ctx.getOuterContext(), IDeviceIdleController.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.DEVICE_IDLE_CONTROLLER)));
            }
        });
        registerService("hwPcManager", HwPCManager.class, new StaticApplicationContextServiceFetcher<HwPCManager>() {
            public HwPCManager createService(Context context) throws ServiceManager.ServiceNotFoundException {
                return HwFrameworkFactory.getHwPCManager();
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

    public static void onServiceNotFound(ServiceManager.ServiceNotFoundException e) {
        if (Process.myUid() < 10000) {
            Log.wtf(TAG, e.getMessage(), e);
        } else {
            Log.w(TAG, e.getMessage());
        }
    }
}
