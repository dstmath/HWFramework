package com.android.server;

import android.content.Context;
import android.net.INetd;
import android.net.ITestNetworkManager;
import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.RouteInfo;
import android.net.StringNetworkSpecifier;
import android.net.TestNetworkInterface;
import android.net.util.NetdService;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.FunctionalUtils;
import com.android.internal.util.Preconditions;
import java.io.UncheckedIOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

/* access modifiers changed from: package-private */
public class TestNetworkService extends ITestNetworkManager.Stub {
    private static final String PERMISSION_NAME = "android.permission.MANAGE_TEST_NETWORKS";
    private static final String TAG = TestNetworkService.class.getSimpleName();
    private static final String TEST_NETWORK_TYPE = "TEST_NETWORK";
    private static final String TEST_TAP_PREFIX = "testtap";
    private static final String TEST_TUN_PREFIX = "testtun";
    private static final AtomicInteger sTestTunIndex = new AtomicInteger();
    private final Context mContext;
    private final Handler mHandler;
    private final HandlerThread mHandlerThread = new HandlerThread("TestNetworkServiceThread");
    private final INetworkManagementService mNMS;
    private final INetd mNetd;
    @GuardedBy({"mTestNetworkTracker"})
    private final SparseArray<TestNetworkAgent> mTestNetworkTracker = new SparseArray<>();

    private static native int jniCreateTunTap(boolean z, String str);

    @VisibleForTesting
    protected TestNetworkService(Context context, INetworkManagementService netManager) {
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        this.mContext = (Context) Preconditions.checkNotNull(context, "missing Context");
        this.mNMS = (INetworkManagementService) Preconditions.checkNotNull(netManager, "missing INetworkManagementService");
        this.mNetd = (INetd) Preconditions.checkNotNull(NetdService.getInstance(), "could not get netd instance");
    }

