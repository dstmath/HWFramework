package vendor.huawei.hardware.weaver.V1_0;

import android.hardware.weaver.V1_0.IWeaver;
import android.hardware.weaver.V1_0.WeaverConfig;
import android.hardware.weaver.V1_0.WeaverReadResponse;
import android.hidl.base.V1_0.DebugInfo;
import android.os.HidlSupport;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.NativeHandle;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public interface IWeaver extends android.hardware.weaver.V1_0.IWeaver {
    public static final String kInterfaceName = "vendor.huawei.hardware.weaver@1.0::IWeaver";

    @FunctionalInterface
    public interface getStatusCallback {
        void onValues(int i, WeaverSlotStatus weaverSlotStatus);
    }

    IHwBinder asBinder();

    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    DebugInfo getDebugInfo() throws RemoteException;

    ArrayList<byte[]> getHashChain() throws RemoteException;

    void getStatus(int i, getStatusCallback getstatuscallback) throws RemoteException;

    int getWeaverHardErr() throws RemoteException;

    ArrayList<String> interfaceChain() throws RemoteException;

    String interfaceDescriptor() throws RemoteException;

    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    void notifySyspropsChanged() throws RemoteException;

    void ping() throws RemoteException;

    void setHALInstrumentation() throws RemoteException;

    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static IWeaver asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IWeaver)) {
            return (IWeaver) iface;
        }
        IWeaver proxy = new Proxy(binder);
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

    static IWeaver castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IWeaver getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IWeaver getService(boolean retry) throws RemoteException {
        return getService("default", retry);
    }

    static IWeaver getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IWeaver getService() throws RemoteException {
        return getService("default");
    }

    public static final class Proxy implements IWeaver {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.huawei.hardware.weaver@1.0::IWeaver]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        public void getConfig(IWeaver.getConfigCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hardware.weaver@1.0::IWeaver");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                WeaverConfig _hidl_out_config = new WeaverConfig();
                _hidl_out_config.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_out_config);
            } finally {
                _hidl_reply.release();
            }
        }

        public int write(int slotId, ArrayList<Byte> key, ArrayList<Byte> value) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hardware.weaver@1.0::IWeaver");
            _hidl_request.writeInt32(slotId);
            _hidl_request.writeInt8Vector(key);
            _hidl_request.writeInt8Vector(value);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        public void read(int slotId, ArrayList<Byte> key, IWeaver.readCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hardware.weaver@1.0::IWeaver");
            _hidl_request.writeInt32(slotId);
            _hidl_request.writeInt8Vector(key);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                WeaverReadResponse _hidl_out_readResponse = new WeaverReadResponse();
                _hidl_out_readResponse.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_out_readResponse);
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
        public void getStatus(int slotId, getStatusCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWeaver.kInterfaceName);
            _hidl_request.writeInt32(slotId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                WeaverSlotStatus _hidl_out_statusResult = new WeaverSlotStatus();
                _hidl_out_statusResult.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_out_statusResult);
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
        public int getWeaverHardErr() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWeaver.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
        public ArrayList<String> interfaceChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
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

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
        public void debug(NativeHandle fd, ArrayList<String> options) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
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

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
        public String interfaceDescriptor() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
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

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
        public ArrayList<byte[]> getHashChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
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

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
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

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
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

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
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

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
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

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IWeaver {
        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
        public IHwBinder asBinder() {
            return this;
        }

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IWeaver.kInterfaceName, "android.hardware.weaver@1.0::IWeaver", "android.hidl.base@1.0::IBase"));
        }

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
        public final String interfaceDescriptor() {
            return IWeaver.kInterfaceName;
        }

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{-113, -50, 110, -97, 91, 11, -10, 118, 106, -126, -76, -81, 79, -110, 85, 113, 32, -17, -34, -9, -110, -75, 97, 99, -42, 81, -38, 119, -23, 108, 75, -81}, new byte[]{-101, -60, 52, 19, -72, 12, -48, -59, -102, 2, 46, -109, -38, 20, 72, -36, -72, 45, -47, 12, 109, -45, 25, 50, -33, 70, 89, -28, -67, -53, 19, 104}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
        public final void setHALInstrumentation() {
        }

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
        public final void ping() {
        }

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IWeaver.kInterfaceName.equals(descriptor)) {
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
            if (_hidl_code == 1) {
                if ((_hidl_flags & 1) != 0) {
                    _hidl_is_oneway = true;
                }
                if (_hidl_is_oneway) {
                    _hidl_reply.writeStatus(Integer.MIN_VALUE);
                    _hidl_reply.send();
                    return;
                }
                _hidl_request.enforceInterface("android.hardware.weaver@1.0::IWeaver");
                getConfig(new IWeaver.getConfigCallback() {
                    /* class vendor.huawei.hardware.weaver.V1_0.IWeaver.Stub.AnonymousClass1 */

                    public void onValues(int status, WeaverConfig config) {
                        _hidl_reply.writeStatus(0);
                        _hidl_reply.writeInt32(status);
                        config.writeToParcel(_hidl_reply);
                        _hidl_reply.send();
                    }
                });
            } else if (_hidl_code == 2) {
                if ((_hidl_flags & 1) == 0) {
                    _hidl_is_oneway2 = false;
                }
                if (_hidl_is_oneway2) {
                    _hidl_reply.writeStatus(Integer.MIN_VALUE);
                    _hidl_reply.send();
                    return;
                }
                _hidl_request.enforceInterface("android.hardware.weaver@1.0::IWeaver");
                int _hidl_out_status = write(_hidl_request.readInt32(), _hidl_request.readInt8Vector(), _hidl_request.readInt8Vector());
                _hidl_reply.writeStatus(0);
                _hidl_reply.writeInt32(_hidl_out_status);
                _hidl_reply.send();
            } else if (_hidl_code == 3) {
                if ((_hidl_flags & 1) != 0) {
                    _hidl_is_oneway = true;
                }
                if (_hidl_is_oneway) {
                    _hidl_reply.writeStatus(Integer.MIN_VALUE);
                    _hidl_reply.send();
                    return;
                }
                _hidl_request.enforceInterface("android.hardware.weaver@1.0::IWeaver");
                read(_hidl_request.readInt32(), _hidl_request.readInt8Vector(), new IWeaver.readCallback() {
                    /* class vendor.huawei.hardware.weaver.V1_0.IWeaver.Stub.AnonymousClass2 */

                    public void onValues(int status, WeaverReadResponse readResponse) {
                        _hidl_reply.writeStatus(0);
                        _hidl_reply.writeInt32(status);
                        readResponse.writeToParcel(_hidl_reply);
                        _hidl_reply.send();
                    }
                });
            } else if (_hidl_code == 4) {
                if ((_hidl_flags & 1) != 0) {
                    _hidl_is_oneway = true;
                }
                if (_hidl_is_oneway) {
                    _hidl_reply.writeStatus(Integer.MIN_VALUE);
                    _hidl_reply.send();
                    return;
                }
                _hidl_request.enforceInterface(IWeaver.kInterfaceName);
                getStatus(_hidl_request.readInt32(), new getStatusCallback() {
                    /* class vendor.huawei.hardware.weaver.V1_0.IWeaver.Stub.AnonymousClass3 */

                    @Override // vendor.huawei.hardware.weaver.V1_0.IWeaver.getStatusCallback
                    public void onValues(int status, WeaverSlotStatus statusResult) {
                        _hidl_reply.writeStatus(0);
                        _hidl_reply.writeInt32(status);
                        statusResult.writeToParcel(_hidl_reply);
                        _hidl_reply.send();
                    }
                });
            } else if (_hidl_code != 5) {
                switch (_hidl_code) {
                    case 256067662:
                        if ((_hidl_flags & 1) == 0) {
                            _hidl_is_oneway2 = false;
                        }
                        if (_hidl_is_oneway2) {
                            _hidl_reply.writeStatus(Integer.MIN_VALUE);
                            _hidl_reply.send();
                            return;
                        }
                        _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
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
                            _hidl_reply.writeStatus(Integer.MIN_VALUE);
                            _hidl_reply.send();
                            return;
                        }
                        _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                        debug(_hidl_request.readNativeHandle(), _hidl_request.readStringVector());
                        _hidl_reply.writeStatus(0);
                        _hidl_reply.send();
                        return;
                    case 256136003:
                        if ((_hidl_flags & 1) == 0) {
                            _hidl_is_oneway2 = false;
                        }
                        if (_hidl_is_oneway2) {
                            _hidl_reply.writeStatus(Integer.MIN_VALUE);
                            _hidl_reply.send();
                            return;
                        }
                        _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
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
                            _hidl_reply.writeStatus(Integer.MIN_VALUE);
                            _hidl_reply.send();
                            return;
                        }
                        _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
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
                            _hidl_reply.writeStatus(Integer.MIN_VALUE);
                            _hidl_reply.send();
                            return;
                        }
                        _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                        setHALInstrumentation();
                        return;
                    case 256660548:
                        if ((_hidl_flags & 1) != 0) {
                            _hidl_is_oneway = true;
                        }
                        if (_hidl_is_oneway) {
                            _hidl_reply.writeStatus(Integer.MIN_VALUE);
                            _hidl_reply.send();
                            return;
                        }
                        return;
                    case 256921159:
                        if ((_hidl_flags & 1) == 0) {
                            _hidl_is_oneway2 = false;
                        }
                        if (_hidl_is_oneway2) {
                            _hidl_reply.writeStatus(Integer.MIN_VALUE);
                            _hidl_reply.send();
                            return;
                        }
                        _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                        ping();
                        _hidl_reply.writeStatus(0);
                        _hidl_reply.send();
                        return;
                    case 257049926:
                        if ((_hidl_flags & 1) == 0) {
                            _hidl_is_oneway2 = false;
                        }
                        if (_hidl_is_oneway2) {
                            _hidl_reply.writeStatus(Integer.MIN_VALUE);
                            _hidl_reply.send();
                            return;
                        }
                        _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
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
                            _hidl_reply.writeStatus(Integer.MIN_VALUE);
                            _hidl_reply.send();
                            return;
                        }
                        _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                        notifySyspropsChanged();
                        return;
                    case 257250372:
                        if ((_hidl_flags & 1) != 0) {
                            _hidl_is_oneway = true;
                        }
                        if (_hidl_is_oneway) {
                            _hidl_reply.writeStatus(Integer.MIN_VALUE);
                            _hidl_reply.send();
                            return;
                        }
                        return;
                    default:
                        return;
                }
            } else {
                if ((_hidl_flags & 1) == 0) {
                    _hidl_is_oneway2 = false;
                }
                if (_hidl_is_oneway2) {
                    _hidl_reply.writeStatus(Integer.MIN_VALUE);
                    _hidl_reply.send();
                    return;
                }
                _hidl_request.enforceInterface(IWeaver.kInterfaceName);
                int _hidl_out_status2 = getWeaverHardErr();
                _hidl_reply.writeStatus(0);
                _hidl_reply.writeInt32(_hidl_out_status2);
                _hidl_reply.send();
            }
        }
    }
}
