package android.widget.sr;

import android.graphics.Bitmap;

public interface SRTaskCallback {
    SRTaskInfo getCurrentSRTask();

    void onSRTaskFail(SRTaskInfo sRTaskInfo);

    void onSRTaskSuccess(SRTaskInfo sRTaskInfo, Bitmap bitmap);
}
