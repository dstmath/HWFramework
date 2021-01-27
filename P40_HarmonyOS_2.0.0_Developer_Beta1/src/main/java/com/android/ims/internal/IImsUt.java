package com.android.ims.internal;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.ims.internal.IImsUtListener;

public interface IImsUt extends IInterface {
    void close() throws RemoteException;

    IBinder getHwInnerService() throws RemoteException;

    int queryCLIP() throws RemoteException;

    int queryCLIR() throws RemoteException;

    int queryCOLP() throws RemoteException;

    int queryCOLR() throws RemoteException;

    int queryCallBarring(int i) throws RemoteException;

    int queryCallBarringForServiceClass(int i, int i2) throws RemoteException;

    int queryCallForward(int i, String str) throws RemoteException;

    int queryCallWaiting() throws RemoteException;

    void setListener(IImsUtListener iImsUtListener) throws RemoteException;

    int transact(Bundle bundle) throws RemoteException;

    int updateCLIP(boolean z) throws RemoteException;

    int updateCLIR(int i) throws RemoteException;

    int updateCOLP(boolean z) throws RemoteException;

    int updateCOLR(int i) throws RemoteException;

    int updateCallBarring(int i, int i2, String[] strArr) throws RemoteException;

    int updateCallBarringForServiceClass(int i, int i2, String[] strArr, int i3) throws RemoteException;

    int updateCallForward(int i, int i2, String str, int i3, int i4) throws RemoteException;

    int updateCallWaiting(boolean z, int i) throws RemoteException;

    public static class Default implements IImsUt {
        @Override // com.android.ims.internal.IImsUt
        public void close() throws RemoteException {
        }

        @Override // com.android.ims.internal.IImsUt
        public int queryCallBarring(int cbType) throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.IImsUt
        public int queryCallForward(int condition, String number) throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.IImsUt
        public int queryCallWaiting() throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.IImsUt
        public int queryCLIR() throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.IImsUt
        public int queryCLIP() throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.IImsUt
        public int queryCOLR() throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.IImsUt
        public int queryCOLP() throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.IImsUt
        public int transact(Bundle ssInfo) throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.IImsUt
        public int updateCallBarring(int cbType, int action, String[] barrList) throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.IImsUt
        public int updateCallForward(int action, int condition, String number, int serviceClass, int timeSeconds) throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.IImsUt
        public int updateCallWaiting(boolean enable, int serviceClass) throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.IImsUt
        public int updateCLIR(int clirMode) throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.IImsUt
        public int updateCLIP(boolean enable) throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.IImsUt
        public int updateCOLR(int presentation) throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.IImsUt
        public int updateCOLP(boolean enable) throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.IImsUt
        public void setListener(IImsUtListener listener) throws RemoteException {
        }

