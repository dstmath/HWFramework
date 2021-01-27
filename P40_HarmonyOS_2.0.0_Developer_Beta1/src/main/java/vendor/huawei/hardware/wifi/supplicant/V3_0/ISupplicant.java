package vendor.huawei.hardware.wifi.supplicant.V3_0;

import android.hardware.wifi.supplicant.V1_0.ISupplicant;
import android.hardware.wifi.supplicant.V1_0.ISupplicantCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.hardware.wifi.supplicant.V1_1.ISupplicant;
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

public interface ISupplicant extends android.hardware.wifi.supplicant.V1_2.ISupplicant {
    public static final String kInterfaceName = "vendor.huawei.hardware.wifi.supplicant@3.0::ISupplicant";

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    @Override // android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static ISupplicant asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof ISupplicant)) {
            return (ISupplicant) iface;
        }
        ISupplicant proxy = new Proxy(binder);
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

    static ISupplicant castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static ISupplicant getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static ISupplicant getService(boolean retry) throws RemoteException {
        return getService("default", retry);
    }

    static ISupplicant getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static ISupplicant getService() throws RemoteException {
        return getService("default");
    }

    public static final class Proxy implements ISupplicant {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.huawei.hardware.wifi.supplicant@3.0::ISupplicant]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicant
        public void getInterface(ISupplicant.IfaceInfo ifaceInfo, ISupplicant.getInterfaceCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicant.kInterfaceName);
            ifaceInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, ISupplicantIface.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicant
        public void listInterfaces(ISupplicant.listInterfacesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicant.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, ISupplicant.IfaceInfo.readVectorFromParcel(_hidl_reply));
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicant
        public SupplicantStatus registerCallback(ISupplicantCallback callback) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicant.kInterfaceName);
            _hidl_request.writeStrongBinder(callback == null ? null : callback.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicant
        public SupplicantStatus setDebugParams(int level, boolean showTimestamp, boolean showKeys) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicant.kInterfaceName);
            _hidl_request.writeInt32(level);
            _hidl_request.writeBool(showTimestamp);
            _hidl_request.writeBool(showKeys);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicant
        public int getDebugLevel() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicant.kInterfaceName);
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

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicant
        public boolean isDebugShowTimestampEnabled() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicant.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicant
        public boolean isDebugShowKeysEnabled() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicant.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicant
        public SupplicantStatus setConcurrencyPriority(int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_0.ISupplicant.kInterfaceName);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_1.ISupplicant
        public void addInterface(ISupplicant.IfaceInfo ifaceInfo, ISupplicant.addInterfaceCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_1.ISupplicant.kInterfaceName);
            ifaceInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, ISupplicantIface.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_1.ISupplicant
        public SupplicantStatus removeInterface(ISupplicant.IfaceInfo ifaceInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_1.ISupplicant.kInterfaceName);
            ifaceInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.supplicant.V1_1.ISupplicant
        public void terminate() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.wifi.supplicant.V1_1.ISupplicant.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements ISupplicant {
        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(ISupplicant.kInterfaceName, android.hardware.wifi.supplicant.V1_2.ISupplicant.kInterfaceName, android.hardware.wifi.supplicant.V1_1.ISupplicant.kInterfaceName, android.hardware.wifi.supplicant.V1_0.ISupplicant.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return ISupplicant.kInterfaceName;
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{26, 91, 52, -51, 13, 63, -94, 107, 49, 97, -59, 86, -55, -28, 29, 22, 18, 7, 39, 103, 74, 9, -120, -124, -46, 99, -122, -50, 58, 40, -80, 123}, new byte[]{6, 123, 34, -17, -59, 5, 41, -88, -115, 101, 15, -25, 64, 6, 3, 66, -99, 17, 100, -92, 126, -23, 106, 23, 71, 111, -37, 10, -83, -42, -76, -45}, new byte[]{-29, 98, 32, 59, -108, 31, 24, -67, 76, -70, 41, -90, 42, -33, -96, 36, 83, -19, 0, -42, -66, 91, 114, -51, -74, -60, -41, -32, -65, 57, 74, 64}, new byte[]{-9, -27, 92, 8, 24, 125, -116, -123, 80, 104, -95, -18, 61, 12, -115, -82, -18, 117, 112, 41, 45, -106, 80, -100, 33, -88, 117, 109, 79, 92, -5, -101}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant, android.hardware.wifi.supplicant.V1_2.ISupplicant, android.hardware.wifi.supplicant.V1_1.ISupplicant, android.hardware.wifi.supplicant.V1_0.ISupplicant, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (ISupplicant.kInterfaceName.equals(descriptor)) {
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicant.kInterfaceName);
                    ISupplicant.IfaceInfo ifaceInfo = new ISupplicant.IfaceInfo();
                    ifaceInfo.readFromParcel(_hidl_request);
                    getInterface(ifaceInfo, new ISupplicant.getInterfaceCallback() {
                        /* class vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant.Stub.AnonymousClass1 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicant.getInterfaceCallback
                        public void onValues(SupplicantStatus status, ISupplicantIface iface) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(iface == null ? null : iface.asBinder());
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicant.kInterfaceName);
                    listInterfaces(new ISupplicant.listInterfacesCallback() {
                        /* class vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant.Stub.AnonymousClass2 */

                        @Override // android.hardware.wifi.supplicant.V1_0.ISupplicant.listInterfacesCallback
                        public void onValues(SupplicantStatus status, ArrayList<ISupplicant.IfaceInfo> ifaces) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            ISupplicant.IfaceInfo.writeVectorToParcel(_hidl_reply, ifaces);
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicant.kInterfaceName);
                    SupplicantStatus _hidl_out_status = registerCallback(ISupplicantCallback.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 4:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicant.kInterfaceName);
                    SupplicantStatus _hidl_out_status2 = setDebugParams(_hidl_request.readInt32(), _hidl_request.readBool(), _hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status2.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 5:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicant.kInterfaceName);
                    int _hidl_out_level = getDebugLevel();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_level);
                    _hidl_reply.send();
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicant.kInterfaceName);
                    boolean _hidl_out_enabled = isDebugShowTimestampEnabled();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_enabled);
                    _hidl_reply.send();
                    return;
                case 7:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicant.kInterfaceName);
                    boolean _hidl_out_enabled2 = isDebugShowKeysEnabled();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_enabled2);
                    _hidl_reply.send();
                    return;
                case 8:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_0.ISupplicant.kInterfaceName);
                    SupplicantStatus _hidl_out_status3 = setConcurrencyPriority(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status3.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 9:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_1.ISupplicant.kInterfaceName);
                    ISupplicant.IfaceInfo ifaceInfo2 = new ISupplicant.IfaceInfo();
                    ifaceInfo2.readFromParcel(_hidl_request);
                    addInterface(ifaceInfo2, new ISupplicant.addInterfaceCallback() {
                        /* class vendor.huawei.hardware.wifi.supplicant.V3_0.ISupplicant.Stub.AnonymousClass3 */

                        @Override // android.hardware.wifi.supplicant.V1_1.ISupplicant.addInterfaceCallback
                        public void onValues(SupplicantStatus status, ISupplicantIface iface) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(iface == null ? null : iface.asBinder());
                            _hidl_reply.send();
                        }
                    });
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
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_1.ISupplicant.kInterfaceName);
                    ISupplicant.IfaceInfo ifaceInfo3 = new ISupplicant.IfaceInfo();
                    ifaceInfo3.readFromParcel(_hidl_request);
                    SupplicantStatus _hidl_out_status4 = removeInterface(ifaceInfo3);
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status4.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 11:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(android.hardware.wifi.supplicant.V1_1.ISupplicant.kInterfaceName);
                    terminate();
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
