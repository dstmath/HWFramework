package com.android.server;

import android.content.Context;
import android.net.IIpSecService.Stub;
import android.net.INetd;
import android.net.IpSecAlgorithm;
import android.net.IpSecConfig;
import android.net.util.NetdService;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

public class IpSecService extends Stub {
    private static final boolean DBG = Log.isLoggable(TAG, 3);
    private static final int[] DIRECTIONS = new int[]{1, 0};
    private static final int NETD_FETCH_TIMEOUT = 5000;
    private static final String NETD_SERVICE_NAME = "netd";
    private static final String TAG = "IpSecService";
    private final Context mContext;
    private Object mLock = new Object();
    private AtomicInteger mNextResourceId = new AtomicInteger(16441040);
    @GuardedBy("mSpiRecords")
    private final SparseArray<SpiRecord> mSpiRecords = new SparseArray();
    @GuardedBy("mTransformRecords")
    private final SparseArray<TransformRecord> mTransformRecords = new SparseArray();

    private abstract class ManagedResource implements DeathRecipient {
        private IBinder mBinder;
        final int pid = Binder.getCallingPid();
        final int uid = Binder.getCallingUid();

        protected abstract void nullifyRecord();

        protected abstract void releaseResources();

        ManagedResource(IBinder binder) {
            this.mBinder = binder;
            try {
                this.mBinder.linkToDeath(this, 0);
            } catch (RemoteException e) {
                binderDied();
            }
        }

        public final void release() {
            releaseResources();
            if (this.mBinder != null) {
                this.mBinder.unlinkToDeath(this, 0);
            }
            this.mBinder = null;
            nullifyRecord();
        }

        public final void binderDied() {
            release();
        }
    }

    private final class SpiRecord extends ManagedResource {
        private final IBinder mBinder;
        private final int mDirection;
        private final String mLocalAddress;
        private final String mRemoteAddress;
        private int mResourceId;
        private int mSpi;

        SpiRecord(int resourceId, int direction, String localAddress, String remoteAddress, int spi, IBinder binder) {
            super(binder);
            this.mResourceId = resourceId;
            this.mDirection = direction;
            this.mLocalAddress = localAddress;
            this.mRemoteAddress = remoteAddress;
            this.mSpi = spi;
            this.mBinder = binder;
        }

        protected void releaseResources() {
            try {
                IpSecService.this.getNetdInstance().ipSecDeleteSecurityAssociation(this.mResourceId, this.mDirection, this.mLocalAddress, this.mRemoteAddress, this.mSpi);
            } catch (ServiceSpecificException e) {
            } catch (RemoteException e2) {
                Log.e(IpSecService.TAG, "Failed to delete SPI reservation with ID: " + this.mResourceId);
            }
        }

        protected void nullifyRecord() {
            this.mSpi = 0;
            this.mResourceId = 0;
        }
    }

    private final class TransformRecord extends ManagedResource {
        private IpSecConfig mConfig;
        private int mResourceId;

        TransformRecord(IpSecConfig config, int resourceId, IBinder binder) {
            super(binder);
            this.mConfig = config;
            this.mResourceId = resourceId;
        }

        public IpSecConfig getConfig() {
            return this.mConfig;
        }

        protected void releaseResources() {
            for (int direction : IpSecService.DIRECTIONS) {
                try {
                    String hostAddress;
                    String hostAddress2;
                    INetd netdInstance = IpSecService.this.getNetdInstance();
                    int i = this.mResourceId;
                    if (this.mConfig.getLocalAddress() != null) {
                        hostAddress = this.mConfig.getLocalAddress().getHostAddress();
                    } else {
                        hostAddress = "";
                    }
                    if (this.mConfig.getRemoteAddress() != null) {
                        hostAddress2 = this.mConfig.getRemoteAddress().getHostAddress();
                    } else {
                        hostAddress2 = "";
                    }
                    netdInstance.ipSecDeleteSecurityAssociation(i, direction, hostAddress, hostAddress2, this.mConfig.getSpi(direction));
                } catch (ServiceSpecificException e) {
                } catch (RemoteException e2) {
                    Log.e(IpSecService.TAG, "Failed to delete SA with ID: " + this.mResourceId);
                }
            }
        }

