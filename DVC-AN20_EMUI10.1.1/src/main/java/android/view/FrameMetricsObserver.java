package android.view;

import android.annotation.UnsupportedAppUsage;
import android.os.Looper;
import android.os.MessageQueue;
import android.view.Window;
import com.android.internal.util.VirtualRefBasePtr;
import java.lang.ref.WeakReference;

public class FrameMetricsObserver {
    @UnsupportedAppUsage
    private FrameMetrics mFrameMetrics;
    Window.OnFrameMetricsAvailableListener mListener;
    @UnsupportedAppUsage
    private MessageQueue mMessageQueue;
    public VirtualRefBasePtr mNative;
    private WeakReference<Window> mWindow;

    FrameMetricsObserver(Window window, Looper looper, Window.OnFrameMetricsAvailableListener listener) {
        if (looper != null) {
            this.mMessageQueue = looper.getQueue();
            if (this.mMessageQueue != null) {
                this.mFrameMetrics = new FrameMetrics();
                this.mWindow = new WeakReference<>(window);
                this.mListener = listener;
                return;
            }
            throw new IllegalStateException("invalid looper, null message queue\n");
        }
        throw new NullPointerException("looper cannot be null");
    }

    @UnsupportedAppUsage
    private void notifyDataAvailable(int dropCount) {
        Window window = this.mWindow.get();
        if (window != null) {
            this.mListener.onFrameMetricsAvailable(window, this.mFrameMetrics, dropCount);
        }
    }
}
