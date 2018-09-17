package com.android.server.connectivity;

import android.app.AppGlobals;
import android.app.AppOpsManager;
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
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.display.RampAnimator;
import com.android.server.net.BaseNetworkObserver;
import com.android.server.usb.UsbAudioDevice;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
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
    private boolean mAlwaysOn;
    @GuardedBy("this")
    private Set<UidRange> mBlockedUsers;
    private VpnConfig mConfig;
    private Connection mConnection;
    private Context mContext;
    private volatile boolean mEnableTeardown;
    private String mInterface;
    private boolean mIsPackageIntentReceiverRegistered;
    private LegacyVpnRunner mLegacyVpnRunner;
    private boolean mLockdown;
    private final Looper mLooper;
    private final INetworkManagementService mNetd;
    private NetworkAgent mNetworkAgent;
    private final NetworkCapabilities mNetworkCapabilities;
    private NetworkInfo mNetworkInfo;
    private INetworkManagementEventObserver mObserver;
    private int mOwnerUID;
    private String mPackage;
    private final BroadcastReceiver mPackageIntentReceiver;
    private PendingIntent mStatusIntent;
    private final int mUserHandle;
    @GuardedBy("this")
    private Set<UidRange> mVpnUsers;

    /* renamed from: com.android.server.connectivity.Vpn.3 */
    class AnonymousClass3 extends NetworkAgent {
        AnonymousClass3(Looper $anonymous0, Context $anonymous1, String $anonymous2, NetworkInfo $anonymous3, NetworkCapabilities $anonymous4, LinkProperties $anonymous5, int $anonymous6, NetworkMisc $anonymous7) {
            super($anonymous0, $anonymous1, $anonymous2, $anonymous3, $anonymous4, $anonymous5, $anonymous6, $anonymous7);
        }

        public void unwanted() {
        }
    }

    private class Connection implements ServiceConnection {
        private IBinder mService;

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
        private final BroadcastReceiver mBroadcastReceiver;
        private final String[] mDaemons;
        private final AtomicInteger mOuterConnection;
        private final String mOuterInterface;
        private final LocalSocket[] mSockets;
        private long mTimer;
        final /* synthetic */ Vpn this$0;

        private void monitorDaemons() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: Incorrect nodes count for selectOther: B:34:0x006a in [B:24:0x0062, B:34:0x006a, B:33:0x0056, B:32:0x0036]
	at jadx.core.utils.BlockUtils.selectOther(BlockUtils.java:53)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:62)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r8 = this;
            r4 = 0;
            r3 = r8.this$0;
            r3 = r3.mNetworkInfo;
            r3 = r3.isConnected();
            if (r3 != 0) goto L_0x000e;
        L_0x000d:
            return;
        L_0x000e:
            r6 = 2000; // 0x7d0 float:2.803E-42 double:9.88E-321;
            java.lang.Thread.sleep(r6);	 Catch:{ InterruptedException -> 0x003f, all -> 0x005c }
            r2 = 0;	 Catch:{ InterruptedException -> 0x003f, all -> 0x005c }
        L_0x0014:
            r3 = r8.mDaemons;	 Catch:{ InterruptedException -> 0x003f, all -> 0x005c }
            r3 = r3.length;	 Catch:{ InterruptedException -> 0x003f, all -> 0x005c }
            if (r2 >= r3) goto L_0x000e;	 Catch:{ InterruptedException -> 0x003f, all -> 0x005c }
        L_0x0019:
            r3 = r8.mArguments;	 Catch:{ InterruptedException -> 0x003f, all -> 0x005c }
            r3 = r3[r2];	 Catch:{ InterruptedException -> 0x003f, all -> 0x005c }
            if (r3 == 0) goto L_0x003c;	 Catch:{ InterruptedException -> 0x003f, all -> 0x005c }
        L_0x001f:
            r3 = r8.mDaemons;	 Catch:{ InterruptedException -> 0x003f, all -> 0x005c }
            r3 = r3[r2];	 Catch:{ InterruptedException -> 0x003f, all -> 0x005c }
            r3 = android.os.SystemService.isStopped(r3);	 Catch:{ InterruptedException -> 0x003f, all -> 0x005c }
            if (r3 == 0) goto L_0x003c;
        L_0x0029:
            r3 = r8.mDaemons;
            r5 = r3.length;
        L_0x002c:
            if (r4 >= r5) goto L_0x0036;
        L_0x002e:
            r0 = r3[r4];
            android.os.SystemService.stop(r0);
            r4 = r4 + 1;
            goto L_0x002c;
        L_0x0036:
            r3 = r8.this$0;
            r3.agentDisconnect();
            return;
        L_0x003c:
            r2 = r2 + 1;
            goto L_0x0014;
        L_0x003f:
            r1 = move-exception;
            r3 = "LegacyVpnRunner";	 Catch:{ InterruptedException -> 0x003f, all -> 0x005c }
            r5 = "interrupted during monitorDaemons(); stopping services";	 Catch:{ InterruptedException -> 0x003f, all -> 0x005c }
            android.util.Log.d(r3, r5);	 Catch:{ InterruptedException -> 0x003f, all -> 0x005c }
            r3 = r8.mDaemons;
            r5 = r3.length;
        L_0x004c:
            if (r4 >= r5) goto L_0x0056;
        L_0x004e:
            r0 = r3[r4];
            android.os.SystemService.stop(r0);
            r4 = r4 + 1;
            goto L_0x004c;
        L_0x0056:
            r3 = r8.this$0;
            r3.agentDisconnect();
            return;
        L_0x005c:
            r3 = move-exception;
            r5 = r8.mDaemons;
            r6 = r5.length;
        L_0x0060:
            if (r4 >= r6) goto L_0x006a;
        L_0x0062:
            r0 = r5[r4];
            android.os.SystemService.stop(r0);
            r4 = r4 + 1;
            goto L_0x0060;
        L_0x006a:
            r4 = r8.this$0;
            r4.agentDisconnect();
            throw r3;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.Vpn.LegacyVpnRunner.monitorDaemons():void");
        }

        public LegacyVpnRunner(Vpn this$0, VpnConfig config, String[] racoon, String[] mtpd) {
            int i = 0;
            this.this$0 = this$0;
            super(TAG);
            this.mOuterConnection = new AtomicInteger(-1);
            this.mTimer = -1;
            this.mBroadcastReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (LegacyVpnRunner.this.this$0.mEnableTeardown && intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE") && intent.getIntExtra("networkType", -1) == LegacyVpnRunner.this.mOuterConnection.get()) {
                        NetworkInfo info = (NetworkInfo) intent.getExtra("networkInfo");
                        if (!(info == null || info.isConnectedOrConnecting())) {
                            try {
                                LegacyVpnRunner.this.this$0.mObserver.interfaceStatusChanged(LegacyVpnRunner.this.mOuterInterface, false);
                            } catch (RemoteException e) {
                            }
                        }
                    }
                }
            };
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
            for (LocalSocket socket : this.mSockets) {
                IoUtils.closeQuietly(socket);
            }
            this.this$0.agentDisconnect();
            try {
                this.this$0.mContext.unregisterReceiver(this.mBroadcastReceiver);
            } catch (IllegalArgumentException e) {
            }
        }

        public void run() {
            Log.v(TAG, "Waiting");
            synchronized (TAG) {
                Log.v(TAG, "Executing");
                execute();
                monitorDaemons();
            }
        }

        private void checkpoint(boolean yield) throws InterruptedException {
            long now = SystemClock.elapsedRealtime();
            if (this.mTimer == -1) {
                this.mTimer = now;
                Thread.sleep(1);
            } else if (now - this.mTimer <= 60000) {
                Thread.sleep((long) (yield ? 200 : 1));
            } else {
                this.this$0.updateState(DetailedState.FAILED, "checkpoint");
                throw new IllegalStateException("Time is up");
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void execute() {
            int length;
            int length2;
            String daemon;
            boolean initFinished = false;
            checkpoint(false);
            for (String daemon2 : this.mDaemons) {
                while (!SystemService.isStopped(daemon2)) {
                    checkpoint(Vpn.LOGD);
                }
            }
            File state = new File("/data/misc/vpn/state");
            state.delete();
            if (state.exists()) {
                throw new IllegalStateException("Cannot delete the state");
            }
            String[] arguments;
            new File("/data/misc/vpn/abort").delete();
            initFinished = Vpn.LOGD;
            boolean restart = false;
            for (String[] arguments2 : this.mArguments) {
                restart = (restart || arguments2 != null) ? Vpn.LOGD : false;
            }
            if (restart) {
                this.this$0.updateState(DetailedState.CONNECTING, "execute");
                int i = 0;
                loop6:
                while (true) {
                    length = this.mDaemons.length;
                    if (i >= r0) {
                        break;
                    }
                    arguments2 = this.mArguments[i];
                    if (arguments2 != null) {
                        daemon2 = this.mDaemons[i];
                        SystemService.start(daemon2);
                        while (!SystemService.isRunning(daemon2)) {
                            checkpoint(Vpn.LOGD);
                        }
                        this.mSockets[i] = new LocalSocket();
                        LocalSocketAddress address = new LocalSocketAddress(daemon2, Namespace.RESERVED);
                        while (true) {
                            try {
                                this.mSockets[i].connect(address);
                                break;
                            } catch (Exception e) {
                                checkpoint(Vpn.LOGD);
                            }
                        }
                        this.mSockets[i].setSoTimeout(com.android.server.SystemService.PHASE_SYSTEM_SERVICES_READY);
                        OutputStream out = this.mSockets[i].getOutputStream();
                        for (String argument : arguments2) {
                            byte[] bytes = argument.getBytes(StandardCharsets.UTF_8);
                            length2 = bytes.length;
                            if (r0 >= 65535) {
                                break loop6;
                            }
                            out.write(bytes.length >> 8);
                            out.write(bytes.length);
                            out.write(bytes);
                            checkpoint(false);
                        }
                        out.write(RampAnimator.DEFAULT_MAX_BRIGHTNESS);
                        out.write(RampAnimator.DEFAULT_MAX_BRIGHTNESS);
                        out.flush();
                        InputStream in = this.mSockets[i].getInputStream();
                        while (in.read() != -1) {
                            checkpoint(Vpn.LOGD);
                        }
                        continue;
                    }
                    i++;
                }
                loop11:
                while (!state.exists()) {
                    i = 0;
                    while (true) {
                        length = this.mDaemons.length;
                        if (i >= r0) {
                            break;
                        }
                        daemon2 = this.mDaemons[i];
                        if (this.mArguments[i] != null && !SystemService.isRunning(daemon2)) {
                            break loop11;
                        }
                        i++;
                    }
                    throw new IllegalStateException(daemon2 + " is dead");
                }
                String[] parameters = FileUtils.readTextFile(state, 0, null).split("\n", -1);
                length = parameters.length;
                if (r0 != 7) {
                    throw new IllegalStateException("Cannot parse the state");
                }
                this.this$0.mConfig.interfaze = parameters[0].trim();
                this.this$0.mConfig.addLegacyAddresses(parameters[1]);
                if (this.this$0.mConfig.routes != null) {
                }
                this.this$0.mConfig.addLegacyRoutes(parameters[2]);
                if (this.this$0.mConfig.dnsServers != null) {
                }
                String dnsServers = parameters[3].trim();
                if (!dnsServers.isEmpty()) {
                    this.this$0.mConfig.dnsServers = Arrays.asList(dnsServers.split(" "));
                }
                if (this.this$0.mConfig.searchDomains != null) {
                }
                String searchDomains = parameters[4].trim();
                if (!searchDomains.isEmpty()) {
                    this.this$0.mConfig.searchDomains = Arrays.asList(searchDomains.split(" "));
                }
                String endpoint = parameters[5];
                if (!endpoint.isEmpty()) {
                    try {
                        InetAddress addr = InetAddress.parseNumericAddress(endpoint);
                        if (addr instanceof Inet4Address) {
                            this.this$0.mConfig.routes.add(new RouteInfo(new IpPrefix(addr, 32), 9));
                        } else if (addr instanceof Inet6Address) {
                            this.this$0.mConfig.routes.add(new RouteInfo(new IpPrefix(addr, DumpState.DUMP_PACKAGES), 9));
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
                if (1 == null) {
                    for (String daemon22 : this.mDaemons) {
                        SystemService.stop(daemon22);
                    }
                }
                if (1 != null) {
                }
                this.this$0.agentDisconnect();
                return;
            }
            this.this$0.agentDisconnect();
            if (1 == null) {
                for (String daemon222 : this.mDaemons) {
                    SystemService.stop(daemon222);
                }
            }
            if (1 != null) {
            }
            this.this$0.agentDisconnect();
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
        this.mEnableTeardown = LOGD;
        this.mAlwaysOn = false;
        this.mLockdown = false;
        this.mVpnUsers = null;
        this.mBlockedUsers = new ArraySet();
        this.mPackageIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean isPackageRemoved = false;
                String packageName = null;
                Uri data = intent.getData();
                if (data != null) {
                    packageName = data.getSchemeSpecificPart();
                }
                if (packageName != null) {
                    synchronized (Vpn.this) {
                        if (packageName.equals(Vpn.this.getAlwaysOnPackage())) {
                            String action = intent.getAction();
                            Log.i(Vpn.TAG, "Received broadcast " + action + " for always-on package " + packageName + " in user " + Vpn.this.mUserHandle);
                            if (action.equals("android.intent.action.PACKAGE_REPLACED")) {
                                Vpn.this.startAlwaysOnVpn();
                            } else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                                if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                                    isPackageRemoved = Vpn.LOGD;
                                }
                                if (isPackageRemoved) {
                                    Vpn.this.setAndSaveAlwaysOnPackage(null, false);
                                }
                            }
                            return;
                        }
                    }
                }
            }
        };
        this.mIsPackageIntentReceiverRegistered = false;
        this.mObserver = new BaseNetworkObserver() {
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

    private void updateState(DetailedState detailedState, String reason) {
        Log.d(TAG, "setting state=" + detailedState + ", reason=" + reason);
        this.mNetworkInfo.setDetailedState(detailedState, reason, null);
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
        }
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
        } else if (!setPackageAuthorization(packageName, LOGD)) {
            return false;
        } else {
            this.mAlwaysOn = LOGD;
        }
        if (!this.mAlwaysOn) {
            lockdown = false;
        }
        this.mLockdown = lockdown;
        if (!isCurrentPreparedPackage(packageName)) {
            prepareInternal(packageName);
        }
        maybeRegisterPackageChangeReceiverLocked(packageName);
        setVpnForcedLocked(this.mLockdown);
        return LOGD;
    }

    private static boolean isNullOrLegacyVpn(String packageName) {
        return packageName != null ? "[Legacy VPN]".equals(packageName) : LOGD;
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
            this.mIsPackageIntentReceiverRegistered = LOGD;
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
        return LOGD;
    }

    public boolean startAlwaysOnVpn() {
        boolean z = LOGD;
        synchronized (this) {
            String alwaysOnPackage = getAlwaysOnPackage();
            if (alwaysOnPackage == null) {
                return LOGD;
            } else if (getNetworkInfo().isConnected()) {
                return LOGD;
            } else {
                Intent serviceIntent = new Intent("android.net.VpnService");
                serviceIntent.setPackage(alwaysOnPackage);
                try {
                    if (this.mContext.startServiceAsUser(serviceIntent, UserHandle.of(this.mUserHandle)) == null) {
                        z = false;
                    }
                    return z;
                } catch (RuntimeException e) {
                    Log.e(TAG, "VpnService " + serviceIntent + " failed to start", e);
                    return false;
                }
            }
        }
    }

    public synchronized boolean prepare(String oldPackage, String newPackage) {
        if (oldPackage != null) {
            if (this.mAlwaysOn && !isCurrentPreparedPackage(oldPackage)) {
                return false;
            }
            if (isCurrentPreparedPackage(oldPackage)) {
                if (!(oldPackage.equals("[Legacy VPN]") || isVpnUserPreConsented(oldPackage))) {
                    prepareInternal("[Legacy VPN]");
                    return false;
                }
            } else if (oldPackage.equals("[Legacy VPN]") || !isVpnUserPreConsented(oldPackage)) {
                return false;
            } else {
                prepareInternal(oldPackage);
                return LOGD;
            }
        }
        if (newPackage == null || (!newPackage.equals("[Legacy VPN]") && isCurrentPreparedPackage(newPackage))) {
            return LOGD;
        }
        enforceControlPermission();
        if (this.mAlwaysOn && !isCurrentPreparedPackage(newPackage)) {
            return false;
        }
        prepareInternal(newPackage);
        return LOGD;
    }

    private boolean isCurrentPreparedPackage(String packageName) {
        return getAppUid(packageName, this.mUserHandle) == this.mOwnerUID ? LOGD : false;
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
        boolean z = LOGD;
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
            return LOGD;
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
        boolean allowIPv4 = this.mConfig.allowIPv4;
        boolean allowIPv6 = this.mConfig.allowIPv6;
        LinkProperties lp = new LinkProperties();
        lp.setInterfaceName(this.mInterface);
        if (this.mConfig.addresses != null) {
            for (LinkAddress address : this.mConfig.addresses) {
                lp.addLinkAddress(address);
                allowIPv4 |= address.getAddress() instanceof Inet4Address;
                allowIPv6 |= address.getAddress() instanceof Inet6Address;
            }
        }
        if (this.mConfig.routes != null) {
            for (RouteInfo route : this.mConfig.routes) {
                lp.addRoute(route);
                InetAddress address2 = route.getDestination().getAddress();
                allowIPv4 |= address2 instanceof Inet4Address;
                allowIPv6 |= address2 instanceof Inet6Address;
            }
        }
        if (this.mConfig.dnsServers != null) {
            for (String dnsServer : this.mConfig.dnsServers) {
                address2 = InetAddress.parseNumericAddress(dnsServer);
                lp.addDnsServer(address2);
                allowIPv4 |= address2 instanceof Inet4Address;
                allowIPv6 |= address2 instanceof Inet6Address;
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
        if (this.mConfig.allowBypass && !this.mLockdown) {
            z = LOGD;
        }
        networkMisc.allowBypass = z;
        long token = Binder.clearCallingIdentity();
        try {
            this.mNetworkAgent = new AnonymousClass3(this.mLooper, this.mContext, NETWORKTYPE, this.mNetworkInfo, this.mNetworkCapabilities, lp, 0, networkMisc);
            this.mVpnUsers = createUserAndRestrictedProfilesRanges(this.mUserHandle, this.mConfig.allowedApplications, this.mConfig.disallowedApplications);
            this.mNetworkAgent.addUidRanges((UidRange[]) this.mVpnUsers.toArray(new UidRange[this.mVpnUsers.size()]));
            this.mNetworkInfo.setIsAvailable(LOGD);
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

    private void agentDisconnect(NetworkInfo networkInfo, NetworkAgent networkAgent) {
        networkInfo.setIsAvailable(false);
        networkInfo.setDetailedState(DetailedState.DISCONNECTED, null, null);
        if (networkAgent != null) {
            networkAgent.sendNetworkInfo(networkInfo);
        }
    }

    private void agentDisconnect(NetworkAgent networkAgent) {
        agentDisconnect(new NetworkInfo(this.mNetworkInfo), networkAgent);
    }

    private void agentDisconnect() {
        if (this.mNetworkInfo.isConnected()) {
            agentDisconnect(this.mNetworkInfo, this.mNetworkAgent);
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
            }
            if ("android.permission.BIND_VPN_SERVICE".equals(info.serviceInfo.permission)) {
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
                    Connection connection = new Connection(null);
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
                        if (!(oldInterface == null || oldInterface.equals(interfaze))) {
                            jniReset(oldInterface);
                        }
                        IoUtils.setBlocking(tun.getFileDescriptor(), config.blocking);
                        String str = config.user;
                        Log.i(TAG, "Established by " + r0 + " on " + this.mInterface);
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
            }
            throw new SecurityException(config.user + " does not require " + "android.permission.BIND_VPN_SERVICE");
        } catch (RemoteException e3) {
            throw new SecurityException("Cannot find " + config.user);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    private boolean isRunningLocked() {
        return (this.mNetworkAgent == null || this.mInterface == null) ? false : LOGD;
    }

    private boolean isCallerEstablishedOwnerLocked() {
        return (isRunningLocked() && Binder.getCallingUid() == this.mOwnerUID) ? LOGD : false;
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
                List<UserInfo> users = UserManager.get(this.mContext).getUsers(LOGD);
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
        }
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
        }
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
            setAllowOnlyVpnForUids(LOGD, addedRanges);
            return;
        }
        setAllowOnlyVpnForUids(false, removedRanges);
    }

    @GuardedBy("this")
    private boolean setAllowOnlyVpnForUids(boolean enforce, Collection<UidRange> ranges) {
        if (ranges.size() == 0) {
            return LOGD;
        }
        try {
            this.mNetd.setAllowOnlyVpnForUids(enforce, (UidRange[]) ranges.toArray(new UidRange[ranges.size()]));
            if (enforce) {
                this.mBlockedUsers.addAll(ranges);
            } else {
                this.mBlockedUsers.removeAll(ranges);
            }
            return LOGD;
        } catch (Exception e) {
            Log.e(TAG, "Updating blocked=" + enforce + " for UIDs " + Arrays.toString(ranges.toArray()) + " failed", e);
            return false;
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
        return LOGD;
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
                return LOGD;
            }
        }
        return false;
    }

    public synchronized boolean isBlockingUid(int uid) {
        boolean z = false;
        synchronized (this) {
            if (!this.mLockdown) {
                return false;
            } else if (this.mNetworkInfo.isConnected()) {
                if (!appliesToUid(uid)) {
                    z = LOGD;
                }
                return z;
            } else {
                for (UidRange uidRange : this.mBlockedUsers) {
                    if (uidRange.contains(uid)) {
                        return LOGD;
                    }
                }
                return false;
            }
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
        if (!mgr.getUserInfo(this.mUserHandle).isRestricted()) {
            if (!mgr.hasUserRestriction("no_config_vpn", new UserHandle(this.mUserHandle))) {
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
                    case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                        racoon = new String[]{iface, profile.server, "udppsk", profile.ipsecIdentifier, profile.ipsecSecret, "1701"};
                        break;
                    case H.REPORT_LOSING_FOCUS /*3*/:
                        racoon = new String[]{iface, profile.server, "udprsa", privateKey, userCert, caCert, serverCert, "1701"};
                        break;
                    case H.DO_TRAVERSAL /*4*/:
                        racoon = new String[]{iface, profile.server, "xauthpsk", profile.ipsecIdentifier, profile.ipsecSecret, profile.username, profile.password, "", gateway};
                        break;
                    case H.ADD_STARTING /*5*/:
                        racoon = new String[]{iface, profile.server, "xauthrsa", privateKey, userCert, caCert, serverCert, profile.username, profile.password, "", gateway};
                        break;
                    case H.REMOVE_STARTING /*6*/:
                        racoon = new String[]{iface, profile.server, "hybridrsa", caCert, serverCert, profile.username, profile.password, "", gateway};
                        break;
                }
                String[] mtpd = null;
                switch (profile.type) {
                    case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
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
                    case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                    case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                    case H.REPORT_LOSING_FOCUS /*3*/:
                        mtpd = new String[]{iface, "l2tp", profile.server, "1701", profile.l2tpSecret, "name", profile.username, "password", profile.password, "linkname", "vpn", "refuse-eap", "nodefaultroute", "usepeerdns", "idle", "1800", "mtu", "1400", "mru", "1400"};
                        break;
                }
                VpnConfig config = new VpnConfig();
                config.legacy = LOGD;
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
                return;
            }
        }
        throw new SecurityException("Restricted users cannot establish VPNs");
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
        return info;
    }

    public VpnConfig getLegacyVpnConfig() {
        if (this.mLegacyVpnRunner != null) {
            return this.mConfig;
        }
        return null;
    }
}
