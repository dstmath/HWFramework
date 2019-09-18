package android.widget.sr;

import android.graphics.drawable.Drawable;

public interface HwAISRImageViewTaskManager {
    public static final int DDK_COMMAND_SUCCESS = 0;
    public static final int DDK_ERROR_AI_EEXIST = 16;
    public static final int DDK_ERROR_AI_EIO = 5;
    public static final int DDK_ERROR_AI_ENODEV = 18;
    public static final int DDK_ERROR_AI_ENOENT = 2;
    public static final int DDK_ERROR_AI_ENOMODEL = 34;

    void addMemory(int i);

    boolean enoughRoomForSize(int i);

    void postCancelTask(SRTaskInfo sRTaskInfo);

    SRTaskInfo postNewTask(SRTaskCallback sRTaskCallback, Drawable drawable);

    void removeMemory(int i);
}
