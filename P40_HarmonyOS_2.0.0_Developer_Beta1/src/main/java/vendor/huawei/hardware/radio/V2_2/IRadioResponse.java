package vendor.huawei.hardware.radio.V2_2;

import android.bluetooth.BluetoothHidDevice;
import android.internal.hidl.base.V1_0.DebugInfo;
import android.internal.hidl.base.V1_0.IBase;
import android.os.HidlSupport;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.NativeHandle;
import android.os.RemoteException;
import com.android.internal.midi.MidiConstants;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.PhoneConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import vendor.huawei.hardware.radio.V2_0.IccIoResultEx;
import vendor.huawei.hardware.radio.V2_0.RILPreferredPLMNSelector;
import vendor.huawei.hardware.radio.V2_0.RadioResponseInfo;
import vendor.huawei.hardware.radio.V2_0.RspMsgPayload;
import vendor.huawei.hardware.radio.V2_1.HwDataRegStateResult_2_1;
import vendor.huawei.hardware.radio.V2_1.HwSignalStrength_2_1;
import vendor.huawei.hardware.radio.V2_1.NrCellSsbIds;

public interface IRadioResponse extends vendor.huawei.hardware.radio.V2_1.IRadioResponse {
    public static final String kInterfaceName = "vendor.huawei.hardware.radio@2.2::IRadioResponse";

    @Override // vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase, android.os.IHwInterface
    IHwBinder asBinder();

    @Override // vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    @Override // vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    void getDsFlowInfoResponse(RadioResponseInfo radioResponseInfo, DsFlowInfo dsFlowInfo) throws RemoteException;

    @Override // vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    void getLteAttachInfoResponse(RadioResponseInfo radioResponseInfo, LteAttachInfo lteAttachInfo) throws RemoteException;

    @Override // vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    @Override // vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    void sendVsimNotificationExResponse(RadioResponseInfo radioResponseInfo, VsimEvent vsimEvent) throws RemoteException;

    @Override // vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    void setUeOperationModeResponse(RadioResponseInfo radioResponseInfo) throws RemoteException;

