package android.hardware.wifi.V1_0;

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

public interface IWifiRttController extends IBase {
    public static final String kInterfaceName = "android.hardware.wifi@1.0::IWifiRttController";

    @FunctionalInterface
    public interface getBoundIfaceCallback {
        void onValues(WifiStatus wifiStatus, IWifiIface iWifiIface);
    }

    @FunctionalInterface
    public interface getCapabilitiesCallback {
        void onValues(WifiStatus wifiStatus, RttCapabilities rttCapabilities);
    }

    @FunctionalInterface
    public interface getResponderInfoCallback {
        void onValues(WifiStatus wifiStatus, RttResponder rttResponder);
    }

    @Override // android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    @Override // android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    WifiStatus disableResponder(int i) throws RemoteException;

    WifiStatus enableResponder(int i, WifiChannelInfo wifiChannelInfo, int i2, RttResponder rttResponder) throws RemoteException;

    void getBoundIface(getBoundIfaceCallback getboundifacecallback) throws RemoteException;

    void getCapabilities(getCapabilitiesCallback getcapabilitiescallback) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    void getResponderInfo(getResponderInfoCallback getresponderinfocallback) throws RemoteException;

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

    WifiStatus rangeCancel(int i, ArrayList<byte[]> arrayList) throws RemoteException;

    WifiStatus rangeRequest(int i, ArrayList<RttConfig> arrayList) throws RemoteException;

    WifiStatus registerEventCallback(IWifiRttControllerEventCallback iWifiRttControllerEventCallback) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    WifiStatus setLci(int i, RttLciInformation rttLciInformation) throws RemoteException;

