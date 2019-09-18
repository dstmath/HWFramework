package android.location;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.internal.location.ProviderProperties;
import java.util.List;

public interface ILocationManager extends IInterface {

    public static abstract class Stub extends Binder implements ILocationManager {
        private static final String DESCRIPTOR = "android.location.ILocationManager";
        static final int TRANSACTION_addGnssBatchingCallback = 19;
        static final int TRANSACTION_addGnssMeasurementsListener = 12;
        static final int TRANSACTION_addGnssNavigationMessageListener = 14;
        static final int TRANSACTION_addTestProvider = 35;
        static final int TRANSACTION_clearTestProviderEnabled = 40;
        static final int TRANSACTION_clearTestProviderLocation = 38;
        static final int TRANSACTION_clearTestProviderStatus = 42;
        static final int TRANSACTION_flushGnssBatch = 22;
        static final int TRANSACTION_geocoderIsPresent = 8;
        static final int TRANSACTION_getAllProviders = 25;
        static final int TRANSACTION_getBackgroundThrottlingWhitelist = 47;
        static final int TRANSACTION_getBestProvider = 27;
        static final int TRANSACTION_getFromLocation = 9;
        static final int TRANSACTION_getFromLocationName = 10;
        static final int TRANSACTION_getGnssBatchSize = 18;
        static final int TRANSACTION_getGnssHardwareModelName = 17;
        static final int TRANSACTION_getGnssYearOfHardware = 16;
        static final int TRANSACTION_getLastLocation = 5;
        static final int TRANSACTION_getNetworkProviderPackage = 30;
        static final int TRANSACTION_getProviderProperties = 29;
        static final int TRANSACTION_getProviders = 26;
        static final int TRANSACTION_injectLocation = 24;
        static final int TRANSACTION_isLocationEnabledForUser = 33;
        static final int TRANSACTION_isProviderEnabledForUser = 31;
        static final int TRANSACTION_locationCallbackFinished = 46;
        static final int TRANSACTION_providerMeetsCriteria = 28;
        static final int TRANSACTION_registerGnssStatusCallback = 6;
        static final int TRANSACTION_removeGeofence = 4;
        static final int TRANSACTION_removeGnssBatchingCallback = 20;
        static final int TRANSACTION_removeGnssMeasurementsListener = 13;
        static final int TRANSACTION_removeGnssNavigationMessageListener = 15;
        static final int TRANSACTION_removeTestProvider = 36;
        static final int TRANSACTION_removeUpdates = 2;
        static final int TRANSACTION_reportLocation = 44;
        static final int TRANSACTION_reportLocationBatch = 45;
        static final int TRANSACTION_requestGeofence = 3;
        static final int TRANSACTION_requestLocationUpdates = 1;
        static final int TRANSACTION_sendExtraCommand = 43;
        static final int TRANSACTION_sendNiResponse = 11;
        static final int TRANSACTION_setLocationEnabledForUser = 34;
        static final int TRANSACTION_setProviderEnabledForUser = 32;
        static final int TRANSACTION_setTestProviderEnabled = 39;
        static final int TRANSACTION_setTestProviderLocation = 37;
        static final int TRANSACTION_setTestProviderStatus = 41;
        static final int TRANSACTION_startGnssBatch = 21;
        static final int TRANSACTION_stopGnssBatch = 23;
        static final int TRANSACTION_unregisterGnssStatusCallback = 7;

        private static class Proxy implements ILocationManager {
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

