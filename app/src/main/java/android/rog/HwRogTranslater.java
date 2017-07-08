package android.rog;

import android.graphics.Rect;
import android.view.WindowManager.LayoutParams;

public class HwRogTranslater {
    public final float applicationInvertedScale;
    public final float applicationScale;

    public HwRogTranslater(float applicationScale, float applicationInvertedScale) {
        this.applicationScale = applicationScale;
        this.applicationInvertedScale = applicationInvertedScale;
    }

    public void translateRectInScreenToAppWinFrame(Rect rect) {
        rect.scale(this.applicationInvertedScale);
    }

    public void translateWindowLayout(LayoutParams params) {
        params.scale(this.applicationScale);
    }

    public void translateRectInAppWindowToScreen(Rect rect) {
        rect.scale(this.applicationScale);
    }
}
