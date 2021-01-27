package com.huawei.android.inputmethod;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.inputmethod.CursorAnchorInfo;

public interface IHwInputMethodListener extends IInterface {
    void onContentChanged(String str) throws RemoteException;

    void onFinishInput() throws RemoteException;

    void onShowInputRequested() throws RemoteException;

    void onStartInput() throws RemoteException;

    void onUpdateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo) throws RemoteException;

    public static class Default implements IHwInputMethodListener {
        @Override // com.huawei.android.inputmethod.IHwInputMethodListener
        public void onStartInput() throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.IHwInputMethodListener
        public void onFinishInput() throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.IHwInputMethodListener
        public void onShowInputRequested() throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.IHwInputMethodListener
        public void onContentChanged(String text) throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.IHwInputMethodListener
        public void onUpdateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwInputMethodListener {
        private static final String DESCRIPTOR = "com.huawei.android.inputmethod.IHwInputMethodListener";
        static final int TRANSACTION_onContentChanged = 4;
        static final int TRANSACTION_onFinishInput = 2;
        static final int TRANSACTION_onShowInputRequested = 3;
        static final int TRANSACTION_onStartInput = 1;
        static final int TRANSACTION_onUpdateCursorAnchorInfo = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwInputMethodListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwInputMethodListener)) {
                return new Proxy(obj);
            }
            return (IHwInputMethodListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onStartInput";
            }
            if (transactionCode == 2) {
                return "onFinishInput";
            }
            if (transactionCode == 3) {
                return "onShowInputRequested";
            }
            if (transactionCode == 4) {
                return "onContentChanged";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "onUpdateCursorAnchorInfo";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            CursorAnchorInfo _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onStartInput();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onFinishInput();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onShowInputRequested();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onContentChanged(data.readString());
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = CursorAnchorInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onUpdateCursorAnchorInfo(_arg0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwInputMethodListener {
            public static IHwInputMethodListener sDefaultImpl;
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

            @Override // com.huawei.android.inputmethod.IHwInputMethodListener
            public void onStartInput() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onStartInput();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.IHwInputMethodListener
            public void onFinishInput() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onFinishInput();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.IHwInputMethodListener
            public void onShowInputRequested() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onShowInputRequested();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.IHwInputMethodListener
            public void onContentChanged(String text) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(text);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onContentChanged(text);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.IHwInputMethodListener
            public void onUpdateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cursorAnchorInfo != null) {
                        _data.writeInt(1);
                        cursorAnchorInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUpdateCursorAnchorInfo(cursorAnchorInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwInputMethodListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwInputMethodListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
