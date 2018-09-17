package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class WifiChannel implements Parcelable {
    public static final Creator<WifiChannel> CREATOR = new Creator<WifiChannel>() {
        public WifiChannel createFromParcel(Parcel in) {
            boolean z = false;
            WifiChannel channel = new WifiChannel();
            channel.freqMHz = in.readInt();
            channel.channelNum = in.readInt();
            if (in.readInt() != 0) {
                z = true;
            }
            channel.isDFS = z;
            return channel;
        }

        public WifiChannel[] newArray(int size) {
            return new WifiChannel[size];
        }
    };
    private static final int MAX_CHANNEL_NUM = 196;
    private static final int MAX_FREQ_MHZ = 5825;
    private static final int MIN_CHANNEL_NUM = 1;
    private static final int MIN_FREQ_MHZ = 2412;
    public int channelNum;
    public int freqMHz;
    public boolean isDFS;

    /* JADX WARNING: Missing block: B:4:0x000e, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isValid() {
        return this.freqMHz >= MIN_FREQ_MHZ && this.freqMHz <= MAX_FREQ_MHZ && this.channelNum >= 1 && this.channelNum <= 196;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.freqMHz);
        out.writeInt(this.channelNum);
        out.writeInt(this.isDFS ? 1 : 0);
    }
}
