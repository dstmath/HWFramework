package android.support.v4.app;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.util.Pair;
import android.view.View;

public class ActivityOptionsCompat {
    public static final String EXTRA_USAGE_TIME_REPORT = "android.activity.usage_time";
    public static final String EXTRA_USAGE_TIME_REPORT_PACKAGES = "android.usage_time_packages";

    @NonNull
    public static ActivityOptionsCompat makeCustomAnimation(@NonNull Context context, int enterResId, int exitResId) {
        if (Build.VERSION.SDK_INT >= 16) {
            return new ActivityOptionsCompatImpl(ActivityOptions.makeCustomAnimation(context, enterResId, exitResId));
        }
        return new ActivityOptionsCompat();
    }

    @NonNull
    public static ActivityOptionsCompat makeScaleUpAnimation(@NonNull View source, int startX, int startY, int startWidth, int startHeight) {
        if (Build.VERSION.SDK_INT >= 16) {
            return new ActivityOptionsCompatImpl(ActivityOptions.makeScaleUpAnimation(source, startX, startY, startWidth, startHeight));
        }
        return new ActivityOptionsCompat();
    }

    @NonNull
    public static ActivityOptionsCompat makeClipRevealAnimation(@NonNull View source, int startX, int startY, int width, int height) {
        if (Build.VERSION.SDK_INT >= 23) {
            return new ActivityOptionsCompatImpl(ActivityOptions.makeClipRevealAnimation(source, startX, startY, width, height));
        }
        return new ActivityOptionsCompat();
    }

    @NonNull
    public static ActivityOptionsCompat makeThumbnailScaleUpAnimation(@NonNull View source, @NonNull Bitmap thumbnail, int startX, int startY) {
        if (Build.VERSION.SDK_INT >= 16) {
            return new ActivityOptionsCompatImpl(ActivityOptions.makeThumbnailScaleUpAnimation(source, thumbnail, startX, startY));
        }
        return new ActivityOptionsCompat();
    }

    @NonNull
    public static ActivityOptionsCompat makeSceneTransitionAnimation(@NonNull Activity activity, @NonNull View sharedElement, @NonNull String sharedElementName) {
        if (Build.VERSION.SDK_INT >= 21) {
            return new ActivityOptionsCompatImpl(ActivityOptions.makeSceneTransitionAnimation(activity, sharedElement, sharedElementName));
        }
        return new ActivityOptionsCompat();
    }

    @NonNull
    public static ActivityOptionsCompat makeSceneTransitionAnimation(@NonNull Activity activity, Pair<View, String>... sharedElements) {
        if (Build.VERSION.SDK_INT < 21) {
            return new ActivityOptionsCompat();
        }
        android.util.Pair<View, String>[] pairs = null;
        if (sharedElements != null) {
            pairs = new android.util.Pair[sharedElements.length];
            for (int i = 0; i < sharedElements.length; i++) {
                pairs[i] = android.util.Pair.create(sharedElements[i].first, sharedElements[i].second);
            }
        }
        return new ActivityOptionsCompatImpl(ActivityOptions.makeSceneTransitionAnimation(activity, pairs));
    }

    @NonNull
    public static ActivityOptionsCompat makeTaskLaunchBehind() {
        if (Build.VERSION.SDK_INT >= 21) {
            return new ActivityOptionsCompatImpl(ActivityOptions.makeTaskLaunchBehind());
        }
        return new ActivityOptionsCompat();
    }

    @NonNull
    public static ActivityOptionsCompat makeBasic() {
        if (Build.VERSION.SDK_INT >= 23) {
            return new ActivityOptionsCompatImpl(ActivityOptions.makeBasic());
        }
        return new ActivityOptionsCompat();
    }

    @RequiresApi(16)
    private static class ActivityOptionsCompatImpl extends ActivityOptionsCompat {
        private final ActivityOptions mActivityOptions;

        ActivityOptionsCompatImpl(ActivityOptions activityOptions) {
            this.mActivityOptions = activityOptions;
        }

        @Override // android.support.v4.app.ActivityOptionsCompat
        public Bundle toBundle() {
            return this.mActivityOptions.toBundle();
        }

        @Override // android.support.v4.app.ActivityOptionsCompat
        public void update(ActivityOptionsCompat otherOptions) {
            if (otherOptions instanceof ActivityOptionsCompatImpl) {
                this.mActivityOptions.update(((ActivityOptionsCompatImpl) otherOptions).mActivityOptions);
            }
        }

        @Override // android.support.v4.app.ActivityOptionsCompat
        public void requestUsageTimeReport(PendingIntent receiver) {
            if (Build.VERSION.SDK_INT >= 23) {
                this.mActivityOptions.requestUsageTimeReport(receiver);
            }
        }

        @Override // android.support.v4.app.ActivityOptionsCompat
        public ActivityOptionsCompat setLaunchBounds(@Nullable Rect screenSpacePixelRect) {
            if (Build.VERSION.SDK_INT < 24) {
                return this;
            }
            return new ActivityOptionsCompatImpl(this.mActivityOptions.setLaunchBounds(screenSpacePixelRect));
        }

        @Override // android.support.v4.app.ActivityOptionsCompat
        public Rect getLaunchBounds() {
            if (Build.VERSION.SDK_INT < 24) {
                return null;
            }
            return this.mActivityOptions.getLaunchBounds();
        }
    }

    protected ActivityOptionsCompat() {
    }

    @NonNull
    public ActivityOptionsCompat setLaunchBounds(@Nullable Rect screenSpacePixelRect) {
        return this;
    }

    @Nullable
    public Rect getLaunchBounds() {
        return null;
    }

    @Nullable
    public Bundle toBundle() {
        return null;
    }

    public void update(@NonNull ActivityOptionsCompat otherOptions) {
    }

    public void requestUsageTimeReport(@NonNull PendingIntent receiver) {
    }
}
