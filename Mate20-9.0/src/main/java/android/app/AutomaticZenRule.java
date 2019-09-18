package android.app;

import android.content.ComponentName;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public final class AutomaticZenRule implements Parcelable {
    public static final Parcelable.Creator<AutomaticZenRule> CREATOR = new Parcelable.Creator<AutomaticZenRule>() {
        public AutomaticZenRule createFromParcel(Parcel source) {
            return new AutomaticZenRule(source);
        }

        public AutomaticZenRule[] newArray(int size) {
            return new AutomaticZenRule[size];
        }
    };
    private Uri conditionId;
    private long creationTime;
    private boolean enabled;
    private int interruptionFilter;
    private String name;
    private ComponentName owner;

    public AutomaticZenRule(String name2, ComponentName owner2, Uri conditionId2, int interruptionFilter2, boolean enabled2) {
        this.enabled = false;
        this.name = name2;
        this.owner = owner2;
        this.conditionId = conditionId2;
        this.interruptionFilter = interruptionFilter2;
        this.enabled = enabled2;
    }

    public AutomaticZenRule(String name2, ComponentName owner2, Uri conditionId2, int interruptionFilter2, boolean enabled2, long creationTime2) {
        this(name2, owner2, conditionId2, interruptionFilter2, enabled2);
        this.creationTime = creationTime2;
    }

    public AutomaticZenRule(Parcel source) {
        boolean z = false;
        this.enabled = false;
        this.enabled = source.readInt() == 1 ? true : z;
        if (source.readInt() == 1) {
            this.name = source.readString();
        }
        this.interruptionFilter = source.readInt();
        this.conditionId = (Uri) source.readParcelable(null);
        this.owner = (ComponentName) source.readParcelable(null);
        this.creationTime = source.readLong();
    }

    public ComponentName getOwner() {
        return this.owner;
    }

    public Uri getConditionId() {
        return this.conditionId;
    }

    public int getInterruptionFilter() {
        return this.interruptionFilter;
    }

    public String getName() {
        return this.name;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    public void setConditionId(Uri conditionId2) {
        this.conditionId = conditionId2;
    }

    public void setInterruptionFilter(int interruptionFilter2) {
        this.interruptionFilter = interruptionFilter2;
    }

    public void setName(String name2) {
        this.name = name2;
    }

    public void setEnabled(boolean enabled2) {
        this.enabled = enabled2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.enabled ? 1 : 0);
        if (this.name != null) {
            dest.writeInt(1);
            dest.writeString(this.name);
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.interruptionFilter);
        dest.writeParcelable(this.conditionId, 0);
        dest.writeParcelable(this.owner, 0);
        dest.writeLong(this.creationTime);
    }

    public String toString() {
        return AutomaticZenRule.class.getSimpleName() + '[' + "enabled=" + this.enabled + ",name=" + this.name + ",interruptionFilter=" + this.interruptionFilter + ",conditionId=" + this.conditionId + ",owner=" + this.owner + ",creationTime=" + this.creationTime + ']';
    }

    public boolean equals(Object o) {
        if (!(o instanceof AutomaticZenRule)) {
            return false;
        }
        boolean z = true;
        if (o == this) {
            return true;
        }
        AutomaticZenRule other = (AutomaticZenRule) o;
        if (other.enabled != this.enabled || !Objects.equals(other.name, this.name) || other.interruptionFilter != this.interruptionFilter || !Objects.equals(other.conditionId, this.conditionId) || !Objects.equals(other.owner, this.owner) || other.creationTime != this.creationTime) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Boolean.valueOf(this.enabled), this.name, Integer.valueOf(this.interruptionFilter), this.conditionId, this.owner, Long.valueOf(this.creationTime)});
    }
}
