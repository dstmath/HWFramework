package vendor.huawei.hardware.hisiradio.V1_3;

import android.bluetooth.BluetoothHidDevice;
import android.internal.hidl.base.V1_0.DebugInfo;
import android.internal.hidl.base.V1_0.IBase;
import android.net.wifi.WifiScanner;
import android.os.HidlSupport;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.NativeHandle;
import android.os.RemoteException;
import com.android.internal.midi.MidiConstants;
import com.android.internal.telephony.PhoneConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import vendor.huawei.hardware.hisiradio.V1_0.RILAPDsFlowInfoReport;
import vendor.huawei.hardware.hisiradio.V1_0.RILUnsolMsgPayload;
import vendor.huawei.hardware.hisiradio.V1_0.RILVsimOtaSmsResponse;
import vendor.huawei.hardware.hisiradio.V1_0.RilSysInfor;
import vendor.huawei.hardware.hisiradio.V1_1.HwSignalStrength_1_1;

public interface IHisiRadioIndication extends vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication {
    public static final String kInterfaceName = "vendor.huawei.hardware.hisiradio@1.3::IHisiRadioIndication";

    @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, android.os.IHwInterface, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    void currentRrcConnetionState(int i, int i2) throws RemoteException;

    @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static default IHisiRadioIndication asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IHisiRadioIndication)) {
            return (IHisiRadioIndication) iface;
        }
        IHisiRadioIndication proxy = new Proxy(binder);
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

    static default IHisiRadioIndication castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static default IHisiRadioIndication getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static default IHisiRadioIndication getService(boolean retry) throws RemoteException {
        return getService(PhoneConstants.APN_TYPE_DEFAULT, retry);
    }

    static default IHisiRadioIndication getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static default IHisiRadioIndication getService() throws RemoteException {
        return getService(PhoneConstants.APN_TYPE_DEFAULT);
    }

    public static final class Proxy implements IHisiRadioIndication {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, android.os.IHwInterface, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.huawei.hardware.hisiradio@1.3::IHisiRadioIndication]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication
        public void UnsolMsg(int type, int MsgId, RILUnsolMsgPayload payload) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(MsgId);
            payload.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication
        public void apDsFlowInfoReport(int type, RILAPDsFlowInfoReport apDsFlowInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            apDsFlowInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication
        public void dsFlowInfoReport(int type, RILAPDsFlowInfoReport apDsFlowInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            apDsFlowInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication
        public void vsimOtaSmsReport(int type, RILVsimOtaSmsResponse vsimOtaSms) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            vsimOtaSms.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication
        public void imsaToVowifiMsg(int type, ArrayList<Byte> msg) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt8Vector(msg);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication
        public void sysInforInd(int type, RilSysInfor rilSysInfor) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            rilSysInfor.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication
        public void simMatchRestartRildInd(int type, int restartType) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(restartType);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication
        public void recPseBaseStationReport(int type, int rat) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(rat);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication
        public void updateUlfreqRPT(int type, int rat, int ulfreq, int ulbw) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(rat);
            _hidl_request.writeInt32(ulfreq);
            _hidl_request.writeInt32(ulbw);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication
        public void currentHwSignalStrength_1_1(int type, HwSignalStrength_1_1 sigStrength) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            sigStrength.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication
        public void reportDl256QamState(int type, int state) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(state);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication
        public void currentRrcConnetionState(int type, int state) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IHisiRadioIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(state);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
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

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IHisiRadioIndication {
        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, android.os.IHwInterface, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IHisiRadioIndication.kInterfaceName, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication.kInterfaceName, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication.kInterfaceName, vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return IHisiRadioIndication.kInterfaceName;
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{-18, 18, -23, 32, 42, BluetoothHidDevice.SUBCLASS1_KEYBOARD, 105, -61, 61, 126, 107, 51, -86, -97, Byte.MIN_VALUE, 34, -23, 123, 6, 34, -113, 90, -42, 78, Byte.MAX_VALUE, -16, 2, -3, -126, -117, MidiConstants.STATUS_POLYPHONIC_AFTERTOUCH, 12}, new byte[]{-108, 50, -91, 74, 80, -51, -93, -58, -75, -30, 26, 19, 56, -106, -7, -81, 34, 74, 92, -41, 34, -19, -29, 80, 119, -42, 30, BluetoothHidDevice.SUBCLASS1_KEYBOARD, 115, -12, -47, 68}, new byte[]{98, -58, -111, 89, -53, -93, -123, BluetoothHidDevice.SUBCLASS1_KEYBOARD, 84, 110, -34, 72, 36, 28, 115, -24, 52, 1, 91, 54, 121, -59, 90, -24, 39, -91, 19, -121, 3, 49, 58, -66}, new byte[]{-104, WifiScanner.PnoSettings.PnoNetwork.FLAG_SAME_NETWORK, MidiConstants.STATUS_PITCH_BEND, -88, 19, 68, 21, MidiConstants.STATUS_CHANNEL_PRESSURE, -110, 43, -63, MidiConstants.STATUS_POLYPHONIC_AFTERTOUCH, 52, 31, -93, -98, -43, -51, -117, -105, -42, -10, -115, -90, -29, 126, 117, 78, 99, -120, 8, -28}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, MidiConstants.STATUS_CHANNEL_PRESSURE, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, MidiConstants.STATUS_SONG_SELECT, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, android.os.IHwBinder, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication, android.os.IHwBinder, vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioIndication, vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication, android.internal.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        @Override // android.os.IHwBinder
        public IHwInterface queryLocalInterface(String descriptor) {
            if (IHisiRadioIndication.kInterfaceName.equals(descriptor)) {
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication.kInterfaceName);
                    int type = _hidl_request.readInt32();
                    int MsgId = _hidl_request.readInt32();
                    RILUnsolMsgPayload payload = new RILUnsolMsgPayload();
                    payload.readFromParcel(_hidl_request);
                    UnsolMsg(type, MsgId, payload);
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication.kInterfaceName);
                    int type2 = _hidl_request.readInt32();
                    RILAPDsFlowInfoReport apDsFlowInfo = new RILAPDsFlowInfoReport();
                    apDsFlowInfo.readFromParcel(_hidl_request);
                    apDsFlowInfoReport(type2, apDsFlowInfo);
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication.kInterfaceName);
                    int type3 = _hidl_request.readInt32();
                    RILAPDsFlowInfoReport apDsFlowInfo2 = new RILAPDsFlowInfoReport();
                    apDsFlowInfo2.readFromParcel(_hidl_request);
                    dsFlowInfoReport(type3, apDsFlowInfo2);
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication.kInterfaceName);
                    int type4 = _hidl_request.readInt32();
                    RILVsimOtaSmsResponse vsimOtaSms = new RILVsimOtaSmsResponse();
                    vsimOtaSms.readFromParcel(_hidl_request);
                    vsimOtaSmsReport(type4, vsimOtaSms);
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication.kInterfaceName);
                    imsaToVowifiMsg(_hidl_request.readInt32(), _hidl_request.readInt8Vector());
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication.kInterfaceName);
                    int type5 = _hidl_request.readInt32();
                    RilSysInfor rilSysInfor = new RilSysInfor();
                    rilSysInfor.readFromParcel(_hidl_request);
                    sysInforInd(type5, rilSysInfor);
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication.kInterfaceName);
                    simMatchRestartRildInd(_hidl_request.readInt32(), _hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hisiradio.V1_0.IHisiRadioIndication.kInterfaceName);
                    recPseBaseStationReport(_hidl_request.readInt32(), _hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication.kInterfaceName);
                    updateUlfreqRPT(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioIndication.kInterfaceName);
                    int type6 = _hidl_request.readInt32();
                    HwSignalStrength_1_1 sigStrength = new HwSignalStrength_1_1();
                    sigStrength.readFromParcel(_hidl_request);
                    currentHwSignalStrength_1_1(type6, sigStrength);
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
                    _hidl_request.enforceInterface(vendor.huawei.hardware.hisiradio.V1_2.IHisiRadioIndication.kInterfaceName);
                    reportDl256QamState(_hidl_request.readInt32(), _hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(IHisiRadioIndication.kInterfaceName);
                    currentRrcConnetionState(_hidl_request.readInt32(), _hidl_request.readInt32());
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
