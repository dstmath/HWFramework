package com.android.server.connectivity;

import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.net.ConnectivityManager;
import android.net.INetworkManagementEventObserver;
import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.Network;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkMisc;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.UidRange;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
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
import android.provider.Settings;
import android.security.KeyStore;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnConfig;
import com.android.internal.net.VpnInfo;
import com.android.internal.net.VpnProfile;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.util.ArrayUtils;
import com.android.server.net.BaseNetworkObserver;
import com.android.server.usb.UsbAudioDevice;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import libcore.io.IoUtils;

public class Vpn {
    private static final boolean LOGD = true;
    private static final int MAX_ROUTES_TO_EVALUATE = 150;
    private static final long MOST_IPV4_ADDRESSES_COUNT = 3650722201L;
    private static final BigInteger MOST_IPV6_ADDRESSES_COUNT = BigInteger.ONE.shiftLeft(128).multiply(BigInteger.valueOf(85)).divide(BigInteger.valueOf(100));
    private static final String NETWORKTYPE = "VPN";
    private static final String TAG = "Vpn";
    private static final long VPN_LAUNCH_IDLE_WHITELIST_DURATION_MS = 60000;
    private boolean mAlwaysOn;
    @GuardedBy("this")
    private Set<UidRange> mBlockedUsers;
    @VisibleForTesting
    protected VpnConfig mConfig;
    /* access modifiers changed from: private */
    public Connection mConnection;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public volatile boolean mEnableTeardown;
    /* access modifiers changed from: private */
    public String mInterface;
    private boolean mIsPackageIntentReceiverRegistered;
    /* access modifiers changed from: private */
    public LegacyVpnRunner mLegacyVpnRunner;
    private boolean mLockdown;
    private final Looper mLooper;
    private final INetworkManagementService mNetd;
    @VisibleForTesting
    protected NetworkAgent mNetworkAgent;
    @VisibleForTesting
    protected final NetworkCapabilities mNetworkCapabilities;
    /* access modifiers changed from: private */
    public NetworkInfo mNetworkInfo;
    /* access modifiers changed from: private */
    public INetworkManagementEventObserver mObserver;
    private int mOwnerUID;
    private String mPackage;
    private final BroadcastReceiver mPackageIntentReceiver;
    /* access modifiers changed from: private */
    public PendingIntent mStatusIntent;
    private final SystemServices mSystemServices;
    /* access modifiers changed from: private */
    public final int mUserHandle;

