package com.android.internal.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.inputmethod.EditorInfo;

public interface IInputMethodClient extends IInterface {
    void applyImeVisibility(boolean z) throws RemoteException;

    void onBindMethod(InputBindResult inputBindResult) throws RemoteException;

    void onUnbindMethod(int i, int i2) throws RemoteException;

    void reportFullscreenMode(boolean z) throws RemoteException;

    void reportPreRendered(EditorInfo editorInfo) throws RemoteException;

    void setActive(boolean z, boolean z2) throws RemoteException;

    void updateActivityViewToScreenMatrix(int i, float[] fArr) throws RemoteException;

    public static class Default implements IInputMethodClient {
        @Override // com.android.internal.view.IInputMethodClient
        public void onBindMethod(InputBindResult res) throws RemoteException {
        }

        @Override // com.android.internal.view.IInputMethodClient
        public void onUnbindMethod(int sequence, int unbindReason) throws RemoteException {
        }

        @Override // com.android.internal.view.IInputMethodClient
        public void setActive(boolean active, boolean fullscreen) throws RemoteException {
        }

        @Override // com.android.internal.view.IInputMethodClient
        public void reportFullscreenMode(boolean fullscreen) throws RemoteException {
        }

        @Override // com.android.internal.view.IInputMethodClient
        public void reportPreRendered(EditorInfo info) throws RemoteException {
        }

        @Override // com.android.internal.view.IInputMethodClient
        public void applyImeVisibility(boolean setVisible) throws RemoteException {
        }

        @Override // com.android.internal.view.IInputMethodClient
        public void updateActivityViewToScreenMatrix(int bindSequence, float[] matrixValues) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IInputMethodClient {
        private static final String DESCRIPTOR = "com.android.internal.view.IInputMethodClient";
        static final int TRANSACTION_applyImeVisibility = 6;
        static final int TRANSACTION_onBindMethod = 1;
        static final int TRANSACTION_onUnbindMethod = 2;
        static final int TRANSACTION_reportFullscreenMode = 4;
        static final int TRANSACTION_reportPreRendered = 5;
        static final int TRANSACTION_setActive = 3;
        static final int TRANSACTION_updateActivityViewToScreenMatrix = 7;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInputMethodClient asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInputMethodClient)) {
                return new Proxy(obj);
            }
            return (IInputMethodClient) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "onBindMethod";
                case 2:
                    return "onUnbindMethod";
                case 3:
                    return "setActive";
                case 4:
                    return "reportFullscreenMode";
                case 5:
                    return "reportPreRendered";
                case 6:
                    return "applyImeVisibility";
                case 7:
                    return "updateActivityViewToScreenMatrix";
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
            InputBindResult _arg0;
            EditorInfo _arg02;
            if (code != 1598968902) {
                boolean _arg03 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = InputBindResult.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        onBindMethod(_arg0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onUnbindMethod(data.readInt(), data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _arg04 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        setActive(_arg04, _arg03);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        reportFullscreenMode(_arg03);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = EditorInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        reportPreRendered(_arg02);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        applyImeVisibility(_arg03);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        updateActivityViewToScreenMatrix(data.readInt(), data.createFloatArray());
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
        public static class Proxy implements IInputMethodClient {
            public static IInputMethodClient sDefaultImpl;
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

            @Override // com.android.internal.view.IInputMethodClient
            public void onBindMethod(InputBindResult res) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (res != null) {
                        _data.writeInt(1);
                        res.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onBindMethod(res);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.view.IInputMethodClient
            public void onUnbindMethod(int sequence, int unbindReason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sequence);
                    _data.writeInt(unbindReason);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUnbindMethod(sequence, unbindReason);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.view.IInputMethodClient
            public void setActive(boolean active, boolean fullscreen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 0;
                    _data.writeInt(active ? 1 : 0);
                    if (fullscreen) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setActive(active, fullscreen);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.view.IInputMethodClient
            public void reportFullscreenMode(boolean fullscreen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fullscreen ? 1 : 0);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().reportFullscreenMode(fullscreen);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.view.IInputMethodClient
            public void reportPreRendered(EditorInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().reportPreRendered(info);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.view.IInputMethodClient
            public void applyImeVisibility(boolean setVisible) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(setVisible ? 1 : 0);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().applyImeVisibility(setVisible);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.view.IInputMethodClient
            public void updateActivityViewToScreenMatrix(int bindSequence, float[] matrixValues) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(bindSequence);
                    _data.writeFloatArray(matrixValues);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().updateActivityViewToScreenMatrix(bindSequence, matrixValues);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IInputMethodClient impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IInputMethodClient getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
