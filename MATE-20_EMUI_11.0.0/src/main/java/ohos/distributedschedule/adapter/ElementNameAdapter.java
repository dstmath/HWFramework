package ohos.distributedschedule.adapter;

import ohos.bundle.ElementName;
import ohos.utils.Parcel;

public final class ElementNameAdapter {
    private static final int PARCELABLE_FLAG = 1;
    private final String abilityName;
    private final String bundleName;
    private final String deviceId;

    public ElementNameAdapter(ElementName elementName) {
        this.deviceId = elementName.getDeviceId();
        this.bundleName = elementName.getBundleName();
        this.abilityName = elementName.getAbilityName();
    }

    public boolean marshalling(Parcel parcel) {
        return parcel != null && parcel.writeInt(1) && parcel.writeString(this.bundleName) && parcel.writeString(this.abilityName) && parcel.writeString(this.deviceId);
    }

    public String toString() {
        return "deviceId: " + this.deviceId + " bundleName: " + this.bundleName + " abilityName: " + this.abilityName;
    }
}
