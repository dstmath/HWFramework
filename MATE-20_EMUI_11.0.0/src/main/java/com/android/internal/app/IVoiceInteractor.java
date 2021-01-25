package com.android.internal.app;

import android.app.VoiceInteractor;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ICancellationSignal;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.internal.app.IVoiceInteractorCallback;
import com.android.internal.app.IVoiceInteractorRequest;

public interface IVoiceInteractor extends IInterface {
    void notifyDirectActionsChanged(int i, IBinder iBinder) throws RemoteException;

    void setKillCallback(ICancellationSignal iCancellationSignal) throws RemoteException;

    IVoiceInteractorRequest startAbortVoice(String str, IVoiceInteractorCallback iVoiceInteractorCallback, VoiceInteractor.Prompt prompt, Bundle bundle) throws RemoteException;

    IVoiceInteractorRequest startCommand(String str, IVoiceInteractorCallback iVoiceInteractorCallback, String str2, Bundle bundle) throws RemoteException;

    IVoiceInteractorRequest startCompleteVoice(String str, IVoiceInteractorCallback iVoiceInteractorCallback, VoiceInteractor.Prompt prompt, Bundle bundle) throws RemoteException;

    IVoiceInteractorRequest startConfirmation(String str, IVoiceInteractorCallback iVoiceInteractorCallback, VoiceInteractor.Prompt prompt, Bundle bundle) throws RemoteException;

    IVoiceInteractorRequest startPickOption(String str, IVoiceInteractorCallback iVoiceInteractorCallback, VoiceInteractor.Prompt prompt, VoiceInteractor.PickOptionRequest.Option[] optionArr, Bundle bundle) throws RemoteException;

    boolean[] supportsCommands(String str, String[] strArr) throws RemoteException;

    public static class Default implements IVoiceInteractor {
        @Override // com.android.internal.app.IVoiceInteractor
        public IVoiceInteractorRequest startConfirmation(String callingPackage, IVoiceInteractorCallback callback, VoiceInteractor.Prompt prompt, Bundle extras) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.IVoiceInteractor
        public IVoiceInteractorRequest startPickOption(String callingPackage, IVoiceInteractorCallback callback, VoiceInteractor.Prompt prompt, VoiceInteractor.PickOptionRequest.Option[] options, Bundle extras) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.IVoiceInteractor
        public IVoiceInteractorRequest startCompleteVoice(String callingPackage, IVoiceInteractorCallback callback, VoiceInteractor.Prompt prompt, Bundle extras) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.IVoiceInteractor
        public IVoiceInteractorRequest startAbortVoice(String callingPackage, IVoiceInteractorCallback callback, VoiceInteractor.Prompt prompt, Bundle extras) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.IVoiceInteractor
        public IVoiceInteractorRequest startCommand(String callingPackage, IVoiceInteractorCallback callback, String command, Bundle extras) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.IVoiceInteractor
        public boolean[] supportsCommands(String callingPackage, String[] commands) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.IVoiceInteractor
        public void notifyDirectActionsChanged(int taskId, IBinder assistToken) throws RemoteException {
        }

