package android.widget;

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
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnTouchModeChangeListener;
import android.view.Window;
import com.android.internal.R;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.Protocol;
import com.hisi.perfhub.PerfHub;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.pgmng.log.LogPower;
import java.util.ArrayList;
import java.util.List;

public class TabHost extends FrameLayout implements OnTouchModeChangeListener {
    private static final int TABWIDGET_LOCATION_BOTTOM = 3;
    private static final int TABWIDGET_LOCATION_LEFT = 0;
    private static final int TABWIDGET_LOCATION_RIGHT = 2;
    private static final int TABWIDGET_LOCATION_TOP = 1;
    protected int mCurrentTab;
    private View mCurrentView;
    protected LocalActivityManager mLocalActivityManager;
    private OnTabChangeListener mOnTabChangeListener;
    private FrameLayout mTabContent;
    private OnKeyListener mTabKeyListener;
    private int mTabLayoutId;
    private List<TabSpec> mTabSpecs;
    private TabWidget mTabWidget;

    private interface ContentStrategy {
        View getContentView();

        void tabClosed();
    }

    private class FactoryContentStrategy implements ContentStrategy {
        private TabContentFactory mFactory;
        private View mTabContent;
        private final CharSequence mTag;

        public FactoryContentStrategy(CharSequence tag, TabContentFactory factory) {
            this.mTag = tag;
            this.mFactory = factory;
        }

        public View getContentView() {
            if (this.mTabContent == null) {
                this.mTabContent = this.mFactory.createTabContent(this.mTag.toString());
            }
            this.mTabContent.setVisibility(TabHost.TABWIDGET_LOCATION_LEFT);
            return this.mTabContent;
        }

        public void tabClosed() {
            this.mTabContent.setVisibility(8);
        }
    }

    private interface IndicatorStrategy {
        View createIndicatorView();
    }

    private class IntentContentStrategy implements ContentStrategy {
        private final Intent mIntent;
        private View mLaunchedView;
        private final String mTag;

        private IntentContentStrategy(String tag, Intent intent) {
            this.mTag = tag;
            this.mIntent = intent;
        }

        public View getContentView() {
            if (TabHost.this.mLocalActivityManager == null) {
                throw new IllegalStateException("Did you forget to call 'public void setup(LocalActivityManager activityGroup)'?");
            }
            Window w = TabHost.this.mLocalActivityManager.startActivity(this.mTag, this.mIntent);
            View decorView = w != null ? w.getDecorView() : null;
            if (!(this.mLaunchedView == decorView || this.mLaunchedView == null || this.mLaunchedView.getParent() == null)) {
                TabHost.this.mTabContent.removeView(this.mLaunchedView);
            }
            this.mLaunchedView = decorView;
            if (this.mLaunchedView != null) {
                this.mLaunchedView.setVisibility(TabHost.TABWIDGET_LOCATION_LEFT);
                this.mLaunchedView.setFocusableInTouchMode(true);
                ((ViewGroup) this.mLaunchedView).setDescendantFocusability(Protocol.BASE_DATA_CONNECTION);
            }
            return this.mLaunchedView;
        }

        public void tabClosed() {
            if (this.mLaunchedView != null) {
                this.mLaunchedView.setVisibility(8);
            }
        }
    }

    private class LabelAndIconIndicatorStrategy implements IndicatorStrategy {
        private final Drawable mIcon;
        private final CharSequence mLabel;

        private LabelAndIconIndicatorStrategy(CharSequence label, Drawable icon) {
            this.mLabel = label;
            this.mIcon = icon;
        }

