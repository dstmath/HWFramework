package android.location;

import android.annotation.UnsupportedAppUsage;
import android.app.PendingIntent;
import android.location.IBatchedLocationCallback;
import android.location.IGnssMeasurementsListener;
import android.location.IGnssNavigationMessageListener;
import android.location.IGnssStatusListener;
import android.location.ILocationListener;
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
    boolean addGnssBatchingCallback(IBatchedLocationCallback iBatchedLocationCallback, String str) throws RemoteException;

    boolean addGnssMeasurementsListener(IGnssMeasurementsListener iGnssMeasurementsListener, String str) throws RemoteException;

    boolean addGnssNavigationMessageListener(IGnssNavigationMessageListener iGnssNavigationMessageListener, String str) throws RemoteException;

    void addTestProvider(String str, ProviderProperties providerProperties, String str2) throws RemoteException;

    void flushGnssBatch(String str) throws RemoteException;

    boolean geocoderIsPresent() throws RemoteException;

    @UnsupportedAppUsage
    List<String> getAllProviders() throws RemoteException;

    String[] getBackgroundThrottlingWhitelist() throws RemoteException;

    String getBestProvider(Criteria criteria, boolean z) throws RemoteException;

    String getExtraLocationControllerPackage() throws RemoteException;

    String getFromLocation(double d, double d2, int i, GeocoderParams geocoderParams, List<Address> list) throws RemoteException;

    String getFromLocationName(String str, double d, double d2, double d3, double d4, int i, GeocoderParams geocoderParams, List<Address> list) throws RemoteException;

    int getGnssBatchSize(String str) throws RemoteException;

    long getGnssCapabilities(String str) throws RemoteException;

    String getGnssHardwareModelName() throws RemoteException;

    LocationTime getGnssTimeMillis() throws RemoteException;

    int getGnssYearOfHardware() throws RemoteException;

    String[] getIgnoreSettingsWhitelist() throws RemoteException;

    Location getLastLocation(LocationRequest locationRequest, String str) throws RemoteException;

    ProviderProperties getProviderProperties(String str) throws RemoteException;

    List<String> getProviders(Criteria criteria, boolean z) throws RemoteException;

    List<LocationRequest> getTestProviderCurrentRequests(String str, String str2) throws RemoteException;

    void injectGnssMeasurementCorrections(GnssMeasurementCorrections gnssMeasurementCorrections, String str) throws RemoteException;

    boolean injectLocation(Location location) throws RemoteException;

    boolean isExtraLocationControllerPackageEnabled() throws RemoteException;

    boolean isLocationEnabledForUser(int i) throws RemoteException;

    boolean isProviderEnabledForUser(String str, int i) throws RemoteException;

    boolean isProviderPackage(String str) throws RemoteException;

    void locationCallbackFinished(ILocationListener iLocationListener) throws RemoteException;

    boolean registerGnssStatusCallback(IGnssStatusListener iGnssStatusListener, String str) throws RemoteException;

    void removeGeofence(Geofence geofence, PendingIntent pendingIntent, String str) throws RemoteException;

    void removeGnssBatchingCallback() throws RemoteException;

    void removeGnssMeasurementsListener(IGnssMeasurementsListener iGnssMeasurementsListener) throws RemoteException;

    void removeGnssNavigationMessageListener(IGnssNavigationMessageListener iGnssNavigationMessageListener) throws RemoteException;

    void removeTestProvider(String str, String str2) throws RemoteException;

    void removeUpdates(ILocationListener iLocationListener, PendingIntent pendingIntent, String str) throws RemoteException;

    void requestGeofence(LocationRequest locationRequest, Geofence geofence, PendingIntent pendingIntent, String str) throws RemoteException;

    void requestLocationUpdates(LocationRequest locationRequest, ILocationListener iLocationListener, PendingIntent pendingIntent, String str) throws RemoteException;

    boolean sendExtraCommand(String str, String str2, Bundle bundle) throws RemoteException;

    boolean sendNiResponse(int i, int i2) throws RemoteException;

    void setExtraLocationControllerPackage(String str) throws RemoteException;

    void setExtraLocationControllerPackageEnabled(boolean z) throws RemoteException;

    void setTestProviderEnabled(String str, boolean z, String str2) throws RemoteException;

    void setTestProviderLocation(String str, Location location, String str2) throws RemoteException;

    void setTestProviderStatus(String str, int i, Bundle bundle, long j, String str2) throws RemoteException;

    boolean startGnssBatch(long j, boolean z, String str) throws RemoteException;

    boolean stopGnssBatch() throws RemoteException;

    void unregisterGnssStatusCallback(IGnssStatusListener iGnssStatusListener) throws RemoteException;

    public static class Default implements ILocationManager {
        @Override // android.location.ILocationManager
        public void requestLocationUpdates(LocationRequest request, ILocationListener listener, PendingIntent intent, String packageName) throws RemoteException {
        }

        @Override // android.location.ILocationManager
        public void removeUpdates(ILocationListener listener, PendingIntent intent, String packageName) throws RemoteException {
        }

        @Override // android.location.ILocationManager
        public void requestGeofence(LocationRequest request, Geofence geofence, PendingIntent intent, String packageName) throws RemoteException {
        }

        @Override // android.location.ILocationManager
        public void removeGeofence(Geofence fence, PendingIntent intent, String packageName) throws RemoteException {
        }

        @Override // android.location.ILocationManager
        public Location getLastLocation(LocationRequest request, String packageName) throws RemoteException {
            return null;
        }

        @Override // android.location.ILocationManager
        public boolean registerGnssStatusCallback(IGnssStatusListener callback, String packageName) throws RemoteException {
            return false;
        }

        @Override // android.location.ILocationManager
        public void unregisterGnssStatusCallback(IGnssStatusListener callback) throws RemoteException {
        }

        @Override // android.location.ILocationManager
        public boolean geocoderIsPresent() throws RemoteException {
            return false;
        }

        @Override // android.location.ILocationManager
        public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> list) throws RemoteException {
            return null;
        }

        @Override // android.location.ILocationManager
        public String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> list) throws RemoteException {
            return null;
        }

        @Override // android.location.ILocationManager
        public boolean sendNiResponse(int notifId, int userResponse) throws RemoteException {
            return false;
        }

        @Override // android.location.ILocationManager
        public boolean addGnssMeasurementsListener(IGnssMeasurementsListener listener, String packageName) throws RemoteException {
            return false;
        }

        @Override // android.location.ILocationManager
        public void injectGnssMeasurementCorrections(GnssMeasurementCorrections corrections, String packageName) throws RemoteException {
        }

        @Override // android.location.ILocationManager
        public long getGnssCapabilities(String packageName) throws RemoteException {
            return 0;
        }

        @Override // android.location.ILocationManager
        public void removeGnssMeasurementsListener(IGnssMeasurementsListener listener) throws RemoteException {
        }

        @Override // android.location.ILocationManager
        public boolean addGnssNavigationMessageListener(IGnssNavigationMessageListener listener, String packageName) throws RemoteException {
            return false;
        }

        @Override // android.location.ILocationManager
        public void removeGnssNavigationMessageListener(IGnssNavigationMessageListener listener) throws RemoteException {
        }

        @Override // android.location.ILocationManager
        public int getGnssYearOfHardware() throws RemoteException {
            return 0;
        }

        @Override // android.location.ILocationManager
        public String getGnssHardwareModelName() throws RemoteException {
            return null;
        }

        @Override // android.location.ILocationManager
        public int getGnssBatchSize(String packageName) throws RemoteException {
            return 0;
        }

        @Override // android.location.ILocationManager
        public boolean addGnssBatchingCallback(IBatchedLocationCallback callback, String packageName) throws RemoteException {
            return false;
        }

        @Override // android.location.ILocationManager
        public void removeGnssBatchingCallback() throws RemoteException {
        }

        @Override // android.location.ILocationManager
        public boolean startGnssBatch(long periodNanos, boolean wakeOnFifoFull, String packageName) throws RemoteException {
            return false;
        }

        @Override // android.location.ILocationManager
        public void flushGnssBatch(String packageName) throws RemoteException {
        }

        @Override // android.location.ILocationManager
        public boolean stopGnssBatch() throws RemoteException {
            return false;
        }

        @Override // android.location.ILocationManager
        public boolean injectLocation(Location location) throws RemoteException {
            return false;
        }

        @Override // android.location.ILocationManager
        public List<String> getAllProviders() throws RemoteException {
            return null;
        }

        @Override // android.location.ILocationManager
        public List<String> getProviders(Criteria criteria, boolean enabledOnly) throws RemoteException {
            return null;
        }

        @Override // android.location.ILocationManager
        public String getBestProvider(Criteria criteria, boolean enabledOnly) throws RemoteException {
            return null;
        }

        @Override // android.location.ILocationManager
        public ProviderProperties getProviderProperties(String provider) throws RemoteException {
            return null;
        }

        @Override // android.location.ILocationManager
        public boolean isProviderPackage(String packageName) throws RemoteException {
            return false;
        }

        @Override // android.location.ILocationManager
        public void setExtraLocationControllerPackage(String packageName) throws RemoteException {
        }

        @Override // android.location.ILocationManager
        public String getExtraLocationControllerPackage() throws RemoteException {
            return null;
        }

        @Override // android.location.ILocationManager
        public void setExtraLocationControllerPackageEnabled(boolean enabled) throws RemoteException {
        }

        @Override // android.location.ILocationManager
        public boolean isExtraLocationControllerPackageEnabled() throws RemoteException {
            return false;
        }

        @Override // android.location.ILocationManager
        public boolean isProviderEnabledForUser(String provider, int userId) throws RemoteException {
            return false;
        }

        @Override // android.location.ILocationManager
        public boolean isLocationEnabledForUser(int userId) throws RemoteException {
            return false;
        }

        @Override // android.location.ILocationManager
        public void addTestProvider(String name, ProviderProperties properties, String opPackageName) throws RemoteException {
        }

        @Override // android.location.ILocationManager
        public void removeTestProvider(String provider, String opPackageName) throws RemoteException {
        }

        @Override // android.location.ILocationManager
        public void setTestProviderLocation(String provider, Location loc, String opPackageName) throws RemoteException {
        }

        @Override // android.location.ILocationManager
        public void setTestProviderEnabled(String provider, boolean enabled, String opPackageName) throws RemoteException {
        }

        @Override // android.location.ILocationManager
        public List<LocationRequest> getTestProviderCurrentRequests(String provider, String opPackageName) throws RemoteException {
            return null;
        }

        @Override // android.location.ILocationManager
        public LocationTime getGnssTimeMillis() throws RemoteException {
            return null;
        }

        @Override // android.location.ILocationManager
        public void setTestProviderStatus(String provider, int status, Bundle extras, long updateTime, String opPackageName) throws RemoteException {
        }

        @Override // android.location.ILocationManager
        public boolean sendExtraCommand(String provider, String command, Bundle extras) throws RemoteException {
            return false;
        }

        @Override // android.location.ILocationManager
        public void locationCallbackFinished(ILocationListener listener) throws RemoteException {
        }

        @Override // android.location.ILocationManager
        public String[] getBackgroundThrottlingWhitelist() throws RemoteException {
            return null;
        }

        @Override // android.location.ILocationManager
        public String[] getIgnoreSettingsWhitelist() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ILocationManager {
        private static final String DESCRIPTOR = "android.location.ILocationManager";
        static final int TRANSACTION_addGnssBatchingCallback = 21;
        static final int TRANSACTION_addGnssMeasurementsListener = 12;
        static final int TRANSACTION_addGnssNavigationMessageListener = 16;
        static final int TRANSACTION_addTestProvider = 38;
        static final int TRANSACTION_flushGnssBatch = 24;
        static final int TRANSACTION_geocoderIsPresent = 8;
        static final int TRANSACTION_getAllProviders = 27;
        static final int TRANSACTION_getBackgroundThrottlingWhitelist = 47;
        static final int TRANSACTION_getBestProvider = 29;
        static final int TRANSACTION_getExtraLocationControllerPackage = 33;
        static final int TRANSACTION_getFromLocation = 9;
        static final int TRANSACTION_getFromLocationName = 10;
        static final int TRANSACTION_getGnssBatchSize = 20;
        static final int TRANSACTION_getGnssCapabilities = 14;
        static final int TRANSACTION_getGnssHardwareModelName = 19;
        static final int TRANSACTION_getGnssTimeMillis = 43;
        static final int TRANSACTION_getGnssYearOfHardware = 18;
        static final int TRANSACTION_getIgnoreSettingsWhitelist = 48;
        static final int TRANSACTION_getLastLocation = 5;
        static final int TRANSACTION_getProviderProperties = 30;
        static final int TRANSACTION_getProviders = 28;
        static final int TRANSACTION_getTestProviderCurrentRequests = 42;
        static final int TRANSACTION_injectGnssMeasurementCorrections = 13;
        static final int TRANSACTION_injectLocation = 26;
        static final int TRANSACTION_isExtraLocationControllerPackageEnabled = 35;
        static final int TRANSACTION_isLocationEnabledForUser = 37;
        static final int TRANSACTION_isProviderEnabledForUser = 36;
        static final int TRANSACTION_isProviderPackage = 31;
        static final int TRANSACTION_locationCallbackFinished = 46;
        static final int TRANSACTION_registerGnssStatusCallback = 6;
        static final int TRANSACTION_removeGeofence = 4;
        static final int TRANSACTION_removeGnssBatchingCallback = 22;
        static final int TRANSACTION_removeGnssMeasurementsListener = 15;
        static final int TRANSACTION_removeGnssNavigationMessageListener = 17;
        static final int TRANSACTION_removeTestProvider = 39;
        static final int TRANSACTION_removeUpdates = 2;
        static final int TRANSACTION_requestGeofence = 3;
        static final int TRANSACTION_requestLocationUpdates = 1;
        static final int TRANSACTION_sendExtraCommand = 45;
        static final int TRANSACTION_sendNiResponse = 11;
        static final int TRANSACTION_setExtraLocationControllerPackage = 32;
        static final int TRANSACTION_setExtraLocationControllerPackageEnabled = 34;
        static final int TRANSACTION_setTestProviderEnabled = 41;
        static final int TRANSACTION_setTestProviderLocation = 40;
        static final int TRANSACTION_setTestProviderStatus = 44;
        static final int TRANSACTION_startGnssBatch = 23;
        static final int TRANSACTION_stopGnssBatch = 25;
        static final int TRANSACTION_unregisterGnssStatusCallback = 7;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "requestLocationUpdates";
                case 2:
                    return "removeUpdates";
                case 3:
                    return "requestGeofence";
                case 4:
                    return "removeGeofence";
                case 5:
                    return "getLastLocation";
                case 6:
                    return "registerGnssStatusCallback";
                case 7:
                    return "unregisterGnssStatusCallback";
                case 8:
                    return "geocoderIsPresent";
                case 9:
                    return "getFromLocation";
                case 10:
                    return "getFromLocationName";
                case 11:
                    return "sendNiResponse";
                case 12:
                    return "addGnssMeasurementsListener";
                case 13:
                    return "injectGnssMeasurementCorrections";
                case 14:
                    return "getGnssCapabilities";
                case 15:
                    return "removeGnssMeasurementsListener";
                case 16:
                    return "addGnssNavigationMessageListener";
                case 17:
                    return "removeGnssNavigationMessageListener";
                case 18:
                    return "getGnssYearOfHardware";
                case 19:
                    return "getGnssHardwareModelName";
                case 20:
                    return "getGnssBatchSize";
                case 21:
                    return "addGnssBatchingCallback";
                case 22:
                    return "removeGnssBatchingCallback";
                case 23:
                    return "startGnssBatch";
                case 24:
                    return "flushGnssBatch";
                case 25:
                    return "stopGnssBatch";
                case 26:
                    return "injectLocation";
                case 27:
                    return "getAllProviders";
                case 28:
                    return "getProviders";
                case 29:
                    return "getBestProvider";
                case 30:
                    return "getProviderProperties";
                case 31:
                    return "isProviderPackage";
                case 32:
                    return "setExtraLocationControllerPackage";
                case 33:
                    return "getExtraLocationControllerPackage";
                case 34:
                    return "setExtraLocationControllerPackageEnabled";
                case 35:
                    return "isExtraLocationControllerPackageEnabled";
                case 36:
                    return "isProviderEnabledForUser";
                case 37:
                    return "isLocationEnabledForUser";
                case 38:
                    return "addTestProvider";
                case 39:
                    return "removeTestProvider";
                case 40:
                    return "setTestProviderLocation";
                case 41:
                    return "setTestProviderEnabled";
                case 42:
                    return "getTestProviderCurrentRequests";
                case 43:
                    return "getGnssTimeMillis";
                case 44:
                    return "setTestProviderStatus";
                case 45:
                    return "sendExtraCommand";
                case 46:
                    return "locationCallbackFinished";
                case 47:
                    return "getBackgroundThrottlingWhitelist";
                case 48:
                    return "getIgnoreSettingsWhitelist";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            LocationRequest _arg0;
            PendingIntent _arg2;
            PendingIntent _arg1;
            LocationRequest _arg02;
            Geofence _arg12;
            PendingIntent _arg22;
            Geofence _arg03;
            PendingIntent _arg13;
            LocationRequest _arg04;
            GeocoderParams _arg3;
            GeocoderParams _arg6;
            GnssMeasurementCorrections _arg05;
            Location _arg06;
            Criteria _arg07;
            Criteria _arg08;
            ProviderProperties _arg14;
            Location _arg15;
            Bundle _arg23;
            Bundle _arg24;
            if (code != 1598968902) {
                boolean _arg16 = false;
                boolean _arg17 = false;
                boolean _arg09 = false;
                boolean _arg18 = false;
                boolean _arg19 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = LocationRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        ILocationListener _arg110 = ILocationListener.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg2 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        requestLocationUpdates(_arg0, _arg110, _arg2, data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        ILocationListener _arg010 = ILocationListener.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        removeUpdates(_arg010, _arg1, data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = LocationRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = Geofence.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        requestGeofence(_arg02, _arg12, _arg22, data.readString());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = Geofence.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg13 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        removeGeofence(_arg03, _arg13, data.readString());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = LocationRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        Location _result = getLastLocation(_arg04, data.readString());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                            return true;
                        }
                        reply.writeInt(0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerGnssStatusCallback = registerGnssStatusCallback(IGnssStatusListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        reply.writeInt(registerGnssStatusCallback ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterGnssStatusCallback(IGnssStatusListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean geocoderIsPresent = geocoderIsPresent();
                        reply.writeNoException();
                        reply.writeInt(geocoderIsPresent ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        double _arg011 = data.readDouble();
                        double _arg111 = data.readDouble();
                        int _arg25 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = GeocoderParams.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        ArrayList arrayList = new ArrayList();
                        String _result2 = getFromLocation(_arg011, _arg111, _arg25, _arg3, arrayList);
                        reply.writeNoException();
                        reply.writeString(_result2);
                        reply.writeTypedList(arrayList);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg012 = data.readString();
                        double _arg112 = data.readDouble();
                        double _arg26 = data.readDouble();
                        double _arg32 = data.readDouble();
                        double _arg4 = data.readDouble();
                        int _arg5 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg6 = GeocoderParams.CREATOR.createFromParcel(data);
                        } else {
                            _arg6 = null;
                        }
                        ArrayList arrayList2 = new ArrayList();
                        String _result3 = getFromLocationName(_arg012, _arg112, _arg26, _arg32, _arg4, _arg5, _arg6, arrayList2);
                        reply.writeNoException();
                        reply.writeString(_result3);
                        reply.writeTypedList(arrayList2);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean sendNiResponse = sendNiResponse(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(sendNiResponse ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean addGnssMeasurementsListener = addGnssMeasurementsListener(IGnssMeasurementsListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        reply.writeInt(addGnssMeasurementsListener ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = GnssMeasurementCorrections.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        injectGnssMeasurementCorrections(_arg05, data.readString());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        long _result4 = getGnssCapabilities(data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result4);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        removeGnssMeasurementsListener(IGnssMeasurementsListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean addGnssNavigationMessageListener = addGnssNavigationMessageListener(IGnssNavigationMessageListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        reply.writeInt(addGnssNavigationMessageListener ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        removeGnssNavigationMessageListener(IGnssNavigationMessageListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getGnssYearOfHardware();
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        String _result6 = getGnssHardwareModelName();
                        reply.writeNoException();
                        reply.writeString(_result6);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = getGnssBatchSize(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        boolean addGnssBatchingCallback = addGnssBatchingCallback(IBatchedLocationCallback.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        reply.writeInt(addGnssBatchingCallback ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        removeGnssBatchingCallback();
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        long _arg013 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg16 = true;
                        }
                        boolean startGnssBatch = startGnssBatch(_arg013, _arg16, data.readString());
                        reply.writeNoException();
                        reply.writeInt(startGnssBatch ? 1 : 0);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        flushGnssBatch(data.readString());
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        boolean stopGnssBatch = stopGnssBatch();
                        reply.writeNoException();
                        reply.writeInt(stopGnssBatch ? 1 : 0);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = Location.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        boolean injectLocation = injectLocation(_arg06);
                        reply.writeNoException();
                        reply.writeInt(injectLocation ? 1 : 0);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result8 = getAllProviders();
                        reply.writeNoException();
                        reply.writeStringList(_result8);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = Criteria.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg19 = true;
                        }
                        List<String> _result9 = getProviders(_arg07, _arg19);
                        reply.writeNoException();
                        reply.writeStringList(_result9);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = Criteria.CREATOR.createFromParcel(data);
                        } else {
                            _arg08 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg18 = true;
                        }
                        String _result10 = getBestProvider(_arg08, _arg18);
                        reply.writeNoException();
                        reply.writeString(_result10);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        ProviderProperties _result11 = getProviderProperties(data.readString());
                        reply.writeNoException();
                        if (_result11 != null) {
                            reply.writeInt(1);
                            _result11.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isProviderPackage = isProviderPackage(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isProviderPackage ? 1 : 0);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        setExtraLocationControllerPackage(data.readString());
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        String _result12 = getExtraLocationControllerPackage();
                        reply.writeNoException();
                        reply.writeString(_result12);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = true;
                        }
                        setExtraLocationControllerPackageEnabled(_arg09);
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isExtraLocationControllerPackageEnabled = isExtraLocationControllerPackageEnabled();
                        reply.writeNoException();
                        reply.writeInt(isExtraLocationControllerPackageEnabled ? 1 : 0);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isProviderEnabledForUser = isProviderEnabledForUser(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isProviderEnabledForUser ? 1 : 0);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isLocationEnabledForUser = isLocationEnabledForUser(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isLocationEnabledForUser ? 1 : 0);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg014 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = ProviderProperties.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        addTestProvider(_arg014, _arg14, data.readString());
                        reply.writeNoException();
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        removeTestProvider(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg015 = data.readString();
                        if (data.readInt() != 0) {
                            _arg15 = Location.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        setTestProviderLocation(_arg015, _arg15, data.readString());
                        reply.writeNoException();
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg016 = data.readString();
                        if (data.readInt() != 0) {
                            _arg17 = true;
                        }
                        setTestProviderEnabled(_arg016, _arg17, data.readString());
                        reply.writeNoException();
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        List<LocationRequest> _result13 = getTestProviderCurrentRequests(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result13);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        LocationTime _result14 = getGnssTimeMillis();
                        reply.writeNoException();
                        if (_result14 != null) {
                            reply.writeInt(1);
                            _result14.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg017 = data.readString();
                        int _arg113 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        setTestProviderStatus(_arg017, _arg113, _arg23, data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg018 = data.readString();
                        String _arg114 = data.readString();
                        if (data.readInt() != 0) {
                            _arg24 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        boolean sendExtraCommand = sendExtraCommand(_arg018, _arg114, _arg24);
                        reply.writeNoException();
                        reply.writeInt(sendExtraCommand ? 1 : 0);
                        if (_arg24 != null) {
                            reply.writeInt(1);
                            _arg24.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        locationCallbackFinished(ILocationListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result15 = getBackgroundThrottlingWhitelist();
                        reply.writeNoException();
                        reply.writeStringArray(_result15);
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result16 = getIgnoreSettingsWhitelist();
                        reply.writeNoException();
                        reply.writeStringArray(_result16);
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
        public static class Proxy implements ILocationManager {
            public static ILocationManager sDefaultImpl;
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

            @Override // android.location.ILocationManager
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
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestLocationUpdates(request, listener, intent, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
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
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeUpdates(listener, intent, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
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
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestGeofence(request, geofence, intent, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
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
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeGeofence(fence, intent, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
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
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLastLocation(request, packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Location.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public boolean registerGnssStatusCallback(IGnssStatusListener callback, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerGnssStatusCallback(callback, packageName);
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

            @Override // android.location.ILocationManager
            public void unregisterGnssStatusCallback(IGnssStatusListener callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterGnssStatusCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public boolean geocoderIsPresent() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().geocoderIsPresent();
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

            @Override // android.location.ILocationManager
            public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) throws RemoteException {
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
                            if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                String _result = _reply.readString();
                                try {
                                    _reply.readTypedList(addrs, Address.CREATOR);
                                    _reply.recycle();
                                    _data.recycle();
                                    return _result;
                                } catch (Throwable th) {
                                    th = th;
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
            }

            @Override // android.location.ILocationManager
            public String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) throws RemoteException {
                Parcel _reply;
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
                        } catch (Throwable th) {
                            th = th;
                            _reply = _reply2;
                        }
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(10, _data, _reply2, 0)) {
                        try {
                            if (Stub.getDefaultImpl() != null) {
                                try {
                                    String fromLocationName = Stub.getDefaultImpl().getFromLocationName(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
                                    _reply2.recycle();
                                    _data.recycle();
                                    return fromLocationName;
                                } catch (Throwable th2) {
                                    th = th2;
                                    _reply = _reply2;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            }
                        } catch (Throwable th3) {
                            th = th3;
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
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply = _reply2;
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
            }

            @Override // android.location.ILocationManager
            public boolean sendNiResponse(int notifId, int userResponse) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(notifId);
                    _data.writeInt(userResponse);
                    boolean _result = false;
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendNiResponse(notifId, userResponse);
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

            @Override // android.location.ILocationManager
            public boolean addGnssMeasurementsListener(IGnssMeasurementsListener listener, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addGnssMeasurementsListener(listener, packageName);
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

            @Override // android.location.ILocationManager
            public void injectGnssMeasurementCorrections(GnssMeasurementCorrections corrections, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (corrections != null) {
                        _data.writeInt(1);
                        corrections.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().injectGnssMeasurementCorrections(corrections, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public long getGnssCapabilities(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGnssCapabilities(packageName);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public void removeGnssMeasurementsListener(IGnssMeasurementsListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeGnssMeasurementsListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public boolean addGnssNavigationMessageListener(IGnssNavigationMessageListener listener, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addGnssNavigationMessageListener(listener, packageName);
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

            @Override // android.location.ILocationManager
            public void removeGnssNavigationMessageListener(IGnssNavigationMessageListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeGnssNavigationMessageListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public int getGnssYearOfHardware() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGnssYearOfHardware();
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

            @Override // android.location.ILocationManager
            public String getGnssHardwareModelName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGnssHardwareModelName();
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

            @Override // android.location.ILocationManager
            public int getGnssBatchSize(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGnssBatchSize(packageName);
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

            @Override // android.location.ILocationManager
            public boolean addGnssBatchingCallback(IBatchedLocationCallback callback, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addGnssBatchingCallback(callback, packageName);
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

            @Override // android.location.ILocationManager
            public void removeGnssBatchingCallback() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeGnssBatchingCallback();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public boolean startGnssBatch(long periodNanos, boolean wakeOnFifoFull, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(periodNanos);
                    boolean _result = true;
                    _data.writeInt(wakeOnFifoFull ? 1 : 0);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startGnssBatch(periodNanos, wakeOnFifoFull, packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public void flushGnssBatch(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().flushGnssBatch(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public boolean stopGnssBatch() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopGnssBatch();
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

            @Override // android.location.ILocationManager
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
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().injectLocation(location);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public List<String> getAllProviders() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllProviders();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public List<String> getProviders(Criteria criteria, boolean enabledOnly) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (criteria != null) {
                        _data.writeInt(1);
                        criteria.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!enabledOnly) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProviders(criteria, enabledOnly);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public String getBestProvider(Criteria criteria, boolean enabledOnly) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (criteria != null) {
                        _data.writeInt(1);
                        criteria.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!enabledOnly) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBestProvider(criteria, enabledOnly);
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

            @Override // android.location.ILocationManager
            public ProviderProperties getProviderProperties(String provider) throws RemoteException {
                ProviderProperties _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProviderProperties(provider);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ProviderProperties.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public boolean isProviderPackage(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isProviderPackage(packageName);
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

            @Override // android.location.ILocationManager
            public void setExtraLocationControllerPackage(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(32, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setExtraLocationControllerPackage(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public String getExtraLocationControllerPackage() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getExtraLocationControllerPackage();
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

            @Override // android.location.ILocationManager
            public void setExtraLocationControllerPackageEnabled(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setExtraLocationControllerPackageEnabled(enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public boolean isExtraLocationControllerPackageEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isExtraLocationControllerPackageEnabled();
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

            @Override // android.location.ILocationManager
            public boolean isProviderEnabledForUser(String provider, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isProviderEnabledForUser(provider, userId);
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

            @Override // android.location.ILocationManager
            public boolean isLocationEnabledForUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isLocationEnabledForUser(userId);
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

            @Override // android.location.ILocationManager
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
                    if (this.mRemote.transact(38, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addTestProvider(name, properties, opPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public void removeTestProvider(String provider, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    _data.writeString(opPackageName);
                    if (this.mRemote.transact(39, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeTestProvider(provider, opPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
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
                    if (this.mRemote.transact(40, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setTestProviderLocation(provider, loc, opPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public void setTestProviderEnabled(String provider, boolean enabled, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    _data.writeInt(enabled ? 1 : 0);
                    _data.writeString(opPackageName);
                    if (this.mRemote.transact(41, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setTestProviderEnabled(provider, enabled, opPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public List<LocationRequest> getTestProviderCurrentRequests(String provider, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    _data.writeString(opPackageName);
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTestProviderCurrentRequests(provider, opPackageName);
                    }
                    _reply.readException();
                    List<LocationRequest> _result = _reply.createTypedArrayList(LocationRequest.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public LocationTime getGnssTimeMillis() throws RemoteException {
                LocationTime _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(43, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGnssTimeMillis();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = LocationTime.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public void setTestProviderStatus(String provider, int status, Bundle extras, long updateTime, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(provider);
                        try {
                            _data.writeInt(status);
                            if (extras != null) {
                                _data.writeInt(1);
                                extras.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                        } catch (Throwable th) {
                            th = th;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(updateTime);
                        try {
                            _data.writeString(opPackageName);
                            if (this.mRemote.transact(44, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().setTestProviderStatus(provider, status, extras, updateTime, opPackageName);
                            _reply.recycle();
                            _data.recycle();
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

            @Override // android.location.ILocationManager
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
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendExtraCommand(provider, command, extras);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    if (_reply.readInt() != 0) {
                        extras.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public void locationCallbackFinished(ILocationListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(46, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().locationCallbackFinished(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public String[] getBackgroundThrottlingWhitelist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(47, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBackgroundThrottlingWhitelist();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationManager
            public String[] getIgnoreSettingsWhitelist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(48, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIgnoreSettingsWhitelist();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ILocationManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ILocationManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
