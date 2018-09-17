package android.app;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;

public class HwCustActivity {
    protected boolean isRequestSplit(Activity a) {
        return false;
    }

    protected void initSplitMode(IBinder token) {
    }

    protected void onSplitActivityNewIntent(Intent intent) {
    }

    protected void onSplitActivityConfigurationChanged(Configuration newConfig) {
    }

    protected void handleBackPressed() {
    }

    protected boolean handleStartSplitActivity(Intent intent) {
        return false;
    }

    protected void setSplitActivityOrientation(int requestedOrientation) {
    }

    protected void onSplitActivityResume() {
    }

    protected void onSplitActivityPaused() {
    }

    protected void onSplitActivityStop() {
    }

    protected void onSplitActivityRestart() {
    }

    protected void splitActivityFinish() {
    }

    protected void onSplitActivityDestroy() {
    }
}
