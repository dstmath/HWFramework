package com.android.internal.telephony;

import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telephony.CellInfo;
import android.telephony.IccOpenLogicalChannelResponse;
import android.telephony.NeighboringCellInfo;
import android.telephony.RadioAccessFamily;
import android.telephony.ServiceState;
import java.util.List;

public interface ITelephony extends IInterface {

    public static abstract class Stub extends Binder implements ITelephony {
        private static final String DESCRIPTOR = "com.android.internal.telephony.ITelephony";
        static final int TRANSACTION_answerRingingCall = 5;
        static final int TRANSACTION_answerRingingCallForSubscriber = 6;
        static final int TRANSACTION_call = 2;
        static final int TRANSACTION_canChangeDtmfToneLength = 110;
        static final int TRANSACTION_checkCarrierPrivilegesForPackage = 94;
        static final int TRANSACTION_checkCarrierPrivilegesForPackageAnyPhone = 95;
        static final int TRANSACTION_dial = 1;
        static final int TRANSACTION_disableDataConnectivity = 38;
        static final int TRANSACTION_disableLocationUpdates = 35;
        static final int TRANSACTION_disableLocationUpdatesForSubscriber = 36;
        static final int TRANSACTION_enableDataConnectivity = 37;
        static final int TRANSACTION_enableLocationUpdates = 33;
        static final int TRANSACTION_enableLocationUpdatesForSubscriber = 34;
        static final int TRANSACTION_enableVideoCalling = 108;
        static final int TRANSACTION_endCall = 3;
        static final int TRANSACTION_endCallForSubscriber = 4;
        static final int TRANSACTION_factoryReset = 122;
        static final int TRANSACTION_getActivePhoneType = 46;
        static final int TRANSACTION_getActivePhoneTypeForSlot = 47;
        static final int TRANSACTION_getAllCellInfo = 67;
        static final int TRANSACTION_getCalculatedPreferredNetworkType = 80;
        static final int TRANSACTION_getCallState = 42;
        static final int TRANSACTION_getCallStateForSlot = 43;
        static final int TRANSACTION_getCarrierPackageNamesForIntentAndPhone = 96;
        static final int TRANSACTION_getCarrierPrivilegeStatus = 93;
        static final int TRANSACTION_getCdmaEriIconIndex = 48;
        static final int TRANSACTION_getCdmaEriIconIndexForSubscriber = 49;
        static final int TRANSACTION_getCdmaEriIconMode = 50;
        static final int TRANSACTION_getCdmaEriIconModeForSubscriber = 51;
        static final int TRANSACTION_getCdmaEriText = 52;
        static final int TRANSACTION_getCdmaEriTextForSubscriber = 53;
        static final int TRANSACTION_getCdmaMdn = 91;
        static final int TRANSACTION_getCdmaMin = 92;
        static final int TRANSACTION_getCellLocation = 40;
        static final int TRANSACTION_getCellNetworkScanResults = 84;
        static final int TRANSACTION_getDataActivity = 44;
        static final int TRANSACTION_getDataEnabled = 88;
        static final int TRANSACTION_getDataNetworkType = 60;
        static final int TRANSACTION_getDataNetworkTypeForSubscriber = 61;
        static final int TRANSACTION_getDataState = 45;
        static final int TRANSACTION_getDefaultSim = 69;
        static final int TRANSACTION_getDeviceId = 118;
        static final int TRANSACTION_getDeviceSoftwareVersionForSlot = 120;
        static final int TRANSACTION_getImeiForSlot = 119;
        static final int TRANSACTION_getLine1AlphaTagForDisplay = 99;
        static final int TRANSACTION_getLine1NumberForDisplay = 98;
        static final int TRANSACTION_getLocaleFromDefaultSim = 123;
        static final int TRANSACTION_getLteOnCdmaMode = 65;
        static final int TRANSACTION_getLteOnCdmaModeForSubscriber = 66;
        static final int TRANSACTION_getMergedSubscriberIds = 100;
        static final int TRANSACTION_getNeighboringCellInfo = 41;
        static final int TRANSACTION_getNetworkType = 58;
        static final int TRANSACTION_getNetworkTypeForSubscriber = 59;
        static final int TRANSACTION_getPackagesWithCarrierPrivileges = 128;
        static final int TRANSACTION_getPcscfAddress = 89;
        static final int TRANSACTION_getPreferredNetworkType = 81;
        static final int TRANSACTION_getRadioAccessFamily = 107;
        static final int TRANSACTION_getServiceStateForSubscriber = 125;
        static final int TRANSACTION_getSubIdForPhoneAccount = 121;
        static final int TRANSACTION_getTetherApnRequired = 82;
        static final int TRANSACTION_getVoiceMessageCount = 56;
        static final int TRANSACTION_getVoiceMessageCountForSubscriber = 57;
        static final int TRANSACTION_getVoiceNetworkTypeForSubscriber = 62;
        static final int TRANSACTION_getVoicemailRingtoneUri = 126;
        static final int TRANSACTION_handlePinMmi = 24;
        static final int TRANSACTION_handlePinMmiForSubscriber = 25;
        static final int TRANSACTION_hasIccCard = 63;
        static final int TRANSACTION_hasIccCardUsingSlotId = 64;
        static final int TRANSACTION_iccCloseLogicalChannel = 71;
        static final int TRANSACTION_iccExchangeSimIO = 74;
        static final int TRANSACTION_iccOpenLogicalChannel = 70;
        static final int TRANSACTION_iccTransmitApduBasicChannel = 73;
        static final int TRANSACTION_iccTransmitApduLogicalChannel = 72;
        static final int TRANSACTION_invokeOemRilRequestRaw = 103;
        static final int TRANSACTION_isDataConnectivityPossible = 39;
        static final int TRANSACTION_isHearingAidCompatibilitySupported = 113;
        static final int TRANSACTION_isIdle = 12;
        static final int TRANSACTION_isIdleForSubscriber = 13;
        static final int TRANSACTION_isImsRegistered = 114;
        static final int TRANSACTION_isOffhook = 8;
        static final int TRANSACTION_isOffhookForSubscriber = 9;
        static final int TRANSACTION_isRadioOn = 14;
        static final int TRANSACTION_isRadioOnForSubscriber = 15;
        static final int TRANSACTION_isRinging = 11;
        static final int TRANSACTION_isRingingForSubscriber = 10;
        static final int TRANSACTION_isTtyModeSupported = 112;
        static final int TRANSACTION_isVideoCallingEnabled = 109;
        static final int TRANSACTION_isVideoTelephonyAvailable = 117;
        static final int TRANSACTION_isVoicemailVibrationEnabled = 127;
        static final int TRANSACTION_isVolteAvailable = 116;
        static final int TRANSACTION_isWifiCallingAvailable = 115;
        static final int TRANSACTION_isWorldPhone = 111;
        static final int TRANSACTION_needMobileRadioShutdown = 104;
        static final int TRANSACTION_needsOtaServiceProvisioning = 54;
        static final int TRANSACTION_nvReadItem = 76;
        static final int TRANSACTION_nvResetConfig = 79;
        static final int TRANSACTION_nvWriteCdmaPrl = 78;
        static final int TRANSACTION_nvWriteItem = 77;
        static final int TRANSACTION_requestModemActivityInfo = 124;
        static final int TRANSACTION_sendEnvelopeWithStatus = 75;
        static final int TRANSACTION_setCellInfoListRate = 68;
        static final int TRANSACTION_setDataEnabled = 87;
        static final int TRANSACTION_setDataEnabledProperties = 129;
        static final int TRANSACTION_setImsRegistrationState = 90;
        static final int TRANSACTION_setLine1NumberForDisplayForSubscriber = 97;
        static final int TRANSACTION_setNetworkSelectionModeAutomatic = 83;
        static final int TRANSACTION_setNetworkSelectionModeManual = 85;
        static final int TRANSACTION_setOperatorBrandOverride = 101;
        static final int TRANSACTION_setPreferredNetworkType = 86;
        static final int TRANSACTION_setRadio = 28;
        static final int TRANSACTION_setRadioCapability = 106;
        static final int TRANSACTION_setRadioForSubscriber = 29;
        static final int TRANSACTION_setRadioPower = 30;
        static final int TRANSACTION_setRoamingOverride = 102;
        static final int TRANSACTION_setVoiceMailNumber = 55;
        static final int TRANSACTION_shutdownMobileRadios = 105;
        static final int TRANSACTION_silenceRinger = 7;
        static final int TRANSACTION_supplyPin = 16;
        static final int TRANSACTION_supplyPinForSubscriber = 17;
        static final int TRANSACTION_supplyPinReportResult = 20;
        static final int TRANSACTION_supplyPinReportResultForSubscriber = 21;
        static final int TRANSACTION_supplyPuk = 18;
        static final int TRANSACTION_supplyPukForSubscriber = 19;
        static final int TRANSACTION_supplyPukReportResult = 22;
        static final int TRANSACTION_supplyPukReportResultForSubscriber = 23;
        static final int TRANSACTION_toggleRadioOnOff = 26;
        static final int TRANSACTION_toggleRadioOnOffForSubscriber = 27;
        static final int TRANSACTION_updateServiceLocation = 31;
        static final int TRANSACTION_updateServiceLocationForSubscriber = 32;

