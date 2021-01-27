package android.view;

import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import com.android.hwext.internal.R;
import com.huawei.android.app.HwActivityManager;

public abstract class ViewOutlineProvider {
    public static final ViewOutlineProvider BACKGROUND = new ViewOutlineProvider() {
        /* class android.view.ViewOutlineProvider.AnonymousClass1 */

        @Override // android.view.ViewOutlineProvider
        public void getOutline(View view, Outline outline) {
            Drawable background = view.getBackground();
            if (background != null) {
                background.getOutline(outline);
            } else {
                outline.setRect(0, 0, view.getWidth(), view.getHeight());
                outline.setAlpha(0.0f);
            }
            outline.setOutColor(0);
        }
    };
    public static final ViewOutlineProvider BOUNDS = new ViewOutlineProvider() {
        /* class android.view.ViewOutlineProvider.AnonymousClass2 */

        @Override // android.view.ViewOutlineProvider
        public void getOutline(View view, Outline outline) {
            outline.setRect(0, 0, view.getWidth(), view.getHeight());
            outline.setOutColor(0);
        }
    };
    public static final ViewOutlineProvider HW_FREEFORM_OUTLINE_PROVIDER = new ViewOutlineProvider() {
        /* class android.view.ViewOutlineProvider.AnonymousClass4 */

        @Override // android.view.ViewOutlineProvider
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), view.getResources().getFloat(R.dimen.hw_freeform_corner_radius));
            outline.setOutColor(0);
        }
    };
    public static final ViewOutlineProvider HW_MULTIWINDOW_FREEFORM_OUTLINE_PROVIDER = new ViewOutlineProvider() {
        /* class android.view.ViewOutlineProvider.AnonymousClass5 */

        @Override // android.view.ViewOutlineProvider
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), (float) view.getResources().getDimensionPixelSize(R.dimen.hw_multiwindow_freeform_corner_radius));
            outline.setOutColor(0);
        }
    };
    public static final ViewOutlineProvider HW_MULTIWINDOW_SPLITSCREEN_BLACK_OUTLINE_PROVIDER = new ViewOutlineProvider() {
        /* class android.view.ViewOutlineProvider.AnonymousClass8 */

        @Override // android.view.ViewOutlineProvider
        public void getOutline(View view, Outline outline) {
            float f;
            int width = view.getWidth();
            int height = view.getHeight();
            if (HwActivityManager.IS_PHONE) {
                f = 0.0f;
            } else {
                f = (float) view.getResources().getDimensionPixelSize(R.dimen.hw_multiwindow_splitscreen_corner_radius);
            }
            outline.setRoundRect(0, 0, width, height, f);
            if (!HwActivityManager.IS_PHONE) {
                outline.setOutColor(-16777216);
            } else {
                outline.setOutColor(0);
            }
        }
    };
    public static final ViewOutlineProvider HW_MULTIWINDOW_SPLITSCREEN_OUTLINE_PROVIDER = new ViewOutlineProvider() {
        /* class android.view.ViewOutlineProvider.AnonymousClass6 */

        @Override // android.view.ViewOutlineProvider
        public void getOutline(View view, Outline outline) {
            float f;
            int width = view.getWidth();
            int height = view.getHeight();
            if (HwActivityManager.IS_PHONE) {
                f = 0.0f;
            } else {
                f = (float) view.getResources().getDimensionPixelSize(R.dimen.hw_multiwindow_splitscreen_corner_radius);
            }
            outline.setRoundRect(0, 0, width, height, f);
            outline.setOutColor(0);
        }
    };
    public static final ViewOutlineProvider HW_TV_FREEFORM_OUTLINE_PROVIDER = new ViewOutlineProvider() {
        /* class android.view.ViewOutlineProvider.AnonymousClass7 */

        @Override // android.view.ViewOutlineProvider
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), (float) view.getResources().getDimensionPixelSize(R.dimen.hw_tv_freeform_corner_radius));
            outline.setOutColor(0);
        }
    };
    public static final ViewOutlineProvider PADDED_BOUNDS = new ViewOutlineProvider() {
        /* class android.view.ViewOutlineProvider.AnonymousClass3 */

        @Override // android.view.ViewOutlineProvider
        public void getOutline(View view, Outline outline) {
            outline.setRect(view.getPaddingLeft(), view.getPaddingTop(), view.getWidth() - view.getPaddingRight(), view.getHeight() - view.getPaddingBottom());
            outline.setOutColor(0);
        }
    };

    public abstract void getOutline(View view, Outline outline);
}
