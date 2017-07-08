package com.android.internal.telephony;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISmsInterceptionListener extends IInterface {

    public static abstract class Stub extends Binder implements ISmsInterceptionListener {
        private static final String DESCRIPTOR = "com.android.internal.telephony.ISmsInterceptionListener";
        static final int TRANSACTION_handleSmsDeliverActionInner = 1;
        static final int TRANSACTION_handleWapPushDeliverActionInner = 2;
        static final int TRANSACTION_sendNumberBlockedRecordInner = 3;

        private static class Proxy implements ISmsInterceptionListener {
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

            public int handleSmsDeliverActionInner(Bundle smsInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (smsInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_handleSmsDeliverActionInner);
                        smsInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_handleSmsDeliverActionInner, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int handleWapPushDeliverActionInner(Bundle wapPushInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (wapPushInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_handleSmsDeliverActionInner);
                        wapPushInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_handleWapPushDeliverActionInner, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean sendNumberBlockedRecordInner(Bundle smsInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (smsInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_handleSmsDeliverActionInner);
                        smsInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendNumberBlockedRecordInner, _data, _reply, 0);
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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            Bundle bundle;
            int _result;
            switch (code) {
                case TRANSACTION_handleSmsDeliverActionInner /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = handleSmsDeliverActionInner(bundle);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_handleWapPushDeliverActionInner /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = handleWapPushDeliverActionInner(bundle);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_sendNumberBlockedRecordInner /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    boolean _result2 = sendNumberBlockedRecordInner(bundle);
                    reply.writeNoException();
                    if (_result2) {
                        i = TRANSACTION_handleSmsDeliverActionInner;
                    }
                    reply.writeInt(i);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int handleSmsDeliverActionInner(Bundle bundle) throws RemoteException;

    int handleWapPushDeliverActionInner(Bundle bundle) throws RemoteException;

    boolean sendNumberBlockedRecordInner(Bundle bundle) throws RemoteException;
}
