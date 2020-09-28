package android.telephony.ims;

import android.os.Parcel;

public class RcsMessageCreationParams {
    private final double mLatitude;
    private final double mLongitude;
    private final int mMessageStatus;
    private final long mOriginationTimestamp;
    private final String mRcsMessageGlobalId;
    private final int mSubId;
    private final String mText;

    public String getRcsMessageGlobalId() {
        return this.mRcsMessageGlobalId;
    }

    public int getSubId() {
        return this.mSubId;
    }

    public int getMessageStatus() {
        return this.mMessageStatus;
    }

    public long getOriginationTimestamp() {
        return this.mOriginationTimestamp;
    }

    public String getText() {
        return this.mText;
    }

    public double getLatitude() {
        return this.mLatitude;
    }

    public double getLongitude() {
        return this.mLongitude;
    }

    public static class Builder {
        private double mLatitude = Double.MIN_VALUE;
        private double mLongitude = Double.MIN_VALUE;
        private int mMessageStatus;
        private long mOriginationTimestamp;
        private String mRcsMessageGlobalId;
        private int mSubId;
        private String mText;

        public Builder(long originationTimestamp, int subscriptionId) {
            this.mOriginationTimestamp = originationTimestamp;
            this.mSubId = subscriptionId;
        }

        public Builder setStatus(int rcsMessageStatus) {
            this.mMessageStatus = rcsMessageStatus;
            return this;
        }

        public Builder setRcsMessageId(String rcsMessageId) {
            this.mRcsMessageGlobalId = rcsMessageId;
            return this;
        }

        public Builder setText(String text) {
            this.mText = text;
            return this;
        }

        public Builder setLatitude(double latitude) {
            this.mLatitude = latitude;
            return this;
        }

        public Builder setLongitude(double longitude) {
            this.mLongitude = longitude;
            return this;
        }

        public RcsMessageCreationParams build() {
            return new RcsMessageCreationParams(this);
        }
    }

    protected RcsMessageCreationParams(Builder builder) {
        this.mRcsMessageGlobalId = builder.mRcsMessageGlobalId;
        this.mSubId = builder.mSubId;
        this.mMessageStatus = builder.mMessageStatus;
        this.mOriginationTimestamp = builder.mOriginationTimestamp;
        this.mText = builder.mText;
        this.mLatitude = builder.mLatitude;
        this.mLongitude = builder.mLongitude;
    }

    RcsMessageCreationParams(Parcel in) {
        this.mRcsMessageGlobalId = in.readString();
        this.mSubId = in.readInt();
        this.mMessageStatus = in.readInt();
        this.mOriginationTimestamp = in.readLong();
        this.mText = in.readString();
        this.mLatitude = in.readDouble();
        this.mLongitude = in.readDouble();
    }

    public void writeToParcel(Parcel dest) {
        dest.writeString(this.mRcsMessageGlobalId);
        dest.writeInt(this.mSubId);
        dest.writeInt(this.mMessageStatus);
        dest.writeLong(this.mOriginationTimestamp);
        dest.writeString(this.mText);
        dest.writeDouble(this.mLatitude);
        dest.writeDouble(this.mLongitude);
    }
}
