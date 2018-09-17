package android.hardware.wifi.V1_0;

import android.hardware.wifi.V1_0.IWifiIface.getNameCallback;
import android.hardware.wifi.V1_0.IWifiIface.getTypeCallback;
import android.hidl.base.V1_0.DebugInfo;
import android.hidl.base.V1_0.IBase;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwBinder.DeathRecipient;
import android.os.IHwInterface;
import android.os.RemoteException;
import android.os.SystemProperties;
import com.android.server.wifi.HalDeviceManager;
import com.android.server.wifi.WifiLoggerHal;
import java.util.ArrayList;
import java.util.Arrays;
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

        public void getType(getTypeCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiIface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getName(getNameCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiIface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        public WifiStatus registerEventCallback(IWifiStaIfaceEventCallback callback) throws RemoteException {
            IHwBinder iHwBinder = null;
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
            if (callback != null) {
                iHwBinder = callback.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
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

        public void getCapabilities(getCapabilitiesCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getApfPacketFilterCapabilities(getApfPacketFilterCapabilitiesCallback cb) throws RemoteException {
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
                cb.onValues(_hidl_out_status, _hidl_out_capabilities);
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

        public void getBackgroundScanCapabilities(getBackgroundScanCapabilitiesCallback cb) throws RemoteException {
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
                cb.onValues(_hidl_out_status, _hidl_out_capabilities);
            } finally {
                _hidl_reply.release();
            }
        }

        public void getValidFrequenciesForBand(int band, getValidFrequenciesForBandCallback cb) throws RemoteException {
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
                cb.onValues(_hidl_out_status, _hidl_reply.readInt32Vector());
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

        public void getLinkLayerStats(getLinkLayerStatsCallback cb) throws RemoteException {
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
                cb.onValues(_hidl_out_status, _hidl_out_stats);
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

        public void getRoamingCapabilities(getRoamingCapabilitiesCallback cb) throws RemoteException {
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
                cb.onValues(_hidl_out_status, _hidl_out_caps);
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
            int _hidl_index_0_0;
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
            _hidl_request.writeInt32(cmdId);
            _hidl_request.writeInt8Vector(ipPacketData);
            _hidl_request.writeInt16(etherType);
            HwBlob _hidl_blob = new HwBlob(6);
            long _hidl_array_offset_0 = 0;
            for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
                _hidl_blob.putInt8(_hidl_array_offset_0, srcAddress[_hidl_index_0_0]);
                _hidl_array_offset_0++;
            }
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_blob = new HwBlob(6);
            _hidl_array_offset_0 = 0;
            for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
                _hidl_blob.putInt8(_hidl_array_offset_0, dstAddress[_hidl_index_0_0]);
                _hidl_array_offset_0++;
            }
            _hidl_request.writeBuffer(_hidl_blob);
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
            long _hidl_array_offset_0 = 0;
            for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 3; _hidl_index_0_0++) {
                _hidl_blob.putInt8(_hidl_array_offset_0, oui[_hidl_index_0_0]);
                _hidl_array_offset_0++;
            }
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

        public void getDebugTxPacketFates(getDebugTxPacketFatesCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(24, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, WifiDebugTxPacketFateReport.readVectorFromParcel(_hidl_reply));
            } finally {
                _hidl_reply.release();
            }
        }

        public void getDebugRxPacketFates(getDebugRxPacketFatesCallback cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiStaIface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(25, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                cb.onValues(_hidl_out_status, WifiDebugRxPacketFateReport.readVectorFromParcel(_hidl_reply));
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
                ArrayList<String> _hidl_out_descriptors = _hidl_reply.readStringVector();
                return _hidl_out_descriptors;
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
                String _hidl_out_descriptor = _hidl_reply.readString();
                return _hidl_out_descriptor;
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<byte[]> getHashChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256398152, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<byte[]> _hidl_out_hashchain = new ArrayList();
                HwBlob _hidl_blob = _hidl_reply.readBuffer(16);
                int _hidl_vec_size = _hidl_blob.getInt32(8);
                HwBlob childBlob = _hidl_reply.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
                _hidl_out_hashchain.clear();
                for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                    Object _hidl_vec_element = new byte[32];
                    long _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
                    for (int _hidl_index_1_0 = 0; _hidl_index_1_0 < 32; _hidl_index_1_0++) {
                        _hidl_vec_element[_hidl_index_1_0] = childBlob.getInt8(_hidl_array_offset_1);
                        _hidl_array_offset_1++;
                    }
                    _hidl_out_hashchain.add(_hidl_vec_element);
                }
                return _hidl_out_hashchain;
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

        public boolean linkToDeath(DeathRecipient recipient, long cookie) throws RemoteException {
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

        public boolean unlinkToDeath(DeathRecipient recipient) throws RemoteException {
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
            if (o == TDLS) {
                return "TDLS";
            }
            if (o == TDLS_OFFCHANNEL) {
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
            ArrayList<String> list = new ArrayList();
            int flipped = 0;
            if ((o & 1) == 1) {
                list.add("APF");
                flipped = 1;
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
            if ((o & TDLS) == TDLS) {
                list.add("TDLS");
                flipped |= TDLS;
            }
            if ((o & TDLS_OFFCHANNEL) == TDLS_OFFCHANNEL) {
                list.add("TDLS_OFFCHANNEL");
                flipped |= TDLS_OFFCHANNEL;
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

    public interface getDebugRxPacketFatesCallback {
        void onValues(WifiStatus wifiStatus, ArrayList<WifiDebugRxPacketFateReport> arrayList);
    }

    public interface getCapabilitiesCallback {
        void onValues(WifiStatus wifiStatus, int i);
    }

    public interface getApfPacketFilterCapabilitiesCallback {
        void onValues(WifiStatus wifiStatus, StaApfPacketFilterCapabilities staApfPacketFilterCapabilities);
    }

    public interface getBackgroundScanCapabilitiesCallback {
        void onValues(WifiStatus wifiStatus, StaBackgroundScanCapabilities staBackgroundScanCapabilities);
    }

    public interface getValidFrequenciesForBandCallback {
        void onValues(WifiStatus wifiStatus, ArrayList<Integer> arrayList);
    }

    public interface getLinkLayerStatsCallback {
        void onValues(WifiStatus wifiStatus, StaLinkLayerStats staLinkLayerStats);
    }

    public interface getRoamingCapabilitiesCallback {
        void onValues(WifiStatus wifiStatus, StaRoamingCapabilities staRoamingCapabilities);
    }

    public interface getDebugTxPacketFatesCallback {
        void onValues(WifiStatus wifiStatus, ArrayList<WifiDebugTxPacketFateReport> arrayList);
    }

    public static abstract class Stub extends HwBinder implements IWifiStaIface {
        public IHwBinder asBinder() {
            return this;
        }

        public final ArrayList<String> interfaceChain() {
            return new ArrayList(Arrays.asList(new String[]{IWifiStaIface.kInterfaceName, IWifiIface.kInterfaceName, IBase.kInterfaceName}));
        }

        public final String interfaceDescriptor() {
            return IWifiStaIface.kInterfaceName;
        }

        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList(Arrays.asList(new byte[][]{new byte[]{(byte) 59, Byte.MIN_VALUE, (byte) -109, (byte) -45, (byte) -98, (byte) -15, (byte) -31, (byte) 14, (byte) 67, (byte) -59, (byte) 83, (byte) -118, (byte) -5, (byte) -11, (byte) -1, (byte) 110, (byte) 57, (byte) -72, (byte) -40, (byte) 22, (byte) -114, (byte) -69, (byte) -31, (byte) -103, (byte) -115, (byte) -103, (byte) 62, (byte) -119, (byte) -30, (byte) 95, (byte) 20, (byte) -91}, new byte[]{(byte) 107, (byte) -102, (byte) -44, (byte) 58, (byte) 94, (byte) -5, (byte) -26, (byte) -54, (byte) 33, (byte) 79, (byte) 117, (byte) 30, (byte) 34, (byte) -50, (byte) 67, (byte) -49, (byte) 92, (byte) -44, (byte) -43, (byte) -43, (byte) -14, (byte) -53, (byte) -88, (byte) 15, (byte) 36, (byte) -52, (byte) -45, (byte) 117, (byte) 90, (byte) 114, WifiLoggerHal.WIFI_ALERT_REASON_MAX, (byte) 28}, new byte[]{(byte) -67, (byte) -38, (byte) -74, (byte) 24, (byte) 77, (byte) 122, (byte) 52, (byte) 109, (byte) -90, (byte) -96, (byte) 125, (byte) -64, (byte) -126, (byte) -116, (byte) -15, (byte) -102, (byte) 105, (byte) 111, (byte) 76, (byte) -86, (byte) 54, (byte) 17, (byte) -59, (byte) 31, (byte) 46, (byte) 20, (byte) 86, (byte) 90, (byte) 20, (byte) -76, (byte) 15, (byte) -39}}));
        }

        public final void setHALInstrumentation() {
        }

        public final boolean linkToDeath(DeathRecipient recipient, long cookie) {
            return true;
        }

        public final void ping() {
        }

        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = -1;
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        public final void notifySyspropsChanged() {
            SystemProperties.reportSyspropChanged();
        }

        public final boolean unlinkToDeath(DeathRecipient recipient) {
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
            final HwParcel hwParcel;
            WifiStatus _hidl_out_status;
            int cmdId;
            HwBlob _hidl_blob;
            long _hidl_array_offset_0;
            int _hidl_index_0_0;
            switch (_hidl_code) {
                case 1:
                    _hidl_request.enforceInterface(IWifiIface.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getType(new getTypeCallback() {
                        public void onValues(WifiStatus status, int type) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeInt32(type);
                            hwParcel.send();
                        }
                    });
                    return;
                case 2:
                    _hidl_request.enforceInterface(IWifiIface.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getName(new getNameCallback() {
                        public void onValues(WifiStatus status, String name) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeString(name);
                            hwParcel.send();
                        }
                    });
                    return;
                case 3:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    _hidl_out_status = registerEventCallback(IWifiStaIfaceEventCallback.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 4:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getCapabilities(new getCapabilitiesCallback() {
                        public void onValues(WifiStatus status, int capabilities) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeInt32(capabilities);
                            hwParcel.send();
                        }
                    });
                    return;
                case 5:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getApfPacketFilterCapabilities(new getApfPacketFilterCapabilitiesCallback() {
                        public void onValues(WifiStatus status, StaApfPacketFilterCapabilities capabilities) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            capabilities.writeToParcel(hwParcel);
                            hwParcel.send();
                        }
                    });
                    return;
                case 6:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    _hidl_out_status = installApfPacketFilter(_hidl_request.readInt32(), _hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 7:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getBackgroundScanCapabilities(new getBackgroundScanCapabilitiesCallback() {
                        public void onValues(WifiStatus status, StaBackgroundScanCapabilities capabilities) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            capabilities.writeToParcel(hwParcel);
                            hwParcel.send();
                        }
                    });
                    return;
                case 8:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getValidFrequenciesForBand(_hidl_request.readInt32(), new getValidFrequenciesForBandCallback() {
                        public void onValues(WifiStatus status, ArrayList<Integer> frequencies) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            hwParcel.writeInt32Vector(frequencies);
                            hwParcel.send();
                        }
                    });
                    return;
                case 9:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    cmdId = _hidl_request.readInt32();
                    StaBackgroundScanParameters params = new StaBackgroundScanParameters();
                    params.readFromParcel(_hidl_request);
                    _hidl_out_status = startBackgroundScan(cmdId, params);
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 10:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    _hidl_out_status = stopBackgroundScan(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 11:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    _hidl_out_status = enableLinkLayerStatsCollection(_hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 12:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    _hidl_out_status = disableLinkLayerStatsCollection();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 13:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getLinkLayerStats(new getLinkLayerStatsCallback() {
                        public void onValues(WifiStatus status, StaLinkLayerStats stats) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            stats.writeToParcel(hwParcel);
                            hwParcel.send();
                        }
                    });
                    return;
                case 14:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    _hidl_out_status = startRssiMonitoring(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 15:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    _hidl_out_status = stopRssiMonitoring(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 16:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getRoamingCapabilities(new getRoamingCapabilitiesCallback() {
                        public void onValues(WifiStatus status, StaRoamingCapabilities caps) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            caps.writeToParcel(hwParcel);
                            hwParcel.send();
                        }
                    });
                    return;
                case 17:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    StaRoamingConfig config = new StaRoamingConfig();
                    config.readFromParcel(_hidl_request);
                    _hidl_out_status = configureRoaming(config);
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 18:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    _hidl_out_status = setRoamingState(_hidl_request.readInt8());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 19:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    _hidl_out_status = enableNdOffload(_hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 20:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    cmdId = _hidl_request.readInt32();
                    ArrayList<Byte> ipPacketData = _hidl_request.readInt8Vector();
                    short etherType = _hidl_request.readInt16();
                    byte[] srcAddress = new byte[6];
                    _hidl_blob = _hidl_request.readBuffer(6);
                    _hidl_array_offset_0 = 0;
                    for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
                        srcAddress[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
                        _hidl_array_offset_0++;
                    }
                    byte[] dstAddress = new byte[6];
                    _hidl_blob = _hidl_request.readBuffer(6);
                    _hidl_array_offset_0 = 0;
                    for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
                        dstAddress[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
                        _hidl_array_offset_0++;
                    }
                    _hidl_out_status = startSendingKeepAlivePackets(cmdId, ipPacketData, etherType, srcAddress, dstAddress, _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 21:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    _hidl_out_status = stopSendingKeepAlivePackets(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 22:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    byte[] oui = new byte[3];
                    _hidl_blob = _hidl_request.readBuffer(3);
                    _hidl_array_offset_0 = 0;
                    for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 3; _hidl_index_0_0++) {
                        oui[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
                        _hidl_array_offset_0++;
                    }
                    _hidl_out_status = setScanningMacOui(oui);
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 23:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    _hidl_out_status = startDebugPacketFateMonitoring();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 24:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getDebugTxPacketFates(new getDebugTxPacketFatesCallback() {
                        public void onValues(WifiStatus status, ArrayList<WifiDebugTxPacketFateReport> fates) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            WifiDebugTxPacketFateReport.writeVectorToParcel(hwParcel, fates);
                            hwParcel.send();
                        }
                    });
                    return;
                case 25:
                    _hidl_request.enforceInterface(IWifiStaIface.kInterfaceName);
                    hwParcel = _hidl_reply;
                    getDebugRxPacketFates(new getDebugRxPacketFatesCallback() {
                        public void onValues(WifiStatus status, ArrayList<WifiDebugRxPacketFateReport> fates) {
                            hwParcel.writeStatus(0);
                            status.writeToParcel(hwParcel);
                            WifiDebugRxPacketFateReport.writeVectorToParcel(hwParcel, fates);
                            hwParcel.send();
                        }
                    });
                    return;
                case 256067662:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    ArrayList<String> _hidl_out_descriptors = interfaceChain();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeStringVector(_hidl_out_descriptors);
                    _hidl_reply.send();
                    return;
                case 256131655:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 256136003:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    String _hidl_out_descriptor = interfaceDescriptor();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeString(_hidl_out_descriptor);
                    _hidl_reply.send();
                    return;
                case 256398152:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    ArrayList<byte[]> _hidl_out_hashchain = getHashChain();
                    _hidl_reply.writeStatus(0);
                    _hidl_blob = new HwBlob(16);
                    int _hidl_vec_size = _hidl_out_hashchain.size();
                    _hidl_blob.putInt32(8, _hidl_vec_size);
                    _hidl_blob.putBool(12, false);
                    HwBlob hwBlob = new HwBlob(_hidl_vec_size * 32);
                    for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                        long _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
                        for (int _hidl_index_1_0 = 0; _hidl_index_1_0 < 32; _hidl_index_1_0++) {
                            hwBlob.putInt8(_hidl_array_offset_1, ((byte[]) _hidl_out_hashchain.get(_hidl_index_0))[_hidl_index_1_0]);
                            _hidl_array_offset_1++;
                        }
                    }
                    _hidl_blob.putBlob(0, hwBlob);
                    _hidl_reply.writeBuffer(_hidl_blob);
                    _hidl_reply.send();
                    return;
                case 256462420:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    setHALInstrumentation();
                    return;
                case 257049926:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    DebugInfo _hidl_out_info = getDebugInfo();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_info.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 257120595:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    notifySyspropsChanged();
                    return;
                default:
                    return;
            }
        }
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

    boolean linkToDeath(DeathRecipient deathRecipient, long j) throws RemoteException;

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

    boolean unlinkToDeath(DeathRecipient deathRecipient) throws RemoteException;

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
            for (String descriptor : proxy.interfaceChain()) {
                if (descriptor.equals(kInterfaceName)) {
                    return proxy;
                }
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    static IWifiStaIface castFrom(IHwInterface iface) {
        return iface == null ? null : asInterface(iface.asBinder());
    }

    static IWifiStaIface getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IWifiStaIface getService() throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, HalDeviceManager.HAL_INSTANCE_NAME));
    }
}
