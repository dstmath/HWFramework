package ohos.media.common.sessioncore;

import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class AVRating implements Sequenceable {
    public static final int AVRATING_NONE = 0;
    private static final float AVRATING_NOT_RATED = -1.0f;
    public static final int AVRATING_STYLE_3_STARS = 3;
    public static final int AVRATING_STYLE_4_STARS = 4;
    public static final int AVRATING_STYLE_5_STARS = 5;
    public static final int AVRATING_STYLE_HEART = 1;
    public static final int AVRATING_STYLE_PERCENTAGE = 6;
    public static final int AVRATING_STYLE_THUMB_UP_DOWN = 2;
    public static final Sequenceable.Producer<AVRating> CREATOR = new Sequenceable.Producer<AVRating>() {
        /* class ohos.media.common.sessioncore.AVRating.AnonymousClass1 */

        @Override // ohos.utils.Sequenceable.Producer
        public AVRating createFromParcel(Parcel parcel) {
            return new AVRating(parcel.readInt(), parcel.readFloat());
        }
    };
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVRating.class);
    private final int ratingStyle;
    private final float ratingValue;

    private static boolean isAVRatingStyle(int i) {
        return i == 6 || i == 3 || i == 4 || i == 5 || i == 2 || i == 1;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(this.ratingStyle);
        parcel.writeFloat(this.ratingValue);
        return true;
    }

    private AVRating(int i, float f) {
        this.ratingStyle = i;
        this.ratingValue = f;
    }

    public static AVRating getUnratedAVRating(int i) {
        if (isAVRatingStyle(i)) {
            return new AVRating(i, AVRATING_NOT_RATED);
        }
        LOGGER.error("Invalid ratingStyle", new Object[0]);
        return null;
    }

    public static AVRating getHeartAVRating(boolean z) {
        return new AVRating(1, z ? 1.0f : ConstantValue.MIN_ZOOM_VALUE);
    }

    public static AVRating getThumbAVRating(boolean z) {
        return new AVRating(2, z ? 1.0f : ConstantValue.MIN_ZOOM_VALUE);
    }

    public static AVRating getStarAVRating(int i, float f) {
        if (f < ConstantValue.MIN_ZOOM_VALUE) {
            LOGGER.error("Invalid starRatingValue", new Object[0]);
            return null;
        } else if (i == 3 && f <= 3.0f) {
            return new AVRating(i, f);
        } else {
            if (i == 4 && f <= 4.0f) {
                return new AVRating(i, f);
            }
            if (i == 5 && f <= 5.0f) {
                return new AVRating(i, f);
            }
            LOGGER.error("Invalid starRatingStyle or starRatingValue", new Object[0]);
            return null;
        }
    }

    public static AVRating getPercentageAVRating(float f) {
        if (f >= ConstantValue.MIN_ZOOM_VALUE && f <= 100.0f) {
            return new AVRating(6, f);
        }
        LOGGER.error("Invalid percentage-based rating value", new Object[0]);
        return null;
    }

    public boolean isAVRatingAvailable() {
        return this.ratingValue >= ConstantValue.MIN_ZOOM_VALUE;
    }

    public int getAVRatingStyle() {
        return this.ratingStyle;
    }

    public boolean isHeartSelected() {
        return this.ratingStyle == 1 && this.ratingValue == 1.0f;
    }

    public boolean isThumbUpStyle() {
        return this.ratingStyle == 2 && this.ratingValue == 1.0f;
    }

    public float getStarAVRatingValue() {
        return (!isStarAVRatingStyle() || !isAVRatingAvailable()) ? AVRATING_NOT_RATED : this.ratingValue;
    }

    private boolean isStarAVRatingStyle() {
        int i = this.ratingStyle;
        return i == 3 || i == 4 || i == 5;
    }

    public float getPercentAVRatingValue() {
        return (this.ratingStyle != 6 || !isAVRatingAvailable()) ? AVRATING_NOT_RATED : this.ratingValue;
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("AVRating:style=");
        sb.append(this.ratingStyle);
        sb.append(" rating=");
        float f = this.ratingValue;
        if (f < ConstantValue.MIN_ZOOM_VALUE) {
            str = "unrated";
        } else {
            str = String.valueOf(f);
        }
        sb.append(str);
        return sb.toString();
    }
}