        public View createIndicatorView() {
            boolean exclusive;
            Context context = TabHost.this.getContext();
            View tabIndicator = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(TabHost.this.mTabLayoutId, TabHost.this.mTabWidget, false);
            TextView tv = (TextView) tabIndicator.findViewById(R.id.title);
            ImageView iconView = (ImageView) tabIndicator.findViewById(R.id.icon);
            if (iconView.getVisibility() == 8) {
                exclusive = true;
            } else {
                exclusive = false;
            }
            boolean isEmpty = exclusive ? TextUtils.isEmpty(this.mLabel) : true;
            tv.setText(this.mLabel);
            if (isEmpty && this.mIcon != null) {
                iconView.setImageDrawable(this.mIcon);
                iconView.setVisibility(TabHost.TABWIDGET_LOCATION_LEFT);
            }
            if (context.getApplicationInfo().targetSdkVersion <= 4) {
                tabIndicator.setBackgroundResource(R.drawable.tab_indicator_v4);
                tv.setTextColor(context.getColorStateList(R.color.tab_indicator_text_v4));
            }
            return tabIndicator;
        }
    }

    private class LabelIndicatorStrategy implements IndicatorStrategy {
        private final CharSequence mLabel;

        private LabelIndicatorStrategy(CharSequence label) {
            this.mLabel = label;
        }

        public View createIndicatorView() {
            Context context = TabHost.this.getContext();
            View tabIndicator = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(TabHost.this.mTabLayoutId, TabHost.this.mTabWidget, false);
            TextView tv = (TextView) tabIndicator.findViewById(R.id.title);
            tv.setText(this.mLabel);
            if (context.getApplicationInfo().targetSdkVersion <= 4) {
                tabIndicator.setBackgroundResource(R.drawable.tab_indicator_v4);
                tv.setTextColor(context.getColorStateList(R.color.tab_indicator_text_v4));
            }
            return tabIndicator;
        }
    }

    public interface OnTabChangeListener {
        void onTabChanged(String str);
    }

    public interface TabContentFactory {
        View createTabContent(String str);
    }

    public class TabSpec {
        private ContentStrategy mContentStrategy;
        private IndicatorStrategy mIndicatorStrategy;
        private String mTag;

        private TabSpec(String tag) {
            this.mTag = tag;
        }

        public TabSpec setIndicator(CharSequence label) {
            this.mIndicatorStrategy = new LabelIndicatorStrategy(label, null);
            return this;
        }

        public TabSpec setIndicator(CharSequence label, Drawable icon) {
            this.mIndicatorStrategy = new LabelAndIconIndicatorStrategy(label, icon, null);
            return this;
        }

        public TabSpec setIndicator(View view) {
            this.mIndicatorStrategy = new ViewIndicatorStrategy(view, null);
            return this;
        }

        public TabSpec setContent(int viewId) {
            this.mContentStrategy = new ViewIdContentStrategy(viewId, null);
            return this;
        }

        public TabSpec setContent(TabContentFactory contentFactory) {
            this.mContentStrategy = new FactoryContentStrategy(this.mTag, contentFactory);
            return this;
        }

        public TabSpec setContent(Intent intent) {
            this.mContentStrategy = new IntentContentStrategy(this.mTag, intent, null);
            return this;
        }

        public String getTag() {
            return this.mTag;
        }
    }

    private class ViewIdContentStrategy implements ContentStrategy {
        private final View mView;

        private ViewIdContentStrategy(int viewId) {
            this.mView = TabHost.this.mTabContent.findViewById(viewId);
            if (this.mView != null) {
                this.mView.setVisibility(8);
                return;
            }
            throw new RuntimeException("Could not create tab content because could not find view with id " + viewId);
        }

        public View getContentView() {
            this.mView.setVisibility(TabHost.TABWIDGET_LOCATION_LEFT);
            return this.mView;
        }

        public void tabClosed() {
            this.mView.setVisibility(8);
        }
    }

    private class ViewIndicatorStrategy implements IndicatorStrategy {
        private final View mView;

        private ViewIndicatorStrategy(View view) {
            this.mView = view;
        }

        public View createIndicatorView() {
            return this.mView;
        }
    }

    public TabHost(Context context) {
        super(context);
        this.mTabSpecs = new ArrayList(TABWIDGET_LOCATION_RIGHT);
        this.mCurrentTab = -1;
        this.mCurrentView = null;
        this.mLocalActivityManager = null;
        initTabHost();
    }

