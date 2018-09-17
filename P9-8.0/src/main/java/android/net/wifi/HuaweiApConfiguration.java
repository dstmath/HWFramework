package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class HuaweiApConfiguration implements Parcelable {
    public static final Creator<HuaweiApConfiguration> CREATOR = new Creator<HuaweiApConfiguration>() {
        public HuaweiApConfiguration createFromParcel(Parcel in) {
            HuaweiApConfiguration config = new HuaweiApConfiguration();
            config.channel = in.readInt();
            config.channel = in.readInt();
            return config;
        }

        public HuaweiApConfiguration[] newArray(int size) {
            return new HuaweiApConfiguration[size];
        }
    };
    public int channel;
    public int maxScb;

    public HuaweiApConfiguration() {
        this.channel = 0;
        this.maxScb = 8;
    }

    public int describeContents() {
        return 0;
    }

    public HuaweiApConfiguration(HuaweiApConfiguration source) {
        if (source != null) {
            this.channel = source.channel;
            this.maxScb = source.maxScb;
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.channel);
        dest.writeInt(this.maxScb);
    }
}
