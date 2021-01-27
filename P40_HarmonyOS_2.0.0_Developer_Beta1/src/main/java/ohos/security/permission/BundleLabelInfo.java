package ohos.security.permission;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class BundleLabelInfo implements Sequenceable {
    public String bundleLabel;
    public String bundleName;
    public String deviceLabel;
    public String deviceType;

    public BundleLabelInfo() {
    }

    public BundleLabelInfo(BundleLabelInfo bundleLabelInfo) {
        this.bundleLabel = bundleLabelInfo.bundleLabel;
        this.bundleName = bundleLabelInfo.bundleName;
        this.deviceLabel = bundleLabelInfo.deviceLabel;
        this.deviceType = bundleLabelInfo.deviceType;
    }

    public boolean valid() {
        return !isEmpty(this.bundleLabel) && !isEmpty(this.bundleName) && !isEmpty(this.deviceLabel) && !isEmpty(this.deviceType);
    }

    private boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        parcel.writeString(this.bundleLabel);
        parcel.writeString(this.bundleName);
        parcel.writeString(this.deviceLabel);
        parcel.writeString(this.deviceType);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        this.bundleLabel = parcel.readString();
        this.bundleName = parcel.readString();
        this.deviceLabel = parcel.readString();
        this.deviceType = parcel.readString();
        return true;
    }
}
