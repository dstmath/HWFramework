package com.android.internal.telephony;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.UiccAuthResponse;
import com.android.internal.telephony.IPhoneCallback;

public interface IHwTelephony extends IInterface {

    public static abstract class Stub extends Binder implements IHwTelephony {
        private static final String DESCRIPTOR = "com.android.internal.telephony.IHwTelephony";
        static final int TRANSACTION_bindSimToProfile = 100;
        static final int TRANSACTION_changeSimPinCode = 86;
        static final int TRANSACTION_closeRrc = 6;
        static final int TRANSACTION_cmdForECInfo = 80;
        static final int TRANSACTION_get2GServiceAbility = 25;
        static final int TRANSACTION_getAntiFakeBaseStation = 108;
        static final int TRANSACTION_getCallForwardingOption = 95;
        static final int TRANSACTION_getCardTrayInfo = 111;
        static final int TRANSACTION_getCdmaGsmImsi = 41;
        static final int TRANSACTION_getCdmaGsmImsiForSubId = 101;
        static final int TRANSACTION_getCdmaMlplVersion = 44;
        static final int TRANSACTION_getCdmaMsplVersion = 45;
        static final int TRANSACTION_getCellLocation = 43;
        static final int TRANSACTION_getCsconEnabled = 113;
        static final int TRANSACTION_getDataRegisteredState = 115;
        static final int TRANSACTION_getDataStateForSubscriber = 12;
        static final int TRANSACTION_getDefault4GSlotId = 28;
        static final int TRANSACTION_getDemoString = 1;
        static final int TRANSACTION_getImsDomain = 65;
        static final int TRANSACTION_getImsDomainByPhoneId = 75;
        static final int TRANSACTION_getImsImpu = 97;
        static final int TRANSACTION_getImsSwitch = 61;
        static final int TRANSACTION_getImsSwitchByPhoneId = 71;
        static final int TRANSACTION_getLaaDetailedState = 89;
        static final int TRANSACTION_getLevel = 114;
        static final int TRANSACTION_getLine1NumberFromImpu = 98;
        static final int TRANSACTION_getLteServiceAbility = 14;
        static final int TRANSACTION_getLteServiceAbilityForSubId = 17;
        static final int TRANSACTION_getMeidForSubscriber = 2;
        static final int TRANSACTION_getNVESN = 11;
        static final int TRANSACTION_getNetworkModeFromDB = 117;
        static final int TRANSACTION_getOnDemandDataSubId = 39;
        static final int TRANSACTION_getPesnForSubscriber = 3;
        static final int TRANSACTION_getPreferredDataSubscription = 40;
        static final int TRANSACTION_getServiceAbilityForSubId = 16;
        static final int TRANSACTION_getSpecCardType = 31;
        static final int TRANSACTION_getSubState = 4;
        static final int TRANSACTION_getUiccAppType = 64;
        static final int TRANSACTION_getUiccAppTypeByPhoneId = 74;
        static final int TRANSACTION_getUiccCardType = 42;
        static final int TRANSACTION_getUniqueDeviceId = 46;
        static final int TRANSACTION_getVoiceRegisteredState = 116;
        static final int TRANSACTION_getWaitingSwitchBalongSlot = 38;
        static final int TRANSACTION_handleMapconImsaReq = 63;
        static final int TRANSACTION_handleMapconImsaReqByPhoneId = 73;
        static final int TRANSACTION_handleUiccAuth = 66;
        static final int TRANSACTION_handleUiccAuthByPhoneId = 76;
        static final int TRANSACTION_informModemTetherStatusToChangeGRO = 105;
        static final int TRANSACTION_invokeOemRilRequestRaw = 92;
        static final int TRANSACTION_is4RMimoEnabled = 107;
        static final int TRANSACTION_isAISCard = 120;
        static final int TRANSACTION_isCTCdmaCardInGsmMode = 7;
        static final int TRANSACTION_isCardUimLocked = 32;
        static final int TRANSACTION_isCspPlmnEnabled = 93;
        static final int TRANSACTION_isCtSimCard = 81;
        static final int TRANSACTION_isCustomAis = 121;
        static final int TRANSACTION_isDomesticCard = 49;
        static final int TRANSACTION_isImsRegisteredForSubId = 19;
        static final int TRANSACTION_isLTESupported = 47;
        static final int TRANSACTION_isNeedToRadioPowerOn = 27;
        static final int TRANSACTION_isRadioAvailable = 59;
        static final int TRANSACTION_isRadioAvailableByPhoneId = 69;
        static final int TRANSACTION_isRadioOn = 33;
        static final int TRANSACTION_isSecondaryCardGsmOnly = 99;
        static final int TRANSACTION_isSetDefault4GSlotIdEnabled = 36;
        static final int TRANSACTION_isSubDeactivedByPowerOff = 26;
        static final int TRANSACTION_isVideoTelephonyAvailableForSubId = 22;
        static final int TRANSACTION_isVolteAvailableForSubId = 21;
        static final int TRANSACTION_isWifiCallingAvailableForSubId = 20;
        static final int TRANSACTION_notifyCModemStatus = 82;
        static final int TRANSACTION_notifyCellularCommParaReady = 84;
        static final int TRANSACTION_notifyDeviceState = 83;
        static final int TRANSACTION_queryServiceCellBand = 52;
        static final int TRANSACTION_registerCommonImsaToMapconInfo = 57;
        static final int TRANSACTION_registerForAntiFakeBaseStation = 109;
        static final int TRANSACTION_registerForCallAltSrv = 90;
        static final int TRANSACTION_registerForPhoneEvent = 67;
        static final int TRANSACTION_registerForRadioAvailable = 53;
        static final int TRANSACTION_registerForRadioNotAvailable = 55;
        static final int TRANSACTION_registerForWirelessState = 77;
        static final int TRANSACTION_saveNetworkModeToDB = 118;
        static final int TRANSACTION_sendLaaCmd = 88;
        static final int TRANSACTION_sendPseudocellCellInfo = 87;
        static final int TRANSACTION_sendSimMatchedOperatorInfo = 106;
        static final int TRANSACTION_set2GServiceAbility = 24;
        static final int TRANSACTION_setCallForwardingOption = 94;
        static final int TRANSACTION_setCellTxPower = 51;
        static final int TRANSACTION_setCsconEnabled = 112;
        static final int TRANSACTION_setDataEnabledWithoutPromp = 9;
        static final int TRANSACTION_setDataRoamingEnabledWithoutPromp = 10;
        static final int TRANSACTION_setDeepNoDisturbState = 103;
        static final int TRANSACTION_setDefault4GSlotId = 35;
        static final int TRANSACTION_setDefaultDataSlotId = 29;
        static final int TRANSACTION_setDefaultMobileEnable = 8;
        static final int TRANSACTION_setISMCOEX = 48;
        static final int TRANSACTION_setImsDomainConfig = 62;
        static final int TRANSACTION_setImsDomainConfigByPhoneId = 72;
        static final int TRANSACTION_setImsRegistrationStateForSubId = 23;
        static final int TRANSACTION_setImsSwitch = 60;
        static final int TRANSACTION_setImsSwitchByPhoneId = 70;
        static final int TRANSACTION_setLine1Number = 102;
        static final int TRANSACTION_setLteServiceAbility = 13;
        static final int TRANSACTION_setLteServiceAbilityForSubId = 18;
        static final int TRANSACTION_setMaxTxPower = 79;
        static final int TRANSACTION_setPinLockEnabled = 85;
        static final int TRANSACTION_setPreferredNetworkType = 34;
        static final int TRANSACTION_setServiceAbilityForSubId = 15;
        static final int TRANSACTION_setSubscription = 96;
        static final int TRANSACTION_setTemperatureControlToModem = 119;
        static final int TRANSACTION_setUplinkFreqBandwidthReportState = 104;
        static final int TRANSACTION_setUserPrefDataSlotId = 5;
        static final int TRANSACTION_setWifiTxPower = 50;
        static final int TRANSACTION_unregisterCommonImsaToMapconInfo = 58;
        static final int TRANSACTION_unregisterForAntiFakeBaseStation = 110;
        static final int TRANSACTION_unregisterForCallAltSrv = 91;
        static final int TRANSACTION_unregisterForPhoneEvent = 68;
        static final int TRANSACTION_unregisterForRadioAvailable = 54;
        static final int TRANSACTION_unregisterForRadioNotAvailable = 56;
        static final int TRANSACTION_unregisterForWirelessState = 78;
        static final int TRANSACTION_updateCrurrentPhone = 30;
        static final int TRANSACTION_waitingSetDefault4GSlotDone = 37;

