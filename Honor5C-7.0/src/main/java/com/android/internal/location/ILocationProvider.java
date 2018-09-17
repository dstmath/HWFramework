package com.android.internal.location;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.WorkSource;

public interface ILocationProvider extends IInterface {

    public static abstract class Stub extends Binder implements ILocationProvider {
        private static final String DESCRIPTOR = "com.android.internal.location.ILocationProvider";
        static final int TRANSACTION_disable = 2;
        static final int TRANSACTION_enable = 1;
        static final int TRANSACTION_getProperties = 4;
        static final int TRANSACTION_getStatus = 5;
        static final int TRANSACTION_getStatusUpdateTime = 6;
        static final int TRANSACTION_sendExtraCommand = 7;
        static final int TRANSACTION_setRequest = 3;

        private static class Proxy implements ILocationProvider {
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

            public void enable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_enable, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_disable, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRequest(ProviderRequest request, WorkSource ws) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(Stub.TRANSACTION_enable);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (ws != null) {
                        _data.writeInt(Stub.TRANSACTION_enable);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setRequest, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ProviderProperties getProperties() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ProviderProperties providerProperties;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getProperties, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        providerProperties = (ProviderProperties) ProviderProperties.CREATOR.createFromParcel(_reply);
                    } else {
                        providerProperties = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return providerProperties;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getStatus(Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getStatus, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        extras.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getStatusUpdateTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getStatusUpdateTime, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean sendExtraCommand(String command, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(command);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_enable);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendExtraCommand, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    if (_reply.readInt() != 0) {
                        extras.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILocationProvider asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILocationProvider)) {
                return new Proxy(obj);
            }
            return (ILocationProvider) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_enable /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    enable();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_disable /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    disable();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setRequest /*3*/:
                    ProviderRequest providerRequest;
                    WorkSource workSource;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        providerRequest = (ProviderRequest) ProviderRequest.CREATOR.createFromParcel(data);
                    } else {
                        providerRequest = null;
                    }
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    setRequest(providerRequest, workSource);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getProperties /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    ProviderProperties _result = getProperties();
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_enable);
                        _result.writeToParcel(reply, TRANSACTION_enable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getStatus /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    Bundle _arg0 = new Bundle();
                    int _result2 = getStatus(_arg0);
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    if (_arg0 != null) {
                        reply.writeInt(TRANSACTION_enable);
                        _arg0.writeToParcel(reply, TRANSACTION_enable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getStatusUpdateTime /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    long _result3 = getStatusUpdateTime();
                    reply.writeNoException();
                    reply.writeLong(_result3);
                    return true;
                case TRANSACTION_sendExtraCommand /*7*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg02 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    boolean _result4 = sendExtraCommand(_arg02, bundle);
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_enable : 0);
                    if (bundle != null) {
                        reply.writeInt(TRANSACTION_enable);
                        bundle.writeToParcel(reply, TRANSACTION_enable);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void disable() throws RemoteException;

    void enable() throws RemoteException;

    ProviderProperties getProperties() throws RemoteException;

    int getStatus(Bundle bundle) throws RemoteException;

    long getStatusUpdateTime() throws RemoteException;

    boolean sendExtraCommand(String str, Bundle bundle) throws RemoteException;

    void setRequest(ProviderRequest providerRequest, WorkSource workSource) throws RemoteException;
}