    @Override // vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static IRadioResponse asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IRadioResponse)) {
            return (IRadioResponse) iface;
        }
        IRadioResponse proxy = new Proxy(binder);
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

    static IRadioResponse castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IRadioResponse getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IRadioResponse getService(boolean retry) throws RemoteException {
        return getService(PhoneConstants.APN_TYPE_DEFAULT, retry);
    }

    static IRadioResponse getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IRadioResponse getService() throws RemoteException {
        return getService(PhoneConstants.APN_TYPE_DEFAULT);
    }

    public static final class Proxy implements IRadioResponse {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse, vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase, android.os.IHwInterface
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.huawei.hardware.radio@2.2::IRadioResponse]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // vendor.huawei.hardware.radio.V2_0.IRadioResponse
        public void RspMsg(RadioResponseInfo info, int msgId, RspMsgPayload payload) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V2_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(msgId);
            payload.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.radio.V2_0.IRadioResponse
        public void getPolListResponse(RadioResponseInfo info, RILPreferredPLMNSelector preferredplmnselector) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V2_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            preferredplmnselector.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.radio.V2_0.IRadioResponse
        public void getSimMatchedFileFromRilCacheResponse(RadioResponseInfo info, IccIoResultEx iccIo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V2_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            iccIo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.radio.V2_1.IRadioResponse
        public void setHwNrSaStateResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V2_1.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.radio.V2_1.IRadioResponse
        public void getHwNrSaStateResponse(RadioResponseInfo info, int on) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V2_1.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(on);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.radio.V2_1.IRadioResponse
        public void getHwNrSsbInfoResponse(RadioResponseInfo info, NrCellSsbIds ssbIds) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V2_1.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            ssbIds.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.radio.V2_1.IRadioResponse
        public void getHwRrcConnectionStateResponse(RadioResponseInfo info, int state) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V2_1.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(state);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.radio.V2_1.IRadioResponse
        public void setHwNrOptionModeResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V2_1.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.radio.V2_1.IRadioResponse
        public void getHwNrOptionModeResponse(RadioResponseInfo info, int mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V2_1.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.radio.V2_1.IRadioResponse
        public void getHwDataRegistrationStateResponse_2_1(RadioResponseInfo info, HwDataRegStateResult_2_1 dataRegResponse) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V2_1.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            dataRegResponse.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.radio.V2_1.IRadioResponse
        public void setTemperatureControlResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V2_1.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.radio.V2_1.IRadioResponse
        public void getHwSignalStrengthResponse_2_1(RadioResponseInfo info, HwSignalStrength_2_1 sigStrength) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V2_1.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            sigStrength.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse
        public void setUeOperationModeResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse
        public void getLteAttachInfoResponse(RadioResponseInfo info, LteAttachInfo attachInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            attachInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse
        public void getDsFlowInfoResponse(RadioResponseInfo info, DsFlowInfo dsFlowInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            dsFlowInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse
        public void sendVsimNotificationExResponse(RadioResponseInfo info, VsimEvent event) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            event.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse, vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse, vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse, vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse, vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse, vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse, vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse, vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse, vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse, vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse, vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IRadioResponse {
        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse, vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase, android.os.IHwInterface
        public IHwBinder asBinder() {
            return this;
        }

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse, vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IRadioResponse.kInterfaceName, vendor.huawei.hardware.radio.V2_1.IRadioResponse.kInterfaceName, vendor.huawei.hardware.radio.V2_0.IRadioResponse.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse, vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse, vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return IRadioResponse.kInterfaceName;
        }

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse, vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{-51, -117, -78, MidiConstants.STATUS_CHANNEL_PRESSURE, -68, 65, -27, MidiConstants.STATUS_SONG_POSITION, 124, -97, -87, 92, -78, 123, -44, -58, 68, 86, 79, -40, 113, GsmAlphabet.GSM_EXTENDED_ESCAPE, 126, MidiConstants.STATUS_CHANNEL_PRESSURE, 22, -90, MidiConstants.STATUS_SONG_POSITION, 119, -66, 18, 43, 83}, new byte[]{-98, 65, 36, -86, 104, -58, 126, 75, GsmAlphabet.GSM_EXTENDED_ESCAPE, -44, 28, -115, 19, -91, 115, 57, -118, -106, -62, 19, -87, -76, 46, MidiConstants.STATUS_NOTE_ON, 36, 5, GsmAlphabet.GSM_EXTENDED_ESCAPE, -43, 54, -51, 79, -68}, new byte[]{MidiConstants.STATUS_CHANNEL_MASK, 34, 114, -67, 99, -117, -25, 34, 98, BluetoothHidDevice.SUBCLASS1_KEYBOARD, -88, -51, 37, 89, 109, -65, -104, 42, -69, -122, -63, 60, 80, -3, -5, -25, 30, -44, 93, -38, 54, -111}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, MidiConstants.STATUS_CHANNEL_PRESSURE, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, MidiConstants.STATUS_SONG_SELECT, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse, vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // android.os.IHwBinder, android.hardware.cas.V1_0.ICas, android.internal.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse, vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse, vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // vendor.huawei.hardware.radio.V2_2.IRadioResponse, vendor.huawei.hardware.radio.V2_1.IRadioResponse, vendor.huawei.hardware.radio.V2_0.IRadioResponse, android.internal.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // android.os.IHwBinder, android.hardware.cas.V1_0.ICas, android.internal.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        @Override // android.os.IHwBinder
        public IHwInterface queryLocalInterface(String descriptor) {
            if (IRadioResponse.kInterfaceName.equals(descriptor)) {
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

        @Override // android.os.HwBinder
        public void onTransact(int _hidl_code, HwParcel _hidl_request, HwParcel _hidl_reply, int _hidl_flags) throws RemoteException {
            boolean _hidl_is_oneway = false;
            boolean _hidl_is_oneway2 = true;
            switch (_hidl_code) {
                case 1:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V2_0.IRadioResponse.kInterfaceName);
                    RadioResponseInfo info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    int msgId = _hidl_request.readInt32();
                    RspMsgPayload payload = new RspMsgPayload();
                    payload.readFromParcel(_hidl_request);
                    RspMsg(info, msgId, payload);
                    return;
                case 2:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V2_0.IRadioResponse.kInterfaceName);
                    RadioResponseInfo info2 = new RadioResponseInfo();
                    info2.readFromParcel(_hidl_request);
                    RILPreferredPLMNSelector preferredplmnselector = new RILPreferredPLMNSelector();
                    preferredplmnselector.readFromParcel(_hidl_request);
                    getPolListResponse(info2, preferredplmnselector);
                    return;
                case 3:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V2_0.IRadioResponse.kInterfaceName);
                    RadioResponseInfo info3 = new RadioResponseInfo();
                    info3.readFromParcel(_hidl_request);
                    IccIoResultEx iccIo = new IccIoResultEx();
                    iccIo.readFromParcel(_hidl_request);
                    getSimMatchedFileFromRilCacheResponse(info3, iccIo);
                    return;
                case 4:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V2_1.IRadioResponse.kInterfaceName);
                    RadioResponseInfo info4 = new RadioResponseInfo();
                    info4.readFromParcel(_hidl_request);
                    setHwNrSaStateResponse(info4);
                    return;
                case 5:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V2_1.IRadioResponse.kInterfaceName);
                    RadioResponseInfo info5 = new RadioResponseInfo();
                    info5.readFromParcel(_hidl_request);
                    getHwNrSaStateResponse(info5, _hidl_request.readInt32());
                    return;
                case 6:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V2_1.IRadioResponse.kInterfaceName);
                    RadioResponseInfo info6 = new RadioResponseInfo();
                    info6.readFromParcel(_hidl_request);
                    NrCellSsbIds ssbIds = new NrCellSsbIds();
                    ssbIds.readFromParcel(_hidl_request);
                    getHwNrSsbInfoResponse(info6, ssbIds);
                    return;
                case 7:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V2_1.IRadioResponse.kInterfaceName);
                    RadioResponseInfo info7 = new RadioResponseInfo();
                    info7.readFromParcel(_hidl_request);
                    getHwRrcConnectionStateResponse(info7, _hidl_request.readInt32());
                    return;
                case 8:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V2_1.IRadioResponse.kInterfaceName);
                    RadioResponseInfo info8 = new RadioResponseInfo();
                    info8.readFromParcel(_hidl_request);
                    setHwNrOptionModeResponse(info8);
                    return;
                case 9:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V2_1.IRadioResponse.kInterfaceName);
                    RadioResponseInfo info9 = new RadioResponseInfo();
                    info9.readFromParcel(_hidl_request);
                    getHwNrOptionModeResponse(info9, _hidl_request.readInt32());
                    return;
                case 10:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V2_1.IRadioResponse.kInterfaceName);
                    RadioResponseInfo info10 = new RadioResponseInfo();
                    info10.readFromParcel(_hidl_request);
                    HwDataRegStateResult_2_1 dataRegResponse = new HwDataRegStateResult_2_1();
                    dataRegResponse.readFromParcel(_hidl_request);
                    getHwDataRegistrationStateResponse_2_1(info10, dataRegResponse);
                    return;
                case 11:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V2_1.IRadioResponse.kInterfaceName);
                    RadioResponseInfo info11 = new RadioResponseInfo();
                    info11.readFromParcel(_hidl_request);
                    setTemperatureControlResponse(info11);
                    return;
                case 12:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V2_1.IRadioResponse.kInterfaceName);
                    RadioResponseInfo info12 = new RadioResponseInfo();
                    info12.readFromParcel(_hidl_request);
                    HwSignalStrength_2_1 sigStrength = new HwSignalStrength_2_1();
                    sigStrength.readFromParcel(_hidl_request);
                    getHwSignalStrengthResponse_2_1(info12, sigStrength);
                    return;
                case 13:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IRadioResponse.kInterfaceName);
                    RadioResponseInfo info13 = new RadioResponseInfo();
                    info13.readFromParcel(_hidl_request);
                    setUeOperationModeResponse(info13);
                    return;
                case 14:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IRadioResponse.kInterfaceName);
                    RadioResponseInfo info14 = new RadioResponseInfo();
                    info14.readFromParcel(_hidl_request);
                    LteAttachInfo attachInfo = new LteAttachInfo();
                    attachInfo.readFromParcel(_hidl_request);
                    getLteAttachInfoResponse(info14, attachInfo);
                    return;
                case 15:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IRadioResponse.kInterfaceName);
                    RadioResponseInfo info15 = new RadioResponseInfo();
                    info15.readFromParcel(_hidl_request);
                    DsFlowInfo dsFlowInfo = new DsFlowInfo();
                    dsFlowInfo.readFromParcel(_hidl_request);
                    getDsFlowInfoResponse(info15, dsFlowInfo);
                    return;
                case 16:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IRadioResponse.kInterfaceName);
                    RadioResponseInfo info16 = new RadioResponseInfo();
                    info16.readFromParcel(_hidl_request);
                    VsimEvent event = new VsimEvent();
                    event.readFromParcel(_hidl_request);
                    sendVsimNotificationExResponse(info16, event);
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