            public void requestLocationUpdates(LocationRequest request, ILocationListener listener, PendingIntent intent, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeUpdates(ILocationListener listener, PendingIntent intent, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestGeofence(LocationRequest request, Geofence geofence, PendingIntent intent, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (geofence != null) {
                        _data.writeInt(1);
                        geofence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeGeofence(Geofence fence, PendingIntent intent, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fence != null) {
                        _data.writeInt(1);
                        fence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Location getLastLocation(LocationRequest request, String packageName) throws RemoteException {
                Location _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Location.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerGnssStatusCallback(IGnssStatusListener callback, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(6, _data, _reply, 0);
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

            public void unregisterGnssStatusCallback(IGnssStatusListener callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean geocoderIsPresent() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(8, _data, _reply, 0);
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
                    this.mRemote.transact(9, _data, _reply, 0);
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
                String _result;
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
                                    this.mRemote.transact(10, _data, _reply, 0);
                                    _reply.readException();
                                    _result = _reply.readString();
                                } catch (Throwable th4) {
                                    th = th4;
                                    List<Address> list222 = addrs;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                List<Address> list2222 = addrs;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            int i222 = maxResults;
                            List<Address> list22222 = addrs;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        double d2222 = upperRightLongitude;
                        int i2222 = maxResults;
                        List<Address> list222222 = addrs;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _reply.readTypedList(addrs, Address.CREATOR);
                        _reply.recycle();
                        _data.recycle();
                        return _result;
                    } catch (Throwable th8) {
                        th = th8;
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

            public boolean sendNiResponse(int notifId, int userResponse) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(notifId);
                    _data.writeInt(userResponse);
                    boolean _result = false;
                    this.mRemote.transact(11, _data, _reply, 0);
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

            public boolean addGnssMeasurementsListener(IGnssMeasurementsListener listener, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(12, _data, _reply, 0);
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

            public void removeGnssMeasurementsListener(IGnssMeasurementsListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean addGnssNavigationMessageListener(IGnssNavigationMessageListener listener, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(14, _data, _reply, 0);
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

            public void removeGnssNavigationMessageListener(IGnssNavigationMessageListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getGnssYearOfHardware() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getGnssHardwareModelName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getGnssBatchSize(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean addGnssBatchingCallback(IBatchedLocationCallback callback, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(19, _data, _reply, 0);
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

            public void removeGnssBatchingCallback() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startGnssBatch(long periodNanos, boolean wakeOnFifoFull, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(periodNanos);
                    _data.writeInt(wakeOnFifoFull);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(21, _data, _reply, 0);
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

            public void flushGnssBatch(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean stopGnssBatch() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(23, _data, _reply, 0);
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

            public boolean injectLocation(Location location) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (location != null) {
                        _data.writeInt(1);
                        location.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getAllProviders() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getProviders(Criteria criteria, boolean enabledOnly) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (criteria != null) {
                        _data.writeInt(1);
                        criteria.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(enabledOnly);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getBestProvider(Criteria criteria, boolean enabledOnly) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (criteria != null) {
                        _data.writeInt(1);
                        criteria.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(enabledOnly);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean providerMeetsCriteria(String provider, Criteria criteria) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    boolean _result = true;
                    if (criteria != null) {
                        _data.writeInt(1);
                        criteria.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ProviderProperties getProviderProperties(String provider) throws RemoteException {
                ProviderProperties _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ProviderProperties) ProviderProperties.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getNetworkProviderPackage() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isProviderEnabledForUser(String provider, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(31, _data, _reply, 0);
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

            public boolean setProviderEnabledForUser(String provider, boolean enabled, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    _data.writeInt(enabled);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(32, _data, _reply, 0);
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

            public boolean isLocationEnabledForUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(33, _data, _reply, 0);
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

            public void setLocationEnabledForUser(boolean enabled, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled);
                    _data.writeInt(userId);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addTestProvider(String name, ProviderProperties properties, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    if (properties != null) {
                        _data.writeInt(1);
                        properties.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(opPackageName);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeTestProvider(String provider, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setTestProviderLocation(String provider, Location loc, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    if (loc != null) {
                        _data.writeInt(1);
                        loc.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(opPackageName);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearTestProviderLocation(String provider, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setTestProviderEnabled(String provider, boolean enabled, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    _data.writeInt(enabled);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearTestProviderEnabled(String provider, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setTestProviderStatus(String provider, int status, Bundle extras, long updateTime, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    _data.writeInt(status);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(updateTime);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearTestProviderStatus(String provider, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean sendExtraCommand(String provider, String command, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    _data.writeString(command);
                    boolean _result = true;
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    if (_reply.readInt() != 0) {
                        extras.readFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportLocation(Location location, boolean passive) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (location != null) {
                        _data.writeInt(1);
                        location.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(passive);
                    this.mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportLocationBatch(List<Location> locations) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(locations);
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void locationCallbackFinished(ILocationListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(46, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getBackgroundThrottlingWhitelist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILocationManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILocationManager)) {
                return new Proxy(obj);
            }
            return (ILocationManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v0, resolved type: com.android.internal.location.ProviderProperties} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v1, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v4, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v5, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v8, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v9, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v12, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v13, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v16, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v17, resolved type: android.location.LocationRequest} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v20, resolved type: android.location.LocationRequest} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v35, resolved type: android.location.Location} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v36, resolved type: android.location.Criteria} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v39, resolved type: android.location.Criteria} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v40, resolved type: android.location.Criteria} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v43, resolved type: android.location.Criteria} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v44, resolved type: com.android.internal.location.ProviderProperties} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v54, resolved type: com.android.internal.location.ProviderProperties} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v59, resolved type: com.android.internal.location.ProviderProperties} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v68, resolved type: com.android.internal.location.ProviderProperties} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v72, resolved type: com.android.internal.location.ProviderProperties} */
        /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
            java.lang.NullPointerException
            */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int r30, android.os.Parcel r31, android.os.Parcel r32, int r33) throws android.os.RemoteException {
            /*
                r29 = this;
                r13 = r29
                r14 = r30
                r15 = r31
                r12 = r32
                java.lang.String r10 = "android.location.ILocationManager"
                r0 = 1598968902(0x5f4e5446, float:1.4867585E19)
                r8 = 1
                if (r14 == r0) goto L_0x0570
                r0 = 0
                r1 = 0
                switch(r14) {
                    case 1: goto L_0x0537;
                    case 2: goto L_0x050e;
                    case 3: goto L_0x04cd;
                    case 4: goto L_0x049c;
                    case 5: goto L_0x046e;
                    case 6: goto L_0x0451;
                    case 7: goto L_0x043c;
                    case 8: goto L_0x042b;
                    case 9: goto L_0x03e8;
                    case 10: goto L_0x0390;
                    case 11: goto L_0x037a;
                    case 12: goto L_0x0360;
                    case 13: goto L_0x034e;
                    case 14: goto L_0x0334;
                    case 15: goto L_0x0322;
                    case 16: goto L_0x0314;
                    case 17: goto L_0x0306;
                    case 18: goto L_0x02f4;
                    case 19: goto L_0x02da;
                    case 20: goto L_0x02d0;
                    case 21: goto L_0x02b2;
                    case 22: goto L_0x02a4;
                    case 23: goto L_0x0296;
                    case 24: goto L_0x0276;
                    case 25: goto L_0x0268;
                    case 26: goto L_0x0242;
                    case 27: goto L_0x021c;
                    case 28: goto L_0x01fa;
                    case 29: goto L_0x01df;
                    case 30: goto L_0x01d1;
                    case 31: goto L_0x01bb;
                    case 32: goto L_0x019d;
                    case 33: goto L_0x018b;
                    case 34: goto L_0x0175;
                    case 35: goto L_0x0153;
                    case 36: goto L_0x0141;
                    case 37: goto L_0x011f;
                    case 38: goto L_0x010d;
                    case 39: goto L_0x00f3;
                    case 40: goto L_0x00e1;
                    case 41: goto L_0x00b0;
                    case 42: goto L_0x009e;
                    case 43: goto L_0x006c;
                    case 44: goto L_0x004a;
                    case 45: goto L_0x003a;
                    case 46: goto L_0x0028;
                    case 47: goto L_0x001a;
                    default: goto L_0x0015;
                }
            L_0x0015:
                boolean r0 = super.onTransact(r30, r31, r32, r33)
                return r0
            L_0x001a:
                r15.enforceInterface(r10)
                java.lang.String[] r0 = r29.getBackgroundThrottlingWhitelist()
                r32.writeNoException()
                r12.writeStringArray(r0)
                return r8
            L_0x0028:
                r15.enforceInterface(r10)
                android.os.IBinder r0 = r31.readStrongBinder()
                android.location.ILocationListener r0 = android.location.ILocationListener.Stub.asInterface(r0)
                r13.locationCallbackFinished(r0)
                r32.writeNoException()
                return r8
            L_0x003a:
                r15.enforceInterface(r10)
                android.os.Parcelable$Creator<android.location.Location> r0 = android.location.Location.CREATOR
                java.util.ArrayList r0 = r15.createTypedArrayList(r0)
                r13.reportLocationBatch(r0)
                r32.writeNoException()
                return r8
            L_0x004a:
                r15.enforceInterface(r10)
                int r2 = r31.readInt()
                if (r2 == 0) goto L_0x005c
                android.os.Parcelable$Creator<android.location.Location> r1 = android.location.Location.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r15)
                android.location.Location r1 = (android.location.Location) r1
                goto L_0x005d
            L_0x005c:
            L_0x005d:
                int r2 = r31.readInt()
                if (r2 == 0) goto L_0x0065
                r0 = r8
            L_0x0065:
                r13.reportLocation(r1, r0)
                r32.writeNoException()
                return r8
            L_0x006c:
                r15.enforceInterface(r10)
                java.lang.String r2 = r31.readString()
                java.lang.String r3 = r31.readString()
                int r4 = r31.readInt()
                if (r4 == 0) goto L_0x0086
                android.os.Parcelable$Creator r1 = android.os.Bundle.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r15)
                android.os.Bundle r1 = (android.os.Bundle) r1
                goto L_0x0087
            L_0x0086:
            L_0x0087:
                boolean r4 = r13.sendExtraCommand(r2, r3, r1)
                r32.writeNoException()
                r12.writeInt(r4)
                if (r1 == 0) goto L_0x009a
                r12.writeInt(r8)
                r1.writeToParcel(r12, r8)
                goto L_0x009d
            L_0x009a:
                r12.writeInt(r0)
            L_0x009d:
                return r8
            L_0x009e:
                r15.enforceInterface(r10)
                java.lang.String r0 = r31.readString()
                java.lang.String r1 = r31.readString()
                r13.clearTestProviderStatus(r0, r1)
                r32.writeNoException()
                return r8
            L_0x00b0:
                r15.enforceInterface(r10)
                java.lang.String r7 = r31.readString()
                int r9 = r31.readInt()
                int r0 = r31.readInt()
                if (r0 == 0) goto L_0x00cb
                android.os.Parcelable$Creator r0 = android.os.Bundle.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r15)
                android.os.Bundle r0 = (android.os.Bundle) r0
                r3 = r0
                goto L_0x00cc
            L_0x00cb:
                r3 = r1
            L_0x00cc:
                long r16 = r31.readLong()
                java.lang.String r11 = r31.readString()
                r0 = r13
                r1 = r7
                r2 = r9
                r4 = r16
                r6 = r11
                r0.setTestProviderStatus(r1, r2, r3, r4, r6)
                r32.writeNoException()
                return r8
            L_0x00e1:
                r15.enforceInterface(r10)
                java.lang.String r0 = r31.readString()
                java.lang.String r1 = r31.readString()
                r13.clearTestProviderEnabled(r0, r1)
                r32.writeNoException()
                return r8
            L_0x00f3:
                r15.enforceInterface(r10)
                java.lang.String r1 = r31.readString()
                int r2 = r31.readInt()
                if (r2 == 0) goto L_0x0102
                r0 = r8
            L_0x0102:
                java.lang.String r2 = r31.readString()
                r13.setTestProviderEnabled(r1, r0, r2)
                r32.writeNoException()
                return r8
            L_0x010d:
                r15.enforceInterface(r10)
                java.lang.String r0 = r31.readString()
                java.lang.String r1 = r31.readString()
                r13.clearTestProviderLocation(r0, r1)
                r32.writeNoException()
                return r8
            L_0x011f:
                r15.enforceInterface(r10)
                java.lang.String r0 = r31.readString()
                int r2 = r31.readInt()
                if (r2 == 0) goto L_0x0135
                android.os.Parcelable$Creator<android.location.Location> r1 = android.location.Location.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r15)
                android.location.Location r1 = (android.location.Location) r1
                goto L_0x0136
            L_0x0135:
            L_0x0136:
                java.lang.String r2 = r31.readString()
                r13.setTestProviderLocation(r0, r1, r2)
                r32.writeNoException()
                return r8
            L_0x0141:
                r15.enforceInterface(r10)
                java.lang.String r0 = r31.readString()
                java.lang.String r1 = r31.readString()
                r13.removeTestProvider(r0, r1)
                r32.writeNoException()
                return r8
            L_0x0153:
                r15.enforceInterface(r10)
                java.lang.String r0 = r31.readString()
                int r2 = r31.readInt()
                if (r2 == 0) goto L_0x0169
                android.os.Parcelable$Creator r1 = com.android.internal.location.ProviderProperties.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r15)
                com.android.internal.location.ProviderProperties r1 = (com.android.internal.location.ProviderProperties) r1
                goto L_0x016a
            L_0x0169:
            L_0x016a:
                java.lang.String r2 = r31.readString()
                r13.addTestProvider(r0, r1, r2)
                r32.writeNoException()
                return r8
            L_0x0175:
                r15.enforceInterface(r10)
                int r1 = r31.readInt()
                if (r1 == 0) goto L_0x0180
                r0 = r8
            L_0x0180:
                int r1 = r31.readInt()
                r13.setLocationEnabledForUser(r0, r1)
                r32.writeNoException()
                return r8
            L_0x018b:
                r15.enforceInterface(r10)
                int r0 = r31.readInt()
                boolean r1 = r13.isLocationEnabledForUser(r0)
                r32.writeNoException()
                r12.writeInt(r1)
                return r8
            L_0x019d:
                r15.enforceInterface(r10)
                java.lang.String r1 = r31.readString()
                int r2 = r31.readInt()
                if (r2 == 0) goto L_0x01ac
                r0 = r8
            L_0x01ac:
                int r2 = r31.readInt()
                boolean r3 = r13.setProviderEnabledForUser(r1, r0, r2)
                r32.writeNoException()
                r12.writeInt(r3)
                return r8
            L_0x01bb:
                r15.enforceInterface(r10)
                java.lang.String r0 = r31.readString()
                int r1 = r31.readInt()
                boolean r2 = r13.isProviderEnabledForUser(r0, r1)
                r32.writeNoException()
                r12.writeInt(r2)
                return r8
            L_0x01d1:
                r15.enforceInterface(r10)
                java.lang.String r0 = r29.getNetworkProviderPackage()
                r32.writeNoException()
                r12.writeString(r0)
                return r8
            L_0x01df:
                r15.enforceInterface(r10)
                java.lang.String r1 = r31.readString()
                com.android.internal.location.ProviderProperties r2 = r13.getProviderProperties(r1)
                r32.writeNoException()
                if (r2 == 0) goto L_0x01f6
                r12.writeInt(r8)
                r2.writeToParcel(r12, r8)
                goto L_0x01f9
            L_0x01f6:
                r12.writeInt(r0)
            L_0x01f9:
                return r8
            L_0x01fa:
                r15.enforceInterface(r10)
                java.lang.String r0 = r31.readString()
                int r2 = r31.readInt()
                if (r2 == 0) goto L_0x0210
                android.os.Parcelable$Creator<android.location.Criteria> r1 = android.location.Criteria.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r15)
                android.location.Criteria r1 = (android.location.Criteria) r1
                goto L_0x0211
            L_0x0210:
            L_0x0211:
                boolean r2 = r13.providerMeetsCriteria(r0, r1)
                r32.writeNoException()
                r12.writeInt(r2)
                return r8
            L_0x021c:
                r15.enforceInterface(r10)
                int r2 = r31.readInt()
                if (r2 == 0) goto L_0x022e
                android.os.Parcelable$Creator<android.location.Criteria> r1 = android.location.Criteria.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r15)
                android.location.Criteria r1 = (android.location.Criteria) r1
                goto L_0x022f
            L_0x022e:
            L_0x022f:
                int r2 = r31.readInt()
                if (r2 == 0) goto L_0x0237
                r0 = r8
            L_0x0237:
                java.lang.String r2 = r13.getBestProvider(r1, r0)
                r32.writeNoException()
                r12.writeString(r2)
                return r8
            L_0x0242:
                r15.enforceInterface(r10)
                int r2 = r31.readInt()
                if (r2 == 0) goto L_0x0254
                android.os.Parcelable$Creator<android.location.Criteria> r1 = android.location.Criteria.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r15)
                android.location.Criteria r1 = (android.location.Criteria) r1
                goto L_0x0255
            L_0x0254:
            L_0x0255:
                int r2 = r31.readInt()
                if (r2 == 0) goto L_0x025d
                r0 = r8
            L_0x025d:
                java.util.List r2 = r13.getProviders(r1, r0)
                r32.writeNoException()
                r12.writeStringList(r2)
                return r8
            L_0x0268:
                r15.enforceInterface(r10)
                java.util.List r0 = r29.getAllProviders()
                r32.writeNoException()
                r12.writeStringList(r0)
                return r8
            L_0x0276:
                r15.enforceInterface(r10)
                int r0 = r31.readInt()
                if (r0 == 0) goto L_0x0289
                android.os.Parcelable$Creator<android.location.Location> r0 = android.location.Location.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r15)
                r1 = r0
                android.location.Location r1 = (android.location.Location) r1
                goto L_0x028a
            L_0x0289:
            L_0x028a:
                r0 = r1
                boolean r1 = r13.injectLocation(r0)
                r32.writeNoException()
                r12.writeInt(r1)
                return r8
            L_0x0296:
                r15.enforceInterface(r10)
                boolean r0 = r29.stopGnssBatch()
                r32.writeNoException()
                r12.writeInt(r0)
                return r8
            L_0x02a4:
                r15.enforceInterface(r10)
                java.lang.String r0 = r31.readString()
                r13.flushGnssBatch(r0)
                r32.writeNoException()
                return r8
            L_0x02b2:
                r15.enforceInterface(r10)
                long r1 = r31.readLong()
                int r3 = r31.readInt()
                if (r3 == 0) goto L_0x02c1
                r0 = r8
            L_0x02c1:
                java.lang.String r3 = r31.readString()
                boolean r4 = r13.startGnssBatch(r1, r0, r3)
                r32.writeNoException()
                r12.writeInt(r4)
                return r8
            L_0x02d0:
                r15.enforceInterface(r10)
                r29.removeGnssBatchingCallback()
                r32.writeNoException()
                return r8
            L_0x02da:
                r15.enforceInterface(r10)
                android.os.IBinder r0 = r31.readStrongBinder()
                android.location.IBatchedLocationCallback r0 = android.location.IBatchedLocationCallback.Stub.asInterface(r0)
                java.lang.String r1 = r31.readString()
                boolean r2 = r13.addGnssBatchingCallback(r0, r1)
                r32.writeNoException()
                r12.writeInt(r2)
                return r8
            L_0x02f4:
                r15.enforceInterface(r10)
                java.lang.String r0 = r31.readString()
                int r1 = r13.getGnssBatchSize(r0)
                r32.writeNoException()
                r12.writeInt(r1)
                return r8
            L_0x0306:
                r15.enforceInterface(r10)
                java.lang.String r0 = r29.getGnssHardwareModelName()
                r32.writeNoException()
                r12.writeString(r0)
                return r8
            L_0x0314:
                r15.enforceInterface(r10)
                int r0 = r29.getGnssYearOfHardware()
                r32.writeNoException()
                r12.writeInt(r0)
                return r8
            L_0x0322:
                r15.enforceInterface(r10)
                android.os.IBinder r0 = r31.readStrongBinder()
                android.location.IGnssNavigationMessageListener r0 = android.location.IGnssNavigationMessageListener.Stub.asInterface(r0)
                r13.removeGnssNavigationMessageListener(r0)
                r32.writeNoException()
                return r8
            L_0x0334:
                r15.enforceInterface(r10)
                android.os.IBinder r0 = r31.readStrongBinder()
                android.location.IGnssNavigationMessageListener r0 = android.location.IGnssNavigationMessageListener.Stub.asInterface(r0)
                java.lang.String r1 = r31.readString()
                boolean r2 = r13.addGnssNavigationMessageListener(r0, r1)
                r32.writeNoException()
                r12.writeInt(r2)
                return r8
            L_0x034e:
                r15.enforceInterface(r10)
                android.os.IBinder r0 = r31.readStrongBinder()
                android.location.IGnssMeasurementsListener r0 = android.location.IGnssMeasurementsListener.Stub.asInterface(r0)
                r13.removeGnssMeasurementsListener(r0)
                r32.writeNoException()
                return r8
            L_0x0360:
                r15.enforceInterface(r10)
                android.os.IBinder r0 = r31.readStrongBinder()
                android.location.IGnssMeasurementsListener r0 = android.location.IGnssMeasurementsListener.Stub.asInterface(r0)
                java.lang.String r1 = r31.readString()
                boolean r2 = r13.addGnssMeasurementsListener(r0, r1)
                r32.writeNoException()
                r12.writeInt(r2)
                return r8
            L_0x037a:
                r15.enforceInterface(r10)
                int r0 = r31.readInt()
                int r1 = r31.readInt()
                boolean r2 = r13.sendNiResponse(r0, r1)
                r32.writeNoException()
                r12.writeInt(r2)
                return r8
            L_0x0390:
                r15.enforceInterface(r10)
                java.lang.String r16 = r31.readString()
                double r17 = r31.readDouble()
                double r19 = r31.readDouble()
                double r21 = r31.readDouble()
                double r23 = r31.readDouble()
                int r25 = r31.readInt()
                int r0 = r31.readInt()
                if (r0 == 0) goto L_0x03bb
                android.os.Parcelable$Creator<android.location.GeocoderParams> r0 = android.location.GeocoderParams.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r15)
                android.location.GeocoderParams r0 = (android.location.GeocoderParams) r0
                r11 = r0
                goto L_0x03bc
            L_0x03bb:
                r11 = r1
            L_0x03bc:
                java.util.ArrayList r0 = new java.util.ArrayList
                r0.<init>()
                r9 = r0
                r0 = r13
                r1 = r16
                r2 = r17
                r4 = r19
                r6 = r21
                r14 = r8
                r26 = r9
                r8 = r23
                r27 = r10
                r10 = r25
                r12 = r26
                java.lang.String r0 = r0.getFromLocationName(r1, r2, r4, r6, r8, r10, r11, r12)
                r32.writeNoException()
                r8 = r32
                r8.writeString(r0)
                r1 = r26
                r8.writeTypedList(r1)
                return r14
            L_0x03e8:
                r14 = r8
                r27 = r10
                r8 = r12
                r9 = r27
                r15.enforceInterface(r9)
                double r10 = r31.readDouble()
                double r16 = r31.readDouble()
                int r12 = r31.readInt()
                int r0 = r31.readInt()
                if (r0 == 0) goto L_0x040d
                android.os.Parcelable$Creator<android.location.GeocoderParams> r0 = android.location.GeocoderParams.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r15)
                android.location.GeocoderParams r0 = (android.location.GeocoderParams) r0
                r6 = r0
                goto L_0x040e
            L_0x040d:
                r6 = r1
            L_0x040e:
                java.util.ArrayList r0 = new java.util.ArrayList
                r0.<init>()
                r7 = r0
                r0 = r13
                r1 = r10
                r3 = r16
                r5 = r12
                r28 = r7
                java.lang.String r0 = r0.getFromLocation(r1, r3, r5, r6, r7)
                r32.writeNoException()
                r8.writeString(r0)
                r1 = r28
                r8.writeTypedList(r1)
                return r14
            L_0x042b:
                r14 = r8
                r9 = r10
                r8 = r12
                r15.enforceInterface(r9)
                boolean r0 = r29.geocoderIsPresent()
                r32.writeNoException()
                r8.writeInt(r0)
                return r14
            L_0x043c:
                r14 = r8
                r9 = r10
                r8 = r12
                r15.enforceInterface(r9)
                android.os.IBinder r0 = r31.readStrongBinder()
                android.location.IGnssStatusListener r0 = android.location.IGnssStatusListener.Stub.asInterface(r0)
                r13.unregisterGnssStatusCallback(r0)
                r32.writeNoException()
                return r14
            L_0x0451:
                r14 = r8
                r9 = r10
                r8 = r12
                r15.enforceInterface(r9)
                android.os.IBinder r0 = r31.readStrongBinder()
                android.location.IGnssStatusListener r0 = android.location.IGnssStatusListener.Stub.asInterface(r0)
                java.lang.String r1 = r31.readString()
                boolean r2 = r13.registerGnssStatusCallback(r0, r1)
                r32.writeNoException()
                r8.writeInt(r2)
                return r14
            L_0x046e:
                r14 = r8
                r9 = r10
                r8 = r12
                r15.enforceInterface(r9)
                int r2 = r31.readInt()
                if (r2 == 0) goto L_0x0483
                android.os.Parcelable$Creator<android.location.LocationRequest> r1 = android.location.LocationRequest.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r15)
                android.location.LocationRequest r1 = (android.location.LocationRequest) r1
                goto L_0x0484
            L_0x0483:
            L_0x0484:
                java.lang.String r2 = r31.readString()
                android.location.Location r3 = r13.getLastLocation(r1, r2)
                r32.writeNoException()
                if (r3 == 0) goto L_0x0498
                r8.writeInt(r14)
                r3.writeToParcel(r8, r14)
                goto L_0x049b
            L_0x0498:
                r8.writeInt(r0)
            L_0x049b:
                return r14
            L_0x049c:
                r14 = r8
                r9 = r10
                r8 = r12
                r15.enforceInterface(r9)
                int r0 = r31.readInt()
                if (r0 == 0) goto L_0x04b1
                android.os.Parcelable$Creator<android.location.Geofence> r0 = android.location.Geofence.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r15)
                android.location.Geofence r0 = (android.location.Geofence) r0
                goto L_0x04b2
            L_0x04b1:
                r0 = r1
            L_0x04b2:
                int r2 = r31.readInt()
                if (r2 == 0) goto L_0x04c1
                android.os.Parcelable$Creator<android.app.PendingIntent> r1 = android.app.PendingIntent.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r15)
                android.app.PendingIntent r1 = (android.app.PendingIntent) r1
                goto L_0x04c2
            L_0x04c1:
            L_0x04c2:
                java.lang.String r2 = r31.readString()
                r13.removeGeofence(r0, r1, r2)
                r32.writeNoException()
                return r14
            L_0x04cd:
                r14 = r8
                r9 = r10
                r8 = r12
                r15.enforceInterface(r9)
                int r0 = r31.readInt()
                if (r0 == 0) goto L_0x04e2
                android.os.Parcelable$Creator<android.location.LocationRequest> r0 = android.location.LocationRequest.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r15)
                android.location.LocationRequest r0 = (android.location.LocationRequest) r0
                goto L_0x04e3
            L_0x04e2:
                r0 = r1
            L_0x04e3:
                int r2 = r31.readInt()
                if (r2 == 0) goto L_0x04f2
                android.os.Parcelable$Creator<android.location.Geofence> r2 = android.location.Geofence.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r15)
                android.location.Geofence r2 = (android.location.Geofence) r2
                goto L_0x04f3
            L_0x04f2:
                r2 = r1
            L_0x04f3:
                int r3 = r31.readInt()
                if (r3 == 0) goto L_0x0502
                android.os.Parcelable$Creator<android.app.PendingIntent> r1 = android.app.PendingIntent.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r15)
                android.app.PendingIntent r1 = (android.app.PendingIntent) r1
                goto L_0x0503
            L_0x0502:
            L_0x0503:
                java.lang.String r3 = r31.readString()
                r13.requestGeofence(r0, r2, r1, r3)
                r32.writeNoException()
                return r14
            L_0x050e:
                r14 = r8
                r9 = r10
                r8 = r12
                r15.enforceInterface(r9)
                android.os.IBinder r0 = r31.readStrongBinder()
                android.location.ILocationListener r0 = android.location.ILocationListener.Stub.asInterface(r0)
                int r2 = r31.readInt()
                if (r2 == 0) goto L_0x052b
                android.os.Parcelable$Creator<android.app.PendingIntent> r1 = android.app.PendingIntent.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r15)
                android.app.PendingIntent r1 = (android.app.PendingIntent) r1
                goto L_0x052c
            L_0x052b:
            L_0x052c:
                java.lang.String r2 = r31.readString()
                r13.removeUpdates(r0, r1, r2)
                r32.writeNoException()
                return r14
            L_0x0537:
                r14 = r8
                r9 = r10
                r8 = r12
                r15.enforceInterface(r9)
                int r0 = r31.readInt()
                if (r0 == 0) goto L_0x054c
                android.os.Parcelable$Creator<android.location.LocationRequest> r0 = android.location.LocationRequest.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r15)
                android.location.LocationRequest r0 = (android.location.LocationRequest) r0
                goto L_0x054d
            L_0x054c:
                r0 = r1
            L_0x054d:
                android.os.IBinder r2 = r31.readStrongBinder()
                android.location.ILocationListener r2 = android.location.ILocationListener.Stub.asInterface(r2)
                int r3 = r31.readInt()
                if (r3 == 0) goto L_0x0564
                android.os.Parcelable$Creator<android.app.PendingIntent> r1 = android.app.PendingIntent.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r15)
                android.app.PendingIntent r1 = (android.app.PendingIntent) r1
                goto L_0x0565
            L_0x0564:
            L_0x0565:
                java.lang.String r3 = r31.readString()
                r13.requestLocationUpdates(r0, r2, r1, r3)
                r32.writeNoException()
                return r14
            L_0x0570:
                r14 = r8
                r9 = r10
                r8 = r12
                r8.writeString(r9)
                return r14
            */
            throw new UnsupportedOperationException("Method not decompiled: android.location.ILocationManager.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }
    }

    boolean addGnssBatchingCallback(IBatchedLocationCallback iBatchedLocationCallback, String str) throws RemoteException;

    boolean addGnssMeasurementsListener(IGnssMeasurementsListener iGnssMeasurementsListener, String str) throws RemoteException;

    boolean addGnssNavigationMessageListener(IGnssNavigationMessageListener iGnssNavigationMessageListener, String str) throws RemoteException;

    void addTestProvider(String str, ProviderProperties providerProperties, String str2) throws RemoteException;

    void clearTestProviderEnabled(String str, String str2) throws RemoteException;

    void clearTestProviderLocation(String str, String str2) throws RemoteException;

    void clearTestProviderStatus(String str, String str2) throws RemoteException;

    void flushGnssBatch(String str) throws RemoteException;

    boolean geocoderIsPresent() throws RemoteException;

    List<String> getAllProviders() throws RemoteException;

    String[] getBackgroundThrottlingWhitelist() throws RemoteException;

    String getBestProvider(Criteria criteria, boolean z) throws RemoteException;

    String getFromLocation(double d, double d2, int i, GeocoderParams geocoderParams, List<Address> list) throws RemoteException;

    String getFromLocationName(String str, double d, double d2, double d3, double d4, int i, GeocoderParams geocoderParams, List<Address> list) throws RemoteException;

    int getGnssBatchSize(String str) throws RemoteException;

    String getGnssHardwareModelName() throws RemoteException;

    int getGnssYearOfHardware() throws RemoteException;

    Location getLastLocation(LocationRequest locationRequest, String str) throws RemoteException;

    String getNetworkProviderPackage() throws RemoteException;

    ProviderProperties getProviderProperties(String str) throws RemoteException;

    List<String> getProviders(Criteria criteria, boolean z) throws RemoteException;

    boolean injectLocation(Location location) throws RemoteException;

    boolean isLocationEnabledForUser(int i) throws RemoteException;

    boolean isProviderEnabledForUser(String str, int i) throws RemoteException;

    void locationCallbackFinished(ILocationListener iLocationListener) throws RemoteException;

    boolean providerMeetsCriteria(String str, Criteria criteria) throws RemoteException;

    boolean registerGnssStatusCallback(IGnssStatusListener iGnssStatusListener, String str) throws RemoteException;

    void removeGeofence(Geofence geofence, PendingIntent pendingIntent, String str) throws RemoteException;

    void removeGnssBatchingCallback() throws RemoteException;

    void removeGnssMeasurementsListener(IGnssMeasurementsListener iGnssMeasurementsListener) throws RemoteException;

    void removeGnssNavigationMessageListener(IGnssNavigationMessageListener iGnssNavigationMessageListener) throws RemoteException;

    void removeTestProvider(String str, String str2) throws RemoteException;

    void removeUpdates(ILocationListener iLocationListener, PendingIntent pendingIntent, String str) throws RemoteException;

    void reportLocation(Location location, boolean z) throws RemoteException;

    void reportLocationBatch(List<Location> list) throws RemoteException;

    void requestGeofence(LocationRequest locationRequest, Geofence geofence, PendingIntent pendingIntent, String str) throws RemoteException;

    void requestLocationUpdates(LocationRequest locationRequest, ILocationListener iLocationListener, PendingIntent pendingIntent, String str) throws RemoteException;

    boolean sendExtraCommand(String str, String str2, Bundle bundle) throws RemoteException;

    boolean sendNiResponse(int i, int i2) throws RemoteException;

    void setLocationEnabledForUser(boolean z, int i) throws RemoteException;

    boolean setProviderEnabledForUser(String str, boolean z, int i) throws RemoteException;

    void setTestProviderEnabled(String str, boolean z, String str2) throws RemoteException;

    void setTestProviderLocation(String str, Location location, String str2) throws RemoteException;

    void setTestProviderStatus(String str, int i, Bundle bundle, long j, String str2) throws RemoteException;

    boolean startGnssBatch(long j, boolean z, String str) throws RemoteException;

    boolean stopGnssBatch() throws RemoteException;

    void unregisterGnssStatusCallback(IGnssStatusListener iGnssStatusListener) throws RemoteException;
}
