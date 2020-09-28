package android.rms.config;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.uikit.effect.BuildConfig;

public class ResourceConfig implements Parcelable {
    public static final Parcelable.Creator<ResourceConfig> CREATOR = new Parcelable.Creator<ResourceConfig>() {
        /* class android.rms.config.ResourceConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ResourceConfig createFromParcel(Parcel source) {
            return new ResourceConfig(source);
        }

        @Override // android.os.Parcelable.Creator
        public ResourceConfig[] newArray(int size) {
            return new ResourceConfig[size];
        }
    };
    private int mLoopInterval = 0;
    private int mResourceId = 0;
    private int mResourceMaxPeroid = 0;
    private String mResourceName = BuildConfig.FLAVOR;
    private int mResourceNormalThreshold = 0;
    private int mResourceStrategy = 0;
    private int mResourceThreshold = 0;
    private int mResourceUrgentThreshold = 0;
    private int mResourceWaringThreshold = 0;
    private int mTotalLoopInterval = 0;

    public ResourceConfig(int resourceId, int resourceThreshold, int resourceStrategy, int resourceMaxPeroid, int loopInterval, String resourceName, int resourceNormalThreshold, int resourceWaringThreshold, int resourceUrgentThreshold, int totalLoopInterval) {
        this.mResourceId = resourceId;
        this.mResourceThreshold = resourceThreshold;
        this.mResourceStrategy = resourceStrategy;
        this.mResourceMaxPeroid = resourceMaxPeroid;
        this.mLoopInterval = loopInterval;
        this.mResourceName = resourceName;
        this.mResourceNormalThreshold = resourceNormalThreshold;
        this.mResourceWaringThreshold = resourceWaringThreshold;
        this.mResourceUrgentThreshold = resourceUrgentThreshold;
        this.mTotalLoopInterval = totalLoopInterval;
    }

    public ResourceConfig(Parcel source) {
        if (source != null) {
            this.mResourceId = source.readInt();
            this.mResourceThreshold = source.readInt();
            this.mResourceStrategy = source.readInt();
            this.mResourceMaxPeroid = source.readInt();
            this.mLoopInterval = source.readInt();
            this.mResourceName = source.readString();
            this.mResourceNormalThreshold = source.readInt();
            this.mResourceWaringThreshold = source.readInt();
            this.mResourceUrgentThreshold = source.readInt();
            this.mTotalLoopInterval = source.readInt();
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (dest != null) {
            dest.writeInt(this.mResourceId);
            dest.writeInt(this.mResourceThreshold);
            dest.writeInt(this.mResourceStrategy);
            dest.writeInt(this.mResourceMaxPeroid);
            dest.writeInt(this.mLoopInterval);
            dest.writeString(this.mResourceName);
            dest.writeInt(this.mResourceNormalThreshold);
            dest.writeInt(this.mResourceWaringThreshold);
            dest.writeInt(this.mResourceUrgentThreshold);
            dest.writeInt(this.mTotalLoopInterval);
        }
    }

    public int getResourceID() {
        return this.mResourceId;
    }

    public int getResourceThreshold() {
        return this.mResourceThreshold;
    }

    public int getResourceStrategy() {
        return this.mResourceStrategy;
    }

    public int getResourceMaxPeroid() {
        return this.mResourceMaxPeroid;
    }

    public int getTotalLoopInterval() {
        return this.mTotalLoopInterval;
    }

    public int getLoopInterval() {
        return this.mLoopInterval;
    }

    public String getResouceName() {
        return this.mResourceName;
    }

    public int getResouceNormalThreshold() {
        return this.mResourceNormalThreshold;
    }

    public int getResouceWarningThreshold() {
        return this.mResourceWaringThreshold;
    }

    public int getResouceUrgentThreshold() {
        return this.mResourceUrgentThreshold;
    }

    public void setResourceID(int id) {
        this.mResourceId = id;
    }

    public void setResourceThreshold(int threshold) {
        this.mResourceThreshold = threshold;
    }

    public void setResourceStrategy(int strategy) {
        this.mResourceStrategy = strategy;
    }

    public void setResourceMaxPeroid(int maxPeroid) {
        this.mResourceMaxPeroid = maxPeroid;
    }

    public void setTotalLoopInterval(int totalLoopInterval) {
        this.mTotalLoopInterval = totalLoopInterval;
    }

    public void setLoopInterval(int interval) {
        this.mLoopInterval = interval;
    }
}
