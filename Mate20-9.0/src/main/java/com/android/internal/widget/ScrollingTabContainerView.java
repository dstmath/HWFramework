package com.android.internal.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.ActionBar;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.android.internal.view.ActionBarPolicy;

public class ScrollingTabContainerView extends HorizontalScrollView implements AdapterView.OnItemClickListener {
    private static final int FADE_DURATION = 200;
    private static final String TAG = "ScrollingTabContainerView";
    private static final TimeInterpolator sAlphaInterpolator = new DecelerateInterpolator();
    private boolean mAllowCollapse;
    private int mContentHeight;
    protected int mLastPos = 0;
    int mMaxTabWidth;
    private int mSelectedTabIndex;
    protected boolean mShouldAnimToTab = true;
    int mStackedTabMaxWidth;
    private TabClickListener mTabClickListener;
    protected boolean mTabClicked = false;
    /* access modifiers changed from: private */
    public LinearLayout mTabLayout;
    Runnable mTabSelector;
    private Spinner mTabSpinner;
    protected final VisibilityAnimListener mVisAnimListener = new VisibilityAnimListener();
    protected Animator mVisibilityAnim;

    private class TabAdapter extends BaseAdapter {
        private Context mDropDownContext;

        public TabAdapter(Context context) {
            setDropDownViewContext(context);
        }

        public void setDropDownViewContext(Context context) {
            this.mDropDownContext = context;
        }

        public int getCount() {
            return ScrollingTabContainerView.this.mTabLayout.getChildCount();
        }

