package android.view;

import android.animation.TimeInterpolator;
import android.graphics.RecordingCanvas;
import android.graphics.RenderNode;
import com.android.internal.view.animation.FallbackLUTInterpolator;
import com.android.internal.view.animation.NativeInterpolatorFactory;
import com.android.internal.view.animation.NativeInterpolatorFactoryHelper;

public class RenderNodeAnimatorSetHelper {
    public static RenderNode getTarget(RecordingCanvas recordingCanvas) {
        return recordingCanvas.mNode;
    }

    public static long createNativeInterpolator(TimeInterpolator interpolator, long duration) {
        if (interpolator == null) {
            return NativeInterpolatorFactoryHelper.createLinearInterpolator();
        }
        if (RenderNodeAnimator.isNativeInterpolator(interpolator)) {
            return ((NativeInterpolatorFactory) interpolator).createNativeInterpolator();
        }
        return FallbackLUTInterpolator.createNativeInterpolator(interpolator, duration);
    }
}
