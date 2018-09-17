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
                    this.mRemote.transact(Stub.TRANSACTION_utConfigurationUpdated, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_utConfigurationUpdated);
                        error.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_utConfigurationUpdateFailed, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_utConfigurationUpdated);
                        ssInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_utConfigurationQueried, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_utConfigurationUpdated);
                        error.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_utConfigurationQueryFailed, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_utConfigurationCallBarringQueried, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_utConfigurationCallForwardQueried, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_utConfigurationCallWaitingQueried, _data, _reply, 0);
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
            ImsReasonInfo imsReasonInfo;
            switch (code) {
                case TRANSACTION_utConfigurationUpdated /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    utConfigurationUpdated(com.android.ims.internal.IImsUt.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_utConfigurationUpdateFailed /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsUt.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        imsReasonInfo = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        imsReasonInfo = null;
                    }
                    utConfigurationUpdateFailed(_arg0, _arg1, imsReasonInfo);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_utConfigurationQueried /*3*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsUt.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    utConfigurationQueried(_arg0, _arg1, bundle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_utConfigurationQueryFailed /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.ims.internal.IImsUt.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        imsReasonInfo = (ImsReasonInfo) ImsReasonInfo.CREATOR.createFromParcel(data);
                    } else {
                        imsReasonInfo = null;
                    }
                    utConfigurationQueryFailed(_arg0, _arg1, imsReasonInfo);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_utConfigurationCallBarringQueried /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    utConfigurationCallBarringQueried(com.android.ims.internal.IImsUt.Stub.asInterface(data.readStrongBinder()), data.readInt(), (ImsSsInfo[]) data.createTypedArray(ImsSsInfo.CREATOR));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_utConfigurationCallForwardQueried /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    utConfigurationCallForwardQueried(com.android.ims.internal.IImsUt.Stub.asInterface(data.readStrongBinder()), data.readInt(), (ImsCallForwardInfo[]) data.createTypedArray(ImsCallForwardInfo.CREATOR));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_utConfigurationCallWaitingQueried /*7*/:
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
