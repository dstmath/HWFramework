package android.media.update;

import android.os.Bundle;

public interface Rating2Provider {
    boolean equals_impl(Object obj);

    float getPercentRating_impl();

    int getRatingStyle_impl();

    float getStarRating_impl();

    boolean hasHeart_impl();

    int hashCode_impl();

    boolean isRated_impl();

    boolean isThumbUp_impl();

    Bundle toBundle_impl();

    String toString_impl();
}
