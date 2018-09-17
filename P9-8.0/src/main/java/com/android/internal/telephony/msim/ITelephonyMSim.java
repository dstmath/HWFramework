package com.android.internal.telephony.msim;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.CellInfo;
import com.huawei.android.util.NoExtAPIException;
import java.util.List;

public interface ITelephonyMSim extends IInterface {

    public static abstract class Stub extends Binder implements ITelephonyMSim {
        private static final String DESCRIPTOR = "com.android.internal.telephony.msim.ITelephonyMSim";
        static final int TRANSACTION_answerRingingCall = 6;
        static final int TRANSACTION_call = 2;
        static final int TRANSACTION_cancelMissedCallsNotification = 13;
        static final int TRANSACTION_dial = 1;
        static final int TRANSACTION_disableApnType = 21;
        static final int TRANSACTION_disableDataConnectivity = 23;
        static final int TRANSACTION_enableApnType = 20;
        static final int TRANSACTION_enableDataConnectivity = 22;
        static final int TRANSACTION_endCall = 5;
        static final int TRANSACTION_getActivePhoneType = 28;
        static final int TRANSACTION_getAllCellInfo = 37;
        static final int TRANSACTION_getCallState = 25;
        static final int TRANSACTION_getCdmaEriIconIndex = 29;
        static final int TRANSACTION_getCdmaEriIconMode = 30;
        static final int TRANSACTION_getCdmaEriText = 31;
        static final int TRANSACTION_getDataActivity = 26;
        static final int TRANSACTION_getDataNetworkType = 42;
        static final int TRANSACTION_getDataState = 27;
        static final int TRANSACTION_getDefaultSubscription = 38;
        static final int TRANSACTION_getLteOnCdmaMode = 36;
        static final int TRANSACTION_getNetworkType = 34;
        static final int TRANSACTION_getPreferredDataSubscription = 40;
        static final int TRANSACTION_getPreferredVoiceSubscription = 39;
        static final int TRANSACTION_getVoiceMessageCount = 33;
        static final int TRANSACTION_handlePinMmi = 16;
        static final int TRANSACTION_hasIccCard = 35;
        static final int TRANSACTION_isDataConnectivityPossible = 24;
        static final int TRANSACTION_isIdle = 10;
        static final int TRANSACTION_isOffhook = 8;
        static final int TRANSACTION_isRadioOn = 11;
        static final int TRANSACTION_isRinging = 9;
        static final int TRANSACTION_isSimPinEnabled = 12;
        static final int TRANSACTION_needsOtaServiceProvisioning = 32;
        static final int TRANSACTION_setPreferredDataSubscription = 41;
        static final int TRANSACTION_setRadio = 18;
        static final int TRANSACTION_showCallScreen = 3;
        static final int TRANSACTION_showCallScreenWithDialpad = 4;
        static final int TRANSACTION_silenceRinger = 7;
        static final int TRANSACTION_supplyPin = 14;
        static final int TRANSACTION_supplyPuk = 15;
        static final int TRANSACTION_toggleRadioOnOff = 17;
        static final int TRANSACTION_updateServiceLocation = 19;

