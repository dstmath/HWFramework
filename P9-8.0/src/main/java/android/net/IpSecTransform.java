package android.net;

import android.content.Context;
import android.net.ConnectivityManager.PacketKeepalive;
import android.net.ConnectivityManager.PacketKeepaliveCallback;
import android.net.IIpSecService.Stub;
import android.net.IpSecManager.ResourceUnavailableException;
import android.net.IpSecManager.SecurityParameterIndex;
import android.net.IpSecManager.SpiUnavailableException;
import android.net.IpSecManager.UdpEncapsulationSocket;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.util.Preconditions;
import dalvik.system.CloseGuard;
import java.io.IOException;
import java.net.InetAddress;

public final class IpSecTransform implements AutoCloseable {
    public static final int DIRECTION_IN = 0;
    public static final int DIRECTION_OUT = 1;
    public static final int ENCAP_ESPINUDP = 1;
    public static final int ENCAP_ESPINUDP_NONIKE = 2;
    public static final int ENCAP_NONE = 0;
    private static final int MODE_TRANSPORT = 1;
    private static final int MODE_TUNNEL = 0;
    private static final String TAG = "IpSecTransform";
    private final CloseGuard mCloseGuard;
    private final IpSecConfig mConfig;
    private final Context mContext;
    private PacketKeepalive mKeepalive;
    private PacketKeepaliveCallback mKeepaliveCallback;
    private int mKeepaliveStatus;
    private Object mKeepaliveSyncLock;
    private int mResourceId;

    public static class Builder {
        private IpSecConfig mConfig = new IpSecConfig();
        private Context mContext;

        public Builder setEncryption(int direction, IpSecAlgorithm algo) {
            this.mConfig.flow[direction].encryption = algo;
            return this;
        }

        public Builder setAuthentication(int direction, IpSecAlgorithm algo) {
            this.mConfig.flow[direction].authentication = algo;
            return this;
        }

        public Builder setSpi(int direction, SecurityParameterIndex spi) {
            this.mConfig.flow[direction].spi = spi.getSpi();
            return this;
        }

        public Builder setUnderlyingNetwork(Network net) {
            this.mConfig.network = net;
            return this;
        }

        public Builder setIpv4Encapsulation(UdpEncapsulationSocket localSocket, int remotePort) {
            this.mConfig.encapType = 1;
            this.mConfig.encapLocalPort = localSocket.getPort();
            this.mConfig.encapRemotePort = remotePort;
            return this;
        }

        public Builder setNattKeepalive(int intervalSeconds) {
            this.mConfig.nattKeepaliveInterval = intervalSeconds;
            return this;
        }

        public IpSecTransform buildTransportModeTransform(InetAddress remoteAddress) throws ResourceUnavailableException, SpiUnavailableException, IOException {
            this.mConfig.mode = 1;
            this.mConfig.remoteAddress = remoteAddress;
            return new IpSecTransform(this.mContext, this.mConfig, null).activate();
        }

        public IpSecTransform buildTunnelModeTransform(InetAddress localAddress, InetAddress remoteAddress) {
            this.mConfig.localAddress = localAddress;
            this.mConfig.remoteAddress = remoteAddress;
            this.mConfig.mode = 0;
            return new IpSecTransform(this.mContext, this.mConfig, null);
        }

        public Builder(Context context) {
            Preconditions.checkNotNull(context);
            this.mContext = context;
        }
    }

    /* synthetic */ IpSecTransform(Context context, IpSecConfig config, IpSecTransform -this2) {
        this(context, config);
    }

