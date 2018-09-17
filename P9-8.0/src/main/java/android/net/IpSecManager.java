package android.net;

import android.os.Binder;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.AndroidException;
import com.android.internal.util.Preconditions;
import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public final class IpSecManager {
    public static final int INVALID_RESOURCE_ID = 0;
    public static final int INVALID_SECURITY_PARAMETER_INDEX = 0;
    public static final String KEY_RESOURCE_ID = "resourceId";
    public static final String KEY_SPI = "spi";
    public static final String KEY_STATUS = "status";
    private static final String TAG = "IpSecManager";
    private final IIpSecService mService;

    public static final class ResourceUnavailableException extends AndroidException {
        ResourceUnavailableException(String msg) {
            super(msg);
        }
    }

    public static final class SecurityParameterIndex implements AutoCloseable {
        private final CloseGuard mCloseGuard;
        private final InetAddress mRemoteAddress;
        private int mResourceId;
        private final IIpSecService mService;
        private int mSpi;

        /* synthetic */ SecurityParameterIndex(IIpSecService service, int direction, InetAddress remoteAddress, int spi, SecurityParameterIndex -this4) {
            this(service, direction, remoteAddress, spi);
        }

        public int getSpi() {
            return this.mSpi;
        }

        public void close() {
            this.mSpi = 0;
            this.mCloseGuard.close();
        }

        protected void finalize() {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            close();
        }

        private SecurityParameterIndex(IIpSecService service, int direction, InetAddress remoteAddress, int spi) throws ResourceUnavailableException, SpiUnavailableException {
            this.mCloseGuard = CloseGuard.get();
            this.mSpi = 0;
            this.mService = service;
            this.mRemoteAddress = remoteAddress;
            try {
                Bundle result = this.mService.reserveSecurityParameterIndex(direction, remoteAddress.getHostAddress(), spi, new Binder());
                if (result == null) {
                    throw new NullPointerException("Received null response from IpSecService");
                }
                int status = result.getInt("status");
                switch (status) {
                    case 0:
                        this.mSpi = result.getInt(IpSecManager.KEY_SPI);
                        this.mResourceId = result.getInt(IpSecManager.KEY_RESOURCE_ID);
                        if (this.mSpi == 0) {
                            throw new RuntimeException("Invalid SPI returned by IpSecService: " + status);
                        } else if (this.mResourceId == 0) {
                            throw new RuntimeException("Invalid Resource ID returned by IpSecService: " + status);
                        } else {
                            this.mCloseGuard.open("open");
                            return;
                        }
                    case 1:
                        throw new ResourceUnavailableException("No more SPIs may be allocated by this requester.");
                    case 2:
                        throw new SpiUnavailableException("Requested SPI is unavailable", spi);
                    default:
                        throw new RuntimeException("Unknown status returned by IpSecService: " + status);
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public static final class SpiUnavailableException extends AndroidException {
        private final int mSpi;

        SpiUnavailableException(String msg, int spi) {
            super(msg + "(spi: " + spi + ")");
            this.mSpi = spi;
        }

        public int getSpi() {
            return this.mSpi;
        }
    }

    public interface Status {
        public static final int OK = 0;
        public static final int RESOURCE_UNAVAILABLE = 1;
        public static final int SPI_UNAVAILABLE = 2;
    }

    public static final class UdpEncapsulationSocket implements AutoCloseable {
        private final CloseGuard mCloseGuard;
        private final FileDescriptor mFd;
        private final IIpSecService mService;

        /* synthetic */ UdpEncapsulationSocket(IIpSecService service, int port, UdpEncapsulationSocket -this2) {
            this(service, port);
        }

        private UdpEncapsulationSocket(IIpSecService service, int port) throws ResourceUnavailableException {
            this.mCloseGuard = CloseGuard.get();
            this.mService = service;
            this.mCloseGuard.open("constructor");
            this.mFd = new FileDescriptor();
        }

        private UdpEncapsulationSocket(IIpSecService service) throws ResourceUnavailableException {
            this.mCloseGuard = CloseGuard.get();
            this.mService = service;
            this.mCloseGuard.open("constructor");
            this.mFd = new FileDescriptor();
        }

        public FileDescriptor getSocket() {
            return this.mFd;
        }

        public int getPort() {
            return 0;
        }

        public void close() throws IOException {
            this.mCloseGuard.close();
        }

        protected void finalize() throws Throwable {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            close();
        }
    }

    public SecurityParameterIndex reserveSecurityParameterIndex(int direction, InetAddress remoteAddress) throws ResourceUnavailableException {
        try {
            return new SecurityParameterIndex(this.mService, direction, remoteAddress, 0, null);
        } catch (SpiUnavailableException e) {
            throw new ResourceUnavailableException("No SPIs available");
        }
    }

    public SecurityParameterIndex reserveSecurityParameterIndex(int direction, InetAddress remoteAddress, int requestedSpi) throws SpiUnavailableException, ResourceUnavailableException {
        if (requestedSpi != 0) {
            return new SecurityParameterIndex(this.mService, direction, remoteAddress, requestedSpi, null);
        }
        throw new IllegalArgumentException("Requested SPI must be a valid (non-zero) SPI");
    }

    public void applyTransportModeTransform(Socket socket, IpSecTransform transform) throws IOException {
        applyTransportModeTransform(ParcelFileDescriptor.fromSocket(socket), transform);
    }

    public void applyTransportModeTransform(DatagramSocket socket, IpSecTransform transform) throws IOException {
        applyTransportModeTransform(ParcelFileDescriptor.fromDatagramSocket(socket), transform);
    }

    private void applyTransportModeTransform(ParcelFileDescriptor pfd, IpSecTransform transform) {
        try {
            this.mService.applyTransportModeTransform(pfd, transform.getResourceId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void applyTransportModeTransform(FileDescriptor socket, IpSecTransform transform) throws IOException {
        applyTransportModeTransform(new ParcelFileDescriptor(socket), transform);
    }

    public void applyTunnelModeTransform(Network net, IpSecTransform transform) {
    }

    public void removeTransportModeTransform(Socket socket, IpSecTransform transform) throws IOException {
        removeTransportModeTransform(ParcelFileDescriptor.fromSocket(socket), transform);
    }

    public void removeTransportModeTransform(DatagramSocket socket, IpSecTransform transform) throws IOException {
        removeTransportModeTransform(ParcelFileDescriptor.fromDatagramSocket(socket), transform);
    }

    public void removeTransportModeTransform(FileDescriptor socket, IpSecTransform transform) throws IOException {
        removeTransportModeTransform(new ParcelFileDescriptor(socket), transform);
    }

    private void removeTransportModeTransform(ParcelFileDescriptor pfd, IpSecTransform transform) {
        try {
            this.mService.removeTransportModeTransform(pfd, transform.getResourceId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeTunnelModeTransform(Network net, IpSecTransform transform) {
    }

    public UdpEncapsulationSocket openUdpEncapsulationSocket(int port) throws IOException, ResourceUnavailableException {
        return new UdpEncapsulationSocket(this.mService, port, null);
    }

    public UdpEncapsulationSocket openUdpEncapsulationSocket() throws IOException, ResourceUnavailableException {
        return new UdpEncapsulationSocket(this.mService, null);
    }

    public IpSecManager(IIpSecService service) {
        this.mService = (IIpSecService) Preconditions.checkNotNull(service, "missing service");
    }
}
