package com.android.server.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbPort;
import android.hardware.usb.UsbPortStatus;
import android.os.Binder;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.ArraySet;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.dump.DualDumpOutputStream;
import com.android.server.SystemService;
import com.android.server.net.watchlist.WatchlistLoggingHandler;
import com.android.server.usb.HwUsbServiceFactory;
import com.android.server.utils.PriorityDump;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collections;

public class UsbService extends IUsbManager.Stub {
    private static final String TAG = "UsbService";
    private final UsbAlsaManager mAlsaManager;
    private final Context mContext;
    @GuardedBy("mLock")
    private int mCurrentUserId;
    /* access modifiers changed from: private */
    public UsbDeviceManager mDeviceManager;
    private UsbHostManager mHostManager;
    private final Object mLock = new Object();
    private UsbPortManager mPortManager;
    private final UsbSettingsManager mSettingsManager;
    private final UserManager mUserManager;

    public static class Lifecycle extends SystemService {
        private UsbService mUsbService;

        public Lifecycle(Context context) {
            super(context);
        }

        /* JADX WARNING: type inference failed for: r1v1, types: [com.android.server.usb.UsbService, android.os.IBinder] */
        public void onStart() {
            this.mUsbService = new UsbService(getContext());
            publishBinderService("usb", this.mUsbService);
        }

        public void onBootPhase(int phase) {
            if (phase == 550) {
                this.mUsbService.systemReady();
            } else if (phase == 1000) {
                this.mUsbService.bootCompleted();
            }
        }

        public void onSwitchUser(int newUserId) {
            this.mUsbService.onSwitchUser(newUserId);
        }

        public void onStopUser(int userHandle) {
            this.mUsbService.onStopUser(UserHandle.of(userHandle));
        }

        public void onUnlockUser(int userHandle) {
            this.mUsbService.onUnlockUser(userHandle);
        }
    }

    private UsbUserSettingsManager getSettingsForUser(int userIdInt) {
        return this.mSettingsManager.getSettingsForUser(userIdInt);
    }

