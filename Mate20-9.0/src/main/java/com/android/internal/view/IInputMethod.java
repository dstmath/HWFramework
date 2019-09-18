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
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodSession;
import com.android.internal.view.IInputSessionCallback;

public interface IInputMethod extends IInterface {

    public static abstract class Stub extends Binder implements IInputMethod {
        private static final String DESCRIPTOR = "com.android.internal.view.IInputMethod";
        static final int TRANSACTION_attachToken = 1;
        static final int TRANSACTION_bindInput = 2;
        static final int TRANSACTION_changeInputMethodSubtype = 10;
        static final int TRANSACTION_createSession = 5;
        static final int TRANSACTION_hideSoftInput = 9;
        static final int TRANSACTION_revokeSession = 7;
        static final int TRANSACTION_setSessionEnabled = 6;
        static final int TRANSACTION_showSoftInput = 8;
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
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void bindInput(InputBinding binding) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (binding != null) {
                        _data.writeInt(1);
                        binding.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void unbindInput() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void startInput(IBinder startInputToken, IInputContext inputContext, int missingMethods, EditorInfo attribute, boolean restarting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(startInputToken);
                    _data.writeStrongBinder(inputContext != null ? inputContext.asBinder() : null);
                    _data.writeInt(missingMethods);
                    if (attribute != null) {
                        _data.writeInt(1);
                        attribute.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(restarting);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void createSession(InputChannel channel, IInputSessionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (channel != null) {
                        _data.writeInt(1);
                        channel.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setSessionEnabled(IInputMethodSession session, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    _data.writeInt(enabled);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void revokeSession(IInputMethodSession session) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    this.mRemote.transact(7, _data, null, 1);
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
                        _data.writeInt(1);
                        resultReceiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, null, 1);
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
                        _data.writeInt(1);
                        resultReceiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void changeInputMethodSubtype(InputMethodSubtype subtype) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (subtype != null) {
                        _data.writeInt(1);
                        subtype.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, null, 1);
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

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v5, resolved type: android.view.inputmethod.InputBinding} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: android.view.inputmethod.InputBinding} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v22, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v26, resolved type: android.view.InputChannel} */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            if (i != 1598968902) {
                boolean _arg1 = false;
                InputChannel _arg0 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        attachToken(data.readStrongBinder());
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (InputBinding) InputBinding.CREATOR.createFromParcel(parcel);
                        }
                        bindInput(_arg0);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        unbindInput();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg02 = data.readStrongBinder();
                        IInputContext _arg12 = IInputContext.Stub.asInterface(data.readStrongBinder());
                        int _arg2 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = (EditorInfo) EditorInfo.CREATOR.createFromParcel(parcel);
                        }
                        startInput(_arg02, _arg12, _arg2, _arg0, data.readInt() != 0);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (InputChannel) InputChannel.CREATOR.createFromParcel(parcel);
                        }
                        createSession(_arg0, IInputSessionCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        IInputMethodSession _arg03 = IInputMethodSession.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setSessionEnabled(_arg03, _arg1);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        revokeSession(IInputMethodSession.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(parcel);
                        }
                        showSoftInput(_arg04, _arg0);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(parcel);
                        }
                        hideSoftInput(_arg05, _arg0);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (InputMethodSubtype) InputMethodSubtype.CREATOR.createFromParcel(parcel);
                        }
                        changeInputMethodSubtype(_arg0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void attachToken(IBinder iBinder) throws RemoteException;

    void bindInput(InputBinding inputBinding) throws RemoteException;

    void changeInputMethodSubtype(InputMethodSubtype inputMethodSubtype) throws RemoteException;

    void createSession(InputChannel inputChannel, IInputSessionCallback iInputSessionCallback) throws RemoteException;

    void hideSoftInput(int i, ResultReceiver resultReceiver) throws RemoteException;

    void revokeSession(IInputMethodSession iInputMethodSession) throws RemoteException;

    void setSessionEnabled(IInputMethodSession iInputMethodSession, boolean z) throws RemoteException;

    void showSoftInput(int i, ResultReceiver resultReceiver) throws RemoteException;

    void startInput(IBinder iBinder, IInputContext iInputContext, int i, EditorInfo editorInfo, boolean z) throws RemoteException;

    void unbindInput() throws RemoteException;
}
