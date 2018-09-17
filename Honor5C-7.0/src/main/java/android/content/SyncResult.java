package android.content;

import android.app.backup.FullBackup;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class SyncResult implements Parcelable {
    public static final SyncResult ALREADY_IN_PROGRESS = null;
    public static final Creator<SyncResult> CREATOR = null;
    public boolean databaseError;
    public long delayUntil;
    public boolean fullSyncRequested;
    public boolean moreRecordsToGet;
    public boolean partialSyncUnavailable;
    public final SyncStats stats;
    public final boolean syncAlreadyInProgress;
    public boolean tooManyDeletions;
    public boolean tooManyRetries;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.SyncResult.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.SyncResult.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.SyncResult.<clinit>():void");
    }

    public SyncResult() {
        this(false);
    }

    private SyncResult(boolean syncAlreadyInProgress) {
        this.syncAlreadyInProgress = syncAlreadyInProgress;
        this.tooManyDeletions = false;
        this.tooManyRetries = false;
        this.fullSyncRequested = false;
        this.partialSyncUnavailable = false;
        this.moreRecordsToGet = false;
        this.delayUntil = 0;
        this.stats = new SyncStats();
    }

    private SyncResult(Parcel parcel) {
        boolean z;
        boolean z2 = true;
        if (parcel.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.syncAlreadyInProgress = z;
        if (parcel.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.tooManyDeletions = z;
        if (parcel.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.tooManyRetries = z;
        if (parcel.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.databaseError = z;
        if (parcel.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.fullSyncRequested = z;
        if (parcel.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.partialSyncUnavailable = z;
        if (parcel.readInt() == 0) {
            z2 = false;
        }
        this.moreRecordsToGet = z2;
        this.delayUntil = parcel.readLong();
        this.stats = new SyncStats(parcel);
    }

    public boolean hasHardError() {
        if (this.stats.numParseExceptions > 0 || this.stats.numConflictDetectedExceptions > 0 || this.stats.numAuthExceptions > 0 || this.tooManyDeletions || this.tooManyRetries) {
            return true;
        }
        return this.databaseError;
    }

    public boolean hasSoftError() {
        return this.syncAlreadyInProgress || this.stats.numIoExceptions > 0;
    }

    public boolean hasError() {
        return !hasSoftError() ? hasHardError() : true;
    }

    public boolean madeSomeProgress() {
        if ((this.stats.numDeletes <= 0 || this.tooManyDeletions) && this.stats.numInserts <= 0 && this.stats.numUpdates <= 0) {
            return false;
        }
        return true;
    }

    public void clear() {
        if (this.syncAlreadyInProgress) {
            throw new UnsupportedOperationException("you are not allowed to clear the ALREADY_IN_PROGRESS SyncStats");
        }
        this.tooManyDeletions = false;
        this.tooManyRetries = false;
        this.databaseError = false;
        this.fullSyncRequested = false;
        this.partialSyncUnavailable = false;
        this.moreRecordsToGet = false;
        this.delayUntil = 0;
        this.stats.clear();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        int i;
        int i2 = 1;
        if (this.syncAlreadyInProgress) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.tooManyDeletions) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.tooManyRetries) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.databaseError) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.fullSyncRequested) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.partialSyncUnavailable) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (!this.moreRecordsToGet) {
            i2 = 0;
        }
        parcel.writeInt(i2);
        parcel.writeLong(this.delayUntil);
        this.stats.writeToParcel(parcel, flags);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SyncResult:");
        if (this.syncAlreadyInProgress) {
            sb.append(" syncAlreadyInProgress: ").append(this.syncAlreadyInProgress);
        }
        if (this.tooManyDeletions) {
            sb.append(" tooManyDeletions: ").append(this.tooManyDeletions);
        }
        if (this.tooManyRetries) {
            sb.append(" tooManyRetries: ").append(this.tooManyRetries);
        }
        if (this.databaseError) {
            sb.append(" databaseError: ").append(this.databaseError);
        }
        if (this.fullSyncRequested) {
            sb.append(" fullSyncRequested: ").append(this.fullSyncRequested);
        }
        if (this.partialSyncUnavailable) {
            sb.append(" partialSyncUnavailable: ").append(this.partialSyncUnavailable);
        }
        if (this.moreRecordsToGet) {
            sb.append(" moreRecordsToGet: ").append(this.moreRecordsToGet);
        }
        if (this.delayUntil > 0) {
            sb.append(" delayUntil: ").append(this.delayUntil);
        }
        sb.append(this.stats);
        return sb.toString();
    }

    public String toDebugString() {
        StringBuffer sb = new StringBuffer();
        if (this.fullSyncRequested) {
            sb.append("f1");
        }
        if (this.partialSyncUnavailable) {
            sb.append("r1");
        }
        if (hasHardError()) {
            sb.append("X1");
        }
        if (this.stats.numParseExceptions > 0) {
            sb.append("e").append(this.stats.numParseExceptions);
        }
        if (this.stats.numConflictDetectedExceptions > 0) {
            sb.append(FullBackup.CACHE_TREE_TOKEN).append(this.stats.numConflictDetectedExceptions);
        }
        if (this.stats.numAuthExceptions > 0) {
            sb.append(FullBackup.APK_TREE_TOKEN).append(this.stats.numAuthExceptions);
        }
        if (this.tooManyDeletions) {
            sb.append("D1");
        }
        if (this.tooManyRetries) {
            sb.append("R1");
        }
        if (this.databaseError) {
            sb.append("b1");
        }
        if (hasSoftError()) {
            sb.append("x1");
        }
        if (this.syncAlreadyInProgress) {
            sb.append("l1");
        }
        if (this.stats.numIoExceptions > 0) {
            sb.append("I").append(this.stats.numIoExceptions);
        }
        return sb.toString();
    }
}
