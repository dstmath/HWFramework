package com.android.server;

import android.location.IGnssMeasurementsListener;
import android.location.ILocationListener;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Handler;
import android.os.IBinder;
import android.os.WorkSource;
import android.util.ArrayMap;
import com.android.server.LocationManagerService;
import com.android.server.location.GeocoderProxy;
import com.android.server.location.GnssMeasurementsProvider;
import com.android.server.location.IHwGpsLogServices;
import com.android.server.location.LocationProviderProxy;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.GetField;
import com.huawei.utils.reflect.annotation.InvokeMethod;
import com.huawei.utils.reflect.annotation.SetField;
import java.util.ArrayList;
import java.util.HashMap;

public class LocationManagerServiceUtils extends EasyInvokeUtils {
    MethodObject<Void> addProviderLocked;
    MethodObject<Void> applyRequirementsLocked;
    MethodObject<LocationManagerService.LocationProvider> getLocationProviderLocked;
    MethodObject<LocationManagerService.Receiver> getReceiverLocked;
    MethodObject<Boolean> isMockProvider;
    MethodObject<Boolean> isProviderEnabledForUser;
    FieldObject<Integer> mCurrentUserId;
    FieldObject<GeocoderProxy> mGeocodeProvider;
    FieldObject<ArrayMap<IBinder, LocationManagerService.LinkedListener<IGnssMeasurementsListener>>> mGnssMeasurementsListeners;
    FieldObject<GnssMeasurementsProvider> mGnssMeasurementsProvider;
    FieldObject<Handler> mHandler;
    FieldObject<IHwGpsLogServices> mHwLocationGpsLogServices;
    FieldObject<Location> mLastFixBroadcast;
    FieldObject<HashMap<String, Location>> mLastLocation;
    FieldObject<Object> mLock;
    FieldObject<ArrayList<LocationManagerService.LocationProvider>> mProviders;
    FieldObject<HashMap<String, LocationManagerService.LocationProvider>> mProvidersByName;
    FieldObject<ArrayList<LocationProviderProxy>> mProxyProviders;
    FieldObject<ArrayList<LocationManagerService.LocationProvider>> mRealProviders;
    FieldObject<HashMap<Object, LocationManagerService.Receiver>> mReceivers;
    FieldObject<HashMap<String, ArrayList<LocationManagerService.UpdateRecord>>> mRecordsByProvider;
    MethodObject<Void> onBackgroundThrottleWhitelistChangedLocked;
    MethodObject<Void> removeProviderLocked;
    MethodObject<Void> removeUpdatesLocked;
    MethodObject<Void> requestLocationUpdatesLocked;
    MethodObject<Void> updateProvidersLocked;

    @GetField(fieldObject = "mProviders")
    public ArrayList<LocationManagerService.LocationProvider> getProviders(LocationManagerService service) {
        return (ArrayList) getField(this.mProviders, service);
    }

    @InvokeMethod(methodObject = "isProviderEnabledForUser")
    public Boolean isProviderEnabledForUser(LocationManagerService service, String providerName, int userId) {
        return (Boolean) invokeMethod(this.isProviderEnabledForUser, service, new Object[]{providerName, Integer.valueOf(userId)});
    }

    @InvokeMethod(methodObject = "addProviderLocked")
    public void addProviderLocked(LocationManagerService service, LocationManagerService.LocationProvider provider) {
        invokeMethod(this.addProviderLocked, service, new Object[]{provider});
    }

    @GetField(fieldObject = "mGeocodeProvider")
    public GeocoderProxy getGeocodeProvider(LocationManagerService service) {
        return (GeocoderProxy) getField(this.mGeocodeProvider, service);
    }

    @SetField(fieldObject = "mGeocodeProvider")
    public void setGeocodeProvider(LocationManagerService service, GeocoderProxy geocoderProxy) {
        setField(this.mGeocodeProvider, service, geocoderProxy);
    }

    @GetField(fieldObject = "mHandler")
    public Handler getLocationHandler(LocationManagerService service) {
        return (Handler) getField(this.mHandler, service);
    }

    @SetField(fieldObject = "mHandler")
    public void setLocationHandler(LocationManagerService service, Handler handler) {
        setField(this.mHandler, service, handler);
    }

    @GetField(fieldObject = "mRealProviders")
    public ArrayList<LocationManagerService.LocationProvider> getRealProviders(LocationManagerService service) {
        return (ArrayList) getField(this.mRealProviders, service);
    }

    @GetField(fieldObject = "mProxyProviders")
    public ArrayList<LocationProviderProxy> getProxyProviders(LocationManagerService service) {
        return (ArrayList) getField(this.mProxyProviders, service);
    }

    @InvokeMethod(methodObject = "updateProvidersLocked")
    public void updateProvidersLocked(LocationManagerService service) {
        invokeMethod(this.updateProvidersLocked, service, new Object[0]);
    }