        protected void nullifyRecord() {
            this.mConfig = null;
            this.mResourceId = 0;
        }
    }

    private IpSecService(Context context) {
        this.mContext = context;
    }

    static IpSecService create(Context context) throws InterruptedException {
        IpSecService service = new IpSecService(context);
        service.connectNativeNetdService();
        return service;
    }

    public void systemReady() {
        if (isNetdAlive()) {
            Slog.d(TAG, "IpSecService is ready");
        } else {
            Slog.wtf(TAG, "IpSecService not ready: failed to connect to NetD Native Service!");
        }
    }

    private void connectNativeNetdService() {
        new Thread(new Runnable() {
            public void run() {
                synchronized (IpSecService.this.mLock) {
                    NetdService.get(5000);
                }
            }
        }).run();
    }

    INetd getNetdInstance() throws RemoteException {
        INetd netd = NetdService.getInstance();
        if (netd != null) {
            return netd;
        }
        throw new RemoteException("Failed to Get Netd Instance");
    }

    boolean isNetdAlive() {
        boolean z = false;
        synchronized (this.mLock) {
            try {
                INetd netd = getNetdInstance();
                if (netd == null) {
                    return z;
                }
                z = netd.isAlive();
                return z;
            } catch (RemoteException e) {
                return z;
            }
        }
    }

