package android.hardware.wifi.V1_2;

import android.hardware.wifi.V1_0.WifiBand;
import android.hardware.wifi.V1_0.WifiDebugRingBufferStatus;
import android.hardware.wifi.V1_0.WifiStatus;
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

public interface IWifiChipEventCallback extends android.hardware.wifi.V1_0.IWifiChipEventCallback {
    public static final String kInterfaceName = "android.hardware.wifi@1.2::IWifiChipEventCallback";

    public static final class IfaceInfo {
        public int channel;
        public String name = new String();

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != IfaceInfo.class) {
                return false;
            }
            IfaceInfo other = (IfaceInfo) otherObject;
            if (HidlSupport.deepEquals(this.name, other.name) && this.channel == other.channel) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.name)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.channel)))});
        }

        public final String toString() {
            return "{" + ".name = " + this.name + ", .channel = " + this.channel + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
        }

        public static final ArrayList<IfaceInfo> readVectorFromParcel(HwParcel parcel) {
            ArrayList<IfaceInfo> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                IfaceInfo _hidl_vec_element = new IfaceInfo();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            this.name = _hidl_blob.getString(_hidl_offset + 0);
            parcel.readEmbeddedBuffer((long) (this.name.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 0 + 0, false);
            this.channel = _hidl_blob.getInt32(16 + _hidl_offset);
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(24);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<IfaceInfo> _hidl_vec) {
            HwBlob _hidl_blob = new HwBlob(16);
            int _hidl_vec_size = _hidl_vec.size();
            _hidl_blob.putInt32(8, _hidl_vec_size);
            _hidl_blob.putBool(12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
            }
            _hidl_blob.putBlob(0, childBlob);
            parcel.writeBuffer(_hidl_blob);
        }

        public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
            _hidl_blob.putString(0 + _hidl_offset, this.name);
            _hidl_blob.putInt32(16 + _hidl_offset, this.channel);
        }
    }

    public static final class Proxy implements IWifiChipEventCallback {
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
                return "[class or subclass of android.hardware.wifi@1.2::IWifiChipEventCallback]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        public void onChipReconfigured(int modeId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChipEventCallback.kInterfaceName);
            _hidl_request.writeInt32(modeId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onChipReconfigureFailure(WifiStatus status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChipEventCallback.kInterfaceName);
            status.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onIfaceAdded(int type, String name) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChipEventCallback.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeString(name);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onIfaceRemoved(int type, String name) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChipEventCallback.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeString(name);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onDebugRingBufferDataAvailable(WifiDebugRingBufferStatus status, ArrayList<Byte> data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChipEventCallback.kInterfaceName);
            status.writeToParcel(_hidl_request);
            _hidl_request.writeInt8Vector(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onDebugErrorAlert(int errorCode, ArrayList<Byte> debugData) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.V1_0.IWifiChipEventCallback.kInterfaceName);
            _hidl_request.writeInt32(errorCode);
            _hidl_request.writeInt8Vector(debugData);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void onRadioModeChange(ArrayList<RadioModeInfo> radioModeInfos) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiChipEventCallback.kInterfaceName);
            RadioModeInfo.writeVectorToParcel(_hidl_request, radioModeInfos);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 1);
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

    public static final class RadioModeInfo {
        public int bandInfo;
        public final ArrayList<IfaceInfo> ifaceInfos = new ArrayList<>();
        public int radioId;

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != RadioModeInfo.class) {
                return false;
            }
            RadioModeInfo other = (RadioModeInfo) otherObject;
            if (this.radioId == other.radioId && this.bandInfo == other.bandInfo && HidlSupport.deepEquals(this.ifaceInfos, other.ifaceInfos)) {
                return true;
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.radioId))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.bandInfo))), Integer.valueOf(HidlSupport.deepHashCode(this.ifaceInfos))});
        }

        public final String toString() {
            return "{" + ".radioId = " + this.radioId + ", .bandInfo = " + WifiBand.toString(this.bandInfo) + ", .ifaceInfos = " + this.ifaceInfos + "}";
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
        }

        public static final ArrayList<RadioModeInfo> readVectorFromParcel(HwParcel parcel) {
            ArrayList<RadioModeInfo> _hidl_vec = new ArrayList<>();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                RadioModeInfo _hidl_vec_element = new RadioModeInfo();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            HwBlob hwBlob = _hidl_blob;
            this.radioId = hwBlob.getInt32(_hidl_offset + 0);
            this.bandInfo = hwBlob.getInt32(_hidl_offset + 4);
            int _hidl_vec_size = hwBlob.getInt32(_hidl_offset + 8 + 8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), _hidl_offset + 8 + 0, true);
            this.ifaceInfos.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                IfaceInfo _hidl_vec_element = new IfaceInfo();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
                this.ifaceInfos.add(_hidl_vec_element);
            }
            HwParcel hwParcel = parcel;
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(24);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RadioModeInfo> _hidl_vec) {
            HwBlob _hidl_blob = new HwBlob(16);
            int _hidl_vec_size = _hidl_vec.size();
            _hidl_blob.putInt32(8, _hidl_vec_size);
            _hidl_blob.putBool(12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
            }
            _hidl_blob.putBlob(0, childBlob);
            parcel.writeBuffer(_hidl_blob);
        }

        public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
            _hidl_blob.putInt32(_hidl_offset + 0, this.radioId);
            _hidl_blob.putInt32(4 + _hidl_offset, this.bandInfo);
            int _hidl_vec_size = this.ifaceInfos.size();
            _hidl_blob.putInt32(_hidl_offset + 8 + 8, _hidl_vec_size);
            int _hidl_index_0 = 0;
            _hidl_blob.putBool(_hidl_offset + 8 + 12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
            while (true) {
                int _hidl_index_02 = _hidl_index_0;
                if (_hidl_index_02 < _hidl_vec_size) {
                    this.ifaceInfos.get(_hidl_index_02).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_02 * 24));
                    _hidl_index_0 = _hidl_index_02 + 1;
                } else {
                    _hidl_blob.putBlob(8 + _hidl_offset + 0, childBlob);
                    return;
                }
            }
        }
    }

    public static abstract class Stub extends HwBinder implements IWifiChipEventCallback {
        public IHwBinder asBinder() {
            return this;
        }

        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(new String[]{IWifiChipEventCallback.kInterfaceName, android.hardware.wifi.V1_0.IWifiChipEventCallback.kInterfaceName, IBase.kInterfaceName}));
        }

        public final String interfaceDescriptor() {
            return IWifiChipEventCallback.kInterfaceName;
        }

        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[][]{new byte[]{22, 122, -8, 112, -3, -72, 126, 28, -69, -86, 15, -90, 46, -13, 94, 16, 49, -54, -83, 32, -35, 27, -90, -107, -104, 61, -19, -79, -23, -103, 52, -122}, new byte[]{-95, -71, -120, 55, 118, 69, -91, -114, 94, 37, 66, -54, 43, -83, 78, 23, -62, 26, 74, 56, -110, 19, -48, 93, -30, -16, -29, 45, 87, -73, -45, 57}, new byte[]{-67, -38, -74, 24, 77, 122, 52, 109, -90, -96, 125, -64, -126, -116, -15, -102, 105, 111, 76, -86, 54, 17, -59, 31, 46, 20, 86, 90, 20, -76, 15, -39}}));
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
            if (IWifiChipEventCallback.kInterfaceName.equals(descriptor)) {
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
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChipEventCallback.kInterfaceName);
                    onChipReconfigured(_hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChipEventCallback.kInterfaceName);
                    WifiStatus status = new WifiStatus();
                    status.readFromParcel(_hidl_request);
                    onChipReconfigureFailure(status);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChipEventCallback.kInterfaceName);
                    onIfaceAdded(_hidl_request.readInt32(), _hidl_request.readString());
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
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChipEventCallback.kInterfaceName);
                    onIfaceRemoved(_hidl_request.readInt32(), _hidl_request.readString());
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
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChipEventCallback.kInterfaceName);
                    WifiDebugRingBufferStatus status2 = new WifiDebugRingBufferStatus();
                    status2.readFromParcel(_hidl_request);
                    onDebugRingBufferDataAvailable(status2, _hidl_request.readInt8Vector());
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
                    _hidl_request.enforceInterface(android.hardware.wifi.V1_0.IWifiChipEventCallback.kInterfaceName);
                    onDebugErrorAlert(_hidl_request.readInt32(), _hidl_request.readInt8Vector());
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
                    _hidl_request.enforceInterface(IWifiChipEventCallback.kInterfaceName);
                    onRadioModeChange(RadioModeInfo.readVectorFromParcel(_hidl_request));
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

    DebugInfo getDebugInfo() throws RemoteException;

    ArrayList<byte[]> getHashChain() throws RemoteException;

    ArrayList<String> interfaceChain() throws RemoteException;

    String interfaceDescriptor() throws RemoteException;

    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    void notifySyspropsChanged() throws RemoteException;

    void onRadioModeChange(ArrayList<RadioModeInfo> arrayList) throws RemoteException;

    void ping() throws RemoteException;

    void setHALInstrumentation() throws RemoteException;

    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static IWifiChipEventCallback asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IWifiChipEventCallback)) {
            return (IWifiChipEventCallback) iface;
        }
        IWifiChipEventCallback proxy = new Proxy(binder);
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

    static IWifiChipEventCallback castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IWifiChipEventCallback getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IWifiChipEventCallback getService(boolean retry) throws RemoteException {
        return getService(HalDeviceManager.HAL_INSTANCE_NAME, retry);
    }

    static IWifiChipEventCallback getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IWifiChipEventCallback getService() throws RemoteException {
        return getService(HalDeviceManager.HAL_INSTANCE_NAME);
    }
}
