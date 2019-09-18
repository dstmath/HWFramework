package android.app;

import android.common.HwFrameworkFactory;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;

public class HwCustSplitActivityImpl extends HwCustSplitActivity {
    private Activity mActivity = null;
    private boolean mInited = false;
    private IHwActivitySplitterImpl mSplitter = null;
    private IBinder mToken = null;

    /* access modifiers changed from: protected */
    public boolean isRequestSplit(Activity a) {
        if (!this.mInited) {
            this.mActivity = a;
            this.mSplitter = HwFrameworkFactory.getHwActivitySplitterImpl(this.mActivity, false);
            this.mInited = true;
        }
        if (this.mSplitter != null) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void initSplitMode(IBinder token) {
        this.mToken = token;
        this.mSplitter.setActivityInfo(this.mActivity, this.mToken);
        if (this.mSplitter.needSplitActivity()) {
            this.mSplitter.splitActivityIfNeeded();
        } else if (this.mSplitter.isSplitMode()) {
            this.mSplitter.reduceActivity();
        }
    }

    /* access modifiers changed from: protected */
    public void onSplitActivityNewIntent(Intent intent) {
        this.mSplitter.onSplitActivityNewIntent(intent);
    }

    /* access modifiers changed from: protected */
    public void onSplitActivityConfigurationChanged(Configuration newConfig) {
        if (this.mSplitter.isSplitBaseActivity() || this.mSplitter.needSplitActivity()) {
            this.mSplitter.onSplitActivityConfigurationChanged(newConfig);
        }
    }

    /* access modifiers changed from: protected */
    public void handleBackPressed() {
        if (this.mSplitter.isSplitSecondActivity()) {
            this.mSplitter.handleBackPressed();
        }
    }

    /* access modifiers changed from: protected */
    public boolean handleStartSplitActivity(Intent intent) {
        if (this.mSplitter.isDuplicateSplittableActivity(intent)) {
            return true;
        }
        this.mSplitter.setSplittableIfNeeded(intent);
        return false;
    }

    /* access modifiers changed from: protected */
    public void setSplitActivityOrientation(int requestedOrientation) {
        this.mSplitter.setSplitActivityOrientation(requestedOrientation);
    }

    /* access modifiers changed from: protected */
    public void onSplitActivityResume() {
        if (this.mSplitter.isSplitBaseActivity() || this.mSplitter.isSplitSecondActivity()) {
            this.mSplitter.setResumed(true);
        }
    }

    /* access modifiers changed from: protected */
    public void onSplitActivityPaused() {
        if (this.mSplitter.isSplitBaseActivity() || this.mSplitter.isSplitSecondActivity()) {
            this.mSplitter.setResumed(false);
        }
    }

    /* access modifiers changed from: protected */
    public void onSplitActivityStop() {
        if (this.mSplitter.isSplitSecondActivity() && this.mSplitter.notSupportSplit()) {
            this.mSplitter.clearIllegalSplitActivity();
        }
    }

    /* access modifiers changed from: protected */
    public void onSplitActivityRestart() {
        this.mSplitter.onSplitActivityRestart();
    }

    /* access modifiers changed from: protected */
    public void splitActivityFinish() {
        this.mSplitter.splitActivityFinish();
    }

    /* access modifiers changed from: protected */
    public void onSplitActivityDestroy() {
        this.mSplitter.onSplitActivityDestroy();
    }
}
