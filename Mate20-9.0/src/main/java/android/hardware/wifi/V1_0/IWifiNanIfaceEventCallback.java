package android.hardware.wifi.V1_0;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
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

    public static abstract class Stub extends HwBinder implements IWifiNanIfaceEventCallback {
        public IHwBinder asBinder() {
            return this;
        }

        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(new String[]{IWifiNanIfaceEventCallback.kInterfaceName, IBase.kInterfaceName}));
        }

        public final String interfaceDescriptor() {
            return IWifiNanIfaceEventCallback.kInterfaceName;
        }

        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[][]{new byte[]{50, 92, -108, -13, -31, -91, 101, -75, 107, -68, 116, -6, -35, -67, 11, -89, -53, -126, 79, 38, 61, -52, -7, -33, -1, 45, -81, 98, -72, 110, -41, 116}, new byte[]{-67, -38, -74, 24, 77, 122, 52, 109, -90, -96, 125, -64, -126, -116, -15, -102, 105, 111, 76, -86, 54, 17, -59, 31, 46, 20, 86, 90, 20, -76, 15, -39}}));
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
            int _hidl_index_0 = 0;
            boolean _hidl_is_oneway = true;
            switch (_hidl_code) {
                case 1:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    short id = _hidl_request.readInt16();
                    WifiNanStatus status = new WifiNanStatus();
                    status.readFromParcel(_hidl_request);
                    NanCapabilities capabilities = new NanCapabilities();
                    capabilities.readFromParcel(_hidl_request);
                    notifyCapabilitiesResponse(id, status, capabilities);
                    return;
                case 2:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    short id2 = _hidl_request.readInt16();
                    WifiNanStatus status2 = new WifiNanStatus();
                    status2.readFromParcel(_hidl_request);
                    notifyEnableResponse(id2, status2);
                    return;
                case 3:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    short id3 = _hidl_request.readInt16();
                    WifiNanStatus status3 = new WifiNanStatus();
                    status3.readFromParcel(_hidl_request);
                    notifyConfigResponse(id3, status3);
                    return;
                case 4:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    short id4 = _hidl_request.readInt16();
                    WifiNanStatus status4 = new WifiNanStatus();
                    status4.readFromParcel(_hidl_request);
                    notifyDisableResponse(id4, status4);
                    return;
                case 5:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    short id5 = _hidl_request.readInt16();
                    WifiNanStatus status5 = new WifiNanStatus();
                    status5.readFromParcel(_hidl_request);
                    notifyStartPublishResponse(id5, status5, _hidl_request.readInt8());
                    return;
                case 6:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    short id6 = _hidl_request.readInt16();
                    WifiNanStatus status6 = new WifiNanStatus();
                    status6.readFromParcel(_hidl_request);
                    notifyStopPublishResponse(id6, status6);
                    return;
                case 7:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    short id7 = _hidl_request.readInt16();
                    WifiNanStatus status7 = new WifiNanStatus();
                    status7.readFromParcel(_hidl_request);
                    notifyStartSubscribeResponse(id7, status7, _hidl_request.readInt8());
                    return;
                case 8:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    short id8 = _hidl_request.readInt16();
                    WifiNanStatus status8 = new WifiNanStatus();
                    status8.readFromParcel(_hidl_request);
                    notifyStopSubscribeResponse(id8, status8);
                    return;
                case 9:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    short id9 = _hidl_request.readInt16();
                    WifiNanStatus status9 = new WifiNanStatus();
                    status9.readFromParcel(_hidl_request);
                    notifyTransmitFollowupResponse(id9, status9);
                    return;
                case 10:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    short id10 = _hidl_request.readInt16();
                    WifiNanStatus status10 = new WifiNanStatus();
                    status10.readFromParcel(_hidl_request);
                    notifyCreateDataInterfaceResponse(id10, status10);
                    return;
                case 11:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    short id11 = _hidl_request.readInt16();
                    WifiNanStatus status11 = new WifiNanStatus();
                    status11.readFromParcel(_hidl_request);
                    notifyDeleteDataInterfaceResponse(id11, status11);
                    return;
                case 12:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    short id12 = _hidl_request.readInt16();
                    WifiNanStatus status12 = new WifiNanStatus();
                    status12.readFromParcel(_hidl_request);
                    notifyInitiateDataPathResponse(id12, status12, _hidl_request.readInt32());
                    return;
                case 13:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    short id13 = _hidl_request.readInt16();
                    WifiNanStatus status13 = new WifiNanStatus();
                    status13.readFromParcel(_hidl_request);
                    notifyRespondToDataPathIndicationResponse(id13, status13);
                    return;
                case 14:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    short id14 = _hidl_request.readInt16();
                    WifiNanStatus status14 = new WifiNanStatus();
                    status14.readFromParcel(_hidl_request);
                    notifyTerminateDataPathResponse(id14, status14);
                    return;
                case 15:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    NanClusterEventInd event = new NanClusterEventInd();
                    event.readFromParcel(_hidl_request);
                    eventClusterEvent(event);
                    return;
                case 16:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    WifiNanStatus status15 = new WifiNanStatus();
                    status15.readFromParcel(_hidl_request);
                    eventDisabled(status15);
                    return;
                case 17:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    byte sessionId = _hidl_request.readInt8();
                    WifiNanStatus status16 = new WifiNanStatus();
                    status16.readFromParcel(_hidl_request);
                    eventPublishTerminated(sessionId, status16);
                    return;
                case 18:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    byte sessionId2 = _hidl_request.readInt8();
                    WifiNanStatus status17 = new WifiNanStatus();
                    status17.readFromParcel(_hidl_request);
                    eventSubscribeTerminated(sessionId2, status17);
                    return;
                case 19:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    NanMatchInd event2 = new NanMatchInd();
                    event2.readFromParcel(_hidl_request);
                    eventMatch(event2);
                    return;
                case 20:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    eventMatchExpired(_hidl_request.readInt8(), _hidl_request.readInt32());
                    return;
                case ISupplicantStaIfaceCallback.ReasonCode.UNSUPPORTED_RSN_IE_VERSION /*21*/:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    NanFollowupReceivedInd event3 = new NanFollowupReceivedInd();
                    event3.readFromParcel(_hidl_request);
                    eventFollowupReceived(event3);
                    return;
                case 22:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    short id15 = _hidl_request.readInt16();
                    WifiNanStatus status18 = new WifiNanStatus();
                    status18.readFromParcel(_hidl_request);
                    eventTransmitFollowup(id15, status18);
                    return;
                case 23:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    NanDataPathRequestInd event4 = new NanDataPathRequestInd();
                    event4.readFromParcel(_hidl_request);
                    eventDataPathRequest(event4);
                    return;
                case 24:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    NanDataPathConfirmInd event5 = new NanDataPathConfirmInd();
                    event5.readFromParcel(_hidl_request);
                    eventDataPathConfirm(event5);
                    return;
                case 25:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiNanIfaceEventCallback.kInterfaceName);
                    eventDataPathTerminated(_hidl_request.readInt32());
                    return;
                default:
                    switch (_hidl_code) {
                        case 256067662:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
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
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.send();
                            return;
                        case 256136003:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
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
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
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
                            while (_hidl_index_0 < _hidl_vec_size) {
                                childBlob.putInt8Array((long) (_hidl_index_0 * 32), _hidl_out_hashchain.get(_hidl_index_0));
                                _hidl_index_0++;
                            }
                            _hidl_blob.putBlob(0, childBlob);
                            _hidl_reply.writeBuffer(_hidl_blob);
                            _hidl_reply.send();
                            return;
                        case 256462420:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_index_0 = 1;
                            }
                            if (_hidl_index_0 != 1) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            setHALInstrumentation();
                            return;
                        case 256660548:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_index_0 = 1;
                            }
                            if (_hidl_index_0 != 0) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            return;
                        case 256921159:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
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
                                _hidl_is_oneway = false;
                            }
                            if (_hidl_is_oneway) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
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
                                _hidl_index_0 = 1;
                            }
                            if (_hidl_index_0 != 1) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            notifySyspropsChanged();
                            return;
                        case 257250372:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_index_0 = 1;
                            }
                            if (_hidl_index_0 != 0) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
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

    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

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

    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

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

    static IWifiNanIfaceEventCallback castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IWifiNanIfaceEventCallback getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IWifiNanIfaceEventCallback getService(boolean retry) throws RemoteException {
        return getService(HalDeviceManager.HAL_INSTANCE_NAME, retry);
    }

    static IWifiNanIfaceEventCallback getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IWifiNanIfaceEventCallback getService() throws RemoteException {
        return getService(HalDeviceManager.HAL_INSTANCE_NAME);
    }
}
