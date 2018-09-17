package com.android.server.connectivity;

import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.net.ConnectivityManager;
import android.net.INetworkManagementEventObserver;
import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.net.Network;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkMisc;
import android.net.RouteInfo;
import android.net.UidRange;
import android.net.Uri;
import android.os.Binder;
import android.os.FileUtils;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemService;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.security.KeyStore;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnConfig;
import com.android.internal.net.VpnInfo;
import com.android.internal.net.VpnProfile;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.display.DisplayTransformManager;
import com.android.server.net.BaseNetworkObserver;
import com.android.server.usb.UsbAudioDevice;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import libcore.io.IoUtils;

public class Vpn {
    private static final boolean LOGD = true;
    private static final String NETWORKTYPE = "VPN";
    private static final String TAG = "Vpn";
    private static final long VPN_LAUNCH_IDLE_WHITELIST_DURATION = 60000;
    private boolean mAlwaysOn = false;
    @GuardedBy("this")
    private Set<UidRange> mBlockedUsers = new ArraySet();
    private VpnConfig mConfig;
    private Connection mConnection;
    private Context mContext;
    private volatile boolean mEnableTeardown = true;
    private String mInterface;
    private boolean mIsPackageIntentReceiverRegistered = false;
    private LegacyVpnRunner mLegacyVpnRunner;
    private boolean mLockdown = false;
    private final Looper mLooper;
    private final INetworkManagementService mNetd;
    private NetworkAgent mNetworkAgent;
    private final NetworkCapabilities mNetworkCapabilities;
    private NetworkInfo mNetworkInfo;
    private INetworkManagementEventObserver mObserver = new BaseNetworkObserver() {
        public void interfaceStatusChanged(String interfaze, boolean up) {
            synchronized (Vpn.this) {
                if (!up) {
                    if (Vpn.this.mLegacyVpnRunner != null) {
                        Vpn.this.mLegacyVpnRunner.check(interfaze);
                    }
                }
            }
        }

        public void interfaceRemoved(String interfaze) {
            synchronized (Vpn.this) {
                if (interfaze.equals(Vpn.this.mInterface) && Vpn.this.jniCheck(interfaze) == 0) {
                    Vpn.this.mStatusIntent = null;
                    Vpn.this.mVpnUsers = null;
                    Vpn.this.mConfig = null;
                    Vpn.this.mInterface = null;
                    if (Vpn.this.mConnection != null) {
                        Vpn.this.mContext.unbindService(Vpn.this.mConnection);
                        Vpn.this.mConnection = null;
                        Vpn.this.agentDisconnect();
                    } else if (Vpn.this.mLegacyVpnRunner != null) {
                        Vpn.this.mLegacyVpnRunner.exit();
                        Vpn.this.mLegacyVpnRunner = null;
                    }
                }
            }
        }
    };
    private int mOwnerUID;
    private String mPackage;
    private final BroadcastReceiver mPackageIntentReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Missing block: B:18:0x0069, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            Uri data = intent.getData();
            String packageName = data == null ? null : data.getSchemeSpecificPart();
            if (packageName != null) {
                synchronized (Vpn.this) {
                    if (packageName.equals(Vpn.this.getAlwaysOnPackage())) {
                        String action = intent.getAction();
                        Log.i(Vpn.TAG, "Received broadcast " + action + " for always-on package " + packageName + " in user " + Vpn.this.mUserHandle);
                        if (action.equals("android.intent.action.PACKAGE_REPLACED")) {
                            Vpn.this.startAlwaysOnVpn();
                        } else if (action.equals("android.intent.action.PACKAGE_REMOVED") && (intent.getBooleanExtra("android.intent.extra.REPLACING", false) ^ 1)) {
                            Vpn.this.setAndSaveAlwaysOnPackage(null, false);
                        }
                    }
                }
            }
        }
    };
    private PendingIntent mStatusIntent;
    private final int mUserHandle;
    @GuardedBy("this")
    private Set<UidRange> mVpnUsers = null;

    private class Connection implements ServiceConnection {
        private IBinder mService;

        /* synthetic */ Connection(Vpn this$0, Connection -this1) {
            this();
        }

        private Connection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            this.mService = service;
        }

        public void onServiceDisconnected(ComponentName name) {
            this.mService = null;
        }
    }

    private class LegacyVpnRunner extends Thread {
        private static final String TAG = "LegacyVpnRunner";
        private final String[][] mArguments;
        private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (LegacyVpnRunner.this.this$0.mEnableTeardown && intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE") && intent.getIntExtra("networkType", -1) == LegacyVpnRunner.this.mOuterConnection.get()) {
                    NetworkInfo info = (NetworkInfo) intent.getExtra("networkInfo");
                    if (!(info == null || (info.isConnectedOrConnecting() ^ 1) == 0)) {
                        try {
                            LegacyVpnRunner.this.this$0.mObserver.interfaceStatusChanged(LegacyVpnRunner.this.mOuterInterface, false);
                        } catch (RemoteException e) {
                        }
                    }
                }
            }
        };
        private final String[] mDaemons;
        private final AtomicInteger mOuterConnection = new AtomicInteger(-1);
        private final String mOuterInterface;
        private final LocalSocket[] mSockets;
        private long mTimer = -1;
        final /* synthetic */ Vpn this$0;

        public LegacyVpnRunner(Vpn this$0, VpnConfig config, String[] racoon, String[] mtpd) {
            int i = 0;
            this.this$0 = this$0;
            super(TAG);
            this$0.mConfig = config;
            this.mDaemons = new String[]{"racoon", "mtpd"};
            this.mArguments = new String[][]{racoon, mtpd};
            this.mSockets = new LocalSocket[this.mDaemons.length];
            this.mOuterInterface = this$0.mConfig.interfaze;
            if (!TextUtils.isEmpty(this.mOuterInterface)) {
                ConnectivityManager cm = ConnectivityManager.from(this$0.mContext);
                Network[] allNetworks = cm.getAllNetworks();
                int length = allNetworks.length;
                while (i < length) {
                    Network network = allNetworks[i];
                    LinkProperties lp = cm.getLinkProperties(network);
                    if (lp != null && lp.getAllInterfaceNames().contains(this.mOuterInterface)) {
                        NetworkInfo networkInfo = cm.getNetworkInfo(network);
                        if (networkInfo != null) {
                            this.mOuterConnection.set(networkInfo.getType());
                        }
                    }
                    i++;
                }
            }
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            this$0.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        }

        public void check(String interfaze) {
            if (interfaze.equals(this.mOuterInterface)) {
                Log.i(TAG, "Legacy VPN is going down with " + interfaze);
                exit();
            }
        }

        public void exit() {
            interrupt();
            this.this$0.agentDisconnect();
            try {
                this.this$0.mContext.unregisterReceiver(this.mBroadcastReceiver);
            } catch (IllegalArgumentException e) {
            }
        }

        public void run() {
            String[] strArr;
            int length;
            int i = 0;
            Log.v(TAG, "Waiting");
            synchronized (TAG) {
                Log.v(TAG, "Executing");
                int length2;
                try {
                    execute();
                    monitorDaemons();
                    interrupted();
                    for (LocalSocket socket : this.mSockets) {
                        IoUtils.closeQuietly(socket);
                    }
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    for (LocalSocket socket2 : this.mSockets) {
                        IoUtils.closeQuietly(socket2);
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e2) {
                    }
                    strArr = this.mDaemons;
                    length = strArr.length;
                    while (i < length) {
                        SystemService.stop(strArr[i]);
                        i++;
                    }
                } catch (InterruptedException e3) {
                } catch (Throwable th) {
                    for (LocalSocket socket22 : this.mSockets) {
                        IoUtils.closeQuietly(socket22);
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e4) {
                    }
                    String[] strArr2 = this.mDaemons;
                    length2 = strArr2.length;
                    while (i < length2) {
                        SystemService.stop(strArr2[i]);
                        i++;
                    }
                }
                strArr = this.mDaemons;
                length = strArr.length;
                while (i < length) {
                    SystemService.stop(strArr[i]);
                    i++;
                }
                this.this$0.agentDisconnect();
            }
        }

        private void checkpoint(boolean yield) throws InterruptedException {
            long now = SystemClock.elapsedRealtime();
            if (this.mTimer == -1) {
                this.mTimer = now;
                Thread.sleep(1);
            } else if (now - this.mTimer <= 60000) {
                Thread.sleep((long) (yield ? DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE : 1));
            } else {
                this.this$0.updateState(DetailedState.FAILED, "checkpoint");
                throw new IllegalStateException("Time is up");
            }
        }

        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Can't wrap try/catch for R(6:60|61|62|57|56|58) */
        /* JADX WARNING: Missing block: B:10:0x0030, code:
            r11 = move-exception;
     */
        /* JADX WARNING: Missing block: B:11:0x0031, code:
            android.util.Log.i(TAG, "Aborting", r11);
            r26.this$0.updateState(android.net.NetworkInfo.DetailedState.FAILED, r11.getMessage());
            exit();
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void execute() {
            String daemon;
            checkpoint(false);
            for (String daemon2 : this.mDaemons) {
                while (!SystemService.isStopped(daemon2)) {
                    checkpoint(true);
                }
            }
            File state = new File("/data/misc/vpn/state");
            state.delete();
            if (state.exists()) {
                throw new IllegalStateException("Cannot delete the state");
            }
            String[] arguments;
            new File("/data/misc/vpn/abort").delete();
            boolean restart = false;
            for (String[] arguments2 : this.mArguments) {
                restart = restart || arguments2 != null;
            }
            if (restart) {
                int i;
                this.this$0.updateState(DetailedState.CONNECTING, "execute");
                for (i = 0; i < this.mDaemons.length; i++) {
                    arguments2 = this.mArguments[i];
                    if (arguments2 != null) {
                        daemon2 = this.mDaemons[i];
                        SystemService.start(daemon2);
                        while (!SystemService.isRunning(daemon2)) {
                            checkpoint(true);
                        }
                        this.mSockets[i] = new LocalSocket();
                        LocalSocketAddress address = new LocalSocketAddress(daemon2, Namespace.RESERVED);
                        while (true) {
                            try {
                                this.mSockets[i].connect(address);
                                break;
                            } catch (Exception e) {
                                checkpoint(true);
                            }
                        }
                        this.mSockets[i].setSoTimeout(500);
                        OutputStream out = this.mSockets[i].getOutputStream();
                        for (String argument : arguments2) {
                            byte[] bytes = argument.getBytes(StandardCharsets.UTF_8);
                            if (bytes.length >= 65535) {
                                throw new IllegalArgumentException("Argument is too large");
                            }
                            out.write(bytes.length >> 8);
                            out.write(bytes.length);
                            out.write(bytes);
                            checkpoint(false);
                        }
                        out.write(255);
                        out.write(255);
                        out.flush();
                        InputStream in = this.mSockets[i].getInputStream();
                        while (in.read() != -1) {
                            checkpoint(true);
                        }
                    }
                }
                while (!state.exists()) {
                    i = 0;
                    while (i < this.mDaemons.length) {
                        daemon2 = this.mDaemons[i];
                        if (this.mArguments[i] == null || (SystemService.isRunning(daemon2) ^ 1) == 0) {
                            i++;
                        } else {
                            throw new IllegalStateException(daemon2 + " is dead");
                        }
                    }
                    checkpoint(true);
                }
                String[] parameters = FileUtils.readTextFile(state, 0, null).split("\n", -1);
                if (parameters.length != 7) {
                    throw new IllegalStateException("Cannot parse the state");
                }
                this.this$0.mConfig.interfaze = parameters[0].trim();
                this.this$0.mConfig.addLegacyAddresses(parameters[1]);
                if (this.this$0.mConfig.routes == null || this.this$0.mConfig.routes.isEmpty()) {
                    this.this$0.mConfig.addLegacyRoutes(parameters[2]);
                }
                if (this.this$0.mConfig.dnsServers == null || this.this$0.mConfig.dnsServers.size() == 0) {
                    String dnsServers = parameters[3].trim();
                    if (!dnsServers.isEmpty()) {
                        this.this$0.mConfig.dnsServers = Arrays.asList(dnsServers.split(" "));
                    }
                }
                if (this.this$0.mConfig.searchDomains == null || this.this$0.mConfig.searchDomains.size() == 0) {
                    String searchDomains = parameters[4].trim();
                    if (!searchDomains.isEmpty()) {
                        this.this$0.mConfig.searchDomains = Arrays.asList(searchDomains.split(" "));
                    }
                }
                String endpoint = parameters[5];
                if (!endpoint.isEmpty()) {
                    try {
                        InetAddress addr = InetAddress.parseNumericAddress(endpoint);
                        if (addr instanceof Inet4Address) {
                            this.this$0.mConfig.routes.add(new RouteInfo(new IpPrefix(addr, 32), 9));
                        } else if (addr instanceof Inet6Address) {
                            this.this$0.mConfig.routes.add(new RouteInfo(new IpPrefix(addr, 128), 9));
                        } else {
                            Log.e(TAG, "Unknown IP address family for VPN endpoint: " + endpoint);
                        }
                    } catch (IllegalArgumentException e2) {
                        Log.e(TAG, "Exception constructing throw route to " + endpoint + ": " + e2);
                    }
                }
                synchronized (this.this$0) {
                    this.this$0.mConfig.startTime = SystemClock.elapsedRealtime();
                    checkpoint(false);
                    if (this.this$0.jniCheck(this.this$0.mConfig.interfaze) == 0) {
                        throw new IllegalStateException(this.this$0.mConfig.interfaze + " is gone");
                    }
                    this.this$0.mInterface = this.this$0.mConfig.interfaze;
                    this.this$0.prepareStatusIntent();
                    this.this$0.agentConnect();
                    Log.i(TAG, "Connected!");
                }
                return;
            }
            this.this$0.agentDisconnect();
        }

        private void monitorDaemons() throws InterruptedException {
            if (!this.this$0.mNetworkInfo.isConnected()) {
                return;
            }
            while (true) {
                Thread.sleep(2000);
                int i = 0;
                while (i < this.mDaemons.length) {
                    if (this.mArguments[i] == null || !SystemService.isStopped(this.mDaemons[i])) {
                        i++;
                    } else {
                        return;
                    }
                }
            }
        }
    }

    private native boolean jniAddAddress(String str, String str2, int i);

    private native int jniCheck(String str);

    private native int jniCreate(int i);

    private native boolean jniDelAddress(String str, String str2, int i);

    private native String jniGetName(int i);

    private native void jniReset(String str);

    private native int jniSetAddresses(String str, String str2);

    public Vpn(Looper looper, Context context, INetworkManagementService netService, int userHandle) {
        this.mContext = context;
        this.mNetd = netService;
        this.mUserHandle = userHandle;
        this.mLooper = looper;
        this.mPackage = "[Legacy VPN]";
        this.mOwnerUID = getAppUid(this.mPackage, this.mUserHandle);
        try {
            netService.registerObserver(this.mObserver);
        } catch (RemoteException e) {
            Log.wtf(TAG, "Problem registering observer", e);
        }
        this.mNetworkInfo = new NetworkInfo(17, 0, NETWORKTYPE, "");
        this.mNetworkCapabilities = new NetworkCapabilities();
        this.mNetworkCapabilities.addTransportType(4);
        this.mNetworkCapabilities.removeCapability(15);
    }

    public void setEnableTeardown(boolean enableTeardown) {
        this.mEnableTeardown = enableTeardown;
    }

    protected void updateState(DetailedState detailedState, String reason) {
        Log.d(TAG, "setting state=" + detailedState + ", reason=" + reason);
        this.mNetworkInfo.setDetailedState(detailedState, reason, null);
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
        }
        updateAlwaysOnNotification(detailedState);
    }

    public synchronized boolean setAlwaysOnPackage(String packageName, boolean lockdown) {
        enforceControlPermissionOrInternalCaller();
        if ("[Legacy VPN]".equals(packageName)) {
            Log.w(TAG, "Not setting legacy VPN \"" + packageName + "\" as always-on.");
            return false;
        }
        if (packageName == null) {
            packageName = "[Legacy VPN]";
            this.mAlwaysOn = false;
        } else if (!setPackageAuthorization(packageName, true)) {
            return false;
        } else {
            this.mAlwaysOn = true;
        }
        if (!this.mAlwaysOn) {
            lockdown = false;
        }
        this.mLockdown = lockdown;
        if (isCurrentPreparedPackage(packageName)) {
            updateAlwaysOnNotification(this.mNetworkInfo.getDetailedState());
        } else {
            prepareInternal(packageName);
        }
        maybeRegisterPackageChangeReceiverLocked(packageName);
        setVpnForcedLocked(this.mLockdown);
        return true;
    }

    private static boolean isNullOrLegacyVpn(String packageName) {
        return packageName != null ? "[Legacy VPN]".equals(packageName) : true;
    }

    private void unregisterPackageChangeReceiverLocked() {
        if (this.mIsPackageIntentReceiverRegistered) {
            this.mContext.unregisterReceiver(this.mPackageIntentReceiver);
            this.mIsPackageIntentReceiverRegistered = false;
        }
    }

    private void maybeRegisterPackageChangeReceiverLocked(String packageName) {
        unregisterPackageChangeReceiverLocked();
        if (!isNullOrLegacyVpn(packageName)) {
            this.mIsPackageIntentReceiverRegistered = true;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
            intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            intentFilter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
            intentFilter.addDataSchemeSpecificPart(packageName, 0);
            this.mContext.registerReceiverAsUser(this.mPackageIntentReceiver, UserHandle.of(this.mUserHandle), intentFilter, null, null);
        }
    }

    public synchronized String getAlwaysOnPackage() {
        enforceControlPermissionOrInternalCaller();
        return this.mAlwaysOn ? this.mPackage : null;
    }

    public synchronized void saveAlwaysOnPackage() {
        long token = Binder.clearCallingIdentity();
        try {
            ContentResolver cr = this.mContext.getContentResolver();
            Secure.putStringForUser(cr, "always_on_vpn_app", getAlwaysOnPackage(), this.mUserHandle);
            Secure.putIntForUser(cr, "always_on_vpn_lockdown", this.mLockdown ? 1 : 0, this.mUserHandle);
            Binder.restoreCallingIdentity(token);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    private synchronized boolean setAndSaveAlwaysOnPackage(String packageName, boolean lockdown) {
        if (!setAlwaysOnPackage(packageName, lockdown)) {
            return false;
        }
        saveAlwaysOnPackage();
        return true;
    }

    /* JADX WARNING: Missing block: B:13:0x0018, code:
            r10 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Missing block: B:15:?, code:
            ((com.android.server.DeviceIdleController.LocalService) com.android.server.LocalServices.getService(com.android.server.DeviceIdleController.LocalService.class)).addPowerSaveTempWhitelistApp(android.os.Process.myUid(), r3, 60000, r14.mUserHandle, false, "vpn");
            r9 = new android.content.Intent("android.net.VpnService");
            r9.setPackage(r3);
     */
    /* JADX WARNING: Missing block: B:18:0x004b, code:
            if (r14.mContext.startServiceAsUser(r9, android.os.UserHandle.of(r14.mUserHandle)) == null) goto L_0x0055;
     */
    /* JADX WARNING: Missing block: B:19:0x004d, code:
            r2 = true;
     */
    /* JADX WARNING: Missing block: B:20:0x004e, code:
            android.os.Binder.restoreCallingIdentity(r10);
     */
    /* JADX WARNING: Missing block: B:21:0x0051, code:
            return r2;
     */
    /* JADX WARNING: Missing block: B:25:0x0055, code:
            r2 = false;
     */
    /* JADX WARNING: Missing block: B:26:0x0057, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:28:?, code:
            android.util.Log.e(TAG, "VpnService " + r9 + " failed to start", r0);
     */
    /* JADX WARNING: Missing block: B:29:0x0079, code:
            android.os.Binder.restoreCallingIdentity(r10);
     */
    /* JADX WARNING: Missing block: B:30:0x007c, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:31:0x007d, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:32:0x007e, code:
            android.os.Binder.restoreCallingIdentity(r10);
     */
    /* JADX WARNING: Missing block: B:33:0x0081, code:
            throw r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean startAlwaysOnVpn() {
        synchronized (this) {
            try {
                String alwaysOnPackage = getAlwaysOnPackage();
                if (alwaysOnPackage == null) {
                    return true;
                } else if (getNetworkInfo().isConnected()) {
                    return true;
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* JADX WARNING: Missing block: B:22:0x002e, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:38:0x005a, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean prepare(String oldPackage, String newPackage) {
        if (oldPackage != null) {
            if (this.mAlwaysOn && (isCurrentPreparedPackage(oldPackage) ^ 1) != 0) {
                return false;
            }
            if (isCurrentPreparedPackage(oldPackage)) {
                if (!(oldPackage.equals("[Legacy VPN]") || (isVpnUserPreConsented(oldPackage) ^ 1) == 0)) {
                    prepareInternal("[Legacy VPN]");
                    return false;
                }
            } else if (!oldPackage.equals("[Legacy VPN]") && isVpnUserPreConsented(oldPackage)) {
                prepareInternal(oldPackage);
                return true;
            }
        }
        if (newPackage != null) {
            if (newPackage.equals("[Legacy VPN]") || !isCurrentPreparedPackage(newPackage)) {
                enforceControlPermission();
                if (this.mAlwaysOn && (isCurrentPreparedPackage(newPackage) ^ 1) != 0) {
                    return false;
                }
                prepareInternal(newPackage);
                return true;
            }
        }
    }

    public synchronized boolean turnOffAllVpn(String packageName) {
        prepareInternal(packageName);
        return true;
    }

    private boolean isCurrentPreparedPackage(String packageName) {
        return getAppUid(packageName, this.mUserHandle) == this.mOwnerUID;
    }

    private void prepareInternal(String newPackage) {
        long token = Binder.clearCallingIdentity();
        if (this.mInterface != null) {
            this.mStatusIntent = null;
            agentDisconnect();
            jniReset(this.mInterface);
            this.mInterface = null;
            this.mVpnUsers = null;
        }
        if (this.mConnection != null) {
            try {
                this.mConnection.mService.transact(UsbAudioDevice.kAudioDeviceClassMask, Parcel.obtain(), null, 1);
            } catch (Exception e) {
            }
            try {
                this.mContext.unbindService(this.mConnection);
                this.mConnection = null;
            } catch (Exception e2) {
                Log.wtf(TAG, "Failed to disallow UID " + this.mOwnerUID + " to call protect() " + e2);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
            }
        } else if (this.mLegacyVpnRunner != null) {
            this.mLegacyVpnRunner.exit();
            this.mLegacyVpnRunner = null;
        }
        this.mNetd.denyProtect(this.mOwnerUID);
        Log.i(TAG, "Switched from " + this.mPackage + " to " + newPackage);
        this.mPackage = newPackage;
        this.mOwnerUID = getAppUid(newPackage, this.mUserHandle);
        try {
            this.mNetd.allowProtect(this.mOwnerUID);
        } catch (Exception e22) {
            Log.wtf(TAG, "Failed to allow UID " + this.mOwnerUID + " to call protect() " + e22);
        }
        this.mConfig = null;
        updateState(DetailedState.IDLE, "prepare");
        Binder.restoreCallingIdentity(token);
    }

    public boolean setPackageAuthorization(String packageName, boolean authorized) {
        boolean z = true;
        enforceControlPermissionOrInternalCaller();
        int uid = getAppUid(packageName, this.mUserHandle);
        if (uid == -1 || "[Legacy VPN]".equals(packageName)) {
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            int i;
            AppOpsManager appOps = (AppOpsManager) this.mContext.getSystemService("appops");
            if (authorized) {
                i = 0;
            } else {
                i = 1;
            }
            appOps.setMode(47, uid, packageName, i);
            return z;
        } catch (Exception e) {
            String str = TAG;
            z = "Failed to set app ops for package " + packageName + ", uid " + uid;
            Log.wtf(str, z, e);
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private boolean isVpnUserPreConsented(String packageName) {
        if (((AppOpsManager) this.mContext.getSystemService("appops")).noteOpNoThrow(47, Binder.getCallingUid(), packageName) == 0) {
            return true;
        }
        return false;
    }

    private int getAppUid(String app, int userHandle) {
        if ("[Legacy VPN]".equals(app)) {
            return Process.myUid();
        }
        int result;
        try {
            result = this.mContext.getPackageManager().getPackageUidAsUser(app, userHandle);
        } catch (NameNotFoundException e) {
            result = -1;
        }
        return result;
    }

    public NetworkInfo getNetworkInfo() {
        return this.mNetworkInfo;
    }

    public int getNetId() {
        return this.mNetworkAgent != null ? this.mNetworkAgent.netId : 0;
    }

    private LinkProperties makeLinkProperties() {
        InetAddress address;
        boolean allowIPv4 = this.mConfig.allowIPv4;
        boolean allowIPv6 = this.mConfig.allowIPv6;
        LinkProperties lp = new LinkProperties();
        lp.setInterfaceName(this.mInterface);
        if (this.mConfig.addresses != null) {
            for (LinkAddress address2 : this.mConfig.addresses) {
                lp.addLinkAddress(address2);
                allowIPv4 |= address2.getAddress() instanceof Inet4Address;
                allowIPv6 |= address2.getAddress() instanceof Inet6Address;
            }
        }
        if (this.mConfig.routes != null) {
            for (RouteInfo route : this.mConfig.routes) {
                lp.addRoute(route);
                address = route.getDestination().getAddress();
                allowIPv4 |= address instanceof Inet4Address;
                allowIPv6 |= address instanceof Inet6Address;
            }
        }
        if (this.mConfig.dnsServers != null) {
            for (String dnsServer : this.mConfig.dnsServers) {
                address = InetAddress.parseNumericAddress(dnsServer);
                lp.addDnsServer(address);
                allowIPv4 |= address instanceof Inet4Address;
                allowIPv6 |= address instanceof Inet6Address;
            }
        }
        if (!allowIPv4) {
            lp.addRoute(new RouteInfo(new IpPrefix(Inet4Address.ANY, 0), 7));
        }
        if (!allowIPv6) {
            lp.addRoute(new RouteInfo(new IpPrefix(Inet6Address.ANY, 0), 7));
        }
        StringBuilder buffer = new StringBuilder();
        if (this.mConfig.searchDomains != null) {
            for (String domain : this.mConfig.searchDomains) {
                buffer.append(domain).append(' ');
            }
        }
        lp.setDomains(buffer.toString().trim());
        return lp;
    }

    private void agentConnect() {
        boolean z = false;
        LinkProperties lp = makeLinkProperties();
        if (lp.hasIPv4DefaultRoute() || lp.hasIPv6DefaultRoute()) {
            this.mNetworkCapabilities.addCapability(12);
        } else {
            this.mNetworkCapabilities.removeCapability(12);
        }
        this.mNetworkInfo.setDetailedState(DetailedState.CONNECTING, null, null);
        NetworkMisc networkMisc = new NetworkMisc();
        if (this.mConfig.allowBypass) {
            z = this.mLockdown ^ 1;
        }
        networkMisc.allowBypass = z;
        long token = Binder.clearCallingIdentity();
        try {
            this.mNetworkAgent = new NetworkAgent(this.mLooper, this.mContext, NETWORKTYPE, this.mNetworkInfo, this.mNetworkCapabilities, lp, 0, networkMisc) {
                public void unwanted() {
                }
            };
            this.mVpnUsers = createUserAndRestrictedProfilesRanges(this.mUserHandle, this.mConfig.allowedApplications, this.mConfig.disallowedApplications);
            this.mNetworkAgent.addUidRanges((UidRange[]) this.mVpnUsers.toArray(new UidRange[this.mVpnUsers.size()]));
            this.mNetworkInfo.setIsAvailable(true);
            updateState(DetailedState.CONNECTED, "agentConnect");
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private boolean canHaveRestrictedProfile(int userId) {
        long token = Binder.clearCallingIdentity();
        try {
            boolean canHaveRestrictedProfile = UserManager.get(this.mContext).canHaveRestrictedProfile(userId);
            return canHaveRestrictedProfile;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void agentDisconnect(NetworkAgent networkAgent) {
        if (networkAgent != null) {
            NetworkInfo networkInfo = new NetworkInfo(this.mNetworkInfo);
            networkInfo.setIsAvailable(false);
            networkInfo.setDetailedState(DetailedState.DISCONNECTED, null, null);
            networkAgent.sendNetworkInfo(networkInfo);
        }
    }

    private void agentDisconnect() {
        if (this.mNetworkInfo.isConnected()) {
            this.mNetworkInfo.setIsAvailable(false);
            updateState(DetailedState.DISCONNECTED, "agentDisconnect");
            this.mNetworkAgent = null;
        }
    }

    public synchronized ParcelFileDescriptor establish(VpnConfig config) {
        UserManager mgr = UserManager.get(this.mContext);
        if (Binder.getCallingUid() != this.mOwnerUID) {
            return null;
        }
        if (!isVpnUserPreConsented(this.mPackage)) {
            return null;
        }
        Intent intent = new Intent("android.net.VpnService");
        intent.setClassName(this.mPackage, config.user);
        long token = Binder.clearCallingIdentity();
        try {
            if (mgr.getUserInfo(this.mUserHandle).isRestricted()) {
                throw new SecurityException("Restricted users cannot establish VPNs");
            }
            ResolveInfo info = AppGlobals.getPackageManager().resolveService(intent, null, 0, this.mUserHandle);
            if (info == null) {
                throw new SecurityException("Cannot find " + config.user);
            } else if ("android.permission.BIND_VPN_SERVICE".equals(info.serviceInfo.permission)) {
                Binder.restoreCallingIdentity(token);
                VpnConfig oldConfig = this.mConfig;
                String oldInterface = this.mInterface;
                Connection oldConnection = this.mConnection;
                NetworkAgent oldNetworkAgent = this.mNetworkAgent;
                this.mNetworkAgent = null;
                Set<UidRange> oldUsers = this.mVpnUsers;
                ParcelFileDescriptor tun = ParcelFileDescriptor.adoptFd(jniCreate(config.mtu));
                try {
                    updateState(DetailedState.CONNECTING, "establish");
                    String interfaze = jniGetName(tun.getFd());
                    StringBuilder builder = new StringBuilder();
                    for (LinkAddress address : config.addresses) {
                        builder.append(" ").append(address);
                    }
                    if (jniSetAddresses(interfaze, builder.toString()) < 1) {
                        throw new IllegalArgumentException("At least one address must be specified");
                    }
                    Connection connection = new Connection(this, null);
                    if (this.mContext.bindServiceAsUser(intent, connection, 67108865, new UserHandle(this.mUserHandle))) {
                        this.mConnection = connection;
                        this.mInterface = interfaze;
                        config.user = this.mPackage;
                        config.interfaze = this.mInterface;
                        config.startTime = SystemClock.elapsedRealtime();
                        this.mConfig = config;
                        agentConnect();
                        if (oldConnection != null) {
                            this.mContext.unbindService(oldConnection);
                        }
                        agentDisconnect(oldNetworkAgent);
                        if (!(oldInterface == null || (oldInterface.equals(interfaze) ^ 1) == 0)) {
                            jniReset(oldInterface);
                        }
                        IoUtils.setBlocking(tun.getFileDescriptor(), config.blocking);
                        Log.i(TAG, "Established by " + config.user + " on " + this.mInterface);
                        return tun;
                    }
                    throw new IllegalStateException("Cannot bind " + config.user);
                } catch (IOException e) {
                    throw new IllegalStateException("Cannot set tunnel's fd as blocking=" + config.blocking, e);
                } catch (RuntimeException e2) {
                    IoUtils.closeQuietly(tun);
                    agentDisconnect();
                    this.mConfig = oldConfig;
                    this.mConnection = oldConnection;
                    this.mVpnUsers = oldUsers;
                    this.mNetworkAgent = oldNetworkAgent;
                    this.mInterface = oldInterface;
                    throw e2;
                }
            } else {
                throw new SecurityException(config.user + " does not require " + "android.permission.BIND_VPN_SERVICE");
            }
        } catch (RemoteException e3) {
            throw new SecurityException("Cannot find " + config.user);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    private boolean isRunningLocked() {
        return (this.mNetworkAgent == null || this.mInterface == null) ? false : true;
    }

    private boolean isCallerEstablishedOwnerLocked() {
        return isRunningLocked() && Binder.getCallingUid() == this.mOwnerUID;
    }

    private SortedSet<Integer> getAppsUids(List<String> packageNames, int userHandle) {
        SortedSet<Integer> uids = new TreeSet();
        for (String app : packageNames) {
            int uid = getAppUid(app, userHandle);
            if (uid != -1) {
                uids.add(Integer.valueOf(uid));
            }
        }
        return uids;
    }

    Set<UidRange> createUserAndRestrictedProfilesRanges(int userHandle, List<String> allowedApplications, List<String> disallowedApplications) {
        Set<UidRange> ranges = new ArraySet();
        addUserToRanges(ranges, userHandle, allowedApplications, disallowedApplications);
        if (canHaveRestrictedProfile(userHandle)) {
            long token = Binder.clearCallingIdentity();
            try {
                List<UserInfo> users = UserManager.get(this.mContext).getUsers(true);
                for (UserInfo user : users) {
                    if (user.isRestricted() && user.restrictedProfileParentId == userHandle) {
                        addUserToRanges(ranges, user.id, allowedApplications, disallowedApplications);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
        return ranges;
    }

    void addUserToRanges(Set<UidRange> ranges, int userHandle, List<String> allowedApplications, List<String> disallowedApplications) {
        int start;
        int uid;
        if (allowedApplications != null) {
            start = -1;
            int stop = -1;
            for (Integer intValue : getAppsUids(allowedApplications, userHandle)) {
                uid = intValue.intValue();
                if (start == -1) {
                    start = uid;
                } else if (uid != stop + 1) {
                    ranges.add(new UidRange(start, stop));
                    start = uid;
                }
                stop = uid;
            }
            if (start != -1) {
                ranges.add(new UidRange(start, stop));
            }
        } else if (disallowedApplications != null) {
            UidRange userRange = UidRange.createForUser(userHandle);
            start = userRange.start;
            for (Integer intValue2 : getAppsUids(disallowedApplications, userHandle)) {
                uid = intValue2.intValue();
                if (uid == start) {
                    start++;
                } else {
                    ranges.add(new UidRange(start, uid - 1));
                    start = uid + 1;
                }
            }
            if (start <= userRange.stop) {
                ranges.add(new UidRange(start, userRange.stop));
            }
        } else {
            ranges.add(UidRange.createForUser(userHandle));
        }
    }

    private List<UidRange> uidRangesForUser(int userHandle) {
        UidRange userRange = UidRange.createForUser(userHandle);
        List<UidRange> ranges = new ArrayList();
        for (UidRange range : this.mVpnUsers) {
            if (userRange.containsRange(range)) {
                ranges.add(range);
            }
        }
        return ranges;
    }

    private void removeVpnUserLocked(int userHandle) {
        if (this.mVpnUsers == null) {
            throw new IllegalStateException("VPN is not active");
        }
        List<UidRange> ranges = uidRangesForUser(userHandle);
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.removeUidRanges((UidRange[]) ranges.toArray(new UidRange[ranges.size()]));
        }
        this.mVpnUsers.removeAll(ranges);
    }

    public void onUserAdded(int userHandle) {
        UserInfo user = UserManager.get(this.mContext).getUserInfo(userHandle);
        if (user.isRestricted() && user.restrictedProfileParentId == this.mUserHandle) {
            synchronized (this) {
                if (this.mVpnUsers != null) {
                    try {
                        addUserToRanges(this.mVpnUsers, userHandle, this.mConfig.allowedApplications, this.mConfig.disallowedApplications);
                        if (this.mNetworkAgent != null) {
                            List<UidRange> ranges = uidRangesForUser(userHandle);
                            this.mNetworkAgent.addUidRanges((UidRange[]) ranges.toArray(new UidRange[ranges.size()]));
                        }
                    } catch (Exception e) {
                        Log.wtf(TAG, "Failed to add restricted user to owner", e);
                    }
                }
                if (this.mAlwaysOn) {
                    setVpnForcedLocked(this.mLockdown);
                }
            }
            return;
        }
        return;
    }

    public void onUserRemoved(int userHandle) {
        UserInfo user = UserManager.get(this.mContext).getUserInfo(userHandle);
        if (user.isRestricted() && user.restrictedProfileParentId == this.mUserHandle) {
            synchronized (this) {
                if (this.mVpnUsers != null) {
                    try {
                        removeVpnUserLocked(userHandle);
                    } catch (Exception e) {
                        Log.wtf(TAG, "Failed to remove restricted user to owner", e);
                    }
                }
                if (this.mAlwaysOn) {
                    setVpnForcedLocked(this.mLockdown);
                }
            }
            return;
        }
        return;
    }

    public synchronized void onUserStopped() {
        setVpnForcedLocked(false);
        this.mAlwaysOn = false;
        unregisterPackageChangeReceiverLocked();
        agentDisconnect();
    }

    @GuardedBy("this")
    private void setVpnForcedLocked(boolean enforce) {
        Set<UidRange> removedRanges = new ArraySet(this.mBlockedUsers);
        if (enforce) {
            Set<UidRange> addedRanges = createUserAndRestrictedProfilesRanges(this.mUserHandle, null, Collections.singletonList(this.mPackage));
            removedRanges.removeAll(addedRanges);
            addedRanges.removeAll(this.mBlockedUsers);
            setAllowOnlyVpnForUids(false, removedRanges);
            setAllowOnlyVpnForUids(true, addedRanges);
            return;
        }
        setAllowOnlyVpnForUids(false, removedRanges);
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0022 A:{Splitter: B:4:0x0015, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:9:0x0022, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:10:0x0023, code:
            android.util.Log.e(TAG, "Updating blocked=" + r7 + " for UIDs " + java.util.Arrays.toString(r8.toArray()) + " failed", r0);
     */
    /* JADX WARNING: Missing block: B:11:0x0057, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @GuardedBy("this")
    private boolean setAllowOnlyVpnForUids(boolean enforce, Collection<UidRange> ranges) {
        if (ranges.size() == 0) {
            return true;
        }
        try {
            this.mNetd.setAllowOnlyVpnForUids(enforce, (UidRange[]) ranges.toArray(new UidRange[ranges.size()]));
            if (enforce) {
                this.mBlockedUsers.addAll(ranges);
            } else {
                this.mBlockedUsers.removeAll(ranges);
            }
            return true;
        } catch (Exception e) {
        }
    }

    public VpnConfig getVpnConfig() {
        enforceControlPermission();
        return this.mConfig;
    }

    @Deprecated
    public synchronized void interfaceStatusChanged(String iface, boolean up) {
        try {
            this.mObserver.interfaceStatusChanged(iface, up);
        } catch (RemoteException e) {
        }
    }

    private void enforceControlPermission() {
        this.mContext.enforceCallingPermission("android.permission.CONTROL_VPN", "Unauthorized Caller");
    }

    private void enforceControlPermissionOrInternalCaller() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONTROL_VPN", "Unauthorized Caller");
    }

    private void prepareStatusIntent() {
        long token = Binder.clearCallingIdentity();
        try {
            this.mStatusIntent = VpnConfig.getIntentForStatusPanel(this.mContext);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public synchronized boolean addAddress(String address, int prefixLength) {
        if (!isCallerEstablishedOwnerLocked()) {
            return false;
        }
        boolean success = jniAddAddress(this.mInterface, address, prefixLength);
        this.mNetworkAgent.sendLinkProperties(makeLinkProperties());
        return success;
    }

    public synchronized boolean removeAddress(String address, int prefixLength) {
        if (!isCallerEstablishedOwnerLocked()) {
            return false;
        }
        boolean success = jniDelAddress(this.mInterface, address, prefixLength);
        this.mNetworkAgent.sendLinkProperties(makeLinkProperties());
        return success;
    }

    public synchronized boolean setUnderlyingNetworks(Network[] networks) {
        if (!isCallerEstablishedOwnerLocked()) {
            return false;
        }
        if (networks == null) {
            this.mConfig.underlyingNetworks = null;
        } else {
            this.mConfig.underlyingNetworks = new Network[networks.length];
            for (int i = 0; i < networks.length; i++) {
                if (networks[i] == null) {
                    this.mConfig.underlyingNetworks[i] = null;
                } else {
                    this.mConfig.underlyingNetworks[i] = new Network(networks[i].netId);
                }
            }
        }
        return true;
    }

    public synchronized Network[] getUnderlyingNetworks() {
        if (!isRunningLocked()) {
            return null;
        }
        return this.mConfig.underlyingNetworks;
    }

    public synchronized VpnInfo getVpnInfo() {
        if (!isRunningLocked()) {
            return null;
        }
        VpnInfo info = new VpnInfo();
        info.ownerUid = this.mOwnerUID;
        info.vpnIface = this.mInterface;
        return info;
    }

    public synchronized boolean appliesToUid(int uid) {
        if (!isRunningLocked()) {
            return false;
        }
        for (UidRange uidRange : this.mVpnUsers) {
            if (uidRange.contains(uid)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isBlockingUid(int uid) {
        if (!this.mLockdown) {
            return false;
        }
        if (this.mNetworkInfo.isConnected()) {
            return appliesToUid(uid) ^ 1;
        }
        for (UidRange uidRange : this.mBlockedUsers) {
            if (uidRange.contains(uid)) {
                return true;
            }
        }
        return false;
    }

    private void updateAlwaysOnNotification(DetailedState networkState) {
        boolean visible = this.mAlwaysOn && networkState != DetailedState.CONNECTED;
        updateAlwaysOnNotificationInternal(visible);
    }

    protected void updateAlwaysOnNotificationInternal(boolean visible) {
        UserHandle user = UserHandle.of(this.mUserHandle);
        long token = Binder.clearCallingIdentity();
        try {
            NotificationManager notificationManager = NotificationManager.from(this.mContext);
            if (visible) {
                notificationManager.notifyAsUser(TAG, 17, new Builder(this.mContext, SystemNotificationChannels.VPN).setSmallIcon(17303583).setContentTitle(this.mContext.getString(17041197)).setContentText(this.mContext.getString(17041194)).setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, new Intent("android.settings.VPN_SETTINGS"), 201326592, null, user)).setCategory("sys").setVisibility(1).setOngoing(true).setColor(this.mContext.getColor(17170769)).build(), user);
                Binder.restoreCallingIdentity(token);
                return;
            }
            notificationManager.cancelAsUser(TAG, 17, user);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private static RouteInfo findIPv4DefaultRoute(LinkProperties prop) {
        for (RouteInfo route : prop.getAllRoutes()) {
            if (route.isDefaultRoute() && (route.getGateway() instanceof Inet4Address)) {
                return route;
            }
        }
        throw new IllegalStateException("Unable to find IPv4 default gateway");
    }

    public void startLegacyVpn(VpnProfile profile, KeyStore keyStore, LinkProperties egress) {
        enforceControlPermission();
        long token = Binder.clearCallingIdentity();
        try {
            startLegacyVpnPrivileged(profile, keyStore, egress);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void startLegacyVpnPrivileged(VpnProfile profile, KeyStore keyStore, LinkProperties egress) {
        UserManager mgr = UserManager.get(this.mContext);
        if (mgr.getUserInfo(this.mUserHandle).isRestricted() || mgr.hasUserRestriction("no_config_vpn", new UserHandle(this.mUserHandle))) {
            throw new SecurityException("Restricted users cannot establish VPNs");
        }
        byte[] value;
        RouteInfo ipv4DefaultRoute = findIPv4DefaultRoute(egress);
        String gateway = ipv4DefaultRoute.getGateway().getHostAddress();
        String iface = ipv4DefaultRoute.getInterface();
        String privateKey = "";
        String userCert = "";
        String caCert = "";
        String serverCert = "";
        if (!profile.ipsecUserCert.isEmpty()) {
            privateKey = "USRPKEY_" + profile.ipsecUserCert;
            value = keyStore.get("USRCERT_" + profile.ipsecUserCert);
            userCert = value == null ? null : new String(value, StandardCharsets.UTF_8);
        }
        if (!profile.ipsecCaCert.isEmpty()) {
            value = keyStore.get("CACERT_" + profile.ipsecCaCert);
            caCert = value == null ? null : new String(value, StandardCharsets.UTF_8);
        }
        if (!profile.ipsecServerCert.isEmpty()) {
            value = keyStore.get("USRCERT_" + profile.ipsecServerCert);
            serverCert = value == null ? null : new String(value, StandardCharsets.UTF_8);
        }
        if (privateKey == null || userCert == null || caCert == null || serverCert == null) {
            throw new IllegalStateException("Cannot load credentials");
        }
        String[] racoon = null;
        switch (profile.type) {
            case 2:
                racoon = new String[]{iface, profile.server, "udppsk", profile.ipsecIdentifier, profile.ipsecSecret, "1701"};
                break;
            case 3:
                racoon = new String[]{iface, profile.server, "udprsa", privateKey, userCert, caCert, serverCert, "1701"};
                break;
            case 4:
                racoon = new String[]{iface, profile.server, "xauthpsk", profile.ipsecIdentifier, profile.ipsecSecret, profile.username, profile.password, "", gateway};
                break;
            case 5:
                racoon = new String[]{iface, profile.server, "xauthrsa", privateKey, userCert, caCert, serverCert, profile.username, profile.password, "", gateway};
                break;
            case 6:
                racoon = new String[]{iface, profile.server, "hybridrsa", caCert, serverCert, profile.username, profile.password, "", gateway};
                break;
        }
        String[] mtpd = null;
        switch (profile.type) {
            case 0:
                mtpd = new String[20];
                mtpd[0] = iface;
                mtpd[1] = "pptp";
                mtpd[2] = profile.server;
                mtpd[3] = "1723";
                mtpd[4] = "name";
                mtpd[5] = profile.username;
                mtpd[6] = "password";
                mtpd[7] = profile.password;
                mtpd[8] = "linkname";
                mtpd[9] = "vpn";
                mtpd[10] = "refuse-eap";
                mtpd[11] = "nodefaultroute";
                mtpd[12] = "usepeerdns";
                mtpd[13] = "idle";
                mtpd[14] = "1800";
                mtpd[15] = "mtu";
                mtpd[16] = "1400";
                mtpd[17] = "mru";
                mtpd[18] = "1400";
                mtpd[19] = profile.mppe ? "+mppe" : "nomppe";
                break;
            case 1:
            case 2:
            case 3:
                mtpd = new String[]{iface, "l2tp", profile.server, "1701", profile.l2tpSecret, "name", profile.username, "password", profile.password, "linkname", "vpn", "refuse-eap", "nodefaultroute", "usepeerdns", "idle", "1800", "mtu", "1400", "mru", "1400"};
                break;
        }
        VpnConfig config = new VpnConfig();
        config.legacy = true;
        config.user = profile.key;
        config.interfaze = iface;
        config.session = profile.name;
        config.addLegacyRoutes(profile.routes);
        if (!profile.dnsServers.isEmpty()) {
            config.dnsServers = Arrays.asList(profile.dnsServers.split(" +"));
        }
        if (!profile.searchDomains.isEmpty()) {
            config.searchDomains = Arrays.asList(profile.searchDomains.split(" +"));
        }
        startLegacyVpn(config, racoon, mtpd);
    }

    private synchronized void startLegacyVpn(VpnConfig config, String[] racoon, String[] mtpd) {
        stopLegacyVpnPrivileged();
        prepareInternal("[Legacy VPN]");
        updateState(DetailedState.CONNECTING, "startLegacyVpn");
        this.mLegacyVpnRunner = new LegacyVpnRunner(this, config, racoon, mtpd);
        this.mLegacyVpnRunner.start();
    }

    public synchronized void stopLegacyVpnPrivileged() {
        if (this.mLegacyVpnRunner != null) {
            this.mLegacyVpnRunner.exit();
            this.mLegacyVpnRunner = null;
            synchronized ("LegacyVpnRunner") {
            }
        }
    }

    public synchronized LegacyVpnInfo getLegacyVpnInfo() {
        enforceControlPermission();
        return getLegacyVpnInfoPrivileged();
    }

    /* JADX WARNING: Missing block: B:12:0x0028, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized LegacyVpnInfo getLegacyVpnInfoPrivileged() {
        if (this.mLegacyVpnRunner == null) {
            return null;
        }
        LegacyVpnInfo info = new LegacyVpnInfo();
        info.key = this.mConfig.user;
        info.state = LegacyVpnInfo.stateFromNetworkInfo(this.mNetworkInfo);
        if (this.mNetworkInfo.isConnected()) {
            info.intent = this.mStatusIntent;
        }
    }

    public VpnConfig getLegacyVpnConfig() {
        if (this.mLegacyVpnRunner != null) {
            return this.mConfig;
        }
        return null;
    }
}
