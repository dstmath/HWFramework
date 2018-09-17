package android.media;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;

public final class MediaRouterClientState implements Parcelable {
    public static final Creator<MediaRouterClientState> CREATOR = new Creator<MediaRouterClientState>() {
        public MediaRouterClientState createFromParcel(Parcel in) {
            return new MediaRouterClientState(in);
        }

        public MediaRouterClientState[] newArray(int size) {
            return new MediaRouterClientState[size];
        }
    };
    public String globallySelectedRouteId;
    public final ArrayList<RouteInfo> routes;

    public static final class RouteInfo implements Parcelable {
        public static final Creator<RouteInfo> CREATOR = new Creator<RouteInfo>() {
            public RouteInfo createFromParcel(Parcel in) {
                return new RouteInfo(in);
            }

            public RouteInfo[] newArray(int size) {
                return new RouteInfo[size];
            }
        };
        public String description;
        public int deviceType;
        public boolean enabled;
        public String id;
        public String name;
        public int playbackStream;
        public int playbackType;
        public int presentationDisplayId;
        public int statusCode;
        public int supportedTypes;
        public int volume;
        public int volumeHandling;
        public int volumeMax;

        public RouteInfo(String id) {
            this.id = id;
            this.enabled = true;
            this.statusCode = 0;
            this.playbackType = 1;
            this.playbackStream = -1;
            this.volumeHandling = 0;
            this.presentationDisplayId = -1;
            this.deviceType = 0;
        }

        public RouteInfo(RouteInfo other) {
            this.id = other.id;
            this.name = other.name;
            this.description = other.description;
            this.supportedTypes = other.supportedTypes;
            this.enabled = other.enabled;
            this.statusCode = other.statusCode;
            this.playbackType = other.playbackType;
            this.playbackStream = other.playbackStream;
            this.volume = other.volume;
            this.volumeMax = other.volumeMax;
            this.volumeHandling = other.volumeHandling;
            this.presentationDisplayId = other.presentationDisplayId;
            this.deviceType = other.deviceType;
        }

        RouteInfo(Parcel in) {
            boolean z = false;
            this.id = in.readString();
            this.name = in.readString();
            this.description = in.readString();
            this.supportedTypes = in.readInt();
            if (in.readInt() != 0) {
                z = true;
            }
            this.enabled = z;
            this.statusCode = in.readInt();
            this.playbackType = in.readInt();
            this.playbackStream = in.readInt();
            this.volume = in.readInt();
            this.volumeMax = in.readInt();
            this.volumeHandling = in.readInt();
            this.presentationDisplayId = in.readInt();
            this.deviceType = in.readInt();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.id);
            dest.writeString(this.name);
            dest.writeString(this.description);
            dest.writeInt(this.supportedTypes);
            dest.writeInt(this.enabled ? 1 : 0);
            dest.writeInt(this.statusCode);
            dest.writeInt(this.playbackType);
            dest.writeInt(this.playbackStream);
            dest.writeInt(this.volume);
            dest.writeInt(this.volumeMax);
            dest.writeInt(this.volumeHandling);
            dest.writeInt(this.presentationDisplayId);
            dest.writeInt(this.deviceType);
        }

        public String toString() {
            return "RouteInfo{ id=" + this.id + ", name=" + this.name + ", description=" + this.description + ", supportedTypes=0x" + Integer.toHexString(this.supportedTypes) + ", enabled=" + this.enabled + ", statusCode=" + this.statusCode + ", playbackType=" + this.playbackType + ", playbackStream=" + this.playbackStream + ", volume=" + this.volume + ", volumeMax=" + this.volumeMax + ", volumeHandling=" + this.volumeHandling + ", presentationDisplayId=" + this.presentationDisplayId + ", deviceType=" + this.deviceType + " }";
        }
    }

    public MediaRouterClientState() {
        this.routes = new ArrayList();
    }

    MediaRouterClientState(Parcel src) {
        this.routes = src.createTypedArrayList(RouteInfo.CREATOR);
        this.globallySelectedRouteId = src.readString();
    }

    public RouteInfo getRoute(String id) {
        int count = this.routes.size();
        for (int i = 0; i < count; i++) {
            RouteInfo route = (RouteInfo) this.routes.get(i);
            if (route.id.equals(id)) {
                return route;
            }
        }
        return null;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.routes);
        dest.writeString(this.globallySelectedRouteId);
    }

    public String toString() {
        return "MediaRouterClientState{ globallySelectedRouteId=" + this.globallySelectedRouteId + ", routes=" + this.routes.toString() + " }";
    }
}
