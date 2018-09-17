package com.android.server;

import android.os.Handler;
import com.android.server.LocationManagerService.Receiver;
import com.android.server.LocationManagerService.UpdateRecord;
import com.android.server.location.GeocoderProxy;
import com.android.server.location.LocationProviderInterface;
import com.android.server.location.LocationProviderProxy;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.GetField;
import com.huawei.utils.reflect.annotation.InvokeMethod;
import com.huawei.utils.reflect.annotation.SetField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class LocationManagerServiceUtils extends EasyInvokeUtils {
    MethodObject<Void> addProviderLocked;
    MethodObject<Boolean> isMockProvider;
    FieldObject<Set<String>> mEnabledProviders;
    FieldObject<GeocoderProxy> mGeocodeProvider;
    FieldObject<Handler> mLocationHandler;
    FieldObject<Object> mLock;
    FieldObject<HashMap<String, LocationProviderInterface>> mProvidersByName;
    FieldObject<ArrayList<LocationProviderProxy>> mProxyProviders;
    FieldObject<HashMap<String, LocationProviderInterface>> mRealProviders;
    FieldObject<HashMap<Object, Receiver>> mReceivers;
    FieldObject<HashMap<String, ArrayList<UpdateRecord>>> mRecordsByProvider;
    MethodObject<Void> removeUpdatesLocked;
    MethodObject<Void> updateProvidersLocked;

    @InvokeMethod(methodObject = "addProviderLocked")
    public void addProviderLocked(LocationManagerService service, LocationProviderInterface provider) {
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

    @GetField(fieldObject = "mLocationHandler")
    public Handler getLocationHandler(LocationManagerService service) {
        return (Handler) getField(this.mLocationHandler, service);
    }

    @GetField(fieldObject = "mRealProviders")
    public HashMap<String, LocationProviderInterface> getRealProviders(LocationManagerService service) {
        return (HashMap) getField(this.mRealProviders, service);
    }

    @GetField(fieldObject = "mEnabledProviders")
    public Set<String> getEnabledProviders(LocationManagerService service) {
        return (Set) getField(this.mEnabledProviders, service);
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
    public HashMap<Object, Receiver> getReceivers(LocationManagerService service) {
        return (HashMap) getField(this.mReceivers, service);
    }

    @InvokeMethod(methodObject = "removeUpdatesLocked")
    public void removeUpdatesLocked(LocationManagerService service, Receiver receiver) {
        invokeMethod(this.removeUpdatesLocked, service, new Object[]{receiver});
    }

    @InvokeMethod(methodObject = "isMockProvider")
    public Boolean isMockProvider(LocationManagerService service, String provider) {
        return (Boolean) invokeMethod(this.isMockProvider, service, new Object[]{provider});
    }

    @GetField(fieldObject = "mProvidersByName")
    public HashMap<String, LocationProviderInterface> getProvidersByName(LocationManagerService service) {
        return (HashMap) getField(this.mProvidersByName, service);
    }

    @GetField(fieldObject = "mRecordsByProvider")
    public HashMap<String, ArrayList<UpdateRecord>> getRecordsByProvider(LocationManagerService service) {
        return (HashMap) getField(this.mRecordsByProvider, service);
    }
}
