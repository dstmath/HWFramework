package com.android.server.backup.encryption.chunking.cdc;

import com.android.internal.util.Preconditions;
import com.android.server.backup.encryption.chunking.Chunker;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class ContentDefinedChunker implements Chunker {
    private static final byte DEFAULT_OUT_BYTE = 0;
    private static final int WINDOW_SIZE = 31;
    private final BreakpointPredicate mBreakpointPredicate;
    private final byte[] mChunkBuffer;
    private final FingerprintMixer mFingerprintMixer;
    private final int mMaxChunkSize;
    private final int mMinChunkSize;
    private final RabinFingerprint64 mRabinFingerprint64;

    public interface BreakpointPredicate {
        boolean isBreakpoint(long j);
    }

    public ContentDefinedChunker(int minChunkSize, int maxChunkSize, RabinFingerprint64 rabinFingerprint64, FingerprintMixer fingerprintMixer, BreakpointPredicate breakpointPredicate) {
        boolean z = true;
        Preconditions.checkArgument(minChunkSize >= 31, "Minimum chunk size must be greater than window size.");
        Preconditions.checkArgument(maxChunkSize < minChunkSize ? false : z, "Maximum chunk size cannot be smaller than minimum chunk size.");
        this.mChunkBuffer = new byte[maxChunkSize];
        this.mRabinFingerprint64 = rabinFingerprint64;
        this.mBreakpointPredicate = breakpointPredicate;
        this.mFingerprintMixer = fingerprintMixer;
        this.mMinChunkSize = minChunkSize;
        this.mMaxChunkSize = maxChunkSize;
    }

    @Override // com.android.server.backup.encryption.chunking.Chunker
    public void chunkify(InputStream inputStream, Chunker.ChunkConsumer chunkConsumer) throws IOException, GeneralSecurityException {
        int chunkLength;
        int initialReadLength = this.mMinChunkSize - 31;
        while (true) {
            int read = inputStream.read(this.mChunkBuffer, 0, initialReadLength);
            int chunkLength2 = read;
            if (read != -1) {
                long fingerprint = 0;
                while (true) {
                    int b = inputStream.read();
                    if (b == -1) {
                        break;
                    }
                    byte inByte = (byte) b;
                    byte outByte = getCurrentWindowStartByte(chunkLength2);
                    chunkLength = chunkLength2 + 1;
                    this.mChunkBuffer[chunkLength2] = inByte;
                    fingerprint = this.mRabinFingerprint64.computeFingerprint64(inByte, outByte, fingerprint);
                    if (chunkLength >= this.mMaxChunkSize || (chunkLength >= this.mMinChunkSize && this.mBreakpointPredicate.isBreakpoint(this.mFingerprintMixer.mix(fingerprint)))) {
                        break;
                    }
                    chunkLength2 = chunkLength;
                }
                chunkConsumer.accept(Arrays.copyOf(this.mChunkBuffer, chunkLength));
                chunkLength2 = 0;
                if (chunkLength2 > 0) {
                    chunkConsumer.accept(Arrays.copyOf(this.mChunkBuffer, chunkLength2));
                }
            } else {
                return;
            }
        }
    }

    private byte getCurrentWindowStartByte(int chunkLength) {
        if (chunkLength < this.mMinChunkSize) {
            return 0;
        }
        return this.mChunkBuffer[chunkLength - 31];
    }
}
