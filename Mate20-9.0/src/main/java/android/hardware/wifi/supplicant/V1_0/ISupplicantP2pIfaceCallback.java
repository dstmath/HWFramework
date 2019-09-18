package android.hardware.wifi.supplicant.V1_0;

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

public interface ISupplicantP2pIfaceCallback extends IBase {
    public static final String kInterfaceName = "android.hardware.wifi.supplicant@1.0::ISupplicantP2pIfaceCallback";

    public static final class P2pProvDiscStatusCode {
        public static final byte INFO_UNAVAILABLE = 4;
        public static final byte REJECTED = 2;
        public static final byte SUCCESS = 0;
        public static final byte TIMEOUT = 1;
        public static final byte TIMEOUT_JOIN = 3;

        public static final String toString(byte o) {
            if (o == 0) {
                return "SUCCESS";
            }
            if (o == 1) {
                return "TIMEOUT";
            }
            if (o == 2) {
                return "REJECTED";
            }
            if (o == 3) {
                return "TIMEOUT_JOIN";
            }
            if (o == 4) {
                return "INFO_UNAVAILABLE";
            }
            return "0x" + Integer.toHexString(Byte.toUnsignedInt(o));
        }

        public static final String dumpBitfield(byte o) {
            ArrayList<String> list = new ArrayList<>();
            byte flipped = 0;
            list.add("SUCCESS");
            if ((o & 1) == 1) {
                list.add("TIMEOUT");
                flipped = (byte) (0 | 1);
            }
            if ((o & 2) == 2) {
                list.add("REJECTED");
                flipped = (byte) (flipped | 2);
            }
            if ((o & 3) == 3) {
                list.add("TIMEOUT_JOIN");
                flipped = (byte) (flipped | 3);
            }
            if ((o & 4) == 4) {
                list.add("INFO_UNAVAILABLE");
                flipped = (byte) (flipped | 4);
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString(Byte.toUnsignedInt((byte) ((~flipped) & o))));
            }
            return String.join(" | ", list);
        }
    }

    public static final class P2pStatusCode {
        public static final int FAIL_BOTH_GO_INTENT_15 = 9;
        public static final int FAIL_INCOMPATIBLE_PARAMS = 2;
        public static final int FAIL_INCOMPATIBLE_PROV_METHOD = 10;
        public static final int FAIL_INFO_CURRENTLY_UNAVAILABLE = 1;
        public static final int FAIL_INVALID_PARAMS = 4;
        public static final int FAIL_LIMIT_REACHED = 3;
        public static final int FAIL_NO_COMMON_CHANNELS = 7;
        public static final int FAIL_PREV_PROTOCOL_ERROR = 6;
        public static final int FAIL_REJECTED_BY_USER = 11;
        public static final int FAIL_UNABLE_TO_ACCOMMODATE = 5;
        public static final int FAIL_UNKNOWN_GROUP = 8;
        public static final int SUCCESS = 0;
        public static final int SUCCESS_DEFERRED = 12;

        public static final String toString(int o) {
            if (o == 0) {
                return "SUCCESS";
            }
            if (o == 1) {
                return "FAIL_INFO_CURRENTLY_UNAVAILABLE";
            }
            if (o == 2) {
                return "FAIL_INCOMPATIBLE_PARAMS";
            }
            if (o == 3) {
                return "FAIL_LIMIT_REACHED";
            }
            if (o == 4) {
                return "FAIL_INVALID_PARAMS";
            }
            if (o == 5) {
                return "FAIL_UNABLE_TO_ACCOMMODATE";
            }
            if (o == 6) {
                return "FAIL_PREV_PROTOCOL_ERROR";
            }
            if (o == 7) {
                return "FAIL_NO_COMMON_CHANNELS";
            }
            if (o == 8) {
                return "FAIL_UNKNOWN_GROUP";
            }
            if (o == 9) {
                return "FAIL_BOTH_GO_INTENT_15";
            }
            if (o == 10) {
                return "FAIL_INCOMPATIBLE_PROV_METHOD";
            }
            if (o == 11) {
                return "FAIL_REJECTED_BY_USER";
            }
            if (o == 12) {
                return "SUCCESS_DEFERRED";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            list.add("SUCCESS");
            if ((o & 1) == 1) {
                list.add("FAIL_INFO_CURRENTLY_UNAVAILABLE");
                flipped = 0 | 1;
            }
            if ((o & 2) == 2) {
                list.add("FAIL_INCOMPATIBLE_PARAMS");
                flipped |= 2;
            }
            if ((o & 3) == 3) {
                list.add("FAIL_LIMIT_REACHED");
                flipped |= 3;
            }
            if ((o & 4) == 4) {
                list.add("FAIL_INVALID_PARAMS");
                flipped |= 4;
            }
            if ((o & 5) == 5) {
                list.add("FAIL_UNABLE_TO_ACCOMMODATE");
                flipped |= 5;
            }
            if ((o & 6) == 6) {
                list.add("FAIL_PREV_PROTOCOL_ERROR");
                flipped |= 6;
            }
            if ((o & 7) == 7) {
                list.add("FAIL_NO_COMMON_CHANNELS");
                flipped |= 7;
            }
            if ((o & 8) == 8) {
                list.add("FAIL_UNKNOWN_GROUP");
                flipped |= 8;
            }
            if ((o & 9) == 9) {
                list.add("FAIL_BOTH_GO_INTENT_15");
                flipped |= 9;
            }
            if ((o & 10) == 10) {
                list.add("FAIL_INCOMPATIBLE_PROV_METHOD");
                flipped |= 10;
            }
            if ((o & 11) == 11) {
                list.add("FAIL_REJECTED_BY_USER");
                flipped |= 11;
            }
            if ((o & 12) == 12) {
                list.add("SUCCESS_DEFERRED");
                flipped |= 12;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class Proxy implements ISupplicantP2pIfaceCallback {
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
                return "[class or subclass of android.hardware.wifi.supplicant@1.0::ISupplicantP2pIfaceCallback]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        public void onNetworkAdded(int id) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantP2pIfaceCallback.kInterfaceName);
            _hidl_request.writeInt32(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onNetworkRemoved(int id) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantP2pIfaceCallback.kInterfaceName);
            _hidl_request.writeInt32(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onDeviceFound(byte[] srcAddress, byte[] p2pDeviceAddress, byte[] primaryDeviceType, String deviceName, short configMethods, byte deviceCapabilities, int groupCapabilities, byte[] wfdDeviceInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantP2pIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            _hidl_blob.putInt8Array(0, srcAddress);
            _hidl_request.writeBuffer(_hidl_blob);
            HwBlob _hidl_blob2 = new HwBlob(6);
            _hidl_blob2.putInt8Array(0, p2pDeviceAddress);
            _hidl_request.writeBuffer(_hidl_blob2);
            HwBlob _hidl_blob3 = new HwBlob(8);
            _hidl_blob3.putInt8Array(0, primaryDeviceType);
            _hidl_request.writeBuffer(_hidl_blob3);
            _hidl_request.writeString(deviceName);
            _hidl_request.writeInt16(configMethods);
            _hidl_request.writeInt8(deviceCapabilities);
            _hidl_request.writeInt32(groupCapabilities);
            HwBlob _hidl_blob4 = new HwBlob(6);
            _hidl_blob4.putInt8Array(0, wfdDeviceInfo);
            _hidl_request.writeBuffer(_hidl_blob4);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onDeviceLost(byte[] p2pDeviceAddress) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantP2pIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            _hidl_blob.putInt8Array(0, p2pDeviceAddress);
            _hidl_request.writeBuffer(_hidl_blob);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onFindStopped() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantP2pIfaceCallback.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onGoNegotiationRequest(byte[] srcAddress, short passwordId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantP2pIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            _hidl_blob.putInt8Array(0, srcAddress);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeInt16(passwordId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onGoNegotiationCompleted(int status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantP2pIfaceCallback.kInterfaceName);
            _hidl_request.writeInt32(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onGroupFormationSuccess() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantP2pIfaceCallback.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onGroupFormationFailure(String failureReason) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantP2pIfaceCallback.kInterfaceName);
            _hidl_request.writeString(failureReason);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onGroupStarted(String groupIfname, boolean isGo, ArrayList<Byte> ssid, int frequency, byte[] psk, String passphrase, byte[] goDeviceAddress, boolean isPersistent) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantP2pIfaceCallback.kInterfaceName);
            _hidl_request.writeString(groupIfname);
            _hidl_request.writeBool(isGo);
            _hidl_request.writeInt8Vector(ssid);
            _hidl_request.writeInt32(frequency);
            HwBlob _hidl_blob = new HwBlob(32);
            _hidl_blob.putInt8Array(0, psk);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeString(passphrase);
            HwBlob _hidl_blob2 = new HwBlob(6);
            _hidl_blob2.putInt8Array(0, goDeviceAddress);
            _hidl_request.writeBuffer(_hidl_blob2);
            _hidl_request.writeBool(isPersistent);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onGroupRemoved(String groupIfname, boolean isGo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantP2pIfaceCallback.kInterfaceName);
            _hidl_request.writeString(groupIfname);
            _hidl_request.writeBool(isGo);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onInvitationReceived(byte[] srcAddress, byte[] goDeviceAddress, byte[] bssid, int persistentNetworkId, int operatingFrequency) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantP2pIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            _hidl_blob.putInt8Array(0, srcAddress);
            _hidl_request.writeBuffer(_hidl_blob);
            HwBlob _hidl_blob2 = new HwBlob(6);
            _hidl_blob2.putInt8Array(0, goDeviceAddress);
            _hidl_request.writeBuffer(_hidl_blob2);
            HwBlob _hidl_blob3 = new HwBlob(6);
            _hidl_blob3.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob3);
            _hidl_request.writeInt32(persistentNetworkId);
            _hidl_request.writeInt32(operatingFrequency);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onInvitationResult(byte[] bssid, int status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantP2pIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            _hidl_blob.putInt8Array(0, bssid);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeInt32(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onProvisionDiscoveryCompleted(byte[] p2pDeviceAddress, boolean isRequest, byte status, short configMethods, String generatedPin) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantP2pIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            _hidl_blob.putInt8Array(0, p2pDeviceAddress);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeBool(isRequest);
            _hidl_request.writeInt8(status);
            _hidl_request.writeInt16(configMethods);
            _hidl_request.writeString(generatedPin);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onServiceDiscoveryResponse(byte[] srcAddress, short updateIndicator, ArrayList<Byte> tlvs) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantP2pIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            _hidl_blob.putInt8Array(0, srcAddress);
            _hidl_request.writeBuffer(_hidl_blob);
            _hidl_request.writeInt16(updateIndicator);
            _hidl_request.writeInt8Vector(tlvs);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onStaAuthorized(byte[] srcAddress, byte[] p2pDeviceAddress) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantP2pIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            _hidl_blob.putInt8Array(0, srcAddress);
            _hidl_request.writeBuffer(_hidl_blob);
            HwBlob _hidl_blob2 = new HwBlob(6);
            _hidl_blob2.putInt8Array(0, p2pDeviceAddress);
            _hidl_request.writeBuffer(_hidl_blob2);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onStaDeauthorized(byte[] srcAddress, byte[] p2pDeviceAddress) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantP2pIfaceCallback.kInterfaceName);
            HwBlob _hidl_blob = new HwBlob(6);
            _hidl_blob.putInt8Array(0, srcAddress);
            _hidl_request.writeBuffer(_hidl_blob);
            HwBlob _hidl_blob2 = new HwBlob(6);
            _hidl_blob2.putInt8Array(0, p2pDeviceAddress);
            _hidl_request.writeBuffer(_hidl_blob2);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 1);
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

    public static abstract class Stub extends HwBinder implements ISupplicantP2pIfaceCallback {
        public IHwBinder asBinder() {
            return this;
        }

        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(new String[]{ISupplicantP2pIfaceCallback.kInterfaceName, IBase.kInterfaceName}));
        }

        public final String interfaceDescriptor() {
            return ISupplicantP2pIfaceCallback.kInterfaceName;
        }

        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[][]{new byte[]{-117, 99, -11, -17, -94, -29, -66, 58, 124, -72, -92, 40, 118, 13, -126, 40, 90, 74, -73, -101, -53, -34, -90, -17, -112, -86, 84, 117, 85, -27, -126, -44}, new byte[]{-67, -38, -74, 24, 77, 122, 52, 109, -90, -96, 125, -64, -126, -116, -15, -102, 105, 111, 76, -86, 54, 17, -59, 31, 46, 20, 86, 90, 20, -76, 15, -39}}));
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
            if (ISupplicantP2pIfaceCallback.kInterfaceName.equals(descriptor)) {
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
            HwParcel hwParcel2 = _hidl_reply;
            int _hidl_index_0 = 0;
            boolean _hidl_is_oneway = true;
            switch (_hidl_code) {
                case 1:
                    if (_hidl_flags != false && true) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ISupplicantP2pIfaceCallback.kInterfaceName);
                    onNetworkAdded(_hidl_request.readInt32());
                    return;
                case 2:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ISupplicantP2pIfaceCallback.kInterfaceName);
                    onNetworkRemoved(_hidl_request.readInt32());
                    return;
                case 3:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ISupplicantP2pIfaceCallback.kInterfaceName);
                    byte[] srcAddress = new byte[6];
                    hwParcel.readBuffer(6).copyToInt8Array(0, srcAddress, 6);
                    byte[] p2pDeviceAddress = new byte[6];
                    hwParcel.readBuffer(6).copyToInt8Array(0, p2pDeviceAddress, 6);
                    byte[] primaryDeviceType = new byte[8];
                    hwParcel.readBuffer(8).copyToInt8Array(0, primaryDeviceType, 8);
                    String deviceName = _hidl_request.readString();
                    short configMethods = _hidl_request.readInt16();
                    byte deviceCapabilities = _hidl_request.readInt8();
                    int groupCapabilities = _hidl_request.readInt32();
                    byte[] wfdDeviceInfo = new byte[6];
                    hwParcel.readBuffer(6).copyToInt8Array(0, wfdDeviceInfo, 6);
                    byte[] bArr = primaryDeviceType;
                    byte[] bArr2 = p2pDeviceAddress;
                    onDeviceFound(srcAddress, p2pDeviceAddress, primaryDeviceType, deviceName, configMethods, deviceCapabilities, groupCapabilities, wfdDeviceInfo);
                    return;
                case 4:
                    if (_hidl_flags != false && true) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ISupplicantP2pIfaceCallback.kInterfaceName);
                    byte[] p2pDeviceAddress2 = new byte[6];
                    hwParcel.readBuffer(6).copyToInt8Array(0, p2pDeviceAddress2, 6);
                    onDeviceLost(p2pDeviceAddress2);
                    return;
                case 5:
                    if (_hidl_flags != false && true) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ISupplicantP2pIfaceCallback.kInterfaceName);
                    onFindStopped();
                    return;
                case 6:
                    if (_hidl_flags != false && true) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ISupplicantP2pIfaceCallback.kInterfaceName);
                    byte[] srcAddress2 = new byte[6];
                    hwParcel.readBuffer(6).copyToInt8Array(0, srcAddress2, 6);
                    onGoNegotiationRequest(srcAddress2, _hidl_request.readInt16());
                    return;
                case 7:
                    if (_hidl_flags != false && true) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ISupplicantP2pIfaceCallback.kInterfaceName);
                    onGoNegotiationCompleted(_hidl_request.readInt32());
                    return;
                case 8:
                    if (_hidl_flags != false && true) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ISupplicantP2pIfaceCallback.kInterfaceName);
                    onGroupFormationSuccess();
                    return;
                case 9:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ISupplicantP2pIfaceCallback.kInterfaceName);
                    onGroupFormationFailure(_hidl_request.readString());
                    return;
                case 10:
                    if (_hidl_flags != false && true) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ISupplicantP2pIfaceCallback.kInterfaceName);
                    String groupIfname = _hidl_request.readString();
                    boolean isGo = _hidl_request.readBool();
                    ArrayList<Byte> ssid = _hidl_request.readInt8Vector();
                    int frequency = _hidl_request.readInt32();
                    byte[] psk = new byte[32];
                    hwParcel.readBuffer(32).copyToInt8Array(0, psk, 32);
                    String passphrase = _hidl_request.readString();
                    byte[] goDeviceAddress = new byte[6];
                    hwParcel.readBuffer(6).copyToInt8Array(0, goDeviceAddress, 6);
                    byte[] bArr3 = goDeviceAddress;
                    byte[] bArr4 = psk;
                    onGroupStarted(groupIfname, isGo, ssid, frequency, psk, passphrase, goDeviceAddress, _hidl_request.readBool());
                    return;
                case 11:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ISupplicantP2pIfaceCallback.kInterfaceName);
                    onGroupRemoved(_hidl_request.readString(), _hidl_request.readBool());
                    return;
                case 12:
                    if (_hidl_flags != false && true) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ISupplicantP2pIfaceCallback.kInterfaceName);
                    byte[] srcAddress3 = new byte[6];
                    hwParcel.readBuffer(6).copyToInt8Array(0, srcAddress3, 6);
                    byte[] goDeviceAddress2 = new byte[6];
                    hwParcel.readBuffer(6).copyToInt8Array(0, goDeviceAddress2, 6);
                    byte[] bssid = new byte[6];
                    hwParcel.readBuffer(6).copyToInt8Array(0, bssid, 6);
                    onInvitationReceived(srcAddress3, goDeviceAddress2, bssid, _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 13:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ISupplicantP2pIfaceCallback.kInterfaceName);
                    byte[] bssid2 = new byte[6];
                    hwParcel.readBuffer(6).copyToInt8Array(0, bssid2, 6);
                    onInvitationResult(bssid2, _hidl_request.readInt32());
                    return;
                case 14:
                    if (_hidl_flags != false && true) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ISupplicantP2pIfaceCallback.kInterfaceName);
                    byte[] p2pDeviceAddress3 = new byte[6];
                    hwParcel.readBuffer(6).copyToInt8Array(0, p2pDeviceAddress3, 6);
                    onProvisionDiscoveryCompleted(p2pDeviceAddress3, _hidl_request.readBool(), _hidl_request.readInt8(), _hidl_request.readInt16(), _hidl_request.readString());
                    return;
                case 15:
                    if (_hidl_flags != false && true) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ISupplicantP2pIfaceCallback.kInterfaceName);
                    byte[] srcAddress4 = new byte[6];
                    hwParcel.readBuffer(6).copyToInt8Array(0, srcAddress4, 6);
                    onServiceDiscoveryResponse(srcAddress4, _hidl_request.readInt16(), _hidl_request.readInt8Vector());
                    return;
                case 16:
                    if (_hidl_flags != false && true) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ISupplicantP2pIfaceCallback.kInterfaceName);
                    byte[] srcAddress5 = new byte[6];
                    hwParcel.readBuffer(6).copyToInt8Array(0, srcAddress5, 6);
                    byte[] p2pDeviceAddress4 = new byte[6];
                    hwParcel.readBuffer(6).copyToInt8Array(0, p2pDeviceAddress4, 6);
                    onStaAuthorized(srcAddress5, p2pDeviceAddress4);
                    return;
                case 17:
                    if (_hidl_flags != false && true) {
                        _hidl_index_0 = 1;
                    }
                    if (_hidl_index_0 != 1) {
                        hwParcel2.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    hwParcel.enforceInterface(ISupplicantP2pIfaceCallback.kInterfaceName);
                    byte[] srcAddress6 = new byte[6];
                    hwParcel.readBuffer(6).copyToInt8Array(0, srcAddress6, 6);
                    byte[] p2pDeviceAddress5 = new byte[6];
                    hwParcel.readBuffer(6).copyToInt8Array(0, p2pDeviceAddress5, 6);
                    onStaDeauthorized(srcAddress6, p2pDeviceAddress5);
                    return;
                default:
                    switch (_hidl_code) {
                        case 256067662:
                            if (_hidl_flags == false || !true) {
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
                            if (_hidl_flags == false || !true) {
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
                            if ((_hidl_flags & 1) == 0) {
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
                            if (_hidl_flags != false && true) {
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
                            if (_hidl_flags != false && true) {
                                _hidl_index_0 = 1;
                            }
                            if (_hidl_index_0 != 0) {
                                hwParcel2.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            return;
                        case 256921159:
                            if (_hidl_flags == false || !true) {
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
                            if (_hidl_flags != false && true) {
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

    public static final class WpsDevPasswordId {
        public static final short DEFAULT = 0;
        public static final short MACHINE_SPECIFIED = 2;
        public static final short NFC_CONNECTION_HANDOVER = 7;
        public static final short P2PS_DEFAULT = 8;
        public static final short PUSHBUTTON = 4;
        public static final short REGISTRAR_SPECIFIED = 5;
        public static final short REKEY = 3;
        public static final short USER_SPECIFIED = 1;

        public static final String toString(short o) {
            if (o == 0) {
                return "DEFAULT";
            }
            if (o == 1) {
                return "USER_SPECIFIED";
            }
            if (o == 2) {
                return "MACHINE_SPECIFIED";
            }
            if (o == 3) {
                return "REKEY";
            }
            if (o == 4) {
                return "PUSHBUTTON";
            }
            if (o == 5) {
                return "REGISTRAR_SPECIFIED";
            }
            if (o == 7) {
                return "NFC_CONNECTION_HANDOVER";
            }
            if (o == 8) {
                return "P2PS_DEFAULT";
            }
            return "0x" + Integer.toHexString(Short.toUnsignedInt(o));
        }

        public static final String dumpBitfield(short o) {
            ArrayList<String> list = new ArrayList<>();
            short flipped = 0;
            list.add("DEFAULT");
            if ((o & 1) == 1) {
                list.add("USER_SPECIFIED");
                flipped = (short) (0 | 1);
            }
            if ((o & 2) == 2) {
                list.add("MACHINE_SPECIFIED");
                flipped = (short) (flipped | 2);
            }
            if ((o & 3) == 3) {
                list.add("REKEY");
                flipped = (short) (flipped | 3);
            }
            if ((o & 4) == 4) {
                list.add("PUSHBUTTON");
                flipped = (short) (flipped | 4);
            }
            if ((o & 5) == 5) {
                list.add("REGISTRAR_SPECIFIED");
                flipped = (short) (flipped | 5);
            }
            if ((o & 7) == 7) {
                list.add("NFC_CONNECTION_HANDOVER");
                flipped = (short) (flipped | 7);
            }
            if ((o & 8) == 8) {
                list.add("P2PS_DEFAULT");
                flipped = (short) (flipped | 8);
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString(Short.toUnsignedInt((short) ((~flipped) & o))));
            }
            return String.join(" | ", list);
        }
    }

    IHwBinder asBinder();

    DebugInfo getDebugInfo() throws RemoteException;

    ArrayList<byte[]> getHashChain() throws RemoteException;

    ArrayList<String> interfaceChain() throws RemoteException;

    String interfaceDescriptor() throws RemoteException;

    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    void notifySyspropsChanged() throws RemoteException;

    void onDeviceFound(byte[] bArr, byte[] bArr2, byte[] bArr3, String str, short s, byte b, int i, byte[] bArr4) throws RemoteException;

    void onDeviceLost(byte[] bArr) throws RemoteException;

    void onFindStopped() throws RemoteException;

    void onGoNegotiationCompleted(int i) throws RemoteException;

    void onGoNegotiationRequest(byte[] bArr, short s) throws RemoteException;

    void onGroupFormationFailure(String str) throws RemoteException;

    void onGroupFormationSuccess() throws RemoteException;

    void onGroupRemoved(String str, boolean z) throws RemoteException;

    void onGroupStarted(String str, boolean z, ArrayList<Byte> arrayList, int i, byte[] bArr, String str2, byte[] bArr2, boolean z2) throws RemoteException;

    void onInvitationReceived(byte[] bArr, byte[] bArr2, byte[] bArr3, int i, int i2) throws RemoteException;

    void onInvitationResult(byte[] bArr, int i) throws RemoteException;

    void onNetworkAdded(int i) throws RemoteException;

    void onNetworkRemoved(int i) throws RemoteException;

    void onProvisionDiscoveryCompleted(byte[] bArr, boolean z, byte b, short s, String str) throws RemoteException;

    void onServiceDiscoveryResponse(byte[] bArr, short s, ArrayList<Byte> arrayList) throws RemoteException;

    void onStaAuthorized(byte[] bArr, byte[] bArr2) throws RemoteException;

    void onStaDeauthorized(byte[] bArr, byte[] bArr2) throws RemoteException;

    void ping() throws RemoteException;

    void setHALInstrumentation() throws RemoteException;

    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static ISupplicantP2pIfaceCallback asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof ISupplicantP2pIfaceCallback)) {
            return (ISupplicantP2pIfaceCallback) iface;
        }
        ISupplicantP2pIfaceCallback proxy = new Proxy(binder);
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

    static ISupplicantP2pIfaceCallback castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static ISupplicantP2pIfaceCallback getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static ISupplicantP2pIfaceCallback getService(boolean retry) throws RemoteException {
        return getService(HalDeviceManager.HAL_INSTANCE_NAME, retry);
    }

    static ISupplicantP2pIfaceCallback getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static ISupplicantP2pIfaceCallback getService() throws RemoteException {
        return getService(HalDeviceManager.HAL_INSTANCE_NAME);
    }
}
