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
                        _data.writeInt(Stub.TRANSACTION_setPhoneNumberBlockList);
                        blocknumberlist.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    _data.writeInt(source);
                    this.mRemote.transact(Stub.TRANSACTION_setPhoneNumberBlockList, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_setPhoneNumberBlockList);
                        blocknumber.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    _data.writeInt(source);
                    this.mRemote.transact(Stub.TRANSACTION_addPhoneNumberBlockItem, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_setPhoneNumberBlockList);
                        blocknumber.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    _data.writeInt(source);
                    this.mRemote.transact(Stub.TRANSACTION_removePhoneNumberBlockItem, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_queryPhoneNumberBlockItem, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_setPhoneNumberBlockList);
                        checknumber.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    this.mRemote.transact(Stub.TRANSACTION_checkPhoneNumberFromBlockItem, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_setPhoneNumberBlockList);
                        callBlockRecords.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendCallBlockRecords, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_setPhoneNumberBlockList);
                        smsInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_handleSmsDeliverAction, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_setPhoneNumberBlockList);
                        callInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_handleIncomingCallAction, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_setPhoneNumberBlockList);
                        checknumber.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    this.mRemote.transact(Stub.TRANSACTION_checkPhoneNumberFromWhiteItem, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_setPhoneNumberBlockList);
                        blocknumber.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    _data.writeInt(source);
                    this.mRemote.transact(Stub.TRANSACTION_removePhoneNumberFromWhiteItem, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_setPhoneNumberBlockList);
                        blocknumber.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    _data.writeInt(source);
                    this.mRemote.transact(Stub.TRANSACTION_addPhoneNumberWhiteItem, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_queryPhoneNumberWhiteItem, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle callHarassmentInterceptionService(String method, Bundle params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle bundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(method);
                    if (params != null) {
                        _data.writeInt(Stub.TRANSACTION_setPhoneNumberBlockList);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callHarassmentInterceptionService, _data, _reply, 0);
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

            public boolean sendGoogleNBRecord(Bundle smsInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (smsInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_setPhoneNumberBlockList);
                        smsInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendGoogleNBRecord, _data, _reply, 0);
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

            public Bundle handleInComingCallAndGetNumberMark(Bundle callInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle bundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_setPhoneNumberBlockList);
                        callInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_handleInComingCallAndGetNumberMark, _data, _reply, 0);
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle bundle;
            int _result;
            String[] _result2;
            Bundle _result3;
            switch (code) {
                case TRANSACTION_setPhoneNumberBlockList /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = setPhoneNumberBlockList(bundle, data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_addPhoneNumberBlockItem /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = addPhoneNumberBlockItem(bundle, data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_removePhoneNumberBlockItem /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = removePhoneNumberBlockItem(bundle, data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_queryPhoneNumberBlockItem /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = queryPhoneNumberBlockItem();
                    reply.writeNoException();
                    reply.writeStringArray(_result2);
                    return true;
                case TRANSACTION_checkPhoneNumberFromBlockItem /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = checkPhoneNumberFromBlockItem(bundle, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_sendCallBlockRecords /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    sendCallBlockRecords(bundle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_handleSmsDeliverAction /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = handleSmsDeliverAction(bundle);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_handleIncomingCallAction /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = handleIncomingCallAction(bundle);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_checkPhoneNumberFromWhiteItem /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = checkPhoneNumberFromWhiteItem(bundle, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_removePhoneNumberFromWhiteItem /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = removePhoneNumberFromWhiteItem(bundle, data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_addPhoneNumberWhiteItem /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = addPhoneNumberWhiteItem(bundle, data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_queryPhoneNumberWhiteItem /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = queryPhoneNumberWhiteItem();
                    reply.writeNoException();
                    reply.writeStringArray(_result2);
                    return true;
                case TRANSACTION_callHarassmentInterceptionService /*13*/:
                    Bundle bundle2;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    _result3 = callHarassmentInterceptionService(_arg0, bundle2);
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_setPhoneNumberBlockList);
                        _result3.writeToParcel(reply, TRANSACTION_setPhoneNumberBlockList);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_sendGoogleNBRecord /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    boolean _result4 = sendGoogleNBRecord(bundle);
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_setPhoneNumberBlockList : 0);
                    return true;
                case TRANSACTION_handleInComingCallAndGetNumberMark /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result3 = handleInComingCallAndGetNumberMark(bundle);
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_setPhoneNumberBlockList);
                        _result3.writeToParcel(reply, TRANSACTION_setPhoneNumberBlockList);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
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
