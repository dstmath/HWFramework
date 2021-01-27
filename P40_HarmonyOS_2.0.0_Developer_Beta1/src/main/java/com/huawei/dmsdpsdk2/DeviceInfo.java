package com.huawei.dmsdpsdk2;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.Surface;
import java.util.HashMap;
import java.util.Map;

public class DeviceInfo implements Parcelable {
    public static final Parcelable.Creator<DeviceInfo> CREATOR = new Parcelable.Creator<DeviceInfo>() {
        /* class com.huawei.dmsdpsdk2.DeviceInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DeviceInfo createFromParcel(Parcel in) {
            return new DeviceInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public DeviceInfo[] newArray(int size) {
            return new DeviceInfo[size];
        }
    };
    private static final String TAG = "DeviceInfo";
    private String mDeviceId;
    private String mDeviceName;
    private int mDeviceType;
    private Map<Integer, Object> mProperties = new HashMap();
    private Map<String, byte[]> mSessionKeyMap = new HashMap();
    private Surface mSurface;

    public DeviceInfo(String deviceId) {
        this.mDeviceId = deviceId;
    }

    protected DeviceInfo(Parcel in) {
        this.mDeviceId = in.readString();
        this.mDeviceType = in.readInt();
        this.mDeviceName = in.readString();
        this.mSurface = (Surface) in.readParcelable(Surface.class.getClassLoader());
        this.mSessionKeyMap = in.readHashMap(HashMap.class.getClassLoader());
        this.mProperties = in.readHashMap(HashMap.class.getClassLoader());
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }

    public void setDeviceId(String deviceId) {
        this.mDeviceId = deviceId;
    }

    public String getDeviceName() {
        return this.mDeviceName;
    }

    public void setDeviceName(String deviceName) {
        this.mDeviceName = deviceName;
    }

    public int getDeviceType() {
        return this.mDeviceType;
    }

    public void setDeviceType(int deviceType) {
        this.mDeviceType = deviceType;
    }

    public Surface getSurface() {
        return this.mSurface;
    }

    public void setSurface(Surface surface) {
        this.mSurface = surface;
    }

    public Map<String, byte[]> getSessionKeyMap() {
        return this.mSessionKeyMap;
    }

    public void setSessionKey(String deviceId, byte[] sessionKey) {
        if (deviceId != null && !deviceId.isEmpty() && sessionKey != null && sessionKey.length > 0) {
            this.mSessionKeyMap.put(deviceId, sessionKey);
        }
    }

    public Map<Integer, Object> getProperties() {
        return this.mProperties;
    }

    public Object getProperty(Integer key) {
        if (this.mProperties.size() != 0 && this.mProperties.containsKey(key)) {
            return this.mProperties.get(key);
        }
        return null;
    }

    public boolean addProperties(int key, Object value) {
        if (!DeviceParameterConst.invalidCheck(key, value)) {
            return false;
        }
        this.mProperties.put(Integer.valueOf(key), value);
        return true;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mDeviceId);
        dest.writeInt(this.mDeviceType);
        dest.writeString(this.mDeviceName);
        dest.writeParcelable(this.mSurface, 0);
        dest.writeMap(this.mSessionKeyMap);
        dest.writeMap(this.mProperties);
    }

    @Override // java.lang.Object
    public String toString() {
        return "DeviceInfo{, mDeviceType=" + this.mDeviceType + ", mDeviceName='" + this.mDeviceName + "'}";
    }
}
