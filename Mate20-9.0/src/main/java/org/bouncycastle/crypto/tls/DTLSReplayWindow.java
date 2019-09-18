package org.bouncycastle.crypto.tls;

class DTLSReplayWindow {
    private static final long VALID_SEQ_MASK = 281474976710655L;
    private static final long WINDOW_SIZE = 64;
    private long bitmap = 0;
    private long latestConfirmedSeq = -1;

    DTLSReplayWindow() {
    }

    /* access modifiers changed from: package-private */
    public void reportAuthenticated(long j) {
        if ((VALID_SEQ_MASK & j) == j) {
            if (j <= this.latestConfirmedSeq) {
                long j2 = this.latestConfirmedSeq - j;
                if (j2 < WINDOW_SIZE) {
                    this.bitmap |= 1 << ((int) j2);
                    return;
                }
            } else {
                long j3 = j - this.latestConfirmedSeq;
                if (j3 >= WINDOW_SIZE) {
                    this.bitmap = 1;
                } else {
                    this.bitmap <<= (int) j3;
                    this.bitmap |= 1;
                }
                this.latestConfirmedSeq = j;
            }
            return;
        }
        throw new IllegalArgumentException("'seq' out of range");
    }

    /* access modifiers changed from: package-private */
    public void reset() {
        this.latestConfirmedSeq = -1;
        this.bitmap = 0;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldDiscard(long j) {
        if ((VALID_SEQ_MASK & j) != j) {
            return true;
        }
        if (j <= this.latestConfirmedSeq) {
            long j2 = this.latestConfirmedSeq - j;
            if (j2 >= WINDOW_SIZE || (this.bitmap & (1 << ((int) j2))) != 0) {
                return true;
            }
        }
        return false;
    }
}
