package android.view;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemProperties;
import android.util.Slog;

public class HwCustRenderThreadMonitorImpl extends HwCustRenderThreadMonitor {
    private static final int MESSAGE_RENDER_DESTROY = 1;
    private static final int MESSAGE_RENDER_SYNC_AND_DRAW_FRAME = 0;
    private static final int RENDER_THREAD_TIMEOUT = SystemProperties.getInt("ro.sysui.renderthread_timeout", 3000);
    private static final String TAG = "HwCustRenderThreadMonitor";
    private static HandlerThread mHandlerThread;
    private static Handler mSubHandler;
    private static int[] msgArray = new int[]{0, MESSAGE_RENDER_DESTROY};

    private final class RenderMonitorHandler extends Handler {
        public RenderMonitorHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                case HwCustRenderThreadMonitorImpl.MESSAGE_RENDER_DESTROY /*1*/:
                    if (HwCustRenderThreadMonitorImpl.this.mContext != null) {
                        Process.killProcess(Process.myPid());
                        Slog.e(HwCustRenderThreadMonitorImpl.TAG, "RenderThreader busy type : " + msg.what + ", Restart " + HwCustRenderThreadMonitorImpl.this.mContext.getPackageName());
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public HwCustRenderThreadMonitorImpl(Context context) {
        super(context);
        if (mHandlerThread == null) {
            mHandlerThread = new HandlerThread(TAG);
            mHandlerThread.start();
        }
        if (mSubHandler == null) {
            mSubHandler = new RenderMonitorHandler(mHandlerThread.getLooper());
        }
    }

    protected void renderMonitorStart(int sceneId) {
        mSubHandler.sendMessageDelayed(mSubHandler.obtainMessage(msgArray[sceneId]), (long) RENDER_THREAD_TIMEOUT);
    }

    protected void renderMonitorStop(int sceneId) {
        mSubHandler.removeMessages(msgArray[sceneId]);
    }
}
