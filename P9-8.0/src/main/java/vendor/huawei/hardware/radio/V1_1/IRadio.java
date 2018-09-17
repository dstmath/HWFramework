package vendor.huawei.hardware.radio.V1_1;

import android.hardware.radio.V1_0.CallForwardInfo;
import android.hardware.radio.V1_0.CarrierRestrictions;
import android.hardware.radio.V1_0.CdmaBroadcastSmsConfigInfo;
import android.hardware.radio.V1_0.CdmaSmsAck;
import android.hardware.radio.V1_0.CdmaSmsMessage;
import android.hardware.radio.V1_0.CdmaSmsWriteArgs;
import android.hardware.radio.V1_0.DataProfileInfo;
import android.hardware.radio.V1_0.Dial;
import android.hardware.radio.V1_0.GsmBroadcastSmsConfigInfo;
import android.hardware.radio.V1_0.GsmSmsMessage;
import android.hardware.radio.V1_0.IRadioIndication;
import android.hardware.radio.V1_0.IRadioResponse;
import android.hardware.radio.V1_0.IccIo;
import android.hardware.radio.V1_0.ImsSmsMessage;
import android.hardware.radio.V1_0.NvWriteItem;
import android.hardware.radio.V1_0.RadioCapability;
import android.hardware.radio.V1_0.RadioError;
import android.hardware.radio.V1_0.SelectUiccSub;
import android.hardware.radio.V1_0.SimApdu;
import android.hardware.radio.V1_0.SmsWriteArgs;
import android.hidl.base.V1_0.DebugInfo;
import android.hidl.base.V1_0.IBase;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwBinder.DeathRecipient;
import android.os.IHwInterface;
import android.os.RemoteException;
import android.os.SystemProperties;
import com.android.internal.telephony.AbstractPhoneBase;
import com.android.internal.telephony.RadioNVItems;
import com.huawei.internal.telephony.HwRadarUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import vendor.huawei.hardware.radio.V1_0.CsgNetworkInfo;
import vendor.huawei.hardware.radio.V1_0.RILCURSMUPDATEFILE;
import vendor.huawei.hardware.radio.V1_0.RILImsCallModify;
import vendor.huawei.hardware.radio.V1_0.RILImsDial;
import vendor.huawei.hardware.radio.V1_0.RILUICCAUTH;
import vendor.huawei.hardware.radio.V1_0.RILUICCKSNAFAUTH;

public interface IRadio extends vendor.huawei.hardware.radio.V1_0.IRadio {
    public static final String kInterfaceName = "vendor.huawei.hardware.radio@1.1::IRadio";

