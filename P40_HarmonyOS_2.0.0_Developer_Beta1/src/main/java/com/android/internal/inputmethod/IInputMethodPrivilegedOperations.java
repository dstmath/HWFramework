package com.android.internal.inputmethod;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.inputmethod.IInputContentUriToken;

public interface IInputMethodPrivilegedOperations extends IInterface {
    void applyImeVisibility(boolean z) throws RemoteException;

    IInputContentUriToken createInputContentUriToken(Uri uri, String str) throws RemoteException;

    void hideMySoftInput(int i) throws RemoteException;

    void notifyUserAction() throws RemoteException;

    void reportFullscreenMode(boolean z) throws RemoteException;

    void reportPreRendered(EditorInfo editorInfo) throws RemoteException;

    void reportStartInput(IBinder iBinder) throws RemoteException;

    void setImeWindowStatus(int i, int i2) throws RemoteException;

    void setInputMethod(String str) throws RemoteException;

    void setInputMethodAndSubtype(String str, InputMethodSubtype inputMethodSubtype) throws RemoteException;

    boolean shouldOfferSwitchingToNextInputMethod() throws RemoteException;

    void showMySoftInput(int i) throws RemoteException;

    boolean switchToNextInputMethod(boolean z) throws RemoteException;

    boolean switchToPreviousInputMethod() throws RemoteException;

    void updateStatusIcon(String str, int i) throws RemoteException;

    public static class Default implements IInputMethodPrivilegedOperations {
        @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
        public void setImeWindowStatus(int vis, int backDisposition) throws RemoteException {
        }

        @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
        public void reportStartInput(IBinder startInputToken) throws RemoteException {
        }

        @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
        public IInputContentUriToken createInputContentUriToken(Uri contentUri, String packageName) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
        public void reportFullscreenMode(boolean fullscreen) throws RemoteException {
        }

        @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
        public void setInputMethod(String id) throws RemoteException {
        }

        @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
        public void setInputMethodAndSubtype(String id, InputMethodSubtype subtype) throws RemoteException {
        }

        @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
        public void hideMySoftInput(int flags) throws RemoteException {
        }

        @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
        public void showMySoftInput(int flags) throws RemoteException {
        }

        @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
        public void updateStatusIcon(String packageName, int iconId) throws RemoteException {
        }

        @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
        public boolean switchToPreviousInputMethod() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
        public boolean switchToNextInputMethod(boolean onlyCurrentIme) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
        public boolean shouldOfferSwitchingToNextInputMethod() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
        public void notifyUserAction() throws RemoteException {
        }

        @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
        public void reportPreRendered(EditorInfo info) throws RemoteException {
        }

