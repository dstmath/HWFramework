package com.android.internal.telephony.euicc;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwEuiccManager extends IInterface {
    void cancelSession() throws RemoteException;

    void requestDefaultSmdpAddress(String str, PendingIntent pendingIntent) throws RemoteException;

    void resetMemory(String str, int i, PendingIntent pendingIntent) throws RemoteException;

    void setDefaultSmdpAddress(String str, String str2, PendingIntent pendingIntent) throws RemoteException;

    public static class Default implements IHwEuiccManager {
        @Override // com.android.internal.telephony.euicc.IHwEuiccManager
        public void requestDefaultSmdpAddress(String cardId, PendingIntent callbackIntent) throws RemoteException {
        }

        @Override // com.android.internal.telephony.euicc.IHwEuiccManager
        public void resetMemory(String cardId, int options, PendingIntent callbackIntent) throws RemoteException {
        }

        @Override // com.android.internal.telephony.euicc.IHwEuiccManager
        public void setDefaultSmdpAddress(String cardId, String defaultSmdpAddress, PendingIntent callbackIntent) throws RemoteException {
        }

        @Override // com.android.internal.telephony.euicc.IHwEuiccManager
        public void cancelSession() throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwEuiccManager {
        private static final String DESCRIPTOR = "com.android.internal.telephony.euicc.IHwEuiccManager";
        static final int TRANSACTION_cancelSession = 4;
        static final int TRANSACTION_requestDefaultSmdpAddress = 1;
        static final int TRANSACTION_resetMemory = 2;
        static final int TRANSACTION_setDefaultSmdpAddress = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwEuiccManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwEuiccManager)) {
                return new Proxy(obj);
            }
            return (IHwEuiccManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PendingIntent _arg1;
            PendingIntent _arg2;
            PendingIntent _arg22;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                String _arg0 = data.readString();
                if (data.readInt() != 0) {
                    _arg1 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                requestDefaultSmdpAddress(_arg0, _arg1);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                String _arg02 = data.readString();
                int _arg12 = data.readInt();
                if (data.readInt() != 0) {
                    _arg2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                resetMemory(_arg02, _arg12, _arg2);
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                String _arg03 = data.readString();
                String _arg13 = data.readString();
                if (data.readInt() != 0) {
                    _arg22 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                } else {
                    _arg22 = null;
                }
                setDefaultSmdpAddress(_arg03, _arg13, _arg22);
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                cancelSession();
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwEuiccManager {
            public static IHwEuiccManager sDefaultImpl;
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

            @Override // com.android.internal.telephony.euicc.IHwEuiccManager
            public void requestDefaultSmdpAddress(String cardId, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cardId);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestDefaultSmdpAddress(cardId, callbackIntent);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.euicc.IHwEuiccManager
            public void resetMemory(String cardId, int options, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cardId);
                    _data.writeInt(options);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetMemory(cardId, options, callbackIntent);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.euicc.IHwEuiccManager
            public void setDefaultSmdpAddress(String cardId, String defaultSmdpAddress, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cardId);
                    _data.writeString(defaultSmdpAddress);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDefaultSmdpAddress(cardId, defaultSmdpAddress, callbackIntent);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.euicc.IHwEuiccManager
            public void cancelSession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelSession();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwEuiccManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwEuiccManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
