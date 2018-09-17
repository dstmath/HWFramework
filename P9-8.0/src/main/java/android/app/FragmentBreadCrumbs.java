package android.app;

import android.animation.LayoutTransition;
import android.app.FragmentManager.BackStackEntry;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.R;

@Deprecated
public class FragmentBreadCrumbs extends ViewGroup implements OnBackStackChangedListener {
    private static final int DEFAULT_GRAVITY = 8388627;
    private boolean bArabic;
    Activity mActivity;
    LinearLayout mContainer;
    private int mGravity;
    LayoutInflater mInflater;
    private int mLayoutResId;
    int mMaxVisible;
    private OnBreadCrumbClickListener mOnBreadCrumbClickListener;
    private OnClickListener mOnClickListener;
    private OnClickListener mParentClickListener;
    BackStackRecord mParentEntry;
    private int mTextColor;
    BackStackRecord mTopEntry;

    public interface OnBreadCrumbClickListener {
        boolean onBreadCrumbClick(BackStackEntry backStackEntry, int i);
    }

    public FragmentBreadCrumbs(Context context) {
        this(context, null);
    }

    public FragmentBreadCrumbs(Context context, AttributeSet attrs) {
        this(context, attrs, 17891386);
    }

    public FragmentBreadCrumbs(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FragmentBreadCrumbs(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mMaxVisible = -1;
        this.bArabic = isRtlLocale();
        this.mOnClickListener = new OnClickListener() {
            public void onClick(View v) {
                BackStackEntry backStackEntry = null;
                if (v.getTag() instanceof BackStackEntry) {
                    BackStackEntry bse = (BackStackEntry) v.getTag();
                    if (bse != FragmentBreadCrumbs.this.mParentEntry) {
                        if (FragmentBreadCrumbs.this.mOnBreadCrumbClickListener != null) {
                            OnBreadCrumbClickListener -get0 = FragmentBreadCrumbs.this.mOnBreadCrumbClickListener;
                            if (bse != FragmentBreadCrumbs.this.mTopEntry) {
                                backStackEntry = bse;
                            }
                            if (-get0.onBreadCrumbClick(backStackEntry, 0)) {
                                return;
                            }
                        }
                        if (bse == FragmentBreadCrumbs.this.mTopEntry) {
                            FragmentBreadCrumbs.this.mActivity.getFragmentManager().popBackStack();
                        } else {
                            FragmentBreadCrumbs.this.mActivity.getFragmentManager().popBackStack(bse.getId(), 0);
                        }
                    } else if (FragmentBreadCrumbs.this.mParentClickListener != null) {
                        FragmentBreadCrumbs.this.mParentClickListener.onClick(v);
                    }
                }
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FragmentBreadCrumbs, defStyleAttr, defStyleRes);
        this.mGravity = a.getInt(0, DEFAULT_GRAVITY);
        this.mLayoutResId = a.getResourceId(2, 17367141);
        this.mTextColor = a.getColor(1, 0);
        a.recycle();
    }

    public void setActivity(Activity a) {
        this.mActivity = a;
        this.mInflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContainer = (LinearLayout) this.mInflater.inflate(17367143, this, false);
        addView(this.mContainer);
        a.getFragmentManager().addOnBackStackChangedListener(this);
        updateCrumbs();
        setLayoutTransition(new LayoutTransition());
    }

    public void setMaxVisible(int visibleCrumbs) {
        if (visibleCrumbs < 1) {
            throw new IllegalArgumentException("visibleCrumbs must be greater than zero");
        }
        this.mMaxVisible = visibleCrumbs;
    }

    public void setParentTitle(CharSequence title, CharSequence shortTitle, OnClickListener listener) {
        this.mParentEntry = createBackStackEntry(title, shortTitle);
        this.mParentClickListener = listener;
        updateCrumbs();
    }

    public void setOnBreadCrumbClickListener(OnBreadCrumbClickListener listener) {
        this.mOnBreadCrumbClickListener = listener;
    }

    private BackStackRecord createBackStackEntry(CharSequence title, CharSequence shortTitle) {
        if (title == null) {
            return null;
        }
        BackStackRecord entry = new BackStackRecord((FragmentManagerImpl) this.mActivity.getFragmentManager());
        entry.setBreadCrumbTitle(title);
        entry.setBreadCrumbShortTitle(shortTitle);
        return entry;
    }

    public void setTitle(CharSequence title, CharSequence shortTitle) {
        this.mTopEntry = createBackStackEntry(title, shortTitle);
        updateCrumbs();
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() != 0) {
            int childLeft;
            int childRight;
            View child = getChildAt(0);
            int childTop = this.mPaddingTop;
            int childBottom = (this.mPaddingTop + child.getMeasuredHeight()) - this.mPaddingBottom;
            switch (Gravity.getAbsoluteGravity(this.mGravity & 8388615, getLayoutDirection())) {
                case 1:
                    childLeft = this.mPaddingLeft + (((this.mRight - this.mLeft) - child.getMeasuredWidth()) / 2);
                    childRight = childLeft + child.getMeasuredWidth();
                    break;
                case 5:
                    childRight = (this.mRight - this.mLeft) - this.mPaddingRight;
                    childLeft = childRight - child.getMeasuredWidth();
                    break;
                default:
                    childLeft = this.mPaddingLeft;
                    childRight = childLeft + child.getMeasuredWidth();
                    break;
            }
            if (childLeft < this.mPaddingLeft) {
                childLeft = this.mPaddingLeft;
            }
            if (childRight > (this.mRight - this.mLeft) - this.mPaddingRight) {
                childRight = (this.mRight - this.mLeft) - this.mPaddingRight;
            }
            child.layout(childLeft, childTop, childRight, childBottom);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int maxHeight = 0;
        int maxWidth = 0;
        int measuredChildState = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
                maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
                measuredChildState = combineMeasuredStates(measuredChildState, child.getMeasuredState());
            }
        }
        setMeasuredDimension(resolveSizeAndState(Math.max(maxWidth + (this.mPaddingLeft + this.mPaddingRight), getSuggestedMinimumWidth()), widthMeasureSpec, measuredChildState), resolveSizeAndState(Math.max(maxHeight + (this.mPaddingTop + this.mPaddingBottom), getSuggestedMinimumHeight()), heightMeasureSpec, measuredChildState << 16));
    }

