package android.media;

import android.os.Parcel;
import android.os.Parcelable;

public final class BufferingParams implements Parcelable {
    private static final int BUFFERING_NO_MARK = -1;
    public static final Parcelable.Creator<BufferingParams> CREATOR = new Parcelable.Creator<BufferingParams>() {
        public BufferingParams createFromParcel(Parcel in) {
            return new BufferingParams(in);
        }

        public BufferingParams[] newArray(int size) {
            return new BufferingParams[size];
        }
    };
    /* access modifiers changed from: private */
    public int mInitialMarkMs;
    /* access modifiers changed from: private */
    public int mResumePlaybackMarkMs;

    public static class Builder {
        private int mInitialMarkMs = -1;
        private int mResumePlaybackMarkMs = -1;

        public Builder() {
        }

        public Builder(BufferingParams bp) {
            this.mInitialMarkMs = bp.mInitialMarkMs;
            this.mResumePlaybackMarkMs = bp.mResumePlaybackMarkMs;
        }

        public BufferingParams build() {
            BufferingParams bp = new BufferingParams();
            int unused = bp.mInitialMarkMs = this.mInitialMarkMs;
            int unused2 = bp.mResumePlaybackMarkMs = this.mResumePlaybackMarkMs;
            return bp;
        }

        public Builder setInitialMarkMs(int markMs) {
            this.mInitialMarkMs = markMs;
            return this;
        }

        public Builder setResumePlaybackMarkMs(int markMs) {
            this.mResumePlaybackMarkMs = markMs;
            return this;
        }
    }

    private BufferingParams() {
        this.mInitialMarkMs = -1;
        this.mResumePlaybackMarkMs = -1;
    }

    public int getInitialMarkMs() {
        return this.mInitialMarkMs;
    }

    public int getResumePlaybackMarkMs() {
        return this.mResumePlaybackMarkMs;
    }

    private BufferingParams(Parcel in) {
        this.mInitialMarkMs = -1;
        this.mResumePlaybackMarkMs = -1;
        this.mInitialMarkMs = in.readInt();
        this.mResumePlaybackMarkMs = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mInitialMarkMs);
        dest.writeInt(this.mResumePlaybackMarkMs);
    }
}
