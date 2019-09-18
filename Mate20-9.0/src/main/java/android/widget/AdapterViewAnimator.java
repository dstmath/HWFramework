package android.widget;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.rms.AppAssociate;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsAdapter;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class AdapterViewAnimator extends AdapterView<Adapter> implements RemoteViewsAdapter.RemoteAdapterConnectionCallback, Advanceable {
    private static final int DEFAULT_ANIMATION_DURATION = 200;
    private static final String TAG = "RemoteViewAnimator";
    static final int TOUCH_MODE_DOWN_IN_CURRENT_VIEW = 1;
    static final int TOUCH_MODE_HANDLED = 2;
    static final int TOUCH_MODE_NONE = 0;
    int mActiveOffset;
    Adapter mAdapter;
    boolean mAnimateFirstTime;
    int mCurrentWindowEnd;
    int mCurrentWindowStart;
    int mCurrentWindowStartUnbounded;
    AdapterView<Adapter>.AdapterDataSetObserver mDataSetObserver;
    boolean mDeferNotifyDataSetChanged;
    boolean mFirstTime;
    ObjectAnimator mInAnimation;
    boolean mLoopViews;
    int mMaxNumActiveViews;
    ObjectAnimator mOutAnimation;
    private Runnable mPendingCheckForTap;
    ArrayList<Integer> mPreviousViews;
    int mReferenceChildHeight;
    int mReferenceChildWidth;
    RemoteViewsAdapter mRemoteViewsAdapter;
    private int mRestoreWhichChild;
    /* access modifiers changed from: private */
    public int mTouchMode;
    HashMap<Integer, ViewAndMetaData> mViewsMap;
    int mWhichChild;

    final class CheckForTap implements Runnable {
        CheckForTap() {
        }

        public void run() {
            if (AdapterViewAnimator.this.mTouchMode == 1) {
                AdapterViewAnimator.this.showTapFeedback(AdapterViewAnimator.this.getCurrentView());
            }
        }
    }

    static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int whichChild;

        SavedState(Parcelable superState, int whichChild2) {
            super(superState);
            this.whichChild = whichChild2;
        }

        private SavedState(Parcel in) {
            super(in);
            this.whichChild = in.readInt();
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.whichChild);
        }

        public String toString() {
            return "AdapterViewAnimator.SavedState{ whichChild = " + this.whichChild + " }";
        }
    }

    class ViewAndMetaData {
        int adapterPosition;
        long itemId;
        int relativeIndex;
        View view;

        ViewAndMetaData(View view2, int relativeIndex2, int adapterPosition2, long itemId2) {
            this.view = view2;
            this.relativeIndex = relativeIndex2;
            this.adapterPosition = adapterPosition2;
            this.itemId = itemId2;
        }
    }

    public AdapterViewAnimator(Context context) {
        this(context, null);
    }

    public AdapterViewAnimator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdapterViewAnimator(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AdapterViewAnimator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mWhichChild = 0;
        this.mRestoreWhichChild = -1;
        this.mAnimateFirstTime = true;
        this.mActiveOffset = 0;
        this.mMaxNumActiveViews = 1;
        this.mViewsMap = new HashMap<>();
        this.mCurrentWindowStart = 0;
        this.mCurrentWindowEnd = -1;
        this.mCurrentWindowStartUnbounded = 0;
        this.mDeferNotifyDataSetChanged = false;
        this.mFirstTime = true;
        this.mLoopViews = true;
        this.mReferenceChildWidth = -1;
        this.mReferenceChildHeight = -1;
        this.mTouchMode = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AdapterViewAnimator, defStyleAttr, defStyleRes);
        int resource = a.getResourceId(0, 0);
        if (resource > 0) {
            setInAnimation(context, resource);
        } else {
            setInAnimation(getDefaultInAnimation());
        }
        int resource2 = a.getResourceId(1, 0);
        if (resource2 > 0) {
            setOutAnimation(context, resource2);
        } else {
            setOutAnimation(getDefaultOutAnimation());
        }
        setAnimateFirstView(a.getBoolean(2, true));
        this.mLoopViews = a.getBoolean(3, false);
        a.recycle();
        initViewAnimator();
    }

    private void initViewAnimator() {
        this.mPreviousViews = new ArrayList<>();
    }

    /* access modifiers changed from: package-private */
    public void configureViewAnimator(int numVisibleViews, int activeOffset) {
        this.mMaxNumActiveViews = numVisibleViews;
        this.mActiveOffset = activeOffset;
        this.mPreviousViews.clear();
        this.mViewsMap.clear();
        removeAllViewsInLayout();
        this.mCurrentWindowStart = 0;
        this.mCurrentWindowEnd = -1;
    }

    /* access modifiers changed from: package-private */
    public void transformViewForTransition(int fromIndex, int toIndex, View view, boolean animate) {
        if (fromIndex == -1) {
            this.mInAnimation.setTarget(view);
            this.mInAnimation.start();
        } else if (toIndex == -1) {
            this.mOutAnimation.setTarget(view);
            this.mOutAnimation.start();
        }
    }

    /* access modifiers changed from: package-private */
    public ObjectAnimator getDefaultInAnimation() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(null, AppAssociate.ASSOC_WINDOW_ALPHA, new float[]{0.0f, 1.0f});
        anim.setDuration(200);
        return anim;
    }

    /* access modifiers changed from: package-private */
    public ObjectAnimator getDefaultOutAnimation() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(null, AppAssociate.ASSOC_WINDOW_ALPHA, new float[]{1.0f, 0.0f});
        anim.setDuration(200);
        return anim;
    }

    @RemotableViewMethod
    public void setDisplayedChild(int whichChild) {
        setDisplayedChild(whichChild, true);
    }

    private void setDisplayedChild(int whichChild, boolean animate) {
        if (this.mAdapter != null) {
            this.mWhichChild = whichChild;
            boolean z = true;
            if (whichChild >= getWindowSize()) {
                this.mWhichChild = this.mLoopViews ? 0 : getWindowSize() - 1;
            } else if (whichChild < 0) {
                this.mWhichChild = this.mLoopViews ? getWindowSize() - 1 : 0;
            }
            if (getFocusedChild() == null) {
                z = false;
            }
            boolean hasFocus = z;
            showOnly(this.mWhichChild, animate);
            if (hasFocus) {
                requestFocus(2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void applyTransformForChildAtIndex(View child, int relativeIndex) {
    }

    public int getDisplayedChild() {
        return this.mWhichChild;
    }

    public void showNext() {
        setDisplayedChild(this.mWhichChild + 1);
    }

    public void showPrevious() {
        setDisplayedChild(this.mWhichChild - 1);
    }

    /* access modifiers changed from: package-private */
    public int modulo(int pos, int size) {
        if (size > 0) {
            return ((pos % size) + size) % size;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public View getViewAtRelativeIndex(int relativeIndex) {
        if (relativeIndex >= 0 && relativeIndex <= getNumActiveViews() - 1 && this.mAdapter != null) {
            int i = modulo(this.mCurrentWindowStartUnbounded + relativeIndex, getWindowSize());
            if (this.mViewsMap.get(Integer.valueOf(i)) != null) {
                return this.mViewsMap.get(Integer.valueOf(i)).view;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public int getNumActiveViews() {
        if (this.mAdapter != null) {
            return Math.min(getCount() + 1, this.mMaxNumActiveViews);
        }
        return this.mMaxNumActiveViews;
    }

    /* access modifiers changed from: package-private */
    public int getWindowSize() {
        if (this.mAdapter == null) {
            return 0;
        }
        int adapterCount = getCount();
        if (adapterCount > getNumActiveViews() || !this.mLoopViews) {
            return adapterCount;
        }
        return this.mMaxNumActiveViews * adapterCount;
    }

    private ViewAndMetaData getMetaDataForChild(View child) {
        for (ViewAndMetaData vm : this.mViewsMap.values()) {
            if (vm.view == child) {
                return vm;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ViewGroup.LayoutParams createOrReuseLayoutParams(View v) {
        ViewGroup.LayoutParams currentLp = v.getLayoutParams();
        if (currentLp != null) {
            return currentLp;
        }
        return new ViewGroup.LayoutParams(0, 0);
    }

    /* access modifiers changed from: package-private */
    public void refreshChildren() {
        if (this.mAdapter != null) {
            for (int i = this.mCurrentWindowStart; i <= this.mCurrentWindowEnd; i++) {
                int index = modulo(i, getWindowSize());
                View updatedChild = this.mAdapter.getView(modulo(i, getCount()), null, this);
                if (updatedChild.getImportantForAccessibility() == 0) {
                    updatedChild.setImportantForAccessibility(1);
                }
                if (this.mViewsMap.containsKey(Integer.valueOf(index))) {
                    FrameLayout fl = (FrameLayout) this.mViewsMap.get(Integer.valueOf(index)).view;
                    if (updatedChild != null) {
                        fl.removeAllViewsInLayout();
                        fl.addView(updatedChild);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public FrameLayout getFrameForChild() {
        return new FrameLayout(this.mContext);
    }

    /* access modifiers changed from: package-private */
    public void showOnly(int childIndex, boolean animate) {
        int oldRelativeIndex;
        int newWindowStartUnbounded;
        int adapterCount;
        int rangeStart;
        int newWindowEndUnbounded;
        int rangeEnd;
        int newWindowStartUnbounded2;
        int adapterCount2;
        boolean z = animate;
        if (this.mAdapter != null) {
            int newWindowStart = getCount();
            if (newWindowStart != 0) {
                for (int i = 0; i < this.mPreviousViews.size(); i++) {
                    View viewToRemove = this.mViewsMap.get(this.mPreviousViews.get(i)).view;
                    this.mViewsMap.remove(this.mPreviousViews.get(i));
                    viewToRemove.clearAnimation();
                    if (viewToRemove instanceof ViewGroup) {
                        ((ViewGroup) viewToRemove).removeAllViewsInLayout();
                    }
                    applyTransformForChildAtIndex(viewToRemove, -1);
                    removeViewInLayout(viewToRemove);
                }
                this.mPreviousViews.clear();
                int newWindowEnd = childIndex - this.mActiveOffset;
                int newWindowEndUnbounded2 = (getNumActiveViews() + newWindowEnd) - 1;
                int newWindowStart2 = Math.max(0, newWindowEnd);
                int newWindowEnd2 = Math.min(newWindowStart - 1, newWindowEndUnbounded2);
                if (this.mLoopViews) {
                    newWindowStart2 = newWindowEnd;
                    newWindowEnd2 = newWindowEndUnbounded2;
                }
                int newWindowStart3 = newWindowStart2;
                int newWindowEnd3 = newWindowEnd2;
                int rangeStart2 = modulo(newWindowStart3, getWindowSize());
                int rangeEnd2 = modulo(newWindowEnd3, getWindowSize());
                boolean wrap = false;
                if (rangeStart2 > rangeEnd2) {
                    wrap = true;
                }
                boolean wrap2 = wrap;
                for (Integer index : this.mViewsMap.keySet()) {
                    boolean remove = false;
                    if (!wrap2 && (index.intValue() < rangeStart2 || index.intValue() > rangeEnd2)) {
                        remove = true;
                    } else if (wrap2 && index.intValue() > rangeEnd2 && index.intValue() < rangeStart2) {
                        remove = true;
                    }
                    if (remove) {
                        View previousView = this.mViewsMap.get(index).view;
                        int oldRelativeIndex2 = this.mViewsMap.get(index).relativeIndex;
                        this.mPreviousViews.add(index);
                        transformViewForTransition(oldRelativeIndex2, -1, previousView, z);
                    }
                }
                if (newWindowStart3 == this.mCurrentWindowStart && newWindowEnd3 == this.mCurrentWindowEnd && newWindowEnd == this.mCurrentWindowStartUnbounded) {
                    int i2 = rangeEnd2;
                    int i3 = rangeStart2;
                    int i4 = newWindowStart;
                    int i5 = newWindowEnd;
                    int i6 = newWindowEndUnbounded2;
                    int adapterCount3 = newWindowStart3;
                    int newWindowStartUnbounded3 = newWindowEnd3;
                } else {
                    int i7 = newWindowStart3;
                    while (true) {
                        int i8 = i7;
                        if (i8 > newWindowEnd3) {
                            break;
                        }
                        int index2 = modulo(i8, getWindowSize());
                        if (this.mViewsMap.containsKey(Integer.valueOf(index2))) {
                            oldRelativeIndex = this.mViewsMap.get(Integer.valueOf(index2)).relativeIndex;
                        } else {
                            oldRelativeIndex = -1;
                        }
                        int oldRelativeIndex3 = oldRelativeIndex;
                        int newRelativeIndex = i8 - newWindowEnd;
                        if (this.mViewsMap.containsKey(Integer.valueOf(index2)) && !this.mPreviousViews.contains(Integer.valueOf(index2))) {
                            View view = this.mViewsMap.get(Integer.valueOf(index2)).view;
                            rangeEnd = rangeEnd2;
                            this.mViewsMap.get(Integer.valueOf(index2)).relativeIndex = newRelativeIndex;
                            applyTransformForChildAtIndex(view, newRelativeIndex);
                            transformViewForTransition(oldRelativeIndex3, newRelativeIndex, view, z);
                            int i9 = newRelativeIndex;
                            rangeStart = rangeStart2;
                            adapterCount = newWindowStart;
                            newWindowStartUnbounded = newWindowEnd;
                            int i10 = oldRelativeIndex3;
                            newWindowEndUnbounded = newWindowEndUnbounded2;
                            adapterCount2 = newWindowStart3;
                            newWindowStartUnbounded2 = newWindowEnd3;
                        } else {
                            rangeEnd = rangeEnd2;
                            int adapterPosition = modulo(i8, newWindowStart);
                            View newView = this.mAdapter.getView(adapterPosition, null, this);
                            long itemId = this.mAdapter.getItemId(adapterPosition);
                            FrameLayout fl = getFrameForChild();
                            if (newView != null) {
                                fl.addView(newView);
                            }
                            HashMap<Integer, ViewAndMetaData> hashMap = this.mViewsMap;
                            int i11 = oldRelativeIndex3;
                            Integer valueOf = Integer.valueOf(index2);
                            newWindowEndUnbounded = newWindowEndUnbounded2;
                            FrameLayout fl2 = fl;
                            View view2 = newView;
                            int newRelativeIndex2 = newRelativeIndex;
                            rangeStart = rangeStart2;
                            adapterCount = newWindowStart;
                            newWindowStartUnbounded = newWindowEnd;
                            adapterCount2 = newWindowStart3;
                            newWindowStartUnbounded2 = newWindowEnd3;
                            ViewAndMetaData viewAndMetaData = new ViewAndMetaData(fl2, newRelativeIndex2, adapterPosition, itemId);
                            hashMap.put(valueOf, viewAndMetaData);
                            FrameLayout fl3 = fl2;
                            addChild(fl3);
                            int newRelativeIndex3 = newRelativeIndex2;
                            applyTransformForChildAtIndex(fl3, newRelativeIndex3);
                            transformViewForTransition(-1, newRelativeIndex3, fl3, z);
                        }
                        this.mViewsMap.get(Integer.valueOf(index2)).view.bringToFront();
                        i7 = i8 + 1;
                        newWindowStart3 = adapterCount2;
                        newWindowEnd3 = newWindowStartUnbounded2;
                        rangeEnd2 = rangeEnd;
                        newWindowEndUnbounded2 = newWindowEndUnbounded;
                        rangeStart2 = rangeStart;
                        newWindowStart = adapterCount;
                        newWindowEnd = newWindowStartUnbounded;
                    }
                    int i12 = rangeStart2;
                    int adapterCount4 = newWindowStart;
                    int i13 = newWindowEndUnbounded2;
                    this.mCurrentWindowStart = newWindowStart3;
                    this.mCurrentWindowEnd = newWindowEnd3;
                    this.mCurrentWindowStartUnbounded = newWindowEnd;
                    if (this.mRemoteViewsAdapter != null) {
                        int adapterCount5 = adapterCount4;
                        this.mRemoteViewsAdapter.setVisibleRangeHint(modulo(this.mCurrentWindowStart, adapterCount5), modulo(this.mCurrentWindowEnd, adapterCount5));
                    }
                }
                requestLayout();
                invalidate();
            }
        }
    }

    private void addChild(View child) {
        addViewInLayout(child, -1, createOrReuseLayoutParams(child));
        if (this.mReferenceChildWidth == -1 || this.mReferenceChildHeight == -1) {
            int measureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
            child.measure(measureSpec, measureSpec);
            this.mReferenceChildWidth = child.getMeasuredWidth();
            this.mReferenceChildHeight = child.getMeasuredHeight();
        }
    }

    /* access modifiers changed from: package-private */
    public void showTapFeedback(View v) {
        v.setPressed(true);
    }

    /* access modifiers changed from: package-private */
    public void hideTapFeedback(View v) {
        v.setPressed(false);
    }

    /* access modifiers changed from: package-private */
    public void cancelHandleClick() {
        View v = getCurrentView();
        if (v != null) {
            hideTapFeedback(v);
        }
        this.mTouchMode = 0;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        boolean handled = false;
        if (action != 6) {
            switch (action) {
                case 0:
                    View v = getCurrentView();
                    if (v != null && isTransformedTouchPointInView(ev.getX(), ev.getY(), v, null)) {
                        if (this.mPendingCheckForTap == null) {
                            this.mPendingCheckForTap = new CheckForTap();
                        }
                        this.mTouchMode = 1;
                        postDelayed(this.mPendingCheckForTap, (long) ViewConfiguration.getTapTimeout());
                        break;
                    }
                case 1:
                    if (this.mTouchMode == 1) {
                        final View v2 = getCurrentView();
                        final ViewAndMetaData viewData = getMetaDataForChild(v2);
                        if (v2 != null && isTransformedTouchPointInView(ev.getX(), ev.getY(), v2, null)) {
                            Handler handler = getHandler();
                            if (handler != null) {
                                handler.removeCallbacks(this.mPendingCheckForTap);
                            }
                            showTapFeedback(v2);
                            postDelayed(new Runnable() {
                                public void run() {
                                    AdapterViewAnimator.this.hideTapFeedback(v2);
                                    AdapterViewAnimator.this.post(new Runnable() {
                                        public void run() {
                                            if (viewData != null) {
                                                AdapterViewAnimator.this.performItemClick(v2, viewData.adapterPosition, viewData.itemId);
                                            } else {
                                                AdapterViewAnimator.this.performItemClick(v2, 0, 0);
                                            }
                                        }
                                    });
                                }
                            }, (long) ViewConfiguration.getPressedStateDuration());
                            handled = true;
                        }
                    }
                    this.mTouchMode = 0;
                    break;
                case 3:
                    View v3 = getCurrentView();
                    if (v3 != null) {
                        hideTapFeedback(v3);
                    }
                    this.mTouchMode = 0;
                    break;
            }
        }
        return handled;
    }

    private void measureChildren() {
        int count = getChildCount();
        int childWidth = (getMeasuredWidth() - this.mPaddingLeft) - this.mPaddingRight;
        int childHeight = (getMeasuredHeight() - this.mPaddingTop) - this.mPaddingBottom;
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(View.MeasureSpec.makeMeasureSpec(childWidth, 1073741824), View.MeasureSpec.makeMeasureSpec(childHeight, 1073741824));
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        int widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec);
        int widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int i2 = 0;
        boolean haveChildRefSize = (this.mReferenceChildWidth == -1 || this.mReferenceChildHeight == -1) ? false : true;
        if (heightSpecMode == 0) {
            if (haveChildRefSize) {
                i = this.mReferenceChildHeight + this.mPaddingTop + this.mPaddingBottom;
            } else {
                i = 0;
            }
            heightSpecSize = i;
        } else if (heightSpecMode == Integer.MIN_VALUE && haveChildRefSize) {
            int height = this.mReferenceChildHeight + this.mPaddingTop + this.mPaddingBottom;
            heightSpecSize = height > heightSpecSize ? heightSpecSize | 16777216 : height;
        }
        if (widthSpecMode == 0) {
            if (haveChildRefSize) {
                i2 = this.mPaddingRight + this.mReferenceChildWidth + this.mPaddingLeft;
            }
            widthSpecSize = i2;
        } else if (heightSpecMode == Integer.MIN_VALUE && haveChildRefSize) {
            int width = this.mReferenceChildWidth + this.mPaddingLeft + this.mPaddingRight;
            widthSpecSize = width > widthSpecSize ? widthSpecSize | 16777216 : width;
        }
        setMeasuredDimension(widthSpecSize, heightSpecSize);
        measureChildren();
    }

    /* access modifiers changed from: package-private */
    public void checkForAndHandleDataChanged() {
        if (this.mDataChanged) {
            post(new Runnable() {
                public void run() {
                    AdapterViewAnimator.this.handleDataChanged();
                    if (AdapterViewAnimator.this.mWhichChild >= AdapterViewAnimator.this.getWindowSize()) {
                        AdapterViewAnimator.this.mWhichChild = 0;
                        AdapterViewAnimator.this.showOnly(AdapterViewAnimator.this.mWhichChild, false);
                    } else if (AdapterViewAnimator.this.mOldItemCount != AdapterViewAnimator.this.getCount()) {
                        AdapterViewAnimator.this.showOnly(AdapterViewAnimator.this.mWhichChild, false);
                    }
                    AdapterViewAnimator.this.refreshChildren();
                    AdapterViewAnimator.this.requestLayout();
                }
            });
        }
        this.mDataChanged = false;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        checkForAndHandleDataChanged();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.layout(this.mPaddingLeft, this.mPaddingTop, this.mPaddingLeft + child.getMeasuredWidth(), this.mPaddingTop + child.getMeasuredHeight());
        }
    }

    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (this.mRemoteViewsAdapter != null) {
            this.mRemoteViewsAdapter.saveRemoteViewsCache();
        }
        return new SavedState(superState, this.mWhichChild);
    }

    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.mWhichChild = ss.whichChild;
        if (this.mRemoteViewsAdapter == null || this.mAdapter != null) {
            setDisplayedChild(this.mWhichChild, false);
        } else {
            this.mRestoreWhichChild = this.mWhichChild;
        }
    }

    public View getCurrentView() {
        return getViewAtRelativeIndex(this.mActiveOffset);
    }

    public ObjectAnimator getInAnimation() {
        return this.mInAnimation;
    }

    public void setInAnimation(ObjectAnimator inAnimation) {
        this.mInAnimation = inAnimation;
    }

    public ObjectAnimator getOutAnimation() {
        return this.mOutAnimation;
    }

    public void setOutAnimation(ObjectAnimator outAnimation) {
        this.mOutAnimation = outAnimation;
    }

    public void setInAnimation(Context context, int resourceID) {
        setInAnimation((ObjectAnimator) AnimatorInflater.loadAnimator(context, resourceID));
    }

    public void setOutAnimation(Context context, int resourceID) {
        setOutAnimation((ObjectAnimator) AnimatorInflater.loadAnimator(context, resourceID));
    }

    public void setAnimateFirstView(boolean animate) {
        this.mAnimateFirstTime = animate;
    }

    public int getBaseline() {
        return getCurrentView() != null ? getCurrentView().getBaseline() : super.getBaseline();
    }

    public Adapter getAdapter() {
        return this.mAdapter;
    }

    public void setAdapter(Adapter adapter) {
        if (!(this.mAdapter == null || this.mDataSetObserver == null)) {
            this.mAdapter.unregisterDataSetObserver(this.mDataSetObserver);
        }
        this.mAdapter = adapter;
        checkFocus();
        if (this.mAdapter != null) {
            this.mDataSetObserver = new AdapterView.AdapterDataSetObserver();
            this.mAdapter.registerDataSetObserver(this.mDataSetObserver);
            this.mItemCount = this.mAdapter.getCount();
        }
        setFocusable(true);
        this.mWhichChild = 0;
        showOnly(this.mWhichChild, false);
    }

    @RemotableViewMethod(asyncImpl = "setRemoteViewsAdapterAsync")
    public void setRemoteViewsAdapter(Intent intent) {
        setRemoteViewsAdapter(intent, false);
    }

    public Runnable setRemoteViewsAdapterAsync(Intent intent) {
        return new RemoteViewsAdapter.AsyncRemoteAdapterAction(this, intent);
    }

    public void setRemoteViewsAdapter(Intent intent, boolean isAsync) {
        if (this.mRemoteViewsAdapter == null || !new Intent.FilterComparison(intent).equals(new Intent.FilterComparison(this.mRemoteViewsAdapter.getRemoteViewsServiceIntent()))) {
            this.mDeferNotifyDataSetChanged = false;
            this.mRemoteViewsAdapter = new RemoteViewsAdapter(getContext(), intent, this, isAsync);
            if (this.mRemoteViewsAdapter.isDataReady()) {
                setAdapter(this.mRemoteViewsAdapter);
            }
        }
    }

    public void setRemoteViewsOnClickHandler(RemoteViews.OnClickHandler handler) {
        if (this.mRemoteViewsAdapter != null) {
            this.mRemoteViewsAdapter.setRemoteViewsOnClickHandler(handler);
        }
    }

    public void setSelection(int position) {
        setDisplayedChild(position);
    }

    public View getSelectedView() {
        return getViewAtRelativeIndex(this.mActiveOffset);
    }

    public void deferNotifyDataSetChanged() {
        this.mDeferNotifyDataSetChanged = true;
    }

    public boolean onRemoteAdapterConnected() {
        if (this.mRemoteViewsAdapter != this.mAdapter) {
            setAdapter(this.mRemoteViewsAdapter);
            if (this.mDeferNotifyDataSetChanged) {
                this.mRemoteViewsAdapter.notifyDataSetChanged();
                this.mDeferNotifyDataSetChanged = false;
            }
            if (this.mRestoreWhichChild > -1) {
                setDisplayedChild(this.mRestoreWhichChild, false);
                this.mRestoreWhichChild = -1;
            }
            return false;
        } else if (this.mRemoteViewsAdapter == null) {
            return false;
        } else {
            this.mRemoteViewsAdapter.superNotifyDataSetChanged();
            return true;
        }
    }

    public void onRemoteAdapterDisconnected() {
    }

    public void advance() {
        showNext();
    }

    public void fyiWillBeAdvancedByHostKThx() {
    }

    public CharSequence getAccessibilityClassName() {
        return AdapterViewAnimator.class.getName();
    }
}
