package com.android.server.location;

import android.database.ContentObserver;
import android.location.GnssMeasurementsEvent;
import android.location.Location;
import android.os.Looper;
import android.os.WorkSource;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.GetField;
import com.huawei.utils.reflect.annotation.InvokeMethod;
import com.huawei.utils.reflect.annotation.SetField;
import java.util.Properties;

public class GpsLocationProviderUtils extends EasyInvokeUtils {
    FieldObject<Integer> UPDATE_LOCATION;
    MethodObject<String> getDefaultApn;
    MethodObject<Void> hibernate;
    FieldObject<Boolean> isRealStoped;
    FieldObject<String> mDefaultApn;
    FieldObject<ContentObserver> mDefaultApnObserver;
    FieldObject<Looper> mLooper;
    FieldObject<Integer> mPositionMode;
    FieldObject<Properties> mProperties;
    FieldObject<Boolean> mStarted;
    FieldObject<Boolean> mSuplEsEnabled;
    FieldObject<String> mSuplServerHost;
    FieldObject<Integer> mSuplServerPort;
    FieldObject<WorkSource> mWorkSource;
    MethodObject<Void> native_inject_psds_data;
    MethodObject<Void> native_set_agps_server;
    MethodObject<Void> reportLocation;
    MethodObject<Void> reportMeasurementData;
    MethodObject<Void> sendMessage;
    MethodObject<Void> setSuplHostPort;
    MethodObject<Void> startNavigating;
    MethodObject<Void> stopNavigating;
    MethodObject<Void> updateLowPowerMode;

    @InvokeMethod(methodObject = "native_set_agps_server")
    public void native_set_agps_server(GnssLocationProvider gnssLocationProvider, int type, String hostname, int port) {
        invokeMethod(this.native_set_agps_server, gnssLocationProvider, new Object[]{Integer.valueOf(type), hostname, Integer.valueOf(port)});
    }

    @SetField(fieldObject = "mSuplServerHost")
    public void setSuplServerHost(GnssLocationProvider gnssLocationProvider, String value) {
        setField(this.mSuplServerHost, gnssLocationProvider, value);
    }

    @SetField(fieldObject = "mSuplServerPort")
    public void setSuplServerPort(GnssLocationProvider gnssLocationProvider, int value) {
        setField(this.mSuplServerPort, gnssLocationProvider, Integer.valueOf(value));
    }

    @GetField(fieldObject = "mSuplServerHost")
    public String getSuplServerHost(GnssLocationProvider gnssLocationProvider) {
        return (String) getField(this.mSuplServerHost, gnssLocationProvider);
    }

    @GetField(fieldObject = "mSuplServerPort")
    public int getSuplServerPort(GnssLocationProvider gnssLocationProvider) {
        return ((Integer) getField(this.mSuplServerPort, gnssLocationProvider)).intValue();
    }

    @InvokeMethod(methodObject = "setSuplHostPort")
    public void setSuplHostPort(GnssLocationProvider gnssLocationProvider, String hostString, String portString) {
        invokeMethod(this.setSuplHostPort, gnssLocationProvider, new Object[]{hostString, portString});
    }

    @GetField(fieldObject = "mProperties")
    public Properties getProperties(GnssLocationProvider gnssLocationProvider) {
        return (Properties) getField(this.mProperties, gnssLocationProvider);
    }

    @GetField(fieldObject = "mPositionMode")
    public int getPositionMode(GnssLocationProvider gnssLocationProvider) {
        return ((Integer) getField(this.mPositionMode, gnssLocationProvider)).intValue();
    }

    @SetField(fieldObject = "mPositionMode")
    public void setPositionMode(GnssLocationProvider gnssLocationProvider, int value) {
        setField(this.mPositionMode, gnssLocationProvider, Integer.valueOf(value));
    }

    @InvokeMethod(methodObject = "getDefaultApn")
    public String getDefaultApn(GnssLocationProvider gnssLocationProvider) {
        return (String) invokeMethod(this.getDefaultApn, gnssLocationProvider, new Object[0]);
    }

