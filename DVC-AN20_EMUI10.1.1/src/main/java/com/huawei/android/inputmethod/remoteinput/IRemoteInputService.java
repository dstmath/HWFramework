package com.huawei.android.inputmethod.remoteinput;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.android.inputmethod.remoteinput.IRemoteInputCallback;

public interface IRemoteInputService extends IInterface {
    void notifyFocus(boolean z) throws RemoteException;

    void requestFocus(String str) throws RemoteException;

    void setCallBack(String str, IRemoteInputCallback iRemoteInputCallback) throws RemoteException;

    void setText(String str, String str2, Bundle bundle) throws RemoteException;

    void setTextFromApp(String str, Bundle bundle) throws RemoteException;

    public static class Default implements IRemoteInputService {
        @Override // com.huawei.android.inputmethod.remoteinput.IRemoteInputService
        public void setText(String deviceId, String text, Bundle config) throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.remoteinput.IRemoteInputService
        public void requestFocus(String deviceId) throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.remoteinput.IRemoteInputService
        public void setCallBack(String deviceId, IRemoteInputCallback inputCallback) throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.remoteinput.IRemoteInputService
        public void notifyFocus(boolean isFocused) throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.remoteinput.IRemoteInputService
        public void setTextFromApp(String text, Bundle style) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IRemoteInputService {
        private static final String DESCRIPTOR = "com.huawei.android.inputmethod.remoteinput.IRemoteInputService";
        static final int TRANSACTION_notifyFocus = 4;
        static final int TRANSACTION_requestFocus = 2;
        static final int TRANSACTION_setCallBack = 3;
        static final int TRANSACTION_setText = 1;
        static final int TRANSACTION_setTextFromApp = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRemoteInputService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRemoteInputService)) {
                return new Proxy(obj);
            }
            return (IRemoteInputService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "setText";
            }
            if (transactionCode == 2) {
                return "requestFocus";
            }
            if (transactionCode == 3) {
                return "setCallBack";
            }
            if (transactionCode == 4) {
                return "notifyFocus";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "setTextFromApp";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg2;
            Bundle _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                String _arg0 = data.readString();
                String _arg12 = data.readString();
                if (data.readInt() != 0) {
                    _arg2 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                setText(_arg0, _arg12, _arg2);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                requestFocus(data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                setCallBack(data.readString(), IRemoteInputCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                notifyFocus(data.readInt() != 0);
                reply.writeNoException();
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                String _arg02 = data.readString();
                if (data.readInt() != 0) {
                    _arg1 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                setTextFromApp(_arg02, _arg1);
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IRemoteInputService {
            public static IRemoteInputService sDefaultImpl;
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

            @Override // com.huawei.android.inputmethod.remoteinput.IRemoteInputService
            public void setText(String deviceId, String text, Bundle config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeString(text);
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setText(deviceId, text, config);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.remoteinput.IRemoteInputService
            public void requestFocus(String deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestFocus(deviceId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.remoteinput.IRemoteInputService
            public void setCallBack(String deviceId, IRemoteInputCallback inputCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeStrongBinder(inputCallback != null ? inputCallback.asBinder() : null);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCallBack(deviceId, inputCallback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.remoteinput.IRemoteInputService
            public void notifyFocus(boolean isFocused) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isFocused ? 1 : 0);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyFocus(isFocused);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.remoteinput.IRemoteInputService
            public void setTextFromApp(String text, Bundle style) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(text);
                    if (style != null) {
                        _data.writeInt(1);
                        style.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setTextFromApp(text, style);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IRemoteInputService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IRemoteInputService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
