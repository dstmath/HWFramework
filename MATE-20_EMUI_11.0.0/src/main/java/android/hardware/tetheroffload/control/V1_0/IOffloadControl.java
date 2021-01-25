package android.hardware.tetheroffload.control.V1_0;

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
import com.android.server.BatteryService;
import com.android.server.usb.descriptors.UsbASFormat;
import com.android.server.usb.descriptors.UsbDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public interface IOffloadControl extends IBase {
    public static final String kInterfaceName = "android.hardware.tetheroffload.control@1.0::IOffloadControl";

    @FunctionalInterface
    public interface addDownstreamCallback {
        void onValues(boolean z, String str);
    }

    @FunctionalInterface
    public interface getForwardedStatsCallback {
        void onValues(long j, long j2);
    }

    @FunctionalInterface
    public interface initOffloadCallback {
        void onValues(boolean z, String str);
    }

    @FunctionalInterface
    public interface removeDownstreamCallback {
        void onValues(boolean z, String str);
    }

    @FunctionalInterface
    public interface setDataLimitCallback {
        void onValues(boolean z, String str);
    }

    @FunctionalInterface
    public interface setLocalPrefixesCallback {
        void onValues(boolean z, String str);
    }

    @FunctionalInterface
    public interface setUpstreamParametersCallback {
        void onValues(boolean z, String str);
    }

    @FunctionalInterface
    public interface stopOffloadCallback {
        void onValues(boolean z, String str);
    }

    void addDownstream(String str, String str2, addDownstreamCallback adddownstreamcallback) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    @Override // android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    void getForwardedStats(String str, getForwardedStatsCallback getforwardedstatscallback) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    void initOffload(ITetheringOffloadCallback iTetheringOffloadCallback, initOffloadCallback initoffloadcallback) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    void removeDownstream(String str, String str2, removeDownstreamCallback removedownstreamcallback) throws RemoteException;

    void setDataLimit(String str, long j, setDataLimitCallback setdatalimitcallback) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    void setLocalPrefixes(ArrayList<String> arrayList, setLocalPrefixesCallback setlocalprefixescallback) throws RemoteException;

    void setUpstreamParameters(String str, String str2, String str3, ArrayList<String> arrayList, setUpstreamParametersCallback setupstreamparameterscallback) throws RemoteException;

    void stopOffload(stopOffloadCallback stopoffloadcallback) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static IOffloadControl asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IOffloadControl)) {
            return (IOffloadControl) iface;
        }
        IOffloadControl proxy = new Proxy(binder);
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

    static IOffloadControl castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IOffloadControl getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IOffloadControl getService(boolean retry) throws RemoteException {
        return getService(BatteryService.HealthServiceWrapper.INSTANCE_VENDOR, retry);
    }

    static IOffloadControl getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IOffloadControl getService() throws RemoteException {
        return getService(BatteryService.HealthServiceWrapper.INSTANCE_VENDOR);
    }

    public static final class Proxy implements IOffloadControl {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of android.hardware.tetheroffload.control@1.0::IOffloadControl]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl
        public void initOffload(ITetheringOffloadCallback cb, initOffloadCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IOffloadControl.kInterfaceName);
            _hidl_request.writeStrongBinder(cb == null ? null : cb.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readBool(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl
        public void stopOffload(stopOffloadCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IOffloadControl.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readBool(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl
        public void setLocalPrefixes(ArrayList<String> prefixes, setLocalPrefixesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IOffloadControl.kInterfaceName);
            _hidl_request.writeStringVector(prefixes);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readBool(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl
        public void getForwardedStats(String upstream, getForwardedStatsCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IOffloadControl.kInterfaceName);
            _hidl_request.writeString(upstream);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt64(), _hidl_reply.readInt64());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl
        public void setDataLimit(String upstream, long limit, setDataLimitCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IOffloadControl.kInterfaceName);
            _hidl_request.writeString(upstream);
            _hidl_request.writeInt64(limit);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readBool(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl
        public void setUpstreamParameters(String iface, String v4Addr, String v4Gw, ArrayList<String> v6Gws, setUpstreamParametersCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IOffloadControl.kInterfaceName);
            _hidl_request.writeString(iface);
            _hidl_request.writeString(v4Addr);
            _hidl_request.writeString(v4Gw);
            _hidl_request.writeStringVector(v6Gws);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readBool(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl
        public void addDownstream(String iface, String prefix, addDownstreamCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IOffloadControl.kInterfaceName);
            _hidl_request.writeString(iface);
            _hidl_request.writeString(prefix);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readBool(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl
        public void removeDownstream(String iface, String prefix, removeDownstreamCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IOffloadControl.kInterfaceName);
            _hidl_request.writeString(iface);
            _hidl_request.writeString(prefix);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readBool(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IOffloadControl {
        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IOffloadControl.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return IOffloadControl.kInterfaceName;
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{68, 123, 0, UsbDescriptor.DESCRIPTORTYPE_ENDPOINT_COMPANION, 107, -55, 90, 122, -81, -20, 29, 102, UsbDescriptor.DESCRIPTORTYPE_BOS, 111, 62, -97, 118, -84, -117, -64, 53, 49, -109, 67, 94, 85, 121, -85, UsbASFormat.EXT_FORMAT_TYPE_III, 61, -90, 25}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, UsbDescriptor.DESCRIPTORTYPE_PHYSICAL, -17, 5, UsbDescriptor.DESCRIPTORTYPE_AUDIO_INTERFACE, -13, -51, 105, 87, 19, -109, UsbDescriptor.DESCRIPTORTYPE_AUDIO_INTERFACE, -72, 59, 24, -54, 76}));
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IOffloadControl.kInterfaceName.equals(descriptor)) {
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
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IOffloadControl.kInterfaceName);
                    initOffload(ITetheringOffloadCallback.asInterface(_hidl_request.readStrongBinder()), new initOffloadCallback() {
                        /* class android.hardware.tetheroffload.control.V1_0.IOffloadControl.Stub.AnonymousClass1 */

                        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl.initOffloadCallback
                        public void onValues(boolean success, String errMsg) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeBool(success);
                            _hidl_reply.writeString(errMsg);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 2:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IOffloadControl.kInterfaceName);
                    stopOffload(new stopOffloadCallback() {
                        /* class android.hardware.tetheroffload.control.V1_0.IOffloadControl.Stub.AnonymousClass2 */

                        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl.stopOffloadCallback
                        public void onValues(boolean success, String errMsg) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeBool(success);
                            _hidl_reply.writeString(errMsg);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 3:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IOffloadControl.kInterfaceName);
                    setLocalPrefixes(_hidl_request.readStringVector(), new setLocalPrefixesCallback() {
                        /* class android.hardware.tetheroffload.control.V1_0.IOffloadControl.Stub.AnonymousClass3 */

                        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl.setLocalPrefixesCallback
                        public void onValues(boolean success, String errMsg) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeBool(success);
                            _hidl_reply.writeString(errMsg);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 4:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IOffloadControl.kInterfaceName);
                    getForwardedStats(_hidl_request.readString(), new getForwardedStatsCallback() {
                        /* class android.hardware.tetheroffload.control.V1_0.IOffloadControl.Stub.AnonymousClass4 */

                        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl.getForwardedStatsCallback
                        public void onValues(long rxBytes, long txBytes) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt64(rxBytes);
                            _hidl_reply.writeInt64(txBytes);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 5:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IOffloadControl.kInterfaceName);
                    setDataLimit(_hidl_request.readString(), _hidl_request.readInt64(), new setDataLimitCallback() {
                        /* class android.hardware.tetheroffload.control.V1_0.IOffloadControl.Stub.AnonymousClass5 */

                        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl.setDataLimitCallback
                        public void onValues(boolean success, String errMsg) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeBool(success);
                            _hidl_reply.writeString(errMsg);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 6:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IOffloadControl.kInterfaceName);
                    setUpstreamParameters(_hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readStringVector(), new setUpstreamParametersCallback() {
                        /* class android.hardware.tetheroffload.control.V1_0.IOffloadControl.Stub.AnonymousClass6 */

                        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl.setUpstreamParametersCallback
                        public void onValues(boolean success, String errMsg) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeBool(success);
                            _hidl_reply.writeString(errMsg);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 7:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IOffloadControl.kInterfaceName);
                    addDownstream(_hidl_request.readString(), _hidl_request.readString(), new addDownstreamCallback() {
                        /* class android.hardware.tetheroffload.control.V1_0.IOffloadControl.Stub.AnonymousClass7 */

                        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl.addDownstreamCallback
                        public void onValues(boolean success, String errMsg) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeBool(success);
                            _hidl_reply.writeString(errMsg);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 8:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IOffloadControl.kInterfaceName);
                    removeDownstream(_hidl_request.readString(), _hidl_request.readString(), new removeDownstreamCallback() {
                        /* class android.hardware.tetheroffload.control.V1_0.IOffloadControl.Stub.AnonymousClass8 */

                        @Override // android.hardware.tetheroffload.control.V1_0.IOffloadControl.removeDownstreamCallback
                        public void onValues(boolean success, String errMsg) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeBool(success);
                            _hidl_reply.writeString(errMsg);
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
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
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
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
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
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
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
                                _hidl_is_oneway = true;
                            }
                            if (!_hidl_is_oneway) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
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
}
