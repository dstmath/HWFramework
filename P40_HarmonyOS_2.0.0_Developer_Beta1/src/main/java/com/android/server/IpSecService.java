package com.android.server;

import android.app.AppOpsManager;
import android.content.Context;
import android.net.IIpSecService;
import android.net.INetd;
import android.net.IpSecAlgorithm;
import android.net.IpSecConfig;
import android.net.IpSecSpiResponse;
import android.net.IpSecTransformResponse;
import android.net.IpSecTunnelInterfaceResponse;
import android.net.IpSecUdpEncapResponse;
import android.net.LinkAddress;
import android.net.Network;
import android.net.NetworkUtils;
import android.net.TrafficStats;
import android.net.util.NetdService;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import libcore.io.IoUtils;

public class IpSecService extends IIpSecService.Stub {
    private static final int[] ADDRESS_FAMILIES = {OsConstants.AF_INET, OsConstants.AF_INET6};
    private static final boolean DBG = Log.isLoggable(TAG, 3);
    static final int FREE_PORT_MIN = 1024;
    private static final InetAddress INADDR_ANY;
    @VisibleForTesting
    static final int MAX_PORT_BIND_ATTEMPTS = 10;
    private static final int NETD_FETCH_TIMEOUT_MS = 5000;
    private static final String NETD_SERVICE_NAME = "netd";
    static final int PORT_MAX = 65535;
    private static final String TAG = "IpSecService";
    private static final String TUNNEL_OP = "android:manage_ipsec_tunnels";
    @VisibleForTesting
    static final int TUN_INTF_NETID_RANGE = 1024;
    @VisibleForTesting
    static final int TUN_INTF_NETID_START = 64512;
    private final Context mContext;
    @GuardedBy({"IpSecService.this"})
    private int mNextResourceId;
    private int mNextTunnelNetIdIndex;
    private final IpSecServiceConfiguration mSrvConfig;
    private final SparseBooleanArray mTunnelNetIds;
    final UidFdTagger mUidFdTagger;
    @VisibleForTesting
    final UserResourceTracker mUserResourceTracker;

    @VisibleForTesting
    public interface IResource {
        void freeUnderlyingResources() throws RemoteException;

        void invalidate() throws RemoteException;
    }

    /* access modifiers changed from: package-private */
    public interface IpSecServiceConfiguration {
        public static final IpSecServiceConfiguration GETSRVINSTANCE = new IpSecServiceConfiguration() {
            /* class com.android.server.IpSecService.IpSecServiceConfiguration.AnonymousClass1 */

            @Override // com.android.server.IpSecService.IpSecServiceConfiguration
            public INetd getNetdInstance() throws RemoteException {
                INetd netd = NetdService.getInstance();
                if (netd != null) {
                    return netd;
                }
                throw new RemoteException("Failed to Get Netd Instance");
            }
        };

        INetd getNetdInstance() throws RemoteException;
    }

    @VisibleForTesting
    public interface UidFdTagger {
        void tag(FileDescriptor fileDescriptor, int i) throws IOException;
    }