    public Bundle reserveSecurityParameterIndex(int direction, String remoteAddress, int requestedSpi, IBinder binder) throws RemoteException {
        int resourceId = this.mNextResourceId.getAndIncrement();
        int spi = 0;
        String localAddress = "";
        Bundle retBundle = new Bundle(3);
        try {
            spi = getNetdInstance().ipSecAllocateSpi(resourceId, direction, localAddress, remoteAddress, requestedSpi);
            Log.d(TAG, "Allocated SPI " + spi);
            retBundle.putInt("status", 0);
            retBundle.putInt("resourceId", resourceId);
            retBundle.putInt("spi", spi);
            synchronized (this.mSpiRecords) {
                this.mSpiRecords.put(resourceId, new SpiRecord(resourceId, direction, localAddress, remoteAddress, spi, binder));
            }
        } catch (ServiceSpecificException e) {
            retBundle.putInt("status", 2);
            retBundle.putInt("resourceId", resourceId);
            retBundle.putInt("spi", spi);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
        return retBundle;
    }

    public void releaseSecurityParameterIndex(int resourceId) throws RemoteException {
    }

    public Bundle openUdpEncapsulationSocket(int port, IBinder binder) throws RemoteException {
        return null;
    }

    public void closeUdpEncapsulationSocket(ParcelFileDescriptor socket) {
    }

    public Bundle createTransportModeTransform(IpSecConfig c, IBinder binder) throws RemoteException {
        int resourceId = this.mNextResourceId.getAndIncrement();
        int[] iArr = DIRECTIONS;
        int i = 0;
        int length = iArr.length;
        while (true) {
            int i2 = i;
            Bundle bundle;
            if (i2 < length) {
                int direction = iArr[i2];
                IpSecAlgorithm auth = c.getAuthentication(direction);
                IpSecAlgorithm crypt = c.getEncryption(direction);
                try {
                    String hostAddress;
                    String hostAddress2;
                    long networkHandle;
                    INetd netdInstance = getNetdInstance();
                    int mode = c.getMode();
                    if (c.getLocalAddress() != null) {
                        hostAddress = c.getLocalAddress().getHostAddress();
                    } else {
                        hostAddress = "";
                    }
                    if (c.getRemoteAddress() != null) {
                        hostAddress2 = c.getRemoteAddress().getHostAddress();
                    } else {
                        hostAddress2 = "";
                    }
                    if (c.getNetwork() != null) {
                        networkHandle = c.getNetwork().getNetworkHandle();
                    } else {
                        networkHandle = 0;
                    }
                    if (netdInstance.ipSecAddSecurityAssociation(resourceId, mode, direction, hostAddress, hostAddress2, networkHandle, c.getSpi(direction), auth != null ? auth.getName() : "", auth != null ? auth.getKey() : null, auth != null ? auth.getTruncationLengthBits() : 0, crypt != null ? crypt.getName() : "", crypt != null ? crypt.getKey() : null, crypt != null ? crypt.getTruncationLengthBits() : 0, c.getEncapType(), c.getEncapLocalPort(), c.getEncapRemotePort()) != c.getSpi(direction)) {
                        bundle = new Bundle(2);
                        bundle.putInt("status", 2);
                        bundle.putInt("resourceId", 0);
                        return bundle;
                    }
                    i = i2 + 1;
                } catch (ServiceSpecificException e) {
                }
            } else {
                synchronized (this.mTransformRecords) {
                    this.mTransformRecords.put(resourceId, new TransformRecord(c, resourceId, binder));
                }
                bundle = new Bundle(2);
                bundle.putInt("status", 0);
                bundle.putInt("resourceId", resourceId);
                return bundle;
            }
        }
    }

    public void deleteTransportModeTransform(int resourceId) throws RemoteException {
        synchronized (this.mTransformRecords) {
            TransformRecord record = (TransformRecord) this.mTransformRecords.get(resourceId);
            if (record == null) {
                throw new IllegalArgumentException("Transform " + resourceId + " is not available to be deleted");
            } else if (record.pid == Binder.getCallingPid() && record.uid == Binder.getCallingUid()) {
                record.releaseResources();
                this.mTransformRecords.remove(resourceId);
                record.nullifyRecord();
            } else {
                throw new SecurityException("Only the owner of an IpSec Transform may delete it!");
            }
        }
    }

    public void applyTransportModeTransform(ParcelFileDescriptor socket, int resourceId) throws RemoteException {
        synchronized (this.mTransformRecords) {
            TransformRecord info = (TransformRecord) this.mTransformRecords.get(resourceId);
            if (info == null) {
                throw new IllegalArgumentException("Transform " + resourceId + " is not active");
            } else if (info.pid == getCallingPid() && info.uid == getCallingUid()) {
                IpSecConfig c = info.getConfig();
                try {
                    for (int direction : DIRECTIONS) {
                        String hostAddress;
                        String hostAddress2;
                        INetd netdInstance = getNetdInstance();
                        FileDescriptor fileDescriptor = socket.getFileDescriptor();
                        if (c.getLocalAddress() != null) {
                            hostAddress = c.getLocalAddress().getHostAddress();
                        } else {
                            hostAddress = "";
                        }
                        if (c.getRemoteAddress() != null) {
                            hostAddress2 = c.getRemoteAddress().getHostAddress();
                        } else {
                            hostAddress2 = "";
                        }
                        netdInstance.ipSecApplyTransportModeTransform(fileDescriptor, resourceId, direction, hostAddress, hostAddress2, c.getSpi(direction));
                    }
                } catch (ServiceSpecificException e) {
                }
            } else {
                throw new SecurityException("Only the owner of an IpSec Transform may apply it!");
            }
        }
    }

    public void removeTransportModeTransform(ParcelFileDescriptor socket, int resourceId) throws RemoteException {
        try {
            getNetdInstance().ipSecRemoveTransportModeTransform(socket.getFileDescriptor());
        } catch (ServiceSpecificException e) {
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        pw.println("IpSecService Log:");
        pw.println("NetdNativeService Connection: " + (isNetdAlive() ? "alive" : "dead"));
        pw.println();
    }
}
