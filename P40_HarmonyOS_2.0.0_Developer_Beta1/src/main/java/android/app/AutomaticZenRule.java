package android.app;

import android.content.ComponentName;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.ZenPolicy;
import java.util.Objects;

public final class AutomaticZenRule implements Parcelable {
    public static final Parcelable.Creator<AutomaticZenRule> CREATOR = new Parcelable.Creator<AutomaticZenRule>() {
        /* class android.app.AutomaticZenRule.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AutomaticZenRule createFromParcel(Parcel source) {
            return new AutomaticZenRule(source);
        }

        @Override // android.os.Parcelable.Creator
        public AutomaticZenRule[] newArray(int size) {
            return new AutomaticZenRule[size];
        }
    };
    private static final int DISABLED = 0;
    private static final int ENABLED = 1;
    private Uri conditionId;
    private ComponentName configurationActivity;
    private long creationTime;
    private boolean enabled;
    private int interruptionFilter;
    private boolean mModified;
    private ZenPolicy mZenPolicy;
    private String name;
    private ComponentName owner;

    @Deprecated
    public AutomaticZenRule(String name2, ComponentName owner2, Uri conditionId2, int interruptionFilter2, boolean enabled2) {
        this(name2, owner2, null, conditionId2, null, interruptionFilter2, enabled2);
    }

    public AutomaticZenRule(String name2, ComponentName owner2, ComponentName configurationActivity2, Uri conditionId2, ZenPolicy policy, int interruptionFilter2, boolean enabled2) {
        this.enabled = false;
        this.mModified = false;
        this.name = name2;
        this.owner = owner2;
        this.configurationActivity = configurationActivity2;
        this.conditionId = conditionId2;
        this.interruptionFilter = interruptionFilter2;
        this.enabled = enabled2;
        this.mZenPolicy = policy;
    }

    public AutomaticZenRule(String name2, ComponentName owner2, ComponentName configurationActivity2, Uri conditionId2, ZenPolicy policy, int interruptionFilter2, boolean enabled2, long creationTime2) {
        this(name2, owner2, configurationActivity2, conditionId2, policy, interruptionFilter2, enabled2);
        this.creationTime = creationTime2;
    }

    public AutomaticZenRule(Parcel source) {
        boolean z = false;
        this.enabled = false;
        this.mModified = false;
        this.enabled = source.readInt() == 1;
        if (source.readInt() == 1) {
            this.name = source.readString();
        }
        this.interruptionFilter = source.readInt();
        this.conditionId = (Uri) source.readParcelable(null);
        this.owner = (ComponentName) source.readParcelable(null);
        this.configurationActivity = (ComponentName) source.readParcelable(null);
        this.creationTime = source.readLong();
        this.mZenPolicy = (ZenPolicy) source.readParcelable(null);
        this.mModified = source.readInt() == 1 ? true : z;
    }

    public ComponentName getOwner() {
        return this.owner;
    }

    public ComponentName getConfigurationActivity() {
        return this.configurationActivity;
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

    public boolean isModified() {
        return this.mModified;
    }

    public ZenPolicy getZenPolicy() {
        ZenPolicy zenPolicy = this.mZenPolicy;
        if (zenPolicy == null) {
            return null;
        }
        return zenPolicy.copy();
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

    public void setModified(boolean modified) {
        this.mModified = modified;
    }

    public void setZenPolicy(ZenPolicy zenPolicy) {
        this.mZenPolicy = zenPolicy == null ? null : zenPolicy.copy();
    }

    public void setConfigurationActivity(ComponentName componentName) {
        this.configurationActivity = componentName;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
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
        dest.writeParcelable(this.configurationActivity, 0);
        dest.writeLong(this.creationTime);
        dest.writeParcelable(this.mZenPolicy, 0);
        dest.writeInt(this.mModified ? 1 : 0);
    }

    public String toString() {
        return AutomaticZenRule.class.getSimpleName() + "[enabled=" + this.enabled + ",name=" + this.name + ",interruptionFilter=" + this.interruptionFilter + ",conditionId=" + this.conditionId + ",owner=" + this.owner + ",configActivity=" + this.configurationActivity + ",creationTime=" + this.creationTime + ",mZenPolicy=" + this.mZenPolicy + ']';
    }

    public boolean equals(Object o) {
        if (!(o instanceof AutomaticZenRule)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        AutomaticZenRule other = (AutomaticZenRule) o;
        if (other.enabled != this.enabled || other.mModified != this.mModified || !Objects.equals(other.name, this.name) || other.interruptionFilter != this.interruptionFilter || !Objects.equals(other.conditionId, this.conditionId) || !Objects.equals(other.owner, this.owner) || !Objects.equals(other.mZenPolicy, this.mZenPolicy) || !Objects.equals(other.configurationActivity, this.configurationActivity) || other.creationTime != this.creationTime) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(Boolean.valueOf(this.enabled), this.name, Integer.valueOf(this.interruptionFilter), this.conditionId, this.owner, this.configurationActivity, this.mZenPolicy, Boolean.valueOf(this.mModified), Long.valueOf(this.creationTime));
    }
}
