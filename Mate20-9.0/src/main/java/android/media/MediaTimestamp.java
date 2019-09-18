package android.media;

public final class MediaTimestamp {
    public static final MediaTimestamp TIMESTAMP_UNKNOWN;
    public final float clockRate;
    public final long mediaTimeUs;
    public final long nanoTime;

    static {
        MediaTimestamp mediaTimestamp = new MediaTimestamp(-1, -1, 0.0f);
        TIMESTAMP_UNKNOWN = mediaTimestamp;
    }

    public long getAnchorMediaTimeUs() {
        return this.mediaTimeUs;
    }

    public long getAnchorSytemNanoTime() {
        return this.nanoTime;
    }

    public float getMediaClockRate() {
        return this.clockRate;
    }

    MediaTimestamp(long mediaUs, long systemNs, float rate) {
        this.mediaTimeUs = mediaUs;
        this.nanoTime = systemNs;
        this.clockRate = rate;
    }

    MediaTimestamp() {
        this.mediaTimeUs = 0;
        this.nanoTime = 0;
        this.clockRate = 1.0f;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MediaTimestamp that = (MediaTimestamp) obj;
        if (!(this.mediaTimeUs == that.mediaTimeUs && this.nanoTime == that.nanoTime && this.clockRate == that.clockRate)) {
            z = false;
        }
        return z;
    }

    public String toString() {
        return getClass().getName() + "{AnchorMediaTimeUs=" + this.mediaTimeUs + " AnchorSystemNanoTime=" + this.nanoTime + " clockRate=" + this.clockRate + "}";
    }
}
