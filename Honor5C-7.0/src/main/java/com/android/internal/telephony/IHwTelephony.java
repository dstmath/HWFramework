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
        static final int TRANSACTION_closeRrc = 6;
        static final int TRANSACTION_cmdForECInfo = 59;
        static final int TRANSACTION_getCdmaGsmImsi = 29;
        static final int TRANSACTION_getCdmaMlplVersion = 32;
        static final int TRANSACTION_getCdmaMsplVersion = 33;
        static final int TRANSACTION_getCellLocation = 31;
        static final int TRANSACTION_getDataStateForSubscriber = 11;
        static final int TRANSACTION_getDefault4GSlotId = 16;
        static final int TRANSACTION_getDemoString = 1;
        static final int TRANSACTION_getImsDomain = 54;
        static final int TRANSACTION_getImsSwitch = 50;
        static final int TRANSACTION_getLteServiceAbility = 13;
        static final int TRANSACTION_getMeidForSubscriber = 2;
        static final int TRANSACTION_getNVESN = 10;
        static final int TRANSACTION_getOnDemandDataSubId = 27;
        static final int TRANSACTION_getPesnForSubscriber = 3;
        static final int TRANSACTION_getPreferredDataSubscription = 28;
        static final int TRANSACTION_getSpecCardType = 19;
        static final int TRANSACTION_getSubState = 4;
        static final int TRANSACTION_getUiccAppType = 53;
        static final int TRANSACTION_getUiccCardType = 30;
        static final int TRANSACTION_getUniqueDeviceId = 34;
        static final int TRANSACTION_getWaitingSwitchBalongSlot = 26;
        static final int TRANSACTION_handleMapconImsaReq = 52;
        static final int TRANSACTION_handleUiccAuth = 55;
        static final int TRANSACTION_isCTCdmaCardInGsmMode = 7;
        static final int TRANSACTION_isCardUimLocked = 20;
        static final int TRANSACTION_isDomesticCard = 38;
        static final int TRANSACTION_isLTESupported = 35;
        static final int TRANSACTION_isNeedToRadioPowerOn = 15;
        static final int TRANSACTION_isRadioAvailable = 48;
        static final int TRANSACTION_isRadioOn = 21;
        static final int TRANSACTION_isSetDefault4GSlotIdEnabled = 24;
        static final int TRANSACTION_isSubDeactivedByPowerOff = 14;
        static final int TRANSACTION_queryServiceCellBand = 41;
        static final int TRANSACTION_registerCommonImsaToMapconInfo = 46;
        static final int TRANSACTION_registerForRadioAvailable = 42;
        static final int TRANSACTION_registerForRadioNotAvailable = 44;
        static final int TRANSACTION_registerForWirelessState = 56;
        static final int TRANSACTION_setCellTxPower = 40;
        static final int TRANSACTION_setDataEnabledWithoutPromp = 9;
        static final int TRANSACTION_setDefault4GSlotId = 23;
        static final int TRANSACTION_setDefaultDataSlotId = 17;
        static final int TRANSACTION_setDefaultMobileEnable = 8;
        static final int TRANSACTION_setISMCOEX = 37;
        static final int TRANSACTION_setImsDomainConfig = 51;
        static final int TRANSACTION_setImsSwitch = 49;
        static final int TRANSACTION_setLteServiceAbility = 12;
        static final int TRANSACTION_setMaxTxPower = 58;
        static final int TRANSACTION_setPreferredNetworkType = 22;
        static final int TRANSACTION_setUserPrefDataSlotId = 5;
        static final int TRANSACTION_setWifiTxPower = 39;
        static final int TRANSACTION_testVoiceLoopBack = 36;
        static final int TRANSACTION_unregisterCommonImsaToMapconInfo = 47;
        static final int TRANSACTION_unregisterForRadioAvailable = 43;
        static final int TRANSACTION_unregisterForRadioNotAvailable = 45;
        static final int TRANSACTION_unregisterForWirelessState = 57;
        static final int TRANSACTION_updateCrurrentPhone = 18;
        static final int TRANSACTION_waitingSetDefault4GSlotDone = 25;

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
                    this.mRemote.transact(Stub.TRANSACTION_getDemoString, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getMeidForSubscriber, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getPesnForSubscriber, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getSubState, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_setUserPrefDataSlotId, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_closeRrc, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isCTCdmaCardInGsmMode, _data, _reply, 0);
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
                        i = Stub.TRANSACTION_getDemoString;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setDefaultMobileEnable, _data, _reply, 0);
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
                        i = Stub.TRANSACTION_getDemoString;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setDataEnabledWithoutPromp, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getNVESN, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getDataStateForSubscriber, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_setLteServiceAbility, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getLteServiceAbility, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isSubDeactivedByPowerOff, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isNeedToRadioPowerOn, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getDefault4GSlotId, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_setDefaultDataSlotId, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_updateCrurrentPhone, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getSpecCardType, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isCardUimLocked, _data, _reply, 0);
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

            public void setPreferredNetworkType(int nwMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nwMode);
                    this.mRemote.transact(Stub.TRANSACTION_setPreferredNetworkType, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getDemoString);
                        msg.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setDefault4GSlotId, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isSetDefault4GSlotIdEnabled, _data, _reply, 0);
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
                        i = Stub.TRANSACTION_getDemoString;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_waitingSetDefault4GSlotDone, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getWaitingSwitchBalongSlot, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getUiccCardType, _data, _reply, 0);
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
                    Bundle bundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
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

            public String getCdmaMlplVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCdmaMlplVersion, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getCdmaMsplVersion, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isDomesticCard, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_setCellTxPower, _data, _reply, 0);
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
                        i = Stub.TRANSACTION_getDemoString;
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
                    this.mRemote.transact(Stub.TRANSACTION_getImsDomain, _data, _reply, 0);
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
                    UiccAuthResponse uiccAuthResponse;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(auth_type);
                    _data.writeByteArray(rand);
                    _data.writeByteArray(auth);
                    this.mRemote.transact(Stub.TRANSACTION_handleUiccAuth, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        uiccAuthResponse = (UiccAuthResponse) UiccAuthResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        uiccAuthResponse = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return uiccAuthResponse;
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
            switch (code) {
                case TRANSACTION_getDemoString /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getDemoString();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_getMeidForSubscriber /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getMeidForSubscriber(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_getPesnForSubscriber /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getPesnForSubscriber(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_getSubState /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getSubState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setUserPrefDataSlotId /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    setUserPrefDataSlotId(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_closeRrc /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    closeRrc();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isCTCdmaCardInGsmMode /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isCTCdmaCardInGsmMode();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_setDefaultMobileEnable /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDefaultMobileEnable(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setDataEnabledWithoutPromp /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDataEnabledWithoutPromp(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getNVESN /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getNVESN();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_getDataStateForSubscriber /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDataStateForSubscriber(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setLteServiceAbility /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    setLteServiceAbility(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getLteServiceAbility /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getLteServiceAbility();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_isSubDeactivedByPowerOff /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isSubDeactivedByPowerOff(data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_isNeedToRadioPowerOn /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isNeedToRadioPowerOn(data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_getDefault4GSlotId /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDefault4GSlotId();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setDefaultDataSlotId /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDefaultDataSlotId(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_updateCrurrentPhone /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    updateCrurrentPhone(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getSpecCardType /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getSpecCardType(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_isCardUimLocked /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isCardUimLocked(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_isRadioOn /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isRadioOn(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_setPreferredNetworkType /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    setPreferredNetworkType(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setDefault4GSlotId /*23*/:
                    Message message;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        message = (Message) Message.CREATOR.createFromParcel(data);
                    } else {
                        message = null;
                    }
                    setDefault4GSlotId(_arg0, message);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isSetDefault4GSlotIdEnabled /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isSetDefault4GSlotIdEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_waitingSetDefault4GSlotDone /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    waitingSetDefault4GSlotDone(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getWaitingSwitchBalongSlot /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getWaitingSwitchBalongSlot();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_getOnDemandDataSubId /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getOnDemandDataSubId();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getPreferredDataSubscription /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPreferredDataSubscription();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getCdmaGsmImsi /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getCdmaGsmImsi();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_getUiccCardType /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getUiccCardType(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getCellLocation /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    Bundle _result4 = getCellLocation(data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_getDemoString);
                        _result4.writeToParcel(reply, TRANSACTION_getDemoString);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getCdmaMlplVersion /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getCdmaMlplVersion();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_getCdmaMsplVersion /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getCdmaMsplVersion();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_getUniqueDeviceId /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getUniqueDeviceId(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_isLTESupported /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isLTESupported();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_testVoiceLoopBack /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    testVoiceLoopBack(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setISMCOEX /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setISMCOEX(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_isDomesticCard /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isDomesticCard(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_setWifiTxPower /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setWifiTxPower(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_setCellTxPower /*40*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setCellTxPower(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_queryServiceCellBand /*41*/:
                    data.enforceInterface(DESCRIPTOR);
                    String[] _result5 = queryServiceCellBand();
                    reply.writeNoException();
                    reply.writeStringArray(_result5);
                    return true;
                case TRANSACTION_registerForRadioAvailable /*42*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = registerForRadioAvailable(com.android.internal.telephony.IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_unregisterForRadioAvailable /*43*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = unregisterForRadioAvailable(com.android.internal.telephony.IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_registerForRadioNotAvailable /*44*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = registerForRadioNotAvailable(com.android.internal.telephony.IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_unregisterForRadioNotAvailable /*45*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = unregisterForRadioNotAvailable(com.android.internal.telephony.IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_registerCommonImsaToMapconInfo /*46*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = registerCommonImsaToMapconInfo(com.android.internal.telephony.IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_unregisterCommonImsaToMapconInfo /*47*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = unregisterCommonImsaToMapconInfo(com.android.internal.telephony.IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_isRadioAvailable /*48*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isRadioAvailable();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_setImsSwitch /*49*/:
                    data.enforceInterface(DESCRIPTOR);
                    setImsSwitch(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getImsSwitch /*50*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getImsSwitch();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_setImsDomainConfig /*51*/:
                    data.enforceInterface(DESCRIPTOR);
                    setImsDomainConfig(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_handleMapconImsaReq /*52*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = handleMapconImsaReq(data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_getUiccAppType /*53*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getUiccAppType();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getImsDomain /*54*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getImsDomain();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_handleUiccAuth /*55*/:
                    data.enforceInterface(DESCRIPTOR);
                    UiccAuthResponse _result6 = handleUiccAuth(data.readInt(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(TRANSACTION_getDemoString);
                        _result6.writeToParcel(reply, TRANSACTION_getDemoString);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_registerForWirelessState /*56*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = registerForWirelessState(data.readInt(), data.readInt(), com.android.internal.telephony.IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_unregisterForWirelessState /*57*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = unregisterForWirelessState(data.readInt(), data.readInt(), com.android.internal.telephony.IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_setMaxTxPower /*58*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setMaxTxPower(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    return true;
                case TRANSACTION_cmdForECInfo /*59*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    int _arg1 = data.readInt();
                    byte[] _arg2 = data.createByteArray();
                    _result3 = cmdForECInfo(_arg0, _arg1, _arg2);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getDemoString : 0);
                    reply.writeByteArray(_arg2);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void closeRrc() throws RemoteException;

    boolean cmdForECInfo(int i, int i2, byte[] bArr) throws RemoteException;

    String getCdmaGsmImsi() throws RemoteException;

    String getCdmaMlplVersion() throws RemoteException;

    String getCdmaMsplVersion() throws RemoteException;

    Bundle getCellLocation(int i) throws RemoteException;

    int getDataStateForSubscriber(int i) throws RemoteException;

    int getDefault4GSlotId() throws RemoteException;

    String getDemoString() throws RemoteException;

    int getImsDomain() throws RemoteException;

    boolean getImsSwitch() throws RemoteException;

    int getLteServiceAbility() throws RemoteException;

    String getMeidForSubscriber(int i) throws RemoteException;

    String getNVESN() throws RemoteException;

    int getOnDemandDataSubId() throws RemoteException;

    String getPesnForSubscriber(int i) throws RemoteException;

    int getPreferredDataSubscription() throws RemoteException;

    int getSpecCardType(int i) throws RemoteException;

    int getSubState(int i) throws RemoteException;

    int getUiccAppType() throws RemoteException;

    int getUiccCardType(int i) throws RemoteException;

    String getUniqueDeviceId(int i) throws RemoteException;

    boolean getWaitingSwitchBalongSlot() throws RemoteException;

    boolean handleMapconImsaReq(byte[] bArr) throws RemoteException;

    UiccAuthResponse handleUiccAuth(int i, byte[] bArr, byte[] bArr2) throws RemoteException;

    boolean isCTCdmaCardInGsmMode() throws RemoteException;

    boolean isCardUimLocked(int i) throws RemoteException;

    boolean isDomesticCard(int i) throws RemoteException;

    boolean isLTESupported() throws RemoteException;

    boolean isNeedToRadioPowerOn(long j) throws RemoteException;

    boolean isRadioAvailable() throws RemoteException;

    boolean isRadioOn(int i) throws RemoteException;

    boolean isSetDefault4GSlotIdEnabled() throws RemoteException;

    boolean isSubDeactivedByPowerOff(long j) throws RemoteException;

    String[] queryServiceCellBand() throws RemoteException;

    boolean registerCommonImsaToMapconInfo(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean registerForRadioAvailable(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean registerForRadioNotAvailable(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean registerForWirelessState(int i, int i2, IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean setCellTxPower(int i) throws RemoteException;

    void setDataEnabledWithoutPromp(boolean z) throws RemoteException;

    void setDefault4GSlotId(int i, Message message) throws RemoteException;

    void setDefaultDataSlotId(int i) throws RemoteException;

    void setDefaultMobileEnable(boolean z) throws RemoteException;

    boolean setISMCOEX(String str) throws RemoteException;

    void setImsDomainConfig(int i) throws RemoteException;

    void setImsSwitch(boolean z) throws RemoteException;

    void setLteServiceAbility(int i) throws RemoteException;

    boolean setMaxTxPower(int i, int i2) throws RemoteException;

    void setPreferredNetworkType(int i) throws RemoteException;

    void setUserPrefDataSlotId(int i) throws RemoteException;

    boolean setWifiTxPower(int i) throws RemoteException;

    void testVoiceLoopBack(int i) throws RemoteException;

    boolean unregisterCommonImsaToMapconInfo(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean unregisterForRadioAvailable(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean unregisterForRadioNotAvailable(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean unregisterForWirelessState(int i, int i2, IPhoneCallback iPhoneCallback) throws RemoteException;

    void updateCrurrentPhone(int i) throws RemoteException;

    void waitingSetDefault4GSlotDone(boolean z) throws RemoteException;
}
