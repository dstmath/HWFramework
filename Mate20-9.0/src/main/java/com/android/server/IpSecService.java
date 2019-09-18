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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import libcore.io.IoUtils;

public class IpSecService extends IIpSecService.Stub {
    private static final boolean DBG = Log.isLoggable(TAG, 3);
    /* access modifiers changed from: private */
    public static final int[] DIRECTIONS = {1, 0};
    static final int FREE_PORT_MIN = 1024;
    private static final InetAddress INADDR_ANY;
    private static final int MAX_PORT_BIND_ATTEMPTS = 10;
    private static final int NETD_FETCH_TIMEOUT_MS = 5000;
    private static final String NETD_SERVICE_NAME = "netd";
    static final int PORT_MAX = 65535;
    private static final String TAG = "IpSecService";
    @VisibleForTesting
    static final int TUN_INTF_NETID_RANGE = 1024;
    @VisibleForTesting
    static final int TUN_INTF_NETID_START = 64512;
    /* access modifiers changed from: private */
    public static final String[] WILDCARD_ADDRESSES = {"0.0.0.0", "::"};
    private final Context mContext;
    @GuardedBy("IpSecService.this")
    private int mNextResourceId;
    private int mNextTunnelNetIdIndex;
    /* access modifiers changed from: private */
    public final IpSecServiceConfiguration mSrvConfig;
    private final SparseBooleanArray mTunnelNetIds;
    final UidFdTagger mUidFdTagger;
    @VisibleForTesting
    final UserResourceTracker mUserResourceTracker;

    private final class EncapSocketRecord extends OwnedResourceRecord {
        private final int mPort;
        private FileDescriptor mSocket;

        EncapSocketRecord(int resourceId, FileDescriptor socket, int port) {
            super(resourceId);
            this.mSocket = socket;
            this.mPort = port;
        }

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
        public ResourceTracker getResourceTracker() {
            return getUserRecord().mSocketQuotaTracker;
        }

        public void invalidate() {
            getUserRecord().removeEncapSocketRecord(this.mResourceId);
        }

        public String toString() {
            return "{super=" + super.toString() + ", mSocket=" + this.mSocket + ", mPort=" + this.mPort + "}";
        }
    }

    @VisibleForTesting
    public interface IResource {
        void freeUnderlyingResources() throws RemoteException;

        void invalidate() throws RemoteException;
    }

    interface IpSecServiceConfiguration {
        public static final IpSecServiceConfiguration GETSRVINSTANCE = new IpSecServiceConfiguration() {
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

    private abstract class OwnedResourceRecord implements IResource {
        protected final int mResourceId;
        final int pid;
        final int uid;

        public abstract void freeUnderlyingResources() throws RemoteException;

        /* access modifiers changed from: protected */
        public abstract ResourceTracker getResourceTracker();

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
                }
            }
        }

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

        @GuardedBy("IpSecService.this")
        public void userRelease() throws RemoteException {
            if (this.mBinder != null) {
                this.mBinder.unlinkToDeath(this, 0);
                this.mBinder = null;
                this.mResource.invalidate();
                releaseReference();
            }
        }

        @GuardedBy("IpSecService.this")
        @VisibleForTesting
        public void releaseReference() throws RemoteException {
            this.mRefCount--;
            if (this.mRefCount <= 0) {
                if (this.mRefCount >= 0) {
                    this.mResource.freeUnderlyingResources();
                    for (RefcountedResource<? extends IResource> child : this.mChildren) {
                        child.releaseReference();
                    }
                    this.mRefCount--;
                    return;
                }
                throw new IllegalStateException("Invalid operation - resource has already been released.");
            }
        }

