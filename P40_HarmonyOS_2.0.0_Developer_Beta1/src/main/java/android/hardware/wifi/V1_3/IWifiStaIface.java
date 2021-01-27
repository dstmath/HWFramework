package android.hardware.wifi.V1_3;

import android.hardware.wifi.V1_0.IWifiIface;
import android.hardware.wifi.V1_0.IWifiStaIface;
import android.hardware.wifi.V1_0.IWifiStaIfaceEventCallback;
import android.hardware.wifi.V1_0.StaApfPacketFilterCapabilities;
import android.hardware.wifi.V1_0.StaBackgroundScanCapabilities;
import android.hardware.wifi.V1_0.StaBackgroundScanParameters;
import android.hardware.wifi.V1_0.StaLinkLayerStats;
import android.hardware.wifi.V1_0.StaRoamingCapabilities;
import android.hardware.wifi.V1_0.StaRoamingConfig;
import android.hardware.wifi.V1_0.WifiDebugRxPacketFateReport;
import android.hardware.wifi.V1_0.WifiDebugTxPacketFateReport;
import android.hardware.wifi.V1_0.WifiStatus;
import android.hardware.wifi.V1_2.IWifiStaIface;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
import android.hidl.base.V1_0.DebugInfo;
import android.hidl.base.V1_0.IBase;
import android.os.HidlSupport;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.NativeHandle;
import android.os.RemoteException;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public interface IWifiStaIface extends android.hardware.wifi.V1_2.IWifiStaIface {
    public static final String kInterfaceName = "android.hardware.wifi@1.3::IWifiStaIface";

    @FunctionalInterface
    public interface getFactoryMacAddressCallback {
        void onValues(WifiStatus wifiStatus, byte[] bArr);
    }

    @FunctionalInterface
    public interface getLinkLayerStats_1_3Callback {
        void onValues(WifiStatus wifiStatus, StaLinkLayerStats staLinkLayerStats);
    }

    @Override // android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    @Override // android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    @Override // android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    void getFactoryMacAddress(getFactoryMacAddressCallback getfactorymacaddresscallback) throws RemoteException;

    @Override // android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    void getLinkLayerStats_1_3(getLinkLayerStats_1_3Callback getlinklayerstats_1_3callback) throws RemoteException;

    @Override // android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    @Override // android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    @Override // android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    @Override // android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static IWifiStaIface asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IWifiStaIface)) {
            return (IWifiStaIface) iface;
        }
        IWifiStaIface proxy = new Proxy(binder);
        try {
            Iterator<String> it = proxy.interfaceChain().iterator();
            while (it.hasNext()) {
                if (it.next().equals(kInterfaceName)) {
                    return proxy;
                }
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    static IWifiStaIface castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IWifiStaIface getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IWifiStaIface getService(boolean retry) throws RemoteException {
        return getService("default", retry);
    }

    static IWifiStaIface getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IWifiStaIface getService() throws RemoteException {
        return getService("default");
    }

    public static final class Proxy implements IWifiStaIface {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of android.hardware.wifi@1.3::IWifiStaIface]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // android.hardware.wifi.V1_0.IWifiIface
        public void getType(IWifiIface.getTypeCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiIface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiIface
        public void getName(IWifiIface.getNameCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiIface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public WifiStatus registerEventCallback(IWifiStaIfaceEventCallback callback) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            _hidl_request.writeStrongBinder(callback == null ? null : callback.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public void getCapabilities(IWifiStaIface.getCapabilitiesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public void getApfPacketFilterCapabilities(IWifiStaIface.getApfPacketFilterCapabilitiesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                StaApfPacketFilterCapabilities _hidl_out_capabilities = new StaApfPacketFilterCapabilities();
                _hidl_out_capabilities.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_out_capabilities);
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public WifiStatus installApfPacketFilter(int cmdId, ArrayList<Byte> program) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            _hidl_request.writeInt32(cmdId);
            _hidl_request.writeInt8Vector(program);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public void getBackgroundScanCapabilities(IWifiStaIface.getBackgroundScanCapabilitiesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                StaBackgroundScanCapabilities _hidl_out_capabilities = new StaBackgroundScanCapabilities();
                _hidl_out_capabilities.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_out_capabilities);
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public void getValidFrequenciesForBand(int band, IWifiStaIface.getValidFrequenciesForBandCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            _hidl_request.writeInt32(band);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public WifiStatus startBackgroundScan(int cmdId, StaBackgroundScanParameters params) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            _hidl_request.writeInt32(cmdId);
            params.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public WifiStatus stopBackgroundScan(int cmdId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            _hidl_request.writeInt32(cmdId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public WifiStatus enableLinkLayerStatsCollection(boolean debug) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            _hidl_request.writeBool(debug);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public WifiStatus disableLinkLayerStatsCollection() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public void getLinkLayerStats(IWifiStaIface.getLinkLayerStatsCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                StaLinkLayerStats _hidl_out_stats = new StaLinkLayerStats();
                _hidl_out_stats.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_out_stats);
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public WifiStatus startRssiMonitoring(int cmdId, int maxRssi, int minRssi) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            _hidl_request.writeInt32(cmdId);
            _hidl_request.writeInt32(maxRssi);
            _hidl_request.writeInt32(minRssi);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public WifiStatus stopRssiMonitoring(int cmdId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            _hidl_request.writeInt32(cmdId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public void getRoamingCapabilities(IWifiStaIface.getRoamingCapabilitiesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                StaRoamingCapabilities _hidl_out_caps = new StaRoamingCapabilities();
                _hidl_out_caps.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_out_caps);
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public WifiStatus configureRoaming(StaRoamingConfig config) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            config.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public WifiStatus setRoamingState(byte state) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            _hidl_request.writeInt8(state);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(18, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public WifiStatus enableNdOffload(boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(19, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public WifiStatus startSendingKeepAlivePackets(int cmdId, ArrayList<Byte> ipPacketData, short etherType, byte[] srcAddress, byte[] dstAddress, int periodInMs) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            _hidl_request.writeInt32(cmdId);
            _hidl_request.writeInt8Vector(ipPacketData);
            _hidl_request.writeInt16(etherType);
            HwBlob _hidl_blob = new HwBlob(6);
            if (srcAddress == null || srcAddress.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, srcAddress);
            _hidl_request.writeBuffer(_hidl_blob);
            HwBlob _hidl_blob2 = new HwBlob(6);
            if (dstAddress == null || dstAddress.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob2.putInt8Array(0, dstAddress);
            _hidl_request.writeBuffer(_hidl_blob2);
            _hidl_request.writeInt32(periodInMs);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(20, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public WifiStatus stopSendingKeepAlivePackets(int cmdId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            _hidl_request.writeInt32(cmdId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(21, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public WifiStatus setScanningMacOui(byte[] oui) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(3);
            if (oui == null || oui.length != 3) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, oui);
            _hidl_request.writeBuffer(_hidl_blob);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(22, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public WifiStatus startDebugPacketFateMonitoring() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(23, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public void getDebugTxPacketFates(IWifiStaIface.getDebugTxPacketFatesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(24, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, WifiDebugTxPacketFateReport.readVectorFromParcel(_hidl_reply));
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiStaIface
        public void getDebugRxPacketFates(IWifiStaIface.getDebugRxPacketFatesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(25, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, WifiDebugRxPacketFateReport.readVectorFromParcel(_hidl_reply));
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_2.IWifiStaIface
        public void readApfPacketFilterData(IWifiStaIface.readApfPacketFilterDataCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_2.IWifiStaIface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(26, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt8Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_2.IWifiStaIface
        public WifiStatus setMacAddress(byte[] mac) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_2.IWifiStaIface.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            if (mac == null || mac.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            _hidl_blob.putInt8Array(0, mac);
            _hidl_request.writeBuffer(_hidl_blob);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(27, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface
        public void getLinkLayerStats_1_3(getLinkLayerStats_1_3Callback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(28, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                StaLinkLayerStats _hidl_out_stats = new StaLinkLayerStats();
                _hidl_out_stats.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_out_stats);
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface
        public void getFactoryMacAddress(getFactoryMacAddressCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(29, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                byte[] _hidl_out_mac = new byte[6];
                _hidl_reply.readBuffer(6).copyToInt8Array(0, _hidl_out_mac, 6);
                _hidl_cb.onValues(_hidl_out_status, _hidl_out_mac);
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public ArrayList<String> interfaceChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256067662, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readStringVector();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> options) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            _hidl_request.writeNativeHandle(fd);
            _hidl_request.writeStringVector(options);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256131655, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public String interfaceDescriptor() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256136003, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readString();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public ArrayList<byte[]> getHashChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256398152, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<byte[]> _hidl_out_hashchain = new ArrayList<>();
                HwBlob _hidl_blob = _hidl_reply.readBuffer(16);
                int _hidl_vec_size = _hidl_blob.getInt32(8);
                HwBlob childBlob = _hidl_reply.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
                _hidl_out_hashchain.clear();
                for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                    byte[] _hidl_vec_element = new byte[32];
                    childBlob.copyToInt8Array((long) (_hidl_index_0 * 32), _hidl_vec_element, 32);
                    _hidl_out_hashchain.add(_hidl_vec_element);
                }
                return _hidl_out_hashchain;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public void setHALInstrumentation() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256462420, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public void ping() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256921159, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public DebugInfo getDebugInfo() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257049926, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                DebugInfo _hidl_out_info = new DebugInfo();
                _hidl_out_info.readFromParcel(_hidl_reply);
                return _hidl_out_info;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public void notifySyspropsChanged() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257120595, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IWifiStaIface {
        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IWifiStaIface.kInterfaceName, android.hardware.wifi.V1_2.IWifiStaIface.kInterfaceName, android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName, IWifiIface.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return IWifiStaIface.kInterfaceName;
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{59, -17, 48, -24, -74, 26, -80, 80, -64, -10, -3, 38, 87, 39, 18, -66, 94, -69, 119, 7, -42, 36, -55, -86, 108, 116, -69, -71, -42, -91, -76, -87}, new byte[]{-11, 104, 45, -65, 25, -9, 18, -66, -7, -52, 63, -86, 95, -29, -36, 103, 11, 111, -5, -53, 98, -95, 71, -95, -40, 107, -99, 67, 87, 76, -40, 63}, new byte[]{59, Byte.MIN_VALUE, -109, -45, -98, -15, -31, 14, 67, -59, 83, -118, -5, -11, -1, 110, 57, -72, -40, 22, -114, -69, -31, -103, -115, -103, 62, -119, -30, 95, 20, -91}, new byte[]{107, -102, -44, 58, 94, -5, -26, -54, 33, 79, 117, 30, 34, -50, 67, -49, 92, -44, -43, -43, -14, -53, -88, 15, 36, -52, -45, 117, 90, 114, 64, 28}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // android.hardware.wifi.V1_3.IWifiStaIface, android.hardware.wifi.V1_2.IWifiStaIface, android.hardware.wifi.V1_0.IWifiStaIface, android.hardware.wifi.V1_0.IWifiIface, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IWifiStaIface.kInterfaceName.equals(descriptor)) {
                return this;
            }
            return null;
        }

        public void registerAsService(String serviceName) throws RemoteException {
            registerService(serviceName);
        }

        public String toString() {
            return interfaceDescriptor() + "@Stub";
        }

        public void onTransact(int _hidl_code, HwParcel _hidl_request, final HwParcel _hidl_reply, int _hidl_flags) throws RemoteException {
            boolean _hidl_is_oneway = false;
            boolean _hidl_is_oneway2 = true;
            switch (_hidl_code) {
                case 1:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiIface.kInterfaceName);
                    getType(new IWifiIface.getTypeCallback() {
                        /* class android.hardware.wifi.V1_3.IWifiStaIface.Stub.AnonymousClass1 */

                        @Override // android.hardware.wifi.V1_0.IWifiIface.getTypeCallback
                        public void onValues(WifiStatus status, int type) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(type);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 2:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiIface.kInterfaceName);
                    getName(new IWifiIface.getNameCallback() {
                        /* class android.hardware.wifi.V1_3.IWifiStaIface.Stub.AnonymousClass2 */

                        @Override // android.hardware.wifi.V1_0.IWifiIface.getNameCallback
                        public void onValues(WifiStatus status, String name) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeString(name);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 3:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status = registerEventCallback(IWifiStaIfaceEventCallback.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 4:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    getCapabilities(new IWifiStaIface.getCapabilitiesCallback() {
                        /* class android.hardware.wifi.V1_3.IWifiStaIface.Stub.AnonymousClass3 */

                        @Override // android.hardware.wifi.V1_0.IWifiStaIface.getCapabilitiesCallback
                        public void onValues(WifiStatus status, int capabilities) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(capabilities);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 5:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    getApfPacketFilterCapabilities(new IWifiStaIface.getApfPacketFilterCapabilitiesCallback() {
                        /* class android.hardware.wifi.V1_3.IWifiStaIface.Stub.AnonymousClass4 */

                        @Override // android.hardware.wifi.V1_0.IWifiStaIface.getApfPacketFilterCapabilitiesCallback
                        public void onValues(WifiStatus status, StaApfPacketFilterCapabilities capabilities) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            capabilities.writeToParcel(_hidl_reply);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 6:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status2 = installApfPacketFilter(_hidl_request.readInt32(), _hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status2.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 7:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    getBackgroundScanCapabilities(new IWifiStaIface.getBackgroundScanCapabilitiesCallback() {
                        /* class android.hardware.wifi.V1_3.IWifiStaIface.Stub.AnonymousClass5 */

                        @Override // android.hardware.wifi.V1_0.IWifiStaIface.getBackgroundScanCapabilitiesCallback
                        public void onValues(WifiStatus status, StaBackgroundScanCapabilities capabilities) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            capabilities.writeToParcel(_hidl_reply);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 8:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    getValidFrequenciesForBand(_hidl_request.readInt32(), new IWifiStaIface.getValidFrequenciesForBandCallback() {
                        /* class android.hardware.wifi.V1_3.IWifiStaIface.Stub.AnonymousClass6 */

                        @Override // android.hardware.wifi.V1_0.IWifiStaIface.getValidFrequenciesForBandCallback
                        public void onValues(WifiStatus status, ArrayList<Integer> frequencies) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32Vector(frequencies);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 9:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    int cmdId = _hidl_request.readInt32();
                    StaBackgroundScanParameters params = new StaBackgroundScanParameters();
                    params.readFromParcel(_hidl_request);
                    WifiStatus _hidl_out_status3 = startBackgroundScan(cmdId, params);
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status3.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 10:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status4 = stopBackgroundScan(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status4.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 11:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status5 = enableLinkLayerStatsCollection(_hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status5.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 12:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status6 = disableLinkLayerStatsCollection();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status6.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 13:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    getLinkLayerStats(new IWifiStaIface.getLinkLayerStatsCallback() {
                        /* class android.hardware.wifi.V1_3.IWifiStaIface.Stub.AnonymousClass7 */

                        @Override // android.hardware.wifi.V1_0.IWifiStaIface.getLinkLayerStatsCallback
                        public void onValues(WifiStatus status, StaLinkLayerStats stats) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            stats.writeToParcel(_hidl_reply);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 14:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status7 = startRssiMonitoring(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status7.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 15:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status8 = stopRssiMonitoring(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status8.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 16:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    getRoamingCapabilities(new IWifiStaIface.getRoamingCapabilitiesCallback() {
                        /* class android.hardware.wifi.V1_3.IWifiStaIface.Stub.AnonymousClass8 */

                        @Override // android.hardware.wifi.V1_0.IWifiStaIface.getRoamingCapabilitiesCallback
                        public void onValues(WifiStatus status, StaRoamingCapabilities caps) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            caps.writeToParcel(_hidl_reply);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 17:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    StaRoamingConfig config = new StaRoamingConfig();
                    config.readFromParcel(_hidl_request);
                    WifiStatus _hidl_out_status9 = configureRoaming(config);
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status9.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 18:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status10 = setRoamingState(_hidl_request.readInt8());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status10.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 19:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status11 = enableNdOffload(_hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status11.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 20:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    int cmdId2 = _hidl_request.readInt32();
                    ArrayList<Byte> ipPacketData = _hidl_request.readInt8Vector();
                    short etherType = _hidl_request.readInt16();
                    byte[] srcAddress = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, srcAddress, 6);
                    byte[] dstAddress = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, dstAddress, 6);
                    WifiStatus _hidl_out_status12 = startSendingKeepAlivePackets(cmdId2, ipPacketData, etherType, srcAddress, dstAddress, _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status12.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case ISupplicantStaIfaceCallback.ReasonCode.UNSUPPORTED_RSN_IE_VERSION /* 21 */:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status13 = stopSendingKeepAlivePackets(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status13.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 22:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    byte[] oui = new byte[3];
                    _hidl_request.readBuffer(3).copyToInt8Array(0, oui, 3);
                    WifiStatus _hidl_out_status14 = setScanningMacOui(oui);
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status14.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 23:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status15 = startDebugPacketFateMonitoring();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status15.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 24:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    getDebugTxPacketFates(new IWifiStaIface.getDebugTxPacketFatesCallback() {
                        /* class android.hardware.wifi.V1_3.IWifiStaIface.Stub.AnonymousClass9 */

                        @Override // android.hardware.wifi.V1_0.IWifiStaIface.getDebugTxPacketFatesCallback
                        public void onValues(WifiStatus status, ArrayList<WifiDebugTxPacketFateReport> fates) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            WifiDebugTxPacketFateReport.writeVectorToParcel(_hidl_reply, fates);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 25:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiStaIface.kInterfaceName);
                    getDebugRxPacketFates(new IWifiStaIface.getDebugRxPacketFatesCallback() {
                        /* class android.hardware.wifi.V1_3.IWifiStaIface.Stub.AnonymousClass10 */

                        @Override // android.hardware.wifi.V1_0.IWifiStaIface.getDebugRxPacketFatesCallback
                        public void onValues(WifiStatus status, ArrayList<WifiDebugRxPacketFateReport> fates) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            WifiDebugRxPacketFateReport.writeVectorToParcel(_hidl_reply, fates);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case ISupplicantStaIfaceCallback.ReasonCode.TDLS_TEARDOWN_UNSPECIFIED /* 26 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_2.IWifiStaIface.kInterfaceName);
                    readApfPacketFilterData(new IWifiStaIface.readApfPacketFilterDataCallback() {
                        /* class android.hardware.wifi.V1_3.IWifiStaIface.Stub.AnonymousClass11 */

                        @Override // android.hardware.wifi.V1_2.IWifiStaIface.readApfPacketFilterDataCallback
                        public void onValues(WifiStatus status, ArrayList<Byte> data) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt8Vector(data);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 27:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_2.IWifiStaIface.kInterfaceName);
                    byte[] mac = new byte[6];
                    _hidl_request.readBuffer(6).copyToInt8Array(0, mac, 6);
                    WifiStatus _hidl_out_status16 = setMacAddress(mac);
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status16.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 28:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    getLinkLayerStats_1_3(new getLinkLayerStats_1_3Callback() {
                        /* class android.hardware.wifi.V1_3.IWifiStaIface.Stub.AnonymousClass12 */

                        @Override // android.hardware.wifi.V1_3.IWifiStaIface.getLinkLayerStats_1_3Callback
                        public void onValues(WifiStatus status, StaLinkLayerStats stats) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            stats.writeToParcel(_hidl_reply);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 29:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    getFactoryMacAddress(new getFactoryMacAddressCallback() {
                        /* class android.hardware.wifi.V1_3.IWifiStaIface.Stub.AnonymousClass13 */

                        @Override // android.hardware.wifi.V1_3.IWifiStaIface.getFactoryMacAddressCallback
                        public void onValues(WifiStatus status, byte[] mac) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            HwBlob _hidl_blob = new HwBlob(6);
                            if (mac == null || mac.length != 6) {
                                throw new IllegalArgumentException("Array element is not of the expected length");
                            }
                            _hidl_blob.putInt8Array(0, mac);
                            _hidl_reply.writeBuffer(_hidl_blob);
                            _hidl_reply.send();
                        }
                    });
                    return;
                default:
                    switch (_hidl_code) {
                        case 256067662:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            ArrayList<String> _hidl_out_descriptors = interfaceChain();
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeStringVector(_hidl_out_descriptors);
                            _hidl_reply.send();
                            return;
                        case 256131655:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            debug(_hidl_request.readNativeHandle(), _hidl_request.readStringVector());
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.send();
                            return;
                        case 256136003:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            String _hidl_out_descriptor = interfaceDescriptor();
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeString(_hidl_out_descriptor);
                            _hidl_reply.send();
                            return;
                        case 256398152:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            ArrayList<byte[]> _hidl_out_hashchain = getHashChain();
                            _hidl_reply.writeStatus(0);
                            HwBlob _hidl_blob = new HwBlob(16);
                            int _hidl_vec_size = _hidl_out_hashchain.size();
                            _hidl_blob.putInt32(8, _hidl_vec_size);
                            _hidl_blob.putBool(12, false);
                            HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
                            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                                long _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
                                byte[] _hidl_array_item_1 = _hidl_out_hashchain.get(_hidl_index_0);
                                if (_hidl_array_item_1 == null || _hidl_array_item_1.length != 32) {
                                    throw new IllegalArgumentException("Array element is not of the expected length");
                                }
                                childBlob.putInt8Array(_hidl_array_offset_1, _hidl_array_item_1);
                            }
                            _hidl_blob.putBlob(0, childBlob);
                            _hidl_reply.writeBuffer(_hidl_blob);
                            _hidl_reply.send();
                            return;
                        case 256462420:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (!_hidl_is_oneway) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            setHALInstrumentation();
                            return;
                        case 256660548:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (_hidl_is_oneway) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            return;
                        case 256921159:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            ping();
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.send();
                            return;
                        case 257049926:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            DebugInfo _hidl_out_info = getDebugInfo();
                            _hidl_reply.writeStatus(0);
                            _hidl_out_info.writeToParcel(_hidl_reply);
                            _hidl_reply.send();
                            return;
                        case 257120595:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (!_hidl_is_oneway) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            notifySyspropsChanged();
                            return;
                        case 257250372:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (_hidl_is_oneway) {
                                _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                                _hidl_reply.send();
                                return;
                            }
                            return;
                        default:
                            return;
                    }
            }
        }
    }
}
