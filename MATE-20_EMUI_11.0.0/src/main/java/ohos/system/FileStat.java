package ohos.system;

import libcore.util.Objects;

public final class FileStat {
    public final long blockCount;
    public final long blockSize;
    public final long devID;
    public final long fileID;
    public final int fileMode;
    public final long fileSize;
    public final int groupID;
    public final long hardLinkCount;
    public final long rDevID;
    public final long secsOfLastAccess;
    public final long secsOfLastChange;
    public final long secsOfLastModify;
    public final TimeSpecGroup timeGroupOfLastAccess;
    public final TimeSpecGroup timeGroupOfLastChange;
    public final TimeSpecGroup timeGroupOfLastModify;
    public final int userID;

    public FileStat(long j, long j2, int i, long j3, int i2, int i3, long j4, long j5, long j6, long j7, long j8, long j9, long j10) {
        this(j, j2, i, j3, i2, i3, j4, j5, new TimeSpecGroup(j6, 0), new TimeSpecGroup(j7, 0), new TimeSpecGroup(j8, 0), j9, j10);
    }

    public FileStat(long j, long j2, int i, long j3, int i2, int i3, long j4, long j5, TimeSpecGroup timeSpecGroup, TimeSpecGroup timeSpecGroup2, TimeSpecGroup timeSpecGroup3, long j6, long j7) {
        this.devID = j;
        this.fileID = j2;
        this.fileMode = i;
        this.hardLinkCount = j3;
        this.userID = i2;
        this.groupID = i3;
        this.rDevID = j4;
        this.fileSize = j5;
        this.secsOfLastAccess = timeSpecGroup.secTime;
        this.secsOfLastModify = timeSpecGroup2.secTime;
        this.secsOfLastChange = timeSpecGroup3.secTime;
        this.timeGroupOfLastAccess = timeSpecGroup;
        this.timeGroupOfLastModify = timeSpecGroup2;
        this.timeGroupOfLastChange = timeSpecGroup3;
        this.blockSize = j6;
        this.blockCount = j7;
    }

    public String toString() {
        return Objects.toString(this);
    }
}