    @GetField(fieldObject = "mLock")
    public Object getmLock(LocationManagerService service) {
        return getField(this.mLock, service);
    }

    @GetField(fieldObject = "mReceivers")
    public HashMap<Object, LocationManagerService.Receiver> getReceivers(LocationManagerService service) {
        return (HashMap) getField(this.mReceivers, service);
    }

    @InvokeMethod(methodObject = "removeUpdatesLocked")
    public void removeUpdatesLocked(LocationManagerService service, LocationManagerService.Receiver receiver) {
        invokeMethod(this.removeUpdatesLocked, service, new Object[]{receiver});
    }

    @InvokeMethod(methodObject = "isMockProvider")
    public Boolean isMockProvider(LocationManagerService service, String provider) {
        return (Boolean) invokeMethod(this.isMockProvider, service, new Object[]{provider});
    }

    @GetField(fieldObject = "mProvidersByName")
    public HashMap<String, LocationManagerService.LocationProvider> getProvidersByName(LocationManagerService service) {
        return (HashMap) getField(this.mProvidersByName, service);
    }

    @GetField(fieldObject = "mRecordsByProvider")
    public HashMap<String, ArrayList<LocationManagerService.UpdateRecord>> getRecordsByProvider(LocationManagerService service) {
        return (HashMap) getField(this.mRecordsByProvider, service);
    }

    @InvokeMethod(methodObject = "applyRequirementsLocked")
    public void applyRequirementsLocked(LocationManagerService service, String provider) {
        invokeMethod(this.applyRequirementsLocked, service, new Object[]{provider});
    }

    @GetField(fieldObject = "mCurrentUserId")
    public int getCurrentUserId(LocationManagerService service) {
        return ((Integer) getField(this.mCurrentUserId, service)).intValue();
    }

    @GetField(fieldObject = "mGnssMeasurementsListeners")
    public ArrayMap<IBinder, LocationManagerService.LinkedListener<IGnssMeasurementsListener>> getGnssMeasurementsListeners(LocationManagerService service) {
        return (ArrayMap) getField(this.mGnssMeasurementsListeners, service);
    }

    @GetField(fieldObject = "mGnssMeasurementsProvider")
    public GnssMeasurementsProvider getGnssMeasurementsProvider(LocationManagerService service) {
        return (GnssMeasurementsProvider) getField(this.mGnssMeasurementsProvider, service);
    }

    @InvokeMethod(methodObject = "updateBackgroundThrottlingWhitelistLocked")
    public void onBackgroundThrottleWhitelistChangedLocked(LocationManagerService service) {
        invokeMethod(this.onBackgroundThrottleWhitelistChangedLocked, service, new Object[0]);
    }

    @GetField(fieldObject = "mLastFixBroadcast")
    public Location getLastFixBroadcast(LocationManagerService.UpdateRecord updateRecord) {
        return (Location) getField(this.mLastFixBroadcast, updateRecord);
    }

    @InvokeMethod(methodObject = "getLocationProviderLocked")
    public LocationManagerService.LocationProvider getLocationProviderLocked(LocationManagerService service, String providerName) {
        return (LocationManagerService.LocationProvider) invokeMethod(this.getLocationProviderLocked, service, new Object[]{providerName});
    }

    @InvokeMethod(methodObject = "removeProviderLocked")
    public void removeProviderLocked(LocationManagerService service, LocationManagerService.LocationProvider provider) {
        invokeMethod(this.removeProviderLocked, service, new Object[]{provider});
    }

    @SetField(fieldObject = "mHwLocationGpsLogServices")
    public void setHwLocationGpsLogServices(LocationManagerService service, IHwGpsLogServices hwGpsLogServices) {
        setField(this.mHwLocationGpsLogServices, service, hwGpsLogServices);
    }

    @InvokeMethod(methodObject = "getReceiverLocked")
    public LocationManagerService.Receiver getReceiverLocked(LocationManagerService service, ILocationListener listener, int pid, int uid, String packageName, WorkSource workSource, boolean hideFromAppOps) {
        return (LocationManagerService.Receiver) invokeMethod(this.getReceiverLocked, service, new Object[]{listener, Integer.valueOf(pid), Integer.valueOf(uid), packageName, workSource, Boolean.valueOf(hideFromAppOps)});
    }

    @InvokeMethod(methodObject = "requestLocationUpdatesLocked")
    public void requestLocationUpdatesLocked(LocationManagerService service, LocationRequest request, LocationManagerService.Receiver receiver, int uid, String packageName) {
        invokeMethod(this.requestLocationUpdatesLocked, service, new Object[]{request, receiver, Integer.valueOf(uid), packageName});
    }

    @GetField(fieldObject = "mLastLocation")
    public HashMap<String, Location> getLastLocation(LocationManagerService service) {
        return (HashMap) getField(this.mLastLocation, service);
    }
}
