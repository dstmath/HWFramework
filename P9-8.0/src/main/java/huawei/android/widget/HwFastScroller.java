package huawei.android.widget;

import android.graphics.Rect;
import android.rms.iaware.AppTypeInfo;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.AbsListView;
import android.widget.FastScroller;
import android.widget.ImageView;
import android.widget.TextView;

public class HwFastScroller extends FastScroller {
    public HwFastScroller(AbsListView listView, int styleResId) {
        super(listView, styleResId);
    }

    protected void measureViewToSide(View view, View adjacent, Rect margins, Rect out) {
        int marginLeft;
        int marginTop;
        int marginRight;
        int maxWidth;
        int left;
        int right;
        int tmpTop;
        int tmpBottom;
        boolean isPreviewImage = false;
        if (view instanceof TextView) {
            isPreviewImage = true;
        }
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
        if (adjacent == null) {
            maxWidth = containerWidth;
        } else if (getLayoutFromRight()) {
            maxWidth = adjacent.getLeft();
        } else {
            maxWidth = containerWidth - adjacent.getRight();
        }
        view.measure(MeasureSpec.makeMeasureSpec((maxWidth - marginLeft) - marginRight, AppTypeInfo.APP_ATTRIBUTE_OVERSEA), MeasureSpec.makeMeasureSpec(0, 0));
        int width = view.getMeasuredWidth();
        int height = view.getMeasuredHeight();
        if (getLayoutFromRight()) {
            int tmpRight;
            int tmpLeft;
            if (isPreviewImage) {
                tmpRight = (containerWidth / 2) + (width / 2);
                tmpLeft = tmpRight - width;
            } else {
                tmpRight = (adjacent == null ? container.right : adjacent.getLeft()) - marginRight;
                tmpLeft = tmpRight - width;
            }
            left = tmpLeft;
            right = tmpRight;
        } else if (isPreviewImage) {
            right = (containerWidth / 2) + (width / 2);
            left = right - width;
        } else {
            left = (adjacent == null ? container.left : adjacent.getRight()) + marginLeft;
            right = left + width;
        }
        if (isPreviewImage) {
            tmpTop = (containerHeight / 2) - (height / 2);
            tmpBottom = tmpTop + height;
        } else {
            tmpTop = marginTop;
            tmpBottom = tmpTop + view.getMeasuredHeight();
        }
        int top = tmpTop;
        int bottom = tmpBottom;
        out.set(left, tmpTop, right, tmpBottom);
    }

    protected void setThumbPos(float position) {
        ImageView trackImage = getTrackImage();
        ImageView thumbImage = getThumbImage();
        float min = (float) trackImage.getTop();
        float offset = min;
        thumbImage.setTranslationY(((position * (((float) trackImage.getBottom()) - min)) + min) - (((float) thumbImage.getHeight()) / 2.0f));
    }
}
