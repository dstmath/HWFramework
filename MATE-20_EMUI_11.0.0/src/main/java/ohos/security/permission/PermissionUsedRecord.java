package ohos.security.permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class PermissionUsedRecord implements Sequenceable {
    private static final int DEFAULT_CAPACITY = 10;
    private int accessCountBg;
    private int accessCountFg;
    private List<Long> accessRecordBg = new ArrayList(10);
    private List<Long> accessRecordFg = new ArrayList(10);
    private long lastAccessTime;
    private long lastRejectTime;
    private String permissionName;
    private int rejectCountBg;
    private int rejectCountFg;
    private List<Long> rejectRecordBg = new ArrayList(10);
    private List<Long> rejectRecordFg = new ArrayList(10);

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeString(this.permissionName);
        parcel.writeInt(this.accessCountFg);
        parcel.writeInt(this.rejectCountFg);
        parcel.writeInt(this.accessCountBg);
        parcel.writeInt(this.rejectCountBg);
        parcel.writeLong(this.lastAccessTime);
        parcel.writeLong(this.lastRejectTime);
        parcel.writeLongArray(this.accessRecordFg.stream().filter($$Lambda$PermissionUsedRecord$zE5AB7BzKQZTM2YCXMr_aZTWoC4.INSTANCE).mapToLong($$Lambda$PermissionUsedRecord$ELHKvd8JMVRD8rbALqYPKbDX2mM.INSTANCE).toArray());
        parcel.writeLongArray(this.rejectRecordFg.stream().filter($$Lambda$PermissionUsedRecord$zE5AB7BzKQZTM2YCXMr_aZTWoC4.INSTANCE).mapToLong($$Lambda$PermissionUsedRecord$ELHKvd8JMVRD8rbALqYPKbDX2mM.INSTANCE).toArray());
        parcel.writeLongArray(this.accessRecordBg.stream().filter($$Lambda$PermissionUsedRecord$zE5AB7BzKQZTM2YCXMr_aZTWoC4.INSTANCE).mapToLong($$Lambda$PermissionUsedRecord$ELHKvd8JMVRD8rbALqYPKbDX2mM.INSTANCE).toArray());
        parcel.writeLongArray(this.rejectRecordBg.stream().filter($$Lambda$PermissionUsedRecord$zE5AB7BzKQZTM2YCXMr_aZTWoC4.INSTANCE).mapToLong($$Lambda$PermissionUsedRecord$ELHKvd8JMVRD8rbALqYPKbDX2mM.INSTANCE).toArray());
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.permissionName = parcel.readString();
        this.accessCountFg = parcel.readInt();
        this.rejectCountFg = parcel.readInt();
        this.accessCountBg = parcel.readInt();
        this.rejectCountBg = parcel.readInt();
        this.lastAccessTime = parcel.readLong();
        this.lastRejectTime = parcel.readLong();
        this.accessRecordFg = (List) Arrays.stream(parcel.readLongArray()).boxed().collect(Collectors.toList());
        this.rejectRecordFg = (List) Arrays.stream(parcel.readLongArray()).boxed().collect(Collectors.toList());
        this.accessRecordBg = (List) Arrays.stream(parcel.readLongArray()).boxed().collect(Collectors.toList());
        this.rejectRecordBg = (List) Arrays.stream(parcel.readLongArray()).boxed().collect(Collectors.toList());
        return true;
    }

    public String getPermissionName() {
        return this.permissionName;
    }

    public void setPermissionName(String str) {
        this.permissionName = str;
    }

    public int getAccessCountFg() {
        return this.accessCountFg;
    }

    public void setAccessCountFg(int i) {
        this.accessCountFg = i;
    }

    public int getRejectCountFg() {
        return this.rejectCountFg;
    }

    public void setRejectCountFg(int i) {
        this.rejectCountFg = i;
    }

    public int getAccessCountBg() {
        return this.accessCountBg;
    }

    public void setAccessCountBg(int i) {
        this.accessCountBg = i;
    }

    public int getRejectCountBg() {
        return this.rejectCountBg;
    }

    public void setRejectCountBg(int i) {
        this.rejectCountBg = i;
    }

    public long getLastAccessTime() {
        return this.lastAccessTime;
    }

    public void setLastAccessTime(long j) {
        this.lastAccessTime = j;
    }

    public long getLastRejectTime() {
        return this.lastRejectTime;
    }

    public void setLastRejectTime(long j) {
        this.lastRejectTime = j;
    }

    public List<Long> getAccessRecordFg() {
        return this.accessRecordFg;
    }

    public void setAccessRecordFg(List<Long> list) {
        this.accessRecordFg = list;
    }

    public List<Long> getRejectRecordFg() {
        return this.rejectRecordFg;
    }

    public void setRejectRecordFg(List<Long> list) {
        this.rejectRecordFg = list;
    }

    public List<Long> getAccessRecordBg() {
        return this.accessRecordBg;
    }

    public void setAccessRecordBg(List<Long> list) {
        this.accessRecordBg = list;
    }

    public List<Long> getRejectRecordBg() {
        return this.rejectRecordBg;
    }

    public void setRejectRecordBg(List<Long> list) {
        this.rejectRecordBg = list;
    }

    public String toString() {
        return "PermissionUsedRecord{permissionName='" + this.permissionName + "', accessCountFg=" + this.accessCountFg + ", rejectCountFg=" + this.rejectCountFg + ", accessCountBg=" + this.accessCountBg + ", rejectCountBg=" + this.rejectCountBg + ", lastAccessTime=" + this.lastAccessTime + ", lastRejectTime=" + this.lastRejectTime + ", accessRecordFg=" + this.accessRecordFg + ", rejectRecordFg=" + this.rejectRecordFg + ", accessRecordBg=" + this.accessRecordBg + ", rejectRecordBg=" + this.rejectRecordBg + '}';
    }
}
