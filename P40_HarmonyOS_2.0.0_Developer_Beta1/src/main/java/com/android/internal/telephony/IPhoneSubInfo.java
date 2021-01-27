package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.ImsiEncryptionInfo;

public interface IPhoneSubInfo extends IInterface {
    ImsiEncryptionInfo getCarrierInfoForImsiEncryption(int i, int i2, String str) throws RemoteException;

    String getCompleteVoiceMailNumber() throws RemoteException;

    String getCompleteVoiceMailNumberForSubscriber(int i) throws RemoteException;

    String getDeviceId(String str) throws RemoteException;

    String getDeviceIdForPhone(int i, String str) throws RemoteException;

    String getDeviceSvn(String str) throws RemoteException;

    String getDeviceSvnUsingSubId(int i, String str) throws RemoteException;

    String getGroupIdLevel1ForSubscriber(int i, String str) throws RemoteException;

    @UnsupportedAppUsage
    String getIccSerialNumber(String str) throws RemoteException;

    String getIccSerialNumberForSubscriber(int i, String str) throws RemoteException;

    String getIccSimChallengeResponse(int i, int i2, int i3, String str) throws RemoteException;

    String getImeiForSubscriber(int i, String str) throws RemoteException;

    String getIsimDomain(int i) throws RemoteException;

    String getIsimImpi(int i) throws RemoteException;

    String[] getIsimImpu(int i) throws RemoteException;

    String getIsimIst(int i) throws RemoteException;

    String[] getIsimPcscf(int i) throws RemoteException;

    String getLine1AlphaTag(String str) throws RemoteException;

    String getLine1AlphaTagForSubscriber(int i, String str) throws RemoteException;

    String getLine1Number(String str) throws RemoteException;

    String getLine1NumberForSubscriber(int i, String str) throws RemoteException;

    String getMsisdn(String str) throws RemoteException;

    String getMsisdnForSubscriber(int i, String str) throws RemoteException;

    String getNaiForSubscriber(int i, String str) throws RemoteException;

    @UnsupportedAppUsage
    String getSubscriberId(String str) throws RemoteException;

    String getSubscriberIdForSubscriber(int i, String str) throws RemoteException;

    String getVoiceMailAlphaTag(String str) throws RemoteException;

    String getVoiceMailAlphaTagForSubscriber(int i, String str) throws RemoteException;

    String getVoiceMailNumber(String str) throws RemoteException;

    String getVoiceMailNumberForSubscriber(int i, String str) throws RemoteException;

    void resetCarrierKeysForImsiEncryption(int i, String str) throws RemoteException;

    void setCarrierInfoForImsiEncryption(int i, String str, ImsiEncryptionInfo imsiEncryptionInfo) throws RemoteException;

