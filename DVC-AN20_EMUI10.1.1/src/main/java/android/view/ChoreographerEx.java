package android.view;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ChoreographerEx {
    private ChoreographerEx() {
    }

    @HwSystemApi
    public static void scheduleFrameNow(View view) {
        if (view != null && view.mAttachInfo != null && view.mAttachInfo.mViewRootImpl != null) {
            view.mAttachInfo.mViewRootImpl.mChoreographer.scheduleFrameNow();
        }
    }
}
