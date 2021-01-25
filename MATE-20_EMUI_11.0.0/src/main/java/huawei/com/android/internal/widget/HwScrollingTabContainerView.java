package huawei.com.android.internal.widget;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.widget.TextView;
import com.android.internal.widget.ScrollingTabContainerView;

public class HwScrollingTabContainerView extends ScrollingTabContainerView {
    public HwScrollingTabContainerView(Context context) {
        super(context);
    }

    public void setShouldAnimToTab(boolean shouldAnim) {
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration newConfig) {
        HwScrollingTabContainerView.super.onConfigurationChanged(newConfig);
    }

    public void updateTabViewContainerWidth(Context context) {
    }

    public void onAttachedToWindow() {
        HwScrollingTabContainerView.super.onAttachedToWindow();
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        HwScrollingTabContainerView.super.onLayout(isChanged, left, top, right, bottom);
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        HwScrollingTabContainerView.super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /* access modifiers changed from: protected */
    public void handlePressed(View view, boolean isPressed) {
    }

    public void setTabScrollingOffsets(int position, float offset) {
    }

    public void removeTabAt(int position) {
        HwScrollingTabContainerView.super.removeTabAt(position);
    }

    public void removeAllTabs() {
        HwScrollingTabContainerView.super.removeAllTabs();
    }

    public void addTab(ActionBar.Tab tab, boolean isSetSelected) {
        HwScrollingTabContainerView.super.addTab(tab, isSetSelected);
    }

    public void addTab(ActionBar.Tab tab, int position, boolean isSetSelected) {
        HwScrollingTabContainerView.super.addTab(tab, position, isSetSelected);
    }

    /* access modifiers changed from: protected */
    public boolean disableMaxTabWidth() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void handleTabViewCreated(ScrollingTabContainerView.TabView tabView) {
    }

    /* access modifiers changed from: protected */
    public void handleTabClicked(int position) {
    }

    /* access modifiers changed from: protected */
    public void initTitleAppearance(TextView textView) {
    }

    /* access modifiers changed from: protected */
    public int adjustPadding(int availableWidth, int itemPaddingSize) {
        return 0;
    }

    public void setTabSelected(int position) {
        HwScrollingTabContainerView.super.setTabSelected(position);
    }

    class TabIndicatorAnimation {
        TabIndicatorAnimation(View view) {
        }

        public void setViewWidth(int width) {
        }

        public void startAnim(int from, int to) {
        }

        public void cancelAnim() {
        }

        public boolean isAnimEnd() {
            return false;
        }
    }
}
