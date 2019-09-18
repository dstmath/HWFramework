package android.rms.config;

import android.os.Parcel;
import android.os.Parcelable;

public class ResourceConfig implements Parcelable {
    public static final Parcelable.Creator<ResourceConfig> CREATOR = new Parcelable.Creator<ResourceConfig>() {
        public ResourceConfig createFromParcel(Parcel source) {
            return new ResourceConfig(source);
        }

        public ResourceConfig[] newArray(int size) {
            return new ResourceConfig[size];
        }
    };
    private int loop_interval = 0;
    private int resource_id = 0;
    private int resource_max_peroid = 0;
    private String resource_name = "";
    private int resource_normal_threshold = 0;
    private int resource_strategy = 0;
    private int resource_threshold = 0;
    private int resource_urgent_threshold = 0;
    private int resource_waring_threshold = 0;
    private int total_loop_interval = 0;

    public ResourceConfig(int resource_id2, int resource_threshold2, int resource_strategy2, int resource_max_peroid2, int loop_interval2, String resource_name2, int resource_normal_threshold2, int resource_waring_threshold2, int resource_urgent_threshold2, int total_loop_interval2) {
        this.resource_id = resource_id2;
        this.resource_threshold = resource_threshold2;
        this.resource_strategy = resource_strategy2;
        this.resource_max_peroid = resource_max_peroid2;
        this.loop_interval = loop_interval2;
        this.resource_name = resource_name2;
        this.resource_normal_threshold = resource_normal_threshold2;
        this.resource_waring_threshold = resource_waring_threshold2;
        this.resource_urgent_threshold = resource_urgent_threshold2;
        this.total_loop_interval = total_loop_interval2;
    }

    public ResourceConfig(Parcel source) {
        this.resource_id = source.readInt();
        this.resource_threshold = source.readInt();
        this.resource_strategy = source.readInt();
        this.resource_max_peroid = source.readInt();
        this.loop_interval = source.readInt();
        this.resource_name = source.readString();
        this.resource_normal_threshold = source.readInt();
        this.resource_waring_threshold = source.readInt();
        this.resource_urgent_threshold = source.readInt();
        this.total_loop_interval = source.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.resource_id);
        dest.writeInt(this.resource_threshold);
        dest.writeInt(this.resource_strategy);
        dest.writeInt(this.resource_max_peroid);
        dest.writeInt(this.loop_interval);
        dest.writeString(this.resource_name);
        dest.writeInt(this.resource_normal_threshold);
        dest.writeInt(this.resource_waring_threshold);
        dest.writeInt(this.resource_urgent_threshold);
        dest.writeInt(this.total_loop_interval);
    }

    public int getResourceID() {
        return this.resource_id;
    }

    public int getResourceThreshold() {
        return this.resource_threshold;
    }

    public int getResourceStrategy() {
        return this.resource_strategy;
    }

    public int getResourceMaxPeroid() {
        return this.resource_max_peroid;
    }

    public int getTotalLoopInterval() {
        return this.total_loop_interval;
    }

    public int getLoopInterval() {
        return this.loop_interval;
    }

    public String getResouceName() {
        return this.resource_name;
    }

    public int getResouceNormalThreshold() {
        return this.resource_normal_threshold;
    }

    public int getResouceWarningThreshold() {
        return this.resource_waring_threshold;
    }

    public int getResouceUrgentThreshold() {
        return this.resource_urgent_threshold;
    }

    public void setResourceID(int id) {
        this.resource_id = id;
    }

    public void setResourceThreshold(int threshold) {
        this.resource_threshold = threshold;
    }

    public void setResourceStrategy(int strategy) {
        this.resource_strategy = strategy;
    }

    public void setResourceMaxPeroid(int max_peroid) {
        this.resource_max_peroid = max_peroid;
    }

    public void setTotalLoopInterval(int total_loop_interval2) {
        this.total_loop_interval = total_loop_interval2;
    }

    public void setLoopInterval(int interval) {
        this.loop_interval = interval;
    }
}
