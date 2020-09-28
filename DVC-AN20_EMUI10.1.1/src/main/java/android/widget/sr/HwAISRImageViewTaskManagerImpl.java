package android.widget.sr;

import android.graphics.drawable.Drawable;

public class HwAISRImageViewTaskManagerImpl implements HwAISRImageViewTaskManager {
    private static final int DDK_STATUS_NOT_STARTED = 0;
    private static final int DDK_STATUS_PROCESSING = 4;
    private static final int DDK_STATUS_STARTED = 2;
    private static final int DDK_STATUS_STARTING = 1;
    private static final int DDK_STATUS_STOPPING = 3;
    private static final int HISI_DDK_WECHAT_MODE = 1;
    private static final long KEEP_WAITING_DURATION = 60000;
    private static final int MAX_PROCESS_ERROR_COUNT = 30;
    private static final int MAX_START_ERROR_COUNT = 5;
    private static final int SR_IMAGEVIEW_RATIO = 3;
    private static final String SR_TAG = "SuperResolution";
    private static final long TIMEOUT_DURATION = 3000;

    public SRTaskInfo postNewTask(SRTaskCallback callback, Drawable drawable) {
        return null;
    }

    public void postCancelTask(SRTaskInfo taskInfo) {
    }

    public void addMemory(int size) {
    }

    public void removeMemory(int size) {
    }

    public boolean enoughRoomForSize(int size) {
        return false;
    }
}
