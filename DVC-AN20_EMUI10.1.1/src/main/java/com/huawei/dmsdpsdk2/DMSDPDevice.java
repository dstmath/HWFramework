package com.huawei.dmsdpsdk2;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.HashMap;
import java.util.Map;

public class DMSDPDevice implements Parcelable {
    public static final Parcelable.Creator<DMSDPDevice> CREATOR = new Parcelable.Creator<DMSDPDevice>() {
        /* class com.huawei.dmsdpsdk2.DMSDPDevice.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DMSDPDevice createFromParcel(Parcel in) {
            return new DMSDPDevice(in);
        }

        @Override // android.os.Parcelable.Creator
        public DMSDPDevice[] newArray(int size) {
            return new DMSDPDevice[size];
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
    private byte[] mSessionKey;

    public DMSDPDevice(String deviceId, int deviceType) {
        this.mProperties = new HashMap(1);
        this.mAbilities = new byte[0];
        this.mSessionKey = new byte[0];
        this.mDeviceId = deviceId;
        this.mDeviceType = deviceType;
    }

    protected DMSDPDevice(Parcel in) {
        this.mProperties = new HashMap(1);
        this.mAbilities = new byte[0];
        this.mSessionKey = new byte[0];
        this.mDeviceId = in.readString();
        this.mDeviceType = in.readInt();
        this.mDeviceName = in.readString();
        this.mBluetoothMac = in.readString();
        this.mBtName = in.readString();
        this.mLocalIp = in.readString();
        this.mRemoteIp = in.readString();
        this.mPort = in.readInt();
        this.mProperties = in.readHashMap(HashMap.class.getClassLoader());
        int abilitiesLen = in.readInt();
        if (abilitiesLen > 0) {
            this.mAbilities = new byte[abilitiesLen];
            in.readByteArray(this.mAbilities);
        }
        int keyLen = in.readInt();
        if (keyLen > 0) {
            this.mSessionKey = new byte[keyLen];
            in.readByteArray(this.mSessionKey);
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

    public void setDeviceName(String deviceName) {
        this.mDeviceName = deviceName;
    }

    public String getBluetoothMac() {
        return this.mBluetoothMac;
    }

    public void setBluetoothMac(String bluetoothMac) {
        this.mBluetoothMac = bluetoothMac;
    }

    public String getBtName() {
        return this.mBtName;
    }

    public void setBtName(String btName) {
        this.mBtName = btName;
    }

    public String getLocalIp() {
        return this.mLocalIp;
    }

    public void setLocalIp(String localIp) {
        this.mLocalIp = localIp;
    }

    public String getRemoteIp() {
        return this.mRemoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.mRemoteIp = remoteIp;
    }

    public int getPort() {
        return this.mPort;
    }

    public void setPort(int port) {
        this.mPort = port;
    }

    public byte[] getSessionKey() {
        return (byte[]) this.mSessionKey.clone();
    }

    public void setSessionKey(byte[] key) {
        this.mSessionKey = (byte[]) key.clone();
    }

    public Object getProperties(int key) {
        return this.mProperties.get(Integer.valueOf(key));
    }

    public Map<Integer, Object> getProperties() {
        return this.mProperties;
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
        this.mProperties.put(Integer.valueOf(key), value);
        return true;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mDeviceId);
        dest.writeInt(this.mDeviceType);
        dest.writeString(this.mDeviceName);
        dest.writeString(this.mBluetoothMac);
        dest.writeString(this.mBtName);
        dest.writeString(this.mLocalIp);
        dest.writeString(this.mRemoteIp);
        dest.writeInt(this.mPort);
        dest.writeMap(this.mProperties);
        dest.writeInt(this.mAbilities.length);
        byte[] bArr = this.mAbilities;
        if (bArr.length > 0) {
            dest.writeByteArray(bArr);
        }
        dest.writeInt(this.mSessionKey.length);
        byte[] bArr2 = this.mSessionKey;
        if (bArr2.length > 0) {
            dest.writeByteArray(bArr2);
        }
    }

    public String toString() {
        return "DMSDPDevice{, mDeviceType=" + this.mDeviceType + '}';
    }
}
