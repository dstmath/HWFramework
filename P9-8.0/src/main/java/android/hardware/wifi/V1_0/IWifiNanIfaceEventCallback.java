package android.hardware.wifi.V1_0;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public interface IWifiNanIfaceEventCallback extends IBase {
    public static final String kInterfaceName = "android.hardware.wifi@1.0::IWifiNanIfaceEventCallback";

    public static final class Proxy implements IWifiNanIfaceEventCallback {
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
                return "[class or subclass of android.hardware.wifi@1.0::IWifiNanIfaceEventCallback]@Proxy";
            }
        }

        public void notifyCapabilitiesResponse(short id, WifiNanStatus status, NanCapabilities capabilities) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            _hidl_request.writeInt16(id);
            status.writeToParcel(_hidl_request);
            capabilities.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifyEnableResponse(short id, WifiNanStatus status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            _hidl_request.writeInt16(id);
            status.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifyConfigResponse(short id, WifiNanStatus status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            _hidl_request.writeInt16(id);
            status.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifyDisableResponse(short id, WifiNanStatus status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            _hidl_request.writeInt16(id);
            status.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifyStartPublishResponse(short id, WifiNanStatus status, byte sessionId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            _hidl_request.writeInt16(id);
            status.writeToParcel(_hidl_request);
            _hidl_request.writeInt8(sessionId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifyStopPublishResponse(short id, WifiNanStatus status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            _hidl_request.writeInt16(id);
            status.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifyStartSubscribeResponse(short id, WifiNanStatus status, byte sessionId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            _hidl_request.writeInt16(id);
            status.writeToParcel(_hidl_request);
            _hidl_request.writeInt8(sessionId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifyStopSubscribeResponse(short id, WifiNanStatus status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            _hidl_request.writeInt16(id);
            status.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifyTransmitFollowupResponse(short id, WifiNanStatus status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            _hidl_request.writeInt16(id);
            status.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifyCreateDataInterfaceResponse(short id, WifiNanStatus status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            _hidl_request.writeInt16(id);
            status.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifyDeleteDataInterfaceResponse(short id, WifiNanStatus status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            _hidl_request.writeInt16(id);
            status.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifyInitiateDataPathResponse(short id, WifiNanStatus status, int ndpInstanceId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            _hidl_request.writeInt16(id);
            status.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(ndpInstanceId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifyRespondToDataPathIndicationResponse(short id, WifiNanStatus status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            _hidl_request.writeInt16(id);
            status.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifyTerminateDataPathResponse(short id, WifiNanStatus status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            _hidl_request.writeInt16(id);
            status.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void eventClusterEvent(NanClusterEventInd event) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            event.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void eventDisabled(WifiNanStatus status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            status.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void eventPublishTerminated(byte sessionId, WifiNanStatus status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            _hidl_request.writeInt8(sessionId);
            status.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void eventSubscribeTerminated(byte sessionId, WifiNanStatus status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            _hidl_request.writeInt8(sessionId);
            status.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(18, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void eventMatch(NanMatchInd event) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            event.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(19, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void eventMatchExpired(byte discoverySessionId, int peerId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            _hidl_request.writeInt8(discoverySessionId);
            _hidl_request.writeInt32(peerId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(20, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void eventFollowupReceived(NanFollowupReceivedInd event) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            event.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(21, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void eventTransmitFollowup(short id, WifiNanStatus status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            _hidl_request.writeInt16(id);
            status.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(22, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void eventDataPathRequest(NanDataPathRequestInd event) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            event.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(23, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void eventDataPathConfirm(NanDataPathConfirmInd event) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            event.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(24, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void eventDataPathTerminated(int ndpInstanceId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiNanIfaceEventCallback.kInterfaceName);
            _hidl_request.writeInt32(ndpInstanceId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(25, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
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

    public static abstract class Stub extends HwBinder implements IWifiNanIfaceEventCallback {
        public IHwBinder asBinder() {
            return this;
        }

        public final ArrayList<String> interfaceChain() {
            return new ArrayList(Arrays.asList(new String[]{IWifiNanIfaceEventCallback.kInterfaceName, IBase.kInterfaceName}));
        }

        public final String interfaceDescriptor() {
            return IWifiNanIfaceEventCallback.kInterfaceName;
        }

        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList(Arrays.asList(new byte[][]{new byte[]{(byte) 50, (byte) 92, (byte) -108, (byte) -13, (byte) -31, (byte) -91, (byte) 101, (byte) -75, (byte) 107, (byte) -68, (byte) 116, (byte) -6, (byte) -35, (byte) -67, (byte) 11, (byte) -89, (byte) -53, (byte) -126, (byte) 79, (byte) 38, (byte) 61, (byte) -52, (byte) -7, (byte) -33, (byte) -1, (byte) 45, (byte) -81, (byte) 98, (byte) -72, (byte) 110, (byte) -41, (byte) 116}, new byte[]{(byte) -67, (byte) -38, (byte) -74, (byte) 24, (byte) 77, (byte) 122, (byte) 52, (byte) 109, (byte) -90, (byte) -96, (byte) 125, (byte) -64, (byte) -126, (byte) -116, (byte) -15, (byte) -102, (byte) 105, (byte) 111, (byte) 76, (byte) -86, (byte) 54, (byte) 17, (byte) -59, (byte) 31, (byte) 46, (byte) 20, (byte) 86, (byte) 90, (byte) 20, (byte) -76, (byte) 15, (byte) -39}}));
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
            if (IWifiNanIfaceEventCallback.kInterfaceName.equals(descriptor)) {
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
            short id;
            WifiNanStatus status;
            byte sessionId;
            switch (_hidl_code) {
                case 1:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    id = _hidl_request.readInt16();
                    status = new WifiNanStatus();
                    status.readFromParcel(_hidl_request);
                    NanCapabilities capabilities = new NanCapabilities();
                    capabilities.readFromParcel(_hidl_request);
                    notifyCapabilitiesResponse(id, status, capabilities);
                    return;
                case 2:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    id = _hidl_request.readInt16();
                    status = new WifiNanStatus();
                    status.readFromParcel(_hidl_request);
                    notifyEnableResponse(id, status);
                    return;
                case 3:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    id = _hidl_request.readInt16();
                    status = new WifiNanStatus();
                    status.readFromParcel(_hidl_request);
                    notifyConfigResponse(id, status);
                    return;
                case 4:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    id = _hidl_request.readInt16();
                    status = new WifiNanStatus();
                    status.readFromParcel(_hidl_request);
                    notifyDisableResponse(id, status);
                    return;
                case 5:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    id = _hidl_request.readInt16();
                    status = new WifiNanStatus();
                    status.readFromParcel(_hidl_request);
                    notifyStartPublishResponse(id, status, _hidl_request.readInt8());
                    return;
                case 6:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    id = _hidl_request.readInt16();
                    status = new WifiNanStatus();
                    status.readFromParcel(_hidl_request);
                    notifyStopPublishResponse(id, status);
                    return;
                case 7:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    id = _hidl_request.readInt16();
                    status = new WifiNanStatus();
                    status.readFromParcel(_hidl_request);
                    notifyStartSubscribeResponse(id, status, _hidl_request.readInt8());
                    return;
                case 8:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    id = _hidl_request.readInt16();
                    status = new WifiNanStatus();
                    status.readFromParcel(_hidl_request);
                    notifyStopSubscribeResponse(id, status);
                    return;
                case 9:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    id = _hidl_request.readInt16();
                    status = new WifiNanStatus();
                    status.readFromParcel(_hidl_request);
                    notifyTransmitFollowupResponse(id, status);
                    return;
                case 10:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    id = _hidl_request.readInt16();
                    status = new WifiNanStatus();
                    status.readFromParcel(_hidl_request);
                    notifyCreateDataInterfaceResponse(id, status);
                    return;
                case 11:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    id = _hidl_request.readInt16();
                    status = new WifiNanStatus();
                    status.readFromParcel(_hidl_request);
                    notifyDeleteDataInterfaceResponse(id, status);
                    return;
                case 12:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    id = _hidl_request.readInt16();
                    status = new WifiNanStatus();
                    status.readFromParcel(_hidl_request);
                    notifyInitiateDataPathResponse(id, status, _hidl_request.readInt32());
                    return;
                case 13:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    id = _hidl_request.readInt16();
                    status = new WifiNanStatus();
                    status.readFromParcel(_hidl_request);
                    notifyRespondToDataPathIndicationResponse(id, status);
                    return;
                case 14:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    id = _hidl_request.readInt16();
                    status = new WifiNanStatus();
                    status.readFromParcel(_hidl_request);
                    notifyTerminateDataPathResponse(id, status);
                    return;
                case 15:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    NanClusterEventInd event = new NanClusterEventInd();
                    event.readFromParcel(_hidl_request);
                    eventClusterEvent(event);
                    return;
                case 16:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    status = new WifiNanStatus();
                    status.readFromParcel(_hidl_request);
                    eventDisabled(status);
                    return;
                case 17:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    sessionId = _hidl_request.readInt8();
                    status = new WifiNanStatus();
                    status.readFromParcel(_hidl_request);
                    eventPublishTerminated(sessionId, status);
                    return;
                case 18:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    sessionId = _hidl_request.readInt8();
                    status = new WifiNanStatus();
                    status.readFromParcel(_hidl_request);
                    eventSubscribeTerminated(sessionId, status);
                    return;
                case 19:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    NanMatchInd event2 = new NanMatchInd();
                    event2.readFromParcel(_hidl_request);
                    eventMatch(event2);
                    return;
                case 20:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    eventMatchExpired(_hidl_request.readInt8(), _hidl_request.readInt32());
                    return;
                case 21:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    NanFollowupReceivedInd event3 = new NanFollowupReceivedInd();
                    event3.readFromParcel(_hidl_request);
                    eventFollowupReceived(event3);
                    return;
                case 22:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    id = _hidl_request.readInt16();
                    status = new WifiNanStatus();
                    status.readFromParcel(_hidl_request);
                    eventTransmitFollowup(id, status);
                    return;
                case 23:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    NanDataPathRequestInd event4 = new NanDataPathRequestInd();
                    event4.readFromParcel(_hidl_request);
                    eventDataPathRequest(event4);
                    return;
                case 24:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    NanDataPathConfirmInd event5 = new NanDataPathConfirmInd();
                    event5.readFromParcel(_hidl_request);
                    eventDataPathConfirm(event5);
                    return;
                case 25:
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    eventDataPathTerminated(_hidl_request.readInt32());
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
                    HwBlob _hidl_blob = new HwBlob(16);
                    int _hidl_vec_size = _hidl_out_hashchain.size();
                    _hidl_blob.putInt32(8, _hidl_vec_size);
                    _hidl_blob.putBool(12, false);
                    HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
                    for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                        long _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
                        for (int _hidl_index_1_0 = 0; _hidl_index_1_0 < 32; _hidl_index_1_0++) {
                            childBlob.putInt8(_hidl_array_offset_1, ((byte[]) _hidl_out_hashchain.get(_hidl_index_0))[_hidl_index_1_0]);
                            _hidl_array_offset_1++;
                        }
                    }
                    _hidl_blob.putBlob(0, childBlob);
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

    void eventClusterEvent(NanClusterEventInd nanClusterEventInd) throws RemoteException;

    void eventDataPathConfirm(NanDataPathConfirmInd nanDataPathConfirmInd) throws RemoteException;

    void eventDataPathRequest(NanDataPathRequestInd nanDataPathRequestInd) throws RemoteException;

    void eventDataPathTerminated(int i) throws RemoteException;

    void eventDisabled(WifiNanStatus wifiNanStatus) throws RemoteException;

    void eventFollowupReceived(NanFollowupReceivedInd nanFollowupReceivedInd) throws RemoteException;

    void eventMatch(NanMatchInd nanMatchInd) throws RemoteException;

    void eventMatchExpired(byte b, int i) throws RemoteException;

    void eventPublishTerminated(byte b, WifiNanStatus wifiNanStatus) throws RemoteException;

    void eventSubscribeTerminated(byte b, WifiNanStatus wifiNanStatus) throws RemoteException;

    void eventTransmitFollowup(short s, WifiNanStatus wifiNanStatus) throws RemoteException;

    DebugInfo getDebugInfo() throws RemoteException;

    ArrayList<byte[]> getHashChain() throws RemoteException;

    ArrayList<String> interfaceChain() throws RemoteException;

    String interfaceDescriptor() throws RemoteException;

    boolean linkToDeath(DeathRecipient deathRecipient, long j) throws RemoteException;

    void notifyCapabilitiesResponse(short s, WifiNanStatus wifiNanStatus, NanCapabilities nanCapabilities) throws RemoteException;

    void notifyConfigResponse(short s, WifiNanStatus wifiNanStatus) throws RemoteException;

    void notifyCreateDataInterfaceResponse(short s, WifiNanStatus wifiNanStatus) throws RemoteException;

    void notifyDeleteDataInterfaceResponse(short s, WifiNanStatus wifiNanStatus) throws RemoteException;

    void notifyDisableResponse(short s, WifiNanStatus wifiNanStatus) throws RemoteException;

    void notifyEnableResponse(short s, WifiNanStatus wifiNanStatus) throws RemoteException;

    void notifyInitiateDataPathResponse(short s, WifiNanStatus wifiNanStatus, int i) throws RemoteException;

    void notifyRespondToDataPathIndicationResponse(short s, WifiNanStatus wifiNanStatus) throws RemoteException;

    void notifyStartPublishResponse(short s, WifiNanStatus wifiNanStatus, byte b) throws RemoteException;

    void notifyStartSubscribeResponse(short s, WifiNanStatus wifiNanStatus, byte b) throws RemoteException;

    void notifyStopPublishResponse(short s, WifiNanStatus wifiNanStatus) throws RemoteException;

    void notifyStopSubscribeResponse(short s, WifiNanStatus wifiNanStatus) throws RemoteException;

    void notifySyspropsChanged() throws RemoteException;

    void notifyTerminateDataPathResponse(short s, WifiNanStatus wifiNanStatus) throws RemoteException;

    void notifyTransmitFollowupResponse(short s, WifiNanStatus wifiNanStatus) throws RemoteException;

    void ping() throws RemoteException;

    void setHALInstrumentation() throws RemoteException;

    boolean unlinkToDeath(DeathRecipient deathRecipient) throws RemoteException;

    static IWifiNanIfaceEventCallback asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IWifiNanIfaceEventCallback)) {
            return (IWifiNanIfaceEventCallback) iface;
        }
        IWifiNanIfaceEventCallback proxy = new Proxy(binder);
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

    static IWifiNanIfaceEventCallback castFrom(IHwInterface iface) {
        return iface == null ? null : asInterface(iface.asBinder());
    }

    static IWifiNanIfaceEventCallback getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IWifiNanIfaceEventCallback getService() throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, HalDeviceManager.HAL_INSTANCE_NAME));
    }
}
