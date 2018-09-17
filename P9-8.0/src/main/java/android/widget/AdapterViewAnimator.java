package android.widget;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.FilterComparison;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RemoteViews.OnClickHandler;
import android.widget.RemoteViewsAdapter.AsyncRemoteAdapterAction;
import android.widget.RemoteViewsAdapter.RemoteAdapterConnectionCallback;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class AdapterViewAnimator extends AdapterView<Adapter> implements RemoteAdapterConnectionCallback, Advanceable {
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
    AdapterDataSetObserver mDataSetObserver;
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

    final class CheckForTap implements Runnable {
        CheckForTap() {
        }

        public void run() {
            if (AdapterViewAnimator.this.mTouchMode == 1) {
                AdapterViewAnimator.this.showTapFeedback(AdapterViewAnimator.this.getCurrentView());
            }
        }
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int whichChild;

        SavedState(Parcelable superState, int whichChild) {
            super(superState);
            this.whichChild = whichChild;
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

        ViewAndMetaData(View view, int relativeIndex, int adapterPosition, long itemId) {
            this.view = view;
            this.relativeIndex = relativeIndex;
            this.adapterPosition = adapterPosition;
            this.itemId = itemId;
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
        this.mViewsMap = new HashMap();
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
        resource = a.getResourceId(1, 0);
        if (resource > 0) {
            setOutAnimation(context, resource);
        } else {
            setOutAnimation(getDefaultOutAnimation());
        }
        setAnimateFirstView(a.getBoolean(2, true));
        this.mLoopViews = a.getBoolean(3, false);
        a.recycle();
        initViewAnimator();
    }

    private void initViewAnimator() {
        this.mPreviousViews = new ArrayList();
    }

    void configureViewAnimator(int numVisibleViews, int activeOffset) {
        int i = numVisibleViews - 1;
        this.mMaxNumActiveViews = numVisibleViews;
        this.mActiveOffset = activeOffset;
        this.mPreviousViews.clear();
        this.mViewsMap.clear();
        removeAllViewsInLayout();
        this.mCurrentWindowStart = 0;
        this.mCurrentWindowEnd = -1;
    }

    void transformViewForTransition(int fromIndex, int toIndex, View view, boolean animate) {
        if (fromIndex == -1) {
            this.mInAnimation.setTarget(view);
            this.mInAnimation.start();
        } else if (toIndex == -1) {
            this.mOutAnimation.setTarget(view);
            this.mOutAnimation.start();
        }
    }

    ObjectAnimator getDefaultInAnimation() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(null, "alpha", new float[]{0.0f, 1.0f});
        anim.setDuration(200);
        return anim;
    }

    ObjectAnimator getDefaultOutAnimation() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(null, "alpha", new float[]{1.0f, 0.0f});
        anim.setDuration(200);
        return anim;
    }

    @RemotableViewMethod
    public void setDisplayedChild(int whichChild) {
        setDisplayedChild(whichChild, true);
    }

    private void setDisplayedChild(int whichChild, boolean animate) {
        int i = 0;
        if (this.mAdapter != null) {
            this.mWhichChild = whichChild;
            if (whichChild >= getWindowSize()) {
                if (!this.mLoopViews) {
                    i = getWindowSize() - 1;
                }
                this.mWhichChild = i;
            } else if (whichChild < 0) {
                if (this.mLoopViews) {
                    i = getWindowSize() - 1;
                }
                this.mWhichChild = i;
            }
            boolean hasFocus = getFocusedChild() != null;
            showOnly(this.mWhichChild, animate);
            if (hasFocus) {
                requestFocus(2);
            }
        }
    }

    void applyTransformForChildAtIndex(View child, int relativeIndex) {
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

    int modulo(int pos, int size) {
        if (size > 0) {
            return ((pos % size) + size) % size;
        }
        return 0;
    }

    View getViewAtRelativeIndex(int relativeIndex) {
        if (relativeIndex >= 0 && relativeIndex <= getNumActiveViews() - 1 && this.mAdapter != null) {
            int i = modulo(this.mCurrentWindowStartUnbounded + relativeIndex, getWindowSize());
            if (this.mViewsMap.get(Integer.valueOf(i)) != null) {
                return ((ViewAndMetaData) this.mViewsMap.get(Integer.valueOf(i))).view;
            }
        }
        return null;
    }

    int getNumActiveViews() {
        if (this.mAdapter != null) {
            return Math.min(getCount() + 1, this.mMaxNumActiveViews);
        }
        return this.mMaxNumActiveViews;
    }

    int getWindowSize() {
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

    LayoutParams createOrReuseLayoutParams(View v) {
        LayoutParams currentLp = v.getLayoutParams();
        if (currentLp != null) {
            return currentLp;
        }
        return new LayoutParams(0, 0);
    }

    void refreshChildren() {
        if (this.mAdapter != null) {
            for (int i = this.mCurrentWindowStart; i <= this.mCurrentWindowEnd; i++) {
                int index = modulo(i, getWindowSize());
                View updatedChild = this.mAdapter.getView(modulo(i, getCount()), null, this);
                if (updatedChild.getImportantForAccessibility() == 0) {
                    updatedChild.setImportantForAccessibility(1);
                }
                if (this.mViewsMap.containsKey(Integer.valueOf(index))) {
                    FrameLayout fl = ((ViewAndMetaData) this.mViewsMap.get(Integer.valueOf(index))).view;
                    if (updatedChild != null) {
                        fl.removeAllViewsInLayout();
                        fl.addView(updatedChild);
                    }
                }
            }
        }
    }

    FrameLayout getFrameForChild() {
        return new FrameLayout(this.mContext);
    }

    void showOnly(int childIndex, boolean animate) {
        if (this.mAdapter != null) {
            int adapterCount = getCount();
            if (adapterCount != 0) {
                int i;
                int oldRelativeIndex;
                for (i = 0; i < this.mPreviousViews.size(); i++) {
                    View viewToRemove = ((ViewAndMetaData) this.mViewsMap.get(this.mPreviousViews.get(i))).view;
                    this.mViewsMap.remove(this.mPreviousViews.get(i));
                    viewToRemove.clearAnimation();
                    if (viewToRemove instanceof ViewGroup) {
                        ((ViewGroup) viewToRemove).removeAllViewsInLayout();
                    }
                    applyTransformForChildAtIndex(viewToRemove, -1);
                    removeViewInLayout(viewToRemove);
                }
                this.mPreviousViews.clear();
                int newWindowStartUnbounded = childIndex - this.mActiveOffset;
                int newWindowEndUnbounded = (getNumActiveViews() + newWindowStartUnbounded) - 1;
                int newWindowStart = Math.max(0, newWindowStartUnbounded);
                int newWindowEnd = Math.min(adapterCount - 1, newWindowEndUnbounded);
                if (this.mLoopViews) {
                    newWindowStart = newWindowStartUnbounded;
                    newWindowEnd = newWindowEndUnbounded;
                }
                int rangeStart = modulo(newWindowStart, getWindowSize());
                int rangeEnd = modulo(newWindowEnd, getWindowSize());
                boolean wrap = false;
                if (rangeStart > rangeEnd) {
                    wrap = true;
                }
                for (Integer index : this.mViewsMap.keySet()) {
                    boolean remove = false;
                    if (!wrap && (index.intValue() < rangeStart || index.intValue() > rangeEnd)) {
                        remove = true;
                    } else if (wrap && index.intValue() > rangeEnd && index.intValue() < rangeStart) {
                        remove = true;
                    }
                    if (remove) {
                        View previousView = ((ViewAndMetaData) this.mViewsMap.get(index)).view;
                        oldRelativeIndex = ((ViewAndMetaData) this.mViewsMap.get(index)).relativeIndex;
                        this.mPreviousViews.add(index);
                        transformViewForTransition(oldRelativeIndex, -1, previousView, animate);
                    }
                }
                if (!(newWindowStart == this.mCurrentWindowStart && newWindowEnd == this.mCurrentWindowEnd && newWindowStartUnbounded == this.mCurrentWindowStartUnbounded)) {
                    for (i = newWindowStart; i <= newWindowEnd; i++) {
                        int index2 = modulo(i, getWindowSize());
                        if (this.mViewsMap.containsKey(Integer.valueOf(index2))) {
                            oldRelativeIndex = ((ViewAndMetaData) this.mViewsMap.get(Integer.valueOf(index2))).relativeIndex;
                        } else {
                            oldRelativeIndex = -1;
                        }
                        int newRelativeIndex = i - newWindowStartUnbounded;
                        if ((this.mViewsMap.containsKey(Integer.valueOf(index2)) ? this.mPreviousViews.contains(Integer.valueOf(index2)) ^ 1 : 0) != 0) {
                            View view = ((ViewAndMetaData) this.mViewsMap.get(Integer.valueOf(index2))).view;
                            ((ViewAndMetaData) this.mViewsMap.get(Integer.valueOf(index2))).relativeIndex = newRelativeIndex;
                            applyTransformForChildAtIndex(view, newRelativeIndex);
                            transformViewForTransition(oldRelativeIndex, newRelativeIndex, view, animate);
                        } else {
                            int adapterPosition = modulo(i, adapterCount);
                            View newView = this.mAdapter.getView(adapterPosition, null, this);
                            long itemId = this.mAdapter.getItemId(adapterPosition);
                            FrameLayout fl = getFrameForChild();
                            if (newView != null) {
                                fl.addView(newView);
                            }
                            this.mViewsMap.put(Integer.valueOf(index2), new ViewAndMetaData(fl, newRelativeIndex, adapterPosition, itemId));
                            addChild(fl);
                            applyTransformForChildAtIndex(fl, newRelativeIndex);
                            transformViewForTransition(-1, newRelativeIndex, fl, animate);
                        }
                        ((ViewAndMetaData) this.mViewsMap.get(Integer.valueOf(index2))).view.bringToFront();
                    }
                    this.mCurrentWindowStart = newWindowStart;
                    this.mCurrentWindowEnd = newWindowEnd;
                    this.mCurrentWindowStartUnbounded = newWindowStartUnbounded;
                    if (this.mRemoteViewsAdapter != null) {
                        this.mRemoteViewsAdapter.setVisibleRangeHint(modulo(this.mCurrentWindowStart, adapterCount), modulo(this.mCurrentWindowEnd, adapterCount));
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
            int measureSpec = MeasureSpec.makeMeasureSpec(0, 0);
            child.measure(measureSpec, measureSpec);
            this.mReferenceChildWidth = child.getMeasuredWidth();
            this.mReferenceChildHeight = child.getMeasuredHeight();
        }
    }

    void showTapFeedback(View v) {
        v.setPressed(true);
    }

    void hideTapFeedback(View v) {
        v.setPressed(false);
    }

    void cancelHandleClick() {
        View v = getCurrentView();
        if (v != null) {
            hideTapFeedback(v);
        }
        this.mTouchMode = 0;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean handled = false;
        View v;
        switch (ev.getAction()) {
            case 0:
                v = getCurrentView();
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
                    v = getCurrentView();
                    final ViewAndMetaData viewData = getMetaDataForChild(v);
                    if (v != null && isTransformedTouchPointInView(ev.getX(), ev.getY(), v, null)) {
                        Handler handler = getHandler();
                        if (handler != null) {
                            handler.removeCallbacks(this.mPendingCheckForTap);
                        }
                        showTapFeedback(v);
                        postDelayed(new Runnable() {
                            public void run() {
                                AdapterViewAnimator.this.hideTapFeedback(v);
                                AdapterViewAnimator adapterViewAnimator = AdapterViewAnimator.this;
                                final ViewAndMetaData viewAndMetaData = viewData;
                                final View view = v;
                                adapterViewAnimator.post(new Runnable() {
                                    public void run() {
                                        if (viewAndMetaData != null) {
                                            AdapterViewAnimator.this.performItemClick(view, viewAndMetaData.adapterPosition, viewAndMetaData.itemId);
                                        } else {
                                            AdapterViewAnimator.this.performItemClick(view, 0, 0);
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
                v = getCurrentView();
                if (v != null) {
                    hideTapFeedback(v);
                }
                this.mTouchMode = 0;
                break;
        }
        return handled;
    }

    private void measureChildren() {
        int count = getChildCount();
        int childWidth = (getMeasuredWidth() - this.mPaddingLeft) - this.mPaddingRight;
        int childHeight = (getMeasuredHeight() - this.mPaddingTop) - this.mPaddingBottom;
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(MeasureSpec.makeMeasureSpec(childWidth, 1073741824), MeasureSpec.makeMeasureSpec(childHeight, 1073741824));
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        boolean haveChildRefSize = (this.mReferenceChildWidth == -1 || this.mReferenceChildHeight == -1) ? false : true;
        if (heightSpecMode == 0) {
            heightSpecSize = haveChildRefSize ? (this.mReferenceChildHeight + this.mPaddingTop) + this.mPaddingBottom : 0;
        } else if (heightSpecMode == Integer.MIN_VALUE && haveChildRefSize) {
            int height = (this.mReferenceChildHeight + this.mPaddingTop) + this.mPaddingBottom;
            heightSpecSize = height > heightSpecSize ? heightSpecSize | 16777216 : height;
        }
        if (widthSpecMode == 0) {
            widthSpecSize = haveChildRefSize ? (this.mReferenceChildWidth + this.mPaddingLeft) + this.mPaddingRight : 0;
        } else if (heightSpecMode == Integer.MIN_VALUE && haveChildRefSize) {
            int width = (this.mReferenceChildWidth + this.mPaddingLeft) + this.mPaddingRight;
            widthSpecSize = width > widthSpecSize ? widthSpecSize | 16777216 : width;
        }
        -wrap6(widthSpecSize, heightSpecSize);
        measureChildren();
    }

    void checkForAndHandleDataChanged() {
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

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        checkForAndHandleDataChanged();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.layout(this.mPaddingLeft, this.mPaddingTop, this.mPaddingLeft + child.getMeasuredWidth(), this.mPaddingTop + child.getMeasuredHeight());
        }
    }

    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.-wrap0();
        if (this.mRemoteViewsAdapter != null) {
            this.mRemoteViewsAdapter.saveRemoteViewsCache();
        }
        return new SavedState(superState, this.mWhichChild);
    }

    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.-wrap2(ss.getSuperState());
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
            this.mDataSetObserver = new AdapterDataSetObserver();
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
        return new AsyncRemoteAdapterAction(this, intent);
    }

    public void setRemoteViewsAdapter(Intent intent, boolean isAsync) {
        if (this.mRemoteViewsAdapter == null || !new FilterComparison(intent).equals(new FilterComparison(this.mRemoteViewsAdapter.getRemoteViewsServiceIntent()))) {
            this.mDeferNotifyDataSetChanged = false;
            this.mRemoteViewsAdapter = new RemoteViewsAdapter(getContext(), intent, this, isAsync);
            if (this.mRemoteViewsAdapter.isDataReady()) {
                setAdapter(this.mRemoteViewsAdapter);
            }
        }
    }

    public void setRemoteViewsOnClickHandler(OnClickHandler handler) {
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
