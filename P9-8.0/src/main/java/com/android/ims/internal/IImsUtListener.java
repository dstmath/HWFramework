package com.android.ims.internal;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.ims.ImsCallForwardInfo;
import com.android.ims.ImsReasonInfo;
import com.android.ims.ImsSsInfo;

public interface IImsUtListener extends IInterface {

    public static abstract class Stub extends Binder implements IImsUtListener {
        private static final String DESCRIPTOR = "com.android.ims.internal.IImsUtListener";
        static final int TRANSACTION_utConfigurationCallBarringQueried = 5;
        static final int TRANSACTION_utConfigurationCallForwardQueried = 6;
        static final int TRANSACTION_utConfigurationCallWaitingQueried = 7;
        static final int TRANSACTION_utConfigurationQueried = 3;
        static final int TRANSACTION_utConfigurationQueryFailed = 4;
        static final int TRANSACTION_utConfigurationUpdateFailed = 2;
        static final int TRANSACTION_utConfigurationUpdated = 1;

        private static class Proxy implements IImsUtListener {
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

            public void utConfigurationUpdated(IImsUt ut, int id) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ut != null) {
                        iBinder = ut.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(id);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void utConfigurationUpdateFailed(IImsUt ut, int id, ImsReasonInfo error) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ut != null) {
                        iBinder = ut.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(id);
                    if (error != null) {
                        _data.writeInt(1);
                        error.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void utConfigurationQueried(IImsUt ut, int id, Bundle ssInfo) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ut != null) {
                        iBinder = ut.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(id);
                    if (ssInfo != null) {
                        _data.writeInt(1);
                        ssInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void utConfigurationQueryFailed(IImsUt ut, int id, ImsReasonInfo error) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ut != null) {
                        iBinder = ut.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(id);
                    if (error != null) {
                        _data.writeInt(1);
                        error.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void utConfigurationCallBarringQueried(IImsUt ut, int id, ImsSsInfo[] cbInfo) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ut != null) {
                        iBinder = ut.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(id);
                    _data.writeTypedArray(cbInfo, 0);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void utConfigurationCallForwardQueried(IImsUt ut, int id, ImsCallForwardInfo[] cfInfo) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ut != null) {
                        iBinder = ut.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(id);
                    _data.writeTypedArray(cfInfo, 0);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void utConfigurationCallWaitingQueried(IImsUt ut, int id, ImsSsInfo[] cwInfo) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ut != null) {
                        iBinder = ut.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(id);
                    _data.writeTypedArray(cwInfo, 0);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImsUtListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImsUtListener)) {
                return new Proxy(obj);
            }
            return (IImsUtListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IImsUt _arg0;
            int _arg1;
            ImsReasonInfo _arg2;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    utConfigurationUpdated(com.android.ims.internal.IImsUt.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsUt.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    utConfigurationUpdateFailed(_arg0, _arg1, _arg2);
                    reply.writeNoException();
                    return true;
                case 3:
                    Bundle _arg22;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsUt.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg22 = null;
                    }
                    utConfigurationQueried(_arg0, _arg1, _arg22);
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsUt.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    utConfigurationQueryFailed(_arg0, _arg1, _arg2);
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    utConfigurationCallBarringQueried(com.android.ims.internal.IImsUt.Stub.asInterface(data.readStrongBinder()), data.readInt(), (ImsSsInfo[]) data.createTypedArray(ImsSsInfo.CREATOR));
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    utConfigurationCallForwardQueried(com.android.ims.internal.IImsUt.Stub.asInterface(data.readStrongBinder()), data.readInt(), (ImsCallForwardInfo[]) data.createTypedArray(ImsCallForwardInfo.CREATOR));
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    utConfigurationCallWaitingQueried(com.android.ims.internal.IImsUt.Stub.asInterface(data.readStrongBinder()), data.readInt(), (ImsSsInfo[]) data.createTypedArray(ImsSsInfo.CREATOR));
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void utConfigurationCallBarringQueried(IImsUt iImsUt, int i, ImsSsInfo[] imsSsInfoArr) throws RemoteException;

    void utConfigurationCallForwardQueried(IImsUt iImsUt, int i, ImsCallForwardInfo[] imsCallForwardInfoArr) throws RemoteException;

    void utConfigurationCallWaitingQueried(IImsUt iImsUt, int i, ImsSsInfo[] imsSsInfoArr) throws RemoteException;

    void utConfigurationQueried(IImsUt iImsUt, int i, Bundle bundle) throws RemoteException;

    void utConfigurationQueryFailed(IImsUt iImsUt, int i, ImsReasonInfo imsReasonInfo) throws RemoteException;

    void utConfigurationUpdateFailed(IImsUt iImsUt, int i, ImsReasonInfo imsReasonInfo) throws RemoteException;

    void utConfigurationUpdated(IImsUt iImsUt, int i) throws RemoteException;
}
