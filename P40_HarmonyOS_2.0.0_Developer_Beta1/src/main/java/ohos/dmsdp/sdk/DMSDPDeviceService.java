package ohos.dmsdp.sdk;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.HashMap;
import java.util.Map;

public class DMSDPDeviceService implements Parcelable {
    public static final Parcelable.Creator<DMSDPDeviceService> CREATOR = new Parcelable.Creator<DMSDPDeviceService>() {
        /* class ohos.dmsdp.sdk.DMSDPDeviceService.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DMSDPDeviceService createFromParcel(Parcel parcel) {
            return new DMSDPDeviceService(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public DMSDPDeviceService[] newArray(int i) {
            return new DMSDPDeviceService[i];
        }
    };
    private static final String TAG = "InnerDMSDPDeviceService";
    private String deviceId;
    private String deviceName;
    private Map<Integer, Object> properties = new HashMap(1);
    private String serviceId;
    private int serviceType;
    private Map<Integer, Object> status = new HashMap(1);

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public DMSDPDeviceService(String str, String str2, String str3, int i) {
        this.deviceId = str;
        this.deviceName = str2;
        this.serviceId = str3;
        this.serviceType = i;
    }

    protected DMSDPDeviceService(Parcel parcel) {
        this.deviceId = parcel.readString();
        this.serviceId = parcel.readString();
        this.serviceType = parcel.readInt();
        this.properties = parcel.readHashMap(HashMap.class.getClassLoader());
        this.status = parcel.readHashMap(HashMap.class.getClassLoader());
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.deviceId);
        parcel.writeString(this.serviceId);
        parcel.writeInt(this.serviceType);
        parcel.writeMap(this.properties);
        parcel.writeMap(this.status);
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

    public Object getProperties(int i) {
        return this.properties.get(Integer.valueOf(i));
    }

    public Map<Integer, Object> getProperties() {
        return this.properties;
    }

    public Object getStatus(int i) {
        return this.status.get(Integer.valueOf(i));
    }

    public boolean addProperties(Map<Integer, Object> map) {
        boolean z = true;
        for (Map.Entry<Integer, Object> entry : map.entrySet()) {
            if (!addProperties(entry.getKey().intValue(), entry.getValue())) {
                z = false;
            }
        }
        return z;
    }

    public boolean addProperties(int i, Object obj) {
        if (!DeviceParameterConst.invalidCheck(i, obj)) {
            return false;
        }
        this.properties.put(Integer.valueOf(i), obj);
        return true;
    }

    public boolean addStatus(Map<Integer, Object> map) {
        boolean z = true;
        for (Map.Entry<Integer, Object> entry : map.entrySet()) {
            if (!addStatus(entry.getKey().intValue(), entry.getValue())) {
                z = false;
            }
        }
        return z;
    }

    public boolean addStatus(int i, Object obj) {
        if (!DeviceParameterConst.invalidCheck(i, obj)) {
            return false;
        }
        this.status.put(Integer.valueOf(i), obj);
        return true;
    }

    @Override // java.lang.Object
    public String toString() {
        return "DMSDPDeviceService{, serviceType=" + this.serviceType + ", status=" + this.status + '}';
    }
}
