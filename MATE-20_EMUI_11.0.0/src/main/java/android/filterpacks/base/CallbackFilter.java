package android.filterpacks.base;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.GenerateFinalPort;
import android.os.Handler;
import android.os.Looper;

public class CallbackFilter extends Filter {
    @GenerateFinalPort(hasDefault = true, name = "callUiThread")
    private boolean mCallbacksOnUiThread = true;
    @GenerateFieldPort(hasDefault = true, name = "listener")
    private FilterContext.OnFrameReceivedListener mListener;
    private Handler mUiThreadHandler;
    @GenerateFieldPort(hasDefault = true, name = "userData")
    private Object mUserData;

    private class CallbackRunnable implements Runnable {
        private Filter mFilter;
        private Frame mFrame;
        private FilterContext.OnFrameReceivedListener mListener;
        private Object mUserData;

        public CallbackRunnable(FilterContext.OnFrameReceivedListener listener, Filter filter, Frame frame, Object userData) {
            this.mListener = listener;
            this.mFilter = filter;
            this.mFrame = frame;
            this.mUserData = userData;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.mListener.onFrameReceived(this.mFilter, this.mFrame, this.mUserData);
            this.mFrame.release();
        }
    }

    public CallbackFilter(String name) {
        super(name);
    }

    @Override // android.filterfw.core.Filter
    public void setupPorts() {
        addInputPort("frame");
    }

    @Override // android.filterfw.core.Filter
    public void prepare(FilterContext context) {
        if (this.mCallbacksOnUiThread) {
            this.mUiThreadHandler = new Handler(Looper.getMainLooper());
        }
    }

    @Override // android.filterfw.core.Filter
    public void process(FilterContext context) {
        Frame input = pullInput("frame");
        FilterContext.OnFrameReceivedListener onFrameReceivedListener = this.mListener;
        if (onFrameReceivedListener == null) {
            throw new RuntimeException("CallbackFilter received frame, but no listener set!");
        } else if (this.mCallbacksOnUiThread) {
            input.retain();
            if (!this.mUiThreadHandler.post(new CallbackRunnable(this.mListener, this, input, this.mUserData))) {
                throw new RuntimeException("Unable to send callback to UI thread!");
            }
        } else {
            onFrameReceivedListener.onFrameReceived(this, input, this.mUserData);
        }
    }
}
