package android.support.v4.app;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.util.Pair;
import android.view.View;

public class ActivityOptionsCompat {
    public static final String EXTRA_USAGE_TIME_REPORT = "android.activity.usage_time";
    public static final String EXTRA_USAGE_TIME_REPORT_PACKAGES = "android.usage_time_packages";

    @RequiresApi(16)
    private static class ActivityOptionsCompatApi16Impl extends ActivityOptionsCompat {
        protected final ActivityOptions mActivityOptions;

        ActivityOptionsCompatApi16Impl(ActivityOptions activityOptions) {
            this.mActivityOptions = activityOptions;
        }

        public Bundle toBundle() {
            return this.mActivityOptions.toBundle();
        }

        public void update(ActivityOptionsCompat otherOptions) {
            if (otherOptions instanceof ActivityOptionsCompatApi16Impl) {
                this.mActivityOptions.update(((ActivityOptionsCompatApi16Impl) otherOptions).mActivityOptions);
            }
        }
    }

    @RequiresApi(23)
    private static class ActivityOptionsCompatApi23Impl extends ActivityOptionsCompatApi16Impl {
        ActivityOptionsCompatApi23Impl(ActivityOptions activityOptions) {
            super(activityOptions);
        }

        public void requestUsageTimeReport(PendingIntent receiver) {
            this.mActivityOptions.requestUsageTimeReport(receiver);
        }
    }

    @RequiresApi(24)
    private static class ActivityOptionsCompatApi24Impl extends ActivityOptionsCompatApi23Impl {
        ActivityOptionsCompatApi24Impl(ActivityOptions activityOptions) {
            super(activityOptions);
        }

        public ActivityOptionsCompat setLaunchBounds(@Nullable Rect screenSpacePixelRect) {
            return new ActivityOptionsCompatApi24Impl(this.mActivityOptions.setLaunchBounds(screenSpacePixelRect));
        }

        public Rect getLaunchBounds() {
            return this.mActivityOptions.getLaunchBounds();
        }
    }

    public static ActivityOptionsCompat makeCustomAnimation(Context context, int enterResId, int exitResId) {
        if (VERSION.SDK_INT >= 16) {
            return createImpl(ActivityOptions.makeCustomAnimation(context, enterResId, exitResId));
        }
        return new ActivityOptionsCompat();
    }

    public static ActivityOptionsCompat makeScaleUpAnimation(View source, int startX, int startY, int startWidth, int startHeight) {
        if (VERSION.SDK_INT >= 16) {
            return createImpl(ActivityOptions.makeScaleUpAnimation(source, startX, startY, startWidth, startHeight));
        }
        return new ActivityOptionsCompat();
    }

    public static ActivityOptionsCompat makeClipRevealAnimation(View source, int startX, int startY, int width, int height) {
        if (VERSION.SDK_INT >= 23) {
            return createImpl(ActivityOptions.makeClipRevealAnimation(source, startX, startY, width, height));
        }
        return new ActivityOptionsCompat();
    }

    public static ActivityOptionsCompat makeThumbnailScaleUpAnimation(View source, Bitmap thumbnail, int startX, int startY) {
        if (VERSION.SDK_INT >= 16) {
            return createImpl(ActivityOptions.makeThumbnailScaleUpAnimation(source, thumbnail, startX, startY));
        }
        return new ActivityOptionsCompat();
    }

    public static ActivityOptionsCompat makeSceneTransitionAnimation(Activity activity, View sharedElement, String sharedElementName) {
        if (VERSION.SDK_INT >= 21) {
            return createImpl(ActivityOptions.makeSceneTransitionAnimation(activity, sharedElement, sharedElementName));
        }
        return new ActivityOptionsCompat();
    }

    public static ActivityOptionsCompat makeSceneTransitionAnimation(Activity activity, Pair<View, String>... sharedElements) {
        if (VERSION.SDK_INT < 21) {
            return new ActivityOptionsCompat();
        }
        android.util.Pair<View, String>[] pairs = null;
        if (sharedElements != null) {
            pairs = new android.util.Pair[sharedElements.length];
            for (int i = 0; i < sharedElements.length; i++) {
                pairs[i] = android.util.Pair.create((View) sharedElements[i].first, (String) sharedElements[i].second);
            }
        }
        return createImpl(ActivityOptions.makeSceneTransitionAnimation(activity, pairs));
    }

    public static ActivityOptionsCompat makeTaskLaunchBehind() {
        if (VERSION.SDK_INT >= 21) {
            return createImpl(ActivityOptions.makeTaskLaunchBehind());
        }
        return new ActivityOptionsCompat();
    }

    public static ActivityOptionsCompat makeBasic() {
        if (VERSION.SDK_INT >= 23) {
            return createImpl(ActivityOptions.makeBasic());
        }
        return new ActivityOptionsCompat();
    }

    @RequiresApi(16)
    private static ActivityOptionsCompat createImpl(ActivityOptions options) {
        if (VERSION.SDK_INT >= 24) {
            return new ActivityOptionsCompatApi24Impl(options);
        }
        if (VERSION.SDK_INT >= 23) {
            return new ActivityOptionsCompatApi23Impl(options);
        }
        return new ActivityOptionsCompatApi16Impl(options);
    }

    protected ActivityOptionsCompat() {
    }

    public ActivityOptionsCompat setLaunchBounds(@Nullable Rect screenSpacePixelRect) {
        return null;
    }

    @Nullable
    public Rect getLaunchBounds() {
        return null;
    }

    public Bundle toBundle() {
        return null;
    }

    public void update(ActivityOptionsCompat otherOptions) {
    }

    public void requestUsageTimeReport(PendingIntent receiver) {
    }
}
