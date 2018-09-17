package android.filterpacks.performance;

public class Throughput {
    private final int mPeriodFrames;
    private final int mPeriodTime;
    private final int mPixels;
    private final int mTotalFrames;

    public Throughput(int totalFrames, int periodFrames, int periodTime, int pixels) {
        this.mTotalFrames = totalFrames;
        this.mPeriodFrames = periodFrames;
        this.mPeriodTime = periodTime;
        this.mPixels = pixels;
    }

    public int getTotalFrameCount() {
        return this.mTotalFrames;
    }

    public int getPeriodFrameCount() {
        return this.mPeriodFrames;
    }

    public int getPeriodTime() {
        return this.mPeriodTime;
    }

    public float getFramesPerSecond() {
        return ((float) this.mPeriodFrames) / ((float) this.mPeriodTime);
    }

    public float getNanosPerPixel() {
        return (float) (((((double) this.mPeriodTime) / ((double) this.mPeriodFrames)) * 1000000.0d) / ((double) this.mPixels));
    }

    public String toString() {
        return getFramesPerSecond() + " FPS";
    }
}
