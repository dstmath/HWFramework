package android.hardware.radio.V1_0;

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

public interface IRadio extends IBase {
    public static final String kInterfaceName = "android.hardware.radio@1.0::IRadio";

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
                return "[class or subclass of android.hardware.radio@1.0::IRadio]@Proxy";
            }
        }

        public void setResponseFunctions(IRadioResponse radioResponse, IRadioIndication radioIndication) throws RemoteException {
            IHwBinder iHwBinder = null;
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
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
            _hidl_request.writeInterfaceToken(IRadio.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(130, _hidl_request, _hidl_reply, 1);
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
            return new ArrayList(Arrays.asList(new String[]{IRadio.kInterfaceName, IBase.kInterfaceName}));
        }

        public final String interfaceDescriptor() {
            return IRadio.kInterfaceName;
        }

        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList(Arrays.asList(new byte[][]{new byte[]{(byte) -101, (byte) 90, (byte) -92, (byte) -103, (byte) -20, (byte) 59, (byte) 66, (byte) 38, (byte) -15, (byte) 95, (byte) 72, (byte) -11, (byte) -19, (byte) 8, (byte) -119, (byte) 110, (byte) 47, (byte) -64, (byte) 103, (byte) 111, (byte) -105, (byte) -116, (byte) -98, (byte) 25, (byte) -100, (byte) 29, (byte) -94, (byte) 29, (byte) -86, (byte) -16, (byte) 2, (byte) -90}, new byte[]{(byte) -67, (byte) -38, (byte) -74, (byte) 24, (byte) 77, (byte) 122, (byte) 52, (byte) 109, (byte) -90, (byte) -96, (byte) 125, (byte) -64, (byte) -126, (byte) -116, (byte) -15, (byte) -102, (byte) 105, (byte) 111, (byte) 76, (byte) -86, (byte) 54, (byte) 17, (byte) -59, (byte) 31, (byte) 46, (byte) 20, (byte) 86, (byte) 90, (byte) 20, (byte) -76, (byte) 15, (byte) -39}}));
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
            switch (_hidl_code) {
                case 1:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setResponseFunctions(IRadioResponse.asInterface(_hidl_request.readStrongBinder()), IRadioIndication.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 2:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getIccCardStatus(_hidl_request.readInt32());
                    return;
                case 3:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    supplyIccPinForApp(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 4:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    supplyIccPukForApp(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 5:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    supplyIccPin2ForApp(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 6:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    supplyIccPuk2ForApp(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 7:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    changeIccPinForApp(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 8:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    changeIccPin2ForApp(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 9:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    supplyNetworkDepersonalization(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 10:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getCurrentCalls(_hidl_request.readInt32());
                    return;
                case 11:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    Dial dialInfo = new Dial();
                    dialInfo.readFromParcel(_hidl_request);
                    dial(serial, dialInfo);
                    return;
                case 12:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getImsiForApp(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 13:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    hangup(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 14:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    hangupWaitingOrBackground(_hidl_request.readInt32());
                    return;
                case 15:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    hangupForegroundResumeBackground(_hidl_request.readInt32());
                    return;
                case 16:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    switchWaitingOrHoldingAndActive(_hidl_request.readInt32());
                    return;
                case 17:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    conference(_hidl_request.readInt32());
                    return;
                case 18:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    rejectCall(_hidl_request.readInt32());
                    return;
                case 19:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getLastCallFailCause(_hidl_request.readInt32());
                    return;
                case 20:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getSignalStrength(_hidl_request.readInt32());
                    return;
                case 21:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getVoiceRegistrationState(_hidl_request.readInt32());
                    return;
                case 22:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getDataRegistrationState(_hidl_request.readInt32());
                    return;
                case 23:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getOperator(_hidl_request.readInt32());
                    return;
                case 24:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setRadioPower(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 25:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    sendDtmf(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 26:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    message = new GsmSmsMessage();
                    message.readFromParcel(_hidl_request);
                    sendSms(serial, message);
                    return;
                case 27:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    message = new GsmSmsMessage();
                    message.readFromParcel(_hidl_request);
                    sendSMSExpectMore(serial, message);
                    return;
                case 28:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    int radioTechnology = _hidl_request.readInt32();
                    dataProfileInfo = new DataProfileInfo();
                    dataProfileInfo.readFromParcel(_hidl_request);
                    setupDataCall(serial, radioTechnology, dataProfileInfo, _hidl_request.readBool(), _hidl_request.readBool(), _hidl_request.readBool());
                    return;
                case 29:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    IccIo iccIo = new IccIo();
                    iccIo.readFromParcel(_hidl_request);
                    iccIOForApp(serial, iccIo);
                    return;
                case 30:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    sendUssd(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 31:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    cancelPendingUssd(_hidl_request.readInt32());
                    return;
                case 32:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getClir(_hidl_request.readInt32());
                    return;
                case 33:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setClir(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 34:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    callInfo = new CallForwardInfo();
                    callInfo.readFromParcel(_hidl_request);
                    getCallForwardStatus(serial, callInfo);
                    return;
                case 35:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    callInfo = new CallForwardInfo();
                    callInfo.readFromParcel(_hidl_request);
                    setCallForward(serial, callInfo);
                    return;
                case 36:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getCallWaiting(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 37:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setCallWaiting(_hidl_request.readInt32(), _hidl_request.readBool(), _hidl_request.readInt32());
                    return;
                case 38:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    acknowledgeLastIncomingGsmSms(_hidl_request.readInt32(), _hidl_request.readBool(), _hidl_request.readInt32());
                    return;
                case 39:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    acceptCall(_hidl_request.readInt32());
                    return;
                case 40:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    deactivateDataCall(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 41:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getFacilityLockForApp(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 42:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setFacilityLockForApp(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readBool(), _hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 43:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setBarringPassword(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 44:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getNetworkSelectionMode(_hidl_request.readInt32());
                    return;
                case 45:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setNetworkSelectionModeAutomatic(_hidl_request.readInt32());
                    return;
                case 46:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setNetworkSelectionModeManual(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 47:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getAvailableNetworks(_hidl_request.readInt32());
                    return;
                case RadioError.NO_SMS_TO_ACK /*48*/:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    startDtmf(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 49:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    stopDtmf(_hidl_request.readInt32());
                    return;
                case 50:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getBasebandVersion(_hidl_request.readInt32());
                    return;
                case 51:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    separateConnection(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 52:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setMute(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 53:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getMute(_hidl_request.readInt32());
                    return;
                case 54:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getClip(_hidl_request.readInt32());
                    return;
                case 55:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getDataCallList(_hidl_request.readInt32());
                    return;
                case 56:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setSuppServiceNotifications(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 57:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    SmsWriteArgs smsWriteArgs = new SmsWriteArgs();
                    smsWriteArgs.readFromParcel(_hidl_request);
                    writeSmsToSim(serial, smsWriteArgs);
                    return;
                case 58:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    deleteSmsOnSim(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 59:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setBandMode(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case RadioError.NETWORK_NOT_READY /*60*/:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getAvailableBandModes(_hidl_request.readInt32());
                    return;
                case RadioError.NOT_PROVISIONED /*61*/:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    sendEnvelope(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case RadioError.NO_SUBSCRIPTION /*62*/:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    sendTerminalResponseToSim(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 63:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    handleStkCallSetupRequestFromSim(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 64:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    explicitCallTransfer(_hidl_request.readInt32());
                    return;
                case 65:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setPreferredNetworkType(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 66:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getPreferredNetworkType(_hidl_request.readInt32());
                    return;
                case HwRadarUtils.RADAR_LEVEL_C /*67*/:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getNeighboringCids(_hidl_request.readInt32());
                    return;
                case 68:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setLocationUpdates(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 69:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setCdmaSubscriptionSource(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 70:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setCdmaRoamingPreference(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 71:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getCdmaRoamingPreference(_hidl_request.readInt32());
                    return;
                case 72:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setTTYMode(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 73:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getTTYMode(_hidl_request.readInt32());
                    return;
                case 74:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setPreferredVoicePrivacy(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 75:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getPreferredVoicePrivacy(_hidl_request.readInt32());
                    return;
                case 76:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    sendCDMAFeatureCode(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 77:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    sendBurstDtmf(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 78:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    CdmaSmsMessage sms = new CdmaSmsMessage();
                    sms.readFromParcel(_hidl_request);
                    sendCdmaSms(serial, sms);
                    return;
                case 79:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    CdmaSmsAck smsAck = new CdmaSmsAck();
                    smsAck.readFromParcel(_hidl_request);
                    acknowledgeLastIncomingCdmaSms(serial, smsAck);
                    return;
                case RadioNVItems.RIL_NV_LTE_NEXT_SCAN /*80*/:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getGsmBroadcastConfig(_hidl_request.readInt32());
                    return;
                case 81:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setGsmBroadcastConfig(_hidl_request.readInt32(), GsmBroadcastSmsConfigInfo.readVectorFromParcel(_hidl_request));
                    return;
                case RadioNVItems.RIL_NV_LTE_BSR_MAX_TIME /*82*/:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setGsmBroadcastActivation(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 83:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getCdmaBroadcastConfig(_hidl_request.readInt32());
                    return;
                case 84:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setCdmaBroadcastConfig(_hidl_request.readInt32(), CdmaBroadcastSmsConfigInfo.readVectorFromParcel(_hidl_request));
                    return;
                case 85:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setCdmaBroadcastActivation(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 86:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getCDMASubscription(_hidl_request.readInt32());
                    return;
                case 87:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    CdmaSmsWriteArgs cdmaSms = new CdmaSmsWriteArgs();
                    cdmaSms.readFromParcel(_hidl_request);
                    writeSmsToRuim(serial, cdmaSms);
                    return;
                case 88:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    deleteSmsOnRuim(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 89:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getDeviceIdentity(_hidl_request.readInt32());
                    return;
                case 90:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    exitEmergencyCallbackMode(_hidl_request.readInt32());
                    return;
                case 91:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getSmscAddress(_hidl_request.readInt32());
                    return;
                case 92:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setSmscAddress(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 93:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    reportSmsMemoryStatus(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 94:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    reportStkServiceIsRunning(_hidl_request.readInt32());
                    return;
                case 95:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getCdmaSubscriptionSource(_hidl_request.readInt32());
                    return;
                case 96:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    requestIsimAuthentication(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 97:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    acknowledgeIncomingGsmSmsWithPdu(_hidl_request.readInt32(), _hidl_request.readBool(), _hidl_request.readString());
                    return;
                case 98:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    sendEnvelopeWithStatus(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 99:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getVoiceRadioTechnology(_hidl_request.readInt32());
                    return;
                case 100:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getCellInfoList(_hidl_request.readInt32());
                    return;
                case 101:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setCellInfoListRate(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 102:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    dataProfileInfo = new DataProfileInfo();
                    dataProfileInfo.readFromParcel(_hidl_request);
                    setInitialAttachApn(serial, dataProfileInfo, _hidl_request.readBool(), _hidl_request.readBool());
                    return;
                case 103:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getImsRegistrationState(_hidl_request.readInt32());
                    return;
                case AbstractPhoneBase.EVENT_ECC_NUM /*104*/:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    ImsSmsMessage message3 = new ImsSmsMessage();
                    message3.readFromParcel(_hidl_request);
                    sendImsSms(serial, message3);
                    return;
                case AbstractPhoneBase.EVENT_GET_IMSI_DONE /*105*/:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    message2 = new SimApdu();
                    message2.readFromParcel(_hidl_request);
                    iccTransmitApduBasicChannel(serial, message2);
                    return;
                case 106:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    iccOpenLogicalChannel(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readInt32());
                    return;
                case 107:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    iccCloseLogicalChannel(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case AbstractPhoneBase.EVENT_GET_LTE_RELEASE_VERSION_DONE /*108*/:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    message2 = new SimApdu();
                    message2.readFromParcel(_hidl_request);
                    iccTransmitApduLogicalChannel(serial, message2);
                    return;
                case 109:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    nvReadItem(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 110:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    NvWriteItem item = new NvWriteItem();
                    item.readFromParcel(_hidl_request);
                    nvWriteItem(serial, item);
                    return;
                case 111:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    nvWriteCdmaPrl(_hidl_request.readInt32(), _hidl_request.readInt8Vector());
                    return;
                case 112:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    nvResetConfig(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 113:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    SelectUiccSub uiccSub = new SelectUiccSub();
                    uiccSub.readFromParcel(_hidl_request);
                    setUiccSubscription(serial, uiccSub);
                    return;
                case 114:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setDataAllowed(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 115:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getHardwareConfig(_hidl_request.readInt32());
                    return;
                case 116:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    requestIccSimAuthentication(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 117:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setDataProfile(_hidl_request.readInt32(), DataProfileInfo.readVectorFromParcel(_hidl_request), _hidl_request.readBool());
                    return;
                case 118:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    requestShutdown(_hidl_request.readInt32());
                    return;
                case 119:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getRadioCapability(_hidl_request.readInt32());
                    return;
                case 120:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    RadioCapability rc = new RadioCapability();
                    rc.readFromParcel(_hidl_request);
                    setRadioCapability(serial, rc);
                    return;
                case 121:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    startLceService(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 122:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    stopLceService(_hidl_request.readInt32());
                    return;
                case 123:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    pullLceData(_hidl_request.readInt32());
                    return;
                case 124:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getModemActivityInfo(_hidl_request.readInt32());
                    return;
                case 125:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    serial = _hidl_request.readInt32();
                    boolean allAllowed = _hidl_request.readBool();
                    CarrierRestrictions carriers = new CarrierRestrictions();
                    carriers.readFromParcel(_hidl_request);
                    setAllowedCarriers(serial, allAllowed, carriers);
                    return;
                case 126:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    getAllowedCarriers(_hidl_request.readInt32());
                    return;
                case 127:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    sendDeviceState(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 128:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setIndicationFilter(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 129:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    setSimCardPower(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 130:
                    _hidl_request.enforceInterface(IRadio.kInterfaceName);
                    responseAcknowledgement();
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

    void acceptCall(int i) throws RemoteException;

    void acknowledgeIncomingGsmSmsWithPdu(int i, boolean z, String str) throws RemoteException;

    void acknowledgeLastIncomingCdmaSms(int i, CdmaSmsAck cdmaSmsAck) throws RemoteException;

    void acknowledgeLastIncomingGsmSms(int i, boolean z, int i2) throws RemoteException;

    IHwBinder asBinder();

    void cancelPendingUssd(int i) throws RemoteException;

    void changeIccPin2ForApp(int i, String str, String str2, String str3) throws RemoteException;

    void changeIccPinForApp(int i, String str, String str2, String str3) throws RemoteException;

    void conference(int i) throws RemoteException;

    void deactivateDataCall(int i, int i2, boolean z) throws RemoteException;

    void deleteSmsOnRuim(int i, int i2) throws RemoteException;

    void deleteSmsOnSim(int i, int i2) throws RemoteException;

    void dial(int i, Dial dial) throws RemoteException;

    void exitEmergencyCallbackMode(int i) throws RemoteException;

    void explicitCallTransfer(int i) throws RemoteException;

    void getAllowedCarriers(int i) throws RemoteException;

    void getAvailableBandModes(int i) throws RemoteException;

    void getAvailableNetworks(int i) throws RemoteException;

    void getBasebandVersion(int i) throws RemoteException;

    void getCDMASubscription(int i) throws RemoteException;

    void getCallForwardStatus(int i, CallForwardInfo callForwardInfo) throws RemoteException;

    void getCallWaiting(int i, int i2) throws RemoteException;

    void getCdmaBroadcastConfig(int i) throws RemoteException;

    void getCdmaRoamingPreference(int i) throws RemoteException;

    void getCdmaSubscriptionSource(int i) throws RemoteException;

    void getCellInfoList(int i) throws RemoteException;

    void getClip(int i) throws RemoteException;

    void getClir(int i) throws RemoteException;

    void getCurrentCalls(int i) throws RemoteException;

    void getDataCallList(int i) throws RemoteException;

    void getDataRegistrationState(int i) throws RemoteException;

    DebugInfo getDebugInfo() throws RemoteException;

    void getDeviceIdentity(int i) throws RemoteException;

    void getFacilityLockForApp(int i, String str, String str2, int i2, String str3) throws RemoteException;

    void getGsmBroadcastConfig(int i) throws RemoteException;

    void getHardwareConfig(int i) throws RemoteException;

    ArrayList<byte[]> getHashChain() throws RemoteException;

    void getIccCardStatus(int i) throws RemoteException;

    void getImsRegistrationState(int i) throws RemoteException;

    void getImsiForApp(int i, String str) throws RemoteException;

    void getLastCallFailCause(int i) throws RemoteException;

    void getModemActivityInfo(int i) throws RemoteException;

    void getMute(int i) throws RemoteException;

    void getNeighboringCids(int i) throws RemoteException;

    void getNetworkSelectionMode(int i) throws RemoteException;

    void getOperator(int i) throws RemoteException;

    void getPreferredNetworkType(int i) throws RemoteException;

    void getPreferredVoicePrivacy(int i) throws RemoteException;

    void getRadioCapability(int i) throws RemoteException;

    void getSignalStrength(int i) throws RemoteException;

    void getSmscAddress(int i) throws RemoteException;

    void getTTYMode(int i) throws RemoteException;

    void getVoiceRadioTechnology(int i) throws RemoteException;

    void getVoiceRegistrationState(int i) throws RemoteException;

    void handleStkCallSetupRequestFromSim(int i, boolean z) throws RemoteException;

    void hangup(int i, int i2) throws RemoteException;

    void hangupForegroundResumeBackground(int i) throws RemoteException;

    void hangupWaitingOrBackground(int i) throws RemoteException;

    void iccCloseLogicalChannel(int i, int i2) throws RemoteException;

    void iccIOForApp(int i, IccIo iccIo) throws RemoteException;

    void iccOpenLogicalChannel(int i, String str, int i2) throws RemoteException;

    void iccTransmitApduBasicChannel(int i, SimApdu simApdu) throws RemoteException;

    void iccTransmitApduLogicalChannel(int i, SimApdu simApdu) throws RemoteException;

    ArrayList<String> interfaceChain() throws RemoteException;

    String interfaceDescriptor() throws RemoteException;

    boolean linkToDeath(DeathRecipient deathRecipient, long j) throws RemoteException;

    void notifySyspropsChanged() throws RemoteException;

    void nvReadItem(int i, int i2) throws RemoteException;

    void nvResetConfig(int i, int i2) throws RemoteException;

    void nvWriteCdmaPrl(int i, ArrayList<Byte> arrayList) throws RemoteException;

    void nvWriteItem(int i, NvWriteItem nvWriteItem) throws RemoteException;

    void ping() throws RemoteException;

    void pullLceData(int i) throws RemoteException;

    void rejectCall(int i) throws RemoteException;

    void reportSmsMemoryStatus(int i, boolean z) throws RemoteException;

    void reportStkServiceIsRunning(int i) throws RemoteException;

    void requestIccSimAuthentication(int i, int i2, String str, String str2) throws RemoteException;

    void requestIsimAuthentication(int i, String str) throws RemoteException;

    void requestShutdown(int i) throws RemoteException;

    void responseAcknowledgement() throws RemoteException;

    void sendBurstDtmf(int i, String str, int i2, int i3) throws RemoteException;

    void sendCDMAFeatureCode(int i, String str) throws RemoteException;

    void sendCdmaSms(int i, CdmaSmsMessage cdmaSmsMessage) throws RemoteException;

    void sendDeviceState(int i, int i2, boolean z) throws RemoteException;

    void sendDtmf(int i, String str) throws RemoteException;

    void sendEnvelope(int i, String str) throws RemoteException;

    void sendEnvelopeWithStatus(int i, String str) throws RemoteException;

    void sendImsSms(int i, ImsSmsMessage imsSmsMessage) throws RemoteException;

    void sendSMSExpectMore(int i, GsmSmsMessage gsmSmsMessage) throws RemoteException;

    void sendSms(int i, GsmSmsMessage gsmSmsMessage) throws RemoteException;

    void sendTerminalResponseToSim(int i, String str) throws RemoteException;

    void sendUssd(int i, String str) throws RemoteException;

    void separateConnection(int i, int i2) throws RemoteException;

    void setAllowedCarriers(int i, boolean z, CarrierRestrictions carrierRestrictions) throws RemoteException;

    void setBandMode(int i, int i2) throws RemoteException;

    void setBarringPassword(int i, String str, String str2, String str3) throws RemoteException;

    void setCallForward(int i, CallForwardInfo callForwardInfo) throws RemoteException;

    void setCallWaiting(int i, boolean z, int i2) throws RemoteException;

    void setCdmaBroadcastActivation(int i, boolean z) throws RemoteException;

    void setCdmaBroadcastConfig(int i, ArrayList<CdmaBroadcastSmsConfigInfo> arrayList) throws RemoteException;

    void setCdmaRoamingPreference(int i, int i2) throws RemoteException;

    void setCdmaSubscriptionSource(int i, int i2) throws RemoteException;

    void setCellInfoListRate(int i, int i2) throws RemoteException;

    void setClir(int i, int i2) throws RemoteException;

    void setDataAllowed(int i, boolean z) throws RemoteException;

    void setDataProfile(int i, ArrayList<DataProfileInfo> arrayList, boolean z) throws RemoteException;

    void setFacilityLockForApp(int i, String str, boolean z, String str2, int i2, String str3) throws RemoteException;

    void setGsmBroadcastActivation(int i, boolean z) throws RemoteException;

    void setGsmBroadcastConfig(int i, ArrayList<GsmBroadcastSmsConfigInfo> arrayList) throws RemoteException;

    void setHALInstrumentation() throws RemoteException;

    void setIndicationFilter(int i, int i2) throws RemoteException;

    void setInitialAttachApn(int i, DataProfileInfo dataProfileInfo, boolean z, boolean z2) throws RemoteException;

    void setLocationUpdates(int i, boolean z) throws RemoteException;

    void setMute(int i, boolean z) throws RemoteException;

    void setNetworkSelectionModeAutomatic(int i) throws RemoteException;

    void setNetworkSelectionModeManual(int i, String str) throws RemoteException;

    void setPreferredNetworkType(int i, int i2) throws RemoteException;

    void setPreferredVoicePrivacy(int i, boolean z) throws RemoteException;

    void setRadioCapability(int i, RadioCapability radioCapability) throws RemoteException;

    void setRadioPower(int i, boolean z) throws RemoteException;

    void setResponseFunctions(IRadioResponse iRadioResponse, IRadioIndication iRadioIndication) throws RemoteException;

    void setSimCardPower(int i, boolean z) throws RemoteException;

    void setSmscAddress(int i, String str) throws RemoteException;

    void setSuppServiceNotifications(int i, boolean z) throws RemoteException;

    void setTTYMode(int i, int i2) throws RemoteException;

    void setUiccSubscription(int i, SelectUiccSub selectUiccSub) throws RemoteException;

    void setupDataCall(int i, int i2, DataProfileInfo dataProfileInfo, boolean z, boolean z2, boolean z3) throws RemoteException;

    void startDtmf(int i, String str) throws RemoteException;

    void startLceService(int i, int i2, boolean z) throws RemoteException;

    void stopDtmf(int i) throws RemoteException;

    void stopLceService(int i) throws RemoteException;

    void supplyIccPin2ForApp(int i, String str, String str2) throws RemoteException;

    void supplyIccPinForApp(int i, String str, String str2) throws RemoteException;

    void supplyIccPuk2ForApp(int i, String str, String str2, String str3) throws RemoteException;

    void supplyIccPukForApp(int i, String str, String str2, String str3) throws RemoteException;

    void supplyNetworkDepersonalization(int i, String str) throws RemoteException;

    void switchWaitingOrHoldingAndActive(int i) throws RemoteException;

    boolean unlinkToDeath(DeathRecipient deathRecipient) throws RemoteException;

    void writeSmsToRuim(int i, CdmaSmsWriteArgs cdmaSmsWriteArgs) throws RemoteException;

    void writeSmsToSim(int i, SmsWriteArgs smsWriteArgs) throws RemoteException;

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
