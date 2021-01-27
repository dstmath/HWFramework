package ohos.dmsdp.sdk;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.HashMap;
import java.util.Map;

public class DMSDPDevice implements Parcelable {
    public static final Parcelable.Creator<DMSDPDevice> CREATOR = new Parcelable.Creator<DMSDPDevice>() {
        /* class ohos.dmsdp.sdk.DMSDPDevice.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DMSDPDevice createFromParcel(Parcel parcel) {
            return new DMSDPDevice(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public DMSDPDevice[] newArray(int i) {
            return new DMSDPDevice[i];
        }
    };
    private static final String TAG = "DMSDPDevice";
    private byte[] mAbilities;
    private String mBluetoothMac;
    private String mBtName;
    private String mDeviceId;
    private String mDeviceName;
    private int mDeviceType;
    private String mLocalIp;
    private int mPort;
    private Map<Integer, Object> mProperties;
    private String mRemoteIp;
    private byte[] mSessionKeys;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public DMSDPDevice(String str, int i) {
        this.mProperties = new HashMap(1);
        this.mAbilities = new byte[0];
        this.mSessionKeys = new byte[0];
        this.mDeviceId = str;
        this.mDeviceType = i;
    }

    protected DMSDPDevice(Parcel parcel) {
        this.mProperties = new HashMap(1);
        this.mAbilities = new byte[0];
        this.mSessionKeys = new byte[0];
        this.mDeviceId = parcel.readString();
        this.mDeviceType = parcel.readInt();
        this.mDeviceName = parcel.readString();
        this.mBluetoothMac = parcel.readString();
        this.mBtName = parcel.readString();
        this.mLocalIp = parcel.readString();
        this.mRemoteIp = parcel.readString();
        this.mPort = parcel.readInt();
        this.mProperties = parcel.readHashMap(HashMap.class.getClassLoader());
        int readInt = parcel.readInt();
        if (readInt > 0) {
            this.mAbilities = new byte[readInt];
            parcel.readByteArray(this.mAbilities);
        }
        int readInt2 = parcel.readInt();
        if (readInt2 > 0) {
            this.mSessionKeys = new byte[readInt2];
            parcel.readByteArray(this.mSessionKeys);
        }
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }

    public int getDeviceType() {
        return this.mDeviceType;
    }

    public String getDeviceName() {
        return this.mDeviceName;
    }

    public void setDeviceName(String str) {
        this.mDeviceName = str;
    }

    public String getBluetoothMac() {
        return this.mBluetoothMac;
    }

    public void setBluetoothMac(String str) {
        this.mBluetoothMac = str;
    }

    public String getBtName() {
        return this.mBtName;
    }

    public void setBtName(String str) {
        this.mBtName = str;
    }

    public String getLocalIp() {
        return this.mLocalIp;
    }

    public void setLocalIp(String str) {
        this.mLocalIp = str;
    }

    public String getRemoteIp() {
        return this.mRemoteIp;
    }

    public void setRemoteIp(String str) {
        this.mRemoteIp = str;
    }

    public int getPort() {
        return this.mPort;
    }

    public void setPort(int i) {
        this.mPort = i;
    }

    public byte[] getSessionKey() {
        return (byte[]) this.mSessionKeys.clone();
    }

    public void setSessionKey(byte[] bArr) {
        this.mSessionKeys = (byte[]) bArr.clone();
    }

    public Object getProperties(int i) {
        return this.mProperties.get(Integer.valueOf(i));
    }

    public Map<Integer, Object> getProperties() {
        return this.mProperties;
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
        this.mProperties.put(Integer.valueOf(i), obj);
        return true;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mDeviceId);
        parcel.writeInt(this.mDeviceType);
        parcel.writeString(this.mDeviceName);
        parcel.writeString(this.mBluetoothMac);
        parcel.writeString(this.mBtName);
        parcel.writeString(this.mLocalIp);
        parcel.writeString(this.mRemoteIp);
        parcel.writeInt(this.mPort);
        parcel.writeMap(this.mProperties);
        parcel.writeInt(this.mAbilities.length);
        byte[] bArr = this.mAbilities;
        if (bArr.length > 0) {
            parcel.writeByteArray(bArr);
        }
        parcel.writeInt(this.mSessionKeys.length);
        byte[] bArr2 = this.mSessionKeys;
        if (bArr2.length > 0) {
            parcel.writeByteArray(bArr2);
        }
    }

    @Override // java.lang.Object
    public String toString() {
        return "DMSDPDevice{, mDeviceType=" + this.mDeviceType + '}';
    }
}
