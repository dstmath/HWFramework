package ohos.media.common.adapter;

import android.media.Rating;
import ohos.media.common.sessioncore.AVRating;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class AVRatingAdapter {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVRatingAdapter.class);

    public static AVRating getAVRating(Rating rating) {
        if (rating == null) {
            LOGGER.error("rating cannot be null", new Object[0]);
            return null;
        }
        int ratingStyle = rating.getRatingStyle();
        if (ratingStyle == 6) {
            return AVRating.getPercentageAVRating(rating.getPercentRating());
        }
        if (ratingStyle == 2) {
            return AVRating.getThumbAVRating(rating.isThumbUp());
        }
        if (ratingStyle == 1) {
            return AVRating.getHeartAVRating(rating.hasHeart());
        }
        if (ratingStyle == 5 || ratingStyle == 4 || ratingStyle == 3) {
            return AVRating.getStarAVRating(ratingStyle, rating.getStarRating());
        }
        return null;
    }

    public static Rating getRating(AVRating aVRating) {
        if (aVRating == null) {
            LOGGER.error("rating cannot be null", new Object[0]);
            return null;
        }
        int aVRatingStyle = aVRating.getAVRatingStyle();
        if (aVRatingStyle == 6) {
            return Rating.newPercentageRating(aVRating.getPercentAVRatingValue());
        }
        if (aVRatingStyle == 2) {
            return Rating.newThumbRating(aVRating.isThumbUpStyle());
        }
        if (aVRatingStyle == 1) {
            return Rating.newHeartRating(aVRating.isHeartSelected());
        }
        if (aVRatingStyle == 5 || aVRatingStyle == 4 || aVRatingStyle == 3) {
            return Rating.newStarRating(aVRatingStyle, aVRating.getStarAVRatingValue());
        }
        return null;
    }
}
