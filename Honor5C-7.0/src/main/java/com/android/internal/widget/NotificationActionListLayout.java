package com.android.internal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.RemoteViews.RemoteView;
import android.widget.TextView;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;
import java.util.Comparator;

@RemoteView
public class NotificationActionListLayout extends ViewGroup {
    public static final Comparator<Pair<Integer, TextView>> MEASURE_ORDER_COMPARATOR = null;
    private ArrayList<View> mMeasureOrderOther;
    private ArrayList<Pair<Integer, TextView>> mMeasureOrderTextViews;
    private int mTotalWidth;

    final /* synthetic */ class -void__clinit___LambdaImpl0 implements Comparator {
        public int compare(Object arg0, Object arg1) {
            return ((Integer) ((Pair) arg0).first).compareTo((Integer) ((Pair) arg1).first);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.widget.NotificationActionListLayout.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.widget.NotificationActionListLayout.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.NotificationActionListLayout.<clinit>():void");
    }

    public NotificationActionListLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mTotalWidth = 0;
        this.mMeasureOrderTextViews = new ArrayList();
        this.mMeasureOrderOther = new ArrayList();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        int N = getChildCount();
        int textViews = 0;
        int otherViews = 0;
        int notGoneChildren = 0;
        View lastNotGoneChild = null;
        for (i = 0; i < N; i++) {
            View c = getChildAt(i);
            if (c instanceof TextView) {
                textViews++;
            } else {
                otherViews++;
            }
            if (c.getVisibility() != 8) {
                notGoneChildren++;
                lastNotGoneChild = c;
            }
        }
        boolean needRebuild = false;
        if (!(textViews == this.mMeasureOrderTextViews.size() && otherViews == this.mMeasureOrderOther.size())) {
            needRebuild = true;
        }
        if (!needRebuild) {
            int size = this.mMeasureOrderTextViews.size();
            for (i = 0; i < size; i++) {
                Pair<Integer, TextView> pair = (Pair) this.mMeasureOrderTextViews.get(i);
                if (((Integer) pair.first).intValue() != ((TextView) pair.second).getText().length()) {
                    needRebuild = true;
                }
            }
        }
        if (notGoneChildren > 1 && needRebuild) {
            rebuildMeasureOrder(textViews, otherViews);
        }
        boolean constrained = MeasureSpec.getMode(widthMeasureSpec) != 0;
        int innerWidth = (MeasureSpec.getSize(widthMeasureSpec) - this.mPaddingLeft) - this.mPaddingRight;
        int otherSize = this.mMeasureOrderOther.size();
        int usedWidth = 0;
        int measuredChildren = 0;
        for (i = 0; i < N && notGoneChildren > 1; i++) {
            if (i < otherSize) {
                c = (View) this.mMeasureOrderOther.get(i);
            } else {
                c = (View) ((Pair) this.mMeasureOrderTextViews.get(i - otherSize)).second;
            }
            if (c.getVisibility() != 8) {
                MarginLayoutParams lp = (MarginLayoutParams) c.getLayoutParams();
                int usedWidthForChild = usedWidth;
                if (constrained) {
                    usedWidthForChild = innerWidth - ((innerWidth - usedWidth) / (notGoneChildren - measuredChildren));
                }
                measureChildWithMargins(c, widthMeasureSpec, usedWidthForChild, heightMeasureSpec, 0);
                usedWidth += (c.getMeasuredWidth() + lp.rightMargin) + lp.leftMargin;
                measuredChildren++;
            }
        }
        if (lastNotGoneChild != null) {
            if (!constrained || usedWidth >= innerWidth) {
                if (notGoneChildren == 1) {
                }
            }
            lp = (MarginLayoutParams) lastNotGoneChild.getLayoutParams();
            if (notGoneChildren > 1) {
                usedWidth -= (lastNotGoneChild.getMeasuredWidth() + lp.rightMargin) + lp.leftMargin;
            }
            measureChildWithMargins(lastNotGoneChild, widthMeasureSpec, usedWidth, heightMeasureSpec, 0);
            usedWidth += (lastNotGoneChild.getMeasuredWidth() + lp.rightMargin) + lp.leftMargin;
        }
        this.mTotalWidth = (this.mPaddingRight + usedWidth) + this.mPaddingLeft;
        setMeasuredDimension(View.resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec), View.resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec));
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

    public void onViewAdded(View child) {
        super.onViewAdded(child);
        clearMeasureOrder();
    }

    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        clearMeasureOrder();
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childLeft;
        boolean isLayoutRtl = isLayoutRtl();
        int paddingTop = this.mPaddingTop;
        int innerHeight = ((bottom - top) - paddingTop) - this.mPaddingBottom;
        int count = getChildCount();
        switch (Gravity.getAbsoluteGravity(Gravity.START, getLayoutDirection())) {
            case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                int i = this.mPaddingLeft;
                childLeft = ((r0 + right) - left) - this.mTotalWidth;
                break;
            default:
                childLeft = this.mPaddingLeft;
                break;
        }
        int start = 0;
        int dir = 1;
        if (isLayoutRtl) {
            start = count - 1;
            dir = -1;
        }
        for (int i2 = 0; i2 < count; i2++) {
            View child = getChildAt(start + (dir * i2));
            if (child.getVisibility() != 8) {
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                int i3 = lp.topMargin;
                int childTop = ((((innerHeight - childHeight) / 2) + paddingTop) + r0) - lp.bottomMargin;
                childLeft += lp.leftMargin;
                child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
                childLeft += lp.rightMargin + childWidth;
            }
        }
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(-2, -1);
    }

    protected LayoutParams generateLayoutParams(LayoutParams p) {
        if (p instanceof MarginLayoutParams) {
            return new MarginLayoutParams((MarginLayoutParams) p);
        }
        return new MarginLayoutParams(p);
    }

    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }
}
