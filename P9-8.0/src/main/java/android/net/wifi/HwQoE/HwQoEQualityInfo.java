package android.net.wifi.HwQoE;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class HwQoEQualityInfo implements Parcelable {
    public static final Creator<HwQoEQualityInfo> CREATOR = new Creator<HwQoEQualityInfo>() {
        public HwQoEQualityInfo createFromParcel(Parcel source) {
            return new HwQoEQualityInfo(source.readLong(), source.readLong(), source.readLong(), source.readInt());
        }

        public HwQoEQualityInfo[] newArray(int size) {
            return new HwQoEQualityInfo[size];
        }
    };
    public static int HWQOE_MONITOR_HAVE_INTERNET = 1;
    public static int HWQOE_MONITOR_NO_INTERNET = 0;
    public static int HWQOE_MONITOR_TYPE_CHR = 3;
    public static int HWQOE_MONITOR_TYPE_GAME = 4;
    public static int HWQOE_MONITOR_TYPE_VOWIFI = 1;
    public static int HWQOE_MONITOR_TYPE_WIFIPLUS = 2;
    public static int HWQOE_VOWIFI_CALL_BEING = 1;
    public static int HWQOE_VOWIFI_CALL_END = 2;
    public int mOtaLossRate;
    public long mOtaRtt;
    public long mRtt;
    public long mSpeed;

    public HwQoEQualityInfo(long speed, long rtt, long otartt, int otalossrate) {
        this.mSpeed = speed;
        this.mRtt = rtt;
        this.mOtaRtt = otartt;
        this.mOtaLossRate = otalossrate;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mSpeed);
        dest.writeLong(this.mRtt);
        dest.writeLong(this.mOtaRtt);
        dest.writeInt(this.mOtaLossRate);
    }
}
