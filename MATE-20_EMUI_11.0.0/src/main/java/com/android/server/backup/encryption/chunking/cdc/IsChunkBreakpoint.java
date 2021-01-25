package com.android.server.backup.encryption.chunking.cdc;

import com.android.internal.util.Preconditions;
import com.android.server.backup.encryption.chunking.cdc.ContentDefinedChunker;

public class IsChunkBreakpoint implements ContentDefinedChunker.BreakpointPredicate {
    private final long mBitmask;
    private final int mLeadingZeros;

    public IsChunkBreakpoint(long averageNumberOfTrialsUntilBreakpoint) {
        Preconditions.checkArgument(averageNumberOfTrialsUntilBreakpoint >= 0, "Average number of trials must be non-negative");
        this.mLeadingZeros = (int) Math.round(log2((double) averageNumberOfTrialsUntilBreakpoint));
        this.mBitmask = ~(-1 >>> this.mLeadingZeros);
    }

    @Override // com.android.server.backup.encryption.chunking.cdc.ContentDefinedChunker.BreakpointPredicate
    public boolean isBreakpoint(long fingerprint) {
        return (this.mBitmask & fingerprint) == 0;
    }

    public int getLeadingZeros() {
        return this.mLeadingZeros;
    }

    private static double log2(double x) {
        return Math.log(x) / Math.log(2.0d);
    }
}
