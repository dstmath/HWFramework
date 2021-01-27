package android.location;

import android.annotation.UnsupportedAppUsage;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface IGeocodeProvider extends IInterface {
    @UnsupportedAppUsage
    String getFromLocation(double d, double d2, int i, GeocoderParams geocoderParams, List<Address> list) throws RemoteException;

    @UnsupportedAppUsage
    String getFromLocationName(String str, double d, double d2, double d3, double d4, int i, GeocoderParams geocoderParams, List<Address> list) throws RemoteException;

    public static class Default implements IGeocodeProvider {
        @Override // android.location.IGeocodeProvider
        public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> list) throws RemoteException {
            return null;
        }

        @Override // android.location.IGeocodeProvider
        public String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> list) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGeocodeProvider {
        private static final String DESCRIPTOR = "android.location.IGeocodeProvider";
        static final int TRANSACTION_getFromLocation = 1;
        static final int TRANSACTION_getFromLocationName = 2;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "getFromLocation";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "getFromLocationName";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            GeocoderParams _arg3;
            GeocoderParams _arg6;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                double _arg0 = data.readDouble();
                double _arg1 = data.readDouble();
                int _arg2 = data.readInt();
                if (data.readInt() != 0) {
                    _arg3 = GeocoderParams.CREATOR.createFromParcel(data);
                } else {
                    _arg3 = null;
                }
                ArrayList arrayList = new ArrayList();
                String _result = getFromLocation(_arg0, _arg1, _arg2, _arg3, arrayList);
                reply.writeNoException();
                reply.writeString(_result);
                reply.writeTypedList(arrayList);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                String _arg02 = data.readString();
                double _arg12 = data.readDouble();
                double _arg22 = data.readDouble();
                double _arg32 = data.readDouble();
                double _arg4 = data.readDouble();
                int _arg5 = data.readInt();
                if (data.readInt() != 0) {
                    _arg6 = GeocoderParams.CREATOR.createFromParcel(data);
                } else {
                    _arg6 = null;
                }
                ArrayList arrayList2 = new ArrayList();
                String _result2 = getFromLocationName(_arg02, _arg12, _arg22, _arg32, _arg4, _arg5, _arg6, arrayList2);
                reply.writeNoException();
                reply.writeString(_result2);
                reply.writeTypedList(arrayList2);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IGeocodeProvider {
            public static IGeocodeProvider sDefaultImpl;
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

            @Override // android.location.IGeocodeProvider
            public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeDouble(latitude);
                        try {
                            _data.writeDouble(longitude);
                            _data.writeInt(maxResults);
                            if (params != null) {
                                _data.writeInt(1);
                                params.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                String _result = _reply.readString();
                                try {
                                    _reply.readTypedList(addrs, Address.CREATOR);
                                    _reply.recycle();
                                    _data.recycle();
                                    return _result;
                                } catch (Throwable th2) {
                                    th = th2;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } else {
                                String fromLocation = Stub.getDefaultImpl().getFromLocation(latitude, longitude, maxResults, params, addrs);
                                _reply.recycle();
                                _data.recycle();
                                return fromLocation;
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

            @Override // android.location.IGeocodeProvider
            public String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) throws RemoteException {
                Parcel _reply;
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply2 = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(locationName);
                    _data.writeDouble(lowerLeftLatitude);
                    _data.writeDouble(lowerLeftLongitude);
                    _data.writeDouble(upperRightLatitude);
                    _data.writeDouble(upperRightLongitude);
                    _data.writeInt(maxResults);
                    if (params != null) {
                        try {
                            _data.writeInt(1);
                            params.writeToParcel(_data, 0);
                        } catch (Throwable th2) {
                            th = th2;
                            _reply = _reply2;
                        }
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(2, _data, _reply2, 0)) {
                        try {
                            if (Stub.getDefaultImpl() != null) {
                                try {
                                    String fromLocationName = Stub.getDefaultImpl().getFromLocationName(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
                                    _reply2.recycle();
                                    _data.recycle();
                                    return fromLocationName;
                                } catch (Throwable th3) {
                                    th = th3;
                                    _reply = _reply2;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _reply = _reply2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    }
                    try {
                        _reply2.readException();
                        String _result = _reply2.readString();
                        _reply = _reply2;
                        try {
                            _reply.readTypedList(addrs, Address.CREATOR);
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply = _reply2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply = _reply2;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }
        }

        public static boolean setDefaultImpl(IGeocodeProvider impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGeocodeProvider getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
