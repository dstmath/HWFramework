package com.huawei.ncdft;

import android.location.Location;
import android.location.LocationRequest;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.internal.location.ProviderRequest;
import java.util.List;

public interface INcDft extends IInterface {

    public static abstract class Stub extends Binder implements INcDft {
        private static final String DESCRIPTOR = "com.huawei.ncdft.INcDft";
        static final int TRANSACTION_getNcDftParam = 2;
        static final int TRANSACTION_getUserType = 9;
        static final int TRANSACTION_notifyNcDftEvent = 1;
        static final int TRANSACTION_reportGnssApkName = 5;
        static final int TRANSACTION_reportGnssLocation = 7;
        static final int TRANSACTION_reportGnssSvStatus = 8;
        static final int TRANSACTION_reportNetworkInfo = 6;
        static final int TRANSACTION_reportNlpLocation = 4;
        static final int TRANSACTION_triggerUpload = 3;

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
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(3, _data, _reply, 0);
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
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
            int _result;
            int _arg0;
            int _arg1;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = notifyNcDftEvent(data.readInt(), data.readInt(), data.createStringArrayList());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    String _result2 = getNcDftParam(data.readInt(), data.createStringArrayList());
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result3 = triggerUpload(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 4:
                    ProviderRequest _arg3;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readInt();
                    String _arg2 = data.readString();
                    if (data.readInt() != 0) {
                        _arg3 = (ProviderRequest) ProviderRequest.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    _result = reportNlpLocation(_arg0, _arg1, _arg2, _arg3);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 5:
                    LocationRequest _arg22;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg22 = (LocationRequest) LocationRequest.CREATOR.createFromParcel(data);
                    } else {
                        _arg22 = null;
                    }
                    _result = reportGnssApkName(_arg0, _arg1, _arg22, data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 6:
                    NetworkInfo _arg23;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg23 = (NetworkInfo) NetworkInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg23 = null;
                    }
                    _result = reportNetworkInfo(_arg0, _arg1, _arg23);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 7:
                    Location _arg24;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg24 = (Location) Location.CREATOR.createFromParcel(data);
                    } else {
                        _arg24 = null;
                    }
                    _result = reportGnssLocation(_arg0, _arg1, _arg24, data.readLong(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _result = reportGnssSvStatus(data.readInt(), data.readInt(), data.readInt(), data.createIntArray(), data.createFloatArray(), data.createFloatArray(), data.createFloatArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getUserType();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    String getNcDftParam(int i, List<String> list) throws RemoteException;

    int getUserType() throws RemoteException;

    int notifyNcDftEvent(int i, int i2, List<String> list) throws RemoteException;

    int reportGnssApkName(int i, int i2, LocationRequest locationRequest, String str) throws RemoteException;

    int reportGnssLocation(int i, int i2, Location location, long j, String str) throws RemoteException;

    int reportGnssSvStatus(int i, int i2, int i3, int[] iArr, float[] fArr, float[] fArr2, float[] fArr3) throws RemoteException;

    int reportNetworkInfo(int i, int i2, NetworkInfo networkInfo) throws RemoteException;

    int reportNlpLocation(int i, int i2, String str, ProviderRequest providerRequest) throws RemoteException;

    boolean triggerUpload(int i, int i2, int i3) throws RemoteException;
}
