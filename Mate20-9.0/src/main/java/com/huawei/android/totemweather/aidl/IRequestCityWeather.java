package com.huawei.android.totemweather.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.android.totemweather.aidl.IRequestCallBack;

public interface IRequestCityWeather extends IInterface {

    public static abstract class Stub extends Binder implements IRequestCityWeather {
        private static final String DESCRIPTOR = "com.huawei.android.totemweather.aidl.IRequestCityWeather";
        static final int TRANSACTION_getWeatherByType = 3;
        static final int TRANSACTION_registerCallBack = 4;
        static final int TRANSACTION_requestWeatherByCityId = 2;
        static final int TRANSACTION_requestWeatherByLocation = 1;
        static final int TRANSACTION_requestWeatherByLocationAndSourceType = 6;
        static final int TRANSACTION_unregisterCallBack = 5;

        private static class Proxy implements IRequestCityWeather {
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

            public void requestWeatherByLocation(RequestData requestData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (requestData != null) {
                        _data.writeInt(1);
                        requestData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestWeatherByCityId(RequestData requestData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (requestData != null) {
                        _data.writeInt(1);
                        requestData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getWeatherByType(RequestData requestData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (requestData != null) {
                        _data.writeInt(1);
                        requestData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerCallBack(IRequestCallBack callback, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(packageName);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterCallBack(IRequestCallBack callback, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(packageName);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestWeatherByLocationAndSourceType(RequestData requestData, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (requestData != null) {
                        _data.writeInt(1);
                        requestData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRequestCityWeather asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRequestCityWeather)) {
                return new Proxy(obj);
            }
            return (IRequestCityWeather) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            RequestData _arg0;
            RequestData _arg02;
            RequestData _arg03;
            RequestData _arg04;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg04 = RequestData.CREATOR.createFromParcel(data);
                    } else {
                        _arg04 = null;
                    }
                    requestWeatherByLocation(_arg04);
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg03 = RequestData.CREATOR.createFromParcel(data);
                    } else {
                        _arg03 = null;
                    }
                    requestWeatherByCityId(_arg03);
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = RequestData.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    getWeatherByType(_arg02);
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    registerCallBack(IRequestCallBack.Stub.asInterface(data.readStrongBinder()), data.readString());
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterCallBack(IRequestCallBack.Stub.asInterface(data.readStrongBinder()), data.readString());
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = RequestData.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    requestWeatherByLocationAndSourceType(_arg0, data.readInt());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void getWeatherByType(RequestData requestData) throws RemoteException;

    void registerCallBack(IRequestCallBack iRequestCallBack, String str) throws RemoteException;

    void requestWeatherByCityId(RequestData requestData) throws RemoteException;

    void requestWeatherByLocation(RequestData requestData) throws RemoteException;

    void requestWeatherByLocationAndSourceType(RequestData requestData, int i) throws RemoteException;

    void unregisterCallBack(IRequestCallBack iRequestCallBack, String str) throws RemoteException;
}
