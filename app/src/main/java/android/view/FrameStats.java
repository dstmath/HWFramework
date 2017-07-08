package android.view;

public abstract class FrameStats {
    public static final long UNDEFINED_TIME_NANO = -1;
    protected long[] mFramesPresentedTimeNano;
    protected long mRefreshPeriodNano;

    public final long getRefreshPeriodNano() {
        return this.mRefreshPeriodNano;
    }

    public final int getFrameCount() {
        return this.mFramesPresentedTimeNano != null ? this.mFramesPresentedTimeNano.length : 0;
    }

    public final long getStartTimeNano() {
        if (getFrameCount() <= 0) {
            return UNDEFINED_TIME_NANO;
        }
        return this.mFramesPresentedTimeNano[0];
    }

    public final long getEndTimeNano() {
        if (getFrameCount() <= 0) {
            return UNDEFINED_TIME_NANO;
        }
        return this.mFramesPresentedTimeNano[this.mFramesPresentedTimeNano.length - 1];
    }

    public final long getFramePresentedTimeNano(int index) {
        if (this.mFramesPresentedTimeNano != null) {
            return this.mFramesPresentedTimeNano[index];
        }
        throw new IndexOutOfBoundsException();
    }
}
