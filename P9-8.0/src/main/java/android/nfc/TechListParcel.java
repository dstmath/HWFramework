package android.nfc;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class TechListParcel implements Parcelable {
    public static final Creator<TechListParcel> CREATOR = new Creator<TechListParcel>() {
        public TechListParcel createFromParcel(Parcel source) {
            int count = source.readInt();
            String[][] techLists = new String[count][];
            for (int i = 0; i < count; i++) {
                techLists[i] = source.readStringArray();
            }
            return new TechListParcel(techLists);
        }

        public TechListParcel[] newArray(int size) {
            return new TechListParcel[size];
        }
    };
    private String[][] mTechLists;

    public TechListParcel(String[]... strings) {
        this.mTechLists = strings;
    }

    public String[][] getTechLists() {
        return this.mTechLists;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(count);
        for (String[] techList : this.mTechLists) {
            dest.writeStringArray(techList);
        }
    }
}
