package com.android.internal.telephony;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.SubscriptionInfo;
import java.util.List;

public interface ISub extends IInterface {

    public static abstract class Stub extends Binder implements ISub {
        private static final String DESCRIPTOR = "com.android.internal.telephony.ISub";
        static final int TRANSACTION_addSubInfoRecord = 9;
        static final int TRANSACTION_clearDefaultsForInactiveSubIds = 27;
        static final int TRANSACTION_clearSubInfo = 19;
        static final int TRANSACTION_getActiveSubIdList = 28;
        static final int TRANSACTION_getActiveSubInfoCount = 7;
        static final int TRANSACTION_getActiveSubInfoCountMax = 8;
        static final int TRANSACTION_getActiveSubscriptionInfo = 3;
        static final int TRANSACTION_getActiveSubscriptionInfoForIccId = 4;
        static final int TRANSACTION_getActiveSubscriptionInfoForSimSlotIndex = 5;
        static final int TRANSACTION_getActiveSubscriptionInfoList = 6;
        static final int TRANSACTION_getAllSubInfoCount = 2;
        static final int TRANSACTION_getAllSubInfoList = 1;
        static final int TRANSACTION_getDefaultDataSubId = 21;
        static final int TRANSACTION_getDefaultSmsSubId = 25;
        static final int TRANSACTION_getDefaultSubId = 18;
        static final int TRANSACTION_getDefaultVoiceSubId = 23;
        static final int TRANSACTION_getPhoneId = 20;
        static final int TRANSACTION_getSimStateForSlotIdx = 31;
        static final int TRANSACTION_getSlotId = 16;
        static final int TRANSACTION_getSubId = 17;
        static final int TRANSACTION_getSubscriptionProperty = 30;
        static final int TRANSACTION_isActiveSubId = 32;
        static final int TRANSACTION_setDataRoaming = 15;
        static final int TRANSACTION_setDefaultDataSubId = 22;
        static final int TRANSACTION_setDefaultSmsSubId = 26;
        static final int TRANSACTION_setDefaultVoiceSubId = 24;
        static final int TRANSACTION_setDisplayName = 11;
        static final int TRANSACTION_setDisplayNameUsingSrc = 13;
        static final int TRANSACTION_setDisplayNumber = 14;
        static final int TRANSACTION_setIconTint = 10;
        static final int TRANSACTION_setSimProvisioningStatus = 12;
        static final int TRANSACTION_setSubscriptionProperty = 29;

        private static class Proxy implements ISub {
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

