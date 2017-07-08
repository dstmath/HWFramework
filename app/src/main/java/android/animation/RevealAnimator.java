package android.animation;

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
        this.mClipView.setRevealClip(false, 0.0f, 0.0f, 0.0f);
        super.onFinished();
    }
}
