package com.huawei.hwpanpayservice;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.Map;

public interface IHwSEService extends IInterface {
    int activateApplet(String str, String str2, String str3) throws RemoteException;

    int checkEligibility(String str) throws RemoteException;

    int checkEligibilityEx(String str, String str2) throws RemoteException;

    int commonExecute(String str, String str2, String str3) throws RemoteException;

    int createSSD(String str, String str2, String str3, String str4) throws RemoteException;

    int createSSDEx(String str, String str2, String str3) throws RemoteException;

    int deleteApplet(String str, String str2, String str3, String str4) throws RemoteException;

    int deleteSSD(String str, String str2, String str3, String str4) throws RemoteException;

    int deleteSSDEx(String str, String str2, String str3) throws RemoteException;

    String getCIN(String str) throws RemoteException;

    String getCPLC(String str) throws RemoteException;

    String getCplc() throws RemoteException;

    String getIIN(String str) throws RemoteException;

    String[] getLastErrorInfo(String str) throws RemoteException;

    boolean getSwitch(String str) throws RemoteException;

    int initEse(String str, String str2, String str3) throws RemoteException;

    int installApplet(String str, String str2, String str3, String str4) throws RemoteException;

    int isSwitchFeatureOn() throws RemoteException;

    int isUKeySwitchDisabled(String str) throws RemoteException;

    int lockApplet(String str, String str2, String str3, String str4) throws RemoteException;

    int setConfig(String str, Map map) throws RemoteException;

    int setSwitch(String str, boolean z) throws RemoteException;

    int setUKeySwitchDisabled(String str, boolean z) throws RemoteException;

    int syncSeInfo(String str, String str2, String str3) throws RemoteException;

    int syncSeInfoEx(String str, String str2) throws RemoteException;

    int unlockApplet(String str, String str2, String str3, String str4) throws RemoteException;

