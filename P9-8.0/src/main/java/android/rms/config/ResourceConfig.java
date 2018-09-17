package android.rms.config;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ResourceConfig implements Parcelable {
    public static final Creator<ResourceConfig> CREATOR = new Creator<ResourceConfig>() {
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

    public ResourceConfig(int resource_id, int resource_threshold, int resource_strategy, int resource_max_peroid, int loop_interval, String resource_name, int resource_normal_threshold, int resource_waring_threshold, int resource_urgent_threshold, int total_loop_interval) {
        this.resource_id = resource_id;
        this.resource_threshold = resource_threshold;
        this.resource_strategy = resource_strategy;
        this.resource_max_peroid = resource_max_peroid;
        this.loop_interval = loop_interval;
        this.resource_name = resource_name;
        this.resource_normal_threshold = resource_normal_threshold;
        this.resource_waring_threshold = resource_waring_threshold;
        this.resource_urgent_threshold = resource_urgent_threshold;
        this.total_loop_interval = total_loop_interval;
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

    public void setTotalLoopInterval(int total_loop_interval) {
        this.total_loop_interval = total_loop_interval;
    }

    public void setLoopInterval(int interval) {
        this.loop_interval = interval;
    }
}