    public UsbService(Context context) {
        this.mContext = context;
        this.mUserManager = (UserManager) context.getSystemService(UserManager.class);
        this.mSettingsManager = new UsbSettingsManager(context);
        this.mAlsaManager = new UsbAlsaManager(context);
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.usb.host")) {
            this.mHostManager = new UsbHostManager(context, this.mAlsaManager, this.mSettingsManager);
        }
        if (new File("/sys/class/android_usb").exists()) {
            HwUsbServiceFactory.IHwUsbDeviceManager iudm = HwUsbServiceFactory.getHuaweiUsbDeviceManager();
            if (iudm != null) {
                this.mDeviceManager = iudm.getInstance(context, this.mAlsaManager, this.mSettingsManager);
            } else {
                this.mDeviceManager = new UsbDeviceManager(context, this.mAlsaManager, this.mSettingsManager);
            }
        }
        if (!(this.mHostManager == null && this.mDeviceManager == null)) {
            this.mPortManager = new UsbPortManager(context);
        }
        onSwitchUser(0);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(intent.getAction()) && UsbService.this.mDeviceManager != null) {
                    UsbService.this.mDeviceManager.updateUserRestrictions();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.setPriority(1000);
        filter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        this.mContext.registerReceiver(receiver, filter, null, null);
    }

    /* access modifiers changed from: private */
    public void onSwitchUser(int newUserId) {
        synchronized (this.mLock) {
            this.mCurrentUserId = newUserId;
            UsbProfileGroupSettingsManager settings = this.mSettingsManager.getSettingsForProfileGroup(UserHandle.of(newUserId));
            if (this.mHostManager != null) {
                this.mHostManager.setCurrentUserSettings(settings);
            }
            if (this.mDeviceManager != null) {
                this.mDeviceManager.setCurrentUser(newUserId, settings);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onStopUser(UserHandle stoppedUser) {
        this.mSettingsManager.remove(stoppedUser);
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

    public void onUnlockUser(int user) {
        if (this.mDeviceManager != null) {
            this.mDeviceManager.onUnlockUser(user);
        }
    }

    public void getDeviceList(Bundle devices) {
        if (this.mHostManager != null) {
            this.mHostManager.getDeviceList(devices);
        }
    }

    @GuardedBy("mLock")
    private boolean isCallerInCurrentUserProfileGroupLocked() {
        int userIdInt = UserHandle.getCallingUserId();
        long ident = clearCallingIdentity();
        try {
            return this.mUserManager.isSameProfileGroup(userIdInt, this.mCurrentUserId);
        } finally {
            restoreCallingIdentity(ident);
        }
    }

    public ParcelFileDescriptor openDevice(String deviceName, String packageName) {
        ParcelFileDescriptor fd = null;
        if (this.mHostManager != null) {
            synchronized (this.mLock) {
                if (deviceName != null) {
                    try {
                        int userIdInt = UserHandle.getCallingUserId();
                        if (isCallerInCurrentUserProfileGroupLocked()) {
                            fd = this.mHostManager.openDevice(deviceName, getSettingsForUser(userIdInt), packageName, Binder.getCallingUid());
                        } else {
                            Slog.w(TAG, "Cannot open " + deviceName + " for user " + userIdInt + " as user is not active.");
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                }
            }
        }
        return fd;
    }

    public UsbAccessory getCurrentAccessory() {
        if (this.mDeviceManager != null) {
            return this.mDeviceManager.getCurrentAccessory();
        }
        return null;
    }

    public ParcelFileDescriptor openAccessory(UsbAccessory accessory) {
        if (this.mDeviceManager != null) {
            int userIdInt = UserHandle.getCallingUserId();
            synchronized (this.mLock) {
                if (isCallerInCurrentUserProfileGroupLocked()) {
                    ParcelFileDescriptor openAccessory = this.mDeviceManager.openAccessory(accessory, getSettingsForUser(userIdInt));
                    return openAccessory;
                }
                Slog.w(TAG, "Cannot open " + accessory + " for user " + userIdInt + " as user is not active.");
            }
        }
        return null;
    }

    public ParcelFileDescriptor getControlFd(long function) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_MTP", null);
        return this.mDeviceManager.getControlFd(function);
    }

    public void setDevicePackage(UsbDevice device, String packageName, int userId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        UserHandle user = UserHandle.of(userId);
        this.mSettingsManager.getSettingsForProfileGroup(user).setDevicePackage((UsbDevice) Preconditions.checkNotNull(device), packageName, user);
    }

    public void setAccessoryPackage(UsbAccessory accessory, String packageName, int userId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        UserHandle user = UserHandle.of(userId);
        this.mSettingsManager.getSettingsForProfileGroup(user).setAccessoryPackage((UsbAccessory) Preconditions.checkNotNull(accessory), packageName, user);
    }

    public boolean hasDevicePermission(UsbDevice device, String packageName) {
        return getSettingsForUser(UserHandle.getCallingUserId()).hasPermission(device, packageName, Binder.getCallingUid());
    }

    public boolean hasAccessoryPermission(UsbAccessory accessory) {
        return getSettingsForUser(UserHandle.getCallingUserId()).hasPermission(accessory);
    }

    public void requestDevicePermission(UsbDevice device, String packageName, PendingIntent pi) {
        getSettingsForUser(UserHandle.getCallingUserId()).requestPermission(device, packageName, pi, Binder.getCallingUid());
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
        UserHandle user = UserHandle.of(userId);
        return this.mSettingsManager.getSettingsForProfileGroup(user).hasDefaults((String) Preconditions.checkStringNotEmpty(packageName), user);
    }

    public void clearDefaults(String packageName, int userId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        UserHandle user = UserHandle.of(userId);
        this.mSettingsManager.getSettingsForProfileGroup(user).clearDefaults((String) Preconditions.checkStringNotEmpty(packageName), user);
    }

    public void setCurrentFunctions(long functions) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        Preconditions.checkArgument(UsbManager.areSettableFunctions(functions));
        Preconditions.checkState(this.mDeviceManager != null);
        this.mDeviceManager.setCurrentFunctions(functions);
    }

    public void setCurrentFunction(String functions, boolean usbDataUnlocked) {
        setCurrentFunctions(UsbManager.usbFunctionsFromString(functions));
    }

    public boolean isFunctionEnabled(String function) {
        return (getCurrentFunctions() & UsbManager.usbFunctionsFromString(function)) != 0;
    }

    public long getCurrentFunctions() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        Preconditions.checkState(this.mDeviceManager != null);
        return this.mDeviceManager.getCurrentFunctions();
    }

    public void setScreenUnlockedFunctions(long functions) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        Preconditions.checkArgument(UsbManager.areSettableFunctions(functions));
        Preconditions.checkState(this.mDeviceManager != null);
        this.mDeviceManager.setScreenUnlockedFunctions(functions);
    }

    public long getScreenUnlockedFunctions() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        Preconditions.checkState(this.mDeviceManager != null);
        return this.mDeviceManager.getScreenUnlockedFunctions();
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
            return usbPortArr;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public UsbPortStatus getPortStatus(String portId) {
        Preconditions.checkNotNull(portId, "portId must not be null");
        UsbPortStatus usbPortStatus = null;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        long ident = Binder.clearCallingIdentity();
        try {
            if (this.mPortManager != null) {
                usbPortStatus = this.mPortManager.getPortStatus(portId);
            }
            return usbPortStatus;
        } finally {
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
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void setUsbDeviceConnectionHandler(ComponentName usbDeviceConnectionHandler) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        synchronized (this.mLock) {
            if (this.mCurrentUserId != UserHandle.getCallingUserId()) {
                throw new IllegalArgumentException("Only the current user can register a usb connection handler");
            } else if (this.mHostManager != null) {
                this.mHostManager.setUsbDeviceConnectionHandler(usbDeviceConnectionHandler);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:148:0x0233  */
    /* JADX WARNING: Removed duplicated region for block: B:151:0x0238  */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x023a  */
    /* JADX WARNING: Removed duplicated region for block: B:158:0x024a A[SYNTHETIC, Splitter:B:158:0x024a] */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x0251 A[SYNTHETIC, Splitter:B:160:0x0251] */
    /* JADX WARNING: Removed duplicated region for block: B:164:0x0259  */
    /* JADX WARNING: Removed duplicated region for block: B:171:0x026a  */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x027a  */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x027f  */
    /* JADX WARNING: Removed duplicated region for block: B:182:0x0281  */
    /* JADX WARNING: Removed duplicated region for block: B:188:0x0292 A[SYNTHETIC, Splitter:B:188:0x0292] */
    /* JADX WARNING: Removed duplicated region for block: B:190:0x0299 A[SYNTHETIC, Splitter:B:190:0x0299] */
    /* JADX WARNING: Removed duplicated region for block: B:195:0x02a4  */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x02b5  */
    /* JADX WARNING: Removed duplicated region for block: B:208:0x02c2  */
    /* JADX WARNING: Removed duplicated region for block: B:211:0x02c7  */
    /* JADX WARNING: Removed duplicated region for block: B:212:0x02c9  */
    /* JADX WARNING: Removed duplicated region for block: B:217:0x02d2 A[Catch:{ all -> 0x0302 }] */
    /* JADX WARNING: Removed duplicated region for block: B:223:0x02fc  */
    /* JADX WARNING: Removed duplicated region for block: B:281:0x04f9 A[Catch:{ all -> 0x04f1, all -> 0x0506 }] */
    /* JADX WARNING: Removed duplicated region for block: B:287:0x050a A[Catch:{ all -> 0x0575 }] */
    /* JADX WARNING: Removed duplicated region for block: B:290:0x0521 A[Catch:{ all -> 0x0575 }] */
    /* JADX WARNING: Removed duplicated region for block: B:293:0x0536 A[Catch:{ all -> 0x0575 }] */
    /* JADX WARNING: Removed duplicated region for block: B:296:0x0546 A[Catch:{ all -> 0x0575 }] */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x009e A[Catch:{ all -> 0x013d }] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00a2 A[Catch:{ all -> 0x013d }] */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00a4 A[Catch:{ all -> 0x013d }] */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00a6 A[Catch:{ all -> 0x013d }] */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00b1 A[Catch:{ all -> 0x013d }] */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00d3 A[Catch:{ all -> 0x013d }] */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x00e0 A[Catch:{ all -> 0x013d }] */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00e3 A[Catch:{ all -> 0x013d }] */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x00e5 A[Catch:{ all -> 0x013d }] */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x00e7 A[Catch:{ all -> 0x013d }] */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x00ee A[Catch:{ all -> 0x013d }] */
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        long ident;
        FileDescriptor fileDescriptor;
        DualDumpOutputStream dump;
        boolean z;
        int mode;
        int hashCode;
        boolean z2;
        int powerRole;
        int hashCode2;
        int dataRole;
        long ident2;
        int supportedModes;
        boolean z3;
        int powerRole2;
        int hashCode3;
        int dataRole2;
        FileDescriptor fileDescriptor2 = fd;
        PrintWriter printWriter = writer;
        String[] strArr = args;
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, printWriter)) {
            IndentingPrintWriter pw = new IndentingPrintWriter(printWriter, "  ");
            long ident3 = Binder.clearCallingIdentity();
            try {
                ArraySet<String> argsSet = new ArraySet<>();
                Collections.addAll(argsSet, strArr);
                boolean dumpAsProto = false;
                if (argsSet.contains(PriorityDump.PROTO_ARG)) {
                    dumpAsProto = true;
                }
                boolean dumpAsProto2 = dumpAsProto;
                if (strArr != null) {
                    try {
                        if (strArr.length != 0 && !strArr[0].equals("-a")) {
                            if (dumpAsProto2) {
                                ident = ident3;
                                if (!dumpAsProto2) {
                                    fileDescriptor = fd;
                                    try {
                                        new DualDumpOutputStream(new ProtoOutputStream(fileDescriptor));
                                    } catch (Throwable th) {
                                        th = th;
                                        Binder.restoreCallingIdentity(ident);
                                        throw th;
                                    }
                                } else {
                                    fileDescriptor = fd;
                                    pw.println("USB MANAGER STATE (dumpsys usb):");
                                    dump = new DualDumpOutputStream(new IndentingPrintWriter(pw, "  "));
                                }
                                if (this.mDeviceManager != null) {
                                    this.mDeviceManager.dump(dump, "device_manager", 1146756268033L);
                                    this.mDeviceManager.dump(fileDescriptor, pw, strArr);
                                }
                                if (this.mHostManager != null) {
                                    this.mHostManager.dump(dump, "host_manager", 1146756268034L);
                                }
                                if (this.mPortManager != null) {
                                    this.mPortManager.dump(dump, "port_manager", 1146756268035L);
                                }
                                this.mAlsaManager.dump(dump, "alsa_manager", 1146756268036L);
                                this.mSettingsManager.dump(dump, "settings_manager", 1146756268037L);
                                dump.flush();
                                Binder.restoreCallingIdentity(ident);
                                return;
                            }
                            char c = 65535;
                            if ("set-port-roles".equals(strArr[0])) {
                                try {
                                    if (strArr.length == 4) {
                                        String portId = strArr[1];
                                        String str = strArr[2];
                                        int hashCode4 = str.hashCode();
                                        if (hashCode4 != -896505829) {
                                            if (hashCode4 != -440560135) {
                                                if (hashCode4 == 3530387) {
                                                    if (str.equals("sink")) {
                                                        z3 = true;
                                                        switch (z3) {
                                                            case false:
                                                                powerRole2 = 1;
                                                                break;
                                                            case true:
                                                                powerRole2 = 2;
                                                                break;
                                                            case true:
                                                                powerRole2 = 0;
                                                                break;
                                                            default:
                                                                pw.println("Invalid power role: " + strArr[2]);
                                                                Binder.restoreCallingIdentity(ident3);
                                                                return;
                                                        }
                                                        String str2 = strArr[3];
                                                        hashCode3 = str2.hashCode();
                                                        if (hashCode3 == -1335157162) {
                                                            if (hashCode3 != 3208616) {
                                                                if (hashCode3 == 2063627318) {
                                                                    if (str2.equals("no-data")) {
                                                                        c = 2;
                                                                    }
                                                                }
                                                            } else if (str2.equals(WatchlistLoggingHandler.WatchlistEventKeys.HOST)) {
                                                                c = 0;
                                                            }
                                                        } else if (str2.equals("device")) {
                                                            c = 1;
                                                        }
                                                        switch (c) {
                                                            case 0:
                                                                dataRole2 = 1;
                                                                break;
                                                            case 1:
                                                                dataRole2 = 2;
                                                                break;
                                                            case 2:
                                                                dataRole2 = 0;
                                                                break;
                                                            default:
                                                                pw.println("Invalid data role: " + strArr[3]);
                                                                Binder.restoreCallingIdentity(ident3);
                                                                return;
                                                        }
                                                        if (this.mPortManager != null) {
                                                            this.mPortManager.setPortRoles(portId, powerRole2, dataRole2, pw);
                                                            pw.println();
                                                            this.mPortManager.dump(new DualDumpOutputStream(new IndentingPrintWriter(pw, "  ")), BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, 0);
                                                        }
                                                        FileDescriptor fileDescriptor3 = fileDescriptor2;
                                                        ident = ident3;
                                                        Binder.restoreCallingIdentity(ident);
                                                        return;
                                                    }
                                                }
                                            } else if (str.equals("no-power")) {
                                                z3 = true;
                                                switch (z3) {
                                                    case false:
                                                        break;
                                                    case true:
                                                        break;
                                                    case true:
                                                        break;
                                                }
                                                String str22 = strArr[3];
                                                hashCode3 = str22.hashCode();
                                                if (hashCode3 == -1335157162) {
                                                }
                                                switch (c) {
                                                    case 0:
                                                        break;
                                                    case 1:
                                                        break;
                                                    case 2:
                                                        break;
                                                }
                                                if (this.mPortManager != null) {
                                                }
                                                FileDescriptor fileDescriptor32 = fileDescriptor2;
                                                ident = ident3;
                                                Binder.restoreCallingIdentity(ident);
                                                return;
                                            }
                                        } else if (str.equals("source")) {
                                            z3 = false;
                                            switch (z3) {
                                                case false:
                                                    break;
                                                case true:
                                                    break;
                                                case true:
                                                    break;
                                            }
                                            String str222 = strArr[3];
                                            hashCode3 = str222.hashCode();
                                            if (hashCode3 == -1335157162) {
                                            }
                                            switch (c) {
                                                case 0:
                                                    break;
                                                case 1:
                                                    break;
                                                case 2:
                                                    break;
                                            }
                                            if (this.mPortManager != null) {
                                            }
                                            FileDescriptor fileDescriptor322 = fileDescriptor2;
                                            ident = ident3;
                                            Binder.restoreCallingIdentity(ident);
                                            return;
                                        }
                                        z3 = true;
                                        switch (z3) {
                                            case false:
                                                break;
                                            case true:
                                                break;
                                            case true:
                                                break;
                                        }
                                        String str2222 = strArr[3];
                                        hashCode3 = str2222.hashCode();
                                        if (hashCode3 == -1335157162) {
                                        }
                                        switch (c) {
                                            case 0:
                                                break;
                                            case 1:
                                                break;
                                            case 2:
                                                break;
                                        }
                                        if (this.mPortManager != null) {
                                        }
                                        FileDescriptor fileDescriptor3222 = fileDescriptor2;
                                        ident = ident3;
                                        Binder.restoreCallingIdentity(ident);
                                        return;
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    FileDescriptor fileDescriptor4 = fileDescriptor2;
                                    ident = ident3;
                                    Binder.restoreCallingIdentity(ident);
                                    throw th;
                                }
                            }
                            if ("add-port".equals(strArr[0])) {
                                if (strArr.length == 3) {
                                    String portId2 = strArr[1];
                                    String str3 = strArr[2];
                                    int hashCode5 = str3.hashCode();
                                    if (hashCode5 != 99374) {
                                        if (hashCode5 != 115711) {
                                            if (hashCode5 != 3094652) {
                                                if (hashCode5 == 3387192) {
                                                    if (str3.equals("none")) {
                                                        c = 3;
                                                    }
                                                }
                                            } else if (str3.equals("dual")) {
                                                c = 2;
                                            }
                                        } else if (str3.equals("ufp")) {
                                            c = 0;
                                        }
                                    } else if (str3.equals("dfp")) {
                                        c = 1;
                                    }
                                    switch (c) {
                                        case 0:
                                            supportedModes = 1;
                                            break;
                                        case 1:
                                            supportedModes = 2;
                                            break;
                                        case 2:
                                            supportedModes = 3;
                                            break;
                                        case 3:
                                            supportedModes = 0;
                                            break;
                                        default:
                                            pw.println("Invalid mode: " + strArr[2]);
                                            Binder.restoreCallingIdentity(ident3);
                                            return;
                                    }
                                    if (this.mPortManager != null) {
                                        this.mPortManager.addSimulatedPort(portId2, supportedModes, pw);
                                        pw.println();
                                        this.mPortManager.dump(new DualDumpOutputStream(new IndentingPrintWriter(pw, "  ")), BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, 0);
                                    }
                                    FileDescriptor fileDescriptor32222 = fileDescriptor2;
                                    ident = ident3;
                                    Binder.restoreCallingIdentity(ident);
                                    return;
                                }
                            }
                            if (!"connect-port".equals(strArr[0]) || strArr.length != 5) {
                                ident = ident3;
                                if ("disconnect-port".equals(strArr[0]) && strArr.length == 2) {
                                    String portId3 = strArr[1];
                                    if (this.mPortManager != null) {
                                        this.mPortManager.disconnectSimulatedPort(portId3, pw);
                                        pw.println();
                                        this.mPortManager.dump(new DualDumpOutputStream(new IndentingPrintWriter(pw, "  ")), BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, 0);
                                    }
                                } else if ("remove-port".equals(strArr[0]) && strArr.length == 2) {
                                    String portId4 = strArr[1];
                                    if (this.mPortManager != null) {
                                        this.mPortManager.removeSimulatedPort(portId4, pw);
                                        pw.println();
                                        this.mPortManager.dump(new DualDumpOutputStream(new IndentingPrintWriter(pw, "  ")), BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, 0);
                                    }
                                } else if (!"reset".equals(strArr[0]) || strArr.length != 1) {
                                    if (!"ports".equals(strArr[0]) || strArr.length != 1) {
                                        if ("dump-descriptors".equals(strArr[0])) {
                                            this.mHostManager.dumpDescriptors(pw, strArr);
                                        } else {
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
                                            pw.println();
                                            pw.println("Example USB device descriptors:");
                                            pw.println("  dumpsys usb dump-descriptors -dump-short");
                                            pw.println("  dumpsys usb dump-descriptors -dump-tree");
                                            pw.println("  dumpsys usb dump-descriptors -dump-list");
                                            pw.println("  dumpsys usb dump-descriptors -dump-raw");
                                        }
                                    } else if (this.mPortManager != null) {
                                        this.mPortManager.dump(new DualDumpOutputStream(new IndentingPrintWriter(pw, "  ")), BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, 0);
                                    }
                                } else if (this.mPortManager != null) {
                                    this.mPortManager.resetSimulation(pw);
                                    pw.println();
                                    this.mPortManager.dump(new DualDumpOutputStream(new IndentingPrintWriter(pw, "  ")), BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, 0);
                                }
                            } else {
                                String portId5 = strArr[1];
                                boolean canChangeMode = strArr[2].endsWith("?");
                                String removeLastChar = canChangeMode ? removeLastChar(strArr[2]) : strArr[2];
                                int hashCode6 = removeLastChar.hashCode();
                                if (hashCode6 != 99374) {
                                    if (hashCode6 == 115711) {
                                        if (removeLastChar.equals("ufp")) {
                                            z = false;
                                            switch (z) {
                                                case false:
                                                    mode = 1;
                                                    break;
                                                case true:
                                                    mode = 2;
                                                    break;
                                                default:
                                                    ident = ident3;
                                                    pw.println("Invalid mode: " + strArr[2]);
                                                    Binder.restoreCallingIdentity(ident);
                                                    return;
                                            }
                                            int mode2 = mode;
                                            boolean canChangePowerRole = strArr[3].endsWith("?");
                                            String removeLastChar2 = canChangePowerRole ? removeLastChar(strArr[3]) : strArr[3];
                                            hashCode = removeLastChar2.hashCode();
                                            if (hashCode != -896505829) {
                                                if (hashCode == 3530387) {
                                                    if (removeLastChar2.equals("sink")) {
                                                        z2 = true;
                                                        switch (z2) {
                                                            case false:
                                                                powerRole = 1;
                                                                break;
                                                            case true:
                                                                powerRole = 2;
                                                                break;
                                                            default:
                                                                ident = ident3;
                                                                pw.println("Invalid power role: " + strArr[3]);
                                                                Binder.restoreCallingIdentity(ident);
                                                                return;
                                                        }
                                                        int powerRole3 = powerRole;
                                                        boolean canChangeDataRole = strArr[4].endsWith("?");
                                                        String removeLastChar3 = canChangeDataRole ? removeLastChar(strArr[4]) : strArr[4];
                                                        hashCode2 = removeLastChar3.hashCode();
                                                        if (hashCode2 != -1335157162) {
                                                            if (hashCode2 == 3208616) {
                                                                if (removeLastChar3.equals(WatchlistLoggingHandler.WatchlistEventKeys.HOST)) {
                                                                    c = 0;
                                                                }
                                                            }
                                                        } else if (removeLastChar3.equals("device")) {
                                                            c = 1;
                                                        }
                                                        switch (c) {
                                                            case 0:
                                                                dataRole = 1;
                                                                break;
                                                            case 1:
                                                                dataRole = 2;
                                                                break;
                                                            default:
                                                                long ident4 = ident3;
                                                                try {
                                                                    pw.println("Invalid data role: " + strArr[4]);
                                                                    Binder.restoreCallingIdentity(ident4);
                                                                    return;
                                                                } catch (Throwable th3) {
                                                                    th = th3;
                                                                    ident = ident4;
                                                                    FileDescriptor fileDescriptor5 = fd;
                                                                    break;
                                                                }
                                                        }
                                                        int dataRole3 = dataRole;
                                                        if (this.mPortManager != null) {
                                                            ident2 = ident3;
                                                            try {
                                                                this.mPortManager.connectSimulatedPort(portId5, mode2, canChangeMode, powerRole3, canChangePowerRole, dataRole3, canChangeDataRole, pw);
                                                                pw.println();
                                                                this.mPortManager.dump(new DualDumpOutputStream(new IndentingPrintWriter(pw, "  ")), BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, 0);
                                                            } catch (Throwable th4) {
                                                                th = th4;
                                                                ident = ident2;
                                                            }
                                                        } else {
                                                            ident2 = ident3;
                                                        }
                                                        ident = ident2;
                                                    }
                                                }
                                            } else if (removeLastChar2.equals("source")) {
                                                z2 = false;
                                                switch (z2) {
                                                    case false:
                                                        break;
                                                    case true:
                                                        break;
                                                }
                                                int powerRole32 = powerRole;
                                                boolean canChangeDataRole2 = strArr[4].endsWith("?");
                                                if (canChangeDataRole2) {
                                                }
                                                hashCode2 = removeLastChar3.hashCode();
                                                if (hashCode2 != -1335157162) {
                                                }
                                                switch (c) {
                                                    case 0:
                                                        break;
                                                    case 1:
                                                        break;
                                                }
                                                int dataRole32 = dataRole;
                                                if (this.mPortManager != null) {
                                                }
                                                ident = ident2;
                                            }
                                            z2 = true;
                                            switch (z2) {
                                                case false:
                                                    break;
                                                case true:
                                                    break;
                                            }
                                            int powerRole322 = powerRole;
                                            boolean canChangeDataRole22 = strArr[4].endsWith("?");
                                            if (canChangeDataRole22) {
                                            }
                                            hashCode2 = removeLastChar3.hashCode();
                                            if (hashCode2 != -1335157162) {
                                            }
                                            switch (c) {
                                                case 0:
                                                    break;
                                                case 1:
                                                    break;
                                            }
                                            int dataRole322 = dataRole;
                                            if (this.mPortManager != null) {
                                            }
                                            ident = ident2;
                                        }
                                    }
                                } else if (removeLastChar.equals("dfp")) {
                                    z = true;
                                    switch (z) {
                                        case false:
                                            break;
                                        case true:
                                            break;
                                    }
                                    int mode22 = mode;
                                    boolean canChangePowerRole2 = strArr[3].endsWith("?");
                                    if (canChangePowerRole2) {
                                    }
                                    hashCode = removeLastChar2.hashCode();
                                    if (hashCode != -896505829) {
                                    }
                                    z2 = true;
                                    switch (z2) {
                                        case false:
                                            break;
                                        case true:
                                            break;
                                    }
                                    int powerRole3222 = powerRole;
                                    boolean canChangeDataRole222 = strArr[4].endsWith("?");
                                    if (canChangeDataRole222) {
                                    }
                                    hashCode2 = removeLastChar3.hashCode();
                                    if (hashCode2 != -1335157162) {
                                    }
                                    switch (c) {
                                        case 0:
                                            break;
                                        case 1:
                                            break;
                                    }
                                    int dataRole3222 = dataRole;
                                    if (this.mPortManager != null) {
                                    }
                                    ident = ident2;
                                }
                                z = true;
                                switch (z) {
                                    case false:
                                        break;
                                    case true:
                                        break;
                                }
                                int mode222 = mode;
                                boolean canChangePowerRole22 = strArr[3].endsWith("?");
                                if (canChangePowerRole22) {
                                }
                                hashCode = removeLastChar2.hashCode();
                                if (hashCode != -896505829) {
                                }
                                z2 = true;
                                switch (z2) {
                                    case false:
                                        break;
                                    case true:
                                        break;
                                }
                                int powerRole32222 = powerRole;
                                boolean canChangeDataRole2222 = strArr[4].endsWith("?");
                                if (canChangeDataRole2222) {
                                }
                                hashCode2 = removeLastChar3.hashCode();
                                if (hashCode2 != -1335157162) {
                                }
                                switch (c) {
                                    case 0:
                                        break;
                                    case 1:
                                        break;
                                }
                                int dataRole32222 = dataRole;
                                try {
                                    if (this.mPortManager != null) {
                                    }
                                    ident = ident2;
                                } catch (Throwable th5) {
                                    th = th5;
                                    ident = ident3;
                                    FileDescriptor fileDescriptor6 = fd;
                                    Binder.restoreCallingIdentity(ident);
                                    throw th;
                                }
                            }
                            FileDescriptor fileDescriptor7 = fd;
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                    }
                }
                ident = ident3;
                if (!dumpAsProto2) {
                }
                if (this.mDeviceManager != null) {
                }
                if (this.mHostManager != null) {
                }
                if (this.mPortManager != null) {
                }
                this.mAlsaManager.dump(dump, "alsa_manager", 1146756268036L);
                this.mSettingsManager.dump(dump, "settings_manager", 1146756268037L);
                dump.flush();
                Binder.restoreCallingIdentity(ident);
                return;
            } catch (Throwable th7) {
                th = th7;
                FileDescriptor fileDescriptor8 = fileDescriptor2;
                ident = ident3;
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } else {
            return;
        }
        FileDescriptor fileDescriptor9 = fd;
        Binder.restoreCallingIdentity(ident);
        throw th;
    }

    private static String removeLastChar(String value) {
        return value.substring(0, value.length() - 1);
    }
}
