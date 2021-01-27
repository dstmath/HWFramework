package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.TabWidget;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.List;

public class TabHost extends FrameLayout implements ViewTreeObserver.OnTouchModeChangeListener {
    private static final int TABWIDGET_LOCATION_BOTTOM = 3;
    private static final int TABWIDGET_LOCATION_LEFT = 0;
    private static final int TABWIDGET_LOCATION_RIGHT = 2;
    private static final int TABWIDGET_LOCATION_TOP = 1;
    @UnsupportedAppUsage
    protected int mCurrentTab;
    private View mCurrentView;
    protected LocalActivityManager mLocalActivityManager;
    @UnsupportedAppUsage
    private OnTabChangeListener mOnTabChangeListener;
    private FrameLayout mTabContent;
    private View.OnKeyListener mTabKeyListener;
    private int mTabLayoutId;
    @UnsupportedAppUsage
    private List<TabSpec> mTabSpecs;
    private TabWidget mTabWidget;

    /* access modifiers changed from: private */
    public interface ContentStrategy {
        View getContentView();

        void tabClosed();
    }

    /* access modifiers changed from: private */
    public interface IndicatorStrategy {
        View createIndicatorView();
    }

    public interface OnTabChangeListener {
        void onTabChanged(String str);
    }

    public interface TabContentFactory {
        View createTabContent(String str);
    }

    public TabHost(Context context) {
        super(context);
        this.mTabSpecs = new ArrayList(2);
        this.mCurrentTab = -1;
        this.mCurrentView = null;
        this.mLocalActivityManager = null;
        initTabHost();
    }

    public TabHost(Context context, AttributeSet attrs) {
        this(context, attrs, 16842883);
    }