        private static class Proxy implements ITelephony {
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

            public void dial(String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(number);
                    this.mRemote.transact(Stub.TRANSACTION_dial, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void call(String callingPackage, String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeString(number);
                    this.mRemote.transact(Stub.TRANSACTION_call, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean endCall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_endCall, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean endCallForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_endCallForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void answerRingingCall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_answerRingingCall, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void answerRingingCallForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_answerRingingCallForSubscriber, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void silenceRinger() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_silenceRinger, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isOffhook(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_isOffhook, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isOffhookForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_isOffhookForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isRingingForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_isRingingForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isRinging(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_isRinging, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isIdle(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_isIdle, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isIdleForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_isIdleForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isRadioOn(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_isRadioOn, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isRadioOnForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_isRadioOnForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean supplyPin(String pin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pin);
                    this.mRemote.transact(Stub.TRANSACTION_supplyPin, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean supplyPinForSubscriber(int subId, String pin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(pin);
                    this.mRemote.transact(Stub.TRANSACTION_supplyPinForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean supplyPuk(String puk, String pin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(puk);
                    _data.writeString(pin);
                    this.mRemote.transact(Stub.TRANSACTION_supplyPuk, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean supplyPukForSubscriber(int subId, String puk, String pin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(puk);
                    _data.writeString(pin);
                    this.mRemote.transact(Stub.TRANSACTION_supplyPukForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] supplyPinReportResult(String pin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pin);
                    this.mRemote.transact(Stub.TRANSACTION_supplyPinReportResult, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] supplyPinReportResultForSubscriber(int subId, String pin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(pin);
                    this.mRemote.transact(Stub.TRANSACTION_supplyPinReportResultForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] supplyPukReportResult(String puk, String pin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(puk);
                    _data.writeString(pin);
                    this.mRemote.transact(Stub.TRANSACTION_supplyPukReportResult, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] supplyPukReportResultForSubscriber(int subId, String puk, String pin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(puk);
                    _data.writeString(pin);
                    this.mRemote.transact(Stub.TRANSACTION_supplyPukReportResultForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean handlePinMmi(String dialString) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(dialString);
                    this.mRemote.transact(Stub.TRANSACTION_handlePinMmi, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean handlePinMmiForSubscriber(int subId, String dialString) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(dialString);
                    this.mRemote.transact(Stub.TRANSACTION_handlePinMmiForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void toggleRadioOnOff() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_toggleRadioOnOff, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void toggleRadioOnOffForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_toggleRadioOnOffForSubscriber, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setRadio(boolean turnOn) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (turnOn) {
                        i = Stub.TRANSACTION_dial;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setRadio, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setRadioForSubscriber(int subId, boolean turnOn) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (turnOn) {
                        i = Stub.TRANSACTION_dial;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setRadioForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setRadioPower(boolean turnOn) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (turnOn) {
                        i = Stub.TRANSACTION_dial;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setRadioPower, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateServiceLocation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_updateServiceLocation, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateServiceLocationForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_updateServiceLocationForSubscriber, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableLocationUpdates() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_enableLocationUpdates, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableLocationUpdatesForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_enableLocationUpdatesForSubscriber, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disableLocationUpdates() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_disableLocationUpdates, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disableLocationUpdatesForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_disableLocationUpdatesForSubscriber, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean enableDataConnectivity() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_enableDataConnectivity, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean disableDataConnectivity() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_disableDataConnectivity, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isDataConnectivityPossible() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isDataConnectivityPossible, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getCellLocation(String callingPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle bundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    this.mRemote.transact(Stub.TRANSACTION_getCellLocation, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        bundle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bundle;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<NeighboringCellInfo> getNeighboringCellInfo(String callingPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    this.mRemote.transact(Stub.TRANSACTION_getNeighboringCellInfo, _data, _reply, 0);
                    _reply.readException();
                    List<NeighboringCellInfo> _result = _reply.createTypedArrayList(NeighboringCellInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCallState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCallState, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCallStateForSlot(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    this.mRemote.transact(Stub.TRANSACTION_getCallStateForSlot, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDataActivity() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getDataActivity, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDataState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getDataState, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getActivePhoneType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getActivePhoneType, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getActivePhoneTypeForSlot(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    this.mRemote.transact(Stub.TRANSACTION_getActivePhoneTypeForSlot, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCdmaEriIconIndex(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getCdmaEriIconIndex, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCdmaEriIconIndexForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getCdmaEriIconIndexForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCdmaEriIconMode(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getCdmaEriIconMode, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCdmaEriIconModeForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getCdmaEriIconModeForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getCdmaEriText(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getCdmaEriText, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getCdmaEriTextForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getCdmaEriTextForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean needsOtaServiceProvisioning() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_needsOtaServiceProvisioning, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setVoiceMailNumber(int subId, String alphaTag, String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(alphaTag);
                    _data.writeString(number);
                    this.mRemote.transact(Stub.TRANSACTION_setVoiceMailNumber, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVoiceMessageCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getVoiceMessageCount, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVoiceMessageCountForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getVoiceMessageCountForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getNetworkType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getNetworkType, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getNetworkTypeForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getNetworkTypeForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDataNetworkType(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getDataNetworkType, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDataNetworkTypeForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getDataNetworkTypeForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVoiceNetworkTypeForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getVoiceNetworkTypeForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasIccCard() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_hasIccCard, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasIccCardUsingSlotId(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    this.mRemote.transact(Stub.TRANSACTION_hasIccCardUsingSlotId, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getLteOnCdmaMode(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getLteOnCdmaMode, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getLteOnCdmaModeForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getLteOnCdmaModeForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<CellInfo> getAllCellInfo(String callingPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    this.mRemote.transact(Stub.TRANSACTION_getAllCellInfo, _data, _reply, 0);
                    _reply.readException();
                    List<CellInfo> _result = _reply.createTypedArrayList(CellInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setCellInfoListRate(int rateInMillis) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rateInMillis);
                    this.mRemote.transact(Stub.TRANSACTION_setCellInfoListRate, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDefaultSim() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getDefaultSim, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IccOpenLogicalChannelResponse iccOpenLogicalChannel(int subId, String AID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    IccOpenLogicalChannelResponse iccOpenLogicalChannelResponse;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(AID);
                    this.mRemote.transact(Stub.TRANSACTION_iccOpenLogicalChannel, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        iccOpenLogicalChannelResponse = (IccOpenLogicalChannelResponse) IccOpenLogicalChannelResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        iccOpenLogicalChannelResponse = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return iccOpenLogicalChannelResponse;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean iccCloseLogicalChannel(int subId, int channel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(channel);
                    this.mRemote.transact(Stub.TRANSACTION_iccCloseLogicalChannel, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String iccTransmitApduLogicalChannel(int subId, int channel, int cla, int instruction, int p1, int p2, int p3, String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(channel);
                    _data.writeInt(cla);
                    _data.writeInt(instruction);
                    _data.writeInt(p1);
                    _data.writeInt(p2);
                    _data.writeInt(p3);
                    _data.writeString(data);
                    this.mRemote.transact(Stub.TRANSACTION_iccTransmitApduLogicalChannel, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String iccTransmitApduBasicChannel(int subId, int cla, int instruction, int p1, int p2, int p3, String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(cla);
                    _data.writeInt(instruction);
                    _data.writeInt(p1);
                    _data.writeInt(p2);
                    _data.writeInt(p3);
                    _data.writeString(data);
                    this.mRemote.transact(Stub.TRANSACTION_iccTransmitApduBasicChannel, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] iccExchangeSimIO(int subId, int fileID, int command, int p1, int p2, int p3, String filePath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(fileID);
                    _data.writeInt(command);
                    _data.writeInt(p1);
                    _data.writeInt(p2);
                    _data.writeInt(p3);
                    _data.writeString(filePath);
                    this.mRemote.transact(Stub.TRANSACTION_iccExchangeSimIO, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String sendEnvelopeWithStatus(int subId, String content) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(content);
                    this.mRemote.transact(Stub.TRANSACTION_sendEnvelopeWithStatus, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String nvReadItem(int itemID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(itemID);
                    this.mRemote.transact(Stub.TRANSACTION_nvReadItem, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean nvWriteItem(int itemID, String itemValue) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(itemID);
                    _data.writeString(itemValue);
                    this.mRemote.transact(Stub.TRANSACTION_nvWriteItem, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean nvWriteCdmaPrl(byte[] preferredRoamingList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(preferredRoamingList);
                    this.mRemote.transact(Stub.TRANSACTION_nvWriteCdmaPrl, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean nvResetConfig(int resetType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resetType);
                    this.mRemote.transact(Stub.TRANSACTION_nvResetConfig, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCalculatedPreferredNetworkType(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getCalculatedPreferredNetworkType, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPreferredNetworkType(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getPreferredNetworkType, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getTetherApnRequired() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getTetherApnRequired, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNetworkSelectionModeAutomatic(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_setNetworkSelectionModeAutomatic, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public CellNetworkScanResult getCellNetworkScanResults(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    CellNetworkScanResult cellNetworkScanResult;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getCellNetworkScanResults, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        cellNetworkScanResult = (CellNetworkScanResult) CellNetworkScanResult.CREATOR.createFromParcel(_reply);
                    } else {
                        cellNetworkScanResult = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return cellNetworkScanResult;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setNetworkSelectionModeManual(int subId, OperatorInfo operator, boolean persistSelection) throws RemoteException {
                int i = Stub.TRANSACTION_dial;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (operator != null) {
                        _data.writeInt(Stub.TRANSACTION_dial);
                        operator.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!persistSelection) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setNetworkSelectionModeManual, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setPreferredNetworkType(int subId, int networkType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(networkType);
                    this.mRemote.transact(Stub.TRANSACTION_setPreferredNetworkType, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDataEnabled(int subId, boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (enable) {
                        i = Stub.TRANSACTION_dial;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setDataEnabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getDataEnabled(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getDataEnabled, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getPcscfAddress(String apnType, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(apnType);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getPcscfAddress, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setImsRegistrationState(boolean registered) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (registered) {
                        i = Stub.TRANSACTION_dial;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setImsRegistrationState, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getCdmaMdn(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getCdmaMdn, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getCdmaMin(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getCdmaMin, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCarrierPrivilegeStatus(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getCarrierPrivilegeStatus, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkCarrierPrivilegesForPackage(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(Stub.TRANSACTION_checkCarrierPrivilegesForPackage, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkCarrierPrivilegesForPackageAnyPhone(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(Stub.TRANSACTION_checkCarrierPrivilegesForPackageAnyPhone, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getCarrierPackageNamesForIntentAndPhone(Intent intent, int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_dial);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(phoneId);
                    this.mRemote.transact(Stub.TRANSACTION_getCarrierPackageNamesForIntentAndPhone, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setLine1NumberForDisplayForSubscriber(int subId, String alphaTag, String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(alphaTag);
                    _data.writeString(number);
                    this.mRemote.transact(Stub.TRANSACTION_setLine1NumberForDisplayForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getLine1NumberForDisplay(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getLine1NumberForDisplay, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getLine1AlphaTagForDisplay(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getLine1AlphaTagForDisplay, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getMergedSubscriberIds(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getMergedSubscriberIds, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setOperatorBrandOverride(int subId, String brand) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(brand);
                    this.mRemote.transact(Stub.TRANSACTION_setOperatorBrandOverride, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setRoamingOverride(int subId, List<String> gsmRoamingList, List<String> gsmNonRoamingList, List<String> cdmaRoamingList, List<String> cdmaNonRoamingList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeStringList(gsmRoamingList);
                    _data.writeStringList(gsmNonRoamingList);
                    _data.writeStringList(cdmaRoamingList);
                    _data.writeStringList(cdmaNonRoamingList);
                    this.mRemote.transact(Stub.TRANSACTION_setRoamingOverride, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int invokeOemRilRequestRaw(byte[] oemReq, byte[] oemResp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(oemReq);
                    if (oemResp == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(oemResp.length);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_invokeOemRilRequestRaw, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(oemResp);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean needMobileRadioShutdown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_needMobileRadioShutdown, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void shutdownMobileRadios() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_shutdownMobileRadios, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRadioCapability(RadioAccessFamily[] rafs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(rafs, 0);
                    this.mRemote.transact(Stub.TRANSACTION_setRadioCapability, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRadioAccessFamily(int phoneId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getRadioAccessFamily, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableVideoCalling(boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = Stub.TRANSACTION_dial;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_enableVideoCalling, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isVideoCallingEnabled(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_isVideoCallingEnabled, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean canChangeDtmfToneLength() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_canChangeDtmfToneLength, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isWorldPhone() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isWorldPhone, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isTtyModeSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isTtyModeSupported, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isHearingAidCompatibilitySupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isHearingAidCompatibilitySupported, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isImsRegistered() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isImsRegistered, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isWifiCallingAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isWifiCallingAvailable, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isVolteAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isVolteAvailable, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isVideoTelephonyAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isVideoTelephonyAvailable, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getDeviceId(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getDeviceId, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getImeiForSlot(int slotId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getImeiForSlot, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getDeviceSoftwareVersionForSlot(int slotId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getDeviceSoftwareVersionForSlot, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSubIdForPhoneAccount(PhoneAccount phoneAccount) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (phoneAccount != null) {
                        _data.writeInt(Stub.TRANSACTION_dial);
                        phoneAccount.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getSubIdForPhoneAccount, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void factoryReset(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_factoryReset, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getLocaleFromDefaultSim() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getLocaleFromDefaultSim, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestModemActivityInfo(ResultReceiver result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result != null) {
                        _data.writeInt(Stub.TRANSACTION_dial);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_requestModemActivityInfo, _data, null, Stub.TRANSACTION_dial);
                } finally {
                    _data.recycle();
                }
            }

            public ServiceState getServiceStateForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ServiceState serviceState;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getServiceStateForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        serviceState = (ServiceState) ServiceState.CREATOR.createFromParcel(_reply);
                    } else {
                        serviceState = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return serviceState;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Uri getVoicemailRingtoneUri(PhoneAccountHandle accountHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Uri uri;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (accountHandle != null) {
                        _data.writeInt(Stub.TRANSACTION_dial);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getVoicemailRingtoneUri, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(_reply);
                    } else {
                        uri = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return uri;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isVoicemailVibrationEnabled(PhoneAccountHandle accountHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (accountHandle != null) {
                        _data.writeInt(Stub.TRANSACTION_dial);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_isVoicemailVibrationEnabled, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getPackagesWithCarrierPrivileges() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPackagesWithCarrierPrivileges, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDataEnabledProperties(String appName, boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appName);
                    if (enable) {
                        i = Stub.TRANSACTION_dial;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setDataEnabledProperties, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITelephony asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITelephony)) {
                return new Proxy(obj);
            }
            return (ITelephony) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            int[] _result2;
            int _result3;
            String _result4;
            String[] _result5;
            List<String> _result6;
            PhoneAccountHandle phoneAccountHandle;
            switch (code) {
                case TRANSACTION_dial /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    dial(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_call /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    call(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_endCall /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = endCall();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_endCallForSubscriber /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = endCallForSubscriber(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_answerRingingCall /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    answerRingingCall();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_answerRingingCallForSubscriber /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    answerRingingCallForSubscriber(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_silenceRinger /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    silenceRinger();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isOffhook /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isOffhook(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_isOffhookForSubscriber /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isOffhookForSubscriber(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_isRingingForSubscriber /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isRingingForSubscriber(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_isRinging /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isRinging(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_isIdle /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isIdle(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_isIdleForSubscriber /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isIdleForSubscriber(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_isRadioOn /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isRadioOn(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_isRadioOnForSubscriber /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isRadioOnForSubscriber(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_supplyPin /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = supplyPin(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_supplyPinForSubscriber /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = supplyPinForSubscriber(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_supplyPuk /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = supplyPuk(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_supplyPukForSubscriber /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = supplyPukForSubscriber(data.readInt(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_supplyPinReportResult /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = supplyPinReportResult(data.readString());
                    reply.writeNoException();
                    reply.writeIntArray(_result2);
                    return true;
                case TRANSACTION_supplyPinReportResultForSubscriber /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = supplyPinReportResultForSubscriber(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeIntArray(_result2);
                    return true;
                case TRANSACTION_supplyPukReportResult /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = supplyPukReportResult(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeIntArray(_result2);
                    return true;
                case TRANSACTION_supplyPukReportResultForSubscriber /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = supplyPukReportResultForSubscriber(data.readInt(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeIntArray(_result2);
                    return true;
                case TRANSACTION_handlePinMmi /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = handlePinMmi(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_handlePinMmiForSubscriber /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = handlePinMmiForSubscriber(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_toggleRadioOnOff /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    toggleRadioOnOff();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_toggleRadioOnOffForSubscriber /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    toggleRadioOnOffForSubscriber(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setRadio /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setRadio(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_setRadioForSubscriber /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setRadioForSubscriber(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_setRadioPower /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setRadioPower(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_updateServiceLocation /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    updateServiceLocation();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_updateServiceLocationForSubscriber /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    updateServiceLocationForSubscriber(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_enableLocationUpdates /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    enableLocationUpdates();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_enableLocationUpdatesForSubscriber /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    enableLocationUpdatesForSubscriber(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_disableLocationUpdates /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    disableLocationUpdates();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_disableLocationUpdatesForSubscriber /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    disableLocationUpdatesForSubscriber(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_enableDataConnectivity /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = enableDataConnectivity();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_disableDataConnectivity /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = disableDataConnectivity();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_isDataConnectivityPossible /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isDataConnectivityPossible();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_getCellLocation /*40*/:
                    data.enforceInterface(DESCRIPTOR);
                    Bundle _result7 = getCellLocation(data.readString());
                    reply.writeNoException();
                    if (_result7 != null) {
                        reply.writeInt(TRANSACTION_dial);
                        _result7.writeToParcel(reply, TRANSACTION_dial);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getNeighboringCellInfo /*41*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<NeighboringCellInfo> _result8 = getNeighboringCellInfo(data.readString());
                    reply.writeNoException();
                    reply.writeTypedList(_result8);
                    return true;
                case TRANSACTION_getCallState /*42*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getCallState();
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getCallStateForSlot /*43*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getCallStateForSlot(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getDataActivity /*44*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getDataActivity();
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getDataState /*45*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getDataState();
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getActivePhoneType /*46*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getActivePhoneType();
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getActivePhoneTypeForSlot /*47*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getActivePhoneTypeForSlot(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getCdmaEriIconIndex /*48*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getCdmaEriIconIndex(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getCdmaEriIconIndexForSubscriber /*49*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getCdmaEriIconIndexForSubscriber(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getCdmaEriIconMode /*50*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getCdmaEriIconMode(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getCdmaEriIconModeForSubscriber /*51*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getCdmaEriIconModeForSubscriber(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getCdmaEriText /*52*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getCdmaEriText(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case TRANSACTION_getCdmaEriTextForSubscriber /*53*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getCdmaEriTextForSubscriber(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case TRANSACTION_needsOtaServiceProvisioning /*54*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = needsOtaServiceProvisioning();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_setVoiceMailNumber /*55*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setVoiceMailNumber(data.readInt(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_getVoiceMessageCount /*56*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getVoiceMessageCount();
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getVoiceMessageCountForSubscriber /*57*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getVoiceMessageCountForSubscriber(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getNetworkType /*58*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getNetworkType();
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getNetworkTypeForSubscriber /*59*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getNetworkTypeForSubscriber(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getDataNetworkType /*60*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getDataNetworkType(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getDataNetworkTypeForSubscriber /*61*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getDataNetworkTypeForSubscriber(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getVoiceNetworkTypeForSubscriber /*62*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getVoiceNetworkTypeForSubscriber(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_hasIccCard /*63*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = hasIccCard();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_hasIccCardUsingSlotId /*64*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = hasIccCardUsingSlotId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_getLteOnCdmaMode /*65*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getLteOnCdmaMode(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getLteOnCdmaModeForSubscriber /*66*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getLteOnCdmaModeForSubscriber(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getAllCellInfo /*67*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<CellInfo> _result9 = getAllCellInfo(data.readString());
                    reply.writeNoException();
                    reply.writeTypedList(_result9);
                    return true;
                case TRANSACTION_setCellInfoListRate /*68*/:
                    data.enforceInterface(DESCRIPTOR);
                    setCellInfoListRate(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getDefaultSim /*69*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getDefaultSim();
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_iccOpenLogicalChannel /*70*/:
                    data.enforceInterface(DESCRIPTOR);
                    IccOpenLogicalChannelResponse _result10 = iccOpenLogicalChannel(data.readInt(), data.readString());
                    reply.writeNoException();
                    if (_result10 != null) {
                        reply.writeInt(TRANSACTION_dial);
                        _result10.writeToParcel(reply, TRANSACTION_dial);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_iccCloseLogicalChannel /*71*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = iccCloseLogicalChannel(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_iccTransmitApduLogicalChannel /*72*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = iccTransmitApduLogicalChannel(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case TRANSACTION_iccTransmitApduBasicChannel /*73*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = iccTransmitApduBasicChannel(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case TRANSACTION_iccExchangeSimIO /*74*/:
                    data.enforceInterface(DESCRIPTOR);
                    byte[] _result11 = iccExchangeSimIO(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeByteArray(_result11);
                    return true;
                case TRANSACTION_sendEnvelopeWithStatus /*75*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = sendEnvelopeWithStatus(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case TRANSACTION_nvReadItem /*76*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = nvReadItem(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case TRANSACTION_nvWriteItem /*77*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = nvWriteItem(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_nvWriteCdmaPrl /*78*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = nvWriteCdmaPrl(data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_nvResetConfig /*79*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = nvResetConfig(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_getCalculatedPreferredNetworkType /*80*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getCalculatedPreferredNetworkType(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getPreferredNetworkType /*81*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getPreferredNetworkType(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getTetherApnRequired /*82*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getTetherApnRequired();
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_setNetworkSelectionModeAutomatic /*83*/:
                    data.enforceInterface(DESCRIPTOR);
                    setNetworkSelectionModeAutomatic(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getCellNetworkScanResults /*84*/:
                    data.enforceInterface(DESCRIPTOR);
                    CellNetworkScanResult _result12 = getCellNetworkScanResults(data.readInt());
                    reply.writeNoException();
                    if (_result12 != null) {
                        reply.writeInt(TRANSACTION_dial);
                        _result12.writeToParcel(reply, TRANSACTION_dial);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setNetworkSelectionModeManual /*85*/:
                    OperatorInfo operatorInfo;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        operatorInfo = (OperatorInfo) OperatorInfo.CREATOR.createFromParcel(data);
                    } else {
                        operatorInfo = null;
                    }
                    _result = setNetworkSelectionModeManual(_arg0, operatorInfo, data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_setPreferredNetworkType /*86*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setPreferredNetworkType(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_setDataEnabled /*87*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDataEnabled(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getDataEnabled /*88*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getDataEnabled(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_getPcscfAddress /*89*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getPcscfAddress(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeStringArray(_result5);
                    return true;
                case TRANSACTION_setImsRegistrationState /*90*/:
                    data.enforceInterface(DESCRIPTOR);
                    setImsRegistrationState(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getCdmaMdn /*91*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getCdmaMdn(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case TRANSACTION_getCdmaMin /*92*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getCdmaMin(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case TRANSACTION_getCarrierPrivilegeStatus /*93*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getCarrierPrivilegeStatus(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_checkCarrierPrivilegesForPackage /*94*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = checkCarrierPrivilegesForPackage(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_checkCarrierPrivilegesForPackageAnyPhone /*95*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = checkCarrierPrivilegesForPackageAnyPhone(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getCarrierPackageNamesForIntentAndPhone /*96*/:
                    Intent intent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    _result6 = getCarrierPackageNamesForIntentAndPhone(intent, data.readInt());
                    reply.writeNoException();
                    reply.writeStringList(_result6);
                    return true;
                case TRANSACTION_setLine1NumberForDisplayForSubscriber /*97*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setLine1NumberForDisplayForSubscriber(data.readInt(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_getLine1NumberForDisplay /*98*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getLine1NumberForDisplay(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case TRANSACTION_getLine1AlphaTagForDisplay /*99*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getLine1AlphaTagForDisplay(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case TRANSACTION_getMergedSubscriberIds /*100*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getMergedSubscriberIds(data.readString());
                    reply.writeNoException();
                    reply.writeStringArray(_result5);
                    return true;
                case TRANSACTION_setOperatorBrandOverride /*101*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setOperatorBrandOverride(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_setRoamingOverride /*102*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setRoamingOverride(data.readInt(), data.createStringArrayList(), data.createStringArrayList(), data.createStringArrayList(), data.createStringArrayList());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_invokeOemRilRequestRaw /*103*/:
                    byte[] bArr;
                    data.enforceInterface(DESCRIPTOR);
                    byte[] _arg02 = data.createByteArray();
                    int _arg1_length = data.readInt();
                    if (_arg1_length < 0) {
                        bArr = null;
                    } else {
                        bArr = new byte[_arg1_length];
                    }
                    _result3 = invokeOemRilRequestRaw(_arg02, bArr);
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    reply.writeByteArray(bArr);
                    return true;
                case TRANSACTION_needMobileRadioShutdown /*104*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = needMobileRadioShutdown();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_shutdownMobileRadios /*105*/:
                    data.enforceInterface(DESCRIPTOR);
                    shutdownMobileRadios();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setRadioCapability /*106*/:
                    data.enforceInterface(DESCRIPTOR);
                    setRadioCapability((RadioAccessFamily[]) data.createTypedArray(RadioAccessFamily.CREATOR));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getRadioAccessFamily /*107*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getRadioAccessFamily(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_enableVideoCalling /*108*/:
                    data.enforceInterface(DESCRIPTOR);
                    enableVideoCalling(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isVideoCallingEnabled /*109*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isVideoCallingEnabled(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_canChangeDtmfToneLength /*110*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = canChangeDtmfToneLength();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_isWorldPhone /*111*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isWorldPhone();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_isTtyModeSupported /*112*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isTtyModeSupported();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_isHearingAidCompatibilitySupported /*113*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isHearingAidCompatibilitySupported();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_isImsRegistered /*114*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isImsRegistered();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_isWifiCallingAvailable /*115*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isWifiCallingAvailable();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_isVolteAvailable /*116*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isVolteAvailable();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_isVideoTelephonyAvailable /*117*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isVideoTelephonyAvailable();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_getDeviceId /*118*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getDeviceId(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case TRANSACTION_getImeiForSlot /*119*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getImeiForSlot(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case TRANSACTION_getDeviceSoftwareVersionForSlot /*120*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getDeviceSoftwareVersionForSlot(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case TRANSACTION_getSubIdForPhoneAccount /*121*/:
                    PhoneAccount phoneAccount;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        phoneAccount = (PhoneAccount) PhoneAccount.CREATOR.createFromParcel(data);
                    } else {
                        phoneAccount = null;
                    }
                    _result3 = getSubIdForPhoneAccount(phoneAccount);
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_factoryReset /*122*/:
                    data.enforceInterface(DESCRIPTOR);
                    factoryReset(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getLocaleFromDefaultSim /*123*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getLocaleFromDefaultSim();
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case TRANSACTION_requestModemActivityInfo /*124*/:
                    ResultReceiver resultReceiver;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        resultReceiver = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(data);
                    } else {
                        resultReceiver = null;
                    }
                    requestModemActivityInfo(resultReceiver);
                    return true;
                case TRANSACTION_getServiceStateForSubscriber /*125*/:
                    data.enforceInterface(DESCRIPTOR);
                    ServiceState _result13 = getServiceStateForSubscriber(data.readInt(), data.readString());
                    reply.writeNoException();
                    if (_result13 != null) {
                        reply.writeInt(TRANSACTION_dial);
                        _result13.writeToParcel(reply, TRANSACTION_dial);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getVoicemailRingtoneUri /*126*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        phoneAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        phoneAccountHandle = null;
                    }
                    Uri _result14 = getVoicemailRingtoneUri(phoneAccountHandle);
                    reply.writeNoException();
                    if (_result14 != null) {
                        reply.writeInt(TRANSACTION_dial);
                        _result14.writeToParcel(reply, TRANSACTION_dial);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_isVoicemailVibrationEnabled /*127*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        phoneAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        phoneAccountHandle = null;
                    }
                    _result = isVoicemailVibrationEnabled(phoneAccountHandle);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_dial : 0);
                    return true;
                case TRANSACTION_getPackagesWithCarrierPrivileges /*128*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result6 = getPackagesWithCarrierPrivileges();
                    reply.writeNoException();
                    reply.writeStringList(_result6);
                    return true;
                case TRANSACTION_setDataEnabledProperties /*129*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDataEnabledProperties(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void answerRingingCall() throws RemoteException;

    void answerRingingCallForSubscriber(int i) throws RemoteException;

    void call(String str, String str2) throws RemoteException;

    boolean canChangeDtmfToneLength() throws RemoteException;

    int checkCarrierPrivilegesForPackage(String str) throws RemoteException;

    int checkCarrierPrivilegesForPackageAnyPhone(String str) throws RemoteException;

    void dial(String str) throws RemoteException;

    boolean disableDataConnectivity() throws RemoteException;

    void disableLocationUpdates() throws RemoteException;

    void disableLocationUpdatesForSubscriber(int i) throws RemoteException;

    boolean enableDataConnectivity() throws RemoteException;

    void enableLocationUpdates() throws RemoteException;

    void enableLocationUpdatesForSubscriber(int i) throws RemoteException;

    void enableVideoCalling(boolean z) throws RemoteException;

    boolean endCall() throws RemoteException;

    boolean endCallForSubscriber(int i) throws RemoteException;

    void factoryReset(int i) throws RemoteException;

    int getActivePhoneType() throws RemoteException;

    int getActivePhoneTypeForSlot(int i) throws RemoteException;

    List<CellInfo> getAllCellInfo(String str) throws RemoteException;

    int getCalculatedPreferredNetworkType(String str) throws RemoteException;

    int getCallState() throws RemoteException;

    int getCallStateForSlot(int i) throws RemoteException;

    List<String> getCarrierPackageNamesForIntentAndPhone(Intent intent, int i) throws RemoteException;

    int getCarrierPrivilegeStatus(int i) throws RemoteException;

    int getCdmaEriIconIndex(String str) throws RemoteException;

    int getCdmaEriIconIndexForSubscriber(int i, String str) throws RemoteException;

    int getCdmaEriIconMode(String str) throws RemoteException;

    int getCdmaEriIconModeForSubscriber(int i, String str) throws RemoteException;

    String getCdmaEriText(String str) throws RemoteException;

    String getCdmaEriTextForSubscriber(int i, String str) throws RemoteException;

    String getCdmaMdn(int i) throws RemoteException;

    String getCdmaMin(int i) throws RemoteException;

    Bundle getCellLocation(String str) throws RemoteException;

    CellNetworkScanResult getCellNetworkScanResults(int i) throws RemoteException;

    int getDataActivity() throws RemoteException;

    boolean getDataEnabled(int i) throws RemoteException;

    int getDataNetworkType(String str) throws RemoteException;

    int getDataNetworkTypeForSubscriber(int i, String str) throws RemoteException;

    int getDataState() throws RemoteException;

    int getDefaultSim() throws RemoteException;

    String getDeviceId(String str) throws RemoteException;

    String getDeviceSoftwareVersionForSlot(int i, String str) throws RemoteException;

    String getImeiForSlot(int i, String str) throws RemoteException;

    String getLine1AlphaTagForDisplay(int i, String str) throws RemoteException;

    String getLine1NumberForDisplay(int i, String str) throws RemoteException;

    String getLocaleFromDefaultSim() throws RemoteException;

    int getLteOnCdmaMode(String str) throws RemoteException;

    int getLteOnCdmaModeForSubscriber(int i, String str) throws RemoteException;

    String[] getMergedSubscriberIds(String str) throws RemoteException;

    List<NeighboringCellInfo> getNeighboringCellInfo(String str) throws RemoteException;

    int getNetworkType() throws RemoteException;

    int getNetworkTypeForSubscriber(int i, String str) throws RemoteException;

    List<String> getPackagesWithCarrierPrivileges() throws RemoteException;

    String[] getPcscfAddress(String str, String str2) throws RemoteException;

    int getPreferredNetworkType(int i) throws RemoteException;

    int getRadioAccessFamily(int i, String str) throws RemoteException;

    ServiceState getServiceStateForSubscriber(int i, String str) throws RemoteException;

    int getSubIdForPhoneAccount(PhoneAccount phoneAccount) throws RemoteException;

    int getTetherApnRequired() throws RemoteException;

    int getVoiceMessageCount() throws RemoteException;

    int getVoiceMessageCountForSubscriber(int i) throws RemoteException;

    int getVoiceNetworkTypeForSubscriber(int i, String str) throws RemoteException;

    Uri getVoicemailRingtoneUri(PhoneAccountHandle phoneAccountHandle) throws RemoteException;

    boolean handlePinMmi(String str) throws RemoteException;

    boolean handlePinMmiForSubscriber(int i, String str) throws RemoteException;

    boolean hasIccCard() throws RemoteException;

    boolean hasIccCardUsingSlotId(int i) throws RemoteException;

    boolean iccCloseLogicalChannel(int i, int i2) throws RemoteException;

    byte[] iccExchangeSimIO(int i, int i2, int i3, int i4, int i5, int i6, String str) throws RemoteException;

    IccOpenLogicalChannelResponse iccOpenLogicalChannel(int i, String str) throws RemoteException;

    String iccTransmitApduBasicChannel(int i, int i2, int i3, int i4, int i5, int i6, String str) throws RemoteException;

    String iccTransmitApduLogicalChannel(int i, int i2, int i3, int i4, int i5, int i6, int i7, String str) throws RemoteException;

    int invokeOemRilRequestRaw(byte[] bArr, byte[] bArr2) throws RemoteException;

    boolean isDataConnectivityPossible() throws RemoteException;

    boolean isHearingAidCompatibilitySupported() throws RemoteException;

    boolean isIdle(String str) throws RemoteException;

    boolean isIdleForSubscriber(int i, String str) throws RemoteException;

    boolean isImsRegistered() throws RemoteException;

    boolean isOffhook(String str) throws RemoteException;

    boolean isOffhookForSubscriber(int i, String str) throws RemoteException;

    boolean isRadioOn(String str) throws RemoteException;

    boolean isRadioOnForSubscriber(int i, String str) throws RemoteException;

    boolean isRinging(String str) throws RemoteException;

    boolean isRingingForSubscriber(int i, String str) throws RemoteException;

    boolean isTtyModeSupported() throws RemoteException;

    boolean isVideoCallingEnabled(String str) throws RemoteException;

    boolean isVideoTelephonyAvailable() throws RemoteException;

    boolean isVoicemailVibrationEnabled(PhoneAccountHandle phoneAccountHandle) throws RemoteException;

    boolean isVolteAvailable() throws RemoteException;

    boolean isWifiCallingAvailable() throws RemoteException;

    boolean isWorldPhone() throws RemoteException;

    boolean needMobileRadioShutdown() throws RemoteException;

    boolean needsOtaServiceProvisioning() throws RemoteException;

    String nvReadItem(int i) throws RemoteException;

    boolean nvResetConfig(int i) throws RemoteException;

    boolean nvWriteCdmaPrl(byte[] bArr) throws RemoteException;

    boolean nvWriteItem(int i, String str) throws RemoteException;

    void requestModemActivityInfo(ResultReceiver resultReceiver) throws RemoteException;

    String sendEnvelopeWithStatus(int i, String str) throws RemoteException;

    void setCellInfoListRate(int i) throws RemoteException;

    void setDataEnabled(int i, boolean z) throws RemoteException;

    void setDataEnabledProperties(String str, boolean z) throws RemoteException;

    void setImsRegistrationState(boolean z) throws RemoteException;

    boolean setLine1NumberForDisplayForSubscriber(int i, String str, String str2) throws RemoteException;

    void setNetworkSelectionModeAutomatic(int i) throws RemoteException;

    boolean setNetworkSelectionModeManual(int i, OperatorInfo operatorInfo, boolean z) throws RemoteException;

    boolean setOperatorBrandOverride(int i, String str) throws RemoteException;

    boolean setPreferredNetworkType(int i, int i2) throws RemoteException;

    boolean setRadio(boolean z) throws RemoteException;

    void setRadioCapability(RadioAccessFamily[] radioAccessFamilyArr) throws RemoteException;

    boolean setRadioForSubscriber(int i, boolean z) throws RemoteException;

    boolean setRadioPower(boolean z) throws RemoteException;

    boolean setRoamingOverride(int i, List<String> list, List<String> list2, List<String> list3, List<String> list4) throws RemoteException;

    boolean setVoiceMailNumber(int i, String str, String str2) throws RemoteException;

    void shutdownMobileRadios() throws RemoteException;

    void silenceRinger() throws RemoteException;

    boolean supplyPin(String str) throws RemoteException;

    boolean supplyPinForSubscriber(int i, String str) throws RemoteException;

    int[] supplyPinReportResult(String str) throws RemoteException;

    int[] supplyPinReportResultForSubscriber(int i, String str) throws RemoteException;

    boolean supplyPuk(String str, String str2) throws RemoteException;

    boolean supplyPukForSubscriber(int i, String str, String str2) throws RemoteException;

    int[] supplyPukReportResult(String str, String str2) throws RemoteException;

    int[] supplyPukReportResultForSubscriber(int i, String str, String str2) throws RemoteException;

    void toggleRadioOnOff() throws RemoteException;

    void toggleRadioOnOffForSubscriber(int i) throws RemoteException;

    void updateServiceLocation() throws RemoteException;

    void updateServiceLocationForSubscriber(int i) throws RemoteException;
}