        private static class Proxy implements ITelephonyMSim {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void dial(String number, int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public void call(String number, int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public boolean showCallScreen() throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public boolean showCallScreenWithDialpad(boolean showDialpad) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public boolean endCall(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public void answerRingingCall(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public void silenceRinger() throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public boolean isOffhook(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public boolean isRinging(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public boolean isIdle(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public boolean isRadioOn(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public boolean isSimPinEnabled(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public void cancelMissedCallsNotification(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public boolean supplyPin(String pin, int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public boolean supplyPuk(String puk, String pin, int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int[] supplyPinReportResult(String pin, int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int[] supplyPukReportResult(String puk, String pin, int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public boolean handlePinMmi(String dialString, int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public void toggleRadioOnOff(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public boolean setRadio(boolean turnOn, int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public void updateServiceLocation(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int enableApnType(String type) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int disableApnType(String type) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public boolean enableDataConnectivity() throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public boolean disableDataConnectivity() throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public boolean isDataConnectivityPossible() throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int getCallState(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int getDataActivity() throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int getDataState() throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int getActivePhoneType(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int sendOemRilRequestRaw(byte[] request, byte[] response) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int getCdmaEriIconIndex(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int getCdmaEriIconMode(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public String getCdmaEriText(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public boolean needsOtaServiceProvisioning() throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int getVoiceMessageCount(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int getNetworkType(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int getDataNetworkType(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int getVoiceNetworkType(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public boolean hasIccCard(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int getLteOnCdmaMode(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int getIccPin1RetryCount(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public List<CellInfo> getAllCellInfo() throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public void setDataReadinessChecks(boolean checkConnectivity, boolean checkSubscription, boolean tryDataCalls) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int getDefaultSubscription() throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int getPreferredVoiceSubscription() throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int getPreferredDataSubscription() throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public boolean setPreferredDataSubscription(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int getMmsAutoSetDataSubscription() throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public boolean setMmsAutoSetDataSubscription(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public boolean isSimPukLocked(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public void setModemPower(boolean on) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public void invokeSimlessHW() throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public int getmActiveSubscription() throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }

            public Bundle getCellLocation(int subscription) throws RemoteException {
                throw new NoExtAPIException("method not supported.");
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITelephonyMSim asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITelephonyMSim)) {
                return new Proxy(obj);
            }
            return (ITelephonyMSim) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            int _result2;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    dial(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    call(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result = showCallScreen();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result = showCallScreenWithDialpad(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _result = endCall(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    answerRingingCall(data.readInt());
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    silenceRinger();
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isOffhook(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isRinging(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isIdle(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isRadioOn(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isSimPinEnabled(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    cancelMissedCallsNotification(data.readInt());
                    reply.writeNoException();
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    _result = supplyPin(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    _result = supplyPuk(data.readString(), data.readString(), 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    _result = handlePinMmi(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    toggleRadioOnOff(data.readInt());
                    reply.writeNoException();
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setRadio(data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    updateServiceLocation(data.readInt());
                    reply.writeNoException();
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = enableApnType(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = disableApnType(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    _result = enableDataConnectivity();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    _result = disableDataConnectivity();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case TRANSACTION_isDataConnectivityPossible /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isDataConnectivityPossible();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 25:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getCallState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getDataActivity /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDataActivity();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 27:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDataState();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 28:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getActivePhoneType(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 29:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getCdmaEriIconIndex(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 30:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getCdmaEriIconMode(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 31:
                    data.enforceInterface(DESCRIPTOR);
                    String _result3 = getCdmaEriText(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case 32:
                    data.enforceInterface(DESCRIPTOR);
                    _result = needsOtaServiceProvisioning();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 33:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getVoiceMessageCount(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getNetworkType /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getNetworkType(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_hasIccCard /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = hasIccCard(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 36:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getLteOnCdmaMode(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getAllCellInfo /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<CellInfo> _result4 = getAllCellInfo();
                    reply.writeNoException();
                    reply.writeTypedList(_result4);
                    return true;
                case TRANSACTION_getDefaultSubscription /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDefaultSubscription();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getPreferredVoiceSubscription /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPreferredVoiceSubscription();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 40:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPreferredDataSubscription();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 41:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setPreferredDataSubscription(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 42:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDataNetworkType(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void answerRingingCall(int i) throws RemoteException;

    void call(String str, int i) throws RemoteException;

    void cancelMissedCallsNotification(int i) throws RemoteException;

    void dial(String str, int i) throws RemoteException;

    int disableApnType(String str) throws RemoteException;

    boolean disableDataConnectivity() throws RemoteException;

    int enableApnType(String str) throws RemoteException;

    boolean enableDataConnectivity() throws RemoteException;

    boolean endCall(int i) throws RemoteException;

    int getActivePhoneType(int i) throws RemoteException;

    List<CellInfo> getAllCellInfo() throws RemoteException;

    int getCallState(int i) throws RemoteException;

    int getCdmaEriIconIndex(int i) throws RemoteException;

    int getCdmaEriIconMode(int i) throws RemoteException;

    String getCdmaEriText(int i) throws RemoteException;

    Bundle getCellLocation(int i) throws RemoteException;

    int getDataActivity() throws RemoteException;

    int getDataNetworkType(int i) throws RemoteException;

    int getDataState() throws RemoteException;

    int getDefaultSubscription() throws RemoteException;

    int getIccPin1RetryCount(int i) throws RemoteException;

    int getLteOnCdmaMode(int i) throws RemoteException;

    int getMmsAutoSetDataSubscription() throws RemoteException;

    int getNetworkType(int i) throws RemoteException;

    int getPreferredDataSubscription() throws RemoteException;

    int getPreferredVoiceSubscription() throws RemoteException;

    int getVoiceMessageCount(int i) throws RemoteException;

    int getVoiceNetworkType(int i) throws RemoteException;

    int getmActiveSubscription() throws RemoteException;

    boolean handlePinMmi(String str, int i) throws RemoteException;

    boolean hasIccCard(int i) throws RemoteException;

    void invokeSimlessHW() throws RemoteException;

    boolean isDataConnectivityPossible() throws RemoteException;

    boolean isIdle(int i) throws RemoteException;

    boolean isOffhook(int i) throws RemoteException;

    boolean isRadioOn(int i) throws RemoteException;

    boolean isRinging(int i) throws RemoteException;

    boolean isSimPinEnabled(int i) throws RemoteException;

    boolean isSimPukLocked(int i) throws RemoteException;

    boolean needsOtaServiceProvisioning() throws RemoteException;

    int sendOemRilRequestRaw(byte[] bArr, byte[] bArr2) throws RemoteException;

    void setDataReadinessChecks(boolean z, boolean z2, boolean z3) throws RemoteException;

    boolean setMmsAutoSetDataSubscription(int i) throws RemoteException;

    void setModemPower(boolean z) throws RemoteException;

    boolean setPreferredDataSubscription(int i) throws RemoteException;

    boolean setRadio(boolean z, int i) throws RemoteException;

    boolean showCallScreen() throws RemoteException;

    boolean showCallScreenWithDialpad(boolean z) throws RemoteException;

    void silenceRinger() throws RemoteException;

    boolean supplyPin(String str, int i) throws RemoteException;

    int[] supplyPinReportResult(String str, int i) throws RemoteException;

    boolean supplyPuk(String str, String str2, int i) throws RemoteException;

    int[] supplyPukReportResult(String str, String str2, int i) throws RemoteException;

    void toggleRadioOnOff(int i) throws RemoteException;

    void updateServiceLocation(int i) throws RemoteException;
}
