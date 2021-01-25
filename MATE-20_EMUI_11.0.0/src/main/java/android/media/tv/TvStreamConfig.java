package android.media.tv;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

@SystemApi
public class TvStreamConfig implements Parcelable {
    public static final Parcelable.Creator<TvStreamConfig> CREATOR = new Parcelable.Creator<TvStreamConfig>() {
        /* class android.media.tv.TvStreamConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TvStreamConfig createFromParcel(Parcel source) {
            try {
                return new Builder().streamId(source.readInt()).type(source.readInt()).maxWidth(source.readInt()).maxHeight(source.readInt()).generation(source.readInt()).build();
            } catch (Exception e) {
                Log.e(TvStreamConfig.TAG, "Exception creating TvStreamConfig from parcel", e);
                return null;
            }
        }

        @Override // android.os.Parcelable.Creator
        public TvStreamConfig[] newArray(int size) {
            return new TvStreamConfig[size];
        }
    };
    public static final int STREAM_TYPE_BUFFER_PRODUCER = 2;
    public static final int STREAM_TYPE_INDEPENDENT_VIDEO_SOURCE = 1;
    static final String TAG = TvStreamConfig.class.getSimpleName();
    private int mGeneration;
    private int mMaxHeight;
    private int mMaxWidth;
    private int mStreamId;
    private int mType;

    private TvStreamConfig() {
    }

    public int getStreamId() {
        return this.mStreamId;
    }

    public int getType() {
        return this.mType;
    }

    public int getMaxWidth() {
        return this.mMaxWidth;
    }

    public int getMaxHeight() {
        return this.mMaxHeight;
    }

    public int getGeneration() {
        return this.mGeneration;
    }

    public String toString() {
        return "TvStreamConfig {mStreamId=" + this.mStreamId + ";mType=" + this.mType + ";mGeneration=" + this.mGeneration + "}";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mStreamId);
        dest.writeInt(this.mType);
        dest.writeInt(this.mMaxWidth);
        dest.writeInt(this.mMaxHeight);
        dest.writeInt(this.mGeneration);
    }

    public static final class Builder {
        private Integer mGeneration;
        private Integer mMaxHeight;
        private Integer mMaxWidth;
        private Integer mStreamId;
        private Integer mType;

        public Builder streamId(int streamId) {
            this.mStreamId = Integer.valueOf(streamId);
            return this;
        }

        public Builder type(int type) {
            this.mType = Integer.valueOf(type);
            return this;
        }

        public Builder maxWidth(int maxWidth) {
            this.mMaxWidth = Integer.valueOf(maxWidth);
            return this;
        }

        public Builder maxHeight(int maxHeight) {
            this.mMaxHeight = Integer.valueOf(maxHeight);
            return this;
        }

        public Builder generation(int generation) {
            this.mGeneration = Integer.valueOf(generation);
            return this;
        }

        public TvStreamConfig build() {
            if (this.mStreamId == null || this.mType == null || this.mMaxWidth == null || this.mMaxHeight == null || this.mGeneration == null) {
                throw new UnsupportedOperationException();
            }
            TvStreamConfig config = new TvStreamConfig();
            config.mStreamId = this.mStreamId.intValue();
            config.mType = this.mType.intValue();
            config.mMaxWidth = this.mMaxWidth.intValue();
            config.mMaxHeight = this.mMaxHeight.intValue();
            config.mGeneration = this.mGeneration.intValue();
            return config;
        }
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TvStreamConfig)) {
            return false;
        }
        TvStreamConfig config = (TvStreamConfig) obj;
        if (config.mGeneration == this.mGeneration && config.mStreamId == this.mStreamId && config.mType == this.mType && config.mMaxWidth == this.mMaxWidth && config.mMaxHeight == this.mMaxHeight) {
            return true;
        }
        return false;
    }
}
