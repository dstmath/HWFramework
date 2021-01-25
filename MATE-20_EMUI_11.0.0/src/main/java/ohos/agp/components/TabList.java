package ohos.agp.components;

import java.util.ArrayList;
import java.util.List;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.element.Element;
import ohos.agp.database.Publisher;
import ohos.agp.styles.Style;
import ohos.agp.styles.Value;
import ohos.agp.styles.attributes.TabAttrsConstants;
import ohos.agp.styles.attributes.TabLayoutAttrsConstants;
import ohos.agp.utils.Color;
import ohos.agp.view.ColorStateList;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class TabList extends ScrollView {
    public static final int INDICATOR_BOTTOM_LINE = 1;
    public static final int INDICATOR_INVISIBLE = 0;
    public static final int INDICATOR_LEFT_LINE = 2;
    public static final int INDICATOR_OVAL = 3;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "TabLayout");
    final TabListSelectionHandler mEventHandler;
    private ComponentContainer.LayoutConfig mTabLayoutParams;
    private ColorStateList mTabTextColors;
    private List<Tab> mTabs;

    public interface TabSelectedListener {
        void onTabReselected(Tab tab);

        void onTabSelected(Tab tab);

        void onTabUnselected(Tab tab);
    }

    private native long nativeGetTabLayoutHandle();

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native long nativeGetTabLayoutTabHandle();

    private native void nativeTabLayoutAddTab(long j, Tab tab, int i);

    private native boolean nativeTabLayoutGetFixedMode(long j);

    private native int nativeTabLayoutGetIndicatorType(long j);

    private native int nativeTabLayoutGetOrientation(long j);

    private native int nativeTabLayoutGetSelectedIndicatorColor(long j);

    private native int nativeTabLayoutGetSelectedIndicatorHeight(long j);

    private native int nativeTabLayoutGetSelectedTabPosition(long j);

    private native Tab nativeTabLayoutGetTabAt(long j, int i);

    private native int nativeTabLayoutGetTabCount(long j);

    private native void nativeTabLayoutRemoveAllTab(long j);

    private native void nativeTabLayoutRemoveOnSelectedTabCallback(long j);

    private native void nativeTabLayoutRemoveTab(long j, Tab tab);

    private native void nativeTabLayoutSetFixedMode(long j, boolean z);

    private native void nativeTabLayoutSetIndicatorType(long j, int i);

    private native void nativeTabLayoutSetOnSelectedTabCallback(long j, TabListSelectionHandler tabListSelectionHandler);

    private native void nativeTabLayoutSetOrientation(long j, int i);

    private native void nativeTabLayoutSetSelectedIndicatorColor(long j, int i);

    private native void nativeTabLayoutSetSelectedIndicatorHeight(long j, int i);

    private native void nativeTabLayoutSetTabLength(long j, int i);

    private native void nativeTabLayoutSetTabMargin(long j, int i);

    private native void nativeTabLayoutSetTextAlignment(long j, int i);

    private native void nativeTabLayoutSetTextColors(long j, int i, int i2);

    private native void nativeTabLayoutSetTextSize(long j, int i);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native int nativeTabLayoutTabPosition(long j);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nativeTabLayoutTabSelect(long j);

    public class Tab extends Text {
        public Tab(TabList tabList, Context context) {
            this(tabList, context, null);
        }

        public Tab(TabList tabList, Context context, AttrSet attrSet) {
            this(context, attrSet, "TabDefaultStyle");
        }

        public Tab(Context context, AttrSet attrSet, String str) {
            super(context, attrSet, str);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.components.Text, ohos.agp.components.Component
        public Style convertAttrToStyle(AttrSet attrSet) {
            if (this.mAttrsConstants == null) {
                this.mAttrsConstants = new TabAttrsConstants();
            }
            return super.convertAttrToStyle(attrSet);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.components.Text, ohos.agp.components.Component
        public void createNativePtr() {
            if (this.mNativeViewPtr == 0) {
                this.mNativeViewPtr = TabList.this.nativeGetTabLayoutTabHandle();
            }
        }

        @Override // ohos.agp.components.Text, ohos.agp.components.Component
        public void applyStyle(Style style) {
            super.applyStyle(style);
            if (style.hasProperty(TabAttrsConstants.TAB_ICON)) {
                setIcon(style.getPropertyValue(TabAttrsConstants.TAB_ICON).asElement());
            }
        }

        public Tab setIcon(Element element) {
            Element[] aroundElements = getAroundElements();
            setAroundElements(element, aroundElements[1], aroundElements[2], aroundElements[3]);
            return this;
        }

        public Element getIcon() {
            return getAroundElements()[0];
        }

        public Tab select() {
            TabList.this.nativeTabLayoutTabSelect(this.mNativeViewPtr);
            return this;
        }

        public int getPosition() {
            return TabList.this.nativeTabLayoutTabPosition(this.mNativeViewPtr);
        }
    }

    protected class TabListSelectionHandler extends Publisher<TabSelectedListener> {
        protected TabListSelectionHandler() {
        }

        /* access modifiers changed from: package-private */
        public int getListenersCount() {
            return this.mSubscribers.size();
        }

        /* access modifiers changed from: package-private */
        public void onTabSelectionChanged(int i, int i2) {
            if (this.mSubscribers.size() == 0) {
                HiLog.error(TabList.TAG, "mObservers.size() = 0", new Object[0]);
            } else if (i == i2) {
                Tab tabAt = TabList.this.getTabAt(i);
                for (TabSelectedListener tabSelectedListener : this.mSubscribers) {
                    tabSelectedListener.onTabReselected(tabAt);
                }
            } else {
                if (i2 >= 0) {
                    Tab tabAt2 = TabList.this.getTabAt(i2);
                    for (TabSelectedListener tabSelectedListener2 : this.mSubscribers) {
                        tabSelectedListener2.onTabUnselected(tabAt2);
                    }
                }
                if (i >= 0) {
                    Tab tabAt3 = TabList.this.getTabAt(i);
                    for (TabSelectedListener tabSelectedListener3 : this.mSubscribers) {
                        tabSelectedListener3.onTabSelected(tabAt3);
                    }
                }
            }
        }
    }

    public TabList(Context context) {
        this(context, null);
    }

    public TabList(Context context, AttrSet attrSet) {
        this(context, attrSet, "TabLayoutDefaultStyle");
    }

    public TabList(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mEventHandler = new TabListSelectionHandler();
        this.mTabs = new ArrayList();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ScrollView, ohos.agp.components.StackLayout, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new TabLayoutAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ScrollView, ohos.agp.components.StackLayout, ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetTabLayoutHandle();
        }
    }

    private ComponentContainer.LayoutConfig initLayoutParams() {
        ComponentContainer.LayoutConfig layoutConfig = getLayoutConfig();
        return layoutConfig == null ? new ComponentContainer.LayoutConfig() : layoutConfig;
    }

    @Override // ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
        int value = Color.BLACK.getValue();
        if (this.mTabTextColors == null) {
            this.mTabTextColors = createColorStateList(value, value);
        }
        if (style.hasProperty("selected_text_color")) {
            setTabTextColors(this.mTabTextColors.getColorForState(new int[]{0}, value), style.getPropertyValue("selected_text_color").asColor().asArgbInt());
        }
        if (style.hasProperty("normal_text_color")) {
            setTabTextColors(style.getPropertyValue("normal_text_color").asColor().asArgbInt(), this.mTabTextColors.getColorForState(new int[]{4}, value));
        }
        if (style.hasProperty("tab_margin") || style.hasProperty("tab_length")) {
            Value propertyValue = style.getPropertyValue("tab_margin");
            Value propertyValue2 = style.getPropertyValue("tab_length");
            int asInteger = propertyValue != null ? propertyValue.asInteger() : 0;
            int asInteger2 = propertyValue2 != null ? propertyValue2.asInteger() : 0;
            this.mTabLayoutParams = initLayoutParams();
            if (getOrientation() == 0) {
                ComponentContainer.LayoutConfig layoutConfig = this.mTabLayoutParams;
                layoutConfig.width = asInteger2;
                layoutConfig.setMargins(asInteger, 0, asInteger, 0);
            } else {
                ComponentContainer.LayoutConfig layoutConfig2 = this.mTabLayoutParams;
                layoutConfig2.height = asInteger2;
                layoutConfig2.setMargins(0, asInteger, 0, asInteger);
            }
            for (int i = 0; i < getTabCount(); i++) {
                Tab tabAt = getTabAt(i);
                if (tabAt != null) {
                    tabAt.setLayoutConfig(this.mTabLayoutParams);
                }
            }
        }
    }

    public void addTab(Tab tab) {
        if (tab == null) {
            HiLog.error(TAG, "tab is null", new Object[0]);
            return;
        }
        ComponentContainer.LayoutConfig layoutConfig = this.mTabLayoutParams;
        if (layoutConfig != null) {
            tab.setLayoutConfig(layoutConfig);
        }
        this.mTabs.add(tab);
        nativeTabLayoutAddTab(this.mNativeViewPtr, tab, -1);
    }

    public void addTab(Tab tab, int i) {
        if (tab == null) {
            HiLog.error(TAG, "tab is null", new Object[0]);
            return;
        }
        ComponentContainer.LayoutConfig layoutConfig = this.mTabLayoutParams;
        if (layoutConfig != null) {
            tab.setLayoutConfig(layoutConfig);
        }
        Tab selectedTab = getSelectedTab();
        this.mTabs.set(i, tab);
        nativeTabLayoutAddTab(this.mNativeViewPtr, tab, i);
        if (selectedTab != null) {
            selectedTab.select();
        }
    }

    public void addTab(Tab tab, boolean z) {
        if (tab == null) {
            HiLog.error(TAG, "tab is null", new Object[0]);
            return;
        }
        addTab(tab);
        if (z) {
            tab.select();
        }
    }

    public void addTab(Tab tab, int i, boolean z) {
        if (tab == null) {
            HiLog.error(TAG, "tab is null", new Object[0]);
            return;
        }
        ComponentContainer.LayoutConfig layoutConfig = this.mTabLayoutParams;
        if (layoutConfig != null) {
            tab.setLayoutConfig(layoutConfig);
        }
        this.mTabs.add(i, tab);
        nativeTabLayoutAddTab(this.mNativeViewPtr, tab, i);
        if (z) {
            tab.select();
        }
    }

    public void removeTab(Tab tab) {
        if (tab == null) {
            HiLog.error(TAG, "tab is null", new Object[0]);
            return;
        }
        this.mTabs.remove(tab);
        nativeTabLayoutRemoveTab(this.mNativeViewPtr, tab);
    }

    public void removeTab(int i) {
        this.mTabs.remove(i);
        removeTab(getTabAt(i));
    }

    @Override // ohos.agp.components.ComponentContainer
    public void removeAllComponents() {
        this.mTabs.clear();
        nativeTabLayoutRemoveAllTab(this.mNativeViewPtr);
    }

    public Tab getTabAt(int i) {
        if (i < 0) {
            return null;
        }
        return nativeTabLayoutGetTabAt(this.mNativeViewPtr, i);
    }

    public void selectTab(Tab tab) {
        tab.select();
    }

    public Tab getSelectedTab() {
        return getTabAt(getSelectedTabPosition());
    }

    public int getSelectedTabPosition() {
        return nativeTabLayoutGetSelectedTabPosition(this.mNativeViewPtr);
    }

    public int getTabCount() {
        return nativeTabLayoutGetTabCount(this.mNativeViewPtr);
    }

    private ColorStateList createColorStateList(int i, int i2) {
        return new ColorStateList(new int[][]{new int[]{0}, new int[]{4}}, new int[]{i, i2});
    }

    public void setTabTextColors(int i, int i2) {
        this.mTabTextColors = createColorStateList(i, i2);
        nativeTabLayoutSetTextColors(this.mNativeViewPtr, i, i2);
    }

    public void setTabTextSize(int i) {
        nativeTabLayoutSetTextSize(this.mNativeViewPtr, i);
    }

    public void setTabTextAlignment(int i) {
        nativeTabLayoutSetTextAlignment(this.mNativeViewPtr, i);
    }

    public void setTabLength(int i) {
        nativeTabLayoutSetTabLength(this.mNativeViewPtr, i);
    }

    public void setTabMargin(int i) {
        nativeTabLayoutSetTabMargin(this.mNativeViewPtr, i);
    }

    public void setFixedMode(boolean z) {
        nativeTabLayoutSetFixedMode(this.mNativeViewPtr, z);
    }

    public boolean isFixedMode() {
        return nativeTabLayoutGetFixedMode(this.mNativeViewPtr);
    }

    public void setSelectedTabIndicatorColor(int i) {
        nativeTabLayoutSetSelectedIndicatorColor(this.mNativeViewPtr, i);
    }

    public int getSelectedTabIndicatorColor() {
        return nativeTabLayoutGetSelectedIndicatorColor(this.mNativeViewPtr);
    }

    public void setSelectedTabIndicatorHeight(int i) {
        nativeTabLayoutSetSelectedIndicatorHeight(this.mNativeViewPtr, i);
    }

    public int getSelectedTabIndicatorHeight() {
        return nativeTabLayoutGetSelectedIndicatorHeight(this.mNativeViewPtr);
    }

    public void setOrientation(int i) {
        nativeTabLayoutSetOrientation(this.mNativeViewPtr, i);
    }

    public int getOrientation() {
        return nativeTabLayoutGetOrientation(this.mNativeViewPtr);
    }

    public void setIndicatorType(int i) {
        nativeTabLayoutSetIndicatorType(this.mNativeViewPtr, i);
    }

    public int getIndicatorType() {
        return nativeTabLayoutGetIndicatorType(this.mNativeViewPtr);
    }

    public TabListSelectionHandler getSelectionHandler() {
        return this.mEventHandler;
    }

    public void addTabSelectedListener(TabSelectedListener tabSelectedListener) {
        if (this.mEventHandler.getListenersCount() == 0) {
            nativeTabLayoutSetOnSelectedTabCallback(this.mNativeViewPtr, this.mEventHandler);
        }
        this.mEventHandler.registerSubscriber(tabSelectedListener);
    }

    public void removeTabSelectedListener(TabSelectedListener tabSelectedListener) {
        this.mEventHandler.unregisterSubscriber((TabListSelectionHandler) tabSelectedListener);
        if (this.mEventHandler.getListenersCount() == 0) {
            nativeTabLayoutRemoveOnSelectedTabCallback(this.mNativeViewPtr);
        }
    }
}
