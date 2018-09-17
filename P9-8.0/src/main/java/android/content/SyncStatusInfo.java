package android.content;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.ArrayList;

public class SyncStatusInfo implements Parcelable {
    public static final Creator<SyncStatusInfo> CREATOR = new Creator<SyncStatusInfo>() {
        public SyncStatusInfo createFromParcel(Parcel in) {
            return new SyncStatusInfo(in);
        }

        public SyncStatusInfo[] newArray(int size) {
            return new SyncStatusInfo[size];
        }
    };
    private static final String TAG = "Sync";
    static final int VERSION = 2;
    public final int authorityId;
    public long initialFailureTime;
    public boolean initialize;
    public String lastFailureMesg;
    public int lastFailureSource;
    public long lastFailureTime;
    public int lastSuccessSource;
    public long lastSuccessTime;
    public int numSourceLocal;
    public int numSourcePeriodic;
    public int numSourcePoll;
    public int numSourceServer;
    public int numSourceUser;
    public int numSyncs;
    public boolean pending;
    private ArrayList<Long> periodicSyncTimes;
    public long totalElapsedTime;

    public SyncStatusInfo(int authorityId) {
        this.authorityId = authorityId;
    }

    public int getLastFailureMesgAsInt(int def) {
        int i = ContentResolver.syncErrorStringToInt(this.lastFailureMesg);
        if (i > 0) {
            return i;
        }
        Log.d(TAG, "Unknown lastFailureMesg:" + this.lastFailureMesg);
        return def;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        int i;
        int i2 = 1;
        parcel.writeInt(2);
        parcel.writeInt(this.authorityId);
        parcel.writeLong(this.totalElapsedTime);
        parcel.writeInt(this.numSyncs);
        parcel.writeInt(this.numSourcePoll);
        parcel.writeInt(this.numSourceServer);
        parcel.writeInt(this.numSourceLocal);
        parcel.writeInt(this.numSourceUser);
        parcel.writeLong(this.lastSuccessTime);
        parcel.writeInt(this.lastSuccessSource);
        parcel.writeLong(this.lastFailureTime);
        parcel.writeInt(this.lastFailureSource);
        parcel.writeString(this.lastFailureMesg);
        parcel.writeLong(this.initialFailureTime);
        if (this.pending) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (!this.initialize) {
            i2 = 0;
        }
        parcel.writeInt(i2);
        if (this.periodicSyncTimes != null) {
            parcel.writeInt(this.periodicSyncTimes.size());
            for (Long longValue : this.periodicSyncTimes) {
                parcel.writeLong(longValue.longValue());
            }
            return;
        }
        parcel.writeInt(-1);
    }

    public SyncStatusInfo(Parcel parcel) {
        boolean z = false;
        int version = parcel.readInt();
        if (!(version == 2 || version == 1)) {
            Log.w("SyncStatusInfo", "Unknown version: " + version);
        }
        this.authorityId = parcel.readInt();
        this.totalElapsedTime = parcel.readLong();
        this.numSyncs = parcel.readInt();
        this.numSourcePoll = parcel.readInt();
        this.numSourceServer = parcel.readInt();
        this.numSourceLocal = parcel.readInt();
        this.numSourceUser = parcel.readInt();
        this.lastSuccessTime = parcel.readLong();
        this.lastSuccessSource = parcel.readInt();
        this.lastFailureTime = parcel.readLong();
        this.lastFailureSource = parcel.readInt();
        this.lastFailureMesg = parcel.readString();
        this.initialFailureTime = parcel.readLong();
        this.pending = parcel.readInt() != 0;
        if (parcel.readInt() != 0) {
            z = true;
        }
        this.initialize = z;
        if (version == 1) {
            this.periodicSyncTimes = null;
            return;
        }
        int N = parcel.readInt();
        if (N < 0) {
            this.periodicSyncTimes = null;
            return;
        }
        this.periodicSyncTimes = new ArrayList();
        for (int i = 0; i < N; i++) {
            this.periodicSyncTimes.add(Long.valueOf(parcel.readLong()));
        }
    }

    public SyncStatusInfo(SyncStatusInfo other) {
        this.authorityId = other.authorityId;
        this.totalElapsedTime = other.totalElapsedTime;
        this.numSyncs = other.numSyncs;
        this.numSourcePoll = other.numSourcePoll;
        this.numSourceServer = other.numSourceServer;
        this.numSourceLocal = other.numSourceLocal;
        this.numSourceUser = other.numSourceUser;
        this.numSourcePeriodic = other.numSourcePeriodic;
        this.lastSuccessTime = other.lastSuccessTime;
        this.lastSuccessSource = other.lastSuccessSource;
        this.lastFailureTime = other.lastFailureTime;
        this.lastFailureSource = other.lastFailureSource;
        this.lastFailureMesg = other.lastFailureMesg;
        this.initialFailureTime = other.initialFailureTime;
        this.pending = other.pending;
        this.initialize = other.initialize;
        if (other.periodicSyncTimes != null) {
            this.periodicSyncTimes = new ArrayList(other.periodicSyncTimes);
        }
    }

    public void setPeriodicSyncTime(int index, long when) {
        ensurePeriodicSyncTimeSize(index);
        this.periodicSyncTimes.set(index, Long.valueOf(when));
    }

    public long getPeriodicSyncTime(int index) {
        if (this.periodicSyncTimes == null || index >= this.periodicSyncTimes.size()) {
            return 0;
        }
        return ((Long) this.periodicSyncTimes.get(index)).longValue();
    }

    public void removePeriodicSyncTime(int index) {
        if (this.periodicSyncTimes != null && index < this.periodicSyncTimes.size()) {
            this.periodicSyncTimes.remove(index);
        }
    }

    private void ensurePeriodicSyncTimeSize(int index) {
        if (this.periodicSyncTimes == null) {
            this.periodicSyncTimes = new ArrayList(0);
        }
        int requiredSize = index + 1;
        if (this.periodicSyncTimes.size() < requiredSize) {
            for (int i = this.periodicSyncTimes.size(); i < requiredSize; i++) {
                this.periodicSyncTimes.add(Long.valueOf(0));
            }
        }
    }
}
