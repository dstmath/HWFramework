package com.android.internal.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.Comparator;

@RemoteViews.RemoteView
public class NotificationActionListLayout extends LinearLayout {
    public static final Comparator<Pair<Integer, TextView>> MEASURE_ORDER_COMPARATOR = $$Lambda$NotificationActionListLayout$uFZFEmIEBpI3kn6c3tNvvgmMSv8.INSTANCE;
    private int mDefaultPaddingBottom;
    private int mDefaultPaddingTop;
    private int mEmphasizedHeight;
    private boolean mEmphasizedMode;
    private final int mGravity;
    private ArrayList<View> mMeasureOrderOther;
    private ArrayList<Pair<Integer, TextView>> mMeasureOrderTextViews;
    private int mRegularHeight;
    private int mTotalWidth;

    public NotificationActionListLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NotificationActionListLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NotificationActionListLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mTotalWidth = 0;
        this.mMeasureOrderTextViews = new ArrayList<>();
        this.mMeasureOrderOther = new ArrayList<>();
        TypedArray ta = context.obtainStyledAttributes(attrs, new int[]{16842927}, defStyleAttr, defStyleRes);
        this.mGravity = ta.getInt(0, 0);
        ta.recycle();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        boolean needRebuild;
        View c;
        int i2;
        int usedWidthForChild;
        if (this.mEmphasizedMode) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int N = getChildCount();
        int i3 = 0;
        int textViews = 0;
        int otherViews = 0;
        int notGoneChildren = 0;
        while (true) {
            i = 8;
            if (i3 >= N) {
                break;
            }
            View c2 = getChildAt(i3);
            if (c2 instanceof TextView) {
                textViews++;
            } else {
                otherViews++;
            }
            if (c2.getVisibility() != 8) {
                notGoneChildren++;
            }
            i3++;
        }
        boolean needRebuild2 = false;
        if (!(textViews == this.mMeasureOrderTextViews.size() && otherViews == this.mMeasureOrderOther.size())) {
            needRebuild2 = true;
        }
        if (!needRebuild2) {
            int size = this.mMeasureOrderTextViews.size();
            for (int i4 = 0; i4 < size; i4++) {
                Pair<Integer, TextView> pair = this.mMeasureOrderTextViews.get(i4);
                if (pair.first.intValue() != pair.second.getText().length()) {
                    needRebuild2 = true;
                }
            }
            needRebuild = needRebuild2;
        } else {
            needRebuild = needRebuild2;
        }
        if (needRebuild) {
            rebuildMeasureOrder(textViews, otherViews);
        }
        boolean constrained = View.MeasureSpec.getMode(widthMeasureSpec) != 0;
        int innerWidth = (View.MeasureSpec.getSize(widthMeasureSpec) - this.mPaddingLeft) - this.mPaddingRight;
        int otherSize = this.mMeasureOrderOther.size();
        int usedWidth = 0;
        int measuredChildren = 0;
        int i5 = 0;
        while (i5 < N) {
            if (i5 < otherSize) {
                c = this.mMeasureOrderOther.get(i5);
            } else {
                c = this.mMeasureOrderTextViews.get(i5 - otherSize).second;
            }
            if (c.getVisibility() == i) {
                i2 = i5;
            } else {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) c.getLayoutParams();
                if (constrained) {
                    usedWidthForChild = innerWidth - ((innerWidth - usedWidth) / (notGoneChildren - measuredChildren));
                } else {
                    usedWidthForChild = usedWidth;
                }
                i2 = i5;
                measureChildWithMargins(c, widthMeasureSpec, usedWidthForChild, heightMeasureSpec, 0);
                usedWidth += c.getMeasuredWidth() + lp.rightMargin + lp.leftMargin;
                measuredChildren++;
            }
            i5 = i2 + 1;
            i = 8;
        }
        this.mTotalWidth = usedWidth + this.mPaddingRight + this.mPaddingLeft;
        setMeasuredDimension(resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec), resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    private void rebuildMeasureOrder(int capacityText, int capacityOther) {
        clearMeasureOrder();
        this.mMeasureOrderTextViews.ensureCapacity(capacityText);
        this.mMeasureOrderOther.ensureCapacity(capacityOther);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View c = getChildAt(i);
            if (!(c instanceof TextView) || ((TextView) c).getText().length() <= 0) {
                this.mMeasureOrderOther.add(c);
            } else {
                this.mMeasureOrderTextViews.add(Pair.create(Integer.valueOf(((TextView) c).getText().length()), (TextView) c));
            }
        }
        this.mMeasureOrderTextViews.sort(MEASURE_ORDER_COMPARATOR);
    }

    private void clearMeasureOrder() {
        this.mMeasureOrderOther.clear();
        this.mMeasureOrderTextViews.clear();
    }

    @Override // android.view.ViewGroup
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        clearMeasureOrder();
        if (child.getBackground() instanceof RippleDrawable) {
            ((RippleDrawable) child.getBackground()).setForceSoftware(true);
        }
    }

    @Override // android.view.ViewGroup
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        clearMeasureOrder();
    }

    /* JADX INFO: Multiple debug info for r5v0 int: [D('height' int), D('absoluteGravity' int)] */
    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childLeft;
        int paddingTop;
        boolean isLayoutRtl;
        NotificationActionListLayout notificationActionListLayout = this;
        if (notificationActionListLayout.mEmphasizedMode) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }
        boolean isLayoutRtl2 = isLayoutRtl();
        int paddingTop2 = notificationActionListLayout.mPaddingTop;
        boolean centerAligned = true;
        if ((notificationActionListLayout.mGravity & 1) == 0) {
            centerAligned = false;
        }
        if (centerAligned) {
            childLeft = ((notificationActionListLayout.mPaddingLeft + left) + ((right - left) / 2)) - (notificationActionListLayout.mTotalWidth / 2);
        } else {
            childLeft = notificationActionListLayout.mPaddingLeft;
            if (Gravity.getAbsoluteGravity(Gravity.START, getLayoutDirection()) == 5) {
                childLeft += (right - left) - notificationActionListLayout.mTotalWidth;
            }
        }
        int innerHeight = ((bottom - top) - paddingTop2) - notificationActionListLayout.mPaddingBottom;
        int count = getChildCount();
        int start = 0;
        int dir = 1;
        if (isLayoutRtl2) {
            start = count - 1;
            dir = -1;
        }
        int i = 0;
        while (i < count) {
            View child = notificationActionListLayout.getChildAt((dir * i) + start);
            if (child.getVisibility() != 8) {
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
                int childTop = ((paddingTop2 + ((innerHeight - childHeight) / 2)) + lp.topMargin) - lp.bottomMargin;
                isLayoutRtl = isLayoutRtl2;
                int childLeft2 = childLeft + lp.leftMargin;
                paddingTop = paddingTop2;
                child.layout(childLeft2, childTop, childLeft2 + childWidth, childTop + childHeight);
                childLeft = childLeft2 + lp.rightMargin + childWidth;
            } else {
                isLayoutRtl = isLayoutRtl2;
                paddingTop = paddingTop2;
            }
            i++;
            notificationActionListLayout = this;
            isLayoutRtl2 = isLayoutRtl;
            paddingTop2 = paddingTop;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mDefaultPaddingBottom = getPaddingBottom();
        this.mDefaultPaddingTop = getPaddingTop();
        updateHeights();
    }

    private void updateHeights() {
        this.mEmphasizedHeight = getResources().getDimensionPixelSize(R.dimen.notification_content_margin_end) + getResources().getDimensionPixelSize(R.dimen.notification_content_margin) + getResources().getDimensionPixelSize(R.dimen.notification_action_emphasized_height);
        this.mRegularHeight = getResources().getDimensionPixelSize(R.dimen.notification_action_list_height);
    }

    @RemotableViewMethod
    public void setEmphasizedMode(boolean emphasizedMode) {
        int height;
        this.mEmphasizedMode = emphasizedMode;
        if (emphasizedMode) {
            int paddingTop = getResources().getDimensionPixelSize(R.dimen.notification_content_margin);
            int paddingBottom = getResources().getDimensionPixelSize(R.dimen.notification_content_margin_end);
            height = this.mEmphasizedHeight;
            int buttonPaddingInternal = getResources().getDimensionPixelSize(R.dimen.button_inset_vertical_material);
            setPaddingRelative(getPaddingStart(), paddingTop - buttonPaddingInternal, getPaddingEnd(), paddingBottom - buttonPaddingInternal);
        } else {
            setPaddingRelative(getPaddingStart(), this.mDefaultPaddingTop, getPaddingEnd(), this.mDefaultPaddingBottom);
            height = this.mRegularHeight;
        }
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = height;
        setLayoutParams(layoutParams);
    }

    public int getExtraMeasureHeight() {
        if (this.mEmphasizedMode) {
            return this.mEmphasizedHeight - this.mRegularHeight;
        }
        return 0;
    }
}