    public TabHost(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TabHost(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs);
        this.mTabSpecs = new ArrayList(2);
        this.mCurrentTab = -1;
        this.mCurrentView = null;
        this.mLocalActivityManager = null;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabWidget, defStyleAttr, defStyleRes);
        saveAttributeDataForStyleable(context, R.styleable.TabWidget, attrs, a, defStyleAttr, defStyleRes);
        this.mTabLayoutId = a.getResourceId(4, 0);
        a.recycle();
        if (this.mTabLayoutId == 0) {
            this.mTabLayoutId = R.layout.tab_indicator_holo;
        }
        initTabHost();
    }

    private void initTabHost() {
        setFocusableInTouchMode(true);
        setDescendantFocusability(262144);
        this.mCurrentTab = -1;
        this.mCurrentView = null;
    }

    public TabSpec newTabSpec(String tag) {
        if (tag != null) {
            return new TabSpec(tag);
        }
        throw new IllegalArgumentException("tag must be non-null");
    }

    public void setup() {
        this.mTabWidget = (TabWidget) findViewById(16908307);
        if (this.mTabWidget != null) {
            this.mTabKeyListener = new View.OnKeyListener() {
                /* class android.widget.TabHost.AnonymousClass1 */

                @Override // android.view.View.OnKeyListener
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (!(KeyEvent.isModifierKey(keyCode) || keyCode == 61 || keyCode == 62 || keyCode == 66)) {
                        switch (keyCode) {
                            case 19:
                            case 20:
                            case 21:
                            case 22:
                            case 23:
                                break;
                            default:
                                TabHost.this.mTabContent.requestFocus(2);
                                return TabHost.this.mTabContent.dispatchKeyEvent(event);
                        }
                    }
                    return false;
                }
            };
            this.mTabWidget.setTabSelectionListener(new TabWidget.OnTabSelectionChanged() {
                /* class android.widget.TabHost.AnonymousClass2 */

                @Override // android.widget.TabWidget.OnTabSelectionChanged
                public void onTabSelectionChanged(int tabIndex, boolean clicked) {
                    TabHost.this.setCurrentTab(tabIndex);
                    if (clicked) {
                        TabHost.this.mTabContent.requestFocus(2);
                    }
                }
            });
            this.mTabContent = (FrameLayout) findViewById(16908305);
            if (this.mTabContent == null) {
                throw new RuntimeException("Your TabHost must have a FrameLayout whose id attribute is 'android.R.id.tabcontent'");
            }
            return;
        }
        throw new RuntimeException("Your TabHost must have a TabWidget whose id attribute is 'android.R.id.tabs'");
    }

    @Override // android.view.View
    public void sendAccessibilityEventInternal(int eventType) {
    }

    public void setup(LocalActivityManager activityGroup) {
        setup();
        this.mLocalActivityManager = activityGroup;
    }

    @Override // android.view.ViewTreeObserver.OnTouchModeChangeListener
    public void onTouchModeChanged(boolean isInTouchMode) {
    }

    public void addTab(TabSpec tabSpec) {
        if (tabSpec.mIndicatorStrategy == null) {
            throw new IllegalArgumentException("you must specify a way to create the tab indicator.");
        } else if (tabSpec.mContentStrategy != null) {
            View tabIndicator = tabSpec.mIndicatorStrategy.createIndicatorView();
            tabIndicator.setOnKeyListener(this.mTabKeyListener);
            if (tabSpec.mIndicatorStrategy instanceof ViewIndicatorStrategy) {
                this.mTabWidget.setStripEnabled(false);
            }
            this.mTabWidget.addView(tabIndicator);
            this.mTabSpecs.add(tabSpec);
            if (this.mCurrentTab == -1) {
                setCurrentTab(0);
            }
            addHwTab(tabIndicator);
        } else {
            throw new IllegalArgumentException("you must specify a way to create the tab content");
        }
    }

    public void clearAllTabs() {
        this.mTabWidget.removeAllViews();
        initTabHost();
        this.mTabContent.removeAllViews();
        this.mTabSpecs.clear();
        requestLayout();
        invalidate();
    }

    public TabWidget getTabWidget() {
        return this.mTabWidget;
    }

    public int getCurrentTab() {
        return this.mCurrentTab;
    }

    public String getCurrentTabTag() {
        int i = this.mCurrentTab;
        if (i < 0 || i >= this.mTabSpecs.size()) {
            return null;
        }
        return this.mTabSpecs.get(this.mCurrentTab).getTag();
    }

    public View getCurrentTabView() {
        int i = this.mCurrentTab;
        if (i < 0 || i >= this.mTabSpecs.size()) {
            return null;
        }
        return this.mTabWidget.getChildTabViewAt(this.mCurrentTab);
    }

    public View getCurrentView() {
        return this.mCurrentView;
    }

    public void setCurrentTabByTag(String tag) {
        int count = this.mTabSpecs.size();
        for (int i = 0; i < count; i++) {
            if (this.mTabSpecs.get(i).getTag().equals(tag)) {
                setCurrentTab(i);
                return;
            }
        }
    }

    public FrameLayout getTabContentView() {
        return this.mTabContent;
    }

    private int getTabWidgetLocation() {
        int location;
        int location2 = 1;
        if (this.mTabWidget.getOrientation() != 1) {
            if (this.mTabContent.getTop() < this.mTabWidget.getTop()) {
                location2 = 3;
            }
            return location2;
        }
        if (this.mTabContent.getLeft() < this.mTabWidget.getLeft()) {
            location = 2;
        } else {
            location = 0;
        }
        return location;
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent event) {
        View view;
        int soundEffect;
        int directionShouldChangeFocus;
        int keyCodeShouldChangeFocus;
        boolean handled = super.dispatchKeyEvent(event);
        if (!handled && event.getAction() == 0 && (view = this.mCurrentView) != null && view.isRootNamespace() && this.mCurrentView.hasFocus()) {
            int tabWidgetLocation = getTabWidgetLocation();
            if (tabWidgetLocation == 0) {
                keyCodeShouldChangeFocus = 21;
                directionShouldChangeFocus = 17;
                soundEffect = 1;
            } else if (tabWidgetLocation == 2) {
                keyCodeShouldChangeFocus = 22;
                directionShouldChangeFocus = 66;
                soundEffect = 3;
            } else if (tabWidgetLocation != 3) {
                keyCodeShouldChangeFocus = 19;
                directionShouldChangeFocus = 33;
                soundEffect = 2;
            } else {
                keyCodeShouldChangeFocus = 20;
                directionShouldChangeFocus = 130;
                soundEffect = 4;
            }
            if (event.getKeyCode() == keyCodeShouldChangeFocus && this.mCurrentView.findFocus() != null && this.mCurrentView.findFocus().focusSearch(directionShouldChangeFocus) == null) {
                this.mTabWidget.getChildTabViewAt(this.mCurrentTab).requestFocus();
                playSoundEffect(soundEffect);
                return true;
            }
        }
        return handled;
    }

    @Override // android.view.ViewGroup, android.view.View
    public void dispatchWindowFocusChanged(boolean hasFocus) {
        View view = this.mCurrentView;
        if (view != null) {
            view.dispatchWindowFocusChanged(hasFocus);
        }
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public CharSequence getAccessibilityClassName() {
        return TabHost.class.getName();
    }

    public void setCurrentTab(int index) {
        int i;
        if (index >= 0 && index < this.mTabSpecs.size() && index != (i = this.mCurrentTab)) {
            if (i != -1) {
                this.mTabSpecs.get(i).mContentStrategy.tabClosed();
            }
            this.mCurrentTab = index;
            this.mTabWidget.focusCurrentTab(this.mCurrentTab);
            this.mCurrentView = this.mTabSpecs.get(index).mContentStrategy.getContentView();
            if (this.mCurrentView.getParent() == null) {
                this.mTabContent.addView(this.mCurrentView, new ViewGroup.LayoutParams(-1, -1));
            }
            if (!this.mTabWidget.hasFocus()) {
                this.mCurrentView.requestFocus();
            }
            invokeOnTabChangeListener();
        }
    }

    public void setOnTabChangedListener(OnTabChangeListener l) {
        this.mOnTabChangeListener = l;
    }

    private void invokeOnTabChangeListener() {
        OnTabChangeListener onTabChangeListener = this.mOnTabChangeListener;
        if (onTabChangeListener != null) {
            onTabChangeListener.onTabChanged(getCurrentTabTag());
        }
    }

    public class TabSpec {
        @UnsupportedAppUsage
        private ContentStrategy mContentStrategy;
        @UnsupportedAppUsage
        private IndicatorStrategy mIndicatorStrategy;
        private final String mTag;

        private TabSpec(String tag) {
            this.mTag = tag;
        }

        public TabSpec setIndicator(CharSequence label) {
            this.mIndicatorStrategy = new LabelIndicatorStrategy(label);
            return this;
        }

        public TabSpec setIndicator(CharSequence label, Drawable icon) {
            this.mIndicatorStrategy = new LabelAndIconIndicatorStrategy(label, icon);
            return this;
        }

        public TabSpec setIndicator(View view) {
            this.mIndicatorStrategy = new ViewIndicatorStrategy(view);
            return this;
        }

        public TabSpec setContent(int viewId) {
            this.mContentStrategy = new ViewIdContentStrategy(viewId);
            return this;
        }

        public TabSpec setContent(TabContentFactory contentFactory) {
            this.mContentStrategy = new FactoryContentStrategy(this.mTag, contentFactory);
            return this;
        }

        public TabSpec setContent(Intent intent) {
            this.mContentStrategy = new IntentContentStrategy(this.mTag, intent);
            return this;
        }

        public String getTag() {
            return this.mTag;
        }
    }

    private class LabelIndicatorStrategy implements IndicatorStrategy {
        private final CharSequence mLabel;

        private LabelIndicatorStrategy(CharSequence label) {
            this.mLabel = label;
        }

        @Override // android.widget.TabHost.IndicatorStrategy
        public View createIndicatorView() {
            Context context = TabHost.this.getContext();
            View tabIndicator = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(TabHost.this.mTabLayoutId, (ViewGroup) TabHost.this.mTabWidget, false);
            TextView tv = (TextView) tabIndicator.findViewById(16908310);
            tv.setText(this.mLabel);
            if (context.getApplicationInfo().targetSdkVersion <= 4) {
                tabIndicator.setBackgroundResource(R.drawable.tab_indicator_v4);
                tv.setTextColor(context.getColorStateList(R.color.tab_indicator_text_v4));
            }
            return tabIndicator;
        }
    }

    private class LabelAndIconIndicatorStrategy implements IndicatorStrategy {
        private final Drawable mIcon;
        private final CharSequence mLabel;

        private LabelAndIconIndicatorStrategy(CharSequence label, Drawable icon) {
            this.mLabel = label;
            this.mIcon = icon;
        }

        @Override // android.widget.TabHost.IndicatorStrategy
        public View createIndicatorView() {
            Drawable drawable;
            Context context = TabHost.this.getContext();
            View tabIndicator = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(TabHost.this.mTabLayoutId, (ViewGroup) TabHost.this.mTabWidget, false);
            TextView tv = (TextView) tabIndicator.findViewById(16908310);
            ImageView iconView = (ImageView) tabIndicator.findViewById(16908294);
            boolean bindIcon = true;
            if ((iconView.getVisibility() == 8) && !TextUtils.isEmpty(this.mLabel)) {
                bindIcon = false;
            }
            tv.setText(this.mLabel);
            if (bindIcon && (drawable = this.mIcon) != null) {
                iconView.setImageDrawable(drawable);
                iconView.setVisibility(0);
            }
            if (context.getApplicationInfo().targetSdkVersion <= 4) {
                tabIndicator.setBackgroundResource(R.drawable.tab_indicator_v4);
                tv.setTextColor(context.getColorStateList(R.color.tab_indicator_text_v4));
            }
            return tabIndicator;
        }
    }

    private class ViewIndicatorStrategy implements IndicatorStrategy {
        private final View mView;

        private ViewIndicatorStrategy(View view) {
            this.mView = view;
        }

        @Override // android.widget.TabHost.IndicatorStrategy
        public View createIndicatorView() {
            return this.mView;
        }
    }

    private class ViewIdContentStrategy implements ContentStrategy {
        private final View mView;

        private ViewIdContentStrategy(int viewId) {
            this.mView = TabHost.this.mTabContent.findViewById(viewId);
            View view = this.mView;
            if (view != null) {
                view.setVisibility(8);
                return;
            }
            throw new RuntimeException("Could not create tab content because could not find view with id " + viewId);
        }

        @Override // android.widget.TabHost.ContentStrategy
        public View getContentView() {
            this.mView.setVisibility(0);
            return this.mView;
        }

        @Override // android.widget.TabHost.ContentStrategy
        public void tabClosed() {
            this.mView.setVisibility(8);
        }
    }

    private class FactoryContentStrategy implements ContentStrategy {
        private TabContentFactory mFactory;
        private View mTabContent;
        private final CharSequence mTag;

        public FactoryContentStrategy(CharSequence tag, TabContentFactory factory) {
            this.mTag = tag;
            this.mFactory = factory;
        }

        @Override // android.widget.TabHost.ContentStrategy
        public View getContentView() {
            if (this.mTabContent == null) {
                this.mTabContent = this.mFactory.createTabContent(this.mTag.toString());
            }
            this.mTabContent.setVisibility(0);
            return this.mTabContent;
        }

        @Override // android.widget.TabHost.ContentStrategy
        public void tabClosed() {
            this.mTabContent.setVisibility(8);
        }
    }

    private class IntentContentStrategy implements ContentStrategy {
        private final Intent mIntent;
        private View mLaunchedView;
        private final String mTag;

        private IntentContentStrategy(String tag, Intent intent) {
            this.mTag = tag;
            this.mIntent = intent;
        }

        @Override // android.widget.TabHost.ContentStrategy
        @UnsupportedAppUsage
        public View getContentView() {
            if (TabHost.this.mLocalActivityManager != null) {
                Window w = TabHost.this.mLocalActivityManager.startActivity(this.mTag, this.mIntent);
                View wd = w != null ? w.getDecorView() : null;
                View view = this.mLaunchedView;
                if (!(view == wd || view == null || view.getParent() == null)) {
                    TabHost.this.mTabContent.removeView(this.mLaunchedView);
                }
                this.mLaunchedView = wd;
                View view2 = this.mLaunchedView;
                if (view2 != null) {
                    view2.setVisibility(0);
                    this.mLaunchedView.setFocusableInTouchMode(true);
                    ((ViewGroup) this.mLaunchedView).setDescendantFocusability(262144);
                }
                return this.mLaunchedView;
            }
            throw new IllegalStateException("Did you forget to call 'public void setup(LocalActivityManager activityGroup)'?");
        }

        @Override // android.widget.TabHost.ContentStrategy
        @UnsupportedAppUsage
        public void tabClosed() {
            View view = this.mLaunchedView;
            if (view != null) {
                view.setVisibility(8);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void addHwTab(View tabIndicator) {
    }
}
