package android.media;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class Rating implements Parcelable {
    public static final Parcelable.Creator<Rating> CREATOR = new Parcelable.Creator<Rating>() {
        /* class android.media.Rating.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Rating createFromParcel(Parcel p) {
            return new Rating(p.readInt(), p.readFloat());
        }

        @Override // android.os.Parcelable.Creator
        public Rating[] newArray(int size) {
            return new Rating[size];
        }
    };
    public static final int RATING_3_STARS = 3;
    public static final int RATING_4_STARS = 4;
    public static final int RATING_5_STARS = 5;
    public static final int RATING_HEART = 1;
    public static final int RATING_NONE = 0;
    private static final float RATING_NOT_RATED = -1.0f;
    public static final int RATING_PERCENTAGE = 6;
    public static final int RATING_THUMB_UP_DOWN = 2;
    private static final String TAG = "Rating";
    private final int mRatingStyle;
    private final float mRatingValue;

    @Retention(RetentionPolicy.SOURCE)
    public @interface StarStyle {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Style {
    }

    private Rating(int ratingStyle, float rating) {
        this.mRatingStyle = ratingStyle;
        this.mRatingValue = rating;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Rating:style=");
        sb.append(this.mRatingStyle);
        sb.append(" rating=");
        float f = this.mRatingValue;
        sb.append(f < 0.0f ? "unrated" : String.valueOf(f));
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return this.mRatingStyle;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mRatingStyle);
        dest.writeFloat(this.mRatingValue);
    }

    public static Rating newUnratedRating(int ratingStyle) {
        switch (ratingStyle) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return new Rating(ratingStyle, -1.0f);
            default:
                return null;
        }
    }

    public static Rating newHeartRating(boolean hasHeart) {
        return new Rating(1, hasHeart ? 1.0f : 0.0f);
    }

    public static Rating newThumbRating(boolean thumbIsUp) {
        return new Rating(2, thumbIsUp ? 1.0f : 0.0f);
    }

    public static Rating newStarRating(int starRatingStyle, float starRating) {
        float maxRating;
        if (starRatingStyle == 3) {
            maxRating = 3.0f;
        } else if (starRatingStyle == 4) {
            maxRating = 4.0f;
        } else if (starRatingStyle != 5) {
            Log.e(TAG, "Invalid rating style (" + starRatingStyle + ") for a star rating");
            return null;
        } else {
            maxRating = 5.0f;
        }
        if (starRating >= 0.0f && starRating <= maxRating) {
            return new Rating(starRatingStyle, starRating);
        }
        Log.e(TAG, "Trying to set out of range star-based rating");
        return null;
    }

    public static Rating newPercentageRating(float percent) {
        if (percent >= 0.0f && percent <= 100.0f) {
            return new Rating(6, percent);
        }
        Log.e(TAG, "Invalid percentage-based rating value");
        return null;
    }

    public boolean isRated() {
        return this.mRatingValue >= 0.0f;
    }

    public int getRatingStyle() {
        return this.mRatingStyle;
    }

    public boolean hasHeart() {
        return this.mRatingStyle == 1 && this.mRatingValue == 1.0f;
    }

    public boolean isThumbUp() {
        if (this.mRatingStyle == 2 && this.mRatingValue == 1.0f) {
            return true;
        }
        return false;
    }

    public float getStarRating() {
        int i = this.mRatingStyle;
        if ((i == 3 || i == 4 || i == 5) && isRated()) {
            return this.mRatingValue;
        }
        return -1.0f;
    }

    public float getPercentRating() {
        if (this.mRatingStyle != 6 || !isRated()) {
            return -1.0f;
        }
        return this.mRatingValue;
    }
}