        public Object getItem(int position) {
            return ((TabView) ScrollingTabContainerView.this.mTabLayout.getChildAt(position)).getTab();
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                return ScrollingTabContainerView.this.createTabView(ScrollingTabContainerView.this.mContext, (ActionBar.Tab) getItem(position), true);
            }
            ((TabView) convertView).bindTab((ActionBar.Tab) getItem(position));
            return convertView;
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                return ScrollingTabContainerView.this.createTabView(this.mDropDownContext, (ActionBar.Tab) getItem(position), true);
            }
            ((TabView) convertView).bindTab((ActionBar.Tab) getItem(position));
            return convertView;
        }
    }

    private class TabClickListener implements View.OnClickListener {
        private TabClickListener() {
        }

        public void onClick(View view) {
            ((TabView) view).getTab().select();
            int tabCount = ScrollingTabContainerView.this.mTabLayout.getChildCount();
            for (int i = 0; i < tabCount; i++) {
                View child = ScrollingTabContainerView.this.mTabLayout.getChildAt(i);
                child.setSelected(child == view);
            }
        }
    }

    public class TabView extends LinearLayout {
        private View mCustomView;
        private ImageView mIconView;
        private ActionBar.Tab mTab;
        public TextView mTextView;

        public TabView(Context context, ActionBar.Tab tab, boolean forList) {
            super(context, null, 16843507);
            this.mTab = tab;
            if (forList) {
                setGravity(8388627);
            }
            update();
        }

        public void bindTab(ActionBar.Tab tab) {
            this.mTab = tab;
            update();
        }

        public void setSelected(boolean selected) {
            boolean changed = isSelected() != selected;
            super.setSelected(selected);
            if (changed && selected) {
                sendAccessibilityEvent(4);
            }
        }

        public CharSequence getAccessibilityClassName() {
            return ActionBar.Tab.class.getName();
        }

        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (ScrollingTabContainerView.this.mMaxTabWidth > 0 && getMeasuredWidth() > ScrollingTabContainerView.this.mMaxTabWidth) {
                super.onMeasure(View.MeasureSpec.makeMeasureSpec(ScrollingTabContainerView.this.mMaxTabWidth, 1073741824), heightMeasureSpec);
            }
        }

        public void update() {
            ActionBar.Tab tab = this.mTab;
            View custom = tab.getCustomView();
            CharSequence charSequence = null;
            if (custom != null) {
                ViewParent customParent = custom.getParent();
                if (customParent != this) {
                    if (customParent != null) {
                        ((ViewGroup) customParent).removeView(custom);
                    }
                    addView(custom);
                }
                this.mCustomView = custom;
                if (this.mTextView != null) {
                    this.mTextView.setVisibility(8);
                }
                if (this.mIconView != null) {
                    this.mIconView.setVisibility(8);
                    this.mIconView.setImageDrawable(null);
                    return;
                }
                return;
            }
            if (this.mCustomView != null) {
                removeView(this.mCustomView);
                this.mCustomView = null;
            }
            Drawable icon = tab.getIcon();
            CharSequence text = tab.getText();
            if (icon != null) {
                if (this.mIconView == null) {
                    ImageView iconView = new ImageView(getContext());
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
                    lp.gravity = 16;
                    iconView.setLayoutParams(lp);
                    addView(iconView, 0);
                    this.mIconView = iconView;
                }
                this.mIconView.setImageDrawable(icon);
                this.mIconView.setVisibility(0);
            } else if (this.mIconView != null) {
                this.mIconView.setVisibility(8);
                this.mIconView.setImageDrawable(null);
            }
            boolean hasText = !TextUtils.isEmpty(text);
            if (hasText) {
                if (this.mTextView == null) {
                    TextView textView = new TextView(getContext(), null, 16843509);
                    textView.setEllipsize(TextUtils.TruncateAt.END);
                    LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(-2, -2);
                    lp2.gravity = 16;
                    textView.setLayoutParams(lp2);
                    addView(textView);
                    this.mTextView = textView;
                }
                this.mTextView.setText(text);
                this.mTextView.setVisibility(0);
            } else if (this.mTextView != null) {
                this.mTextView.setVisibility(8);
                this.mTextView.setText(null);
            }
            if (this.mIconView != null) {
                this.mIconView.setContentDescription(tab.getContentDescription());
            }
            if (!hasText) {
                charSequence = tab.getContentDescription();
            }
            setTooltipText(charSequence);
        }

        public ActionBar.Tab getTab() {
            return this.mTab;
        }

        @Deprecated
        public TextView getTextView() {
            return this.mTextView;
        }

        @Deprecated
        public ImageView getIconView() {
            return this.mIconView;
        }

        @Deprecated
        public View getCustomView() {
            return this.mCustomView;
        }

        /* access modifiers changed from: protected */
        @Deprecated
        public void insertOrAppendView(View view, boolean insertPreferentially) {
        }
    }

    protected class VisibilityAnimListener implements Animator.AnimatorListener {
        private boolean mCanceled = false;
        private int mFinalVisibility;

        protected VisibilityAnimListener() {
        }

        public VisibilityAnimListener withFinalVisibility(int visibility) {
            this.mFinalVisibility = visibility;
            return this;
        }

        public void onAnimationStart(Animator animation) {
            ScrollingTabContainerView.this.setVisibility(0);
            ScrollingTabContainerView.this.mVisibilityAnim = animation;
            this.mCanceled = false;
        }

        public void onAnimationEnd(Animator animation) {
            if (!this.mCanceled) {
                ScrollingTabContainerView.this.mVisibilityAnim = null;
                ScrollingTabContainerView.this.setVisibility(this.mFinalVisibility);
            }
        }

        public void onAnimationCancel(Animator animation) {
            this.mCanceled = true;
        }

        public void onAnimationRepeat(Animator animation) {
        }
    }

    public ScrollingTabContainerView(Context context) {
        super(context);
        setHorizontalScrollBarEnabled(false);
        ActionBarPolicy abp = ActionBarPolicy.get(context);
        setContentHeight(abp.getTabContainerHeight());
        this.mStackedTabMaxWidth = abp.getStackedTabMaxWidth();
        this.mTabLayout = createTabLayout();
        addView(this.mTabLayout, new ViewGroup.LayoutParams(-2, -1));
    }

    @Deprecated
    public void setShouldAnimToTab(boolean shouldAnim) {
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        boolean canCollapse = true;
        boolean lockedExpanded = widthMode == 1073741824;
        setFillViewport(lockedExpanded);
        int childCount = this.mTabLayout.getChildCount();
        if (childCount <= 1 || !(widthMode == 1073741824 || widthMode == Integer.MIN_VALUE)) {
            this.mMaxTabWidth = -1;
        } else {
            if (childCount > 2) {
                this.mMaxTabWidth = (int) (((float) View.MeasureSpec.getSize(widthMeasureSpec)) * 0.4f);
            } else {
                this.mMaxTabWidth = View.MeasureSpec.getSize(widthMeasureSpec) / 2;
            }
            this.mMaxTabWidth = Math.min(this.mMaxTabWidth, this.mStackedTabMaxWidth);
        }
        int heightMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mContentHeight, 1073741824);
        if (lockedExpanded || !this.mAllowCollapse) {
            canCollapse = false;
        }
        if (canCollapse) {
            this.mTabLayout.measure(0, heightMeasureSpec2);
            if (this.mTabLayout.getMeasuredWidth() > View.MeasureSpec.getSize(widthMeasureSpec)) {
                performCollapse();
            } else {
                performExpand();
            }
        } else {
            performExpand();
        }
        int oldWidth = getMeasuredWidth();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec2);
        int newWidth = getMeasuredWidth();
        if (lockedExpanded && oldWidth != newWidth) {
            setTabSelected(this.mSelectedTabIndex);
        }
    }

    private boolean isCollapsed() {
        return this.mTabSpinner != null && this.mTabSpinner.getParent() == this;
    }

    public void setAllowCollapse(boolean allowCollapse) {
        this.mAllowCollapse = allowCollapse;
    }

    private void performCollapse() {
        if (!isCollapsed()) {
            if (this.mTabSpinner == null) {
                this.mTabSpinner = createSpinner();
            }
            removeView(this.mTabLayout);
            addView(this.mTabSpinner, new ViewGroup.LayoutParams(-2, -1));
            if (this.mTabSpinner.getAdapter() == null) {
                TabAdapter adapter = new TabAdapter(this.mContext);
                adapter.setDropDownViewContext(this.mTabSpinner.getPopupContext());
                this.mTabSpinner.setAdapter(adapter);
            }
            if (this.mTabSelector != null) {
                removeCallbacks(this.mTabSelector);
                this.mTabSelector = null;
            }
            this.mTabSpinner.setSelection(this.mSelectedTabIndex);
        }
    }

    private boolean performExpand() {
        if (!isCollapsed()) {
            return false;
        }
        removeView(this.mTabSpinner);
        addView(this.mTabLayout, new ViewGroup.LayoutParams(-2, -1));
        setTabSelected(this.mTabSpinner.getSelectedItemPosition());
        return false;
    }

    public void setTabSelected(int position) {
        this.mSelectedTabIndex = position;
        int tabCount = this.mTabLayout.getChildCount();
        int i = 0;
        while (i < tabCount) {
            View child = this.mTabLayout.getChildAt(i);
            boolean isSelected = i == position;
            child.setSelected(isSelected);
            if (isSelected) {
                animateToTab(position);
            }
            i++;
        }
        if (this.mTabSpinner != null && position >= 0) {
            this.mTabSpinner.setSelection(position);
        }
    }

    public void setContentHeight(int contentHeight) {
        this.mContentHeight = contentHeight;
        requestLayout();
    }

    private LinearLayout createTabLayout() {
        LinearLayout tabLayout = new LinearLayout(getContext(), null, 16843508);
        tabLayout.setMeasureWithLargestChildEnabled(true);
        tabLayout.setGravity(17);
        tabLayout.setLayoutParams(new LinearLayout.LayoutParams(-2, -1));
        return tabLayout;
    }

    private Spinner createSpinner() {
        Spinner spinner = new Spinner(getContext(), null, 16843479);
        spinner.setLayoutParams(new LinearLayout.LayoutParams(-2, -1));
        spinner.setOnItemClickListenerInt(this);
        return spinner;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ActionBarPolicy abp = ActionBarPolicy.get(getContext());
        setContentHeight(abp.getTabContainerHeight());
        this.mStackedTabMaxWidth = abp.getStackedTabMaxWidth();
    }

    public void animateToVisibility(int visibility) {
        if (this.mVisibilityAnim != null) {
            this.mVisibilityAnim.cancel();
        }
        if (visibility == 0) {
            if (getVisibility() != 0) {
                setAlpha(0.0f);
            }
            ObjectAnimator anim = ObjectAnimator.ofFloat(this, "alpha", new float[]{1.0f});
            anim.setDuration(200);
            anim.setInterpolator(sAlphaInterpolator);
            anim.addListener(this.mVisAnimListener.withFinalVisibility(visibility));
            anim.start();
            return;
        }
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(this, "alpha", new float[]{0.0f});
        anim2.setDuration(200);
        anim2.setInterpolator(sAlphaInterpolator);
        anim2.addListener(this.mVisAnimListener.withFinalVisibility(visibility));
        anim2.start();
    }

    public void animateToTab(int position) {
        final View tabView = this.mTabLayout.getChildAt(position);
        if (this.mTabSelector != null) {
            removeCallbacks(this.mTabSelector);
        }
        this.mTabSelector = new Runnable() {
            public void run() {
                ScrollingTabContainerView.this.smoothScrollTo(tabView.getLeft() - ((ScrollingTabContainerView.this.getWidth() - tabView.getWidth()) / 2), 0);
                ScrollingTabContainerView.this.mTabSelector = null;
            }
        };
        post(this.mTabSelector);
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mTabSelector != null) {
            post(this.mTabSelector);
        }
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mTabSelector != null) {
            removeCallbacks(this.mTabSelector);
        }
    }

    /* access modifiers changed from: private */
    public TabView createTabView(Context context, ActionBar.Tab tab, boolean forAdapter) {
        TabView tabView = new TabView(context, tab, forAdapter);
        if (forAdapter) {
            tabView.setBackgroundDrawable(null);
            tabView.setLayoutParams(new AbsListView.LayoutParams(-1, this.mContentHeight));
        } else {
            tabView.setFocusable(true);
            if (this.mTabClickListener == null) {
                this.mTabClickListener = new TabClickListener();
            }
            tabView.setOnClickListener(this.mTabClickListener);
        }
        return tabView;
    }

    public void addTab(ActionBar.Tab tab, boolean setSelected) {
        TabView tabView = createTabView(this.mContext, tab, false);
        this.mTabLayout.addView(tabView, new LinearLayout.LayoutParams(0, -1, 1.0f));
        if (this.mTabSpinner != null) {
            ((TabAdapter) this.mTabSpinner.getAdapter()).notifyDataSetChanged();
        }
        if (setSelected) {
            tabView.setSelected(true);
        }
        if (this.mAllowCollapse) {
            requestLayout();
        }
    }

    public void addTab(ActionBar.Tab tab, int position, boolean setSelected) {
        TabView tabView = createTabView(this.mContext, tab, false);
        this.mTabLayout.addView(tabView, position, new LinearLayout.LayoutParams(0, -1, 1.0f));
        if (this.mTabSpinner != null) {
            ((TabAdapter) this.mTabSpinner.getAdapter()).notifyDataSetChanged();
        }
        if (setSelected) {
            tabView.setSelected(true);
        }
        if (this.mAllowCollapse) {
            requestLayout();
        }
    }

    public void updateTab(int position) {
        ((TabView) this.mTabLayout.getChildAt(position)).update();
        if (this.mTabSpinner != null) {
            ((TabAdapter) this.mTabSpinner.getAdapter()).notifyDataSetChanged();
        }
        if (this.mAllowCollapse) {
            requestLayout();
        }
    }

    public void removeTabAt(int position) {
        this.mTabLayout.removeViewAt(position);
        if (this.mTabSpinner != null) {
            ((TabAdapter) this.mTabSpinner.getAdapter()).notifyDataSetChanged();
        }
        if (this.mAllowCollapse) {
            requestLayout();
        }
    }

    public void removeAllTabs() {
        this.mTabLayout.removeAllViews();
        if (this.mTabSpinner != null) {
            ((TabAdapter) this.mTabSpinner.getAdapter()).notifyDataSetChanged();
        }
        if (this.mAllowCollapse) {
            requestLayout();
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        ((TabView) view).getTab().select();
    }

    @Deprecated
    public boolean isTabClicked() {
        return false;
    }

    @Deprecated
    public int getLastPos() {
        return 0;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public LinearLayout getTabLayout() {
        return this.mTabLayout;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int getSelectedTabIndex() {
        return this.mSelectedTabIndex;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void setMaxTabWidth(int width) {
        this.mMaxTabWidth = width;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void handleTabClicked(int position) {
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public boolean disableMaxTabWidth() {
        return false;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void handleTabViewCreated(TabView view) {
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void initTitleAppearance(TextView textView) {
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int adjustPadding(int availableWidth, int itemPaddingSize) {
        return itemPaddingSize;
    }

    @Deprecated
    public void updateTabViewContainerWidth(Context context) {
    }
}
