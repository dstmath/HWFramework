package android.rms.iaware;

import android.common.HwPartIawareFactory;
import android.content.Context;
import android.view.InputEvent;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class HwDynBufManager {
    private static final String TAG = "HwDynBufManager";
    private static IDynBufManager sInstance = null;

    public interface IDynBufManager {
        void beginFling(boolean z, int i);

        boolean canAddVsync();

        void endFling(boolean z, int i);

        int getTargetBufCount();

        void init(Context context);

        void initFrameInterval(long j);

        void notifyInputEvent(InputEvent inputEvent);

        void onVsync();

        void updateMultiViews();

        long updateSplineTime(boolean z, long j, int i);

        void updateSurfaceBufCount(int i);

        void updateSurfaceTexture();
    }

    public static IDynBufManager getImpl() {
        IDynBufManager iDynBufManager = sInstance;
        if (iDynBufManager != null) {
            return iDynBufManager;
        }
        sInstance = HwPartIawareFactory.loadFactory(HwPartIawareFactory.IAWARE_FACTORY_IMPL_NAME).getDynBufManagerImpl();
        return sInstance;
    }
}
