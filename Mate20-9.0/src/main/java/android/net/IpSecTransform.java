package android.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.IIpSecService;
import android.net.IpSecManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import dalvik.system.CloseGuard;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.InetAddress;

public final class IpSecTransform implements AutoCloseable {
    public static final int ENCAP_ESPINUDP = 2;
    public static final int ENCAP_ESPINUDP_NON_IKE = 1;
    public static final int ENCAP_NONE = 0;
    public static final int MODE_TRANSPORT = 0;
    public static final int MODE_TUNNEL = 1;
    private static final String TAG = "IpSecTransform";
    /* access modifiers changed from: private */
    public Handler mCallbackHandler;
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private final IpSecConfig mConfig;
    private final Context mContext;
    /* access modifiers changed from: private */
    public ConnectivityManager.PacketKeepalive mKeepalive;
    private final ConnectivityManager.PacketKeepaliveCallback mKeepaliveCallback = new ConnectivityManager.PacketKeepaliveCallback() {
        public void onStarted() {
            synchronized (this) {
                IpSecTransform.this.mCallbackHandler.post(new Runnable() {
                    public final void run() {
                        IpSecTransform.this.mUserKeepaliveCallback.onStarted();
                    }
                });
            }
        }

        public void onStopped() {
            synchronized (this) {
                ConnectivityManager.PacketKeepalive unused = IpSecTransform.this.mKeepalive = null;
                IpSecTransform.this.mCallbackHandler.post(new Runnable() {
                    public final void run() {
                        IpSecTransform.this.mUserKeepaliveCallback.onStopped();
                    }
                });
            }
        }

        public void onError(int error) {
            synchronized (this) {
                ConnectivityManager.PacketKeepalive unused = IpSecTransform.this.mKeepalive = null;
                IpSecTransform.this.mCallbackHandler.post(new Runnable(error) {
                    private final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        IpSecTransform.this.mUserKeepaliveCallback.onError(this.f$1);
                    }
                });
            }
        }
    };
    private int mResourceId;
    /* access modifiers changed from: private */
    public NattKeepaliveCallback mUserKeepaliveCallback;

    public static class Builder {
        private IpSecConfig mConfig = new IpSecConfig();
        private Context mContext;

        public Builder setEncryption(IpSecAlgorithm algo) {
            Preconditions.checkNotNull(algo);
            this.mConfig.setEncryption(algo);
            return this;
        }

        public Builder setAuthentication(IpSecAlgorithm algo) {
            Preconditions.checkNotNull(algo);
            this.mConfig.setAuthentication(algo);
            return this;
        }

        public Builder setAuthenticatedEncryption(IpSecAlgorithm algo) {
            Preconditions.checkNotNull(algo);
            this.mConfig.setAuthenticatedEncryption(algo);
            return this;
        }

        public Builder setIpv4Encapsulation(IpSecManager.UdpEncapsulationSocket localSocket, int remotePort) {
            Preconditions.checkNotNull(localSocket);
            this.mConfig.setEncapType(2);
            if (localSocket.getResourceId() != -1) {
                this.mConfig.setEncapSocketResourceId(localSocket.getResourceId());
                this.mConfig.setEncapRemotePort(remotePort);
                return this;
            }
            throw new IllegalArgumentException("Invalid UdpEncapsulationSocket");
        }

        public IpSecTransform buildTransportModeTransform(InetAddress sourceAddress, IpSecManager.SecurityParameterIndex spi) throws IpSecManager.ResourceUnavailableException, IpSecManager.SpiUnavailableException, IOException {
            Preconditions.checkNotNull(sourceAddress);
            Preconditions.checkNotNull(spi);
            if (spi.getResourceId() != -1) {
                this.mConfig.setMode(0);
                this.mConfig.setSourceAddress(sourceAddress.getHostAddress());
                this.mConfig.setSpiResourceId(spi.getResourceId());
                return new IpSecTransform(this.mContext, this.mConfig).activate();
            }
            throw new IllegalArgumentException("Invalid SecurityParameterIndex");
        }

        public IpSecTransform buildTunnelModeTransform(InetAddress sourceAddress, IpSecManager.SecurityParameterIndex spi) throws IpSecManager.ResourceUnavailableException, IpSecManager.SpiUnavailableException, IOException {
            Preconditions.checkNotNull(sourceAddress);
            Preconditions.checkNotNull(spi);
            if (spi.getResourceId() != -1) {
                this.mConfig.setMode(1);
                this.mConfig.setSourceAddress(sourceAddress.getHostAddress());
                this.mConfig.setSpiResourceId(spi.getResourceId());
                return new IpSecTransform(this.mContext, this.mConfig).activate();
            }
            throw new IllegalArgumentException("Invalid SecurityParameterIndex");
        }

        public Builder(Context context) {
            Preconditions.checkNotNull(context);
            this.mContext = context;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface EncapType {
    }

    public static class NattKeepaliveCallback {
        public static final int ERROR_HARDWARE_ERROR = 3;
        public static final int ERROR_HARDWARE_UNSUPPORTED = 2;
        public static final int ERROR_INVALID_NETWORK = 1;

        public void onStarted() {
        }

        public void onStopped() {
        }

        public void onError(int error) {
        }
    }

    @VisibleForTesting
    public IpSecTransform(Context context, IpSecConfig config) {
        this.mContext = context;
        this.mConfig = new IpSecConfig(config);
        this.mResourceId = -1;
    }

    private IIpSecService getIpSecService() {
        IBinder b = ServiceManager.getService("ipsec");
        if (b != null) {
            return IIpSecService.Stub.asInterface(b);
        }
        throw new RemoteException("Failed to connect to IpSecService").rethrowAsRuntimeException();
    }

    private void checkResultStatus(int status) throws IOException, IpSecManager.ResourceUnavailableException, IpSecManager.SpiUnavailableException {
        switch (status) {
            case 0:
                return;
            case 1:
                throw new IpSecManager.ResourceUnavailableException("Failed to allocate a new IpSecTransform");
            case 2:
                Log.wtf(TAG, "Attempting to use an SPI that was somehow not reserved");
                break;
        }
        throw new IllegalStateException("Failed to Create a Transform with status code " + status);
    }

    /* access modifiers changed from: private */
    public IpSecTransform activate() throws IOException, IpSecManager.ResourceUnavailableException, IpSecManager.SpiUnavailableException {
        synchronized (this) {
            try {
                IpSecTransformResponse result = getIpSecService().createTransform(this.mConfig, new Binder(), this.mContext.getOpPackageName());
                checkResultStatus(result.status);
                this.mResourceId = result.resourceId;
                Log.d(TAG, "Added Transform with Id " + this.mResourceId);
                this.mCloseGuard.open("build");
            } catch (ServiceSpecificException e) {
                throw IpSecManager.rethrowUncheckedExceptionFromServiceSpecificException(e);
            } catch (RemoteException e2) {
                throw e2.rethrowAsRuntimeException();
            } catch (Throwable th) {
                throw th;
            }
        }
        return this;
    }

    @VisibleForTesting
    public static boolean equals(IpSecTransform lhs, IpSecTransform rhs) {
        boolean z = false;
        if (lhs == null || rhs == null) {
            if (lhs == rhs) {
                z = true;
            }
            return z;
        }
        if (IpSecConfig.equals(lhs.getConfig(), rhs.getConfig()) && lhs.mResourceId == rhs.mResourceId) {
            z = true;
        }
        return z;
    }

    public void close() {
        Log.d(TAG, "Removing Transform with Id " + this.mResourceId);
        if (this.mResourceId == -1) {
            this.mCloseGuard.close();
            return;
        }
        try {
            getIpSecService().deleteTransform(this.mResourceId);
            stopNattKeepalive();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        } catch (Exception e2) {
            Log.e(TAG, "Failed to close " + this + ", Exception=" + e2);
        } catch (Throwable th) {
            this.mResourceId = -1;
            this.mCloseGuard.close();
            throw th;
        }
        this.mResourceId = -1;
        this.mCloseGuard.close();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        if (this.mCloseGuard != null) {
            this.mCloseGuard.warnIfOpen();
        }
        close();
    }

    /* access modifiers changed from: package-private */
    public IpSecConfig getConfig() {
        return this.mConfig;
    }

    @VisibleForTesting
    public int getResourceId() {
        return this.mResourceId;
    }

    public void startNattKeepalive(NattKeepaliveCallback userCallback, int intervalSeconds, Handler handler) throws IOException {
        Preconditions.checkNotNull(userCallback);
        if (intervalSeconds < 20 || intervalSeconds > 3600) {
            throw new IllegalArgumentException("Invalid NAT-T keepalive interval");
        }
        Preconditions.checkNotNull(handler);
        if (this.mResourceId != -1) {
            synchronized (this.mKeepaliveCallback) {
                if (this.mKeepaliveCallback == null) {
                    this.mUserKeepaliveCallback = userCallback;
                    this.mKeepalive = ((ConnectivityManager) this.mContext.getSystemService(Context.CONNECTIVITY_SERVICE)).startNattKeepalive(this.mConfig.getNetwork(), intervalSeconds, this.mKeepaliveCallback, NetworkUtils.numericToInetAddress(this.mConfig.getSourceAddress()), ConnectivityManager.PacketKeepalive.NATT_PORT, NetworkUtils.numericToInetAddress(this.mConfig.getDestinationAddress()));
                    this.mCallbackHandler = handler;
                } else {
                    throw new IllegalStateException("Keepalive already active");
                }
            }
            return;
        }
        throw new IllegalStateException("Packet keepalive cannot be started for an inactive transform");
    }

    public void stopNattKeepalive() {
        synchronized (this.mKeepaliveCallback) {
            if (this.mKeepalive == null) {
                Log.e(TAG, "No active keepalive to stop");
            } else {
                this.mKeepalive.stop();
            }
        }
    }

    public String toString() {
        return "IpSecTransform{resourceId=" + this.mResourceId + "}";
    }
}
