package android.hidl.manager.V1_0;

import android.hidl.base.V1_0.DebugInfo;
import android.hidl.base.V1_0.DebugInfo.Architecture;
import android.hidl.base.V1_0.IBase;
import android.os.HidlSupport;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwBinder.DeathRecipient;
import android.os.IHwInterface;
import android.os.RemoteException;
import android.os.SystemProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public interface IServiceManager extends IBase {
    public static final String kInterfaceName = "android.hidl.manager@1.0::IServiceManager";

    public static final class InstanceDebugInfo {
        public int arch;
        public final ArrayList<Integer> clientPids = new ArrayList();
        public String instanceName = new String();
        public String interfaceName = new String();
        public int pid;

        public final boolean equals(Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (otherObject == null || otherObject.getClass() != InstanceDebugInfo.class) {
                return false;
            }
            InstanceDebugInfo other = (InstanceDebugInfo) otherObject;
            return HidlSupport.deepEquals(this.interfaceName, other.interfaceName) && HidlSupport.deepEquals(this.instanceName, other.instanceName) && this.pid == other.pid && HidlSupport.deepEquals(this.clientPids, other.clientPids) && this.arch == other.arch;
        }

        public final int hashCode() {
            return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.interfaceName)), Integer.valueOf(HidlSupport.deepHashCode(this.instanceName)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.pid))), Integer.valueOf(HidlSupport.deepHashCode(this.clientPids)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.arch)))});
        }

        public final String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            builder.append(".interfaceName = ");
            builder.append(this.interfaceName);
            builder.append(", .instanceName = ");
            builder.append(this.instanceName);
            builder.append(", .pid = ");
            builder.append(this.pid);
            builder.append(", .clientPids = ");
            builder.append(this.clientPids);
            builder.append(", .arch = ");
            builder.append(Architecture.toString(this.arch));
            builder.append("}");
            return builder.toString();
        }

        public final void readFromParcel(HwParcel parcel) {
            readEmbeddedFromParcel(parcel, parcel.readBuffer(64), 0);
        }

        public static final ArrayList<InstanceDebugInfo> readVectorFromParcel(HwParcel parcel) {
            ArrayList<InstanceDebugInfo> _hidl_vec = new ArrayList();
            HwBlob _hidl_blob = parcel.readBuffer(16);
            int _hidl_vec_size = _hidl_blob.getInt32(8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 64), _hidl_blob.handle(), 0, true);
            _hidl_vec.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                InstanceDebugInfo _hidl_vec_element = new InstanceDebugInfo();
                _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 64));
                _hidl_vec.add(_hidl_vec_element);
            }
            return _hidl_vec;
        }

        public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
            this.interfaceName = _hidl_blob.getString(0 + _hidl_offset);
            parcel.readEmbeddedBuffer((long) (this.interfaceName.getBytes().length + 1), _hidl_blob.handle(), (0 + _hidl_offset) + 0, false);
            this.instanceName = _hidl_blob.getString(16 + _hidl_offset);
            parcel.readEmbeddedBuffer((long) (this.instanceName.getBytes().length + 1), _hidl_blob.handle(), (16 + _hidl_offset) + 0, false);
            this.pid = _hidl_blob.getInt32(32 + _hidl_offset);
            int _hidl_vec_size = _hidl_blob.getInt32((40 + _hidl_offset) + 8);
            HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 4), _hidl_blob.handle(), (40 + _hidl_offset) + 0, true);
            this.clientPids.clear();
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                this.clientPids.add(Integer.valueOf(childBlob.getInt32((long) (_hidl_index_0 * 4))));
            }
            this.arch = _hidl_blob.getInt32(56 + _hidl_offset);
        }

        public final void writeToParcel(HwParcel parcel) {
            HwBlob _hidl_blob = new HwBlob(64);
            writeEmbeddedToBlob(_hidl_blob, 0);
            parcel.writeBuffer(_hidl_blob);
        }

        public static final void writeVectorToParcel(HwParcel parcel, ArrayList<InstanceDebugInfo> _hidl_vec) {
            HwBlob _hidl_blob = new HwBlob(16);
            int _hidl_vec_size = _hidl_vec.size();
            _hidl_blob.putInt32(8, _hidl_vec_size);
            _hidl_blob.putBool(12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 64);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                ((InstanceDebugInfo) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 64));
            }
            _hidl_blob.putBlob(0, childBlob);
            parcel.writeBuffer(_hidl_blob);
        }

        public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
            _hidl_blob.putString(_hidl_offset + 0, this.interfaceName);
            _hidl_blob.putString(16 + _hidl_offset, this.instanceName);
            _hidl_blob.putInt32(32 + _hidl_offset, this.pid);
            int _hidl_vec_size = this.clientPids.size();
            _hidl_blob.putInt32((_hidl_offset + 40) + 8, _hidl_vec_size);
            _hidl_blob.putBool((_hidl_offset + 40) + 12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 4);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                childBlob.putInt32((long) (_hidl_index_0 * 4), ((Integer) this.clientPids.get(_hidl_index_0)).intValue());
            }
            _hidl_blob.putBlob((_hidl_offset + 40) + 0, childBlob);
            _hidl_blob.putInt32(56 + _hidl_offset, this.arch);
        }
    }

    public static final class PidConstant {
        public static final int NO_PID = -1;

        public static final String toString(int o) {
            if (o == -1) {
                return "NO_PID";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList();
            int flipped = 0;
            if ((o & -1) == -1) {
                list.add("NO_PID");
                flipped = -1;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class Proxy implements IServiceManager {
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
                return "[class or subclass of android.hidl.manager@1.0::IServiceManager]@Proxy";
            }
        }

        public IBase get(String fqName, String name) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IServiceManager.kInterfaceName);
            _hidl_request.writeString(fqName);
            _hidl_request.writeString(name);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                IBase _hidl_out_service = IBase.asInterface(_hidl_reply.readStrongBinder());
                return _hidl_out_service;
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean add(String name, IBase service) throws RemoteException {
            IHwBinder iHwBinder = null;
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IServiceManager.kInterfaceName);
            _hidl_request.writeString(name);
            if (service != null) {
                iHwBinder = service.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                boolean _hidl_out_success = _hidl_reply.readBool();
                return _hidl_out_success;
            } finally {
                _hidl_reply.release();
            }
        }

        public byte getTransport(String fqName, String name) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IServiceManager.kInterfaceName);
            _hidl_request.writeString(fqName);
            _hidl_request.writeString(name);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                byte _hidl_out_transport = _hidl_reply.readInt8();
                return _hidl_out_transport;
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<String> list() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IServiceManager.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<String> _hidl_out_fqInstanceNames = _hidl_reply.readStringVector();
                return _hidl_out_fqInstanceNames;
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<String> listByInterface(String fqName) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IServiceManager.kInterfaceName);
            _hidl_request.writeString(fqName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<String> _hidl_out_instanceNames = _hidl_reply.readStringVector();
                return _hidl_out_instanceNames;
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean registerForNotifications(String fqName, String name, IServiceNotification callback) throws RemoteException {
            IHwBinder iHwBinder = null;
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IServiceManager.kInterfaceName);
            _hidl_request.writeString(fqName);
            _hidl_request.writeString(name);
            if (callback != null) {
                iHwBinder = callback.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                boolean _hidl_out_success = _hidl_reply.readBool();
                return _hidl_out_success;
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<InstanceDebugInfo> debugDump() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IServiceManager.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<InstanceDebugInfo> _hidl_out_info = InstanceDebugInfo.readVectorFromParcel(_hidl_reply);
                return _hidl_out_info;
            } finally {
                _hidl_reply.release();
            }
        }

        public void registerPassthroughClient(String fqName, String name) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IServiceManager.kInterfaceName);
            _hidl_request.writeString(fqName);
            _hidl_request.writeString(name);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<String> interfaceChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
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
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
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
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
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
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
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
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
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
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
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
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
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

    public static abstract class Stub extends HwBinder implements IServiceManager {
        public IHwBinder asBinder() {
            return this;
        }

        public final ArrayList<String> interfaceChain() {
            return new ArrayList(Arrays.asList(new String[]{IServiceManager.kInterfaceName, "android.hidl.base@1.0::IBase"}));
        }

        public final String interfaceDescriptor() {
            return IServiceManager.kInterfaceName;
        }

        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList(Arrays.asList(new byte[][]{new byte[]{(byte) 77, (byte) 4, (byte) 106, (byte) 89, (byte) -114, (byte) -123, (byte) -15, (byte) -62, (byte) -45, (byte) -125, (byte) -61, (byte) -87, (byte) 9, (byte) 108, (byte) 60, (byte) 5, (byte) 120, (byte) -23, (byte) 116, (byte) 88, (byte) 7, (byte) 46, (byte) -25, (byte) -26, Byte.MAX_VALUE, (byte) 112, (byte) 78, (byte) -103, (byte) -43, (byte) -5, (byte) 13, (byte) 63}, new byte[]{(byte) -67, (byte) -38, (byte) -74, (byte) 24, (byte) 77, (byte) 122, (byte) 52, (byte) 109, (byte) -90, (byte) -96, (byte) 125, (byte) -64, (byte) -126, (byte) -116, (byte) -15, (byte) -102, (byte) 105, (byte) 111, (byte) 76, (byte) -86, (byte) 54, (byte) 17, (byte) -59, (byte) 31, (byte) 46, (byte) 20, (byte) 86, (byte) 90, (byte) 20, (byte) -76, (byte) 15, (byte) -39}}));
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
            if (IServiceManager.kInterfaceName.equals(descriptor)) {
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
            boolean _hidl_out_success;
            switch (_hidl_code) {
                case 1:
                    _hidl_request.enforceInterface(IServiceManager.kInterfaceName);
                    IBase _hidl_out_service = get(_hidl_request.readString(), _hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeStrongBinder(_hidl_out_service == null ? null : _hidl_out_service.asBinder());
                    _hidl_reply.send();
                    return;
                case 2:
                    _hidl_request.enforceInterface(IServiceManager.kInterfaceName);
                    _hidl_out_success = add(_hidl_request.readString(), IBase.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_success);
                    _hidl_reply.send();
                    return;
                case 3:
                    _hidl_request.enforceInterface(IServiceManager.kInterfaceName);
                    byte _hidl_out_transport = getTransport(_hidl_request.readString(), _hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt8(_hidl_out_transport);
                    _hidl_reply.send();
                    return;
                case 4:
                    _hidl_request.enforceInterface(IServiceManager.kInterfaceName);
                    ArrayList<String> _hidl_out_fqInstanceNames = list();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeStringVector(_hidl_out_fqInstanceNames);
                    _hidl_reply.send();
                    return;
                case 5:
                    _hidl_request.enforceInterface(IServiceManager.kInterfaceName);
                    ArrayList<String> _hidl_out_instanceNames = listByInterface(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeStringVector(_hidl_out_instanceNames);
                    _hidl_reply.send();
                    return;
                case 6:
                    _hidl_request.enforceInterface(IServiceManager.kInterfaceName);
                    _hidl_out_success = registerForNotifications(_hidl_request.readString(), _hidl_request.readString(), IServiceNotification.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_success);
                    _hidl_reply.send();
                    return;
                case 7:
                    _hidl_request.enforceInterface(IServiceManager.kInterfaceName);
                    ArrayList<InstanceDebugInfo> _hidl_out_info = debugDump();
                    _hidl_reply.writeStatus(0);
                    InstanceDebugInfo.writeVectorToParcel(_hidl_reply, _hidl_out_info);
                    _hidl_reply.send();
                    return;
                case 8:
                    _hidl_request.enforceInterface(IServiceManager.kInterfaceName);
                    registerPassthroughClient(_hidl_request.readString(), _hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 256067662:
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    ArrayList<String> _hidl_out_descriptors = interfaceChain();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeStringVector(_hidl_out_descriptors);
                    _hidl_reply.send();
                    return;
                case 256131655:
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 256136003:
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    String _hidl_out_descriptor = interfaceDescriptor();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeString(_hidl_out_descriptor);
                    _hidl_reply.send();
                    return;
                case 256398152:
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    ArrayList<byte[]> _hidl_out_hashchain = getHashChain();
                    _hidl_reply.writeStatus(0);
                    HwBlob _hidl_blob = new HwBlob(16);
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
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    setHALInstrumentation();
                    return;
                case 257049926:
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    DebugInfo _hidl_out_info2 = getDebugInfo();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_info2.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 257120595:
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    notifySyspropsChanged();
                    return;
                default:
                    return;
            }
        }
    }

    public static final class Transport {
        public static final byte EMPTY = (byte) 0;
        public static final byte HWBINDER = (byte) 1;
        public static final byte PASSTHROUGH = (byte) 2;

        public static final String toString(byte o) {
            if (o == (byte) 0) {
                return "EMPTY";
            }
            if (o == (byte) 1) {
                return "HWBINDER";
            }
            if (o == (byte) 2) {
                return "PASSTHROUGH";
            }
            return "0x" + Integer.toHexString(Byte.toUnsignedInt(o));
        }

        public static final String dumpBitfield(byte o) {
            ArrayList<String> list = new ArrayList();
            byte flipped = (byte) 0;
            if ((o & 0) == 0) {
                list.add("EMPTY");
                flipped = (byte) 0;
            }
            if ((o & 1) == 1) {
                list.add("HWBINDER");
                flipped = (byte) (flipped | 1);
            }
            if ((o & 2) == 2) {
                list.add("PASSTHROUGH");
                flipped = (byte) (flipped | 2);
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString(Byte.toUnsignedInt((byte) ((~flipped) & o))));
            }
            return String.join(" | ", list);
        }
    }

    boolean add(String str, IBase iBase) throws RemoteException;

    IHwBinder asBinder();

    ArrayList<InstanceDebugInfo> debugDump() throws RemoteException;

    IBase get(String str, String str2) throws RemoteException;

    DebugInfo getDebugInfo() throws RemoteException;

    ArrayList<byte[]> getHashChain() throws RemoteException;

    byte getTransport(String str, String str2) throws RemoteException;

    ArrayList<String> interfaceChain() throws RemoteException;

    String interfaceDescriptor() throws RemoteException;

    boolean linkToDeath(DeathRecipient deathRecipient, long j) throws RemoteException;

    ArrayList<String> list() throws RemoteException;

    ArrayList<String> listByInterface(String str) throws RemoteException;

    void notifySyspropsChanged() throws RemoteException;

    void ping() throws RemoteException;

    boolean registerForNotifications(String str, String str2, IServiceNotification iServiceNotification) throws RemoteException;

    void registerPassthroughClient(String str, String str2) throws RemoteException;

    void setHALInstrumentation() throws RemoteException;

    boolean unlinkToDeath(DeathRecipient deathRecipient) throws RemoteException;

    static IServiceManager asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IServiceManager)) {
            return (IServiceManager) iface;
        }
        IServiceManager proxy = new Proxy(binder);
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

    static IServiceManager castFrom(IHwInterface iface) {
        return iface == null ? null : asInterface(iface.asBinder());
    }

    static IServiceManager getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IServiceManager getService() throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, "default"));
    }
}
