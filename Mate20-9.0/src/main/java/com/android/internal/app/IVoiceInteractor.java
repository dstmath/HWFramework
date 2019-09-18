package com.android.internal.app;

import android.app.VoiceInteractor;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.internal.app.IVoiceInteractorCallback;
import com.android.internal.app.IVoiceInteractorRequest;

public interface IVoiceInteractor extends IInterface {

    public static abstract class Stub extends Binder implements IVoiceInteractor {
        private static final String DESCRIPTOR = "com.android.internal.app.IVoiceInteractor";
        static final int TRANSACTION_startAbortVoice = 4;
        static final int TRANSACTION_startCommand = 5;
        static final int TRANSACTION_startCompleteVoice = 3;
        static final int TRANSACTION_startConfirmation = 1;
        static final int TRANSACTION_startPickOption = 2;
        static final int TRANSACTION_supportsCommands = 6;

        private static class Proxy implements IVoiceInteractor {
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
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return IVoiceInteractorRequest.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return IVoiceInteractorRequest.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return IVoiceInteractorRequest.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return IVoiceInteractorRequest.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return IVoiceInteractorRequest.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean[] supportsCommands(String callingPackage, String[] commands) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeStringArray(commands);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createBooleanArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

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
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                IBinder iBinder = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        IVoiceInteractorCallback _arg1 = IVoiceInteractorCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg2 = (VoiceInteractor.Prompt) VoiceInteractor.Prompt.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg3 = null;
                        }
                        IVoiceInteractorRequest _result = startConfirmation(_arg0, _arg1, _arg2, _arg3);
                        reply.writeNoException();
                        if (_result != null) {
                            iBinder = _result.asBinder();
                        }
                        parcel2.writeStrongBinder(iBinder);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        IVoiceInteractorCallback _arg12 = IVoiceInteractorCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg22 = (VoiceInteractor.Prompt) VoiceInteractor.Prompt.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg22 = null;
                        }
                        VoiceInteractor.PickOptionRequest.Option[] _arg35 = (VoiceInteractor.PickOptionRequest.Option[]) parcel.createTypedArray(VoiceInteractor.PickOptionRequest.Option.CREATOR);
                        if (data.readInt() != 0) {
                            _arg4 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg4 = null;
                        }
                        IVoiceInteractorRequest _result2 = startPickOption(_arg02, _arg12, _arg22, _arg35, _arg4);
                        reply.writeNoException();
                        if (_result2 != null) {
                            iBinder = _result2.asBinder();
                        }
                        parcel2.writeStrongBinder(iBinder);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        IVoiceInteractorCallback _arg13 = IVoiceInteractorCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg23 = (VoiceInteractor.Prompt) VoiceInteractor.Prompt.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg23 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg32 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg32 = null;
                        }
                        IVoiceInteractorRequest _result3 = startCompleteVoice(_arg03, _arg13, _arg23, _arg32);
                        reply.writeNoException();
                        if (_result3 != null) {
                            iBinder = _result3.asBinder();
                        }
                        parcel2.writeStrongBinder(iBinder);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        IVoiceInteractorCallback _arg14 = IVoiceInteractorCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg24 = (VoiceInteractor.Prompt) VoiceInteractor.Prompt.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg24 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg33 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg33 = null;
                        }
                        IVoiceInteractorRequest _result4 = startAbortVoice(_arg04, _arg14, _arg24, _arg33);
                        reply.writeNoException();
                        if (_result4 != null) {
                            iBinder = _result4.asBinder();
                        }
                        parcel2.writeStrongBinder(iBinder);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        IVoiceInteractorCallback _arg15 = IVoiceInteractorCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg25 = data.readString();
                        if (data.readInt() != 0) {
                            _arg34 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg34 = null;
                        }
                        IVoiceInteractorRequest _result5 = startCommand(_arg05, _arg15, _arg25, _arg34);
                        reply.writeNoException();
                        if (_result5 != null) {
                            iBinder = _result5.asBinder();
                        }
                        parcel2.writeStrongBinder(iBinder);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean[] _result6 = supportsCommands(data.readString(), data.createStringArray());
                        reply.writeNoException();
                        parcel2.writeBooleanArray(_result6);
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

    IVoiceInteractorRequest startAbortVoice(String str, IVoiceInteractorCallback iVoiceInteractorCallback, VoiceInteractor.Prompt prompt, Bundle bundle) throws RemoteException;

    IVoiceInteractorRequest startCommand(String str, IVoiceInteractorCallback iVoiceInteractorCallback, String str2, Bundle bundle) throws RemoteException;

    IVoiceInteractorRequest startCompleteVoice(String str, IVoiceInteractorCallback iVoiceInteractorCallback, VoiceInteractor.Prompt prompt, Bundle bundle) throws RemoteException;

    IVoiceInteractorRequest startConfirmation(String str, IVoiceInteractorCallback iVoiceInteractorCallback, VoiceInteractor.Prompt prompt, Bundle bundle) throws RemoteException;

    IVoiceInteractorRequest startPickOption(String str, IVoiceInteractorCallback iVoiceInteractorCallback, VoiceInteractor.Prompt prompt, VoiceInteractor.PickOptionRequest.Option[] optionArr, Bundle bundle) throws RemoteException;

    boolean[] supportsCommands(String str, String[] strArr) throws RemoteException;
}
