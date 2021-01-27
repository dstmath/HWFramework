package vendor.huawei.hardware.eid.V1_0;

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
import com.android.server.appactcontrol.AppActConstant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public interface IEid extends IBase {
    public static final String kInterfaceName = "vendor.huawei.hardware.eid@1.0::IEid";

    @FunctionalInterface
    public interface HWEidGetCertificateRequestMessageCallback {
        void onValues(int i, CERTIFICATE_REQUEST_MESSAGE_S certificate_request_message_s);
    }

    @FunctionalInterface
    public interface HWEidGetFaceIsChangedCallback {
        void onValues(int i, FACE_CHANGE_OUTPUT_INFO_S face_change_output_info_s);
    }

    @FunctionalInterface
    public interface HWEidGetIdentityInformationCallback {
        void onValues(int i, IDENTITY_INFORMATION_S identity_information_s);
    }

    @FunctionalInterface
    public interface HWEidGetImageCallback {
        void onValues(int i, SEC_IMAGE_S sec_image_s);
    }

    @FunctionalInterface
    public interface HWEidGetInfoSignCallback {
        void onValues(int i, INFO_SIGN_OUTPUT_INFO_S info_sign_output_info_s);
    }

    @FunctionalInterface
    public interface HWEidGetUnsecImageCallback {
        void onValues(int i, SEC_IMAGE_S sec_image_s);
    }

    int HWEidFiniTa() throws RemoteException;

    void HWEidGetCertificateRequestMessage(CERTIFICATE_REQUEST_MESSAGE_INPUT_INFO_S certificate_request_message_input_info_s, HWEidGetCertificateRequestMessageCallback hWEidGetCertificateRequestMessageCallback) throws RemoteException;

    void HWEidGetFaceIsChanged(FACE_CHANGE_INPUT_INFO_S face_change_input_info_s, HWEidGetFaceIsChangedCallback hWEidGetFaceIsChangedCallback) throws RemoteException;

    void HWEidGetIdentityInformation(IDENTITY_INFORMATION_INPUT_INFO_S identity_information_input_info_s, HWEidGetIdentityInformationCallback hWEidGetIdentityInformationCallback) throws RemoteException;

    void HWEidGetImage(ENCRYPTION_FACTOR_S encryption_factor_s, HWEidGetImageCallback hWEidGetImageCallback) throws RemoteException;

    void HWEidGetInfoSign(INFO_SIGN_INPUT_INFO_S info_sign_input_info_s, HWEidGetInfoSignCallback hWEidGetInfoSignCallback) throws RemoteException;

    void HWEidGetUnsecImage(IMAGE_CONTAINER_S image_container_s, ENCRYPTION_FACTOR_S encryption_factor_s, HWEidGetUnsecImageCallback hWEidGetUnsecImageCallback) throws RemoteException;

    int HWEidInitTa(INIT_TA_MSG_S init_ta_msg_s) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    @Override // android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

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

    @Override // android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static IEid asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IEid)) {
            return (IEid) iface;
        }
        IEid proxy = new Proxy(binder);
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

    static IEid castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IEid getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IEid getService(boolean retry) throws RemoteException {
        return getService(AppActConstant.VALUE_DEFAULT, retry);
    }

    static IEid getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IEid getService() throws RemoteException {
        return getService(AppActConstant.VALUE_DEFAULT);
    }

    public static final class Proxy implements IEid {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.huawei.hardware.eid@1.0::IEid]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // vendor.huawei.hardware.eid.V1_0.IEid
        public int HWEidInitTa(INIT_TA_MSG_S init_msg) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEid.kInterfaceName);
            init_msg.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.eid.V1_0.IEid
        public int HWEidFiniTa() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEid.kInterfaceName);
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

        @Override // vendor.huawei.hardware.eid.V1_0.IEid
        public void HWEidGetImage(ENCRYPTION_FACTOR_S factor, HWEidGetImageCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEid.kInterfaceName);
            factor.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_ret = _hidl_reply.readInt32();
                SEC_IMAGE_S _hidl_out_secImage = new SEC_IMAGE_S();
                _hidl_out_secImage.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_ret, _hidl_out_secImage);
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.eid.V1_0.IEid
        public void HWEidGetUnsecImage(IMAGE_CONTAINER_S container, ENCRYPTION_FACTOR_S factor, HWEidGetUnsecImageCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEid.kInterfaceName);
            container.writeToParcel(_hidl_request);
            factor.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_ret = _hidl_reply.readInt32();
                SEC_IMAGE_S _hidl_out_secImage = new SEC_IMAGE_S();
                _hidl_out_secImage.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_ret, _hidl_out_secImage);
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.eid.V1_0.IEid
        public void HWEidGetCertificateRequestMessage(CERTIFICATE_REQUEST_MESSAGE_INPUT_INFO_S input, HWEidGetCertificateRequestMessageCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEid.kInterfaceName);
            input.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_ret = _hidl_reply.readInt32();
                CERTIFICATE_REQUEST_MESSAGE_S _hidl_out_certReqMsg = new CERTIFICATE_REQUEST_MESSAGE_S();
                _hidl_out_certReqMsg.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_ret, _hidl_out_certReqMsg);
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.eid.V1_0.IEid
        public void HWEidGetInfoSign(INFO_SIGN_INPUT_INFO_S input, HWEidGetInfoSignCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEid.kInterfaceName);
            input.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_ret = _hidl_reply.readInt32();
                INFO_SIGN_OUTPUT_INFO_S _hidl_out_output = new INFO_SIGN_OUTPUT_INFO_S();
                _hidl_out_output.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_ret, _hidl_out_output);
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.eid.V1_0.IEid
        public void HWEidGetIdentityInformation(IDENTITY_INFORMATION_INPUT_INFO_S input, HWEidGetIdentityInformationCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEid.kInterfaceName);
            input.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_ret = _hidl_reply.readInt32();
                IDENTITY_INFORMATION_S _hidl_out_idInfo = new IDENTITY_INFORMATION_S();
                _hidl_out_idInfo.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_ret, _hidl_out_idInfo);
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.eid.V1_0.IEid
        public void HWEidGetFaceIsChanged(FACE_CHANGE_INPUT_INFO_S input, HWEidGetFaceIsChangedCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEid.kInterfaceName);
            input.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_ret = _hidl_reply.readInt32();
                FACE_CHANGE_OUTPUT_INFO_S _hidl_out_output = new FACE_CHANGE_OUTPUT_INFO_S();
                _hidl_out_output.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_ret, _hidl_out_output);
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IEid {
        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IEid.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return IEid.kInterfaceName;
        }

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{61, -19, 84, 41, -25, 29, -106, 79, 96, 47, 120, 123, -46, 1, -54, 102, 115, 88, 123, -105, 95, 101, -79, 101, 119, 77, -46, -9, 54, 45, 117, 84}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // vendor.huawei.hardware.eid.V1_0.IEid, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IEid.kInterfaceName.equals(descriptor)) {
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
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEid.kInterfaceName);
                    INIT_TA_MSG_S init_msg = new INIT_TA_MSG_S();
                    init_msg.readFromParcel(_hidl_request);
                    int _hidl_out_ret = HWEidInitTa(init_msg);
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_ret);
                    _hidl_reply.send();
                    return;
                case 2:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEid.kInterfaceName);
                    int _hidl_out_ret2 = HWEidFiniTa();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_ret2);
                    _hidl_reply.send();
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
                    _hidl_request.enforceInterface(IEid.kInterfaceName);
                    ENCRYPTION_FACTOR_S factor = new ENCRYPTION_FACTOR_S();
                    factor.readFromParcel(_hidl_request);
                    HWEidGetImage(factor, new HWEidGetImageCallback() {
                        /* class vendor.huawei.hardware.eid.V1_0.IEid.Stub.AnonymousClass1 */

                        @Override // vendor.huawei.hardware.eid.V1_0.IEid.HWEidGetImageCallback
                        public void onValues(int ret, SEC_IMAGE_S secImage) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(ret);
                            secImage.writeToParcel(_hidl_reply);
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
                    _hidl_request.enforceInterface(IEid.kInterfaceName);
                    IMAGE_CONTAINER_S container = new IMAGE_CONTAINER_S();
                    container.readFromParcel(_hidl_request);
                    ENCRYPTION_FACTOR_S factor2 = new ENCRYPTION_FACTOR_S();
                    factor2.readFromParcel(_hidl_request);
                    HWEidGetUnsecImage(container, factor2, new HWEidGetUnsecImageCallback() {
                        /* class vendor.huawei.hardware.eid.V1_0.IEid.Stub.AnonymousClass2 */

                        @Override // vendor.huawei.hardware.eid.V1_0.IEid.HWEidGetUnsecImageCallback
                        public void onValues(int ret, SEC_IMAGE_S secImage) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(ret);
                            secImage.writeToParcel(_hidl_reply);
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
                    _hidl_request.enforceInterface(IEid.kInterfaceName);
                    CERTIFICATE_REQUEST_MESSAGE_INPUT_INFO_S input = new CERTIFICATE_REQUEST_MESSAGE_INPUT_INFO_S();
                    input.readFromParcel(_hidl_request);
                    HWEidGetCertificateRequestMessage(input, new HWEidGetCertificateRequestMessageCallback() {
                        /* class vendor.huawei.hardware.eid.V1_0.IEid.Stub.AnonymousClass3 */

                        @Override // vendor.huawei.hardware.eid.V1_0.IEid.HWEidGetCertificateRequestMessageCallback
                        public void onValues(int ret, CERTIFICATE_REQUEST_MESSAGE_S certReqMsg) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(ret);
                            certReqMsg.writeToParcel(_hidl_reply);
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
                    _hidl_request.enforceInterface(IEid.kInterfaceName);
                    INFO_SIGN_INPUT_INFO_S input2 = new INFO_SIGN_INPUT_INFO_S();
                    input2.readFromParcel(_hidl_request);
                    HWEidGetInfoSign(input2, new HWEidGetInfoSignCallback() {
                        /* class vendor.huawei.hardware.eid.V1_0.IEid.Stub.AnonymousClass4 */

                        @Override // vendor.huawei.hardware.eid.V1_0.IEid.HWEidGetInfoSignCallback
                        public void onValues(int ret, INFO_SIGN_OUTPUT_INFO_S output) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(ret);
                            output.writeToParcel(_hidl_reply);
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
                    _hidl_request.enforceInterface(IEid.kInterfaceName);
                    IDENTITY_INFORMATION_INPUT_INFO_S input3 = new IDENTITY_INFORMATION_INPUT_INFO_S();
                    input3.readFromParcel(_hidl_request);
                    HWEidGetIdentityInformation(input3, new HWEidGetIdentityInformationCallback() {
                        /* class vendor.huawei.hardware.eid.V1_0.IEid.Stub.AnonymousClass5 */

                        @Override // vendor.huawei.hardware.eid.V1_0.IEid.HWEidGetIdentityInformationCallback
                        public void onValues(int ret, IDENTITY_INFORMATION_S idInfo) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(ret);
                            idInfo.writeToParcel(_hidl_reply);
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
                    _hidl_request.enforceInterface(IEid.kInterfaceName);
                    FACE_CHANGE_INPUT_INFO_S input4 = new FACE_CHANGE_INPUT_INFO_S();
                    input4.readFromParcel(_hidl_request);
                    HWEidGetFaceIsChanged(input4, new HWEidGetFaceIsChangedCallback() {
                        /* class vendor.huawei.hardware.eid.V1_0.IEid.Stub.AnonymousClass6 */

                        @Override // vendor.huawei.hardware.eid.V1_0.IEid.HWEidGetFaceIsChangedCallback
                        public void onValues(int ret, FACE_CHANGE_OUTPUT_INFO_S output) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(ret);
                            output.writeToParcel(_hidl_reply);
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
