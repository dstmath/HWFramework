package com.android.internal.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.ActionBar.Tab;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.R;
import com.android.internal.view.ActionBarPolicy;

public class ScrollingTabContainerView extends HorizontalScrollView implements OnItemClickListener {
    private static final int FADE_DURATION = 200;
    private static final String TAG = "ScrollingTabContainerView";
    private static final TimeInterpolator sAlphaInterpolator = null;
    private boolean mAllowCollapse;
    private int mContentHeight;
    protected int mLastPos;
    int mMaxTabWidth;
    private int mSelectedTabIndex;
    protected boolean mShouldAnimToTab;
    int mStackedTabMaxWidth;
    private TabClickListener mTabClickListener;
    protected boolean mTabClicked;
    private LinearLayout mTabLayout;
    Runnable mTabSelector;
    private Spinner mTabSpinner;
    protected final VisibilityAnimListener mVisAnimListener;
    protected Animator mVisibilityAnim;

    /* renamed from: com.android.internal.widget.ScrollingTabContainerView.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ View val$tabView;

        AnonymousClass1(View val$tabView) {
            this.val$tabView = val$tabView;
        }

        public void run() {
            ScrollingTabContainerView.this.smoothScrollTo(this.val$tabView.getLeft() - ((ScrollingTabContainerView.this.getWidth() - this.val$tabView.getWidth()) / 2), 0);
            ScrollingTabContainerView.this.mTabSelector = null;
        }
    }

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
                return ScrollingTabContainerView.this.createTabView(ScrollingTabContainerView.this.mContext, (Tab) getItem(position), true);
            }
            ((TabView) convertView).bindTab((Tab) getItem(position));
            return convertView;
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                return ScrollingTabContainerView.this.createTabView(this.mDropDownContext, (Tab) getItem(position), true);
            }
            ((TabView) convertView).bindTab((Tab) getItem(position));
            return convertView;
        }
    }

    private class TabClickListener implements OnClickListener {
        private TabClickListener() {
        }

        public void onClick(View view) {
            ScrollingTabContainerView.this.mTabClicked = true;
            ((TabView) view).getTab().select();
            int tabCount = ScrollingTabContainerView.this.mTabLayout.getChildCount();
            for (int i = 0; i < tabCount; i++) {
                View child = ScrollingTabContainerView.this.mTabLayout.getChildAt(i);
                child.setSelected(child == view);
                if (child == view) {
                    ScrollingTabContainerView.this.handleTabClicked(i);
                }
            }
        }
    }

    public class TabView extends LinearLayout implements OnLongClickListener {
        private View mCustomView;
        private ImageView mIconView;
        private Tab mTab;
        public TextView mTextView;

        public TabView(Context context, Tab tab, boolean forList) {
            super(context, null, R.attr.actionBarTabStyle);
            this.mTab = tab;
            if (forList) {
                setGravity(8388627);
            }
            update();
        }

        public void bindTab(Tab tab) {
            this.mTab = tab;
            update();
        }

        public void setSelected(boolean selected) {
            boolean changed = isSelected() != selected;
            super.setSelected(selected);
            if (changed && selected) {
                sendAccessibilityEvent(4);
            }
            if (this.mTextView != null) {
                this.mTextView.setSelected(selected);
            }
        }

        public void setPressed(boolean pressed) {
            super.setPressed(pressed);
            if (this.mTextView != null) {
                this.mTextView.setPressed(pressed);
            }
        }

        public CharSequence getAccessibilityClassName() {
            return Tab.class.getName();
        }

        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (ScrollingTabContainerView.this.mMaxTabWidth > 0 && getMeasuredWidth() > ScrollingTabContainerView.this.mMaxTabWidth && !ScrollingTabContainerView.this.disableMaxTabWidth()) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(ScrollingTabContainerView.this.mMaxTabWidth, EditorInfo.IME_FLAG_NO_ENTER_ACTION), heightMeasureSpec);
            }
        }

        public void update() {
            Tab tab = this.mTab;
            View custom = tab.getCustomView();
            if (custom != null) {
                TabView customParent = custom.getParent();
                if (customParent != this) {
                    if (customParent != null) {
                        customParent.removeView(custom);
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
            boolean hasText;
            if (this.mCustomView != null) {
                removeView(this.mCustomView);
                this.mCustomView = null;
            }
            Drawable icon = tab.getIcon();
            CharSequence text = tab.getText();
            if (icon != null) {
                if (this.mIconView == null) {
                    ImageView iconView = new ImageView(getContext());
                    LayoutParams lp = new LayoutParams(-2, -2);
                    lp.gravity = 16;
                    iconView.setLayoutParams(lp);
                    insertOrAppendView(iconView, false);
                    this.mIconView = iconView;
                }
                this.mIconView.setImageDrawable(icon);
                this.mIconView.setVisibility(0);
            } else if (this.mIconView != null) {
                this.mIconView.setVisibility(8);
                this.mIconView.setImageDrawable(null);
            }
            if (TextUtils.isEmpty(text)) {
                hasText = false;
            } else {
                hasText = true;
            }
            if (hasText) {
                if (this.mTextView == null) {
                    TextView textView = new TextView(getContext(), null, R.attr.actionBarTabTextStyle);
                    textView.setEllipsize(TruncateAt.END);
                    lp = new LayoutParams(-2, -2);
                    lp.gravity = 16;
                    textView.setLayoutParams(lp);
                    textView.setSingleLine(true);
                    insertOrAppendView(textView, true);
                    this.mTextView = textView;
                    ScrollingTabContainerView.this.initTitleAppearance(this.mTextView);
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
            if (hasText || TextUtils.isEmpty(tab.getContentDescription())) {
                setOnLongClickListener(null);
                setLongClickable(false);
                return;
            }
            setOnLongClickListener(this);
        }

        public boolean onLongClick(View v) {
            int[] screenPos = new int[2];
            getLocationOnScreen(screenPos);
            Context context = getContext();
            int width = getWidth();
            int height = getHeight();
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            Toast cheatSheet = Toast.makeText(context, this.mTab.getContentDescription(), 0);
            cheatSheet.setGravity(49, (screenPos[0] + (width / 2)) - (screenWidth / 2), height);
            cheatSheet.show();
            return true;
        }

        public Tab getTab() {
            return this.mTab;
        }

        public TextView getTextView() {
            return this.mTextView;
        }

        public ImageView getIconView() {
            return this.mIconView;
        }

        public View getCustomView() {
            return this.mCustomView;
        }

        protected void insertOrAppendView(View view, boolean insertPreferentially) {
            int i = 0;
            int i2 = -1;
            if (this.mTab.getTag() == null || !"show-icon-right".equals(this.mTab.getTag().toString())) {
                if (!insertPreferentially) {
                    i2 = 0;
                }
                addView(view, i2);
                return;
            }
            if (!insertPreferentially) {
                i = -1;
            }
            addView(view, i);
        }
    }

    protected class VisibilityAnimListener implements AnimatorListener {
        private boolean mCanceled;
        private int mFinalVisibility;

        protected VisibilityAnimListener() {
            this.mCanceled = false;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.widget.ScrollingTabContainerView.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.widget.ScrollingTabContainerView.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.ScrollingTabContainerView.<clinit>():void");
    }

    public ScrollingTabContainerView(Context context) {
        super(context);
        this.mVisAnimListener = new VisibilityAnimListener();
        this.mLastPos = 0;
        this.mTabClicked = false;
        this.mShouldAnimToTab = true;
        setHorizontalScrollBarEnabled(false);
        ActionBarPolicy abp = ActionBarPolicy.get(context);
        setContentHeight(abp.getTabContainerHeight());
        this.mStackedTabMaxWidth = abp.getStackedTabMaxWidth();
        this.mTabLayout = createTabLayout();
        addView(this.mTabLayout, new ViewGroup.LayoutParams(-2, -1));
    }

    public void setShouldAnimToTab(boolean shouldAnim) {
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean lockedExpanded;
        boolean canCollapse;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == EditorInfo.IME_FLAG_NO_ENTER_ACTION) {
            lockedExpanded = true;
        } else {
            lockedExpanded = false;
        }
        setFillViewport(lockedExpanded);
        int childCount = this.mTabLayout.getChildCount();
        if (childCount <= 1 || !(widthMode == EditorInfo.IME_FLAG_NO_ENTER_ACTION || widthMode == RtlSpacingHelper.UNDEFINED)) {
            this.mMaxTabWidth = -1;
        } else {
            if (childCount > 2) {
                this.mMaxTabWidth = (int) (((float) MeasureSpec.getSize(widthMeasureSpec)) * 0.4f);
            } else {
                this.mMaxTabWidth = MeasureSpec.getSize(widthMeasureSpec) / 2;
            }
            this.mMaxTabWidth = Math.min(this.mMaxTabWidth, this.mStackedTabMaxWidth);
        }
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(this.mContentHeight, EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        if (lockedExpanded) {
            canCollapse = false;
        } else {
            canCollapse = this.mAllowCollapse;
        }
        if (canCollapse) {
            this.mTabLayout.measure(0, heightMeasureSpec);
            if (this.mTabLayout.getMeasuredWidth() > MeasureSpec.getSize(widthMeasureSpec)) {
                performCollapse();
            } else {
                performExpand();
            }
        } else {
            performExpand();
        }
        int oldWidth = getMeasuredWidth();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
        for (int i = 0; i < tabCount; i++) {
            boolean isSelected;
            View child = this.mTabLayout.getChildAt(i);
            if (i == position) {
                isSelected = true;
            } else {
                isSelected = false;
            }
            child.setSelected(isSelected);
            if (isSelected) {
                animateToTab(position);
            }
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
        LinearLayout tabLayout = new LinearLayout(getContext(), null, R.attr.actionBarTabBarStyle);
        tabLayout.setMeasureWithLargestChildEnabled(true);
        tabLayout.setGravity(17);
        tabLayout.setLayoutParams(new LayoutParams(-2, -1));
        return tabLayout;
    }

    private Spinner createSpinner() {
        Spinner spinner = new Spinner(getContext(), null, R.attr.actionDropDownStyle);
        spinner.setLayoutParams(new LayoutParams(-2, -1));
        spinner.setOnItemClickListenerInt(this);
        return spinner;
    }

    protected void onConfigurationChanged(Configuration newConfig) {
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
            ObjectAnimator anim = ObjectAnimator.ofFloat(this, "alpha", new float[]{WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL});
            anim.setDuration(200);
            anim.setInterpolator(sAlphaInterpolator);
            anim.addListener(this.mVisAnimListener.withFinalVisibility(visibility));
            anim.start();
            return;
        }
        anim = ObjectAnimator.ofFloat(this, "alpha", new float[]{0.0f});
        anim.setDuration(200);
        anim.setInterpolator(sAlphaInterpolator);
        anim.addListener(this.mVisAnimListener.withFinalVisibility(visibility));
        anim.start();
    }

    public void animateToTab(int position) {
        if (this.mShouldAnimToTab) {
            View tabView = this.mTabLayout.getChildAt(position);
            if (this.mTabSelector != null) {
                removeCallbacks(this.mTabSelector);
            }
            this.mTabSelector = new AnonymousClass1(tabView);
            post(this.mTabSelector);
        }
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

    private TabView createTabView(Context context, Tab tab, boolean forAdapter) {
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
        handleTabViewCreated(tabView);
        return tabView;
    }

    public void addTab(Tab tab, boolean setSelected) {
        TabView tabView = createTabView(this.mContext, tab, false);
        this.mTabLayout.addView((View) tabView, new LayoutParams(0, -1, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL));
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

    public void addTab(Tab tab, int position, boolean setSelected) {
        TabView tabView = createTabView(this.mContext, tab, false);
        this.mTabLayout.addView((View) tabView, position, new LayoutParams(0, -1, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL));
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

    public boolean isTabClicked() {
        return this.mTabClicked;
    }

    public int getLastPos() {
        return this.mLastPos;
    }

    protected LinearLayout getTabLayout() {
        return this.mTabLayout;
    }

    protected int getSelectedTabIndex() {
        return this.mSelectedTabIndex;
    }

    protected void setMaxTabWidth(int width) {
        this.mMaxTabWidth = width;
    }

    protected void handleTabClicked(int position) {
    }

    protected boolean disableMaxTabWidth() {
        return false;
    }

    protected void handleTabViewCreated(TabView view) {
    }

    protected void initTitleAppearance(TextView textView) {
    }

    protected int adjustPadding(int availableWidth, int itemPaddingSize) {
        return itemPaddingSize;
    }

    public void updateTabViewContainerWidth(Context context) {
    }
}
