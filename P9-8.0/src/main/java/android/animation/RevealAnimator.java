package android.animation;

import android.hardware.camera2.params.TonemapCurve;
import android.view.RenderNodeAnimator;
import android.view.View;

public class RevealAnimator extends RenderNodeAnimator {
    private View mClipView;

    public RevealAnimator(View clipView, int x, int y, float startRadius, float endRadius) {
        super(x, y, startRadius, endRadius);
        this.mClipView = clipView;
        setTarget(this.mClipView);
    }

    protected void onFinished() {
        this.mClipView.setRevealClip(false, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK);
        super.onFinished();
    }
}