        private static class Proxy implements IHwTelephony {
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

            public String getDemoString() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getMeidForSubscriber(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getPesnForSubscriber(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSubState(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUserPrefDataSlotId(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void closeRrc() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isCTCdmaCardInGsmMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(7, _data, _reply, 0);
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

            public void setDefaultMobileEnable(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDataEnabledWithoutPromp(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDataRoamingEnabledWithoutPromp(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getNVESN() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDataStateForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setLteServiceAbility(int ability) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ability);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getLteServiceAbility() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setServiceAbilityForSubId(int subId, int type, int ability) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(type);
                    _data.writeInt(ability);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getServiceAbilityForSubId(int subId, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(type);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getLteServiceAbilityForSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setLteServiceAbilityForSubId(int subId, int ability) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(ability);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isImsRegisteredForSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
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

            public boolean isWifiCallingAvailableForSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(20, _data, _reply, 0);
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

            public boolean isVolteAvailableForSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(21, _data, _reply, 0);
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

            public boolean isVideoTelephonyAvailableForSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(22, _data, _reply, 0);
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

            public void setImsRegistrationStateForSubId(int subId, boolean registered) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(registered);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void set2GServiceAbility(int ability) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ability);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int get2GServiceAbility() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSubDeactivedByPowerOff(long sub) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(sub);
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

            public boolean isNeedToRadioPowerOn(long sub) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(sub);
                    boolean _result = false;
                    this.mRemote.transact(27, _data, _reply, 0);
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

            public int getDefault4GSlotId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDefaultDataSlotId(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateCrurrentPhone(int lteSlot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(lteSlot);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSpecCardType(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isCardUimLocked(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    boolean _result = false;
                    this.mRemote.transact(32, _data, _reply, 0);
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

            public boolean isRadioOn(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    boolean _result = false;
                    this.mRemote.transact(33, _data, _reply, 0);
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

            public void setPreferredNetworkType(int nwMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nwMode);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDefault4GSlotId(int slotId, Message msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (msg != null) {
                        _data.writeInt(1);
                        msg.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSetDefault4GSlotIdEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(36, _data, _reply, 0);
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

            public void waitingSetDefault4GSlotDone(boolean waiting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(waiting);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getWaitingSwitchBalongSlot() throws RemoteException {
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

            public int getOnDemandDataSubId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPreferredDataSubscription() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getCdmaGsmImsi() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getUiccCardType(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getCellLocation(int slotId) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    this.mRemote.transact(43, _data, _reply, 0);
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

            public String getCdmaMlplVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCdmaMlplVersion, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getCdmaMsplVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCdmaMsplVersion, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getUniqueDeviceId(int scope) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(scope);
                    this.mRemote.transact(Stub.TRANSACTION_getUniqueDeviceId, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isLTESupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(47, _data, _reply, 0);
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

            public boolean setISMCOEX(String setISMCoex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(setISMCoex);
                    boolean _result = false;
                    this.mRemote.transact(48, _data, _reply, 0);
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

            public boolean isDomesticCard(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_isDomesticCard, _data, _reply, 0);
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

            public boolean setWifiTxPower(int power) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(power);
                    boolean _result = false;
                    this.mRemote.transact(50, _data, _reply, 0);
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

            public boolean setCellTxPower(int power) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(power);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_setCellTxPower, _data, _reply, 0);
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

            public String[] queryServiceCellBand() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_queryServiceCellBand, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerForRadioAvailable(IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_registerForRadioAvailable, _data, _reply, 0);
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

            public boolean unregisterForRadioAvailable(IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_unregisterForRadioAvailable, _data, _reply, 0);
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

            public boolean registerForRadioNotAvailable(IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_registerForRadioNotAvailable, _data, _reply, 0);
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

            public boolean unregisterForRadioNotAvailable(IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_unregisterForRadioNotAvailable, _data, _reply, 0);
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

            public boolean registerCommonImsaToMapconInfo(IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_registerCommonImsaToMapconInfo, _data, _reply, 0);
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

            public boolean unregisterCommonImsaToMapconInfo(IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_unregisterCommonImsaToMapconInfo, _data, _reply, 0);
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

            public boolean isRadioAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_isRadioAvailable, _data, _reply, 0);
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

            public void setImsSwitch(boolean value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(value);
                    this.mRemote.transact(Stub.TRANSACTION_setImsSwitch, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getImsSwitch() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_getImsSwitch, _data, _reply, 0);
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

            public void setImsDomainConfig(int domainType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(domainType);
                    this.mRemote.transact(Stub.TRANSACTION_setImsDomainConfig, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean handleMapconImsaReq(byte[] Msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(Msg);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_handleMapconImsaReq, _data, _reply, 0);
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

            public int getUiccAppType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(64, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getImsDomain() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(65, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public UiccAuthResponse handleUiccAuth(int auth_type, byte[] rand, byte[] auth) throws RemoteException {
                UiccAuthResponse _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(auth_type);
                    _data.writeByteArray(rand);
                    _data.writeByteArray(auth);
                    this.mRemote.transact(66, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = UiccAuthResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerForPhoneEvent(int phoneId, IPhoneCallback callback, int events) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(events);
                    boolean _result = false;
                    this.mRemote.transact(67, _data, _reply, 0);
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

            public void unregisterForPhoneEvent(IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(68, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isRadioAvailableByPhoneId(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_isRadioAvailableByPhoneId, _data, _reply, 0);
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

            public void setImsSwitchByPhoneId(int phoneId, boolean value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(value);
                    this.mRemote.transact(Stub.TRANSACTION_setImsSwitchByPhoneId, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getImsSwitchByPhoneId(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_getImsSwitchByPhoneId, _data, _reply, 0);
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

            public void setImsDomainConfigByPhoneId(int phoneId, int domainType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(domainType);
                    this.mRemote.transact(Stub.TRANSACTION_setImsDomainConfigByPhoneId, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean handleMapconImsaReqByPhoneId(int phoneId, byte[] Msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeByteArray(Msg);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_handleMapconImsaReqByPhoneId, _data, _reply, 0);
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

            public int getUiccAppTypeByPhoneId(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    this.mRemote.transact(Stub.TRANSACTION_getUiccAppTypeByPhoneId, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getImsDomainByPhoneId(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    this.mRemote.transact(Stub.TRANSACTION_getImsDomainByPhoneId, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public UiccAuthResponse handleUiccAuthByPhoneId(int phoneId, int auth_type, byte[] rand, byte[] auth) throws RemoteException {
                UiccAuthResponse _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(auth_type);
                    _data.writeByteArray(rand);
                    _data.writeByteArray(auth);
                    this.mRemote.transact(Stub.TRANSACTION_handleUiccAuthByPhoneId, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = UiccAuthResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerForWirelessState(int type, int slotId, IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(slotId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_registerForWirelessState, _data, _reply, 0);
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

            public boolean unregisterForWirelessState(int type, int slotId, IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(slotId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_unregisterForWirelessState, _data, _reply, 0);
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

            public boolean setMaxTxPower(int type, int power) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(power);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_setMaxTxPower, _data, _reply, 0);
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

            public boolean cmdForECInfo(int event, int action, byte[] buf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(event);
                    _data.writeInt(action);
                    _data.writeByteArray(buf);
                    boolean z = false;
                    this.mRemote.transact(Stub.TRANSACTION_cmdForECInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        z = true;
                    }
                    boolean _result = z;
                    _reply.readByteArray(buf);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isCtSimCard(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_isCtSimCard, _data, _reply, 0);
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

            public void notifyCModemStatus(int status, IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(Stub.TRANSACTION_notifyCModemStatus, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean notifyDeviceState(String device, String state, String extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(device);
                    _data.writeString(state);
                    _data.writeString(extras);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_notifyDeviceState, _data, _reply, 0);
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

            public void notifyCellularCommParaReady(int paratype, int pathtype, Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(paratype);
                    _data.writeInt(pathtype);
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_notifyCellularCommParaReady, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setPinLockEnabled(boolean enablePinLock, String password, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enablePinLock);
                    _data.writeString(password);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_setPinLockEnabled, _data, _reply, 0);
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

            public boolean changeSimPinCode(String oldPinCode, String newPinCode, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(oldPinCode);
                    _data.writeString(newPinCode);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_changeSimPinCode, _data, _reply, 0);
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

            public boolean sendPseudocellCellInfo(int type, int lac, int cid, int radioTech, String plmn, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(lac);
                    _data.writeInt(cid);
                    _data.writeInt(radioTech);
                    _data.writeString(plmn);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_sendPseudocellCellInfo, _data, _reply, 0);
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

            public boolean sendLaaCmd(int cmd, String reserved, Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cmd);
                    _data.writeString(reserved);
                    boolean _result = true;
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendLaaCmd, _data, _reply, 0);
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

            public boolean getLaaDetailedState(String reserved, Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reserved);
                    boolean _result = true;
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getLaaDetailedState, _data, _reply, 0);
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

            public void registerForCallAltSrv(int subId, IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(Stub.TRANSACTION_registerForCallAltSrv, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterForCallAltSrv(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterForCallAltSrv, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int invokeOemRilRequestRaw(int phoneId, byte[] oemReq, byte[] oemResp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeByteArray(oemReq);
                    _data.writeByteArray(oemResp);
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

            public boolean isCspPlmnEnabled(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_isCspPlmnEnabled, _data, _reply, 0);
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

            public void setCallForwardingOption(int subId, int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(commandInterfaceCFAction);
                    _data.writeInt(commandInterfaceCFReason);
                    _data.writeString(dialingNumber);
                    _data.writeInt(timerSeconds);
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setCallForwardingOption, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getCallForwardingOption(int subId, int commandInterfaceCFReason, Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(commandInterfaceCFReason);
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getCallForwardingOption, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setSubscription(int subId, boolean activate, Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(activate);
                    boolean _result = true;
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setSubscription, _data, _reply, 0);
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

            public String getImsImpu(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getImsImpu, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getLine1NumberFromImpu(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getLine1NumberFromImpu, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSecondaryCardGsmOnly() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(99, _data, _reply, 0);
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

            public boolean bindSimToProfile(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    boolean _result = false;
                    this.mRemote.transact(100, _data, _reply, 0);
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

            public String getCdmaGsmImsiForSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(101, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setLine1Number(int subId, String alphaTag, String number, Message onComplete) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(alphaTag);
                    _data.writeString(number);
                    boolean _result = true;
                    if (onComplete != null) {
                        _data.writeInt(1);
                        onComplete.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setLine1Number, _data, _reply, 0);
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

            public boolean setDeepNoDisturbState(int slotId, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(state);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_setDeepNoDisturbState, _data, _reply, 0);
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

            public boolean setUplinkFreqBandwidthReportState(int slotId, int state, IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(state);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_setUplinkFreqBandwidthReportState, _data, _reply, 0);
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

            public void informModemTetherStatusToChangeGRO(int enable, String faceName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    _data.writeString(faceName);
                    this.mRemote.transact(Stub.TRANSACTION_informModemTetherStatusToChangeGRO, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean sendSimMatchedOperatorInfo(int slotId, String opKey, String opName, int state, String reserveField) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeString(opKey);
                    _data.writeString(opName);
                    _data.writeInt(state);
                    _data.writeString(reserveField);
                    boolean _result = false;
                    this.mRemote.transact(106, _data, _reply, 0);
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

            public boolean is4RMimoEnabled(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_is4RMimoEnabled, _data, _reply, 0);
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

            public boolean getAntiFakeBaseStation(Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getAntiFakeBaseStation, _data, _reply, 0);
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

            public boolean registerForAntiFakeBaseStation(IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_registerForAntiFakeBaseStation, _data, _reply, 0);
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

            public boolean unregisterForAntiFakeBaseStation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_unregisterForAntiFakeBaseStation, _data, _reply, 0);
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

            public byte[] getCardTrayInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCardTrayInfo, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setCsconEnabled(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_setCsconEnabled, _data, _reply, 0);
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

            public int[] getCsconEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCsconEnabled, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getLevel(int type, int rssi, int ecio, int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(rssi);
                    _data.writeInt(ecio);
                    _data.writeInt(phoneId);
                    this.mRemote.transact(Stub.TRANSACTION_getLevel, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDataRegisteredState(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getDataRegisteredState, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVoiceRegisteredState(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getVoiceRegisteredState, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getNetworkModeFromDB(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getNetworkModeFromDB, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void saveNetworkModeToDB(int subId, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(mode);
                    this.mRemote.transact(Stub.TRANSACTION_saveNetworkModeToDB, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setTemperatureControlToModem(int level, int type, int subId, Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(level);
                    _data.writeInt(type);
                    _data.writeInt(subId);
                    boolean _result = true;
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setTemperatureControlToModem, _data, _reply, 0);
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

            public boolean isAISCard(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_isAISCard, _data, _reply, 0);
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

            public boolean isCustomAis() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_isCustomAis, _data, _reply, 0);
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwTelephony asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwTelephony)) {
                return new Proxy(obj);
            }
            return (IHwTelephony) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                Message _arg3 = null;
                boolean _arg1 = false;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result = getDemoString();
                        reply.writeNoException();
                        parcel2.writeString(_result);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result2 = getMeidForSubscriber(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result2);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result3 = getPesnForSubscriber(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result3);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result4 = getSubState(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        setUserPrefDataSlotId(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        closeRrc();
                        reply.writeNoException();
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result5 = isCTCdmaCardInGsmMode();
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setDefaultMobileEnable(_arg1);
                        reply.writeNoException();
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setDataEnabledWithoutPromp(_arg1);
                        reply.writeNoException();
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setDataRoamingEnabledWithoutPromp(_arg1);
                        reply.writeNoException();
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result6 = getNVESN();
                        reply.writeNoException();
                        parcel2.writeString(_result6);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result7 = getDataStateForSubscriber(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result7);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        setLteServiceAbility(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result8 = getLteServiceAbility();
                        reply.writeNoException();
                        parcel2.writeInt(_result8);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        setServiceAbilityForSubId(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result9 = getServiceAbilityForSubId(data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result9);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result10 = getLteServiceAbilityForSubId(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result10);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        setLteServiceAbilityForSubId(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result11 = isImsRegisteredForSubId(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result11);
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result12 = isWifiCallingAvailableForSubId(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result12);
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result13 = isVolteAvailableForSubId(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result13);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result14 = isVideoTelephonyAvailableForSubId(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result14);
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setImsRegistrationStateForSubId(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        set2GServiceAbility(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result15 = get2GServiceAbility();
                        reply.writeNoException();
                        parcel2.writeInt(_result15);
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result16 = isSubDeactivedByPowerOff(data.readLong());
                        reply.writeNoException();
                        parcel2.writeInt(_result16);
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result17 = isNeedToRadioPowerOn(data.readLong());
                        reply.writeNoException();
                        parcel2.writeInt(_result17);
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result18 = getDefault4GSlotId();
                        reply.writeNoException();
                        parcel2.writeInt(_result18);
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        setDefaultDataSlotId(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        updateCrurrentPhone(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result19 = getSpecCardType(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result19);
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result20 = isCardUimLocked(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result20);
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result21 = isRadioOn(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result21);
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        setPreferredNetworkType(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = (Message) Message.CREATOR.createFromParcel(parcel);
                        }
                        setDefault4GSlotId(_arg02, _arg3);
                        reply.writeNoException();
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result22 = isSetDefault4GSlotIdEnabled();
                        reply.writeNoException();
                        parcel2.writeInt(_result22);
                        return true;
                    case 37:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        waitingSetDefault4GSlotDone(_arg1);
                        reply.writeNoException();
                        return true;
                    case 38:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result23 = getWaitingSwitchBalongSlot();
                        reply.writeNoException();
                        parcel2.writeInt(_result23);
                        return true;
                    case 39:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result24 = getOnDemandDataSubId();
                        reply.writeNoException();
                        parcel2.writeInt(_result24);
                        return true;
                    case 40:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result25 = getPreferredDataSubscription();
                        reply.writeNoException();
                        parcel2.writeInt(_result25);
                        return true;
                    case 41:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result26 = getCdmaGsmImsi();
                        reply.writeNoException();
                        parcel2.writeString(_result26);
                        return true;
                    case 42:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result27 = getUiccCardType(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result27);
                        return true;
                    case 43:
                        parcel.enforceInterface(DESCRIPTOR);
                        Bundle _result28 = getCellLocation(data.readInt());
                        reply.writeNoException();
                        if (_result28 != null) {
                            parcel2.writeInt(1);
                            _result28.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_getCdmaMlplVersion /*44*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result29 = getCdmaMlplVersion();
                        reply.writeNoException();
                        parcel2.writeString(_result29);
                        return true;
                    case TRANSACTION_getCdmaMsplVersion /*45*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result30 = getCdmaMsplVersion();
                        reply.writeNoException();
                        parcel2.writeString(_result30);
                        return true;
                    case TRANSACTION_getUniqueDeviceId /*46*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result31 = getUniqueDeviceId(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result31);
                        return true;
                    case 47:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result32 = isLTESupported();
                        reply.writeNoException();
                        parcel2.writeInt(_result32);
                        return true;
                    case 48:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result33 = setISMCOEX(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result33);
                        return true;
                    case TRANSACTION_isDomesticCard /*49*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result34 = isDomesticCard(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result34);
                        return true;
                    case 50:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result35 = setWifiTxPower(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result35);
                        return true;
                    case TRANSACTION_setCellTxPower /*51*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result36 = setCellTxPower(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result36);
                        return true;
                    case TRANSACTION_queryServiceCellBand /*52*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result37 = queryServiceCellBand();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result37);
                        return true;
                    case TRANSACTION_registerForRadioAvailable /*53*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result38 = registerForRadioAvailable(IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result38);
                        return true;
                    case TRANSACTION_unregisterForRadioAvailable /*54*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result39 = unregisterForRadioAvailable(IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result39);
                        return true;
                    case TRANSACTION_registerForRadioNotAvailable /*55*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result40 = registerForRadioNotAvailable(IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result40);
                        return true;
                    case TRANSACTION_unregisterForRadioNotAvailable /*56*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result41 = unregisterForRadioNotAvailable(IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result41);
                        return true;
                    case TRANSACTION_registerCommonImsaToMapconInfo /*57*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result42 = registerCommonImsaToMapconInfo(IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result42);
                        return true;
                    case TRANSACTION_unregisterCommonImsaToMapconInfo /*58*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result43 = unregisterCommonImsaToMapconInfo(IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result43);
                        return true;
                    case TRANSACTION_isRadioAvailable /*59*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result44 = isRadioAvailable();
                        reply.writeNoException();
                        parcel2.writeInt(_result44);
                        return true;
                    case TRANSACTION_setImsSwitch /*60*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setImsSwitch(_arg1);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getImsSwitch /*61*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result45 = getImsSwitch();
                        reply.writeNoException();
                        parcel2.writeInt(_result45);
                        return true;
                    case TRANSACTION_setImsDomainConfig /*62*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        setImsDomainConfig(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_handleMapconImsaReq /*63*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result46 = handleMapconImsaReq(data.createByteArray());
                        reply.writeNoException();
                        parcel2.writeInt(_result46);
                        return true;
                    case 64:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result47 = getUiccAppType();
                        reply.writeNoException();
                        parcel2.writeInt(_result47);
                        return true;
                    case 65:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result48 = getImsDomain();
                        reply.writeNoException();
                        parcel2.writeInt(_result48);
                        return true;
                    case 66:
                        parcel.enforceInterface(DESCRIPTOR);
                        UiccAuthResponse _result49 = handleUiccAuth(data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        if (_result49 != null) {
                            parcel2.writeInt(1);
                            _result49.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 67:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result50 = registerForPhoneEvent(data.readInt(), IPhoneCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result50);
                        return true;
                    case 68:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterForPhoneEvent(IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_isRadioAvailableByPhoneId /*69*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result51 = isRadioAvailableByPhoneId(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result51);
                        return true;
                    case TRANSACTION_setImsSwitchByPhoneId /*70*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setImsSwitchByPhoneId(_arg03, _arg1);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getImsSwitchByPhoneId /*71*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result52 = getImsSwitchByPhoneId(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result52);
                        return true;
                    case TRANSACTION_setImsDomainConfigByPhoneId /*72*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        setImsDomainConfigByPhoneId(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_handleMapconImsaReqByPhoneId /*73*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result53 = handleMapconImsaReqByPhoneId(data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        parcel2.writeInt(_result53);
                        return true;
                    case TRANSACTION_getUiccAppTypeByPhoneId /*74*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result54 = getUiccAppTypeByPhoneId(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result54);
                        return true;
                    case TRANSACTION_getImsDomainByPhoneId /*75*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result55 = getImsDomainByPhoneId(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result55);
                        return true;
                    case TRANSACTION_handleUiccAuthByPhoneId /*76*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        UiccAuthResponse _result56 = handleUiccAuthByPhoneId(data.readInt(), data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        if (_result56 != null) {
                            parcel2.writeInt(1);
                            _result56.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_registerForWirelessState /*77*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result57 = registerForWirelessState(data.readInt(), data.readInt(), IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result57);
                        return true;
                    case TRANSACTION_unregisterForWirelessState /*78*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result58 = unregisterForWirelessState(data.readInt(), data.readInt(), IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result58);
                        return true;
                    case TRANSACTION_setMaxTxPower /*79*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result59 = setMaxTxPower(data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result59);
                        return true;
                    case TRANSACTION_cmdForECInfo /*80*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        int _arg12 = data.readInt();
                        byte[] _arg2 = data.createByteArray();
                        boolean _result60 = cmdForECInfo(_arg04, _arg12, _arg2);
                        reply.writeNoException();
                        parcel2.writeInt(_result60);
                        parcel2.writeByteArray(_arg2);
                        return true;
                    case TRANSACTION_isCtSimCard /*81*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result61 = isCtSimCard(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result61);
                        return true;
                    case TRANSACTION_notifyCModemStatus /*82*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        notifyCModemStatus(data.readInt(), IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_notifyDeviceState /*83*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result62 = notifyDeviceState(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result62);
                        return true;
                    case TRANSACTION_notifyCellularCommParaReady /*84*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        int _arg13 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = (Message) Message.CREATOR.createFromParcel(parcel);
                        }
                        notifyCellularCommParaReady(_arg05, _arg13, _arg3);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setPinLockEnabled /*85*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean _result63 = setPinLockEnabled(_arg1, data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result63);
                        return true;
                    case TRANSACTION_changeSimPinCode /*86*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result64 = changeSimPinCode(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result64);
                        return true;
                    case TRANSACTION_sendPseudocellCellInfo /*87*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result65 = sendPseudocellCellInfo(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result65);
                        return true;
                    case TRANSACTION_sendLaaCmd /*88*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        String _arg14 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = (Message) Message.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result66 = sendLaaCmd(_arg06, _arg14, _arg3);
                        reply.writeNoException();
                        parcel2.writeInt(_result66);
                        return true;
                    case TRANSACTION_getLaaDetailedState /*89*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = (Message) Message.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result67 = getLaaDetailedState(_arg07, _arg3);
                        reply.writeNoException();
                        parcel2.writeInt(_result67);
                        return true;
                    case TRANSACTION_registerForCallAltSrv /*90*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        registerForCallAltSrv(data.readInt(), IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_unregisterForCallAltSrv /*91*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterForCallAltSrv(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_invokeOemRilRequestRaw /*92*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        byte[] _arg15 = data.createByteArray();
                        byte[] _arg22 = data.createByteArray();
                        int _result68 = invokeOemRilRequestRaw(_arg08, _arg15, _arg22);
                        reply.writeNoException();
                        parcel2.writeInt(_result68);
                        parcel2.writeByteArray(_arg22);
                        return true;
                    case TRANSACTION_isCspPlmnEnabled /*93*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result69 = isCspPlmnEnabled(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result69);
                        return true;
                    case TRANSACTION_setCallForwardingOption /*94*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg09 = data.readInt();
                        int _arg16 = data.readInt();
                        int _arg23 = data.readInt();
                        String _arg32 = data.readString();
                        int _arg4 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = (Message) Message.CREATOR.createFromParcel(parcel);
                        }
                        setCallForwardingOption(_arg09, _arg16, _arg23, _arg32, _arg4, _arg3);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getCallForwardingOption /*95*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg010 = data.readInt();
                        int _arg17 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = (Message) Message.CREATOR.createFromParcel(parcel);
                        }
                        getCallForwardingOption(_arg010, _arg17, _arg3);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setSubscription /*96*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg011 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = (Message) Message.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result70 = setSubscription(_arg011, _arg1, _arg3);
                        reply.writeNoException();
                        parcel2.writeInt(_result70);
                        return true;
                    case TRANSACTION_getImsImpu /*97*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result71 = getImsImpu(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result71);
                        return true;
                    case TRANSACTION_getLine1NumberFromImpu /*98*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result72 = getLine1NumberFromImpu(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result72);
                        return true;
                    case 99:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result73 = isSecondaryCardGsmOnly();
                        reply.writeNoException();
                        parcel2.writeInt(_result73);
                        return true;
                    case 100:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result74 = bindSimToProfile(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result74);
                        return true;
                    case 101:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result75 = getCdmaGsmImsiForSubId(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result75);
                        return true;
                    case TRANSACTION_setLine1Number /*102*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg012 = data.readInt();
                        String _arg18 = data.readString();
                        String _arg24 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = (Message) Message.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result76 = setLine1Number(_arg012, _arg18, _arg24, _arg3);
                        reply.writeNoException();
                        parcel2.writeInt(_result76);
                        return true;
                    case TRANSACTION_setDeepNoDisturbState /*103*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result77 = setDeepNoDisturbState(data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result77);
                        return true;
                    case TRANSACTION_setUplinkFreqBandwidthReportState /*104*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result78 = setUplinkFreqBandwidthReportState(data.readInt(), data.readInt(), IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result78);
                        return true;
                    case TRANSACTION_informModemTetherStatusToChangeGRO /*105*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        informModemTetherStatusToChangeGRO(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 106:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result79 = sendSimMatchedOperatorInfo(data.readInt(), data.readString(), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result79);
                        return true;
                    case TRANSACTION_is4RMimoEnabled /*107*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result80 = is4RMimoEnabled(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result80);
                        return true;
                    case TRANSACTION_getAntiFakeBaseStation /*108*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg3 = (Message) Message.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result81 = getAntiFakeBaseStation(_arg3);
                        reply.writeNoException();
                        parcel2.writeInt(_result81);
                        return true;
                    case TRANSACTION_registerForAntiFakeBaseStation /*109*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result82 = registerForAntiFakeBaseStation(IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result82);
                        return true;
                    case TRANSACTION_unregisterForAntiFakeBaseStation /*110*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result83 = unregisterForAntiFakeBaseStation();
                        reply.writeNoException();
                        parcel2.writeInt(_result83);
                        return true;
                    case TRANSACTION_getCardTrayInfo /*111*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        byte[] _result84 = getCardTrayInfo();
                        reply.writeNoException();
                        parcel2.writeByteArray(_result84);
                        return true;
                    case TRANSACTION_setCsconEnabled /*112*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean _result85 = setCsconEnabled(_arg1);
                        reply.writeNoException();
                        parcel2.writeInt(_result85);
                        return true;
                    case TRANSACTION_getCsconEnabled /*113*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int[] _result86 = getCsconEnabled();
                        reply.writeNoException();
                        parcel2.writeIntArray(_result86);
                        return true;
                    case TRANSACTION_getLevel /*114*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result87 = getLevel(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result87);
                        return true;
                    case TRANSACTION_getDataRegisteredState /*115*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result88 = getDataRegisteredState(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result88);
                        return true;
                    case TRANSACTION_getVoiceRegisteredState /*116*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result89 = getVoiceRegisteredState(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result89);
                        return true;
                    case TRANSACTION_getNetworkModeFromDB /*117*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result90 = getNetworkModeFromDB(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result90);
                        return true;
                    case TRANSACTION_saveNetworkModeToDB /*118*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        saveNetworkModeToDB(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setTemperatureControlToModem /*119*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg013 = data.readInt();
                        int _arg19 = data.readInt();
                        int _arg25 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = (Message) Message.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result91 = setTemperatureControlToModem(_arg013, _arg19, _arg25, _arg3);
                        reply.writeNoException();
                        parcel2.writeInt(_result91);
                        return true;
                    case TRANSACTION_isAISCard /*120*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result92 = isAISCard(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result92);
                        return true;
                    case TRANSACTION_isCustomAis /*121*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result93 = isCustomAis();
                        reply.writeNoException();
                        parcel2.writeInt(_result93);
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

    boolean bindSimToProfile(int i) throws RemoteException;

    boolean changeSimPinCode(String str, String str2, int i) throws RemoteException;

    void closeRrc() throws RemoteException;

    boolean cmdForECInfo(int i, int i2, byte[] bArr) throws RemoteException;

    int get2GServiceAbility() throws RemoteException;

    boolean getAntiFakeBaseStation(Message message) throws RemoteException;

    void getCallForwardingOption(int i, int i2, Message message) throws RemoteException;

    byte[] getCardTrayInfo() throws RemoteException;

    String getCdmaGsmImsi() throws RemoteException;

    String getCdmaGsmImsiForSubId(int i) throws RemoteException;

    String getCdmaMlplVersion() throws RemoteException;

    String getCdmaMsplVersion() throws RemoteException;

    Bundle getCellLocation(int i) throws RemoteException;

    int[] getCsconEnabled() throws RemoteException;

    int getDataRegisteredState(int i) throws RemoteException;

    int getDataStateForSubscriber(int i) throws RemoteException;

    int getDefault4GSlotId() throws RemoteException;

    String getDemoString() throws RemoteException;

    int getImsDomain() throws RemoteException;

    int getImsDomainByPhoneId(int i) throws RemoteException;

    String getImsImpu(int i) throws RemoteException;

    boolean getImsSwitch() throws RemoteException;

    boolean getImsSwitchByPhoneId(int i) throws RemoteException;

    boolean getLaaDetailedState(String str, Message message) throws RemoteException;

    int getLevel(int i, int i2, int i3, int i4) throws RemoteException;

    String getLine1NumberFromImpu(int i) throws RemoteException;

    int getLteServiceAbility() throws RemoteException;

    int getLteServiceAbilityForSubId(int i) throws RemoteException;

    String getMeidForSubscriber(int i) throws RemoteException;

    String getNVESN() throws RemoteException;

    int getNetworkModeFromDB(int i) throws RemoteException;

    int getOnDemandDataSubId() throws RemoteException;

    String getPesnForSubscriber(int i) throws RemoteException;

    int getPreferredDataSubscription() throws RemoteException;

    int getServiceAbilityForSubId(int i, int i2) throws RemoteException;

    int getSpecCardType(int i) throws RemoteException;

    int getSubState(int i) throws RemoteException;

    int getUiccAppType() throws RemoteException;

    int getUiccAppTypeByPhoneId(int i) throws RemoteException;

    int getUiccCardType(int i) throws RemoteException;

    String getUniqueDeviceId(int i) throws RemoteException;

    int getVoiceRegisteredState(int i) throws RemoteException;

    boolean getWaitingSwitchBalongSlot() throws RemoteException;

    boolean handleMapconImsaReq(byte[] bArr) throws RemoteException;

    boolean handleMapconImsaReqByPhoneId(int i, byte[] bArr) throws RemoteException;

    UiccAuthResponse handleUiccAuth(int i, byte[] bArr, byte[] bArr2) throws RemoteException;

    UiccAuthResponse handleUiccAuthByPhoneId(int i, int i2, byte[] bArr, byte[] bArr2) throws RemoteException;

    void informModemTetherStatusToChangeGRO(int i, String str) throws RemoteException;

    int invokeOemRilRequestRaw(int i, byte[] bArr, byte[] bArr2) throws RemoteException;

    boolean is4RMimoEnabled(int i) throws RemoteException;

    boolean isAISCard(int i) throws RemoteException;

    boolean isCTCdmaCardInGsmMode() throws RemoteException;

    boolean isCardUimLocked(int i) throws RemoteException;

    boolean isCspPlmnEnabled(int i) throws RemoteException;

    boolean isCtSimCard(int i) throws RemoteException;

    boolean isCustomAis() throws RemoteException;

    boolean isDomesticCard(int i) throws RemoteException;

    boolean isImsRegisteredForSubId(int i) throws RemoteException;

    boolean isLTESupported() throws RemoteException;

    boolean isNeedToRadioPowerOn(long j) throws RemoteException;

    boolean isRadioAvailable() throws RemoteException;

    boolean isRadioAvailableByPhoneId(int i) throws RemoteException;

    boolean isRadioOn(int i) throws RemoteException;

    boolean isSecondaryCardGsmOnly() throws RemoteException;

    boolean isSetDefault4GSlotIdEnabled() throws RemoteException;

    boolean isSubDeactivedByPowerOff(long j) throws RemoteException;

    boolean isVideoTelephonyAvailableForSubId(int i) throws RemoteException;

    boolean isVolteAvailableForSubId(int i) throws RemoteException;

    boolean isWifiCallingAvailableForSubId(int i) throws RemoteException;

    void notifyCModemStatus(int i, IPhoneCallback iPhoneCallback) throws RemoteException;

    void notifyCellularCommParaReady(int i, int i2, Message message) throws RemoteException;

    boolean notifyDeviceState(String str, String str2, String str3) throws RemoteException;

    String[] queryServiceCellBand() throws RemoteException;

    boolean registerCommonImsaToMapconInfo(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean registerForAntiFakeBaseStation(IPhoneCallback iPhoneCallback) throws RemoteException;

    void registerForCallAltSrv(int i, IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean registerForPhoneEvent(int i, IPhoneCallback iPhoneCallback, int i2) throws RemoteException;

    boolean registerForRadioAvailable(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean registerForRadioNotAvailable(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean registerForWirelessState(int i, int i2, IPhoneCallback iPhoneCallback) throws RemoteException;

    void saveNetworkModeToDB(int i, int i2) throws RemoteException;

    boolean sendLaaCmd(int i, String str, Message message) throws RemoteException;

    boolean sendPseudocellCellInfo(int i, int i2, int i3, int i4, String str, int i5) throws RemoteException;

    boolean sendSimMatchedOperatorInfo(int i, String str, String str2, int i2, String str3) throws RemoteException;

    void set2GServiceAbility(int i) throws RemoteException;

    void setCallForwardingOption(int i, int i2, int i3, String str, int i4, Message message) throws RemoteException;

    boolean setCellTxPower(int i) throws RemoteException;

    boolean setCsconEnabled(boolean z) throws RemoteException;

    void setDataEnabledWithoutPromp(boolean z) throws RemoteException;

    void setDataRoamingEnabledWithoutPromp(boolean z) throws RemoteException;

    boolean setDeepNoDisturbState(int i, int i2) throws RemoteException;

    void setDefault4GSlotId(int i, Message message) throws RemoteException;

    void setDefaultDataSlotId(int i) throws RemoteException;

    void setDefaultMobileEnable(boolean z) throws RemoteException;

    boolean setISMCOEX(String str) throws RemoteException;

    void setImsDomainConfig(int i) throws RemoteException;

    void setImsDomainConfigByPhoneId(int i, int i2) throws RemoteException;

    void setImsRegistrationStateForSubId(int i, boolean z) throws RemoteException;

    void setImsSwitch(boolean z) throws RemoteException;

    void setImsSwitchByPhoneId(int i, boolean z) throws RemoteException;

    boolean setLine1Number(int i, String str, String str2, Message message) throws RemoteException;

    void setLteServiceAbility(int i) throws RemoteException;

    void setLteServiceAbilityForSubId(int i, int i2) throws RemoteException;

    boolean setMaxTxPower(int i, int i2) throws RemoteException;

    boolean setPinLockEnabled(boolean z, String str, int i) throws RemoteException;

    void setPreferredNetworkType(int i) throws RemoteException;

    void setServiceAbilityForSubId(int i, int i2, int i3) throws RemoteException;

    boolean setSubscription(int i, boolean z, Message message) throws RemoteException;

    boolean setTemperatureControlToModem(int i, int i2, int i3, Message message) throws RemoteException;

    boolean setUplinkFreqBandwidthReportState(int i, int i2, IPhoneCallback iPhoneCallback) throws RemoteException;

    void setUserPrefDataSlotId(int i) throws RemoteException;

    boolean setWifiTxPower(int i) throws RemoteException;

    boolean unregisterCommonImsaToMapconInfo(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean unregisterForAntiFakeBaseStation() throws RemoteException;

    void unregisterForCallAltSrv(int i) throws RemoteException;

    void unregisterForPhoneEvent(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean unregisterForRadioAvailable(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean unregisterForRadioNotAvailable(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean unregisterForWirelessState(int i, int i2, IPhoneCallback iPhoneCallback) throws RemoteException;

    void updateCrurrentPhone(int i) throws RemoteException;

    void waitingSetDefault4GSlotDone(boolean z) throws RemoteException;
}
