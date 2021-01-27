package com.google.android.startop.iorap;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITaskListener extends IInterface {
    void onComplete(RequestId requestId, TaskResult taskResult) throws RemoteException;

    void onProgress(RequestId requestId, TaskResult taskResult) throws RemoteException;

    public static class Default implements ITaskListener {
        @Override // com.google.android.startop.iorap.ITaskListener
        public void onProgress(RequestId requestId, TaskResult result) throws RemoteException {
        }

        @Override // com.google.android.startop.iorap.ITaskListener
        public void onComplete(RequestId requestId, TaskResult result) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITaskListener {
        private static final String DESCRIPTOR = "com.google.android.startop.iorap.ITaskListener";
        static final int TRANSACTION_onComplete = 2;
        static final int TRANSACTION_onProgress = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITaskListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITaskListener)) {
                return new Proxy(obj);
            }
            return (ITaskListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            RequestId _arg0;
            TaskResult _arg1;
            RequestId _arg02;
            TaskResult _arg12;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = RequestId.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                if (data.readInt() != 0) {
                    _arg1 = TaskResult.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                onProgress(_arg0, _arg1);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = RequestId.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                if (data.readInt() != 0) {
                    _arg12 = TaskResult.CREATOR.createFromParcel(data);
                } else {
                    _arg12 = null;
                }
                onComplete(_arg02, _arg12);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ITaskListener {
            public static ITaskListener sDefaultImpl;
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

            @Override // com.google.android.startop.iorap.ITaskListener
            public void onProgress(RequestId requestId, TaskResult result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (requestId != null) {
                        _data.writeInt(1);
                        requestId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onProgress(requestId, result);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.google.android.startop.iorap.ITaskListener
            public void onComplete(RequestId requestId, TaskResult result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (requestId != null) {
                        _data.writeInt(1);
                        requestId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onComplete(requestId, result);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ITaskListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITaskListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