    public TabHost(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.tabWidgetStyle);
    }

    public TabHost(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, TABWIDGET_LOCATION_LEFT);
    }

    public TabHost(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs);
        this.mTabSpecs = new ArrayList(TABWIDGET_LOCATION_RIGHT);
        this.mCurrentTab = -1;
        this.mCurrentView = null;
        this.mLocalActivityManager = null;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabWidget, defStyleAttr, defStyleRes);
        this.mTabLayoutId = a.getResourceId(4, TABWIDGET_LOCATION_LEFT);
        a.recycle();
        if (this.mTabLayoutId == 0) {
            this.mTabLayoutId = R.layout.tab_indicator_holo;
        }
        initTabHost();
    }

    private void initTabHost() {
        setFocusableInTouchMode(true);
        setDescendantFocusability(Protocol.BASE_DATA_CONNECTION);
        this.mCurrentTab = -1;
        this.mCurrentView = null;
    }

    public TabSpec newTabSpec(String tag) {
        return new TabSpec(tag, null);
    }

    public void setup() {
        this.mTabWidget = (TabWidget) findViewById(R.id.tabs);
        if (this.mTabWidget == null) {
            throw new RuntimeException("Your TabHost must have a TabWidget whose id attribute is 'android.R.id.tabs'");
        }
        this.mTabKeyListener = new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case PerfHub.PERF_TAG_IPA_SUSTAINABLE_POWER /*19*/:
                    case HwPerformance.PERF_TAG_TASK_FORK_ON_B_CLUSTER /*20*/:
                    case HwPerformance.PERF_TAG_DEF_L_CPU_MIN /*21*/:
                    case HwPerformance.PERF_TAG_DEF_L_CPU_MAX /*22*/:
                    case HwPerformance.PERF_TAG_DEF_B_CPU_MIN /*23*/:
                    case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                        return false;
                    default:
                        TabHost.this.mTabContent.requestFocus(TabHost.TABWIDGET_LOCATION_RIGHT);
                        return TabHost.this.mTabContent.dispatchKeyEvent(event);
                }
            }
        };
        this.mTabWidget.setTabSelectionListener(new OnTabSelectionChanged() {
            public void onTabSelectionChanged(int tabIndex, boolean clicked) {
                TabHost.this.setCurrentTab(tabIndex);
                if (clicked) {
                    TabHost.this.mTabContent.requestFocus(TabHost.TABWIDGET_LOCATION_RIGHT);
                }
            }
        });
        this.mTabContent = (FrameLayout) findViewById(R.id.tabcontent);
        if (this.mTabContent == null) {
            throw new RuntimeException("Your TabHost must have a FrameLayout whose id attribute is 'android.R.id.tabcontent'");
        }
    }

    public void sendAccessibilityEventInternal(int eventType) {
    }

    public void setup(LocalActivityManager activityGroup) {
        setup();
        this.mLocalActivityManager = activityGroup;
    }

    public void onTouchModeChanged(boolean isInTouchMode) {
    }

    public void addTab(TabSpec tabSpec) {
        if (tabSpec.mIndicatorStrategy == null) {
            throw new IllegalArgumentException("you must specify a way to create the tab indicator.");
        } else if (tabSpec.mContentStrategy == null) {
            throw new IllegalArgumentException("you must specify a way to create the tab content");
        } else {
            View tabIndicator = tabSpec.mIndicatorStrategy.createIndicatorView();
            tabIndicator.setOnKeyListener(this.mTabKeyListener);
            if (tabSpec.mIndicatorStrategy instanceof ViewIndicatorStrategy) {
                this.mTabWidget.setStripEnabled(false);
            }
            this.mTabWidget.addView(tabIndicator);
            this.mTabSpecs.add(tabSpec);
            if (this.mCurrentTab == -1) {
                setCurrentTab(TABWIDGET_LOCATION_LEFT);
            }
            addHwTab(tabIndicator);
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
        if (this.mCurrentTab < 0 || this.mCurrentTab >= this.mTabSpecs.size()) {
            return null;
        }
        return ((TabSpec) this.mTabSpecs.get(this.mCurrentTab)).getTag();
    }

    public View getCurrentTabView() {
        if (this.mCurrentTab < 0 || this.mCurrentTab >= this.mTabSpecs.size()) {
            return null;
        }
        return this.mTabWidget.getChildTabViewAt(this.mCurrentTab);
    }

    public View getCurrentView() {
        return this.mCurrentView;
    }

    public void setCurrentTabByTag(String tag) {
        for (int i = TABWIDGET_LOCATION_LEFT; i < this.mTabSpecs.size(); i += TABWIDGET_LOCATION_TOP) {
            if (((TabSpec) this.mTabSpecs.get(i)).getTag().equals(tag)) {
                setCurrentTab(i);
                return;
            }
        }
    }

    public FrameLayout getTabContentView() {
        return this.mTabContent;
    }

    private int getTabWidgetLocation() {
        switch (this.mTabWidget.getOrientation()) {
            case TABWIDGET_LOCATION_TOP /*1*/:
                if (this.mTabContent.getLeft() < this.mTabWidget.getLeft()) {
                    return TABWIDGET_LOCATION_RIGHT;
                }
                return TABWIDGET_LOCATION_LEFT;
            default:
                if (this.mTabContent.getTop() < this.mTabWidget.getTop()) {
                    return TABWIDGET_LOCATION_BOTTOM;
                }
                return TABWIDGET_LOCATION_TOP;
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled = super.dispatchKeyEvent(event);
        if (!handled && event.getAction() == 0 && this.mCurrentView != null && this.mCurrentView.isRootNamespace() && this.mCurrentView.hasFocus()) {
            int keyCodeShouldChangeFocus;
            int soundEffect;
            int directionShouldChangeFocus;
            switch (getTabWidgetLocation()) {
                case TABWIDGET_LOCATION_LEFT /*0*/:
                    keyCodeShouldChangeFocus = 21;
                    directionShouldChangeFocus = 17;
                    soundEffect = TABWIDGET_LOCATION_TOP;
                    break;
                case TABWIDGET_LOCATION_RIGHT /*2*/:
                    keyCodeShouldChangeFocus = 22;
                    directionShouldChangeFocus = 66;
                    soundEffect = TABWIDGET_LOCATION_BOTTOM;
                    break;
                case TABWIDGET_LOCATION_BOTTOM /*3*/:
                    keyCodeShouldChangeFocus = 20;
                    directionShouldChangeFocus = LogPower.END_CHG_ROTATION;
                    soundEffect = 4;
                    break;
                default:
                    keyCodeShouldChangeFocus = 19;
                    directionShouldChangeFocus = 33;
                    soundEffect = TABWIDGET_LOCATION_RIGHT;
                    break;
            }
            if (event.getKeyCode() == keyCodeShouldChangeFocus && this.mCurrentView.findFocus() != null && this.mCurrentView.findFocus().focusSearch(directionShouldChangeFocus) == null) {
                this.mTabWidget.getChildTabViewAt(this.mCurrentTab).requestFocus();
                playSoundEffect(soundEffect);
                return true;
            }
        }
        return handled;
    }

    public void dispatchWindowFocusChanged(boolean hasFocus) {
        if (this.mCurrentView != null) {
            this.mCurrentView.dispatchWindowFocusChanged(hasFocus);
        }
    }

    public CharSequence getAccessibilityClassName() {
        return TabHost.class.getName();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setCurrentTab(int index) {
        if (index >= 0 && index < this.mTabSpecs.size() && index != this.mCurrentTab) {
            if (this.mCurrentTab != -1) {
                ((TabSpec) this.mTabSpecs.get(this.mCurrentTab)).mContentStrategy.tabClosed();
            }
            this.mCurrentTab = index;
            TabSpec spec = (TabSpec) this.mTabSpecs.get(index);
            this.mTabWidget.focusCurrentTab(this.mCurrentTab);
            this.mCurrentView = spec.mContentStrategy.getContentView();
            if (this.mCurrentView.getParent() == null) {
                this.mTabContent.addView(this.mCurrentView, new LayoutParams(-1, -1));
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
        if (this.mOnTabChangeListener != null) {
            this.mOnTabChangeListener.onTabChanged(getCurrentTabTag());
        }
    }

    protected void addHwTab(View tabIndicator) {
    }
}
