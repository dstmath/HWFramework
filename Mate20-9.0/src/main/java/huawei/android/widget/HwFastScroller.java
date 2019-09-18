package huawei.android.widget;

import android.graphics.Rect;
import android.rms.iaware.AppTypeInfo;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FastScrollerEx;
import android.widget.ImageView;
import android.widget.TextView;

public class HwFastScroller extends FastScrollerEx {
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

    public /* bridge */ /* synthetic */ void onSizeChanged(int x0, int x1, int x2, int x3) {
        HwFastScroller.super.onSizeChanged(x0, x1, x2, x3);
    }

    public /* bridge */ /* synthetic */ boolean onTouchEvent(MotionEvent x0) {
        return HwFastScroller.super.onTouchEvent(x0);
    }

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
        int maxWidth;
        int tmpLeft;
        int tmpLeft2;
        int tmpBottom;
        int tmpTop;
        int tmpRight;
        View view2 = view;
        Rect rect = margins;
        boolean isPreviewImage = false;
        if (view2 instanceof TextView) {
            isPreviewImage = true;
        }
        if (rect == null) {
            marginLeft = 0;
            marginTop = 0;
            marginRight = 0;
        } else {
            marginLeft = rect.left;
            marginTop = rect.top;
            marginRight = rect.right;
        }
        Rect container = getContainerRect();
        int containerWidth = container.width();
        int containerHeight = container.height();
        if (adjacent == null) {
            maxWidth = containerWidth;
        } else if (getLayoutFromRight() != 0) {
            maxWidth = adjacent.getLeft();
        } else {
            maxWidth = containerWidth - adjacent.getRight();
        }
        view2.measure(View.MeasureSpec.makeMeasureSpec((maxWidth - marginLeft) - marginRight, AppTypeInfo.APP_ATTRIBUTE_OVERSEA), View.MeasureSpec.makeMeasureSpec(0, 0));
        int width = view.getMeasuredWidth();
        int height = view.getMeasuredHeight();
        if (getLayoutFromRight()) {
            if (isPreviewImage) {
                tmpRight = (containerWidth / 2) + (width / 2);
                tmpLeft = tmpRight - width;
            } else {
                tmpRight = (adjacent == null ? container.right : adjacent.getLeft()) - marginRight;
                tmpLeft = tmpRight - width;
            }
            int i = tmpLeft;
            tmpLeft2 = tmpRight;
        } else if (isPreviewImage) {
            tmpLeft2 = (containerWidth / 2) + (width / 2);
            tmpLeft = tmpLeft2 - width;
        } else {
            tmpLeft = (adjacent == null ? container.left : adjacent.getRight()) + marginLeft;
            tmpLeft2 = tmpLeft + width;
        }
        int left = tmpLeft;
        if (isPreviewImage) {
            tmpTop = (containerHeight / 2) - (height / 2);
            tmpBottom = tmpTop + height;
        } else {
            tmpTop = marginTop;
            tmpBottom = tmpTop + view.getMeasuredHeight();
        }
        boolean z = isPreviewImage;
        int i2 = marginLeft;
        out.set(left, tmpTop, tmpLeft2, tmpBottom);
    }

    /* access modifiers changed from: protected */
    public void setThumbPos(float position) {
        ImageView trackImage = getTrackImage();
        ImageView thumbImage = getThumbImage();
        float min = (float) trackImage.getTop();
        thumbImage.setTranslationY(((position * (((float) trackImage.getBottom()) - min)) + min) - (((float) thumbImage.getHeight()) / 2.0f));
    }
}
