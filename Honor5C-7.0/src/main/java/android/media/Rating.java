package android.media;

import android.hardware.SensorManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.speech.tts.TextToSpeech.Engine;
import android.util.Log;

public final class Rating implements Parcelable {
    public static final Creator<Rating> CREATOR = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.Rating.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.Rating.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.Rating.<clinit>():void");
    }

    private Rating(int ratingStyle, float rating) {
        this.mRatingStyle = ratingStyle;
        this.mRatingValue = rating;
    }

    public String toString() {
        return "Rating:style=" + this.mRatingStyle + " rating=" + (this.mRatingValue < 0.0f ? "unrated" : String.valueOf(this.mRatingValue));
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
            case RATING_HEART /*1*/:
            case RATING_THUMB_UP_DOWN /*2*/:
            case RATING_3_STARS /*3*/:
            case RATING_4_STARS /*4*/:
            case RATING_5_STARS /*5*/:
            case RATING_PERCENTAGE /*6*/:
                return new Rating(ratingStyle, RATING_NOT_RATED);
            default:
                return null;
        }
    }

    public static Rating newHeartRating(boolean hasHeart) {
        return new Rating(RATING_HEART, hasHeart ? Engine.DEFAULT_VOLUME : 0.0f);
    }

    public static Rating newThumbRating(boolean thumbIsUp) {
        return new Rating(RATING_THUMB_UP_DOWN, thumbIsUp ? Engine.DEFAULT_VOLUME : 0.0f);
    }

    public static Rating newStarRating(int starRatingStyle, float starRating) {
        float maxRating;
        switch (starRatingStyle) {
            case RATING_3_STARS /*3*/:
                maxRating = 3.0f;
                break;
            case RATING_4_STARS /*4*/:
                maxRating = 4.0f;
                break;
            case RATING_5_STARS /*5*/:
                maxRating = 5.0f;
                break;
            default:
                Log.e(TAG, "Invalid rating style (" + starRatingStyle + ") for a star rating");
                return null;
        }
        if (starRating >= 0.0f && starRating <= maxRating) {
            return new Rating(starRatingStyle, starRating);
        }
        Log.e(TAG, "Trying to set out of range star-based rating");
        return null;
    }

    public static Rating newPercentageRating(float percent) {
        if (percent >= 0.0f && percent <= SensorManager.LIGHT_CLOUDY) {
            return new Rating(RATING_PERCENTAGE, percent);
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
        boolean z = true;
        if (this.mRatingStyle != RATING_HEART) {
            return false;
        }
        if (this.mRatingValue != Engine.DEFAULT_VOLUME) {
            z = false;
        }
        return z;
    }

    public boolean isThumbUp() {
        boolean z = false;
        if (this.mRatingStyle != RATING_THUMB_UP_DOWN) {
            return false;
        }
        if (this.mRatingValue == Engine.DEFAULT_VOLUME) {
            z = true;
        }
        return z;
    }

    public float getStarRating() {
        switch (this.mRatingStyle) {
            case RATING_3_STARS /*3*/:
            case RATING_4_STARS /*4*/:
            case RATING_5_STARS /*5*/:
                if (isRated()) {
                    return this.mRatingValue;
                }
                break;
        }
        return RATING_NOT_RATED;
    }

    public float getPercentRating() {
        if (this.mRatingStyle == RATING_PERCENTAGE && isRated()) {
            return this.mRatingValue;
        }
        return RATING_NOT_RATED;
    }
}
