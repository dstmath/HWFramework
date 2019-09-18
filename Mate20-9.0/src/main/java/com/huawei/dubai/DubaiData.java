package com.huawei.dubai;

import android.os.Parcel;
import android.os.Parcelable;

public class DubaiData implements Parcelable {
    public static final Parcelable.Creator<DubaiData> CREATOR = new Parcelable.Creator<DubaiData>() {
        public DubaiData createFromParcel(Parcel source) {
            return new DubaiData(source);
        }

        public DubaiData[] newArray(int size) {
            return new DubaiData[size];
        }
    };
    private Parcel data = Parcel.obtain();

    public DubaiData() {
    }

    public DubaiData(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.appendFrom(this.data, 0, this.data.dataSize());
        dest.setDataPosition(0);
    }

    private void readFromParcel(Parcel in) {
        int position = in.dataPosition();
        this.data.appendFrom(in, position, in.dataSize() - position);
    }
}
