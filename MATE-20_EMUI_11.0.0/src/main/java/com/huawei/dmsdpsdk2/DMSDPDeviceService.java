package com.huawei.dmsdpsdk2;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.HashMap;
import java.util.Map;

public class DMSDPDeviceService implements Parcelable {
    public static final Parcelable.Creator<DMSDPDeviceService> CREATOR = new Parcelable.Creator<DMSDPDeviceService>() {
        /* class com.huawei.dmsdpsdk2.DMSDPDeviceService.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DMSDPDeviceService createFromParcel(Parcel in) {
            return new DMSDPDeviceService(in);
        }

        @Override // android.os.Parcelable.Creator
        public DMSDPDeviceService[] newArray(int size) {
            return new DMSDPDeviceService[size];
        }
    };
    private static final String TAG = "DMSDPDeviceService";
    private String deviceId;
    private String deviceName;
    private Map<Integer, Object> properties = new HashMap(1);
    private String serviceId;
    private int serviceType;
    private Map<Integer, Object> status = new HashMap(1);

    public DMSDPDeviceService(String deviceId2, String deviceName2, String serviceId2, int serviceType2) {
        this.deviceId = deviceId2;
        this.deviceName = deviceName2;
        this.serviceId = serviceId2;
        this.serviceType = serviceType2;
    }

    protected DMSDPDeviceService(Parcel in) {
        this.deviceId = in.readString();
        this.serviceId = in.readString();
        this.serviceType = in.readInt();
        this.properties = in.readHashMap(HashMap.class.getClassLoader());
        this.status = in.readHashMap(HashMap.class.getClassLoader());
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceId);
        dest.writeString(this.serviceId);
        dest.writeInt(this.serviceType);
        dest.writeMap(this.properties);
        dest.writeMap(this.status);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public int getServiceType() {
        return this.serviceType;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public Object getProperties(int key) {
        return this.properties.get(Integer.valueOf(key));
    }

    public Map<Integer, Object> getProperties() {
        return this.properties;
    }

    public Object getStatus(int key) {
        return this.status.get(Integer.valueOf(key));
    }

    public boolean addProperties(Map<Integer, Object> propertiesObj) {
        boolean result = true;
        for (Map.Entry<Integer, Object> entry : propertiesObj.entrySet()) {
            if (!addProperties(entry.getKey().intValue(), entry.getValue())) {
                result = false;
            }
        }
        return result;
    }

    public boolean addProperties(int key, Object value) {
        if (!DeviceParameterConst.invalidCheck(key, value)) {
            return false;
        }
        this.properties.put(Integer.valueOf(key), value);
        return true;
    }

    public boolean addStatus(Map<Integer, Object> statusObj) {
        boolean result = true;
        for (Map.Entry<Integer, Object> entry : statusObj.entrySet()) {
            if (!addStatus(entry.getKey().intValue(), entry.getValue())) {
                result = false;
            }
        }
        return result;
    }

    public boolean addStatus(int key, Object value) {
        if (!DeviceParameterConst.invalidCheck(key, value)) {
            return false;
        }
        this.status.put(Integer.valueOf(key), value);
        return true;
    }

    @Override // java.lang.Object
    public String toString() {
        return "DMSDPDeviceService{, serviceType=" + this.serviceType + ", status=" + this.status + '}';
    }
}
