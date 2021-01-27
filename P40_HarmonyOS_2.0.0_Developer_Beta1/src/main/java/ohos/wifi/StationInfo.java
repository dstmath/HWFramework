package ohos.wifi;

import ohos.annotation.SystemApi;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

@SystemApi
public final class StationInfo implements Sequenceable {
    private String ipAddress;
    private String macAddress;
    private String name;

    public StationInfo(String str, String str2, String str3) {
        this.name = str;
        this.macAddress = str2;
        this.ipAddress = str3;
    }

    public StationInfo() {
        this(null, null, null);
    }

    public String getName() {
        return this.name;
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if ((parcel.writeString(this.name) && parcel.writeString(this.macAddress)) && parcel.writeString(this.ipAddress)) {
            return true;
        }
        parcel.reclaim();
        return false;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.name = parcel.readString();
        this.macAddress = parcel.readString();
        this.ipAddress = parcel.readString();
        return true;
    }
}
