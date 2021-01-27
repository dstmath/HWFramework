package ohos.dmsdp.sdk;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.Surface;
import java.util.HashMap;
import java.util.Map;
import ohos.global.icu.impl.PatternTokenizer;

public class DeviceInfo implements Parcelable {
    public static final Parcelable.Creator<DeviceInfo> CREATOR = new Parcelable.Creator<DeviceInfo>() {
        /* class ohos.dmsdp.sdk.DeviceInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DeviceInfo createFromParcel(Parcel parcel) {
            return new DeviceInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public DeviceInfo[] newArray(int i) {
            return new DeviceInfo[i];
        }
    };
    private static final String TAG = "DeviceInfo";
    private String mDeviceId;
    private String mDeviceName;
    private int mDeviceType;
    private Map<Integer, Object> mProperties = new HashMap();
    private Map<String, byte[]> mSessionKeyMap = new HashMap();
    private Surface mSurface;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public DeviceInfo(String str) {
        this.mDeviceId = str;
    }

    protected DeviceInfo(Parcel parcel) {
        this.mDeviceId = parcel.readString();
        this.mDeviceType = parcel.readInt();
        this.mDeviceName = parcel.readString();
        this.mSurface = (Surface) parcel.readParcelable(Surface.class.getClassLoader());
        this.mSessionKeyMap = parcel.readHashMap(HashMap.class.getClassLoader());
        this.mProperties = parcel.readHashMap(HashMap.class.getClassLoader());
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }

    public void setDeviceId(String str) {
        this.mDeviceId = str;
    }

    public String getDeviceName() {
        return this.mDeviceName;
    }

    public void setDeviceName(String str) {
        this.mDeviceName = str;
    }

    public int getDeviceType() {
        return this.mDeviceType;
    }

    public void setDeviceType(int i) {
        this.mDeviceType = i;
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

    public void setSessionKey(String str, byte[] bArr) {
        if (str != null && !str.isEmpty() && bArr != null && bArr.length > 0) {
            this.mSessionKeyMap.put(str, bArr);
        }
    }

    public Map<Integer, Object> getProperties() {
        return this.mProperties;
    }

    public Object getProperty(Integer num) {
        if (this.mProperties.size() != 0 && this.mProperties.containsKey(num)) {
            return this.mProperties.get(num);
        }
        return null;
    }

    public boolean addProperties(int i, Object obj) {
        if (!DeviceParameterConst.invalidCheck(i, obj)) {
            return false;
        }
        this.mProperties.put(Integer.valueOf(i), obj);
        return true;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mDeviceId);
        parcel.writeInt(this.mDeviceType);
        parcel.writeString(this.mDeviceName);
        parcel.writeParcelable(this.mSurface, 0);
        parcel.writeMap(this.mSessionKeyMap);
        parcel.writeMap(this.mProperties);
    }

    @Override // java.lang.Object
    public String toString() {
        return "DeviceInfo{, mDeviceType=" + this.mDeviceType + ", mDeviceName='" + this.mDeviceName + PatternTokenizer.SINGLE_QUOTE + '}';
    }
}