            public List<SubscriptionInfo> getAllSubInfoList(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getAllSubInfoList, _data, _reply, 0);
                    _reply.readException();
                    List<SubscriptionInfo> _result = _reply.createTypedArrayList(SubscriptionInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAllSubInfoCount(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getAllSubInfoCount, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public SubscriptionInfo getActiveSubscriptionInfo(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    SubscriptionInfo subscriptionInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getActiveSubscriptionInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        subscriptionInfo = (SubscriptionInfo) SubscriptionInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        subscriptionInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return subscriptionInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public SubscriptionInfo getActiveSubscriptionInfoForIccId(String iccId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    SubscriptionInfo subscriptionInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iccId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getActiveSubscriptionInfoForIccId, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        subscriptionInfo = (SubscriptionInfo) SubscriptionInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        subscriptionInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return subscriptionInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public SubscriptionInfo getActiveSubscriptionInfoForSimSlotIndex(int slotIdx, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    SubscriptionInfo subscriptionInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIdx);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getActiveSubscriptionInfoForSimSlotIndex, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        subscriptionInfo = (SubscriptionInfo) SubscriptionInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        subscriptionInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return subscriptionInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<SubscriptionInfo> getActiveSubscriptionInfoList(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getActiveSubscriptionInfoList, _data, _reply, 0);
                    _reply.readException();
                    List<SubscriptionInfo> _result = _reply.createTypedArrayList(SubscriptionInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getActiveSubInfoCount(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getActiveSubInfoCount, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getActiveSubInfoCountMax() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getActiveSubInfoCountMax, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addSubInfoRecord(String iccId, int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iccId);
                    _data.writeInt(slotId);
                    this.mRemote.transact(Stub.TRANSACTION_addSubInfoRecord, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setIconTint(int tint, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(tint);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_setIconTint, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setDisplayName(String displayName, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(displayName);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_setDisplayName, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setSimProvisioningStatus(int simProvisioningStatus, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(simProvisioningStatus);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_setSimProvisioningStatus, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setDisplayNameUsingSrc(String displayName, int subId, long nameSource) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(displayName);
                    _data.writeInt(subId);
                    _data.writeLong(nameSource);
                    this.mRemote.transact(Stub.TRANSACTION_setDisplayNameUsingSrc, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setDisplayNumber(String number, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(number);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_setDisplayNumber, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setDataRoaming(int roaming, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(roaming);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_setDataRoaming, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSlotId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getSlotId, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getSubId(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    this.mRemote.transact(Stub.TRANSACTION_getSubId, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDefaultSubId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getDefaultSubId, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int clearSubInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_clearSubInfo, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPhoneId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getPhoneId, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDefaultDataSubId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getDefaultDataSubId, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDefaultDataSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_setDefaultDataSubId, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDefaultVoiceSubId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getDefaultVoiceSubId, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDefaultVoiceSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_setDefaultVoiceSubId, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDefaultSmsSubId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getDefaultSmsSubId, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDefaultSmsSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_setDefaultSmsSubId, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearDefaultsForInactiveSubIds() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_clearDefaultsForInactiveSubIds, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getActiveSubIdList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getActiveSubIdList, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSubscriptionProperty(int subId, String propKey, String propValue) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(propKey);
                    _data.writeString(propValue);
                    this.mRemote.transact(Stub.TRANSACTION_setSubscriptionProperty, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getSubscriptionProperty(int subId, String propKey, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(propKey);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getSubscriptionProperty, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSimStateForSlotIdx(int slotIdx) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIdx);
                    this.mRemote.transact(Stub.TRANSACTION_getSimStateForSlotIdx, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isActiveSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_isActiveSubId, _data, _reply, 0);
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISub asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISub)) {
                return new Proxy(obj);
            }
            return (ISub) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            List<SubscriptionInfo> _result;
            int _result2;
            SubscriptionInfo _result3;
            int[] _result4;
            switch (code) {
                case TRANSACTION_getAllSubInfoList /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAllSubInfoList(data.readString());
                    reply.writeNoException();
                    reply.writeTypedList(_result);
                    return true;
                case TRANSACTION_getAllSubInfoCount /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAllSubInfoCount(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getActiveSubscriptionInfo /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getActiveSubscriptionInfo(data.readInt(), data.readString());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_getAllSubInfoList);
                        _result3.writeToParcel(reply, TRANSACTION_getAllSubInfoList);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getActiveSubscriptionInfoForIccId /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getActiveSubscriptionInfoForIccId(data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_getAllSubInfoList);
                        _result3.writeToParcel(reply, TRANSACTION_getAllSubInfoList);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getActiveSubscriptionInfoForSimSlotIndex /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getActiveSubscriptionInfoForSimSlotIndex(data.readInt(), data.readString());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_getAllSubInfoList);
                        _result3.writeToParcel(reply, TRANSACTION_getAllSubInfoList);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getActiveSubscriptionInfoList /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getActiveSubscriptionInfoList(data.readString());
                    reply.writeNoException();
                    reply.writeTypedList(_result);
                    return true;
                case TRANSACTION_getActiveSubInfoCount /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getActiveSubInfoCount(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getActiveSubInfoCountMax /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getActiveSubInfoCountMax();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_addSubInfoRecord /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = addSubInfoRecord(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setIconTint /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setIconTint(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setDisplayName /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setDisplayName(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setSimProvisioningStatus /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setSimProvisioningStatus(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setDisplayNameUsingSrc /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setDisplayNameUsingSrc(data.readString(), data.readInt(), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setDisplayNumber /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setDisplayNumber(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setDataRoaming /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setDataRoaming(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getSlotId /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getSlotId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getSubId /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSubId(data.readInt());
                    reply.writeNoException();
                    reply.writeIntArray(_result4);
                    return true;
                case TRANSACTION_getDefaultSubId /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDefaultSubId();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_clearSubInfo /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = clearSubInfo();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getPhoneId /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPhoneId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getDefaultDataSubId /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDefaultDataSubId();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setDefaultDataSubId /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDefaultDataSubId(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getDefaultVoiceSubId /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDefaultVoiceSubId();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setDefaultVoiceSubId /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDefaultVoiceSubId(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getDefaultSmsSubId /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDefaultSmsSubId();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setDefaultSmsSubId /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDefaultSmsSubId(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearDefaultsForInactiveSubIds /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearDefaultsForInactiveSubIds();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getActiveSubIdList /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getActiveSubIdList();
                    reply.writeNoException();
                    reply.writeIntArray(_result4);
                    return true;
                case TRANSACTION_setSubscriptionProperty /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    setSubscriptionProperty(data.readInt(), data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getSubscriptionProperty /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    String _result5 = getSubscriptionProperty(data.readInt(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case TRANSACTION_getSimStateForSlotIdx /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getSimStateForSlotIdx(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_isActiveSubId /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result6 = isActiveSubId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result6 ? TRANSACTION_getAllSubInfoList : 0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int addSubInfoRecord(String str, int i) throws RemoteException;

    void clearDefaultsForInactiveSubIds() throws RemoteException;

    int clearSubInfo() throws RemoteException;

    int[] getActiveSubIdList() throws RemoteException;

    int getActiveSubInfoCount(String str) throws RemoteException;

    int getActiveSubInfoCountMax() throws RemoteException;

    SubscriptionInfo getActiveSubscriptionInfo(int i, String str) throws RemoteException;

    SubscriptionInfo getActiveSubscriptionInfoForIccId(String str, String str2) throws RemoteException;

    SubscriptionInfo getActiveSubscriptionInfoForSimSlotIndex(int i, String str) throws RemoteException;

    List<SubscriptionInfo> getActiveSubscriptionInfoList(String str) throws RemoteException;

    int getAllSubInfoCount(String str) throws RemoteException;

    List<SubscriptionInfo> getAllSubInfoList(String str) throws RemoteException;

    int getDefaultDataSubId() throws RemoteException;

    int getDefaultSmsSubId() throws RemoteException;

    int getDefaultSubId() throws RemoteException;

    int getDefaultVoiceSubId() throws RemoteException;

    int getPhoneId(int i) throws RemoteException;

    int getSimStateForSlotIdx(int i) throws RemoteException;

    int getSlotId(int i) throws RemoteException;

    int[] getSubId(int i) throws RemoteException;

    String getSubscriptionProperty(int i, String str, String str2) throws RemoteException;

    boolean isActiveSubId(int i) throws RemoteException;

    int setDataRoaming(int i, int i2) throws RemoteException;

    void setDefaultDataSubId(int i) throws RemoteException;

    void setDefaultSmsSubId(int i) throws RemoteException;

    void setDefaultVoiceSubId(int i) throws RemoteException;

    int setDisplayName(String str, int i) throws RemoteException;

    int setDisplayNameUsingSrc(String str, int i, long j) throws RemoteException;

    int setDisplayNumber(String str, int i) throws RemoteException;

    int setIconTint(int i, int i2) throws RemoteException;

    int setSimProvisioningStatus(int i, int i2) throws RemoteException;

    void setSubscriptionProperty(int i, String str, String str2) throws RemoteException;
}
