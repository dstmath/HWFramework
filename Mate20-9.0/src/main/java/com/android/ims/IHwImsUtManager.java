package com.android.ims;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwImsUtManager extends IInterface {

    public static abstract class Stub extends Binder implements IHwImsUtManager {
        private static final String DESCRIPTOR = "com.android.ims.IHwImsUtManager";
        static final int TRANSACTION_getUtIMPUFromNetwork = 7;
        static final int TRANSACTION_isSupportCFT = 1;
        static final int TRANSACTION_isUtEnable = 2;
        static final int TRANSACTION_processECT = 6;
        static final int TRANSACTION_queryCallForwardForServiceClass = 5;
        static final int TRANSACTION_updateCallBarringOption = 4;
        static final int TRANSACTION_updateCallForwardUncondTimer = 3;

        private static class Proxy implements IHwImsUtManager {
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

            public boolean isSupportCFT(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    boolean _result = false;
                    this.mRemote.transact(1, _data, _reply, 0);
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

            public boolean isUtEnable(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    boolean _result = false;
                    this.mRemote.transact(2, _data, _reply, 0);
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

            public int updateCallForwardUncondTimer(int phoneId, int starthour, int startminute, int endhour, int endminute, int action, int condition, String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(starthour);
                    _data.writeInt(startminute);
                    _data.writeInt(endhour);
                    _data.writeInt(endminute);
                    _data.writeInt(action);
                    _data.writeInt(condition);
                    _data.writeString(number);
                    this.mRemote.transact(Stub.TRANSACTION_updateCallForwardUncondTimer, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int updateCallBarringOption(int phoneId, String password, int cbType, boolean enable, int serviceClass, String[] barrList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeString(password);
                    _data.writeInt(cbType);
                    _data.writeInt(enable);
                    _data.writeInt(serviceClass);
                    _data.writeStringArray(barrList);
                    this.mRemote.transact(Stub.TRANSACTION_updateCallBarringOption, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int queryCallForwardForServiceClass(int phoneId, int condition, String number, int serviceClass) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(condition);
                    _data.writeString(number);
                    _data.writeInt(serviceClass);
                    this.mRemote.transact(Stub.TRANSACTION_queryCallForwardForServiceClass, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void processECT(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    this.mRemote.transact(Stub.TRANSACTION_processECT, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getUtIMPUFromNetwork(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    this.mRemote.transact(Stub.TRANSACTION_getUtIMPUFromNetwork, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwImsUtManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwImsUtManager)) {
                return new Proxy(obj);
            }
            return (IHwImsUtManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result = isSupportCFT(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result2 = isUtEnable(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case TRANSACTION_updateCallForwardUncondTimer /*3*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result3 = updateCallForwardUncondTimer(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case TRANSACTION_updateCallBarringOption /*4*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result4 = updateCallBarringOption(data.readInt(), data.readString(), data.readInt(), data.readInt() != 0, data.readInt(), data.createStringArray());
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case TRANSACTION_queryCallForwardForServiceClass /*5*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result5 = queryCallForwardForServiceClass(data.readInt(), data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case TRANSACTION_processECT /*6*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        processECT(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getUtIMPUFromNetwork /*7*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result6 = getUtIMPUFromNetwork(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result6);
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

    String getUtIMPUFromNetwork(int i) throws RemoteException;

    boolean isSupportCFT(int i) throws RemoteException;

    boolean isUtEnable(int i) throws RemoteException;

    void processECT(int i) throws RemoteException;

    int queryCallForwardForServiceClass(int i, int i2, String str, int i3) throws RemoteException;

    int updateCallBarringOption(int i, String str, int i2, boolean z, int i3, String[] strArr) throws RemoteException;

    int updateCallForwardUncondTimer(int i, int i2, int i3, int i4, int i5, int i6, int i7, String str) throws RemoteException;
}