    public static final class Proxy implements IRadio {
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
                return "[class or subclass of vendor.huawei.hardware.radio@1.1::IRadio]@Proxy";
            }
        }

        public void setResponseFunctions(IRadioResponse radioResponse, IRadioIndication radioIndication) throws RemoteException {
            IHwBinder iHwBinder = null;
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeStrongBinder(radioResponse == null ? null : radioResponse.asBinder());
            if (radioIndication != null) {
                iHwBinder = radioIndication.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getIccCardStatus(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void supplyIccPinForApp(int serial, String pin, String aid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(pin);
            _hidl_request.writeString(aid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void supplyIccPukForApp(int serial, String puk, String pin, String aid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(puk);
            _hidl_request.writeString(pin);
            _hidl_request.writeString(aid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void supplyIccPin2ForApp(int serial, String pin2, String aid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(pin2);
            _hidl_request.writeString(aid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void supplyIccPuk2ForApp(int serial, String puk2, String pin2, String aid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(puk2);
            _hidl_request.writeString(pin2);
            _hidl_request.writeString(aid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void changeIccPinForApp(int serial, String oldPin, String newPin, String aid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(oldPin);
            _hidl_request.writeString(newPin);
            _hidl_request.writeString(aid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void changeIccPin2ForApp(int serial, String oldPin2, String newPin2, String aid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(oldPin2);
            _hidl_request.writeString(newPin2);
            _hidl_request.writeString(aid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void supplyNetworkDepersonalization(int serial, String netPin) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(netPin);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCurrentCalls(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void dial(int serial, Dial dialInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            dialInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getImsiForApp(int serial, String aid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(aid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void hangup(int serial, int gsmIndex) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(gsmIndex);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void hangupWaitingOrBackground(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void hangupForegroundResumeBackground(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void switchWaitingOrHoldingAndActive(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void conference(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void rejectCall(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(18, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getLastCallFailCause(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(19, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getSignalStrength(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(20, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getVoiceRegistrationState(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(21, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getDataRegistrationState(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(22, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getOperator(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(23, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setRadioPower(int serial, boolean on) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(on);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(24, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendDtmf(int serial, String s) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(s);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(25, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendSms(int serial, GsmSmsMessage message) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            message.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(26, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendSMSExpectMore(int serial, GsmSmsMessage message) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            message.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(27, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setupDataCall(int serial, int radioTechnology, DataProfileInfo dataProfileInfo, boolean modemCognitive, boolean roamingAllowed, boolean isRoaming) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(radioTechnology);
            dataProfileInfo.writeToParcel(_hidl_request);
            _hidl_request.writeBool(modemCognitive);
            _hidl_request.writeBool(roamingAllowed);
            _hidl_request.writeBool(isRoaming);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(28, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void iccIOForApp(int serial, IccIo iccIo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            iccIo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(29, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendUssd(int serial, String ussd) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(ussd);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(30, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void cancelPendingUssd(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(31, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getClir(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(32, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setClir(int serial, int status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(33, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCallForwardStatus(int serial, CallForwardInfo callInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            callInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(34, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setCallForward(int serial, CallForwardInfo callInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            callInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(35, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCallWaiting(int serial, int serviceClass) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(serviceClass);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(36, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setCallWaiting(int serial, boolean enable, int serviceClass) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(enable);
            _hidl_request.writeInt32(serviceClass);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(37, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void acknowledgeLastIncomingGsmSms(int serial, boolean success, int cause) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(success);
            _hidl_request.writeInt32(cause);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(38, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void acceptCall(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(39, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void deactivateDataCall(int serial, int cid, boolean reasonRadioShutDown) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(cid);
            _hidl_request.writeBool(reasonRadioShutDown);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(40, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getFacilityLockForApp(int serial, String facility, String password, int serviceClass, String appId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(facility);
            _hidl_request.writeString(password);
            _hidl_request.writeInt32(serviceClass);
            _hidl_request.writeString(appId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(41, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setFacilityLockForApp(int serial, String facility, boolean lockState, String password, int serviceClass, String appId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(facility);
            _hidl_request.writeBool(lockState);
            _hidl_request.writeString(password);
            _hidl_request.writeInt32(serviceClass);
            _hidl_request.writeString(appId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(42, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setBarringPassword(int serial, String facility, String oldPassword, String newPassword) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(facility);
            _hidl_request.writeString(oldPassword);
            _hidl_request.writeString(newPassword);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(43, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getNetworkSelectionMode(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(44, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setNetworkSelectionModeAutomatic(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(45, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setNetworkSelectionModeManual(int serial, String operatorNumeric) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(operatorNumeric);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(46, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getAvailableNetworks(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(47, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void startDtmf(int serial, String s) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(s);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(48, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void stopDtmf(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(49, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getBasebandVersion(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(50, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void separateConnection(int serial, int gsmIndex) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(gsmIndex);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(51, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setMute(int serial, boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(52, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getMute(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(53, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getClip(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(54, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getDataCallList(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(55, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setSuppServiceNotifications(int serial, boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(56, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void writeSmsToSim(int serial, SmsWriteArgs smsWriteArgs) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            smsWriteArgs.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(57, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void deleteSmsOnSim(int serial, int index) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(index);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(58, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setBandMode(int serial, int mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(59, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getAvailableBandModes(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(60, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendEnvelope(int serial, String command) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(command);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(61, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendTerminalResponseToSim(int serial, String commandResponse) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(commandResponse);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(62, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void handleStkCallSetupRequestFromSim(int serial, boolean accept) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(accept);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(63, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void explicitCallTransfer(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(64, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setPreferredNetworkType(int serial, int nwType) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(nwType);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(65, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getPreferredNetworkType(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(66, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getNeighboringCids(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(67, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setLocationUpdates(int serial, boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(68, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setCdmaSubscriptionSource(int serial, int cdmaSub) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(cdmaSub);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(69, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setCdmaRoamingPreference(int serial, int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(70, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCdmaRoamingPreference(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(71, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setTTYMode(int serial, int mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(72, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getTTYMode(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(73, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setPreferredVoicePrivacy(int serial, boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(74, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getPreferredVoicePrivacy(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(75, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendCDMAFeatureCode(int serial, String featureCode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(featureCode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(76, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendBurstDtmf(int serial, String dtmf, int on, int off) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(dtmf);
            _hidl_request.writeInt32(on);
            _hidl_request.writeInt32(off);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(77, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendCdmaSms(int serial, CdmaSmsMessage sms) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            sms.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(78, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void acknowledgeLastIncomingCdmaSms(int serial, CdmaSmsAck smsAck) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            smsAck.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(79, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getGsmBroadcastConfig(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(80, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setGsmBroadcastConfig(int serial, ArrayList<GsmBroadcastSmsConfigInfo> configInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            GsmBroadcastSmsConfigInfo.writeVectorToParcel(_hidl_request, configInfo);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(81, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setGsmBroadcastActivation(int serial, boolean activate) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(activate);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(82, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCdmaBroadcastConfig(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(83, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setCdmaBroadcastConfig(int serial, ArrayList<CdmaBroadcastSmsConfigInfo> configInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            CdmaBroadcastSmsConfigInfo.writeVectorToParcel(_hidl_request, configInfo);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(84, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setCdmaBroadcastActivation(int serial, boolean activate) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(activate);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(85, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCDMASubscription(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(86, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void writeSmsToRuim(int serial, CdmaSmsWriteArgs cdmaSms) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            cdmaSms.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(87, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void deleteSmsOnRuim(int serial, int index) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(index);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(88, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getDeviceIdentity(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(89, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void exitEmergencyCallbackMode(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(90, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getSmscAddress(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(91, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setSmscAddress(int serial, String smsc) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(smsc);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(92, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void reportSmsMemoryStatus(int serial, boolean available) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(available);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(93, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void reportStkServiceIsRunning(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(94, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCdmaSubscriptionSource(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(95, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void requestIsimAuthentication(int serial, String challenge) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(challenge);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(96, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void acknowledgeIncomingGsmSmsWithPdu(int serial, boolean success, String ackPdu) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(success);
            _hidl_request.writeString(ackPdu);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(97, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendEnvelopeWithStatus(int serial, String contents) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(contents);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(98, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getVoiceRadioTechnology(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(99, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCellInfoList(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(100, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setCellInfoListRate(int serial, int rate) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(rate);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(101, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setInitialAttachApn(int serial, DataProfileInfo dataProfileInfo, boolean modemCognitive, boolean isRoaming) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            dataProfileInfo.writeToParcel(_hidl_request);
            _hidl_request.writeBool(modemCognitive);
            _hidl_request.writeBool(isRoaming);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(102, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getImsRegistrationState(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(103, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendImsSms(int serial, ImsSmsMessage message) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            message.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(AbstractPhoneBase.EVENT_ECC_NUM, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void iccTransmitApduBasicChannel(int serial, SimApdu message) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            message.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(AbstractPhoneBase.EVENT_GET_IMSI_DONE, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void iccOpenLogicalChannel(int serial, String aid, int p2) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(aid);
            _hidl_request.writeInt32(p2);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(106, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void iccCloseLogicalChannel(int serial, int channelId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(channelId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(107, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void iccTransmitApduLogicalChannel(int serial, SimApdu message) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            message.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(AbstractPhoneBase.EVENT_GET_LTE_RELEASE_VERSION_DONE, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void nvReadItem(int serial, int itemId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(itemId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(109, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void nvWriteItem(int serial, NvWriteItem item) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            item.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(110, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void nvWriteCdmaPrl(int serial, ArrayList<Byte> prl) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt8Vector(prl);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(111, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void nvResetConfig(int serial, int resetType) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(resetType);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(112, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setUiccSubscription(int serial, SelectUiccSub uiccSub) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            uiccSub.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(113, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setDataAllowed(int serial, boolean allow) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(allow);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(114, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getHardwareConfig(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(115, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void requestIccSimAuthentication(int serial, int authContext, String authData, String aid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(authContext);
            _hidl_request.writeString(authData);
            _hidl_request.writeString(aid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(116, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setDataProfile(int serial, ArrayList<DataProfileInfo> profiles, boolean isRoaming) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            DataProfileInfo.writeVectorToParcel(_hidl_request, profiles);
            _hidl_request.writeBool(isRoaming);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(117, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void requestShutdown(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(118, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getRadioCapability(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(119, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setRadioCapability(int serial, RadioCapability rc) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            rc.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(120, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void startLceService(int serial, int reportInterval, boolean pullMode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(reportInterval);
            _hidl_request.writeBool(pullMode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(121, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void stopLceService(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(122, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void pullLceData(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(123, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getModemActivityInfo(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(124, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setAllowedCarriers(int serial, boolean allAllowed, CarrierRestrictions carriers) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(allAllowed);
            carriers.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(125, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getAllowedCarriers(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(126, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendDeviceState(int serial, int deviceStateType, boolean state) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(deviceStateType);
            _hidl_request.writeBool(state);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(127, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setIndicationFilter(int serial, int indicationFilter) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(indicationFilter);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(128, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setSimCardPower(int serial, boolean powerUp) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(powerUp);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(129, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void responseAcknowledgement() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadio.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(130, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setResponseFunctionsHuawei(vendor.huawei.hardware.radio.V1_0.IRadioResponse radioResponse, vendor.huawei.hardware.radio.V1_0.IRadioIndication radioIndication) throws RemoteException {
            IHwBinder iHwBinder = null;
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeStrongBinder(radioResponse == null ? null : radioResponse.asBinder());
            if (radioIndication != null) {
                iHwBinder = radioIndication.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(131, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setPowerGrade(int serial, String powergrade) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(powergrade);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(132, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getSimHotplugState(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(133, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCardType(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(134, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCdmaGsmImsi(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(135, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void restartRILD(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(136, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getSimATR(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(137, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getIccid(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(138, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setSimSlot(int serial, int modem1ToSlot, int modem2ToSlot, int modem3ToSlot) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(modem1ToSlot);
            _hidl_request.writeInt32(modem2ToSlot);
            _hidl_request.writeInt32(modem3ToSlot);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(139, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getSimSlot(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(140, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setCdmaModeSide(int serial, int modemId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(modemId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(141, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCdmaModeSide(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(142, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setActiveModemMode(int serial, int mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(143, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void openChannelWithP2(int serial, String aid, String p2) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(aid);
            _hidl_request.writeString(p2);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(144, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getEccNum(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(145, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setEccNum(int serial, String ecclist_withcard, String ecclist_nocard) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(ecclist_withcard);
            _hidl_request.writeString(ecclist_nocard);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(146, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setVoicecallBackGroundState(int serial, int state) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(state);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(147, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getVoicecallBackGroundState(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(148, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setVoicePreferStatus(int serial, int status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(149, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getVoicePreferStatus(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(150, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void imsDial(int serial, RILImsDial dialInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            dialInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(151, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCurrentImsCalls(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(152, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void acceptImsCall(int serial, int callType) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(callType);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(153, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getImsImpu(int serial, int impuIndex) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(impuIndex);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(154, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void addImsConfMember(int serial, String s) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(s);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(155, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setImsCallWaiting(int serial, boolean enable, int serviceClass) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(enable);
            _hidl_request.writeInt32(serviceClass);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(156, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void modifyImsCallInitiate(int serial, RILImsCallModify callModifyInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            callModifyInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(157, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void modifyImsCallConfirm(int serial, RILImsCallModify callModifyInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            callModifyInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(158, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setImsSwitch(int serial, int status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(159, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getImsSwitch(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(160, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void rejectCallWithReason(int serial, int callIndex, int cause) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(callIndex);
            _hidl_request.writeInt32(cause);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(161, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getChannelInfo(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(162, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void dataConnectionDeatch(int serial, int detachFlag) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(detachFlag);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(163, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void dataConnectionAttach(int serial, int attachFlag) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(attachFlag);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(164, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setLongMessage(int serial, int msgFlag) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(msgFlag);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(165, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void resetAllConnections(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(166, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setLteReleaseVersion(int serial, int version) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(version);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(167, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getLteReleaseVersion(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(168, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setImsDomain(int serial, int domain) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(domain);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(169, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getImsDomain(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(170, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void vowifiToImsaMsg(int serial, ArrayList<Byte> msg) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt8Vector(msg);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(171, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void uiccAuth(int serial, RILUICCAUTH uiccAuthParam) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            uiccAuthParam.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(172, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void uiccGbaBootStrap(int serial, RILUICCAUTH gbaParam) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            gbaParam.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(173, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void updateUiccFile(int serial, RILCURSMUPDATEFILE uiccUpdateParam) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            uiccUpdateParam.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(174, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getUiccFile(int serial, int uiccType, int fileId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(uiccType);
            _hidl_request.writeInt32(fileId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(175, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void uiccKsNafAuth(int serial, RILUICCKSNAFAUTH ksNafAuthParam) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            ksNafAuthParam.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(176, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setVtLlteQualityRptCfg(int serial, int enabled, int threshHold) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(enabled);
            _hidl_request.writeInt32(threshHold);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(177, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void clearDsFlowInfo(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(178, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getLocationInfo(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(179, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setWifiPowerGrade(int serial, String power_grade) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(power_grade);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(180, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void impactAntDevstate(int serial, String device, String status, String reserve) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(device);
            _hidl_request.writeString(status);
            _hidl_request.writeString(reserve);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(181, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setNetworkRatAndSrvDomain(int serial, int at_rat, int srv_domain) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(at_rat);
            _hidl_request.writeInt32(srv_domain);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(182, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setVsimSimState(int serial, int index, int enable, int slot) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(index);
            _hidl_request.writeInt32(enable);
            _hidl_request.writeInt32(slot);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(183, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getVsimSimState(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(184, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getSystemInfoEx(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(185, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getPlmnInfo(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(186, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getPolCapability(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(187, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getPolList(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(188, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setPolEntry(int serial, String index, String opern, String arg) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(index);
            _hidl_request.writeString(opern);
            _hidl_request.writeString(arg);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(189, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setIsmcoex(int serial, String p_lte_freq) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(p_lte_freq);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(190, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getRatCombinePrio(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(191, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getDsFlowInfo(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(192, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getDeviceVersion(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(193, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setSciChgCfg(int serial, int slot1, int slot2, int slot3) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(slot1);
            _hidl_request.writeInt32(slot2);
            _hidl_request.writeInt32(slot3);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(194, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void vsimCheckCard(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(195, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setDsFlowNvWriteConfigPara(int serial, int index1, int index2) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(index1);
            _hidl_request.writeInt32(index2);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(196, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setUeOperationMode(int serial, int index1) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(index1);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(197, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void vsimPower(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(198, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void queryServiceCellBand(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(199, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setTime(int serial, String date, String strtime, String zone) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(date);
            _hidl_request.writeString(strtime);
            _hidl_request.writeString(zone);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(200, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void vsimBasebandVersion(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(201, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getLwclash(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(202, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setDmPcscf(int serial, String pcscf) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(pcscf);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(203, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setDmDynamicPram(int serial, String dyPram) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(dyPram);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(204, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setDmTimer(int serial, String timer) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(timer);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(205, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setDmSmsPsi(int serial, String smsPsi) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(smsPsi);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(206, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setNickName(int serial, String nickName) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(nickName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(207, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getDmPcscf(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(208, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getDmDynamicPram(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(209, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getDmTimer(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(210, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getSmsPsi(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(211, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getNickName(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(212, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getDmUser(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(213, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void imsRegister(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(214, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void informBatteryStatus(int serial, int status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(215, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setApDsFlowReportConfig(int serial, int enable_rpt, int threshold, int total_threshold, int oper) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(enable_rpt);
            _hidl_request.writeInt32(threshold);
            _hidl_request.writeInt32(total_threshold);
            _hidl_request.writeInt32(oper);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(216, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifyCellularCommParaReady(int serial, int paratype, int pathtype) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(paratype);
            _hidl_request.writeInt32(pathtype);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(217, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendPseudocellCellInfo(int serial, int infoType, int lac, int cid, int radiotech, String plmn) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(infoType);
            _hidl_request.writeInt32(lac);
            _hidl_request.writeInt32(cid);
            _hidl_request.writeInt32(radiotech);
            _hidl_request.writeString(plmn);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(218, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifyCellularCloudParaReady(int serial, int paratype, int pathtype) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(paratype);
            _hidl_request.writeInt32(pathtype);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(219, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getHwSignalStrength(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(220, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getAvailableCsgIds(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(221, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void manualSelectionCsgId(int serial, CsgNetworkInfo csgInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            csgInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(222, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getLastImsCallFailCause(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(223, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setImsvtCapability(int serial, int capabilityType, int capabilitySwitch) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(capabilityType);
            _hidl_request.writeInt32(capabilitySwitch);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(224, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setWifiEmergencyAid(int serial, String wifiAid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(wifiAid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(225, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendLaaCmd(int serial, int cmd, String reserved) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(cmd);
            _hidl_request.writeString(reserved);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(226, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getLaaDetailedState(int serial, String reserved) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(reserved);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(227, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifyCModemStatus(int serial, int status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(228, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setRatCombinePrio(int serial, int combineMode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(combineMode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(229, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setHwRFChannelSwitch(int serial, int rfChannel) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(rfChannel);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(230, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setRiseCdmaCutoffFreq(int serial, boolean on) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(on);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(231, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setVsimTEEDataReady(int serial, int apn, int dh, int sim) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(apn);
            _hidl_request.writeInt32(dh);
            _hidl_request.writeInt32(sim);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(232, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCdmaChrInfo(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(233, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void testVoiceLoopback(int serial, int mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(234, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setupDataCallEmergency(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(235, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void deactivateDataCallEmergency(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(236, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCellInfoListOtdoa(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(237, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void switchImsWaitingOrHoldingAndActiveWithType(int serial, int callType) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(callType);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(238, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void simlockNwDataWrite(int serial, int field, String data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(field);
            _hidl_request.writeString(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(239, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void informModemTetherStatusToChangeGRO(int serial, int tetherConnectStatus, String rmnetName) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(tetherConnectStatus);
            _hidl_request.writeString(rmnetName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(240, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setDmRcsConfig(int serial, int rcsCapability, int devConfig) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(rcsCapability);
            _hidl_request.writeInt32(devConfig);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(241, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setRcsSwitch(int serial, int switchState) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(switchState);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(242, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getRcsSwitchState(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(243, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getVolteImpi(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(244, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getVolteDomain(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(245, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void cancelImsVideoCall(int serial, int callIndex) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(callIndex);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(246, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void supplyDepersonalization(int serial, String netPin, int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(netPin);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(247, _hidl_request, _hidl_reply, 1);
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
                ArrayList<String> _hidl_out_descriptors = _hidl_reply.readStringVector();
                return _hidl_out_descriptors;
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
                String _hidl_out_descriptor = _hidl_reply.readString();
                return _hidl_out_descriptor;
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<byte[]> getHashChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
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

        public boolean unlinkToDeath(DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IRadio {
        public IHwBinder asBinder() {
            return this;
        }

        public final ArrayList<String> interfaceChain() {
            return new ArrayList(Arrays.asList(new String[]{IRadio.kInterfaceName, vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName, android.hardware.radio.V1_0.IRadio.kInterfaceName, IBase.kInterfaceName}));
        }

        public final String interfaceDescriptor() {
            return IRadio.kInterfaceName;
        }

        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList(Arrays.asList(new byte[][]{new byte[]{(byte) 95, (byte) -14, (byte) 61, (byte) 44, (byte) 10, (byte) 114, (byte) -115, (byte) 2, (byte) -112, (byte) -70, Byte.MIN_VALUE, (byte) -32, (byte) 95, (byte) 13, (byte) -114, (byte) -37, (byte) -72, (byte) -49, (byte) -98, (byte) 89, (byte) -50, (byte) 74, (byte) -41, (byte) 88, (byte) -24, (byte) -65, (byte) -68, (byte) 70, (byte) 85, (byte) -15, (byte) 65, (byte) -63}, new byte[]{(byte) 64, (byte) 74, (byte) 70, (byte) 79, (byte) -81, (byte) -114, (byte) -13, (byte) 19, (byte) -52, (byte) 21, (byte) 41, (byte) -31, (byte) -49, (byte) 31, (byte) 5, (byte) 100, (byte) -50, (byte) -45, (byte) -39, (byte) 3, (byte) 118, (byte) 29, (byte) -10, (byte) -33, (byte) -117, (byte) -43, (byte) 116, (byte) -117, (byte) 114, (byte) 68, (byte) 83, (byte) -56}, new byte[]{(byte) -101, (byte) 90, (byte) -92, (byte) -103, (byte) -20, (byte) 59, (byte) 66, (byte) 38, (byte) -15, (byte) 95, (byte) 72, (byte) -11, (byte) -19, (byte) 8, (byte) -119, (byte) 110, (byte) 47, (byte) -64, (byte) 103, (byte) 111, (byte) -105, (byte) -116, (byte) -98, (byte) 25, (byte) -100, (byte) 29, (byte) -94, (byte) 29, (byte) -86, (byte) -16, (byte) 2, (byte) -90}, new byte[]{(byte) -67, (byte) -38, (byte) -74, (byte) 24, (byte) 77, (byte) 122, (byte) 52, (byte) 109, (byte) -90, (byte) -96, (byte) 125, (byte) -64, (byte) -126, (byte) -116, (byte) -15, (byte) -102, (byte) 105, (byte) 111, (byte) 76, (byte) -86, (byte) 54, (byte) 17, (byte) -59, (byte) 31, (byte) 46, (byte) 20, (byte) 86, (byte) 90, (byte) 20, (byte) -76, (byte) 15, (byte) -39}}));
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
            if (IRadio.kInterfaceName.equals(descriptor)) {
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
            int serial;
            GsmSmsMessage message;
            DataProfileInfo dataProfileInfo;
            CallForwardInfo callInfo;
            SimApdu message2;
            RILImsCallModify callModifyInfo;
            switch (_hidl_code) {
                case 1:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setResponseFunctions(IRadioResponse.asInterface(_hidl_request.readStrongBinder()), IRadioIndication.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 2:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getIccCardStatus(_hidl_request.readInt32());
                    return;
                case 3:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    supplyIccPinForApp(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 4:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    supplyIccPukForApp(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 5:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    supplyIccPin2ForApp(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 6:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    supplyIccPuk2ForApp(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 7:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    changeIccPinForApp(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 8:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    changeIccPin2ForApp(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 9:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    supplyNetworkDepersonalization(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 10:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getCurrentCalls(_hidl_request.readInt32());
                    return;
                case 11:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    Dial dialInfo = new Dial();
                    dialInfo.readFromParcel(_hidl_request);
                    dial(serial, dialInfo);
                    return;
                case 12:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getImsiForApp(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 13:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    hangup(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 14:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    hangupWaitingOrBackground(_hidl_request.readInt32());
                    return;
                case 15:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    hangupForegroundResumeBackground(_hidl_request.readInt32());
                    return;
                case 16:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    switchWaitingOrHoldingAndActive(_hidl_request.readInt32());
                    return;
                case 17:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    conference(_hidl_request.readInt32());
                    return;
                case 18:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    rejectCall(_hidl_request.readInt32());
                    return;
                case 19:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getLastCallFailCause(_hidl_request.readInt32());
                    return;
                case 20:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getSignalStrength(_hidl_request.readInt32());
                    return;
                case 21:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getVoiceRegistrationState(_hidl_request.readInt32());
                    return;
                case 22:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getDataRegistrationState(_hidl_request.readInt32());
                    return;
                case 23:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getOperator(_hidl_request.readInt32());
                    return;
                case 24:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setRadioPower(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 25:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    sendDtmf(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 26:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    message = new GsmSmsMessage();
                    message.readFromParcel(_hidl_request);
                    sendSms(serial, message);
                    return;
                case 27:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    message = new GsmSmsMessage();
                    message.readFromParcel(_hidl_request);
                    sendSMSExpectMore(serial, message);
                    return;
                case 28:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    int radioTechnology = _hidl_request.readInt32();
                    dataProfileInfo = new DataProfileInfo();
                    dataProfileInfo.readFromParcel(_hidl_request);
                    setupDataCall(serial, radioTechnology, dataProfileInfo, _hidl_request.readBool(), _hidl_request.readBool(), _hidl_request.readBool());
                    return;
                case 29:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    IccIo iccIo = new IccIo();
                    iccIo.readFromParcel(_hidl_request);
                    iccIOForApp(serial, iccIo);
                    return;
                case 30:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    sendUssd(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 31:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    cancelPendingUssd(_hidl_request.readInt32());
                    return;
                case 32:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getClir(_hidl_request.readInt32());
                    return;
                case 33:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setClir(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 34:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    callInfo = new CallForwardInfo();
                    callInfo.readFromParcel(_hidl_request);
                    getCallForwardStatus(serial, callInfo);
                    return;
                case 35:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    callInfo = new CallForwardInfo();
                    callInfo.readFromParcel(_hidl_request);
                    setCallForward(serial, callInfo);
                    return;
                case 36:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getCallWaiting(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 37:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setCallWaiting(_hidl_request.readInt32(), _hidl_request.readBool(), _hidl_request.readInt32());
                    return;
                case 38:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    acknowledgeLastIncomingGsmSms(_hidl_request.readInt32(), _hidl_request.readBool(), _hidl_request.readInt32());
                    return;
                case 39:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    acceptCall(_hidl_request.readInt32());
                    return;
                case 40:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    deactivateDataCall(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 41:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getFacilityLockForApp(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 42:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setFacilityLockForApp(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readBool(), _hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 43:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setBarringPassword(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 44:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getNetworkSelectionMode(_hidl_request.readInt32());
                    return;
                case 45:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setNetworkSelectionModeAutomatic(_hidl_request.readInt32());
                    return;
                case 46:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setNetworkSelectionModeManual(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 47:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getAvailableNetworks(_hidl_request.readInt32());
                    return;
                case RadioError.NO_SMS_TO_ACK /*48*/:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    startDtmf(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 49:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    stopDtmf(_hidl_request.readInt32());
                    return;
                case 50:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getBasebandVersion(_hidl_request.readInt32());
                    return;
                case 51:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    separateConnection(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 52:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setMute(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 53:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getMute(_hidl_request.readInt32());
                    return;
                case 54:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getClip(_hidl_request.readInt32());
                    return;
                case 55:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getDataCallList(_hidl_request.readInt32());
                    return;
                case 56:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setSuppServiceNotifications(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 57:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    SmsWriteArgs smsWriteArgs = new SmsWriteArgs();
                    smsWriteArgs.readFromParcel(_hidl_request);
                    writeSmsToSim(serial, smsWriteArgs);
                    return;
                case 58:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    deleteSmsOnSim(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 59:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setBandMode(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case RadioError.NETWORK_NOT_READY /*60*/:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getAvailableBandModes(_hidl_request.readInt32());
                    return;
                case RadioError.NOT_PROVISIONED /*61*/:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    sendEnvelope(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case RadioError.NO_SUBSCRIPTION /*62*/:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    sendTerminalResponseToSim(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 63:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    handleStkCallSetupRequestFromSim(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 64:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    explicitCallTransfer(_hidl_request.readInt32());
                    return;
                case 65:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setPreferredNetworkType(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 66:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getPreferredNetworkType(_hidl_request.readInt32());
                    return;
                case HwRadarUtils.RADAR_LEVEL_C /*67*/:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getNeighboringCids(_hidl_request.readInt32());
                    return;
                case 68:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setLocationUpdates(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 69:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setCdmaSubscriptionSource(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 70:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setCdmaRoamingPreference(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 71:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getCdmaRoamingPreference(_hidl_request.readInt32());
                    return;
                case 72:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setTTYMode(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 73:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getTTYMode(_hidl_request.readInt32());
                    return;
                case 74:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setPreferredVoicePrivacy(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 75:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getPreferredVoicePrivacy(_hidl_request.readInt32());
                    return;
                case 76:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    sendCDMAFeatureCode(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 77:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    sendBurstDtmf(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 78:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    CdmaSmsMessage sms = new CdmaSmsMessage();
                    sms.readFromParcel(_hidl_request);
                    sendCdmaSms(serial, sms);
                    return;
                case 79:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    CdmaSmsAck smsAck = new CdmaSmsAck();
                    smsAck.readFromParcel(_hidl_request);
                    acknowledgeLastIncomingCdmaSms(serial, smsAck);
                    return;
                case RadioNVItems.RIL_NV_LTE_NEXT_SCAN /*80*/:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getGsmBroadcastConfig(_hidl_request.readInt32());
                    return;
                case 81:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setGsmBroadcastConfig(_hidl_request.readInt32(), GsmBroadcastSmsConfigInfo.readVectorFromParcel(_hidl_request));
                    return;
                case RadioNVItems.RIL_NV_LTE_BSR_MAX_TIME /*82*/:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setGsmBroadcastActivation(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 83:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getCdmaBroadcastConfig(_hidl_request.readInt32());
                    return;
                case 84:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setCdmaBroadcastConfig(_hidl_request.readInt32(), CdmaBroadcastSmsConfigInfo.readVectorFromParcel(_hidl_request));
                    return;
                case 85:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setCdmaBroadcastActivation(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 86:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getCDMASubscription(_hidl_request.readInt32());
                    return;
                case 87:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    CdmaSmsWriteArgs cdmaSms = new CdmaSmsWriteArgs();
                    cdmaSms.readFromParcel(_hidl_request);
                    writeSmsToRuim(serial, cdmaSms);
                    return;
                case 88:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    deleteSmsOnRuim(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 89:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getDeviceIdentity(_hidl_request.readInt32());
                    return;
                case 90:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    exitEmergencyCallbackMode(_hidl_request.readInt32());
                    return;
                case 91:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getSmscAddress(_hidl_request.readInt32());
                    return;
                case 92:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setSmscAddress(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 93:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    reportSmsMemoryStatus(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 94:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    reportStkServiceIsRunning(_hidl_request.readInt32());
                    return;
                case 95:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getCdmaSubscriptionSource(_hidl_request.readInt32());
                    return;
                case 96:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    requestIsimAuthentication(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 97:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    acknowledgeIncomingGsmSmsWithPdu(_hidl_request.readInt32(), _hidl_request.readBool(), _hidl_request.readString());
                    return;
                case 98:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    sendEnvelopeWithStatus(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 99:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getVoiceRadioTechnology(_hidl_request.readInt32());
                    return;
                case 100:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getCellInfoList(_hidl_request.readInt32());
                    return;
                case 101:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setCellInfoListRate(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 102:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    dataProfileInfo = new DataProfileInfo();
                    dataProfileInfo.readFromParcel(_hidl_request);
                    setInitialAttachApn(serial, dataProfileInfo, _hidl_request.readBool(), _hidl_request.readBool());
                    return;
                case 103:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getImsRegistrationState(_hidl_request.readInt32());
                    return;
                case AbstractPhoneBase.EVENT_ECC_NUM /*104*/:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    ImsSmsMessage message3 = new ImsSmsMessage();
                    message3.readFromParcel(_hidl_request);
                    sendImsSms(serial, message3);
                    return;
                case AbstractPhoneBase.EVENT_GET_IMSI_DONE /*105*/:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    message2 = new SimApdu();
                    message2.readFromParcel(_hidl_request);
                    iccTransmitApduBasicChannel(serial, message2);
                    return;
                case 106:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    iccOpenLogicalChannel(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readInt32());
                    return;
                case 107:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    iccCloseLogicalChannel(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case AbstractPhoneBase.EVENT_GET_LTE_RELEASE_VERSION_DONE /*108*/:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    message2 = new SimApdu();
                    message2.readFromParcel(_hidl_request);
                    iccTransmitApduLogicalChannel(serial, message2);
                    return;
                case 109:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    nvReadItem(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 110:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    NvWriteItem item = new NvWriteItem();
                    item.readFromParcel(_hidl_request);
                    nvWriteItem(serial, item);
                    return;
                case 111:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    nvWriteCdmaPrl(_hidl_request.readInt32(), _hidl_request.readInt8Vector());
                    return;
                case 112:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    nvResetConfig(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 113:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    SelectUiccSub uiccSub = new SelectUiccSub();
                    uiccSub.readFromParcel(_hidl_request);
                    setUiccSubscription(serial, uiccSub);
                    return;
                case 114:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setDataAllowed(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 115:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getHardwareConfig(_hidl_request.readInt32());
                    return;
                case 116:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    requestIccSimAuthentication(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 117:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setDataProfile(_hidl_request.readInt32(), DataProfileInfo.readVectorFromParcel(_hidl_request), _hidl_request.readBool());
                    return;
                case 118:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    requestShutdown(_hidl_request.readInt32());
                    return;
                case 119:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getRadioCapability(_hidl_request.readInt32());
                    return;
                case 120:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    RadioCapability rc = new RadioCapability();
                    rc.readFromParcel(_hidl_request);
                    setRadioCapability(serial, rc);
                    return;
                case 121:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    startLceService(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 122:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    stopLceService(_hidl_request.readInt32());
                    return;
                case 123:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    pullLceData(_hidl_request.readInt32());
                    return;
                case 124:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getModemActivityInfo(_hidl_request.readInt32());
                    return;
                case 125:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    boolean allAllowed = _hidl_request.readBool();
                    CarrierRestrictions carriers = new CarrierRestrictions();
                    carriers.readFromParcel(_hidl_request);
                    setAllowedCarriers(serial, allAllowed, carriers);
                    return;
                case 126:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getAllowedCarriers(_hidl_request.readInt32());
                    return;
                case 127:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    sendDeviceState(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 128:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setIndicationFilter(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 129:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setSimCardPower(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 130:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadio.kInterfaceName);
                    responseAcknowledgement();
                    return;
                case 131:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setResponseFunctionsHuawei(vendor.huawei.hardware.radio.V1_0.IRadioResponse.asInterface(_hidl_request.readStrongBinder()), vendor.huawei.hardware.radio.V1_0.IRadioIndication.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 132:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setPowerGrade(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 133:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getSimHotplugState(_hidl_request.readInt32());
                    return;
                case 134:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getCardType(_hidl_request.readInt32());
                    return;
                case 135:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getCdmaGsmImsi(_hidl_request.readInt32());
                    return;
                case 136:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    restartRILD(_hidl_request.readInt32());
                    return;
                case 137:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getSimATR(_hidl_request.readInt32());
                    return;
                case 138:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getIccid(_hidl_request.readInt32());
                    return;
                case 139:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setSimSlot(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 140:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getSimSlot(_hidl_request.readInt32());
                    return;
                case 141:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setCdmaModeSide(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 142:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getCdmaModeSide(_hidl_request.readInt32());
                    return;
                case 143:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setActiveModemMode(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 144:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    openChannelWithP2(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 145:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getEccNum(_hidl_request.readInt32());
                    return;
                case 146:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setEccNum(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 147:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setVoicecallBackGroundState(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 148:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getVoicecallBackGroundState(_hidl_request.readInt32());
                    return;
                case 149:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setVoicePreferStatus(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 150:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getVoicePreferStatus(_hidl_request.readInt32());
                    return;
                case 151:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    RILImsDial dialInfo2 = new RILImsDial();
                    dialInfo2.readFromParcel(_hidl_request);
                    imsDial(serial, dialInfo2);
                    return;
                case 152:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getCurrentImsCalls(_hidl_request.readInt32());
                    return;
                case 153:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    acceptImsCall(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 154:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getImsImpu(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 155:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    addImsConfMember(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 156:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setImsCallWaiting(_hidl_request.readInt32(), _hidl_request.readBool(), _hidl_request.readInt32());
                    return;
                case 157:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    callModifyInfo = new RILImsCallModify();
                    callModifyInfo.readFromParcel(_hidl_request);
                    modifyImsCallInitiate(serial, callModifyInfo);
                    return;
                case 158:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    callModifyInfo = new RILImsCallModify();
                    callModifyInfo.readFromParcel(_hidl_request);
                    modifyImsCallConfirm(serial, callModifyInfo);
                    return;
                case 159:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setImsSwitch(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 160:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getImsSwitch(_hidl_request.readInt32());
                    return;
                case 161:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    rejectCallWithReason(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 162:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getChannelInfo(_hidl_request.readInt32());
                    return;
                case 163:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    dataConnectionDeatch(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 164:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    dataConnectionAttach(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 165:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setLongMessage(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 166:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    resetAllConnections(_hidl_request.readInt32());
                    return;
                case 167:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setLteReleaseVersion(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 168:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getLteReleaseVersion(_hidl_request.readInt32());
                    return;
                case 169:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setImsDomain(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 170:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getImsDomain(_hidl_request.readInt32());
                    return;
                case 171:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    vowifiToImsaMsg(_hidl_request.readInt32(), _hidl_request.readInt8Vector());
                    return;
                case 172:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    RILUICCAUTH uiccAuthParam = new RILUICCAUTH();
                    uiccAuthParam.readFromParcel(_hidl_request);
                    uiccAuth(serial, uiccAuthParam);
                    return;
                case 173:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    RILUICCAUTH gbaParam = new RILUICCAUTH();
                    gbaParam.readFromParcel(_hidl_request);
                    uiccGbaBootStrap(serial, gbaParam);
                    return;
                case 174:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    RILCURSMUPDATEFILE uiccUpdateParam = new RILCURSMUPDATEFILE();
                    uiccUpdateParam.readFromParcel(_hidl_request);
                    updateUiccFile(serial, uiccUpdateParam);
                    return;
                case 175:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getUiccFile(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 176:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    RILUICCKSNAFAUTH ksNafAuthParam = new RILUICCKSNAFAUTH();
                    ksNafAuthParam.readFromParcel(_hidl_request);
                    uiccKsNafAuth(serial, ksNafAuthParam);
                    return;
                case 177:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setVtLlteQualityRptCfg(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 178:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    clearDsFlowInfo(_hidl_request.readInt32());
                    return;
                case 179:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getLocationInfo(_hidl_request.readInt32());
                    return;
                case 180:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setWifiPowerGrade(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 181:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    impactAntDevstate(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 182:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setNetworkRatAndSrvDomain(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 183:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setVsimSimState(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 184:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getVsimSimState(_hidl_request.readInt32());
                    return;
                case 185:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getSystemInfoEx(_hidl_request.readInt32());
                    return;
                case 186:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getPlmnInfo(_hidl_request.readInt32());
                    return;
                case 187:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getPolCapability(_hidl_request.readInt32());
                    return;
                case 188:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getPolList(_hidl_request.readInt32());
                    return;
                case 189:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setPolEntry(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 190:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setIsmcoex(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 191:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getRatCombinePrio(_hidl_request.readInt32());
                    return;
                case 192:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getDsFlowInfo(_hidl_request.readInt32());
                    return;
                case 193:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getDeviceVersion(_hidl_request.readInt32());
                    return;
                case 194:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setSciChgCfg(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 195:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    vsimCheckCard(_hidl_request.readInt32());
                    return;
                case 196:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setDsFlowNvWriteConfigPara(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 197:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setUeOperationMode(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 198:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    vsimPower(_hidl_request.readInt32());
                    return;
                case 199:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    queryServiceCellBand(_hidl_request.readInt32());
                    return;
                case 200:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setTime(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 201:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    vsimBasebandVersion(_hidl_request.readInt32());
                    return;
                case 202:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getLwclash(_hidl_request.readInt32());
                    return;
                case 203:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setDmPcscf(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 204:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setDmDynamicPram(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 205:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setDmTimer(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 206:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setDmSmsPsi(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 207:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setNickName(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 208:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getDmPcscf(_hidl_request.readInt32());
                    return;
                case 209:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getDmDynamicPram(_hidl_request.readInt32());
                    return;
                case 210:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getDmTimer(_hidl_request.readInt32());
                    return;
                case 211:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getSmsPsi(_hidl_request.readInt32());
                    return;
                case 212:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getNickName(_hidl_request.readInt32());
                    return;
                case 213:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getDmUser(_hidl_request.readInt32());
                    return;
                case 214:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    imsRegister(_hidl_request.readInt32());
                    return;
                case 215:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    informBatteryStatus(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 216:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setApDsFlowReportConfig(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 217:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    notifyCellularCommParaReady(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 218:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    sendPseudocellCellInfo(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 219:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    notifyCellularCloudParaReady(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 220:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getHwSignalStrength(_hidl_request.readInt32());
                    return;
                case 221:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getAvailableCsgIds(_hidl_request.readInt32());
                    return;
                case 222:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    CsgNetworkInfo csgInfo = new CsgNetworkInfo();
                    csgInfo.readFromParcel(_hidl_request);
                    manualSelectionCsgId(serial, csgInfo);
                    return;
                case 223:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getLastImsCallFailCause(_hidl_request.readInt32());
                    return;
                case 224:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setImsvtCapability(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 225:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setWifiEmergencyAid(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 226:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    sendLaaCmd(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 227:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getLaaDetailedState(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 228:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    notifyCModemStatus(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 229:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setRatCombinePrio(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 230:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setHwRFChannelSwitch(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 231:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setRiseCdmaCutoffFreq(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 232:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setVsimTEEDataReady(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 233:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getCdmaChrInfo(_hidl_request.readInt32());
                    return;
                case 234:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    testVoiceLoopback(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 235:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    setupDataCallEmergency(_hidl_request.readInt32());
                    return;
                case 236:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    deactivateDataCallEmergency(_hidl_request.readInt32());
                    return;
                case 237:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    getCellInfoListOtdoa(_hidl_request.readInt32());
                    return;
                case 238:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    switchImsWaitingOrHoldingAndActiveWithType(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 239:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    simlockNwDataWrite(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 240:
                    _hidl_request.enforceInterface(vendor.huawei.hardware.radio.V1_0.IRadio.kInterfaceName);
                    informModemTetherStatusToChangeGRO(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 241:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setDmRcsConfig(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 242:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setRcsSwitch(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 243:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getRcsSwitchState(_hidl_request.readInt32());
                    return;
                case 244:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getVolteImpi(_hidl_request.readInt32());
                    return;
                case 245:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getVolteDomain(_hidl_request.readInt32());
                    return;
                case 246:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    cancelImsVideoCall(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 247:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    supplyDepersonalization(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readInt32());
                    return;
                case 256067662:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    ArrayList<String> _hidl_out_descriptors = interfaceChain();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeStringVector(_hidl_out_descriptors);
                    _hidl_reply.send();
                    return;
                case 256131655:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 256136003:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    String _hidl_out_descriptor = interfaceDescriptor();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeString(_hidl_out_descriptor);
                    _hidl_reply.send();
                    return;
                case 256398152:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    ArrayList<byte[]> _hidl_out_hashchain = getHashChain();
                    _hidl_reply.writeStatus(0);
                    HwBlob hwBlob = new HwBlob(16);
                    int _hidl_vec_size = _hidl_out_hashchain.size();
                    hwBlob.putInt32(8, _hidl_vec_size);
                    hwBlob.putBool(12, false);
                    hwBlob = new HwBlob(_hidl_vec_size * 32);
                    for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                        long _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
                        for (int _hidl_index_1_0 = 0; _hidl_index_1_0 < 32; _hidl_index_1_0++) {
                            hwBlob.putInt8(_hidl_array_offset_1, ((byte[]) _hidl_out_hashchain.get(_hidl_index_0))[_hidl_index_1_0]);
                            _hidl_array_offset_1++;
                        }
                    }
                    hwBlob.putBlob(0, hwBlob);
                    _hidl_reply.writeBuffer(hwBlob);
                    _hidl_reply.send();
                    return;
                case 256462420:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    setHALInstrumentation();
                    return;
                case 257049926:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    DebugInfo _hidl_out_info = getDebugInfo();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_info.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 257120595:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    notifySyspropsChanged();
                    return;
                default:
                    return;
            }
        }
    }

    IHwBinder asBinder();

    void cancelImsVideoCall(int i, int i2) throws RemoteException;

    DebugInfo getDebugInfo() throws RemoteException;

    ArrayList<byte[]> getHashChain() throws RemoteException;

    void getRcsSwitchState(int i) throws RemoteException;

    void getVolteDomain(int i) throws RemoteException;

    void getVolteImpi(int i) throws RemoteException;

    ArrayList<String> interfaceChain() throws RemoteException;

    String interfaceDescriptor() throws RemoteException;

    boolean linkToDeath(DeathRecipient deathRecipient, long j) throws RemoteException;

    void notifySyspropsChanged() throws RemoteException;

    void ping() throws RemoteException;

    void setDmRcsConfig(int i, int i2, int i3) throws RemoteException;

    void setHALInstrumentation() throws RemoteException;

    void setRcsSwitch(int i, int i2) throws RemoteException;

    void supplyDepersonalization(int i, String str, int i2) throws RemoteException;

    boolean unlinkToDeath(DeathRecipient deathRecipient) throws RemoteException;

    static IRadio asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IRadio)) {
            return (IRadio) iface;
        }
        IRadio proxy = new Proxy(binder);
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

    static IRadio castFrom(IHwInterface iface) {
        return iface == null ? null : asInterface(iface.asBinder());
    }

    static IRadio getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IRadio getService() throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, "default"));
    }
}
