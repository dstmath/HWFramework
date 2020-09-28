package android.net;

import android.annotation.SystemApi;
import android.content.Context;
import android.os.Binder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.util.AndroidException;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public final class IpSecManager {
    public static final int DIRECTION_IN = 0;
    public static final int DIRECTION_OUT = 1;
    public static final int INVALID_RESOURCE_ID = -1;
    public static final int INVALID_SECURITY_PARAMETER_INDEX = 0;
    private static final String TAG = "IpSecManager";
    private final Context mContext;
    private final IIpSecService mService;

    @Retention(RetentionPolicy.SOURCE)
    public @interface PolicyDirection {
    }

    public interface Status {
        public static final int OK = 0;
        public static final int RESOURCE_UNAVAILABLE = 1;
        public static final int SPI_UNAVAILABLE = 2;
    }

    public static final class SpiUnavailableException extends AndroidException {
        private final int mSpi;

        SpiUnavailableException(String msg, int spi) {
            super(msg + " (spi: " + spi + ")");
            this.mSpi = spi;
        }

        public int getSpi() {
            return this.mSpi;
        }
    }

    public static final class ResourceUnavailableException extends AndroidException {
        ResourceUnavailableException(String msg) {
            super(msg);
        }
    }

    public static final class SecurityParameterIndex implements AutoCloseable {
        private final CloseGuard mCloseGuard;
        private final InetAddress mDestinationAddress;
        private int mResourceId;
        private final IIpSecService mService;
        private int mSpi;

        public int getSpi() {
            return this.mSpi;
        }

        @Override // java.lang.AutoCloseable
        public void close() {
            try {
                this.mService.releaseSecurityParameterIndex(this.mResourceId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Exception e2) {
                Log.e(IpSecManager.TAG, "Failed to close " + this + ", Exception=" + e2);
            } catch (Throwable th) {
                this.mResourceId = -1;
                this.mCloseGuard.close();
                throw th;
            }
            this.mResourceId = -1;
            this.mCloseGuard.close();
        }

        /* access modifiers changed from: protected */
        @Override // java.lang.Object
        public void finalize() throws Throwable {
            CloseGuard closeGuard = this.mCloseGuard;
            if (closeGuard != null) {
                closeGuard.warnIfOpen();
            }
            close();
        }

        private SecurityParameterIndex(IIpSecService service, InetAddress destinationAddress, int spi) throws ResourceUnavailableException, SpiUnavailableException {
            this.mCloseGuard = CloseGuard.get();
            this.mSpi = 0;
            this.mResourceId = -1;
            this.mService = service;
            this.mDestinationAddress = destinationAddress;
            try {
                IpSecSpiResponse result = this.mService.allocateSecurityParameterIndex(destinationAddress.getHostAddress(), spi, new Binder());
                if (result != null) {
                    int status = result.status;
                    if (status == 0) {
                        this.mSpi = result.spi;
                        this.mResourceId = result.resourceId;
                        if (this.mSpi == 0) {
                            throw new RuntimeException("Invalid SPI returned by IpSecService: " + status);
                        } else if (this.mResourceId != -1) {
                            this.mCloseGuard.open("open");
                        } else {
                            throw new RuntimeException("Invalid Resource ID returned by IpSecService: " + status);
                        }
                    } else if (status == 1) {
                        throw new ResourceUnavailableException("No more SPIs may be allocated by this requester.");
                    } else if (status != 2) {
                        throw new RuntimeException("Unknown status returned by IpSecService: " + status);
                    } else {
                        throw new SpiUnavailableException("Requested SPI is unavailable", spi);
                    }
                } else {
                    throw new NullPointerException("Received null response from IpSecService");
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        @VisibleForTesting
        public int getResourceId() {
            return this.mResourceId;
        }

        public String toString() {
            return "SecurityParameterIndex{spi=" + this.mSpi + ",resourceId=" + this.mResourceId + "}";
        }
    }

    public SecurityParameterIndex allocateSecurityParameterIndex(InetAddress destinationAddress) throws ResourceUnavailableException {
        try {
            return new SecurityParameterIndex(this.mService, destinationAddress, 0);
        } catch (ServiceSpecificException e) {
            throw rethrowUncheckedExceptionFromServiceSpecificException(e);
        } catch (SpiUnavailableException e2) {
            throw new ResourceUnavailableException("No SPIs available");
        }
    }

    public SecurityParameterIndex allocateSecurityParameterIndex(InetAddress destinationAddress, int requestedSpi) throws SpiUnavailableException, ResourceUnavailableException {
        if (requestedSpi != 0) {
            try {
                return new SecurityParameterIndex(this.mService, destinationAddress, requestedSpi);
            } catch (ServiceSpecificException e) {
                throw rethrowUncheckedExceptionFromServiceSpecificException(e);
            }
        } else {
            throw new IllegalArgumentException("Requested SPI must be a valid (non-zero) SPI");
        }
    }

    public void applyTransportModeTransform(Socket socket, int direction, IpSecTransform transform) throws IOException {
        socket.getSoLinger();
        applyTransportModeTransform(socket.getFileDescriptor$(), direction, transform);
    }

    public void applyTransportModeTransform(DatagramSocket socket, int direction, IpSecTransform transform) throws IOException {
        applyTransportModeTransform(socket.getFileDescriptor$(), direction, transform);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0017, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0018, code lost:
        if (r0 != null) goto L_0x001a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001a, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        throw r2;
     */
    public void applyTransportModeTransform(FileDescriptor socket, int direction, IpSecTransform transform) throws IOException {
        try {
            ParcelFileDescriptor pfd = ParcelFileDescriptor.dup(socket);
            this.mService.applyTransportModeTransform(pfd, direction, transform.getResourceId());
            if (pfd != null) {
                $closeResource(null, pfd);
            }
        } catch (ServiceSpecificException e) {
            throw rethrowCheckedExceptionFromServiceSpecificException(e);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    public void removeTransportModeTransforms(Socket socket) throws IOException {
        socket.getSoLinger();
        removeTransportModeTransforms(socket.getFileDescriptor$());
    }

    public void removeTransportModeTransforms(DatagramSocket socket) throws IOException {
        removeTransportModeTransforms(socket.getFileDescriptor$());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0013, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0014, code lost:
        if (r0 != null) goto L_0x0016;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0016, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0019, code lost:
        throw r2;
     */
    public void removeTransportModeTransforms(FileDescriptor socket) throws IOException {
        try {
            ParcelFileDescriptor pfd = ParcelFileDescriptor.dup(socket);
            this.mService.removeTransportModeTransforms(pfd);
            if (pfd != null) {
                $closeResource(null, pfd);
            }
        } catch (ServiceSpecificException e) {
            throw rethrowCheckedExceptionFromServiceSpecificException(e);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    public void removeTunnelModeTransform(Network net, IpSecTransform transform) {
    }

    public static final class UdpEncapsulationSocket implements AutoCloseable {
        private final CloseGuard mCloseGuard;
        private final ParcelFileDescriptor mPfd;
        private final int mPort;
        private int mResourceId;
        private final IIpSecService mService;

        private UdpEncapsulationSocket(IIpSecService service, int port) throws ResourceUnavailableException, IOException {
            this.mResourceId = -1;
            this.mCloseGuard = CloseGuard.get();
            this.mService = service;
            try {
                IpSecUdpEncapResponse result = this.mService.openUdpEncapsulationSocket(port, new Binder());
                int i = result.status;
                if (i == 0) {
                    this.mResourceId = result.resourceId;
                    this.mPort = result.port;
                    this.mPfd = result.fileDescriptor;
                    this.mCloseGuard.open("constructor");
                } else if (i != 1) {
                    throw new RuntimeException("Unknown status returned by IpSecService: " + result.status);
                } else {
                    throw new ResourceUnavailableException("No more Sockets may be allocated by this requester.");
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public FileDescriptor getFileDescriptor() {
            ParcelFileDescriptor parcelFileDescriptor = this.mPfd;
            if (parcelFileDescriptor == null) {
                return null;
            }
            return parcelFileDescriptor.getFileDescriptor();
        }

        public int getPort() {
            return this.mPort;
        }

        @Override // java.lang.AutoCloseable
        public void close() throws IOException {
            try {
                this.mService.closeUdpEncapsulationSocket(this.mResourceId);
                this.mResourceId = -1;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Exception e2) {
                Log.e(IpSecManager.TAG, "Failed to close " + this + ", Exception=" + e2);
            } catch (Throwable th) {
                this.mResourceId = -1;
                this.mCloseGuard.close();
                throw th;
            }
            this.mResourceId = -1;
            this.mCloseGuard.close();
            try {
                this.mPfd.close();
            } catch (IOException e3) {
                Log.e(IpSecManager.TAG, "Failed to close UDP Encapsulation Socket with Port= " + this.mPort);
                throw e3;
            }
        }

        /* access modifiers changed from: protected */
        @Override // java.lang.Object
        public void finalize() throws Throwable {
            CloseGuard closeGuard = this.mCloseGuard;
            if (closeGuard != null) {
                closeGuard.warnIfOpen();
            }
            close();
        }

        @VisibleForTesting
        public int getResourceId() {
            return this.mResourceId;
        }

        public String toString() {
            return "UdpEncapsulationSocket{port=" + this.mPort + ",resourceId=" + this.mResourceId + "}";
        }
    }

    public UdpEncapsulationSocket openUdpEncapsulationSocket(int port) throws IOException, ResourceUnavailableException {
        if (port != 0) {
            try {
                return new UdpEncapsulationSocket(this.mService, port);
            } catch (ServiceSpecificException e) {
                throw rethrowCheckedExceptionFromServiceSpecificException(e);
            }
        } else {
            throw new IllegalArgumentException("Specified port must be a valid port number!");
        }
    }

    public UdpEncapsulationSocket openUdpEncapsulationSocket() throws IOException, ResourceUnavailableException {
        try {
            return new UdpEncapsulationSocket(this.mService, 0);
        } catch (ServiceSpecificException e) {
            throw rethrowCheckedExceptionFromServiceSpecificException(e);
        }
    }

    @SystemApi
    public static final class IpSecTunnelInterface implements AutoCloseable {
        private final CloseGuard mCloseGuard;
        private String mInterfaceName;
        private final InetAddress mLocalAddress;
        private final String mOpPackageName;
        private final InetAddress mRemoteAddress;
        private int mResourceId;
        private final IIpSecService mService;
        private final Network mUnderlyingNetwork;

        public String getInterfaceName() {
            return this.mInterfaceName;
        }

        @SystemApi
        public void addAddress(InetAddress address, int prefixLen) throws IOException {
            try {
                this.mService.addAddressToTunnelInterface(this.mResourceId, new LinkAddress(address, prefixLen), this.mOpPackageName);
            } catch (ServiceSpecificException e) {
                throw IpSecManager.rethrowCheckedExceptionFromServiceSpecificException(e);
            } catch (RemoteException e2) {
                throw e2.rethrowFromSystemServer();
            }
        }

        @SystemApi
        public void removeAddress(InetAddress address, int prefixLen) throws IOException {
            try {
                this.mService.removeAddressFromTunnelInterface(this.mResourceId, new LinkAddress(address, prefixLen), this.mOpPackageName);
            } catch (ServiceSpecificException e) {
                throw IpSecManager.rethrowCheckedExceptionFromServiceSpecificException(e);
            } catch (RemoteException e2) {
                throw e2.rethrowFromSystemServer();
            }
        }

        private IpSecTunnelInterface(Context ctx, IIpSecService service, InetAddress localAddress, InetAddress remoteAddress, Network underlyingNetwork) throws ResourceUnavailableException, IOException {
            this.mCloseGuard = CloseGuard.get();
            this.mResourceId = -1;
            this.mOpPackageName = ctx.getOpPackageName();
            this.mService = service;
            this.mLocalAddress = localAddress;
            this.mRemoteAddress = remoteAddress;
            this.mUnderlyingNetwork = underlyingNetwork;
            try {
                IpSecTunnelInterfaceResponse result = this.mService.createTunnelInterface(localAddress.getHostAddress(), remoteAddress.getHostAddress(), underlyingNetwork, new Binder(), this.mOpPackageName);
                int i = result.status;
                if (i == 0) {
                    this.mResourceId = result.resourceId;
                    this.mInterfaceName = result.interfaceName;
                    this.mCloseGuard.open("constructor");
                } else if (i != 1) {
                    throw new RuntimeException("Unknown status returned by IpSecService: " + result.status);
                } else {
                    throw new ResourceUnavailableException("No more tunnel interfaces may be allocated by this requester.");
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        @Override // java.lang.AutoCloseable
        public void close() {
            try {
                this.mService.deleteTunnelInterface(this.mResourceId, this.mOpPackageName);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Exception e2) {
                Log.e(IpSecManager.TAG, "Failed to close " + this + ", Exception=" + e2);
            } catch (Throwable th) {
                this.mResourceId = -1;
                this.mCloseGuard.close();
                throw th;
            }
            this.mResourceId = -1;
            this.mCloseGuard.close();
        }

        /* access modifiers changed from: protected */
        @Override // java.lang.Object
        public void finalize() throws Throwable {
            CloseGuard closeGuard = this.mCloseGuard;
            if (closeGuard != null) {
                closeGuard.warnIfOpen();
            }
            close();
        }

        @VisibleForTesting
        public int getResourceId() {
            return this.mResourceId;
        }

        public String toString() {
            return "IpSecTunnelInterface{ifname=" + this.mInterfaceName + ",resourceId=" + this.mResourceId + "}";
        }
    }

    @SystemApi
    public IpSecTunnelInterface createIpSecTunnelInterface(InetAddress localAddress, InetAddress remoteAddress, Network underlyingNetwork) throws ResourceUnavailableException, IOException {
        try {
            return new IpSecTunnelInterface(this.mContext, this.mService, localAddress, remoteAddress, underlyingNetwork);
        } catch (ServiceSpecificException e) {
            throw rethrowCheckedExceptionFromServiceSpecificException(e);
        }
    }

    @SystemApi
    public void applyTunnelModeTransform(IpSecTunnelInterface tunnel, int direction, IpSecTransform transform) throws IOException {
        try {
            this.mService.applyTunnelModeTransform(tunnel.getResourceId(), direction, transform.getResourceId(), this.mContext.getOpPackageName());
        } catch (ServiceSpecificException e) {
            throw rethrowCheckedExceptionFromServiceSpecificException(e);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    public IpSecManager(Context ctx, IIpSecService service) {
        this.mContext = ctx;
        this.mService = (IIpSecService) Preconditions.checkNotNull(service, "missing service");
    }

    private static void maybeHandleServiceSpecificException(ServiceSpecificException sse) {
        if (sse.errorCode == OsConstants.EINVAL) {
            throw new IllegalArgumentException(sse);
        } else if (sse.errorCode == OsConstants.EAGAIN) {
            throw new IllegalStateException(sse);
        } else if (sse.errorCode == OsConstants.EOPNOTSUPP || sse.errorCode == OsConstants.EPROTONOSUPPORT) {
            throw new UnsupportedOperationException(sse);
        }
    }

    static RuntimeException rethrowUncheckedExceptionFromServiceSpecificException(ServiceSpecificException sse) {
        maybeHandleServiceSpecificException(sse);
        throw new RuntimeException(sse);
    }

    static IOException rethrowCheckedExceptionFromServiceSpecificException(ServiceSpecificException sse) throws IOException {
        maybeHandleServiceSpecificException(sse);
        throw new ErrnoException("IpSec encountered errno=" + sse.errorCode, sse.errorCode).rethrowAsIOException();
    }
}