    public static abstract class Stub extends Binder implements IHwSEService {
        private static final String DESCRIPTOR = "com.huawei.hwpanpayservice.IHwSEService";
        static final int TRANSACTION_activateApplet = 13;
        static final int TRANSACTION_checkEligibility = 1;
        static final int TRANSACTION_checkEligibilityEx = 2;
        static final int TRANSACTION_commonExecute = 14;
        static final int TRANSACTION_createSSD = 5;
        static final int TRANSACTION_createSSDEx = 6;
        static final int TRANSACTION_deleteApplet = 10;
        static final int TRANSACTION_deleteSSD = 7;
        static final int TRANSACTION_deleteSSDEx = 8;
        static final int TRANSACTION_getCIN = 16;
        static final int TRANSACTION_getCPLC = 15;
        static final int TRANSACTION_getCplc = 22;
        static final int TRANSACTION_getIIN = 17;
        static final int TRANSACTION_getLastErrorInfo = 20;
        static final int TRANSACTION_getSwitch = 18;
        static final int TRANSACTION_initEse = 23;
        static final int TRANSACTION_installApplet = 9;
        static final int TRANSACTION_isSwitchFeatureOn = 24;
        static final int TRANSACTION_isUKeySwitchDisabled = 26;
        static final int TRANSACTION_lockApplet = 11;
        static final int TRANSACTION_setConfig = 21;
        static final int TRANSACTION_setSwitch = 19;
        static final int TRANSACTION_setUKeySwitchDisabled = 25;
        static final int TRANSACTION_syncSeInfo = 3;
        static final int TRANSACTION_syncSeInfoEx = 4;
        static final int TRANSACTION_unlockApplet = 12;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwSEService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwSEService)) {
                return new Proxy(obj);
            }
            return (IHwSEService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg1 = false;
                boolean _arg12 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = checkEligibility(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = checkEligibilityEx(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = syncSeInfo(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = syncSeInfoEx(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = createSSD(data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = createSSDEx(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = deleteSSD(data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = deleteSSDEx(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = installApplet(data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = deleteApplet(data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = lockApplet(data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = unlockApplet(data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = activateApplet(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = commonExecute(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        String _result15 = getCPLC(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result15);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        String _result16 = getCIN(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result16);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        String _result17 = getIIN(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result17);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean z = getSwitch(data.readString());
                        reply.writeNoException();
                        reply.writeInt(z ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        int _result18 = setSwitch(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result19 = getLastErrorInfo(data.readString());
                        reply.writeNoException();
                        reply.writeStringArray(_result19);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = setConfig(data.readString(), data.readHashMap(getClass().getClassLoader()));
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        String _result21 = getCplc();
                        reply.writeNoException();
                        reply.writeString(_result21);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        int _result22 = initEse(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        return true;
                    case TRANSACTION_isSwitchFeatureOn /*{ENCODED_INT: 24}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result23 = isSwitchFeatureOn();
                        reply.writeNoException();
                        reply.writeInt(_result23);
                        return true;
                    case TRANSACTION_setUKeySwitchDisabled /*{ENCODED_INT: 25}*/:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        int _result24 = setUKeySwitchDisabled(_arg02, _arg12);
                        reply.writeNoException();
                        reply.writeInt(_result24);
                        return true;
                    case TRANSACTION_isUKeySwitchDisabled /*{ENCODED_INT: 26}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result25 = isUKeySwitchDisabled(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result25);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IHwSEService {
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

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public int checkEligibility(String spID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(spID);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public int checkEligibilityEx(String serviceId, String funCallId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(serviceId);
                    _data.writeString(funCallId);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public int syncSeInfo(String spID, String sign, String timeStamp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(spID);
                    _data.writeString(sign);
                    _data.writeString(timeStamp);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public int syncSeInfoEx(String serviceId, String funCallId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(serviceId);
                    _data.writeString(funCallId);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public int createSSD(String spID, String sign, String timeStamp, String ssdAid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(spID);
                    _data.writeString(sign);
                    _data.writeString(timeStamp);
                    _data.writeString(ssdAid);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public int createSSDEx(String serviceId, String funCallId, String ssdAid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(serviceId);
                    _data.writeString(funCallId);
                    _data.writeString(ssdAid);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public int deleteSSD(String spID, String sign, String timeStamp, String ssdAid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(spID);
                    _data.writeString(sign);
                    _data.writeString(timeStamp);
                    _data.writeString(ssdAid);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public int deleteSSDEx(String serviceId, String funCallId, String ssdAid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(serviceId);
                    _data.writeString(funCallId);
                    _data.writeString(ssdAid);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public int installApplet(String serviceId, String funCallId, String appletAid, String appletVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(serviceId);
                    _data.writeString(funCallId);
                    _data.writeString(appletAid);
                    _data.writeString(appletVersion);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public int deleteApplet(String serviceId, String funCallId, String appletAid, String appletVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(serviceId);
                    _data.writeString(funCallId);
                    _data.writeString(appletAid);
                    _data.writeString(appletVersion);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public int lockApplet(String serviceId, String funCallId, String appletAid, String appletVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(serviceId);
                    _data.writeString(funCallId);
                    _data.writeString(appletAid);
                    _data.writeString(appletVersion);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public int unlockApplet(String serviceId, String funCallId, String appletAid, String appletVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(serviceId);
                    _data.writeString(funCallId);
                    _data.writeString(appletAid);
                    _data.writeString(appletVersion);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public int activateApplet(String serviceId, String funCallId, String appletAid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(serviceId);
                    _data.writeString(funCallId);
                    _data.writeString(appletAid);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public int commonExecute(String spID, String serviceId, String funCallId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(spID);
                    _data.writeString(serviceId);
                    _data.writeString(funCallId);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public String getCPLC(String spID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(spID);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public String getCIN(String spID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(spID);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public String getIIN(String spID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(spID);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public boolean getSwitch(String spID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(spID);
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

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public int setSwitch(String spID, boolean choice) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(spID);
                    _data.writeInt(choice ? 1 : 0);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public String[] getLastErrorInfo(String spID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(spID);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public int setConfig(String spID, Map config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(spID);
                    _data.writeMap(config);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public String getCplc() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public int initEse(String spID, String sign, String timeStamp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(spID);
                    _data.writeString(sign);
                    _data.writeString(timeStamp);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public int isSwitchFeatureOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isSwitchFeatureOn, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public int setUKeySwitchDisabled(String packageName, boolean isDisabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(isDisabled ? 1 : 0);
                    this.mRemote.transact(Stub.TRANSACTION_setUKeySwitchDisabled, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwpanpayservice.IHwSEService
            public int isUKeySwitchDisabled(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_isUKeySwitchDisabled, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
