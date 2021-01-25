package android.app.usage;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;
import java.util.Objects;

@SystemApi
public final class CacheQuotaHint implements Parcelable {
    public static final Parcelable.Creator<CacheQuotaHint> CREATOR = new Parcelable.Creator<CacheQuotaHint>() {
        /* class android.app.usage.CacheQuotaHint.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CacheQuotaHint createFromParcel(Parcel in) {
            return new Builder().setVolumeUuid(in.readString()).setUid(in.readInt()).setQuota(in.readLong()).setUsageStats((UsageStats) in.readParcelable(UsageStats.class.getClassLoader())).build();
        }

        @Override // android.os.Parcelable.Creator
        public CacheQuotaHint[] newArray(int size) {
            return new CacheQuotaHint[size];
        }
    };
    public static final long QUOTA_NOT_SET = -1;
    private final long mQuota;
    private final int mUid;
    private final UsageStats mUsageStats;
    private final String mUuid;

    public CacheQuotaHint(Builder builder) {
        this.mUuid = builder.mUuid;
        this.mUid = builder.mUid;
        this.mUsageStats = builder.mUsageStats;
        this.mQuota = builder.mQuota;
    }

    public String getVolumeUuid() {
        return this.mUuid;
    }

    public int getUid() {
        return this.mUid;
    }

    public long getQuota() {
        return this.mQuota;
    }

    public UsageStats getUsageStats() {
        return this.mUsageStats;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mUuid);
        dest.writeInt(this.mUid);
        dest.writeLong(this.mQuota);
        dest.writeParcelable(this.mUsageStats, 0);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        if (!(o instanceof CacheQuotaHint)) {
            return false;
        }
        CacheQuotaHint other = (CacheQuotaHint) o;
        if (!Objects.equals(this.mUuid, other.mUuid) || !Objects.equals(this.mUsageStats, other.mUsageStats) || this.mUid != other.mUid || this.mQuota != other.mQuota) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.mUuid, Integer.valueOf(this.mUid), this.mUsageStats, Long.valueOf(this.mQuota));
    }

    public static final class Builder {
        private long mQuota;
        private int mUid;
        private UsageStats mUsageStats;
        private String mUuid;

        public Builder() {
        }

        public Builder(CacheQuotaHint hint) {
            setVolumeUuid(hint.getVolumeUuid());
            setUid(hint.getUid());
            setUsageStats(hint.getUsageStats());
            setQuota(hint.getQuota());
        }

        public Builder setVolumeUuid(String uuid) {
            this.mUuid = uuid;
            return this;
        }

        public Builder setUid(int uid) {
            Preconditions.checkArgumentNonnegative(uid, "Proposed uid was negative.");
            this.mUid = uid;
            return this;
        }

        public Builder setUsageStats(UsageStats stats) {
            this.mUsageStats = stats;
            return this;
        }

        public Builder setQuota(long quota) {
            Preconditions.checkArgument(quota >= -1);
            this.mQuota = quota;
            return this;
        }

        public CacheQuotaHint build() {
            return new CacheQuotaHint(this);
        }
    }
}
