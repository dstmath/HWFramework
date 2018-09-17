package android.view;

import android.os.Looper;
import android.os.MessageQueue;
import android.view.Window.OnFrameMetricsAvailableListener;
import com.android.internal.util.VirtualRefBasePtr;
import java.lang.ref.WeakReference;

public class FrameMetricsObserver {
    private FrameMetrics mFrameMetrics;
    OnFrameMetricsAvailableListener mListener;
    private MessageQueue mMessageQueue;
    VirtualRefBasePtr mNative;
    private WeakReference<Window> mWindow;

    FrameMetricsObserver(Window window, Looper looper, OnFrameMetricsAvailableListener listener) {
        if (looper == null) {
            throw new NullPointerException("looper cannot be null");
        }
        this.mMessageQueue = looper.getQueue();
        if (this.mMessageQueue == null) {
            throw new IllegalStateException("invalid looper, null message queue\n");
        }
        this.mFrameMetrics = new FrameMetrics();
        this.mWindow = new WeakReference(window);
        this.mListener = listener;
    }

    private void notifyDataAvailable(int dropCount) {
        Window window = (Window) this.mWindow.get();
        if (window != null) {
            this.mListener.onFrameMetricsAvailable(window, this.mFrameMetrics, dropCount);
        }
    }
}
