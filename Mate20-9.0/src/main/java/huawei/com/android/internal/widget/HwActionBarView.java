package huawei.com.android.internal.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ActionMenuPresenter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.widget.ActionBarView;

public class HwActionBarView extends ActionBarView {

    private static class ButtonState {
        public float mAlpha1;
        public float mAlpha2;
        public boolean mEnabled1;
        public boolean mEnabled2;
        public boolean mUsed1;
        public boolean mUsed2;

        private ButtonState() {
        }
    }

    public static class HwHomeView extends ActionBarView.HomeView {
        public HwHomeView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public void setShowUp(boolean isUp) {
        }

        public void setShowIcon(boolean showIcon) {
            HwActionBarView.super.setShowIcon(false);
        }

        public int getStartOffset() {
            return 0;
        }

        /* access modifiers changed from: protected */
        public void layoutUpView(View view, int upLeft, int upTop, int upRight, int upBottom, int leftMargin, int upOffset) {
        }
    }

    public HwActionBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* access modifiers changed from: protected */
    public int measureChildView(View child, int availableWidth, int childSpecHeight, int spacing) {
        return HwActionBarView.super.measureChildView(child, availableWidth, childSpecHeight, spacing);
    }

    /* access modifiers changed from: protected */
    public LinearLayout initTitleLayout() {
        return null;
    }

    /* access modifiers changed from: protected */
    public void initTitleIcons() {
    }

    /* access modifiers changed from: protected */
    public void initTitleIcons(LinearLayout titleLayout) {
    }

    public void setTitle(CharSequence title) {
    }

    public void setCustomTitle(View view) {
    }

    public void setStartIconVisible(boolean icon1Visible) {
    }

    public void setEndIconVisible(boolean icon2Visible) {
    }

    public void setStartContentDescription(CharSequence contentDescription) {
    }

    public void setEndContentDescription(CharSequence contentDescription) {
    }

    public void triggerIconsVisible(boolean icon1Visible, boolean icon2Visible) {
    }

    public void setStartIconImage(Drawable icon1) {
    }

    public void setEndIconImage(Drawable icon2) {
    }

    public void setStartIconListener(View.OnClickListener listener1) {
    }

    public void setEndIconListener(View.OnClickListener listener2) {
    }

    /* access modifiers changed from: protected */
    public ActionMenuPresenter initActionMenuPresenter(Context context) {
        return null;
    }

    /* access modifiers changed from: protected */
    public void deleteExpandedHomeIfNeed() {
    }

    public void onConfigurationChanged(Configuration newConfig) {
    }

    public void saveState(ButtonState state) {
    }

    public void restoreState(ButtonState saved) {
    }

    /* access modifiers changed from: protected */
    public void initTitleAppearance() {
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        HwActionBarView.super.onFinishInflate();
    }

    public void invalidateAllViews() {
    }

    public void setSplitViewLocation(int start, int end) {
    }

    /* access modifiers changed from: protected */
    public void updateSplitLocation() {
    }

    public static void invalidateTitleLayout(int gravity, int margin, TextView title, TextView subTitle) {
    }

    public static void initTitleAppearance(Context context, TextView title, TextView subTitle) {
    }

    public void setSmartColor(ColorStateList iconColor, ColorStateList titleColor) {
    }
}
