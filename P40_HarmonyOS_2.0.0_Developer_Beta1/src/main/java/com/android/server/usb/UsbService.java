package com.android.server.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.ParcelableUsbPort;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UsbService extends IUsbManager.Stub {
    private static final String TAG = "UsbService";
    private final UsbAlsaManager mAlsaManager;
    private final Context mContext;
    @GuardedBy({"mLock"})
    private int mCurrentUserId;
    private UsbDeviceManager mDeviceManager;
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

        /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.usb.UsbService$Lifecycle */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r0v1, types: [com.android.server.usb.UsbService, android.os.IBinder] */
        /* JADX WARNING: Unknown variable types count: 1 */
        @Override // com.android.server.SystemService
        public void onStart() {
            this.mUsbService = new UsbService(getContext());
            publishBinderService("usb", this.mUsbService);
        }

        @Override // com.android.server.SystemService
        public void onBootPhase(int phase) {
            if (phase == 550) {
                this.mUsbService.systemReady();
            } else if (phase == 1000) {
                this.mUsbService.bootCompleted();
            }
        }

        @Override // com.android.server.SystemService
        public void onSwitchUser(int newUserId) {
            this.mUsbService.onSwitchUser(newUserId);
        }

        @Override // com.android.server.SystemService
        public void onStopUser(int userHandle) {
            this.mUsbService.onStopUser(UserHandle.of(userHandle));
        }

        @Override // com.android.server.SystemService
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
            /* class com.android.server.usb.UsbService.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
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
    /* access modifiers changed from: public */
    private void onSwitchUser(int newUserId) {
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
    /* access modifiers changed from: public */
    private void onStopUser(UserHandle stoppedUser) {
        this.mSettingsManager.remove(stoppedUser);
    }

    public void systemReady() {
        this.mAlsaManager.systemReady();
        UsbDeviceManager usbDeviceManager = this.mDeviceManager;
        if (usbDeviceManager != null) {
            usbDeviceManager.systemReady();
        }
        UsbHostManager usbHostManager = this.mHostManager;
        if (usbHostManager != null) {
            usbHostManager.systemReady();
        }
        UsbPortManager usbPortManager = this.mPortManager;
        if (usbPortManager != null) {
            usbPortManager.systemReady();
        }
    }

    public void bootCompleted() {
        UsbDeviceManager usbDeviceManager = this.mDeviceManager;
        if (usbDeviceManager != null) {
            usbDeviceManager.bootCompleted();
        }
    }

    public void onUnlockUser(int user) {
        UsbDeviceManager usbDeviceManager = this.mDeviceManager;
        if (usbDeviceManager != null) {
            usbDeviceManager.onUnlockUser(user);
        }
    }

    public void getDeviceList(Bundle devices) {
        UsbHostManager usbHostManager = this.mHostManager;
        if (usbHostManager != null) {
            usbHostManager.getDeviceList(devices);
        }
    }

    public ParcelFileDescriptor openDevice(String deviceName, String packageName) {
        ParcelFileDescriptor fd = null;
        if (!(this.mHostManager == null || deviceName == null)) {
            int uid = Binder.getCallingUid();
            int pid = Binder.getCallingPid();
            int user = UserHandle.getUserId(uid);
            long ident = clearCallingIdentity();
            try {
                synchronized (this.mLock) {
                    if (this.mUserManager.isSameProfileGroup(user, this.mCurrentUserId)) {
                        fd = this.mHostManager.openDevice(deviceName, getSettingsForUser(user), packageName, pid, uid);
                    } else {
                        Slog.w(TAG, "Cannot open " + deviceName + " for user " + user + " as user is not active.");
                    }
                }
            } finally {
                restoreCallingIdentity(ident);
            }
        }
        return fd;
    }

    public UsbAccessory getCurrentAccessory() {
        UsbDeviceManager usbDeviceManager = this.mDeviceManager;
        if (usbDeviceManager != null) {
            return usbDeviceManager.getCurrentAccessory();
        }
        return null;
    }

    public ParcelFileDescriptor openAccessory(UsbAccessory accessory) {
        if (this.mDeviceManager == null) {
            return null;
        }
        int uid = Binder.getCallingUid();
        int user = UserHandle.getUserId(uid);
        long ident = clearCallingIdentity();
        try {
            synchronized (this.mLock) {
                if (this.mUserManager.isSameProfileGroup(user, this.mCurrentUserId)) {
                    return this.mDeviceManager.openAccessory(accessory, getSettingsForUser(user), uid);
                }
                Slog.w(TAG, "Cannot open " + accessory + " for user " + user + " as user is not active.");
                restoreCallingIdentity(ident);
                return null;
            }
        } finally {
            restoreCallingIdentity(ident);
        }
    }

    public ParcelFileDescriptor getControlFd(long function) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_MTP", null);
        return this.mDeviceManager.getControlFd(function);
    }

    public void setDevicePackage(UsbDevice device, String packageName, int userId) {
        UsbDevice device2 = (UsbDevice) Preconditions.checkNotNull(device);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        UserHandle user = UserHandle.of(userId);
        long token = Binder.clearCallingIdentity();
        try {
            this.mSettingsManager.getSettingsForProfileGroup(user).setDevicePackage(device2, packageName, user);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void setAccessoryPackage(UsbAccessory accessory, String packageName, int userId) {
        UsbAccessory accessory2 = (UsbAccessory) Preconditions.checkNotNull(accessory);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        UserHandle user = UserHandle.of(userId);
        long token = Binder.clearCallingIdentity();
        try {
            this.mSettingsManager.getSettingsForProfileGroup(user).setAccessoryPackage(accessory2, packageName, user);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean hasDevicePermission(UsbDevice device, String packageName) {
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        int userId = UserHandle.getUserId(uid);
        long token = Binder.clearCallingIdentity();
        try {
            return getSettingsForUser(userId).hasPermission(device, packageName, pid, uid);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean hasAccessoryPermission(UsbAccessory accessory) {
        int uid = Binder.getCallingUid();
        int userId = UserHandle.getUserId(uid);
        long token = Binder.clearCallingIdentity();
        try {
            return getSettingsForUser(userId).hasPermission(accessory, uid);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void requestDevicePermission(UsbDevice device, String packageName, PendingIntent pi) {
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        int userId = UserHandle.getUserId(uid);
        long token = Binder.clearCallingIdentity();
        try {
            getSettingsForUser(userId).requestPermission(device, packageName, pi, pid, uid);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void requestAccessoryPermission(UsbAccessory accessory, String packageName, PendingIntent pi) {
        int uid = Binder.getCallingUid();
        int userId = UserHandle.getUserId(uid);
        long token = Binder.clearCallingIdentity();
        try {
            getSettingsForUser(userId).requestPermission(accessory, packageName, pi, uid);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void grantDevicePermission(UsbDevice device, int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        int userId = UserHandle.getUserId(uid);
        long token = Binder.clearCallingIdentity();
        try {
            getSettingsForUser(userId).grantDevicePermission(device, uid);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void grantAccessoryPermission(UsbAccessory accessory, int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        int userId = UserHandle.getUserId(uid);
        long token = Binder.clearCallingIdentity();
        try {
            getSettingsForUser(userId).grantAccessoryPermission(accessory, uid);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean hasDefaults(String packageName, int userId) {
        String packageName2 = (String) Preconditions.checkStringNotEmpty(packageName);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        UserHandle user = UserHandle.of(userId);
        long token = Binder.clearCallingIdentity();
        try {
            return this.mSettingsManager.getSettingsForProfileGroup(user).hasDefaults(packageName2, user);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void clearDefaults(String packageName, int userId) {
        String packageName2 = (String) Preconditions.checkStringNotEmpty(packageName);
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        UserHandle user = UserHandle.of(userId);
        long token = Binder.clearCallingIdentity();
        try {
            this.mSettingsManager.getSettingsForProfileGroup(user).clearDefaults(packageName2, user);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
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

    public List<ParcelableUsbPort> getPorts() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        long ident = Binder.clearCallingIdentity();
        try {
            if (this.mPortManager == null) {
                return null;
            }
            UsbPort[] ports = this.mPortManager.getPorts();
            ArrayList<ParcelableUsbPort> parcelablePorts = new ArrayList<>();
            for (UsbPort usbPort : ports) {
                parcelablePorts.add(ParcelableUsbPort.of(usbPort));
            }
            Binder.restoreCallingIdentity(ident);
            return parcelablePorts;
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

    public void enableContaminantDetection(String portId, boolean enable) {
        Preconditions.checkNotNull(portId, "portId must not be null");
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        long ident = Binder.clearCallingIdentity();
        try {
            if (this.mPortManager != null) {
                this.mPortManager.enableContaminantDetection(portId, enable, null);
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

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Removed duplicated region for block: B:128:0x0220  */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x023e  */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x024b  */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x0254  */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x0260  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x0271  */
    /* JADX WARNING: Removed duplicated region for block: B:149:0x027c  */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x029d  */
    /* JADX WARNING: Removed duplicated region for block: B:156:0x02a9  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x02b1  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x02be  */
    /* JADX WARNING: Removed duplicated region for block: B:165:0x02cf  */
    /* JADX WARNING: Removed duplicated region for block: B:170:0x02dc  */
    /* JADX WARNING: Removed duplicated region for block: B:174:0x02fd  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x0304  */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x0330  */
    /* JADX WARNING: Removed duplicated region for block: B:233:0x052f A[Catch:{ all -> 0x0525, all -> 0x053c }] */
    /* JADX WARNING: Removed duplicated region for block: B:239:0x053e A[Catch:{ all -> 0x05a3 }] */
    /* JADX WARNING: Removed duplicated region for block: B:242:0x0553 A[Catch:{ all -> 0x05a3 }] */
    /* JADX WARNING: Removed duplicated region for block: B:245:0x0563 A[Catch:{ all -> 0x05a3 }] */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x0574 A[Catch:{ all -> 0x05a3 }] */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00a0  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00c2  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00cf  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00f0  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00fd  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x011f  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0125  */
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        DualDumpOutputStream dump;
        boolean dumpAsProto;
        DualDumpOutputStream dump2;
        char c;
        int mode;
        int hashCode;
        char c2;
        int powerRole;
        int hashCode2;
        char c3;
        int dataRole;
        char c4;
        int supportedModes;
        char c5;
        int powerRole2;
        int hashCode3;
        char c6;
        int dataRole2;
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, writer)) {
            IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
            long ident = Binder.clearCallingIdentity();
            try {
                ArraySet<String> argsSet = new ArraySet<>();
                Collections.addAll(argsSet, args);
                if (argsSet.contains(PriorityDump.PROTO_ARG)) {
                    dumpAsProto = true;
                } else {
                    dumpAsProto = false;
                }
                if (args != null) {
                    try {
                        if (args.length != 0 && !args[0].equals("-a")) {
                            if (dumpAsProto) {
                                if (!dumpAsProto) {
                                    try {
                                        dump2 = new DualDumpOutputStream(new ProtoOutputStream(fd));
                                    } catch (Throwable th) {
                                        dump = th;
                                        Binder.restoreCallingIdentity(ident);
                                        throw dump;
                                    }
                                } else {
                                    pw.println("USB MANAGER STATE (dumpsys usb):");
                                    dump2 = new DualDumpOutputStream(new IndentingPrintWriter(pw, "  "));
                                }
                                if (this.mDeviceManager != null) {
                                    this.mDeviceManager.dump(dump2, "device_manager", 1146756268033L);
                                }
                                if (this.mHostManager != null) {
                                    this.mHostManager.dump(dump2, "host_manager", 1146756268034L);
                                }
                                if (this.mPortManager != null) {
                                    this.mPortManager.dump(dump2, "port_manager", 1146756268035L);
                                }
                                this.mAlsaManager.dump(dump2, "alsa_manager", 1146756268036L);
                                this.mSettingsManager.dump(dump2, "settings_manager", 1146756268037L);
                                dump2.flush();
                                Binder.restoreCallingIdentity(ident);
                            } else if ("set-port-roles".equals(args[0]) && args.length == 4) {
                                String portId = args[1];
                                String str = args[2];
                                int hashCode4 = str.hashCode();
                                if (hashCode4 != -896505829) {
                                    if (hashCode4 != -440560135) {
                                        if (hashCode4 == 3530387 && str.equals("sink")) {
                                            c5 = 1;
                                            if (c5 != 0) {
                                                powerRole2 = 1;
                                            } else if (c5 == 1) {
                                                powerRole2 = 2;
                                            } else if (c5 != 2) {
                                                pw.println("Invalid power role: " + args[2]);
                                                Binder.restoreCallingIdentity(ident);
                                                return;
                                            } else {
                                                powerRole2 = 0;
                                            }
                                            String str2 = args[3];
                                            hashCode3 = str2.hashCode();
                                            if (hashCode3 == -1335157162) {
                                                if (hashCode3 != 3208616) {
                                                    if (hashCode3 == 2063627318 && str2.equals("no-data")) {
                                                        c6 = 2;
                                                        if (c6 == 0) {
                                                            dataRole2 = 1;
                                                        } else if (c6 == 1) {
                                                            dataRole2 = 2;
                                                        } else if (c6 != 2) {
                                                            pw.println("Invalid data role: " + args[3]);
                                                            Binder.restoreCallingIdentity(ident);
                                                            return;
                                                        } else {
                                                            dataRole2 = 0;
                                                        }
                                                        if (this.mPortManager != null) {
                                                            this.mPortManager.setPortRoles(portId, powerRole2, dataRole2, pw);
                                                            pw.println();
                                                            this.mPortManager.dump(new DualDumpOutputStream(new IndentingPrintWriter(pw, "  ")), "", 0);
                                                        }
                                                        Binder.restoreCallingIdentity(ident);
                                                    }
                                                } else if (str2.equals(WatchlistLoggingHandler.WatchlistEventKeys.HOST)) {
                                                    c6 = 0;
                                                    if (c6 == 0) {
                                                    }
                                                    if (this.mPortManager != null) {
                                                    }
                                                    Binder.restoreCallingIdentity(ident);
                                                }
                                            } else if (str2.equals("device")) {
                                                c6 = 1;
                                                if (c6 == 0) {
                                                }
                                                if (this.mPortManager != null) {
                                                }
                                                Binder.restoreCallingIdentity(ident);
                                            }
                                            c6 = 65535;
                                            if (c6 == 0) {
                                            }
                                            if (this.mPortManager != null) {
                                            }
                                            Binder.restoreCallingIdentity(ident);
                                        }
                                    } else if (str.equals("no-power")) {
                                        c5 = 2;
                                        if (c5 != 0) {
                                        }
                                        String str22 = args[3];
                                        hashCode3 = str22.hashCode();
                                        if (hashCode3 == -1335157162) {
                                        }
                                        c6 = 65535;
                                        if (c6 == 0) {
                                        }
                                        if (this.mPortManager != null) {
                                        }
                                        Binder.restoreCallingIdentity(ident);
                                    }
                                } else if (str.equals("source")) {
                                    c5 = 0;
                                    if (c5 != 0) {
                                    }
                                    String str222 = args[3];
                                    hashCode3 = str222.hashCode();
                                    if (hashCode3 == -1335157162) {
                                    }
                                    c6 = 65535;
                                    if (c6 == 0) {
                                    }
                                    if (this.mPortManager != null) {
                                    }
                                    Binder.restoreCallingIdentity(ident);
                                }
                                c5 = 65535;
                                if (c5 != 0) {
                                }
                                String str2222 = args[3];
                                hashCode3 = str2222.hashCode();
                                if (hashCode3 == -1335157162) {
                                }
                                c6 = 65535;
                                if (c6 == 0) {
                                }
                                if (this.mPortManager != null) {
                                }
                                Binder.restoreCallingIdentity(ident);
                            } else if (!"add-port".equals(args[0]) || args.length != 3) {
                                if ("connect-port".equals(args[0])) {
                                    try {
                                        if (args.length == 5) {
                                            String portId2 = args[1];
                                            boolean canChangeMode = args[2].endsWith("?");
                                            String removeLastChar = canChangeMode ? removeLastChar(args[2]) : args[2];
                                            int hashCode5 = removeLastChar.hashCode();
                                            if (hashCode5 != 99374) {
                                                if (hashCode5 == 115711 && removeLastChar.equals("ufp")) {
                                                    c = 0;
                                                    if (c == 0) {
                                                        mode = 1;
                                                    } else if (c != 1) {
                                                        pw.println("Invalid mode: " + args[2]);
                                                        Binder.restoreCallingIdentity(ident);
                                                        return;
                                                    } else {
                                                        mode = 2;
                                                    }
                                                    boolean canChangePowerRole = args[3].endsWith("?");
                                                    String removeLastChar2 = canChangePowerRole ? removeLastChar(args[3]) : args[3];
                                                    hashCode = removeLastChar2.hashCode();
                                                    if (hashCode != -896505829) {
                                                        if (hashCode == 3530387 && removeLastChar2.equals("sink")) {
                                                            c2 = 1;
                                                            if (c2 == 0) {
                                                                powerRole = 1;
                                                            } else if (c2 != 1) {
                                                                pw.println("Invalid power role: " + args[3]);
                                                                Binder.restoreCallingIdentity(ident);
                                                                return;
                                                            } else {
                                                                powerRole = 2;
                                                            }
                                                            boolean canChangeDataRole = args[4].endsWith("?");
                                                            String removeLastChar3 = canChangeDataRole ? removeLastChar(args[4]) : args[4];
                                                            hashCode2 = removeLastChar3.hashCode();
                                                            if (hashCode2 != -1335157162) {
                                                                if (hashCode2 == 3208616 && removeLastChar3.equals(WatchlistLoggingHandler.WatchlistEventKeys.HOST)) {
                                                                    c3 = 0;
                                                                    if (c3 == 0) {
                                                                        dataRole = 1;
                                                                    } else if (c3 != 1) {
                                                                        pw.println("Invalid data role: " + args[4]);
                                                                        Binder.restoreCallingIdentity(ident);
                                                                        return;
                                                                    } else {
                                                                        dataRole = 2;
                                                                    }
                                                                    if (this.mPortManager != null) {
                                                                        this.mPortManager.connectSimulatedPort(portId2, mode, canChangeMode, powerRole, canChangePowerRole, dataRole, canChangeDataRole, pw);
                                                                        pw.println();
                                                                        this.mPortManager.dump(new DualDumpOutputStream(new IndentingPrintWriter(pw, "  ")), "", 0);
                                                                    }
                                                                    Binder.restoreCallingIdentity(ident);
                                                                }
                                                            } else if (removeLastChar3.equals("device")) {
                                                                c3 = 1;
                                                                if (c3 == 0) {
                                                                }
                                                                if (this.mPortManager != null) {
                                                                }
                                                                Binder.restoreCallingIdentity(ident);
                                                            }
                                                            c3 = 65535;
                                                            if (c3 == 0) {
                                                            }
                                                            if (this.mPortManager != null) {
                                                            }
                                                            Binder.restoreCallingIdentity(ident);
                                                        }
                                                    } else if (removeLastChar2.equals("source")) {
                                                        c2 = 0;
                                                        if (c2 == 0) {
                                                        }
                                                        boolean canChangeDataRole2 = args[4].endsWith("?");
                                                        if (canChangeDataRole2) {
                                                        }
                                                        hashCode2 = removeLastChar3.hashCode();
                                                        if (hashCode2 != -1335157162) {
                                                        }
                                                        c3 = 65535;
                                                        if (c3 == 0) {
                                                        }
                                                        if (this.mPortManager != null) {
                                                        }
                                                        Binder.restoreCallingIdentity(ident);
                                                    }
                                                    c2 = 65535;
                                                    if (c2 == 0) {
                                                    }
                                                    boolean canChangeDataRole22 = args[4].endsWith("?");
                                                    if (canChangeDataRole22) {
                                                    }
                                                    hashCode2 = removeLastChar3.hashCode();
                                                    if (hashCode2 != -1335157162) {
                                                    }
                                                    c3 = 65535;
                                                    if (c3 == 0) {
                                                    }
                                                    if (this.mPortManager != null) {
                                                    }
                                                    Binder.restoreCallingIdentity(ident);
                                                }
                                            } else if (removeLastChar.equals("dfp")) {
                                                c = 1;
                                                if (c == 0) {
                                                }
                                                boolean canChangePowerRole2 = args[3].endsWith("?");
                                                if (canChangePowerRole2) {
                                                }
                                                hashCode = removeLastChar2.hashCode();
                                                if (hashCode != -896505829) {
                                                }
                                                c2 = 65535;
                                                if (c2 == 0) {
                                                }
                                                boolean canChangeDataRole222 = args[4].endsWith("?");
                                                if (canChangeDataRole222) {
                                                }
                                                hashCode2 = removeLastChar3.hashCode();
                                                if (hashCode2 != -1335157162) {
                                                }
                                                c3 = 65535;
                                                if (c3 == 0) {
                                                }
                                                if (this.mPortManager != null) {
                                                }
                                                Binder.restoreCallingIdentity(ident);
                                            }
                                            c = 65535;
                                            if (c == 0) {
                                            }
                                            boolean canChangePowerRole22 = args[3].endsWith("?");
                                            if (canChangePowerRole22) {
                                            }
                                            hashCode = removeLastChar2.hashCode();
                                            if (hashCode != -896505829) {
                                            }
                                            c2 = 65535;
                                            if (c2 == 0) {
                                            }
                                            boolean canChangeDataRole2222 = args[4].endsWith("?");
                                            if (canChangeDataRole2222) {
                                            }
                                            hashCode2 = removeLastChar3.hashCode();
                                            if (hashCode2 != -1335157162) {
                                            }
                                            c3 = 65535;
                                            if (c3 == 0) {
                                            }
                                            if (this.mPortManager != null) {
                                            }
                                            Binder.restoreCallingIdentity(ident);
                                        }
                                    } catch (Throwable th2) {
                                        dump = th2;
                                        Binder.restoreCallingIdentity(ident);
                                        throw dump;
                                    }
                                }
                                if ("disconnect-port".equals(args[0]) && args.length == 2) {
                                    String portId3 = args[1];
                                    if (this.mPortManager != null) {
                                        this.mPortManager.disconnectSimulatedPort(portId3, pw);
                                        pw.println();
                                        this.mPortManager.dump(new DualDumpOutputStream(new IndentingPrintWriter(pw, "  ")), "", 0);
                                    }
                                    Binder.restoreCallingIdentity(ident);
                                } else if ("remove-port".equals(args[0]) && args.length == 2) {
                                    String portId4 = args[1];
                                    if (this.mPortManager != null) {
                                        this.mPortManager.removeSimulatedPort(portId4, pw);
                                        pw.println();
                                        this.mPortManager.dump(new DualDumpOutputStream(new IndentingPrintWriter(pw, "  ")), "", 0);
                                    }
                                    Binder.restoreCallingIdentity(ident);
                                } else if ("reset".equals(args[0]) && args.length == 1) {
                                    if (this.mPortManager != null) {
                                        this.mPortManager.resetSimulation(pw);
                                        pw.println();
                                        this.mPortManager.dump(new DualDumpOutputStream(new IndentingPrintWriter(pw, "  ")), "", 0);
                                    }
                                    Binder.restoreCallingIdentity(ident);
                                } else if ("set-contaminant-status".equals(args[0]) && args.length == 3) {
                                    String portId5 = args[1];
                                    Boolean wet = Boolean.valueOf(Boolean.parseBoolean(args[2]));
                                    if (this.mPortManager != null) {
                                        this.mPortManager.simulateContaminantStatus(portId5, wet.booleanValue(), pw);
                                        pw.println();
                                        this.mPortManager.dump(new DualDumpOutputStream(new IndentingPrintWriter(pw, "  ")), "", 0);
                                    }
                                    Binder.restoreCallingIdentity(ident);
                                } else if (!"ports".equals(args[0]) || args.length != 1) {
                                    if ("dump-descriptors".equals(args[0])) {
                                        this.mHostManager.dumpDescriptors(pw, args);
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
                                        pw.println("Example simulate contaminant status:");
                                        pw.println("  dumpsys usb add-port \"matrix\" ufp");
                                        pw.println("  dumpsys usb set-contaminant-status \"matrix\" true");
                                        pw.println("  dumpsys usb set-contaminant-status \"matrix\" false");
                                        pw.println();
                                        pw.println("Example USB device descriptors:");
                                        pw.println("  dumpsys usb dump-descriptors -dump-short");
                                        pw.println("  dumpsys usb dump-descriptors -dump-tree");
                                        pw.println("  dumpsys usb dump-descriptors -dump-list");
                                        pw.println("  dumpsys usb dump-descriptors -dump-raw");
                                    }
                                    Binder.restoreCallingIdentity(ident);
                                } else {
                                    if (this.mPortManager != null) {
                                        this.mPortManager.dump(new DualDumpOutputStream(new IndentingPrintWriter(pw, "  ")), "", 0);
                                    }
                                    Binder.restoreCallingIdentity(ident);
                                }
                            } else {
                                String portId6 = args[1];
                                String str3 = args[2];
                                switch (str3.hashCode()) {
                                    case 99374:
                                        if (str3.equals("dfp")) {
                                            c4 = 1;
                                            break;
                                        }
                                        c4 = 65535;
                                        break;
                                    case 115711:
                                        if (str3.equals("ufp")) {
                                            c4 = 0;
                                            break;
                                        }
                                        c4 = 65535;
                                        break;
                                    case 3094652:
                                        if (str3.equals("dual")) {
                                            c4 = 2;
                                            break;
                                        }
                                        c4 = 65535;
                                        break;
                                    case 3387192:
                                        if (str3.equals("none")) {
                                            c4 = 3;
                                            break;
                                        }
                                        c4 = 65535;
                                        break;
                                    default:
                                        c4 = 65535;
                                        break;
                                }
                                if (c4 == 0) {
                                    supportedModes = 1;
                                } else if (c4 == 1) {
                                    supportedModes = 2;
                                } else if (c4 == 2) {
                                    supportedModes = 3;
                                } else if (c4 != 3) {
                                    pw.println("Invalid mode: " + args[2]);
                                    Binder.restoreCallingIdentity(ident);
                                    return;
                                } else {
                                    supportedModes = 0;
                                }
                                if (this.mPortManager != null) {
                                    this.mPortManager.addSimulatedPort(portId6, supportedModes, pw);
                                    pw.println();
                                    this.mPortManager.dump(new DualDumpOutputStream(new IndentingPrintWriter(pw, "  ")), "", 0);
                                }
                                Binder.restoreCallingIdentity(ident);
                            }
                        }
                    } catch (Throwable th3) {
                        dump = th3;
                        Binder.restoreCallingIdentity(ident);
                        throw dump;
                    }
                }
                if (!dumpAsProto) {
                }
                if (this.mDeviceManager != null) {
                }
                if (this.mHostManager != null) {
                }
                if (this.mPortManager != null) {
                }
                this.mAlsaManager.dump(dump2, "alsa_manager", 1146756268036L);
                this.mSettingsManager.dump(dump2, "settings_manager", 1146756268037L);
                dump2.flush();
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th4) {
                dump = th4;
                Binder.restoreCallingIdentity(ident);
                throw dump;
            }
        }
    }

    private static String removeLastChar(String value) {
        return value.substring(0, value.length() - 1);
    }
}
