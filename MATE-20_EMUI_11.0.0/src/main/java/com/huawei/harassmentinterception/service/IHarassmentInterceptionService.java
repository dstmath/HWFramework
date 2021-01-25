package com.huawei.harassmentinterception.service;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHarassmentInterceptionService extends IInterface {
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

    public static class Default implements IHarassmentInterceptionService {
        @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
        public int setPhoneNumberBlockList(Bundle blocknumberlist, int type, int source) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
        public int addPhoneNumberBlockItem(Bundle blocknumber, int type, int source) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
        public int removePhoneNumberBlockItem(Bundle blocknumber, int type, int source) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
        public String[] queryPhoneNumberBlockItem() throws RemoteException {
            return null;
        }

        @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
        public int checkPhoneNumberFromBlockItem(Bundle checknumber, int type) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
        public void sendCallBlockRecords(Bundle callBlockRecords) throws RemoteException {
        }

        @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
        public int handleSmsDeliverAction(Bundle smsInfo) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
        public int handleIncomingCallAction(Bundle callInfo) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
        public int checkPhoneNumberFromWhiteItem(Bundle checknumber, int type) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
        public int removePhoneNumberFromWhiteItem(Bundle blocknumber, int type, int source) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
        public int addPhoneNumberWhiteItem(Bundle blocknumber, int type, int source) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
        public String[] queryPhoneNumberWhiteItem() throws RemoteException {
            return null;
        }

        @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
        public Bundle callHarassmentInterceptionService(String method, Bundle params) throws RemoteException {
            return null;
        }

        @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
        public boolean sendGoogleNBRecord(Bundle smsInfo) throws RemoteException {
            return false;
        }

        @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
        public Bundle handleInComingCallAndGetNumberMark(Bundle callInfo) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg0;
            Bundle _arg02;
            Bundle _arg03;
            Bundle _arg04;
            Bundle _arg05;
            Bundle _arg06;
            Bundle _arg07;
            Bundle _arg08;
            Bundle _arg09;
            Bundle _arg010;
            Bundle _arg1;
            Bundle _arg011;
            Bundle _arg012;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        int _result = setPhoneNumberBlockList(_arg0, data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        int _result2 = addPhoneNumberBlockItem(_arg02, data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        int _result3 = removePhoneNumberBlockItem(_arg03, data.readInt(), data.readInt());
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
                            _arg04 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        int _result5 = checkPhoneNumberFromBlockItem(_arg04, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        sendCallBlockRecords(_arg05);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        int _result6 = handleSmsDeliverAction(_arg06);
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        int _result7 = handleIncomingCallAction(_arg07);
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg08 = null;
                        }
                        int _result8 = checkPhoneNumberFromWhiteItem(_arg08, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg09 = null;
                        }
                        int _result9 = removePhoneNumberFromWhiteItem(_arg09, data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg010 = null;
                        }
                        int _result10 = addPhoneNumberWhiteItem(_arg010, data.readInt(), data.readInt());
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
                        String _arg013 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        Bundle _result12 = callHarassmentInterceptionService(_arg013, _arg1);
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
                            _arg011 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg011 = null;
                        }
                        boolean sendGoogleNBRecord = sendGoogleNBRecord(_arg011);
                        reply.writeNoException();
                        reply.writeInt(sendGoogleNBRecord ? 1 : 0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg012 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg012 = null;
                        }
                        Bundle _result13 = handleInComingCallAndGetNumberMark(_arg012);
                        reply.writeNoException();
                        if (_result13 != null) {
                            reply.writeInt(1);
                            _result13.writeToParcel(reply, 1);
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

        /* access modifiers changed from: private */
        public static class Proxy implements IHarassmentInterceptionService {
            public static IHarassmentInterceptionService sDefaultImpl;
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

            @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
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
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setPhoneNumberBlockList(blocknumberlist, type, source);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
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
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addPhoneNumberBlockItem(blocknumber, type, source);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
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
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removePhoneNumberBlockItem(blocknumber, type, source);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
            public String[] queryPhoneNumberBlockItem() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryPhoneNumberBlockItem();
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

            @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
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
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkPhoneNumberFromBlockItem(checknumber, type);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
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
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendCallBlockRecords(callBlockRecords);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
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
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().handleSmsDeliverAction(smsInfo);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
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
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().handleIncomingCallAction(callInfo);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
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
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkPhoneNumberFromWhiteItem(checknumber, type);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
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
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removePhoneNumberFromWhiteItem(blocknumber, type, source);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
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
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addPhoneNumberWhiteItem(blocknumber, type, source);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
            public String[] queryPhoneNumberWhiteItem() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryPhoneNumberWhiteItem();
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

            @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
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
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().callHarassmentInterceptionService(method, params);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
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
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendGoogleNBRecord(smsInfo);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.harassmentinterception.service.IHarassmentInterceptionService
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
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().handleInComingCallAndGetNumberMark(callInfo);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
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
        }

        public static boolean setDefaultImpl(IHarassmentInterceptionService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHarassmentInterceptionService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
