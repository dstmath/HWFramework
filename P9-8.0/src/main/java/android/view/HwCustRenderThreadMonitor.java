package android.view;

import android.content.Context;
import android.util.HwPCUtils;
import com.android.internal.util.ArrayUtils;

public class HwCustRenderThreadMonitor {
    public static final int DESTROY_SCENE_ID = 1;
    private static final String[] MONITOR_PACKAGE_LIST = new String[]{HwPCUtils.PKG_PHONE_SYSTEMUI};
    public static final int SYNC_AND_DRAW_FRAME_SCENE_ID = 0;
    public final Context mContext;

    public HwCustRenderThreadMonitor(Context context) {
        this.mContext = context;
    }

    protected void renderMonitorStart(int sceneId) {
    }

    protected void renderMonitorStop(int sceneId) {
    }

    public static boolean shouldStartMonitot(Context context) {
        if (context == null) {
            return false;
        }
        return ArrayUtils.contains(MONITOR_PACKAGE_LIST, context.getPackageName());
    }
}