    public void onBackStackChanged() {
        updateCrumbs();
    }

    private int getPreEntryCount() {
        int i = 1;
        int i2 = this.mTopEntry != null ? 1 : 0;
        if (this.mParentEntry == null) {
            i = 0;
        }
        return i2 + i;
    }

    private BackStackEntry getPreEntry(int index) {
        if (this.mParentEntry == null) {
            return this.mTopEntry;
        }
        return index == 0 ? this.mParentEntry : this.mTopEntry;
    }

    void updateCrumbs() {
        int i;
        FragmentManager fm = this.mActivity.getFragmentManager();
        int numEntries = fm.getBackStackEntryCount();
        int numPreEntries = getPreEntryCount();
        int numViews = this.mContainer.getChildCount();
        for (i = 0; i < numEntries + numPreEntries; i++) {
            BackStackEntry bse;
            if (i < numPreEntries) {
                bse = getPreEntry(i);
            } else {
                bse = fm.getBackStackEntryAt(i - numPreEntries);
            }
            if (i < numViews) {
                View v;
                if (this.bArabic) {
                    v = this.mContainer.getChildAt(0);
                } else {
                    v = this.mContainer.getChildAt(i);
                }
                if (v.getTag() != bse) {
                    for (int j = i; j < numViews; j++) {
                        if (this.bArabic) {
                            this.mContainer.removeViewAt(0);
                        } else {
                            this.mContainer.removeViewAt(i);
                        }
                    }
                    numViews = i;
                }
            }
            if (i >= numViews) {
                View item = this.mInflater.inflate(this.mLayoutResId, this, false);
                TextView text = (TextView) item.findViewById(android.R.id.title);
                text.setText(bse.getBreadCrumbTitle());
                text.setTag(bse);
                text.setTextColor(this.mTextColor);
                if (i == 0) {
                    item.findViewById(16909018).setVisibility(8);
                }
                if (this.bArabic) {
                    this.mContainer.addView(item, 0);
                } else {
                    this.mContainer.addView(item);
                }
                text.setOnClickListener(this.mOnClickListener);
            }
        }
        int viewI = numEntries + numPreEntries;
        numViews = this.mContainer.getChildCount();
        while (numViews > viewI) {
            this.mContainer.removeViewAt(numViews - 1);
            numViews--;
        }
        i = 0;
        while (i < numViews) {
            View child = this.mContainer.getChildAt(i);
            if (this.bArabic) {
                child.findViewById(android.R.id.title).setEnabled(i != 0);
            } else {
                child.findViewById(android.R.id.title).setEnabled(i < numViews + -1);
            }
            if (this.mMaxVisible > 0) {
                int i2;
                child.setVisibility(i < numViews - this.mMaxVisible ? 8 : 0);
                View leftIcon = child.findViewById(16909018);
                if (i <= numViews - this.mMaxVisible || i == 0) {
                    i2 = 8;
                } else {
                    i2 = 0;
                }
                leftIcon.setVisibility(i2);
            }
            i++;
        }
    }
}
