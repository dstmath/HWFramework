package android.app;

import android.common.HwFrameworkFactory;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;

public class HwCustActivityImpl extends HwCustActivity {
    private Activity mActivity = null;
    private boolean mInited = false;
    private IHwActivitySplitterImpl mSplitter = null;
    private IBinder mToken = null;

    protected boolean isRequestSplit(Activity a) {
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

    protected void initSplitMode(IBinder token) {
        this.mToken = token;
        this.mSplitter.setActivityInfo(this.mActivity, this.mToken);
        if (this.mSplitter.needSplitActivity()) {
            this.mSplitter.splitActivityIfNeeded();
        } else if (this.mSplitter.isSplitMode()) {
            this.mSplitter.reduceActivity();
        }
    }

    protected void onSplitActivityNewIntent(Intent intent) {
        this.mSplitter.onSplitActivityNewIntent(intent);
    }

    protected void onSplitActivityConfigurationChanged(Configuration newConfig) {
        if (this.mSplitter.isSplitBaseActivity() || this.mSplitter.needSplitActivity()) {
            this.mSplitter.onSplitActivityConfigurationChanged(newConfig);
        }
    }

    protected void handleBackPressed() {
        if (this.mSplitter.isSplitSecondActivity()) {
            this.mSplitter.handleBackPressed();
        }
    }

    protected boolean handleStartSplitActivity(Intent intent) {
        if (this.mSplitter.isDuplicateSplittableActivity(intent)) {
            return true;
        }
        this.mSplitter.setSplittableIfNeeded(intent);
        return false;
    }

    protected void setSplitActivityOrientation(int requestedOrientation) {
        this.mSplitter.setSplitActivityOrientation(requestedOrientation);
    }

    protected void onSplitActivityResume() {
        if (this.mSplitter.isSplitBaseActivity() || this.mSplitter.isSplitSecondActivity()) {
            this.mSplitter.setResumed(true);
        }
    }

    protected void onSplitActivityPaused() {
        if (this.mSplitter.isSplitBaseActivity() || this.mSplitter.isSplitSecondActivity()) {
            this.mSplitter.setResumed(false);
        }
    }

    protected void onSplitActivityStop() {
        if (this.mSplitter.isSplitSecondActivity() && this.mSplitter.notSupportSplit()) {
            this.mSplitter.clearIllegalSplitActivity();
        }
    }

    protected void onSplitActivityRestart() {
        this.mSplitter.onSplitActivityRestart();
    }

    protected void splitActivityFinish() {
        this.mSplitter.splitActivityFinish();
    }

    protected void onSplitActivityDestroy() {
        if (this.mSplitter.isSplitBaseActivity() || this.mSplitter.isSplitSecondActivity()) {
            this.mSplitter.onSplitActivityDestroy();
        }
    }
}
