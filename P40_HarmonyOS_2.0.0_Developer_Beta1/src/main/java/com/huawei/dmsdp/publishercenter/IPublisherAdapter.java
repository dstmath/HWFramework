package com.huawei.dmsdp.publishercenter;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.dmsdp.publishercenter.IPublisherListener;

public interface IPublisherAdapter extends IInterface {
    String queryMsg(int i) throws RemoteException;

    boolean registerPublisherListener(int i, IPublisherListener iPublisherListener) throws RemoteException;

    boolean sendMsg(int i, String str) throws RemoteException;

    boolean unregisterPublisherListener(int i) throws RemoteException;

    public static abstract class Stub extends Binder implements IPublisherAdapter {
        private static final String DESCRIPTOR = "com.huawei.dmsdp.publishercenter.IPublisherAdapter";
        static final int TRANSACTION_queryMsg = 4;
        static final int TRANSACTION_registerPublisherListener = 1;
        static final int TRANSACTION_sendMsg = 3;
        static final int TRANSACTION_unregisterPublisherListener = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPublisherAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPublisherAdapter)) {
                return new Proxy(obj);
            }
            return (IPublisherAdapter) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean registerPublisherListener = registerPublisherListener(data.readInt(), IPublisherListener.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(registerPublisherListener ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                boolean unregisterPublisherListener = unregisterPublisherListener(data.readInt());
                reply.writeNoException();
                reply.writeInt(unregisterPublisherListener ? 1 : 0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                boolean sendMsg = sendMsg(data.readInt(), data.readString());
                reply.writeNoException();
                reply.writeInt(sendMsg ? 1 : 0);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                String _result = queryMsg(data.readInt());
                reply.writeNoException();
                reply.writeString(_result);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IPublisherAdapter {
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

            @Override // com.huawei.dmsdp.publishercenter.IPublisherAdapter
            public boolean registerPublisherListener(int businessType, IPublisherListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessType);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdp.publishercenter.IPublisherAdapter
            public boolean unregisterPublisherListener(int businessType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessType);
                    boolean _result = false;
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdp.publishercenter.IPublisherAdapter
            public boolean sendMsg(int businessType, String msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessType);
                    _data.writeString(msg);
                    boolean _result = false;
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdp.publishercenter.IPublisherAdapter
            public String queryMsg(int key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(key);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
