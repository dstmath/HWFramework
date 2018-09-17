package android.filterpacks.base;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.FilterContext.OnFrameReceivedListener;
import android.filterfw.core.Frame;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.GenerateFinalPort;
import android.os.Handler;
import android.os.Looper;

public class CallbackFilter extends Filter {
    @GenerateFinalPort(hasDefault = true, name = "callUiThread")
    private boolean mCallbacksOnUiThread = true;
    @GenerateFieldPort(hasDefault = true, name = "listener")
    private OnFrameReceivedListener mListener;
    private Handler mUiThreadHandler;
    @GenerateFieldPort(hasDefault = true, name = "userData")
    private Object mUserData;

    private class CallbackRunnable implements Runnable {
        private Filter mFilter;
        private Frame mFrame;
        private OnFrameReceivedListener mListener;
        private Object mUserData;

        public CallbackRunnable(OnFrameReceivedListener listener, Filter filter, Frame frame, Object userData) {
            this.mListener = listener;
            this.mFilter = filter;
            this.mFrame = frame;
            this.mUserData = userData;
        }

        public void run() {
            this.mListener.onFrameReceived(this.mFilter, this.mFrame, this.mUserData);
            this.mFrame.release();
        }
    }

    public CallbackFilter(String name) {
        super(name);
    }

    public void setupPorts() {
        addInputPort("frame");
    }

    public void prepare(FilterContext context) {
        if (this.mCallbacksOnUiThread) {
            this.mUiThreadHandler = new Handler(Looper.getMainLooper());
        }
    }

    public void process(FilterContext context) {
        Frame input = pullInput("frame");
        if (this.mListener == null) {
            throw new RuntimeException("CallbackFilter received frame, but no listener set!");
        } else if (this.mCallbacksOnUiThread) {
            input.retain();
            if (!this.mUiThreadHandler.post(new CallbackRunnable(this.mListener, this, input, this.mUserData))) {
                throw new RuntimeException("Unable to send callback to UI thread!");
            }
        } else {
            this.mListener.onFrameReceived(this, input, this.mUserData);
        }
    }
}