        @Override // com.android.internal.app.IVoiceInteractor
        public void setKillCallback(ICancellationSignal callback) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IVoiceInteractor {
        private static final String DESCRIPTOR = "com.android.internal.app.IVoiceInteractor";
        static final int TRANSACTION_notifyDirectActionsChanged = 7;
        static final int TRANSACTION_setKillCallback = 8;
        static final int TRANSACTION_startAbortVoice = 4;
        static final int TRANSACTION_startCommand = 5;
        static final int TRANSACTION_startCompleteVoice = 3;
        static final int TRANSACTION_startConfirmation = 1;
        static final int TRANSACTION_startPickOption = 2;
        static final int TRANSACTION_supportsCommands = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVoiceInteractor asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVoiceInteractor)) {
                return new Proxy(obj);
            }
            return (IVoiceInteractor) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "startConfirmation";
                case 2:
                    return "startPickOption";
                case 3:
                    return "startCompleteVoice";
                case 4:
                    return "startAbortVoice";
                case 5:
                    return "startCommand";
                case 6:
                    return "supportsCommands";
                case 7:
                    return "notifyDirectActionsChanged";
                case 8:
                    return "setKillCallback";
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
            VoiceInteractor.Prompt _arg2;
            Bundle _arg3;
            VoiceInteractor.Prompt _arg22;
            Bundle _arg4;
            VoiceInteractor.Prompt _arg23;
            Bundle _arg32;
            VoiceInteractor.Prompt _arg24;
            Bundle _arg33;
            Bundle _arg34;
            if (code != 1598968902) {
                IBinder iBinder = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        IVoiceInteractorCallback _arg1 = IVoiceInteractorCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg2 = VoiceInteractor.Prompt.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        IVoiceInteractorRequest _result = startConfirmation(_arg0, _arg1, _arg2, _arg3);
                        reply.writeNoException();
                        if (_result != null) {
                            iBinder = _result.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        IVoiceInteractorCallback _arg12 = IVoiceInteractorCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg22 = VoiceInteractor.Prompt.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        VoiceInteractor.PickOptionRequest.Option[] _arg35 = (VoiceInteractor.PickOptionRequest.Option[]) data.createTypedArray(VoiceInteractor.PickOptionRequest.Option.CREATOR);
                        if (data.readInt() != 0) {
                            _arg4 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        IVoiceInteractorRequest _result2 = startPickOption(_arg02, _arg12, _arg22, _arg35, _arg4);
                        reply.writeNoException();
                        if (_result2 != null) {
                            iBinder = _result2.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        IVoiceInteractorCallback _arg13 = IVoiceInteractorCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg23 = VoiceInteractor.Prompt.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg32 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        IVoiceInteractorRequest _result3 = startCompleteVoice(_arg03, _arg13, _arg23, _arg32);
                        reply.writeNoException();
                        if (_result3 != null) {
                            iBinder = _result3.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        IVoiceInteractorCallback _arg14 = IVoiceInteractorCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg24 = VoiceInteractor.Prompt.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg33 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg33 = null;
                        }
                        IVoiceInteractorRequest _result4 = startAbortVoice(_arg04, _arg14, _arg24, _arg33);
                        reply.writeNoException();
                        if (_result4 != null) {
                            iBinder = _result4.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        IVoiceInteractorCallback _arg15 = IVoiceInteractorCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg25 = data.readString();
                        if (data.readInt() != 0) {
                            _arg34 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg34 = null;
                        }
                        IVoiceInteractorRequest _result5 = startCommand(_arg05, _arg15, _arg25, _arg34);
                        reply.writeNoException();
                        if (_result5 != null) {
                            iBinder = _result5.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean[] _result6 = supportsCommands(data.readString(), data.createStringArray());
                        reply.writeNoException();
                        reply.writeBooleanArray(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        notifyDirectActionsChanged(data.readInt(), data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        setKillCallback(ICancellationSignal.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
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
        public static class Proxy implements IVoiceInteractor {
            public static IVoiceInteractor sDefaultImpl;
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

            @Override // com.android.internal.app.IVoiceInteractor
            public IVoiceInteractorRequest startConfirmation(String callingPackage, IVoiceInteractorCallback callback, VoiceInteractor.Prompt prompt, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (prompt != null) {
                        _data.writeInt(1);
                        prompt.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startConfirmation(callingPackage, callback, prompt, extras);
                    }
                    _reply.readException();
                    IVoiceInteractorRequest _result = IVoiceInteractorRequest.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractor
            public IVoiceInteractorRequest startPickOption(String callingPackage, IVoiceInteractorCallback callback, VoiceInteractor.Prompt prompt, VoiceInteractor.PickOptionRequest.Option[] options, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (prompt != null) {
                        _data.writeInt(1);
                        prompt.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedArray(options, 0);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startPickOption(callingPackage, callback, prompt, options, extras);
                    }
                    _reply.readException();
                    IVoiceInteractorRequest _result = IVoiceInteractorRequest.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractor
            public IVoiceInteractorRequest startCompleteVoice(String callingPackage, IVoiceInteractorCallback callback, VoiceInteractor.Prompt prompt, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (prompt != null) {
                        _data.writeInt(1);
                        prompt.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startCompleteVoice(callingPackage, callback, prompt, extras);
                    }
                    _reply.readException();
                    IVoiceInteractorRequest _result = IVoiceInteractorRequest.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractor
            public IVoiceInteractorRequest startAbortVoice(String callingPackage, IVoiceInteractorCallback callback, VoiceInteractor.Prompt prompt, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (prompt != null) {
                        _data.writeInt(1);
                        prompt.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startAbortVoice(callingPackage, callback, prompt, extras);
                    }
                    _reply.readException();
                    IVoiceInteractorRequest _result = IVoiceInteractorRequest.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractor
            public IVoiceInteractorRequest startCommand(String callingPackage, IVoiceInteractorCallback callback, String command, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(command);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startCommand(callingPackage, callback, command, extras);
                    }
                    _reply.readException();
                    IVoiceInteractorRequest _result = IVoiceInteractorRequest.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractor
            public boolean[] supportsCommands(String callingPackage, String[] commands) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeStringArray(commands);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supportsCommands(callingPackage, commands);
                    }
                    _reply.readException();
                    boolean[] _result = _reply.createBooleanArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractor
            public void notifyDirectActionsChanged(int taskId, IBinder assistToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeStrongBinder(assistToken);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyDirectActionsChanged(taskId, assistToken);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IVoiceInteractor
            public void setKillCallback(ICancellationSignal callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setKillCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IVoiceInteractor impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IVoiceInteractor getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
