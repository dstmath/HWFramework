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
    private int mTouchMode;
    HashMap<Integer, ViewAndMetaData> mViewsMap;
    int mWhichChild;

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
        saveAttributeDataForStyleable(context, R.styleable.AdapterViewAnimator, attrs, a, defStyleAttr, defStyleRes);
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
    public class ViewAndMetaData {
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
        ObjectAnimator anim = ObjectAnimator.ofFloat((Object) null, AppAssociate.ASSOC_WINDOW_ALPHA, 0.0f, 1.0f);
        anim.setDuration(200L);
        return anim;
    }

    /* access modifiers changed from: package-private */
    public ObjectAnimator getDefaultOutAnimation() {
        ObjectAnimator anim = ObjectAnimator.ofFloat((Object) null, AppAssociate.ASSOC_WINDOW_ALPHA, 1.0f, 0.0f);
        anim.setDuration(200L);
        return anim;
    }

    @RemotableViewMethod
    public void setDisplayedChild(int whichChild) {
        setDisplayedChild(whichChild, true);
    }

    private void setDisplayedChild(int whichChild, boolean animate) {
        if (this.mAdapter != null) {
            this.mWhichChild = whichChild;
            boolean hasFocus = false;
            if (whichChild >= getWindowSize()) {
                this.mWhichChild = this.mLoopViews ? 0 : getWindowSize() - 1;
            } else if (whichChild < 0) {
                this.mWhichChild = this.mLoopViews ? getWindowSize() - 1 : 0;
            }
            if (getFocusedChild() != null) {
                hasFocus = true;
            }
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
        if (relativeIndex < 0 || relativeIndex > getNumActiveViews() - 1 || this.mAdapter == null) {
            return null;
        }
        int i = modulo(this.mCurrentWindowStartUnbounded + relativeIndex, getWindowSize());
        if (this.mViewsMap.get(Integer.valueOf(i)) != null) {
            return this.mViewsMap.get(Integer.valueOf(i)).view;
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
                    fl.removeAllViewsInLayout();
                    fl.addView(updatedChild);
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
        int adapterCount;
        int newWindowStart;
        int newWindowEnd;
        int rangeStart;
        int rangeEnd;
        int newWindowStartUnbounded;
        int adapterCount2;
        int newWindowEndUnbounded;
        int newWindowEnd2;
        int newRelativeIndex;
        if (!(this.mAdapter == null || (adapterCount = getCount()) == 0)) {
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
            int newWindowStartUnbounded2 = childIndex - this.mActiveOffset;
            int newWindowEndUnbounded2 = (getNumActiveViews() + newWindowStartUnbounded2) - 1;
            int newWindowStart2 = Math.max(0, newWindowStartUnbounded2);
            int newWindowEnd3 = Math.min(adapterCount - 1, newWindowEndUnbounded2);
            if (this.mLoopViews) {
                newWindowStart = newWindowStartUnbounded2;
                newWindowEnd = newWindowEndUnbounded2;
            } else {
                newWindowStart = newWindowStart2;
                newWindowEnd = newWindowEnd3;
            }
            int rangeStart2 = modulo(newWindowStart, getWindowSize());
            int rangeEnd2 = modulo(newWindowEnd, getWindowSize());
            boolean wrap = rangeStart2 > rangeEnd2;
            for (Integer index : this.mViewsMap.keySet()) {
                boolean remove = false;
                if (!wrap && (index.intValue() < rangeStart2 || index.intValue() > rangeEnd2)) {
                    remove = true;
                } else if (wrap && index.intValue() > rangeEnd2 && index.intValue() < rangeStart2) {
                    remove = true;
                }
                if (remove) {
                    View previousView = this.mViewsMap.get(index).view;
                    int oldRelativeIndex = this.mViewsMap.get(index).relativeIndex;
                    this.mPreviousViews.add(index);
                    transformViewForTransition(oldRelativeIndex, -1, previousView, animate);
                }
            }
            if (newWindowStart != this.mCurrentWindowStart || newWindowEnd != this.mCurrentWindowEnd || newWindowStartUnbounded2 != this.mCurrentWindowStartUnbounded) {
                int i2 = newWindowStart;
                while (i2 <= newWindowEnd) {
                    int index2 = modulo(i2, getWindowSize());
                    int oldRelativeIndex2 = this.mViewsMap.containsKey(Integer.valueOf(index2)) ? this.mViewsMap.get(Integer.valueOf(index2)).relativeIndex : -1;
                    int newRelativeIndex2 = i2 - newWindowStartUnbounded2;
                    if (this.mViewsMap.containsKey(Integer.valueOf(index2)) && !this.mPreviousViews.contains(Integer.valueOf(index2))) {
                        View view = this.mViewsMap.get(Integer.valueOf(index2)).view;
                        this.mViewsMap.get(Integer.valueOf(index2)).relativeIndex = newRelativeIndex2;
                        applyTransformForChildAtIndex(view, newRelativeIndex2);
                        transformViewForTransition(oldRelativeIndex2, newRelativeIndex2, view, animate);
                        rangeEnd = rangeEnd2;
                        newWindowEnd2 = newWindowEnd;
                        rangeStart = rangeStart2;
                        adapterCount2 = adapterCount;
                        newWindowStartUnbounded = newWindowStartUnbounded2;
                        newWindowEndUnbounded = newWindowEndUnbounded2;
                        newRelativeIndex = -1;
                    } else {
                        int adapterPosition = modulo(i2, adapterCount);
                        View newView = this.mAdapter.getView(adapterPosition, null, this);
                        long itemId = this.mAdapter.getItemId(adapterPosition);
                        FrameLayout fl = getFrameForChild();
                        if (newView != null) {
                            fl.addView(newView);
                        }
                        newWindowEndUnbounded = newWindowEndUnbounded2;
                        adapterCount2 = adapterCount;
                        newWindowStartUnbounded = newWindowStartUnbounded2;
                        rangeEnd = rangeEnd2;
                        rangeStart = rangeStart2;
                        newWindowEnd2 = newWindowEnd;
                        this.mViewsMap.put(Integer.valueOf(index2), new ViewAndMetaData(fl, newRelativeIndex2, adapterPosition, itemId));
                        addChild(fl);
                        applyTransformForChildAtIndex(fl, newRelativeIndex2);
                        newRelativeIndex = -1;
                        transformViewForTransition(-1, newRelativeIndex2, fl, animate);
                    }
                    this.mViewsMap.get(Integer.valueOf(index2)).view.bringToFront();
                    i2++;
                    newWindowEnd = newWindowEnd2;
                    newWindowEndUnbounded2 = newWindowEndUnbounded;
                    adapterCount = adapterCount2;
                    newWindowStartUnbounded2 = newWindowStartUnbounded;
                    rangeEnd2 = rangeEnd;
                    rangeStart2 = rangeStart;
                }
                this.mCurrentWindowStart = newWindowStart;
                this.mCurrentWindowEnd = newWindowEnd;
                this.mCurrentWindowStartUnbounded = newWindowStartUnbounded2;
                if (this.mRemoteViewsAdapter != null) {
                    this.mRemoteViewsAdapter.setVisibleRangeHint(modulo(this.mCurrentWindowStart, adapterCount), modulo(this.mCurrentWindowEnd, adapterCount));
                }
            }
            requestLayout();
            invalidate();
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

    final class CheckForTap implements Runnable {
        CheckForTap() {
        }

        @Override // java.lang.Runnable
        public void run() {
            if (AdapterViewAnimator.this.mTouchMode == 1) {
                AdapterViewAnimator.this.showTapFeedback(AdapterViewAnimator.this.getCurrentView());
            }
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        boolean handled = false;
        if (action == 0) {
            View v = getCurrentView();
            if (v != null && isTransformedTouchPointInView(ev.getX(), ev.getY(), v, null)) {
                if (this.mPendingCheckForTap == null) {
                    this.mPendingCheckForTap = new CheckForTap();
                }
                this.mTouchMode = 1;
                postDelayed(this.mPendingCheckForTap, (long) ViewConfiguration.getTapTimeout());
            }
        } else if (action == 1) {
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
                        /* class android.widget.AdapterViewAnimator.AnonymousClass1 */

                        @Override // java.lang.Runnable
                        public void run() {
                            AdapterViewAnimator.this.hideTapFeedback(v2);
                            AdapterViewAnimator.this.post(new Runnable() {
                                /* class android.widget.AdapterViewAnimator.AnonymousClass1.AnonymousClass1 */

                                @Override // java.lang.Runnable
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
        } else if (action != 2) {
            if (action == 3) {
                View v3 = getCurrentView();
                if (v3 != null) {
                    hideTapFeedback(v3);
                }
                this.mTouchMode = 0;
            } else if (action != 6) {
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
    @Override // android.view.View
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
                /* class android.widget.AdapterViewAnimator.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    AdapterViewAnimator.this.handleDataChanged();
                    if (AdapterViewAnimator.this.mWhichChild >= AdapterViewAnimator.this.getWindowSize()) {
                        AdapterViewAnimator adapterViewAnimator = AdapterViewAnimator.this;
                        adapterViewAnimator.mWhichChild = 0;
                        adapterViewAnimator.showOnly(adapterViewAnimator.mWhichChild, false);
                    } else if (AdapterViewAnimator.this.mOldItemCount != AdapterViewAnimator.this.getCount()) {
                        AdapterViewAnimator adapterViewAnimator2 = AdapterViewAnimator.this;
                        adapterViewAnimator2.showOnly(adapterViewAnimator2.mWhichChild, false);
                    }
                    AdapterViewAnimator.this.refreshChildren();
                    AdapterViewAnimator.this.requestLayout();
                }
            });
        }
        this.mDataChanged = false;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AdapterView, android.view.ViewGroup, android.view.View
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        checkForAndHandleDataChanged();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.layout(this.mPaddingLeft, this.mPaddingTop, this.mPaddingLeft + child.getMeasuredWidth(), this.mPaddingTop + child.getMeasuredHeight());
        }
    }

    /* access modifiers changed from: package-private */
    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            /* class android.widget.AdapterViewAnimator.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override // android.os.Parcelable.Creator
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

        @Override // android.view.View.BaseSavedState, android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.whichChild);
        }

        public String toString() {
            return "AdapterViewAnimator.SavedState{ whichChild = " + this.whichChild + " }";
        }
    }

    @Override // android.view.View
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        RemoteViewsAdapter remoteViewsAdapter = this.mRemoteViewsAdapter;
        if (remoteViewsAdapter != null) {
            remoteViewsAdapter.saveRemoteViewsCache();
        }
        return new SavedState(superState, this.mWhichChild);
    }

    @Override // android.view.View
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

    @Override // android.view.View
    public int getBaseline() {
        return getCurrentView() != null ? getCurrentView().getBaseline() : super.getBaseline();
    }

    @Override // android.widget.AdapterView
    public Adapter getAdapter() {
        return this.mAdapter;
    }

    @Override // android.widget.AdapterView
    public void setAdapter(Adapter adapter) {
        AdapterView<Adapter>.AdapterDataSetObserver adapterDataSetObserver;
        Adapter adapter2 = this.mAdapter;
        if (!(adapter2 == null || (adapterDataSetObserver = this.mDataSetObserver) == null)) {
            adapter2.unregisterDataSetObserver(adapterDataSetObserver);
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

    @Override // android.widget.RemoteViewsAdapter.RemoteAdapterConnectionCallback
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
        RemoteViewsAdapter remoteViewsAdapter = this.mRemoteViewsAdapter;
        if (remoteViewsAdapter != null) {
            remoteViewsAdapter.setRemoteViewsOnClickHandler(handler);
        }
    }

    @Override // android.widget.AdapterView
    public void setSelection(int position) {
        setDisplayedChild(position);
    }

    @Override // android.widget.AdapterView
    public View getSelectedView() {
        return getViewAtRelativeIndex(this.mActiveOffset);
    }

    @Override // android.widget.RemoteViewsAdapter.RemoteAdapterConnectionCallback
    public void deferNotifyDataSetChanged() {
        this.mDeferNotifyDataSetChanged = true;
    }

    @Override // android.widget.RemoteViewsAdapter.RemoteAdapterConnectionCallback
    public boolean onRemoteAdapterConnected() {
        RemoteViewsAdapter remoteViewsAdapter = this.mRemoteViewsAdapter;
        if (remoteViewsAdapter != this.mAdapter) {
            setAdapter(remoteViewsAdapter);
            if (this.mDeferNotifyDataSetChanged) {
                this.mRemoteViewsAdapter.notifyDataSetChanged();
                this.mDeferNotifyDataSetChanged = false;
            }
            int i = this.mRestoreWhichChild;
            if (i > -1) {
                setDisplayedChild(i, false);
                this.mRestoreWhichChild = -1;
            }
            return false;
        } else if (remoteViewsAdapter == null) {
            return false;
        } else {
            remoteViewsAdapter.superNotifyDataSetChanged();
            return true;
        }
    }

    @Override // android.widget.RemoteViewsAdapter.RemoteAdapterConnectionCallback
    public void onRemoteAdapterDisconnected() {
    }

    @Override // android.widget.Advanceable
    public void advance() {
        showNext();
    }

    @Override // android.widget.Advanceable
    public void fyiWillBeAdvancedByHostKThx() {
    }

    @Override // android.widget.AdapterView, android.view.ViewGroup, android.view.View
    public CharSequence getAccessibilityClassName() {
        return AdapterViewAnimator.class.getName();
    }
}
