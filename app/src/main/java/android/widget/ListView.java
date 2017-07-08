package android.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Trace;
import android.util.AttributeSet;
import android.util.Jlog;
import android.util.MathUtils;
import android.util.SparseBooleanArray;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.RemotableViewMethod;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewGroup;
import android.view.ViewHierarchyEncoder;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.accessibility.AccessibilityNodeInfo.CollectionInfo;
import android.view.accessibility.AccessibilityNodeInfo.CollectionItemInfo;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView.LayoutParams;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.os.HwBootFail;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.Predicate;
import com.android.internal.util.Protocol;
import com.google.android.collect.Lists;
import com.hisi.perfhub.PerfHub;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.pgmng.log.LogPower;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;
import java.util.List;

@RemoteView
public class ListView extends HwAbsListView {
    private static final float MAX_SCROLL_FACTOR = 0.33f;
    private static final int MIN_SCROLL_PREVIEW_PIXELS = 2;
    static final int NO_POSITION = -1;
    private boolean mAreAllItemsSelectable;
    private final ArrowScrollFocusResult mArrowScrollFocusResult;
    Drawable mDivider;
    int mDividerHeight;
    private boolean mDividerIsOpaque;
    private Paint mDividerPaint;
    private FocusSelector mFocusSelector;
    private boolean mFooterDividersEnabled;
    private ArrayList<FixedViewInfo> mFooterViewInfos;
    private boolean mHeaderDividersEnabled;
    private ArrayList<FixedViewInfo> mHeaderViewInfos;
    private boolean mIsCacheColorOpaque;
    private boolean mItemsCanFocus;
    Drawable mOverScrollFooter;
    Drawable mOverScrollHeader;
    private final Rect mTempRect;

    private static class ArrowScrollFocusResult {
        private int mAmountToScroll;
        private int mSelectedPosition;

        private ArrowScrollFocusResult() {
        }

        void populate(int selectedPosition, int amountToScroll) {
            this.mSelectedPosition = selectedPosition;
            this.mAmountToScroll = amountToScroll;
        }

        public int getSelectedPosition() {
            return this.mSelectedPosition;
        }

        public int getAmountToScroll() {
            return this.mAmountToScroll;
        }
    }

    public class FixedViewInfo {
        public Object data;
        public boolean isSelectable;
        public View view;
    }

    private class FocusSelector implements Runnable {
        private static final int STATE_REQUEST_FOCUS = 3;
        private static final int STATE_SET_SELECTION = 1;
        private static final int STATE_WAIT_FOR_LAYOUT = 2;
        private int mAction;
        private int mPosition;
        private int mPositionTop;

        private FocusSelector() {
        }

        FocusSelector setupForSetSelection(int position, int top) {
            this.mPosition = position;
            this.mPositionTop = top;
            this.mAction = STATE_SET_SELECTION;
            return this;
        }

        public void run() {
            if (this.mAction == STATE_SET_SELECTION) {
                ListView.this.setSelectionFromTop(this.mPosition, this.mPositionTop);
                this.mAction = STATE_WAIT_FOR_LAYOUT;
            } else if (this.mAction == STATE_REQUEST_FOCUS) {
                View child = ListView.this.getChildAt(this.mPosition - ListView.this.mFirstPosition);
                if (child != null) {
                    child.requestFocus();
                }
                this.mAction = ListView.NO_POSITION;
            }
        }

        Runnable setupFocusIfValid(int position) {
            if (this.mAction != STATE_WAIT_FOR_LAYOUT || position != this.mPosition) {
                return null;
            }
            this.mAction = STATE_REQUEST_FOCUS;
            return this;
        }

        void onLayoutComplete() {
            if (this.mAction == STATE_WAIT_FOR_LAYOUT) {
                this.mAction = ListView.NO_POSITION;
            }
        }
    }