    private IpSecTransform(Context context, IpSecConfig config) {
        this.mCloseGuard = CloseGuard.get();
        this.mKeepaliveStatus = -1;
        this.mKeepaliveSyncLock = new Object();
        this.mKeepaliveCallback = new PacketKeepaliveCallback() {
            public void onStarted() {
                synchronized (IpSecTransform.this.mKeepaliveSyncLock) {
                    IpSecTransform.this.mKeepaliveStatus = 0;
                    IpSecTransform.this.mKeepaliveSyncLock.notifyAll();
                }
            }

            public void onStopped() {
                synchronized (IpSecTransform.this.mKeepaliveSyncLock) {
                    IpSecTransform.this.mKeepaliveStatus = -1;
                    IpSecTransform.this.mKeepaliveSyncLock.notifyAll();
                }
            }

            public void onError(int error) {
                synchronized (IpSecTransform.this.mKeepaliveSyncLock) {
                    IpSecTransform.this.mKeepaliveStatus = error;
                    IpSecTransform.this.mKeepaliveSyncLock.notifyAll();
                }
            }
        };
        this.mContext = context;
        this.mConfig = config;
        this.mResourceId = 0;
    }

    private IIpSecService getIpSecService() {
        IBinder b = ServiceManager.getService(Context.IPSEC_SERVICE);
        if (b != null) {
            return Stub.asInterface(b);
        }
        throw new RemoteException("Failed to connect to IpSecService").rethrowAsRuntimeException();
    }

    private void checkResultStatusAndThrow(int status) throws IOException, ResourceUnavailableException, SpiUnavailableException {
        switch (status) {
            case 0:
                return;
            case 1:
                throw new ResourceUnavailableException("Failed to allocate a new IpSecTransform");
            case 2:
                Log.wtf(TAG, "Attempting to use an SPI that was somehow not reserved");
                break;
        }
        throw new IllegalStateException("Failed to Create a Transform with status code " + status);
    }

    private IpSecTransform activate() throws IOException, ResourceUnavailableException, SpiUnavailableException {
        synchronized (this) {
            try {
                Bundle result = getIpSecService().createTransportModeTransform(this.mConfig, new Binder());
                checkResultStatusAndThrow(result.getInt("status"));
                this.mResourceId = result.getInt(IpSecManager.KEY_RESOURCE_ID, 0);
                startKeepalive(this.mContext);
                Log.d(TAG, "Added Transform with Id " + this.mResourceId);
                this.mCloseGuard.open("build");
            } catch (RemoteException e) {
                throw e.rethrowAsRuntimeException();
            }
        }
        return this;
    }

    public void close() {
        Log.d(TAG, "Removing Transform with Id " + this.mResourceId);
        if (this.mResourceId == 0) {
            this.mCloseGuard.close();
            return;
        }
        try {
            getIpSecService().deleteTransportModeTransform(this.mResourceId);
            stopKeepalive();
            this.mResourceId = 0;
            this.mCloseGuard.close();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        } catch (Throwable th) {
            this.mResourceId = 0;
            this.mCloseGuard.close();
        }
    }

    protected void finalize() throws Throwable {
        if (this.mCloseGuard != null) {
            this.mCloseGuard.warnIfOpen();
        }
        close();
    }

    IpSecConfig getConfig() {
        return this.mConfig;
    }

    void startKeepalive(Context c) {
        if (this.mConfig.getNattKeepaliveInterval() != 0) {
            ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (this.mKeepalive != null) {
                Log.wtf(TAG, "Keepalive already started for this IpSecTransform.");
                return;
            }
            synchronized (this.mKeepaliveSyncLock) {
                this.mKeepalive = cm.startNattKeepalive(this.mConfig.getNetwork(), this.mConfig.getNattKeepaliveInterval(), this.mKeepaliveCallback, this.mConfig.getLocalAddress(), this.mConfig.getEncapLocalPort(), this.mConfig.getRemoteAddress());
                try {
                    this.mKeepaliveSyncLock.wait(2000);
                } catch (InterruptedException e) {
                }
            }
            if (this.mKeepaliveStatus != 0) {
                throw new UnsupportedOperationException("Packet Keepalive cannot be started");
            }
        }
    }

    int getResourceId() {
        return this.mResourceId;
    }

    void stopKeepalive() {
        if (this.mKeepalive != null) {
            this.mKeepalive.stop();
            synchronized (this.mKeepaliveSyncLock) {
                if (this.mKeepaliveStatus == 0) {
                    try {
                        this.mKeepaliveSyncLock.wait(2000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }
}