    public static class Default implements IPhoneSubInfo {
        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getDeviceId(String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getNaiForSubscriber(int subId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getDeviceIdForPhone(int phoneId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getImeiForSubscriber(int subId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getDeviceSvn(String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getDeviceSvnUsingSubId(int subId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getSubscriberId(String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getSubscriberIdForSubscriber(int subId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getGroupIdLevel1ForSubscriber(int subId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getIccSerialNumber(String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getIccSerialNumberForSubscriber(int subId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getLine1Number(String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getLine1NumberForSubscriber(int subId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getLine1AlphaTag(String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getLine1AlphaTagForSubscriber(int subId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getMsisdn(String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getMsisdnForSubscriber(int subId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getVoiceMailNumber(String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getVoiceMailNumberForSubscriber(int subId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getCompleteVoiceMailNumber() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getCompleteVoiceMailNumberForSubscriber(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public ImsiEncryptionInfo getCarrierInfoForImsiEncryption(int subId, int keyType, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public void setCarrierInfoForImsiEncryption(int subId, String callingPackage, ImsiEncryptionInfo imsiEncryptionInfo) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public void resetCarrierKeysForImsiEncryption(int subId, String callingPackage) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getVoiceMailAlphaTag(String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getVoiceMailAlphaTagForSubscriber(int subId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getIsimImpi(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getIsimDomain(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String[] getIsimImpu(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getIsimIst(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String[] getIsimPcscf(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IPhoneSubInfo
        public String getIccSimChallengeResponse(int subId, int appType, int authType, String data) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPhoneSubInfo {
        private static final String DESCRIPTOR = "com.android.internal.telephony.IPhoneSubInfo";
        static final int TRANSACTION_getCarrierInfoForImsiEncryption = 22;
        static final int TRANSACTION_getCompleteVoiceMailNumber = 20;
        static final int TRANSACTION_getCompleteVoiceMailNumberForSubscriber = 21;
        static final int TRANSACTION_getDeviceId = 1;
        static final int TRANSACTION_getDeviceIdForPhone = 3;
        static final int TRANSACTION_getDeviceSvn = 5;
        static final int TRANSACTION_getDeviceSvnUsingSubId = 6;
        static final int TRANSACTION_getGroupIdLevel1ForSubscriber = 9;
        static final int TRANSACTION_getIccSerialNumber = 10;
        static final int TRANSACTION_getIccSerialNumberForSubscriber = 11;
        static final int TRANSACTION_getIccSimChallengeResponse = 32;
        static final int TRANSACTION_getImeiForSubscriber = 4;
        static final int TRANSACTION_getIsimDomain = 28;
        static final int TRANSACTION_getIsimImpi = 27;
        static final int TRANSACTION_getIsimImpu = 29;
        static final int TRANSACTION_getIsimIst = 30;
        static final int TRANSACTION_getIsimPcscf = 31;
        static final int TRANSACTION_getLine1AlphaTag = 14;
        static final int TRANSACTION_getLine1AlphaTagForSubscriber = 15;
        static final int TRANSACTION_getLine1Number = 12;
        static final int TRANSACTION_getLine1NumberForSubscriber = 13;
        static final int TRANSACTION_getMsisdn = 16;
        static final int TRANSACTION_getMsisdnForSubscriber = 17;
        static final int TRANSACTION_getNaiForSubscriber = 2;
        static final int TRANSACTION_getSubscriberId = 7;
        static final int TRANSACTION_getSubscriberIdForSubscriber = 8;
        static final int TRANSACTION_getVoiceMailAlphaTag = 25;
        static final int TRANSACTION_getVoiceMailAlphaTagForSubscriber = 26;
        static final int TRANSACTION_getVoiceMailNumber = 18;
        static final int TRANSACTION_getVoiceMailNumberForSubscriber = 19;
        static final int TRANSACTION_resetCarrierKeysForImsiEncryption = 24;
        static final int TRANSACTION_setCarrierInfoForImsiEncryption = 23;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPhoneSubInfo asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPhoneSubInfo)) {
                return new Proxy(obj);
            }
            return (IPhoneSubInfo) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getDeviceId";
                case 2:
                    return "getNaiForSubscriber";
                case 3:
                    return "getDeviceIdForPhone";
                case 4:
                    return "getImeiForSubscriber";
                case 5:
                    return "getDeviceSvn";
                case 6:
                    return "getDeviceSvnUsingSubId";
                case 7:
                    return "getSubscriberId";
                case 8:
                    return "getSubscriberIdForSubscriber";
                case 9:
                    return "getGroupIdLevel1ForSubscriber";
                case 10:
                    return "getIccSerialNumber";
                case 11:
                    return "getIccSerialNumberForSubscriber";
                case 12:
                    return "getLine1Number";
                case 13:
                    return "getLine1NumberForSubscriber";
                case 14:
                    return "getLine1AlphaTag";
                case 15:
                    return "getLine1AlphaTagForSubscriber";
                case 16:
                    return "getMsisdn";
                case 17:
                    return "getMsisdnForSubscriber";
                case 18:
                    return "getVoiceMailNumber";
                case 19:
                    return "getVoiceMailNumberForSubscriber";
                case 20:
                    return "getCompleteVoiceMailNumber";
                case 21:
                    return "getCompleteVoiceMailNumberForSubscriber";
                case 22:
                    return "getCarrierInfoForImsiEncryption";
                case 23:
                    return "setCarrierInfoForImsiEncryption";
                case 24:
                    return "resetCarrierKeysForImsiEncryption";
                case 25:
                    return "getVoiceMailAlphaTag";
                case 26:
                    return "getVoiceMailAlphaTagForSubscriber";
                case 27:
                    return "getIsimImpi";
                case 28:
                    return "getIsimDomain";
                case 29:
                    return "getIsimImpu";
                case 30:
                    return "getIsimIst";
                case 31:
                    return "getIsimPcscf";
                case 32:
                    return "getIccSimChallengeResponse";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ImsiEncryptionInfo _arg2;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getDeviceId(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _result2 = getNaiForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _result3 = getDeviceIdForPhone(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _result4 = getImeiForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String _result5 = getDeviceSvn(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _result6 = getDeviceSvnUsingSubId(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        String _result7 = getSubscriberId(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result7);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        String _result8 = getSubscriberIdForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result8);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String _result9 = getGroupIdLevel1ForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result9);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        String _result10 = getIccSerialNumber(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result10);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        String _result11 = getIccSerialNumberForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result11);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        String _result12 = getLine1Number(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result12);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        String _result13 = getLine1NumberForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result13);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        String _result14 = getLine1AlphaTag(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result14);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        String _result15 = getLine1AlphaTagForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result15);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        String _result16 = getMsisdn(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result16);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        String _result17 = getMsisdnForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result17);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        String _result18 = getVoiceMailNumber(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result18);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        String _result19 = getVoiceMailNumberForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result19);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        String _result20 = getCompleteVoiceMailNumber();
                        reply.writeNoException();
                        reply.writeString(_result20);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        String _result21 = getCompleteVoiceMailNumberForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result21);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        ImsiEncryptionInfo _result22 = getCarrierInfoForImsiEncryption(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result22 != null) {
                            reply.writeInt(1);
                            _result22.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        String _arg1 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = ImsiEncryptionInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        setCarrierInfoForImsiEncryption(_arg0, _arg1, _arg2);
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        resetCarrierKeysForImsiEncryption(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        String _result23 = getVoiceMailAlphaTag(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result23);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        String _result24 = getVoiceMailAlphaTagForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result24);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        String _result25 = getIsimImpi(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result25);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        String _result26 = getIsimDomain(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result26);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result27 = getIsimImpu(data.readInt());
                        reply.writeNoException();
                        reply.writeStringArray(_result27);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        String _result28 = getIsimIst(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result28);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result29 = getIsimPcscf(data.readInt());
                        reply.writeNoException();
                        reply.writeStringArray(_result29);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        String _result30 = getIccSimChallengeResponse(data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result30);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IPhoneSubInfo {
            public static IPhoneSubInfo sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getDeviceId(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceId(callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getNaiForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNaiForSubscriber(subId, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getDeviceIdForPhone(int phoneId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceIdForPhone(phoneId, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getImeiForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImeiForSubscriber(subId, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getDeviceSvn(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceSvn(callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getDeviceSvnUsingSubId(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceSvnUsingSubId(subId, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getSubscriberId(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSubscriberId(callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getSubscriberIdForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSubscriberIdForSubscriber(subId, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getGroupIdLevel1ForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGroupIdLevel1ForSubscriber(subId, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getIccSerialNumber(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIccSerialNumber(callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getIccSerialNumberForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIccSerialNumberForSubscriber(subId, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getLine1Number(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLine1Number(callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getLine1NumberForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLine1NumberForSubscriber(subId, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getLine1AlphaTag(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLine1AlphaTag(callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getLine1AlphaTagForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLine1AlphaTagForSubscriber(subId, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getMsisdn(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMsisdn(callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getMsisdnForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMsisdnForSubscriber(subId, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getVoiceMailNumber(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVoiceMailNumber(callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getVoiceMailNumberForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVoiceMailNumberForSubscriber(subId, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getCompleteVoiceMailNumber() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCompleteVoiceMailNumber();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getCompleteVoiceMailNumberForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCompleteVoiceMailNumberForSubscriber(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public ImsiEncryptionInfo getCarrierInfoForImsiEncryption(int subId, int keyType, String callingPackage) throws RemoteException {
                ImsiEncryptionInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(keyType);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCarrierInfoForImsiEncryption(subId, keyType, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ImsiEncryptionInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public void setCarrierInfoForImsiEncryption(int subId, String callingPackage, ImsiEncryptionInfo imsiEncryptionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (imsiEncryptionInfo != null) {
                        _data.writeInt(1);
                        imsiEncryptionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCarrierInfoForImsiEncryption(subId, callingPackage, imsiEncryptionInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public void resetCarrierKeysForImsiEncryption(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetCarrierKeysForImsiEncryption(subId, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getVoiceMailAlphaTag(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVoiceMailAlphaTag(callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getVoiceMailAlphaTagForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVoiceMailAlphaTagForSubscriber(subId, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getIsimImpi(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIsimImpi(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getIsimDomain(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIsimDomain(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String[] getIsimImpu(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIsimImpu(subId);
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getIsimIst(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIsimIst(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String[] getIsimPcscf(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIsimPcscf(subId);
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IPhoneSubInfo
            public String getIccSimChallengeResponse(int subId, int appType, int authType, String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(appType);
                    _data.writeInt(authType);
                    _data.writeString(data);
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIccSimChallengeResponse(subId, appType, authType, data);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IPhoneSubInfo impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPhoneSubInfo getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
