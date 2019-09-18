package com.android.server.location;

import android.database.ContentObserver;
import android.os.Handler;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.GetField;
import com.huawei.utils.reflect.annotation.InvokeMethod;
import com.huawei.utils.reflect.annotation.SetField;
import java.util.Properties;

public class GpsLocationProviderUtils extends EasyInvokeUtils {
    MethodObject<String> getDefaultApn;
    FieldObject<String> mDefaultApn;
    FieldObject<ContentObserver> mDefaultApnObserver;
    FieldObject<Handler> mHandler;
    FieldObject<Integer> mPositionMode;
    FieldObject<Properties> mProperties;
    FieldObject<Boolean> mStarted;
    FieldObject<Boolean> mSuplEsEnabled;
    FieldObject<String> mSuplServerHost;
    FieldObject<Integer> mSuplServerPort;
    MethodObject<Void> native_set_agps_server;
    MethodObject<Void> setSuplHostPort;

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

    @GetField(fieldObject = "mStarted")
    public boolean getGnssStarted(GnssLocationProvider gnssLocationProvider) {
        return ((Boolean) getField(this.mStarted, gnssLocationProvider)).booleanValue();
    }

    @SetField(fieldObject = "mSuplEsEnabled")
    public void setSuplEsEnabled(GnssLocationProvider gnssLocationProvider, boolean value) {
        setField(this.mSuplEsEnabled, gnssLocationProvider, Boolean.valueOf(value));
    }

    @GetField(fieldObject = "mHandler")
    public Handler getHandler(GnssLocationProvider gnssLocationProvider) {
        return (Handler) getField(this.mHandler, gnssLocationProvider);
    }
}
