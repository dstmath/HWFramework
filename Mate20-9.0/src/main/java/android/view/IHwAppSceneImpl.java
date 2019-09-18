package android.view;

import android.app.Activity;
import android.content.Context;

public interface IHwAppSceneImpl {
    public static final int PAUSE_ACTIVITY_FLAG = 1;
    public static final int RESUME_ACTIVITY_FLAG = 0;

    void changeActivityState(int i, Activity activity);

    void handleWindowFocusChanged(Context context);
}
