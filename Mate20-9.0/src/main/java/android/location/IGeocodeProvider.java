package android.location;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface IGeocodeProvider extends IInterface {

    public static abstract class Stub extends Binder implements IGeocodeProvider {
        private static final String DESCRIPTOR = "android.location.IGeocodeProvider";
        static final int TRANSACTION_getFromLocation = 1;
        static final int TRANSACTION_getFromLocationName = 2;

        private static class Proxy implements IGeocodeProvider {
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

            public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeDouble(latitude);
                    _data.writeDouble(longitude);
                    _data.writeInt(maxResults);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.readTypedList(addrs, Address.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) throws RemoteException {
                GeocoderParams geocoderParams = params;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(locationName);
                        try {
                            _data.writeDouble(lowerLeftLatitude);
                            try {
                                _data.writeDouble(lowerLeftLongitude);
                            } catch (Throwable th) {
                                th = th;
                                double d = upperRightLatitude;
                                double d2 = upperRightLongitude;
                                int i = maxResults;
                                List<Address> list = addrs;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            double d3 = lowerLeftLongitude;
                            double d4 = upperRightLatitude;
                            double d22 = upperRightLongitude;
                            int i2 = maxResults;
                            List<Address> list2 = addrs;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        double d5 = lowerLeftLatitude;
                        double d32 = lowerLeftLongitude;
                        double d42 = upperRightLatitude;
                        double d222 = upperRightLongitude;
                        int i22 = maxResults;
                        List<Address> list22 = addrs;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeDouble(upperRightLatitude);
                        try {
                            _data.writeDouble(upperRightLongitude);
                            try {
                                _data.writeInt(maxResults);
                                if (geocoderParams != null) {
                                    _data.writeInt(1);
                                    geocoderParams.writeToParcel(_data, 0);
                                } else {
                                    _data.writeInt(0);
                                }
                                try {
                                    this.mRemote.transact(2, _data, _reply, 0);
                                    _reply.readException();
                                    String _result = _reply.readString();
                                    try {
                                        _reply.readTypedList(addrs, Address.CREATOR);
                                        _reply.recycle();
                                        _data.recycle();
                                        return _result;
                                    } catch (Throwable th4) {
                                        th = th4;
                                        _reply.recycle();
                                        _data.recycle();
                                        throw th;
                                    }
                                } catch (Throwable th5) {
                                    th = th5;
                                    List<Address> list222 = addrs;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th6) {
                                th = th6;
                                List<Address> list2222 = addrs;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            int i222 = maxResults;
                            List<Address> list22222 = addrs;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        double d2222 = upperRightLongitude;
                        int i2222 = maxResults;
                        List<Address> list222222 = addrs;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th9) {
                    th = th9;
                    String str = locationName;
                    double d52 = lowerLeftLatitude;
                    double d322 = lowerLeftLongitude;
                    double d422 = upperRightLatitude;
                    double d22222 = upperRightLongitude;
                    int i22222 = maxResults;
                    List<Address> list2222222 = addrs;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGeocodeProvider asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGeocodeProvider)) {
                return new Proxy(obj);
            }
            return (IGeocodeProvider) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                GeocoderParams geocoderParams = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        double _arg0 = data.readDouble();
                        double _arg1 = data.readDouble();
                        int _arg2 = data.readInt();
                        if (data.readInt() != 0) {
                            geocoderParams = GeocoderParams.CREATOR.createFromParcel(parcel);
                        }
                        GeocoderParams _arg3 = geocoderParams;
                        ArrayList arrayList = new ArrayList();
                        String _result = getFromLocation(_arg0, _arg1, _arg2, _arg3, arrayList);
                        reply.writeNoException();
                        parcel2.writeString(_result);
                        parcel2.writeTypedList(arrayList);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        double _arg12 = data.readDouble();
                        double _arg22 = data.readDouble();
                        double _arg32 = data.readDouble();
                        double _arg4 = data.readDouble();
                        int _arg5 = data.readInt();
                        if (data.readInt() != 0) {
                            geocoderParams = GeocoderParams.CREATOR.createFromParcel(parcel);
                        }
                        GeocoderParams _arg6 = geocoderParams;
                        ArrayList arrayList2 = new ArrayList();
                        String _result2 = getFromLocationName(_arg02, _arg12, _arg22, _arg32, _arg4, _arg5, _arg6, arrayList2);
                        reply.writeNoException();
                        parcel2.writeString(_result2);
                        parcel2.writeTypedList(arrayList2);
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

    String getFromLocation(double d, double d2, int i, GeocoderParams geocoderParams, List<Address> list) throws RemoteException;

    String getFromLocationName(String str, double d, double d2, double d3, double d4, int i, GeocoderParams geocoderParams, List<Address> list) throws RemoteException;
}
