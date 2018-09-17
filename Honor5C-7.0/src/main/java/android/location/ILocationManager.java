package android.location;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.internal.location.ProviderProperties;
import java.util.ArrayList;
import java.util.List;

public interface ILocationManager extends IInterface {

    public static abstract class Stub extends Binder implements ILocationManager {
        private static final String DESCRIPTOR = "android.location.ILocationManager";
        static final int TRANSACTION_addGnssMeasurementsListener = 12;
        static final int TRANSACTION_addGnssNavigationMessageListener = 14;
        static final int TRANSACTION_addTestProvider = 24;
        static final int TRANSACTION_clearTestProviderEnabled = 29;
        static final int TRANSACTION_clearTestProviderLocation = 27;
        static final int TRANSACTION_clearTestProviderStatus = 31;
        static final int TRANSACTION_geocoderIsPresent = 8;
        static final int TRANSACTION_getAllProviders = 17;
        static final int TRANSACTION_getBestProvider = 19;
        static final int TRANSACTION_getFromLocation = 9;
        static final int TRANSACTION_getFromLocationName = 10;
        static final int TRANSACTION_getGnssYearOfHardware = 16;
        static final int TRANSACTION_getLastLocation = 5;
        static final int TRANSACTION_getNetworkProviderPackage = 22;
        static final int TRANSACTION_getProviderProperties = 21;
        static final int TRANSACTION_getProviders = 18;
        static final int TRANSACTION_isProviderEnabled = 23;
        static final int TRANSACTION_locationCallbackFinished = 34;
        static final int TRANSACTION_providerMeetsCriteria = 20;
        static final int TRANSACTION_registerGnssStatusCallback = 6;
        static final int TRANSACTION_removeGeofence = 4;
        static final int TRANSACTION_removeGnssMeasurementsListener = 13;
        static final int TRANSACTION_removeGnssNavigationMessageListener = 15;
        static final int TRANSACTION_removeTestProvider = 25;
        static final int TRANSACTION_removeUpdates = 2;
        static final int TRANSACTION_reportLocation = 33;
        static final int TRANSACTION_requestGeofence = 3;
        static final int TRANSACTION_requestLocationUpdates = 1;
        static final int TRANSACTION_sendExtraCommand = 32;
        static final int TRANSACTION_sendNiResponse = 11;
        static final int TRANSACTION_setTestProviderEnabled = 28;
        static final int TRANSACTION_setTestProviderLocation = 26;
        static final int TRANSACTION_setTestProviderStatus = 30;
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
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(Stub.TRANSACTION_requestLocationUpdates);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_requestLocationUpdates);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_requestLocationUpdates, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeUpdates(ILocationListener listener, PendingIntent intent, String packageName) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_requestLocationUpdates);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_removeUpdates, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_requestLocationUpdates);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (geofence != null) {
                        _data.writeInt(Stub.TRANSACTION_requestLocationUpdates);
                        geofence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_requestLocationUpdates);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_requestGeofence, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_requestLocationUpdates);
                        fence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_requestLocationUpdates);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_removeGeofence, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Location getLastLocation(LocationRequest request, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Location location;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(Stub.TRANSACTION_requestLocationUpdates);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_getLastLocation, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        location = (Location) Location.CREATOR.createFromParcel(_reply);
                    } else {
                        location = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return location;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerGnssStatusCallback(IGnssStatusListener callback, String packageName) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_registerGnssStatusCallback, _data, _reply, 0);
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

            public void unregisterGnssStatusCallback(IGnssStatusListener callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterGnssStatusCallback, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_geocoderIsPresent, _data, _reply, 0);
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

            public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeDouble(latitude);
                    _data.writeDouble(longitude);
                    _data.writeInt(maxResults);
                    if (params != null) {
                        _data.writeInt(Stub.TRANSACTION_requestLocationUpdates);
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
                        _data.writeInt(Stub.TRANSACTION_requestLocationUpdates);
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

            public boolean sendNiResponse(int notifId, int userResponse) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(notifId);
                    _data.writeInt(userResponse);
                    this.mRemote.transact(Stub.TRANSACTION_sendNiResponse, _data, _reply, 0);
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

            public boolean addGnssMeasurementsListener(IGnssMeasurementsListener listener, String packageName) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_addGnssMeasurementsListener, _data, _reply, 0);
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

            public void removeGnssMeasurementsListener(IGnssMeasurementsListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_removeGnssMeasurementsListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean addGnssNavigationMessageListener(IGnssNavigationMessageListener listener, String packageName) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_addGnssNavigationMessageListener, _data, _reply, 0);
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

            public void removeGnssNavigationMessageListener(IGnssNavigationMessageListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_removeGnssNavigationMessageListener, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getGnssYearOfHardware, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
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
                    this.mRemote.transact(Stub.TRANSACTION_getAllProviders, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getProviders(Criteria criteria, boolean enabledOnly) throws RemoteException {
                int i = Stub.TRANSACTION_requestLocationUpdates;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (criteria != null) {
                        _data.writeInt(Stub.TRANSACTION_requestLocationUpdates);
                        criteria.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!enabledOnly) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_getProviders, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getBestProvider(Criteria criteria, boolean enabledOnly) throws RemoteException {
                int i = Stub.TRANSACTION_requestLocationUpdates;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (criteria != null) {
                        _data.writeInt(Stub.TRANSACTION_requestLocationUpdates);
                        criteria.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!enabledOnly) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_getBestProvider, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    if (criteria != null) {
                        _data.writeInt(Stub.TRANSACTION_requestLocationUpdates);
                        criteria.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_providerMeetsCriteria, _data, _reply, 0);
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

            public ProviderProperties getProviderProperties(String provider) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ProviderProperties providerProperties;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    this.mRemote.transact(Stub.TRANSACTION_getProviderProperties, _data, _reply, 0);
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

            public String getNetworkProviderPackage() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getNetworkProviderPackage, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isProviderEnabled(String provider) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    this.mRemote.transact(Stub.TRANSACTION_isProviderEnabled, _data, _reply, 0);
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

            public void addTestProvider(String name, ProviderProperties properties, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    if (properties != null) {
                        _data.writeInt(Stub.TRANSACTION_requestLocationUpdates);
                        properties.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(opPackageName);
                    this.mRemote.transact(Stub.TRANSACTION_addTestProvider, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_removeTestProvider, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_requestLocationUpdates);
                        loc.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(opPackageName);
                    this.mRemote.transact(Stub.TRANSACTION_setTestProviderLocation, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_clearTestProviderLocation, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setTestProviderEnabled(String provider, boolean enabled, String opPackageName) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    if (enabled) {
                        i = Stub.TRANSACTION_requestLocationUpdates;
                    }
                    _data.writeInt(i);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(Stub.TRANSACTION_setTestProviderEnabled, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_clearTestProviderEnabled, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_requestLocationUpdates);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(updateTime);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(Stub.TRANSACTION_setTestProviderStatus, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_clearTestProviderStatus, _data, _reply, 0);
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
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_requestLocationUpdates);
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

            public void reportLocation(Location location, boolean passive) throws RemoteException {
                int i = Stub.TRANSACTION_requestLocationUpdates;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (location != null) {
                        _data.writeInt(Stub.TRANSACTION_requestLocationUpdates);
                        location.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!passive) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_reportLocation, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void locationCallbackFinished(ILocationListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_locationCallbackFinished, _data, _reply, 0);
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            LocationRequest locationRequest;
            PendingIntent pendingIntent;
            PendingIntent pendingIntent2;
            boolean _result;
            double _arg1;
            String _result2;
            String _arg0;
            List<String> _result3;
            Criteria criteria;
            Bundle bundle;
            switch (code) {
                case TRANSACTION_requestLocationUpdates /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        locationRequest = (LocationRequest) LocationRequest.CREATOR.createFromParcel(data);
                    } else {
                        locationRequest = null;
                    }
                    ILocationListener _arg12 = android.location.ILocationListener.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent = null;
                    }
                    requestLocationUpdates(locationRequest, _arg12, pendingIntent, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeUpdates /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    ILocationListener _arg02 = android.location.ILocationListener.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        pendingIntent2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent2 = null;
                    }
                    removeUpdates(_arg02, pendingIntent2, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_requestGeofence /*3*/:
                    Geofence geofence;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        locationRequest = (LocationRequest) LocationRequest.CREATOR.createFromParcel(data);
                    } else {
                        locationRequest = null;
                    }
                    if (data.readInt() != 0) {
                        geofence = (Geofence) Geofence.CREATOR.createFromParcel(data);
                    } else {
                        geofence = null;
                    }
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent = null;
                    }
                    requestGeofence(locationRequest, geofence, pendingIntent, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeGeofence /*4*/:
                    Geofence geofence2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        geofence2 = (Geofence) Geofence.CREATOR.createFromParcel(data);
                    } else {
                        geofence2 = null;
                    }
                    if (data.readInt() != 0) {
                        pendingIntent2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent2 = null;
                    }
                    removeGeofence(geofence2, pendingIntent2, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getLastLocation /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        locationRequest = (LocationRequest) LocationRequest.CREATOR.createFromParcel(data);
                    } else {
                        locationRequest = null;
                    }
                    Location _result4 = getLastLocation(locationRequest, data.readString());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_requestLocationUpdates);
                        _result4.writeToParcel(reply, TRANSACTION_requestLocationUpdates);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_registerGnssStatusCallback /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = registerGnssStatusCallback(android.location.IGnssStatusListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_requestLocationUpdates : 0);
                    return true;
                case TRANSACTION_unregisterGnssStatusCallback /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterGnssStatusCallback(android.location.IGnssStatusListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_geocoderIsPresent /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = geocoderIsPresent();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_requestLocationUpdates : 0);
                    return true;
                case TRANSACTION_getFromLocation /*9*/:
                    GeocoderParams geocoderParams;
                    data.enforceInterface(DESCRIPTOR);
                    double _arg03 = data.readDouble();
                    _arg1 = data.readDouble();
                    int _arg2 = data.readInt();
                    if (data.readInt() != 0) {
                        geocoderParams = (GeocoderParams) GeocoderParams.CREATOR.createFromParcel(data);
                    } else {
                        geocoderParams = null;
                    }
                    List<Address> _arg4 = new ArrayList();
                    _result2 = getFromLocation(_arg03, _arg1, _arg2, geocoderParams, _arg4);
                    reply.writeNoException();
                    reply.writeString(_result2);
                    reply.writeTypedList(_arg4);
                    return true;
                case TRANSACTION_getFromLocationName /*10*/:
                    GeocoderParams geocoderParams2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
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
                    _result2 = getFromLocationName(_arg0, _arg1, _arg22, _arg3, _arg42, _arg5, geocoderParams2, _arg7);
                    reply.writeNoException();
                    reply.writeString(_result2);
                    reply.writeTypedList(_arg7);
                    return true;
                case TRANSACTION_sendNiResponse /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = sendNiResponse(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_requestLocationUpdates : 0);
                    return true;
                case TRANSACTION_addGnssMeasurementsListener /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = addGnssMeasurementsListener(android.location.IGnssMeasurementsListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_requestLocationUpdates : 0);
                    return true;
                case TRANSACTION_removeGnssMeasurementsListener /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeGnssMeasurementsListener(android.location.IGnssMeasurementsListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addGnssNavigationMessageListener /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = addGnssNavigationMessageListener(android.location.IGnssNavigationMessageListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_requestLocationUpdates : 0);
                    return true;
                case TRANSACTION_removeGnssNavigationMessageListener /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeGnssNavigationMessageListener(android.location.IGnssNavigationMessageListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getGnssYearOfHardware /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result5 = getGnssYearOfHardware();
                    reply.writeNoException();
                    reply.writeInt(_result5);
                    return true;
                case TRANSACTION_getAllProviders /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getAllProviders();
                    reply.writeNoException();
                    reply.writeStringList(_result3);
                    return true;
                case TRANSACTION_getProviders /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        criteria = (Criteria) Criteria.CREATOR.createFromParcel(data);
                    } else {
                        criteria = null;
                    }
                    _result3 = getProviders(criteria, data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeStringList(_result3);
                    return true;
                case TRANSACTION_getBestProvider /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        criteria = (Criteria) Criteria.CREATOR.createFromParcel(data);
                    } else {
                        criteria = null;
                    }
                    _result2 = getBestProvider(criteria, data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case TRANSACTION_providerMeetsCriteria /*20*/:
                    Criteria criteria2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        criteria2 = (Criteria) Criteria.CREATOR.createFromParcel(data);
                    } else {
                        criteria2 = null;
                    }
                    _result = providerMeetsCriteria(_arg0, criteria2);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_requestLocationUpdates : 0);
                    return true;
                case TRANSACTION_getProviderProperties /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    ProviderProperties _result6 = getProviderProperties(data.readString());
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(TRANSACTION_requestLocationUpdates);
                        _result6.writeToParcel(reply, TRANSACTION_requestLocationUpdates);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getNetworkProviderPackage /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getNetworkProviderPackage();
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case TRANSACTION_isProviderEnabled /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isProviderEnabled(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_requestLocationUpdates : 0);
                    return true;
                case TRANSACTION_addTestProvider /*24*/:
                    ProviderProperties providerProperties;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        providerProperties = (ProviderProperties) ProviderProperties.CREATOR.createFromParcel(data);
                    } else {
                        providerProperties = null;
                    }
                    addTestProvider(_arg0, providerProperties, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeTestProvider /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeTestProvider(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setTestProviderLocation /*26*/:
                    Location location;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        location = (Location) Location.CREATOR.createFromParcel(data);
                    } else {
                        location = null;
                    }
                    setTestProviderLocation(_arg0, location, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearTestProviderLocation /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearTestProviderLocation(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setTestProviderEnabled /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    setTestProviderEnabled(data.readString(), data.readInt() != 0, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearTestProviderEnabled /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearTestProviderEnabled(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setTestProviderStatus /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    int _arg13 = data.readInt();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    setTestProviderStatus(_arg0, _arg13, bundle, data.readLong(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearTestProviderStatus /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearTestProviderStatus(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sendExtraCommand /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    String _arg14 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = sendExtraCommand(_arg0, _arg14, bundle);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_requestLocationUpdates : 0);
                    if (bundle != null) {
                        reply.writeInt(TRANSACTION_requestLocationUpdates);
                        bundle.writeToParcel(reply, TRANSACTION_requestLocationUpdates);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_reportLocation /*33*/:
                    Location location2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        location2 = (Location) Location.CREATOR.createFromParcel(data);
                    } else {
                        location2 = null;
                    }
                    reportLocation(location2, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_locationCallbackFinished /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    locationCallbackFinished(android.location.ILocationListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean addGnssMeasurementsListener(IGnssMeasurementsListener iGnssMeasurementsListener, String str) throws RemoteException;

    boolean addGnssNavigationMessageListener(IGnssNavigationMessageListener iGnssNavigationMessageListener, String str) throws RemoteException;

    void addTestProvider(String str, ProviderProperties providerProperties, String str2) throws RemoteException;

    void clearTestProviderEnabled(String str, String str2) throws RemoteException;

    void clearTestProviderLocation(String str, String str2) throws RemoteException;

    void clearTestProviderStatus(String str, String str2) throws RemoteException;

    boolean geocoderIsPresent() throws RemoteException;

    List<String> getAllProviders() throws RemoteException;

    String getBestProvider(Criteria criteria, boolean z) throws RemoteException;

    String getFromLocation(double d, double d2, int i, GeocoderParams geocoderParams, List<Address> list) throws RemoteException;

    String getFromLocationName(String str, double d, double d2, double d3, double d4, int i, GeocoderParams geocoderParams, List<Address> list) throws RemoteException;

    int getGnssYearOfHardware() throws RemoteException;

    Location getLastLocation(LocationRequest locationRequest, String str) throws RemoteException;

    String getNetworkProviderPackage() throws RemoteException;

    ProviderProperties getProviderProperties(String str) throws RemoteException;

    List<String> getProviders(Criteria criteria, boolean z) throws RemoteException;

    boolean isProviderEnabled(String str) throws RemoteException;

    void locationCallbackFinished(ILocationListener iLocationListener) throws RemoteException;

    boolean providerMeetsCriteria(String str, Criteria criteria) throws RemoteException;

    boolean registerGnssStatusCallback(IGnssStatusListener iGnssStatusListener, String str) throws RemoteException;

    void removeGeofence(Geofence geofence, PendingIntent pendingIntent, String str) throws RemoteException;

    void removeGnssMeasurementsListener(IGnssMeasurementsListener iGnssMeasurementsListener) throws RemoteException;

    void removeGnssNavigationMessageListener(IGnssNavigationMessageListener iGnssNavigationMessageListener) throws RemoteException;

    void removeTestProvider(String str, String str2) throws RemoteException;

    void removeUpdates(ILocationListener iLocationListener, PendingIntent pendingIntent, String str) throws RemoteException;

    void reportLocation(Location location, boolean z) throws RemoteException;

    void requestGeofence(LocationRequest locationRequest, Geofence geofence, PendingIntent pendingIntent, String str) throws RemoteException;

    void requestLocationUpdates(LocationRequest locationRequest, ILocationListener iLocationListener, PendingIntent pendingIntent, String str) throws RemoteException;

    boolean sendExtraCommand(String str, String str2, Bundle bundle) throws RemoteException;

    boolean sendNiResponse(int i, int i2) throws RemoteException;

    void setTestProviderEnabled(String str, boolean z, String str2) throws RemoteException;

    void setTestProviderLocation(String str, Location location, String str2) throws RemoteException;

    void setTestProviderStatus(String str, int i, Bundle bundle, long j, String str2) throws RemoteException;

    void unregisterGnssStatusCallback(IGnssStatusListener iGnssStatusListener) throws RemoteException;
}
