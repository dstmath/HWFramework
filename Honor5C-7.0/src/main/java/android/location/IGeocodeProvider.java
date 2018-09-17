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
                        _data.writeInt(Stub.TRANSACTION_getFromLocation);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getFromLocation, _data, _reply, 0);
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
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(locationName);
                    _data.writeDouble(lowerLeftLatitude);
                    _data.writeDouble(lowerLeftLongitude);
                    _data.writeDouble(upperRightLatitude);
                    _data.writeDouble(upperRightLongitude);
                    _data.writeInt(maxResults);
                    if (params != null) {
                        _data.writeInt(Stub.TRANSACTION_getFromLocation);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getFromLocationName, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.readTypedList(addrs, Address.CREATOR);
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
            double _arg1;
            String _result;
            switch (code) {
                case TRANSACTION_getFromLocation /*1*/:
                    GeocoderParams geocoderParams;
                    data.enforceInterface(DESCRIPTOR);
                    double _arg0 = data.readDouble();
                    _arg1 = data.readDouble();
                    int _arg2 = data.readInt();
                    if (data.readInt() != 0) {
                        geocoderParams = (GeocoderParams) GeocoderParams.CREATOR.createFromParcel(data);
                    } else {
                        geocoderParams = null;
                    }
                    List<Address> _arg4 = new ArrayList();
                    _result = getFromLocation(_arg0, _arg1, _arg2, geocoderParams, _arg4);
                    reply.writeNoException();
                    reply.writeString(_result);
                    reply.writeTypedList(_arg4);
                    return true;
                case TRANSACTION_getFromLocationName /*2*/:
                    GeocoderParams geocoderParams2;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg02 = data.readString();
                    _arg1 = data.readDouble();
                    double _arg22 = data.readDouble();
                    double _arg3 = data.readDouble();
                    double _arg42 = data.readDouble();
                    int _arg5 = data.readInt();
                    if (data.readInt() != 0) {
                        geocoderParams2 = (GeocoderParams) GeocoderParams.CREATOR.createFromParcel(data);
                    } else {
                        geocoderParams2 = null;
                    }
                    List<Address> _arg7 = new ArrayList();
                    _result = getFromLocationName(_arg02, _arg1, _arg22, _arg3, _arg42, _arg5, geocoderParams2, _arg7);
                    reply.writeNoException();
                    reply.writeString(_result);
                    reply.writeTypedList(_arg7);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    String getFromLocation(double d, double d2, int i, GeocoderParams geocoderParams, List<Address> list) throws RemoteException;

    String getFromLocationName(String str, double d, double d2, double d3, double d4, int i, GeocoderParams geocoderParams, List<Address> list) throws RemoteException;
}
