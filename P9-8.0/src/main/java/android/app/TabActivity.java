package android.app;

import android.R;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

@Deprecated
public class TabActivity extends ActivityGroup {
    private String mDefaultTab = null;
    private int mDefaultTabIndex = -1;
    private TabHost mTabHost;

    public void setDefaultTab(String tag) {
        this.mDefaultTab = tag;
        this.mDefaultTabIndex = -1;
    }

    public void setDefaultTab(int index) {
        this.mDefaultTab = null;
        this.mDefaultTabIndex = index;
    }

    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        ensureTabHost();
        String cur = state.getString("currentTab");
        if (cur != null) {
            this.mTabHost.setCurrentTabByTag(cur);
        }
        if (this.mTabHost.getCurrentTab() >= 0) {
            return;
        }
        if (this.mDefaultTab != null) {
            this.mTabHost.setCurrentTabByTag(this.mDefaultTab);
        } else if (this.mDefaultTabIndex >= 0) {
            this.mTabHost.setCurrentTab(this.mDefaultTabIndex);
        }
    }

    protected void onPostCreate(Bundle icicle) {
        super.onPostCreate(icicle);
        ensureTabHost();
        if (this.mTabHost.getCurrentTab() == -1) {
            this.mTabHost.setCurrentTab(0);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String currentTabTag = this.mTabHost.getCurrentTabTag();
        if (currentTabTag != null) {
            outState.putString("currentTab", currentTabTag);
        }
    }

    public void onContentChanged() {
        super.onContentChanged();
        this.mTabHost = (TabHost) findViewById(R.id.tabhost);
        if (this.mTabHost == null) {
            throw new RuntimeException("Your content must have a TabHost whose id attribute is 'android.R.id.tabhost'");
        }
        this.mTabHost.setup(getLocalActivityManager());
    }

    private void ensureTabHost() {
        if (this.mTabHost == null) {
            setContentView(17367281);
        }
    }

    protected void onChildTitleChanged(Activity childActivity, CharSequence title) {
        if (getLocalActivityManager().getCurrentActivity() == childActivity) {
            View tabView = this.mTabHost.getCurrentTabView();
            if (tabView != null && (tabView instanceof TextView)) {
                ((TextView) tabView).setText(title);
            }
        }
    }

    public TabHost getTabHost() {
        ensureTabHost();
        return this.mTabHost;
    }

    public TabWidget getTabWidget() {
        return this.mTabHost.getTabWidget();
    }
}
