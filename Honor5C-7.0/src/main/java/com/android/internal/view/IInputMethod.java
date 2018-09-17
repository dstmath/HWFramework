package com.android.internal.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.view.InputChannel;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputMethodSubtype;

public interface IInputMethod extends IInterface {

    public static abstract class Stub extends Binder implements IInputMethod {
        private static final String DESCRIPTOR = "com.android.internal.view.IInputMethod";
        static final int TRANSACTION_attachToken = 1;
        static final int TRANSACTION_bindInput = 2;
        static final int TRANSACTION_changeInputMethodSubtype = 11;
        static final int TRANSACTION_createSession = 6;
        static final int TRANSACTION_hideSoftInput = 10;
        static final int TRANSACTION_restartInput = 5;
        static final int TRANSACTION_revokeSession = 8;
        static final int TRANSACTION_setSessionEnabled = 7;
        static final int TRANSACTION_showSoftInput = 9;
        static final int TRANSACTION_startInput = 4;
        static final int TRANSACTION_unbindInput = 3;

        private static class Proxy implements IInputMethod {
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

            public void attachToken(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_attachToken, _data, null, Stub.TRANSACTION_attachToken);
                } finally {
                    _data.recycle();
                }
            }

            public void bindInput(InputBinding binding) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (binding != null) {
                        _data.writeInt(Stub.TRANSACTION_attachToken);
                        binding.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_bindInput, _data, null, Stub.TRANSACTION_attachToken);
                } finally {
                    _data.recycle();
                }
            }

            public void unbindInput() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_unbindInput, _data, null, Stub.TRANSACTION_attachToken);
                } finally {
                    _data.recycle();
                }
            }

            public void startInput(IInputContext inputContext, int missingMethods, EditorInfo attribute) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (inputContext != null) {
                        iBinder = inputContext.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(missingMethods);
                    if (attribute != null) {
                        _data.writeInt(Stub.TRANSACTION_attachToken);
                        attribute.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startInput, _data, null, Stub.TRANSACTION_attachToken);
                } finally {
                    _data.recycle();
                }
            }

            public void restartInput(IInputContext inputContext, int missingMethods, EditorInfo attribute) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (inputContext != null) {
                        iBinder = inputContext.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(missingMethods);
                    if (attribute != null) {
                        _data.writeInt(Stub.TRANSACTION_attachToken);
                        attribute.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_restartInput, _data, null, Stub.TRANSACTION_attachToken);
                } finally {
                    _data.recycle();
                }
            }

            public void createSession(InputChannel channel, IInputSessionCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (channel != null) {
                        _data.writeInt(Stub.TRANSACTION_attachToken);
                        channel.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_createSession, _data, null, Stub.TRANSACTION_attachToken);
                } finally {
                    _data.recycle();
                }
            }

            public void setSessionEnabled(IInputMethodSession session, boolean enabled) throws RemoteException {
                int i = Stub.TRANSACTION_attachToken;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (!enabled) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setSessionEnabled, _data, null, Stub.TRANSACTION_attachToken);
                } finally {
                    _data.recycle();
                }
            }

            public void revokeSession(IInputMethodSession session) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_revokeSession, _data, null, Stub.TRANSACTION_attachToken);
                } finally {
                    _data.recycle();
                }
            }

            public void showSoftInput(int flags, ResultReceiver resultReceiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    if (resultReceiver != null) {
                        _data.writeInt(Stub.TRANSACTION_attachToken);
                        resultReceiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_showSoftInput, _data, null, Stub.TRANSACTION_attachToken);
                } finally {
                    _data.recycle();
                }
            }

            public void hideSoftInput(int flags, ResultReceiver resultReceiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    if (resultReceiver != null) {
                        _data.writeInt(Stub.TRANSACTION_attachToken);
                        resultReceiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_hideSoftInput, _data, null, Stub.TRANSACTION_attachToken);
                } finally {
                    _data.recycle();
                }
            }

            public void changeInputMethodSubtype(InputMethodSubtype subtype) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (subtype != null) {
                        _data.writeInt(Stub.TRANSACTION_attachToken);
                        subtype.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_changeInputMethodSubtype, _data, null, Stub.TRANSACTION_attachToken);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInputMethod asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInputMethod)) {
                return new Proxy(obj);
            }
            return (IInputMethod) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IInputContext _arg0;
            int _arg1;
            EditorInfo editorInfo;
            int _arg02;
            ResultReceiver resultReceiver;
            switch (code) {
                case TRANSACTION_attachToken /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    attachToken(data.readStrongBinder());
                    return true;
                case TRANSACTION_bindInput /*2*/:
                    InputBinding inputBinding;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        inputBinding = (InputBinding) InputBinding.CREATOR.createFromParcel(data);
                    } else {
                        inputBinding = null;
                    }
                    bindInput(inputBinding);
                    return true;
                case TRANSACTION_unbindInput /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    unbindInput();
                    return true;
                case TRANSACTION_startInput /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.internal.view.IInputContext.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        editorInfo = (EditorInfo) EditorInfo.CREATOR.createFromParcel(data);
                    } else {
                        editorInfo = null;
                    }
                    startInput(_arg0, _arg1, editorInfo);
                    return true;
                case TRANSACTION_restartInput /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = com.android.internal.view.IInputContext.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        editorInfo = (EditorInfo) EditorInfo.CREATOR.createFromParcel(data);
                    } else {
                        editorInfo = null;
                    }
                    restartInput(_arg0, _arg1, editorInfo);
                    return true;
                case TRANSACTION_createSession /*6*/:
                    InputChannel inputChannel;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        inputChannel = (InputChannel) InputChannel.CREATOR.createFromParcel(data);
                    } else {
                        inputChannel = null;
                    }
                    createSession(inputChannel, com.android.internal.view.IInputSessionCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_setSessionEnabled /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    setSessionEnabled(com.android.internal.view.IInputMethodSession.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0);
                    return true;
                case TRANSACTION_revokeSession /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    revokeSession(com.android.internal.view.IInputMethodSession.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_showSoftInput /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readInt();
                    if (data.readInt() != 0) {
                        resultReceiver = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(data);
                    } else {
                        resultReceiver = null;
                    }
                    showSoftInput(_arg02, resultReceiver);
                    return true;
                case TRANSACTION_hideSoftInput /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readInt();
                    if (data.readInt() != 0) {
                        resultReceiver = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(data);
                    } else {
                        resultReceiver = null;
                    }
                    hideSoftInput(_arg02, resultReceiver);
                    return true;
                case TRANSACTION_changeInputMethodSubtype /*11*/:
                    InputMethodSubtype inputMethodSubtype;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        inputMethodSubtype = (InputMethodSubtype) InputMethodSubtype.CREATOR.createFromParcel(data);
                    } else {
                        inputMethodSubtype = null;
                    }
                    changeInputMethodSubtype(inputMethodSubtype);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void attachToken(IBinder iBinder) throws RemoteException;

    void bindInput(InputBinding inputBinding) throws RemoteException;

    void changeInputMethodSubtype(InputMethodSubtype inputMethodSubtype) throws RemoteException;

    void createSession(InputChannel inputChannel, IInputSessionCallback iInputSessionCallback) throws RemoteException;

    void hideSoftInput(int i, ResultReceiver resultReceiver) throws RemoteException;

    void restartInput(IInputContext iInputContext, int i, EditorInfo editorInfo) throws RemoteException;

    void revokeSession(IInputMethodSession iInputMethodSession) throws RemoteException;

    void setSessionEnabled(IInputMethodSession iInputMethodSession, boolean z) throws RemoteException;

    void showSoftInput(int i, ResultReceiver resultReceiver) throws RemoteException;

    void startInput(IInputContext iInputContext, int i, EditorInfo editorInfo) throws RemoteException;

    void unbindInput() throws RemoteException;
}
