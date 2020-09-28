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

    public static class Default implements INcDft {
        @Override // com.huawei.ncdft.INcDft
        public int notifyNcDftEvent(int domain, int ncEventID, List<String> list) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.ncdft.INcDft
        public int notifyNcDftBundleEvent(int domain, int ncEventID, Bundle data) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.ncdft.INcDft
        public String getNcDftParam(int domain, List<String> list) throws RemoteException {
            return null;
        }

        @Override // com.huawei.ncdft.INcDft
        public boolean triggerUpload(int domain, int event, int errorCode) throws RemoteException {
            return false;
        }

        @Override // com.huawei.ncdft.INcDft
        public int reportNlpLocation(int domain, int event, String provider, ProviderRequest providerRequest) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.ncdft.INcDft
        public int reportGnssApkName(int domain, int event, LocationRequest request, String name) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.ncdft.INcDft
        public int reportNetworkInfo(int domain, int event, NetworkInfo info) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.ncdft.INcDft
        public int reportGnssLocation(int domain, int event, Location info, long time, String provider) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.ncdft.INcDft
        public int reportGnssSvStatus(int domain, int event, int svCount, int[] svs, float[] snrs, float[] svElevations, float[] svAzimuths) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.ncdft.INcDft
        public int getUserType() throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

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

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg2;
            ProviderRequest _arg3;
            LocationRequest _arg22;
            NetworkInfo _arg23;
            Location _arg24;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = notifyNcDftEvent(data.readInt(), data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        int _arg1 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        int _result2 = notifyNcDftBundleEvent(_arg0, _arg1, _arg2);
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _result3 = getNcDftParam(data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeString(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean triggerUpload = triggerUpload(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(triggerUpload ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        int _arg12 = data.readInt();
                        String _arg25 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = (ProviderRequest) ProviderRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        int _result4 = reportNlpLocation(_arg02, _arg12, _arg25, _arg3);
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        int _arg13 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = (LocationRequest) LocationRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        int _result5 = reportGnssApkName(_arg03, _arg13, _arg22, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        int _arg14 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = (NetworkInfo) NetworkInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        int _result6 = reportNetworkInfo(_arg04, _arg14, _arg23);
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        int _arg15 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg24 = (Location) Location.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        int _result7 = reportGnssLocation(_arg05, _arg15, _arg24, data.readLong(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = reportGnssSvStatus(data.readInt(), data.readInt(), data.readInt(), data.createIntArray(), data.createFloatArray(), data.createFloatArray(), data.createFloatArray());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = getUserType();
                        reply.writeNoException();
                        reply.writeInt(_result9);
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
        public static class Proxy implements INcDft {
            public static INcDft sDefaultImpl;
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

            @Override // com.huawei.ncdft.INcDft
            public int notifyNcDftEvent(int domain, int ncEventID, List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(domain);
                    _data.writeInt(ncEventID);
                    _data.writeStringList(list);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().notifyNcDftEvent(domain, ncEventID, list);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.ncdft.INcDft
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
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().notifyNcDftBundleEvent(domain, ncEventID, data);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.ncdft.INcDft
            public String getNcDftParam(int domain, List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(domain);
                    _data.writeStringList(list);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNcDftParam(domain, list);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.ncdft.INcDft
            public boolean triggerUpload(int domain, int event, int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(domain);
                    _data.writeInt(event);
                    _data.writeInt(errorCode);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().triggerUpload(domain, event, errorCode);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.ncdft.INcDft
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
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().reportNlpLocation(domain, event, provider, providerRequest);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.ncdft.INcDft
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
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().reportGnssApkName(domain, event, request, name);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.ncdft.INcDft
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
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().reportNetworkInfo(domain, event, info);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.ncdft.INcDft
            public int reportGnssLocation(int domain, int event, Location info, long time, String provider) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(domain);
                        try {
                            _data.writeInt(event);
                            if (info != null) {
                                _data.writeInt(1);
                                info.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                        } catch (Throwable th) {
                            th = th;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeLong(time);
                            try {
                                _data.writeString(provider);
                                if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                    _reply.readException();
                                    int _result = _reply.readInt();
                                    _reply.recycle();
                                    _data.recycle();
                                    return _result;
                                }
                                int reportGnssLocation = Stub.getDefaultImpl().reportGnssLocation(domain, event, info, time, provider);
                                _reply.recycle();
                                _data.recycle();
                                return reportGnssLocation;
                            } catch (Throwable th2) {
                                th = th2;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.huawei.ncdft.INcDft
            public int reportGnssSvStatus(int domain, int event, int svCount, int[] svs, float[] snrs, float[] svElevations, float[] svAzimuths) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(domain);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(event);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(svCount);
                        try {
                            _data.writeIntArray(svs);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeFloatArray(snrs);
                            _data.writeFloatArray(svElevations);
                            _data.writeFloatArray(svAzimuths);
                            if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int reportGnssSvStatus = Stub.getDefaultImpl().reportGnssSvStatus(domain, event, svCount, svs, snrs, svElevations, svAzimuths);
                            _reply.recycle();
                            _data.recycle();
                            return reportGnssSvStatus;
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.huawei.ncdft.INcDft
            public int getUserType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUserType();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INcDft impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INcDft getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
