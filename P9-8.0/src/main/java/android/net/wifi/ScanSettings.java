package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.Collection;

public class ScanSettings implements Parcelable {
    public static final Creator<ScanSettings> CREATOR = new Creator<ScanSettings>() {
        public ScanSettings createFromParcel(Parcel in) {
            ScanSettings settings = new ScanSettings();
            int size = in.readInt();
            if (size > 0) {
                settings.channelSet = new ArrayList(size);
                while (true) {
                    int size2 = size;
                    size = size2 - 1;
                    if (size2 <= 0) {
                        break;
                    }
                    settings.channelSet.add((WifiChannel) WifiChannel.CREATOR.createFromParcel(in));
                }
            }
            return settings;
        }

        public ScanSettings[] newArray(int size) {
            return new ScanSettings[size];
        }
    };
    public Collection<WifiChannel> channelSet;

    public ScanSettings(ScanSettings source) {
        if (source.channelSet != null) {
            this.channelSet = new ArrayList(source.channelSet);
        }
    }

    public boolean isValid() {
        for (WifiChannel channel : this.channelSet) {
            if (!channel.isValid()) {
                return false;
            }
        }
        return true;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.channelSet == null ? 0 : this.channelSet.size());
        if (this.channelSet != null) {
            for (WifiChannel channel : this.channelSet) {
                channel.writeToParcel(out, flags);
            }
        }
    }
}
