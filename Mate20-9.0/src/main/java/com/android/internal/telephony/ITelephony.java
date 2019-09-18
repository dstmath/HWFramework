package com.android.internal.telephony;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.NetworkStats;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.service.carrier.CarrierIdentifier;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telephony.CellInfo;
import android.telephony.ClientRequestStats;
import android.telephony.IccOpenLogicalChannelResponse;
import android.telephony.NeighboringCellInfo;
import android.telephony.NetworkScanRequest;
import android.telephony.RadioAccessFamily;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyHistogram;
import android.telephony.UiccSlotInfo;
import android.telephony.VisualVoicemailSmsFilterSettings;
import android.telephony.ims.aidl.IImsConfig;
import android.telephony.ims.aidl.IImsMmTelFeature;
import android.telephony.ims.aidl.IImsRcsFeature;
import android.telephony.ims.aidl.IImsRegistration;
import com.android.ims.internal.IImsServiceFeatureCallback;
import java.util.List;

public interface ITelephony extends IInterface {

    public static abstract class Stub extends Binder implements ITelephony {
        private static final String DESCRIPTOR = "com.android.internal.telephony.ITelephony";
        static final int TRANSACTION_answerRingingCall = 5;
        static final int TRANSACTION_answerRingingCallForSubscriber = 6;
        static final int TRANSACTION_call = 2;
        static final int TRANSACTION_canChangeDtmfToneLength = 139;
        static final int TRANSACTION_carrierActionReportDefaultNetworkStatus = 172;
        static final int TRANSACTION_carrierActionSetMeteredApnsEnabled = 170;
        static final int TRANSACTION_carrierActionSetRadioEnabled = 171;
        static final int TRANSACTION_checkCarrierPrivilegesForPackage = 123;
        static final int TRANSACTION_checkCarrierPrivilegesForPackageAnyPhone = 124;
        static final int TRANSACTION_dial = 1;
        static final int TRANSACTION_disableDataConnectivity = 39;
        static final int TRANSACTION_disableIms = 99;
        static final int TRANSACTION_disableLocationUpdates = 36;
        static final int TRANSACTION_disableLocationUpdatesForSubscriber = 37;
        static final int TRANSACTION_disableVisualVoicemailSmsFilter = 68;
        static final int TRANSACTION_enableDataConnectivity = 38;
        static final int TRANSACTION_enableIms = 98;
        static final int TRANSACTION_enableLocationUpdates = 34;
        static final int TRANSACTION_enableLocationUpdatesForSubscriber = 35;
        static final int TRANSACTION_enableVideoCalling = 137;
        static final int TRANSACTION_enableVisualVoicemailSmsFilter = 67;
        static final int TRANSACTION_endCall = 3;
        static final int TRANSACTION_endCallForSubscriber = 4;
        static final int TRANSACTION_factoryReset = 153;
        static final int TRANSACTION_getActivePhoneType = 48;
        static final int TRANSACTION_getActivePhoneTypeForSlot = 49;
        static final int TRANSACTION_getActiveVisualVoicemailSmsFilterSettings = 70;
        static final int TRANSACTION_getAidForAppType = 162;
        static final int TRANSACTION_getAllCellInfo = 82;
        static final int TRANSACTION_getAllowedCarriers = 167;
        static final int TRANSACTION_getCalculatedPreferredNetworkType = 95;
        static final int TRANSACTION_getCallState = 44;
        static final int TRANSACTION_getCallStateForSlot = 45;
        static final int TRANSACTION_getCarrierIdListVersion = 185;
        static final int TRANSACTION_getCarrierPackageNamesForIntentAndPhone = 125;
        static final int TRANSACTION_getCarrierPrivilegeStatus = 121;
        static final int TRANSACTION_getCarrierPrivilegeStatusForUid = 122;
        static final int TRANSACTION_getCdmaEriIconIndex = 50;
        static final int TRANSACTION_getCdmaEriIconIndexForSubscriber = 51;
        static final int TRANSACTION_getCdmaEriIconMode = 52;
        static final int TRANSACTION_getCdmaEriIconModeForSubscriber = 53;
        static final int TRANSACTION_getCdmaEriText = 54;
        static final int TRANSACTION_getCdmaEriTextForSubscriber = 55;
        static final int TRANSACTION_getCdmaMdn = 119;
        static final int TRANSACTION_getCdmaMin = 120;
        static final int TRANSACTION_getCdmaPrlVersion = 164;
        static final int TRANSACTION_getCellLocation = 41;
        static final int TRANSACTION_getCellNetworkScanResults = 108;
        static final int TRANSACTION_getClientRequestStats = 175;
        static final int TRANSACTION_getDataActivationState = 61;
        static final int TRANSACTION_getDataActivity = 46;
        static final int TRANSACTION_getDataEnabled = 114;
        static final int TRANSACTION_getDataNetworkType = 75;
        static final int TRANSACTION_getDataNetworkTypeForSubscriber = 76;
        static final int TRANSACTION_getDataState = 47;
        static final int TRANSACTION_getDefaultSim = 84;
        static final int TRANSACTION_getDeviceId = 148;
        static final int TRANSACTION_getDeviceSoftwareVersionForSlot = 151;
        static final int TRANSACTION_getEmergencyCallbackMode = 179;
        static final int TRANSACTION_getEsn = 163;
        static final int TRANSACTION_getForbiddenPlmns = 178;
        static final int TRANSACTION_getImeiForSlot = 149;
        static final int TRANSACTION_getImsConfig = 103;
        static final int TRANSACTION_getImsRegTechnologyForMmTel = 147;
        static final int TRANSACTION_getImsRegistration = 102;
        static final int TRANSACTION_getImsService = 106;
        static final int TRANSACTION_getLine1AlphaTagForDisplay = 128;
        static final int TRANSACTION_getLine1NumberForDisplay = 127;
        static final int TRANSACTION_getLocaleFromDefaultSim = 154;
        static final int TRANSACTION_getLteOnCdmaMode = 80;
        static final int TRANSACTION_getLteOnCdmaModeForSubscriber = 81;
        static final int TRANSACTION_getMeidForSlot = 150;
        static final int TRANSACTION_getMergedSubscriberIds = 129;
        static final int TRANSACTION_getMmTelFeatureAndListen = 100;
        static final int TRANSACTION_getNeighboringCellInfo = 43;
        static final int TRANSACTION_getNetworkCountryIsoForPhone = 42;
        static final int TRANSACTION_getNetworkType = 73;
        static final int TRANSACTION_getNetworkTypeForSubscriber = 74;
        static final int TRANSACTION_getPackagesWithCarrierPrivileges = 161;
        static final int TRANSACTION_getPcscfAddress = 117;
        static final int TRANSACTION_getPreferredNetworkType = 96;
        static final int TRANSACTION_getRadioAccessFamily = 136;
        static final int TRANSACTION_getRcsFeatureAndListen = 101;
        static final int TRANSACTION_getServiceStateForSubscriber = 156;
        static final int TRANSACTION_getSignalStrength = 180;
        static final int TRANSACTION_getSubIdForPhoneAccount = 152;
        static final int TRANSACTION_getSubscriptionCarrierId = 168;
        static final int TRANSACTION_getSubscriptionCarrierName = 169;
        static final int TRANSACTION_getTelephonyHistograms = 165;
        static final int TRANSACTION_getTetherApnRequired = 97;
        static final int TRANSACTION_getUiccSlotsInfo = 181;
        static final int TRANSACTION_getVisualVoicemailPackageName = 66;
        static final int TRANSACTION_getVisualVoicemailSettings = 65;
        static final int TRANSACTION_getVisualVoicemailSmsFilterSettings = 69;
        static final int TRANSACTION_getVoiceActivationState = 60;
        static final int TRANSACTION_getVoiceMessageCount = 62;
        static final int TRANSACTION_getVoiceMessageCountForSubscriber = 63;
        static final int TRANSACTION_getVoiceNetworkTypeForSubscriber = 77;
        static final int TRANSACTION_getVoicemailRingtoneUri = 157;
        static final int TRANSACTION_getVtDataUsage = 173;
        static final int TRANSACTION_handlePinMmi = 24;
        static final int TRANSACTION_handlePinMmiForSubscriber = 26;
        static final int TRANSACTION_handleUssdRequest = 25;
        static final int TRANSACTION_hasIccCard = 78;
        static final int TRANSACTION_hasIccCardUsingSlotIndex = 79;
        static final int TRANSACTION_iccCloseLogicalChannel = 86;
        static final int TRANSACTION_iccExchangeSimIO = 89;
        static final int TRANSACTION_iccOpenLogicalChannel = 85;
        static final int TRANSACTION_iccTransmitApduBasicChannel = 88;
        static final int TRANSACTION_iccTransmitApduLogicalChannel = 87;
        static final int TRANSACTION_invokeOemRilRequestRaw = 132;
        static final int TRANSACTION_isConcurrentVoiceAndDataAllowed = 64;
        static final int TRANSACTION_isDataConnectivityPossible = 40;
        static final int TRANSACTION_isDataEnabled = 116;
        static final int TRANSACTION_isHearingAidCompatibilitySupported = 142;
        static final int TRANSACTION_isIdle = 12;
        static final int TRANSACTION_isIdleForSubscriber = 13;
        static final int TRANSACTION_isImsRegistered = 143;
        static final int TRANSACTION_isOffhook = 8;
        static final int TRANSACTION_isOffhookForSubscriber = 9;
        static final int TRANSACTION_isRadioOn = 14;
        static final int TRANSACTION_isRadioOnForSubscriber = 15;
        static final int TRANSACTION_isResolvingImsBinding = 104;
        static final int TRANSACTION_isRinging = 11;
        static final int TRANSACTION_isRingingForSubscriber = 10;
        static final int TRANSACTION_isTtyModeSupported = 141;
        static final int TRANSACTION_isUserDataEnabled = 115;
        static final int TRANSACTION_isVideoCallingEnabled = 138;
        static final int TRANSACTION_isVideoTelephonyAvailable = 146;
        static final int TRANSACTION_isVoicemailVibrationEnabled = 159;
        static final int TRANSACTION_isVolteAvailable = 145;
        static final int TRANSACTION_isWifiCallingAvailable = 144;
        static final int TRANSACTION_isWorldPhone = 140;
        static final int TRANSACTION_needMobileRadioShutdown = 133;
        static final int TRANSACTION_needsOtaServiceProvisioning = 56;
        static final int TRANSACTION_nvReadItem = 91;
        static final int TRANSACTION_nvResetConfig = 94;
        static final int TRANSACTION_nvWriteCdmaPrl = 93;
        static final int TRANSACTION_nvWriteItem = 92;
        static final int TRANSACTION_refreshUiccProfile = 186;
        static final int TRANSACTION_requestModemActivityInfo = 155;
        static final int TRANSACTION_requestNetworkScan = 109;
        static final int TRANSACTION_sendDialerSpecialCode = 72;
        static final int TRANSACTION_sendEnvelopeWithStatus = 90;
        static final int TRANSACTION_sendVisualVoicemailSmsForSubscriber = 71;
        static final int TRANSACTION_setAllowedCarriers = 166;
        static final int TRANSACTION_setCarrierTestOverride = 184;
        static final int TRANSACTION_setCellInfoListRate = 83;
        static final int TRANSACTION_setDataActivationState = 59;
        static final int TRANSACTION_setDataEnabledProperties = 176;
        static final int TRANSACTION_setImsRegistrationState = 118;
        static final int TRANSACTION_setImsService = 105;
        static final int TRANSACTION_setLine1NumberForDisplayForSubscriber = 126;
        static final int TRANSACTION_setNetworkSelectionModeAutomatic = 107;
        static final int TRANSACTION_setNetworkSelectionModeManual = 111;
        static final int TRANSACTION_setOperatorBrandOverride = 130;
        static final int TRANSACTION_setPolicyDataEnabled = 174;
        static final int TRANSACTION_setPreferredNetworkType = 112;
        static final int TRANSACTION_setRadio = 29;
        static final int TRANSACTION_setRadioCapability = 135;
        static final int TRANSACTION_setRadioForSubscriber = 30;
        static final int TRANSACTION_setRadioIndicationUpdateMode = 183;
        static final int TRANSACTION_setRadioPower = 31;
        static final int TRANSACTION_setRoamingOverride = 131;
        static final int TRANSACTION_setSimPowerStateForSlot = 177;
        static final int TRANSACTION_setUserDataEnabled = 113;
        static final int TRANSACTION_setVoiceActivationState = 58;
        static final int TRANSACTION_setVoiceMailNumber = 57;
        static final int TRANSACTION_setVoicemailRingtoneUri = 158;
        static final int TRANSACTION_setVoicemailVibrationEnabled = 160;
        static final int TRANSACTION_shutdownMobileRadios = 134;
        static final int TRANSACTION_silenceRinger = 7;
        static final int TRANSACTION_stopNetworkScan = 110;
        static final int TRANSACTION_supplyPin = 16;
        static final int TRANSACTION_supplyPinForSubscriber = 17;
        static final int TRANSACTION_supplyPinReportResult = 20;
        static final int TRANSACTION_supplyPinReportResultForSubscriber = 21;
        static final int TRANSACTION_supplyPuk = 18;
        static final int TRANSACTION_supplyPukForSubscriber = 19;
        static final int TRANSACTION_supplyPukReportResult = 22;
        static final int TRANSACTION_supplyPukReportResultForSubscriber = 23;
        static final int TRANSACTION_switchSlots = 182;
        static final int TRANSACTION_toggleRadioOnOff = 27;
        static final int TRANSACTION_toggleRadioOnOffForSubscriber = 28;
        static final int TRANSACTION_updateServiceLocation = 32;
        static final int TRANSACTION_updateServiceLocationForSubscriber = 33;

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
                    this.mRemote.transact(1, _data, _reply, 0);
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
                    this.mRemote.transact(2, _data, _reply, 0);
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
                    boolean _result = false;
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void answerRingingCall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
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
                    this.mRemote.transact(6, _data, _reply, 0);
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
                    this.mRemote.transact(7, _data, _reply, 0);
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
                    boolean _result = false;
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
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
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
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
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
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
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
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
                    boolean _result = false;
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void handleUssdRequest(int subId, String ussdRequest, ResultReceiver wrappedCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(ussdRequest);
                    if (wrappedCallback != null) {
                        _data.writeInt(1);
                        wrappedCallback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void toggleRadioOnOff() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(27, _data, _reply, 0);
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
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setRadio(boolean turnOn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(turnOn);
                    boolean _result = false;
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setRadioForSubscriber(int subId, boolean turnOn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(turnOn);
                    boolean _result = false;
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setRadioPower(boolean turnOn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(turnOn);
                    boolean _result = false;
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateServiceLocation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(32, _data, _reply, 0);
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
                    this.mRemote.transact(33, _data, _reply, 0);
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
                    this.mRemote.transact(34, _data, _reply, 0);
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
                    this.mRemote.transact(35, _data, _reply, 0);
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
                    this.mRemote.transact(36, _data, _reply, 0);
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
                    this.mRemote.transact(37, _data, _reply, 0);
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
                    boolean _result = false;
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean disableDataConnectivity() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isDataConnectivityPossible(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getCellLocation(String callingPkg) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getNetworkCountryIsoForPhone(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
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
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(NeighboringCellInfo.CREATOR);
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
                    this.mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCallStateForSlot(int slotIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(46, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(48, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getActivePhoneTypeForSlot(int slotIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    this.mRemote.transact(49, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(50, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(51, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(52, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(53, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(54, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
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
                    this.mRemote.transact(55, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
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
                    boolean _result = false;
                    this.mRemote.transact(56, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(57, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setVoiceActivationState(int subId, int activationState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(activationState);
                    this.mRemote.transact(58, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDataActivationState(int subId, int activationState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(activationState);
                    this.mRemote.transact(59, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVoiceActivationState(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(60, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDataActivationState(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(61, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVoiceMessageCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(62, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(63, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isConcurrentVoiceAndDataAllowed(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(64, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getVisualVoicemailSettings(String callingPackage, int subId) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(subId);
                    this.mRemote.transact(65, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getVisualVoicemailPackageName(String callingPackage, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(subId);
                    this.mRemote.transact(66, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableVisualVoicemailSmsFilter(String callingPackage, int subId, VisualVoicemailSmsFilterSettings settings) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(subId);
                    if (settings != null) {
                        _data.writeInt(1);
                        settings.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(67, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disableVisualVoicemailSmsFilter(String callingPackage, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(subId);
                    this.mRemote.transact(68, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public VisualVoicemailSmsFilterSettings getVisualVoicemailSmsFilterSettings(String callingPackage, int subId) throws RemoteException {
                VisualVoicemailSmsFilterSettings _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(subId);
                    this.mRemote.transact(69, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (VisualVoicemailSmsFilterSettings) VisualVoicemailSmsFilterSettings.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public VisualVoicemailSmsFilterSettings getActiveVisualVoicemailSmsFilterSettings(int subId) throws RemoteException {
                VisualVoicemailSmsFilterSettings _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(70, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (VisualVoicemailSmsFilterSettings) VisualVoicemailSmsFilterSettings.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendVisualVoicemailSmsForSubscriber(String callingPackage, int subId, String number, int port, String text, PendingIntent sentIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(subId);
                    _data.writeString(number);
                    _data.writeInt(port);
                    _data.writeString(text);
                    if (sentIntent != null) {
                        _data.writeInt(1);
                        sentIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(71, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendDialerSpecialCode(String callingPackageName, String inputCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackageName);
                    _data.writeString(inputCode);
                    this.mRemote.transact(72, _data, _reply, 0);
                    _reply.readException();
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
                    this.mRemote.transact(73, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(74, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(75, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(76, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(77, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    boolean _result = false;
                    this.mRemote.transact(78, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasIccCardUsingSlotIndex(int slotIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    boolean _result = false;
                    this.mRemote.transact(79, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    this.mRemote.transact(80, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(81, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(82, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(CellInfo.CREATOR);
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
                    this.mRemote.transact(83, _data, _reply, 0);
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
                    this.mRemote.transact(84, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IccOpenLogicalChannelResponse iccOpenLogicalChannel(int subId, String callingPackage, String AID, int p2) throws RemoteException {
                IccOpenLogicalChannelResponse _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    _data.writeString(AID);
                    _data.writeInt(p2);
                    this.mRemote.transact(85, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (IccOpenLogicalChannelResponse) IccOpenLogicalChannelResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(86, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    this.mRemote.transact(87, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String iccTransmitApduBasicChannel(int subId, String callingPackage, int cla, int instruction, int p1, int p2, int p3, String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    _data.writeInt(cla);
                    _data.writeInt(instruction);
                    _data.writeInt(p1);
                    _data.writeInt(p2);
                    _data.writeInt(p3);
                    _data.writeString(data);
                    this.mRemote.transact(88, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
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
                    this.mRemote.transact(89, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
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
                    this.mRemote.transact(90, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
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
                    this.mRemote.transact(91, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
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
                    boolean _result = false;
                    this.mRemote.transact(92, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(93, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(94, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    this.mRemote.transact(95, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(96, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(97, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableIms(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    this.mRemote.transact(98, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disableIms(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    this.mRemote.transact(99, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IImsMmTelFeature getMmTelFeatureAndListen(int slotId, IImsServiceFeatureCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(100, _data, _reply, 0);
                    _reply.readException();
                    return IImsMmTelFeature.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IImsRcsFeature getRcsFeatureAndListen(int slotId, IImsServiceFeatureCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(101, _data, _reply, 0);
                    _reply.readException();
                    return IImsRcsFeature.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IImsRegistration getImsRegistration(int slotId, int feature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(feature);
                    this.mRemote.transact(102, _data, _reply, 0);
                    _reply.readException();
                    return IImsRegistration.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IImsConfig getImsConfig(int slotId, int feature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(feature);
                    this.mRemote.transact(103, _data, _reply, 0);
                    _reply.readException();
                    return IImsConfig.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isResolvingImsBinding() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(104, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setImsService(int slotId, boolean isCarrierImsService, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(isCarrierImsService);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(105, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getImsService(int slotId, boolean isCarrierImsService) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(isCarrierImsService);
                    this.mRemote.transact(106, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
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
                    this.mRemote.transact(107, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public CellNetworkScanResult getCellNetworkScanResults(int subId) throws RemoteException {
                CellNetworkScanResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(108, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = CellNetworkScanResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int requestNetworkScan(int subId, NetworkScanRequest request, Messenger messenger, IBinder binder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(binder);
                    this.mRemote.transact(109, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopNetworkScan(int subId, int scanId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(scanId);
                    this.mRemote.transact(110, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setNetworkSelectionModeManual(int subId, String operatorNumeric, boolean persistSelection) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(operatorNumeric);
                    _data.writeInt(persistSelection);
                    boolean _result = false;
                    this.mRemote.transact(111, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(112, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUserDataEnabled(int subId, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(enable);
                    this.mRemote.transact(113, _data, _reply, 0);
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
                    boolean _result = false;
                    this.mRemote.transact(114, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isUserDataEnabled(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(115, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isDataEnabled(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(116, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    this.mRemote.transact(117, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setImsRegistrationState(boolean registered) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(registered);
                    this.mRemote.transact(118, _data, _reply, 0);
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
                    this.mRemote.transact(119, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
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
                    this.mRemote.transact(120, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
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
                    this.mRemote.transact(121, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCarrierPrivilegeStatusForUid(int subId, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(uid);
                    this.mRemote.transact(122, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(123, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(124, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(phoneId);
                    this.mRemote.transact(125, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
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
                    boolean _result = false;
                    this.mRemote.transact(126, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    this.mRemote.transact(127, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
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
                    this.mRemote.transact(128, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
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
                    this.mRemote.transact(129, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
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
                    boolean _result = false;
                    this.mRemote.transact(130, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(131, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
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
                    this.mRemote.transact(132, _data, _reply, 0);
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
                    boolean _result = false;
                    this.mRemote.transact(133, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void shutdownMobileRadios() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(134, _data, _reply, 0);
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
                    this.mRemote.transact(135, _data, _reply, 0);
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
                    this.mRemote.transact(136, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableVideoCalling(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    this.mRemote.transact(137, _data, _reply, 0);
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
                    boolean _result = false;
                    this.mRemote.transact(138, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean canChangeDtmfToneLength() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(139, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isWorldPhone() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(140, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isTtyModeSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(141, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isHearingAidCompatibilitySupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(142, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isImsRegistered(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(143, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isWifiCallingAvailable(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(144, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isVolteAvailable(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(145, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isVideoTelephonyAvailable(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(146, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getImsRegTechnologyForMmTel(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(147, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
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
                    this.mRemote.transact(148, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getImeiForSlot(int slotIndex, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(149, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getMeidForSlot(int slotIndex, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(150, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getDeviceSoftwareVersionForSlot(int slotIndex, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(151, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
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
                        _data.writeInt(1);
                        phoneAccount.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(152, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(153, _data, _reply, 0);
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
                    this.mRemote.transact(154, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
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
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(155, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public ServiceState getServiceStateForSubscriber(int subId, String callingPackage) throws RemoteException {
                ServiceState _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(156, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ServiceState) ServiceState.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Uri getVoicemailRingtoneUri(PhoneAccountHandle accountHandle) throws RemoteException {
                Uri _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (accountHandle != null) {
                        _data.writeInt(1);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(157, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Uri) Uri.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setVoicemailRingtoneUri(String callingPackage, PhoneAccountHandle phoneAccountHandle, Uri uri) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (phoneAccountHandle != null) {
                        _data.writeInt(1);
                        phoneAccountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(158, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isVoicemailVibrationEnabled(PhoneAccountHandle accountHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (accountHandle != null) {
                        _data.writeInt(1);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(159, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setVoicemailVibrationEnabled(String callingPackage, PhoneAccountHandle phoneAccountHandle, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (phoneAccountHandle != null) {
                        _data.writeInt(1);
                        phoneAccountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(enabled);
                    this.mRemote.transact(160, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getPackagesWithCarrierPrivileges() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(161, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getAidForAppType(int subId, int appType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(appType);
                    this.mRemote.transact(162, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getEsn(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(163, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getCdmaPrlVersion(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(164, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<TelephonyHistogram> getTelephonyHistograms() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(165, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(TelephonyHistogram.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setAllowedCarriers(int slotIndex, List<CarrierIdentifier> carriers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    _data.writeTypedList(carriers);
                    this.mRemote.transact(166, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<CarrierIdentifier> getAllowedCarriers(int slotIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    this.mRemote.transact(167, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(CarrierIdentifier.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSubscriptionCarrierId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(168, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getSubscriptionCarrierName(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(169, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void carrierActionSetMeteredApnsEnabled(int subId, boolean visible) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(visible);
                    this.mRemote.transact(170, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void carrierActionSetRadioEnabled(int subId, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(enabled);
                    this.mRemote.transact(171, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void carrierActionReportDefaultNetworkStatus(int subId, boolean report) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(report);
                    this.mRemote.transact(172, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkStats getVtDataUsage(int subId, boolean perUidStats) throws RemoteException {
                NetworkStats _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(perUidStats);
                    this.mRemote.transact(173, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (NetworkStats) NetworkStats.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPolicyDataEnabled(boolean enabled, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled);
                    _data.writeInt(subId);
                    this.mRemote.transact(174, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<ClientRequestStats> getClientRequestStats(String callingPackage, int subid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(subid);
                    this.mRemote.transact(175, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(ClientRequestStats.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDataEnabledProperties(String appName, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appName);
                    _data.writeInt(enable);
                    this.mRemote.transact(176, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSimPowerStateForSlot(int slotIndex, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    _data.writeInt(state);
                    this.mRemote.transact(177, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getForbiddenPlmns(int subId, int appType, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(appType);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(178, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getEmergencyCallbackMode(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(179, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public SignalStrength getSignalStrength(int subId) throws RemoteException {
                SignalStrength _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(180, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (SignalStrength) SignalStrength.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public UiccSlotInfo[] getUiccSlotsInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(181, _data, _reply, 0);
                    _reply.readException();
                    return (UiccSlotInfo[]) _reply.createTypedArray(UiccSlotInfo.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean switchSlots(int[] physicalSlots) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(physicalSlots);
                    boolean _result = false;
                    this.mRemote.transact(182, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRadioIndicationUpdateMode(int subId, int filters, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(filters);
                    _data.writeInt(mode);
                    this.mRemote.transact(183, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setCarrierTestOverride(int subId, String mccmnc, String imsi, String iccid, String gid1, String gid2, String plmn, String spn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(mccmnc);
                    _data.writeString(imsi);
                    _data.writeString(iccid);
                    _data.writeString(gid1);
                    _data.writeString(gid2);
                    _data.writeString(plmn);
                    _data.writeString(spn);
                    this.mRemote.transact(184, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCarrierIdListVersion(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(185, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void refreshUiccProfile(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(186, _data, _reply, 0);
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

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v67, resolved type: android.telephony.VisualVoicemailSmsFilterSettings} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v123, resolved type: android.os.Messenger} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v176, resolved type: android.telecom.PhoneAccount} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v187, resolved type: android.telecom.PhoneAccountHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v191, resolved type: android.net.Uri} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v195, resolved type: android.telecom.PhoneAccountHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v199, resolved type: android.telecom.PhoneAccountHandle} */
        /* JADX WARNING: type inference failed for: r0v1 */
        /* JADX WARNING: type inference failed for: r0v24, types: [android.os.ResultReceiver] */
        /* JADX WARNING: type inference failed for: r0v74 */
        /* JADX WARNING: type inference failed for: r0v110, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r0v112, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r0v114, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r0v116, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r0v143, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r0v182, types: [android.os.ResultReceiver] */
        /* JADX WARNING: type inference failed for: r0v231 */
        /* JADX WARNING: type inference failed for: r0v232 */
        /* JADX WARNING: type inference failed for: r0v233 */
        /* JADX WARNING: type inference failed for: r0v234 */
        /* JADX WARNING: type inference failed for: r0v235 */
        /* JADX WARNING: type inference failed for: r0v236 */
        /* JADX WARNING: type inference failed for: r0v237 */
        /* JADX WARNING: type inference failed for: r0v238 */
        /* JADX WARNING: type inference failed for: r0v239 */
        /* JADX WARNING: type inference failed for: r0v240 */
        /* JADX WARNING: type inference failed for: r0v241 */
        /* JADX WARNING: type inference failed for: r0v242 */
        /* JADX WARNING: type inference failed for: r0v243 */
        /* JADX WARNING: type inference failed for: r0v244 */
        /* JADX WARNING: type inference failed for: r0v245 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            NetworkScanRequest _arg1;
            byte[] _arg12;
            PhoneAccountHandle _arg13;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                ? _arg14 = 0;
                boolean _arg15 = false;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        dial(data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        call(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result = endCall();
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result2 = endCallForSubscriber(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        answerRingingCall();
                        reply.writeNoException();
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        answerRingingCallForSubscriber(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        silenceRinger();
                        reply.writeNoException();
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result3 = isOffhook(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result4 = isOffhookForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result5 = isRingingForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result6 = isRinging(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result6);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result7 = isIdle(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result7);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result8 = isIdleForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result8);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result9 = isRadioOn(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result9);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result10 = isRadioOnForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result10);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result11 = supplyPin(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result11);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result12 = supplyPinForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result12);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result13 = supplyPuk(data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result13);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result14 = supplyPukForSubscriber(data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result14);
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        int[] _result15 = supplyPinReportResult(data.readString());
                        reply.writeNoException();
                        parcel2.writeIntArray(_result15);
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        int[] _result16 = supplyPinReportResultForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeIntArray(_result16);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        int[] _result17 = supplyPukReportResult(data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeIntArray(_result17);
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        int[] _result18 = supplyPukReportResultForSubscriber(data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeIntArray(_result18);
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result19 = handlePinMmi(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result19);
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        String _arg16 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(parcel);
                        }
                        handleUssdRequest(_arg0, _arg16, _arg14);
                        reply.writeNoException();
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result20 = handlePinMmiForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result20);
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        toggleRadioOnOff();
                        reply.writeNoException();
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        toggleRadioOnOffForSubscriber(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        boolean _result21 = setRadio(_arg15);
                        reply.writeNoException();
                        parcel2.writeInt(_result21);
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        boolean _result22 = setRadioForSubscriber(_arg02, _arg15);
                        reply.writeNoException();
                        parcel2.writeInt(_result22);
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        boolean _result23 = setRadioPower(_arg15);
                        reply.writeNoException();
                        parcel2.writeInt(_result23);
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        updateServiceLocation();
                        reply.writeNoException();
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        updateServiceLocationForSubscriber(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        enableLocationUpdates();
                        reply.writeNoException();
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        enableLocationUpdatesForSubscriber(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        disableLocationUpdates();
                        reply.writeNoException();
                        return true;
                    case 37:
                        parcel.enforceInterface(DESCRIPTOR);
                        disableLocationUpdatesForSubscriber(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 38:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result24 = enableDataConnectivity();
                        reply.writeNoException();
                        parcel2.writeInt(_result24);
                        return true;
                    case 39:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result25 = disableDataConnectivity();
                        reply.writeNoException();
                        parcel2.writeInt(_result25);
                        return true;
                    case 40:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result26 = isDataConnectivityPossible(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result26);
                        return true;
                    case 41:
                        parcel.enforceInterface(DESCRIPTOR);
                        Bundle _result27 = getCellLocation(data.readString());
                        reply.writeNoException();
                        if (_result27 != null) {
                            parcel2.writeInt(1);
                            _result27.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 42:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result28 = getNetworkCountryIsoForPhone(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result28);
                        return true;
                    case 43:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<NeighboringCellInfo> _result29 = getNeighboringCellInfo(data.readString());
                        reply.writeNoException();
                        parcel2.writeTypedList(_result29);
                        return true;
                    case 44:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result30 = getCallState();
                        reply.writeNoException();
                        parcel2.writeInt(_result30);
                        return true;
                    case 45:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result31 = getCallStateForSlot(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result31);
                        return true;
                    case 46:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result32 = getDataActivity();
                        reply.writeNoException();
                        parcel2.writeInt(_result32);
                        return true;
                    case 47:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result33 = getDataState();
                        reply.writeNoException();
                        parcel2.writeInt(_result33);
                        return true;
                    case 48:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result34 = getActivePhoneType();
                        reply.writeNoException();
                        parcel2.writeInt(_result34);
                        return true;
                    case 49:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result35 = getActivePhoneTypeForSlot(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result35);
                        return true;
                    case 50:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result36 = getCdmaEriIconIndex(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result36);
                        return true;
                    case 51:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result37 = getCdmaEriIconIndexForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result37);
                        return true;
                    case 52:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result38 = getCdmaEriIconMode(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result38);
                        return true;
                    case 53:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result39 = getCdmaEriIconModeForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result39);
                        return true;
                    case 54:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result40 = getCdmaEriText(data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result40);
                        return true;
                    case 55:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result41 = getCdmaEriTextForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result41);
                        return true;
                    case 56:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result42 = needsOtaServiceProvisioning();
                        reply.writeNoException();
                        parcel2.writeInt(_result42);
                        return true;
                    case 57:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result43 = setVoiceMailNumber(data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result43);
                        return true;
                    case 58:
                        parcel.enforceInterface(DESCRIPTOR);
                        setVoiceActivationState(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 59:
                        parcel.enforceInterface(DESCRIPTOR);
                        setDataActivationState(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 60:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result44 = getVoiceActivationState(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result44);
                        return true;
                    case 61:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result45 = getDataActivationState(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result45);
                        return true;
                    case 62:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result46 = getVoiceMessageCount();
                        reply.writeNoException();
                        parcel2.writeInt(_result46);
                        return true;
                    case 63:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result47 = getVoiceMessageCountForSubscriber(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result47);
                        return true;
                    case 64:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result48 = isConcurrentVoiceAndDataAllowed(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result48);
                        return true;
                    case 65:
                        parcel.enforceInterface(DESCRIPTOR);
                        Bundle _result49 = getVisualVoicemailSettings(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result49 != null) {
                            parcel2.writeInt(1);
                            _result49.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 66:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result50 = getVisualVoicemailPackageName(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result50);
                        return true;
                    case 67:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        int _arg17 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg14 = (VisualVoicemailSmsFilterSettings) VisualVoicemailSmsFilterSettings.CREATOR.createFromParcel(parcel);
                        }
                        enableVisualVoicemailSmsFilter(_arg03, _arg17, _arg14);
                        reply.writeNoException();
                        return true;
                    case 68:
                        parcel.enforceInterface(DESCRIPTOR);
                        disableVisualVoicemailSmsFilter(data.readString(), data.readInt());
                        return true;
                    case 69:
                        parcel.enforceInterface(DESCRIPTOR);
                        VisualVoicemailSmsFilterSettings _result51 = getVisualVoicemailSmsFilterSettings(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result51 != null) {
                            parcel2.writeInt(1);
                            _result51.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 70:
                        parcel.enforceInterface(DESCRIPTOR);
                        VisualVoicemailSmsFilterSettings _result52 = getActiveVisualVoicemailSmsFilterSettings(data.readInt());
                        reply.writeNoException();
                        if (_result52 != null) {
                            parcel2.writeInt(1);
                            _result52.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 71:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        int _arg18 = data.readInt();
                        String _arg2 = data.readString();
                        int _arg3 = data.readInt();
                        String _arg4 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(parcel);
                        }
                        sendVisualVoicemailSmsForSubscriber(_arg04, _arg18, _arg2, _arg3, _arg4, _arg14);
                        reply.writeNoException();
                        return true;
                    case 72:
                        parcel.enforceInterface(DESCRIPTOR);
                        sendDialerSpecialCode(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 73:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result53 = getNetworkType();
                        reply.writeNoException();
                        parcel2.writeInt(_result53);
                        return true;
                    case 74:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result54 = getNetworkTypeForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result54);
                        return true;
                    case 75:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result55 = getDataNetworkType(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result55);
                        return true;
                    case 76:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result56 = getDataNetworkTypeForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result56);
                        return true;
                    case 77:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result57 = getVoiceNetworkTypeForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result57);
                        return true;
                    case 78:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result58 = hasIccCard();
                        reply.writeNoException();
                        parcel2.writeInt(_result58);
                        return true;
                    case 79:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result59 = hasIccCardUsingSlotIndex(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result59);
                        return true;
                    case 80:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result60 = getLteOnCdmaMode(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result60);
                        return true;
                    case 81:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result61 = getLteOnCdmaModeForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result61);
                        return true;
                    case 82:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<CellInfo> _result62 = getAllCellInfo(data.readString());
                        reply.writeNoException();
                        parcel2.writeTypedList(_result62);
                        return true;
                    case 83:
                        parcel.enforceInterface(DESCRIPTOR);
                        setCellInfoListRate(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 84:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result63 = getDefaultSim();
                        reply.writeNoException();
                        parcel2.writeInt(_result63);
                        return true;
                    case 85:
                        parcel.enforceInterface(DESCRIPTOR);
                        IccOpenLogicalChannelResponse _result64 = iccOpenLogicalChannel(data.readInt(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result64 != null) {
                            parcel2.writeInt(1);
                            _result64.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 86:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result65 = iccCloseLogicalChannel(data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result65);
                        return true;
                    case 87:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result66 = iccTransmitApduLogicalChannel(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result66);
                        return true;
                    case 88:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result67 = iccTransmitApduBasicChannel(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result67);
                        return true;
                    case 89:
                        parcel.enforceInterface(DESCRIPTOR);
                        byte[] _result68 = iccExchangeSimIO(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeByteArray(_result68);
                        return true;
                    case 90:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result69 = sendEnvelopeWithStatus(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result69);
                        return true;
                    case 91:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result70 = nvReadItem(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result70);
                        return true;
                    case 92:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result71 = nvWriteItem(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result71);
                        return true;
                    case 93:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result72 = nvWriteCdmaPrl(data.createByteArray());
                        reply.writeNoException();
                        parcel2.writeInt(_result72);
                        return true;
                    case 94:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result73 = nvResetConfig(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result73);
                        return true;
                    case 95:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result74 = getCalculatedPreferredNetworkType(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result74);
                        return true;
                    case 96:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result75 = getPreferredNetworkType(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result75);
                        return true;
                    case 97:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result76 = getTetherApnRequired();
                        reply.writeNoException();
                        parcel2.writeInt(_result76);
                        return true;
                    case 98:
                        parcel.enforceInterface(DESCRIPTOR);
                        enableIms(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 99:
                        parcel.enforceInterface(DESCRIPTOR);
                        disableIms(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 100:
                        parcel.enforceInterface(DESCRIPTOR);
                        IImsMmTelFeature _result77 = getMmTelFeatureAndListen(data.readInt(), IImsServiceFeatureCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        if (_result77 != null) {
                            _arg14 = _result77.asBinder();
                        }
                        parcel2.writeStrongBinder(_arg14);
                        return true;
                    case 101:
                        parcel.enforceInterface(DESCRIPTOR);
                        IImsRcsFeature _result78 = getRcsFeatureAndListen(data.readInt(), IImsServiceFeatureCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        if (_result78 != null) {
                            _arg14 = _result78.asBinder();
                        }
                        parcel2.writeStrongBinder(_arg14);
                        return true;
                    case 102:
                        parcel.enforceInterface(DESCRIPTOR);
                        IImsRegistration _result79 = getImsRegistration(data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result79 != null) {
                            _arg14 = _result79.asBinder();
                        }
                        parcel2.writeStrongBinder(_arg14);
                        return true;
                    case 103:
                        parcel.enforceInterface(DESCRIPTOR);
                        IImsConfig _result80 = getImsConfig(data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result80 != null) {
                            _arg14 = _result80.asBinder();
                        }
                        parcel2.writeStrongBinder(_arg14);
                        return true;
                    case 104:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result81 = isResolvingImsBinding();
                        reply.writeNoException();
                        parcel2.writeInt(_result81);
                        return true;
                    case 105:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        boolean _result82 = setImsService(_arg05, _arg15, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result82);
                        return true;
                    case 106:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        String _result83 = getImsService(_arg06, _arg15);
                        reply.writeNoException();
                        parcel2.writeString(_result83);
                        return true;
                    case 107:
                        parcel.enforceInterface(DESCRIPTOR);
                        setNetworkSelectionModeAutomatic(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 108:
                        parcel.enforceInterface(DESCRIPTOR);
                        CellNetworkScanResult _result84 = getCellNetworkScanResults(data.readInt());
                        reply.writeNoException();
                        if (_result84 != null) {
                            parcel2.writeInt(1);
                            _result84.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 109:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg07 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (NetworkScanRequest) NetworkScanRequest.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = (Messenger) Messenger.CREATOR.createFromParcel(parcel);
                        }
                        int _result85 = requestNetworkScan(_arg07, _arg1, _arg14, data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result85);
                        return true;
                    case 110:
                        parcel.enforceInterface(DESCRIPTOR);
                        stopNetworkScan(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 111:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        String _arg19 = data.readString();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        boolean _result86 = setNetworkSelectionModeManual(_arg08, _arg19, _arg15);
                        reply.writeNoException();
                        parcel2.writeInt(_result86);
                        return true;
                    case 112:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result87 = setPreferredNetworkType(data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result87);
                        return true;
                    case 113:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg09 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setUserDataEnabled(_arg09, _arg15);
                        reply.writeNoException();
                        return true;
                    case 114:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result88 = getDataEnabled(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result88);
                        return true;
                    case 115:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result89 = isUserDataEnabled(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result89);
                        return true;
                    case 116:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result90 = isDataEnabled(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result90);
                        return true;
                    case 117:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result91 = getPcscfAddress(data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeStringArray(_result91);
                        return true;
                    case 118:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setImsRegistrationState(_arg15);
                        reply.writeNoException();
                        return true;
                    case 119:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result92 = getCdmaMdn(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result92);
                        return true;
                    case 120:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result93 = getCdmaMin(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result93);
                        return true;
                    case 121:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result94 = getCarrierPrivilegeStatus(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result94);
                        return true;
                    case 122:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result95 = getCarrierPrivilegeStatusForUid(data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result95);
                        return true;
                    case 123:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result96 = checkCarrierPrivilegesForPackage(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result96);
                        return true;
                    case 124:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result97 = checkCarrierPrivilegesForPackageAnyPhone(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result97);
                        return true;
                    case 125:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = (Intent) Intent.CREATOR.createFromParcel(parcel);
                        }
                        List<String> _result98 = getCarrierPackageNamesForIntentAndPhone(_arg14, data.readInt());
                        reply.writeNoException();
                        parcel2.writeStringList(_result98);
                        return true;
                    case 126:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result99 = setLine1NumberForDisplayForSubscriber(data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result99);
                        return true;
                    case 127:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result100 = getLine1NumberForDisplay(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result100);
                        return true;
                    case 128:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result101 = getLine1AlphaTagForDisplay(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result101);
                        return true;
                    case 129:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result102 = getMergedSubscriberIds(data.readString());
                        reply.writeNoException();
                        parcel2.writeStringArray(_result102);
                        return true;
                    case 130:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result103 = setOperatorBrandOverride(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result103);
                        return true;
                    case 131:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result104 = setRoamingOverride(data.readInt(), data.createStringArrayList(), data.createStringArrayList(), data.createStringArrayList(), data.createStringArrayList());
                        reply.writeNoException();
                        parcel2.writeInt(_result104);
                        return true;
                    case 132:
                        parcel.enforceInterface(DESCRIPTOR);
                        byte[] _arg010 = data.createByteArray();
                        int _arg1_length = data.readInt();
                        if (_arg1_length < 0) {
                            _arg12 = null;
                        } else {
                            _arg12 = new byte[_arg1_length];
                        }
                        int _result105 = invokeOemRilRequestRaw(_arg010, _arg12);
                        reply.writeNoException();
                        parcel2.writeInt(_result105);
                        parcel2.writeByteArray(_arg12);
                        return true;
                    case 133:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result106 = needMobileRadioShutdown();
                        reply.writeNoException();
                        parcel2.writeInt(_result106);
                        return true;
                    case 134:
                        parcel.enforceInterface(DESCRIPTOR);
                        shutdownMobileRadios();
                        reply.writeNoException();
                        return true;
                    case 135:
                        parcel.enforceInterface(DESCRIPTOR);
                        setRadioCapability((RadioAccessFamily[]) parcel.createTypedArray(RadioAccessFamily.CREATOR));
                        reply.writeNoException();
                        return true;
                    case 136:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result107 = getRadioAccessFamily(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result107);
                        return true;
                    case 137:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        enableVideoCalling(_arg15);
                        reply.writeNoException();
                        return true;
                    case 138:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result108 = isVideoCallingEnabled(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result108);
                        return true;
                    case 139:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result109 = canChangeDtmfToneLength();
                        reply.writeNoException();
                        parcel2.writeInt(_result109);
                        return true;
                    case 140:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result110 = isWorldPhone();
                        reply.writeNoException();
                        parcel2.writeInt(_result110);
                        return true;
                    case 141:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result111 = isTtyModeSupported();
                        reply.writeNoException();
                        parcel2.writeInt(_result111);
                        return true;
                    case 142:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result112 = isHearingAidCompatibilitySupported();
                        reply.writeNoException();
                        parcel2.writeInt(_result112);
                        return true;
                    case 143:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result113 = isImsRegistered(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result113);
                        return true;
                    case 144:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result114 = isWifiCallingAvailable(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result114);
                        return true;
                    case 145:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result115 = isVolteAvailable(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result115);
                        return true;
                    case 146:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result116 = isVideoTelephonyAvailable(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result116);
                        return true;
                    case 147:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result117 = getImsRegTechnologyForMmTel(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result117);
                        return true;
                    case 148:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result118 = getDeviceId(data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result118);
                        return true;
                    case 149:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result119 = getImeiForSlot(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result119);
                        return true;
                    case 150:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result120 = getMeidForSlot(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result120);
                        return true;
                    case 151:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result121 = getDeviceSoftwareVersionForSlot(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result121);
                        return true;
                    case 152:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = (PhoneAccount) PhoneAccount.CREATOR.createFromParcel(parcel);
                        }
                        int _result122 = getSubIdForPhoneAccount(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result122);
                        return true;
                    case 153:
                        parcel.enforceInterface(DESCRIPTOR);
                        factoryReset(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 154:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result123 = getLocaleFromDefaultSim();
                        reply.writeNoException();
                        parcel2.writeString(_result123);
                        return true;
                    case 155:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(parcel);
                        }
                        requestModemActivityInfo(_arg14);
                        return true;
                    case 156:
                        parcel.enforceInterface(DESCRIPTOR);
                        ServiceState _result124 = getServiceStateForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result124 != null) {
                            parcel2.writeInt(1);
                            _result124.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 157:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(parcel);
                        }
                        Uri _result125 = getVoicemailRingtoneUri(_arg14);
                        reply.writeNoException();
                        if (_result125 != null) {
                            parcel2.writeInt(1);
                            _result125.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 158:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg011 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg13 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = (Uri) Uri.CREATOR.createFromParcel(parcel);
                        }
                        setVoicemailRingtoneUri(_arg011, _arg13, _arg14);
                        reply.writeNoException();
                        return true;
                    case 159:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg14 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result126 = isVoicemailVibrationEnabled(_arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result126);
                        return true;
                    case 160:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg012 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setVoicemailVibrationEnabled(_arg012, _arg14, _arg15);
                        reply.writeNoException();
                        return true;
                    case 161:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<String> _result127 = getPackagesWithCarrierPrivileges();
                        reply.writeNoException();
                        parcel2.writeStringList(_result127);
                        return true;
                    case 162:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result128 = getAidForAppType(data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result128);
                        return true;
                    case 163:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result129 = getEsn(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result129);
                        return true;
                    case 164:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result130 = getCdmaPrlVersion(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result130);
                        return true;
                    case 165:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<TelephonyHistogram> _result131 = getTelephonyHistograms();
                        reply.writeNoException();
                        parcel2.writeTypedList(_result131);
                        return true;
                    case 166:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result132 = setAllowedCarriers(data.readInt(), parcel.createTypedArrayList(CarrierIdentifier.CREATOR));
                        reply.writeNoException();
                        parcel2.writeInt(_result132);
                        return true;
                    case 167:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<CarrierIdentifier> _result133 = getAllowedCarriers(data.readInt());
                        reply.writeNoException();
                        parcel2.writeTypedList(_result133);
                        return true;
                    case 168:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result134 = getSubscriptionCarrierId(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result134);
                        return true;
                    case 169:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result135 = getSubscriptionCarrierName(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result135);
                        return true;
                    case 170:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg013 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        carrierActionSetMeteredApnsEnabled(_arg013, _arg15);
                        reply.writeNoException();
                        return true;
                    case 171:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg014 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        carrierActionSetRadioEnabled(_arg014, _arg15);
                        reply.writeNoException();
                        return true;
                    case 172:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg015 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        carrierActionReportDefaultNetworkStatus(_arg015, _arg15);
                        reply.writeNoException();
                        return true;
                    case 173:
                        parcel.enforceInterface(DESCRIPTOR);
                        NetworkStats _result136 = getVtDataUsage(data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result136 != null) {
                            parcel2.writeInt(1);
                            _result136.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 174:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setPolicyDataEnabled(_arg15, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 175:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<ClientRequestStats> _result137 = getClientRequestStats(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeTypedList(_result137);
                        return true;
                    case 176:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg016 = data.readString();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setDataEnabledProperties(_arg016, _arg15);
                        reply.writeNoException();
                        return true;
                    case 177:
                        parcel.enforceInterface(DESCRIPTOR);
                        setSimPowerStateForSlot(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 178:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result138 = getForbiddenPlmns(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeStringArray(_result138);
                        return true;
                    case 179:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result139 = getEmergencyCallbackMode(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result139);
                        return true;
                    case 180:
                        parcel.enforceInterface(DESCRIPTOR);
                        SignalStrength _result140 = getSignalStrength(data.readInt());
                        reply.writeNoException();
                        if (_result140 != null) {
                            parcel2.writeInt(1);
                            _result140.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 181:
                        parcel.enforceInterface(DESCRIPTOR);
                        UiccSlotInfo[] _result141 = getUiccSlotsInfo();
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result141, 1);
                        return true;
                    case 182:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result142 = switchSlots(data.createIntArray());
                        reply.writeNoException();
                        parcel2.writeInt(_result142);
                        return true;
                    case 183:
                        parcel.enforceInterface(DESCRIPTOR);
                        setRadioIndicationUpdateMode(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 184:
                        parcel.enforceInterface(DESCRIPTOR);
                        setCarrierTestOverride(data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 185:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result143 = getCarrierIdListVersion(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result143);
                        return true;
                    case 186:
                        parcel.enforceInterface(DESCRIPTOR);
                        refreshUiccProfile(data.readInt());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void answerRingingCall() throws RemoteException;

    void answerRingingCallForSubscriber(int i) throws RemoteException;

    void call(String str, String str2) throws RemoteException;

    boolean canChangeDtmfToneLength() throws RemoteException;

    void carrierActionReportDefaultNetworkStatus(int i, boolean z) throws RemoteException;

    void carrierActionSetMeteredApnsEnabled(int i, boolean z) throws RemoteException;

    void carrierActionSetRadioEnabled(int i, boolean z) throws RemoteException;

    int checkCarrierPrivilegesForPackage(String str) throws RemoteException;

    int checkCarrierPrivilegesForPackageAnyPhone(String str) throws RemoteException;

    void dial(String str) throws RemoteException;

    boolean disableDataConnectivity() throws RemoteException;

    void disableIms(int i) throws RemoteException;

    void disableLocationUpdates() throws RemoteException;

    void disableLocationUpdatesForSubscriber(int i) throws RemoteException;

    void disableVisualVoicemailSmsFilter(String str, int i) throws RemoteException;

    boolean enableDataConnectivity() throws RemoteException;

    void enableIms(int i) throws RemoteException;

    void enableLocationUpdates() throws RemoteException;

    void enableLocationUpdatesForSubscriber(int i) throws RemoteException;

    void enableVideoCalling(boolean z) throws RemoteException;

    void enableVisualVoicemailSmsFilter(String str, int i, VisualVoicemailSmsFilterSettings visualVoicemailSmsFilterSettings) throws RemoteException;

    boolean endCall() throws RemoteException;

    boolean endCallForSubscriber(int i) throws RemoteException;

    void factoryReset(int i) throws RemoteException;

    int getActivePhoneType() throws RemoteException;

    int getActivePhoneTypeForSlot(int i) throws RemoteException;

    VisualVoicemailSmsFilterSettings getActiveVisualVoicemailSmsFilterSettings(int i) throws RemoteException;

    String getAidForAppType(int i, int i2) throws RemoteException;

    List<CellInfo> getAllCellInfo(String str) throws RemoteException;

    List<CarrierIdentifier> getAllowedCarriers(int i) throws RemoteException;

    int getCalculatedPreferredNetworkType(String str) throws RemoteException;

    int getCallState() throws RemoteException;

    int getCallStateForSlot(int i) throws RemoteException;

    int getCarrierIdListVersion(int i) throws RemoteException;

    List<String> getCarrierPackageNamesForIntentAndPhone(Intent intent, int i) throws RemoteException;

    int getCarrierPrivilegeStatus(int i) throws RemoteException;

    int getCarrierPrivilegeStatusForUid(int i, int i2) throws RemoteException;

    int getCdmaEriIconIndex(String str) throws RemoteException;

    int getCdmaEriIconIndexForSubscriber(int i, String str) throws RemoteException;

    int getCdmaEriIconMode(String str) throws RemoteException;

    int getCdmaEriIconModeForSubscriber(int i, String str) throws RemoteException;

    String getCdmaEriText(String str) throws RemoteException;

    String getCdmaEriTextForSubscriber(int i, String str) throws RemoteException;

    String getCdmaMdn(int i) throws RemoteException;

    String getCdmaMin(int i) throws RemoteException;

    String getCdmaPrlVersion(int i) throws RemoteException;

    Bundle getCellLocation(String str) throws RemoteException;

    CellNetworkScanResult getCellNetworkScanResults(int i) throws RemoteException;

    List<ClientRequestStats> getClientRequestStats(String str, int i) throws RemoteException;

    int getDataActivationState(int i, String str) throws RemoteException;

    int getDataActivity() throws RemoteException;

    boolean getDataEnabled(int i) throws RemoteException;

    int getDataNetworkType(String str) throws RemoteException;

    int getDataNetworkTypeForSubscriber(int i, String str) throws RemoteException;

    int getDataState() throws RemoteException;

    int getDefaultSim() throws RemoteException;

    String getDeviceId(String str) throws RemoteException;

    String getDeviceSoftwareVersionForSlot(int i, String str) throws RemoteException;

    boolean getEmergencyCallbackMode(int i) throws RemoteException;

    String getEsn(int i) throws RemoteException;

    String[] getForbiddenPlmns(int i, int i2, String str) throws RemoteException;

    String getImeiForSlot(int i, String str) throws RemoteException;

    IImsConfig getImsConfig(int i, int i2) throws RemoteException;

    int getImsRegTechnologyForMmTel(int i) throws RemoteException;

    IImsRegistration getImsRegistration(int i, int i2) throws RemoteException;

    String getImsService(int i, boolean z) throws RemoteException;

    String getLine1AlphaTagForDisplay(int i, String str) throws RemoteException;

    String getLine1NumberForDisplay(int i, String str) throws RemoteException;

    String getLocaleFromDefaultSim() throws RemoteException;

    int getLteOnCdmaMode(String str) throws RemoteException;

    int getLteOnCdmaModeForSubscriber(int i, String str) throws RemoteException;

    String getMeidForSlot(int i, String str) throws RemoteException;

    String[] getMergedSubscriberIds(String str) throws RemoteException;

    IImsMmTelFeature getMmTelFeatureAndListen(int i, IImsServiceFeatureCallback iImsServiceFeatureCallback) throws RemoteException;

    List<NeighboringCellInfo> getNeighboringCellInfo(String str) throws RemoteException;

    String getNetworkCountryIsoForPhone(int i) throws RemoteException;

    int getNetworkType() throws RemoteException;

    int getNetworkTypeForSubscriber(int i, String str) throws RemoteException;

    List<String> getPackagesWithCarrierPrivileges() throws RemoteException;

    String[] getPcscfAddress(String str, String str2) throws RemoteException;

    int getPreferredNetworkType(int i) throws RemoteException;

    int getRadioAccessFamily(int i, String str) throws RemoteException;

    IImsRcsFeature getRcsFeatureAndListen(int i, IImsServiceFeatureCallback iImsServiceFeatureCallback) throws RemoteException;

    ServiceState getServiceStateForSubscriber(int i, String str) throws RemoteException;

    SignalStrength getSignalStrength(int i) throws RemoteException;

    int getSubIdForPhoneAccount(PhoneAccount phoneAccount) throws RemoteException;

    int getSubscriptionCarrierId(int i) throws RemoteException;

    String getSubscriptionCarrierName(int i) throws RemoteException;

    List<TelephonyHistogram> getTelephonyHistograms() throws RemoteException;

    int getTetherApnRequired() throws RemoteException;

    UiccSlotInfo[] getUiccSlotsInfo() throws RemoteException;

    String getVisualVoicemailPackageName(String str, int i) throws RemoteException;

    Bundle getVisualVoicemailSettings(String str, int i) throws RemoteException;

    VisualVoicemailSmsFilterSettings getVisualVoicemailSmsFilterSettings(String str, int i) throws RemoteException;

    int getVoiceActivationState(int i, String str) throws RemoteException;

    int getVoiceMessageCount() throws RemoteException;

    int getVoiceMessageCountForSubscriber(int i) throws RemoteException;

    int getVoiceNetworkTypeForSubscriber(int i, String str) throws RemoteException;

    Uri getVoicemailRingtoneUri(PhoneAccountHandle phoneAccountHandle) throws RemoteException;

    NetworkStats getVtDataUsage(int i, boolean z) throws RemoteException;

    boolean handlePinMmi(String str) throws RemoteException;

    boolean handlePinMmiForSubscriber(int i, String str) throws RemoteException;

    void handleUssdRequest(int i, String str, ResultReceiver resultReceiver) throws RemoteException;

    boolean hasIccCard() throws RemoteException;

    boolean hasIccCardUsingSlotIndex(int i) throws RemoteException;

    boolean iccCloseLogicalChannel(int i, int i2) throws RemoteException;

    byte[] iccExchangeSimIO(int i, int i2, int i3, int i4, int i5, int i6, String str) throws RemoteException;

    IccOpenLogicalChannelResponse iccOpenLogicalChannel(int i, String str, String str2, int i2) throws RemoteException;

    String iccTransmitApduBasicChannel(int i, String str, int i2, int i3, int i4, int i5, int i6, String str2) throws RemoteException;

    String iccTransmitApduLogicalChannel(int i, int i2, int i3, int i4, int i5, int i6, int i7, String str) throws RemoteException;

    int invokeOemRilRequestRaw(byte[] bArr, byte[] bArr2) throws RemoteException;

    boolean isConcurrentVoiceAndDataAllowed(int i) throws RemoteException;

    boolean isDataConnectivityPossible(int i) throws RemoteException;

    boolean isDataEnabled(int i) throws RemoteException;

    boolean isHearingAidCompatibilitySupported() throws RemoteException;

    boolean isIdle(String str) throws RemoteException;

    boolean isIdleForSubscriber(int i, String str) throws RemoteException;

    boolean isImsRegistered(int i) throws RemoteException;

    boolean isOffhook(String str) throws RemoteException;

    boolean isOffhookForSubscriber(int i, String str) throws RemoteException;

    boolean isRadioOn(String str) throws RemoteException;

    boolean isRadioOnForSubscriber(int i, String str) throws RemoteException;

    boolean isResolvingImsBinding() throws RemoteException;

    boolean isRinging(String str) throws RemoteException;

    boolean isRingingForSubscriber(int i, String str) throws RemoteException;

    boolean isTtyModeSupported() throws RemoteException;

    boolean isUserDataEnabled(int i) throws RemoteException;

    boolean isVideoCallingEnabled(String str) throws RemoteException;

    boolean isVideoTelephonyAvailable(int i) throws RemoteException;

    boolean isVoicemailVibrationEnabled(PhoneAccountHandle phoneAccountHandle) throws RemoteException;

    boolean isVolteAvailable(int i) throws RemoteException;

    boolean isWifiCallingAvailable(int i) throws RemoteException;

    boolean isWorldPhone() throws RemoteException;

    boolean needMobileRadioShutdown() throws RemoteException;

    boolean needsOtaServiceProvisioning() throws RemoteException;

    String nvReadItem(int i) throws RemoteException;

    boolean nvResetConfig(int i) throws RemoteException;

    boolean nvWriteCdmaPrl(byte[] bArr) throws RemoteException;

    boolean nvWriteItem(int i, String str) throws RemoteException;

    void refreshUiccProfile(int i) throws RemoteException;

    void requestModemActivityInfo(ResultReceiver resultReceiver) throws RemoteException;

    int requestNetworkScan(int i, NetworkScanRequest networkScanRequest, Messenger messenger, IBinder iBinder) throws RemoteException;

    void sendDialerSpecialCode(String str, String str2) throws RemoteException;

    String sendEnvelopeWithStatus(int i, String str) throws RemoteException;

    void sendVisualVoicemailSmsForSubscriber(String str, int i, String str2, int i2, String str3, PendingIntent pendingIntent) throws RemoteException;

    int setAllowedCarriers(int i, List<CarrierIdentifier> list) throws RemoteException;

    void setCarrierTestOverride(int i, String str, String str2, String str3, String str4, String str5, String str6, String str7) throws RemoteException;

    void setCellInfoListRate(int i) throws RemoteException;

    void setDataActivationState(int i, int i2) throws RemoteException;

    void setDataEnabledProperties(String str, boolean z) throws RemoteException;

    void setImsRegistrationState(boolean z) throws RemoteException;

    boolean setImsService(int i, boolean z, String str) throws RemoteException;

    boolean setLine1NumberForDisplayForSubscriber(int i, String str, String str2) throws RemoteException;

    void setNetworkSelectionModeAutomatic(int i) throws RemoteException;

    boolean setNetworkSelectionModeManual(int i, String str, boolean z) throws RemoteException;

    boolean setOperatorBrandOverride(int i, String str) throws RemoteException;

    void setPolicyDataEnabled(boolean z, int i) throws RemoteException;

    boolean setPreferredNetworkType(int i, int i2) throws RemoteException;

    boolean setRadio(boolean z) throws RemoteException;

    void setRadioCapability(RadioAccessFamily[] radioAccessFamilyArr) throws RemoteException;

    boolean setRadioForSubscriber(int i, boolean z) throws RemoteException;

    void setRadioIndicationUpdateMode(int i, int i2, int i3) throws RemoteException;

    boolean setRadioPower(boolean z) throws RemoteException;

    boolean setRoamingOverride(int i, List<String> list, List<String> list2, List<String> list3, List<String> list4) throws RemoteException;

    void setSimPowerStateForSlot(int i, int i2) throws RemoteException;

    void setUserDataEnabled(int i, boolean z) throws RemoteException;

    void setVoiceActivationState(int i, int i2) throws RemoteException;

    boolean setVoiceMailNumber(int i, String str, String str2) throws RemoteException;

    void setVoicemailRingtoneUri(String str, PhoneAccountHandle phoneAccountHandle, Uri uri) throws RemoteException;

    void setVoicemailVibrationEnabled(String str, PhoneAccountHandle phoneAccountHandle, boolean z) throws RemoteException;

    void shutdownMobileRadios() throws RemoteException;

    void silenceRinger() throws RemoteException;

    void stopNetworkScan(int i, int i2) throws RemoteException;

    boolean supplyPin(String str) throws RemoteException;

    boolean supplyPinForSubscriber(int i, String str) throws RemoteException;

    int[] supplyPinReportResult(String str) throws RemoteException;

    int[] supplyPinReportResultForSubscriber(int i, String str) throws RemoteException;

    boolean supplyPuk(String str, String str2) throws RemoteException;

    boolean supplyPukForSubscriber(int i, String str, String str2) throws RemoteException;

    int[] supplyPukReportResult(String str, String str2) throws RemoteException;

    int[] supplyPukReportResultForSubscriber(int i, String str, String str2) throws RemoteException;

    boolean switchSlots(int[] iArr) throws RemoteException;

    void toggleRadioOnOff() throws RemoteException;

    void toggleRadioOnOffForSubscriber(int i) throws RemoteException;

    void updateServiceLocation() throws RemoteException;

    void updateServiceLocationForSubscriber(int i) throws RemoteException;
}