        public String toString() {
            return "{mResource=" + this.mResource + ", mRefCount=" + this.mRefCount + ", mChildren=" + this.mChildren + "}";
        }
    }

    static class RefcountedResourceArray<T extends IResource> {
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
            throw new IllegalArgumentException(String.format("No such %s found for given id: %d", new Object[]{this.mTypeName, Integer.valueOf(key)}));
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

    @VisibleForTesting
    static class ResourceTracker {
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

    private final class SpiRecord extends OwnedResourceRecord {
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

        public void freeUnderlyingResources() {
            try {
                if (!this.mOwnedByTransform) {
                    IpSecService.this.mSrvConfig.getNetdInstance().ipSecDeleteSecurityAssociation(this.mResourceId, this.mSourceAddress, this.mDestinationAddress, this.mSpi, 0, 0);
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

        public void invalidate() throws RemoteException {
            getUserRecord().removeSpiRecord(this.mResourceId);
        }

        /* access modifiers changed from: protected */
        public ResourceTracker getResourceTracker() {
            return getUserRecord().mSpiQuotaTracker;
        }

        public String toString() {
            return "{super=" + super.toString() + ", mSpi=" + this.mSpi + ", mSourceAddress=" + this.mSourceAddress + ", mDestinationAddress=" + this.mDestinationAddress + ", mOwnedByTransform=" + this.mOwnedByTransform + "}";
        }
    }

    private final class TransformRecord extends OwnedResourceRecord {
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

        public void freeUnderlyingResources() {
            try {
                IpSecService.this.mSrvConfig.getNetdInstance().ipSecDeleteSecurityAssociation(this.mResourceId, this.mConfig.getSourceAddress(), this.mConfig.getDestinationAddress(), this.mSpi.getSpi(), this.mConfig.getMarkValue(), this.mConfig.getMarkMask());
            } catch (RemoteException | ServiceSpecificException e) {
                Log.e(IpSecService.TAG, "Failed to delete SA with ID: " + this.mResourceId, e);
            }
            getResourceTracker().give();
        }

        public void invalidate() throws RemoteException {
            getUserRecord().removeTransformRecord(this.mResourceId);
        }

        /* access modifiers changed from: protected */
        public ResourceTracker getResourceTracker() {
            return getUserRecord().mTransformQuotaTracker;
        }

        public String toString() {
            return "{super=" + super.toString() + ", mSocket=" + this.mSocket + ", mSpi.mResourceId=" + this.mSpi.mResourceId + ", mConfig=" + this.mConfig + "}";
        }
    }

    private final class TunnelInterfaceRecord extends OwnedResourceRecord {
        private final int mIkey;
        /* access modifiers changed from: private */
        public final String mInterfaceName;
        private final String mLocalAddress;
        private final int mOkey;
        private final String mRemoteAddress;
        private final Network mUnderlyingNetwork;

        TunnelInterfaceRecord(int resourceId, String interfaceName, Network underlyingNetwork, String localAddr, String remoteAddr, int ikey, int okey) {
            super(resourceId);
            this.mInterfaceName = interfaceName;
            this.mUnderlyingNetwork = underlyingNetwork;
            this.mLocalAddress = localAddr;
            this.mRemoteAddress = remoteAddr;
            this.mIkey = ikey;
            this.mOkey = okey;
        }

        public void freeUnderlyingResources() {
            try {
                IpSecService.this.mSrvConfig.getNetdInstance().removeVirtualTunnelInterface(this.mInterfaceName);
                for (String wildcardAddr : IpSecService.WILDCARD_ADDRESSES) {
                    int[] access$200 = IpSecService.DIRECTIONS;
                    int length = access$200.length;
                    for (int i = 0; i < length; i++) {
                        int direction = access$200[i];
                        IpSecService.this.mSrvConfig.getNetdInstance().ipSecDeleteSecurityPolicy(0, direction, wildcardAddr, wildcardAddr, direction == 0 ? this.mIkey : this.mOkey, -1);
                    }
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

        /* access modifiers changed from: protected */
        public ResourceTracker getResourceTracker() {
            return getUserRecord().mTunnelQuotaTracker;
        }

        public void invalidate() {
            getUserRecord().removeTunnelInterfaceRecord(this.mResourceId);
        }

        public String toString() {
            return "{super=" + super.toString() + ", mInterfaceName=" + this.mInterfaceName + ", mUnderlyingNetwork=" + this.mUnderlyingNetwork + ", mLocalAddress=" + this.mLocalAddress + ", mRemoteAddress=" + this.mRemoteAddress + ", mIkey=" + this.mIkey + ", mOkey=" + this.mOkey + "}";
        }
    }

    @VisibleForTesting
    public interface UidFdTagger {
        void tag(FileDescriptor fileDescriptor, int i) throws IOException;
    }

    @VisibleForTesting
    static final class UserRecord {
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

    @VisibleForTesting
    static final class UserResourceTracker {
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

    static {
        try {
            INADDR_ANY = InetAddress.getByAddress(new byte[]{0, 0, 0, 0});
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int reserveNetId() {
        synchronized (this.mTunnelNetIds) {
            int i = 0;
            while (i < 1024) {
                try {
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
                    i++;
                } catch (Throwable th) {
                    while (true) {
                        throw th;
                    }
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
            public void run() {
                synchronized (IpSecService.this) {
                    NetdService.get(5000);
                }
            }
        }.start();
    }

    /* Debug info: failed to restart local var, previous not found, register: 3 */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0017, code lost:
        return false;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    public synchronized boolean isNetdAlive() {
        INetd netd = this.mSrvConfig.getNetdInstance();
        if (netd == null) {
            return false;
        }
        return netd.isAlive();
    }

    private static void checkInetAddress(String inetAddress) {
        if (TextUtils.isEmpty(inetAddress)) {
            throw new IllegalArgumentException("Unspecified address");
        } else if (NetworkUtils.numericToInetAddress(inetAddress).isAnyLocalAddress()) {
            throw new IllegalArgumentException("Inappropriate wildcard address: " + inetAddress);
        }
    }

    private static void checkDirection(int direction) {
        switch (direction) {
            case 0:
            case 1:
                return;
            default:
                throw new IllegalArgumentException("Invalid Direction: " + direction);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:45:0x00a8 A[Catch:{ ServiceSpecificException -> 0x009e, RemoteException -> 0x0095 }] */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00b1 A[SYNTHETIC, Splitter:B:48:0x00b1] */
    public synchronized IpSecSpiResponse allocateSecurityParameterIndex(String destinationAddress, int requestedSpi, IBinder binder) throws RemoteException {
        int spi;
        int i = requestedSpi;
        IBinder iBinder = binder;
        synchronized (this) {
            checkInetAddress(destinationAddress);
            if (i > 0) {
                if (i < 256) {
                    throw new IllegalArgumentException("ESP SPI must not be in the range of 0-255.");
                }
            }
            Preconditions.checkNotNull(iBinder, "Null Binder passed to allocateSecurityParameterIndex");
            UserRecord userRecord = this.mUserResourceTracker.getUserRecord(Binder.getCallingUid());
            int i2 = this.mNextResourceId;
            this.mNextResourceId = i2 + 1;
            int resourceId = i2;
            try {
                if (!userRecord.mSpiQuotaTracker.isAvailable()) {
                    IpSecSpiResponse ipSecSpiResponse = new IpSecSpiResponse(1, -1, 0);
                    return ipSecSpiResponse;
                }
                String str = destinationAddress;
                try {
                    spi = this.mSrvConfig.getNetdInstance().ipSecAllocateSpi(resourceId, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, str, i);
                } catch (ServiceSpecificException e) {
                    e = e;
                    spi = 0;
                    if (e.errorCode != OsConstants.ENOENT) {
                    }
                } catch (RemoteException e2) {
                    e = e2;
                    throw e.rethrowFromSystemServer();
                }
                try {
                    Log.d(TAG, "Allocated SPI " + spi);
                    RefcountedResourceArray<SpiRecord> refcountedResourceArray = userRecord.mSpiRecords;
                    SpiRecord spiRecord = r1;
                    SpiRecord spiRecord2 = new SpiRecord(resourceId, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, str, spi);
                    RefcountedResource refcountedResource = new RefcountedResource(spiRecord, iBinder, new RefcountedResource[0]);
                    refcountedResourceArray.put(resourceId, refcountedResource);
                    IpSecSpiResponse ipSecSpiResponse2 = new IpSecSpiResponse(0, resourceId, spi);
                    return ipSecSpiResponse2;
                } catch (ServiceSpecificException e3) {
                    e = e3;
                } catch (RemoteException e4) {
                    e = e4;
                    throw e.rethrowFromSystemServer();
                }
            } catch (ServiceSpecificException e5) {
                e = e5;
                String str2 = destinationAddress;
                spi = 0;
                if (e.errorCode != OsConstants.ENOENT) {
                    IpSecSpiResponse ipSecSpiResponse3 = new IpSecSpiResponse(2, -1, spi);
                    return ipSecSpiResponse3;
                }
                throw e;
            } catch (RemoteException e6) {
                e = e6;
                String str3 = destinationAddress;
                throw e.rethrowFromSystemServer();
            }
        }
    }

    private void releaseResource(RefcountedResourceArray resArray, int resourceId) throws RemoteException {
        resArray.getRefcountedResourceOrThrow(resourceId).userRelease();
    }

    public synchronized void releaseSecurityParameterIndex(int resourceId) throws RemoteException {
        releaseResource(this.mUserResourceTracker.getUserRecord(Binder.getCallingUid()).mSpiRecords, resourceId);
    }

    private int bindToRandomPort(FileDescriptor sockFd) throws IOException {
        int i = 10;
        while (i > 0) {
            try {
                FileDescriptor probeSocket = Os.socket(OsConstants.AF_INET, OsConstants.SOCK_DGRAM, OsConstants.IPPROTO_UDP);
                Os.bind(probeSocket, INADDR_ANY, 0);
                int port = ((InetSocketAddress) Os.getsockname(probeSocket)).getPort();
                Os.close(probeSocket);
                Log.v(TAG, "Binding to port " + port);
                Os.bind(sockFd, INADDR_ANY, port);
                return port;
            } catch (ErrnoException e) {
                if (e.errno == OsConstants.EADDRINUSE) {
                    i--;
                } else {
                    throw e.rethrowAsIOException();
                }
            }
        }
        throw new IOException("Failed 10 attempts to bind to a port");
    }

    public synchronized IpSecUdpEncapResponse openUdpEncapsulationSocket(int port, IBinder binder) throws RemoteException {
        if (port == 0 || (port >= 1024 && port <= 65535)) {
            Preconditions.checkNotNull(binder, "Null Binder passed to openUdpEncapsulationSocket");
            int callingUid = Binder.getCallingUid();
            UserRecord userRecord = this.mUserResourceTracker.getUserRecord(callingUid);
            int resourceId = this.mNextResourceId;
            this.mNextResourceId = resourceId + 1;
            try {
                if (!userRecord.mSocketQuotaTracker.isAvailable()) {
                    return new IpSecUdpEncapResponse(1);
                }
                FileDescriptor sockFd = Os.socket(OsConstants.AF_INET, OsConstants.SOCK_DGRAM, OsConstants.IPPROTO_UDP);
                this.mUidFdTagger.tag(sockFd, callingUid);
                Os.setsockoptInt(sockFd, OsConstants.IPPROTO_UDP, OsConstants.UDP_ENCAP, OsConstants.UDP_ENCAP_ESPINUDP);
                this.mSrvConfig.getNetdInstance().ipSecSetEncapSocketOwner(sockFd, callingUid);
                if (port != 0) {
                    Log.v(TAG, "Binding to port " + port);
                    Os.bind(sockFd, INADDR_ANY, port);
                } else {
                    port = bindToRandomPort(sockFd);
                }
                userRecord.mEncapSocketRecords.put(resourceId, new RefcountedResource(new EncapSocketRecord(resourceId, sockFd, port), binder, new RefcountedResource[0]));
                return new IpSecUdpEncapResponse(0, resourceId, port, sockFd);
            } catch (ErrnoException | IOException e) {
                IoUtils.closeQuietly(null);
                return new IpSecUdpEncapResponse(1);
            }
        } else {
            throw new IllegalArgumentException("Specified port number must be a valid non-reserved UDP port");
        }
    }

    public synchronized void closeUdpEncapsulationSocket(int resourceId) throws RemoteException {
        releaseResource(this.mUserResourceTracker.getUserRecord(Binder.getCallingUid()).mEncapSocketRecords, resourceId);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0113, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0114, code lost:
        r1 = r6;
        r26 = r8;
        r27 = r14;
        r14 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0123, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0124, code lost:
        r1 = r6;
        r2 = r8;
        r27 = r14;
        r14 = r9;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0113 A[ExcHandler: Throwable (th java.lang.Throwable), Splitter:B:12:0x005d] */
    public synchronized IpSecTunnelInterfaceResponse createTunnelInterface(String localAddr, String remoteAddr, Network underlyingNetwork, IBinder binder, String callingPackage) {
        int ikey;
        int okey;
        int okey2;
        RefcountedResourceArray<TunnelInterfaceRecord> refcountedResourceArray;
        TunnelInterfaceRecord tunnelInterfaceRecord;
        Network network;
        TunnelInterfaceRecord tunnelInterfaceRecord2;
        RefcountedResource refcountedResource;
        IBinder iBinder = binder;
        synchronized (this) {
            enforceTunnelPermissions(callingPackage);
            Preconditions.checkNotNull(iBinder, "Null Binder passed to createTunnelInterface");
            Network network2 = underlyingNetwork;
            Preconditions.checkNotNull(network2, "No underlying network was specified");
            checkInetAddress(localAddr);
            checkInetAddress(remoteAddr);
            UserRecord userRecord = this.mUserResourceTracker.getUserRecord(Binder.getCallingUid());
            int i = 1;
            if (!userRecord.mTunnelQuotaTracker.isAvailable()) {
                IpSecTunnelInterfaceResponse ipSecTunnelInterfaceResponse = new IpSecTunnelInterfaceResponse(1);
                return ipSecTunnelInterfaceResponse;
            }
            int i2 = this.mNextResourceId;
            this.mNextResourceId = i2 + 1;
            int resourceId = i2;
            int ikey2 = reserveNetId();
            int okey3 = reserveNetId();
            String intfName = String.format("%s%d", new Object[]{INetd.IPSEC_INTERFACE_PREFIX, Integer.valueOf(resourceId)});
            try {
                String intfName2 = intfName;
                int i3 = 0;
                try {
                    this.mSrvConfig.getNetdInstance().addVirtualTunnelInterface(intfName, localAddr, remoteAddr, ikey2, okey3);
                    String[] strArr = WILDCARD_ADDRESSES;
                    int length = strArr.length;
                    int i4 = 0;
                    while (i4 < length) {
                        try {
                            String wildcardAddr = strArr[i4];
                            int[] iArr = DIRECTIONS;
                            int length2 = iArr.length;
                            int i5 = i3;
                            while (i5 < length2) {
                                int direction = iArr[i5];
                                this.mSrvConfig.getNetdInstance().ipSecAddSecurityPolicy(0, direction, wildcardAddr, wildcardAddr, 0, direction == i ? okey3 : ikey2, -1);
                                i5++;
                                i = 1;
                            }
                            i4++;
                            i3 = 0;
                            i = 1;
                        } catch (RemoteException e) {
                            e = e;
                            okey = okey3;
                            UserRecord userRecord2 = userRecord;
                            String str = intfName2;
                            ikey = ikey2;
                            releaseNetId(ikey);
                            releaseNetId(okey);
                            throw e.rethrowFromSystemServer();
                        } catch (Throwable th) {
                            t = th;
                            okey2 = okey3;
                            UserRecord userRecord3 = userRecord;
                            String str2 = intfName2;
                            ikey = ikey2;
                            releaseNetId(ikey);
                            releaseNetId(okey2);
                            throw t;
                        }
                    }
                    refcountedResourceArray = userRecord.mTunnelInterfaceRecords;
                    tunnelInterfaceRecord = tunnelInterfaceRecord;
                    network = network2;
                    tunnelInterfaceRecord2 = tunnelInterfaceRecord;
                    refcountedResource = refcountedResource;
                    okey2 = okey3;
                    UserRecord userRecord4 = userRecord;
                    ikey = ikey2;
                } catch (RemoteException e2) {
                    e = e2;
                    UserRecord userRecord5 = userRecord;
                    String str3 = intfName2;
                    ikey = ikey2;
                    okey = okey3;
                    releaseNetId(ikey);
                    releaseNetId(okey);
                    throw e.rethrowFromSystemServer();
                } catch (Throwable th2) {
                    t = th2;
                    okey2 = okey3;
                    UserRecord userRecord6 = userRecord;
                    String str4 = intfName2;
                    ikey = ikey2;
                    releaseNetId(ikey);
                    releaseNetId(okey2);
                    throw t;
                }
                try {
                    tunnelInterfaceRecord = new TunnelInterfaceRecord(resourceId, intfName2, network, localAddr, remoteAddr, ikey2, okey2);
                    refcountedResource = new RefcountedResource(tunnelInterfaceRecord2, iBinder, new RefcountedResource[0]);
                    refcountedResourceArray.put(resourceId, refcountedResource);
                    try {
                        IpSecTunnelInterfaceResponse ipSecTunnelInterfaceResponse2 = new IpSecTunnelInterfaceResponse(0, resourceId, intfName2);
                        return ipSecTunnelInterfaceResponse2;
                    } catch (RemoteException e3) {
                        e = e3;
                        okey = okey2;
                        releaseNetId(ikey);
                        releaseNetId(okey);
                        throw e.rethrowFromSystemServer();
                    } catch (Throwable th3) {
                        t = th3;
                        releaseNetId(ikey);
                        releaseNetId(okey2);
                        throw t;
                    }
                } catch (RemoteException e4) {
                    e = e4;
                    String str5 = intfName2;
                    okey = okey2;
                    releaseNetId(ikey);
                    releaseNetId(okey);
                    throw e.rethrowFromSystemServer();
                } catch (Throwable th4) {
                    t = th4;
                    String str6 = intfName2;
                    releaseNetId(ikey);
                    releaseNetId(okey2);
                    throw t;
                }
            } catch (RemoteException e5) {
                e = e5;
                String str7 = intfName;
                UserRecord userRecord7 = userRecord;
                ikey = ikey2;
                okey = okey3;
                releaseNetId(ikey);
                releaseNetId(okey);
                throw e.rethrowFromSystemServer();
            } catch (Throwable th5) {
            }
        }
    }

    public synchronized void addAddressToTunnelInterface(int tunnelResourceId, LinkAddress localAddr, String callingPackage) {
        enforceTunnelPermissions(callingPackage);
        try {
            this.mSrvConfig.getNetdInstance().interfaceAddAddress(this.mUserResourceTracker.getUserRecord(Binder.getCallingUid()).mTunnelInterfaceRecords.getResourceOrThrow(tunnelResourceId).mInterfaceName, localAddr.getAddress().getHostAddress(), localAddr.getPrefixLength());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public synchronized void removeAddressFromTunnelInterface(int tunnelResourceId, LinkAddress localAddr, String callingPackage) {
        enforceTunnelPermissions(callingPackage);
        try {
            this.mSrvConfig.getNetdInstance().interfaceDelAddress(this.mUserResourceTracker.getUserRecord(Binder.getCallingUid()).mTunnelInterfaceRecords.getResourceOrThrow(tunnelResourceId).mInterfaceName, localAddr.getAddress().getHostAddress(), localAddr.getPrefixLength());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public synchronized void deleteTunnelInterface(int resourceId, String callingPackage) throws RemoteException {
        enforceTunnelPermissions(callingPackage);
        releaseResource(this.mUserResourceTracker.getUserRecord(Binder.getCallingUid()).mTunnelInterfaceRecords, resourceId);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void validateAlgorithms(IpSecConfig config) throws IllegalArgumentException {
        IpSecAlgorithm auth = config.getAuthentication();
        IpSecAlgorithm crypt = config.getEncryption();
        IpSecAlgorithm aead = config.getAuthenticatedEncryption();
        boolean z = true;
        Preconditions.checkArgument((aead == null && crypt == null && auth == null) ? false : true, "No Encryption or Authentication algorithms specified");
        Preconditions.checkArgument(auth == null || auth.isAuthentication(), "Unsupported algorithm for Authentication");
        Preconditions.checkArgument(crypt == null || crypt.isEncryption(), "Unsupported algorithm for Encryption");
        Preconditions.checkArgument(aead == null || aead.isAead(), "Unsupported algorithm for Authenticated Encryption");
        if (!(aead == null || (auth == null && crypt == null))) {
            z = false;
        }
        Preconditions.checkArgument(z, "Authenticated Encryption is mutually exclusive with other Authentication or Encryption algorithms");
    }

    private void checkIpSecConfig(IpSecConfig config) {
        UserRecord userRecord = this.mUserResourceTracker.getUserRecord(Binder.getCallingUid());
        switch (config.getEncapType()) {
            case 0:
                break;
            case 1:
            case 2:
                userRecord.mEncapSocketRecords.getResourceOrThrow(config.getEncapSocketResourceId());
                int port = config.getEncapRemotePort();
                if (port <= 0 || port > 65535) {
                    throw new IllegalArgumentException("Invalid remote UDP port: " + port);
                }
            default:
                throw new IllegalArgumentException("Invalid Encap Type: " + config.getEncapType());
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
                switch (config.getMode()) {
                    case 0:
                    case 1:
                        return;
                    default:
                        throw new IllegalArgumentException("Invalid IpSecTransform.mode: " + config.getMode());
                }
            } else {
                throw new IllegalArgumentException("Mismatched remote addresseses.");
            }
        } else {
            throw new IllegalStateException("SPI already in use; cannot be used in new Transforms");
        }
    }

    private void enforceTunnelPermissions(String callingPackage) {
        Preconditions.checkNotNull(callingPackage, "Null calling package cannot create IpSec tunnels");
        int noteOp = getAppOpsManager().noteOp(75, Binder.getCallingUid(), callingPackage);
        if (noteOp == 0) {
            return;
        }
        if (noteOp == 3) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_IPSEC_TUNNELS", TAG);
            return;
        }
        throw new SecurityException("Request to ignore AppOps for non-legacy API");
    }

    private void createOrUpdateTransform(IpSecConfig c, int resourceId, SpiRecord spiRecord, EncapSocketRecord socketRecord) throws RemoteException {
        String cryptName;
        int i;
        int i2;
        int i3;
        int encapType = c.getEncapType();
        int encapLocalPort = 0;
        int encapRemotePort = 0;
        if (encapType != 0) {
            encapLocalPort = socketRecord.getPort();
            encapRemotePort = c.getEncapRemotePort();
        }
        int encapLocalPort2 = encapLocalPort;
        int encapRemotePort2 = encapRemotePort;
        IpSecAlgorithm auth = c.getAuthentication();
        IpSecAlgorithm crypt = c.getEncryption();
        IpSecAlgorithm authCrypt = c.getAuthenticatedEncryption();
        if (crypt == null) {
            cryptName = authCrypt == null ? "ecb(cipher_null)" : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        } else {
            cryptName = crypt.getName();
        }
        String cryptName2 = cryptName;
        INetd netdInstance = this.mSrvConfig.getNetdInstance();
        int mode = c.getMode();
        String sourceAddress = c.getSourceAddress();
        String destinationAddress = c.getDestinationAddress();
        if (c.getNetwork() != null) {
            i = c.getNetwork().netId;
        } else {
            i = 0;
        }
        int spi = spiRecord.getSpi();
        int markValue = c.getMarkValue();
        int markMask = c.getMarkMask();
        String name = auth != null ? auth.getName() : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        byte[] key = auth != null ? auth.getKey() : new byte[0];
        if (auth != null) {
            i2 = auth.getTruncationLengthBits();
        } else {
            i2 = 0;
        }
        byte[] key2 = crypt != null ? crypt.getKey() : new byte[0];
        if (crypt != null) {
            i3 = crypt.getTruncationLengthBits();
        } else {
            i3 = 0;
        }
        IpSecAlgorithm ipSecAlgorithm = authCrypt;
        IpSecAlgorithm ipSecAlgorithm2 = crypt;
        IpSecAlgorithm ipSecAlgorithm3 = auth;
        netdInstance.ipSecAddSecurityAssociation(resourceId, mode, sourceAddress, destinationAddress, i, spi, markValue, markMask, name, key, i2, cryptName2, key2, i3, authCrypt != null ? authCrypt.getName() : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, authCrypt != null ? authCrypt.getKey() : new byte[0], authCrypt != null ? authCrypt.getTruncationLengthBits() : 0, encapType, encapLocalPort2, encapRemotePort2);
    }

    public synchronized IpSecTransformResponse createTransform(IpSecConfig c, IBinder binder, String callingPackage) throws RemoteException {
        IBinder iBinder = binder;
        synchronized (this) {
            Preconditions.checkNotNull(c);
            if (c.getMode() == 1) {
                enforceTunnelPermissions(callingPackage);
            } else {
                String str = callingPackage;
            }
            checkIpSecConfig(c);
            Preconditions.checkNotNull(iBinder, "Null Binder passed to createTransform");
            int i = this.mNextResourceId;
            this.mNextResourceId = i + 1;
            int resourceId = i;
            UserRecord userRecord = this.mUserResourceTracker.getUserRecord(Binder.getCallingUid());
            List<RefcountedResource> dependencies = new ArrayList<>();
            if (!userRecord.mTransformQuotaTracker.isAvailable()) {
                IpSecTransformResponse ipSecTransformResponse = new IpSecTransformResponse(1);
                return ipSecTransformResponse;
            }
            EncapSocketRecord socketRecord = null;
            if (c.getEncapType() != 0) {
                RefcountedResource<EncapSocketRecord> refcountedSocketRecord = userRecord.mEncapSocketRecords.getRefcountedResourceOrThrow(c.getEncapSocketResourceId());
                dependencies.add(refcountedSocketRecord);
                socketRecord = refcountedSocketRecord.getResource();
            }
            EncapSocketRecord socketRecord2 = socketRecord;
            RefcountedResource<SpiRecord> refcountedSpiRecord = userRecord.mSpiRecords.getRefcountedResourceOrThrow(c.getSpiResourceId());
            dependencies.add(refcountedSpiRecord);
            SpiRecord spiRecord = refcountedSpiRecord.getResource();
            IpSecConfig ipSecConfig = c;
            createOrUpdateTransform(ipSecConfig, resourceId, spiRecord, socketRecord2);
            TransformRecord transformRecord = r1;
            UserRecord userRecord2 = userRecord;
            RefcountedResource<SpiRecord> refcountedResource = refcountedSpiRecord;
            RefcountedResourceArray<TransformRecord> refcountedResourceArray = userRecord.mTransformRecords;
            TransformRecord transformRecord2 = new TransformRecord(resourceId, ipSecConfig, spiRecord, socketRecord2);
            RefcountedResource refcountedResource2 = new RefcountedResource(transformRecord, iBinder, (RefcountedResource[]) dependencies.toArray(new RefcountedResource[dependencies.size()]));
            refcountedResourceArray.put(resourceId, refcountedResource2);
            IpSecTransformResponse ipSecTransformResponse2 = new IpSecTransformResponse(0, resourceId);
            return ipSecTransformResponse2;
        }
    }

    public synchronized void deleteTransform(int resourceId) throws RemoteException {
        releaseResource(this.mUserResourceTracker.getUserRecord(Binder.getCallingUid()).mTransformRecords, resourceId);
    }

    public synchronized void applyTransportModeTransform(ParcelFileDescriptor socket, int direction, int resourceId) throws RemoteException {
        UserRecord userRecord = this.mUserResourceTracker.getUserRecord(Binder.getCallingUid());
        checkDirection(direction);
        TransformRecord info = userRecord.mTransformRecords.getResourceOrThrow(resourceId);
        if (info.pid == getCallingPid() && info.uid == getCallingUid()) {
            IpSecConfig c = info.getConfig();
            Preconditions.checkArgument(c.getMode() == 0, "Transform mode was not Transport mode; cannot be applied to a socket");
            this.mSrvConfig.getNetdInstance().ipSecApplyTransportModeTransform(socket.getFileDescriptor(), resourceId, direction, c.getSourceAddress(), c.getDestinationAddress(), info.getSpiRecord().getSpi());
        } else {
            throw new SecurityException("Only the owner of an IpSec Transform may apply it!");
        }
    }

    public synchronized void removeTransportModeTransforms(ParcelFileDescriptor socket) throws RemoteException {
        this.mSrvConfig.getNetdInstance().ipSecRemoveTransportModeTransform(socket.getFileDescriptor());
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x00ff  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0109  */
    public synchronized void applyTunnelModeTransform(int tunnelResourceId, int direction, int transformResourceId, String callingPackage) throws RemoteException {
        int i;
        int mark = direction;
        int i2 = transformResourceId;
        synchronized (this) {
            enforceTunnelPermissions(callingPackage);
            checkDirection(direction);
            UserRecord userRecord = this.mUserResourceTracker.getUserRecord(Binder.getCallingUid());
            TransformRecord transformInfo = userRecord.mTransformRecords.getResourceOrThrow(i2);
            TunnelInterfaceRecord tunnelInterfaceInfo = userRecord.mTunnelInterfaceRecords.getResourceOrThrow(tunnelResourceId);
            IpSecConfig c = transformInfo.getConfig();
            int i3 = 0;
            Preconditions.checkArgument(c.getMode() == 1, "Transform mode was not Tunnel mode; cannot be applied to a tunnel interface");
            EncapSocketRecord socketRecord = null;
            if (c.getEncapType() != 0) {
                socketRecord = userRecord.mEncapSocketRecords.getResourceOrThrow(c.getEncapSocketResourceId());
            }
            EncapSocketRecord socketRecord2 = socketRecord;
            SpiRecord spiRecord = userRecord.mSpiRecords.getResourceOrThrow(c.getSpiResourceId());
            if (mark == 0) {
                i = tunnelInterfaceInfo.getIkey();
            } else {
                i = tunnelInterfaceInfo.getOkey();
            }
            int mark2 = i;
            try {
                c.setMarkValue(mark2);
                c.setMarkMask(-1);
                if (mark == 1) {
                    try {
                        c.setNetwork(tunnelInterfaceInfo.getUnderlyingNetwork());
                        String[] strArr = WILDCARD_ADDRESSES;
                        int length = strArr.length;
                        while (i3 < length) {
                            String wildcardAddr = strArr[i3];
                            String[] strArr2 = strArr;
                            int i4 = i3;
                            int i5 = length;
                            int mark3 = mark2;
                            SpiRecord spiRecord2 = spiRecord;
                            EncapSocketRecord socketRecord3 = socketRecord2;
                            IpSecConfig c2 = c;
                            TunnelInterfaceRecord tunnelInterfaceInfo2 = tunnelInterfaceInfo;
                            try {
                                this.mSrvConfig.getNetdInstance().ipSecUpdateSecurityPolicy(0, mark, wildcardAddr, wildcardAddr, transformInfo.getSpiRecord().getSpi(), mark3, -1);
                                i3 = i4 + 1;
                                length = i5;
                                strArr = strArr2;
                                mark2 = mark3;
                                tunnelInterfaceInfo = tunnelInterfaceInfo2;
                                spiRecord = spiRecord2;
                                socketRecord2 = socketRecord3;
                                c = c2;
                            } catch (ServiceSpecificException e) {
                                e = e;
                                SpiRecord spiRecord3 = spiRecord2;
                                EncapSocketRecord encapSocketRecord = socketRecord3;
                                IpSecConfig ipSecConfig = c2;
                                if (e.errorCode == OsConstants.EINVAL) {
                                    throw new IllegalArgumentException(e.toString());
                                }
                                throw e;
                            }
                        }
                    } catch (ServiceSpecificException e2) {
                        e = e2;
                        int i6 = mark2;
                        TunnelInterfaceRecord tunnelInterfaceRecord = tunnelInterfaceInfo;
                        SpiRecord spiRecord4 = spiRecord;
                        EncapSocketRecord encapSocketRecord2 = socketRecord2;
                        IpSecConfig ipSecConfig2 = c;
                        if (e.errorCode == OsConstants.EINVAL) {
                        }
                    }
                }
                TunnelInterfaceRecord tunnelInterfaceRecord2 = tunnelInterfaceInfo;
                try {
                    createOrUpdateTransform(c, i2, spiRecord, socketRecord2);
                } catch (ServiceSpecificException e3) {
                    e = e3;
                }
            } catch (ServiceSpecificException e4) {
                e = e4;
                int i7 = mark2;
                SpiRecord spiRecord5 = spiRecord;
                EncapSocketRecord encapSocketRecord3 = socketRecord2;
                IpSecConfig ipSecConfig3 = c;
                TunnelInterfaceRecord tunnelInterfaceRecord3 = tunnelInterfaceInfo;
                if (e.errorCode == OsConstants.EINVAL) {
                }
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
