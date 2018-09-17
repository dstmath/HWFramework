package com.android.internal.telephony;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.UiccAuthResponse;

public interface IHwTelephony extends IInterface {

    public static abstract class Stub extends Binder implements IHwTelephony {
        private static final String DESCRIPTOR = "com.android.internal.telephony.IHwTelephony";
        static final int TRANSACTION_changeSimPinCode = 85;
        static final int TRANSACTION_closeRrc = 6;
        static final int TRANSACTION_cmdForECInfo = 79;
        static final int TRANSACTION_get2GServiceAbility = 23;
        static final int TRANSACTION_getCdmaGsmImsi = 39;
        static final int TRANSACTION_getCdmaGsmImsiForSubId = 97;
        static final int TRANSACTION_getCdmaMlplVersion = 42;
        static final int TRANSACTION_getCdmaMsplVersion = 43;
        static final int TRANSACTION_getCellLocation = 41;
        static final int TRANSACTION_getDataStateForSubscriber = 12;
        static final int TRANSACTION_getDefault4GSlotId = 26;
        static final int TRANSACTION_getDemoString = 1;
        static final int TRANSACTION_getImsDomain = 64;
        static final int TRANSACTION_getImsDomainByPhoneId = 74;
        static final int TRANSACTION_getImsImpu = 95;
        static final int TRANSACTION_getImsSwitch = 60;
        static final int TRANSACTION_getImsSwitchByPhoneId = 70;
        static final int TRANSACTION_getLaaDetailedState = 92;
        static final int TRANSACTION_getLteServiceAbility = 14;
        static final int TRANSACTION_getLteServiceAbilityForSubId = 15;
        static final int TRANSACTION_getMeidForSubscriber = 2;
        static final int TRANSACTION_getNVESN = 11;
        static final int TRANSACTION_getOnDemandDataSubId = 37;
        static final int TRANSACTION_getPesnForSubscriber = 3;
        static final int TRANSACTION_getPreferredDataSubscription = 38;
        static final int TRANSACTION_getRcsSwitchState = 89;
        static final int TRANSACTION_getSpecCardType = 29;
        static final int TRANSACTION_getSubState = 4;
        static final int TRANSACTION_getUiccAppType = 63;
        static final int TRANSACTION_getUiccAppTypeByPhoneId = 73;
        static final int TRANSACTION_getUiccCardType = 40;
        static final int TRANSACTION_getUniqueDeviceId = 44;
        static final int TRANSACTION_getWaitingSwitchBalongSlot = 36;
        static final int TRANSACTION_handleMapconImsaReq = 62;
        static final int TRANSACTION_handleMapconImsaReqByPhoneId = 72;
        static final int TRANSACTION_handleUiccAuth = 65;
        static final int TRANSACTION_handleUiccAuthByPhoneId = 75;
        static final int TRANSACTION_isCTCdmaCardInGsmMode = 7;
        static final int TRANSACTION_isCardUimLocked = 30;
        static final int TRANSACTION_isCtSimCard = 80;
        static final int TRANSACTION_isDomesticCard = 48;
        static final int TRANSACTION_isImsRegisteredForSubId = 17;
        static final int TRANSACTION_isLTESupported = 45;
        static final int TRANSACTION_isNeedToRadioPowerOn = 25;
        static final int TRANSACTION_isRadioAvailable = 58;
        static final int TRANSACTION_isRadioAvailableByPhoneId = 68;
        static final int TRANSACTION_isRadioOn = 31;
        static final int TRANSACTION_isSetDefault4GSlotIdEnabled = 34;
        static final int TRANSACTION_isSubDeactivedByPowerOff = 24;
        static final int TRANSACTION_isVideoTelephonyAvailableForSubId = 20;
        static final int TRANSACTION_isVolteAvailableForSubId = 19;
        static final int TRANSACTION_isWifiCallingAvailableForSubId = 18;
        static final int TRANSACTION_notifyCModemStatus = 81;
        static final int TRANSACTION_notifyCellularCommParaReady = 83;
        static final int TRANSACTION_notifyDeviceState = 82;
        static final int TRANSACTION_queryServiceCellBand = 51;
        static final int TRANSACTION_registerCommonImsaToMapconInfo = 56;
        static final int TRANSACTION_registerForCallAltSrv = 93;
        static final int TRANSACTION_registerForPhoneEvent = 66;
        static final int TRANSACTION_registerForRadioAvailable = 52;
        static final int TRANSACTION_registerForRadioNotAvailable = 54;
        static final int TRANSACTION_registerForWirelessState = 76;
        static final int TRANSACTION_sendLaaCmd = 91;
        static final int TRANSACTION_sendPseudocellCellInfo = 86;
        static final int TRANSACTION_set2GServiceAbility = 22;
        static final int TRANSACTION_setCellTxPower = 50;
        static final int TRANSACTION_setDataEnabledWithoutPromp = 9;
        static final int TRANSACTION_setDataRoamingEnabledWithoutPromp = 10;
        static final int TRANSACTION_setDefault4GSlotId = 33;
        static final int TRANSACTION_setDefaultDataSlotId = 27;
        static final int TRANSACTION_setDefaultMobileEnable = 8;
        static final int TRANSACTION_setDmPcscf = 90;
        static final int TRANSACTION_setDmRcsConfig = 87;
        static final int TRANSACTION_setISMCOEX = 47;
        static final int TRANSACTION_setImsDomainConfig = 61;
        static final int TRANSACTION_setImsDomainConfigByPhoneId = 71;
        static final int TRANSACTION_setImsRegistrationStateForSubId = 21;
        static final int TRANSACTION_setImsSwitch = 59;
        static final int TRANSACTION_setImsSwitchByPhoneId = 69;
        static final int TRANSACTION_setLteServiceAbility = 13;
        static final int TRANSACTION_setLteServiceAbilityForSubId = 16;
        static final int TRANSACTION_setMaxTxPower = 78;
        static final int TRANSACTION_setPinLockEnabled = 84;
        static final int TRANSACTION_setPreferredNetworkType = 32;
        static final int TRANSACTION_setRcsSwitch = 88;
        static final int TRANSACTION_setUserPrefDataSlotId = 5;
        static final int TRANSACTION_setWifiTxPower = 49;
        static final int TRANSACTION_testVoiceLoopBack = 46;
        static final int TRANSACTION_unregisterCommonImsaToMapconInfo = 57;
        static final int TRANSACTION_unregisterForCallAltSrv = 94;
        static final int TRANSACTION_unregisterForPhoneEvent = 67;
        static final int TRANSACTION_unregisterForRadioAvailable = 53;
        static final int TRANSACTION_unregisterForRadioNotAvailable = 55;
        static final int TRANSACTION_unregisterForWirelessState = 77;
        static final int TRANSACTION_updateCrurrentPhone = 28;
        static final int TRANSACTION_waitingSetDefault4GSlotDone = 35;
        static final int TRANSACTION_writeSimLockNwData = 96;

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
                    String _result = _reply.readString();
                    return _result;
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
                    String _result = _reply.readString();
                    return _result;
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
                    String _result = _reply.readString();
                    return _result;
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
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(7, _data, _reply, 0);
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

