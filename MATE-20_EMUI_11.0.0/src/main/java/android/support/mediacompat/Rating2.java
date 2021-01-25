package android.support.mediacompat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.util.ObjectsCompat;
import android.util.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class Rating2 {
    private static final String KEY_STYLE = "android.media.rating2.style";
    private static final String KEY_VALUE = "android.media.rating2.value";
    public static final int RATING_3_STARS = 3;
    public static final int RATING_4_STARS = 4;
    public static final int RATING_5_STARS = 5;
    public static final int RATING_HEART = 1;
    public static final int RATING_NONE = 0;
    private static final float RATING_NOT_RATED = -1.0f;
    public static final int RATING_PERCENTAGE = 6;
    public static final int RATING_THUMB_UP_DOWN = 2;
    private static final String TAG = "Rating2";
    private final int mRatingStyle;
    private final float mRatingValue;

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public @interface StarStyle {
    }

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public @interface Style {
    }

    private Rating2(int ratingStyle, float rating) {
        this.mRatingStyle = ratingStyle;
        this.mRatingValue = rating;
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("Rating2:style=");
        sb.append(this.mRatingStyle);
        sb.append(" rating=");
        if (this.mRatingValue < 0.0f) {
            str = "unrated";
        } else {
            str = String.valueOf(this.mRatingValue);
        }
        sb.append(str);
        return sb.toString();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Rating2)) {
            return false;
        }
        Rating2 other = (Rating2) obj;
        if (this.mRatingStyle == other.mRatingStyle && this.mRatingValue == other.mRatingValue) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return ObjectsCompat.hash(Integer.valueOf(this.mRatingStyle), Float.valueOf(this.mRatingValue));
    }

    public static Rating2 fromBundle(@Nullable Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        return new Rating2(bundle.getInt(KEY_STYLE), bundle.getFloat(KEY_VALUE));
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_STYLE, this.mRatingStyle);
        bundle.putFloat(KEY_VALUE, this.mRatingValue);
        return bundle;
    }

    @Nullable
    public static Rating2 newUnratedRating(int ratingStyle) {
        switch (ratingStyle) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return new Rating2(ratingStyle, -1.0f);
            default:
                return null;
        }
    }

    @Nullable
    public static Rating2 newHeartRating(boolean hasHeart) {
        return new Rating2(1, hasHeart ? 1.0f : 0.0f);
    }

    @Nullable
    public static Rating2 newThumbRating(boolean thumbIsUp) {
        return new Rating2(2, thumbIsUp ? 1.0f : 0.0f);
    }

    @Nullable
    public static Rating2 newStarRating(int starRatingStyle, float starRating) {
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
        if (starRating >= 0.0f && starRating <= maxRating) {
            return new Rating2(starRatingStyle, starRating);
        }
        Log.e(TAG, "Trying to set out of range star-based rating");
        return null;
    }

    @Nullable
    public static Rating2 newPercentageRating(float percent) {
        if (percent >= 0.0f && percent <= 100.0f) {
            return new Rating2(6, percent);
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
        return this.mRatingStyle == 2 && this.mRatingValue == 1.0f;
    }

    public float getStarRating() {
        switch (this.mRatingStyle) {
            case 3:
            case 4:
            case 5:
                if (isRated()) {
                    return this.mRatingValue;
                }
                return -1.0f;
            default:
                return -1.0f;
        }
    }

    public float getPercentRating() {
        if (this.mRatingStyle != 6 || !isRated()) {
            return -1.0f;
        }
        return this.mRatingValue;
    }
}