    private TestNetworkInterface createInterface(boolean isTun, LinkAddress[] linkAddrs) {
        enforceTestNetworkPermissions(this.mContext);
        Preconditions.checkNotNull(linkAddrs, "missing linkAddrs");
        String ifacePrefix = isTun ? TEST_TUN_PREFIX : TEST_TAP_PREFIX;
        return (TestNetworkInterface) Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingSupplier(isTun, ifacePrefix + sTestTunIndex.getAndIncrement(), linkAddrs) {
            /* class com.android.server.$$Lambda$TestNetworkService$kNsToB0Cr6DV8jrvpBel_EzoIHE */
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ String f$2;
            private final /* synthetic */ LinkAddress[] f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final Object getOrThrow() {
                return TestNetworkService.this.lambda$createInterface$0$TestNetworkService(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public /* synthetic */ TestNetworkInterface lambda$createInterface$0$TestNetworkService(boolean isTun, String iface, LinkAddress[] linkAddrs) throws Exception {
        try {
            ParcelFileDescriptor tunIntf = ParcelFileDescriptor.adoptFd(jniCreateTunTap(isTun, iface));
            for (LinkAddress addr : linkAddrs) {
                this.mNetd.interfaceAddAddress(iface, addr.getAddress().getHostAddress(), addr.getPrefixLength());
            }
            return new TestNetworkInterface(tunIntf, iface);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public TestNetworkInterface createTunInterface(LinkAddress[] linkAddrs) {
        return createInterface(true, linkAddrs);
    }

    public TestNetworkInterface createTapInterface() {
        return createInterface(false, new LinkAddress[0]);
    }

    public class TestNetworkAgent extends NetworkAgent implements IBinder.DeathRecipient {
        private static final int NETWORK_SCORE = 1;
        @GuardedBy({"mBinderLock"})
        private IBinder mBinder;
        private final Object mBinderLock;
        private final LinkProperties mLp;
        private final NetworkCapabilities mNc;
        private final NetworkInfo mNi;
        private final int mUid;

        private TestNetworkAgent(Looper looper, Context context, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int uid, IBinder binder) throws RemoteException {
            super(looper, context, TestNetworkService.TEST_NETWORK_TYPE, ni, nc, lp, 1);
            this.mBinderLock = new Object();
            this.mUid = uid;
            this.mNi = ni;
            this.mNc = nc;
            this.mLp = lp;
            synchronized (this.mBinderLock) {
                this.mBinder = binder;
                try {
                    this.mBinder.linkToDeath(this, 0);
                } catch (RemoteException e) {
                    binderDied();
                    throw e;
                }
            }
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            teardown();
        }

        /* access modifiers changed from: protected */
        public void unwanted() {
            teardown();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void teardown() {
            this.mNi.setDetailedState(NetworkInfo.DetailedState.DISCONNECTED, null, null);
            this.mNi.setIsAvailable(false);
            sendNetworkInfo(this.mNi);
            synchronized (this.mBinderLock) {
                if (this.mBinder != null) {
                    this.mBinder.unlinkToDeath(this, 0);
                    this.mBinder = null;
                    synchronized (TestNetworkService.this.mTestNetworkTracker) {
                        TestNetworkService.this.mTestNetworkTracker.remove(this.netId);
                    }
                }
            }
        }
    }

    private TestNetworkAgent registerTestNetworkAgent(Looper looper, Context context, String iface, int callingUid, IBinder binder) throws RemoteException, SocketException {
        Preconditions.checkNotNull(looper, "missing Looper");
        Preconditions.checkNotNull(context, "missing Context");
        NetworkInfo ni = new NetworkInfo(18, 0, TEST_NETWORK_TYPE, "");
        ni.setDetailedState(NetworkInfo.DetailedState.CONNECTED, null, null);
        ni.setIsAvailable(true);
        NetworkCapabilities nc = new NetworkCapabilities();
        nc.clearAll();
        nc.addTransportType(7);
        nc.addCapability(21);
        nc.addCapability(13);
        nc.setNetworkSpecifier(new StringNetworkSpecifier(iface));
        LinkProperties lp = new LinkProperties();
        lp.setInterfaceName(iface);
        NetworkInterface netIntf = NetworkInterface.getByName(iface);
        Preconditions.checkNotNull(netIntf, "No such network interface found: " + netIntf);
        boolean allowIPv4 = false;
        boolean allowIPv6 = false;
        for (InterfaceAddress intfAddr : netIntf.getInterfaceAddresses()) {
            lp.addLinkAddress(new LinkAddress(intfAddr.getAddress(), intfAddr.getNetworkPrefixLength()));
            if (intfAddr.getAddress() instanceof Inet6Address) {
                allowIPv6 |= !intfAddr.getAddress().isLinkLocalAddress();
            } else if (intfAddr.getAddress() instanceof Inet4Address) {
                allowIPv4 = true;
            }
        }
        if (allowIPv4) {
            lp.addRoute(new RouteInfo(new IpPrefix(Inet4Address.ANY, 0), null, iface));
        }
        if (allowIPv6) {
            lp.addRoute(new RouteInfo(new IpPrefix(Inet6Address.ANY, 0), null, iface));
        }
        return new TestNetworkAgent(looper, context, ni, nc, lp, callingUid, binder);
    }

    public void setupTestNetwork(String iface, IBinder binder) {
        enforceTestNetworkPermissions(this.mContext);
        Preconditions.checkNotNull(iface, "missing Iface");
        Preconditions.checkNotNull(binder, "missing IBinder");
        if (iface.startsWith(INetd.IPSEC_INTERFACE_PREFIX) || iface.startsWith(TEST_TUN_PREFIX)) {
            Binder.withCleanCallingIdentity(new FunctionalUtils.ThrowingRunnable(iface, Binder.getCallingUid(), binder) {
                /* class com.android.server.$$Lambda$TestNetworkService$jaBdxV1WIiJrgh0fXY_tPjFxN8I */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ int f$2;
                private final /* synthetic */ IBinder f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void runOrThrow() {
                    TestNetworkService.this.lambda$setupTestNetwork$1$TestNetworkService(this.f$1, this.f$2, this.f$3);
                }
            });
            return;
        }
        throw new IllegalArgumentException("Cannot create network for non ipsec, non-testtun interface");
    }

    public /* synthetic */ void lambda$setupTestNetwork$1$TestNetworkService(String iface, int callingUid, IBinder binder) throws Exception {
        try {
            this.mNMS.setInterfaceUp(iface);
            synchronized (this.mTestNetworkTracker) {
                TestNetworkAgent agent = registerTestNetworkAgent(this.mHandler.getLooper(), this.mContext, iface, callingUid, binder);
                this.mTestNetworkTracker.put(agent.netId, agent);
            }
        } catch (SocketException e) {
            throw new UncheckedIOException(e);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    public void teardownTestNetwork(int netId) {
        TestNetworkAgent agent;
        enforceTestNetworkPermissions(this.mContext);
        synchronized (this.mTestNetworkTracker) {
            agent = this.mTestNetworkTracker.get(netId);
        }
        if (agent != null) {
            if (agent.mUid == Binder.getCallingUid()) {
                agent.teardown();
                return;
            }
            throw new SecurityException("Attempted to modify other user's test networks");
        }
    }

    public static void enforceTestNetworkPermissions(Context context) {
        context.enforceCallingOrSelfPermission(PERMISSION_NAME, "TestNetworkService");
    }
}
