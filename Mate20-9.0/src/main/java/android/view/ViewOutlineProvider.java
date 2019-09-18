package android.view;

import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import com.android.hwext.internal.R;

public abstract class ViewOutlineProvider {
    public static final ViewOutlineProvider BACKGROUND = new ViewOutlineProvider() {
        public void getOutline(View view, Outline outline) {
            Drawable background = view.getBackground();
            if (background != null) {
                background.getOutline(outline);
                return;
            }
            outline.setRect(0, 0, view.getWidth(), view.getHeight());
            outline.setAlpha(0.0f);
        }
    };
    public static final ViewOutlineProvider BOUNDS = new ViewOutlineProvider() {
        public void getOutline(View view, Outline outline) {
            outline.setRect(0, 0, view.getWidth(), view.getHeight());
        }
    };
    public static final ViewOutlineProvider HW_FREEFORM_OUTLINE_PROVIDER = new ViewOutlineProvider() {
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), view.getResources().getFloat(R.dimen.hw_freeform_corner_radius));
        }
    };
    public static final ViewOutlineProvider PADDED_BOUNDS = new ViewOutlineProvider() {
        public void getOutline(View view, Outline outline) {
            outline.setRect(view.getPaddingLeft(), view.getPaddingTop(), view.getWidth() - view.getPaddingRight(), view.getHeight() - view.getPaddingBottom());
        }
    };

    public abstract void getOutline(View view, Outline outline);
}
