package android.hardware.radio;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@SystemApi
public final class Announcement implements Parcelable {
    public static final Parcelable.Creator<Announcement> CREATOR = new Parcelable.Creator<Announcement>() {
        public Announcement createFromParcel(Parcel in) {
            return new Announcement(in);
        }

        public Announcement[] newArray(int size) {
            return new Announcement[size];
        }
    };
    public static final int TYPE_EMERGENCY = 1;
    public static final int TYPE_EVENT = 6;
    public static final int TYPE_MISC = 8;
    public static final int TYPE_NEWS = 5;
    public static final int TYPE_SPORT = 7;
    public static final int TYPE_TRAFFIC = 3;
    public static final int TYPE_WARNING = 2;
    public static final int TYPE_WEATHER = 4;
    private final ProgramSelector mSelector;
    private final int mType;
    private final Map<String, String> mVendorInfo;

    public interface OnListUpdatedListener {
        void onListUpdated(Collection<Announcement> collection);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    public Announcement(ProgramSelector selector, int type, Map<String, String> vendorInfo) {
        this.mSelector = (ProgramSelector) Objects.requireNonNull(selector);
        this.mType = ((Integer) Objects.requireNonNull(Integer.valueOf(type))).intValue();
        this.mVendorInfo = (Map) Objects.requireNonNull(vendorInfo);
    }

    private Announcement(Parcel in) {
        this.mSelector = (ProgramSelector) in.readTypedObject(ProgramSelector.CREATOR);
        this.mType = in.readInt();
        this.mVendorInfo = Utils.readStringMap(in);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedObject(this.mSelector, 0);
        dest.writeInt(this.mType);
        Utils.writeStringMap(dest, this.mVendorInfo);
    }

    public int describeContents() {
        return 0;
    }

    public ProgramSelector getSelector() {
        return this.mSelector;
    }

    public int getType() {
        return this.mType;
    }

    public Map<String, String> getVendorInfo() {
        return this.mVendorInfo;
    }
}
