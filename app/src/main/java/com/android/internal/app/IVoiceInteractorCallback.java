package com.android.internal.app;

import android.app.VoiceInteractor.PickOptionRequest.Option;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IVoiceInteractorCallback extends IInterface {

    public static abstract class Stub extends Binder implements IVoiceInteractorCallback {
        private static final String DESCRIPTOR = "com.android.internal.app.IVoiceInteractorCallback";
        static final int TRANSACTION_deliverAbortVoiceResult = 4;
        static final int TRANSACTION_deliverCancel = 6;
        static final int TRANSACTION_deliverCommandResult = 5;
        static final int TRANSACTION_deliverCompleteVoiceResult = 3;
        static final int TRANSACTION_deliverConfirmationResult = 1;
        static final int TRANSACTION_deliverPickOptionResult = 2;

        private static class Proxy implements IVoiceInteractorCallback {
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

            public void deliverConfirmationResult(IVoiceInteractorRequest request, boolean confirmed, Bundle result) throws RemoteException {
                IBinder iBinder = null;
                int i = Stub.TRANSACTION_deliverConfirmationResult;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        iBinder = request.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (!confirmed) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (result != null) {
                        _data.writeInt(Stub.TRANSACTION_deliverConfirmationResult);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_deliverConfirmationResult, _data, null, Stub.TRANSACTION_deliverConfirmationResult);
                } finally {
                    _data.recycle();
                }
            }

            public void deliverPickOptionResult(IVoiceInteractorRequest request, boolean finished, Option[] selections, Bundle result) throws RemoteException {
                int i = Stub.TRANSACTION_deliverConfirmationResult;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        iBinder = request.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (!finished) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeTypedArray(selections, 0);
                    if (result != null) {
                        _data.writeInt(Stub.TRANSACTION_deliverConfirmationResult);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_deliverPickOptionResult, _data, null, Stub.TRANSACTION_deliverConfirmationResult);
                } finally {
                    _data.recycle();
                }
            }

            public void deliverCompleteVoiceResult(IVoiceInteractorRequest request, Bundle result) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        iBinder = request.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (result != null) {
                        _data.writeInt(Stub.TRANSACTION_deliverConfirmationResult);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_deliverCompleteVoiceResult, _data, null, Stub.TRANSACTION_deliverConfirmationResult);
                } finally {
                    _data.recycle();
                }
            }

            public void deliverAbortVoiceResult(IVoiceInteractorRequest request, Bundle result) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        iBinder = request.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (result != null) {
                        _data.writeInt(Stub.TRANSACTION_deliverConfirmationResult);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_deliverAbortVoiceResult, _data, null, Stub.TRANSACTION_deliverConfirmationResult);
                } finally {
                    _data.recycle();
                }
            }

            public void deliverCommandResult(IVoiceInteractorRequest request, boolean finished, Bundle result) throws RemoteException {
                int i = Stub.TRANSACTION_deliverConfirmationResult;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        iBinder = request.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (!finished) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (result != null) {
                        _data.writeInt(Stub.TRANSACTION_deliverConfirmationResult);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_deliverCommandResult, _data, null, Stub.TRANSACTION_deliverConfirmationResult);
                } finally {
                    _data.recycle();
                }
            }

            public void deliverCancel(IVoiceInteractorRequest request) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        iBinder = request.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_deliverCancel, _data, null, Stub.TRANSACTION_deliverConfirmationResult);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVoiceInteractorCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVoiceInteractorCallback)) {
                return new Proxy(obj);
            }
            return (IVoiceInteractorCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IVoiceInteractorRequest _arg0;
            boolean _arg1;
            Bundle bundle;
            Bundle bundle2;
            switch (code) {
                case TRANSACTION_deliverConfirmationResult /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.internal.app.IVoiceInteractorRequest.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    deliverConfirmationResult(_arg0, _arg1, bundle);
                    return true;
                case TRANSACTION_deliverPickOptionResult /*2*/:
                    Bundle bundle3;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.internal.app.IVoiceInteractorRequest.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readInt() != 0;
                    Option[] _arg2 = (Option[]) data.createTypedArray(Option.CREATOR);
                    if (data.readInt() != 0) {
                        bundle3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle3 = null;
                    }
                    deliverPickOptionResult(_arg0, _arg1, _arg2, bundle3);
                    return true;
                case TRANSACTION_deliverCompleteVoiceResult /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.internal.app.IVoiceInteractorRequest.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    deliverCompleteVoiceResult(_arg0, bundle2);
                    return true;
                case TRANSACTION_deliverAbortVoiceResult /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.internal.app.IVoiceInteractorRequest.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    deliverAbortVoiceResult(_arg0, bundle2);
                    return true;
                case TRANSACTION_deliverCommandResult /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.internal.app.IVoiceInteractorRequest.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    deliverCommandResult(_arg0, _arg1, bundle);
                    return true;
                case TRANSACTION_deliverCancel /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    deliverCancel(com.android.internal.app.IVoiceInteractorRequest.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void deliverAbortVoiceResult(IVoiceInteractorRequest iVoiceInteractorRequest, Bundle bundle) throws RemoteException;

    void deliverCancel(IVoiceInteractorRequest iVoiceInteractorRequest) throws RemoteException;

    void deliverCommandResult(IVoiceInteractorRequest iVoiceInteractorRequest, boolean z, Bundle bundle) throws RemoteException;

    void deliverCompleteVoiceResult(IVoiceInteractorRequest iVoiceInteractorRequest, Bundle bundle) throws RemoteException;

    void deliverConfirmationResult(IVoiceInteractorRequest iVoiceInteractorRequest, boolean z, Bundle bundle) throws RemoteException;

    void deliverPickOptionResult(IVoiceInteractorRequest iVoiceInteractorRequest, boolean z, Option[] optionArr, Bundle bundle) throws RemoteException;
}