    protected void layoutChildren() {
        /* JADX: method processing error */
/*
        Error: java.lang.OutOfMemoryError: Java heap space
	at java.util.Arrays.copyOf(Arrays.java:3181)
	at java.util.ArrayList.grow(ArrayList.java:261)
	at java.util.ArrayList.ensureExplicitCapacity(ArrayList.java:235)
	at java.util.ArrayList.ensureCapacityInternal(ArrayList.java:227)
	at java.util.ArrayList.add(ArrayList.java:458)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:447)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:448)
*/
        /*
        r42 = this;
        r0 = r42;
        r13 = r0.mBlockLayoutRequests;
        if (r13 == 0) goto L_0x0007;
    L_0x0006:
        return;
    L_0x0007:
        r4 = 1;
        r0 = r42;
        r0.mBlockLayoutRequests = r4;
        super.layoutChildren();	 Catch:{ all -> 0x0144 }
        r42.invalidate();	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mAdapter;	 Catch:{ all -> 0x0144 }
        if (r4 != 0) goto L_0x0033;	 Catch:{ all -> 0x0144 }
    L_0x0018:
        r42.resetList();	 Catch:{ all -> 0x0144 }
        r42.invokeOnItemScrollListener();	 Catch:{ all -> 0x0144 }
        r0 = r42;
        r4 = r0.mFocusSelector;
        if (r4 == 0) goto L_0x002b;
    L_0x0024:
        r0 = r42;
        r4 = r0.mFocusSelector;
        r4.onLayoutComplete();
    L_0x002b:
        if (r13 != 0) goto L_0x0032;
    L_0x002d:
        r4 = 0;
        r0 = r42;
        r0.mBlockLayoutRequests = r4;
    L_0x0032:
        return;
    L_0x0033:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mListPadding;	 Catch:{ all -> 0x0144 }
        r8 = r4.top;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mBottom;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0 = r0.mTop;	 Catch:{ all -> 0x0144 }
        r39 = r0;	 Catch:{ all -> 0x0144 }
        r4 = r4 - r39;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0 = r0.mListPadding;	 Catch:{ all -> 0x0144 }
        r39 = r0;	 Catch:{ all -> 0x0144 }
        r0 = r39;	 Catch:{ all -> 0x0144 }
        r0 = r0.bottom;	 Catch:{ all -> 0x0144 }
        r39 = r0;	 Catch:{ all -> 0x0144 }
        r9 = r4 - r39;	 Catch:{ all -> 0x0144 }
        r15 = r42.getChildCount();	 Catch:{ all -> 0x0144 }
        r28 = 0;	 Catch:{ all -> 0x0144 }
        r7 = 0;	 Catch:{ all -> 0x0144 }
        r5 = 0;	 Catch:{ all -> 0x0144 }
        r30 = 0;	 Catch:{ all -> 0x0144 }
        r6 = 0;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mLayoutMode;	 Catch:{ all -> 0x0144 }
        switch(r4) {
            case 1: goto L_0x00a0;
            case 2: goto L_0x00cc;
            case 3: goto L_0x00a0;
            case 4: goto L_0x00a0;
            case 5: goto L_0x00a0;
            default: goto L_0x0065;
        };	 Catch:{ all -> 0x0144 }
    L_0x0065:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mSelectedPosition;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0 = r0.mFirstPosition;	 Catch:{ all -> 0x0144 }
        r39 = r0;	 Catch:{ all -> 0x0144 }
        r28 = r4 - r39;	 Catch:{ all -> 0x0144 }
        if (r28 < 0) goto L_0x007f;	 Catch:{ all -> 0x0144 }
    L_0x0073:
        r0 = r28;	 Catch:{ all -> 0x0144 }
        if (r0 >= r15) goto L_0x007f;	 Catch:{ all -> 0x0144 }
    L_0x0077:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r1 = r28;	 Catch:{ all -> 0x0144 }
        r5 = r0.getChildAt(r1);	 Catch:{ all -> 0x0144 }
    L_0x007f:
        r4 = 0;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r30 = r0.getChildAt(r4);	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mNextSelectedPosition;	 Catch:{ all -> 0x0144 }
        if (r4 < 0) goto L_0x0098;	 Catch:{ all -> 0x0144 }
    L_0x008c:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mNextSelectedPosition;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0 = r0.mSelectedPosition;	 Catch:{ all -> 0x0144 }
        r39 = r0;	 Catch:{ all -> 0x0144 }
        r7 = r4 - r39;	 Catch:{ all -> 0x0144 }
    L_0x0098:
        r4 = r28 + r7;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r6 = r0.getChildAt(r4);	 Catch:{ all -> 0x0144 }
    L_0x00a0:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0 = r0.mDataChanged;	 Catch:{ all -> 0x0144 }
        r16 = r0;	 Catch:{ all -> 0x0144 }
        if (r16 == 0) goto L_0x00ab;	 Catch:{ all -> 0x0144 }
    L_0x00a8:
        r42.handleDataChanged();	 Catch:{ all -> 0x0144 }
    L_0x00ab:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mItemCount;	 Catch:{ all -> 0x0144 }
        if (r4 != 0) goto L_0x00e7;	 Catch:{ all -> 0x0144 }
    L_0x00b1:
        r42.resetList();	 Catch:{ all -> 0x0144 }
        r42.invokeOnItemScrollListener();	 Catch:{ all -> 0x0144 }
        r0 = r42;
        r4 = r0.mFocusSelector;
        if (r4 == 0) goto L_0x00c4;
    L_0x00bd:
        r0 = r42;
        r4 = r0.mFocusSelector;
        r4.onLayoutComplete();
    L_0x00c4:
        if (r13 != 0) goto L_0x00cb;
    L_0x00c6:
        r4 = 0;
        r0 = r42;
        r0.mBlockLayoutRequests = r4;
    L_0x00cb:
        return;
    L_0x00cc:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mNextSelectedPosition;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0 = r0.mFirstPosition;	 Catch:{ all -> 0x0144 }
        r39 = r0;	 Catch:{ all -> 0x0144 }
        r28 = r4 - r39;	 Catch:{ all -> 0x0144 }
        if (r28 < 0) goto L_0x00a0;	 Catch:{ all -> 0x0144 }
    L_0x00da:
        r0 = r28;	 Catch:{ all -> 0x0144 }
        if (r0 >= r15) goto L_0x00a0;	 Catch:{ all -> 0x0144 }
    L_0x00de:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r1 = r28;	 Catch:{ all -> 0x0144 }
        r6 = r0.getChildAt(r1);	 Catch:{ all -> 0x0144 }
        goto L_0x00a0;	 Catch:{ all -> 0x0144 }
    L_0x00e7:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mItemCount;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0 = r0.mAdapter;	 Catch:{ all -> 0x0144 }
        r39 = r0;	 Catch:{ all -> 0x0144 }
        r39 = r39.getCount();	 Catch:{ all -> 0x0144 }
        r0 = r39;	 Catch:{ all -> 0x0144 }
        if (r4 == r0) goto L_0x0161;	 Catch:{ all -> 0x0144 }
    L_0x00f9:
        r4 = new java.lang.IllegalStateException;	 Catch:{ all -> 0x0144 }
        r39 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0144 }
        r39.<init>();	 Catch:{ all -> 0x0144 }
        r40 = "The content of the adapter has changed but ListView did not receive a notification. Make sure the content of your adapter is not modified from a background thread, but only from the UI thread. Make sure your adapter calls notifyDataSetChanged() when its content changes. [in ListView(";	 Catch:{ all -> 0x0144 }
        r39 = r39.append(r40);	 Catch:{ all -> 0x0144 }
        r40 = r42.getId();	 Catch:{ all -> 0x0144 }
        r39 = r39.append(r40);	 Catch:{ all -> 0x0144 }
        r40 = ", ";	 Catch:{ all -> 0x0144 }
        r39 = r39.append(r40);	 Catch:{ all -> 0x0144 }
        r40 = r42.getClass();	 Catch:{ all -> 0x0144 }
        r39 = r39.append(r40);	 Catch:{ all -> 0x0144 }
        r40 = ") with Adapter(";	 Catch:{ all -> 0x0144 }
        r39 = r39.append(r40);	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0 = r0.mAdapter;	 Catch:{ all -> 0x0144 }
        r40 = r0;	 Catch:{ all -> 0x0144 }
        r40 = r40.getClass();	 Catch:{ all -> 0x0144 }
        r39 = r39.append(r40);	 Catch:{ all -> 0x0144 }
        r40 = ")]";	 Catch:{ all -> 0x0144 }
        r39 = r39.append(r40);	 Catch:{ all -> 0x0144 }
        r39 = r39.toString();	 Catch:{ all -> 0x0144 }
        r0 = r39;	 Catch:{ all -> 0x0144 }
        r4.<init>(r0);	 Catch:{ all -> 0x0144 }
        throw r4;	 Catch:{ all -> 0x0144 }
    L_0x0144:
        r4 = move-exception;
        r0 = r42;
        r0 = r0.mFocusSelector;
        r39 = r0;
        if (r39 == 0) goto L_0x0156;
    L_0x014d:
        r0 = r42;
        r0 = r0.mFocusSelector;
        r39 = r0;
        r39.onLayoutComplete();
    L_0x0156:
        if (r13 != 0) goto L_0x0160;
    L_0x0158:
        r39 = 0;
        r0 = r39;
        r1 = r42;
        r1.mBlockLayoutRequests = r0;
    L_0x0160:
        throw r4;
    L_0x0161:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mNextSelectedPosition;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0.setSelectedPositionInt(r4);	 Catch:{ all -> 0x0144 }
        r10 = 0;	 Catch:{ all -> 0x0144 }
        r11 = 0;	 Catch:{ all -> 0x0144 }
        r12 = -1;	 Catch:{ all -> 0x0144 }
        r37 = r42.getViewRootImpl();	 Catch:{ all -> 0x0144 }
        if (r37 == 0) goto L_0x01a9;	 Catch:{ all -> 0x0144 }
    L_0x0173:
        r19 = r37.getAccessibilityFocusedHost();	 Catch:{ all -> 0x0144 }
        if (r19 == 0) goto L_0x01a9;	 Catch:{ all -> 0x0144 }
    L_0x0179:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r1 = r19;	 Catch:{ all -> 0x0144 }
        r18 = r0.getAccessibilityFocusedChild(r1);	 Catch:{ all -> 0x0144 }
        if (r18 == 0) goto L_0x01a9;	 Catch:{ all -> 0x0144 }
    L_0x0183:
        if (r16 == 0) goto L_0x019b;	 Catch:{ all -> 0x0144 }
    L_0x0185:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r1 = r18;	 Catch:{ all -> 0x0144 }
        r4 = r0.isDirectChildHeaderOrFooter(r1);	 Catch:{ all -> 0x0144 }
        if (r4 != 0) goto L_0x019b;	 Catch:{ all -> 0x0144 }
    L_0x018f:
        r4 = r18.hasTransientState();	 Catch:{ all -> 0x0144 }
        if (r4 != 0) goto L_0x019b;	 Catch:{ all -> 0x0144 }
    L_0x0195:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mAdapterHasStableIds;	 Catch:{ all -> 0x0144 }
        if (r4 == 0) goto L_0x01a1;	 Catch:{ all -> 0x0144 }
    L_0x019b:
        r11 = r19;	 Catch:{ all -> 0x0144 }
        r10 = r37.getAccessibilityFocusedVirtualView();	 Catch:{ all -> 0x0144 }
    L_0x01a1:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r1 = r18;	 Catch:{ all -> 0x0144 }
        r12 = r0.getPositionForView(r1);	 Catch:{ all -> 0x0144 }
    L_0x01a9:
        r20 = 0;	 Catch:{ all -> 0x0144 }
        r21 = 0;	 Catch:{ all -> 0x0144 }
        r25 = r42.getFocusedChild();	 Catch:{ all -> 0x0144 }
        if (r25 == 0) goto L_0x01d9;	 Catch:{ all -> 0x0144 }
    L_0x01b3:
        if (r16 == 0) goto L_0x01cb;	 Catch:{ all -> 0x0144 }
    L_0x01b5:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r1 = r25;	 Catch:{ all -> 0x0144 }
        r4 = r0.isDirectChildHeaderOrFooter(r1);	 Catch:{ all -> 0x0144 }
        if (r4 != 0) goto L_0x01cb;	 Catch:{ all -> 0x0144 }
    L_0x01bf:
        r4 = r25.hasTransientState();	 Catch:{ all -> 0x0144 }
        if (r4 != 0) goto L_0x01cb;	 Catch:{ all -> 0x0144 }
    L_0x01c5:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mAdapterHasStableIds;	 Catch:{ all -> 0x0144 }
        if (r4 == 0) goto L_0x01d6;	 Catch:{ all -> 0x0144 }
    L_0x01cb:
        r20 = r25;	 Catch:{ all -> 0x0144 }
        r21 = r42.findFocus();	 Catch:{ all -> 0x0144 }
        if (r21 == 0) goto L_0x01d6;	 Catch:{ all -> 0x0144 }
    L_0x01d3:
        r21.dispatchStartTemporaryDetach();	 Catch:{ all -> 0x0144 }
    L_0x01d6:
        r42.requestFocus();	 Catch:{ all -> 0x0144 }
    L_0x01d9:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0 = r0.mFirstPosition;	 Catch:{ all -> 0x0144 }
        r17 = r0;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0 = r0.mRecycler;	 Catch:{ all -> 0x0144 }
        r33 = r0;	 Catch:{ all -> 0x0144 }
        if (r16 == 0) goto L_0x0201;	 Catch:{ all -> 0x0144 }
    L_0x01e7:
        r26 = 0;	 Catch:{ all -> 0x0144 }
    L_0x01e9:
        r0 = r26;	 Catch:{ all -> 0x0144 }
        if (r0 >= r15) goto L_0x0208;	 Catch:{ all -> 0x0144 }
    L_0x01ed:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r1 = r26;	 Catch:{ all -> 0x0144 }
        r4 = r0.getChildAt(r1);	 Catch:{ all -> 0x0144 }
        r39 = r17 + r26;	 Catch:{ all -> 0x0144 }
        r0 = r33;	 Catch:{ all -> 0x0144 }
        r1 = r39;	 Catch:{ all -> 0x0144 }
        r0.addScrapView(r4, r1);	 Catch:{ all -> 0x0144 }
        r26 = r26 + 1;	 Catch:{ all -> 0x0144 }
        goto L_0x01e9;	 Catch:{ all -> 0x0144 }
    L_0x0201:
        r0 = r33;	 Catch:{ all -> 0x0144 }
        r1 = r17;	 Catch:{ all -> 0x0144 }
        r0.fillActiveViews(r15, r1);	 Catch:{ all -> 0x0144 }
    L_0x0208:
        r42.detachAllViewsFromParent();	 Catch:{ all -> 0x0144 }
        r33.removeSkippedScrap();	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mLayoutMode;	 Catch:{ all -> 0x0144 }
        switch(r4) {
            case 1: goto L_0x0331;
            case 2: goto L_0x02f6;
            case 3: goto L_0x0320;
            case 4: goto L_0x0341;
            case 5: goto L_0x030c;
            case 6: goto L_0x036e;
            default: goto L_0x0215;
        };	 Catch:{ all -> 0x0144 }
    L_0x0215:
        if (r15 != 0) goto L_0x039b;	 Catch:{ all -> 0x0144 }
    L_0x0217:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mStackFromBottom;	 Catch:{ all -> 0x0144 }
        if (r4 != 0) goto L_0x0376;	 Catch:{ all -> 0x0144 }
    L_0x021d:
        r4 = 0;	 Catch:{ all -> 0x0144 }
        r39 = 1;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r1 = r39;	 Catch:{ all -> 0x0144 }
        r31 = r0.lookForSelectablePosition(r4, r1);	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r1 = r31;	 Catch:{ all -> 0x0144 }
        r0.setSelectedPositionInt(r1);	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r35 = r0.fillFromTop(r8);	 Catch:{ all -> 0x0144 }
    L_0x0235:
        r33.scrapActiveViews();	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mHeaderViewInfos;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0.removeUnusedFixedViews(r4);	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mFooterViewInfos;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0.removeUnusedFixedViews(r4);	 Catch:{ all -> 0x0144 }
        if (r35 == 0) goto L_0x0425;	 Catch:{ all -> 0x0144 }
    L_0x024c:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mItemsCanFocus;	 Catch:{ all -> 0x0144 }
        if (r4 == 0) goto L_0x025e;	 Catch:{ all -> 0x0144 }
    L_0x0252:
        r4 = r42.hasFocus();	 Catch:{ all -> 0x0144 }
        if (r4 == 0) goto L_0x025e;	 Catch:{ all -> 0x0144 }
    L_0x0258:
        r4 = r35.hasFocus();	 Catch:{ all -> 0x0144 }
        if (r4 == 0) goto L_0x03ec;	 Catch:{ all -> 0x0144 }
    L_0x025e:
        r4 = -1;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r1 = r35;	 Catch:{ all -> 0x0144 }
        r0.positionSelector(r4, r1);	 Catch:{ all -> 0x0144 }
    L_0x0266:
        r4 = r35.getTop();	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0.mSelectedTop = r4;	 Catch:{ all -> 0x0144 }
    L_0x026e:
        if (r37 == 0) goto L_0x029b;	 Catch:{ all -> 0x0144 }
    L_0x0270:
        r29 = r37.getAccessibilityFocusedHost();	 Catch:{ all -> 0x0144 }
        if (r29 != 0) goto L_0x029b;	 Catch:{ all -> 0x0144 }
    L_0x0276:
        if (r11 == 0) goto L_0x04a7;	 Catch:{ all -> 0x0144 }
    L_0x0278:
        r4 = r11.isAttachedToWindow();	 Catch:{ all -> 0x0144 }
        if (r4 == 0) goto L_0x04a7;	 Catch:{ all -> 0x0144 }
    L_0x027e:
        r32 = r11.getAccessibilityNodeProvider();	 Catch:{ all -> 0x0144 }
        if (r10 == 0) goto L_0x04a2;	 Catch:{ all -> 0x0144 }
    L_0x0284:
        if (r32 == 0) goto L_0x04a2;	 Catch:{ all -> 0x0144 }
    L_0x0286:
        r40 = r10.getSourceNodeId();	 Catch:{ all -> 0x0144 }
        r38 = android.view.accessibility.AccessibilityNodeInfo.getVirtualDescendantId(r40);	 Catch:{ all -> 0x0144 }
        r4 = 64;	 Catch:{ all -> 0x0144 }
        r39 = 0;	 Catch:{ all -> 0x0144 }
        r0 = r32;	 Catch:{ all -> 0x0144 }
        r1 = r38;	 Catch:{ all -> 0x0144 }
        r2 = r39;	 Catch:{ all -> 0x0144 }
        r0.performAction(r1, r4, r2);	 Catch:{ all -> 0x0144 }
    L_0x029b:
        if (r21 == 0) goto L_0x02a6;	 Catch:{ all -> 0x0144 }
    L_0x029d:
        r4 = r21.getWindowToken();	 Catch:{ all -> 0x0144 }
        if (r4 == 0) goto L_0x02a6;	 Catch:{ all -> 0x0144 }
    L_0x02a3:
        r21.dispatchFinishTemporaryDetach();	 Catch:{ all -> 0x0144 }
    L_0x02a6:
        r4 = 0;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0.mLayoutMode = r4;	 Catch:{ all -> 0x0144 }
        r4 = 0;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0.mDataChanged = r4;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mPositionScrollAfterLayout;	 Catch:{ all -> 0x0144 }
        if (r4 == 0) goto L_0x02c4;	 Catch:{ all -> 0x0144 }
    L_0x02b6:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mPositionScrollAfterLayout;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0.post(r4);	 Catch:{ all -> 0x0144 }
        r4 = 0;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0.mPositionScrollAfterLayout = r4;	 Catch:{ all -> 0x0144 }
    L_0x02c4:
        r4 = 0;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0.mNeedSync = r4;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mSelectedPosition;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0.setNextSelectedPositionInt(r4);	 Catch:{ all -> 0x0144 }
        r42.updateScrollIndicators();	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mItemCount;	 Catch:{ all -> 0x0144 }
        if (r4 <= 0) goto L_0x02de;	 Catch:{ all -> 0x0144 }
    L_0x02db:
        r42.checkSelectionChanged();	 Catch:{ all -> 0x0144 }
    L_0x02de:
        r42.invokeOnItemScrollListener();	 Catch:{ all -> 0x0144 }
        r0 = r42;
        r4 = r0.mFocusSelector;
        if (r4 == 0) goto L_0x02ee;
    L_0x02e7:
        r0 = r42;
        r4 = r0.mFocusSelector;
        r4.onLayoutComplete();
    L_0x02ee:
        if (r13 != 0) goto L_0x02f5;
    L_0x02f0:
        r4 = 0;
        r0 = r42;
        r0.mBlockLayoutRequests = r4;
    L_0x02f5:
        return;
    L_0x02f6:
        if (r6 == 0) goto L_0x0304;
    L_0x02f8:
        r4 = r6.getTop();	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r35 = r0.fillFromSelection(r4, r8, r9);	 Catch:{ all -> 0x0144 }
        goto L_0x0235;	 Catch:{ all -> 0x0144 }
    L_0x0304:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r35 = r0.fillFromMiddle(r8, r9);	 Catch:{ all -> 0x0144 }
        goto L_0x0235;	 Catch:{ all -> 0x0144 }
    L_0x030c:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mSyncPosition;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0 = r0.mSpecificTop;	 Catch:{ all -> 0x0144 }
        r39 = r0;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r1 = r39;	 Catch:{ all -> 0x0144 }
        r35 = r0.fillSpecific(r4, r1);	 Catch:{ all -> 0x0144 }
        goto L_0x0235;	 Catch:{ all -> 0x0144 }
    L_0x0320:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mItemCount;	 Catch:{ all -> 0x0144 }
        r4 = r4 + -1;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r35 = r0.fillUp(r4, r9);	 Catch:{ all -> 0x0144 }
        r42.adjustViewsUpOrDown();	 Catch:{ all -> 0x0144 }
        goto L_0x0235;	 Catch:{ all -> 0x0144 }
    L_0x0331:
        r4 = 0;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0.mFirstPosition = r4;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r35 = r0.fillFromTop(r8);	 Catch:{ all -> 0x0144 }
        r42.adjustViewsUpOrDown();	 Catch:{ all -> 0x0144 }
        goto L_0x0235;	 Catch:{ all -> 0x0144 }
    L_0x0341:
        r36 = r42.reconcileSelectedPosition();	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mSpecificTop;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r1 = r36;	 Catch:{ all -> 0x0144 }
        r35 = r0.fillSpecific(r1, r4);	 Catch:{ all -> 0x0144 }
        if (r35 != 0) goto L_0x0235;	 Catch:{ all -> 0x0144 }
    L_0x0353:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mFocusSelector;	 Catch:{ all -> 0x0144 }
        if (r4 == 0) goto L_0x0235;	 Catch:{ all -> 0x0144 }
    L_0x0359:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mFocusSelector;	 Catch:{ all -> 0x0144 }
        r0 = r36;	 Catch:{ all -> 0x0144 }
        r22 = r4.setupFocusIfValid(r0);	 Catch:{ all -> 0x0144 }
        if (r22 == 0) goto L_0x0235;	 Catch:{ all -> 0x0144 }
    L_0x0365:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r1 = r22;	 Catch:{ all -> 0x0144 }
        r0.post(r1);	 Catch:{ all -> 0x0144 }
        goto L_0x0235;	 Catch:{ all -> 0x0144 }
    L_0x036e:
        r4 = r42;	 Catch:{ all -> 0x0144 }
        r35 = r4.moveSelection(r5, r6, r7, r8, r9);	 Catch:{ all -> 0x0144 }
        goto L_0x0235;	 Catch:{ all -> 0x0144 }
    L_0x0376:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mItemCount;	 Catch:{ all -> 0x0144 }
        r4 = r4 + -1;	 Catch:{ all -> 0x0144 }
        r39 = 0;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r1 = r39;	 Catch:{ all -> 0x0144 }
        r31 = r0.lookForSelectablePosition(r4, r1);	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r1 = r31;	 Catch:{ all -> 0x0144 }
        r0.setSelectedPositionInt(r1);	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mItemCount;	 Catch:{ all -> 0x0144 }
        r4 = r4 + -1;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r35 = r0.fillUp(r4, r9);	 Catch:{ all -> 0x0144 }
        goto L_0x0235;	 Catch:{ all -> 0x0144 }
    L_0x039b:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mSelectedPosition;	 Catch:{ all -> 0x0144 }
        if (r4 < 0) goto L_0x03c2;	 Catch:{ all -> 0x0144 }
    L_0x03a1:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mSelectedPosition;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0 = r0.mItemCount;	 Catch:{ all -> 0x0144 }
        r39 = r0;	 Catch:{ all -> 0x0144 }
        r0 = r39;	 Catch:{ all -> 0x0144 }
        if (r4 >= r0) goto L_0x03c2;	 Catch:{ all -> 0x0144 }
    L_0x03af:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mSelectedPosition;	 Catch:{ all -> 0x0144 }
        if (r5 != 0) goto L_0x03bd;	 Catch:{ all -> 0x0144 }
    L_0x03b5:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r35 = r0.fillSpecific(r4, r8);	 Catch:{ all -> 0x0144 }
        goto L_0x0235;	 Catch:{ all -> 0x0144 }
    L_0x03bd:
        r8 = r5.getTop();	 Catch:{ all -> 0x0144 }
        goto L_0x03b5;	 Catch:{ all -> 0x0144 }
    L_0x03c2:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mFirstPosition;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0 = r0.mItemCount;	 Catch:{ all -> 0x0144 }
        r39 = r0;	 Catch:{ all -> 0x0144 }
        r0 = r39;	 Catch:{ all -> 0x0144 }
        if (r4 >= r0) goto L_0x03e3;	 Catch:{ all -> 0x0144 }
    L_0x03d0:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mFirstPosition;	 Catch:{ all -> 0x0144 }
        if (r30 != 0) goto L_0x03de;	 Catch:{ all -> 0x0144 }
    L_0x03d6:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r35 = r0.fillSpecific(r4, r8);	 Catch:{ all -> 0x0144 }
        goto L_0x0235;	 Catch:{ all -> 0x0144 }
    L_0x03de:
        r8 = r30.getTop();	 Catch:{ all -> 0x0144 }
        goto L_0x03d6;	 Catch:{ all -> 0x0144 }
    L_0x03e3:
        r4 = 0;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r35 = r0.fillSpecific(r4, r8);	 Catch:{ all -> 0x0144 }
        goto L_0x0235;	 Catch:{ all -> 0x0144 }
    L_0x03ec:
        r0 = r35;	 Catch:{ all -> 0x0144 }
        r1 = r20;	 Catch:{ all -> 0x0144 }
        if (r0 != r1) goto L_0x03fa;	 Catch:{ all -> 0x0144 }
    L_0x03f2:
        if (r21 == 0) goto L_0x03fa;	 Catch:{ all -> 0x0144 }
    L_0x03f4:
        r4 = r21.requestFocus();	 Catch:{ all -> 0x0144 }
        if (r4 != 0) goto L_0x0413;	 Catch:{ all -> 0x0144 }
    L_0x03fa:
        r23 = r35.requestFocus();	 Catch:{ all -> 0x0144 }
    L_0x03fe:
        if (r23 != 0) goto L_0x0416;	 Catch:{ all -> 0x0144 }
    L_0x0400:
        r24 = r42.getFocusedChild();	 Catch:{ all -> 0x0144 }
        if (r24 == 0) goto L_0x0409;	 Catch:{ all -> 0x0144 }
    L_0x0406:
        r24.clearFocus();	 Catch:{ all -> 0x0144 }
    L_0x0409:
        r4 = -1;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r1 = r35;	 Catch:{ all -> 0x0144 }
        r0.positionSelector(r4, r1);	 Catch:{ all -> 0x0144 }
        goto L_0x0266;	 Catch:{ all -> 0x0144 }
    L_0x0413:
        r23 = 1;	 Catch:{ all -> 0x0144 }
        goto L_0x03fe;	 Catch:{ all -> 0x0144 }
    L_0x0416:
        r4 = 0;	 Catch:{ all -> 0x0144 }
        r0 = r35;	 Catch:{ all -> 0x0144 }
        r0.setSelected(r4);	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mSelectorRect;	 Catch:{ all -> 0x0144 }
        r4.setEmpty();	 Catch:{ all -> 0x0144 }
        goto L_0x0266;	 Catch:{ all -> 0x0144 }
    L_0x0425:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mTouchMode;	 Catch:{ all -> 0x0144 }
        r39 = 1;	 Catch:{ all -> 0x0144 }
        r0 = r39;	 Catch:{ all -> 0x0144 }
        if (r4 == r0) goto L_0x0467;	 Catch:{ all -> 0x0144 }
    L_0x042f:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mTouchMode;	 Catch:{ all -> 0x0144 }
        r39 = 2;	 Catch:{ all -> 0x0144 }
        r0 = r39;	 Catch:{ all -> 0x0144 }
        if (r4 != r0) goto L_0x046a;	 Catch:{ all -> 0x0144 }
    L_0x0439:
        r27 = 1;	 Catch:{ all -> 0x0144 }
    L_0x043b:
        if (r27 == 0) goto L_0x046d;	 Catch:{ all -> 0x0144 }
    L_0x043d:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mMotionPosition;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0 = r0.mFirstPosition;	 Catch:{ all -> 0x0144 }
        r39 = r0;	 Catch:{ all -> 0x0144 }
        r4 = r4 - r39;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r14 = r0.getChildAt(r4);	 Catch:{ all -> 0x0144 }
        if (r14 == 0) goto L_0x045a;	 Catch:{ all -> 0x0144 }
    L_0x0451:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mMotionPosition;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0.positionSelector(r4, r14);	 Catch:{ all -> 0x0144 }
    L_0x045a:
        r4 = r42.hasFocus();	 Catch:{ all -> 0x0144 }
        if (r4 == 0) goto L_0x026e;	 Catch:{ all -> 0x0144 }
    L_0x0460:
        if (r21 == 0) goto L_0x026e;	 Catch:{ all -> 0x0144 }
    L_0x0462:
        r21.requestFocus();	 Catch:{ all -> 0x0144 }
        goto L_0x026e;	 Catch:{ all -> 0x0144 }
    L_0x0467:
        r27 = 1;	 Catch:{ all -> 0x0144 }
        goto L_0x043b;	 Catch:{ all -> 0x0144 }
    L_0x046a:
        r27 = 0;	 Catch:{ all -> 0x0144 }
        goto L_0x043b;	 Catch:{ all -> 0x0144 }
    L_0x046d:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mSelectorPosition;	 Catch:{ all -> 0x0144 }
        r39 = -1;	 Catch:{ all -> 0x0144 }
        r0 = r39;	 Catch:{ all -> 0x0144 }
        if (r4 == r0) goto L_0x0495;	 Catch:{ all -> 0x0144 }
    L_0x0477:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mSelectorPosition;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0 = r0.mFirstPosition;	 Catch:{ all -> 0x0144 }
        r39 = r0;	 Catch:{ all -> 0x0144 }
        r4 = r4 - r39;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r14 = r0.getChildAt(r4);	 Catch:{ all -> 0x0144 }
        if (r14 == 0) goto L_0x045a;	 Catch:{ all -> 0x0144 }
    L_0x048b:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mSelectorPosition;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0.positionSelector(r4, r14);	 Catch:{ all -> 0x0144 }
        goto L_0x045a;	 Catch:{ all -> 0x0144 }
    L_0x0495:
        r4 = 0;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r0.mSelectedTop = r4;	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mSelectorRect;	 Catch:{ all -> 0x0144 }
        r4.setEmpty();	 Catch:{ all -> 0x0144 }
        goto L_0x045a;	 Catch:{ all -> 0x0144 }
    L_0x04a2:
        r11.requestAccessibilityFocus();	 Catch:{ all -> 0x0144 }
        goto L_0x029b;	 Catch:{ all -> 0x0144 }
    L_0x04a7:
        r4 = -1;	 Catch:{ all -> 0x0144 }
        if (r12 == r4) goto L_0x029b;	 Catch:{ all -> 0x0144 }
    L_0x04aa:
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r4 = r0.mFirstPosition;	 Catch:{ all -> 0x0144 }
        r4 = r12 - r4;	 Catch:{ all -> 0x0144 }
        r39 = r42.getChildCount();	 Catch:{ all -> 0x0144 }
        r39 = r39 + -1;	 Catch:{ all -> 0x0144 }
        r40 = 0;	 Catch:{ all -> 0x0144 }
        r0 = r40;	 Catch:{ all -> 0x0144 }
        r1 = r39;	 Catch:{ all -> 0x0144 }
        r31 = android.util.MathUtils.constrain(r4, r0, r1);	 Catch:{ all -> 0x0144 }
        r0 = r42;	 Catch:{ all -> 0x0144 }
        r1 = r31;	 Catch:{ all -> 0x0144 }
        r34 = r0.getChildAt(r1);	 Catch:{ all -> 0x0144 }
        if (r34 == 0) goto L_0x029b;	 Catch:{ all -> 0x0144 }
    L_0x04ca:
        r34.requestAccessibilityFocus();	 Catch:{ all -> 0x0144 }
        goto L_0x029b;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.ListView.layoutChildren():void");
    }

    public ListView(Context context) {
        this(context, null);
    }

    public ListView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.listViewStyle);
    }

    public ListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mHeaderViewInfos = Lists.newArrayList();
        this.mFooterViewInfos = Lists.newArrayList();
        this.mAreAllItemsSelectable = true;
        this.mItemsCanFocus = false;
        this.mTempRect = new Rect();
        this.mArrowScrollFocusResult = new ArrowScrollFocusResult();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ListView, defStyleAttr, defStyleRes);
        Object[] entries = a.getTextArray(0);
        if (entries != null) {
            setAdapter(new ArrayAdapter(context, (int) R.layout.simple_list_item_1, entries));
        }
        Drawable d = a.getDrawable(1);
        if (d != null) {
            setDivider(d);
        }
        Drawable osHeader = a.getDrawable(5);
        if (osHeader != null) {
            setOverscrollHeader(osHeader);
        }
        Drawable osFooter = a.getDrawable(6);
        if (osFooter != null) {
            setOverscrollFooter(osFooter);
        }
        if (a.hasValueOrEmpty(MIN_SCROLL_PREVIEW_PIXELS)) {
            int dividerHeight = a.getDimensionPixelSize(MIN_SCROLL_PREVIEW_PIXELS, 0);
            if (dividerHeight != 0) {
                setDividerHeight(dividerHeight);
            }
        }
        this.mHeaderDividersEnabled = a.getBoolean(3, true);
        this.mFooterDividersEnabled = a.getBoolean(4, true);
        a.recycle();
    }

    public int getMaxScrollAmount() {
        return (int) (((float) (this.mBottom - this.mTop)) * MAX_SCROLL_FACTOR);
    }

    private void adjustViewsUpOrDown() {
        int childCount = getChildCount();
        if (childCount > 0) {
            int delta;
            if (this.mStackFromBottom) {
                delta = getChildAt(childCount + NO_POSITION).getBottom() - (getHeight() - this.mListPadding.bottom);
                if (this.mFirstPosition + childCount < this.mItemCount) {
                    delta += this.mDividerHeight;
                }
                if (delta > 0) {
                    delta = 0;
                }
            } else {
                delta = getChildAt(0).getTop() - this.mListPadding.top;
                if (this.mFirstPosition != 0) {
                    delta -= this.mDividerHeight;
                }
                if (delta < 0) {
                    delta = 0;
                }
            }
            if (delta != 0) {
                offsetChildrenTopAndBottom(-delta);
            }
        }
    }

    public void addHeaderView(View v, Object data, boolean isSelectable) {
        FixedViewInfo info = new FixedViewInfo();
        info.view = v;
        info.data = data;
        info.isSelectable = isSelectable;
        this.mHeaderViewInfos.add(info);
        this.mAreAllItemsSelectable &= isSelectable;
        if (this.mAdapter != null) {
            if (!(this.mAdapter instanceof HeaderViewListAdapter)) {
                this.mAdapter = new HeaderViewListAdapter(this.mHeaderViewInfos, this.mFooterViewInfos, this.mAdapter);
            }
            if (this.mDataSetObserver != null) {
                this.mDataSetObserver.onChanged();
            }
        }
    }

    public void addHeaderView(View v) {
        addHeaderView(v, null, true);
    }

    public int getHeaderViewsCount() {
        return this.mHeaderViewInfos.size();
    }

    public boolean removeHeaderView(View v) {
        if (this.mHeaderViewInfos.size() <= 0) {
            return false;
        }
        boolean result = false;
        if (this.mAdapter != null && ((HeaderViewListAdapter) this.mAdapter).removeHeader(v)) {
            if (this.mDataSetObserver != null) {
                this.mDataSetObserver.onChanged();
            }
            result = true;
        }
        removeFixedViewInfo(v, this.mHeaderViewInfos);
        return result;
    }

    private void removeFixedViewInfo(View v, ArrayList<FixedViewInfo> where) {
        int len = where.size();
        for (int i = 0; i < len; i++) {
            if (((FixedViewInfo) where.get(i)).view == v) {
                where.remove(i);
                return;
            }
        }
    }

    public void addFooterView(View v, Object data, boolean isSelectable) {
        FixedViewInfo info = new FixedViewInfo();
        info.view = v;
        info.data = data;
        info.isSelectable = isSelectable;
        this.mFooterViewInfos.add(info);
        this.mAreAllItemsSelectable &= isSelectable;
        if (this.mAdapter != null) {
            if (!(this.mAdapter instanceof HeaderViewListAdapter)) {
                this.mAdapter = new HeaderViewListAdapter(this.mHeaderViewInfos, this.mFooterViewInfos, this.mAdapter);
            }
            if (this.mDataSetObserver != null) {
                this.mDataSetObserver.onChanged();
            }
        }
    }

    public void addFooterView(View v) {
        addFooterView(v, null, true);
    }

    public int getFooterViewsCount() {
        return this.mFooterViewInfos.size();
    }

    public boolean removeFooterView(View v) {
        if (this.mFooterViewInfos.size() <= 0) {
            return false;
        }
        boolean result = false;
        if (this.mAdapter != null && ((HeaderViewListAdapter) this.mAdapter).removeFooter(v)) {
            if (this.mDataSetObserver != null) {
                this.mDataSetObserver.onChanged();
            }
            result = true;
        }
        removeFixedViewInfo(v, this.mFooterViewInfos);
        return result;
    }

    public ListAdapter getAdapter() {
        return this.mAdapter;
    }

    @RemotableViewMethod
    public void setRemoteViewsAdapter(Intent intent) {
        super.setRemoteViewsAdapter(intent);
    }

    public void setAdapter(ListAdapter adapter) {
        if (!(this.mAdapter == null || this.mDataSetObserver == null)) {
            this.mAdapter.unregisterDataSetObserver(this.mDataSetObserver);
        }
        resetList();
        this.mRecycler.clear();
        if (this.mHeaderViewInfos.size() > 0 || this.mFooterViewInfos.size() > 0) {
            this.mAdapter = new HeaderViewListAdapter(this.mHeaderViewInfos, this.mFooterViewInfos, adapter);
        } else {
            this.mAdapter = adapter;
        }
        this.mOldSelectedPosition = NO_POSITION;
        this.mOldSelectedRowId = Long.MIN_VALUE;
        super.setAdapter(adapter);
        if (this.mAdapter != null) {
            int position;
            this.mAreAllItemsSelectable = this.mAdapter.areAllItemsEnabled();
            this.mOldItemCount = this.mItemCount;
            this.mItemCount = this.mAdapter.getCount();
            checkFocus();
            this.mDataSetObserver = new AdapterDataSetObserver();
            this.mAdapter.registerDataSetObserver(this.mDataSetObserver);
            this.mRecycler.setViewTypeCount(this.mAdapter.getViewTypeCount());
            if (this.mStackFromBottom) {
                position = lookForSelectablePosition(this.mItemCount + NO_POSITION, false);
            } else {
                position = lookForSelectablePosition(0, true);
            }
            setSelectedPositionInt(position);
            setNextSelectedPositionInt(position);
            if (this.mItemCount == 0) {
                checkSelectionChanged();
            }
        } else {
            this.mAreAllItemsSelectable = true;
            checkFocus();
            checkSelectionChanged();
        }
        requestLayout();
    }

    void resetList() {
        clearRecycledState(this.mHeaderViewInfos);
        clearRecycledState(this.mFooterViewInfos);
        super.resetList();
        this.mLayoutMode = 0;
    }

    private void clearRecycledState(ArrayList<FixedViewInfo> infos) {
        if (infos != null) {
            int count = infos.size();
            for (int i = 0; i < count; i++) {
                LayoutParams p = (LayoutParams) ((FixedViewInfo) infos.get(i)).view.getLayoutParams();
                if (p != null) {
                    p.recycledHeaderFooter = false;
                }
            }
        }
    }

    private boolean showingTopFadingEdge() {
        int listTop = this.mScrollY + this.mListPadding.top;
        if (this.mFirstPosition > 0 || getChildAt(0).getTop() > listTop) {
            return true;
        }
        return false;
    }

    private boolean showingBottomFadingEdge() {
        int childCount = getChildCount();
        int bottomOfBottomChild = getChildAt(childCount + NO_POSITION).getBottom();
        int listBottom = (this.mScrollY + getHeight()) - this.mListPadding.bottom;
        if ((this.mFirstPosition + childCount) + NO_POSITION < this.mItemCount + NO_POSITION || bottomOfBottomChild < listBottom) {
            return true;
        }
        return false;
    }

    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
        int rectTopWithinChild = rect.top;
        rect.offset(child.getLeft(), child.getTop());
        rect.offset(-child.getScrollX(), -child.getScrollY());
        int height = getHeight();
        int listUnfadedTop = getScrollY();
        int listUnfadedBottom = listUnfadedTop + height;
        int fadingEdge = getVerticalFadingEdgeLength();
        if (showingTopFadingEdge() && (this.mSelectedPosition > 0 || rectTopWithinChild > fadingEdge)) {
            listUnfadedTop += fadingEdge;
        }
        int bottomOfBottomChild = getChildAt(getChildCount() + NO_POSITION).getBottom();
        if (showingBottomFadingEdge() && (this.mSelectedPosition < this.mItemCount + NO_POSITION || rect.bottom < bottomOfBottomChild - fadingEdge)) {
            listUnfadedBottom -= fadingEdge;
        }
        int scrollYDelta = 0;
        if (rect.bottom > listUnfadedBottom && rect.top > listUnfadedTop) {
            if (rect.height() > height) {
                scrollYDelta = (rect.top - listUnfadedTop) + 0;
            } else {
                scrollYDelta = (rect.bottom - listUnfadedBottom) + 0;
            }
            scrollYDelta = Math.min(scrollYDelta, bottomOfBottomChild - listUnfadedBottom);
        } else if (rect.top < listUnfadedTop && rect.bottom < listUnfadedBottom) {
            if (rect.height() > height) {
                scrollYDelta = 0 - (listUnfadedBottom - rect.bottom);
            } else {
                scrollYDelta = 0 - (listUnfadedTop - rect.top);
            }
            scrollYDelta = Math.max(scrollYDelta, getChildAt(0).getTop() - listUnfadedTop);
        }
        boolean scroll = scrollYDelta != 0;
        if (scroll) {
            scrollListItemsBy(-scrollYDelta);
            positionSelector(NO_POSITION, child);
            this.mSelectedTop = child.getTop();
            invalidate();
        }
        return scroll;
    }

    void fillGap(boolean down) {
        int count = getChildCount();
        int startOffset;
        if (down) {
            int paddingTop = 0;
            if ((this.mGroupFlags & 34) == 34) {
                paddingTop = getListPaddingTop();
            }
            if (count > 0) {
                startOffset = getChildAt(count + NO_POSITION).getBottom() + this.mDividerHeight;
            } else {
                startOffset = paddingTop;
            }
            fillDown(this.mFirstPosition + count, startOffset);
            correctTooHigh(getChildCount());
            return;
        }
        int paddingBottom = 0;
        if ((this.mGroupFlags & 34) == 34) {
            paddingBottom = getListPaddingBottom();
        }
        if (count > 0) {
            startOffset = getChildAt(0).getTop() - this.mDividerHeight;
        } else {
            startOffset = getHeight() - paddingBottom;
        }
        fillUp(this.mFirstPosition + NO_POSITION, startOffset);
        correctTooLow(getChildCount());
    }

    private View fillDown(int pos, int nextTop) {
        View selectedView = null;
        int end = this.mBottom - this.mTop;
        if ((this.mGroupFlags & 34) == 34) {
            end -= this.mListPadding.bottom;
        }
        while (nextTop < end && pos < this.mItemCount) {
            boolean selected = pos == this.mSelectedPosition;
            View child = makeAndAddView(pos, nextTop, true, this.mListPadding.left, selected);
            nextTop = child.getBottom() + this.mDividerHeight;
            if (selected) {
                selectedView = child;
            }
            pos++;
        }
        setVisibleRangeHint(this.mFirstPosition, (this.mFirstPosition + getChildCount()) + NO_POSITION);
        return selectedView;
    }

    private View fillUp(int pos, int nextBottom) {
        View selectedView = null;
        int end = 0;
        if ((this.mGroupFlags & 34) == 34) {
            end = this.mListPadding.top;
        }
        while (nextBottom > end && pos >= 0) {
            boolean selected = pos == this.mSelectedPosition;
            View child = makeAndAddView(pos, nextBottom, false, this.mListPadding.left, selected);
            nextBottom = child.getTop() - this.mDividerHeight;
            if (selected) {
                selectedView = child;
            }
            pos += NO_POSITION;
        }
        this.mFirstPosition = pos + 1;
        setVisibleRangeHint(this.mFirstPosition, (this.mFirstPosition + getChildCount()) + NO_POSITION);
        return selectedView;
    }

    private View fillFromTop(int nextTop) {
        this.mFirstPosition = Math.min(this.mFirstPosition, this.mSelectedPosition);
        this.mFirstPosition = Math.min(this.mFirstPosition, this.mItemCount + NO_POSITION);
        if (this.mFirstPosition < 0) {
            this.mFirstPosition = 0;
        }
        return fillDown(this.mFirstPosition, nextTop);
    }

    private View fillFromMiddle(int childrenTop, int childrenBottom) {
        int height = childrenBottom - childrenTop;
        int position = reconcileSelectedPosition();
        View sel = makeAndAddView(position, childrenTop, true, this.mListPadding.left, true);
        this.mFirstPosition = position;
        int selHeight = sel.getMeasuredHeight();
        if (selHeight <= height) {
            sel.offsetTopAndBottom((height - selHeight) / MIN_SCROLL_PREVIEW_PIXELS);
        }
        fillAboveAndBelow(sel, position);
        if (this.mStackFromBottom) {
            correctTooLow(getChildCount());
        } else {
            correctTooHigh(getChildCount());
        }
        return sel;
    }

    private void fillAboveAndBelow(View sel, int position) {
        int dividerHeight = this.mDividerHeight;
        if (this.mStackFromBottom) {
            fillDown(position + 1, sel.getBottom() + dividerHeight);
            adjustViewsUpOrDown();
            fillUp(position + NO_POSITION, sel.getTop() - dividerHeight);
            return;
        }
        fillUp(position + NO_POSITION, sel.getTop() - dividerHeight);
        adjustViewsUpOrDown();
        fillDown(position + 1, sel.getBottom() + dividerHeight);
    }

    private View fillFromSelection(int selectedTop, int childrenTop, int childrenBottom) {
        int fadingEdgeLength = getVerticalFadingEdgeLength();
        int selectedPosition = this.mSelectedPosition;
        int topSelectionPixel = getTopSelectionPixel(childrenTop, fadingEdgeLength, selectedPosition);
        int bottomSelectionPixel = getBottomSelectionPixel(childrenBottom, fadingEdgeLength, selectedPosition);
        View sel = makeAndAddView(selectedPosition, selectedTop, true, this.mListPadding.left, true);
        if (sel.getBottom() > bottomSelectionPixel) {
            sel.offsetTopAndBottom(-Math.min(sel.getTop() - topSelectionPixel, sel.getBottom() - bottomSelectionPixel));
        } else if (sel.getTop() < topSelectionPixel) {
            sel.offsetTopAndBottom(Math.min(topSelectionPixel - sel.getTop(), bottomSelectionPixel - sel.getBottom()));
        }
        fillAboveAndBelow(sel, selectedPosition);
        if (this.mStackFromBottom) {
            correctTooLow(getChildCount());
        } else {
            correctTooHigh(getChildCount());
        }
        return sel;
    }

    private int getBottomSelectionPixel(int childrenBottom, int fadingEdgeLength, int selectedPosition) {
        int bottomSelectionPixel = childrenBottom;
        if (selectedPosition != this.mItemCount + NO_POSITION) {
            return childrenBottom - fadingEdgeLength;
        }
        return bottomSelectionPixel;
    }

    private int getTopSelectionPixel(int childrenTop, int fadingEdgeLength, int selectedPosition) {
        int topSelectionPixel = childrenTop;
        if (selectedPosition > 0) {
            return childrenTop + fadingEdgeLength;
        }
        return topSelectionPixel;
    }

    @RemotableViewMethod
    public void smoothScrollToPosition(int position) {
        super.smoothScrollToPosition(position);
    }

    @RemotableViewMethod
    public void smoothScrollByOffset(int offset) {
        super.smoothScrollByOffset(offset);
    }

    private View moveSelection(View oldSel, View newSel, int delta, int childrenTop, int childrenBottom) {
        View sel;
        int fadingEdgeLength = getVerticalFadingEdgeLength();
        int selectedPosition = this.mSelectedPosition;
        int topSelectionPixel = getTopSelectionPixel(childrenTop, fadingEdgeLength, selectedPosition);
        int bottomSelectionPixel = getBottomSelectionPixel(childrenTop, fadingEdgeLength, selectedPosition);
        int halfVerticalSpace;
        if (delta > 0) {
            oldSel = makeAndAddView(selectedPosition + NO_POSITION, oldSel.getTop(), true, this.mListPadding.left, false);
            int dividerHeight = this.mDividerHeight;
            sel = makeAndAddView(selectedPosition, oldSel.getBottom() + dividerHeight, true, this.mListPadding.left, true);
            if (sel.getBottom() > bottomSelectionPixel) {
                halfVerticalSpace = (childrenBottom - childrenTop) / MIN_SCROLL_PREVIEW_PIXELS;
                int offset = Math.min(Math.min(sel.getTop() - topSelectionPixel, sel.getBottom() - bottomSelectionPixel), halfVerticalSpace);
                oldSel.offsetTopAndBottom(-offset);
                sel.offsetTopAndBottom(-offset);
            }
            if (this.mStackFromBottom) {
                fillDown(this.mSelectedPosition + 1, sel.getBottom() + dividerHeight);
                adjustViewsUpOrDown();
                fillUp(this.mSelectedPosition - 2, sel.getTop() - dividerHeight);
            } else {
                fillUp(this.mSelectedPosition - 2, sel.getTop() - dividerHeight);
                adjustViewsUpOrDown();
                fillDown(this.mSelectedPosition + 1, sel.getBottom() + dividerHeight);
            }
        } else if (delta < 0) {
            if (newSel != null) {
                sel = makeAndAddView(selectedPosition, newSel.getTop(), true, this.mListPadding.left, true);
            } else {
                sel = makeAndAddView(selectedPosition, oldSel.getTop(), false, this.mListPadding.left, true);
            }
            if (sel.getTop() < topSelectionPixel) {
                halfVerticalSpace = (childrenBottom - childrenTop) / MIN_SCROLL_PREVIEW_PIXELS;
                sel.offsetTopAndBottom(Math.min(Math.min(topSelectionPixel - sel.getTop(), bottomSelectionPixel - sel.getBottom()), halfVerticalSpace));
            }
            fillAboveAndBelow(sel, selectedPosition);
        } else {
            int oldTop = oldSel.getTop();
            sel = makeAndAddView(selectedPosition, oldTop, true, this.mListPadding.left, true);
            if (oldTop < childrenTop && sel.getBottom() < childrenTop + 20) {
                sel.offsetTopAndBottom(childrenTop - sel.getTop());
            }
            fillAboveAndBelow(sel, selectedPosition);
        }
        return sel;
    }

    protected void onDetachedFromWindow() {
        if (this.mFocusSelector != null) {
            removeCallbacks(this.mFocusSelector);
            this.mFocusSelector = null;
        }
        super.onDetachedFromWindow();
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (getChildCount() > 0) {
            View focusedChild = getFocusedChild();
            if (focusedChild != null) {
                int childPosition = this.mFirstPosition + indexOfChild(focusedChild);
                int top = focusedChild.getTop() - Math.max(0, focusedChild.getBottom() - (h - this.mPaddingTop));
                if (this.mFocusSelector == null) {
                    this.mFocusSelector = new FocusSelector();
                }
                post(this.mFocusSelector.setupForSetSelection(childPosition, top));
            }
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int childWidth = 0;
        int childHeight = 0;
        int childState = 0;
        if (this.mAdapter == null) {
            i = 0;
        } else {
            i = this.mAdapter.getCount();
        }
        this.mItemCount = i;
        if (this.mItemCount > 0 && (widthMode == 0 || heightMode == 0)) {
            View child = obtainView(0, this.mIsScrap);
            measureScrapChild(child, 0, widthMeasureSpec, heightSize);
            childWidth = child.getMeasuredWidth();
            childHeight = child.getMeasuredHeight();
            childState = View.combineMeasuredStates(0, child.getMeasuredState());
            if (recycleOnMeasure() && this.mRecycler.shouldRecycleViewType(((LayoutParams) child.getLayoutParams()).viewType)) {
                this.mRecycler.addScrapView(child, 0);
            }
        }
        if (widthMode == 0) {
            widthSize = ((this.mListPadding.left + this.mListPadding.right) + childWidth) + getVerticalScrollbarWidth();
        } else {
            widthSize |= View.MEASURED_STATE_MASK & childState;
        }
        if (heightMode == 0) {
            heightSize = ((this.mListPadding.top + this.mListPadding.bottom) + childHeight) + (getVerticalFadingEdgeLength() * MIN_SCROLL_PREVIEW_PIXELS);
        }
        if (heightMode == RtlSpacingHelper.UNDEFINED) {
            heightSize = measureHeightOfChildren(widthMeasureSpec, 0, NO_POSITION, heightSize, NO_POSITION);
        }
        setMeasuredDimension(widthSize, heightSize);
        this.mWidthMeasureSpec = widthMeasureSpec;
    }

    private void measureScrapChild(View child, int position, int widthMeasureSpec, int heightHint) {
        int childHeightSpec;
        LayoutParams p = (LayoutParams) child.getLayoutParams();
        if (p == null) {
            p = (LayoutParams) generateDefaultLayoutParams();
            child.setLayoutParams(p);
        }
        p.viewType = this.mAdapter.getItemViewType(position);
        p.isEnabled = this.mAdapter.isEnabled(position);
        p.forceAdd = true;
        int childWidthSpec = ViewGroup.getChildMeasureSpec(widthMeasureSpec, this.mListPadding.left + this.mListPadding.right, p.width);
        int lpHeight = p.height;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        } else {
            childHeightSpec = MeasureSpec.makeSafeMeasureSpec(heightHint, 0);
        }
        child.measure(childWidthSpec, childHeightSpec);
        child.forceLayout();
    }

    @ExportedProperty(category = "list")
    protected boolean recycleOnMeasure() {
        return true;
    }

    final int measureHeightOfChildren(int widthMeasureSpec, int startPosition, int endPosition, int maxHeight, int disallowPartialChildPosition) {
        ListAdapter adapter = this.mAdapter;
        if (adapter == null) {
            return this.mListPadding.top + this.mListPadding.bottom;
        }
        int returnedHeight = this.mListPadding.top + this.mListPadding.bottom;
        int dividerHeight = this.mDividerHeight;
        int prevHeightWithoutPartialChild = 0;
        if (endPosition == NO_POSITION) {
            endPosition = adapter.getCount() + NO_POSITION;
        }
        RecycleBin recycleBin = this.mRecycler;
        boolean recyle = recycleOnMeasure();
        boolean[] isScrap = this.mIsScrap;
        int i = startPosition;
        while (i <= endPosition) {
            View child = obtainView(i, isScrap);
            measureScrapChild(child, i, widthMeasureSpec, maxHeight);
            if (i > 0) {
                returnedHeight += dividerHeight;
            }
            if (recyle && recycleBin.shouldRecycleViewType(((LayoutParams) child.getLayoutParams()).viewType)) {
                recycleBin.addScrapView(child, NO_POSITION);
            }
            returnedHeight += child.getMeasuredHeight();
            if (returnedHeight >= maxHeight) {
                if (disallowPartialChildPosition < 0 || i <= disallowPartialChildPosition || prevHeightWithoutPartialChild <= 0 || returnedHeight == maxHeight) {
                    prevHeightWithoutPartialChild = maxHeight;
                }
                return prevHeightWithoutPartialChild;
            }
            if (disallowPartialChildPosition >= 0 && i >= disallowPartialChildPosition) {
                prevHeightWithoutPartialChild = returnedHeight;
            }
            i++;
        }
        return returnedHeight;
    }

    int findMotionRow(int y) {
        int childCount = getChildCount();
        if (childCount > 0) {
            int i;
            if (this.mStackFromBottom) {
                for (i = childCount + NO_POSITION; i >= 0; i += NO_POSITION) {
                    if (y >= getChildAt(i).getTop()) {
                        return this.mFirstPosition + i;
                    }
                }
            } else {
                for (i = 0; i < childCount; i++) {
                    if (y <= getChildAt(i).getBottom()) {
                        return this.mFirstPosition + i;
                    }
                }
            }
        }
        return NO_POSITION;
    }

    private View fillSpecific(int position, int top) {
        View below;
        View above;
        boolean tempIsSelected = position == this.mSelectedPosition;
        View temp = makeAndAddView(position, top, true, this.mListPadding.left, tempIsSelected);
        this.mFirstPosition = position;
        int dividerHeight = this.mDividerHeight;
        int childCount;
        if (this.mStackFromBottom) {
            below = fillDown(position + 1, temp.getBottom() + dividerHeight);
            adjustViewsUpOrDown();
            above = fillUp(position + NO_POSITION, temp.getTop() - dividerHeight);
            childCount = getChildCount();
            if (childCount > 0) {
                correctTooLow(childCount);
            }
        } else {
            above = fillUp(position + NO_POSITION, temp.getTop() - dividerHeight);
            adjustViewsUpOrDown();
            below = fillDown(position + 1, temp.getBottom() + dividerHeight);
            childCount = getChildCount();
            if (childCount > 0) {
                correctTooHigh(childCount);
            }
        }
        if (tempIsSelected) {
            return temp;
        }
        if (above != null) {
            return above;
        }
        return below;
    }

    private void correctTooHigh(int childCount) {
        if ((this.mFirstPosition + childCount) + NO_POSITION == this.mItemCount + NO_POSITION && childCount > 0) {
            int bottomOffset = ((this.mBottom - this.mTop) - this.mListPadding.bottom) - getChildAt(childCount + NO_POSITION).getBottom();
            View firstChild = getChildAt(0);
            int firstTop = firstChild.getTop();
            if (bottomOffset <= 0) {
                return;
            }
            if (this.mFirstPosition > 0 || firstTop < this.mListPadding.top) {
                if (this.mFirstPosition == 0) {
                    bottomOffset = Math.min(bottomOffset, this.mListPadding.top - firstTop);
                }
                offsetChildrenTopAndBottom(bottomOffset);
                if (this.mFirstPosition > 0) {
                    fillUp(this.mFirstPosition + NO_POSITION, firstChild.getTop() - this.mDividerHeight);
                    adjustViewsUpOrDown();
                }
            }
        }
    }

    private void correctTooLow(int childCount) {
        if (this.mFirstPosition == 0 && childCount > 0) {
            int end = (this.mBottom - this.mTop) - this.mListPadding.bottom;
            int topOffset = getChildAt(0).getTop() - this.mListPadding.top;
            View lastChild = getChildAt(childCount + NO_POSITION);
            int lastBottom = lastChild.getBottom();
            int lastPosition = (this.mFirstPosition + childCount) + NO_POSITION;
            if (topOffset <= 0) {
                return;
            }
            if (lastPosition < this.mItemCount + NO_POSITION || lastBottom > end) {
                if (lastPosition == this.mItemCount + NO_POSITION) {
                    topOffset = Math.min(topOffset, lastBottom - end);
                }
                offsetChildrenTopAndBottom(-topOffset);
                if (lastPosition < this.mItemCount + NO_POSITION) {
                    fillDown(lastPosition + 1, lastChild.getBottom() + this.mDividerHeight);
                    adjustViewsUpOrDown();
                }
            } else if (lastPosition == this.mItemCount + NO_POSITION) {
                adjustViewsUpOrDown();
            }
        }
    }

    boolean trackMotionScroll(int deltaY, int incrementalDeltaY) {
        boolean result = super.trackMotionScroll(deltaY, incrementalDeltaY);
        removeUnusedFixedViews(this.mHeaderViewInfos);
        removeUnusedFixedViews(this.mFooterViewInfos);
        return result;
    }

    private void removeUnusedFixedViews(List<FixedViewInfo> infoList) {
        if (infoList != null) {
            for (int i = infoList.size() + NO_POSITION; i >= 0; i += NO_POSITION) {
                View view = ((FixedViewInfo) infoList.get(i)).view;
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
                if (view.getParent() == null && lp != null && lp.recycledHeaderFooter) {
                    removeDetachedView(view, false);
                    lp.recycledHeaderFooter = false;
                }
            }
        }
    }

    private boolean isDirectChildHeaderOrFooter(View child) {
        int i;
        ArrayList<FixedViewInfo> headers = this.mHeaderViewInfos;
        int numHeaders = headers.size();
        for (i = 0; i < numHeaders; i++) {
            if (child == ((FixedViewInfo) headers.get(i)).view) {
                return true;
            }
        }
        ArrayList<FixedViewInfo> footers = this.mFooterViewInfos;
        int numFooters = footers.size();
        for (i = 0; i < numFooters; i++) {
            if (child == ((FixedViewInfo) footers.get(i)).view) {
                return true;
            }
        }
        return false;
    }

    private View makeAndAddView(int position, int y, boolean flow, int childrenLeft, boolean selected) {
        View child;
        if (!this.mDataChanged) {
            child = this.mRecycler.getActiveView(position);
            if (child != null) {
                setupChild(child, position, y, flow, childrenLeft, selected, true);
                return child;
            }
        }
        Jlog.perfEvent(HwPerformance.PERF_EVENT_PROBE, "", 1);
        this.mAddItemViewType = this.mAdapter.getItemViewType(position);
        this.mAddItemViewPosition = position;
        child = obtainView(position, this.mIsScrap);
        Jlog.perfEvent(HwPerformance.PERF_EVENT_PROBE, "", 0);
        setupChild(child, position, y, flow, childrenLeft, selected, this.mIsScrap[0]);
        this.mAddItemViewType = -10000;
        this.mAddItemViewPosition = NO_POSITION;
        return child;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setupChild(View child, int position, int y, boolean flowDown, int childrenLeft, boolean selected, boolean recycled) {
        boolean isPressed;
        int i;
        int lpHeight;
        int childHeightSpec;
        int w;
        int h;
        int childTop;
        Trace.traceBegin(8, "setupListItem");
        boolean shouldShowSelector = selected ? shouldShowSelector() : false;
        boolean updateChildSelected = shouldShowSelector != child.isSelected();
        int mode = this.mTouchMode;
        if (mode <= 0 || mode >= 3) {
            isPressed = false;
        } else {
            i = this.mMotionPosition;
            isPressed = r0 == position;
        }
        boolean updateChildPressed = isPressed != child.isPressed();
        boolean isLayoutRequested = (!recycled || updateChildSelected) ? true : child.isLayoutRequested();
        LayoutParams p = (LayoutParams) child.getLayoutParams();
        if (p == null) {
            p = (LayoutParams) generateDefaultLayoutParams();
        }
        p.viewType = getHwItemViewType(position);
        p.isEnabled = this.mAdapter.isEnabled(position);
        if (!recycled || p.forceAdd) {
            if (p.recycledHeaderFooter) {
                i = p.viewType;
            }
            p.forceAdd = false;
            i = p.viewType;
            if (r0 == -2) {
                p.recycledHeaderFooter = true;
            }
            addViewInLayout(child, flowDown ? NO_POSITION : 0, p, true);
            if (child.isRtlLocale()) {
                child.resolveRtlPropertiesIfNeeded();
            }
            if (updateChildSelected) {
                child.setSelected(shouldShowSelector);
            }
            if (updateChildPressed) {
                child.setPressed(isPressed);
            }
            if (!(this.mChoiceMode == 0 || this.mCheckStates == null)) {
                if (child instanceof Checkable) {
                    i = getContext().getApplicationInfo().targetSdkVersion;
                    if (r0 >= 11) {
                        child.setActivated(this.mCheckStates.get(position));
                    }
                } else {
                    ((Checkable) child).setChecked(this.mCheckStates.get(position));
                }
            }
            if (isLayoutRequested) {
                cleanupLayoutState(child);
            } else {
                int childWidthSpec = ViewGroup.getChildMeasureSpec(this.mWidthMeasureSpec, this.mListPadding.left + this.mListPadding.right, p.width);
                lpHeight = p.height;
                if (lpHeight <= 0) {
                    childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                } else {
                    childHeightSpec = MeasureSpec.makeSafeMeasureSpec(getMeasuredHeight(), 0);
                }
                child.measure(childWidthSpec, childHeightSpec);
            }
            w = child.getMeasuredWidth();
            h = child.getMeasuredHeight();
            childTop = flowDown ? y : y - h;
            if (isLayoutRequested) {
                child.offsetLeftAndRight(childrenLeft - child.getLeft());
                child.offsetTopAndBottom(childTop - child.getTop());
            } else {
                child.layout(childrenLeft, childTop, childrenLeft + w, childTop + h);
            }
            if (this.mCachingStarted && !child.isDrawingCacheEnabled()) {
                child.setDrawingCacheEnabled(true);
            }
            if (recycled) {
                i = ((LayoutParams) child.getLayoutParams()).scrappedFromPosition;
                if (r0 != position) {
                    child.jumpDrawablesToCurrentState();
                }
            }
            Trace.traceEnd(8);
        }
        if (flowDown) {
            i = NO_POSITION;
        } else {
            i = 0;
        }
        attachViewToParent(child, i, p);
        if (updateChildSelected) {
            child.setSelected(shouldShowSelector);
        }
        if (updateChildPressed) {
            child.setPressed(isPressed);
        }
        if (child instanceof Checkable) {
            i = getContext().getApplicationInfo().targetSdkVersion;
            if (r0 >= 11) {
                child.setActivated(this.mCheckStates.get(position));
            }
        } else {
            ((Checkable) child).setChecked(this.mCheckStates.get(position));
        }
        if (isLayoutRequested) {
            cleanupLayoutState(child);
        } else {
            int childWidthSpec2 = ViewGroup.getChildMeasureSpec(this.mWidthMeasureSpec, this.mListPadding.left + this.mListPadding.right, p.width);
            lpHeight = p.height;
            if (lpHeight <= 0) {
                childHeightSpec = MeasureSpec.makeSafeMeasureSpec(getMeasuredHeight(), 0);
            } else {
                childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, EditorInfo.IME_FLAG_NO_ENTER_ACTION);
            }
            child.measure(childWidthSpec2, childHeightSpec);
        }
        w = child.getMeasuredWidth();
        h = child.getMeasuredHeight();
        if (flowDown) {
        }
        if (isLayoutRequested) {
            child.offsetLeftAndRight(childrenLeft - child.getLeft());
            child.offsetTopAndBottom(childTop - child.getTop());
        } else {
            child.layout(childrenLeft, childTop, childrenLeft + w, childTop + h);
        }
        child.setDrawingCacheEnabled(true);
        if (recycled) {
            i = ((LayoutParams) child.getLayoutParams()).scrappedFromPosition;
            if (r0 != position) {
                child.jumpDrawablesToCurrentState();
            }
        }
        Trace.traceEnd(8);
    }

    protected boolean canAnimate() {
        return super.canAnimate() && this.mItemCount > 0;
    }

    public void setSelection(int position) {
        setSelectionFromTop(position, 0);
    }

    void setSelectionInt(int position) {
        setNextSelectedPositionInt(position);
        boolean awakeScrollbars = false;
        int selectedPosition = this.mSelectedPosition;
        if (selectedPosition >= 0) {
            if (position == selectedPosition + NO_POSITION) {
                awakeScrollbars = true;
            } else if (position == selectedPosition + 1) {
                awakeScrollbars = true;
            }
        }
        if (this.mPositionScroller != null) {
            this.mPositionScroller.stop();
        }
        layoutChildren();
        if (awakeScrollbars) {
            awakenScrollBars();
        }
    }

    int lookForSelectablePosition(int position, boolean lookDown) {
        ListAdapter adapter = this.mAdapter;
        if (adapter == null || isInTouchMode()) {
            return NO_POSITION;
        }
        int count = adapter.getCount();
        if (!this.mAreAllItemsSelectable) {
            if (lookDown) {
                position = Math.max(0, position);
                while (position < count && !adapter.isEnabled(position)) {
                    position++;
                }
            } else {
                position = Math.min(position, count + NO_POSITION);
                while (position >= 0 && !adapter.isEnabled(position)) {
                    position += NO_POSITION;
                }
            }
        }
        if (position < 0 || position >= count) {
            return NO_POSITION;
        }
        return position;
    }

    int lookForSelectablePositionAfter(int current, int position, boolean lookDown) {
        ListAdapter adapter = this.mAdapter;
        if (adapter == null || isInTouchMode()) {
            return NO_POSITION;
        }
        int after = lookForSelectablePosition(position, lookDown);
        if (after != NO_POSITION) {
            return after;
        }
        int count = adapter.getCount();
        current = MathUtils.constrain(current, (int) NO_POSITION, count + NO_POSITION);
        if (lookDown) {
            position = Math.min(position + NO_POSITION, count + NO_POSITION);
            while (position > current && !adapter.isEnabled(position)) {
                position += NO_POSITION;
            }
            if (position <= current) {
                return NO_POSITION;
            }
        }
        position = Math.max(0, position + 1);
        while (position < current && !adapter.isEnabled(position)) {
            position++;
        }
        if (position >= current) {
            return NO_POSITION;
        }
        return position;
    }

    public void setSelectionAfterHeaderView() {
        int count = this.mHeaderViewInfos.size();
        if (count > 0) {
            this.mNextSelectedPosition = 0;
            return;
        }
        if (this.mAdapter != null) {
            setSelection(count);
        } else {
            this.mNextSelectedPosition = count;
            this.mLayoutMode = MIN_SCROLL_PREVIEW_PIXELS;
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled = super.dispatchKeyEvent(event);
        if (handled || getFocusedChild() == null || event.getAction() != 0) {
            return handled;
        }
        return onKeyDown(event.getKeyCode(), event);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return commonKey(keyCode, 1, event);
    }

    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return commonKey(keyCode, repeatCount, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return commonKey(keyCode, 1, event);
    }

    private boolean commonKey(int keyCode, int count, KeyEvent event) {
        if (this.mAdapter == null || !isAttachedToWindow()) {
            return false;
        }
        if (this.mDataChanged) {
            layoutChildren();
        }
        boolean z = false;
        int action = event.getAction();
        if (KeyEvent.isConfirmKey(keyCode) && event.hasNoModifiers() && action != 1) {
            z = resurrectSelectionIfNeeded();
            if (!z && event.getRepeatCount() == 0 && getChildCount() > 0) {
                keyPressed();
                z = true;
            }
        }
        if (!(z || action == 1)) {
            int count2;
            switch (keyCode) {
                case PerfHub.PERF_TAG_IPA_SUSTAINABLE_POWER /*19*/:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(MIN_SCROLL_PREVIEW_PIXELS)) {
                            if (!resurrectSelectionIfNeeded()) {
                                z = fullScroll(33);
                                break;
                            }
                            z = true;
                            break;
                        }
                    }
                    z = resurrectSelectionIfNeeded();
                    if (!z) {
                        while (true) {
                            count2 = count;
                            count = count2 + NO_POSITION;
                            if (count2 > 0 && arrowScroll(33)) {
                                z = true;
                            }
                        }
                    }
                    break;
                case HwPerformance.PERF_TAG_TASK_FORK_ON_B_CLUSTER /*20*/:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(MIN_SCROLL_PREVIEW_PIXELS)) {
                            if (!resurrectSelectionIfNeeded()) {
                                z = fullScroll(LogPower.END_CHG_ROTATION);
                                break;
                            }
                            z = true;
                            break;
                        }
                    }
                    z = resurrectSelectionIfNeeded();
                    if (!z) {
                        while (true) {
                            count2 = count;
                            count = count2 + NO_POSITION;
                            if (count2 > 0 && arrowScroll(LogPower.END_CHG_ROTATION)) {
                                z = true;
                            }
                        }
                    }
                    break;
                case HwPerformance.PERF_TAG_DEF_L_CPU_MIN /*21*/:
                    if (event.hasNoModifiers()) {
                        z = handleHorizontalFocusWithinListItem(17);
                        break;
                    }
                    break;
                case HwPerformance.PERF_TAG_DEF_L_CPU_MAX /*22*/:
                    if (event.hasNoModifiers()) {
                        z = handleHorizontalFocusWithinListItem(66);
                        break;
                    }
                    break;
                case StatisticalConstant.TYPE_WIFI_END /*61*/:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(1)) {
                            if (!resurrectSelectionIfNeeded()) {
                                z = arrowScroll(33);
                                break;
                            }
                            z = true;
                            break;
                        }
                    } else if (!resurrectSelectionIfNeeded()) {
                        z = arrowScroll(LogPower.END_CHG_ROTATION);
                        break;
                    } else {
                        z = true;
                        break;
                    }
                    break;
                case RILConstants.RIL_REQUEST_CDMA_GET_BROADCAST_CONFIG /*92*/:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(MIN_SCROLL_PREVIEW_PIXELS)) {
                            if (!resurrectSelectionIfNeeded()) {
                                z = fullScroll(33);
                                break;
                            }
                            z = true;
                            break;
                        }
                    } else if (!resurrectSelectionIfNeeded()) {
                        z = pageScroll(33);
                        break;
                    } else {
                        z = true;
                        break;
                    }
                    break;
                case RILConstants.RIL_REQUEST_CDMA_SET_BROADCAST_CONFIG /*93*/:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(MIN_SCROLL_PREVIEW_PIXELS)) {
                            if (!resurrectSelectionIfNeeded()) {
                                z = fullScroll(LogPower.END_CHG_ROTATION);
                                break;
                            }
                            z = true;
                            break;
                        }
                    } else if (!resurrectSelectionIfNeeded()) {
                        z = pageScroll(LogPower.END_CHG_ROTATION);
                        break;
                    } else {
                        z = true;
                        break;
                    }
                    break;
                case LogPower.NOTIFICATION_ENQUEUE /*122*/:
                    if (event.hasNoModifiers()) {
                        if (!resurrectSelectionIfNeeded()) {
                            z = fullScroll(33);
                            break;
                        }
                        z = true;
                        break;
                    }
                    break;
                case LogPower.NOTIFICATION_CANCEL /*123*/:
                    if (event.hasNoModifiers()) {
                        if (!resurrectSelectionIfNeeded()) {
                            z = fullScroll(LogPower.END_CHG_ROTATION);
                            break;
                        }
                        z = true;
                        break;
                    }
                    break;
            }
        }
        if (z || sendToTextFilter(keyCode, count, event)) {
            return true;
        }
        switch (action) {
            case HwCfgFilePolicy.GLOBAL /*0*/:
                return super.onKeyDown(keyCode, event);
            case HwCfgFilePolicy.EMUI /*1*/:
                return super.onKeyUp(keyCode, event);
            case MIN_SCROLL_PREVIEW_PIXELS /*2*/:
                return super.onKeyMultiple(keyCode, count, event);
            default:
                return false;
        }
    }

    boolean pageScroll(int direction) {
        int nextPage;
        boolean down;
        if (direction == 33) {
            nextPage = Math.max(0, (this.mSelectedPosition - getChildCount()) + NO_POSITION);
            down = false;
        } else if (direction != LogPower.END_CHG_ROTATION) {
            return false;
        } else {
            nextPage = Math.min(this.mItemCount + NO_POSITION, (this.mSelectedPosition + getChildCount()) + NO_POSITION);
            down = true;
        }
        if (nextPage >= 0) {
            int position = lookForSelectablePositionAfter(this.mSelectedPosition, nextPage, down);
            if (position >= 0) {
                this.mLayoutMode = 4;
                this.mSpecificTop = this.mPaddingTop + getVerticalFadingEdgeLength();
                if (down && position > this.mItemCount - getChildCount()) {
                    this.mLayoutMode = 3;
                }
                if (!down && position < getChildCount()) {
                    this.mLayoutMode = 1;
                }
                setSelectionInt(position);
                invokeOnItemScrollListener();
                if (!awakenScrollBars()) {
                    invalidate();
                }
                return true;
            }
        }
        return false;
    }

    boolean fullScroll(int direction) {
        boolean moved = false;
        int position;
        if (direction == 33) {
            if (this.mSelectedPosition != 0) {
                position = lookForSelectablePositionAfter(this.mSelectedPosition, 0, true);
                if (position >= 0) {
                    this.mLayoutMode = 1;
                    setSelectionInt(position);
                    invokeOnItemScrollListener();
                }
                moved = true;
            }
        } else if (direction == LogPower.END_CHG_ROTATION) {
            int lastItem = this.mItemCount + NO_POSITION;
            if (this.mSelectedPosition < lastItem) {
                position = lookForSelectablePositionAfter(this.mSelectedPosition, lastItem, false);
                if (position >= 0) {
                    this.mLayoutMode = 3;
                    setSelectionInt(position);
                    invokeOnItemScrollListener();
                }
                moved = true;
            }
        }
        if (moved && !awakenScrollBars()) {
            awakenScrollBars();
            invalidate();
        }
        return moved;
    }

    private boolean handleHorizontalFocusWithinListItem(int direction) {
        if (direction == 17 || direction == 66) {
            int numChildren = getChildCount();
            if (this.mItemsCanFocus && numChildren > 0 && this.mSelectedPosition != NO_POSITION) {
                View selectedView = getSelectedView();
                if (selectedView != null && selectedView.hasFocus() && (selectedView instanceof ViewGroup)) {
                    View currentFocus = selectedView.findFocus();
                    View nextFocus = FocusFinder.getInstance().findNextFocus((ViewGroup) selectedView, currentFocus, direction);
                    if (nextFocus != null) {
                        Rect focusedRect = this.mTempRect;
                        if (currentFocus != null) {
                            currentFocus.getFocusedRect(focusedRect);
                            offsetDescendantRectToMyCoords(currentFocus, focusedRect);
                            offsetRectIntoDescendantCoords(nextFocus, focusedRect);
                        } else {
                            focusedRect = null;
                        }
                        if (nextFocus.requestFocus(direction, focusedRect)) {
                            return true;
                        }
                    }
                    View globalNextFocus = FocusFinder.getInstance().findNextFocus((ViewGroup) getRootView(), currentFocus, direction);
                    if (globalNextFocus != null) {
                        return isViewAncestorOf(globalNextFocus, this);
                    }
                }
            }
            return false;
        }
        throw new IllegalArgumentException("direction must be one of {View.FOCUS_LEFT, View.FOCUS_RIGHT}");
    }

    boolean arrowScroll(int direction) {
        try {
            this.mInLayout = true;
            boolean handled = arrowScrollImpl(direction);
            if (handled) {
                playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
            }
            this.mInLayout = false;
            return handled;
        } catch (Throwable th) {
            this.mInLayout = false;
        }
    }

    private final int nextSelectedPositionForDirection(View selectedView, int selectedPos, int direction) {
        int nextSelected;
        boolean z = false;
        if (direction == LogPower.END_CHG_ROTATION) {
            int listBottom = getHeight() - this.mListPadding.bottom;
            if (selectedView == null || selectedView.getBottom() > listBottom) {
                return NO_POSITION;
            }
            if (selectedPos == NO_POSITION || selectedPos < this.mFirstPosition) {
                nextSelected = this.mFirstPosition;
            } else {
                nextSelected = selectedPos + 1;
            }
        } else {
            int listTop = this.mListPadding.top;
            if (selectedView == null || selectedView.getTop() < listTop) {
                return NO_POSITION;
            }
            int lastPos = (this.mFirstPosition + getChildCount()) + NO_POSITION;
            if (selectedPos == NO_POSITION || selectedPos > lastPos) {
                nextSelected = lastPos;
            } else {
                nextSelected = selectedPos + NO_POSITION;
            }
        }
        if (nextSelected < 0 || nextSelected >= this.mAdapter.getCount()) {
            return NO_POSITION;
        }
        if (direction == LogPower.END_CHG_ROTATION) {
            z = true;
        }
        return lookForSelectablePosition(nextSelected, z);
    }

    private boolean arrowScrollImpl(int direction) {
        ArrowScrollFocusResult focusResult = null;
        if (getChildCount() <= 0) {
            return false;
        }
        View focused;
        View selectedView = getSelectedView();
        int selectedPos = this.mSelectedPosition;
        int nextSelectedPosition = nextSelectedPositionForDirection(selectedView, selectedPos, direction);
        int amountToScroll = amountToScroll(direction, nextSelectedPosition);
        if (this.mItemsCanFocus) {
            focusResult = arrowScrollFocused(direction);
        }
        if (focusResult != null) {
            nextSelectedPosition = focusResult.getSelectedPosition();
            amountToScroll = focusResult.getAmountToScroll();
        }
        boolean z = focusResult != null;
        if (nextSelectedPosition != NO_POSITION) {
            boolean z2;
            if (focusResult != null) {
                z2 = true;
            } else {
                z2 = false;
            }
            handleNewSelectionChange(selectedView, direction, nextSelectedPosition, z2);
            setSelectedPositionInt(nextSelectedPosition);
            setNextSelectedPositionInt(nextSelectedPosition);
            selectedView = getSelectedView();
            selectedPos = nextSelectedPosition;
            if (this.mItemsCanFocus && focusResult == null) {
                focused = getFocusedChild();
                if (focused != null) {
                    focused.clearFocus();
                }
            }
            z = true;
            checkSelectionChanged();
        }
        if (amountToScroll > 0) {
            if (direction != 33) {
                amountToScroll = -amountToScroll;
            }
            scrollListItemsBy(amountToScroll);
            z = true;
        }
        if (this.mItemsCanFocus && focusResult == null && selectedView != null && selectedView.hasFocus()) {
            focused = selectedView.findFocus();
            if (focused != null && (!isViewAncestorOf(focused, this) || distanceToView(focused) > 0)) {
                focused.clearFocus();
            }
        }
        if (!(nextSelectedPosition != NO_POSITION || selectedView == null || isViewAncestorOf(selectedView, this))) {
            selectedView = null;
            hideSelector();
            this.mResurrectToPosition = NO_POSITION;
        }
        if (!z) {
            return false;
        }
        if (selectedView != null) {
            positionSelectorLikeFocus(selectedPos, selectedView);
            this.mSelectedTop = selectedView.getTop();
        }
        if (!awakenScrollBars()) {
            invalidate();
        }
        invokeOnItemScrollListener();
        return true;
    }

    private void handleNewSelectionChange(View selectedView, int direction, int newSelectedPosition, boolean newFocusAssigned) {
        boolean z = false;
        if (newSelectedPosition == NO_POSITION) {
            throw new IllegalArgumentException("newSelectedPosition needs to be valid");
        }
        int topViewIndex;
        int bottomViewIndex;
        View topView;
        View bottomView;
        boolean topSelected = false;
        int selectedIndex = this.mSelectedPosition - this.mFirstPosition;
        int nextSelectedIndex = newSelectedPosition - this.mFirstPosition;
        if (direction == 33) {
            topViewIndex = nextSelectedIndex;
            bottomViewIndex = selectedIndex;
            topView = getChildAt(nextSelectedIndex);
            bottomView = selectedView;
            topSelected = true;
        } else {
            topViewIndex = selectedIndex;
            bottomViewIndex = nextSelectedIndex;
            topView = selectedView;
            bottomView = getChildAt(nextSelectedIndex);
        }
        int numChildren = getChildCount();
        if (topView != null) {
            boolean z2;
            if (newFocusAssigned) {
                z2 = false;
            } else {
                z2 = topSelected;
            }
            topView.setSelected(z2);
            measureAndAdjustDown(topView, topViewIndex, numChildren);
        }
        if (bottomView != null) {
            if (!(newFocusAssigned || topSelected)) {
                z = true;
            }
            bottomView.setSelected(z);
            measureAndAdjustDown(bottomView, bottomViewIndex, numChildren);
        }
    }

    private void measureAndAdjustDown(View child, int childIndex, int numChildren) {
        int oldHeight = child.getHeight();
        measureItem(child);
        if (child.getMeasuredHeight() != oldHeight) {
            relayoutMeasuredItem(child);
            int heightDelta = child.getMeasuredHeight() - oldHeight;
            for (int i = childIndex + 1; i < numChildren; i++) {
                getChildAt(i).offsetTopAndBottom(heightDelta);
            }
        }
    }

    private void measureItem(View child) {
        int childHeightSpec;
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams((int) NO_POSITION, -2);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(this.mWidthMeasureSpec, this.mListPadding.left + this.mListPadding.right, p.width);
        int lpHeight = p.height;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        } else {
            childHeightSpec = MeasureSpec.makeSafeMeasureSpec(getMeasuredHeight(), 0);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    private void relayoutMeasuredItem(View child) {
        int w = child.getMeasuredWidth();
        int h = child.getMeasuredHeight();
        int childLeft = this.mListPadding.left;
        int childRight = childLeft + w;
        int childTop = child.getTop();
        child.layout(childLeft, childTop, childRight, childTop + h);
    }

    private int getArrowScrollPreviewLength() {
        return Math.max(MIN_SCROLL_PREVIEW_PIXELS, getVerticalFadingEdgeLength());
    }

    private int amountToScroll(int direction, int nextSelectedPosition) {
        int listBottom = getHeight() - this.mListPadding.bottom;
        int listTop = this.mListPadding.top;
        int numChildren = getChildCount();
        int indexToMakeVisible;
        int positionToMakeVisible;
        View viewToMakeVisible;
        int amountToScroll;
        if (direction == LogPower.END_CHG_ROTATION) {
            indexToMakeVisible = numChildren + NO_POSITION;
            if (nextSelectedPosition != NO_POSITION) {
                indexToMakeVisible = nextSelectedPosition - this.mFirstPosition;
            }
            while (numChildren <= indexToMakeVisible) {
                addViewBelow(getChildAt(numChildren + NO_POSITION), (this.mFirstPosition + numChildren) + NO_POSITION);
                numChildren++;
            }
            positionToMakeVisible = this.mFirstPosition + indexToMakeVisible;
            viewToMakeVisible = getChildAt(indexToMakeVisible);
            int goalBottom = listBottom;
            if (positionToMakeVisible < this.mItemCount + NO_POSITION) {
                goalBottom = listBottom - getArrowScrollPreviewLength();
            }
            if (viewToMakeVisible.getBottom() <= goalBottom) {
                return 0;
            }
            if (nextSelectedPosition != NO_POSITION && goalBottom - viewToMakeVisible.getTop() >= getMaxScrollAmount()) {
                return 0;
            }
            amountToScroll = viewToMakeVisible.getBottom() - goalBottom;
            if (this.mFirstPosition + numChildren == this.mItemCount) {
                amountToScroll = Math.min(amountToScroll, getChildAt(numChildren + NO_POSITION).getBottom() - listBottom);
            }
            return Math.min(amountToScroll, getMaxScrollAmount());
        }
        indexToMakeVisible = 0;
        if (nextSelectedPosition != NO_POSITION) {
            indexToMakeVisible = nextSelectedPosition - this.mFirstPosition;
        }
        while (indexToMakeVisible < 0) {
            addViewAbove(getChildAt(0), this.mFirstPosition);
            this.mFirstPosition += NO_POSITION;
            indexToMakeVisible = nextSelectedPosition - this.mFirstPosition;
        }
        positionToMakeVisible = this.mFirstPosition + indexToMakeVisible;
        viewToMakeVisible = getChildAt(indexToMakeVisible);
        int goalTop = listTop;
        if (positionToMakeVisible > 0) {
            goalTop = listTop + getArrowScrollPreviewLength();
        }
        if (viewToMakeVisible.getTop() >= goalTop) {
            return 0;
        }
        if (nextSelectedPosition != NO_POSITION && viewToMakeVisible.getBottom() - goalTop >= getMaxScrollAmount()) {
            return 0;
        }
        amountToScroll = goalTop - viewToMakeVisible.getTop();
        if (this.mFirstPosition == 0) {
            amountToScroll = Math.min(amountToScroll, listTop - getChildAt(0).getTop());
        }
        return Math.min(amountToScroll, getMaxScrollAmount());
    }

    private int lookForSelectablePositionOnScreen(int direction) {
        int firstPosition = this.mFirstPosition;
        int startPos;
        ListAdapter adapter;
        int pos;
        if (direction == LogPower.END_CHG_ROTATION) {
            if (this.mSelectedPosition != NO_POSITION) {
                startPos = this.mSelectedPosition + 1;
            } else {
                startPos = firstPosition;
            }
            if (startPos >= this.mAdapter.getCount()) {
                return NO_POSITION;
            }
            if (startPos < firstPosition) {
                startPos = firstPosition;
            }
            int lastVisiblePos = getLastVisiblePosition();
            adapter = getAdapter();
            pos = startPos;
            while (pos <= lastVisiblePos) {
                if (adapter.isEnabled(pos) && getChildAt(pos - firstPosition).getVisibility() == 0) {
                    return pos;
                }
                pos++;
            }
        } else {
            int last = (getChildCount() + firstPosition) + NO_POSITION;
            if (this.mSelectedPosition != NO_POSITION) {
                startPos = this.mSelectedPosition + NO_POSITION;
            } else {
                startPos = (getChildCount() + firstPosition) + NO_POSITION;
            }
            if (startPos < 0 || startPos >= this.mAdapter.getCount()) {
                return NO_POSITION;
            }
            if (startPos > last) {
                startPos = last;
            }
            adapter = getAdapter();
            pos = startPos;
            while (pos >= firstPosition) {
                if (adapter.isEnabled(pos) && getChildAt(pos - firstPosition).getVisibility() == 0) {
                    return pos;
                }
                pos += NO_POSITION;
            }
        }
        return NO_POSITION;
    }

    private ArrowScrollFocusResult arrowScrollFocused(int direction) {
        View newFocus;
        View selectedView = getSelectedView();
        if (selectedView == null || !selectedView.hasFocus()) {
            int ySearchPoint;
            if (direction == 130) {
                int listTop = this.mListPadding.top + (this.mFirstPosition > 0 ? getArrowScrollPreviewLength() : 0);
                if (selectedView == null || selectedView.getTop() <= listTop) {
                    ySearchPoint = listTop;
                } else {
                    ySearchPoint = selectedView.getTop();
                }
                this.mTempRect.set(0, ySearchPoint, 0, ySearchPoint);
            } else {
                int listBottom = (getHeight() - this.mListPadding.bottom) - ((this.mFirstPosition + getChildCount()) + NO_POSITION < this.mItemCount ? getArrowScrollPreviewLength() : 0);
                if (selectedView == null || selectedView.getBottom() >= listBottom) {
                    ySearchPoint = listBottom;
                } else {
                    ySearchPoint = selectedView.getBottom();
                }
                this.mTempRect.set(0, ySearchPoint, 0, ySearchPoint);
            }
            newFocus = FocusFinder.getInstance().findNextFocusFromRect(this, this.mTempRect, direction);
        } else {
            newFocus = FocusFinder.getInstance().findNextFocus(this, selectedView.findFocus(), direction);
        }
        if (newFocus != null) {
            int positionOfNewFocus = positionOfNewFocus(newFocus);
            if (!(this.mSelectedPosition == NO_POSITION || positionOfNewFocus == this.mSelectedPosition)) {
                int selectablePosition = lookForSelectablePositionOnScreen(direction);
                if (selectablePosition != NO_POSITION && ((direction == 130 && selectablePosition < positionOfNewFocus) || (direction == 33 && selectablePosition > positionOfNewFocus))) {
                    return null;
                }
            }
            int focusScroll = amountToScrollToNewFocus(direction, newFocus, positionOfNewFocus);
            int maxScrollAmount = getMaxScrollAmount();
            if (focusScroll < maxScrollAmount) {
                newFocus.requestFocus(direction);
                this.mArrowScrollFocusResult.populate(positionOfNewFocus, focusScroll);
                return this.mArrowScrollFocusResult;
            } else if (distanceToView(newFocus) < maxScrollAmount) {
                newFocus.requestFocus(direction);
                this.mArrowScrollFocusResult.populate(positionOfNewFocus, maxScrollAmount);
                return this.mArrowScrollFocusResult;
            }
        }
        return null;
    }

    private int positionOfNewFocus(View newFocus) {
        int numChildren = getChildCount();
        for (int i = 0; i < numChildren; i++) {
            if (isViewAncestorOf(newFocus, getChildAt(i))) {
                return this.mFirstPosition + i;
            }
        }
        throw new IllegalArgumentException("newFocus is not a child of any of the children of the list!");
    }

    private boolean isViewAncestorOf(View child, View parent) {
        if (child == parent) {
            return true;
        }
        ViewParent theParent = child.getParent();
        return theParent instanceof ViewGroup ? isViewAncestorOf((View) theParent, parent) : false;
    }

    private int amountToScrollToNewFocus(int direction, View newFocus, int positionOfNewFocus) {
        newFocus.getDrawingRect(this.mTempRect);
        offsetDescendantRectToMyCoords(newFocus, this.mTempRect);
        int amountToScroll;
        if (direction != 33) {
            int listBottom = getHeight() - this.mListPadding.bottom;
            if (this.mTempRect.bottom <= listBottom) {
                return 0;
            }
            amountToScroll = this.mTempRect.bottom - listBottom;
            if (positionOfNewFocus < this.mItemCount + NO_POSITION) {
                return amountToScroll + getArrowScrollPreviewLength();
            }
            return amountToScroll;
        } else if (this.mTempRect.top >= this.mListPadding.top) {
            return 0;
        } else {
            amountToScroll = this.mListPadding.top - this.mTempRect.top;
            return positionOfNewFocus > 0 ? amountToScroll + getArrowScrollPreviewLength() : amountToScroll;
        }
    }

    private int distanceToView(View descendant) {
        descendant.getDrawingRect(this.mTempRect);
        offsetDescendantRectToMyCoords(descendant, this.mTempRect);
        int listBottom = (this.mBottom - this.mTop) - this.mListPadding.bottom;
        if (this.mTempRect.bottom < this.mListPadding.top) {
            return this.mListPadding.top - this.mTempRect.bottom;
        }
        if (this.mTempRect.top > listBottom) {
            return this.mTempRect.top - listBottom;
        }
        return 0;
    }

    private void scrollListItemsBy(int amount) {
        offsetChildrenTopAndBottom(amount);
        int listBottom = getHeight() - this.mListPadding.bottom;
        int listTop = this.mListPadding.top;
        RecycleBin recycleBin = this.mRecycler;
        View last;
        View first;
        if (amount < 0) {
            int numChildren = getChildCount();
            last = getChildAt(numChildren + NO_POSITION);
            while (last.getBottom() < listBottom) {
                int lastVisiblePosition = (this.mFirstPosition + numChildren) + NO_POSITION;
                if (lastVisiblePosition >= this.mItemCount + NO_POSITION) {
                    break;
                }
                last = addViewBelow(last, lastVisiblePosition);
                numChildren++;
            }
            if (last.getBottom() < listBottom) {
                offsetChildrenTopAndBottom(listBottom - last.getBottom());
            }
            first = getChildAt(0);
            while (first != null && first.getBottom() < listTop) {
                if (recycleBin.shouldRecycleViewType(((LayoutParams) first.getLayoutParams()).viewType)) {
                    recycleBin.addScrapView(first, this.mFirstPosition);
                }
                detachViewFromParent(first);
                first = getChildAt(0);
                this.mFirstPosition++;
            }
        } else {
            first = getChildAt(0);
            while (first.getTop() > listTop && this.mFirstPosition > 0) {
                first = addViewAbove(first, this.mFirstPosition);
                this.mFirstPosition += NO_POSITION;
            }
            if (first.getTop() > listTop) {
                offsetChildrenTopAndBottom(listTop - first.getTop());
            }
            int lastIndex = getChildCount() + NO_POSITION;
            last = getChildAt(lastIndex);
            while (last != null && last.getTop() > listBottom) {
                if (recycleBin.shouldRecycleViewType(((LayoutParams) last.getLayoutParams()).viewType)) {
                    recycleBin.addScrapView(last, this.mFirstPosition + lastIndex);
                }
                detachViewFromParent(last);
                lastIndex += NO_POSITION;
                last = getChildAt(lastIndex);
            }
        }
        recycleBin.fullyDetachScrapViews();
        removeUnusedFixedViews(this.mHeaderViewInfos);
        removeUnusedFixedViews(this.mFooterViewInfos);
    }

    private View addViewAbove(View theView, int position) {
        int abovePosition = position + NO_POSITION;
        View view = obtainView(abovePosition, this.mIsScrap);
        setupChild(view, abovePosition, theView.getTop() - this.mDividerHeight, false, this.mListPadding.left, false, this.mIsScrap[0]);
        return view;
    }

    private View addViewBelow(View theView, int position) {
        int belowPosition = position + 1;
        View view = obtainView(belowPosition, this.mIsScrap);
        setupChild(view, belowPosition, theView.getBottom() + this.mDividerHeight, true, this.mListPadding.left, false, this.mIsScrap[0]);
        return view;
    }

    public void setItemsCanFocus(boolean itemsCanFocus) {
        this.mItemsCanFocus = itemsCanFocus;
        if (!itemsCanFocus) {
            setDescendantFocusability(Protocol.BASE_NSD_MANAGER);
        }
    }

    public boolean getItemsCanFocus() {
        return this.mItemsCanFocus;
    }

    public boolean isOpaque() {
        boolean retValue;
        if (this.mCachingActive && this.mIsCacheColorOpaque && this.mDividerIsOpaque && hasOpaqueScrollbars()) {
            retValue = true;
        } else {
            retValue = super.isOpaque();
        }
        if (retValue) {
            int listTop = this.mListPadding != null ? this.mListPadding.top : this.mPaddingTop;
            View first = getChildAt(0);
            if (first == null || first.getTop() > listTop) {
                return false;
            }
            int listBottom = getHeight() - (this.mListPadding != null ? this.mListPadding.bottom : this.mPaddingBottom);
            View last = getChildAt(getChildCount() + NO_POSITION);
            if (last == null || last.getBottom() < listBottom) {
                return false;
            }
        }
        return retValue;
    }

    public void setCacheColorHint(int color) {
        boolean opaque = (color >>> 24) == MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
        this.mIsCacheColorOpaque = opaque;
        if (opaque) {
            if (this.mDividerPaint == null) {
                this.mDividerPaint = new Paint();
            }
            this.mDividerPaint.setColor(color);
        }
        super.setCacheColorHint(color);
    }

    void drawOverscrollHeader(Canvas canvas, Drawable drawable, Rect bounds) {
        int height = drawable.getMinimumHeight();
        canvas.save();
        canvas.clipRect(bounds);
        if (bounds.bottom - bounds.top < height) {
            bounds.top = bounds.bottom - height;
        }
        drawable.setBounds(bounds);
        drawable.draw(canvas);
        canvas.restore();
    }

    void drawOverscrollFooter(Canvas canvas, Drawable drawable, Rect bounds) {
        int height = drawable.getMinimumHeight();
        canvas.save();
        canvas.clipRect(bounds);
        if (bounds.bottom - bounds.top < height) {
            bounds.bottom = bounds.top + height;
        }
        drawable.setBounds(bounds);
        drawable.draw(canvas);
        canvas.restore();
    }

    protected void dispatchDraw(Canvas canvas) {
        if (this.mCachingStarted) {
            this.mCachingActive = true;
        }
        int dividerHeight = this.mDividerHeight;
        Drawable overscrollHeader = this.mOverScrollHeader;
        Drawable overscrollFooter = this.mOverScrollFooter;
        boolean drawOverscrollHeader = overscrollHeader != null;
        boolean drawOverscrollFooter = overscrollFooter != null;
        boolean drawDividers = dividerHeight > 0 && this.mDivider != null;
        if (drawDividers || drawOverscrollHeader || drawOverscrollFooter) {
            Rect bounds = this.mTempRect;
            bounds.left = this.mPaddingLeft;
            bounds.right = (this.mRight - this.mLeft) - this.mPaddingRight;
            int count = getChildCount();
            int headerCount = this.mHeaderViewInfos.size();
            int itemCount = this.mItemCount;
            int footerLimit = itemCount - this.mFooterViewInfos.size();
            boolean headerDividers = this.mHeaderDividersEnabled;
            boolean footerDividers = this.mFooterDividersEnabled;
            int first = this.mFirstPosition;
            boolean areAllItemsSelectable = this.mAreAllItemsSelectable;
            ListAdapter adapter = this.mAdapter;
            boolean fillForMissingDividers = isOpaque() && !super.isOpaque();
            if (fillForMissingDividers && this.mDividerPaint == null && this.mIsCacheColorOpaque) {
                this.mDividerPaint = new Paint();
                this.mDividerPaint.setColor(getCacheColorHint());
            }
            Paint paint = this.mDividerPaint;
            int effectivePaddingTop = 0;
            int effectivePaddingBottom = 0;
            if ((this.mGroupFlags & 34) == 34) {
                effectivePaddingTop = this.mListPadding.top;
                effectivePaddingBottom = this.mListPadding.bottom;
            }
            int listBottom = ((this.mBottom - this.mTop) - effectivePaddingBottom) + this.mScrollY;
            int scrollY;
            int i;
            int itemIndex;
            boolean isHeader;
            boolean isFooter;
            if (this.mStackFromBottom) {
                scrollY = this.mScrollY;
                if (count > 0 && drawOverscrollHeader) {
                    bounds.top = scrollY;
                    bounds.bottom = getChildAt(0).getTop();
                    drawOverscrollHeader(canvas, overscrollHeader, bounds);
                }
                int start = drawOverscrollHeader ? 1 : 0;
                i = start;
                while (i < count) {
                    itemIndex = first + i;
                    isHeader = itemIndex < headerCount;
                    isFooter = itemIndex >= footerLimit;
                    if ((headerDividers || !isHeader) && (footerDividers || !isFooter)) {
                        int top = getChildAt(i).getTop();
                        if (drawDividers && top > effectivePaddingTop) {
                            boolean isFirstItem = i == start;
                            int previousIndex = itemIndex + NO_POSITION;
                            if (checkIsEnabled(adapter, itemIndex) && ((headerDividers || (!isHeader && previousIndex >= headerCount)) && (isFirstItem || (checkIsEnabled(adapter, previousIndex) && (footerDividers || (!isFooter && previousIndex < footerLimit)))))) {
                                bounds.top = top - dividerHeight;
                                bounds.bottom = top;
                                drawDivider(canvas, bounds, i + NO_POSITION);
                            } else if (fillForMissingDividers) {
                                bounds.top = top - dividerHeight;
                                bounds.bottom = top;
                                canvas.drawRect(bounds, paint);
                            }
                        }
                    }
                    i++;
                }
                if (count > 0 && scrollY > 0) {
                    if (drawOverscrollFooter) {
                        int absListBottom = this.mBottom;
                        bounds.top = absListBottom;
                        bounds.bottom = absListBottom + scrollY;
                        drawOverscrollFooter(canvas, overscrollFooter, bounds);
                    } else if (drawDividers) {
                        bounds.top = listBottom;
                        bounds.bottom = listBottom + dividerHeight;
                        drawDivider(canvas, bounds, NO_POSITION);
                    }
                }
            } else {
                int bottom = 0;
                scrollY = this.mScrollY;
                if (count > 0 && scrollY < 0) {
                    if (drawOverscrollHeader) {
                        bounds.bottom = 0;
                        bounds.top = scrollY;
                        drawOverscrollHeader(canvas, overscrollHeader, bounds);
                    } else if (drawDividers) {
                        bounds.bottom = 0;
                        bounds.top = -dividerHeight;
                        drawDivider(canvas, bounds, NO_POSITION);
                    }
                }
                i = 0;
                while (i < count) {
                    itemIndex = first + i;
                    isHeader = itemIndex < headerCount;
                    isFooter = itemIndex >= footerLimit;
                    if ((headerDividers || !isHeader) && (footerDividers || !isFooter)) {
                        bottom = getChildAt(i).getBottom();
                        boolean isLastItem = i == count + NO_POSITION;
                        if (drawDividers && bottom < listBottom && !(drawOverscrollFooter && isLastItem)) {
                            int nextIndex = itemIndex + 1;
                            if (checkIsEnabled(adapter, itemIndex) && ((headerDividers || (!isHeader && nextIndex >= headerCount)) && (isLastItem || (checkIsEnabled(adapter, nextIndex) && (footerDividers || (!isFooter && nextIndex < footerLimit)))))) {
                                bounds.top = bottom;
                                bounds.bottom = bottom + dividerHeight;
                                drawDivider(canvas, bounds, i);
                            } else if (fillForMissingDividers) {
                                bounds.top = bottom;
                                bounds.bottom = bottom + dividerHeight;
                                canvas.drawRect(bounds, paint);
                            }
                        }
                    }
                    i++;
                }
                int overFooterBottom = this.mBottom + this.mScrollY;
                if (drawOverscrollFooter && first + count == itemCount && overFooterBottom > bottom) {
                    bounds.top = bottom;
                    bounds.bottom = overFooterBottom;
                    drawOverscrollFooter(canvas, overscrollFooter, bounds);
                }
            }
        }
        super.dispatchDraw(canvas);
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean more = super.drawChild(canvas, child, drawingTime);
        if (this.mCachingActive && child.mCachingFailed) {
            this.mCachingActive = false;
        }
        return more;
    }

    void drawDivider(Canvas canvas, Rect bounds, int childIndex) {
        Drawable divider = this.mDivider;
        divider.setBounds(bounds);
        divider.draw(canvas);
    }

    public Drawable getDivider() {
        return this.mDivider;
    }

    public void setDivider(Drawable divider) {
        boolean z = true;
        if (divider != null) {
            this.mDividerHeight = divider.getIntrinsicHeight();
        } else {
            this.mDividerHeight = 0;
        }
        this.mDivider = divider;
        if (!(divider == null || divider.getOpacity() == NO_POSITION)) {
            z = false;
        }
        this.mDividerIsOpaque = z;
        requestLayout();
        invalidate();
    }

    public int getDividerHeight() {
        return this.mDividerHeight;
    }

    public void setDividerHeight(int height) {
        this.mDividerHeight = height;
        requestLayout();
        invalidate();
    }

    public void setHeaderDividersEnabled(boolean headerDividersEnabled) {
        this.mHeaderDividersEnabled = headerDividersEnabled;
        invalidate();
    }

    public boolean areHeaderDividersEnabled() {
        return this.mHeaderDividersEnabled;
    }

    public void setFooterDividersEnabled(boolean footerDividersEnabled) {
        this.mFooterDividersEnabled = footerDividersEnabled;
        invalidate();
    }

    public boolean areFooterDividersEnabled() {
        return this.mFooterDividersEnabled;
    }

    public void setOverscrollHeader(Drawable header) {
        this.mOverScrollHeader = header;
        if (this.mScrollY < 0) {
            invalidate();
        }
    }

    public Drawable getOverscrollHeader() {
        return this.mOverScrollHeader;
    }

    public void setOverscrollFooter(Drawable footer) {
        this.mOverScrollFooter = footer;
        invalidate();
    }

    public Drawable getOverscrollFooter() {
        return this.mOverScrollFooter;
    }

    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        ListAdapter adapter = this.mAdapter;
        int closetChildIndex = NO_POSITION;
        int closestChildTop = 0;
        if (!(adapter == null || !gainFocus || previouslyFocusedRect == null)) {
            previouslyFocusedRect.offset(this.mScrollX, this.mScrollY);
            if (adapter.getCount() < getChildCount() + this.mFirstPosition) {
                this.mLayoutMode = 0;
                layoutChildren();
            }
            Rect otherRect = this.mTempRect;
            int minDistance = HwBootFail.STAGE_BOOT_SUCCESS;
            int childCount = getChildCount();
            int firstPosition = this.mFirstPosition;
            for (int i = 0; i < childCount; i++) {
                if (adapter.isEnabled(firstPosition + i)) {
                    View other = getChildAt(i);
                    other.getDrawingRect(otherRect);
                    offsetDescendantRectToMyCoords(other, otherRect);
                    int distance = AbsListView.getDistance(previouslyFocusedRect, otherRect, direction);
                    if (distance < minDistance) {
                        minDistance = distance;
                        closetChildIndex = i;
                        closestChildTop = other.getTop();
                    }
                }
            }
        }
        if (closetChildIndex >= 0) {
            setSelectionFromTop(this.mFirstPosition + closetChildIndex, closestChildTop);
        } else {
            requestLayout();
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        int count = getChildCount();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                addHeaderView(getChildAt(i));
            }
            removeAllViews();
        }
    }

    protected View findViewTraversal(int id) {
        View v = super.findViewTraversal(id);
        if (v == null) {
            v = findViewInHeadersOrFooters(this.mHeaderViewInfos, id);
            if (v != null) {
                return v;
            }
            v = findViewInHeadersOrFooters(this.mFooterViewInfos, id);
            if (v != null) {
                return v;
            }
        }
        return v;
    }

    View findViewInHeadersOrFooters(ArrayList<FixedViewInfo> where, int id) {
        if (where != null) {
            int len = where.size();
            for (int i = 0; i < len; i++) {
                View v = ((FixedViewInfo) where.get(i)).view;
                if (!v.isRootNamespace()) {
                    v = v.findViewById(id);
                    if (v != null) {
                        return v;
                    }
                }
            }
        }
        return null;
    }

    protected View findViewWithTagTraversal(Object tag) {
        View v = super.findViewWithTagTraversal(tag);
        if (v == null) {
            v = findViewWithTagInHeadersOrFooters(this.mHeaderViewInfos, tag);
            if (v != null) {
                return v;
            }
            v = findViewWithTagInHeadersOrFooters(this.mFooterViewInfos, tag);
            if (v != null) {
                return v;
            }
        }
        return v;
    }

    View findViewWithTagInHeadersOrFooters(ArrayList<FixedViewInfo> where, Object tag) {
        if (where != null) {
            int len = where.size();
            for (int i = 0; i < len; i++) {
                View v = ((FixedViewInfo) where.get(i)).view;
                if (!v.isRootNamespace()) {
                    v = v.findViewWithTag(tag);
                    if (v != null) {
                        return v;
                    }
                }
            }
        }
        return null;
    }

    protected View findViewByPredicateTraversal(Predicate<View> predicate, View childToSkip) {
        View v = super.findViewByPredicateTraversal(predicate, childToSkip);
        if (v == null) {
            v = findViewByPredicateInHeadersOrFooters(this.mHeaderViewInfos, predicate, childToSkip);
            if (v != null) {
                return v;
            }
            v = findViewByPredicateInHeadersOrFooters(this.mFooterViewInfos, predicate, childToSkip);
            if (v != null) {
                return v;
            }
        }
        return v;
    }

    View findViewByPredicateInHeadersOrFooters(ArrayList<FixedViewInfo> where, Predicate<View> predicate, View childToSkip) {
        if (where != null) {
            int len = where.size();
            for (int i = 0; i < len; i++) {
                View v = ((FixedViewInfo) where.get(i)).view;
                if (!(v == childToSkip || v.isRootNamespace())) {
                    v = v.findViewByPredicate(predicate);
                    if (v != null) {
                        return v;
                    }
                }
            }
        }
        return null;
    }

    @Deprecated
    public long[] getCheckItemIds() {
        if (this.mAdapter != null && this.mAdapter.hasStableIds()) {
            return getCheckedItemIds();
        }
        if (this.mChoiceMode == 0 || this.mCheckStates == null || this.mAdapter == null) {
            return new long[0];
        }
        SparseBooleanArray states = this.mCheckStates;
        int count = states.size();
        long[] ids = new long[count];
        ListAdapter adapter = this.mAdapter;
        int i = 0;
        int checkedCount = 0;
        while (i < count) {
            int checkedCount2;
            if (states.valueAt(i)) {
                checkedCount2 = checkedCount + 1;
                ids[checkedCount] = adapter.getItemId(states.keyAt(i));
            } else {
                checkedCount2 = checkedCount;
            }
            i++;
            checkedCount = checkedCount2;
        }
        if (checkedCount == count) {
            return ids;
        }
        long[] result = new long[checkedCount];
        System.arraycopy(ids, 0, result, 0, checkedCount);
        return result;
    }

    int getHeightForPosition(int position) {
        int height = super.getHeightForPosition(position);
        if (shouldAdjustHeightForDivider(position)) {
            return this.mDividerHeight + height;
        }
        return height;
    }

    private boolean shouldAdjustHeightForDivider(int itemIndex) {
        int dividerHeight = this.mDividerHeight;
        Drawable overscrollHeader = this.mOverScrollHeader;
        Drawable overscrollFooter = this.mOverScrollFooter;
        boolean drawOverscrollHeader = overscrollHeader != null;
        boolean drawOverscrollFooter = overscrollFooter != null;
        boolean drawDividers = dividerHeight > 0 && this.mDivider != null;
        if (drawDividers) {
            boolean fillForMissingDividers = isOpaque() && !super.isOpaque();
            int itemCount = this.mItemCount;
            int headerCount = this.mHeaderViewInfos.size();
            int footerLimit = itemCount - this.mFooterViewInfos.size();
            boolean isHeader = itemIndex < headerCount;
            boolean isFooter = itemIndex >= footerLimit;
            boolean headerDividers = this.mHeaderDividersEnabled;
            boolean footerDividers = this.mFooterDividersEnabled;
            if ((headerDividers || !isHeader) && (footerDividers || !isFooter)) {
                ListAdapter adapter = this.mAdapter;
                if (this.mStackFromBottom) {
                    boolean isFirstItem = itemIndex == (drawOverscrollHeader ? 1 : 0);
                    if (!isFirstItem) {
                        int previousIndex = itemIndex + NO_POSITION;
                        if (checkIsEnabled(adapter, itemIndex) && ((headerDividers || (!isHeader && previousIndex >= headerCount)) && (isFirstItem || (checkIsEnabled(adapter, previousIndex) && (footerDividers || (!isFooter && previousIndex < footerLimit)))))) {
                            return true;
                        }
                        if (fillForMissingDividers) {
                            return true;
                        }
                    }
                }
                boolean isLastItem = itemIndex == itemCount + NO_POSITION;
                if (!(drawOverscrollFooter && isLastItem)) {
                    int nextIndex = itemIndex + 1;
                    if (checkIsEnabled(adapter, itemIndex) && ((headerDividers || (!isHeader && nextIndex >= headerCount)) && (isLastItem || (checkIsEnabled(adapter, nextIndex) && (footerDividers || (!isFooter && nextIndex < footerLimit)))))) {
                        return true;
                    }
                    if (fillForMissingDividers) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public CharSequence getAccessibilityClassName() {
        return ListView.class.getName();
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        int rowsCount = getCount();
        info.setCollectionInfo(CollectionInfo.obtain(rowsCount, 1, false, getSelectionModeForAccessibility()));
        if (rowsCount > 0) {
            info.addAction(AccessibilityAction.ACTION_SCROLL_TO_POSITION);
        }
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (super.performAccessibilityActionInternal(action, arguments)) {
            return true;
        }
        switch (action) {
            case R.id.accessibilityActionScrollToPosition /*16908343*/:
                int row = arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_ROW_INT, NO_POSITION);
                int position = Math.min(row, getCount() + NO_POSITION);
                if (row >= 0) {
                    smoothScrollToPosition(position);
                    return true;
                }
                break;
        }
        return false;
    }

    public void onInitializeAccessibilityNodeInfoForItem(View view, int position, AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoForItem(view, position, info);
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        boolean isHeading = lp != null && lp.viewType == -2;
        info.setCollectionItemInfo(CollectionItemInfo.obtain(position, 1, 0, 1, isHeading, isItemChecked(position)));
    }

    protected void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("recycleOnMeasure", recycleOnMeasure());
    }
}
