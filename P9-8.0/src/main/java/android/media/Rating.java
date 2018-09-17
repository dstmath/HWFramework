package android.media;

import android.hardware.camera2.params.TonemapCurve;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public final class Rating implements Parcelable {
    public static final Creator<Rating> CREATOR = new Creator<Rating>() {
        public Rating createFromParcel(Parcel p) {
            return new Rating(p.readInt(), p.readFloat(), null);
        }

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

    /* synthetic */ Rating(int ratingStyle, float rating, Rating -this2) {
        this(ratingStyle, rating);
    }

    private Rating(int ratingStyle, float rating) {
        this.mRatingStyle = ratingStyle;
        this.mRatingValue = rating;
    }

    public String toString() {
        return "Rating:style=" + this.mRatingStyle + " rating=" + (this.mRatingValue < TonemapCurve.LEVEL_BLACK ? "unrated" : String.valueOf(this.mRatingValue));
    }

    public int describeContents() {
        return this.mRatingStyle;
    }

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
        return new Rating(1, hasHeart ? 1.0f : TonemapCurve.LEVEL_BLACK);
    }

    public static Rating newThumbRating(boolean thumbIsUp) {
        return new Rating(2, thumbIsUp ? 1.0f : TonemapCurve.LEVEL_BLACK);
    }

    public static Rating newStarRating(int starRatingStyle, float starRating) {
        float maxRating;
        switch (starRatingStyle) {
            case 3:
                maxRating = 3.0f;
                break;
            case 4:
                maxRating = 4.0f;
                break;
            case 5:
                maxRating = 5.0f;
                break;
            default:
                Log.e(TAG, "Invalid rating style (" + starRatingStyle + ") for a star rating");
                return null;
        }
        if (starRating >= TonemapCurve.LEVEL_BLACK && starRating <= maxRating) {
            return new Rating(starRatingStyle, starRating);
        }
        Log.e(TAG, "Trying to set out of range star-based rating");
        return null;
    }

    public static Rating newPercentageRating(float percent) {
        if (percent >= TonemapCurve.LEVEL_BLACK && percent <= 100.0f) {
            return new Rating(6, percent);
        }
        Log.e(TAG, "Invalid percentage-based rating value");
        return null;
    }

    public boolean isRated() {
        return this.mRatingValue >= TonemapCurve.LEVEL_BLACK;
    }

    public int getRatingStyle() {
        return this.mRatingStyle;
    }

    public boolean hasHeart() {
        boolean z = true;
        if (this.mRatingStyle != 1) {
            return false;
        }
        if (this.mRatingValue != 1.0f) {
            z = false;
        }
        return z;
    }

    public boolean isThumbUp() {
        boolean z = false;
        if (this.mRatingStyle != 2) {
            return false;
        }
        if (this.mRatingValue == 1.0f) {
            z = true;
        }
        return z;
    }

    public float getStarRating() {
        switch (this.mRatingStyle) {
            case 3:
            case 4:
            case 5:
                if (isRated()) {
                    return this.mRatingValue;
                }
                break;
        }
        return -1.0f;
    }

    public float getPercentRating() {
        if (this.mRatingStyle == 6 && (isRated() ^ 1) == 0) {
            return this.mRatingValue;
        }
        return -1.0f;
    }
}
