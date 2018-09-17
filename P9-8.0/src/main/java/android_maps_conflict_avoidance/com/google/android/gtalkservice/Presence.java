package android_maps_conflict_avoidance.com.google.android.gtalkservice;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public final class Presence implements Parcelable {
    public static final Creator<Presence> CREATOR = new Creator<Presence>() {
        public Presence createFromParcel(Parcel source) {
            return new Presence(source);
        }

        public Presence[] newArray(int size) {
            return new Presence[size];
        }
    };
    public static final Presence OFFLINE = new Presence();
    private boolean mAllowInvisibility;
    private boolean mAvailable;
    private int mCapabilities;
    private List<String> mDefaultStatusList;
    private List<String> mDndStatusList;
    private boolean mInvisible;
    private Show mShow;
    private String mStatus;
    private int mStatusListContentsMax;
    private int mStatusListMax;
    private int mStatusMax;

    public enum Show {
        NONE,
        AWAY,
        EXTENDED_AWAY,
        DND,
        AVAILABLE
    }

    public Presence() {
        this(false, Show.NONE, null, 8);
    }

    public Presence(boolean available, Show show, String status, int caps) {
        this.mAvailable = available;
        this.mShow = show;
        this.mStatus = status;
        this.mInvisible = false;
        this.mDefaultStatusList = new ArrayList();
        this.mDndStatusList = new ArrayList();
        this.mCapabilities = caps;
    }

    public Presence(Parcel source) {
        boolean z;
        boolean z2 = true;
        setStatusMax(source.readInt());
        setStatusListMax(source.readInt());
        setStatusListContentsMax(source.readInt());
        setAllowInvisibility(source.readInt() != 0);
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        setAvailable(z);
        setShow((Show) Enum.valueOf(Show.class, source.readString()));
        this.mStatus = source.readString();
        if (source.readInt() == 0) {
            z2 = false;
        }
        setInvisible(z2);
        this.mDefaultStatusList = new ArrayList();
        source.readStringList(this.mDefaultStatusList);
        this.mDndStatusList = new ArrayList();
        source.readStringList(this.mDndStatusList);
        setCapabilities(source.readInt());
    }

    public int getStatusMax() {
        return this.mStatusMax;
    }

    public void setStatusMax(int max) {
        this.mStatusMax = max;
    }

    public int getStatusListMax() {
        return this.mStatusListMax;
    }

    public void setStatusListMax(int max) {
        this.mStatusListMax = max;
    }

    public int getStatusListContentsMax() {
        return this.mStatusListContentsMax;
    }

    public void setStatusListContentsMax(int max) {
        this.mStatusListContentsMax = max;
    }

    public boolean allowInvisibility() {
        return this.mAllowInvisibility;
    }

    public void setAllowInvisibility(boolean allowInvisibility) {
        this.mAllowInvisibility = allowInvisibility;
    }

    public boolean isAvailable() {
        return this.mAvailable;
    }

    public void setAvailable(boolean available) {
        this.mAvailable = available;
    }

    public boolean isInvisible() {
        return this.mInvisible;
    }

    public boolean setInvisible(boolean invisible) {
        this.mInvisible = invisible;
        if (!invisible || (allowInvisibility() ^ 1) == 0) {
            return true;
        }
        return false;
    }

    public void setShow(Show show) {
        this.mShow = show;
    }

    public int getCapabilities() {
        return this.mCapabilities;
    }

    public void setCapabilities(int capabilities) {
        this.mCapabilities = capabilities;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeInt(getStatusMax());
        dest.writeInt(getStatusListMax());
        dest.writeInt(getStatusListContentsMax());
        dest.writeInt(allowInvisibility() ? 1 : 0);
        if (this.mAvailable) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeString(this.mShow.toString());
        dest.writeString(this.mStatus);
        if (!this.mInvisible) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeStringList(this.mDefaultStatusList);
        dest.writeStringList(this.mDndStatusList);
        dest.writeInt(getCapabilities());
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        if (!isAvailable()) {
            return "UNAVAILABLE";
        }
        if (isInvisible()) {
            return "INVISIBLE";
        }
        StringBuilder sb = new StringBuilder(40);
        if (this.mShow == Show.NONE) {
            sb.append("AVAILABLE(x)");
        } else {
            sb.append(this.mShow.toString());
        }
        if ((this.mCapabilities & 8) != 0) {
            sb.append(" pmuc-v1");
        }
        if ((this.mCapabilities & 1) != 0) {
            sb.append(" voice-v1");
        }
        if ((this.mCapabilities & 2) != 0) {
            sb.append(" video-v1");
        }
        if ((this.mCapabilities & 4) != 0) {
            sb.append(" camera-v1");
        }
        return sb.toString();
    }
}
