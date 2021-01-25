package com.android.server.backup.encryption.chunking;

import com.android.internal.util.Preconditions;

final class ByteRange {
    private final long mEnd;
    private final long mStart;

    ByteRange(long start, long end) {
        boolean z = true;
        Preconditions.checkArgument(start >= 0);
        Preconditions.checkArgument(end < start ? false : z);
        this.mStart = start;
        this.mEnd = end;
    }

    /* access modifiers changed from: package-private */
    public long getStart() {
        return this.mStart;
    }

    /* access modifiers changed from: package-private */
    public long getEnd() {
        return this.mEnd;
    }

    /* access modifiers changed from: package-private */
    public int getLength() {
        return (int) ((this.mEnd - this.mStart) + 1);
    }

    /* access modifiers changed from: package-private */
    public ByteRange extend(long length) {
        Preconditions.checkArgument(length > 0);
        return new ByteRange(this.mStart, this.mEnd + length);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ByteRange)) {
            return false;
        }
        ByteRange byteRange = (ByteRange) o;
        if (this.mEnd == byteRange.mEnd && this.mStart == byteRange.mStart) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        long j = this.mStart;
        long j2 = this.mEnd;
        return (((17 * 31) + ((int) (j ^ (j >>> 32)))) * 31) + ((int) (j2 ^ (j2 >>> 32)));
    }

    public String toString() {
        return String.format("ByteRange{mStart=%d, mEnd=%d}", Long.valueOf(this.mStart), Long.valueOf(this.mEnd));
    }
}