    @SetField(fieldObject = "mDefaultApn")
    public void setDefaultApn(GnssLocationProvider gnssLocationProvider, String value) {
        setField(this.mDefaultApn, gnssLocationProvider, value);
    }

    @SetField(fieldObject = "mDefaultApnObserver")
    public void setDefaultApnObserver(GnssLocationProvider gnssLocationProvider, ContentObserver value) {
        setField(this.mDefaultApnObserver, gnssLocationProvider, value);
    }

    @GetField(fieldObject = "mWorkSource")
    public WorkSource getWorkSource(GnssLocationProvider gnssLocationProvider) {
        return (WorkSource) getField(this.mWorkSource, gnssLocationProvider);
    }

    @GetField(fieldObject = "mStarted")
    public boolean getGnssStarted(GnssLocationProvider gnssLocationProvider) {
        return ((Boolean) getField(this.mStarted, gnssLocationProvider)).booleanValue();
    }

    @InvokeMethod(methodObject = "reportMeasurementData")
    public void reportMeasurementData(GnssLocationProvider gnssLocationProvider, GnssMeasurementsEvent event) {
        invokeMethod(this.reportMeasurementData, gnssLocationProvider, new Object[]{event});
    }

    @InvokeMethod(methodObject = "sendMessage")
    public void sendMessage(GnssLocationProvider gnssLocationProvider, int message, int arg, Object obj) {
        invokeMethod(this.sendMessage, gnssLocationProvider, new Object[]{Integer.valueOf(message), Integer.valueOf(arg), obj});
    }

    @GetField(fieldObject = "UPDATE_LOCATION")
    public int getUpdateLocationMassage(GnssLocationProvider gnssLocationProvider) {
        return ((Integer) getField(this.UPDATE_LOCATION, gnssLocationProvider)).intValue();
    }

    @InvokeMethod(methodObject = "startNavigating")
    public void startNavigating(GnssLocationProvider gnssLocationProvider) {
        invokeMethod(this.startNavigating, gnssLocationProvider, new Object[0]);
    }

    @InvokeMethod(methodObject = "stopNavigating")
    public void stopNavigating(GnssLocationProvider gnssLocationProvider) {
        invokeMethod(this.stopNavigating, gnssLocationProvider, new Object[0]);
    }

    @InvokeMethod(methodObject = "hibernate")
    public void hibernate(GnssLocationProvider gnssLocationProvider) {
        invokeMethod(this.hibernate, gnssLocationProvider, new Object[0]);
    }

    @InvokeMethod(methodObject = "updateLowPowerMode")
    public void updateLowPowerMode(GnssLocationProvider gnssLocationProvider) {
        invokeMethod(this.updateLowPowerMode, gnssLocationProvider, new Object[0]);
    }

    @GetField(fieldObject = "isRealStoped")
    public boolean getGnssRealStoped(GnssLocationProvider gnssLocationProvider) {
        return ((Boolean) getField(this.isRealStoped, gnssLocationProvider)).booleanValue();
    }

    @GetField(fieldObject = "mLooper")
    public Looper getGnssLooper(GnssLocationProvider gnssLocationProvider) {
        return (Looper) getField(this.mLooper, gnssLocationProvider);
    }

    @InvokeMethod(methodObject = "reportLocation")
    public void reportLocation(GnssLocationProvider gnssLocationProvider, boolean hasLatLong, Location location) {
        invokeMethod(this.reportLocation, gnssLocationProvider, new Object[]{Boolean.valueOf(hasLatLong), location});
    }

    @InvokeMethod(methodObject = "native_inject_psds_data")
    public void native_inject_psds_data(GnssLocationProvider gnssLocationProvider, byte[] data, int length) {
        invokeMethod(this.native_inject_psds_data, gnssLocationProvider, new Object[]{data, Integer.valueOf(length)});
    }
}
