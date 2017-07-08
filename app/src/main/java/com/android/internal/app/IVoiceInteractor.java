package com.android.internal.app;

import android.app.VoiceInteractor.PickOptionRequest.Option;
import android.app.VoiceInteractor.Prompt;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

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

            public IVoiceInteractorRequest startConfirmation(String callingPackage, IVoiceInteractorCallback callback, Prompt prompt, Bundle extras) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (prompt != null) {
                        _data.writeInt(Stub.TRANSACTION_startConfirmation);
                        prompt.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_startConfirmation);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startConfirmation, _data, _reply, 0);
                    _reply.readException();
                    IVoiceInteractorRequest _result = com.android.internal.app.IVoiceInteractorRequest.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IVoiceInteractorRequest startPickOption(String callingPackage, IVoiceInteractorCallback callback, Prompt prompt, Option[] options, Bundle extras) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (prompt != null) {
                        _data.writeInt(Stub.TRANSACTION_startConfirmation);
                        prompt.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedArray(options, 0);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_startConfirmation);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startPickOption, _data, _reply, 0);
                    _reply.readException();
                    IVoiceInteractorRequest _result = com.android.internal.app.IVoiceInteractorRequest.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IVoiceInteractorRequest startCompleteVoice(String callingPackage, IVoiceInteractorCallback callback, Prompt prompt, Bundle extras) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (prompt != null) {
                        _data.writeInt(Stub.TRANSACTION_startConfirmation);
                        prompt.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_startConfirmation);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startCompleteVoice, _data, _reply, 0);
                    _reply.readException();
                    IVoiceInteractorRequest _result = com.android.internal.app.IVoiceInteractorRequest.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IVoiceInteractorRequest startAbortVoice(String callingPackage, IVoiceInteractorCallback callback, Prompt prompt, Bundle extras) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (prompt != null) {
                        _data.writeInt(Stub.TRANSACTION_startConfirmation);
                        prompt.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_startConfirmation);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startAbortVoice, _data, _reply, 0);
                    _reply.readException();
                    IVoiceInteractorRequest _result = com.android.internal.app.IVoiceInteractorRequest.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IVoiceInteractorRequest startCommand(String callingPackage, IVoiceInteractorCallback callback, String command, Bundle extras) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(command);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_startConfirmation);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startCommand, _data, _reply, 0);
                    _reply.readException();
                    IVoiceInteractorRequest _result = com.android.internal.app.IVoiceInteractorRequest.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_supportsCommands, _data, _reply, 0);
                    _reply.readException();
                    boolean[] _result = _reply.createBooleanArray();
                    return _result;
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
            String _arg0;
            IVoiceInteractorCallback _arg1;
            Prompt prompt;
            Bundle bundle;
            IVoiceInteractorRequest _result;
            switch (code) {
                case TRANSACTION_startConfirmation /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = com.android.internal.app.IVoiceInteractorCallback.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        prompt = (Prompt) Prompt.CREATOR.createFromParcel(data);
                    } else {
                        prompt = null;
                    }
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = startConfirmation(_arg0, _arg1, prompt, bundle);
                    reply.writeNoException();
                    reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                    return true;
                case TRANSACTION_startPickOption /*2*/:
                    Bundle bundle2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = com.android.internal.app.IVoiceInteractorCallback.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        prompt = (Prompt) Prompt.CREATOR.createFromParcel(data);
                    } else {
                        prompt = null;
                    }
                    Option[] _arg3 = (Option[]) data.createTypedArray(Option.CREATOR);
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    _result = startPickOption(_arg0, _arg1, prompt, _arg3, bundle2);
                    reply.writeNoException();
                    reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                    return true;
                case TRANSACTION_startCompleteVoice /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = com.android.internal.app.IVoiceInteractorCallback.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        prompt = (Prompt) Prompt.CREATOR.createFromParcel(data);
                    } else {
                        prompt = null;
                    }
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = startCompleteVoice(_arg0, _arg1, prompt, bundle);
                    reply.writeNoException();
                    reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                    return true;
                case TRANSACTION_startAbortVoice /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = com.android.internal.app.IVoiceInteractorCallback.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        prompt = (Prompt) Prompt.CREATOR.createFromParcel(data);
                    } else {
                        prompt = null;
                    }
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = startAbortVoice(_arg0, _arg1, prompt, bundle);
                    reply.writeNoException();
                    reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                    return true;
                case TRANSACTION_startCommand /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = com.android.internal.app.IVoiceInteractorCallback.Stub.asInterface(data.readStrongBinder());
                    String _arg2 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = startCommand(_arg0, _arg1, _arg2, bundle);
                    reply.writeNoException();
                    reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                    return true;
                case TRANSACTION_supportsCommands /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean[] _result2 = supportsCommands(data.readString(), data.createStringArray());
                    reply.writeNoException();
                    reply.writeBooleanArray(_result2);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    IVoiceInteractorRequest startAbortVoice(String str, IVoiceInteractorCallback iVoiceInteractorCallback, Prompt prompt, Bundle bundle) throws RemoteException;

    IVoiceInteractorRequest startCommand(String str, IVoiceInteractorCallback iVoiceInteractorCallback, String str2, Bundle bundle) throws RemoteException;

    IVoiceInteractorRequest startCompleteVoice(String str, IVoiceInteractorCallback iVoiceInteractorCallback, Prompt prompt, Bundle bundle) throws RemoteException;

    IVoiceInteractorRequest startConfirmation(String str, IVoiceInteractorCallback iVoiceInteractorCallback, Prompt prompt, Bundle bundle) throws RemoteException;

    IVoiceInteractorRequest startPickOption(String str, IVoiceInteractorCallback iVoiceInteractorCallback, Prompt prompt, Option[] optionArr, Bundle bundle) throws RemoteException;

    boolean[] supportsCommands(String str, String[] strArr) throws RemoteException;
}
