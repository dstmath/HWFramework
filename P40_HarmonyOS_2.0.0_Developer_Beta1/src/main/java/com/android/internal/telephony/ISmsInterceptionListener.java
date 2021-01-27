package com.android.internal.telephony;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISmsInterceptionListener extends IInterface {
    int handleSmsDeliverActionInner(Bundle bundle) throws RemoteException;

    int handleWapPushDeliverActionInner(Bundle bundle) throws RemoteException;

    boolean sendNumberBlockedRecordInner(Bundle bundle) throws RemoteException;

    public static class Default implements ISmsInterceptionListener {
        @Override // com.android.internal.telephony.ISmsInterceptionListener
        public int handleSmsDeliverActionInner(Bundle smsInfo) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ISmsInterceptionListener
        public int handleWapPushDeliverActionInner(Bundle wapPushInfo) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ISmsInterceptionListener
        public boolean sendNumberBlockedRecordInner(Bundle smsInfo) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISmsInterceptionListener {
        private static final String DESCRIPTOR = "com.android.internal.telephony.ISmsInterceptionListener";
        static final int TRANSACTION_handleSmsDeliverActionInner = 1;
        static final int TRANSACTION_handleWapPushDeliverActionInner = 2;
        static final int TRANSACTION_sendNumberBlockedRecordInner = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISmsInterceptionListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISmsInterceptionListener)) {
                return new Proxy(obj);
            }
            return (ISmsInterceptionListener) iin;
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
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                int _result = handleSmsDeliverActionInner(_arg0);
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                int _result2 = handleWapPushDeliverActionInner(_arg02);
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg03 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg03 = null;
                }
                boolean sendNumberBlockedRecordInner = sendNumberBlockedRecordInner(_arg03);
                reply.writeNoException();
                reply.writeInt(sendNumberBlockedRecordInner ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ISmsInterceptionListener {
            public static ISmsInterceptionListener sDefaultImpl;
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

            @Override // com.android.internal.telephony.ISmsInterceptionListener
            public int handleSmsDeliverActionInner(Bundle smsInfo) throws RemoteException {
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
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().handleSmsDeliverActionInner(smsInfo);
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

            @Override // com.android.internal.telephony.ISmsInterceptionListener
            public int handleWapPushDeliverActionInner(Bundle wapPushInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (wapPushInfo != null) {
                        _data.writeInt(1);
                        wapPushInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().handleWapPushDeliverActionInner(wapPushInfo);
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

            @Override // com.android.internal.telephony.ISmsInterceptionListener
            public boolean sendNumberBlockedRecordInner(Bundle smsInfo) throws RemoteException {
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
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendNumberBlockedRecordInner(smsInfo);
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
        }

        public static boolean setDefaultImpl(ISmsInterceptionListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISmsInterceptionListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
