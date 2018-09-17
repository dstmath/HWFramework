package android.app;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.IBinder;
import android.view.View;

public interface IHwActivitySplitterImpl {
    void adjustToFullScreen();

    void adjustToSplitScreen();

    void adjustWindow(int i);

    void adjustWindow(Rect rect);

    void cancelSplit(Intent intent);

    boolean checkAllContentGone();

    void clearIllegalSplitActivity();

    void finishAllSubActivities();

    Intent getCurrentSubIntent();

    void handleBackPressed();

    boolean isControllerShowing();

    boolean isDuplicateSplittableActivity(Intent intent);

    boolean isSplitBaseActivity();

    boolean isSplitMode();

    boolean isSplitSecondActivity();

    boolean needSplitActivity();

    boolean notSupportSplit();

    void onSplitActivityConfigurationChanged(Configuration configuration);

    void onSplitActivityDestroy();

    void onSplitActivityNewIntent(Intent intent);

    void onSplitActivityRestart();

    boolean reachSplitSize();

    void reduceActivity();

    void reduceIndexView();

    void restartLastContentIfNeeded();

    void setActivityInfo(Activity activity, IBinder iBinder);

    void setControllerShowing(boolean z);

    void setFirstIntent(Intent intent);

    void setResumed(boolean z);

    void setSplit();

    void setSplit(float f);

    void setSplit(View view);

    void setSplit(View view, float f);

    void setSplitActivityOrientation(int i);

    void setSplittableIfNeeded(Intent intent);

    void setTargetIntent(Intent intent);

    void splitActivityFinish();

    void splitActivityIfNeeded();
}
