package com.android.server.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.IUsbManager.Stub;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbPort;
import android.hardware.usb.UsbPortStatus;
import android.os.Binder;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.UserHandle;
import android.util.Flog;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.server.SystemService;
import com.android.server.am.ProcessList;
import com.android.server.usb.HwUsbServiceFactory.IHwUsbDeviceManager;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class UsbService extends Stub {
    private static final String TAG = "UsbService";
    private final UsbAlsaManager mAlsaManager;
    private final Context mContext;
    private UsbDeviceManager mDeviceManager;
    private UsbHostManager mHostManager;
    private final Object mLock;
    private UsbPortManager mPortManager;
    private BroadcastReceiver mReceiver;
    @GuardedBy("mLock")
    private final SparseArray<UsbSettingsManager> mSettingsByUser;

    public static class Lifecycle extends SystemService {
        private UsbService mUsbService;

        public Lifecycle(Context context) {
            super(context);
        }

        public void onStart() {
            this.mUsbService = new UsbService(getContext());
            publishBinderService("usb", this.mUsbService);
        }

        public void onBootPhase(int phase) {
            if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
                this.mUsbService.systemReady();
            } else if (phase == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
                this.mUsbService.bootCompleted();
            }
        }
    }

    private UsbSettingsManager getSettingsForUser(int userId) {
        UsbSettingsManager settings;
        synchronized (this.mLock) {
            settings = (UsbSettingsManager) this.mSettingsByUser.get(userId);
            if (settings == null) {
                settings = new UsbSettingsManager(this.mContext, new UserHandle(userId));
                this.mSettingsByUser.put(userId, settings);
            }
        }
        return settings;
    }

    public UsbService(Context context) {
        this.mLock = new Object();
        this.mSettingsByUser = new SparseArray();
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                String action = intent.getAction();
                if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    UsbService.this.setCurrentUser(userId);
                } else if ("android.intent.action.USER_STOPPED".equals(action)) {
                    synchronized (UsbService.this.mLock) {
                        UsbService.this.mSettingsByUser.remove(userId);
                    }
                } else if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(action) && UsbService.this.mDeviceManager != null) {
                    UsbService.this.mDeviceManager.updateUserRestrictions();
                }
            }
        };
        this.mContext = context;
        this.mAlsaManager = new UsbAlsaManager(context);
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.usb.host")) {
            this.mHostManager = new UsbHostManager(context, this.mAlsaManager);
        }
        if (new File("/sys/class/android_usb").exists()) {
            IHwUsbDeviceManager iudm = HwUsbServiceFactory.getHuaweiUsbDeviceManager();
            if (iudm != null) {
                this.mDeviceManager = iudm.getInstance(context, this.mAlsaManager);
            } else {
                this.mDeviceManager = new UsbDeviceManager(context, this.mAlsaManager);
            }
        }
        if (!(this.mHostManager == null && this.mDeviceManager == null)) {
            this.mPortManager = new UsbPortManager(context);
        }
        setCurrentUser(0);
        IntentFilter filter = new IntentFilter();
        filter.setPriority(ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE);
        filter.addAction("android.intent.action.USER_SWITCHED");
        filter.addAction("android.intent.action.USER_STOPPED");
        filter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        this.mContext.registerReceiver(this.mReceiver, filter, null, null);
    }

    private void setCurrentUser(int userId) {
        UsbSettingsManager userSettings = getSettingsForUser(userId);
        if (this.mHostManager != null) {
            this.mHostManager.setCurrentSettings(userSettings);
        }
        if (this.mDeviceManager != null) {
            this.mDeviceManager.setCurrentUser(userId, userSettings);
        }
    }

    public void systemReady() {
        this.mAlsaManager.systemReady();
        if (this.mDeviceManager != null) {
            this.mDeviceManager.systemReady();
        }
        if (this.mHostManager != null) {
            this.mHostManager.systemReady();
        }
        if (this.mPortManager != null) {
            this.mPortManager.systemReady();
        }
    }

    public void bootCompleted() {
        if (this.mDeviceManager != null) {
            this.mDeviceManager.bootCompleted();
        }
    }

    public void getDeviceList(Bundle devices) {
        if (this.mHostManager != null) {
            this.mHostManager.getDeviceList(devices);
        }
    }

    public ParcelFileDescriptor openDevice(String deviceName) {
        if (this.mHostManager != null) {
            return this.mHostManager.openDevice(deviceName);
        }
        return null;
    }

    public UsbAccessory getCurrentAccessory() {
        if (this.mDeviceManager != null) {
            return this.mDeviceManager.getCurrentAccessory();
        }
        return null;
    }

    public ParcelFileDescriptor openAccessory(UsbAccessory accessory) {
        if (this.mDeviceManager != null) {
            return this.mDeviceManager.openAccessory(accessory);
        }
        return null;
    }

    public void setDevicePackage(UsbDevice device, String packageName, int userId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        getSettingsForUser(userId).setDevicePackage(device, packageName);
    }

    public void setAccessoryPackage(UsbAccessory accessory, String packageName, int userId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        getSettingsForUser(userId).setAccessoryPackage(accessory, packageName);
    }

    public boolean hasDevicePermission(UsbDevice device) {
        return getSettingsForUser(UserHandle.getCallingUserId()).hasPermission(device);
    }

    public boolean hasAccessoryPermission(UsbAccessory accessory) {
        return getSettingsForUser(UserHandle.getCallingUserId()).hasPermission(accessory);
    }

    public void requestDevicePermission(UsbDevice device, String packageName, PendingIntent pi) {
        getSettingsForUser(UserHandle.getCallingUserId()).requestPermission(device, packageName, pi);
    }

    public void requestAccessoryPermission(UsbAccessory accessory, String packageName, PendingIntent pi) {
        getSettingsForUser(UserHandle.getCallingUserId()).requestPermission(accessory, packageName, pi);
    }

    public void grantDevicePermission(UsbDevice device, int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        getSettingsForUser(UserHandle.getUserId(uid)).grantDevicePermission(device, uid);
    }

    public void grantAccessoryPermission(UsbAccessory accessory, int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        getSettingsForUser(UserHandle.getUserId(uid)).grantAccessoryPermission(accessory, uid);
    }

    public boolean hasDefaults(String packageName, int userId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        return getSettingsForUser(userId).hasDefaults(packageName);
    }

    public void clearDefaults(String packageName, int userId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        getSettingsForUser(userId).clearDefaults(packageName);
    }

    public boolean isFunctionEnabled(String function) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        return this.mDeviceManager != null ? this.mDeviceManager.isFunctionEnabled(function) : false;
    }

    public void setCurrentFunction(String function) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        int uid = Binder.getCallingUid();
        Flog.i(1306, "UsbService Caller of setCurrentFunction is uid=" + uid + ", pid=" + Binder.getCallingPid());
        if (!isSupportedCurrentFunction(function)) {
            Slog.w(TAG, "Caller of setCurrentFunction() requested unsupported USB function: " + function);
            function = "none";
        }
        if (this.mDeviceManager != null) {
            this.mDeviceManager.setCurrentFunctions(function);
            return;
        }
        throw new IllegalStateException("USB device mode not supported");
    }

    private static boolean isSupportedCurrentFunction(String function) {
        if (function == null || function.equals("none") || function.equals("audio_source") || function.equals("midi") || function.equals("mtp") || function.equals("ptp") || function.equals("rndis") || function.equals("rndis,serial") || function.equals("hisuite,mtp,mass_storage") || function.equals("hisuite,mtp,mass_storage,hdb") || function.equals("mass_storage") || function.equals("manufacture,adb") || function.equals("ncm")) {
            return true;
        }
        return false;
    }

    public void setUsbDataUnlocked(boolean unlocked) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        int uid = Binder.getCallingUid();
        Flog.i(1306, "UsbService Caller of setUsbDataUnlocked is uid=" + uid + ", pid=" + Binder.getCallingPid());
        this.mDeviceManager.setUsbDataUnlocked(unlocked);
    }

    public void allowUsbDebugging(boolean alwaysAllow, String publicKey) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        this.mDeviceManager.allowUsbDebugging(alwaysAllow, publicKey);
    }

    public void denyUsbDebugging() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        this.mDeviceManager.denyUsbDebugging();
    }

    public void clearUsbDebuggingKeys() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        this.mDeviceManager.clearUsbDebuggingKeys();
    }

    public UsbPort[] getPorts() {
        UsbPort[] usbPortArr = null;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        long ident = Binder.clearCallingIdentity();
        try {
            if (this.mPortManager != null) {
                usbPortArr = this.mPortManager.getPorts();
            }
            Binder.restoreCallingIdentity(ident);
            return usbPortArr;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public UsbPortStatus getPortStatus(String portId) {
        UsbPortStatus usbPortStatus = null;
        Preconditions.checkNotNull(portId, "portId must not be null");
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        long ident = Binder.clearCallingIdentity();
        try {
            if (this.mPortManager != null) {
                usbPortStatus = this.mPortManager.getPortStatus(portId);
            }
            Binder.restoreCallingIdentity(ident);
            return usbPortStatus;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void setPortRoles(String portId, int powerRole, int dataRole) {
        Preconditions.checkNotNull(portId, "portId must not be null");
        UsbPort.checkRoles(powerRole, dataRole);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        long ident = Binder.clearCallingIdentity();
        try {
            if (this.mPortManager != null) {
                this.mPortManager.setPortRoles(portId, powerRole, dataRole, null);
            }
            Binder.restoreCallingIdentity(ident);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        long ident = Binder.clearCallingIdentity();
        if (args != null) {
            if (args.length != 0) {
                if (!"-a".equals(args[0])) {
                    String portId;
                    String str;
                    int powerRole;
                    int dataRole;
                    if (args.length == 4) {
                        if ("set-port-roles".equals(args[0])) {
                            portId = args[1];
                            str = args[2];
                            if (str.equals("source")) {
                                powerRole = 1;
                            } else {
                                if (str.equals("sink")) {
                                    powerRole = 2;
                                } else {
                                    if (str.equals("no-power")) {
                                        powerRole = 0;
                                    } else {
                                        pw.println("Invalid power role: " + args[2]);
                                        Binder.restoreCallingIdentity(ident);
                                        return;
                                    }
                                }
                            }
                            str = args[3];
                            if (str.equals("host")) {
                                dataRole = 1;
                            } else {
                                if (str.equals("device")) {
                                    dataRole = 2;
                                } else {
                                    if (str.equals("no-data")) {
                                        dataRole = 0;
                                    } else {
                                        pw.println("Invalid data role: " + args[3]);
                                        Binder.restoreCallingIdentity(ident);
                                        return;
                                    }
                                }
                            }
                            if (this.mPortManager != null) {
                                this.mPortManager.setPortRoles(portId, powerRole, dataRole, pw);
                                pw.println();
                                this.mPortManager.dump(pw);
                            }
                            Binder.restoreCallingIdentity(ident);
                        }
                    }
                    try {
                        if (args.length == 3) {
                            if ("add-port".equals(args[0])) {
                                int supportedModes;
                                portId = args[1];
                                str = args[2];
                                if (str.equals("ufp")) {
                                    supportedModes = 2;
                                } else {
                                    if (str.equals("dfp")) {
                                        supportedModes = 1;
                                    } else {
                                        if (str.equals("dual")) {
                                            supportedModes = 3;
                                        } else {
                                            if (str.equals("none")) {
                                                supportedModes = 0;
                                            } else {
                                                pw.println("Invalid mode: " + args[2]);
                                                Binder.restoreCallingIdentity(ident);
                                                return;
                                            }
                                        }
                                    }
                                }
                                if (this.mPortManager != null) {
                                    this.mPortManager.addSimulatedPort(portId, supportedModes, pw);
                                    pw.println();
                                    this.mPortManager.dump(pw);
                                }
                                Binder.restoreCallingIdentity(ident);
                            }
                        }
                        if (args.length == 5) {
                            if ("connect-port".equals(args[0])) {
                                int mode;
                                portId = args[1];
                                boolean canChangeMode = args[2].endsWith("?");
                                str = canChangeMode ? removeLastChar(args[2]) : args[2];
                                if (str.equals("ufp")) {
                                    mode = 2;
                                } else {
                                    if (str.equals("dfp")) {
                                        mode = 1;
                                    } else {
                                        pw.println("Invalid mode: " + args[2]);
                                        Binder.restoreCallingIdentity(ident);
                                        return;
                                    }
                                }
                                boolean canChangePowerRole = args[3].endsWith("?");
                                str = canChangePowerRole ? removeLastChar(args[3]) : args[3];
                                if (str.equals("source")) {
                                    powerRole = 1;
                                } else {
                                    if (str.equals("sink")) {
                                        powerRole = 2;
                                    } else {
                                        pw.println("Invalid power role: " + args[3]);
                                        Binder.restoreCallingIdentity(ident);
                                        return;
                                    }
                                }
                                boolean canChangeDataRole = args[4].endsWith("?");
                                str = canChangeDataRole ? removeLastChar(args[4]) : args[4];
                                if (str.equals("host")) {
                                    dataRole = 1;
                                } else {
                                    if (str.equals("device")) {
                                        dataRole = 2;
                                    } else {
                                        pw.println("Invalid data role: " + args[4]);
                                        Binder.restoreCallingIdentity(ident);
                                        return;
                                    }
                                }
                                if (this.mPortManager != null) {
                                    this.mPortManager.connectSimulatedPort(portId, mode, canChangeMode, powerRole, canChangePowerRole, dataRole, canChangeDataRole, pw);
                                    pw.println();
                                    this.mPortManager.dump(pw);
                                }
                                Binder.restoreCallingIdentity(ident);
                            }
                        }
                        if (args.length == 2) {
                            if ("disconnect-port".equals(args[0])) {
                                portId = args[1];
                                if (this.mPortManager != null) {
                                    this.mPortManager.disconnectSimulatedPort(portId, pw);
                                    pw.println();
                                    this.mPortManager.dump(pw);
                                }
                                Binder.restoreCallingIdentity(ident);
                            }
                        }
                        if (args.length == 2) {
                            if ("remove-port".equals(args[0])) {
                                portId = args[1];
                                if (this.mPortManager != null) {
                                    this.mPortManager.removeSimulatedPort(portId, pw);
                                    pw.println();
                                    this.mPortManager.dump(pw);
                                }
                                Binder.restoreCallingIdentity(ident);
                            }
                        }
                        if (args.length == 1) {
                            if ("reset".equals(args[0])) {
                                if (this.mPortManager != null) {
                                    this.mPortManager.resetSimulation(pw);
                                    pw.println();
                                    this.mPortManager.dump(pw);
                                }
                                Binder.restoreCallingIdentity(ident);
                            }
                        }
                        if (args.length == 1) {
                            if ("ports".equals(args[0])) {
                                if (this.mPortManager != null) {
                                    this.mPortManager.dump(pw);
                                }
                                Binder.restoreCallingIdentity(ident);
                            }
                        }
                        pw.println("Dump current USB state or issue command:");
                        pw.println("  ports");
                        pw.println("  set-port-roles <id> <source|sink|no-power> <host|device|no-data>");
                        pw.println("  add-port <id> <ufp|dfp|dual|none>");
                        pw.println("  connect-port <id> <ufp|dfp><?> <source|sink><?> <host|device><?>");
                        pw.println("    (add ? suffix if mode, power role, or data role can be changed)");
                        pw.println("  disconnect-port <id>");
                        pw.println("  remove-port <id>");
                        pw.println("  reset");
                        pw.println();
                        pw.println("Example USB type C port role switch:");
                        pw.println("  dumpsys usb set-port-roles \"default\" source device");
                        pw.println();
                        pw.println("Example USB type C port simulation with full capabilities:");
                        pw.println("  dumpsys usb add-port \"matrix\" dual");
                        pw.println("  dumpsys usb connect-port \"matrix\" ufp? sink? device?");
                        pw.println("  dumpsys usb ports");
                        pw.println("  dumpsys usb disconnect-port \"matrix\"");
                        pw.println("  dumpsys usb remove-port \"matrix\"");
                        pw.println("  dumpsys usb reset");
                        pw.println();
                        pw.println("Example USB type C port where only power role can be changed:");
                        pw.println("  dumpsys usb add-port \"matrix\" dual");
                        pw.println("  dumpsys usb connect-port \"matrix\" dfp source? host");
                        pw.println("  dumpsys usb reset");
                        pw.println();
                        pw.println("Example USB OTG port where id pin determines function:");
                        pw.println("  dumpsys usb add-port \"matrix\" dual");
                        pw.println("  dumpsys usb connect-port \"matrix\" dfp source host");
                        pw.println("  dumpsys usb reset");
                        pw.println();
                        pw.println("Example USB device-only port:");
                        pw.println("  dumpsys usb add-port \"matrix\" ufp");
                        pw.println("  dumpsys usb connect-port \"matrix\" ufp sink device");
                        pw.println("  dumpsys usb reset");
                        Binder.restoreCallingIdentity(ident);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
            }
        }
        pw.println("USB Manager State:");
        pw.increaseIndent();
        if (this.mDeviceManager != null) {
            this.mDeviceManager.dump(pw);
        }
        if (this.mHostManager != null) {
            this.mHostManager.dump(pw);
        }
        if (this.mPortManager != null) {
            this.mPortManager.dump(pw);
        }
        this.mAlsaManager.dump(pw);
        synchronized (this.mLock) {
            int i = 0;
            while (true) {
                if (i >= this.mSettingsByUser.size()) {
                    break;
                }
                UsbSettingsManager settings = (UsbSettingsManager) this.mSettingsByUser.valueAt(i);
                pw.println("Settings for user " + this.mSettingsByUser.keyAt(i) + ":");
                pw.increaseIndent();
                settings.dump(pw);
                pw.decreaseIndent();
                i++;
            }
        }
        Binder.restoreCallingIdentity(ident);
    }

    private static final String removeLastChar(String value) {
        return value.substring(0, value.length() - 1);
    }
}
