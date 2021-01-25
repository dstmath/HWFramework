package android.media;

public final class MediaTimestamp {
    public static final MediaTimestamp TIMESTAMP_UNKNOWN = new MediaTimestamp(-1, -1, 0.0f);
    public final float clockRate;
    public final long mediaTimeUs;
    public final long nanoTime;

    public long getAnchorMediaTimeUs() {
        return this.mediaTimeUs;
    }

    @Deprecated
    public long getAnchorSytemNanoTime() {
        return getAnchorSystemNanoTime();
    }

    public long getAnchorSystemNanoTime() {
        return this.nanoTime;
    }

    public float getMediaClockRate() {
        return this.clockRate;
    }

    public MediaTimestamp(long mediaTimeUs2, long nanoTimeNs, float clockRate2) {
        this.mediaTimeUs = mediaTimeUs2;
        this.nanoTime = nanoTimeNs;
        this.clockRate = clockRate2;
    }

    MediaTimestamp() {
        this.mediaTimeUs = 0;
        this.nanoTime = 0;
        this.clockRate = 1.0f;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MediaTimestamp that = (MediaTimestamp) obj;
        if (this.mediaTimeUs == that.mediaTimeUs && this.nanoTime == that.nanoTime && this.clockRate == that.clockRate) {
            return true;
        }
        return false;
    }

    public String toString() {
        return getClass().getName() + "{AnchorMediaTimeUs=" + this.mediaTimeUs + " AnchorSystemNanoTime=" + this.nanoTime + " clockRate=" + this.clockRate + "}";
    }
}