        @Override // com.android.ims.internal.IImsUt
        public int queryCallBarringForServiceClass(int cbType, int serviceClass) throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.IImsUt
        public int updateCallBarringForServiceClass(int cbType, int action, String[] barrList, int serviceClass) throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.internal.IImsUt
        public IBinder getHwInnerService() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IImsUt {
        private static final String DESCRIPTOR = "com.android.ims.internal.IImsUt";
        static final int TRANSACTION_close = 1;
        static final int TRANSACTION_getHwInnerService = 20;
        static final int TRANSACTION_queryCLIP = 6;
        static final int TRANSACTION_queryCLIR = 5;
        static final int TRANSACTION_queryCOLP = 8;
        static final int TRANSACTION_queryCOLR = 7;
        static final int TRANSACTION_queryCallBarring = 2;
        static final int TRANSACTION_queryCallBarringForServiceClass = 18;
        static final int TRANSACTION_queryCallForward = 3;
        static final int TRANSACTION_queryCallWaiting = 4;
        static final int TRANSACTION_setListener = 17;
        static final int TRANSACTION_transact = 9;
        static final int TRANSACTION_updateCLIP = 14;
        static final int TRANSACTION_updateCLIR = 13;
        static final int TRANSACTION_updateCOLP = 16;
        static final int TRANSACTION_updateCOLR = 15;
        static final int TRANSACTION_updateCallBarring = 10;
        static final int TRANSACTION_updateCallBarringForServiceClass = 19;
        static final int TRANSACTION_updateCallForward = 11;
        static final int TRANSACTION_updateCallWaiting = 12;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImsUt asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImsUt)) {
                return new Proxy(obj);
            }
            return (IImsUt) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "close";
                case 2:
                    return "queryCallBarring";
                case 3:
                    return "queryCallForward";
                case 4:
                    return "queryCallWaiting";
                case 5:
                    return "queryCLIR";
                case 6:
                    return "queryCLIP";
                case 7:
                    return "queryCOLR";
                case 8:
                    return "queryCOLP";
                case 9:
                    return "transact";
                case 10:
                    return "updateCallBarring";
                case 11:
                    return "updateCallForward";
                case 12:
                    return "updateCallWaiting";
                case 13:
                    return "updateCLIR";
                case 14:
                    return "updateCLIP";
                case 15:
                    return "updateCOLR";
                case 16:
                    return "updateCOLP";
                case 17:
                    return "setListener";
                case 18:
                    return "queryCallBarringForServiceClass";
                case 19:
                    return "updateCallBarringForServiceClass";
                case 20:
                    return "getHwInnerService";
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
            Bundle _arg0;
            if (code != 1598968902) {
                boolean _arg02 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        close();
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = queryCallBarring(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = queryCallForward(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = queryCallWaiting();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = queryCLIR();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = queryCLIP();
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = queryCOLR();
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = queryCOLP();
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        int _result8 = transact(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = updateCallBarring(data.readInt(), data.readInt(), data.createStringArray());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = updateCallForward(data.readInt(), data.readInt(), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        int _result11 = updateCallWaiting(_arg02, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = updateCLIR(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        int _result13 = updateCLIP(_arg02);
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = updateCOLR(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        int _result15 = updateCOLP(_arg02);
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        setListener(IImsUtListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _result16 = queryCallBarringForServiceClass(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _result17 = updateCallBarringForServiceClass(data.readInt(), data.readInt(), data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result18 = getHwInnerService();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result18);
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
        public static class Proxy implements IImsUt {
            public static IImsUt sDefaultImpl;
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

            @Override // com.android.ims.internal.IImsUt
            public void close() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().close();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsUt
            public int queryCallBarring(int cbType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cbType);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryCallBarring(cbType);
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

            @Override // com.android.ims.internal.IImsUt
            public int queryCallForward(int condition, String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(condition);
                    _data.writeString(number);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryCallForward(condition, number);
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

            @Override // com.android.ims.internal.IImsUt
            public int queryCallWaiting() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryCallWaiting();
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

            @Override // com.android.ims.internal.IImsUt
            public int queryCLIR() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryCLIR();
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

            @Override // com.android.ims.internal.IImsUt
            public int queryCLIP() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryCLIP();
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

            @Override // com.android.ims.internal.IImsUt
            public int queryCOLR() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryCOLR();
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

            @Override // com.android.ims.internal.IImsUt
            public int queryCOLP() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryCOLP();
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

            @Override // com.android.ims.internal.IImsUt
            public int transact(Bundle ssInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ssInfo != null) {
                        _data.writeInt(1);
                        ssInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().transact(ssInfo);
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

            @Override // com.android.ims.internal.IImsUt
            public int updateCallBarring(int cbType, int action, String[] barrList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cbType);
                    _data.writeInt(action);
                    _data.writeStringArray(barrList);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateCallBarring(cbType, action, barrList);
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

            @Override // com.android.ims.internal.IImsUt
            public int updateCallForward(int action, int condition, String number, int serviceClass, int timeSeconds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action);
                    _data.writeInt(condition);
                    _data.writeString(number);
                    _data.writeInt(serviceClass);
                    _data.writeInt(timeSeconds);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateCallForward(action, condition, number, serviceClass, timeSeconds);
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

            @Override // com.android.ims.internal.IImsUt
            public int updateCallWaiting(boolean enable, int serviceClass) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    _data.writeInt(serviceClass);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateCallWaiting(enable, serviceClass);
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

            @Override // com.android.ims.internal.IImsUt
            public int updateCLIR(int clirMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clirMode);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateCLIR(clirMode);
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

            @Override // com.android.ims.internal.IImsUt
            public int updateCLIP(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateCLIP(enable);
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

            @Override // com.android.ims.internal.IImsUt
            public int updateCOLR(int presentation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(presentation);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateCOLR(presentation);
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

            @Override // com.android.ims.internal.IImsUt
            public int updateCOLP(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateCOLP(enable);
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

            @Override // com.android.ims.internal.IImsUt
            public void setListener(IImsUtListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsUt
            public int queryCallBarringForServiceClass(int cbType, int serviceClass) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cbType);
                    _data.writeInt(serviceClass);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryCallBarringForServiceClass(cbType, serviceClass);
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

            @Override // com.android.ims.internal.IImsUt
            public int updateCallBarringForServiceClass(int cbType, int action, String[] barrList, int serviceClass) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cbType);
                    _data.writeInt(action);
                    _data.writeStringArray(barrList);
                    _data.writeInt(serviceClass);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateCallBarringForServiceClass(cbType, action, barrList, serviceClass);
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

            @Override // com.android.ims.internal.IImsUt
            public IBinder getHwInnerService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwInnerService();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IImsUt impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IImsUt getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