            public void setDefaultMobileEnable(boolean enabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enabled) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDataEnabledWithoutPromp(boolean enabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enabled) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDataRoamingEnabledWithoutPromp(boolean enabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enabled) {
                        i = 1;
                    }
                    _data.writeInt(i);
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
                    String _result = _reply.readString();
                    return _result;
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
                    int _result = _reply.readInt();
                    return _result;
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
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(16, _data, _reply, 0);
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
                    this.mRemote.transact(17, _data, _reply, 0);
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

            public boolean isWifiCallingAvailableForSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(18, _data, _reply, 0);
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

            public boolean isVolteAvailableForSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(19, _data, _reply, 0);
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

            public boolean isVideoTelephonyAvailableForSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(20, _data, _reply, 0);
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

            public void setImsRegistrationStateForSubId(int subId, boolean registered) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (registered) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(21, _data, _reply, 0);
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
                    this.mRemote.transact(22, _data, _reply, 0);
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
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(24, _data, _reply, 0);
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

            public boolean isNeedToRadioPowerOn(long sub) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(sub);
                    this.mRemote.transact(25, _data, _reply, 0);
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

            public int getDefault4GSlotId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(27, _data, _reply, 0);
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
                    this.mRemote.transact(28, _data, _reply, 0);
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
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(30, _data, _reply, 0);
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