    WifiStatus setLcr(int i, RttLcrInformation rttLcrInformation) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static IWifiRttController asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IWifiRttController)) {
            return (IWifiRttController) iface;
        }
        IWifiRttController proxy = new Proxy(binder);
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

    static IWifiRttController castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IWifiRttController getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IWifiRttController getService(boolean retry) throws RemoteException {
        return getService("default", retry);
    }

    static IWifiRttController getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IWifiRttController getService() throws RemoteException {
        return getService("default");
    }

    public static final class Proxy implements IWifiRttController {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of android.hardware.wifi@1.0::IWifiRttController]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // android.hardware.wifi.V1_0.IWifiRttController
        public void getBoundIface(getBoundIfaceCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiRttController.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, IWifiIface.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiRttController
        public WifiStatus registerEventCallback(IWifiRttControllerEventCallback callback) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiRttController.kInterfaceName);
            _hidl_request.writeStrongBinder(callback == null ? null : callback.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiRttController
        public WifiStatus rangeRequest(int cmdId, ArrayList<RttConfig> rttConfigs) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiRttController.kInterfaceName);
            _hidl_request.writeInt32(cmdId);
            RttConfig.writeVectorToParcel(_hidl_request, rttConfigs);
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

        @Override // android.hardware.wifi.V1_0.IWifiRttController
        public WifiStatus rangeCancel(int cmdId, ArrayList<byte[]> addrs) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiRttController.kInterfaceName);
            _hidl_request.writeInt32(cmdId);
            HwBlob _hidl_blob = new HwBlob(16);
            int _hidl_vec_size = addrs.size();
            _hidl_blob.putInt32(8, _hidl_vec_size);
            _hidl_blob.putBool(12, false);
            HwBlob childBlob = new HwBlob(_hidl_vec_size * 6);
            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                long _hidl_array_offset_1 = (long) (_hidl_index_0 * 6);
                byte[] _hidl_array_item_1 = addrs.get(_hidl_index_0);
                if (_hidl_array_item_1 == null || _hidl_array_item_1.length != 6) {
                    throw new IllegalArgumentException("Array element is not of the expected length");
                }
                childBlob.putInt8Array(_hidl_array_offset_1, _hidl_array_item_1);
            }
            _hidl_blob.putBlob(0, childBlob);
            _hidl_request.writeBuffer(_hidl_blob);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiRttController
        public void getCapabilities(getCapabilitiesCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiRttController.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                RttCapabilities _hidl_out_capabilities = new RttCapabilities();
                _hidl_out_capabilities.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_out_capabilities);
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiRttController
        public WifiStatus setLci(int cmdId, RttLciInformation lci) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiRttController.kInterfaceName);
            _hidl_request.writeInt32(cmdId);
            lci.writeToParcel(_hidl_request);
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

        @Override // android.hardware.wifi.V1_0.IWifiRttController
        public WifiStatus setLcr(int cmdId, RttLcrInformation lcr) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiRttController.kInterfaceName);
            _hidl_request.writeInt32(cmdId);
            lcr.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiRttController
        public void getResponderInfo(getResponderInfoCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiRttController.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                WifiStatus _hidl_out_status = new WifiStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                RttResponder _hidl_out_info = new RttResponder();
                _hidl_out_info.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_out_info);
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // android.hardware.wifi.V1_0.IWifiRttController
        public WifiStatus enableResponder(int cmdId, WifiChannelInfo channelHint, int maxDurationSeconds, RttResponder info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiRttController.kInterfaceName);
            _hidl_request.writeInt32(cmdId);
            channelHint.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(maxDurationSeconds);
            info.writeToParcel(_hidl_request);
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

        @Override // android.hardware.wifi.V1_0.IWifiRttController
        public WifiStatus disableResponder(int cmdId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IWifiRttController.kInterfaceName);
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

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
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

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IWifiRttController {
        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IWifiRttController.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return IWifiRttController.kInterfaceName;
        }

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{118, 110, -105, 101, -11, -55, -57, 89, -78, -89, 99, -62, 40, -125, 83, -5, 93, -17, -13, 56, -100, 44, -62, -113, -127, -41, -100, -109, -105, 4, -50, -117}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // android.hardware.wifi.V1_0.IWifiRttController, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IWifiRttController.kInterfaceName.equals(descriptor)) {
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
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiRttController.kInterfaceName);
                    getBoundIface(new getBoundIfaceCallback() {
                        /* class android.hardware.wifi.V1_0.IWifiRttController.Stub.AnonymousClass1 */

                        @Override // android.hardware.wifi.V1_0.IWifiRttController.getBoundIfaceCallback
                        public void onValues(WifiStatus status, IWifiIface boundIface) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(boundIface == null ? null : boundIface.asBinder());
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 2:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiRttController.kInterfaceName);
                    WifiStatus _hidl_out_status = registerEventCallback(IWifiRttControllerEventCallback.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
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
                    _hidl_request.enforceInterface(IWifiRttController.kInterfaceName);
                    WifiStatus _hidl_out_status2 = rangeRequest(_hidl_request.readInt32(), RttConfig.readVectorFromParcel(_hidl_request));
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status2.writeToParcel(_hidl_reply);
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
                    _hidl_request.enforceInterface(IWifiRttController.kInterfaceName);
                    int cmdId = _hidl_request.readInt32();
                    ArrayList<byte[]> addrs = new ArrayList<>();
                    HwBlob _hidl_blob = _hidl_request.readBuffer(16);
                    int _hidl_vec_size = _hidl_blob.getInt32(8);
                    HwBlob childBlob = _hidl_request.readEmbeddedBuffer((long) (_hidl_vec_size * 6), _hidl_blob.handle(), 0, true);
                    addrs.clear();
                    for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                        byte[] _hidl_vec_element = new byte[6];
                        childBlob.copyToInt8Array((long) (_hidl_index_0 * 6), _hidl_vec_element, 6);
                        addrs.add(_hidl_vec_element);
                    }
                    WifiStatus _hidl_out_status3 = rangeCancel(cmdId, addrs);
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status3.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 5:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiRttController.kInterfaceName);
                    getCapabilities(new getCapabilitiesCallback() {
                        /* class android.hardware.wifi.V1_0.IWifiRttController.Stub.AnonymousClass2 */

                        @Override // android.hardware.wifi.V1_0.IWifiRttController.getCapabilitiesCallback
                        public void onValues(WifiStatus status, RttCapabilities capabilities) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            capabilities.writeToParcel(_hidl_reply);
                            _hidl_reply.send();
                        }
                    });
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
                    _hidl_request.enforceInterface(IWifiRttController.kInterfaceName);
                    int cmdId2 = _hidl_request.readInt32();
                    RttLciInformation lci = new RttLciInformation();
                    lci.readFromParcel(_hidl_request);
                    WifiStatus _hidl_out_status4 = setLci(cmdId2, lci);
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status4.writeToParcel(_hidl_reply);
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
                    _hidl_request.enforceInterface(IWifiRttController.kInterfaceName);
                    int cmdId3 = _hidl_request.readInt32();
                    RttLcrInformation lcr = new RttLcrInformation();
                    lcr.readFromParcel(_hidl_request);
                    WifiStatus _hidl_out_status5 = setLcr(cmdId3, lcr);
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status5.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 8:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiRttController.kInterfaceName);
                    getResponderInfo(new getResponderInfoCallback() {
                        /* class android.hardware.wifi.V1_0.IWifiRttController.Stub.AnonymousClass3 */

                        @Override // android.hardware.wifi.V1_0.IWifiRttController.getResponderInfoCallback
                        public void onValues(WifiStatus status, RttResponder info) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            info.writeToParcel(_hidl_reply);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 9:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus((int) WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IWifiRttController.kInterfaceName);
                    int cmdId4 = _hidl_request.readInt32();
                    WifiChannelInfo channelHint = new WifiChannelInfo();
                    channelHint.readFromParcel(_hidl_request);
                    int maxDurationSeconds = _hidl_request.readInt32();
                    RttResponder info = new RttResponder();
                    info.readFromParcel(_hidl_request);
                    WifiStatus _hidl_out_status6 = enableResponder(cmdId4, channelHint, maxDurationSeconds, info);
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status6.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
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
                    _hidl_request.enforceInterface(IWifiRttController.kInterfaceName);
                    WifiStatus _hidl_out_status7 = disableResponder(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status7.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
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
                            HwBlob _hidl_blob2 = new HwBlob(16);
                            int _hidl_vec_size2 = _hidl_out_hashchain.size();
                            _hidl_blob2.putInt32(8, _hidl_vec_size2);
                            _hidl_blob2.putBool(12, false);
                            HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 32);
                            for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
                                long _hidl_array_offset_1 = (long) (_hidl_index_02 * 32);
                                byte[] _hidl_array_item_1 = _hidl_out_hashchain.get(_hidl_index_02);
                                if (_hidl_array_item_1 == null || _hidl_array_item_1.length != 32) {
                                    throw new IllegalArgumentException("Array element is not of the expected length");
                                }
                                childBlob2.putInt8Array(_hidl_array_offset_1, _hidl_array_item_1);
                            }
                            _hidl_blob2.putBlob(0, childBlob2);
                            _hidl_reply.writeBuffer(_hidl_blob2);
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
