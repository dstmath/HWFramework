package com.huawei.ncdft;

import android.location.Location;
import android.location.LocationRequest;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.internal.location.ProviderRequest;
import java.util.List;

public interface INcDft extends IInterface {

    public static abstract class Stub extends Binder implements INcDft {
        private static final String DESCRIPTOR = "com.huawei.ncdft.INcDft";
        static final int TRANSACTION_getNcDftParam = 3;
        static final int TRANSACTION_getUserType = 10;
        static final int TRANSACTION_notifyNcDftBundleEvent = 2;
        static final int TRANSACTION_notifyNcDftEvent = 1;
        static final int TRANSACTION_reportGnssApkName = 6;
        static final int TRANSACTION_reportGnssLocation = 8;
        static final int TRANSACTION_reportGnssSvStatus = 9;
        static final int TRANSACTION_reportNetworkInfo = 7;
        static final int TRANSACTION_reportNlpLocation = 5;
        static final int TRANSACTION_triggerUpload = 4;

        private static class Proxy implements INcDft {
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

            public int notifyNcDftEvent(int domain, int ncEventID, List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(domain);
                    _data.writeInt(ncEventID);
                    _data.writeStringList(list);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int notifyNcDftBundleEvent(int domain, int ncEventID, Bundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(domain);
                    _data.writeInt(ncEventID);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getNcDftParam(int domain, List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(domain);
                    _data.writeStringList(list);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean triggerUpload(int domain, int event, int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(domain);
                    _data.writeInt(event);
                    _data.writeInt(errorCode);
                    boolean _result = false;
                    this.mRemote.transact(4, _data, _reply, 0);
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

            public int reportNlpLocation(int domain, int event, String provider, ProviderRequest providerRequest) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(domain);
                    _data.writeInt(event);
                    _data.writeString(provider);
                    if (providerRequest != null) {
                        _data.writeInt(1);
                        providerRequest.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int reportGnssApkName(int domain, int event, LocationRequest request, String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(domain);
                    _data.writeInt(event);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(name);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int reportNetworkInfo(int domain, int event, NetworkInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(domain);
                    _data.writeInt(event);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int reportGnssLocation(int domain, int event, Location info, long time, String provider) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(domain);
                    _data.writeInt(event);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(time);
                    _data.writeString(provider);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int reportGnssSvStatus(int domain, int event, int svCount, int[] svs, float[] snrs, float[] svElevations, float[] svAzimuths) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(domain);
                    _data.writeInt(event);
                    _data.writeInt(svCount);
                    _data.writeIntArray(svs);
                    _data.writeFloatArray(snrs);
                    _data.writeFloatArray(svElevations);
                    _data.writeFloatArray(svAzimuths);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getUserType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INcDft asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INcDft)) {
                return new Proxy(obj);
            }
            return (INcDft) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                ProviderRequest _arg3 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result = notifyNcDftEvent(data.readInt(), data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        int _arg1 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        int _result2 = notifyNcDftBundleEvent(_arg0, _arg1, _arg3);
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result3 = getNcDftParam(data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        parcel2.writeString(_result3);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result4 = triggerUpload(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        int _arg12 = data.readInt();
                        String _arg2 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = (ProviderRequest) ProviderRequest.CREATOR.createFromParcel(parcel);
                        }
                        int _result5 = reportNlpLocation(_arg02, _arg12, _arg2, _arg3);
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        int _arg13 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = (LocationRequest) LocationRequest.CREATOR.createFromParcel(parcel);
                        }
                        int _result6 = reportGnssApkName(_arg03, _arg13, _arg3, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result6);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        int _arg14 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = (NetworkInfo) NetworkInfo.CREATOR.createFromParcel(parcel);
                        }
                        int _result7 = reportNetworkInfo(_arg04, _arg14, _arg3);
                        reply.writeNoException();
                        parcel2.writeInt(_result7);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        int _arg15 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = (Location) Location.CREATOR.createFromParcel(parcel);
                        }
                        ProviderRequest providerRequest = _arg3;
                        int _result8 = reportGnssLocation(_arg05, _arg15, providerRequest, data.readLong(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result8);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result9 = reportGnssSvStatus(data.readInt(), data.readInt(), data.readInt(), data.createIntArray(), data.createFloatArray(), data.createFloatArray(), data.createFloatArray());
                        reply.writeNoException();
                        parcel2.writeInt(_result9);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result10 = getUserType();
                        reply.writeNoException();
                        parcel2.writeInt(_result10);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    String getNcDftParam(int i, List<String> list) throws RemoteException;

    int getUserType() throws RemoteException;

    int notifyNcDftBundleEvent(int i, int i2, Bundle bundle) throws RemoteException;

    int notifyNcDftEvent(int i, int i2, List<String> list) throws RemoteException;

    int reportGnssApkName(int i, int i2, LocationRequest locationRequest, String str) throws RemoteException;

    int reportGnssLocation(int i, int i2, Location location, long j, String str) throws RemoteException;

    int reportGnssSvStatus(int i, int i2, int i3, int[] iArr, float[] fArr, float[] fArr2, float[] fArr3) throws RemoteException;

    int reportNetworkInfo(int i, int i2, NetworkInfo networkInfo) throws RemoteException;

    int reportNlpLocation(int i, int i2, String str, ProviderRequest providerRequest) throws RemoteException;

    boolean triggerUpload(int i, int i2, int i3) throws RemoteException;
}