        @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
        public void applyImeVisibility(boolean setVisible) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IInputMethodPrivilegedOperations {
        private static final String DESCRIPTOR = "com.android.internal.inputmethod.IInputMethodPrivilegedOperations";
        static final int TRANSACTION_applyImeVisibility = 15;
        static final int TRANSACTION_createInputContentUriToken = 3;
        static final int TRANSACTION_hideMySoftInput = 7;
        static final int TRANSACTION_notifyUserAction = 13;
        static final int TRANSACTION_reportFullscreenMode = 4;
        static final int TRANSACTION_reportPreRendered = 14;
        static final int TRANSACTION_reportStartInput = 2;
        static final int TRANSACTION_setImeWindowStatus = 1;
        static final int TRANSACTION_setInputMethod = 5;
        static final int TRANSACTION_setInputMethodAndSubtype = 6;
        static final int TRANSACTION_shouldOfferSwitchingToNextInputMethod = 12;
        static final int TRANSACTION_showMySoftInput = 8;
        static final int TRANSACTION_switchToNextInputMethod = 11;
        static final int TRANSACTION_switchToPreviousInputMethod = 10;
        static final int TRANSACTION_updateStatusIcon = 9;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInputMethodPrivilegedOperations asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInputMethodPrivilegedOperations)) {
                return new Proxy(obj);
            }
            return (IInputMethodPrivilegedOperations) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "setImeWindowStatus";
                case 2:
                    return "reportStartInput";
                case 3:
                    return "createInputContentUriToken";
                case 4:
                    return "reportFullscreenMode";
                case 5:
                    return "setInputMethod";
                case 6:
                    return "setInputMethodAndSubtype";
                case 7:
                    return "hideMySoftInput";
                case 8:
                    return "showMySoftInput";
                case 9:
                    return "updateStatusIcon";
                case 10:
                    return "switchToPreviousInputMethod";
                case 11:
                    return "switchToNextInputMethod";
                case 12:
                    return "shouldOfferSwitchingToNextInputMethod";
                case 13:
                    return "notifyUserAction";
                case 14:
                    return "reportPreRendered";
                case 15:
                    return "applyImeVisibility";
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
            Uri _arg0;
            InputMethodSubtype _arg1;
            EditorInfo _arg02;
            if (code != 1598968902) {
                boolean _arg03 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        setImeWindowStatus(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        reportStartInput(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        IInputContentUriToken _result = createInputContentUriToken(_arg0, data.readString());
                        reply.writeNoException();
                        reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        reportFullscreenMode(_arg03);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        setInputMethod(data.readString());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = InputMethodSubtype.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        setInputMethodAndSubtype(_arg04, _arg1);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        hideMySoftInput(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        showMySoftInput(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        updateStatusIcon(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean switchToPreviousInputMethod = switchToPreviousInputMethod();
                        reply.writeNoException();
                        reply.writeInt(switchToPreviousInputMethod ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        boolean switchToNextInputMethod = switchToNextInputMethod(_arg03);
                        reply.writeNoException();
                        reply.writeInt(switchToNextInputMethod ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean shouldOfferSwitchingToNextInputMethod = shouldOfferSwitchingToNextInputMethod();
                        reply.writeNoException();
                        reply.writeInt(shouldOfferSwitchingToNextInputMethod ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        notifyUserAction();
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = EditorInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        reportPreRendered(_arg02);
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        applyImeVisibility(_arg03);
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
        public static class Proxy implements IInputMethodPrivilegedOperations {
            public static IInputMethodPrivilegedOperations sDefaultImpl;
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

            @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
            public void setImeWindowStatus(int vis, int backDisposition) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(vis);
                    _data.writeInt(backDisposition);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setImeWindowStatus(vis, backDisposition);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
            public void reportStartInput(IBinder startInputToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(startInputToken);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportStartInput(startInputToken);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
            public IInputContentUriToken createInputContentUriToken(Uri contentUri, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (contentUri != null) {
                        _data.writeInt(1);
                        contentUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createInputContentUriToken(contentUri, packageName);
                    }
                    _reply.readException();
                    IInputContentUriToken _result = IInputContentUriToken.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
            public void reportFullscreenMode(boolean fullscreen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fullscreen ? 1 : 0);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportFullscreenMode(fullscreen);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
            public void setInputMethod(String id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setInputMethod(id);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
            public void setInputMethodAndSubtype(String id, InputMethodSubtype subtype) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    if (subtype != null) {
                        _data.writeInt(1);
                        subtype.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setInputMethodAndSubtype(id, subtype);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
            public void hideMySoftInput(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().hideMySoftInput(flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
            public void showMySoftInput(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().showMySoftInput(flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
            public void updateStatusIcon(String packageName, int iconId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(iconId);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateStatusIcon(packageName, iconId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
            public boolean switchToPreviousInputMethod() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().switchToPreviousInputMethod();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
            public boolean switchToNextInputMethod(boolean onlyCurrentIme) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(onlyCurrentIme ? 1 : 0);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().switchToNextInputMethod(onlyCurrentIme);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
            public boolean shouldOfferSwitchingToNextInputMethod() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().shouldOfferSwitchingToNextInputMethod();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
            public void notifyUserAction() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyUserAction();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
            public void reportPreRendered(EditorInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportPreRendered(info);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IInputMethodPrivilegedOperations
            public void applyImeVisibility(boolean setVisible) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(setVisible ? 1 : 0);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().applyImeVisibility(setVisible);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IInputMethodPrivilegedOperations impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IInputMethodPrivilegedOperations getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
