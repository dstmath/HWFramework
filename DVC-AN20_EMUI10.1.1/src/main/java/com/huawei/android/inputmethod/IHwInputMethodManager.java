package com.huawei.android.inputmethod;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputBinding;
import com.huawei.android.inputmethod.IHwInputContentListener;
import com.huawei.android.inputmethod.IHwInputMethodListener;

public interface IHwInputMethodManager extends IInterface {
    InputBinding getCurInputBinding() throws RemoteException;

    EditorInfo getCurrentInputStyle() throws RemoteException;

    void onContentChanged(String str) throws RemoteException;

    void onFinishInput() throws RemoteException;

    void onReceivedComposingText(String str) throws RemoteException;

    void onReceivedInputContent(String str) throws RemoteException;

    void onShowInputRequested() throws RemoteException;

    void onStartInput() throws RemoteException;

    void onUpdateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo) throws RemoteException;

    void registerInputContentListener(IHwInputContentListener iHwInputContentListener) throws RemoteException;

    void registerInputMethodListener(IHwInputMethodListener iHwInputMethodListener) throws RemoteException;

    void restartInputMethodForMultiDisplay() throws RemoteException;

    void setDefaultIme(String str) throws RemoteException;

    void setInputSource(boolean z) throws RemoteException;

    void unregisterInputContentListener() throws RemoteException;

    void unregisterInputMethodListener() throws RemoteException;

    public static class Default implements IHwInputMethodManager {
        @Override // com.huawei.android.inputmethod.IHwInputMethodManager
        public void setDefaultIme(String imeName) throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.IHwInputMethodManager
        public void setInputSource(boolean isFingerTouch) throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.IHwInputMethodManager
        public void restartInputMethodForMultiDisplay() throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.IHwInputMethodManager
        public void registerInputMethodListener(IHwInputMethodListener listener) throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.IHwInputMethodManager
        public void unregisterInputMethodListener() throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.IHwInputMethodManager
        public void onStartInput() throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.IHwInputMethodManager
        public void onFinishInput() throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.IHwInputMethodManager
        public void onShowInputRequested() throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.IHwInputMethodManager
        public void onContentChanged(String text) throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.IHwInputMethodManager
        public void onUpdateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo) throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.IHwInputMethodManager
        public void registerInputContentListener(IHwInputContentListener listener) throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.IHwInputMethodManager
        public void unregisterInputContentListener() throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.IHwInputMethodManager
        public void onReceivedInputContent(String content) throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.IHwInputMethodManager
        public void onReceivedComposingText(String content) throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.IHwInputMethodManager
        public InputBinding getCurInputBinding() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.inputmethod.IHwInputMethodManager
        public EditorInfo getCurrentInputStyle() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwInputMethodManager {
        private static final String DESCRIPTOR = "com.huawei.android.inputmethod.IHwInputMethodManager";
        static final int TRANSACTION_getCurInputBinding = 15;
        static final int TRANSACTION_getCurrentInputStyle = 16;
        static final int TRANSACTION_onContentChanged = 9;
        static final int TRANSACTION_onFinishInput = 7;
        static final int TRANSACTION_onReceivedComposingText = 14;
        static final int TRANSACTION_onReceivedInputContent = 13;
        static final int TRANSACTION_onShowInputRequested = 8;
        static final int TRANSACTION_onStartInput = 6;
        static final int TRANSACTION_onUpdateCursorAnchorInfo = 10;
        static final int TRANSACTION_registerInputContentListener = 11;
        static final int TRANSACTION_registerInputMethodListener = 4;
        static final int TRANSACTION_restartInputMethodForMultiDisplay = 3;
        static final int TRANSACTION_setDefaultIme = 1;
        static final int TRANSACTION_setInputSource = 2;
        static final int TRANSACTION_unregisterInputContentListener = 12;
        static final int TRANSACTION_unregisterInputMethodListener = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwInputMethodManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwInputMethodManager)) {
                return new Proxy(obj);
            }
            return (IHwInputMethodManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "setDefaultIme";
                case 2:
                    return "setInputSource";
                case 3:
                    return "restartInputMethodForMultiDisplay";
                case 4:
                    return "registerInputMethodListener";
                case 5:
                    return "unregisterInputMethodListener";
                case 6:
                    return "onStartInput";
                case 7:
                    return "onFinishInput";
                case 8:
                    return "onShowInputRequested";
                case 9:
                    return "onContentChanged";
                case 10:
                    return "onUpdateCursorAnchorInfo";
                case 11:
                    return "registerInputContentListener";
                case 12:
                    return "unregisterInputContentListener";
                case 13:
                    return "onReceivedInputContent";
                case 14:
                    return "onReceivedComposingText";
                case 15:
                    return "getCurInputBinding";
                case 16:
                    return "getCurrentInputStyle";
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
            CursorAnchorInfo _arg0;
            if (code != 1598968902) {
                boolean _arg02 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        setDefaultIme(data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setInputSource(_arg02);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        restartInputMethodForMultiDisplay();
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        registerInputMethodListener(IHwInputMethodListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterInputMethodListener();
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onStartInput();
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        onFinishInput();
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        onShowInputRequested();
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        onContentChanged(data.readString());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = CursorAnchorInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        onUpdateCursorAnchorInfo(_arg0);
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        registerInputContentListener(IHwInputContentListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterInputContentListener();
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        onReceivedInputContent(data.readString());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        onReceivedComposingText(data.readString());
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        InputBinding _result = getCurInputBinding();
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        EditorInfo _result2 = getCurrentInputStyle();
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
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
        public static class Proxy implements IHwInputMethodManager {
            public static IHwInputMethodManager sDefaultImpl;
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

            @Override // com.huawei.android.inputmethod.IHwInputMethodManager
            public void setDefaultIme(String imeName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imeName);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDefaultIme(imeName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.IHwInputMethodManager
            public void setInputSource(boolean isFingerTouch) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isFingerTouch ? 1 : 0);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setInputSource(isFingerTouch);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.IHwInputMethodManager
            public void restartInputMethodForMultiDisplay() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().restartInputMethodForMultiDisplay();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.IHwInputMethodManager
            public void registerInputMethodListener(IHwInputMethodListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerInputMethodListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.IHwInputMethodManager
            public void unregisterInputMethodListener() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterInputMethodListener();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.IHwInputMethodManager
            public void onStartInput() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onStartInput();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.IHwInputMethodManager
            public void onFinishInput() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onFinishInput();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.IHwInputMethodManager
            public void onShowInputRequested() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onShowInputRequested();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.IHwInputMethodManager
            public void onContentChanged(String text) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(text);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onContentChanged(text);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.IHwInputMethodManager
            public void onUpdateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cursorAnchorInfo != null) {
                        _data.writeInt(1);
                        cursorAnchorInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onUpdateCursorAnchorInfo(cursorAnchorInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.IHwInputMethodManager
            public void registerInputContentListener(IHwInputContentListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerInputContentListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.IHwInputMethodManager
            public void unregisterInputContentListener() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterInputContentListener();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.IHwInputMethodManager
            public void onReceivedInputContent(String content) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(content);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onReceivedInputContent(content);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.IHwInputMethodManager
            public void onReceivedComposingText(String content) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(content);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onReceivedComposingText(content);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.IHwInputMethodManager
            public InputBinding getCurInputBinding() throws RemoteException {
                InputBinding _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurInputBinding();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = InputBinding.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.IHwInputMethodManager
            public EditorInfo getCurrentInputStyle() throws RemoteException {
                EditorInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurrentInputStyle();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = EditorInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwInputMethodManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwInputMethodManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
