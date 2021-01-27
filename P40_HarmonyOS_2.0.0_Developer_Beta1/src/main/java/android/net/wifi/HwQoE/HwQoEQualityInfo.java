package android.net.wifi.HwQoE;

import android.os.Parcel;
import android.os.Parcelable;

public class HwQoEQualityInfo implements Parcelable {
    public static final Parcelable.Creator<HwQoEQualityInfo> CREATOR = new Parcelable.Creator<HwQoEQualityInfo>() {
        /* class android.net.wifi.HwQoE.HwQoEQualityInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwQoEQualityInfo createFromParcel(Parcel source) {
            return new HwQoEQualityInfo(source.readLong(), source.readLong(), source.readLong(), source.readInt());
        }

        @Override // android.os.Parcelable.Creator
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

    public HwQoEQualityInfo() {
    }

    public HwQoEQualityInfo(long speed, long rtt, long otaRtt, int otalossrate) {
        this.mSpeed = speed;
        this.mRtt = rtt;
        this.mOtaRtt = otaRtt;
        this.mOtaLossRate = otalossrate;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mSpeed);
        dest.writeLong(this.mRtt);
        dest.writeLong(this.mOtaRtt);
        dest.writeInt(this.mOtaLossRate);
    }
}
