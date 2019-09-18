package android.hardware.wifi.V1_0;

import android.hardware.wifi.V1_0.IWifiIface;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
import android.hidl.base.V1_0.DebugInfo;
import android.hidl.base.V1_0.IBase;
import android.os.HidlSupport;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.RemoteException;
import com.android.server.wifi.HalDeviceManager;
import com.android.server.wifi.WifiLoggerHal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public interface IWifiStaIface extends IWifiIface {
    public static final String kInterfaceName = "android.hardware.wifi@1.0::IWifiStaIface";

    public static final class Proxy implements IWifiStaIface {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of android.hardware.wifi@1.0::IWifiStaIface]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

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

        public WifiStatus registerEventCallback(IWifiStaIfaceEventCallback callback) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public void getCapabilities(getCapabilitiesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public void getApfPacketFilterCapabilities(getApfPacketFilterCapabilitiesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public WifiStatus installApfPacketFilter(int cmdId, ArrayList<Byte> program) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public void getBackgroundScanCapabilities(getBackgroundScanCapabilitiesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public void getValidFrequenciesForBand(int band, getValidFrequenciesForBandCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public WifiStatus startBackgroundScan(int cmdId, StaBackgroundScanParameters params) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public WifiStatus stopBackgroundScan(int cmdId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public WifiStatus enableLinkLayerStatsCollection(boolean debug) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public WifiStatus disableLinkLayerStatsCollection() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public void getLinkLayerStats(getLinkLayerStatsCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public WifiStatus startRssiMonitoring(int cmdId, int maxRssi, int minRssi) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public WifiStatus stopRssiMonitoring(int cmdId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public void getRoamingCapabilities(getRoamingCapabilitiesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public WifiStatus configureRoaming(StaRoamingConfig config) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public WifiStatus setRoamingState(byte state) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public WifiStatus enableNdOffload(boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public WifiStatus startSendingKeepAlivePackets(int cmdId, ArrayList<Byte> ipPacketData, short etherType, byte[] srcAddress, byte[] dstAddress, int periodInMs) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
            _hidl_request.writeInt32(cmdId);
            _hidl_request.writeInt8Vector(ipPacketData);
            _hidl_request.writeInt16(etherType);
            HwBlob _hidl_blob = new HwBlob(6);
            _hidl_blob.putInt8Array(0, srcAddress);
            _hidl_request.writeBuffer(_hidl_blob);
            HwBlob _hidl_blob2 = new HwBlob(6);
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

        public WifiStatus stopSendingKeepAlivePackets(int cmdId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public WifiStatus setScanningMacOui(byte[] oui) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(3);
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

        public WifiStatus startDebugPacketFateMonitoring() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public void getDebugTxPacketFates(getDebugTxPacketFatesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public void getDebugRxPacketFates(getDebugRxPacketFatesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
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

        public ArrayList<byte[]> getHashChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                int _hidl_index_0 = 0;
                this.mRemote.transact(256398152, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<byte[]> _hidl_out_hashchain = new ArrayList<>();
                HwBlob _hidl_blob = _hidl_reply.readBuffer(16);
                int _hidl_vec_size = _hidl_blob.getInt32(8);
                HwBlob childBlob = _hidl_reply.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
                _hidl_out_hashchain.clear();
                while (true) {
                    int _hidl_index_02 = _hidl_index_0;
                    if (_hidl_index_02 >= _hidl_vec_size) {
                        return _hidl_out_hashchain;
                    }
                    byte[] _hidl_vec_element = new byte[32];
                    childBlob.copyToInt8Array((long) (_hidl_index_02 * 32), _hidl_vec_element, 32);
                    _hidl_out_hashchain.add(_hidl_vec_element);
                    _hidl_index_0 = _hidl_index_02 + 1;
                }
            } finally {
                _hidl_reply.release();
            }
        }

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

        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

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

        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static final class StaIfaceCapabilityMask {
        public static final int APF = 1;
        public static final int BACKGROUND_SCAN = 2;
        public static final int CONTROL_ROAMING = 16;
        public static final int DEBUG_PACKET_FATE = 16384;
        public static final int HOTSPOT = 256;
        public static final int KEEP_ALIVE = 8192;
        public static final int LINK_LAYER_STATS = 4;
        public static final int ND_OFFLOAD = 4096;
        public static final int PNO = 512;
        public static final int PROBE_IE_WHITELIST = 32;
        public static final int RSSI_MONITOR = 8;
        public static final int SCAN_RAND = 64;
        public static final int STA_5G = 128;
        public static final int TDLS = 1024;
        public static final int TDLS_OFFCHANNEL = 2048;

        public static final String toString(int o) {
            if (o == 1) {
                return "APF";
            }
            if (o == 2) {
                return "BACKGROUND_SCAN";
            }
            if (o == 4) {
                return "LINK_LAYER_STATS";
            }
            if (o == 8) {
                return "RSSI_MONITOR";
            }
            if (o == 16) {
                return "CONTROL_ROAMING";
            }
            if (o == 32) {
                return "PROBE_IE_WHITELIST";
            }
            if (o == 64) {
                return "SCAN_RAND";
            }
            if (o == 128) {
                return "STA_5G";
            }
            if (o == 256) {
                return "HOTSPOT";
            }
            if (o == 512) {
                return "PNO";
            }
            if (o == 1024) {
                return "TDLS";
            }
            if (o == 2048) {
                return "TDLS_OFFCHANNEL";
            }
            if (o == 4096) {
                return "ND_OFFLOAD";
            }
            if (o == 8192) {
                return "KEEP_ALIVE";
            }
            if (o == 16384) {
                return "DEBUG_PACKET_FATE";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            if ((o & 1) == 1) {
                list.add("APF");
                flipped = 0 | 1;
            }
            if ((o & 2) == 2) {
                list.add("BACKGROUND_SCAN");
                flipped |= 2;
            }
            if ((o & 4) == 4) {
                list.add("LINK_LAYER_STATS");
                flipped |= 4;
            }
            if ((o & 8) == 8) {
                list.add("RSSI_MONITOR");
                flipped |= 8;
            }
            if ((o & 16) == 16) {
                list.add("CONTROL_ROAMING");
                flipped |= 16;
            }
            if ((o & 32) == 32) {
                list.add("PROBE_IE_WHITELIST");
                flipped |= 32;
            }
            if ((o & 64) == 64) {
                list.add("SCAN_RAND");
                flipped |= 64;
            }
            if ((o & 128) == 128) {
                list.add("STA_5G");
                flipped |= 128;
            }
            if ((o & 256) == 256) {
                list.add("HOTSPOT");
                flipped |= 256;
            }
            if ((o & 512) == 512) {
                list.add("PNO");
                flipped |= 512;
            }
            if ((o & 1024) == 1024) {
                list.add("TDLS");
                flipped |= 1024;
            }
            if ((o & 2048) == 2048) {
                list.add("TDLS_OFFCHANNEL");
                flipped |= 2048;
            }
            if ((o & 4096) == 4096) {
                list.add("ND_OFFLOAD");
                flipped |= 4096;
            }
            if ((o & 8192) == 8192) {
                list.add("KEEP_ALIVE");
                flipped |= 8192;
            }
            if ((o & 16384) == 16384) {
                list.add("DEBUG_PACKET_FATE");
                flipped |= 16384;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static abstract class Stub extends HwBinder implements IWifiStaIface {
        public IHwBinder asBinder() {
            return this;
        }

        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(new String[]{IWifiStaIface.kInterfaceName, IWifiIface.kInterfaceName, IBase.kInterfaceName}));
        }

        public final String interfaceDescriptor() {
            return IWifiStaIface.kInterfaceName;
        }

        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[][]{new byte[]{59, Byte.MIN_VALUE, -109, -45, -98, -15, -31, 14, 67, -59, 83, -118, -5, -11, -1, 110, 57, -72, -40, 22, -114, -69, -31, -103, -115, -103, 62, -119, -30, 95, 20, -91}, new byte[]{107, -102, -44, 58, 94, -5, -26, -54, 33, 79, 117, 30, 34, -50, 67, -49, 92, -44, -43, -43, -14, -53, -88, 15, 36, -52, -45, 117, 90, 114, WifiLoggerHal.WIFI_ALERT_REASON_MAX, 28}, new byte[]{-67, -38, -74, 24, 77, 122, 52, 109, -90, -96, 125, -64, -126, -116, -15, -102, 105, 111, 76, -86, 54, 17, -59, 31, 46, 20, 86, 90, 20, -76, 15, -39}}));
        }

        public final void setHALInstrumentation() {
        }

        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        public final void ping() {
        }

        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

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

        public void onTransact(int _hidl_code, HwParcel _hidl_request, HwParcel _hidl_reply, int _hidl_flags) throws RemoteException {
            HwParcel hwParcel = _hidl_request;
            final HwParcel hwParcel2 = _hidl_reply;
            int _hidl_index_0 = 0;
            boolean _hidl_is_oneway = true;
            switch (_hidl_code) {
                case 1:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiIface.kInterfaceName);
                    getType(new IWifiIface.getTypeCallback() {
                        public void onValues(WifiStatus status, int type) {
                            hwParcel2.writeStatus(0);
                            status.writeToParcel(hwParcel2);
                            hwParcel2.writeInt32(type);
                            hwParcel2.send();
                        }
                    });
                    return;
                case 2:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiIface.kInterfaceName);
                    getName(new IWifiIface.getNameCallback() {
                        public void onValues(WifiStatus status, String name) {
                            hwParcel2.writeStatus(0);
                            status.writeToParcel(hwParcel2);
                            hwParcel2.writeString(name);
                            hwParcel2.send();
                        }
                    });
                    return;
                case 3:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status = registerEventCallback(IWifiStaIfaceEventCallback.asInterface(_hidl_request.readStrongBinder()));
                    hwParcel2.writeStatus(0);
                    _hidl_out_status.writeToParcel(hwParcel2);
                    _hidl_reply.send();
                    return;
                case 4:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    getCapabilities(new getCapabilitiesCallback() {
                        public void onValues(WifiStatus status, int capabilities) {
                            hwParcel2.writeStatus(0);
                            status.writeToParcel(hwParcel2);
                            hwParcel2.writeInt32(capabilities);
                            hwParcel2.send();
                        }
                    });
                    return;
                case 5:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    getApfPacketFilterCapabilities(new getApfPacketFilterCapabilitiesCallback() {
                        public void onValues(WifiStatus status, StaApfPacketFilterCapabilities capabilities) {
                            hwParcel2.writeStatus(0);
                            status.writeToParcel(hwParcel2);
                            capabilities.writeToParcel(hwParcel2);
                            hwParcel2.send();
                        }
                    });
                    return;
                case 6:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status2 = installApfPacketFilter(_hidl_request.readInt32(), _hidl_request.readInt8Vector());
                    hwParcel2.writeStatus(0);
                    _hidl_out_status2.writeToParcel(hwParcel2);
                    _hidl_reply.send();
                    return;
                case 7:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    getBackgroundScanCapabilities(new getBackgroundScanCapabilitiesCallback() {
                        public void onValues(WifiStatus status, StaBackgroundScanCapabilities capabilities) {
                            hwParcel2.writeStatus(0);
                            status.writeToParcel(hwParcel2);
                            capabilities.writeToParcel(hwParcel2);
                            hwParcel2.send();
                        }
                    });
                    return;
                case 8:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    getValidFrequenciesForBand(_hidl_request.readInt32(), new getValidFrequenciesForBandCallback() {
                        public void onValues(WifiStatus status, ArrayList<Integer> frequencies) {
                            hwParcel2.writeStatus(0);
                            status.writeToParcel(hwParcel2);
                            hwParcel2.writeInt32Vector(frequencies);
                            hwParcel2.send();
                        }
                    });
                    return;
                case 9:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    int cmdId = _hidl_request.readInt32();
                    StaBackgroundScanParameters params = new StaBackgroundScanParameters();
                    params.readFromParcel(hwParcel);
                    WifiStatus _hidl_out_status3 = startBackgroundScan(cmdId, params);
                    hwParcel2.writeStatus(0);
                    _hidl_out_status3.writeToParcel(hwParcel2);
                    _hidl_reply.send();
                    return;
                case 10:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status4 = stopBackgroundScan(_hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    _hidl_out_status4.writeToParcel(hwParcel2);
                    _hidl_reply.send();
                    return;
                case 11:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status5 = enableLinkLayerStatsCollection(_hidl_request.readBool());
                    hwParcel2.writeStatus(0);
                    _hidl_out_status5.writeToParcel(hwParcel2);
                    _hidl_reply.send();
                    return;
                case 12:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status6 = disableLinkLayerStatsCollection();
                    hwParcel2.writeStatus(0);
                    _hidl_out_status6.writeToParcel(hwParcel2);
                    _hidl_reply.send();
                    return;
                case 13:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    getLinkLayerStats(new getLinkLayerStatsCallback() {
                        public void onValues(WifiStatus status, StaLinkLayerStats stats) {
                            hwParcel2.writeStatus(0);
                            status.writeToParcel(hwParcel2);
                            stats.writeToParcel(hwParcel2);
                            hwParcel2.send();
                        }
                    });
                    return;
                case 14:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status7 = startRssiMonitoring(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    _hidl_out_status7.writeToParcel(hwParcel2);
                    _hidl_reply.send();
                    return;
                case 15:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status8 = stopRssiMonitoring(_hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    _hidl_out_status8.writeToParcel(hwParcel2);
                    _hidl_reply.send();
                    return;
                case 16:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    getRoamingCapabilities(new getRoamingCapabilitiesCallback() {
                        public void onValues(WifiStatus status, StaRoamingCapabilities caps) {
                            hwParcel2.writeStatus(0);
                            status.writeToParcel(hwParcel2);
                            caps.writeToParcel(hwParcel2);
                            hwParcel2.send();
                        }
                    });
                    return;
                case 17:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    StaRoamingConfig config = new StaRoamingConfig();
                    config.readFromParcel(hwParcel);
                    WifiStatus _hidl_out_status9 = configureRoaming(config);
                    hwParcel2.writeStatus(0);
                    _hidl_out_status9.writeToParcel(hwParcel2);
                    _hidl_reply.send();
                    return;
                case 18:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status10 = setRoamingState(_hidl_request.readInt8());
                    hwParcel2.writeStatus(0);
                    _hidl_out_status10.writeToParcel(hwParcel2);
                    _hidl_reply.send();
                    return;
                case 19:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status11 = enableNdOffload(_hidl_request.readBool());
                    hwParcel2.writeStatus(0);
                    _hidl_out_status11.writeToParcel(hwParcel2);
                    _hidl_reply.send();
                    return;
                case 20:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    int cmdId2 = _hidl_request.readInt32();
                    ArrayList<Byte> ipPacketData = _hidl_request.readInt8Vector();
                    short etherType = _hidl_request.readInt16();
                    byte[] srcAddress = new byte[6];
                    hwParcel.readBuffer(6).copyToInt8Array(0, srcAddress, 6);
                    byte[] dstAddress = new byte[6];
                    hwParcel.readBuffer(6).copyToInt8Array(0, dstAddress, 6);
                    byte[] bArr = dstAddress;
                    byte[] bArr2 = srcAddress;
                    WifiStatus _hidl_out_status12 = startSendingKeepAlivePackets(cmdId2, ipPacketData, etherType, srcAddress, dstAddress, _hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    _hidl_out_status12.writeToParcel(hwParcel2);
                    _hidl_reply.send();
                    return;
                case ISupplicantStaIfaceCallback.ReasonCode.UNSUPPORTED_RSN_IE_VERSION /*21*/:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status13 = stopSendingKeepAlivePackets(_hidl_request.readInt32());
                    hwParcel2.writeStatus(0);
                    _hidl_out_status13.writeToParcel(hwParcel2);
                    _hidl_reply.send();
                    return;
                case 22:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    byte[] oui = new byte[3];
                    hwParcel.readBuffer(3).copyToInt8Array(0, oui, 3);
                    WifiStatus _hidl_out_status14 = setScanningMacOui(oui);
                    hwParcel2.writeStatus(0);
                    _hidl_out_status14.writeToParcel(hwParcel2);
                    _hidl_reply.send();
                    return;
                case 23:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway = false;
                    }
                    if (_hidl_is_oneway) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    WifiStatus _hidl_out_status15 = startDebugPacketFateMonitoring();
                    hwParcel2.writeStatus(0);
                    _hidl_out_status15.writeToParcel(hwParcel2);
                    _hidl_reply.send();
                    return;
                case 24:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    getDebugTxPacketFates(new getDebugTxPacketFatesCallback() {
                        public void onValues(WifiStatus status, ArrayList<WifiDebugTxPacketFateReport> fates) {
                            hwParcel2.writeStatus(0);
                            status.writeToParcel(hwParcel2);
                            WifiDebugTxPacketFateReport.writeVectorToParcel(hwParcel2, fates);
                            hwParcel2.send();
                        }
                    });
                    return;
                case 25:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 0) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(IWifiStaIface.kInterfaceName);
                    getDebugRxPacketFates(new getDebugRxPacketFatesCallback() {
                        public void onValues(WifiStatus status, ArrayList<WifiDebugRxPacketFateReport> fates) {
                            hwParcel2.writeStatus(0);
                            status.writeToParcel(hwParcel2);
                            WifiDebugRxPacketFateReport.writeVectorToParcel(hwParcel2, fates);
                            hwParcel2.send();
                        }
                    });
                    return;
                default:
                    switch (_hidl_code) {
                        case 256067662:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
                                hwParcel2.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            hwParcel.enforceInterface(IBase.kInterfaceName);
                            ArrayList<String> _hidl_out_descriptors = interfaceChain();
                            hwParcel2.writeStatus(0);
                            hwParcel2.writeStringVector(_hidl_out_descriptors);
                            _hidl_reply.send();
                            return;
                        case 256131655:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
                                hwParcel2.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            hwParcel.enforceInterface(IBase.kInterfaceName);
                            hwParcel2.writeStatus(0);
                            _hidl_reply.send();
                            return;
                        case 256136003:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
                                hwParcel2.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            hwParcel.enforceInterface(IBase.kInterfaceName);
                            String _hidl_out_descriptor = interfaceDescriptor();
                            hwParcel2.writeStatus(0);
                            hwParcel2.writeString(_hidl_out_descriptor);
                            _hidl_reply.send();
                            return;
                        case 256398152:
                            if (_hidl_flags == false || !true) {
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
                                hwParcel2.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            hwParcel.enforceInterface(IBase.kInterfaceName);
                            ArrayList<byte[]> _hidl_out_hashchain = getHashChain();
                            hwParcel2.writeStatus(0);
                            HwBlob _hidl_blob = new HwBlob(16);
                            int _hidl_vec_size = _hidl_out_hashchain.size();
                            _hidl_blob.putInt32(8, _hidl_vec_size);
                            _hidl_blob.putBool(12, false);
                            HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
                            while (true) {
                                int _hidl_index_02 = _hidl_index_0;
                                if (_hidl_index_02 < _hidl_vec_size) {
                                    childBlob.putInt8Array((long) (_hidl_index_02 * 32), _hidl_out_hashchain.get(_hidl_index_02));
                                    _hidl_index_0 = _hidl_index_02 + 1;
                                } else {
                                    _hidl_blob.putBlob(0, childBlob);
                                    hwParcel2.writeBuffer(_hidl_blob);
                                    _hidl_reply.send();
                                    return;
                                }
                            }
                        case 256462420:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_index_0 = 1;
                            }
                            if (_hidl_index_0 != 1) {
                                hwParcel2.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            hwParcel.enforceInterface(IBase.kInterfaceName);
                            setHALInstrumentation();
                            return;
                        case 256660548:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_index_0 = 1;
                            }
                            if (_hidl_index_0 != 0) {
                                hwParcel2.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            return;
                        case 256921159:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
                                hwParcel2.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            hwParcel.enforceInterface(IBase.kInterfaceName);
                            ping();
                            hwParcel2.writeStatus(0);
                            _hidl_reply.send();
                            return;
                        case 257049926:
                            if (_hidl_flags == false || !true) {
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
                                hwParcel2.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            hwParcel.enforceInterface(IBase.kInterfaceName);
                            DebugInfo _hidl_out_info = getDebugInfo();
                            hwParcel2.writeStatus(0);
                            _hidl_out_info.writeToParcel(hwParcel2);
                            _hidl_reply.send();
                            return;
                        case 257120595:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_index_0 = 1;
                            }
                            if (_hidl_index_0 != 1) {
                                hwParcel2.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            hwParcel.enforceInterface(IBase.kInterfaceName);
                            notifySyspropsChanged();
                            return;
                        case 257250372:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_index_0 = 1;
                            }
                            if (_hidl_index_0 != 0) {
                                hwParcel2.writeStatus(Integer.MIN_VALUE);
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

    @FunctionalInterface
    public interface getApfPacketFilterCapabilitiesCallback {
        void onValues(WifiStatus wifiStatus, StaApfPacketFilterCapabilities staApfPacketFilterCapabilities);
    }

    @FunctionalInterface
    public interface getBackgroundScanCapabilitiesCallback {
        void onValues(WifiStatus wifiStatus, StaBackgroundScanCapabilities staBackgroundScanCapabilities);
    }

    @FunctionalInterface
    public interface getCapabilitiesCallback {
        void onValues(WifiStatus wifiStatus, int i);
    }

    @FunctionalInterface
    public interface getDebugRxPacketFatesCallback {
        void onValues(WifiStatus wifiStatus, ArrayList<WifiDebugRxPacketFateReport> arrayList);
    }

    @FunctionalInterface
    public interface getDebugTxPacketFatesCallback {
        void onValues(WifiStatus wifiStatus, ArrayList<WifiDebugTxPacketFateReport> arrayList);
    }

    @FunctionalInterface
    public interface getLinkLayerStatsCallback {
        void onValues(WifiStatus wifiStatus, StaLinkLayerStats staLinkLayerStats);
    }

    @FunctionalInterface
    public interface getRoamingCapabilitiesCallback {
        void onValues(WifiStatus wifiStatus, StaRoamingCapabilities staRoamingCapabilities);
    }

    @FunctionalInterface
    public interface getValidFrequenciesForBandCallback {
        void onValues(WifiStatus wifiStatus, ArrayList<Integer> arrayList);
    }

    IHwBinder asBinder();

    WifiStatus configureRoaming(StaRoamingConfig staRoamingConfig) throws RemoteException;

    WifiStatus disableLinkLayerStatsCollection() throws RemoteException;

    WifiStatus enableLinkLayerStatsCollection(boolean z) throws RemoteException;

    WifiStatus enableNdOffload(boolean z) throws RemoteException;

    void getApfPacketFilterCapabilities(getApfPacketFilterCapabilitiesCallback getapfpacketfiltercapabilitiescallback) throws RemoteException;

    void getBackgroundScanCapabilities(getBackgroundScanCapabilitiesCallback getbackgroundscancapabilitiescallback) throws RemoteException;

    void getCapabilities(getCapabilitiesCallback getcapabilitiescallback) throws RemoteException;

    DebugInfo getDebugInfo() throws RemoteException;

    void getDebugRxPacketFates(getDebugRxPacketFatesCallback getdebugrxpacketfatescallback) throws RemoteException;

    void getDebugTxPacketFates(getDebugTxPacketFatesCallback getdebugtxpacketfatescallback) throws RemoteException;

    ArrayList<byte[]> getHashChain() throws RemoteException;

    void getLinkLayerStats(getLinkLayerStatsCallback getlinklayerstatscallback) throws RemoteException;

    void getRoamingCapabilities(getRoamingCapabilitiesCallback getroamingcapabilitiescallback) throws RemoteException;

    void getValidFrequenciesForBand(int i, getValidFrequenciesForBandCallback getvalidfrequenciesforbandcallback) throws RemoteException;

    WifiStatus installApfPacketFilter(int i, ArrayList<Byte> arrayList) throws RemoteException;

    ArrayList<String> interfaceChain() throws RemoteException;

    String interfaceDescriptor() throws RemoteException;

    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    void notifySyspropsChanged() throws RemoteException;

    void ping() throws RemoteException;

    WifiStatus registerEventCallback(IWifiStaIfaceEventCallback iWifiStaIfaceEventCallback) throws RemoteException;

    void setHALInstrumentation() throws RemoteException;

    WifiStatus setRoamingState(byte b) throws RemoteException;

    WifiStatus setScanningMacOui(byte[] bArr) throws RemoteException;

    WifiStatus startBackgroundScan(int i, StaBackgroundScanParameters staBackgroundScanParameters) throws RemoteException;

    WifiStatus startDebugPacketFateMonitoring() throws RemoteException;

    WifiStatus startRssiMonitoring(int i, int i2, int i3) throws RemoteException;

    WifiStatus startSendingKeepAlivePackets(int i, ArrayList<Byte> arrayList, short s, byte[] bArr, byte[] bArr2, int i2) throws RemoteException;

    WifiStatus stopBackgroundScan(int i) throws RemoteException;

    WifiStatus stopRssiMonitoring(int i) throws RemoteException;

    WifiStatus stopSendingKeepAlivePackets(int i) throws RemoteException;

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
        return getService(HalDeviceManager.HAL_INSTANCE_NAME, retry);
    }

    static IWifiStaIface getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IWifiStaIface getService() throws RemoteException {
        return getService(HalDeviceManager.HAL_INSTANCE_NAME);
    }
}
