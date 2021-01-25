package ohos.security.permission;

import java.util.ArrayList;
import java.util.List;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class QueryPermissionUsedResult implements Sequenceable {
    private static final int DEFAULT_CAPACITY = 10;
    private long beginTimeMillis;
    private List<BundlePermissionUsedRecord> bundlePermissionUsedRecords = new ArrayList(10);
    private int code;
    private long endTimeMillis;

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(this.code);
        parcel.writeLong(this.beginTimeMillis);
        parcel.writeLong(this.endTimeMillis);
        parcel.writeInt(this.bundlePermissionUsedRecords.size());
        for (BundlePermissionUsedRecord bundlePermissionUsedRecord : this.bundlePermissionUsedRecords) {
            parcel.writeSequenceable(bundlePermissionUsedRecord);
        }
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.code = parcel.readInt();
        this.beginTimeMillis = parcel.readLong();
        this.endTimeMillis = parcel.readLong();
        int readInt = parcel.readInt();
        for (int i = 0; i < readInt; i++) {
            BundlePermissionUsedRecord bundlePermissionUsedRecord = new BundlePermissionUsedRecord();
            if (parcel.readSequenceable(bundlePermissionUsedRecord)) {
                this.bundlePermissionUsedRecords.add(bundlePermissionUsedRecord);
            }
        }
        return true;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int i) {
        this.code = i;
    }

    public long getBeginTimeMillis() {
        return this.beginTimeMillis;
    }

    public void setBeginTimeMillis(long j) {
        this.beginTimeMillis = j;
    }

    public long getEndTimeMillis() {
        return this.endTimeMillis;
    }

    public void setEndTimeMillis(long j) {
        this.endTimeMillis = j;
    }

    public List<BundlePermissionUsedRecord> getBundlePermissionUsedRecords() {
        return this.bundlePermissionUsedRecords;
    }

    public void setBundlePermissionUsedRecords(List<BundlePermissionUsedRecord> list) {
        this.bundlePermissionUsedRecords = list;
    }

    public String toString() {
        return "QueryPermissionUsedResult{code=" + this.code + ", beginTimeMillis=" + this.beginTimeMillis + ", endTimeMillis=" + this.endTimeMillis + ", bundlePermissionUsedRecords=" + this.bundlePermissionUsedRecords + '}';
    }
}
