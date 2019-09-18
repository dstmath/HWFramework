package com.huawei.harassmentinterception.service;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHarassmentInterceptionService extends IInterface {

    public static abstract class Stub extends Binder implements IHarassmentInterceptionService {
        private static final String DESCRIPTOR = "com.huawei.harassmentinterception.service.IHarassmentInterceptionService";
        static final int TRANSACTION_addPhoneNumberBlockItem = 2;
        static final int TRANSACTION_addPhoneNumberWhiteItem = 11;
        static final int TRANSACTION_callHarassmentInterceptionService = 13;
        static final int TRANSACTION_checkPhoneNumberFromBlockItem = 5;
        static final int TRANSACTION_checkPhoneNumberFromWhiteItem = 9;
        static final int TRANSACTION_handleInComingCallAndGetNumberMark = 15;
        static final int TRANSACTION_handleIncomingCallAction = 8;
        static final int TRANSACTION_handleSmsDeliverAction = 7;
        static final int TRANSACTION_queryPhoneNumberBlockItem = 4;
        static final int TRANSACTION_queryPhoneNumberWhiteItem = 12;
        static final int TRANSACTION_removePhoneNumberBlockItem = 3;
        static final int TRANSACTION_removePhoneNumberFromWhiteItem = 10;
        static final int TRANSACTION_sendCallBlockRecords = 6;
        static final int TRANSACTION_sendGoogleNBRecord = 14;
        static final int TRANSACTION_setPhoneNumberBlockList = 1;

        private static class Proxy implements IHarassmentInterceptionService {
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

            public int setPhoneNumberBlockList(Bundle blocknumberlist, int type, int source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (blocknumberlist != null) {
                        _data.writeInt(1);
                        blocknumberlist.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    _data.writeInt(source);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addPhoneNumberBlockItem(Bundle blocknumber, int type, int source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (blocknumber != null) {
                        _data.writeInt(1);
                        blocknumber.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    _data.writeInt(source);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int removePhoneNumberBlockItem(Bundle blocknumber, int type, int source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (blocknumber != null) {
                        _data.writeInt(1);
                        blocknumber.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    _data.writeInt(source);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] queryPhoneNumberBlockItem() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkPhoneNumberFromBlockItem(Bundle checknumber, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (checknumber != null) {
                        _data.writeInt(1);
                        checknumber.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendCallBlockRecords(Bundle callBlockRecords) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callBlockRecords != null) {
                        _data.writeInt(1);
                        callBlockRecords.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int handleSmsDeliverAction(Bundle smsInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (smsInfo != null) {
                        _data.writeInt(1);
                        smsInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int handleIncomingCallAction(Bundle callInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callInfo != null) {
                        _data.writeInt(1);
                        callInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkPhoneNumberFromWhiteItem(Bundle checknumber, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (checknumber != null) {
                        _data.writeInt(1);
                        checknumber.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int removePhoneNumberFromWhiteItem(Bundle blocknumber, int type, int source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (blocknumber != null) {
                        _data.writeInt(1);
                        blocknumber.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    _data.writeInt(source);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addPhoneNumberWhiteItem(Bundle blocknumber, int type, int source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (blocknumber != null) {
                        _data.writeInt(1);
                        blocknumber.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    _data.writeInt(source);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] queryPhoneNumberWhiteItem() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle callHarassmentInterceptionService(String method, Bundle params) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(method);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(13, _data, _reply, 0);
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

            public boolean sendGoogleNBRecord(Bundle smsInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (smsInfo != null) {
                        _data.writeInt(1);
                        smsInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, _reply, 0);
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

            public Bundle handleInComingCallAndGetNumberMark(Bundle callInfo) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callInfo != null) {
                        _data.writeInt(1);
                        callInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, _reply, 0);
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHarassmentInterceptionService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHarassmentInterceptionService)) {
                return new Proxy(obj);
            }
            return (IHarassmentInterceptionService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v5, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v3, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v9, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v7, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v13, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v11, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v18, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v15, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v22, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v18, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v26, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v22, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v30, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v26, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v34, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v30, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v38, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v34, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v42, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v38, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v47, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v46, resolved type: android.os.Bundle} */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                Bundle _arg0 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Bundle.CREATOR.createFromParcel(data);
                        }
                        int _result = setPhoneNumberBlockList(_arg0, data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Bundle.CREATOR.createFromParcel(data);
                        }
                        int _result2 = addPhoneNumberBlockItem(_arg0, data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Bundle.CREATOR.createFromParcel(data);
                        }
                        int _result3 = removePhoneNumberBlockItem(_arg0, data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result4 = queryPhoneNumberBlockItem();
                        reply.writeNoException();
                        reply.writeStringArray(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Bundle.CREATOR.createFromParcel(data);
                        }
                        int _result5 = checkPhoneNumberFromBlockItem(_arg0, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Bundle.CREATOR.createFromParcel(data);
                        }
                        sendCallBlockRecords(_arg0);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Bundle.CREATOR.createFromParcel(data);
                        }
                        int _result6 = handleSmsDeliverAction(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Bundle.CREATOR.createFromParcel(data);
                        }
                        int _result7 = handleIncomingCallAction(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Bundle.CREATOR.createFromParcel(data);
                        }
                        int _result8 = checkPhoneNumberFromWhiteItem(_arg0, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Bundle.CREATOR.createFromParcel(data);
                        }
                        int _result9 = removePhoneNumberFromWhiteItem(_arg0, data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Bundle.CREATOR.createFromParcel(data);
                        }
                        int _result10 = addPhoneNumberWhiteItem(_arg0, data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result11 = queryPhoneNumberWhiteItem();
                        reply.writeNoException();
                        reply.writeStringArray(_result11);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        Bundle _result12 = callHarassmentInterceptionService(_arg02, _arg0);
                        reply.writeNoException();
                        if (_result12 != null) {
                            reply.writeInt(1);
                            _result12.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Bundle.CREATOR.createFromParcel(data);
                        }
                        boolean _result13 = sendGoogleNBRecord(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        Bundle _result14 = handleInComingCallAndGetNumberMark(_arg0);
                        reply.writeNoException();
                        if (_result14 != null) {
                            reply.writeInt(1);
                            _result14.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    int addPhoneNumberBlockItem(Bundle bundle, int i, int i2) throws RemoteException;

    int addPhoneNumberWhiteItem(Bundle bundle, int i, int i2) throws RemoteException;

    Bundle callHarassmentInterceptionService(String str, Bundle bundle) throws RemoteException;

    int checkPhoneNumberFromBlockItem(Bundle bundle, int i) throws RemoteException;

    int checkPhoneNumberFromWhiteItem(Bundle bundle, int i) throws RemoteException;

    Bundle handleInComingCallAndGetNumberMark(Bundle bundle) throws RemoteException;

    int handleIncomingCallAction(Bundle bundle) throws RemoteException;

    int handleSmsDeliverAction(Bundle bundle) throws RemoteException;

    String[] queryPhoneNumberBlockItem() throws RemoteException;

    String[] queryPhoneNumberWhiteItem() throws RemoteException;

    int removePhoneNumberBlockItem(Bundle bundle, int i, int i2) throws RemoteException;

    int removePhoneNumberFromWhiteItem(Bundle bundle, int i, int i2) throws RemoteException;

    void sendCallBlockRecords(Bundle bundle) throws RemoteException;

    boolean sendGoogleNBRecord(Bundle bundle) throws RemoteException;

    int setPhoneNumberBlockList(Bundle bundle, int i, int i2) throws RemoteException;
}
