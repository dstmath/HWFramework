package android.media;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.util.ArrayList;

public final class RemoteDisplayState implements Parcelable {
    public static final Parcelable.Creator<RemoteDisplayState> CREATOR = new Parcelable.Creator<RemoteDisplayState>() {
        /* class android.media.RemoteDisplayState.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RemoteDisplayState createFromParcel(Parcel in) {
            return new RemoteDisplayState(in);
        }

        @Override // android.os.Parcelable.Creator
        public RemoteDisplayState[] newArray(int size) {
            return new RemoteDisplayState[size];
        }
    };
    public static final int DISCOVERY_MODE_ACTIVE = 2;
    public static final int DISCOVERY_MODE_NONE = 0;
    public static final int DISCOVERY_MODE_PASSIVE = 1;
    public static final String SERVICE_INTERFACE = "com.android.media.remotedisplay.RemoteDisplayProvider";
    @UnsupportedAppUsage
    public final ArrayList<RemoteDisplayInfo> displays;

    @UnsupportedAppUsage
    public RemoteDisplayState() {
        this.displays = new ArrayList<>();
    }

    RemoteDisplayState(Parcel src) {
        this.displays = src.createTypedArrayList(RemoteDisplayInfo.CREATOR);
    }

    public boolean isValid() {
        ArrayList<RemoteDisplayInfo> arrayList = this.displays;
        if (arrayList == null) {
            return false;
        }
        int count = arrayList.size();
        for (int i = 0; i < count; i++) {
            if (!this.displays.get(i).isValid()) {
                return false;
            }
        }
        return true;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.displays);
    }

    public static final class RemoteDisplayInfo implements Parcelable {
        public static final Parcelable.Creator<RemoteDisplayInfo> CREATOR = new Parcelable.Creator<RemoteDisplayInfo>() {
            /* class android.media.RemoteDisplayState.RemoteDisplayInfo.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public RemoteDisplayInfo createFromParcel(Parcel in) {
                return new RemoteDisplayInfo(in);
            }

            @Override // android.os.Parcelable.Creator
            public RemoteDisplayInfo[] newArray(int size) {
                return new RemoteDisplayInfo[size];
            }
        };
        public static final int PLAYBACK_VOLUME_FIXED = 0;
        public static final int PLAYBACK_VOLUME_VARIABLE = 1;
        public static final int STATUS_AVAILABLE = 2;
        public static final int STATUS_CONNECTED = 4;
        public static final int STATUS_CONNECTING = 3;
        public static final int STATUS_IN_USE = 1;
        public static final int STATUS_NOT_AVAILABLE = 0;
        public String description;
        public String id;
        public String name;
        public int presentationDisplayId;
        public int status;
        public int volume;
        public int volumeHandling;
        public int volumeMax;

        public RemoteDisplayInfo(String id2) {
            this.id = id2;
            this.status = 0;
            this.volumeHandling = 0;
            this.presentationDisplayId = -1;
        }

        public RemoteDisplayInfo(RemoteDisplayInfo other) {
            this.id = other.id;
            this.name = other.name;
            this.description = other.description;
            this.status = other.status;
            this.volume = other.volume;
            this.volumeMax = other.volumeMax;
            this.volumeHandling = other.volumeHandling;
            this.presentationDisplayId = other.presentationDisplayId;
        }

        RemoteDisplayInfo(Parcel in) {
            this.id = in.readString();
            this.name = in.readString();
            this.description = in.readString();
            this.status = in.readInt();
            this.volume = in.readInt();
            this.volumeMax = in.readInt();
            this.volumeHandling = in.readInt();
            this.presentationDisplayId = in.readInt();
        }

        public boolean isValid() {
            return !TextUtils.isEmpty(this.id) && !TextUtils.isEmpty(this.name);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.id);
            dest.writeString(this.name);
            dest.writeString(this.description);
            dest.writeInt(this.status);
            dest.writeInt(this.volume);
            dest.writeInt(this.volumeMax);
            dest.writeInt(this.volumeHandling);
            dest.writeInt(this.presentationDisplayId);
        }

        public String toString() {
            return "RemoteDisplayInfo{ id=" + this.id + ", name=" + this.name + ", description=" + this.description + ", status=" + this.status + ", volume=" + this.volume + ", volumeMax=" + this.volumeMax + ", volumeHandling=" + this.volumeHandling + ", presentationDisplayId=" + this.presentationDisplayId + " }";
        }
    }
}
