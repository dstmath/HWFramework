package android.media;

public final class MediaTimestamp {
    public final float clockRate;
    public final long mediaTimeUs;
    public final long nanoTime;

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
}