    static {
        try {
            INADDR_ANY = InetAddress.getByAddress(new byte[]{0, 0, 0, 0});
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    public class RefcountedResource<T extends IResource> implements IBinder.DeathRecipient {
        IBinder mBinder;
        private final List<RefcountedResource> mChildren;
        int mRefCount = 1;
        private final T mResource;

        RefcountedResource(T resource, IBinder binder, RefcountedResource... children) {
            synchronized (IpSecService.this) {
                this.mResource = resource;
                this.mChildren = new ArrayList(children.length);
                this.mBinder = binder;
                for (RefcountedResource child : children) {
                    this.mChildren.add(child);
                    child.mRefCount++;
                }
                try {
                    this.mBinder.linkToDeath(this, 0);
                } catch (RemoteException e) {
                    binderDied();
                    e.rethrowFromSystemServer();
                }
            }
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (IpSecService.this) {
                try {
                    userRelease();
                } catch (Exception e) {
                    Log.e(IpSecService.TAG, "Failed to release resource: " + e);
                }
            }
        }

        public T getResource() {
            return this.mResource;
        }

        @GuardedBy({"IpSecService.this"})
        public void userRelease() throws RemoteException {
            IBinder iBinder = this.mBinder;
            if (iBinder != null) {
                iBinder.unlinkToDeath(this, 0);
                this.mBinder = null;
                this.mResource.invalidate();
                releaseReference();
            }
        }

        @GuardedBy({"IpSecService.this"})
        @VisibleForTesting
        public void releaseReference() throws RemoteException {
            this.mRefCount--;
            int i = this.mRefCount;
            if (i <= 0) {
                if (i >= 0) {
                    this.mResource.freeUnderlyingResources();
                    Iterator<RefcountedResource> it = this.mChildren.iterator();
                    while (it.hasNext()) {
                        it.next().releaseReference();
                    }
                    this.mRefCount--;
                    return;
                }
                throw new IllegalStateException("Invalid operation - resource has already been released.");
            }
        }

        @Override // java.lang.Object
        public String toString() {
            return "{mResource=" + this.mResource + ", mRefCount=" + this.mRefCount + ", mChildren=" + this.mChildren + "}";
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class ResourceTracker {
        int mCurrent = 0;
        private final int mMax;

        ResourceTracker(int max) {
            this.mMax = max;
        }

        /* access modifiers changed from: package-private */
        public boolean isAvailable() {
            return this.mCurrent < this.mMax;
        }

        /* access modifiers changed from: package-private */
        public void take() {
            if (!isAvailable()) {
                Log.wtf(IpSecService.TAG, "Too many resources allocated!");
            }
            this.mCurrent++;
        }

        /* access modifiers changed from: package-private */
        public void give() {
            if (this.mCurrent <= 0) {
                Log.wtf(IpSecService.TAG, "We've released this resource too many times");
            }
            this.mCurrent--;
        }

        public String toString() {
            return "{mCurrent=" + this.mCurrent + ", mMax=" + this.mMax + "}";
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static final class UserRecord {
        public static final int MAX_NUM_ENCAP_SOCKETS = 2;
        public static final int MAX_NUM_SPIS = 8;
        public static final int MAX_NUM_TRANSFORMS = 4;
        public static final int MAX_NUM_TUNNEL_INTERFACES = 2;
        final RefcountedResourceArray<EncapSocketRecord> mEncapSocketRecords = new RefcountedResourceArray<>(EncapSocketRecord.class.getSimpleName());
        final ResourceTracker mSocketQuotaTracker = new ResourceTracker(2);
        final ResourceTracker mSpiQuotaTracker = new ResourceTracker(8);
        final RefcountedResourceArray<SpiRecord> mSpiRecords = new RefcountedResourceArray<>(SpiRecord.class.getSimpleName());
        final ResourceTracker mTransformQuotaTracker = new ResourceTracker(4);
        final RefcountedResourceArray<TransformRecord> mTransformRecords = new RefcountedResourceArray<>(TransformRecord.class.getSimpleName());
        final RefcountedResourceArray<TunnelInterfaceRecord> mTunnelInterfaceRecords = new RefcountedResourceArray<>(TunnelInterfaceRecord.class.getSimpleName());
        final ResourceTracker mTunnelQuotaTracker = new ResourceTracker(2);

        UserRecord() {
        }

        /* access modifiers changed from: package-private */
        public void removeSpiRecord(int resourceId) {
            this.mSpiRecords.remove(resourceId);
        }

        /* access modifiers changed from: package-private */
        public void removeTransformRecord(int resourceId) {
            this.mTransformRecords.remove(resourceId);
        }

        /* access modifiers changed from: package-private */
        public void removeTunnelInterfaceRecord(int resourceId) {
            this.mTunnelInterfaceRecords.remove(resourceId);
        }

        /* access modifiers changed from: package-private */
        public void removeEncapSocketRecord(int resourceId) {
            this.mEncapSocketRecords.remove(resourceId);
        }

        public String toString() {
            return "{mSpiQuotaTracker=" + this.mSpiQuotaTracker + ", mTransformQuotaTracker=" + this.mTransformQuotaTracker + ", mSocketQuotaTracker=" + this.mSocketQuotaTracker + ", mTunnelQuotaTracker=" + this.mTunnelQuotaTracker + ", mSpiRecords=" + this.mSpiRecords + ", mTransformRecords=" + this.mTransformRecords + ", mEncapSocketRecords=" + this.mEncapSocketRecords + ", mTunnelInterfaceRecords=" + this.mTunnelInterfaceRecords + "}";
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static final class UserResourceTracker {
        private final SparseArray<UserRecord> mUserRecords = new SparseArray<>();

        UserResourceTracker() {
        }

        public UserRecord getUserRecord(int uid) {
            checkCallerUid(uid);
            UserRecord r = this.mUserRecords.get(uid);
            if (r != null) {
                return r;
            }
            UserRecord r2 = new UserRecord();
            this.mUserRecords.put(uid, r2);
            return r2;
        }

        private void checkCallerUid(int uid) {
            if (uid != Binder.getCallingUid() && 1000 != Binder.getCallingUid()) {
                throw new SecurityException("Attempted access of unowned resources");
            }
        }

        public String toString() {
            return this.mUserRecords.toString();
        }
    }

    private abstract class OwnedResourceRecord implements IResource {
        protected final int mResourceId;
        final int pid;
        final int uid;

        @Override // com.android.server.IpSecService.IResource
        public abstract void freeUnderlyingResources() throws RemoteException;

        /* access modifiers changed from: protected */
        public abstract ResourceTracker getResourceTracker();

        @Override // com.android.server.IpSecService.IResource
        public abstract void invalidate() throws RemoteException;

        OwnedResourceRecord(int resourceId) {
            if (resourceId != -1) {
                this.mResourceId = resourceId;
                this.pid = Binder.getCallingPid();
                this.uid = Binder.getCallingUid();
                getResourceTracker().take();
                return;
            }
            throw new IllegalArgumentException("Resource ID must not be INVALID_RESOURCE_ID");
        }

        /* access modifiers changed from: protected */
        public UserRecord getUserRecord() {
            return IpSecService.this.mUserResourceTracker.getUserRecord(this.uid);
        }

        public String toString() {
            return "{mResourceId=" + this.mResourceId + ", pid=" + this.pid + ", uid=" + this.uid + "}";
        }
    }

    /* access modifiers changed from: package-private */
    public static class RefcountedResourceArray<T extends IResource> {
        SparseArray<RefcountedResource<T>> mArray = new SparseArray<>();
        private final String mTypeName;

        public RefcountedResourceArray(String typeName) {
            this.mTypeName = typeName;
        }

        /* access modifiers changed from: package-private */
        public T getResourceOrThrow(int key) {
            return getRefcountedResourceOrThrow(key).getResource();
        }

        /* access modifiers changed from: package-private */
        public RefcountedResource<T> getRefcountedResourceOrThrow(int key) {
            RefcountedResource<T> resource = this.mArray.get(key);
            if (resource != null) {
                return resource;
            }
            throw new IllegalArgumentException(String.format("No such %s found for given id: %d", this.mTypeName, Integer.valueOf(key)));
        }

        /* access modifiers changed from: package-private */
        public void put(int key, RefcountedResource<T> obj) {
            Preconditions.checkNotNull(obj, "Null resources cannot be added");
            this.mArray.put(key, obj);
        }

        /* access modifiers changed from: package-private */
        public void remove(int key) {
            this.mArray.remove(key);
        }

        public String toString() {
            return this.mArray.toString();
        }
    }

    /* access modifiers changed from: private */
    public final class TransformRecord extends OwnedResourceRecord {
        private final IpSecConfig mConfig;
        private final EncapSocketRecord mSocket;
        private final SpiRecord mSpi;

        TransformRecord(int resourceId, IpSecConfig config, SpiRecord spi, EncapSocketRecord socket) {
            super(resourceId);
            this.mConfig = config;
            this.mSpi = spi;
            this.mSocket = socket;
            spi.setOwnedByTransform();
        }

        public IpSecConfig getConfig() {
            return this.mConfig;
        }

        public SpiRecord getSpiRecord() {
            return this.mSpi;
        }

        public EncapSocketRecord getSocketRecord() {
            return this.mSocket;
        }

        @Override // com.android.server.IpSecService.OwnedResourceRecord, com.android.server.IpSecService.IResource
        public void freeUnderlyingResources() {
            try {
                IpSecService.this.mSrvConfig.getNetdInstance().ipSecDeleteSecurityAssociation(this.uid, this.mConfig.getSourceAddress(), this.mConfig.getDestinationAddress(), this.mSpi.getSpi(), this.mConfig.getMarkValue(), this.mConfig.getMarkMask(), this.mConfig.getXfrmInterfaceId());
            } catch (RemoteException | ServiceSpecificException e) {
                Log.e(IpSecService.TAG, "Failed to delete SA with ID: " + this.mResourceId, e);
            }
            getResourceTracker().give();
        }

        @Override // com.android.server.IpSecService.OwnedResourceRecord, com.android.server.IpSecService.IResource
        public void invalidate() throws RemoteException {
            getUserRecord().removeTransformRecord(this.mResourceId);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.IpSecService.OwnedResourceRecord
        public ResourceTracker getResourceTracker() {
            return getUserRecord().mTransformQuotaTracker;
        }

        @Override // com.android.server.IpSecService.OwnedResourceRecord
        public String toString() {
            return "{super=" + super.toString() + ", mSocket=" + this.mSocket + ", mSpi.mResourceId=" + this.mSpi.mResourceId + ", mConfig=" + this.mConfig + "}";
        }
    }

    /* access modifiers changed from: private */
    public final class SpiRecord extends OwnedResourceRecord {
        private final String mDestinationAddress;
        private boolean mOwnedByTransform = false;
        private final String mSourceAddress;
        private int mSpi;

        SpiRecord(int resourceId, String sourceAddress, String destinationAddress, int spi) {
            super(resourceId);
            this.mSourceAddress = sourceAddress;
            this.mDestinationAddress = destinationAddress;
            this.mSpi = spi;
        }

        @Override // com.android.server.IpSecService.OwnedResourceRecord, com.android.server.IpSecService.IResource
        public void freeUnderlyingResources() {
            try {
                if (!this.mOwnedByTransform) {
                    IpSecService.this.mSrvConfig.getNetdInstance().ipSecDeleteSecurityAssociation(this.uid, this.mSourceAddress, this.mDestinationAddress, this.mSpi, 0, 0, 0);
                }
            } catch (RemoteException | ServiceSpecificException e) {
                Log.e(IpSecService.TAG, "Failed to delete SPI reservation with ID: " + this.mResourceId, e);
            }
            this.mSpi = 0;
            getResourceTracker().give();
        }

        public int getSpi() {
            return this.mSpi;
        }

        public String getDestinationAddress() {
            return this.mDestinationAddress;
        }

        public void setOwnedByTransform() {
            if (!this.mOwnedByTransform) {
                this.mOwnedByTransform = true;
                return;
            }
            throw new IllegalStateException("Cannot own an SPI twice!");
        }

        public boolean getOwnedByTransform() {
            return this.mOwnedByTransform;
        }

        @Override // com.android.server.IpSecService.OwnedResourceRecord, com.android.server.IpSecService.IResource
        public void invalidate() throws RemoteException {
            getUserRecord().removeSpiRecord(this.mResourceId);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.IpSecService.OwnedResourceRecord
        public ResourceTracker getResourceTracker() {
            return getUserRecord().mSpiQuotaTracker;
        }

        @Override // com.android.server.IpSecService.OwnedResourceRecord
        public String toString() {
            return "{super=" + super.toString() + ", mSpi=" + this.mSpi + ", mSourceAddress=" + this.mSourceAddress + ", mDestinationAddress=" + this.mDestinationAddress + ", mOwnedByTransform=" + this.mOwnedByTransform + "}";
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int reserveNetId() {
        synchronized (this.mTunnelNetIds) {
            for (int i = 0; i < 1024; i++) {
                int netId = TUN_INTF_NETID_START + this.mNextTunnelNetIdIndex;
                int i2 = this.mNextTunnelNetIdIndex + 1;
                this.mNextTunnelNetIdIndex = i2;
                if (i2 >= 1024) {
                    this.mNextTunnelNetIdIndex = 0;
                }
                if (!this.mTunnelNetIds.get(netId)) {
                    this.mTunnelNetIds.put(netId, true);
                    return netId;
                }
            }
            throw new IllegalStateException("No free netIds to allocate");
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void releaseNetId(int netId) {
        synchronized (this.mTunnelNetIds) {
            this.mTunnelNetIds.delete(netId);
        }
    }

    /* access modifiers changed from: private */
    public final class TunnelInterfaceRecord extends OwnedResourceRecord {
        private final int mIfId;
        private final int mIkey;
        private final String mInterfaceName;
        private final String mLocalAddress;
        private final int mOkey;
        private final String mRemoteAddress;
        private final Network mUnderlyingNetwork;

        TunnelInterfaceRecord(int resourceId, String interfaceName, Network underlyingNetwork, String localAddr, String remoteAddr, int ikey, int okey, int intfId) {
            super(resourceId);
            this.mInterfaceName = interfaceName;
            this.mUnderlyingNetwork = underlyingNetwork;
            this.mLocalAddress = localAddr;
            this.mRemoteAddress = remoteAddr;
            this.mIkey = ikey;
            this.mOkey = okey;
            this.mIfId = intfId;
        }

        @Override // com.android.server.IpSecService.OwnedResourceRecord, com.android.server.IpSecService.IResource
        public void freeUnderlyingResources() {
            try {
                INetd netd = IpSecService.this.mSrvConfig.getNetdInstance();
                netd.ipSecRemoveTunnelInterface(this.mInterfaceName);
                int[] iArr = IpSecService.ADDRESS_FAMILIES;
                for (int selAddrFamily : iArr) {
                    netd.ipSecDeleteSecurityPolicy(this.uid, selAddrFamily, 1, this.mOkey, -1, this.mIfId);
                    netd.ipSecDeleteSecurityPolicy(this.uid, selAddrFamily, 0, this.mIkey, -1, this.mIfId);
                }
            } catch (RemoteException | ServiceSpecificException e) {
                Log.e(IpSecService.TAG, "Failed to delete VTI with interface name: " + this.mInterfaceName + " and id: " + this.mResourceId, e);
            }
            getResourceTracker().give();
            IpSecService.this.releaseNetId(this.mIkey);
            IpSecService.this.releaseNetId(this.mOkey);
        }

        public String getInterfaceName() {
            return this.mInterfaceName;
        }

        public Network getUnderlyingNetwork() {
            return this.mUnderlyingNetwork;
        }

        public String getLocalAddress() {
            return this.mLocalAddress;
        }

        public String getRemoteAddress() {
            return this.mRemoteAddress;
        }

        public int getIkey() {
            return this.mIkey;
        }

        public int getOkey() {
            return this.mOkey;
        }

        public int getIfId() {
            return this.mIfId;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.IpSecService.OwnedResourceRecord
        public ResourceTracker getResourceTracker() {
            return getUserRecord().mTunnelQuotaTracker;
        }

        @Override // com.android.server.IpSecService.OwnedResourceRecord, com.android.server.IpSecService.IResource
        public void invalidate() {
            getUserRecord().removeTunnelInterfaceRecord(this.mResourceId);
        }

        @Override // com.android.server.IpSecService.OwnedResourceRecord
        public String toString() {
            return "{super=" + super.toString() + ", mInterfaceName=" + this.mInterfaceName + ", mUnderlyingNetwork=" + this.mUnderlyingNetwork + ", mLocalAddress=" + this.mLocalAddress + ", mRemoteAddress=" + this.mRemoteAddress + ", mIkey=" + this.mIkey + ", mOkey=" + this.mOkey + "}";
        }
    }

    /* access modifiers changed from: private */
    public final class EncapSocketRecord extends OwnedResourceRecord {
        private final int mPort;
        private FileDescriptor mSocket;

        EncapSocketRecord(int resourceId, FileDescriptor socket, int port) {
            super(resourceId);
            this.mSocket = socket;
            this.mPort = port;
        }

        @Override // com.android.server.IpSecService.OwnedResourceRecord, com.android.server.IpSecService.IResource
        public void freeUnderlyingResources() {
            Log.d(IpSecService.TAG, "Closing port " + this.mPort);
            IoUtils.closeQuietly(this.mSocket);
            this.mSocket = null;
            getResourceTracker().give();
        }

        public int getPort() {
            return this.mPort;
        }

        public FileDescriptor getFileDescriptor() {
            return this.mSocket;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.IpSecService.OwnedResourceRecord
        public ResourceTracker getResourceTracker() {
            return getUserRecord().mSocketQuotaTracker;
        }

        @Override // com.android.server.IpSecService.OwnedResourceRecord, com.android.server.IpSecService.IResource
        public void invalidate() {
            getUserRecord().removeEncapSocketRecord(this.mResourceId);
        }

        @Override // com.android.server.IpSecService.OwnedResourceRecord
        public String toString() {
            return "{super=" + super.toString() + ", mSocket=" + this.mSocket + ", mPort=" + this.mPort + "}";
        }
    }

    private IpSecService(Context context) {
        this(context, IpSecServiceConfiguration.GETSRVINSTANCE);
    }

    static IpSecService create(Context context) throws InterruptedException {
        IpSecService service = new IpSecService(context);
        service.connectNativeNetdService();
        return service;
    }

    private AppOpsManager getAppOpsManager() {
        AppOpsManager appOps = (AppOpsManager) this.mContext.getSystemService("appops");
        if (appOps != null) {
            return appOps;
        }
        throw new RuntimeException("System Server couldn't get AppOps");
    }

    @VisibleForTesting
    public IpSecService(Context context, IpSecServiceConfiguration config) {
        this(context, config, $$Lambda$IpSecService$AnqunmSwm_yQvDDEPggokhVs5M.INSTANCE);
    }

    static /* synthetic */ void lambda$new$0(FileDescriptor fd, int uid) throws IOException {
        try {
            TrafficStats.setThreadStatsUid(uid);
            TrafficStats.tagFileDescriptor(fd);
        } finally {
            TrafficStats.clearThreadStatsUid();
        }
    }

    @VisibleForTesting
    public IpSecService(Context context, IpSecServiceConfiguration config, UidFdTagger uidFdTagger) {
        this.mNextResourceId = 1;
        this.mUserResourceTracker = new UserResourceTracker();
        this.mTunnelNetIds = new SparseBooleanArray();
        this.mNextTunnelNetIdIndex = 0;
        this.mContext = context;
        this.mSrvConfig = config;
        this.mUidFdTagger = uidFdTagger;
    }

    public void systemReady() {
        if (isNetdAlive()) {
            Slog.d(TAG, "IpSecService is ready");
        } else {
            Slog.wtf(TAG, "IpSecService not ready: failed to connect to NetD Native Service!");
        }
    }

    private void connectNativeNetdService() {
        new Thread() {
            /* class com.android.server.IpSecService.AnonymousClass1 */

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                synchronized (IpSecService.this) {
                    NetdService.get(5000);
                }
            }
        }.start();
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    public synchronized boolean isNetdAlive() {
        try {
            INetd netd = this.mSrvConfig.getNetdInstance();
            if (netd == null) {
                return false;
            }
            return netd.isAlive();
        } catch (RemoteException e) {
            return false;
        }
    }

    private static void checkInetAddress(String inetAddress) {
        if (TextUtils.isEmpty(inetAddress)) {
            throw new IllegalArgumentException("Unspecified address");
        } else if (NetworkUtils.numericToInetAddress(inetAddress).isAnyLocalAddress()) {
            throw new IllegalArgumentException("Inappropriate wildcard address: " + inetAddress);
        }
    }

    private static void checkDirection(int direction) {
        if (direction != 0 && direction != 1) {
            throw new IllegalArgumentException("Invalid Direction: " + direction);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x00a8  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00b1  */
    public synchronized IpSecSpiResponse allocateSecurityParameterIndex(String destinationAddress, int requestedSpi, IBinder binder) throws RemoteException {
        int spi;
        ServiceSpecificException e;
        RemoteException e2;
        checkInetAddress(destinationAddress);
        if (requestedSpi > 0) {
            if (requestedSpi < 256) {
                throw new IllegalArgumentException("ESP SPI must not be in the range of 0-255.");
            }
        }
        Preconditions.checkNotNull(binder, "Null Binder passed to allocateSecurityParameterIndex");
        int callingUid = Binder.getCallingUid();
        UserRecord userRecord = this.mUserResourceTracker.getUserRecord(callingUid);
        int resourceId = this.mNextResourceId;
        this.mNextResourceId = resourceId + 1;
        try {
            if (!userRecord.mSpiQuotaTracker.isAvailable()) {
                return new IpSecSpiResponse(1, -1, 0);
            }
            try {
                spi = this.mSrvConfig.getNetdInstance().ipSecAllocateSpi(callingUid, "", destinationAddress, requestedSpi);
            } catch (ServiceSpecificException e3) {
                e = e3;
                spi = 0;
                if (e.errorCode == OsConstants.ENOENT) {
                }
            } catch (RemoteException e4) {
                e2 = e4;
                throw e2.rethrowFromSystemServer();
            }
            try {
                Log.d(TAG, "Allocated SPI " + spi);
                userRecord.mSpiRecords.put(resourceId, new RefcountedResource<>(new SpiRecord(resourceId, "", destinationAddress, spi), binder, new RefcountedResource[0]));
                return new IpSecSpiResponse(0, resourceId, spi);
            } catch (ServiceSpecificException e5) {
                e = e5;
                if (e.errorCode == OsConstants.ENOENT) {
                }
            } catch (RemoteException e6) {
                e2 = e6;
                throw e2.rethrowFromSystemServer();
            }
        } catch (ServiceSpecificException e7) {
            e = e7;
            spi = 0;
            if (e.errorCode == OsConstants.ENOENT) {
                return new IpSecSpiResponse(2, -1, spi);
            }
            throw e;
        } catch (RemoteException e8) {
            e2 = e8;
            throw e2.rethrowFromSystemServer();
        }
    }

    private void releaseResource(RefcountedResourceArray resArray, int resourceId) throws RemoteException {
        resArray.getRefcountedResourceOrThrow(resourceId).userRelease();
    }

    public synchronized void releaseSecurityParameterIndex(int resourceId) throws RemoteException {
        releaseResource(this.mUserResourceTracker.getUserRecord(Binder.getCallingUid()).mSpiRecords, resourceId);
    }

    private int bindToRandomPort(FileDescriptor sockFd) throws IOException {
        for (int i = 10; i > 0; i--) {
            try {
                FileDescriptor probeSocket = Os.socket(OsConstants.AF_INET, OsConstants.SOCK_DGRAM, OsConstants.IPPROTO_UDP);
                Os.bind(probeSocket, INADDR_ANY, 0);
                int port = ((InetSocketAddress) Os.getsockname(probeSocket)).getPort();
                Os.close(probeSocket);
                Log.v(TAG, "Binding to port " + port);
                Os.bind(sockFd, INADDR_ANY, port);
                return port;
            } catch (ErrnoException e) {
                if (e.errno != OsConstants.EADDRINUSE) {
                    throw e.rethrowAsIOException();
                }
            }
        }
        throw new IOException("Failed 10 attempts to bind to a port");
    }

    public synchronized IpSecUdpEncapResponse openUdpEncapsulationSocket(int port, IBinder binder) throws RemoteException {
        if (port == 0 || (port >= 1024 && port <= PORT_MAX)) {
            Preconditions.checkNotNull(binder, "Null Binder passed to openUdpEncapsulationSocket");
            int callingUid = Binder.getCallingUid();
            UserRecord userRecord = this.mUserResourceTracker.getUserRecord(callingUid);
            int resourceId = this.mNextResourceId;
            this.mNextResourceId = resourceId + 1;
            if (!userRecord.mSocketQuotaTracker.isAvailable()) {
                return new IpSecUdpEncapResponse(1);
            }
            try {
                FileDescriptor sockFd = Os.socket(OsConstants.AF_INET, OsConstants.SOCK_DGRAM, OsConstants.IPPROTO_UDP);
                this.mUidFdTagger.tag(sockFd, callingUid);
                Os.setsockoptInt(sockFd, OsConstants.IPPROTO_UDP, OsConstants.UDP_ENCAP, OsConstants.UDP_ENCAP_ESPINUDP);
                this.mSrvConfig.getNetdInstance().ipSecSetEncapSocketOwner(new ParcelFileDescriptor(sockFd), callingUid);
                if (port != 0) {
                    Log.v(TAG, "Binding to port " + port);
                    Os.bind(sockFd, INADDR_ANY, port);
                } else {
                    port = bindToRandomPort(sockFd);
                }
                userRecord.mEncapSocketRecords.put(resourceId, new RefcountedResource<>(new EncapSocketRecord(resourceId, sockFd, port), binder, new RefcountedResource[0]));
                return new IpSecUdpEncapResponse(0, resourceId, port, sockFd);
            } catch (ErrnoException | IOException e) {
                IoUtils.closeQuietly((FileDescriptor) null);
                return new IpSecUdpEncapResponse(1);
            }
        } else {
            throw new IllegalArgumentException("Specified port number must be a valid non-reserved UDP port");
        }
    }

    public synchronized void closeUdpEncapsulationSocket(int resourceId) throws RemoteException {
        releaseResource(this.mUserResourceTracker.getUserRecord(Binder.getCallingUid()).mEncapSocketRecords, resourceId);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:68:0x019a, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x019b, code lost:
        r29 = r7;
        r30 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x01b0, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x01b1, code lost:
        r4 = r7;
        r1 = r8;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x019a A[ExcHandler: all (th java.lang.Throwable), Splitter:B:10:0x005f] */
    public synchronized IpSecTunnelInterfaceResponse createTunnelInterface(String localAddr, String remoteAddr, Network underlyingNetwork, IBinder binder, String callingPackage) {
        int okey;
        int ikey;
        RemoteException e;
        int ikey2;
        int okey2;
        Throwable t;
        int selAddrFamily;
        enforceTunnelFeatureAndPermissions(callingPackage);
        Preconditions.checkNotNull(binder, "Null Binder passed to createTunnelInterface");
        Preconditions.checkNotNull(underlyingNetwork, "No underlying network was specified");
        checkInetAddress(localAddr);
        checkInetAddress(remoteAddr);
        int callerUid = Binder.getCallingUid();
        UserRecord userRecord = this.mUserResourceTracker.getUserRecord(callerUid);
        if (!userRecord.mTunnelQuotaTracker.isAvailable()) {
            return new IpSecTunnelInterfaceResponse(1);
        }
        int i = this.mNextResourceId;
        this.mNextResourceId = i + 1;
        int resourceId = i;
        int ikey3 = reserveNetId();
        int okey3 = reserveNetId();
        String intfName = String.format("%s%d", INetd.IPSEC_INTERFACE_PREFIX, Integer.valueOf(resourceId));
        try {
            INetd netd = this.mSrvConfig.getNetdInstance();
            netd.ipSecAddTunnelInterface(intfName, localAddr, remoteAddr, ikey3, okey3, resourceId);
            int[] iArr = ADDRESS_FAMILIES;
            int length = iArr.length;
            int i2 = 0;
            while (i2 < length) {
                try {
                    selAddrFamily = iArr[i2];
                    okey2 = okey3;
                    ikey2 = ikey3;
                } catch (RemoteException e2) {
                    e = e2;
                    okey = okey3;
                    ikey = ikey3;
                    releaseNetId(ikey);
                    releaseNetId(okey);
                    throw e.rethrowFromSystemServer();
                } catch (Throwable th) {
                    t = th;
                    okey2 = okey3;
                    ikey2 = ikey3;
                    releaseNetId(ikey2);
                    releaseNetId(okey2);
                    throw t;
                }
                try {
                    netd.ipSecAddSecurityPolicy(callerUid, selAddrFamily, 1, localAddr, remoteAddr, 0, okey2, -1, resourceId);
                    try {
                        netd.ipSecAddSecurityPolicy(callerUid, selAddrFamily, 0, remoteAddr, localAddr, 0, ikey2, -1, resourceId);
                        i2++;
                        userRecord = userRecord;
                        length = length;
                        intfName = intfName;
                        okey3 = okey2;
                        ikey3 = ikey2;
                        resourceId = resourceId;
                        callerUid = callerUid;
                    } catch (RemoteException e3) {
                        e = e3;
                        okey = okey2;
                        ikey = ikey2;
                        releaseNetId(ikey);
                        releaseNetId(okey);
                        throw e.rethrowFromSystemServer();
                    } catch (Throwable th2) {
                        t = th2;
                        releaseNetId(ikey2);
                        releaseNetId(okey2);
                        throw t;
                    }
                } catch (RemoteException e4) {
                    e = e4;
                    okey = okey2;
                    ikey = ikey2;
                    releaseNetId(ikey);
                    releaseNetId(okey);
                    throw e.rethrowFromSystemServer();
                } catch (Throwable th3) {
                    t = th3;
                    releaseNetId(ikey2);
                    releaseNetId(okey2);
                    throw t;
                }
            }
            okey2 = okey3;
            ikey2 = ikey3;
            try {
                try {
                } catch (RemoteException e5) {
                    e = e5;
                    okey = okey2;
                    ikey = ikey2;
                    releaseNetId(ikey);
                    releaseNetId(okey);
                    throw e.rethrowFromSystemServer();
                } catch (Throwable th4) {
                    t = th4;
                    releaseNetId(ikey2);
                    releaseNetId(okey2);
                    throw t;
                }
                try {
                    userRecord.mTunnelInterfaceRecords.put(resourceId, new RefcountedResource<>(new TunnelInterfaceRecord(resourceId, intfName, underlyingNetwork, localAddr, remoteAddr, ikey2, okey2, resourceId), binder, new RefcountedResource[0]));
                } catch (RemoteException e6) {
                    e = e6;
                    okey = okey2;
                    ikey = ikey2;
                    releaseNetId(ikey);
                    releaseNetId(okey);
                    throw e.rethrowFromSystemServer();
                } catch (Throwable th5) {
                    t = th5;
                    releaseNetId(ikey2);
                    releaseNetId(okey2);
                    throw t;
                }
            } catch (RemoteException e7) {
                e = e7;
                okey = okey2;
                ikey = ikey2;
                releaseNetId(ikey);
                releaseNetId(okey);
                throw e.rethrowFromSystemServer();
            } catch (Throwable th6) {
                t = th6;
                releaseNetId(ikey2);
                releaseNetId(okey2);
                throw t;
            }
            try {
                return new IpSecTunnelInterfaceResponse(0, resourceId, intfName);
            } catch (RemoteException e8) {
                e = e8;
                okey = okey2;
                ikey = ikey2;
                releaseNetId(ikey);
                releaseNetId(okey);
                throw e.rethrowFromSystemServer();
            } catch (Throwable th7) {
                t = th7;
                releaseNetId(ikey2);
                releaseNetId(okey2);
                throw t;
            }
        } catch (RemoteException e9) {
            e = e9;
            okey = okey3;
            ikey = ikey3;
            releaseNetId(ikey);
            releaseNetId(okey);
            throw e.rethrowFromSystemServer();
        } catch (Throwable th8) {
        }
    }

    public synchronized void addAddressToTunnelInterface(int tunnelResourceId, LinkAddress localAddr, String callingPackage) {
        enforceTunnelFeatureAndPermissions(callingPackage);
        try {
            this.mSrvConfig.getNetdInstance().interfaceAddAddress(this.mUserResourceTracker.getUserRecord(Binder.getCallingUid()).mTunnelInterfaceRecords.getResourceOrThrow(tunnelResourceId).mInterfaceName, localAddr.getAddress().getHostAddress(), localAddr.getPrefixLength());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public synchronized void removeAddressFromTunnelInterface(int tunnelResourceId, LinkAddress localAddr, String callingPackage) {
        enforceTunnelFeatureAndPermissions(callingPackage);
        try {
            this.mSrvConfig.getNetdInstance().interfaceDelAddress(this.mUserResourceTracker.getUserRecord(Binder.getCallingUid()).mTunnelInterfaceRecords.getResourceOrThrow(tunnelResourceId).mInterfaceName, localAddr.getAddress().getHostAddress(), localAddr.getPrefixLength());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public synchronized void deleteTunnelInterface(int resourceId, String callingPackage) throws RemoteException {
        enforceTunnelFeatureAndPermissions(callingPackage);
        releaseResource(this.mUserResourceTracker.getUserRecord(Binder.getCallingUid()).mTunnelInterfaceRecords, resourceId);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void validateAlgorithms(IpSecConfig config) throws IllegalArgumentException {
        IpSecAlgorithm auth = config.getAuthentication();
        IpSecAlgorithm crypt = config.getEncryption();
        IpSecAlgorithm aead = config.getAuthenticatedEncryption();
        boolean z = false;
        Preconditions.checkArgument((aead == null && crypt == null && auth == null) ? false : true, "No Encryption or Authentication algorithms specified");
        Preconditions.checkArgument(auth == null || auth.isAuthentication(), "Unsupported algorithm for Authentication");
        Preconditions.checkArgument(crypt == null || crypt.isEncryption(), "Unsupported algorithm for Encryption");
        Preconditions.checkArgument(aead == null || aead.isAead(), "Unsupported algorithm for Authenticated Encryption");
        if (aead == null || (auth == null && crypt == null)) {
            z = true;
        }
        Preconditions.checkArgument(z, "Authenticated Encryption is mutually exclusive with other Authentication or Encryption algorithms");
    }

    private int getFamily(String inetAddress) {
        int family = OsConstants.AF_UNSPEC;
        InetAddress checkAddress = NetworkUtils.numericToInetAddress(inetAddress);
        if (checkAddress instanceof Inet4Address) {
            return OsConstants.AF_INET;
        }
        if (checkAddress instanceof Inet6Address) {
            return OsConstants.AF_INET6;
        }
        return family;
    }

    private void checkIpSecConfig(IpSecConfig config) {
        UserRecord userRecord = this.mUserResourceTracker.getUserRecord(Binder.getCallingUid());
        int encapType = config.getEncapType();
        if (encapType != 0) {
            if (encapType == 1 || encapType == 2) {
                userRecord.mEncapSocketRecords.getResourceOrThrow(config.getEncapSocketResourceId());
                int port = config.getEncapRemotePort();
                if (port <= 0 || port > PORT_MAX) {
                    throw new IllegalArgumentException("Invalid remote UDP port: " + port);
                }
            } else {
                throw new IllegalArgumentException("Invalid Encap Type: " + config.getEncapType());
            }
        }
        validateAlgorithms(config);
        SpiRecord s = userRecord.mSpiRecords.getResourceOrThrow(config.getSpiResourceId());
        if (!s.getOwnedByTransform()) {
            if (TextUtils.isEmpty(config.getDestinationAddress())) {
                config.setDestinationAddress(s.getDestinationAddress());
            }
            if (config.getDestinationAddress().equals(s.getDestinationAddress())) {
                checkInetAddress(config.getDestinationAddress());
                checkInetAddress(config.getSourceAddress());
                String sourceAddress = config.getSourceAddress();
                String destinationAddress = config.getDestinationAddress();
                int sourceFamily = getFamily(sourceAddress);
                if (sourceFamily != getFamily(destinationAddress)) {
                    throw new IllegalArgumentException("Source address (" + sourceAddress + ") and destination address (" + destinationAddress + ") have different address families.");
                } else if (config.getEncapType() == 0 || sourceFamily == OsConstants.AF_INET) {
                    int mode = config.getMode();
                    if (mode == 0 || mode == 1) {
                        config.setMarkValue(0);
                        config.setMarkMask(0);
                        return;
                    }
                    throw new IllegalArgumentException("Invalid IpSecTransform.mode: " + config.getMode());
                } else {
                    throw new IllegalArgumentException("UDP Encapsulation is not supported for this address family");
                }
            } else {
                throw new IllegalArgumentException("Mismatched remote addresseses.");
            }
        } else {
            throw new IllegalStateException("SPI already in use; cannot be used in new Transforms");
        }
    }

    private void enforceTunnelFeatureAndPermissions(String callingPackage) {
        if (this.mContext.getPackageManager().hasSystemFeature("android.software.ipsec_tunnels")) {
            Preconditions.checkNotNull(callingPackage, "Null calling package cannot create IpSec tunnels");
            int noteOp = getAppOpsManager().noteOp(TUNNEL_OP, Binder.getCallingUid(), callingPackage);
            if (noteOp == 0) {
                return;
            }
            if (noteOp == 3) {
                this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_IPSEC_TUNNELS", TAG);
                return;
            }
            throw new SecurityException("Request to ignore AppOps for non-legacy API");
        }
        throw new UnsupportedOperationException("IPsec Tunnel Mode requires PackageManager.FEATURE_IPSEC_TUNNELS");
    }

    private void createOrUpdateTransform(IpSecConfig c, int resourceId, SpiRecord spiRecord, EncapSocketRecord socketRecord) throws RemoteException {
        int encapRemotePort;
        int encapLocalPort;
        String cryptName;
        int encapType = c.getEncapType();
        if (encapType != 0) {
            encapLocalPort = socketRecord.getPort();
            encapRemotePort = c.getEncapRemotePort();
        } else {
            encapLocalPort = 0;
            encapRemotePort = 0;
        }
        IpSecAlgorithm auth = c.getAuthentication();
        IpSecAlgorithm crypt = c.getEncryption();
        IpSecAlgorithm authCrypt = c.getAuthenticatedEncryption();
        String str = "";
        if (crypt == null) {
            cryptName = authCrypt == null ? "ecb(cipher_null)" : str;
        } else {
            cryptName = crypt.getName();
        }
        INetd netdInstance = this.mSrvConfig.getNetdInstance();
        int callingUid = Binder.getCallingUid();
        int mode = c.getMode();
        String sourceAddress = c.getSourceAddress();
        String destinationAddress = c.getDestinationAddress();
        int i = c.getNetwork() != null ? c.getNetwork().netId : 0;
        int spi = spiRecord.getSpi();
        int markValue = c.getMarkValue();
        int markMask = c.getMarkMask();
        String name = auth != null ? auth.getName() : str;
        byte[] key = auth != null ? auth.getKey() : new byte[0];
        int truncationLengthBits = auth != null ? auth.getTruncationLengthBits() : 0;
        byte[] key2 = crypt != null ? crypt.getKey() : new byte[0];
        int truncationLengthBits2 = crypt != null ? crypt.getTruncationLengthBits() : 0;
        if (authCrypt != null) {
            str = authCrypt.getName();
        }
        netdInstance.ipSecAddSecurityAssociation(callingUid, mode, sourceAddress, destinationAddress, i, spi, markValue, markMask, name, key, truncationLengthBits, cryptName, key2, truncationLengthBits2, str, authCrypt != null ? authCrypt.getKey() : new byte[0], authCrypt != null ? authCrypt.getTruncationLengthBits() : 0, encapType, encapLocalPort, encapRemotePort, c.getXfrmInterfaceId());
    }

    public synchronized IpSecTransformResponse createTransform(IpSecConfig c, IBinder binder, String callingPackage) throws RemoteException {
        EncapSocketRecord socketRecord;
        Preconditions.checkNotNull(c);
        if (c.getMode() == 1) {
            enforceTunnelFeatureAndPermissions(callingPackage);
        }
        checkIpSecConfig(c);
        Preconditions.checkNotNull(binder, "Null Binder passed to createTransform");
        int resourceId = this.mNextResourceId;
        this.mNextResourceId = resourceId + 1;
        UserRecord userRecord = this.mUserResourceTracker.getUserRecord(Binder.getCallingUid());
        List<RefcountedResource> dependencies = new ArrayList<>();
        if (!userRecord.mTransformQuotaTracker.isAvailable()) {
            return new IpSecTransformResponse(1);
        }
        if (c.getEncapType() != 0) {
            RefcountedResource<EncapSocketRecord> refcountedSocketRecord = userRecord.mEncapSocketRecords.getRefcountedResourceOrThrow(c.getEncapSocketResourceId());
            dependencies.add(refcountedSocketRecord);
            socketRecord = refcountedSocketRecord.getResource();
        } else {
            socketRecord = null;
        }
        RefcountedResource<SpiRecord> refcountedSpiRecord = userRecord.mSpiRecords.getRefcountedResourceOrThrow(c.getSpiResourceId());
        dependencies.add(refcountedSpiRecord);
        SpiRecord spiRecord = refcountedSpiRecord.getResource();
        createOrUpdateTransform(c, resourceId, spiRecord, socketRecord);
        userRecord.mTransformRecords.put(resourceId, new RefcountedResource<>(new TransformRecord(resourceId, c, spiRecord, socketRecord), binder, (RefcountedResource[]) dependencies.toArray(new RefcountedResource[dependencies.size()])));
        return new IpSecTransformResponse(0, resourceId);
    }

    public synchronized void deleteTransform(int resourceId) throws RemoteException {
        releaseResource(this.mUserResourceTracker.getUserRecord(Binder.getCallingUid()).mTransformRecords, resourceId);
    }

    public synchronized void applyTransportModeTransform(ParcelFileDescriptor socket, int direction, int resourceId) throws RemoteException {
        int callingUid = Binder.getCallingUid();
        UserRecord userRecord = this.mUserResourceTracker.getUserRecord(callingUid);
        checkDirection(direction);
        TransformRecord info = userRecord.mTransformRecords.getResourceOrThrow(resourceId);
        if (info.pid == getCallingPid() && info.uid == callingUid) {
            IpSecConfig c = info.getConfig();
            Preconditions.checkArgument(c.getMode() == 0, "Transform mode was not Transport mode; cannot be applied to a socket");
            this.mSrvConfig.getNetdInstance().ipSecApplyTransportModeTransform(socket, callingUid, direction, c.getSourceAddress(), c.getDestinationAddress(), info.getSpiRecord().getSpi());
        } else {
            throw new SecurityException("Only the owner of an IpSec Transform may apply it!");
        }
    }

    public synchronized void removeTransportModeTransforms(ParcelFileDescriptor socket) throws RemoteException {
        this.mSrvConfig.getNetdInstance().ipSecRemoveTransportModeTransform(socket);
    }

    /* JADX WARNING: Removed duplicated region for block: B:48:0x0119  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0123  */
    public synchronized void applyTunnelModeTransform(int tunnelResourceId, int direction, int transformResourceId, String callingPackage) throws RemoteException {
        EncapSocketRecord socketRecord;
        int mark;
        ServiceSpecificException e;
        enforceTunnelFeatureAndPermissions(callingPackage);
        checkDirection(direction);
        int callingUid = Binder.getCallingUid();
        UserRecord userRecord = this.mUserResourceTracker.getUserRecord(callingUid);
        TransformRecord transformInfo = userRecord.mTransformRecords.getResourceOrThrow(transformResourceId);
        TunnelInterfaceRecord tunnelInterfaceInfo = userRecord.mTunnelInterfaceRecords.getResourceOrThrow(tunnelResourceId);
        IpSecConfig c = transformInfo.getConfig();
        int i = 0;
        Preconditions.checkArgument(c.getMode() == 1, "Transform mode was not Tunnel mode; cannot be applied to a tunnel interface");
        if (c.getEncapType() != 0) {
            socketRecord = userRecord.mEncapSocketRecords.getResourceOrThrow(c.getEncapSocketResourceId());
        } else {
            socketRecord = null;
        }
        SpiRecord spiRecord = userRecord.mSpiRecords.getResourceOrThrow(c.getSpiResourceId());
        if (direction == 1) {
            mark = tunnelInterfaceInfo.getOkey();
        } else {
            mark = tunnelInterfaceInfo.getIkey();
        }
        int spi = 0;
        try {
            c.setXfrmInterfaceId(tunnelInterfaceInfo.getIfId());
            if (direction == 1) {
                try {
                    c.setNetwork(tunnelInterfaceInfo.getUnderlyingNetwork());
                    spi = transformInfo.getSpiRecord().getSpi();
                } catch (ServiceSpecificException e2) {
                    e = e2;
                    if (e.errorCode != OsConstants.EINVAL) {
                    }
                }
            }
            int[] iArr = ADDRESS_FAMILIES;
            int length = iArr.length;
            while (i < length) {
                try {
                } catch (ServiceSpecificException e3) {
                    e = e3;
                    if (e.errorCode != OsConstants.EINVAL) {
                        throw new IllegalArgumentException(e.toString());
                    }
                    throw e;
                }
                try {
                    this.mSrvConfig.getNetdInstance().ipSecUpdateSecurityPolicy(callingUid, iArr[i], direction, transformInfo.getConfig().getSourceAddress(), transformInfo.getConfig().getDestinationAddress(), spi, mark, -1, c.getXfrmInterfaceId());
                    i++;
                    c = c;
                    userRecord = userRecord;
                    length = length;
                    iArr = iArr;
                    spiRecord = spiRecord;
                    socketRecord = socketRecord;
                } catch (ServiceSpecificException e4) {
                    e = e4;
                    if (e.errorCode != OsConstants.EINVAL) {
                    }
                }
            }
            try {
                createOrUpdateTransform(c, transformResourceId, spiRecord, socketRecord);
            } catch (ServiceSpecificException e5) {
                e = e5;
            }
        } catch (ServiceSpecificException e6) {
            e = e6;
            if (e.errorCode != OsConstants.EINVAL) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        pw.println("IpSecService dump:");
        StringBuilder sb = new StringBuilder();
        sb.append("NetdNativeService Connection: ");
        sb.append(isNetdAlive() ? "alive" : "dead");
        pw.println(sb.toString());
        pw.println();
        pw.println("mUserResourceTracker:");
        pw.println(this.mUserResourceTracker);
    }
}