    private class Connection implements ServiceConnection {
        /* access modifiers changed from: private */
        public IBinder mService;

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
        private long mBringupStartTime = -1;
        private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (Vpn.this.mEnableTeardown && intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE") && intent.getIntExtra("networkType", -1) == LegacyVpnRunner.this.mOuterConnection.get()) {
                    NetworkInfo info = (NetworkInfo) intent.getExtra("networkInfo");
                    if (info != null && !info.isConnectedOrConnecting()) {
                        try {
                            Vpn.this.mObserver.interfaceStatusChanged(LegacyVpnRunner.this.mOuterInterface, false);
                        } catch (RemoteException e) {
                        }
                    }
                }
            }
        };
        private final String[] mDaemons;
        /* access modifiers changed from: private */
        public final AtomicInteger mOuterConnection = new AtomicInteger(-1);
        /* access modifiers changed from: private */
        public final String mOuterInterface;
        private final LocalSocket[] mSockets;

        public LegacyVpnRunner(VpnConfig config, String[] racoon, String[] mtpd) {
            super(TAG);
            Vpn.this.mConfig = config;
            this.mDaemons = new String[]{"racoon", "mtpd"};
            this.mArguments = new String[][]{racoon, mtpd};
            this.mSockets = new LocalSocket[this.mDaemons.length];
            this.mOuterInterface = Vpn.this.mConfig.interfaze;
            if (!TextUtils.isEmpty(this.mOuterInterface)) {
                ConnectivityManager cm = ConnectivityManager.from(Vpn.this.mContext);
                for (Network network : cm.getAllNetworks()) {
                    LinkProperties lp = cm.getLinkProperties(network);
                    if (lp != null && lp.getAllInterfaceNames().contains(this.mOuterInterface)) {
                        NetworkInfo networkInfo = cm.getNetworkInfo(network);
                        if (networkInfo != null) {
                            this.mOuterConnection.set(networkInfo.getType());
                        }
                    }
                }
            }
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            Vpn.this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        }

        public void check(String interfaze) {
            if (interfaze.equals(this.mOuterInterface)) {
                Log.i(TAG, "Legacy VPN is going down with " + interfaze);
                exit();
            }
        }

        public void exit() {
            interrupt();
            Vpn.this.agentDisconnect();
            try {
                Vpn.this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            } catch (IllegalArgumentException e) {
            }
        }

        /*  JADX ERROR: StackOverflow in pass: MarkFinallyVisitor
            jadx.core.utils.exceptions.JadxOverflowException: 
            	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:47)
            	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:81)
            */
        /* JADX WARNING: Unknown top exception splitter block from list: {B:16:0x0030=Splitter:B:16:0x0030, B:28:0x0051=Splitter:B:28:0x0051, B:41:0x0073=Splitter:B:41:0x0073} */
        public void run() {
            /*
                r9 = this;
                java.lang.String r0 = "LegacyVpnRunner"
                java.lang.String r1 = "Waiting"
                android.util.Log.v(r0, r1)
                java.lang.String r0 = "LegacyVpnRunner"
                monitor-enter(r0)
                java.lang.String r1 = "LegacyVpnRunner"
                java.lang.String r2 = "Executing"
                android.util.Log.v(r1, r2)     // Catch:{ all -> 0x0088 }
                r1 = 50
                r3 = 0
                r9.bringup()     // Catch:{ InterruptedException -> 0x005f, all -> 0x003d }
                r9.waitForDaemonsToStop()     // Catch:{ InterruptedException -> 0x005f, all -> 0x003d }
                interrupted()     // Catch:{ InterruptedException -> 0x005f, all -> 0x003d }
                android.net.LocalSocket[] r4 = r9.mSockets     // Catch:{ all -> 0x0088 }
                int r5 = r4.length     // Catch:{ all -> 0x0088 }
                r6 = r3
            L_0x0021:
                if (r6 >= r5) goto L_0x002b
                r7 = r4[r6]     // Catch:{ all -> 0x0088 }
                libcore.io.IoUtils.closeQuietly(r7)     // Catch:{ all -> 0x0088 }
                int r6 = r6 + 1
                goto L_0x0021
            L_0x002b:
                java.lang.Thread.sleep(r1)     // Catch:{ InterruptedException -> 0x002f }
                goto L_0x0030
            L_0x002f:
                r1 = move-exception
            L_0x0030:
                java.lang.String[] r1 = r9.mDaemons     // Catch:{ all -> 0x0088 }
                int r2 = r1.length     // Catch:{ all -> 0x0088 }
            L_0x0033:
                if (r3 >= r2) goto L_0x0080
                r4 = r1[r3]     // Catch:{ all -> 0x0088 }
                android.os.SystemService.stop(r4)     // Catch:{ all -> 0x0088 }
                int r3 = r3 + 1
                goto L_0x0033
            L_0x003d:
                r4 = move-exception
                android.net.LocalSocket[] r5 = r9.mSockets     // Catch:{ all -> 0x0088 }
                int r6 = r5.length     // Catch:{ all -> 0x0088 }
                r7 = r3
            L_0x0042:
                if (r7 >= r6) goto L_0x004c
                r8 = r5[r7]     // Catch:{ all -> 0x0088 }
                libcore.io.IoUtils.closeQuietly(r8)     // Catch:{ all -> 0x0088 }
                int r7 = r7 + 1
                goto L_0x0042
            L_0x004c:
                java.lang.Thread.sleep(r1)     // Catch:{ InterruptedException -> 0x0050 }
                goto L_0x0051
            L_0x0050:
                r1 = move-exception
            L_0x0051:
                java.lang.String[] r1 = r9.mDaemons     // Catch:{ all -> 0x0088 }
                int r2 = r1.length     // Catch:{ all -> 0x0088 }
            L_0x0054:
                if (r3 >= r2) goto L_0x005e
                r5 = r1[r3]     // Catch:{ all -> 0x0088 }
                android.os.SystemService.stop(r5)     // Catch:{ all -> 0x0088 }
                int r3 = r3 + 1
                goto L_0x0054
            L_0x005e:
                throw r4     // Catch:{ all -> 0x0088 }
            L_0x005f:
                r4 = move-exception
                android.net.LocalSocket[] r4 = r9.mSockets     // Catch:{ all -> 0x0088 }
                int r5 = r4.length     // Catch:{ all -> 0x0088 }
                r6 = r3
            L_0x0064:
                if (r6 >= r5) goto L_0x006e
                r7 = r4[r6]     // Catch:{ all -> 0x0088 }
                libcore.io.IoUtils.closeQuietly(r7)     // Catch:{ all -> 0x0088 }
                int r6 = r6 + 1
                goto L_0x0064
            L_0x006e:
                java.lang.Thread.sleep(r1)     // Catch:{ InterruptedException -> 0x0072 }
                goto L_0x0073
            L_0x0072:
                r1 = move-exception
            L_0x0073:
                java.lang.String[] r1 = r9.mDaemons     // Catch:{ all -> 0x0088 }
                int r2 = r1.length     // Catch:{ all -> 0x0088 }
            L_0x0076:
                if (r3 >= r2) goto L_0x0080
                r4 = r1[r3]     // Catch:{ all -> 0x0088 }
                android.os.SystemService.stop(r4)     // Catch:{ all -> 0x0088 }
                int r3 = r3 + 1
                goto L_0x0076
            L_0x0080:
                com.android.server.connectivity.Vpn r1 = com.android.server.connectivity.Vpn.this     // Catch:{ all -> 0x0088 }
                r1.agentDisconnect()     // Catch:{ all -> 0x0088 }
                monitor-exit(r0)     // Catch:{ all -> 0x0088 }
                return
            L_0x0088:
                r1 = move-exception
                monitor-exit(r0)     // Catch:{ all -> 0x0088 }
                throw r1
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.Vpn.LegacyVpnRunner.run():void");
        }

        private void checkInterruptAndDelay(boolean sleepLonger) throws InterruptedException {
            if (SystemClock.elapsedRealtime() - this.mBringupStartTime <= 60000) {
                Thread.sleep(sleepLonger ? 200 : 1);
            } else {
                Vpn.this.updateState(NetworkInfo.DetailedState.FAILED, "checkpoint");
                throw new IllegalStateException("VPN bringup took too long");
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:101:0x021e, code lost:
            if ((r0 instanceof java.net.Inet6Address) == false) goto L_0x0236;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:102:0x0220, code lost:
            r1.this$0.mConfig.routes.add(new android.net.RouteInfo(new android.net.IpPrefix(r0, 128), 9));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:103:0x0236, code lost:
            android.util.Log.e(TAG, "Unknown IP address family for VPN endpoint: " + r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:104:0x024d, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:107:?, code lost:
            android.util.Log.e(TAG, "Exception constructing throw route to " + r6 + ": " + r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:123:0x02cf, code lost:
            throw new java.lang.IllegalStateException("Cannot parse the state");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:60:0x0113, code lost:
            if (r4.exists() != false) goto L_0x014b;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:61:0x0115, code lost:
            r0 = 0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:63:0x0119, code lost:
            if (r0 >= r1.mDaemons.length) goto L_0x0146;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:64:0x011b, code lost:
            r5 = r1.mDaemons[r0];
         */
        /* JADX WARNING: Code restructure failed: missing block: B:65:0x0123, code lost:
            if (r1.mArguments[r0] == null) goto L_0x0143;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:67:0x0129, code lost:
            if (android.os.SystemService.isRunning(r5) == false) goto L_0x012c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:70:0x0142, code lost:
            throw new java.lang.IllegalStateException(r5 + " is dead");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:71:0x0143, code lost:
            r0 = r0 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:72:0x0146, code lost:
            checkInterruptAndDelay(true);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:73:0x014b, code lost:
            r5 = android.os.FileUtils.readTextFile(r4, 0, null).split("\n", -1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:74:0x015a, code lost:
            if (r5.length != 7) goto L_0x02c8;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:75:0x015c, code lost:
            r1.this$0.mConfig.interfaze = r5[0].trim();
            r1.this$0.mConfig.addLegacyAddresses(r5[1]);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:76:0x0178, code lost:
            if (r1.this$0.mConfig.routes == null) goto L_0x0186;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:78:0x0184, code lost:
            if (r1.this$0.mConfig.routes.isEmpty() == false) goto L_0x0190;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:79:0x0186, code lost:
            r1.this$0.mConfig.addLegacyRoutes(r5[2]);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:81:0x0196, code lost:
            if (r1.this$0.mConfig.dnsServers == null) goto L_0x01a4;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:83:0x01a2, code lost:
            if (r1.this$0.mConfig.dnsServers.size() != 0) goto L_0x01c1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:84:0x01a4, code lost:
            r0 = r5[3].trim();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:85:0x01af, code lost:
            if (r0.isEmpty() != false) goto L_0x01c1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:86:0x01b1, code lost:
            r1.this$0.mConfig.dnsServers = java.util.Arrays.asList(r0.split(" "));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:88:0x01c7, code lost:
            if (r1.this$0.mConfig.searchDomains == null) goto L_0x01d5;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:90:0x01d3, code lost:
            if (r1.this$0.mConfig.searchDomains.size() != 0) goto L_0x01f2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:91:0x01d5, code lost:
            r0 = r5[4].trim();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:92:0x01e0, code lost:
            if (r0.isEmpty() != false) goto L_0x01f2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:93:0x01e2, code lost:
            r1.this$0.mConfig.searchDomains = java.util.Arrays.asList(r0.split(" "));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:94:0x01f2, code lost:
            r6 = r5[5];
         */
        /* JADX WARNING: Code restructure failed: missing block: B:95:0x01fa, code lost:
            if (r6.isEmpty() != false) goto L_0x026c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:97:?, code lost:
            r0 = java.net.InetAddress.parseNumericAddress(r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:98:0x0204, code lost:
            if ((r0 instanceof java.net.Inet4Address) == false) goto L_0x021c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:99:0x0206, code lost:
            r1.this$0.mConfig.routes.add(new android.net.RouteInfo(new android.net.IpPrefix(r0, 32), 9));
         */
        private void bringup() {
            boolean z;
            boolean z2;
            try {
                this.mBringupStartTime = SystemClock.elapsedRealtime();
                String[] strArr = this.mDaemons;
                int length = strArr.length;
                int i = 0;
                while (true) {
                    z = true;
                    if (i >= length) {
                        break;
                    }
                    String daemon = strArr[i];
                    while (!SystemService.isStopped(daemon)) {
                        checkInterruptAndDelay(true);
                    }
                    i++;
                }
                File state = new File("/data/misc/vpn/state");
                state.delete();
                if (!state.exists()) {
                    new File("/data/misc/vpn/abort").delete();
                    boolean restart = false;
                    for (String[] arguments : this.mArguments) {
                        if (!restart) {
                            if (arguments == null) {
                                z2 = false;
                                restart = z2;
                            }
                        }
                        z2 = true;
                        restart = z2;
                    }
                    if (!restart) {
                        Vpn.this.agentDisconnect();
                        return;
                    }
                    Vpn.this.updateState(NetworkInfo.DetailedState.CONNECTING, "execute");
                    int i2 = 0;
                    while (true) {
                        int i3 = i2;
                        if (i3 >= this.mDaemons.length) {
                            break;
                        }
                        String[] arguments2 = this.mArguments[i3];
                        if (arguments2 != null) {
                            String daemon2 = this.mDaemons[i3];
                            SystemService.start(daemon2);
                            while (!SystemService.isRunning(daemon2)) {
                                checkInterruptAndDelay(z);
                            }
                            this.mSockets[i3] = new LocalSocket();
                            LocalSocketAddress address = new LocalSocketAddress(daemon2, LocalSocketAddress.Namespace.RESERVED);
                            while (true) {
                                LocalSocketAddress address2 = address;
                                try {
                                    this.mSockets[i3].connect(address2);
                                    break;
                                } catch (Exception e) {
                                    checkInterruptAndDelay(true);
                                    address = address2;
                                }
                            }
                            this.mSockets[i3].setSoTimeout(500);
                            OutputStream out = this.mSockets[i3].getOutputStream();
                            int length2 = arguments2.length;
                            int i4 = 0;
                            while (i4 < length2) {
                                byte[] bytes = arguments2[i4].getBytes(StandardCharsets.UTF_8);
                                if (bytes.length < 65535) {
                                    out.write(bytes.length >> 8);
                                    out.write(bytes.length);
                                    out.write(bytes);
                                    checkInterruptAndDelay(false);
                                    i4++;
                                } else {
                                    throw new IllegalArgumentException("Argument is too large");
                                }
                            }
                            out.write(255);
                            out.write(255);
                            out.flush();
                            InputStream in = this.mSockets[i3].getInputStream();
                            while (true) {
                                InputStream in2 = in;
                                try {
                                    if (in2.read() == -1) {
                                        break;
                                    }
                                } catch (Exception e2) {
                                }
                                checkInterruptAndDelay(true);
                                in = in2;
                            }
                        }
                        i2 = i3 + 1;
                        z = true;
                    }
                }
                throw new IllegalStateException("Cannot delete the state");
                synchronized (Vpn.this) {
                    Vpn.this.mConfig.startTime = SystemClock.elapsedRealtime();
                    checkInterruptAndDelay(false);
                    if (Vpn.this.jniCheck(Vpn.this.mConfig.interfaze) != 0) {
                        String unused = Vpn.this.mInterface = Vpn.this.mConfig.interfaze;
                        Vpn.this.prepareStatusIntent();
                        Vpn.this.agentConnect();
                        Log.i(TAG, "Connected!");
                    } else {
                        throw new IllegalStateException(Vpn.this.mConfig.interfaze + " is gone");
                    }
                }
            } catch (Exception e3) {
                Log.i(TAG, "Aborting", e3);
                Vpn.this.updateState(NetworkInfo.DetailedState.FAILED, e3.getMessage());
                exit();
            }
        }

        private void waitForDaemonsToStop() throws InterruptedException {
            if (!Vpn.this.mNetworkInfo.isConnected()) {
                return;
            }
            while (true) {
                Thread.sleep(2000);
                int i = 0;
                while (true) {
                    if (i < this.mDaemons.length) {
                        if (this.mArguments[i] == null || !SystemService.isStopped(this.mDaemons[i])) {
                            i++;
                        } else {
                            return;
                        }
                    }
                }
            }
        }
    }

    @VisibleForTesting
    public static class SystemServices {
        private final Context mContext;

        public SystemServices(Context context) {
            this.mContext = context;
        }

        public PendingIntent pendingIntentGetActivityAsUser(Intent intent, int flags, UserHandle user) {
            return PendingIntent.getActivityAsUser(this.mContext, 0, intent, flags, null, user);
        }

        public void settingsSecurePutStringForUser(String key, String value, int userId) {
            Settings.Secure.putStringForUser(this.mContext.getContentResolver(), key, value, userId);
        }

        public void settingsSecurePutIntForUser(String key, int value, int userId) {
            Settings.Secure.putIntForUser(this.mContext.getContentResolver(), key, value, userId);
        }

        public String settingsSecureGetStringForUser(String key, int userId) {
            return Settings.Secure.getStringForUser(this.mContext.getContentResolver(), key, userId);
        }

        public int settingsSecureGetIntForUser(String key, int def, int userId) {
            return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), key, def, userId);
        }
    }

    private native boolean jniAddAddress(String str, String str2, int i);

    /* access modifiers changed from: private */
    public native int jniCheck(String str);

    private native int jniCreate(int i);

    private native boolean jniDelAddress(String str, String str2, int i);

    private native String jniGetName(int i);

    private native void jniReset(String str);

    private native int jniSetAddresses(String str, String str2);

    public Vpn(Looper looper, Context context, INetworkManagementService netService, int userHandle) {
        this(looper, context, netService, userHandle, new SystemServices(context));
    }

    @VisibleForTesting
    protected Vpn(Looper looper, Context context, INetworkManagementService netService, int userHandle, SystemServices systemServices) {
        this.mEnableTeardown = true;
        this.mAlwaysOn = false;
        this.mLockdown = false;
        this.mBlockedUsers = new ArraySet();
        this.mPackageIntentReceiver = new BroadcastReceiver() {
            /* JADX WARNING: Code restructure failed: missing block: B:31:0x0090, code lost:
                return;
             */
            public void onReceive(Context context, Intent intent) {
                Uri data = intent.getData();
                String packageName = data == null ? null : data.getSchemeSpecificPart();
                if (packageName != null) {
                    synchronized (Vpn.this) {
                        if (packageName.equals(Vpn.this.getAlwaysOnPackage())) {
                            String action = intent.getAction();
                            Log.i(Vpn.TAG, "Received broadcast " + action + " for always-on VPN package " + packageName + " in user " + Vpn.this.mUserHandle);
                            char c = 65535;
                            int hashCode = action.hashCode();
                            if (hashCode != -810471698) {
                                if (hashCode == 525384130) {
                                    if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                                        c = 1;
                                    }
                                }
                            } else if (action.equals("android.intent.action.PACKAGE_REPLACED")) {
                                c = 0;
                            }
                            switch (c) {
                                case 0:
                                    Vpn.this.startAlwaysOnVpn();
                                    break;
                                case 1:
                                    if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                                        Vpn.this.setAlwaysOnPackage(null, false);
                                        break;
                                    }
                                    break;
                            }
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
                        try {
                            if (Vpn.this.mLegacyVpnRunner != null) {
                                Vpn.this.mLegacyVpnRunner.check(interfaze);
                            }
                        } catch (Throwable th) {
                            throw th;
                        }
                    }
                }
            }

            public void interfaceRemoved(String interfaze) {
                synchronized (Vpn.this) {
                    if (interfaze.equals(Vpn.this.mInterface) && Vpn.this.jniCheck(interfaze) == 0) {
                        PendingIntent unused = Vpn.this.mStatusIntent = null;
                        Vpn.this.mNetworkCapabilities.setUids(null);
                        Vpn.this.mConfig = null;
                        String unused2 = Vpn.this.mInterface = null;
                        if (Vpn.this.mConnection != null) {
                            Vpn.this.mContext.unbindService(Vpn.this.mConnection);
                            Connection unused3 = Vpn.this.mConnection = null;
                            Vpn.this.agentDisconnect();
                        } else if (Vpn.this.mLegacyVpnRunner != null) {
                            Vpn.this.mLegacyVpnRunner.exit();
                            LegacyVpnRunner unused4 = Vpn.this.mLegacyVpnRunner = null;
                        }
                    }
                }
            }
        };
        this.mContext = context;
        this.mNetd = netService;
        this.mUserHandle = userHandle;
        this.mLooper = looper;
        this.mSystemServices = systemServices;
        this.mPackage = "[Legacy VPN]";
        this.mOwnerUID = getAppUid(this.mPackage, this.mUserHandle);
        try {
            netService.registerObserver(this.mObserver);
        } catch (RemoteException e) {
            Log.wtf(TAG, "Problem registering observer", e);
        }
        this.mNetworkInfo = new NetworkInfo(17, 0, NETWORKTYPE, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        this.mNetworkCapabilities = new NetworkCapabilities();
        this.mNetworkCapabilities.addTransportType(4);
        this.mNetworkCapabilities.removeCapability(15);
        updateCapabilities();
        loadAlwaysOnPackage();
    }

    public void setEnableTeardown(boolean enableTeardown) {
        this.mEnableTeardown = enableTeardown;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void updateState(NetworkInfo.DetailedState detailedState, String reason) {
        Log.d(TAG, "setting state=" + detailedState + ", reason=" + reason);
        this.mNetworkInfo.setDetailedState(detailedState, reason, null);
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
        }
        updateAlwaysOnNotification(detailedState);
    }

    public void updateCapabilities() {
        updateCapabilities((ConnectivityManager) this.mContext.getSystemService(ConnectivityManager.class), this.mConfig != null ? this.mConfig.underlyingNetworks : null, this.mNetworkCapabilities);
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendNetworkCapabilities(this.mNetworkCapabilities);
        }
    }

    @VisibleForTesting
    public static void updateCapabilities(ConnectivityManager cm, Network[] underlyingNetworks, NetworkCapabilities caps) {
        int upKbps;
        Network[] networkArr = underlyingNetworks;
        NetworkCapabilities networkCapabilities = caps;
        boolean z = true;
        int[] transportTypes = {4};
        int downKbps = 0;
        boolean metered = false;
        boolean roaming = false;
        boolean congested = false;
        boolean hadUnderlyingNetworks = false;
        if (networkArr != null) {
            int length = networkArr.length;
            upKbps = 0;
            int downKbps2 = 0;
            int[] transportTypes2 = transportTypes;
            int i = 0;
            while (i < length) {
                Network underlying = networkArr[i];
                NetworkCapabilities underlyingCaps = cm.getNetworkCapabilities(underlying);
                if (underlyingCaps != null) {
                    hadUnderlyingNetworks = true;
                    int[] transportTypes3 = underlyingCaps.getTransportTypes();
                    int length2 = transportTypes3.length;
                    int[] transportTypes4 = transportTypes2;
                    int i2 = 0;
                    while (i2 < length2) {
                        transportTypes4 = ArrayUtils.appendInt(transportTypes4, transportTypes3[i2]);
                        i2++;
                        underlying = underlying;
                    }
                    downKbps2 = NetworkCapabilities.minBandwidth(downKbps2, underlyingCaps.getLinkDownstreamBandwidthKbps());
                    upKbps = NetworkCapabilities.minBandwidth(upKbps, underlyingCaps.getLinkUpstreamBandwidthKbps());
                    z = true;
                    metered |= !underlyingCaps.hasCapability(11);
                    roaming |= !underlyingCaps.hasCapability(18);
                    congested |= !underlyingCaps.hasCapability(20);
                    transportTypes2 = transportTypes4;
                }
                i++;
                networkArr = underlyingNetworks;
            }
            ConnectivityManager connectivityManager = cm;
            transportTypes = transportTypes2;
            downKbps = downKbps2;
        } else {
            ConnectivityManager connectivityManager2 = cm;
            upKbps = 0;
        }
        if (!hadUnderlyingNetworks) {
            metered = true;
            roaming = false;
            congested = false;
        }
        networkCapabilities.setTransportTypes(transportTypes);
        networkCapabilities.setLinkDownstreamBandwidthKbps(downKbps);
        networkCapabilities.setLinkUpstreamBandwidthKbps(upKbps);
        networkCapabilities.setCapability(11, !metered ? z : false);
        networkCapabilities.setCapability(18, !roaming ? z : false);
        if (congested) {
            z = false;
        }
        networkCapabilities.setCapability(20, z);
    }

    public synchronized void setLockdown(boolean lockdown) {
        enforceControlPermissionOrInternalCaller();
        setVpnForcedLocked(lockdown);
        this.mLockdown = lockdown;
        if (this.mAlwaysOn) {
            saveAlwaysOnPackage();
        }
    }

    public boolean isAlwaysOnPackageSupported(String packageName) {
        enforceSettingsPermission();
        if (packageName == null) {
            return false;
        }
        PackageManager pm = this.mContext.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = pm.getApplicationInfoAsUser(packageName, 0, this.mUserHandle);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Can't find \"" + packageName + "\" when checking always-on support");
        }
        if (appInfo == null || appInfo.targetSdkVersion < 24) {
            return false;
        }
        Intent intent = new Intent("android.net.VpnService");
        intent.setPackage(packageName);
        List<ResolveInfo> services = pm.queryIntentServicesAsUser(intent, 128, this.mUserHandle);
        if (services == null || services.size() == 0) {
            return false;
        }
        for (ResolveInfo rInfo : services) {
            Bundle metaData = rInfo.serviceInfo.metaData;
            if (metaData != null && !metaData.getBoolean("android.net.VpnService.SUPPORTS_ALWAYS_ON", true)) {
                return false;
            }
        }
        return true;
    }

    public synchronized boolean setAlwaysOnPackage(String packageName, boolean lockdown) {
        enforceControlPermissionOrInternalCaller();
        if (!setAlwaysOnPackageInternal(packageName, lockdown)) {
            return false;
        }
        saveAlwaysOnPackage();
        return true;
    }

    @GuardedBy("this")
    private boolean setAlwaysOnPackageInternal(String packageName, boolean lockdown) {
        boolean z = false;
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
        if (this.mAlwaysOn && lockdown) {
            z = true;
        }
        this.mLockdown = z;
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
        return packageName == null || "[Legacy VPN]".equals(packageName);
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
            intentFilter.addDataScheme("package");
            intentFilter.addDataSchemeSpecificPart(packageName, 0);
            this.mContext.registerReceiverAsUser(this.mPackageIntentReceiver, UserHandle.of(this.mUserHandle), intentFilter, null, null);
        }
    }

    public synchronized String getAlwaysOnPackage() {
        enforceControlPermissionOrInternalCaller();
        return this.mAlwaysOn ? this.mPackage : null;
    }

    @GuardedBy("this")
    private void saveAlwaysOnPackage() {
        long token = Binder.clearCallingIdentity();
        try {
            this.mSystemServices.settingsSecurePutStringForUser("always_on_vpn_app", getAlwaysOnPackage(), this.mUserHandle);
            this.mSystemServices.settingsSecurePutIntForUser("always_on_vpn_lockdown", (!this.mAlwaysOn || !this.mLockdown) ? 0 : 1, this.mUserHandle);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @GuardedBy("this")
    private void loadAlwaysOnPackage() {
        long token = Binder.clearCallingIdentity();
        try {
            String alwaysOnPackage = this.mSystemServices.settingsSecureGetStringForUser("always_on_vpn_app", this.mUserHandle);
            boolean alwaysOnLockdown = false;
            if (this.mSystemServices.settingsSecureGetIntForUser("always_on_vpn_lockdown", 0, this.mUserHandle) != 0) {
                alwaysOnLockdown = true;
            }
            setAlwaysOnPackageInternal(alwaysOnPackage, alwaysOnLockdown);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0024, code lost:
        r11 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        ((com.android.server.DeviceIdleController.LocalService) com.android.server.LocalServices.getService(com.android.server.DeviceIdleController.LocalService.class)).addPowerSaveTempWhitelistApp(android.os.Process.myUid(), r0, 60000, r13.mUserHandle, false, "vpn");
        r2 = new android.content.Intent("android.net.VpnService");
        r2.setPackage(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0058, code lost:
        if (r13.mContext.startServiceAsUser(r2, android.os.UserHandle.of(r13.mUserHandle)) == null) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005b, code lost:
        r9 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005f, code lost:
        return r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0060, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        android.util.Log.e(TAG, "VpnService " + r2 + " failed to start", r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0080, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0081, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0082, code lost:
        android.os.Binder.restoreCallingIdentity(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0085, code lost:
        throw r1;
     */
    public boolean startAlwaysOnVpn() {
        synchronized (this) {
            String alwaysOnPackage = getAlwaysOnPackage();
            boolean z = true;
            if (alwaysOnPackage == null) {
                return true;
            }
            if (!isAlwaysOnPackageSupported(alwaysOnPackage)) {
                setAlwaysOnPackage(null, false);
                return false;
            } else if (getNetworkInfo().isConnected()) {
                return true;
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 3 */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x002b, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x006b, code lost:
        return true;
     */
    public synchronized boolean prepare(String oldPackage, String newPackage) {
        if (oldPackage != null) {
            if (this.mAlwaysOn && !isCurrentPreparedPackage(oldPackage)) {
                return false;
            }
            if (!isCurrentPreparedPackage(oldPackage)) {
                if (!oldPackage.equals("[Legacy VPN]") && isVpnUserPreConsented(oldPackage)) {
                    prepareInternal(oldPackage);
                    return true;
                }
            } else if (!oldPackage.equals("[Legacy VPN]") && !isVpnUserPreConsented(oldPackage)) {
                prepareInternal("[Legacy VPN]");
                return false;
            }
        }
        if (newPackage != null) {
            if (newPackage.equals("[Legacy VPN]") || !isCurrentPreparedPackage(newPackage)) {
                enforceControlPermission();
                if (this.mAlwaysOn && !isCurrentPreparedPackage(newPackage)) {
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
        try {
            if (this.mInterface != null) {
                this.mStatusIntent = null;
                agentDisconnect();
                jniReset(this.mInterface);
                this.mInterface = null;
                this.mNetworkCapabilities.setUids(null);
            }
            if (this.mConnection != null) {
                try {
                    this.mConnection.mService.transact(UsbAudioDevice.kAudioDeviceClassMask, Parcel.obtain(), null, 1);
                } catch (Exception e) {
                }
                this.mContext.unbindService(this.mConnection);
                this.mConnection = null;
            } else if (this.mLegacyVpnRunner != null) {
                this.mLegacyVpnRunner.exit();
                this.mLegacyVpnRunner = null;
            }
            this.mNetd.denyProtect(this.mOwnerUID);
        } catch (Exception e2) {
            Log.wtf(TAG, "Failed to disallow UID " + this.mOwnerUID + " to call protect() " + e2);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
        Log.i(TAG, "Switched from " + this.mPackage + " to " + newPackage);
        this.mPackage = newPackage;
        this.mOwnerUID = getAppUid(newPackage, this.mUserHandle);
        try {
            this.mNetd.allowProtect(this.mOwnerUID);
        } catch (Exception e3) {
            Log.wtf(TAG, "Failed to allow UID " + this.mOwnerUID + " to call protect() " + e3);
        }
        this.mConfig = null;
        updateState(NetworkInfo.DetailedState.IDLE, "prepare");
        setVpnForcedLocked(this.mLockdown);
        Binder.restoreCallingIdentity(token);
    }

    public boolean setPackageAuthorization(String packageName, boolean authorized) {
        enforceControlPermissionOrInternalCaller();
        int uid = getAppUid(packageName, this.mUserHandle);
        if (uid == -1 || "[Legacy VPN]".equals(packageName)) {
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            ((AppOpsManager) this.mContext.getSystemService("appops")).setMode(47, uid, packageName, authorized ^ true ? 1 : 0);
            Binder.restoreCallingIdentity(token);
            return true;
        } catch (Exception e) {
            Log.wtf(TAG, "Failed to set app ops for package " + packageName + ", uid " + uid, e);
            Binder.restoreCallingIdentity(token);
            return false;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    private boolean isVpnUserPreConsented(String packageName) {
        return ((AppOpsManager) this.mContext.getSystemService("appops")).noteOpNoThrow(47, Binder.getCallingUid(), packageName) == 0;
    }

    private int getAppUid(String app, int userHandle) {
        int result;
        if ("[Legacy VPN]".equals(app)) {
            return Process.myUid();
        }
        try {
            result = this.mContext.getPackageManager().getPackageUidAsUser(app, userHandle);
        } catch (PackageManager.NameNotFoundException e) {
            result = -1;
        }
        return result;
    }

    public NetworkInfo getNetworkInfo() {
        return this.mNetworkInfo;
    }

    public int getNetId() {
        if (this.mNetworkAgent != null) {
            return this.mNetworkAgent.netId;
        }
        return 0;
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
                InetAddress address3 = InetAddress.parseNumericAddress(dnsServer);
                lp.addDnsServer(address3);
                allowIPv4 |= address3 instanceof Inet4Address;
                allowIPv6 |= address3 instanceof Inet6Address;
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
                buffer.append(domain);
                buffer.append(' ');
            }
        }
        lp.setDomains(buffer.toString().trim());
        return lp;
    }

    @VisibleForTesting
    static boolean providesRoutesToMostDestinations(LinkProperties lp) {
        List<RouteInfo> routes = lp.getAllRoutes();
        boolean z = true;
        if (routes.size() > 150) {
            return true;
        }
        Comparator<IpPrefix> prefixLengthComparator = IpPrefix.lengthComparator();
        TreeSet<IpPrefix> ipv4Prefixes = new TreeSet<>(prefixLengthComparator);
        TreeSet<IpPrefix> ipv6Prefixes = new TreeSet<>(prefixLengthComparator);
        for (RouteInfo route : routes) {
            IpPrefix destination = route.getDestination();
            if (destination.isIPv4()) {
                ipv4Prefixes.add(destination);
            } else {
                ipv6Prefixes.add(destination);
            }
        }
        if (NetworkUtils.routedIPv4AddressCount(ipv4Prefixes) > MOST_IPV4_ADDRESSES_COUNT) {
            return true;
        }
        if (NetworkUtils.routedIPv6AddressCount(ipv6Prefixes).compareTo(MOST_IPV6_ADDRESSES_COUNT) < 0) {
            z = false;
        }
        return z;
    }

    private boolean updateLinkPropertiesInPlaceIfPossible(NetworkAgent agent, VpnConfig oldConfig) {
        if (oldConfig.allowBypass != this.mConfig.allowBypass) {
            Log.i(TAG, "Handover not possible due to changes to allowBypass");
            return false;
        } else if (!Objects.equals(oldConfig.allowedApplications, this.mConfig.allowedApplications) || !Objects.equals(oldConfig.disallowedApplications, this.mConfig.disallowedApplications)) {
            Log.i(TAG, "Handover not possible due to changes to whitelisted/blacklisted apps");
            return false;
        } else {
            LinkProperties lp = makeLinkProperties();
            if (this.mNetworkCapabilities.hasCapability(12) != providesRoutesToMostDestinations(lp)) {
                Log.i(TAG, "Handover not possible due to changes to INTERNET capability");
                return false;
            }
            agent.sendLinkProperties(lp);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void agentConnect() {
        long token;
        LinkProperties lp = makeLinkProperties();
        if (providesRoutesToMostDestinations(lp)) {
            this.mNetworkCapabilities.addCapability(12);
        } else {
            this.mNetworkCapabilities.removeCapability(12);
        }
        this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.CONNECTING, null, null);
        NetworkMisc networkMisc = new NetworkMisc();
        networkMisc.allowBypass = this.mConfig.allowBypass && !this.mLockdown;
        this.mNetworkCapabilities.setEstablishingVpnAppUid(Binder.getCallingUid());
        this.mNetworkCapabilities.setUids(createUserAndRestrictedProfilesRanges(this.mUserHandle, this.mConfig.allowedApplications, this.mConfig.disallowedApplications));
        long token2 = Binder.clearCallingIdentity();
        try {
            r1 = r1;
            long token3 = token2;
            try {
                AnonymousClass2 r1 = new NetworkAgent(this, this.mLooper, this.mContext, NETWORKTYPE, this.mNetworkInfo, this.mNetworkCapabilities, lp, 101, networkMisc) {
                    final /* synthetic */ Vpn this$0;

                    {
                        this.this$0 = this$0;
                    }

                    public void unwanted() {
                    }
                };
                this.mNetworkAgent = r1;
                Binder.restoreCallingIdentity(token3);
                this.mNetworkInfo.setIsAvailable(true);
                updateState(NetworkInfo.DetailedState.CONNECTED, "agentConnect");
            } catch (Throwable th) {
                th = th;
                token = token3;
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            token = token2;
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    private boolean canHaveRestrictedProfile(int userId) {
        long token = Binder.clearCallingIdentity();
        try {
            return UserManager.get(this.mContext).canHaveRestrictedProfile(userId);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void agentDisconnect(NetworkAgent networkAgent) {
        if (networkAgent != null) {
            NetworkInfo networkInfo = new NetworkInfo(this.mNetworkInfo);
            networkInfo.setIsAvailable(false);
            networkInfo.setDetailedState(NetworkInfo.DetailedState.DISCONNECTED, null, null);
            networkAgent.sendNetworkInfo(networkInfo);
        }
    }

    /* access modifiers changed from: private */
    public void agentDisconnect() {
        if (this.mNetworkInfo.isConnected()) {
            this.mNetworkInfo.setIsAvailable(false);
            updateState(NetworkInfo.DetailedState.DISCONNECTED, "agentDisconnect");
            this.mNetworkAgent = null;
        }
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:97:0x0230=Splitter:B:97:0x0230, B:77:0x01aa=Splitter:B:77:0x01aa} */
    public synchronized ParcelFileDescriptor establish(VpnConfig config) {
        VpnConfig vpnConfig = config;
        synchronized (this) {
            UserManager mgr = UserManager.get(this.mContext);
            if (Binder.getCallingUid() != this.mOwnerUID) {
                return null;
            }
            if (!isVpnUserPreConsented(this.mPackage)) {
                return null;
            }
            Intent intent = new Intent("android.net.VpnService");
            intent.setClassName(this.mPackage, vpnConfig.user);
            long token = Binder.clearCallingIdentity();
            try {
                if (!mgr.getUserInfo(this.mUserHandle).isRestricted()) {
                    ResolveInfo info = AppGlobals.getPackageManager().resolveService(intent, null, 0, this.mUserHandle);
                    if (info == null) {
                        Intent intent2 = intent;
                        throw new SecurityException("Cannot find " + vpnConfig.user);
                    } else if ("android.permission.BIND_VPN_SERVICE".equals(info.serviceInfo.permission)) {
                        Binder.restoreCallingIdentity(token);
                        VpnConfig oldConfig = this.mConfig;
                        String oldInterface = this.mInterface;
                        Connection oldConnection = this.mConnection;
                        NetworkAgent oldNetworkAgent = this.mNetworkAgent;
                        Set uids = this.mNetworkCapabilities.getUids();
                        ParcelFileDescriptor tun = ParcelFileDescriptor.adoptFd(jniCreate(vpnConfig.mtu));
                        try {
                            String interfaze = jniGetName(tun.getFd());
                            StringBuilder builder = new StringBuilder();
                            Iterator it = vpnConfig.addresses.iterator();
                            while (it.hasNext()) {
                                try {
                                    StringBuilder sb = new StringBuilder();
                                    Iterator it2 = it;
                                    sb.append(" ");
                                    sb.append((LinkAddress) it.next());
                                    builder.append(sb.toString());
                                    it = it2;
                                } catch (RuntimeException e) {
                                    e = e;
                                    UserManager userManager = mgr;
                                    Intent intent3 = intent;
                                    IoUtils.closeQuietly(tun);
                                    agentDisconnect();
                                    this.mConfig = oldConfig;
                                    this.mConnection = oldConnection;
                                    this.mNetworkCapabilities.setUids(uids);
                                    this.mNetworkAgent = oldNetworkAgent;
                                    this.mInterface = oldInterface;
                                    throw e;
                                }
                            }
                            if (jniSetAddresses(interfaze, builder.toString()) >= 1) {
                                Connection connection = new Connection();
                                UserManager userManager2 = mgr;
                                try {
                                    StringBuilder sb2 = builder;
                                    if (this.mContext.bindServiceAsUser(intent, connection, 67108865, new UserHandle(this.mUserHandle))) {
                                        this.mConnection = connection;
                                        this.mInterface = interfaze;
                                        vpnConfig.user = this.mPackage;
                                        vpnConfig.interfaze = this.mInterface;
                                        Intent intent4 = intent;
                                        try {
                                            vpnConfig.startTime = SystemClock.elapsedRealtime();
                                            this.mConfig = vpnConfig;
                                            if (oldConfig == null || !updateLinkPropertiesInPlaceIfPossible(this.mNetworkAgent, oldConfig)) {
                                                this.mNetworkAgent = null;
                                                updateState(NetworkInfo.DetailedState.CONNECTING, "establish");
                                                agentConnect();
                                                agentDisconnect(oldNetworkAgent);
                                            }
                                            if (oldConnection != null) {
                                                this.mContext.unbindService(oldConnection);
                                            }
                                            if (oldInterface != null && !oldInterface.equals(interfaze)) {
                                                jniReset(oldInterface);
                                            }
                                            IoUtils.setBlocking(tun.getFileDescriptor(), vpnConfig.blocking);
                                            Log.i(TAG, "Established by " + vpnConfig.user + " on " + this.mInterface);
                                            return tun;
                                        } catch (IOException e2) {
                                            throw new IllegalStateException("Cannot set tunnel's fd as blocking=" + vpnConfig.blocking, e2);
                                        } catch (RuntimeException e3) {
                                            e = e3;
                                            IoUtils.closeQuietly(tun);
                                            agentDisconnect();
                                            this.mConfig = oldConfig;
                                            this.mConnection = oldConnection;
                                            this.mNetworkCapabilities.setUids(uids);
                                            this.mNetworkAgent = oldNetworkAgent;
                                            this.mInterface = oldInterface;
                                            throw e;
                                        }
                                    } else {
                                        throw new IllegalStateException("Cannot bind " + vpnConfig.user);
                                    }
                                } catch (RuntimeException e4) {
                                    e = e4;
                                    Intent intent5 = intent;
                                    IoUtils.closeQuietly(tun);
                                    agentDisconnect();
                                    this.mConfig = oldConfig;
                                    this.mConnection = oldConnection;
                                    this.mNetworkCapabilities.setUids(uids);
                                    this.mNetworkAgent = oldNetworkAgent;
                                    this.mInterface = oldInterface;
                                    throw e;
                                }
                            } else {
                                Intent intent6 = intent;
                                StringBuilder sb3 = builder;
                                throw new IllegalArgumentException("At least one address must be specified");
                            }
                        } catch (RuntimeException e5) {
                            e = e5;
                            UserManager userManager3 = mgr;
                            Intent intent7 = intent;
                            IoUtils.closeQuietly(tun);
                            agentDisconnect();
                            this.mConfig = oldConfig;
                            this.mConnection = oldConnection;
                            this.mNetworkCapabilities.setUids(uids);
                            this.mNetworkAgent = oldNetworkAgent;
                            this.mInterface = oldInterface;
                            throw e;
                        }
                    } else {
                        Intent intent8 = intent;
                        try {
                            throw new SecurityException(vpnConfig.user + " does not require " + "android.permission.BIND_VPN_SERVICE");
                        } catch (RemoteException e6) {
                            try {
                                throw new SecurityException("Cannot find " + vpnConfig.user);
                            } catch (Throwable th) {
                                e = th;
                                Binder.restoreCallingIdentity(token);
                                throw e;
                            }
                        }
                    }
                } else {
                    Intent intent9 = intent;
                    throw new SecurityException("Restricted users cannot establish VPNs");
                }
            } catch (RemoteException e7) {
                UserManager userManager4 = mgr;
                Intent intent10 = intent;
                throw new SecurityException("Cannot find " + vpnConfig.user);
            } catch (Throwable th2) {
                e = th2;
                UserManager userManager5 = mgr;
                Intent intent11 = intent;
                Binder.restoreCallingIdentity(token);
                throw e;
            }
        }
    }

    private boolean isRunningLocked() {
        return (this.mNetworkAgent == null || this.mInterface == null) ? false : true;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean isCallerEstablishedOwnerLocked() {
        return isRunningLocked() && Binder.getCallingUid() == this.mOwnerUID;
    }

    private SortedSet<Integer> getAppsUids(List<String> packageNames, int userHandle) {
        SortedSet<Integer> uids = new TreeSet<>();
        for (String app : packageNames) {
            int uid = getAppUid(app, userHandle);
            if (uid != -1) {
                uids.add(Integer.valueOf(uid));
            }
        }
        return uids;
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Set<UidRange> createUserAndRestrictedProfilesRanges(int userHandle, List<String> allowedApplications, List<String> disallowedApplications) {
        Set<UidRange> ranges = new ArraySet<>();
        addUserToRanges(ranges, userHandle, allowedApplications, disallowedApplications);
        if (canHaveRestrictedProfile(userHandle)) {
            long token = Binder.clearCallingIdentity();
            try {
                List<UserInfo> users = UserManager.get(this.mContext).getUsers(true);
                Binder.restoreCallingIdentity(token);
                for (UserInfo user : users) {
                    if (user.isRestricted() && user.restrictedProfileParentId == userHandle) {
                        addUserToRanges(ranges, user.id, allowedApplications, disallowedApplications);
                    }
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }
        return ranges;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void addUserToRanges(Set<UidRange> ranges, int userHandle, List<String> allowedApplications, List<String> disallowedApplications) {
        if (allowedApplications != null) {
            int start = -1;
            int stop = -1;
            for (Integer intValue : getAppsUids(allowedApplications, userHandle)) {
                int uid = intValue.intValue();
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
            int start2 = userRange.start;
            for (Integer intValue2 : getAppsUids(disallowedApplications, userHandle)) {
                int uid2 = intValue2.intValue();
                if (uid2 == start2) {
                    start2++;
                } else {
                    ranges.add(new UidRange(start2, uid2 - 1));
                    start2 = uid2 + 1;
                }
            }
            if (start2 <= userRange.stop) {
                ranges.add(new UidRange(start2, userRange.stop));
            }
        } else {
            ranges.add(UidRange.createForUser(userHandle));
        }
    }

    private static List<UidRange> uidRangesForUser(int userHandle, Set<UidRange> existingRanges) {
        UidRange userRange = UidRange.createForUser(userHandle);
        List<UidRange> ranges = new ArrayList<>();
        for (UidRange range : existingRanges) {
            if (userRange.containsRange(range)) {
                ranges.add(range);
            }
        }
        return ranges;
    }

    public void onUserAdded(int userHandle) {
        UserInfo user = UserManager.get(this.mContext).getUserInfo(userHandle);
        if (user.isRestricted() && user.restrictedProfileParentId == this.mUserHandle) {
            synchronized (this) {
                Set<UidRange> existingRanges = this.mNetworkCapabilities.getUids();
                if (existingRanges != null) {
                    try {
                        addUserToRanges(existingRanges, userHandle, this.mConfig.allowedApplications, this.mConfig.disallowedApplications);
                        this.mNetworkCapabilities.setUids(existingRanges);
                        updateCapabilities();
                    } catch (Exception e) {
                        Log.wtf(TAG, "Failed to add restricted user to owner", e);
                    }
                }
                setVpnForcedLocked(this.mLockdown);
            }
        }
    }

    public void onUserRemoved(int userHandle) {
        UserInfo user = UserManager.get(this.mContext).getUserInfo(userHandle);
        if (user.isRestricted() && user.restrictedProfileParentId == this.mUserHandle) {
            synchronized (this) {
                Set<UidRange> existingRanges = this.mNetworkCapabilities.getUids();
                if (existingRanges != null) {
                    try {
                        existingRanges.removeAll(uidRangesForUser(userHandle, existingRanges));
                        this.mNetworkCapabilities.setUids(existingRanges);
                        updateCapabilities();
                    } catch (Exception e) {
                        Log.wtf(TAG, "Failed to remove restricted user to owner", e);
                    }
                }
                setVpnForcedLocked(this.mLockdown);
            }
        }
    }

    public synchronized void onUserStopped() {
        setLockdown(false);
        this.mAlwaysOn = false;
        unregisterPackageChangeReceiverLocked();
        agentDisconnect();
    }

    @GuardedBy("this")
    private void setVpnForcedLocked(boolean enforce) {
        List<String> exemptedPackages = isNullOrLegacyVpn(this.mPackage) ? null : Collections.singletonList(this.mPackage);
        Set<UidRange> removedRanges = new ArraySet<>(this.mBlockedUsers);
        Set<UidRange> addedRanges = Collections.emptySet();
        if (enforce) {
            addedRanges = createUserAndRestrictedProfilesRanges(this.mUserHandle, null, exemptedPackages);
            for (UidRange range : addedRanges) {
                if (range.start == 0) {
                    addedRanges.remove(range);
                    if (range.stop != 0) {
                        addedRanges.add(new UidRange(1, range.stop));
                    }
                }
            }
            removedRanges.removeAll(addedRanges);
            addedRanges.removeAll(this.mBlockedUsers);
        }
        setAllowOnlyVpnForUids(false, removedRanges);
        setAllowOnlyVpnForUids(true, addedRanges);
    }

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
        } catch (RemoteException | RuntimeException e) {
            Log.e(TAG, "Updating blocked=" + enforce + " for UIDs " + Arrays.toString(ranges.toArray()) + " failed", e);
            return false;
        }
    }

    public VpnConfig getVpnConfig() {
        enforceControlPermission();
        return this.mConfig;
    }

    /* Debug info: failed to restart local var, previous not found, register: 1 */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    @Deprecated
    public synchronized void interfaceStatusChanged(String iface, boolean up) {
        this.mObserver.interfaceStatusChanged(iface, up);
    }

    private void enforceControlPermission() {
        this.mContext.enforceCallingPermission("android.permission.CONTROL_VPN", "Unauthorized Caller");
    }

    private void enforceControlPermissionOrInternalCaller() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONTROL_VPN", "Unauthorized Caller");
    }

    private void enforceSettingsPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.NETWORK_SETTINGS", "Unauthorized Caller");
    }

    /* access modifiers changed from: private */
    public void prepareStatusIntent() {
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
        updateCapabilities();
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
        return this.mNetworkCapabilities.appliesToUid(uid);
    }

    public synchronized boolean isBlockingUid(int uid) {
        if (!this.mLockdown) {
            return false;
        }
        if (this.mNetworkInfo.isConnected()) {
            return !appliesToUid(uid);
        }
        for (UidRange uidRange : this.mBlockedUsers) {
            if (uidRange.contains(uid)) {
                return true;
            }
        }
        return false;
    }

    private void updateAlwaysOnNotification(NetworkInfo.DetailedState networkState) {
        boolean visible = this.mAlwaysOn && networkState != NetworkInfo.DetailedState.CONNECTED;
        UserHandle user = UserHandle.of(this.mUserHandle);
        long token = Binder.clearCallingIdentity();
        try {
            NotificationManager notificationManager = NotificationManager.from(this.mContext);
            if (!visible) {
                notificationManager.cancelAsUser(TAG, 17, user);
                return;
            }
            Intent intent = new Intent();
            intent.setComponent(ComponentName.unflattenFromString(this.mContext.getString(17039777)));
            intent.putExtra("lockdown", this.mLockdown);
            intent.addFlags(268435456);
            notificationManager.notifyAsUser(TAG, 17, new Notification.Builder(this.mContext, SystemNotificationChannels.VPN).setSmallIcon(17303738).setContentTitle(this.mContext.getString(17041342)).setContentText(this.mContext.getString(17041339)).setContentIntent(this.mSystemServices.pendingIntentGetActivityAsUser(intent, 201326592, user)).setCategory("sys").setVisibility(1).setOngoing(true).setColor(this.mContext.getColor(17170784)).build(), user);
            Binder.restoreCallingIdentity(token);
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
        VpnProfile vpnProfile = profile;
        KeyStore keyStore2 = keyStore;
        UserManager mgr = UserManager.get(this.mContext);
        if (mgr.getUserInfo(this.mUserHandle).isRestricted() || mgr.hasUserRestriction("no_config_vpn", new UserHandle(this.mUserHandle))) {
            throw new SecurityException("Restricted users cannot establish VPNs");
        }
        RouteInfo ipv4DefaultRoute = findIPv4DefaultRoute(egress);
        String gateway = ipv4DefaultRoute.getGateway().getHostAddress();
        String iface = ipv4DefaultRoute.getInterface();
        String privateKey = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        String userCert = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        String caCert = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        String serverCert = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        String str = null;
        if (!vpnProfile.ipsecUserCert.isEmpty()) {
            privateKey = "USRPKEY_" + vpnProfile.ipsecUserCert;
            byte[] value = keyStore2.get("USRCERT_" + vpnProfile.ipsecUserCert);
            userCert = value == null ? null : new String(value, StandardCharsets.UTF_8);
        }
        if (!vpnProfile.ipsecCaCert.isEmpty()) {
            byte[] value2 = keyStore2.get("CACERT_" + vpnProfile.ipsecCaCert);
            caCert = value2 == null ? null : new String(value2, StandardCharsets.UTF_8);
        }
        if (!vpnProfile.ipsecServerCert.isEmpty()) {
            byte[] value3 = keyStore2.get("USRCERT_" + vpnProfile.ipsecServerCert);
            if (value3 != null) {
                str = new String(value3, StandardCharsets.UTF_8);
            }
            serverCert = str;
        }
        if (privateKey == null || userCert == null || caCert == null || serverCert == null) {
            throw new IllegalStateException("Cannot load credentials");
        }
        String[] racoon = null;
        switch (vpnProfile.type) {
            case 2:
                racoon = new String[]{iface, vpnProfile.server, "udppsk", vpnProfile.ipsecIdentifier, vpnProfile.ipsecSecret, "1701"};
                break;
            case 3:
                racoon = new String[]{iface, vpnProfile.server, "udprsa", privateKey, userCert, caCert, serverCert, "1701"};
                break;
            case 4:
                racoon = new String[]{iface, vpnProfile.server, "xauthpsk", vpnProfile.ipsecIdentifier, vpnProfile.ipsecSecret, vpnProfile.username, vpnProfile.password, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, gateway};
                break;
            case 5:
                racoon = new String[]{iface, vpnProfile.server, "xauthrsa", privateKey, userCert, caCert, serverCert, vpnProfile.username, vpnProfile.password, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, gateway};
                break;
            case 6:
                racoon = new String[]{iface, vpnProfile.server, "hybridrsa", caCert, serverCert, vpnProfile.username, vpnProfile.password, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, gateway};
                break;
        }
        String[] mtpd = null;
        switch (vpnProfile.type) {
            case 0:
                String[] strArr = new String[20];
                strArr[0] = iface;
                strArr[1] = "pptp";
                strArr[2] = vpnProfile.server;
                strArr[3] = "1723";
                strArr[4] = com.android.server.pm.Settings.ATTR_NAME;
                strArr[5] = vpnProfile.username;
                strArr[6] = "password";
                strArr[7] = vpnProfile.password;
                strArr[8] = "linkname";
                strArr[9] = "vpn";
                strArr[10] = "refuse-eap";
                strArr[11] = "nodefaultroute";
                strArr[12] = "usepeerdns";
                strArr[13] = "idle";
                strArr[14] = "1800";
                strArr[15] = "mtu";
                strArr[16] = "1400";
                strArr[17] = "mru";
                strArr[18] = "1400";
                strArr[19] = vpnProfile.mppe ? "+mppe" : "nomppe";
                mtpd = strArr;
                break;
            case 1:
            case 2:
            case 3:
                mtpd = new String[]{iface, "l2tp", vpnProfile.server, "1701", vpnProfile.l2tpSecret, com.android.server.pm.Settings.ATTR_NAME, vpnProfile.username, "password", vpnProfile.password, "linkname", "vpn", "refuse-eap", "nodefaultroute", "usepeerdns", "idle", "1800", "mtu", "1400", "mru", "1400"};
                break;
        }
        VpnConfig config = new VpnConfig();
        config.legacy = true;
        config.user = vpnProfile.key;
        config.interfaze = iface;
        config.session = vpnProfile.name;
        config.addLegacyRoutes(vpnProfile.routes);
        if (!vpnProfile.dnsServers.isEmpty()) {
            config.dnsServers = Arrays.asList(vpnProfile.dnsServers.split(" +"));
        }
        if (!vpnProfile.searchDomains.isEmpty()) {
            config.searchDomains = Arrays.asList(vpnProfile.searchDomains.split(" +"));
        }
        startLegacyVpn(config, racoon, mtpd);
    }

    private synchronized void startLegacyVpn(VpnConfig config, String[] racoon, String[] mtpd) {
        stopLegacyVpnPrivileged();
        prepareInternal("[Legacy VPN]");
        updateState(NetworkInfo.DetailedState.CONNECTING, "startLegacyVpn");
        this.mLegacyVpnRunner = new LegacyVpnRunner(config, racoon, mtpd);
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

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0028, code lost:
        return r0;
     */
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