            public boolean isRadioOn(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    this.mRemote.transact(31, _data, _reply, 0);
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

            public void setPreferredNetworkType(int nwMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nwMode);
                    this.mRemote.transact(32, _data, _reply, 0);
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
                    this.mRemote.transact(33, _data, _reply, 0);
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
                    this.mRemote.transact(34, _data, _reply, 0);
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

            public void waitingSetDefault4GSlotDone(boolean waiting) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (waiting) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(35, _data, _reply, 0);
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
                    this.mRemote.transact(36, _data, _reply, 0);
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

            public int getOnDemandDataSubId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getOnDemandDataSubId, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getPreferredDataSubscription, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getCdmaGsmImsi, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getCellLocation(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getCdmaMlplVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_isLTESupported, _data, _reply, 0);
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

            public void testVoiceLoopBack(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    this.mRemote.transact(Stub.TRANSACTION_testVoiceLoopBack, _data, _reply, 0);
                    _reply.readException();
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
                    this.mRemote.transact(Stub.TRANSACTION_setISMCOEX, _data, _reply, 0);
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

            public boolean isDomesticCard(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    this.mRemote.transact(48, _data, _reply, 0);
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

            public boolean setWifiTxPower(int power) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(power);
                    this.mRemote.transact(Stub.TRANSACTION_setWifiTxPower, _data, _reply, 0);
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

            public boolean setCellTxPower(int power) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(power);
                    this.mRemote.transact(50, _data, _reply, 0);
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

            public String[] queryServiceCellBand() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_queryServiceCellBand, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerForRadioAvailable(IPhoneCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerForRadioAvailable, _data, _reply, 0);
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

            public boolean unregisterForRadioAvailable(IPhoneCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterForRadioAvailable, _data, _reply, 0);
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

            public boolean registerForRadioNotAvailable(IPhoneCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerForRadioNotAvailable, _data, _reply, 0);
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

            public boolean unregisterForRadioNotAvailable(IPhoneCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterForRadioNotAvailable, _data, _reply, 0);
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

            public boolean registerCommonImsaToMapconInfo(IPhoneCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerCommonImsaToMapconInfo, _data, _reply, 0);
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

            public boolean unregisterCommonImsaToMapconInfo(IPhoneCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterCommonImsaToMapconInfo, _data, _reply, 0);
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

            public boolean isRadioAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isRadioAvailable, _data, _reply, 0);
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

            public void setImsSwitch(boolean value) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (value) {
                        i = 1;
                    }
                    _data.writeInt(i);
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
                    this.mRemote.transact(Stub.TRANSACTION_getImsSwitch, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_handleMapconImsaReq, _data, _reply, 0);
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

            public int getUiccAppType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getUiccAppType, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(64, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public UiccAuthResponse handleUiccAuth(int auth_type, byte[] rand, byte[] auth) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    UiccAuthResponse _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(auth_type);
                    _data.writeByteArray(rand);
                    _data.writeByteArray(auth);
                    this.mRemote.transact(65, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (UiccAuthResponse) UiccAuthResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerForPhoneEvent(int phoneId, IPhoneCallback callback, int events) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(events);
                    this.mRemote.transact(66, _data, _reply, 0);
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

            public void unregisterForPhoneEvent(IPhoneCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(67, _data, _reply, 0);
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
                    this.mRemote.transact(68, _data, _reply, 0);
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

            public void setImsSwitchByPhoneId(int phoneId, boolean value) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (value) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(69, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getImsSwitchByPhoneId, _data, _reply, 0);
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

            public void setImsDomainConfigByPhoneId(int phoneId, int domainType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(domainType);
                    this.mRemote.transact(71, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_handleMapconImsaReqByPhoneId, _data, _reply, 0);
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

            public int getUiccAppTypeByPhoneId(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    this.mRemote.transact(Stub.TRANSACTION_getUiccAppTypeByPhoneId, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public UiccAuthResponse handleUiccAuthByPhoneId(int phoneId, int auth_type, byte[] rand, byte[] auth) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    UiccAuthResponse _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(auth_type);
                    _data.writeByteArray(rand);
                    _data.writeByteArray(auth);
                    this.mRemote.transact(Stub.TRANSACTION_handleUiccAuthByPhoneId, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (UiccAuthResponse) UiccAuthResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerForWirelessState(int type, int slotId, IPhoneCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(slotId);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerForWirelessState, _data, _reply, 0);
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

            public boolean unregisterForWirelessState(int type, int slotId, IPhoneCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(slotId);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterForWirelessState, _data, _reply, 0);
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

            public boolean setMaxTxPower(int type, int power) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(power);
                    this.mRemote.transact(Stub.TRANSACTION_setMaxTxPower, _data, _reply, 0);
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

            public boolean cmdForECInfo(int event, int action, byte[] buf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(event);
                    _data.writeInt(action);
                    _data.writeByteArray(buf);
                    this.mRemote.transact(Stub.TRANSACTION_cmdForECInfo, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
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
                    this.mRemote.transact(80, _data, _reply, 0);
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

            public void notifyCModemStatus(int status, IPhoneCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(81, _data, _reply, 0);
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
                    this.mRemote.transact(82, _data, _reply, 0);
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
                    this.mRemote.transact(83, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setPinLockEnabled(boolean enablePinLock, String password, int subId) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enablePinLock) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeString(password);
                    _data.writeInt(subId);
                    this.mRemote.transact(84, _data, _reply, 0);
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

            public boolean changeSimPinCode(String oldPinCode, String newPinCode, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(oldPinCode);
                    _data.writeString(newPinCode);
                    _data.writeInt(subId);
                    this.mRemote.transact(85, _data, _reply, 0);
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
                    this.mRemote.transact(86, _data, _reply, 0);
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

            public boolean setDmRcsConfig(int subId, int rcsCapability, int devConfig, Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(rcsCapability);
                    _data.writeInt(devConfig);
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(87, _data, _reply, 0);
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

            public boolean setRcsSwitch(int subId, int switchState, Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(switchState);
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setRcsSwitch, _data, _reply, 0);
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

            public boolean getRcsSwitchState(int subId, Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getRcsSwitchState, _data, _reply, 0);
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

            public boolean setDmPcscf(int subId, String pcscf, Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(pcscf);
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(90, _data, _reply, 0);
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

            public boolean sendLaaCmd(int cmd, String reserved, Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cmd);
                    _data.writeString(reserved);
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(91, _data, _reply, 0);
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

            public boolean getLaaDetailedState(String reserved, Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reserved);
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(92, _data, _reply, 0);
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

            public void registerForCallAltSrv(int subId, IPhoneCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(93, _data, _reply, 0);
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
                    this.mRemote.transact(94, _data, _reply, 0);
                    _reply.readException();
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
                    this.mRemote.transact(95, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void writeSimLockNwData(String[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(data);
                    this.mRemote.transact(96, _data, _reply, 0);
                    _reply.readException();
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
                    this.mRemote.transact(97, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
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
            String _result;
            int _result2;
            boolean _result3;
            int _arg0;
            Message _arg1;
            UiccAuthResponse _result4;
            int _arg12;
            Message _arg2;
            String _arg13;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getDemoString();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getMeidForSubscriber(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getPesnForSubscriber(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getSubState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    setUserPrefDataSlotId(data.readInt());
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    closeRrc();
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isCTCdmaCardInGsmMode();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    setDefaultMobileEnable(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    setDataEnabledWithoutPromp(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    setDataRoamingEnabledWithoutPromp(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getNVESN();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDataStateForSubscriber(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    setLteServiceAbility(data.readInt());
                    reply.writeNoException();
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getLteServiceAbility();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getLteServiceAbilityForSubId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    setLteServiceAbilityForSubId(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isImsRegisteredForSubId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isWifiCallingAvailableForSubId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isVolteAvailableForSubId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isVideoTelephonyAvailableForSubId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    setImsRegistrationStateForSubId(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    set2GServiceAbility(data.readInt());
                    reply.writeNoException();
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = get2GServiceAbility();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 24:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isSubDeactivedByPowerOff(data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 25:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isNeedToRadioPowerOn(data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 26:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDefault4GSlotId();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 27:
                    data.enforceInterface(DESCRIPTOR);
                    setDefaultDataSlotId(data.readInt());
                    reply.writeNoException();
                    return true;
                case 28:
                    data.enforceInterface(DESCRIPTOR);
                    updateCrurrentPhone(data.readInt());
                    reply.writeNoException();
                    return true;
                case 29:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getSpecCardType(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 30:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isCardUimLocked(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 31:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isRadioOn(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 32:
                    data.enforceInterface(DESCRIPTOR);
                    setPreferredNetworkType(data.readInt());
                    reply.writeNoException();
                    return true;
                case 33:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg1 = (Message) Message.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    setDefault4GSlotId(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                case 34:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isSetDefault4GSlotIdEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 35:
                    data.enforceInterface(DESCRIPTOR);
                    waitingSetDefault4GSlotDone(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 36:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getWaitingSwitchBalongSlot();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_getOnDemandDataSubId /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getOnDemandDataSubId();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getPreferredDataSubscription /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPreferredDataSubscription();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getCdmaGsmImsi /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getCdmaGsmImsi();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 40:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getUiccCardType(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 41:
                    data.enforceInterface(DESCRIPTOR);
                    Bundle _result5 = getCellLocation(data.readInt());
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(1);
                        _result5.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 42:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getCdmaMlplVersion();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 43:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getCdmaMsplVersion();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_getUniqueDeviceId /*44*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getUniqueDeviceId(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_isLTESupported /*45*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isLTESupported();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_testVoiceLoopBack /*46*/:
                    data.enforceInterface(DESCRIPTOR);
                    testVoiceLoopBack(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setISMCOEX /*47*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setISMCOEX(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 48:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isDomesticCard(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_setWifiTxPower /*49*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setWifiTxPower(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 50:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setCellTxPower(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_queryServiceCellBand /*51*/:
                    data.enforceInterface(DESCRIPTOR);
                    String[] _result6 = queryServiceCellBand();
                    reply.writeNoException();
                    reply.writeStringArray(_result6);
                    return true;
                case TRANSACTION_registerForRadioAvailable /*52*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = registerForRadioAvailable(com.android.internal.telephony.IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_unregisterForRadioAvailable /*53*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = unregisterForRadioAvailable(com.android.internal.telephony.IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_registerForRadioNotAvailable /*54*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = registerForRadioNotAvailable(com.android.internal.telephony.IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_unregisterForRadioNotAvailable /*55*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = unregisterForRadioNotAvailable(com.android.internal.telephony.IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_registerCommonImsaToMapconInfo /*56*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = registerCommonImsaToMapconInfo(com.android.internal.telephony.IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_unregisterCommonImsaToMapconInfo /*57*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = unregisterCommonImsaToMapconInfo(com.android.internal.telephony.IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_isRadioAvailable /*58*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isRadioAvailable();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_setImsSwitch /*59*/:
                    data.enforceInterface(DESCRIPTOR);
                    setImsSwitch(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getImsSwitch /*60*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getImsSwitch();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_setImsDomainConfig /*61*/:
                    data.enforceInterface(DESCRIPTOR);
                    setImsDomainConfig(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_handleMapconImsaReq /*62*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = handleMapconImsaReq(data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_getUiccAppType /*63*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getUiccAppType();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 64:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getImsDomain();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 65:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = handleUiccAuth(data.readInt(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(1);
                        _result4.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 66:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = registerForPhoneEvent(data.readInt(), com.android.internal.telephony.IPhoneCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 67:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterForPhoneEvent(com.android.internal.telephony.IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 68:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isRadioAvailableByPhoneId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 69:
                    data.enforceInterface(DESCRIPTOR);
                    setImsSwitchByPhoneId(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getImsSwitchByPhoneId /*70*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getImsSwitchByPhoneId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 71:
                    data.enforceInterface(DESCRIPTOR);
                    setImsDomainConfigByPhoneId(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_handleMapconImsaReqByPhoneId /*72*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = handleMapconImsaReqByPhoneId(data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_getUiccAppTypeByPhoneId /*73*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getUiccAppTypeByPhoneId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getImsDomainByPhoneId /*74*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getImsDomainByPhoneId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_handleUiccAuthByPhoneId /*75*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = handleUiccAuthByPhoneId(data.readInt(), data.readInt(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(1);
                        _result4.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_registerForWirelessState /*76*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = registerForWirelessState(data.readInt(), data.readInt(), com.android.internal.telephony.IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_unregisterForWirelessState /*77*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = unregisterForWirelessState(data.readInt(), data.readInt(), com.android.internal.telephony.IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_setMaxTxPower /*78*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setMaxTxPower(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_cmdForECInfo /*79*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg12 = data.readInt();
                    byte[] _arg22 = data.createByteArray();
                    _result3 = cmdForECInfo(_arg0, _arg12, _arg22);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    reply.writeByteArray(_arg22);
                    return true;
                case 80:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isCtSimCard(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 81:
                    data.enforceInterface(DESCRIPTOR);
                    notifyCModemStatus(data.readInt(), com.android.internal.telephony.IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 82:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = notifyDeviceState(data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 83:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg12 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (Message) Message.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    notifyCellularCommParaReady(_arg0, _arg12, _arg2);
                    reply.writeNoException();
                    return true;
                case 84:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setPinLockEnabled(data.readInt() != 0, data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 85:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = changeSimPinCode(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 86:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = sendPseudocellCellInfo(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 87:
                    Message _arg3;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg12 = data.readInt();
                    int _arg23 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg3 = (Message) Message.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    _result3 = setDmRcsConfig(_arg0, _arg12, _arg23, _arg3);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_setRcsSwitch /*88*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg12 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (Message) Message.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    _result3 = setRcsSwitch(_arg0, _arg12, _arg2);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case TRANSACTION_getRcsSwitchState /*89*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg1 = (Message) Message.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    _result3 = getRcsSwitchState(_arg0, _arg1);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 90:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg13 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = (Message) Message.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    _result3 = setDmPcscf(_arg0, _arg13, _arg2);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 91:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg13 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = (Message) Message.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    _result3 = sendLaaCmd(_arg0, _arg13, _arg2);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 92:
                    data.enforceInterface(DESCRIPTOR);
                    String _arg02 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Message) Message.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    _result3 = getLaaDetailedState(_arg02, _arg1);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 93:
                    data.enforceInterface(DESCRIPTOR);
                    registerForCallAltSrv(data.readInt(), com.android.internal.telephony.IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 94:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterForCallAltSrv(data.readInt());
                    reply.writeNoException();
                    return true;
                case 95:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getImsImpu(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 96:
                    data.enforceInterface(DESCRIPTOR);
                    writeSimLockNwData(data.createStringArray());
                    reply.writeNoException();
                    return true;
                case 97:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getCdmaGsmImsiForSubId(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean changeSimPinCode(String str, String str2, int i) throws RemoteException;

    void closeRrc() throws RemoteException;

    boolean cmdForECInfo(int i, int i2, byte[] bArr) throws RemoteException;

    int get2GServiceAbility() throws RemoteException;

    String getCdmaGsmImsi() throws RemoteException;

    String getCdmaGsmImsiForSubId(int i) throws RemoteException;

    String getCdmaMlplVersion() throws RemoteException;

    String getCdmaMsplVersion() throws RemoteException;

    Bundle getCellLocation(int i) throws RemoteException;

    int getDataStateForSubscriber(int i) throws RemoteException;

    int getDefault4GSlotId() throws RemoteException;

    String getDemoString() throws RemoteException;

    int getImsDomain() throws RemoteException;

    int getImsDomainByPhoneId(int i) throws RemoteException;

    String getImsImpu(int i) throws RemoteException;

    boolean getImsSwitch() throws RemoteException;

    boolean getImsSwitchByPhoneId(int i) throws RemoteException;

    boolean getLaaDetailedState(String str, Message message) throws RemoteException;

    int getLteServiceAbility() throws RemoteException;

    int getLteServiceAbilityForSubId(int i) throws RemoteException;

    String getMeidForSubscriber(int i) throws RemoteException;

    String getNVESN() throws RemoteException;

    int getOnDemandDataSubId() throws RemoteException;

    String getPesnForSubscriber(int i) throws RemoteException;

    int getPreferredDataSubscription() throws RemoteException;

    boolean getRcsSwitchState(int i, Message message) throws RemoteException;

    int getSpecCardType(int i) throws RemoteException;

    int getSubState(int i) throws RemoteException;

    int getUiccAppType() throws RemoteException;

    int getUiccAppTypeByPhoneId(int i) throws RemoteException;

    int getUiccCardType(int i) throws RemoteException;

    String getUniqueDeviceId(int i) throws RemoteException;

    boolean getWaitingSwitchBalongSlot() throws RemoteException;

    boolean handleMapconImsaReq(byte[] bArr) throws RemoteException;

    boolean handleMapconImsaReqByPhoneId(int i, byte[] bArr) throws RemoteException;

    UiccAuthResponse handleUiccAuth(int i, byte[] bArr, byte[] bArr2) throws RemoteException;

    UiccAuthResponse handleUiccAuthByPhoneId(int i, int i2, byte[] bArr, byte[] bArr2) throws RemoteException;

    boolean isCTCdmaCardInGsmMode() throws RemoteException;

    boolean isCardUimLocked(int i) throws RemoteException;

    boolean isCtSimCard(int i) throws RemoteException;

    boolean isDomesticCard(int i) throws RemoteException;

    boolean isImsRegisteredForSubId(int i) throws RemoteException;

    boolean isLTESupported() throws RemoteException;

    boolean isNeedToRadioPowerOn(long j) throws RemoteException;

    boolean isRadioAvailable() throws RemoteException;

    boolean isRadioAvailableByPhoneId(int i) throws RemoteException;

    boolean isRadioOn(int i) throws RemoteException;

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

    void registerForCallAltSrv(int i, IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean registerForPhoneEvent(int i, IPhoneCallback iPhoneCallback, int i2) throws RemoteException;

    boolean registerForRadioAvailable(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean registerForRadioNotAvailable(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean registerForWirelessState(int i, int i2, IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean sendLaaCmd(int i, String str, Message message) throws RemoteException;

    boolean sendPseudocellCellInfo(int i, int i2, int i3, int i4, String str, int i5) throws RemoteException;

    void set2GServiceAbility(int i) throws RemoteException;

    boolean setCellTxPower(int i) throws RemoteException;

    void setDataEnabledWithoutPromp(boolean z) throws RemoteException;

    void setDataRoamingEnabledWithoutPromp(boolean z) throws RemoteException;

    void setDefault4GSlotId(int i, Message message) throws RemoteException;

    void setDefaultDataSlotId(int i) throws RemoteException;

    void setDefaultMobileEnable(boolean z) throws RemoteException;

    boolean setDmPcscf(int i, String str, Message message) throws RemoteException;

    boolean setDmRcsConfig(int i, int i2, int i3, Message message) throws RemoteException;

    boolean setISMCOEX(String str) throws RemoteException;

    void setImsDomainConfig(int i) throws RemoteException;

    void setImsDomainConfigByPhoneId(int i, int i2) throws RemoteException;

    void setImsRegistrationStateForSubId(int i, boolean z) throws RemoteException;

    void setImsSwitch(boolean z) throws RemoteException;

    void setImsSwitchByPhoneId(int i, boolean z) throws RemoteException;

    void setLteServiceAbility(int i) throws RemoteException;

    void setLteServiceAbilityForSubId(int i, int i2) throws RemoteException;

    boolean setMaxTxPower(int i, int i2) throws RemoteException;

    boolean setPinLockEnabled(boolean z, String str, int i) throws RemoteException;

    void setPreferredNetworkType(int i) throws RemoteException;

    boolean setRcsSwitch(int i, int i2, Message message) throws RemoteException;

    void setUserPrefDataSlotId(int i) throws RemoteException;

    boolean setWifiTxPower(int i) throws RemoteException;

    void testVoiceLoopBack(int i) throws RemoteException;

    boolean unregisterCommonImsaToMapconInfo(IPhoneCallback iPhoneCallback) throws RemoteException;

    void unregisterForCallAltSrv(int i) throws RemoteException;

    void unregisterForPhoneEvent(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean unregisterForRadioAvailable(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean unregisterForRadioNotAvailable(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean unregisterForWirelessState(int i, int i2, IPhoneCallback iPhoneCallback) throws RemoteException;

    void updateCrurrentPhone(int i) throws RemoteException;

    void waitingSetDefault4GSlotDone(boolean z) throws RemoteException;

    void writeSimLockNwData(String[] strArr) throws RemoteException;
}
