package android.media;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class BufferingParams implements Parcelable {
    public static final int BUFFERING_MODE_NONE = 0;
    public static final int BUFFERING_MODE_SIZE_ONLY = 2;
    public static final int BUFFERING_MODE_TIME_ONLY = 1;
    public static final int BUFFERING_MODE_TIME_THEN_SIZE = 3;
    private static final int BUFFERING_NO_WATERMARK = -1;
    public static final Creator<BufferingParams> CREATOR = new Creator<BufferingParams>() {
        public BufferingParams createFromParcel(Parcel in) {
            return new BufferingParams(in, null);
        }

        public BufferingParams[] newArray(int size) {
            return new BufferingParams[size];
        }
    };
    private int mInitialBufferingMode;
    private int mInitialWatermarkKB;
    private int mInitialWatermarkMs;
    private int mRebufferingMode;
    private int mRebufferingWatermarkHighKB;
    private int mRebufferingWatermarkHighMs;
    private int mRebufferingWatermarkLowKB;
    private int mRebufferingWatermarkLowMs;

    public static class Builder {
        private int mInitialBufferingMode = 0;
        private int mInitialWatermarkKB = -1;
        private int mInitialWatermarkMs = -1;
        private int mRebufferingMode = 0;
        private int mRebufferingWatermarkHighKB = -1;
        private int mRebufferingWatermarkHighMs = -1;
        private int mRebufferingWatermarkLowKB = -1;
        private int mRebufferingWatermarkLowMs = -1;

        public Builder(BufferingParams bp) {
            this.mInitialBufferingMode = bp.mInitialBufferingMode;
            this.mRebufferingMode = bp.mRebufferingMode;
            this.mInitialWatermarkMs = bp.mInitialWatermarkMs;
            this.mInitialWatermarkKB = bp.mInitialWatermarkKB;
            this.mRebufferingWatermarkLowMs = bp.mRebufferingWatermarkLowMs;
            this.mRebufferingWatermarkHighMs = bp.mRebufferingWatermarkHighMs;
            this.mRebufferingWatermarkLowKB = bp.mRebufferingWatermarkLowKB;
            this.mRebufferingWatermarkHighKB = bp.mRebufferingWatermarkHighKB;
        }

        public BufferingParams build() {
            if (isTimeBasedMode(this.mRebufferingMode) && this.mRebufferingWatermarkLowMs > this.mRebufferingWatermarkHighMs) {
                throw new IllegalStateException("Illegal watermark:" + this.mRebufferingWatermarkLowMs + " : " + this.mRebufferingWatermarkHighMs);
            } else if (!isSizeBasedMode(this.mRebufferingMode) || this.mRebufferingWatermarkLowKB <= this.mRebufferingWatermarkHighKB) {
                BufferingParams bp = new BufferingParams();
                bp.mInitialBufferingMode = this.mInitialBufferingMode;
                bp.mRebufferingMode = this.mRebufferingMode;
                bp.mInitialWatermarkMs = this.mInitialWatermarkMs;
                bp.mInitialWatermarkKB = this.mInitialWatermarkKB;
                bp.mRebufferingWatermarkLowMs = this.mRebufferingWatermarkLowMs;
                bp.mRebufferingWatermarkHighMs = this.mRebufferingWatermarkHighMs;
                bp.mRebufferingWatermarkLowKB = this.mRebufferingWatermarkLowKB;
                bp.mRebufferingWatermarkHighKB = this.mRebufferingWatermarkHighKB;
                return bp;
            } else {
                throw new IllegalStateException("Illegal watermark:" + this.mRebufferingWatermarkLowKB + " : " + this.mRebufferingWatermarkHighKB);
            }
        }

        private boolean isTimeBasedMode(int mode) {
            return mode == 1 || mode == 3;
        }

        private boolean isSizeBasedMode(int mode) {
            return mode == 2 || mode == 3;
        }

        public Builder setInitialBufferingMode(int mode) {
            switch (mode) {
                case 0:
                case 1:
                case 2:
                case 3:
                    this.mInitialBufferingMode = mode;
                    return this;
                default:
                    throw new IllegalArgumentException("Illegal buffering mode " + mode);
            }
        }

        public Builder setRebufferingMode(int mode) {
            switch (mode) {
                case 0:
                case 1:
                case 2:
                case 3:
                    this.mRebufferingMode = mode;
                    return this;
                default:
                    throw new IllegalArgumentException("Illegal buffering mode " + mode);
            }
        }

        public Builder setInitialBufferingWatermarkMs(int watermarkMs) {
            this.mInitialWatermarkMs = watermarkMs;
            return this;
        }

        public Builder setInitialBufferingWatermarkKB(int watermarkKB) {
            this.mInitialWatermarkKB = watermarkKB;
            return this;
        }

        public Builder setRebufferingWatermarkLowMs(int watermarkMs) {
            this.mRebufferingWatermarkLowMs = watermarkMs;
            return this;
        }

        public Builder setRebufferingWatermarkHighMs(int watermarkMs) {
            this.mRebufferingWatermarkHighMs = watermarkMs;
            return this;
        }

        public Builder setRebufferingWatermarkLowKB(int watermarkKB) {
            this.mRebufferingWatermarkLowKB = watermarkKB;
            return this;
        }

        public Builder setRebufferingWatermarkHighKB(int watermarkKB) {
            this.mRebufferingWatermarkHighKB = watermarkKB;
            return this;
        }

        public Builder setRebufferingWatermarksMs(int lowWatermarkMs, int highWatermarkMs) {
            this.mRebufferingWatermarkLowMs = lowWatermarkMs;
            this.mRebufferingWatermarkHighMs = highWatermarkMs;
            return this;
        }

        public Builder setRebufferingWatermarksKB(int lowWatermarkKB, int highWatermarkKB) {
            this.mRebufferingWatermarkLowKB = lowWatermarkKB;
            this.mRebufferingWatermarkHighKB = highWatermarkKB;
            return this;
        }
    }

    /* synthetic */ BufferingParams(Parcel in, BufferingParams -this1) {
        this(in);
    }

    private BufferingParams() {
        this.mInitialBufferingMode = 0;
        this.mRebufferingMode = 0;
        this.mInitialWatermarkMs = -1;
        this.mInitialWatermarkKB = -1;
        this.mRebufferingWatermarkLowMs = -1;
        this.mRebufferingWatermarkHighMs = -1;
        this.mRebufferingWatermarkLowKB = -1;
        this.mRebufferingWatermarkHighKB = -1;
    }

    public int getInitialBufferingMode() {
        return this.mInitialBufferingMode;
    }

    public int getRebufferingMode() {
        return this.mRebufferingMode;
    }

    public int getInitialBufferingWatermarkMs() {
        return this.mInitialWatermarkMs;
    }

    public int getInitialBufferingWatermarkKB() {
        return this.mInitialWatermarkKB;
    }

    public int getRebufferingWatermarkLowMs() {
        return this.mRebufferingWatermarkLowMs;
    }

    public int getRebufferingWatermarkHighMs() {
        return this.mRebufferingWatermarkHighMs;
    }

    public int getRebufferingWatermarkLowKB() {
        return this.mRebufferingWatermarkLowKB;
    }

    public int getRebufferingWatermarkHighKB() {
        return this.mRebufferingWatermarkHighKB;
    }

    private BufferingParams(Parcel in) {
        this.mInitialBufferingMode = 0;
        this.mRebufferingMode = 0;
        this.mInitialWatermarkMs = -1;
        this.mInitialWatermarkKB = -1;
        this.mRebufferingWatermarkLowMs = -1;
        this.mRebufferingWatermarkHighMs = -1;
        this.mRebufferingWatermarkLowKB = -1;
        this.mRebufferingWatermarkHighKB = -1;
        this.mInitialBufferingMode = in.readInt();
        this.mRebufferingMode = in.readInt();
        this.mInitialWatermarkMs = in.readInt();
        this.mInitialWatermarkKB = in.readInt();
        this.mRebufferingWatermarkLowMs = in.readInt();
        this.mRebufferingWatermarkHighMs = in.readInt();
        this.mRebufferingWatermarkLowKB = in.readInt();
        this.mRebufferingWatermarkHighKB = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mInitialBufferingMode);
        dest.writeInt(this.mRebufferingMode);
        dest.writeInt(this.mInitialWatermarkMs);
        dest.writeInt(this.mInitialWatermarkKB);
        dest.writeInt(this.mRebufferingWatermarkLowMs);
        dest.writeInt(this.mRebufferingWatermarkHighMs);
        dest.writeInt(this.mRebufferingWatermarkLowKB);
        dest.writeInt(this.mRebufferingWatermarkHighKB);
    }
}
