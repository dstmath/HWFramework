package ohos.nfc;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class ProfileParcel implements Sequenceable {
    private String[][] mProfiles;

    public ProfileParcel(String[]... strArr) {
        this.mProfiles = strArr;
    }

    public String[][] getProfiles() {
        return this.mProfiles;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        String[][] strArr = this.mProfiles;
        if (strArr == null || strArr.length == 0) {
            parcel.writeInt(0);
            return true;
        }
        int length = strArr.length;
        parcel.writeInt(length);
        for (int i = 0; i < length; i++) {
            parcel.writeStringArray(this.mProfiles[i]);
        }
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        int readInt = parcel.readInt();
        if (readInt == 0) {
            return true;
        }
        String[][] strArr = new String[readInt][];
        for (int i = 0; i < readInt; i++) {
            strArr[i] = parcel.readStringArray();
        }
        this.mProfiles = strArr;
        return true;
    }
}
