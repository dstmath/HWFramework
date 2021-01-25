package ohos.media.recorderimpl.adapter;

import android.app.ActivityThread;

public class RecorderServiceAdapter {
    public static String getPackageName() {
        String currentPackageName = ActivityThread.currentPackageName();
        return currentPackageName == null ? "" : currentPackageName;
    }

    public static String getOpPackageName() {
        String currentOpPackageName = ActivityThread.currentOpPackageName();
        return currentOpPackageName == null ? "" : currentOpPackageName;
    }
}
