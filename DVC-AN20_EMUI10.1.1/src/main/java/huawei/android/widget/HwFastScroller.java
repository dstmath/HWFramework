package huawei.android.widget;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FastScrollerEx;
import android.widget.ImageView;
import android.widget.TextView;

public class HwFastScroller extends FastScrollerEx {
    private static final int DICHOTOMY_SIZE = 2;

    public /* bridge */ /* synthetic */ int getWidth() {
        return HwFastScroller.super.getWidth();
    }

    public /* bridge */ /* synthetic */ boolean isAlwaysShowEnabled() {
        return HwFastScroller.super.isAlwaysShowEnabled();
    }

    public /* bridge */ /* synthetic */ boolean isEnabled() {
        return HwFastScroller.super.isEnabled();
    }

    public /* bridge */ /* synthetic */ boolean onInterceptHoverEvent(MotionEvent x0) {
        return HwFastScroller.super.onInterceptHoverEvent(x0);
    }

    @UnsupportedAppUsage
    public /* bridge */ /* synthetic */ boolean onInterceptTouchEvent(MotionEvent x0) {
        return HwFastScroller.super.onInterceptTouchEvent(x0);
    }

    public /* bridge */ /* synthetic */ void onItemCountChanged(int x0, int x1) {
        HwFastScroller.super.onItemCountChanged(x0, x1);
    }

    public /* bridge */ /* synthetic */ PointerIcon onResolvePointerIcon(MotionEvent x0, int x1) {
        return HwFastScroller.super.onResolvePointerIcon(x0, x1);
    }

    public /* bridge */ /* synthetic */ void onScroll(int x0, int x1, int x2) {
        HwFastScroller.super.onScroll(x0, x1, x2);
    }

    public /* bridge */ /* synthetic */ void onSectionsChanged() {
        HwFastScroller.super.onSectionsChanged();
    }

    @UnsupportedAppUsage
    public /* bridge */ /* synthetic */ void onSizeChanged(int x0, int x1, int x2, int x3) {
        HwFastScroller.super.onSizeChanged(x0, x1, x2, x3);
    }

    @UnsupportedAppUsage
    public /* bridge */ /* synthetic */ boolean onTouchEvent(MotionEvent x0) {
        return HwFastScroller.super.onTouchEvent(x0);
    }

    @UnsupportedAppUsage
    public /* bridge */ /* synthetic */ void remove() {
        HwFastScroller.super.remove();
    }

    public /* bridge */ /* synthetic */ void setAlwaysShow(boolean x0) {
        HwFastScroller.super.setAlwaysShow(x0);
    }

    public /* bridge */ /* synthetic */ void setEnabled(boolean x0) {
        HwFastScroller.super.setEnabled(x0);
    }

    public /* bridge */ /* synthetic */ void setScrollBarStyle(int x0) {
        HwFastScroller.super.setScrollBarStyle(x0);
    }

    public /* bridge */ /* synthetic */ void setScrollbarPosition(int x0) {
        HwFastScroller.super.setScrollbarPosition(x0);
    }

    public /* bridge */ /* synthetic */ void setStyle(int x0) {
        HwFastScroller.super.setStyle(x0);
    }

    public /* bridge */ /* synthetic */ void stop() {
        HwFastScroller.super.stop();
    }

    public /* bridge */ /* synthetic */ void updateLayout() {
        HwFastScroller.super.updateLayout();
    }

    public HwFastScroller(AbsListView listView, int styleResId) {
        super(listView, styleResId);
    }

    /* access modifiers changed from: protected */
    public void measureViewToSide(View view, View adjacent, Rect margins, Rect out) {
        int marginRight;
        int marginTop;
        int marginLeft;
        int right;
        int left;
        int marginLeft2;
        int top;
        if (margins == null) {
            marginLeft = 0;
            marginTop = 0;
            marginRight = 0;
        } else {
            marginLeft = margins.left;
            marginTop = margins.top;
            marginRight = margins.right;
        }
        Rect container = getContainerRect();
        int containerWidth = container.width();
        int containerHeight = container.height();
        view.measure(View.MeasureSpec.makeMeasureSpec((getMaxWidth(adjacent, containerWidth) - marginLeft) - marginRight, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(0, 0));
        int width = view.getMeasuredWidth();
        int height = view.getMeasuredHeight();
        boolean isPreviewImage = view instanceof TextView;
        if (isPreviewImage) {
            int right2 = (containerWidth / 2) + (width / 2);
            right = right2;
            left = right2 - width;
        } else if (getLayoutFromRight()) {
            int right3 = (adjacent == null ? container.right : adjacent.getLeft()) - marginRight;
            right = right3;
            left = right3 - width;
        } else {
            int left2 = (adjacent == null ? container.left : adjacent.getRight()) + marginLeft;
            right = left2 + width;
            left = left2;
        }
        if (isPreviewImage) {
            int top2 = (containerHeight / 2) - (height / 2);
            top = top2;
            marginLeft2 = top2 + height;
        } else {
            top = marginTop;
            marginLeft2 = marginTop + view.getMeasuredHeight();
        }
        out.set(left, top, right, marginLeft2);
    }

    private int getMaxWidth(View adjacent, int containerWidth) {
        if (adjacent == null) {
            return containerWidth;
        }
        if (getLayoutFromRight()) {
            return adjacent.getLeft();
        }
        return containerWidth - adjacent.getRight();
    }

    /* access modifiers changed from: protected */
    public void setThumbPos(float position) {
        ImageView trackImage = getTrackImage();
        ImageView thumbImage = getThumbImage();
        float min = (float) trackImage.getTop();
        thumbImage.setTranslationY(((position * (((float) trackImage.getBottom()) - min)) + min) - (((float) thumbImage.getHeight()) / 2.0f));
    }
}
