package com.android.ims.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.ims.ImsConfigListener;

public interface IImsConfig extends IInterface {

    public static abstract class Stub extends Binder implements IImsConfig {
        private static final String DESCRIPTOR = "com.android.ims.internal.IImsConfig";
        static final int TRANSACTION_getFeatureValue = 5;
        static final int TRANSACTION_getProvisionedStringValue = 2;
        static final int TRANSACTION_getProvisionedValue = 1;
        static final int TRANSACTION_getVideoQuality = 8;
        static final int TRANSACTION_getVolteProvisioned = 7;
        static final int TRANSACTION_setFeatureValue = 6;
        static final int TRANSACTION_setProvisionedStringValue = 4;
        static final int TRANSACTION_setProvisionedValue = 3;
        static final int TRANSACTION_setVideoQuality = 9;

        private static class Proxy implements IImsConfig {
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

            public int getProvisionedValue(int item) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(item);
                    this.mRemote.transact(Stub.TRANSACTION_getProvisionedValue, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getProvisionedStringValue(int item) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(item);
                    this.mRemote.transact(Stub.TRANSACTION_getProvisionedStringValue, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setProvisionedValue(int item, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(item);
                    _data.writeInt(value);
                    this.mRemote.transact(Stub.TRANSACTION_setProvisionedValue, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setProvisionedStringValue(int item, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(item);
                    _data.writeString(value);
                    this.mRemote.transact(Stub.TRANSACTION_setProvisionedStringValue, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getFeatureValue(int feature, int network, ImsConfigListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(feature);
                    _data.writeInt(network);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_getFeatureValue, _data, null, Stub.TRANSACTION_getProvisionedValue);
                } finally {
                    _data.recycle();
                }
            }

            public void setFeatureValue(int feature, int network, int value, ImsConfigListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(feature);
                    _data.writeInt(network);
                    _data.writeInt(value);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setFeatureValue, _data, null, Stub.TRANSACTION_getProvisionedValue);
                } finally {
                    _data.recycle();
                }
            }

            public boolean getVolteProvisioned() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getVolteProvisioned, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getVideoQuality(ImsConfigListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_getVideoQuality, _data, null, Stub.TRANSACTION_getProvisionedValue);
                } finally {
                    _data.recycle();
                }
            }

            public void setVideoQuality(int quality, ImsConfigListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(quality);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setVideoQuality, _data, null, Stub.TRANSACTION_getProvisionedValue);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImsConfig asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImsConfig)) {
                return new Proxy(obj);
            }
            return (IImsConfig) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            switch (code) {
                case TRANSACTION_getProvisionedValue /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getProvisionedValue(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getProvisionedStringValue /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    String _result2 = getProvisionedStringValue(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case TRANSACTION_setProvisionedValue /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setProvisionedValue(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_setProvisionedStringValue /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setProvisionedStringValue(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getFeatureValue /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    getFeatureValue(data.readInt(), data.readInt(), com.android.ims.ImsConfigListener.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_setFeatureValue /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    setFeatureValue(data.readInt(), data.readInt(), data.readInt(), com.android.ims.ImsConfigListener.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_getVolteProvisioned /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result3 = getVolteProvisioned();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getProvisionedValue : 0);
                    return true;
                case TRANSACTION_getVideoQuality /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    getVideoQuality(com.android.ims.ImsConfigListener.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_setVideoQuality /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    setVideoQuality(data.readInt(), com.android.ims.ImsConfigListener.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void getFeatureValue(int i, int i2, ImsConfigListener imsConfigListener) throws RemoteException;

    String getProvisionedStringValue(int i) throws RemoteException;

    int getProvisionedValue(int i) throws RemoteException;

    void getVideoQuality(ImsConfigListener imsConfigListener) throws RemoteException;

    boolean getVolteProvisioned() throws RemoteException;

    void setFeatureValue(int i, int i2, int i3, ImsConfigListener imsConfigListener) throws RemoteException;

    int setProvisionedStringValue(int i, String str) throws RemoteException;

    int setProvisionedValue(int i, int i2) throws RemoteException;

    void setVideoQuality(int i, ImsConfigListener imsConfigListener) throws RemoteException;
}
